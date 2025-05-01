# Online Registration Platform

## Project Overview

This project is a complete registration management system featuring a 5-step registration process, automated document validation, transactional email generation, and an analytical dashboard. It consists of a Spring Boot backend and an Angular frontend.

## Prerequisites

Before you begin, ensure you have the following installed:

*   **Java Development Kit (JDK):** Version 17 or higher.
*   **Apache Maven:** Version 3.6 or higher.
*   **PostgreSQL Database:** A running instance of PostgreSQL (Docker container recommanded).
*   **pgAdmin:** A PostgreSQL administration tool (recommended for database restoration).
*   **Node.js:** Version 18 or higher.
*   **npm or Yarn:** Package manager for Node.js (npm is included with Node.js).

## Backend Setup (Spring Boot)

1.  **Database Setup:**
    *   Ensure your PostgreSQL server is running.
    *   Open **pgAdmin**.
    *   Connect to your PostgreSQL server.
    *   Create a new database (e.g., `registration_db`).
    *   Create a new user with appropriate permissions for the `registration_db` database.
    *   Right-click on the `registration_db` database and select **Restore...**.
    *   In the Restore dialog, select the `subscription_db.sql` file provided in the project root as the **Filename**.
    *   Ensure the **Format** is set to `Custom or tar`.
    *   Click **Restore**. This will create the necessary tables and populate some initial data (if any is included in the SQL file).

2.  **Configuration:**
    *   Open the `src/main/resources/application.properties` file in your IDE.
    *   **Database Connection:** Update the following properties with your PostgreSQL database credentials:
        ```properties
        spring.datasource.url=jdbc:postgresql://localhost:5432/registration_db # Replace registration_db if you used a different name
        spring.datasource.username=YOUR_POSTGRES_USERNAME # Replace with your DB username
        spring.datasource.password=YOUR_POSTGRES_PASSWORD # Replace with your DB password
        ```
    *   **OAuth2 Configuration:** If you plan to use Google or Microsoft login, replace the placeholder values with your actual API credentials. If not, you can comment out the `spring.security.oauth2.client.registration.*` lines or remove `.oauth2Login(withDefaults())` from `SecurityConfig.java`.
    *   **Email Configuration:** Update the `spring.mail.*` properties with your SMTP server details if you want email sending to work.
    *   **File Upload Directory:** Ensure the directory specified by `app.file-storage.upload-dir` (default is `./uploads/registration-docs`) exists and is writable by the application.
    *   **Default Admin User:** Note the default admin credentials configured for initial access:
        ```properties
        app.admin.email=admin@example.com
        app.admin.password=adminpassword # CHANGE THIS IN PRODUCTION!
        ```

3.  **Run the Backend:**
    *   **Recommended (IntelliJ IDEA):** Open the project in IntelliJ IDEA. Locate the `RegistrationPlatformApplication.java` file (usually in `src/main/java/com/registration/platform/`). Right-click on the file and select **Run 'RegistrationPlatformApplication'**.
    *   **Using Maven:** Open your terminal in the project root directory (`c:/Projects/k48/subscription mangement`) and run:
        ```bash
        mvn spring-boot:run
        ```
    *   The backend server should start on `http://localhost:8080`.

## Frontend Setup (Angular)

1.  **Navigate to Frontend Directory:** Open your terminal and change directory to the frontend project:
    ```bash
    cd frontend/registration-platform
    ```
2.  **Install Dependencies:** Install the Angular project dependencies:
    ```bash
    npm install
    # or yarn install
    ```
3.  **Run the Frontend:** Start the Angular development server:
    ```bash
    npm start
    # or yarn start
    ```
    The frontend application should be available at `http://localhost:4200` (or another port if configured differently).

## Accessing the Application and API Documentation

*   **Frontend Application:** Open your web browser and go to `http://localhost:4200`.
*   **Backend API:** The backend API is available at `http://localhost:8080/api/`.
*   **API Documentation:** Open the `API_DOCUMENTATION.md` file in your editor to view the detailed API endpoints, request/response formats, and authentication requirements.

## Test Credentials

*   **Default Admin:**
    *   Email: `admin@example.com`
    *   Password: `adminpassword` (Remember to change this!)
*   **Applicant (for testing):**
    *   Email: `johndoe@rc.com`
    *   Password: `Ssssssss00-` (You might need to register this user first via the registration endpoint if the SQL file doesn't include it).

This README provides a comprehensive guide to setting up and running the project.