# 🐳 Docker Containerization Guide

This guide explains how to run the Reservation System using Docker and Docker Compose.

## 📋 Prerequisites

- Docker Desktop installed and running
- At least 4GB RAM available
- Git (for cloning the repository)

## 🚀 Quick Start

### 1. Clone and Setup
```bash
git clone <your-repository-url>
cd reservation_system
```

### 2. Configure Environment Variables
```bash
# Copy the example environment file
cp .env.example .env

# Edit .env with your actual Stripe keys
nano .env
```

**Required Environment Variables:**
- `STRIPE_SECRET_KEY`: Your Stripe secret key (starts with `sk_`)
- `STRIPE_WEBHOOK_SECRET`: Your Stripe webhook secret (starts with `whsec_`)
- `STRIPE_PUBLISHABLE_KEY`: Your Stripe publishable key (starts with `pk_`)

### 3. Run with Docker Compose
```bash
# Build and start all services
docker-compose up --build

# Or run in detached mode
docker-compose up --build -d
```

## 🏗️ Architecture

### Services Overview

| Service | Image | Port | Description |
|----------|--------|------|-------------|
| `postgres` | postgres:15-alpine | 5432 | PostgreSQL database |
| `mailhog` | mailhog/mailhog | 1025, 8025 | Email testing service |
| `reservation-app` | Custom build | 8080 | Spring Boot application |

### Network Configuration
- All services communicate via `reservation-network` bridge network
- Database is only accessible within the network
- MailHog catches all outgoing emails for development

## 📁 Volume Management

| Volume | Purpose | Host Path |
|---------|-----------|------------|
| `postgres_data` | Database persistence | Docker-managed |
| `app_logs` | Application logs | Docker-managed |
| `app_invoices` | Generated PDFs | Docker-managed |

## 🔧 Development Workflow

### Access Points

Once running, access your application at:

- **🌐 Main Application**: http://localhost:8080
- **📧 MailHog Web UI**: http://localhost:8025
- **🗄️ Database**: localhost:5432 (with your credentials)

### Health Checks
```bash
# Check application health
curl http://localhost:8080/actuator/health

# Check database health
docker exec reservation-postgres pg_isready -U postgres -d registration_hotel

# View logs
docker-compose logs -f reservation-app
```

## 🛠️ Development Commands

### Build Only
```bash
# Build the Docker image
docker build -t reservation-system .

# Build with no cache
docker build --no-cache -t reservation-system .
```

### Run Individual Services
```bash
# Run database only
docker-compose up postgres

# Run application only (requires external DB)
docker-compose up reservation-app
```

### Debug Mode
```bash
# Run with remote debugging enabled
docker-compose up --build
# Then connect debugger to localhost:5005
```

## 🔄 Production Deployment

### Environment Configuration
```bash
# Production environment variables
STRIPE_SECRET_KEY=sk_live_your_production_key
STRIPE_WEBHOOK_SECRET=whsec_your_production_webhook_secret
STRIPE_PUBLISHABLE_KEY=pk_live_your_production_key
SPRING_PROFILES_ACTIVE=prod
```

### Production Docker Compose
```bash
# Use production compose file
docker-compose -f docker-compose.prod.yml up --build
```

## 🔍 Troubleshooting

### Common Issues

**Port Conflicts:**
```bash
# Check what's using port 8080
netstat -tulpn | grep :8080

# Kill conflicting processes
sudo kill -9 <PID>
```

**Database Connection Issues:**
```bash
# Check database logs
docker-compose logs postgres

# Restart database
docker-compose restart postgres
```

**Build Issues:**
```bash
# Clean build
docker-compose down --volumes
docker system prune -f
docker-compose up --build
```

### Performance Optimization

```bash
# Limit memory usage
docker-compose up --build --scale reservation-app=1

# Monitor resource usage
docker stats
```

## 📊 Monitoring

### Application Logs
```bash
# Follow application logs
docker-compose logs -f reservation-app

# View last 100 lines
docker-compose logs --tail=100 reservation-app
```

### Database Monitoring
```bash
# Connect to database
docker exec -it reservation-postgres psql -U postgres -d registration_hotel

# View database size
docker exec reservation-postgres psql -U postgres -d registration_hotel -c "SELECT pg_size_database('registration_hotel');"
```

## 🔒 Security Considerations

- **Never commit `.env` file** to version control
- **Use production keys** in production environments
- **Database password** should be strong in production
- **MailHog** is for development only
- **HTTPS** should be used in production

## 📝 Additional Configuration

### Custom Application Properties
```yaml
# application-docker.yml (overrides application.yml)
spring:
  jpa:
    show-sql: false  # Disable in production
    properties:
      hibernate:
        format_sql: false
logging:
  level:
    com.example.reservation_system: INFO
    org.springframework.security: DEBUG
```

### Custom Docker Compose
```yaml
# docker-compose.override.yml (for local development)
version: '3.8'
services:
  reservation-app:
    environment:
      SPRING_JPA_SHOW_SQL: "true"
      SPRING_PROFILES_ACTIVE: dev,debug
    ports:
      - "8081:8080"  # Different port
    volumes:
      - ./src:/app/src  # Mount source for hot reload
```

## 🚀 Next Steps

1. **Set up your Stripe keys** in `.env` file
2. **Run `docker-compose up --build`**
3. **Access the application** at http://localhost:8080
4. **Test the booking flow** with Stripe integration
5. **Check email functionality** at http://localhost:8025

## 📞 Support

For issues with Docker setup:
1. Check this guide's troubleshooting section
2. Review Docker logs: `docker-compose logs`
3. Verify environment variables
4. Ensure Docker Desktop is running properly
