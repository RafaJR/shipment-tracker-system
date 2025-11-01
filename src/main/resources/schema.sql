-- =====================================================
-- Shipment Tracker System - Database Schema
-- =====================================================
-- Description: DDL script to create the database structure
-- Database: H2 (PostgreSQL compatibility mode)
-- Author: Generated for shipment-tracker-system
-- =====================================================

-- Drop table if exists (for clean recreation)
DROP TABLE IF EXISTS trackings CASCADE;

-- =====================================================
-- Table: trackings
-- Description: Stores shipment tracking information and status history
-- =====================================================
CREATE TABLE trackings (
    id BIGSERIAL PRIMARY KEY,
    tracking_id VARCHAR(50) NOT NULL UNIQUE,
    current_status VARCHAR(30) NOT NULL,
    previous_status VARCHAR(30),
    last_location VARCHAR(200),
    carrier VARCHAR(100),
    estimated_delivery TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_checked_at TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_current_status CHECK (current_status IN (
        'PENDING', 'IN_TRANSIT', 'OUT_FOR_DELIVERY',
        'DELIVERED', 'FAILED_DELIVERY', 'RETURNED', 'CANCELLED'
    )),
    CONSTRAINT chk_previous_status CHECK (previous_status IS NULL OR previous_status IN (
        'PENDING', 'IN_TRANSIT', 'OUT_FOR_DELIVERY',
        'DELIVERED', 'FAILED_DELIVERY', 'RETURNED', 'CANCELLED'
    ))
);

-- =====================================================
-- Indexes for performance optimization
-- =====================================================
CREATE INDEX idx_tracking_id ON trackings(tracking_id);
CREATE INDEX idx_current_status ON trackings(current_status);
CREATE INDEX idx_last_checked_at ON trackings(last_checked_at);
CREATE INDEX idx_created_at ON trackings(created_at);

-- =====================================================
-- Comments for documentation
-- =====================================================
COMMENT ON TABLE trackings IS 'Stores shipment tracking information and status changes';
COMMENT ON COLUMN trackings.tracking_id IS 'Unique identifier for the shipment from external API';
COMMENT ON COLUMN trackings.current_status IS 'Current status of the shipment';
COMMENT ON COLUMN trackings.previous_status IS 'Previous status before the last update';
COMMENT ON COLUMN trackings.last_location IS 'Last known location of the shipment';
COMMENT ON COLUMN trackings.carrier IS 'Shipping carrier handling the delivery';
COMMENT ON COLUMN trackings.estimated_delivery IS 'Estimated delivery date and time';
COMMENT ON COLUMN trackings.last_checked_at IS 'Timestamp of last external API check';

-- Drop table if exists (for clean recreation)
DROP TABLE IF EXISTS notifications CASCADE;

-- =====================================================
-- Table: notifications
-- Description: Stores notification records for shipment status changes
-- =====================================================
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    tracking_id VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(500),
    message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    retry_count INTEGER NOT NULL DEFAULT 0,
    error_message TEXT,

    -- Constraints
    CONSTRAINT chk_notification_type CHECK (type IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP')),
    CONSTRAINT chk_notification_status CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'RETRYING'))
);

-- =====================================================
-- Indexes for notifications
-- =====================================================
CREATE INDEX idx_notification_tracking_id ON notifications(tracking_id);
CREATE INDEX idx_notification_status ON notifications(status);
CREATE INDEX idx_notification_created_at ON notifications(created_at);

-- =====================================================
-- Comments for notifications
-- =====================================================
COMMENT ON TABLE notifications IS 'Stores notification records sent to customers';
COMMENT ON COLUMN notifications.tracking_id IS 'Reference to the shipment tracking ID';
COMMENT ON COLUMN notifications.type IS 'Type of notification channel used';
COMMENT ON COLUMN notifications.status IS 'Current status of the notification';
COMMENT ON COLUMN notifications.recipient IS 'Recipient address (email, phone, etc.)';
COMMENT ON COLUMN notifications.retry_count IS 'Number of retry attempts made';
