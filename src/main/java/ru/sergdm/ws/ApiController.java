package ru.sergdm.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.sergdm.ws.enums.DeliveryStatuses;
import ru.sergdm.ws.exception.AgendaConflictException;
import ru.sergdm.ws.exception.ResourceNotExpectedException;
import ru.sergdm.ws.exception.ResourceNotFoundException;
import ru.sergdm.ws.exception.WrongStatusException;
import ru.sergdm.ws.model.ConfirmRequest;
import ru.sergdm.ws.model.Delivery;
import ru.sergdm.ws.model.SystemName;
import ru.sergdm.ws.service.DeliveryService;

import javax.validation.Valid;
import java.util.List;

@RestController
public class ApiController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private DeliveryService deliveryService;

	@GetMapping("/")
	public ResponseEntity<Object> name() {
		SystemName name = new SystemName();
		return new ResponseEntity<>(name, HttpStatus.OK);
	}

	@GetMapping("/deliveries")
	public ResponseEntity<List<Delivery>> deliveries(){
		logger.info("deliveries");
		return new ResponseEntity(deliveryService.getDeliveries(), HttpStatus.OK);
	}

	@GetMapping("/deliveries/{deliveryId}")
	public ResponseEntity<?> rest(@PathVariable Long deliveryId){
		logger.info("deliveries. deliveryId = {}", deliveryId);
		try {
			Delivery delivery = deliveryService.getDelivery(deliveryId);
			return new ResponseEntity(delivery, HttpStatus.OK);
		} catch (ResourceNotFoundException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
		}
	}

	@PostMapping("/deliveries")
	public ResponseEntity<?> addDelivery(@Valid @RequestBody Delivery delivery) {
		logger.info("addDelivery. delivery = {}", delivery);
		List<Delivery> deliveries = deliveryService.getDeliveries(delivery.getCourierId());
		if (deliveries.stream().filter(d -> d.getStatus() != DeliveryStatuses.CANCELLED)
				.anyMatch(d -> d.getTimeslot() == delivery.getTimeslot())) {
			return ResponseEntity.status(409).body("Timeslot = " + delivery.getTimeslot() + " already in use");
		}
		Delivery deliveryNew = deliveryService.addDelivery(delivery);
		return ResponseEntity.ok().body(deliveryNew);
	}

	@PostMapping("/confirm")
	public ResponseEntity<?> confirm(@Valid @RequestBody ConfirmRequest request) {
		try {
			deliveryService.confirmDelivery(request.getDeliveryId(), request.getOrderId());
			return ResponseEntity.ok().body(HttpStatus.OK);
		} catch (ResourceNotExpectedException ex) {
			logger.error(ex.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
		} catch (ResourceNotFoundException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
		} catch (WrongStatusException ex) {
			return ResponseEntity.status(409).body(ex.getMessage());
		}
	}

	@PostMapping("/cancel")
	public ResponseEntity<?> cancel(@Valid @RequestBody ConfirmRequest request) {
		try {
			deliveryService.cancelDelivery(request.getDeliveryId(), request.getOrderId());
			return ResponseEntity.ok().body(HttpStatus.OK);
		} catch (ResourceNotExpectedException ex) {
			logger.error(ex.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
		} catch (ResourceNotFoundException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(request);
		} catch (WrongStatusException ex) {
			return ResponseEntity.status(409).body(ex.getMessage());
		}
	}

	@DeleteMapping("/deliveries")
	public ResponseEntity<?> deleteDeliveries(){
		logger.info("Delete all deliveries");
		deliveryService.deleteAll();
		return ResponseEntity.ok().body(HttpStatus.OK);
	}

}
