-- =====================================================
-- Shipment Tracker System - Initial Test Data
-- =====================================================
-- Description: DML script to load initial test data
-- Database: H2 (PostgreSQL compatibility mode)
-- Author: Generated for shipment-tracker-system
-- =====================================================

-- =====================================================
-- Sample shipment tracking records for testing
-- =====================================================

-- Shipment 1: In Transit
INSERT INTO trackings (tracking_id, current_status, previous_status, last_location, carrier, estimated_delivery, created_at, updated_at, last_checked_at)
VALUES ('TRK001234567890', 'IN_TRANSIT', 'PENDING', 'Distribution Center - Madrid', 'DHL Express',
        TIMESTAMP '2025-10-29 14:00:00',
        TIMESTAMP '2025-10-25 08:30:00',
        TIMESTAMP '2025-10-27 10:15:00',
        TIMESTAMP '2025-10-27 10:15:00');

-- Shipment 2: Out for Delivery
INSERT INTO trackings (tracking_id, current_status, previous_status, last_location, carrier, estimated_delivery, created_at, updated_at, last_checked_at)
VALUES ('TRK987654321000', 'OUT_FOR_DELIVERY', 'IN_TRANSIT', 'Local Hub - Barcelona', 'Correos',
        TIMESTAMP '2025-10-27 18:00:00',
        TIMESTAMP '2025-10-24 09:00:00',
        TIMESTAMP '2025-10-27 07:30:00',
        TIMESTAMP '2025-10-27 07:30:00');

-- Shipment 3: Delivered
INSERT INTO trackings (tracking_id, current_status, previous_status, last_location, carrier, estimated_delivery, created_at, updated_at, last_checked_at)
VALUES ('TRK555666777888', 'DELIVERED', 'OUT_FOR_DELIVERY', 'Customer Address - Valencia', 'SEUR',
        TIMESTAMP '2025-10-26 16:00:00',
        TIMESTAMP '2025-10-23 11:00:00',
        TIMESTAMP '2025-10-26 15:45:00',
        TIMESTAMP '2025-10-26 15:45:00');

-- Shipment 4: Pending
INSERT INTO trackings (tracking_id, current_status, previous_status, last_location, carrier, estimated_delivery, created_at, updated_at, last_checked_at)
VALUES ('TRK111222333444', 'PENDING', NULL, 'Origin Warehouse - Sevilla', 'MRW',
        TIMESTAMP '2025-10-30 12:00:00',
        TIMESTAMP '2025-10-27 09:00:00',
        TIMESTAMP '2025-10-27 09:00:00',
        TIMESTAMP '2025-10-27 09:00:00');

-- Shipment 5: Failed Delivery (will retry)
INSERT INTO trackings (tracking_id, current_status, previous_status, last_location, carrier, estimated_delivery, created_at, updated_at, last_checked_at)
VALUES ('TRK999888777666', 'FAILED_DELIVERY', 'OUT_FOR_DELIVERY', 'Customer Address - Bilbao', 'UPS',
        TIMESTAMP '2025-10-28 14:00:00',
        TIMESTAMP '2025-10-25 10:30:00',
        TIMESTAMP '2025-10-27 12:00:00',
        TIMESTAMP '2025-10-27 12:00:00');

-- Shipment 6: In Transit (International)
INSERT INTO trackings (tracking_id, current_status, previous_status, last_location, carrier, estimated_delivery, created_at, updated_at, last_checked_at)
VALUES ('TRK444555666777', 'IN_TRANSIT', 'PENDING', 'International Hub - Frankfurt', 'FedEx',
        TIMESTAMP '2025-11-02 10:00:00',
        TIMESTAMP '2025-10-26 14:00:00',
        TIMESTAMP '2025-10-27 08:00:00',
        TIMESTAMP '2025-10-27 08:00:00');

-- =====================================================
-- Data verification
-- =====================================================
-- Expected result: 6 tracking records inserted
