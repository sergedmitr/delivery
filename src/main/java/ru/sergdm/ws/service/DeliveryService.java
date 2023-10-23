package ru.sergdm.ws.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sergdm.ws.enums.DeliveryStatuses;
import ru.sergdm.ws.exception.ResourceNotExpectedException;
import ru.sergdm.ws.exception.ResourceNotFoundException;
import ru.sergdm.ws.exception.WrongStatusException;
import ru.sergdm.ws.model.Delivery;
import ru.sergdm.ws.repository.DeliveryRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class DeliveryService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private DeliveryRepository deliveryRepository;

	public List<Delivery> getDeliveries() {
		List<Delivery> deliveries = new ArrayList<>();
		deliveryRepository.findAll().forEach(deliveries::add);
		logger.info("reserves = {}", deliveries);
		return deliveries;
	}

	public List<Delivery> getDeliveries(String courierId) {
		List<Delivery> deliveries = deliveryRepository.findByCourierId(courierId);
		logger.info("deliveries = {}", deliveries);
		return deliveries;
	}

	public Delivery getDelivery(Long deliveryId) throws ResourceNotFoundException{
		Delivery delivery = deliveryRepository.findById(deliveryId)
				.orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
		logger.info("delivery = {}", delivery);
		return delivery;
	}

	public Delivery addDelivery(Delivery delivery) {
		delivery.setStatus(DeliveryStatuses.CREATED);
		return deliveryRepository.save(delivery);
	}

	public void confirmDelivery(Long deliveryId, Long orderId) throws ResourceNotFoundException, ResourceNotExpectedException,
			WrongStatusException {
		Delivery delivery = deliveryRepository.findById(deliveryId)
				.orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
		if (!delivery.getOrderId().equals(orderId)) {
			throw new ResourceNotExpectedException("Reserve with other orderId");
		}
		if (delivery.getStatus() != DeliveryStatuses.CREATED) {
			throw new WrongStatusException("Wrong status = " + delivery.getStatus() + " for confirm operation");
		}
		delivery.setStatus(DeliveryStatuses.CONFIRMED);
		deliveryRepository.save(delivery);
	}

	public void cancelDelivery(Long deliveryId, Long orderId) throws ResourceNotFoundException, ResourceNotExpectedException,
			WrongStatusException {
		Delivery delivery = deliveryRepository.findById(deliveryId)
				.orElseThrow(() -> new ResourceNotFoundException("Reserve not found"));
		if (!delivery.getOrderId().equals(orderId)) {
			throw new ResourceNotExpectedException("Reserve with other orderId");
		}
		if (delivery.getStatus() != DeliveryStatuses.CREATED) {
			throw new WrongStatusException("Wrong status = " + delivery.getStatus() + " for cancel operation");
		}
		delivery.setStatus(DeliveryStatuses.CANCELLED);
		deliveryRepository.save(delivery);
	}

	public void deleteAll() {
		deliveryRepository.deleteAll();
	}
}
