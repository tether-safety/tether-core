# Safety Kernel Invariants (V3)

This document defines the **non-negotiable invariants** of the Tether Safety Kernel.
Any change that violates these invariants is considered a **breaking safety defect**
and must be rejected.

These invariants apply regardless of platform, network state, UI, or future features.

---

## Invariant 1 — Device Authority (Primary)

The mobile device is the **primary authority** for all safety decisions.

- Check-in timers are evaluated on-device
- Emergencies are created on-device
- Escalation logic runs on-device
- Safety state transitions originate on-device

The cloud may **never** override or cancel a device-originated safety decision.

---

## Invariant 2 — Event First, State Second

All safety-relevant behavior must be expressed as immutable events.

- Events are append-only
- State is derived from events
- State may be rebuilt at any time from the event log
- No safety decision may be made without emitting an event

If state exists that cannot be derived from events, the system is invalid.

---

## Invariant 3 — Silence Is a Signal

Failure to confirm safety is treated as meaningful input.

- A missed check-in is not neutral
- Time passing without confirmation is actionable
- User inaction after an explicit contract implies risk

The system must never assume safety in the absence of confirmation.

---

## Invariant 4 — Missed Check-In Always Creates an Emergency

If a check-in contract reaches expiry:

- An `EMERGENCY_TRIGGERED` event must be created
- This must occur regardless of:
  - App state
  - Network availability
  - Battery level
  - OS background restrictions

Delayed relay is allowed.  
Emergency creation is not.

---

## Invariant 5 — Emergencies Are Persistent

Once an emergency is created:

- It must persist until explicitly resolved
- It must not be deleted, overwritten, or silently dismissed
- Resolution must be explicit and recorded as an event

Emergencies cannot disappear due to retries, restarts, or crashes.

---

## Invariant 6 — Escalation Requires Human Acknowledgement

Escalation stops **only** when a human explicitly acknowledges responsibility.

- Delivery of notifications does not count
- Silence is treated as failure
- Acknowledgement must be recorded as an event

No automatic system action may mark an emergency as “handled.”

---

## Invariant 7 — Sequential Escalation

Guardians are contacted sequentially, not in parallel.

- One guardian at a time
- Each guardian has a bounded acknowledgement window
- Failure to acknowledge escalates to the next guardian

This prevents ambiguity and responsibility diffusion.

---

## Invariant 8 — Safety Logic Is Offline-First

Safety behavior must function correctly with:

- No internet connection
- Intermittent connectivity
- Complete network absence

Network availability may improve outcomes but must never be required
for safety logic to execute.

---

## Invariant 9 — Cloud Is a Courier, Not a Judge

The cloud may:

- Relay events
- Deliver notifications
- Store forensic data
- Accept acknowledgements

The cloud may **not**:

- Decide when an emergency exists
- Modify safety state
- Suppress escalation
- Invent or delete events

---

## Invariant 10 — Cloud Failsafe Is Last Resort Only

Cloud-side emergency creation is permitted **only** when:

- A check-in expectation was previously registered
- The expected time passes
- No device-originated confirmation or emergency was received

Such events must be explicitly labeled as cloud failsafe actions
and must never overwrite device history.

---

## Invariant 11 — Forensic Location Snapshots Do Not Affect Safety Logic

Forensic Location Snapshots (FLS):

- Are best-effort
- Are non-authoritative
- Do not trigger emergencies
- Do not modify state
- Exist only to reduce post-failure uncertainty

Safety decisions must never depend on FLS availability.

---

## Invariant 12 — Pre-Expiry Signals Do Not Alter Expiry

Pre-expiry alerts (T-30, T-15, T-5):

- Are advisory only
- Exist to reduce false alarms
- Do not extend deadlines
- Do not prevent expiry

Expiry rules remain absolute.

---

## Invariant 13 — Activity Modifiers Are Bounded

Activity-based check-in modifiers:

- May delay expiry evaluation
- Must have a hard maximum silence limit
- Must degrade to standard expiry behavior

No modifier may suppress expiry indefinitely.

---

## Invariant 14 — Safety Is Isolated From Social Systems

Safety logic must not depend on:

- Chat systems
- Community activity
- Nearby users
- Discovery algorithms
- Reputation scores
- Financial state

Higher-level systems may observe safety events.
Safety must ignore higher-level systems.

---

## Invariant 15 — Failure Must Be Observable

All failure modes must:

- Produce events
- Leave audit trails
- Be diagnosable after the fact

Silent failure is considered a critical defect.

---

## Invariant 16 — No AI Overrides Safety Logic

No machine learning or probabilistic system may:

- Override timers
- Suppress escalation
- Cancel emergencies
- Infer safety

Deterministic rules always take precedence.

---

## Invariant 17 — Physical Destruction Is a Hard Limit

The system acknowledges that:

- Device destruction may prevent any further action
- Software cannot guarantee rescue
- All guarantees are bounded by physical reality

This limitation must be documented and never obscured.

---

## Enforcement

Any code, feature, or proposal that violates one or more invariants
must be rejected, regardless of usability, growth, or commercial value.

Safety invariants take precedence over all other concerns.

