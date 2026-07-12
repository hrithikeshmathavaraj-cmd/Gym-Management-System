# FitCore — Gym Membership Management System

An enterprise-style gym membership management system built with **Spring Boot 3 / Java 21 / MongoDB** on the backend and a **Bootstrap 5 + Chart.js** premium dashboard on the frontend.

## Status: feature-complete core system

### Backend — all 9 modules complete
| Module | Entity | Repository | Service | Controller |
|---|---|---|---|---|
| Auth (register/login/forgot password) | User | ✅ | ✅ | ✅ |
| Members (CRUD, search, pagination, renewal, QR) | Member | ✅ | ✅ | ✅ |
| Plans | Plan | ✅ | ✅ | ✅ |
| Payments | Payment | ✅ | ✅ | ✅ |
| Attendance (QR check-in/out) | Attendance | ✅ | ✅ | ✅ |
| Trainers | Trainer | ✅ | ✅ | ✅ |
| Equipment | Equipment | ✅ | ✅ | ✅ |
| Notifications | Notification | ✅ | ✅ | ✅ |
| Settings | Settings | ✅ | ✅ | ✅ |
| Reports / Dashboard analytics | — | ✅ | ✅ | ✅ |
| Excel / PDF export | — | — | ✅ | ✅ |
| QR code generation | — | — | ✅ | ✅ |
| Scheduled expiry checks + reminders | — | — | ✅ (nightly cron) | — |

Also included: JWT auth + role-based security (Admin/Receptionist/Trainer/Member), global exception handler, Bean Validation, Swagger/OpenAPI docs, MapStruct mapping, SLF4J logging.

### Frontend — all 13 pages complete
- `login.html` / `register.html` — glassmorphism split-screen auth
- `dashboard.html` — live stat cards + Chart.js revenue/attendance/membership charts + recent payments
- `members.html` — full CRUD, search, pagination, add/edit modal
- `plans.html` — plan cards with CRUD
- `payments.html` — payment history + record-payment modal + revenue stats
- `attendance.html` — QR check-in/out simulation, live QR preview, today's attendance table
- `trainers.html` — trainer roster cards with CRUD
- `equipment.html` — inventory table with condition/status tracking
- `reports.html` — Excel/PDF export buttons, expiring & expired member widgets
- `notifications.html` — notification feed with mark-as-read
- `settings.html` — admin gym settings + dark/light mode toggle
- `profile.html` — current user's profile view/edit

All pages share one design system (`css/style.css`), a JWT-aware fetch wrapper with toast notifications (`js/app.js`), and a common sidebar/topbar shell (`js/layout.js`) — fully responsive down to mobile, with dark mode.

### Real-time dashboard & UPI payments (new)
- **UPI QR checkout**: the Payments page has a "Pay via UPI" card. Staff pick a member, enter the amount, and a scannable UPI QR is generated server-side (encoding a standard `upi://pay?...` deep link — works with GPay, PhonePe, Paytm, BHIM, any UPI app). The UPI ID is configurable in `application.properties` (`app.upi.id`, `app.upi.payee-name`) — currently set to `hrithikeshmathan-1@okicici`.
  - **Important limitation, stated plainly:** a personal UPI QR code has no callback to tell any server a payment succeeded — that requires a licensed payment gateway (Razorpay, Cashfree, PayU, etc.) with webhooks. This app's flow is: member scans and pays on their own phone → staff taps "Confirm Payment Received" → the record is written to MongoDB and broadcast instantly. If you need fully automatic bank-side confirmation, swap this QR flow for a gateway integration (the `PaymentService` interface is already the right seam for that).
