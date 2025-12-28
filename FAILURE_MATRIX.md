# Failure Mode Matrix (V3)

This document enumerates all **known, expected, and catastrophic failure modes**
of the Tether Safety Kernel and defines the system’s deterministic behavior
in each case.

If a failure mode is not listed here, it is considered **unhandled**.

The goal is not to eliminate failure, but to ensure that
failure is **predictable, observable, and bounded**.

---

## Failure Classification

Failures are grouped into:

- Device failures
- OS / platform failures
- Network failures
- User failures
- Guardian failures
- Cloud failures
- Physical reality limits

---

## F1 — App Killed by OS

**Scenario**  
The operating system terminates the app due to memory, battery, or policy.

**Expected Behavior**
- No timers run while the app is dead

**Recovery Behavior**
- On restart:
  - Events are replayed
  - Check-in deadlines are recalculated
  - Missed expiry triggers immediately if overdue

**Guarantee**
- Time passing while the app is dead still counts

---

## F2 — Device Rebooted

**Scenario**  
The device reboots due to crash, update, or power loss.

**Expected Behavior**
- Safety execution halts temporarily

**Recovery Behavior**
- On boot:
  - Event log is replayed
  - Timers are evaluated
  - Emergency is created if expiry passed

**Guarantee**
- Intent is preserved across reboots

---

## F3 — Battery Fully Depleted

**Scenario**  
Battery reaches zero during an active check-in.

**Expected Behavior**
- No execution possible

**Recovery Behavior**
- On power restore:
  - Elapsed time is evaluated
  - Emergency is created if overdue
  - Escalation resumes

**Guarantee**
- Battery loss delays action, never cancels it

---

## F4 — No Network Connectivity

**Scenario**  
The device has no network access.

**Expected Behavior**
- All safety logic continues locally
- Events are stored in outbox

**Recovery Behavior**
- On first connectivity:
  - Emergency relay resumes
  - Outbox is flushed

**Guarantee**
- Network absence does not block safety decisions

---

## F5 — Intermittent Connectivity

**Scenario**  
Connectivity is unstable or brief.

**Expected Behavior**
- Partial sync attempts
- Idempotent retries

**Recovery Behavior**
- Emergency events are retried aggressively
- Non-emergency events retry opportunistically

**Guarantee**
- Duplicate delivery is acceptable
- Lost delivery is not

---

## F6 — GPS Unavailable

**Scenario**  
Location sensors cannot provide a fix.

**Expected Behavior**
- LOCATION_UNAVAILABLE is recorded
- Last known location is retained

**Guarantee**
- Emergency creation does not depend on GPS

---

## F7 — Inaccurate Location

**Scenario**  
Location accuracy is poor or degraded.

**Expected Behavior**
- Accuracy metadata is preserved
- No correction or smoothing is applied

**Guarantee**
- Transparency over false precision

---

## F8 — User Forgets to Check In

**Scenario**  
User does not confirm safety before expiry.

**Expected Behavior**
- Pre-expiry alerts fire
- Expiry occurs
- Emergency is created

**Guarantee**
- Forgetfulness is treated as risk, not error

---

## F9 — User Confirms After Expiry

**Scenario**  
User attempts to confirm safety after expiry.

**Expected Behavior**
- Confirmation is rejected
- Emergency remains active

**Guarantee**
- Late confirmation cannot suppress escalation

---

## F10 — Guardian Does Not Respond

**Scenario**  
Guardian ignores or misses notifications.

**Expected Behavior**
- Acknowledgement timeout expires
- Escalation proceeds to next guardian

**Guarantee**
- Silence is treated as failure

---

## F11 — Guardian Acknowledges but Does Nothing

**Scenario**  
Guardian confirms receipt but takes no action.

**Expected Behavior**
- Escalation stops
- Emergency remains unresolved until explicitly closed

**Guarantee**
- System verifies awareness, not action

---

## F12 — Cloud Service Unavailable

**Scenario**  
Cloud relay or notification service is down.

**Expected Behavior**
- Device continues operating
- Events remain queued

**Recovery Behavior**
- Sync resumes when cloud recovers

**Guarantee**
- Cloud failure does not break safety logic

---

## F13 — Partial Cloud Failure

**Scenario**  
Some notification channels fail.

**Expected Behavior**
- Failures are logged
- Alternative channels are attempted
- Escalation continues

**Guarantee**
- No silent notification loss

---

## F14 — Local Data Corruption

**Scenario**  
Local database is partially corrupted.

**Expected Behavior**
- System enters DEGRADED state
- Non-essential operations halt

**Recovery Behavior**
- Attempt partial replay
- Preserve raw event log if possible

**Guarantee**
- Failure is detectable and observable

---

## F15 — Device Destroyed or Lost

**Scenario**  
Device is destroyed, lost, or rendered inoperable.

**Expected Behavior**
- No further device-side action possible

**Recovery Behavior**
- Cloud-side expected check-in expiry may trigger failsafe escalation

**Guarantee**
- Software responsibility ends at physical destruction

---

## F16 — Device Destroyed After Emergency Trigger, No Connectivity

**Scenario**  
Emergency is triggered locally but cannot be relayed before destruction.

**Expected Behavior**
- Forensic Location Snapshot may exist
- Cloud uses last available forensic data

**Guarantee**
- Uncertainty is reduced where possible
- Rescue is not guaranteed

---

## F17 — Forensic Snapshot Fails

**Scenario**  
Forensic snapshot cannot be transmitted.

**Expected Behavior**
- Failure is silent
- No retries occur

**Guarantee**
- Battery is not sacrificed for telemetry

---

## Hard Limits

The system explicitly acknowledges:

- Software cannot guarantee rescue
- Hardware destruction is final
- Zero connectivity may prevent communication entirely

These limits must be documented and never obscured.

---

## Enforcement

Any failure mode discovered in testing or production
that is not represented in this matrix must be added
before the system can be considered complete.

Predictable failure is a feature, not a defect.

