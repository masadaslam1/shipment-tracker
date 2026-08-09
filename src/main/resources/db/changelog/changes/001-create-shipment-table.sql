--liquibase formatted sql

--changeset masadaslam1:1
--comment: create the shipment table
CREATE TABLE shipment
(
    id                 BIGSERIAL PRIMARY KEY,
    tracking_number    VARCHAR(255) NOT NULL UNIQUE,
    status             VARCHAR(50)  NOT NULL,
    origin             VARCHAR(255) NOT NULL,
    destination        VARCHAR(500) NOT NULL,
    current_location   VARCHAR(255),
    estimated_delivery TIMESTAMPTZ  NOT NULL,
    actual_delivery    TIMESTAMPTZ,
    created_at         TIMESTAMPTZ  NOT NULL,
    last_updated       TIMESTAMPTZ  NOT NULL,
    description        TEXT,
    weight             NUMERIC(10, 2),
    carrier            VARCHAR(255)
);