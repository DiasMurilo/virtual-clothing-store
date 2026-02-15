# Virtual Clothing Store - REST API

A Spring Boot REST API for managing customers, products, orders, and categories in a virtual clothing store.

## Technologies

- Java 21
- Spring Boot 3.2.0
- PostgreSQL
- Docker

## Quick Start

1. **Run with Docker Compose:**

   ```bash
   docker-compose up --build
   ```

2. **Access the API:**
   - Base URL: `http://localhost:8080`

## API Endpoints

### Customers

- `GET /api/customers` - Get all customers (paginated)
- `GET /api/customers/{id}` - Get customer by ID
- `POST /api/customers` - Create customer
- `PUT /api/customers/{id}` - Update customer
- `DELETE /api/customers/{id}` - Delete customer

### Products

- `GET /api/products` - Get all products (paginated)
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Categories

- `GET /api/categories` - Get all categories
- `GET /api/categories/{id}` - Get category by ID
- `POST /api/categories` - Create category
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category

### Orders

- `GET /api/orders` - Get all orders (paginated, optional date filter)
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/customer/{customerId}` - Get orders by customer
- `POST /api/orders?customerId={id}` - Create order
- `PUT /api/orders/{orderId}/products` - Add product to order
- `DELETE /api/orders/{orderId}/products/{productId}` - Remove product from order
- `PUT /api/orders/{id}` - Update order
- `DELETE /api/orders/{id}` - Delete order
