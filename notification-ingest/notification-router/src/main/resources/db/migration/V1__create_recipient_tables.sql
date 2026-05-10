-- Phase 2: minimal recipient registry schema (SDS §8 "User prefs + device registry").
-- recipient_id is the same key used as the Pulsar message key (per-recipient ordering).

CREATE TABLE IF NOT EXISTS recipient_preferences (
    recipient_id   VARCHAR(128) PRIMARY KEY,
    locale         VARCHAR(16)  NOT NULL DEFAULT 'en-US',
    timezone       VARCHAR(64)  NOT NULL DEFAULT 'UTC',
    push_enabled   BOOLEAN      NOT NULL DEFAULT TRUE,
    email_enabled  BOOLEAN      NOT NULL DEFAULT TRUE,
    email_address  VARCHAR(320)
);

CREATE TABLE IF NOT EXISTS recipient_devices (
    id            BIGSERIAL    PRIMARY KEY,
    recipient_id  VARCHAR(128) NOT NULL,
    platform      VARCHAR(16)  NOT NULL,
    device_token  VARCHAR(512) NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT recipient_devices_recipient_token_uk UNIQUE (recipient_id, device_token)
);

CREATE INDEX IF NOT EXISTS idx_recipient_devices_recipient_active
    ON recipient_devices (recipient_id, active);
