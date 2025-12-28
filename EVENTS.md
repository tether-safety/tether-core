# Safety Kernel Events (V3)

This document defines the complete set of **authoritative events**
used by the Tether Safety Kernel.

All safety behavior is expressed as immutable events.
State is always derived from these events.

If a behavior cannot be represented by one of these events,
it is not part of the safety system.

---

## Event Design Rules

- Events are append-only
- Events are immutable
- Events are timestamped at creation
- Events are the single source of truth
- Events must be persisted before any state transition

---

## System Lifecycle Events

### SYSTEM_STARTED
Emitted when the safety kernel starts or resumes.

Used for:
- Boot recovery
- Replay boundaries
- Diagnostics

---

### SYSTEM_DEGRADED
Emitted when the system detects reduced operating capability.

Examples:
- OS background restrictions
- Battery-critical state
- Missed heartbeats
- Sensor unavailability

---

### SYSTEM_RECOVERED
Emitted when degraded conditions are resolved.

---

## Location Events

### LOCATION_RECORDED
Represents a persisted location snapshot.

Payload includes:
- latitude
- longitude
- accuracy_meters
- timestamp
- source (GPS | NETWORK | MANUAL)

---

## Check-In Contract Events

### CHECKIN_SET
Represents the creation of a check-in contract.

Payload includes:
- expected_checkin_time
- grace_period_seconds
- activity_modifiers (optional)

---

### CHECKIN_PRE_EXPIRY_T30
Advisory signal emitted 30 minutes before expiry.

---

### CHECKIN_PRE_EXPIRY_T15
Advisory signal emitted 15 minutes before expiry.

---

### CHECKIN_PRE_EXPIRY_T5
Advisory signal emitted 5 minutes before expiry.

---

### GRACE_STARTED
Emitted when the grace period begins.

---

### CHECKIN_CONFIRMED
Represents explicit user confirmation of safety.

---

### CHECKIN_MISSED
Represents expiry of a check-in contract.

This event must always precede emergency creation.

---

## Emergency Events

### EMERGENCY_TRIGGERED
Represents creation of an emergency state.

Payload includes:
- trigger_reason (CHECKIN | MANUAL | FAILSAFE)
- linked_checkin_id
- last_known_location_id (if available)

---

### EMERGENCY_RELAY_ATTEMPTED
Represents an attempt to relay the emergency externally.

Payload includes:
- relay_channel (SMS | EMAIL | PUSH | CALL)
- target_identifier
- attempt_number

---

### EMERGENCY_RELAY_FAILED
Represents a failed relay attempt.

---

### EMERGENCY_RELAY_SUCCEEDED
Represents successful relay delivery.

Delivery does not imply acknowledgement.

---

### EMERGENCY_RESOLVED
Represents explicit resolution of an emergency.

Resolution must be initiated by:
- Acknowledging guardian
- Authorized authority

---

## Guardian Escalation Events

### GUARDIAN_NOTIFIED
Represents notification sent to a guardian.

Payload includes:
- guardian_id
- contact_method
- escalation_tier

---

### GUARDIAN_ACKNOWLEDGED
Represents explicit acknowledgement by a guardian.

Payload includes:
- guardian_id
- acknowledgement_method
- timestamp

---

### GUARDIAN_ESCALATION_TIMEOUT
Represents failure of a guardian to acknowledge within the allowed window.

---

## Cloud Failsafe Events (Explicit Exception)

### CLOUD_EXPECTED_CHECKIN_REGISTERED
Represents registration of an expected check-in with the cloud.

---

### CLOUD_EXPECTED_CHECKIN_EXPIRED
Represents expiry of a cloud-registered expected check-in.

---

### CLOUD_FAILSAFE_EMERGENCY_TRIGGERED
Represents emergency creation by the cloud due to device non-response.

This event must be explicitly labeled and never overwrite device history.

---

## Forensic Events

### FORENSIC_LOCATION_SNAPSHOT_SENT
Represents successful transmission of a forensic location snapshot.

---

### FORENSIC_LOCATION_SNAPSHOT_FAILED
Represents failed attempt to transmit a forensic snapshot.

Failure must be silent and non-blocking.

---

## Prohibited Events

The following event types are explicitly forbidden:

- SAFETY_CANCELLED
- EMERGENCY_SUPPRESSED
- AUTO_RESOLVED
- COMMUNITY_RESPONDED
- AI_OVERRIDE

If an event of this nature is proposed, it must be rejected.

---

## Event Completeness Guarantee

This list is exhaustive.

Adding a new event type requires:
- Explicit documentation
- Invariant review
- Major version increment

Untracked behavior is considered a safety defect.

