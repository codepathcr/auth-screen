**Auth Screen**

A small Java Swing authentication demo. This README explains how to build and run the app, create the required PostgreSQL database `pswe06`, and where to find the SQL script.

**Requirements:**
- **JDK:** Java 11+ (the project was tested with JDK 21).
- **PostgreSQL:** running locally or accessible from this machine.
- **JDBC driver:** PostgreSQL JDBC jar in `lib/` OR use Maven to fetch dependencies.
- **Shell:** Windows PowerShell examples below.

**Project layout (important files):**
- `src/main/java/com/auth/` : Java sources (UI and DB code).
- `dbScript/auth.sql` : SQL script to create the `usuarios` table and sample data.
- `lib/` : (optional) drop `postgresql-<version>.jar` here if not using Maven.
- `pom.xml` : Maven POM (contains dependency for PostgreSQL driver).

**1) Prepare the database**

Open a PowerShell session and use `psql` (or pgAdmin) to create the DB and user. Replace `myuser` and `mypassword` with your values.

PowerShell / psql example:
```powershell
# Connect as the postgres superuser (you may need to provide the postgres password)
psql -U postgres

# In psql prompt, run:
CREATE DATABASE pswe06;
CREATE USER myuser WITH PASSWORD 'mypassword';
GRANT ALL PRIVILEGES ON DATABASE pswe06 TO myuser;
\q
```

Load the schema/data from the repository `dbScript/auth.sql` into `pswe06`:
```powershell
psql -U myuser -d pswe06 -f dbScript/auth.sql
```

If you prefer to run the SQL from inside `psql` interactive mode, open `psql -U myuser -d pswe06` and run `\i dbScript/auth.sql`.

**2) Provide DB credentials to the app**

The app reads database configuration from environment variables if present, otherwise it uses sensible defaults in `DbConnection.java`.

- Option A (recommended): set environment variables in PowerShell before launching the app:
```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/pswe06'
$env:DB_USER = 'myuser'
$env:DB_PASSWORD = 'mypassword'
```

- Option B (less secure): edit `src/main/java/com/auth/DbConnection.java` and change the default values (not recommended for production).

**3) Ensure JDBC driver is available**

- If you use Maven, run (if `mvn` is available):
```powershell
#maven: copy dependencies to target/dependency
mvn dependency:copy-dependencies -DoutputDirectory=target/dependency
```
Then run the app with `target/dependency/*` on the classpath.

- If you don't use Maven: download the PostgreSQL JDBC jar (example: `postgresql-42.6.0.jar`) and place it in the project's `lib/` folder.

**4) Compile and run (PowerShell examples)**

- Using system `javac/java` and `lib/*` (no Maven):
```powershell
# from project root
Remove-Item -Recurse -Force target -ErrorAction SilentlyContinue
javac -d target/classes src/main/java/com/auth/*.java
java -cp "target/classes;lib/*" com.auth.App
```

- If you have Maven and want to build with it (recommended for dependency management):
```powershell
mvn package
# or to copy dependencies
mvn dependency:copy-dependencies -DoutputDirectory=target/dependency
java -cp "target/classes;target/dependency/*" com.auth.App
```

**5) Troubleshooting**
- Error: "No suitable driver found for jdbc:postgresql://..." — make sure the PostgreSQL JDBC jar is on the runtime classpath (either `lib/*` or `target/dependency/*`).
- Error: "The server requested SCRAM-based authentication, but the password is an empty string." — ensure `$env:DB_PASSWORD` (or the value in `DbConnection.java`) is set and matches the PostgreSQL user's password.
- Error connecting? Check that PostgreSQL is running, listening on `localhost:5432`, and that `pg_hba.conf` allows local password authentication (SCRAM/MD5).

**6) Where to find things in this repo**
- Entry point: `src/main/java/com/auth/App.java` (main class `com.auth.App`).
- UI: `src/main/java/com/auth/AuthFrame.java`.
- DB connection helper: `src/main/java/com/auth/DbConnection.java` (reads `DB_URL`, `DB_USER`, `DB_PASSWORD` env vars by default).
- SQL schema/script: `dbScript/auth.sql`.
