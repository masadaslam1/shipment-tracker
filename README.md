# Shipment Tracker

Real-time shipment tracking dashboard. A shipment simulator publishes events to Kafka, a Spring Boot backend consumes them, persists them to PostgreSQL, and streams every update over WebSocket to an Angular table. Built as a portfolio project to demonstrate an end-to-end event-driven system.

## Architecture

```
Shipment Simulator ──> Kafka ──> Spring Boot Consumer ──> PostgreSQL
      (3s ticks)    topic:          upsert by
                    shipment-events  tracking number
                                          │
                                          ▼
                                 WebSocket broadcast
                                 /topic/shipments
                                          │
                                          ▼
                              Angular 19 table (live)
```

Two data paths reach the frontend:

- **REST** `GET /api/shipments` for the initial snapshot and reconnects
- **STOMP over WebSocket** on `/ws`, topic `/topic/shipments`, for live updates

Both channels carry the same `ShipmentDto` JSON shape. The frontend merges them with a single rule: the newest `lastUpdated` wins per tracking number, no matter which channel it arrived on.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 4.1, Spring Kafka, Spring WebSocket (STOMP) |
| Persistence | PostgreSQL 16, Liquibase migrations |
| Messaging | Apache Kafka 7.7 (KRaft mode, no ZooKeeper) |
| Frontend | Angular 19, Bootstrap 5, @stomp/stompjs |
| Build | Maven, npm |
| Deployment | Docker Compose, nginx, single VM (Oracle Cloud Free Tier) |

## Run It

### Full stack in Docker (recommended)

```bash
cp .env.example .env   # fill in your Postgres credentials, or keep the defaults
docker compose up -d --build
```

Open http://localhost and watch shipments move. This starts Postgres, Kafka, the backend (with the simulation profile), and nginx serving the Angular build.

### Development mode

Backend outside Docker with hot reload, frontend with the dev proxy:

```bash
# Terminal 1: infrastructure only
docker compose up -d postgres kafka

# Terminal 2: backend with simulator
set -a; source .env; set +a; ./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=simulation

# Terminal 3: Angular dev server (proxy forwards /api and /ws to 8083)
cd frontend && npm start
```

Open http://localhost:4200. Without the simulation profile, the backend runs quiet and you can feed it events manually with a console producer.

## Tests

```bash
# Backend (needs the .env sourced and docker running for the context test)
set -a; source .env; set +a; ./mvnw.cmd test

# Frontend
cd frontend && npm test -- --watch=false --browsers=ChromeHeadless
```

## API Contract

`GET /api/shipments` returns an array of:

```json
{
  "id": 6,
  "trackingNumber": "TRK-1004",
  "status": "IN_TRANSIT",
  "origin": "Rotterdam",
  "destination": "Singapore",
  "currentLocation": "Colombo",
  "estimatedDelivery": "2026-10-21T12:03:00Z",
  "actualDelivery": null,
  "weight": 18.90,
  "carrier": "Evergreen",
  "description": "Machinery and equipment",
  "createdAt": "2026-10-18T11:23:34Z",
  "lastUpdated": "2026-10-18T12:03:13Z"
}
```

Statuses: `CREATED`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`, `DELIVERED`, `EXCEPTION`.

WebSocket frames on `/topic/shipments` carry the identical shape, one full shipment snapshot per frame.

## Design Decisions

- **Kafka over RabbitMQ**: the project is a resume piece, and Kafka plus KRaft mode shows event-streaming depth.
- **Modular monolith, not microservices**: one deployable, appropriate for the scope. The simulator, consumer, and REST API are cleanly separated packages that could split later.
- **STOMP over plain WebSocket, no SockJS**: every modern browser speaks WebSocket natively.
- **One wire DTO everywhere**: MapStruct maps entities to `ShipmentDto`, and REST plus WebSocket speak the same JSON. The inbound Kafka contract stays a tolerant reader; the outbound contract is explicit.
- **Same-origin deployment**: the frontend never learns where the backend lives. In dev, the Angular proxy forwards; in production, nginx forwards. Zero code changes.
- **Simulation as a runtime mode**: the simulator is a profile-gated Spring bean. It is just another Kafka producer, which proves the rest of the system would accept any real producer unchanged.

## Scope Notes

This is a demo system. There is no authentication on the REST or WebSocket endpoints, and the deployment targets a single VM with the simulator always on. Both are deliberate simplifications, documented here rather than discovered.
