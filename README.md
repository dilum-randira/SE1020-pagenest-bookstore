# PageNest

PageNest is an Online Bookstore Management System developed as an individual SE1020 university OOP project. It has a public book collection and a role-protected admin workspace for managing books.

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
- Stores books in UTF-8 text at `data/books.txt`. On the first run only, the app creates that file with eight demo books. Existing files are loaded without reseeding.
- Shows book details at `/books/{id}` and a friendly 404 page for unknown IDs.
- Reads the same saved book collection for the homepage, catalog, detail pages, and admin screens.
- Lets admins choose Printed Book or E-Book; the catalog and details show each book's saved format.
- Provides a secured `/admin` workspace for ADMIN accounts, with live inventory totals and working add, edit, and delete forms.
- Uses `/login` for form login. ADMIN goes to `/admin`; CUSTOMER goes to `/books`. Logout uses a CSRF-protected POST and ends the session.

Only ADMIN can change books through `/admin/**`; CUSTOMER and anonymous visitors cannot call the write routes. The old `/preview/admin/**` URLs remain unavailable. The initial book data and LKR prices are examples; changes made by admins are saved immediately and appear on the public pages.

## Book text file

The storage location defaults to `data/books.txt`. Set the Spring property `pagenest.books.file` to use a different trusted local path (the automated tests do this under `target/test-data`). The path never comes from a browser form. The live file is ignored by Git because it may contain private data; back it up before manually editing or moving it.

Each book is one tab-separated line after a header. Backslashes, tabs, and line breaks inside text fields are escaped as `\\`, `\t`, and `\n`; ordinary quotes remain readable. The repository rejects malformed records and duplicate IDs/ISBNs rather than silently replacing data. Saves write a temporary file in the same folder and then replace the old file atomically where supported.

The original v1 file has nine fields. PageNest still reads it unchanged and treats those books as printed books. New saves use v2, which adds a tenth `format` field (`Printed Book` or `E-Book`). A plain `Book` object supplied by older Java code is treated as a printed book; edits to a stored e-book keep its format unless an admin explicitly changes the selector.

Run the tests with `./mvnw test` on macOS/Linux or `.\mvnw.cmd test` on Windows.
