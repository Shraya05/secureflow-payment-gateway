# 💳 SecureFlow - Streamlined Payment Gateway

SecureFlow is a robust and secure payment gateway system built with **Spring Boot**, integrated with **PayPal REST APIs**, and backed by **PostgreSQL**. It provides a streamlined and reliable way to create, approve, execute, and track payments online, ensuring safe transaction processing and seamless user experience.

---

## 🚀 Features

- 🔐 **JWT Authentication** – Secure user login and role-based access control
- 💸 **PayPal Integration** – Payment creation, approval, execution, and cancellation
- 🧾 **Transaction Logging** – All payment records stored in PostgreSQL with user reference
- 📡 **RESTful APIs** – Clean and modular endpoint structure
- 📊 **Real-Time Status** – Tracks payment states and updates them post-execution
- 🧠 **Developer Friendly** – Logs, exception handling, and modular service-layer architecture

---

## 🧰 Tech Stack

| Category         | Technology             |
|------------------|-------------------------|
| Language         | Java 17                |
| Framework        | Spring Boot 3.x        |
| Security         | Spring Security, JWT   |
| Database         | PostgreSQL             |
| External API     | PayPal REST SDK        |
| Caching (optional)| Redis                  |
| Build Tool       | Maven                  |
| Logging          | SLF4J, Logback         |
| Others           | Lombok, RestTemplate   |

---

## 🔐 Authentication APIs

| Method | Endpoint              | Description           |
|--------|------------------------|------------------------|
| POST   | `/api/auth/register`   | Register a new user   |
| POST   | `/api/auth/login`      | Login and get JWT     |

---

## 💳 Payment APIs

| Method | Endpoint                  | Description                                 |
|--------|---------------------------|---------------------------------------------|
| POST   | `/api/payment/create`     | Create a PayPal payment                     |
| GET    | `/api/payment/success`    | Execute payment after approval              |
| GET    | `/api/payment/cancel`     | Handle payment cancellation                 |

> 🔐 All payment APIs are protected and require a valid JWT token in the Authorization header.

---

## 📦 Sample Request Payload for Payment Creation

```json
{
  "amount": 15.99,
  "currency": "USD",
  "description": "SecureFlow test payment",
  "cancelUrl": "http://localhost:8080/api/payment/cancel",
  "successUrl": "http://localhost:8080/api/payment/success"
}

**##🧪 Testing**
Use Postman to test endpoints with the JWT in the Authorization tab:

Type: Bearer Token
Token: <your-jwt-token>

Contact
Made with ❤️ by Shraya

📧 Email: shraya978@example.com

🔗 LinkedIn: https://linkedin.com/in/shraya555

💻 GitHub: https://github.com/Shraya05


