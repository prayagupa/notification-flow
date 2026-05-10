package com.pratyabhi.notification.router.registry.jdbc;

import com.pratyabhi.notification.router.registry.RecipientChannel;
import com.pratyabhi.notification.router.registry.RecipientLookupService;
import com.pratyabhi.notification.router.registry.RecipientProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.pratyabhi.notification.events.v1.Channel;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class JdbcRecipientLookupService implements RecipientLookupService {

  private final RecipientPreferencesRepository preferencesRepo;
  private final RecipientDeviceRepository deviceRepo;

  public JdbcRecipientLookupService(
      RecipientPreferencesRepository preferencesRepo, RecipientDeviceRepository deviceRepo) {
    this.preferencesRepo = preferencesRepo;
    this.deviceRepo = deviceRepo;
  }

  @Override
  public Optional<RecipientProfile> findByRecipientId(String recipientId) {
    var prefsOpt = preferencesRepo.findById(recipientId);
    if (prefsOpt.isEmpty()) {
      return Optional.empty();
    }
    var prefs = prefsOpt.get();
    var devices = deviceRepo.findByRecipientIdAndActiveTrue(recipientId);
    List<RecipientChannel> channels = new ArrayList<>();
    if (prefs.isPushEnabled()) {
      for (var d : devices) {
        Channel c =
            switch (d.getPlatform()) {
              case FCM -> Channel.CHANNEL_PUSH_FCM;
              case APNS -> Channel.CHANNEL_PUSH_APNS;
            };
        channels.add(new RecipientChannel(c, d.getDeviceToken()));
      }
    }
    if (prefs.isEmailEnabled() && prefs.getEmailAddress() != null && !prefs.getEmailAddress().isBlank()) {
      channels.add(new RecipientChannel(Channel.CHANNEL_EMAIL, prefs.getEmailAddress()));
    }
    return Optional.of(
        new RecipientProfile(
            prefs.getRecipientId(),
            prefs.getLocale(),
            prefs.getTimezone(),
            prefs.isPushEnabled(),
            prefs.isEmailEnabled(),
            channels));
  }
}
