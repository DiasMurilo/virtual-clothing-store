# Virtual Clothing Store - REST API

A comprehensive REST API for a Virtual Clothing Store built with Spring Boot, demonstrating best practices in microservices architecture, entity relationships, DTOs, error handling, and pagination.

## Table of Contents
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Database Schema](#database-schema)
- [API Endpoints](#api-endpoints)
- [Getting Started](#getting-started)
- [Docker Setup](#docker-setup)
- [API Documentation](#api-documentation)
- [Error Handling](#error-handling)
- [Testing](#testing)

## Architecture

This application follows a layered architecture pattern:

```
Controller Layer (REST API)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
Database (PostgreSQL)
```

### Design Decisions
- **DTO Pattern**: Decouples internal entities from API contracts for better maintainability
- **Global Exception Handling**: Centralized error handling with consistent error responses
- **Pagination**: Efficient data retrieval for large datasets
- **Validation**: Input validation using Bean Validation API
- **RESTful Design**: Follows REST principles with proper HTTP status codes

## Technologies

- **Java 21**: LTS version for long-term support
- **Spring Boot 3.2.0**: Framework for rapid REST API development
- **Spring Data JPA**: Data access layer with Hibernate
- **PostgreSQL**: Relational database
- **Maven**: Build and dependency management
- **Docker**: Containerization for easy deployment

## Database Schema

### Entity-Relationship Diagram

```
+------------+       +------------+       +------------+
|  Customer  |       |    Order   |       | OrderItem  |
+------------+       +------------+       +------------+
| id (PK)    |<--1--| id (PK)     |<--1--| id (PK)     |
| firstName  |      | customer_id |      | order_id    |
| lastName   |      | orderDate   |      | product_id  |
| email      |      | totalAmount |      | quantity    |
| phone      |      | status      |      | price       |
| createdAt  |      +------------+       +------------+
+------------+              |                    |
                             |                    |
                             v                    v
                     +------------+       +------------+
                     |  Product   |       |  Category  |
                     +------------+       +------------+
                     | id (PK)    |       | id (PK)    |
                     | name       |       | name       |
                     | description|       | description|
                     | price      |       +------------+
                     | stockQty   |
                     | category_id|
                     +------------+
```

### Relationships
- **Customer ↔ Order**: One-to-Many (1:N)
- **Order ↔ OrderItem**: One-to-Many (1:N)
- **OrderItem → Product**: Many-to-One (N:1)
- **Product → Category**: Many-to-One (N:1)

## API Endpoints

### Customers
- `GET /api/customers` - Get all customers (paginated)
- `GET /api/customers/{id}` - Get customer by ID
- `POST /api/customers` - Create new customer
- `PUT /api/customers/{id}` - Update customer
- `DELETE /api/customers/{id}` - Delete customer

### Products
- `GET /api/products?page=0&size=10` - Get all products (paginated)
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create new product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Categories
- `GET /api/categories` - Get all categories
- `GET /api/categories/{id}` - Get category by ID
- `POST /api/categories` - Create new category
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category

### Orders
- `GET /api/orders?page=0&size=10&startDate=...&endDate=...` - Get all orders (paginated, optional date filter)
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/customer/{customerId}?page=0&size=10` - Get orders by customer (paginated)
- `POST /api/orders?customerId=1` - Create new order
- `PUT /api/orders/{orderId}/products?productId=1&quantity=2` - Add product to order
- `DELETE /api/orders/{orderId}/products/{productId}` - Remove product from order
- `PUT /api/orders/{id}` - Update order
- `DELETE /api/orders/{id}` - Delete order

## Getting Started

### Prerequisites
- Java 21
- Maven 3.6+
- PostgreSQL (or Docker)

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd virtual-clothing-store
   ```

2. **Set up PostgreSQL database**
   ```sql
   CREATE DATABASE clothingstore;
   CREATE USER postgres WITH PASSWORD 'password';
   GRANT ALL PRIVILEGES ON DATABASE clothingstore TO postgres;
   ```

3. **Update application.properties** (if needed)
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/clothingstore
   spring.datasource.username=postgres
   spring.datasource.password=password
   ```

4. **Build and run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **Access the API**
   - Base URL: `http://localhost:8080`
   - API Docs: `http://localhost:8080/swagger-ui.html` (if Swagger is added)

## Docker Setup

### Quick Start with Docker Compose

1. **Build and run with Docker Compose**
   ```bash
   docker-compose up --build
   ```

2. **Access the application**
   - API: `http://localhost:8080`
   - Database: `localhost:5432` (postgres/password)

### Manual Docker Build

```bash
# Build the image
docker build -t virtual-clothing-store .

# Run with PostgreSQL
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/clothingstore \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=password \
  virtual-clothing-store
```

## API Documentation

### Sample Requests & Responses

#### Create Customer
```bash
POST /api/customers
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+1234567890"
}
```

**Response:**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "createdAt": "2024-01-15T10:30:00"
}
```

#### Get Products (Paginated)
```bash
GET /api/products?page=0&size=5
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "T-Shirt",
      "description": "Cotton T-Shirt",
      "price": 19.99,
      "stockQuantity": 100,
      "categoryId": 1
    }
  ],
  "pageable": {
    "page": 0,
    "size": 5
  },
  "totalElements": 25,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

#### Create Order
```bash
POST /api/orders?customerId=1
Content-Type: application/json

[
  {
    "productId": 1,
    "quantity": 2,
    "price": 19.99
  }
]
```

**Response:**
```json
{
  "id": 1,
  "customerId": 1,
  "orderDate": "2024-01-15T10:30:00",
  "status": "PENDING",
  "totalAmount": 39.98,
  "orderItems": [
    {
      "id": 1,
      "productId": 1,
      "quantity": 2,
      "price": 19.99
    }
  ]
}
```

## Error Handling

The API uses consistent error response formats:

### Standard Error Response
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found with id: 999",
  "path": "/api/customers/999"
}
```

### Validation Error Response
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed",
  "path": "/api/customers",
  "fieldErrors": {
    "email": "Email should be valid",
    "firstName": "First name is required"
  }
}
```

### HTTP Status Codes
- `200 OK` - Successful request
- `201 Created` - Resource created
- `400 Bad Request` - Validation error or bad request
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### Manual Testing with curl

```bash
# Create a customer
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jane","lastName":"Smith","email":"jane@example.com","phone":"+1987654321"}'

# Get all products (paginated)
curl "http://localhost:8080/api/products?page=0&size=10"

# Create an order
curl -X POST "http://localhost:8080/api/orders?customerId=1" \
  -H "Content-Type: application/json" \
  -d '[{"productId":1,"quantity":1,"price":19.99}]'
```

## Challenges & Solutions

### 1. Entity Relationships
**Challenge**: Managing complex JPA relationships (Customer-Order-OrderItem-Product-Category)
**Solution**: Used proper cascade types and fetch strategies, implemented DTOs for API decoupling

### 2. Pagination Implementation
**Challenge**: Efficient pagination with filtering
**Solution**: Spring Data JPA Pageable interface with custom repository methods

### 3. Error Handling
**Challenge**: Consistent error responses across all endpoints
**Solution**: Global exception handler with custom exception classes

### 4. Docker Configuration
**Challenge**: Multi-service setup with database dependencies
**Solution**: Docker Compose with proper service dependencies and networking

### 5. Input Validation
**Challenge**: Comprehensive validation for all DTOs
**Solution**: Bean Validation API with custom error messages

## Future Enhancements

- [ ] Add Swagger/OpenAPI documentation
- [ ] Implement authentication and authorization
- [ ] Add caching layer (Redis)
- [ ] Implement event-driven architecture
- [ ] Add comprehensive test coverage
- [ ] API rate limiting
- [ ] Add search and filtering capabilities

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## GitHub Repository

[Link to GitHub Repository](https://github.com/your-username/virtual-clothing-store)