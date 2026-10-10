# PageNest

PageNest is an Online Bookstore Management System developed as an individual SE1020 university OOP project. This first milestone provides a minimal Spring Boot web application and a working homepage.

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
- Serves a basic Thymeleaf homepage at `/`.
- Includes folders for future controllers, models, services, repositories, static assets, templates, and text-file data.
