# Social Media App

A Java-based social media application scaffold using **JavaFX**, **Maven**, and **MySQL**.

**This Project is a part for DEBI Software Development Program "just a checkpoint application."**

---

## Team Members:

- Alaa Reda Farouq Saleh | WA | SD2614030.
- Ahmed Ehab Abdelazeem Bahnasey | WA | SD2614028.
- Sohila Arif Mohamed Admeen | MA | SD2614024.
---
## Features

- Authentication (Login/Register)
- User profiles
- Posts with images
- Likes & comments
- Friend system
- Notifications
- Privacy settings
- Real-time chat using sockets
- Search users and posts

---
## 🛠 Tech Stack

- Java 17
- JavaFX 20
- Maven
- MySQL
- JDBC
- FXML + CSS

## Project Location
Main Maven module:
- `socialmedia/`
---

## 📂 Project Structure

```text
socialmedia/
│
├── pom.xml
│
├── src/
│   └── main/
│
│       ├── java/
│       │   └── com/
│       │       └── socialmedia/
│       │
│       │           ├── app/
│       │           │   ├── Main.java
│       │           │   ├── Navigator.java
│       │           │   ├── DbPing.java
│       │           │   └── SeedRunner.java
│       │
│       │           ├── config/
│       │           │   └── DatabaseConfig.java
│       │
│       │           ├── models/
│       │           │   ├── User.java
│       │           │   ├── Profile.java
│       │           │   ├── Post.java
│       │           │   ├── FeedPost.java
│       │           │   ├── Comment.java
│       │           │   ├── Like.java
│       │           │   ├── Friend.java
│       │           │   ├── Notification.java
│       │           │   └── Message.java
│       │
│       │           ├── dao/
│       │           │   ├── UserDao.java
│       │           │   ├── ProfileDao.java
│       │           │   ├── PostDao.java
│       │           │   ├── CommentDao.java
│       │           │   ├── LikeDao.java
│       │           │   ├── FriendDao.java
│       │           │   ├── NotificationDao.java
│       │           │   └── ChatDao.java
│       │
│       │           ├── services/
│       │           │   ├── AuthService.java
│       │           │   ├── UserService.java
│       │           │   ├── ProfileService.java
│       │           │   ├── PostService.java
│       │           │   ├── CommentService.java
│       │           │   ├── LikeService.java
│       │           │   ├── FriendService.java
│       │           │   ├── NotificationService.java
│       │           │   └── ChatService.java
│       │
│       │           ├── realtime/
│       │           │   ├── ChatServer.java
│       │           │   └── ChatClient.java
│       │
│       │           ├── ui/
│       │           │   └── controllers/
│       │           │       ├── LoginController.java
│       │           │       ├── RegisterController.java
│       │           │       ├── FeedController.java
│       │           │       ├── ProfileController.java
│       │           │       ├── UserProfileController.java
│       │           │       └── ChatController.java
│       │
│       │           └── utils/
│       │               ├── PasswordHasher.java
│       │               ├── Validator.java
│       │               ├── Session.java
│       │               └── TimeAgo.java
│
│
│       └── resources/
│           │
│           ├── styles/
│           │   └── main.css
│           │
│           ├── images/
│           │   └── default-avatar.png
│           │
│           ├── ui/
│           │   └── views/
│           │       ├── login.fxml
│           │       ├── register.fxml
│           │       ├── feed.fxml
│           │       ├── profile.fxml
│           │       ├── user-profile.fxml
│           │       └── chat.fxml
│           │
│           └── application.properties.example
```
---
## Layer Responsibilities
- `app`: Entry point and screen navigation.
- `ui/controllers`: JavaFX controller classes.
- `models`: Core domain entities.
- `services`: Business logic and orchestration.
- `dao`: Database interaction layer.
- `utils`: Shared helper utilities.
- `config`: Application and database configuration.
- `resources`: CSS, images, and runtime FXML assets.

---

## 🗄 Database Schema (Core Tables)

- `user` – stores user credentials
- `profile` – stores user profile data
- `friend` – manages friend relationships
- `post` – user posts
- `comment` – comments on posts
- `likes` – likes on posts
- `message` – RTC messages 
- `notification` – users notifications

All tables are connected using foreign keys with cascade rules.

---

## 🚀 Installation & Setup

### 1️⃣ Prerequisites
- Java JDK 17+
- MySQL Server (XAMPP or MySQL Workbench)
- Maven
- IDE (Eclipse or IntelliJ IDEA)

---

### 2️⃣ Database Setup

1. Start MySQL server.
2. Create database: `CREATE DATABASE social_media_db;`.
3. Remove `.example` from `main/resources/application.properties.example`.
4. Update DB credentials in: `application.properties`.
5. Run `app/SeedRunner.java` it will create tables and mock data. 

---

## 🌱 Running the Seeder (Database Initialization)

### Seeder Responsibilities:
- Create tables if they do not exist
- Run **only once** if tables are empty
- Insert **30 rows** into each table with dummy data

### How to Run:
1. Open `com.socialmedia.app.SeedRunner`
2. Run the `main()` method
3. Check console for success message

> The seeder will NOT run again if data already exists.

---

## ▶️ Running the Application

Using Maven:
- mvn clean javafx:run

Or:
- Run `Main.java` from the IDE
