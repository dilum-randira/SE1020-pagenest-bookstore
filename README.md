# PageNest

PageNest is an Online Bookstore Management System developed as an individual SE1020 university OOP project. It currently has a public landing page, a searchable book collection, and individual book detail pages.

## Technology stack

- Java 21
- Spring Boot 4.1.1
- Maven
- Spring MVC
- Thymeleaf
- Plain HTML, CSS, and JavaScript

## Requirements

- JDK 21 or a newer JDK that can compile Java 21 projects
- Maven is downloaded automatically by the included Maven Wrapper.

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
4. Select JDK 21 (or a newer installed JDK) as the Project SDK.
5. Open `PageNestApplication.java` and run its `main` method.
6. Visit <http://localhost:8080>.

## Run in Visual Studio Code

1. Install the **Extension Pack for Java** and **Spring Boot Extension Pack**.
2. Open the project folder in VS Code.
3. Confirm that VS Code is using JDK 21 or newer.
4. Open `PageNestApplication.java` and select **Run Java**, or run `.\mvnw.cmd spring-boot:run` in the integrated terminal on Windows.
5. Visit <http://localhost:8080>.

## Current functionality

- Starts as a Spring Boot web application on port 8080.
- Serves a responsive Thymeleaf homepage at `/`.
- Lists eight sample books at `/books`, with title/author search, category filtering, and name/price sorting.
- Shows book details at `/books/{id}` and a friendly 404 page for unknown IDs.
- Uses one in-memory sample collection in `BookService` for the homepage, catalog, and detail pages.

The book data, stock numbers, and LKR prices are examples for this project, not live store inventory. Text-file storage and admin editing are planned for later milestones.

Run the tests with `./mvnw test` on macOS/Linux or `.\mvnw.cmd test` on Windows.
