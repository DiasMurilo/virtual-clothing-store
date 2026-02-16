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

The API provides comprehensive CRUD operations for managing a virtual clothing store. Endpoints are organized by resource type with clear relationships between customers, orders, products, and categories.

### Customers

Customer management endpoints handle user accounts and their associated data.

- `GET /api/customers` - **List all customers** (paginated)
  - Returns paginated list of all customers with basic info
  - Use: Browse customer directory, admin dashboard

- `GET /api/customers/{id}` - **Get specific customer details**
  - Returns complete customer information by ID
  - Use: View customer profile, account management

- `POST /api/customers` - **Create new customer account**
  - Creates customer with validation (email uniqueness, required fields)
  - Use: User registration, customer onboarding

- `PUT /api/customers/{id}` - **Update customer information**
  - Modifies existing customer data (name, email, phone)
  - Use: Profile updates, account management

- `DELETE /api/customers/{id}` - **Remove customer account**
  - **⚠️ Cascading delete**: Removes customer AND all their orders
  - Use: Account deletion (use with caution)

### Products

Product catalog management with category relationships.

- `GET /api/products` - **Browse product catalog** (paginated)
  - Returns all products with category information
  - Use: Product listings, search results, inventory views

- `GET /api/products/{id}` - **Get detailed product information**
  - Returns complete product details including category
  - Use: Product detail pages, inventory management

- `POST /api/products` - **Add new product to catalog**
  - Creates product with category association and stock tracking
  - Use: Inventory management, new product launches

- `PUT /api/products/{id}` - **Update product information**
  - Modifies product details, pricing, or stock levels
  - Use: Price updates, inventory adjustments

- `DELETE /api/products/{id}` - **Remove product from catalog**
  - Removes product (only if not referenced in existing orders)
  - Use: Product discontinuation, catalog cleanup

### Categories

Product categorization system.

- `GET /api/categories` - **List all product categories**
  - Returns all available categories
  - Use: Category navigation, filter options

- `GET /api/categories/{id}` - **Get category details**
  - Returns category information
  - Use: Category management, navigation

- `POST /api/categories` - **Create new category**
  - Adds new product category
  - Use: Expand product catalog organization

- `PUT /api/categories/{id}` - **Update category**
  - Modifies category name/description
  - Use: Category reorganization

- `DELETE /api/categories/{id}` - **Remove category**
  - Removes category (only if no products are assigned)
  - Use: Catalog cleanup

### Orders

Order management with customer relationships and product line items.

- `GET /api/orders` - **List all orders** (paginated, optional date filtering)
  - Returns orders with optional date range filtering
  - Use: Order history, sales reporting, admin dashboard

- `GET /api/orders/{id}` - **Get complete order details**
  - Returns order with customer info and all line items
  - Use: Order details, fulfillment processing

- `GET /api/orders/customer/{customerId}` - **Get customer's order history**
  - Returns all orders for a specific customer (paginated)
  - Use: Customer order history, account dashboard

- `POST /api/orders?customerId={id}` - **Create new order for customer**
  - Creates order linked to existing customer
  - Body: Array of `{productId, quantity, price}` items
  - Use: Checkout process, order placement

- `PUT /api/orders/{orderId}/products` - **Add products to existing order**
  - Adds additional items to an order
  - Use: Order modification, upsell opportunities

- `DELETE /api/orders/{orderId}/products/{productId}` - **Remove product from order**
  - Removes specific item from order line items
  - Use: Order modification, item removal

- `PUT /api/orders/{id}` - **Update order information**
  - Modifies order details (status, dates, etc.)
  - Use: Order status updates, fulfillment tracking

- `DELETE /api/orders/{id}` - **Cancel/delete order**
  - Removes entire order
  - Use: Order cancellation, cleanup

## API Navigation Flow

### Customer → Orders Relationship
```
Customer Profile → Order History
     ↓               ↓
GET /api/customers/{id} → GET /api/orders/customer/{customerId}
     ↓
Order Creation
     ↓
POST /api/orders?customerId={id}
```

### Order → Products Relationship
```
Order Details → Product Information
     ↓               ↓
GET /api/orders/{id} → GET /api/products/{id}
     ↓
Modify Order Items
     ↓
PUT /api/orders/{orderId}/products (add)
DELETE /api/orders/{orderId}/products/{productId} (remove)
```

### Product → Category Relationship
```
Product Details → Category Information
     ↓               ↓
GET /api/products/{id} → GET /api/categories/{id}
```
