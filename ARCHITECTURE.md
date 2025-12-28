# Safety Kernel Architecture (V3)

This document defines the **structural architecture** of the Tether Safety Kernel.
It describes components, responsibilities, and allowed interactions.

This architecture is designed to preserve determinism, auditability,
and offline-first execution.

---

## Architectural Style

The Safety Kernel uses a **local-first, event-sourced architecture**.

Key characteristics:
- Append-only event log
- Derived state projections
- Deterministic state machines
- Explicit failure handling
- No hidden side effects

---

## High-Level Components

+-------------------------------+
|           Device / OS         |
|  (Battery, GPS, Alarms, Boot, |
|           Sensors)            |
+---------------^---------------+
                |
+---------------+---------------+
|          Safety Kernel        |
|      (Authoritative Logic)    |
|                               |
|  +-------------------------+  |
|  | Event Store (Append)    |  |
|  +-----------^-------------+  |
|              |                |
|  +-----------+-------------+  |
|  | State Projections       |  |
|  +-----------^-------------+  |
|              |                |
|  +-----------+-------------+  |
|  | Time & Escalation       |  |
|  +-----------^-------------+  |
|              |                |
|  +-----------+-------------+  |
|  | Sync Outbox              |  |
|  +-----------^-------------+  |
|              |                |
+---------------+---------------+
                |
+---------------v---------------+
|             Cloud             |
|     (Relay, Notify, Store)    |
+-------------------------------+


## Core Components

### 1. Event Store

**Responsibility**
- Persist all safety-relevant events
- Guarantee append-only behavior
- Support deterministic replay

**Rules**
- No updates or deletes
- Events must be written before state changes

---

### 2. State Projections

**Responsibility**
- Derive current state from events
- Enable fast reads
- Be fully rebuildable

**Examples**
- SystemState
- CheckinState
- EscalationState

State is **never authoritative**.

---

### 3. Check-In Timer Engine

**Responsibility**
- Evaluate check-in deadlines
- Emit pre-expiry signals
- Trigger expiry deterministically

**Characteristics**
- Monotonic-time based
- Survives app kill and reboot
- Offline-first

---

### 4. Emergency & Escalation Engine

**Responsibility**
- Create emergencies
- Escalate sequentially
- Require human acknowledgement

**Rules**
- One emergency per expired contract
- Escalation stops only on acknowledgement
- All actions emit events

---

### 5. Location Ledger

**Responsibility**
- Store append-only location snapshots
- Preserve accuracy metadata
- Provide last-known-point for emergencies

**Rules**
- No smoothing
- No overwrites
- Emergency snapshot is mandatory

---

### 6. Forensic Location Snapshot (FLS)

**Responsibility**
- Opportunistically transmit last-known location
- Reduce uncertainty if device is destroyed

**Rules**
- Best-effort only
- Non-authoritative
- No retries
- Does not affect safety logic

---

### 7. Sync Outbox

**Responsibility**
- Queue outbound events
- Ensure eventual delivery
- Prioritize emergencies

**Rules**
- No direct network calls from core logic
- Idempotent delivery
- Failure does not block local execution

---

## Cloud Role (Non-Authoritative)

The cloud acts strictly as a **relay and store**.

It may:
- Receive events
- Dispatch notifications
- Accept acknowledgements
- Store forensic data

It may not:
- Decide safety state
- Modify events
- Suppress escalation
- Override device decisions

---

## Control Flow Rules

- All control decisions originate in the Safety Kernel
- External systems react to events only
- No external system may invoke internal logic directly

---

## Data Flow Rules

Events → Projections → Decisions → Events


- Data flows in one direction
- No circular dependencies
- No implicit state

---

## Offline Guarantees

The Safety Kernel must function correctly when:
- No network is available
- Connectivity is intermittent
- Cloud services are unreachable

Network access improves outcomes but is never required.

---

## Forbidden Architecture Patterns

The following patterns are explicitly prohibited:

- Request/response-driven safety decisions
- Cloud-authoritative timers
- Parallel guardian escalation
- Community-driven emergency handling
- AI-mediated safety logic
- Hidden mutable state

---

## Architectural Enforcement

This architecture must be enforced via:
- Module boundaries
- Dependency rules
- Code review
- Failure testing

If a component cannot be tested in isolation under failure,
the architecture is considered violated.

---

## Status

This architecture is **locked** for V3.

Any change requires:
- Invariant review
- Failure matrix update
- Major version increment

