# 🏨 Hotel Reservation System

A comprehensive hotel booking management system built with Spring Boot, featuring role-based access control, PDF invoice generation, and RESTful API architecture.

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
- [API Endpoints](#-api-endpoints)
- [Security](#-security)
- [Database Schema](#-database-schema)
- [Contributing](#-contributing)
- [License](#-license)

## ✨ Features

### Core Functionality
- **Room Management**: Create, update, and manage hotel rooms with different categories
- **Booking System**: Complete reservation workflow from search to confirmation
- **User Management**: Role-based user registration and authentication (Guest, Staff, Admin)
- **Invoice Generation**: Automatic PDF invoice generation for bookings
- **Payment Processing**: Secure payment handling and tracking

### Role-Based Features

#### 👤 Guest
- Search and book available rooms
- View booking history
- Generate invoices for their bookings
- Update profile information

#### 👔 Staff
- Manage room inventory
- Process bookings
- Generate invoices for any booking
- View all reservations

#### 🔐 Admin
- Full system access
- User management
- System configuration
- Analytics and reporting

## 🛠 Tech Stack

### Backend
- **Java 17+**
- **Spring Boot 3.x**
  - Spring Security (JWT Authentication)
  - Spring Data JPA
  - Spring Web
- **PostgreSQL** - Primary database with PL/pgSQL stored procedures
- **Maven** - Dependency management

### Libraries & Tools
- **iText/PDFBox** - PDF generation for invoices
- **Lombok** - Reduce boilerplate code
- **MapStruct** - Object mapping
- **Hibernate Validator** - Input validation

## 🏗 Architecture

```
reservation_system/
├── src/main/java/
│   └── com/hotel/reservation/
│       ├── config/          # Security & App configuration
│       ├── controller/      # REST API endpoints
│       ├── dto/             # Data Transfer Objects
│       ├── entity/          # JPA entities
│       ├── exception/       # Custom exceptions
│       ├── repository/      # Data access layer
│       ├── security/        # JWT & Auth logic
│       ├── service/         # Business logic
│       └── util/            # Helper utilities
├── src/main/resources/
│   ├── application.yml      # Application configuration
│   └── db/migration/        # Database migrations
└── GAP_ANALYSIS.md          # Project analysis document
```

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- PostgreSQL 14+
- Maven 3.8+

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/MAX959954/reservation_system.git
cd reservation_system
```

2. **Configure database**

Create a PostgreSQL database and update `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/hotel_reservation
    username: your_username
    password: your_password
```

3. **Build the project**
```bash
mvn clean install
```

4. **Run the application**
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Default Users

After initial setup, you can create users with different roles:
- **Admin**: Full system access
- **Staff**: Booking and room management
- **Guest**: Room booking and personal bookings view

## 📡 API Endpoints

### Authentication
```
POST   /api/auth/register     # Register new user
POST   /api/auth/login        # Login and get JWT token
POST   /api/auth/refresh      # Refresh JWT token
```

### Bookings
```
GET    /api/bookings                    # Get all bookings (Staff/Admin)
GET    /api/bookings/{id}               # Get booking by ID
POST   /api/bookings                    # Create new booking
PUT    /api/bookings/{id}               # Update booking
DELETE /api/bookings/{id}               # Cancel booking
POST   /api/bookings/{id}/generate      # Generate PDF invoice
```

### Rooms
```
GET    /api/rooms              # Get all rooms
GET    /api/rooms/{id}         # Get room by ID
GET    /api/rooms/available    # Search available rooms
POST   /api/rooms              # Create room (Staff/Admin)
PUT    /api/rooms/{id}         # Update room (Staff/Admin)
DELETE /api/rooms/{id}         # Delete room (Admin)
```

### Users
```
GET    /api/users              # Get all users (Admin)
GET    /api/users/{id}         # Get user by ID
PUT    /api/users/{id}         # Update user profile
DELETE /api/users/{id}         # Delete user (Admin)
```

## 🔒 Security

### Authentication Flow
1. User registers or logs in with credentials
2. Server validates and returns JWT token
3. Client includes token in `Authorization: Bearer <token>` header
4. Server validates token for each protected endpoint

### Role-Based Authorization

Example endpoint security:
```java
@PreAuthorize("hasRole('GUEST') or hasRole('STAFF') or hasRole('ADMIN')")
@PostMapping("/booking/{bookingId}/generate")
public ResponseEntity<byte[]> generateInvoice(@PathVariable Long bookingId)
```

**Why this matters:**
- Prevents unauthorized access to sensitive operations
- Ensures guests can only access their own bookings
- Restricts administrative functions to authorized personnel
- Protects against data breaches and unauthorized modifications

## 🗄 Database Schema

### Main Entities

**Users**
- id, username, email, password (hashed)
- role (GUEST, STAFF, ADMIN)
- created_at, updated_at

**Rooms**
- id, room_number, category, price
- capacity, description
- available, created_at, updated_at

**Bookings**
- id, user_id, room_id
- check_in_date, check_out_date
- total_price, status
- payment_status, created_at

**Invoices**
- id, booking_id, invoice_number
- amount, generated_at
- pdf_path

### Database Features
- PL/pgSQL stored procedures for complex operations
- Proper indexing for performance
- Foreign key constraints for data integrity
- Audit columns (created_at, updated_at)

## 🧪 Testing

Run tests with:
```bash
mvn test
```

## 📈 Future Enhancements

- [ ] Email notifications for booking confirmations
- [ ] Payment gateway integration (Stripe/PayPal)
- [ ] Room availability calendar view
- [ ] Review and rating system
- [ ] Multi-language support
- [ ] Mobile app integration
- [ ] Analytics dashboard for admins

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**MAX959954**
- GitHub: [@MAX959954](https://github.com/MAX959954)

## 📞 Support

For support, please open an issue in the GitHub repository or contact the maintainers.

---

⭐ If you find this project helpful, please consider giving it a star!
