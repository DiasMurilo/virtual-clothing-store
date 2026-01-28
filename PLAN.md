# Virtual Clothing Store – Microservices REST API

Design and implement a Virtual Clothing Store RESTful API in Java, demonstrating best practices in microservices architecture, entity relationships, DTOs, error handling, and pagination. The project will be containerized with Docker and versioned on GitHub. The deliverable includes a concise report and a working demo.

## Steps
1. **Set up project structure**: Initialize a Java microservice project (Spring Boot recommended) with Docker support and GitHub repository.
2. **Design database schema**: Model entities:
    - `Customer` (parent)
    - `Order` (child, one-to-many with Customer)
    - `Product` (clothing items, many-to-many with Order)
    - `Category` (optional, for product grouping)
    - Create ERD.
3. **Implement REST endpoints**: CRUD for `Customer`, `Order`, and `Product` entities, including:
    - Retrieve all orders for a customer
    - Add/remove products to/from orders
    - Cascading deletes as appropriate
4. **Handle date fields**: Use consistent date formats (e.g., `YYYY-MM-DD`), validate inputs, and add endpoints for filtering/sorting orders by date.
5. **Create DTOs**: Define and use DTOs for all API responses and requests, decoupling internal models from API contracts.
6. **Add error handling & validation**: Implement global exception handling, input validation, and consistent error responses with proper HTTP status codes.
7. **Implement pagination**: Add pagination to product and order listing endpoints, document parameters, and include metadata in responses.
8. **Containerize with Docker**: Write Dockerfile and docker-compose for local development and demonstration.
9. **Write documentation/report**: Cover architecture, design decisions, API usage, sample responses, and challenges. Include ERD and GitHub link.

## Further Considerations
- **Framework**: Spring Boot is recommended for rapid REST API development and best practice support.
- **Database**: Use PostgreSQL or MySQL for relational data; configure via Docker Compose.
- **Optional**: Consider using Spinnaker for CI/CD if deployment automation is required, otherwise GitHub Actions may suffice.
- **Testing**: Add unit and integration tests for endpoints and error cases (optional but recommended).
- **API Documentation**: Use Swagger/OpenAPI for interactive API docs (optional but best practice).
