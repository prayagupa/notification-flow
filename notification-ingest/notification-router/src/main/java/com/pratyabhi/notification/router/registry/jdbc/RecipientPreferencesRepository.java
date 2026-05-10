package com.pratyabhi.notification.router.registry.jdbc;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipientPreferencesRepository
    extends JpaRepository<RecipientPreferencesEntity, String> {}
