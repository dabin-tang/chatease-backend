# ChatEase - Instant Messaging Backend

## Introduction
ChatEase is an Instant Messaging (IM) backend system built with Spring Boot 3 and WebSocket. It supports one-on-one and group chat, an admin dashboard, file uploads, and app version management.
By using REST APIs together with WebSocket connections, it supports real-time messaging and is easy to extend.

## Tech Stack

### Core Frameworks
* **Java**: JDK 17
* **Spring Boot 3.5.6**
* **Spring Data JPA**
* **Spring WebSocket**

### Security & Auth
* **JWT (jjwt)**: Stateless token management
* **Custom Interceptors**: Hand-written authentication logic (`JwtInterceptor`, `AdminInterceptor`) for securing API endpoints without the overhead of Spring Security.

### Data Storage & Middleware
* **MySQL 8.0**
* **Redis**: Used for caching verification codes and managing user ban lists (TTL support).

### Utilities
* **Lombok**: Boilerplate code reduction
* **SpringDoc OpenAPI (Swagger)**: API documentation generation
* **jBCrypt**: Password hashing
* **Commons-Validator**: Data validation
* **Java Mail Sender**: Email verification service
* **Jackson**: High-performance JSON processor

### Build & Dev Tools
* **Maven**
* **Git**
* **JUnit**

---

## Project Structure

```text
src/main/java/com/dbt/chatease
├── Config          # Global configurations (Web, Redis, WebSocket, Interceptors)
├── Controller      # RESTful API controllers (Entry points)
├── DTO             # Data Transfer Objects 
├── Entity          # Database entities (JPA mappings)
├── Exception       # Global exception handling
├── Handler         # WebSocket message handlers (Core messaging logic)
├── Repository      # Data access layer (DAO interfaces)
├── Service         # Business logic layer
│   └── impl        # Service implementations
├── Utils           # Utility classes (JWT, Snowflake ID, GlobalExceptionHandler, Result wrapper)
└── VO              # View Objects (response data)
```

---

# Don't Forget to Configure!!!

## 1. Database Configuration
1. Create a database named `chatease` in your MySQL server.
2. Import the SQL script to create necessary tables. (Under the resources folder)
3. **Update** `src/main/resources/application.yml` with your database credentials:

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/chatease?useSSL=false&serverTimezone=UTC
    username: root
    password: YOUR_PASSWORD
```

---

## 2. Redis Configuration

Ensure Redis is running on the default port `6379`.  
If you have a password, update `application.yml`:

```yaml
spring:
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      database: 0
```

---

## 3. File Storage Configuration

**This project uses Local Disk Storage by default.**

- Default path (Windows):  
  `C:\imgStore\`

### If you want to use Cloud Storage
Modify `UploadController.java` → `handleUpload()`  
Replace the local file saving logic with your Cloud SDK upload logic.

### If you stay with Local Disk Storage on Linux/Mac
You MUST change the paths in these files:

**UploadController.java**
- Update `BASE_STORE_DIR` to your Linux/Mac path (e.g. `/home/user/imgStore/`)

**WebConfig.java**
- Update the path in `addResourceHandlers()` to match the above

---
## 4. Email Configuration
To enable email verification, configure the email settings in `application.yml`:




## 👨‍💻 Author
* **GitHub**: https://github.com/dabin-tang
* **Email**: [dabint2003@gamil.com]

If you like this project, please give it a **Star** ⭐️!

