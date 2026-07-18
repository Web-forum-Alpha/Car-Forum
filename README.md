# Car Forum

A full-stack automotive discussion forum built with Spring Boot. The application provides a server-rendered web interface with Thymeleaf and a REST API for managing users, posts, comments, likes, and administrative actions.

## Features

### Users and authentication

- User registration, login, and logout
- Session-based authentication
- User profile viewing and editing
- Profile picture uploads to Supabase Storage
- Administrator access to user management

### Forum

- Create, view, update, and delete posts
- Comment on posts
- Edit and delete comments
- Like and unlike posts
- Filter posts by title, author, number of likes, or number of comments
- Sort filtered results
- Home page with recent posts, and most-commented posts

### Validation and error handling

- DTO-based input validation
- Duplicate username and email checks
- Validation constraints for user details, post titles, post content, and comments
- Custom exceptions for missing entities, duplicate data, and unauthorized actions

## Tech Stack

| Area | Technology |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 4.1 |
| Web | Spring Web MVC |
| Front end | Thymeleaf, HTML, CSS |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL |
| File storage | Supabase Storage |
| Validation | Jakarta Bean Validation |
| Build tool | Gradle |

## Architecture

The application follows a layered architecture:

- **Controllers** expose the Thymeleaf web interface and REST endpoints.
- **Services** contain business rules and authorization logic.
- **Repositories** handle persistence through JPA and PostgreSQL.
- **Models and DTOs** represent stored entities and validated request data.
- **Helpers** handle authentication and mapping between DTOs and entities.

The main domain entities are:

- **User** — account, profile, administrator, and blocked status
- **Post** — forum topic created by a user
- **Comment** — response associated with a post and user
- **Like** — relationship between a user and a liked post

## Getting Started

### Prerequisites

- JDK 17
- PostgreSQL
- Git

Gradle does not need to be installed separately because the project includes the Gradle Wrapper.

### 1. Clone the repository

```bash
git clone https://github.com/Web-forum-Alpha/Car-Forum.git
cd Car-Forum
```

### 2. Configure the application

The application activates the `local` Spring profile. Create `src/main/resources/application-local.properties` and add:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/car_forum
spring.datasource.username=YOUR_DATABASE_USERNAME
spring.datasource.password=YOUR_DATABASE_PASSWORD

supabase.url=https://YOUR_PROJECT_ID.supabase.co
supabase.service-key=YOUR_SUPABASE_SERVICE_ROLE_KEY
supabase.bucket=uploads
```

Create the PostgreSQL database before starting the application:

```sql
CREATE DATABASE car_forum;
```

> Never commit database passwords or Supabase service keys. Keep `application-local.properties` local and excluded from version control.

Supabase Storage is used for profile picture uploads. All `supabase.*` properties must be defined for the application context to start.

### 3. Run the application

macOS or Linux:

```bash
./gradlew bootRun
```

Windows:

```powershell
.\gradlew.bat bootRun
```

Open [http://localhost:8080](http://localhost:8080).

## Web Routes

| Route | Description |
| --- | --- |
| `/` | Home page and forum statistics |
| `/users/register` | Create an account |
| `/users/login` | Sign in |
| `/users/profile` | View or update the current profile |
| `/users/admin` | Administrator panel |
| `/posts` | Browse and filter posts |
| `/posts/new` | Create a post |
| `/posts/{postId}` | View a post, comments, and likes |

## REST API

Swagger - http://localhost:8080/swagger-ui/index.html

### Users — `/api/users`

| Method | Endpoint | Description                                   |
| --- | --- |-----------------------------------------------|
| `POST` | `/register` | Register a user                               |
| `POST` | `/login` | Login                                         |
| `POST` | `/logout` | Logout                                        |
| `GET` | `/` | List users                                    |
| `GET` | `/{userId}` | Get a user                                    |
| `GET` | `/search` | Search by `username`, `email`, or `firstname` |
| `PUT` | `/{userId}` | Update the current user                       |
| `POST` | `/{userId}/picture` | Upload a profile picture                      |
| `PUT` | `/{userId}/block` | Block a user                                  |
| `PUT` | `/{userId}/unblock` | Unblock a user                                |
| `DELETE` | `/{userId}` | Delete a user                                 |

### Posts — `/api/posts`

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/` | List, filter, and sort posts |
| `GET` | `/{id}` | Get a post |
| `POST` | `/` | Create a post |
| `PUT` | `/{id}` | Update a post |
| `DELETE` | `/{id}` | Delete a post |
| `GET` | `/{postId}/likes` | Get the like count |
| `POST` | `/{postId}/likes` | Like a post |
| `DELETE` | `/{postId}/likes` | Remove the current user's like |



### Comments — `/api/comments`

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/` | List comments |
| `GET` | `/{id}` | Get a comment |
| `GET` | `/user/{id}` | List comments by user |
| `GET` | `/post/{id}` | List comments for a post |
| `POST` | `/` | Create a comment |
| `PUT` | `/{id}` | Update a comment |
| `DELETE` | `/{id}` | Delete a comment |

## Authorization Rules

- Visitors can see top 10 commented posts, register and sign in.
- Authenticated users can browse the forum and interact with posts.
- Users can modify their own profile and content.
- Administrators can manage users and moderate content.
- Blocked users cannot create, update, or delete posts.
- Protected REST requests require a valid HTTP session.
