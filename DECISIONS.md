# Design Decisions Log

## Decision 1: Thread-Safe In-Memory Architecture
- **Context:** The system requirements specify that an in-memory data store is adequate without an external database, but the app handles multi-threaded HTTP web requests.
- **Options Considered:** - *Option A:* standard HashMap with synchronized blocks.
  - *Option B:* `ConcurrentHashMap` combined with `AtomicLong` and `AtomicReference`.
- **Choice:** Option B.
- **Why:** `ConcurrentHashMap` allows high-performance lock-free reads and segmented lock writes, mitigating multi-user concurrent thread lockups while ensuring zero data degradation or race conditions when calculating metrics.

## Decision 2: Model Representation via Java Records
- **Context:** Modeling structural immutable records such as `Product`, `CartItem`, and `Coupon`.
- **Options Considered:** - *Option A:* Standard POJO Classes with Lombok annotations (`@Data`, `@AllArgsConstructor`).
  - *Option B:* Native Java Records.
- **Choice:** Option B.
- **Why:** Records natively eliminate boilerplate code, clearly communicate data immutability for thread-safe entities, and keep the code lean without requiring third-party library installations like Lombok.

## Decision 3: Use of BigDecimal for Financial Calculations
- **Context:** Choosing data types to evaluate system cart item values and cumulative tracking statistics.
- **Options Considered:** - *Option A:* Primitive `double` or wrapper `Double`.
  - *Option B:* `BigDecimal`.
- **Choice:** Option B.
- **Why:** Double values introduce floating-point arithmetic rounding errors. `BigDecimal` prevents precision loss during arithmetic percentage calculations on currency.

## Decision 4: Global vs. User Specific Coupon Scope
- **Context:** Determining how coupon data structures are managed globally inside the system.
- **Options Considered:**
  - *Option A:* Store issued coupons locked exclusively inside a specific user's cart model entity.
  - *Option B:* Maintain a central transactional Coupon ledger inside the `InMemoryStore`.
- **Choice:** Option B.
- **Why:** A global ledger makes it trivial for an Admin to view or manage metrics for generated codes, and mimics real-world databases where checkout requests validate codes globally before marking them spent.

## Decision 5: Triggering Condition Strategy for Nth Order Coupons
- **Context:** Deciding how and when the Nth sequence checkout criteria qualifies for dynamic code generation.
- **Options Considered:**
  - *Option A:* Check user-specific history.
  - *Option B:* Check global continuous sequential order metrics processing sequentially.
- **Choice:** Option B.
- **Why:** The assessment instructions indicate that "Every nth order gets a coupon code", implying global sequencing. Using a thread-safe atomic order counter ensures accurate checks under high concurrency.