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
