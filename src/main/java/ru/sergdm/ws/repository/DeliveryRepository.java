package ru.sergdm.ws.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import ru.sergdm.ws.model.Delivery;

import java.util.List;

public interface DeliveryRepository extends CrudRepository<Delivery, Long>,
		JpaSpecificationExecutor<Delivery> {
	List<Delivery> findByCourierId(String courierId);
}
