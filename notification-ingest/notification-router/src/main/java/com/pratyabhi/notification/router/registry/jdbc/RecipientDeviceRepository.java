package com.pratyabhi.notification.router.registry.jdbc;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipientDeviceRepository extends JpaRepository<RecipientDeviceEntity, Long> {

  List<RecipientDeviceEntity> findByRecipientIdAndActiveTrue(String recipientId);
}