- **Real-time dashboard**: the backend uses Spring WebSocket (STOMP over SockJS, `/ws` endpoint). Every payment, check-in/out, or member create/update/delete/renewal immediately triggers a broadcast of fresh dashboard stats to `/topic/dashboard`. `dashboard.html` subscribes on load and updates its stat cards and all three charts in place, with no page refresh — you'll see a "Live" badge at the top of the dashboard. If the socket can't connect (e.g. restrictive proxy), it automatically falls back to polling every 20 seconds and keeps retrying the socket connection.
- **MongoDB is always the source of truth**: every create/update/delete in this system (members, payments, attendance, plans, trainers, equipment, settings) writes straight to MongoDB via Spring Data — there is no in-memory cache to go stale. The WebSocket layer only broadcasts a read-only snapshot after each write; it never bypasses the database.

### Testing
- `postman/FitCore-Gym-Management.postman_collection.json` — a ready-to-import Postman collection covering every module (auth token is captured automatically on login).
- Swagger UI at `/swagger-ui.html` for interactive API testing.


---

### Login system — Login ID + password, no email anywhere (new)
Every account (Admin, Receptionist, Trainer, Member) signs in with a **Login ID** and password — never an email address.

| Role | Login ID | How it's created |
|---|---|---|
| Admin | `admin` | Seeded automatically on first startup — password `admin123` |
| Receptionist / additional Admins | chosen at signup on `/register.html` | Self-registered by staff |
| Trainer | auto-generated code, e.g. `TRN-2026-48213` | Created automatically the moment a trainer is added on the Trainers page |
| Member | auto-generated code, e.g. `GYM-2026-77410` | Created automatically the moment a member is added on the Members page |

- **Default admin account**: the app seeds `admin` / `admin123` on first run (see `DataInitializer.java`). Log in with this immediately — no registration step needed. Change the password afterward via "Forgot password?" on the login page.
- **Member/Trainer accounts**: when staff add a member or trainer, the backend auto-creates a matching login account (`Role.MEMBER` / `Role.TRAINER`) using their generated code as the Login ID and a default password (`Member@123` / `Trainer@123`, configurable in `application.properties`). The UI shows these credentials once in a popup right after creation — write them down or share them with the person, since they aren't shown again automatically (use "Forgot password?" to reset if lost).
- Members and Trainers log in at the same `/login.html` — the backend doesn't care which "kind" of Login ID it is, it just checks the ID + password and returns the right role, and the app already routes by role via Spring Security.

### Main class — where everything connects
```
src/main/java/com/gymmanagement/GymManagementSystemApplication.java
```
This is the Spring Boot entry point, annotated with `@SpringBootApplication`. That single annotation is what wires the whole app together: Spring Boot component-scans every class under the `com.gymmanagement` package and its subpackages (`config`, `controller`, `dto`, `entity`, `exception`, `mapper`, `repository`, `security`, `service`, `serviceImpl`, `util`), auto-detects everything annotated `@RestController`, `@Service`, `@Repository`, `@Component`, `@Configuration`, etc., and injects them into each other via constructor injection (`@RequiredArgsConstructor` from Lombok). You don't need to manually register or import classes anywhere — dropping a new `@Service`/`@Controller` class anywhere under `com.gymmanagement` is enough for Spring to pick it up automatically.


- Java 21+
- Maven 3.9+
- MongoDB 6+ (local) or a MongoDB Atlas cluster

## MongoDB Setup

**Local:**
```bash
# macOS (Homebrew)
brew tap mongodb/brew
brew install mongodb-community@7.0
brew services start mongodb-community@7.0
```
The app defaults to `mongodb://localhost:27017/gymdb` — no changes needed for local dev.

**MongoDB Atlas (cloud):**
1. Create a free cluster at https://www.mongodb.com/cloud/atlas
2. Create a database user and allow your IP (or `0.0.0.0/0` for testing)
3. Copy the connection string and update `src/main/resources/application.properties`:
```properties
spring.data.mongodb.uri=mongodb+srv://<username>:<password>@cluster0.mongodb.net/gymdb?retryWrites=true&w=majority
```

