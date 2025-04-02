# CozyStay - Travel Service Marketplace

CozyStay is a comprehensive travel service marketplace that connects travelers with local service providers. This platform allows hosts to list various services including accommodations, transportation options, tours, activities, and more. Travelers can browse, book, and review these services.

## Table of Contents

[//]: # (- [System Requirements]&#40;#system-requirements&#41;)
- [Backend Setup](#backend-setup)
- [Frontend Setup](#frontend-setup)
- [Environment Configuration](#environment-configuration)
- [Running the Application](#running-the-application)
- [Key Features](#key-features)
- [Architecture Overview](#architecture-overview)
- [API Documentation](#api-documentation)

## System Requirements

### Backend
- Java 17 or higher
- Maven 3.8+
- PostgreSQL 14+ (or MySQL 8+)
- Redis (optional, for caching)

### Frontend
- Node.js 18+
- npm 10+ or Yarn 1.22+
- Next.js 15+

## Backend Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-organization/cozystay-backend.git
   cd cozystay-backend
   ```

2. **Configure database**

   Create a PostgreSQL database:
   ```sql
   CREATE DATABASE cozystay;
   CREATE USER cozystay_user WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE cozystay TO cozystay_user;
   ```

3. **Configure application.properties**

   Update `src/main/resources/application.properties` with your database credentials:
   ```properties
   # Database Configuration
   spring.datasource.url=jdbc:postgresql://localhost:5432/cozystay
   spring.datasource.username=cozystay_user
   spring.datasource.password=your_password
   
   # JPA Configuration
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
   spring.jpa.properties.hibernate.format_sql=true
   
   # File Upload Configuration
   spring.servlet.multipart.max-file-size=10MB
   spring.servlet.multipart.max-request-size=50MB
   
   # JWT Configuration
   jwt.secret=your_jwt_secret_key
   jwt.expiration=86400000
   
   # File Storage Configuration
   file.upload-dir=./uploads
   ```

4. **Build the application**
   ```bash
   mvn clean install -DskipTests
   ```

5. **Run database migrations (if using Flyway)**
   ```bash
   mvn flyway:migrate
   ```

6. **Start the backend server**
   ```bash
   mvn spring-boot:run
   ```

   The backend server will start on http://localhost:8080

## Frontend Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-organization/cozystay-frontend.git
   cd cozystay-frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   # or
   yarn install
   ```

3. **Configure environment variables**

   Create a `.env.local` file in the root directory:
   ```env
   NEXT_PUBLIC_API_URL=http://localhost:808/api/v1
   NEXT_PUBLIC_UPLOAD_URL=http://localhost:8080/uploads
   NEXT_PUBLIC_GOOGLE_MAPS_API_KEY=your_google_maps_api_key
   ```

4. **Start the development server**
   ```bash
   npm run dev
   # or
   yarn dev
   ```

   The frontend application will be available at http://localhost:3000

## Environment Configuration

### Production Deployment

For production deployment, additional configuration is needed:

1. **Backend Production Config**

   Create a `application-prod.properties` file:
   ```properties
   # Production Database
   spring.datasource.url=jdbc:postgresql://production-db-host:5432/cozystay
   spring.datasource.username=prod_user
   spring.datasource.password=prod_password
   
   # Logging
   logging.level.root=WARN
   logging.level.com.cozystay=INFO
   
   # CORS Configuration
   cors.allowed-origins=https://cozystay.com
   
   # File Storage (cloudinary)
   cloudinary.cloud-name=${CLOUD_NAME}
    cloudinary.api-key=${API_KEY}
    cloudinary.api-secret=${API_SECRET}
   ```

2. **Frontend Production Build**
   ```bash
   npm run build
   # or
   yarn build
   ```

3. **Run Production Build**
   ```bash
   npm start
   # or
   yarn start
   ```

## Running the Application

### Development Mode

1. Start the backend server:
   ```bash
   cd cozystay-backend
   mvn spring-boot:run
   ```

2. Start the frontend development server:
   ```bash
   cd cozystay-frontend
   npm run dev
   ```

3. Access the application at http://localhost:3000

### Production Mode

1. Start the backend server with production profile:
   ```bash
   cd cozystay-backend
   java -jar target/cozystay-backend.jar --spring.profiles.active=prod
   ```

2. Serve the frontend build with a web server like Nginx or deploy to a platform like Vercel.

## Key Features

The CozyStay platform includes the following key features:

### For Travelers
- Browse and search services by location, type, price, and ratings
- Make bookings with real-time availability checking
- Manage bookings (view, cancel, modify)
- User authentication and profile management

### For Service Providers
- Create and manage service listings
- Set availability and pricing
- Handle booking requests
- View dashboard with analytics and earnings

### Admin Features
- User management
- Service verification
- Category and location management
- System monitoring and reporting

## Architecture Overview

The application follows a modern, scalable architecture:

### Backend
- Spring Boot REST API
- Spring Security with JWT authentication
- Spring Data JPA for database access
- Hibernate ORM
- Model-Service-Repository pattern
- Exception handling middleware
- File upload management

### Frontend
- Next.js React framework
- TypeScript for type safety
- Component-based architecture
- Context API for state management
- Responsive design with Tailwind CSS
- RESTful API client

## API Documentation

The API is documented using Swagger/OpenAPI. After starting the backend server, access the API documentation at:

http://localhost:8081/api/v1/swagger-ui/index.html#

Key API endpoints include:

| Endpoint                    | Method | Description |
|-----------------------------|--------|-------------|
| `/api/v1/auth/register`     | POST | Register a new user |
| `/api/v1/auth/login`           | POST | Authenticate and get JWT token |
| `/api/v1/services`             | GET | Get all services with filtering options |
| `/api/v1/services/{id}`        | GET | Get a specific service by ID |
| `/api/v1/services`             | POST | Create a new service (requires provider role) |
| `/api/v1/bookings`             | GET | Get current user's bookings |
| `/api/v1/bookings`             | POST | Create a new booking |
| `/api/v1/bookings/{id}/status` | PATCH | Update booking status |
For a complete list of endpoints, refer to the API documentation.

## Contributors

- Gershom Nsegiyumva : Developer
