

# Neustack E-commerce Assignment - Spring Boot

This project is a RESTful E-commerce backend built with Spring Boot. It utilizes thread-safe, in-memory data structures to handle product catalog management, cart operations, dynamic coupon generation rules, and admin metrics tracking without external database dependencies.

## Technical Stack & Highlights
- **Java 21:** Modern structural modeling via Java Records.
- **Spring Boot 3.x:** Rest Controller and Dependency Injection architecture.
- **Concurrency Guarded:** Built utilizing `ConcurrentHashMap`, `AtomicLong`, and `AtomicReference` to safeguard state consistency across multi-threaded HTTP request contexts.
- **Precision Currency:** `BigDecimal` handles all price configurations and calculation percentages to negate native float/double rounding errors.

---

## Setup Instructions

### 1. Prerequisites
- Ensure **Java 21 JDK** or higher is installed and configured in your environment.
- Ensure **Maven 3.8+** is installed (or use the included Maven Wrapper script `./mvnw`).

### 2. Run the Application
1. Clone this repository to your local directory.
2. Open a terminal in the project's root folder.
3. Build the project using Maven:
```bash
./mvnw clean install
./mvnw spring-boot:run

The application bootstraps locally and listens on port 8080 (http://localhost:8080).
```

### 3. Running Unit Tests
To run the automated suite testing cart rules, voucher redemption, error tracking, and concurrency simulations, run:

```Bash
./mvnw test 
```

### 4. Pre-Loaded Inventory Catalog
The in-memory data store automatically seeds itself with the following items for testing:

P1 - Wireless Mouse ($50.00)

P2 - Mechanical Keyboard ($50.00)

P3 - Gaming Monitor ($50.00)

### 5. API Specification
``` 1. Cart Controller Operations (/api/cart)
A. Add Item to Cart
Adds specified item quantities into a targeted user's cart session. If the item already exists, the quantities are safely combined.

URL: /api/cart/{userId}/add

Method: POST

Headers: Content-Type: application/json

Request Body JSON:

JSON
{
  "productId": "p1",
  "quantity": 2
}
Success Response (200 OK):

Body: Item added to cart successfully

Error Response (400 Bad Request):

Condition: Product code does not exist in inventory catalog.

Body: Product not found
```

```B. Checkout Cart
Calculates subtotals, verifies and updates optional promo codes, empties the target cart, updates global reporting tallies, and returns a detailed breakdown. If this checkout happens to be the Nth order (every 3rd system checkout), a discount code is automatically created and returned.

URL: /api/cart/{userId}/checkout

Method: POST

Query Parameters: couponCode (Optional string)

Example Requests:

Standard: /api/cart/user_abc/checkout

With Voucher: /api/cart/user_abc/checkout?couponCode=REWARD-X7Y2Z9

Success Payload - Normal Order (e.g., System Checkout #1 or #2)
Status: 200 OK

Response Body JSON:

JSON
{
  "subtotal": 125.00,
  "discountApplied": 0.00,
  "netTotal": 125.00,
  "rewardCouponCode": null
}
Success Payload - Nth Order Milestone Triggered (e.g., System Checkout #3, #6...)
Status: 200 OK

Response Body JSON:

JSON
{
  "subtotal": 50.00,
  "discountApplied": 0.00,
  "netTotal": 50.00,
  "rewardCouponCode": "REWARD-A4F8E2B1"
}
Success Payload - Checking out with a valid Coupon Applied
Status: 200 OK

Response Body JSON:

JSON
{
  "subtotal": 100.00,
  "discountApplied": 10.00,
  "netTotal": 90.00,
  "rewardCouponCode": null
}
Error Responses (400 Bad Request)
Condition 1: Checking out with an empty cart.

Body: Cart is empty

Condition 2: The provided coupon string does not exist or has already been used.

Body: Invalid or already used coupon code

2. Admin Controller Operations (/api/admin)
A. Fetch System Performance Metrics
Gathers cumulative analytical information across all users for administrative reporting.

URL: /api/admin/metrics

Method: GET

Success Response (200 OK):

Response Body JSON:

JSON
{
  "totalItemsPurchased": 5,
  "totalRevenue": 265.00,
  "discountCodes": [
    "ADMIN-C7B2A10F",
    "REWARD-A4F8E2B1"
  ],
  "totalDiscountsGiven": 10.00
}
B. Generate Manual Coupon
Allows administrators to manually inject a newly valid 10% discount token code directly into the active coupon ledger registry.

URL: /api/admin/coupon/generate

Method: POST

Success Response (200 OK):

Body: Coupon Generated: ADMIN-C7B2A10F

```