## Backend Setup & Run
```bash
cd gym-management
mvn clean install
mvn spring-boot:run
```
The API starts on **http://localhost:8080**.

## Frontend Setup
The frontend is plain HTML/CSS/JS served as static resources — no build step. Once the backend is running, open:
```
http://localhost:8080/login.html
```
Register a new account (choose role `ADMIN` for full access), then you'll land on the dashboard.

## API Documentation
Swagger UI: **http://localhost:8080/swagger-ui.html**
OpenAPI JSON: **http://localhost:8080/api-docs**

## Key REST Endpoints
```
POST   /api/auth/register        { "name", "username", "password", "role" }
POST   /api/auth/login           { "username", "password" }
POST   /api/auth/forgot-password { "username", "newPassword" }

GET    /api/members?page=0&size=10&sortBy=createdAt&direction=desc
GET    /api/members/search?keyword=john
POST   /api/members
PUT    /api/members/{id}
DELETE /api/members/{id}
POST   /api/members/{id}/renew?planId={planId}
GET    /api/members/{id}/qrcode

GET    /api/plans
POST   /api/plans

POST   /api/payments
GET    /api/payments/member/{memberId}

POST   /api/attendance/check-in?memberId={id}
POST   /api/attendance/check-out?memberId={id}
GET    /api/attendance/today

GET    /api/reports/dashboard
GET    /api/reports/members/expiring?withinDays=7
GET    /api/reports/export/members/excel
GET    /api/reports/export/members/pdf

GET    /api/trainers
GET    /api/equipment
GET    /api/notifications
GET    /api/admin/settings   (ADMIN only)
```

## Deployment Notes
- **MongoDB Atlas**: see setup above.
- **Backend (Render)**: push this repo to GitHub, create a new Render "Web Service", set build command `mvn clean package -DskipTests` and start command `java -jar target/gym-management-system.jar`, and add the `spring.data.mongodb.uri` as an environment variable.
- **Frontend (Netlify)**: since the frontend is currently served by Spring Boot as static resources, if you want it hosted separately on Netlify, update `API_BASE` in `js/app.js` to point at your deployed backend URL, then deploy the `src/main/resources/static` folder as a Netlify site.
- **GitHub**: standard `git init && git add . && git commit -m "Initial commit"` then push to a new repository.

## Troubleshooting

**`ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN`**
Lombok/JDK version mismatch — usually means your installed JDK is newer than Lombok supports. Fixed in this project by pinning `lombok.version=1.18.36` in `pom.xml`. If you still hit this, run `java -version`; if it's JDK 23+, either update the Lombok version further or install JDK 21 specifically (this project targets Java 21) and point `JAVA_HOME` at it.

**`incompatible types: org.apache.poi.ss.usermodel.Font cannot be converted to com.lowagie.text.Font`**
This was a real bug in an earlier build of this project (fixed) — `ExportServiceImpl.java` imported `Font` from both Apache POI (Excel) and OpenPDF (PDF) via a wildcard + explicit import, and Java resolved the ambiguous name to the wrong one in the Excel methods. Fixed by fully qualifying `org.apache.poi.ss.usermodel.Font` in the Excel export methods. If you see this again after editing that file, check for the same ambiguous-import pattern.

**Login fails with "Invalid Login ID or password" for the admin account**
Confirm the app actually started successfully — the admin account is seeded on first boot only (see `DataInitializer.java`). Check your startup logs for a line like `Default admin account created — Login ID: admin  Password: admin123`. If you don't see it, the app may have failed to start, or an `admin` user already existed in your database from a previous run — check MongoDB directly (`db.users.find({username: "admin"})`) if the password isn't working.


1. Unit/integration tests (JUnit + Testcontainers for MongoDB)
2. Image upload wiring for profile pictures (backend field exists; add a multipart upload endpoint + UI file input)
3. Refresh tokens / token rotation
4. CSV export alongside Excel/PDF
5. Dedicated screenshots section once the app is run locally
