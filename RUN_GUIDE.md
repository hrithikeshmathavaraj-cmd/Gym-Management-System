# Run Guide — Read This First

## Why you were seeing errors on port 8081

Your backend runs on **port 8080** (set in `src/main/resources/application.properties`:
`server.port=8080`). Nothing in this project ever starts a server on 8081 — so
`localhost:8081/login.html` was never pointed at your app. That tab was either a
leftover from a different tool (Live Server, `http-server`, another project) or
an empty port your browser refused to open. Either way, it is not this project.

**The only correct URL, once the backend is running, is:**
```
http://localhost:8080/login.html
```
Never open `login.html` by double-clicking the file, and never serve `static/`
with a separate tool (Live Server, port 5500, etc). Spring Boot serves the
frontend AND the API from the same port — that's the whole point of putting the
HTML files under `src/main/resources/static/`.

## One-time setup in VS Code

1. Install **Java 21** (JDK), and confirm it: `java -version` should print `21`.
2. Install MongoDB locally, or have an Atlas URI ready.
3. Open the `gym-management` folder in VS Code (the one containing `pom.xml`) —
   not the parent zip folder.
4. Install the recommended extensions when VS Code prompts you (or manually
   install **Extension Pack for Java** — this includes Lombok annotation
   support, which this project needs to compile cleanly. Without it, VS Code's
   Problems panel will show false errors on every `@Data`/`@Builder` class even
   though the project builds fine with Maven).
5. Wait for the bottom-right "Java: Ready" / project import to finish before
   running anything — VS Code needs to index the Maven dependencies first.

## Running it

**Option A — VS Code Run/Debug (recommended)**
Open `GymManagementSystemApplication.java`, click the ▶ *Run* button above
`main()` (or press F5 — a launch config is already set up in `.vscode/launch.json`).

**Option B — Terminal**
```bash
cd gym-management
mvn spring-boot:run
```

Either way, wait for this in the terminal/Debug Console before opening a browser:
```
Tomcat started on port(s): 8080 (http)
Started GymManagementSystemApplication in X.XXX seconds
```

Then go to **http://localhost:8080/login.html**.

## Default logins (auto-created on first run)

| Role   | Login ID                        | Password       |
|--------|----------------------------------|----------------|
| Admin  | `admin`                          | `admin123`     |
| Member | the generated code, e.g. `GYM-2026-xxxxx` | `Member@123`  |
| Trainer| the generated code, e.g. `TRN-2026-xxxxx` | `Trainer@123` |

Member/Trainer Login IDs are shown in the success message when you (as admin)
create their record on the Members/Trainers page — write it down there.

## If MongoDB isn't running

The backend will fail to start with a `MongoTimeoutException` in the console.
Start MongoDB first (`mongod`, or your MongoDB Compass/service), *then* start
the backend.

## If port 8080 is already taken

Change `server.port=8080` in `application.properties` to something free (e.g.
`8090`), then always browse to `http://localhost:8090/login.html` instead —
whatever you set there is the one and only correct URL.
