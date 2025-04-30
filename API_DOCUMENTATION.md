# Online Registration Platform - API Documentation

This document details the API endpoints for the Online Registration Platform backend.

---
## Authentication (`/api/auth`)

### 1. Register New User

*   **Method:** `POST`
*   **Path:** `/api/auth/register`
*   **Description:** Registers a new applicant user in the system using email and password.
*   **Authentication:** None required.

**Request Body:**

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "yourSecurePassword123"
}
```

*   `firstName` (String, required): User's first name (2-50 characters).
*   `lastName` (String, required): User's last name (2-50 characters).
*   `email` (String, required): User's unique email address (valid format, max 100 characters).
*   `password` (String, required): User's password (min 8 characters).

**Success Response:**

*   **Code:** `201 Created`
*   **Body:** The `AuthResponseDTO` containing the JWT and user details.
    ```json
    {
      "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTcxNDA...",
      "tokenType": "Bearer",
      "userId": 15, // Example user ID
      "email": "john.doe@example.com",
      "roles": ["ROLE_APPLICANT"] // Example roles
    }
    ```
    *   `accessToken` (String): The JWT access token.
    *   `tokenType` (String): The token type (always "Bearer").
    *   `userId` (Long): The ID of the registered user.
    *   `email` (String): The email address of the registered user.
    *   `roles` (Array of String): A list of roles assigned to the user (e.g., "ROLE_APPLICANT", "ROLE_ADMIN").

**Error Responses:**

*   **Code:** `400 Bad Request`
    *   **Body (Validation Error):** Standard Spring Boot validation error response detailing field errors.
    *   **Body (Email Exists):**
        ```json
        "Error: Email address is already taken!"
        ```

---
### 2. Authenticate User (Login)

*   **Method:** `POST`
*   **Path:** `/api/auth/login`
*   **Description:** Authenticates a user with email and password, returning a JWT token upon success.
*   **Authentication:** None required.

**Request Body:**

```json
{
  "email": "john.doe@example.com",
  "password": "yourSecurePassword123"
}
```

*   `email` (String, required): User's registered email address.
*   `password` (String, required): User's password.

**Success Response:**

*   **Code:** `200 OK`
*   **Body:** The `AuthResponseDTO` containing the JWT and user details.
    ```json
    {
      "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImi...",
      "tokenType": "Bearer",
      "userId": 15, // Example user ID
      "email": "john.doe@example.com",
      "roles": ["ROLE_APPLICANT"] // Example roles
    }
    ```
    *   `accessToken` (String): The JWT access token.
    *   `tokenType` (String): The token type (always "Bearer").
    *   `userId` (Long): The ID of the authenticated user.
    *   `email` (String): The email address of the authenticated user.
    *   `roles` (Array of String): A list of roles assigned to the user.

**Error Responses:**

*   **Code:** `400 Bad Request`
    *   **Body (Validation Error):** Standard Spring Boot validation error response detailing field errors.
*   **Code:** `401 Unauthorized`
    *   **Body:**
        ```json
        "Error: Invalid credentials!"
        ```
*   **Code:** `500 Internal Server Error`
    *   **Body:** Contains a generic error message.

---
## Applicant Area (`/api/applicant`)

Endpoints in this section require the user to be authenticated with a valid JWT token (obtained via `/api/auth/login`). The token must be included in the `Authorization: Bearer <token>` header.

### Personal Information (`/api/applicant/personal-info`)

#### 1. Get Personal Information

*   **Method:** `GET`
*   **Path:** `/api/applicant/personal-info`
*   **Description:** Retrieves the personal information associated with the currently authenticated applicant.
*   **Authentication:** Required (Applicant Role).

**Success Response:**

*   **Code:** `200 OK`
*   **Body:**
    ```json
    {
      "lastName": "Doe",
      "firstNames": "John Michael",
      "gender": "MALE",
      "dateOfBirth": "1995-08-15",
      "nationality": "Exampleland",
      "idDocumentType": "NATIONAL_ID_CARD"
    }
    ```
    (Fields match the `PersonalInfoDTO`)

**Error Responses:**

*   **Code:** `401 Unauthorized` - If the JWT token is missing, invalid, or expired.
*   **Code:** `403 Forbidden` - If the authenticated user is not an applicant (e.g., an admin).
*   **Code:** `404 Not Found` - If the authenticated applicant has not yet saved any personal information.
*   **Code:** `500 Internal Server Error` - For unexpected server errors.

#### 2. Save/Update Personal Information

*   **Method:** `PUT`
*   **Path:** `/api/applicant/personal-info`
*   **Description:** Creates or updates the personal information for the currently authenticated applicant. Validates input, including age (must be >= 16).
*   **Authentication:** Required (Applicant Role).

**Request Body:**

```json
{
  "lastName": "Doe",
  "firstNames": "John Michael",
  "gender": "MALE",
  "dateOfBirth": "1995-08-15",
  "nationality": "Exampleland",
  "idDocumentType": "NATIONAL_ID_CARD"
}
```
*   All fields are required and match the `PersonalInfoDTO` structure and validation rules.

**Success Response:**

*   **Code:** `200 OK`
*   **Body:** The saved/updated `PersonalInfoDTO` (same structure as the request body).
    ```json
    {
      "lastName": "Doe",
      "firstNames": "John Michael",
      "gender": "MALE",
      "dateOfBirth": "1995-08-15",
      "nationality": "Exampleland",
      "idDocumentType": "NATIONAL_ID_CARD"
    }
    ```

**Error Responses:**

*   **Code:** `400 Bad Request`
    *   **Body (Validation Error):** Standard Spring Boot validation error response detailing field errors (e.g., missing fields, invalid patterns, invalid enum values).
    *   **Body (Age Error):**
        ```json
        "Applicant must be at least 16 years old."
        ```
*   **Code:** `401 Unauthorized` - If the JWT token is missing, invalid, or expired.
*   **Code:** `403 Forbidden` - If the authenticated user is not an applicant.
*   **Code:** `500 Internal Server Error` - For unexpected server errors.

---
### Document Management (`/api/applicant/documents`)

#### 1. Upload Document

*   **Method:** `POST`
*   **Path:** `/api/applicant/documents/{documentType}`
*   **Description:** Uploads a document file for the specified `documentType`. Validates file type and size based on the `DocumentType` enum constraints. Organizes files by user ID on the server. Handles potential duplicates for non-repeatable types (e.g., ID_PHOTO).
*   **Authentication:** Required (Applicant Role).
*   **Path Variable:**
    *   `documentType` (String, required): The type of document being uploaded (must match one of the `DocumentType` enum values, e.g., `DIPLOMA_BAC`, `ID_CARD_FRONT`, `ID_PHOTO`).
*   **Request:** `multipart/form-data`
    *   `file` (File, required): The document file to upload.

**Success Response:**

*   **Code:** `201 Created`
*   **Body:** The `DocumentDTO` of the saved document metadata.
    ```json
    {
      "id": 123,
      "documentType": "ID_PHOTO",
      "originalFilename": "profile_picture.jpg",
      "fileSize": 512000,
      "contentType": "image/jpeg",
      "status": "UPLOADED",
      "uploadedAt": "2025-04-25T10:44:00.123456",
      "validatedAt": null,
      "validationNotes": null
    }
    ```

**Error Responses:**

*   **Code:** `400 Bad Request`
    *   **Body (Validation Error):** e.g., "Please select a file to upload.", "Invalid file type for ID Photo. Allowed types: image/jpeg, image/png", "File size exceeds the limit of 1MB for ID Photo.", "Document type 'ID Photo' already exists for this user."
*   **Code:** `401 Unauthorized` - If the JWT token is missing, invalid, or expired.
*   **Code:** `403 Forbidden` - If the authenticated user is not an applicant.
*   **Code:** `404 Not Found` - If the specified `{documentType}` in the path is not a valid `DocumentType` enum value.
*   **Code:** `500 Internal Server Error` - For unexpected server errors during storage or processing.

#### 2. List User Documents

*   **Method:** `GET`
*   **Path:** `/api/applicant/documents`
*   **Description:** Retrieves a list of metadata for all documents uploaded by the currently authenticated applicant, ordered by upload date descending.
*   **Authentication:** Required (Applicant Role).

**Success Response:**

*   **Code:** `200 OK`
*   **Body:** An array of `DocumentDTO` objects.
    ```json
    [
      {
        "id": 123,
        "documentType": "ID_PHOTO",
        "originalFilename": "profile_picture.jpg",
        "fileSize": 512000,
        "contentType": "image/jpeg",
        "status": "UPLOADED",
        "uploadedAt": "2025-04-25T10:44:00.123456",
        "validatedAt": null,
        "validationNotes": null
      },
      {
        "id": 120,
        "documentType": "DIPLOMA_BAC",
        "originalFilename": "bac_diploma.pdf",
        "fileSize": 1024000,
        "contentType": "application/pdf",
        "status": "VALIDATED",
        "uploadedAt": "2025-04-25T09:30:00.000000",
        "validatedAt": "2025-04-25T10:00:00.000000",
        "validationNotes": "Verified."
      }
      // ... other documents
    ]
    ```

**Error Responses:**

*   **Code:** `401 Unauthorized` - If the JWT token is missing, invalid, or expired.
*   **Code:** `403 Forbidden` - If the authenticated user is not an applicant.
*   **Code:** `500 Internal Server Error` - For unexpected server errors.

#### 3. Download Document

*   **Method:** `GET`
*   **Path:** `/api/applicant/documents/{id}/download`
*   **Description:** Downloads the physical file associated with the specified document ID. Ensures the document belongs to the authenticated user.
*   **Authentication:** Required (Applicant Role).
*   **Path Variable:**
    *   `id` (Long, required): The ID of the document to download.

**Success Response:**

*   **Code:** `200 OK`
*   **Headers:**
    *   `Content-Type`: (e.g., `image/jpeg`, `application/pdf`, `application/octet-stream`)
    *   `Content-Disposition`: `attachment; filename="<original_filename>"` (Suggests the original filename to the browser)
*   **Body:** The raw binary content of the document file.

**Error Responses:**

*   **Code:** `401 Unauthorized` - If the JWT token is missing, invalid, or expired.
*   **Code:** `403 Forbidden` - If the authenticated user is not an applicant.
*   **Code:** `404 Not Found` - If the document ID does not exist or does not belong to the authenticated user.
*   **Code:** `500 Internal Server Error` - For unexpected server errors during file retrieval.

#### 4. Delete Document

*   **Method:** `DELETE`
*   **Path:** `/api/applicant/documents/{id}`
*   **Description:** Deletes the specified document (both metadata and the physical file). Ensures the document belongs to the authenticated user.
*   **Authentication:** Required (Applicant Role).
*   **Path Variable:**
    *   `id` (Long, required): The ID of the document to delete.

**Success Response:**

*   **Code:** `200 OK`
*   **Body:**
    ```json
    "Document deleted successfully."
    ```
    (Alternatively, could be `204 No Content` with no body)

**Error Responses:**

*   **Code:** `401 Unauthorized` - If the JWT token is missing, invalid, or expired.
*   **Code:** `403 Forbidden` - If the authenticated user is not an applicant.
*   **Code:** `404 Not Found` - If the document ID does not exist or does not belong to the authenticated user.
*   **Code:** `500 Internal Server Error` - For unexpected server errors during deletion.

---
### Academic History (`/api/applicant/academic-history`)

#### 1. Get Academic History List

*   **Method:** `GET`
*   **Path:** `/api/applicant/academic-history`
*   **Description:** Retrieves a list of all academic history entries for the currently authenticated applicant, ordered by start date descending.
*   **Authentication:** Required (Applicant Role).

**Success Response:**

*   **Code:** `200 OK`
*   **Body:** An array of `AcademicHistoryDTO` objects.
    ```json
    [
      {
        "id": 5,
        "institutionName": "University of Example",
        "specialization": "Computer Science",
        "startDate": "2020-09-01",
        "endDate": "2024-06-30"
      },
      {
        "id": 2,
        "institutionName": "Example Community College",
        "specialization": "General Studies",
        "startDate": "2018-09-01",
        "endDate": "2020-06-30"
      }
      // ... other history entries
    ]
    ```

**Error Responses:**

*   **Code:** `401 Unauthorized` - If the JWT token is missing, invalid, or expired.
*   **Code:** `403 Forbidden` - If the authenticated user is not an applicant.
*   **Code:** `500 Internal Server Error` - For unexpected server errors.

#### 2. Add Academic History Entry

*   **Method:** `POST`
*   **Path:** `/api/applicant/academic-history`
*   **Description:** Adds a new academic history entry for the authenticated applicant. Validates input and checks for date overlaps with existing entries.
*   **Authentication:** Required (Applicant Role).

**Request Body:** (`AcademicHistoryDTO`, `id` must be null or omitted)

```json
{
  "institutionName": "New Institute of Technology",
  "specialization": "Software Engineering",
  "startDate": "2024-09-01",
  "endDate": null // null if ongoing
}
```
*   All fields except `endDate` are required and match the `AcademicHistoryDTO` structure and validation rules.

**Success Response:**

*   **Code:** `201 Created`
*   **Body:** The created `AcademicHistoryDTO` including its newly generated `id`.
    ```json
    {
      "id": 6, // New ID assigned by the server
      "institutionName": "New Institute of Technology",
      "specialization": "Software Engineering",
      "startDate": "2024-09-01",
      "endDate": null
    }
    ```

**Error Responses:**

*   **Code:** `400 Bad Request`
    *   **Body (Validation Error):** Standard Spring Boot validation error response or specific messages like "ID must be null when adding new academic history.", "End date cannot be before start date.", "Academic history periods cannot overlap."
*   **Code:** `401 Unauthorized` - If the JWT token is missing, invalid, or expired.
*   **Code:** `403 Forbidden` - If the authenticated user is not an applicant.
*   **Code:** `500 Internal Server Error` - For unexpected server errors.

#### 3. Update Academic History Entry

*   **Method:** `PUT`
*   **Path:** `/api/applicant/academic-history/{id}`
*   **Description:** Updates an existing academic history entry identified by `{id}`. Ensures the entry belongs to the authenticated user. Validates input and checks for date overlaps (excluding the entry being updated).
*   **Authentication:** Required (Applicant Role).
*   **Path Variable:**
    *   `id` (Long, required): The ID of the academic history entry to update.

**Request Body:** (`AcademicHistoryDTO`, `id` is ignored in the body, use the path variable)

```json
{
  // "id": 5, // Ignored
  "institutionName": "University of Example (Updated)",
  "specialization": "Computer Science & AI",
  "startDate": "2020-09-01",
  "endDate": "2024-07-15" // Updated end date
}
```
*   All fields except `endDate` are required and match the `AcademicHistoryDTO` structure and validation rules.

**Success Response:**

*   **Code:** `200 OK`
*   **Body:** The updated `AcademicHistoryDTO`.
    ```json
    {
      "id": 5,
      "institutionName": "University of Example (Updated)",
      "specialization": "Computer Science & AI",
      "startDate": "2020-09-01",
      "endDate": "2024-07-15"
    }
    ```

**Error Responses:**

*   **Code:** `400 Bad Request`
    *   **Body (Validation Error):** Standard Spring Boot validation error response or specific messages like "End date cannot be before start date.", "Academic history periods cannot overlap."
*   **Code:** `401 Unauthorized` - If the JWT token is missing, invalid, or expired.
*   **Code:** `403 Forbidden` - If the authenticated user is not an applicant.
*   **Code:** `404 Not Found` - If the academic history `{id}` does not exist or does not belong to the authenticated user.
*   **Code:** `500 Internal Server Error` - For unexpected server errors.

#### 4. Delete Academic History Entry

*   **Method:** `DELETE`
*   **Path:** `/api/applicant/academic-history/{id}`
*   **Description:** Deletes the academic history entry identified by `{id}`. Ensures the entry belongs to the authenticated user.
*   **Authentication:** Required (Applicant Role).
*   **Path Variable:**
    *   `id` (Long, required): The ID of the academic history entry to delete.

**Success Response:**

*   **Code:** `200 OK`
*   **Body:**
    ```json
    "Academic history entry deleted successfully."
    ```
    (Alternatively, could be `204 No Content` with no body)

**Error Responses:**

*   **Code:** `401 Unauthorized` - If the JWT token is missing, invalid, or expired.
*   **Code:** `403 Forbidden` - If the authenticated user is not an applicant.
*   **Code:** `404 Not Found` - If the academic history `{id}` does not exist or does not belong to the authenticated user.
*   **Code:** `500 Internal Server Error` - For unexpected server errors.

---
### Contact Information (`/api/applicant/contact-info`)

#### 1. Get Contact Information

*   **Method:** `GET`
*   **Path:** `/api/applicant/contact-info`
*   **Description:** Retrieves the contact information (phone, address, emergency contact, email verification status) associated with the currently authenticated applicant.
*   **Authentication:** Required (Applicant Role).

**Success Response:**

*   **Code:** `200 OK`
*   **Body:** The `ContactInfoDTO` object.
    ```json
    {
      "emailVerified": false, // Example: Email not yet verified
      "phoneNumber": "+1 555-123-4567",
      "address": {
        "street": "123 Main St",
        "street2": "Apt 4B",
        "city": "Anytown",
        "postalCode": "12345",
        "country": "Exampleland",
        "latitude": null, // Optional, may be populated by backend
        "longitude": null // Optional, may be populated by backend
      },
      "emergencyContact": {
        "name": "Jane Doe",
        "relationship": "Spouse",
        "phone": "+1 555-987-6543"
      }
    }
    ```

**Error Responses:**

*   **Code:** `401 Unauthorized` - If the JWT token is missing, invalid, or expired.
*   **Code:** `403 Forbidden` - If the authenticated user is not an applicant.
*   **Code:** `404 Not Found` - If the authenticated applicant has not yet saved any contact information.
*   **Code:** `500 Internal Server Error` - For unexpected server errors.

#### 2. Save/Update Contact Information

*   **Method:** `PUT`
*   **Path:** `/api/applicant/contact-info`
*   **Description:** Creates or updates the contact information (phone, address, emergency contact) for the currently authenticated applicant. Does **not** modify the `emailVerified` status. Validates input for all fields, including nested address and emergency contact details.
*   **Authentication:** Required (Applicant Role).

**Request Body:** (`ContactInfoDTO`)

```json
{
  // emailVerified is ignored in the request
  "phoneNumber": "+1 555-123-4567",
  "address": {
    "street": "123 Main St",
    "street2": "Apt 4B",
    "city": "Anytown",
    "postalCode": "12345",
    "country": "Exampleland"
    // latitude/longitude are typically not sent by the client
  },
  "emergencyContact": {
    "name": "Jane Doe",
    "relationship": "Spouse",
    "phone": "+1 555-987-6543"
  }
}
```
*   All fields within the nested `address` and `emergencyContact` objects (except `street2`) are required and validated. `phoneNumber` is also required.

**Success Response:**

*   **Code:** `200 OK`
*   **Body:** The updated `ContactInfoDTO` (reflecting saved data and current `emailVerified` status).
    ```json
    {
      "emailVerified": false, // Status remains unchanged by this endpoint
      "phoneNumber": "+1 555-123-4567",
      "address": {
        "street": "123 Main St",
        "street2": "Apt 4B",
        "city": "Anytown",
        "postalCode": "12345",
        "country": "Exampleland",
        "latitude": null,
        "longitude": null
      },
      "emergencyContact": {
        "name": "Jane Doe",
        "relationship": "Spouse",
        "phone": "+1 555-987-6543"
      }
    }
    ```

**Error Responses:**

*   **Code:** `400 Bad Request`
    *   **Body (Validation Error):** Standard Spring Boot validation error response detailing field errors in the main DTO or nested DTOs (e.g., missing fields, invalid phone format).
*   **Code:** `401 Unauthorized` - If the JWT token is missing, invalid, or expired.
*   **Code:** `403 Forbidden` - If the authenticated user is not an applicant.
*   **Code:** `404 Not Found` - If the authenticated user somehow doesn't exist (shouldn't happen).
*   **Code:** `500 Internal Server Error` - For unexpected server errors.

---
## Admin Area (`/api/admin`)

Endpoints in this section require the user to be authenticated with a valid JWT token and possess the `ROLE_ADMIN`.

### User Management (`/api/admin/users`)

#### 1. List Users (Paginated)

*   **Method:** `GET`
*   **Path:** `/api/admin/users`
*   **Description:** Retrieves a paginated list of registered users with summary information. Supports pagination and sorting via query parameters.
*   **Authentication:** Required (Admin Role).
*   **Query Parameters (Optional):**
    *   `page` (integer, default: 0): The page number to retrieve (0-indexed).
    *   `size` (integer, default: 20): The number of users per page.
    *   `sort` (string, default: unsorted): Sorting criteria in the format `property,(asc|desc)`. Example: `sort=email,asc` or `sort=createdAt,desc`. Multiple sort criteria can be provided (e.g., `sort=lastName,asc&sort=firstName,asc`). Properties refer to fields in the `User` entity.

**Success Response:**

*   **Code:** `200 OK`
*   **Body:** A Spring Data Page object containing `UserSummaryDTO`s.
    ```json
    {
      "content": [ // Array of UserSummaryDTO objects for the current page
        {
          "id": 1,
          "email": "admin@example.com",
          "firstName": "Admin",
          "lastName": "User",
          "roles": ["ROLE_ADMIN", "ROLE_APPLICANT"],
          "provider": "LOCAL",
          "enabled": true,
          "locked": false,
          "createdAt": "2025-04-25T15:55:00.123456"
        },
        {
          "id": 15,
          "email": "john.doe@example.com",
          "firstName": "John",
          "lastName": "Doe",
          "roles": ["ROLE_APPLICANT"],
          "provider": "LOCAL",
          "enabled": true,
          "locked": false,
          "createdAt": "2025-04-25T10:12:00.987654"
        }
        // ... other users on the current page
      ],
      "pageable": { // Information about the requested page
        "pageNumber": 0,
        "pageSize": 20,
        "sort": {
          "empty": true,
          "sorted": false,
          "unsorted": true
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
      },
      "last": true, // Is this the last page?
      "totalPages": 1, // Total number of pages available
      "totalElements": 2, // Total number of users matching the query
      "size": 20, // The requested page size
      "number": 0, // The current page number (0-indexed)
      "sort": { // Information about the applied sorting
        "empty": true,
        "sorted": false,
        "unsorted": true
      },
      "first": true, // Is this the first page?
      "numberOfElements": 2, // Number of users on the current page
      "empty": false // Is the current page empty?
    }
    ```

**Error Responses:**

*   **Code:** `401 Unauthorized` - If the JWT token is missing, invalid, or expired.
*   **Code:** `403 Forbidden` - If the authenticated user does not have the `ROLE_ADMIN`.
*   **Code:** `500 Internal Server Error` - For unexpected server errors.

---
#### 2. Get Application Details

*   **Method:** `GET`
*   **Path:** `/api/admin/users/{userId}/application`
*   **Description:** Retrieves the complete application details for a specific user, including personal info, contact info, academic history, and uploaded documents.
*   **Authentication:** Required (Admin Role).
*   **Path Variable:**
    *   `userId` (Long, required): The ID of the user whose application details are to be retrieved.

**Success Response:**

*   **Code:** `200 OK`
*   **Body:** The `ApplicationDetailDTO` object containing aggregated application data.
    ```json
    {
      "userSummary": { // UserSummaryDTO
        "id": 15,
        "email": "john.doe@example.com",
        "firstName": "John",
        "lastName": "Doe",
        "roles": ["ROLE_APPLICANT"],
        "provider": "LOCAL",
        "enabled": true,
        "locked": false,
        "createdAt": "2025-04-25T10:12:00.987654"
      },
      "personalInfo": { // PersonalInfoDTO
        "lastName": "Doe",
        "firstNames": "John Michael",
        "gender": "MALE",
        "dateOfBirth": "1995-08-15",
        "nationality": "Exampleland",
        "idDocumentType": "NATIONAL_ID_CARD"
      },
      "contactInfo": { // ContactInfoDTO
        "emailVerified": false,
        "phoneNumber": "+1 555-123-4567",
        "address": { // AddressDTO
          "street": "123 Main St",
          "street2": "Apt 4B",
          "city": "Anytown",
          "postalCode": "12345",
          "country": "Exampleland",
          "latitude": null,
          "longitude": null
        },
        "emergencyContact": { // EmergencyContactDTO
          "name": "Jane Doe",
          "relationship": "Spouse",
          "phone": "+1 555-987-6543"
        }
      },
      "academicHistory": [ // List<AcademicHistoryDTO>
        {
          "id": 5,
          "institutionName": "University of Example",
          "specialization": "Computer Science",
          "startDate": "2020-09-01",
          "endDate": "2024-06-30"
        }
        // ... other entries
      ],
      "documents": [ // List<DocumentDTO>
        {
          "id": 123,
          "documentType": "ID_PHOTO",
          "originalFilename": "profile_picture.jpg",
          "fileSize": 512000,
          "contentType": "image/jpeg",
          "status": "UPLOADED",
          "uploadedAt": "2025-04-25T10:44:00.123456",
          "validatedAt": null,
          "validationNotes": null
        }
        // ... other documents
      ]
    }
    ```

**Error Responses:**

*   **Code:** `401 Unauthorized` - If the JWT token is missing, invalid, or expired.
*   **Code:** `403 Forbidden` - If the authenticated user does not have the `ROLE_ADMIN`.
*   **Code:** `404 Not Found` - If no user exists with the specified `{userId}`.
*   **Code:** `500 Internal Server Error` - For unexpected server errors.

---
### Document Management (Admin Actions)

*(Note: These endpoints might be moved to `/api/admin/documents/` in the future for better organization)*

#### 1. Update Document Status

*   **Method:** `PUT`
*   **Path:** `/api/admin/users/documents/{documentId}/status`
*   **Description:** Updates the status (e.g., to `VALIDATED` or `REJECTED`) and optionally adds validation notes for a specific document. Sends an email notification to the applicant upon status change.
*   **Authentication:** Required (Admin Role).
*   **Path Variable:**
    *   `documentId` (Long, required): The ID of the document whose status is to be updated.

**Request Body:** (`DocumentStatusUpdateDTO`)

```json
{
  "newStatus": "VALIDATED", // Or "REJECTED", etc. (Must be a valid DocumentStatus enum value)
  "validationNotes": "Document verified successfully." // Optional notes, especially useful for REJECTED status
}
```
*   `newStatus` (String, required): The target status for the document.
*   `validationNotes` (String, optional): Notes regarding the validation decision (max 500 characters).

**Success Response:**

*   **Code:** `200 OK`
*   **Body:** The updated `DocumentDTO` reflecting the new status and notes.
    ```json
    {
      "id": 123,
      "documentType": "ID_PHOTO",
      "originalFilename": "profile_picture.jpg",
      "fileSize": 512000,
      "contentType": "image/jpeg",
      "status": "VALIDATED", // Updated status
      "uploadedAt": "2025-04-25T10:44:00.123456",
      "validatedAt": "2025-04-25T16:02:00.000000", // Timestamp likely updated by backend
      "validationNotes": "Document verified successfully." // Updated notes
    }
    ```

**Error Responses:**

*   **Code:** `400 Bad Request`
    *   **Body (Validation Error):** Standard validation errors (e.g., `newStatus` is null).
    *   **Body (Invalid Transition):** e.g., "Invalid status transition from UPLOADED to MISSING" (if such rules are implemented).
*   **Code:** `401 Unauthorized` - If the JWT token is missing, invalid, or expired.
*   **Code:** `403 Forbidden` - If the authenticated user does not have the `ROLE_ADMIN`.
*   **Code:** `404 Not Found` - If no document exists with the specified `{documentId}`.
*   **Code:** `500 Internal Server Error` - For unexpected server errors.

---