# PageNest

PageNest is an Online Bookstore Management System developed as an individual SE1020 university OOP project. It has a public book collection and a role-protected, read-only admin workspace.

## Technology stack

- Java 21
- Spring Boot 4.1.1
- Maven
- Spring MVC
- Thymeleaf
- Spring Security (session-based login and role checks)
- Plain HTML, CSS, and JavaScript

## Requirements

- JDK 21 or a newer JDK that can compile Java 21 projects
- Maven is downloaded automatically by the included Maven Wrapper.
- Set both local passwords before starting. The application deliberately fails to start if either is missing.

## Local development accounts

The in-memory usernames are `admin` (ADMIN role) and `customer` (CUSTOMER role). Choose your own passwords; do not put them in source files, screenshots, commits, or shared run configurations. In PowerShell, set them for the current terminal session:

```powershell
$env:PAGENEST_ADMIN_PASSWORD = '<your private admin password>'
$env:PAGENEST_CUSTOMER_PASSWORD = '<your private customer password>'
```

These values last only for that PowerShell session. For IntelliJ IDEA, open **Run > Edit Configurations**, select `PageNestApplication`, and set `PAGENEST_ADMIN_PASSWORD` and `PAGENEST_CUSTOMER_PASSWORD` under **Environment variables**. Keep the run configuration local and never commit it. In VS Code, launch Maven from the same PowerShell terminal after setting the variables.

## Run from the command line

Open a terminal in the project directory and run:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS or Linux, use `./mvnw spring-boot:run` instead.

Then open <http://localhost:8080> in a web browser.

## Run in IntelliJ IDEA

1. Open IntelliJ IDEA and select **Open**.
2. Select the project folder containing `pom.xml`.
3. Allow IntelliJ IDEA to import the Maven project and download its dependencies.
4. Select JDK 21 (or a newer installed JDK) as the Project SDK and configure the two environment variables above.
5. Open `PageNestApplication.java` and run its `main` method.
6. Visit <http://localhost:8080>.

## Run in Visual Studio Code

1. Install the **Extension Pack for Java** and **Spring Boot Extension Pack**.
2. Open the project folder in VS Code.
3. Confirm that VS Code is using JDK 21 or newer.
4. Set the two environment variables in the integrated PowerShell terminal, then run `.\mvnw.cmd spring-boot:run` on Windows.
5. Visit <http://localhost:8080>.

## Current functionality

- Starts as a Spring Boot web application on port 8080.
- Serves a responsive Thymeleaf homepage at `/`.
- Lists eight sample books at `/books`, with title/author search, category filtering, and name/price sorting.
- Shows book details at `/books/{id}` and a friendly 404 page for unknown IDs.
- Uses one in-memory sample collection in `BookService` for the homepage, catalog, and detail pages.
- Provides a secured `/admin` workspace for ADMIN accounts, with an inventory summary, searchable table, add/edit form previews, and a non-destructive delete dialog.
- Uses `/login` for form login. ADMIN goes to `/admin`; CUSTOMER goes to `/books`. Logout uses a CSRF-protected POST and ends the session.

The book data, stock numbers, and LKR prices are examples, not live store inventory. Only ADMIN can access `/admin/**`. The old `/preview/admin/**` URLs are removed. The admin forms are still read-only: there are no book save/delete endpoints or persistence. Protected CRUD and text-file storage are planned for later milestones.

Run the tests with `./mvnw test` on macOS/Linux or `.\mvnw.cmd test` on Windows.
