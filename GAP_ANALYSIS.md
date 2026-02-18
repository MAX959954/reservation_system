# Hotel Reservation System - Gap Analysis

## Executive Summary
This document compares the current implementation against the detailed requirements provided.

---

## ✅ WHAT YOU HAVE

### 1. Database Models (Entities)
**Status: Partially Complete**

You have most of the core entities:
- ✅ `users` (AppUser) - but missing some fields
- ✅ `rooms` 
- ✅ `rates`
- ✅ `bookings`
- ✅ `booking_guests` (BookinGuests)
- ✅ `booking_nights` (Booking_nights)
- ✅ `room_inventory` (RoomInventory)
- ✅ `invoices`
- ✅ `payments`

**Missing:**
- ❌ `booking_rooms` entity (for multi-room booking support)

### 2. Roles & Security
**Status: Basic Implementation**

- ✅ Roles defined: `GUEST`, `STAFF`, `ADMIN` (AppUserRole enum)
- ✅ JWT filter exists (JwtAuthenticationFilter)
- ✅ SecurityConfig with basic setup
- ✅ Password encoding (BCrypt)
- ⚠️ JWT filter is incomplete (missing token generation endpoint)

### 3. Business Logic Services
**Status: Basic Structure Exists**

- ✅ BookingService (but not transaction-safe)
- ✅ RoomService
- ✅ PaymentService (basic CRUD)
- ✅ InvoiceService (basic queries only)
- ✅ SearchRoomService (but doesn't use room_inventory properly)
- ✅ RatesService
- ✅ RoomInventoryService

### 4. Controllers
**Status: Minimal Endpoints**

- ✅ BookingController (admin only, limited endpoints)
- ✅ RoomController (admin CRUD)
- ✅ RegistrationController
- ✅ AdminDashboardController

### 5. Database
**Status: Wrong Database & Configuration**

- ⚠️ Using **PostgreSQL** (requirement specifies **MySQL**)
- ⚠️ Using `hibernate.ddl-auto: update` (requirement specifies **Flyway** migrations)
- ❌ No Flyway migrations exist

---

## ❌ WHAT'S MISSING / NEEDS FIXING

### 🔴 CRITICAL ISSUES

#### 1. Database Schema Issues

**Missing Fields:**
- `users` table: missing `full_name`, `password_hash` (you have `password`), `created_at`
- `room_inventory.night_date`: should be `LocalDate` not `LocalDateTime`
- `booking_nights.night_date`: should be `LocalDate` not `LocalDateTime`
- `rates.start_date/end_date`: should be `LocalDate` not `LocalDateTime`

**Missing Table:**
- `booking_rooms(id, booking_id, room_id)` - needed for multi-room bookings

**Missing Constraints:**
- `UNIQUE(room_id, night_date)` on `room_inventory`
- `UNIQUE(booking_id, room_id, night_date)` on `booking_nights`

#### 2. Flyway Migrations
- ❌ No `src/main/resources/db/migration/V1__init.sql`
- ❌ Flyway dependency missing from `pom.xml`
- ❌ Database schema should be managed by SQL migrations, not `ddl-auto`

#### 3. Database Type
- ❌ Currently PostgreSQL → Need to switch to **MySQL**
- ❌ MySQL driver dependency missing

#### 4. Booking Service - Transaction Safety
**Status: NOT IMPLEMENTED**

Current `BookingService.createBooking()`:
- ❌ No `@Transactional` with row-level locking
- ❌ Doesn't use `FOR UPDATE` on inventory rows
- ❌ Doesn't prevent double-booking
- ❌ Doesn't check `booked_count < allotment` before creating booking
- ❌ Doesn't create `booking_nights` records
- ❌ Doesn't update `room_inventory.booked_count`

**Required Implementation:**
```java
@Transactional
public Booking createBooking(CreateBookingCmd cmd, Long userId) {
    // 1. Lock inventory rows with FOR UPDATE
    // 2. Check availability (booked_count < allotment)
    // 3. Create booking with status PENDING_PAYMENT
    // 4. Increment booked_count for each night
    // 5. Create booking_nights records
}
```

#### 5. Booking Status
- ❌ Missing `PENDING_PAYMENT` status (required enum value)
- Current enum: `RESERVED, CONFIRMED, CHECKED_IN, IN_PROGRESS, CHECKED_OUT, CANCELLED, COMPLETED`

#### 6. Pricing Service
- ❌ No pricing service to compute:
  - Base price + seasonal rates
  - Nightly prices
  - Total booking amount

#### 7. Availability Search
**Status: INCORRECT IMPLEMENTATION**

Current `SearchRoomService.findAvailableRoom()`:
- ❌ Doesn't use `room_inventory` table
- ❌ Uses naive date overlap checking on bookings
- ❌ Should query `room_inventory` where `booked_count < allotment` for all nights in range

**Required:** Query `room_inventory` table with per-night availability check.

---

### 🟡 HIGH PRIORITY MISSING FEATURES

#### 8. Stripe Payment Integration
**Status: NOT IMPLEMENTED**

- ❌ Stripe SDK dependency missing from `pom.xml`
- ❌ No PaymentIntent creation
- ❌ No webhook handler for `/payments/webhook`
- ❌ No endpoint `POST /payments/create-intent`
- ❌ Payment service doesn't integrate with Stripe

#### 9. JWT Login Endpoint
**Status: MISSING**

- ❌ No `POST /api/auth/login` endpoint that:
  - Authenticates user
  - Returns JWT access token
  - Sets refresh token in httpOnly cookie (or rotates tokens)

JWT filter exists but there's no controller to generate tokens.

#### 10. Invoice PDF Generation
**Status: NOT IMPLEMENTED**

- ❌ OpenPDF or iText dependency missing from `pom.xml`
- ❌ `InvoiceService` only has query methods
- ❌ No PDF generation logic
- ❌ No endpoint `GET /invoices/{bookingId}` to stream PDF
- ❌ No email sending after invoice generation

#### 11. REST API Endpoints
**Status: MOSTLY MISSING**

**Missing Public Endpoints:**
- ❌ `GET /availability?checkIn=...&checkOut=...&guests=...&roomType=...`
- ❌ `POST /auth/login` (returns JWT)
- ❌ `POST /auth/register` (optional)

**Missing Guest Endpoints:**
- ❌ `POST /bookings` (create booking)
- ❌ `GET /bookings/my` (user's own bookings)
- ❌ `GET /bookings/{id}` (view booking)
- ❌ `DELETE /bookings/{id}` (cancel booking)
- ❌ `POST /payments/create-intent`
- ❌ `GET /invoices/{bookingId}` (PDF download)

**Missing Admin/Staff Endpoints:**
- ⚠️ `GET /rooms` (list)
- ✅ `POST /rooms` (create) - exists but admin only
- ⚠️ `PUT /rooms/{id}` - exists but has bug (@PostMapping instead of @PutMapping)
- ✅ `DELETE /rooms/{id}` - exists
- ❌ CRUD for `/rates`
- ❌ `GET /dashboard/stats?from&to` (occupancy, RevPAR, ADR, revenue)

#### 12. Security Configuration
**Status: INCOMPLETE**

- ❌ SecurityConfig doesn't use `@PreAuthorize` properly
- ❌ Missing role-based URL protection:
  - Public: `/auth/**`, `/availability/**`, `/payments/webhook`
  - GUEST: `/bookings/**` (own), `/invoices/**` (own)
  - STAFF/ADMIN: `/rooms/**`, `/rates/**`, all bookings
- ⚠️ JWT filter not added to SecurityConfig filter chain

#### 13. Dependency Management (pom.xml)
**Missing Dependencies:**
- ❌ `spring-boot-starter-validation`
- ❌ Flyway (`org.flywaydb:flyway-core`)
- ❌ MySQL driver (`com.mysql:mysql-connector-j`)
- ❌ Stripe SDK (`com.stripe:stripe-java`)
- ❌ OpenPDF (`com.github.librepdf:openpdf`) or iText
- ❌ MapStruct (optional but mentioned)

**Wrong Dependencies:**
- ⚠️ PostgreSQL driver (should be MySQL)

#### 14. Data Types Mismatch
**Status: DATE vs DATETIME**

The specification uses `LocalDate` for dates (night_date, check_in, check_out dates), but your code uses `LocalDateTime`:
- `Booking.check_in` / `check_out` → should be `LocalDate`
- `RoomInventory.night_date` → should be `LocalDate`
- `Booking_nights.night_date` → should be `LocalDate`
- `Rates.start_date` / `end_date` → should be `LocalDate`

**Note:** `created_at`, `updated_at`, `issued_at` can remain `LocalDateTime`.

---

### 🟢 MEDIUM PRIORITY / NICE TO HAVE

#### 15. Frontend
**Status: NOT STARTED**

- ❌ No React frontend
- ❌ No React Router, React Query, React Hook Form
- ❌ No MUI or Tailwind
- ❌ No JWT token management in frontend
- ❌ No Stripe Elements integration

#### 16. Docker & Infrastructure
**Status: NOT IMPLEMENTED**

- ❌ No `docker-compose.yml` for MySQL
- ❌ No Dockerfile for backend
- ❌ No environment variable configuration

#### 17. Testing
**Status: MINIMAL**

- ⚠️ Only basic test class exists
- ❌ No unit tests for services
- ❌ No integration tests with Testcontainers
- ❌ No API tests with MockMvc
- ❌ No E2E tests

#### 18. Seed Data
**Status: MISSING**

- ❌ No data seeding for rooms
- ❌ No inventory generation for next 365 days

#### 19. Admin Dashboard Stats
**Status: BASIC EXISTS, NEEDS EXPANSION**

- ⚠️ AdminDashboardService exists but likely incomplete
- ❌ Need: occupancy %, ADR (Average Daily Rate), RevPAR (Revenue per Available Room), revenue

---

## 📋 IMPLEMENTATION CHECKLIST

--- : COMPILTED
### Phase 1: Database & Schema (Sprint 1)
- [ ] Switch from PostgreSQL to MySQL
- [ ] Add Flyway dependency
- [ ] Create `V1__init.sql` with all tables and constraints
- [ ] Fix date types (LocalDate vs LocalDateTime)
- [ ] Add `booking_rooms` table
- [ ] Add missing fields to `users` table
- [ ] Remove `ddl-auto: update`, use Flyway only
- [ ] Create seed data script (rooms + 365 days inventory)
--- : COMPILTED



### Phase 2: Core Booking Logic (Sprint 2)
- [ ] Implement transaction-safe `BookingService.createBooking()` with `FOR UPDATE`
- [ ] Create `PricingService` for base + seasonal rates
- [ ] Fix `SearchRoomService` to use `room_inventory` table
- [ ] Add `PENDING_PAYMENT` to BookingStatus enum
- [ ] Implement availability endpoint `GET /availability`
- [ ] Implement `POST /bookings` endpoint
- [ ] Add `booking_rooms` entity and repository

### Phase 3: Payments & Invoices (Sprint 2)
- [ ] Add Stripe SDK dependency
- [ ] Implement `POST /payments/create-intent`
- [ ] Implement `POST /payments/webhook` (Stripe webhook handler)
- [ ] Update booking status to CONFIRMED on payment success
- [ ] Add OpenPDF/iText dependency
- [ ] Implement PDF invoice generation
- [ ] Implement `GET /invoices/{bookingId}` endpoint
- [ ] Send invoice via email after confirmation

### Phase 4: API & Security (Sprint 2-3)
- [ ] Implement `POST /api/auth/login` (returns JWT)
- [ ] Fix SecurityConfig with proper `@PreAuthorize` annotations
- [ ] Add JWT filter to SecurityConfig
- [ ] Implement guest endpoints (`GET /bookings/my`, etc.)
- [ ] Implement role-based authorization
- [ ] Fix RoomController (change @PostMapping to @PutMapping for update)
- [ ] Add rates CRUD endpoints
- [ ] Add validation annotations

--- : COMPILTED
### Phase 5: Admin Features (Sprint 3)
- [ ] Complete admin dashboard stats (ADR, RevPoccupancy, AR, revenue)
- [ ] Implement booking modification logic
- [ ] Implement cancellation policies
- [ ] Add comprehensive admin endpoints~~
--- : COMPILTED

### Phase 6: Frontend (Future)
- [ ] Set up React + TypeScript project
- [ ] Implement authentication pages
- [ ] Implement availability search UI
- [ ] Implement booking checkout flow
- [ ] Integrate Stripe Elements
- [ ] Create admin dashboard UI

### Phase 7: DevOps & Testing
- [ ] Create `docker-compose.yml` for MySQL
- [ ] Add environment variable configuration
- [ ] Write unit tests
- [ ] Write integration tests (Testcontainers)
- [ ] Write API tests (MockMvc)

---

## 🔍 SPECIFIC CODE ISSUES TO FIX

1. **Booking.java**: `check_in` and `check_out` should be `LocalDate`, not `LocalDateTime`
2. **RoomInventory.java**: `night_date` should be `LocalDate`, not `LocalDateTime`
3. **Booking_nights.java**: `night_date` should be `LocalDate`, not `LocalDateTime`
4. **Rates.java**: `start_date` and `end_date` should be `LocalDate`, not `LocalDateTime`
5. **RoomController.java**: Line 23 has `@PostMapping` for update method, should be `@PutMapping`
6. **Booking.java**: Has direct `room_id` foreign key, but spec requires separate `booking_rooms` table for multi-room support
7. **AppUser.java**: Missing `full_name` and `created_at` fields
8. **BookingService.java**: `CreateBooking()` method not transaction-safe, doesn't lock inventory
9. **SearchRoomService.java**: Doesn't use `room_inventory` table at all

---

## 📊 COMPLETION ESTIMATE

- **Database Schema**: ~60% (most entities exist, but missing booking_rooms, wrong types, no Flyway)
- **Backend Services**: ~40% (basic structure, but core logic missing)
- **API Endpoints**: ~20% (very few endpoints, most missing)
- **Security**: ~50% (JWT filter exists, but no login endpoint, incomplete authorization)
- **Payments**: ~10% (Payment entity exists, but no Stripe integration)
- **Invoices**: ~20% (Invoice entity exists, but no PDF generation)
- **Frontend**: 0%
- **Testing**: ~5%
- **DevOps**: 0%

**Overall Backend Completion: ~30-35%**

