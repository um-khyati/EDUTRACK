# 🎓 EDUTRACK – Student Management System

![Java](https://img.shields.io/badge/Java-17-blue)
![JavaFX](https://img.shields.io/badge/JavaFX-UI-green)
![Maven](https://img.shields.io/badge/Maven-Build-red)
![Status](https://img.shields.io/badge/Status-Completed-brightgreen)

---

## 📌 Overview

**EDUTRACK** is a desktop-based Student Management System built using Java and JavaFX. It provides an organized platform for managing students, teachers, attendance, grades, and feedback through role-based dashboards.

The application follows a modular architecture using the **MVC pattern and DAO layer**, ensuring clean code structure, scalability, and maintainability.

---

## ✨ Features

### 🔐 Authentication

* Secure login system
* Role-based access (Admin, Teacher, Student)

### 👩‍💼 Admin Dashboard

* Manage students and teachers
* Monitor overall system data

### 👨‍🏫 Teacher Dashboard

* Mark attendance
* Assign grades
* Provide feedback

### 🎓 Student Dashboard

* View attendance
* Check grades
* Access feedback

### 📊 System Design

* DAO Pattern for database operations
* Structured MVC architecture
* Clean separation of logic and UI

---

## 🛠️ Tech Stack

| Category     | Technology                          |
| ------------ | ----------------------------------- |
| Language     | Java                                |
| UI Framework | JavaFX (FXML)                       |
| Build Tool   | Maven                               |
| Architecture | MVC + DAO                           |
| Database     | MySQL / SQLite *(update if needed)* |

---

## 📂 Project Structure

```id="k3a2jd"
EDUTRACK/
│── pom.xml
│── src/
│   ├── main/
│   │   ├── java/com/edutrack/
│   │   │   ├── controller/
│   │   │   ├── dao/
│   │   │   ├── model/
│   │   │   ├── util/
│   │   │   └── Main.java
│   │   └── resources/
│   │       ├── views/
│   │       │   ├── Login.fxml
│   │       │   ├── AdminDashboard.fxml
│   │       │   ├── TeacherDashboard.fxml
│   │       │   └── StudentDashboard.fxml
│   │       └── css/
│   │           └── style.css
```

---

## ⚙️ Setup & Installation

### 1️⃣ Clone Repository

```bash id="1z0t6l"
git clone https://github.com/um-khyati/EDUTRACK.git
cd EDUTRACK
```

### 2️⃣ Open in IDE

* IntelliJ IDEA (recommended)
* Eclipse / VS Code

Import as **Maven Project**

---

### 3️⃣ Configure Database

Update your database credentials in:

```id="h6w2c9"
src/main/java/com/edutrack/util/DBConnection.java
```

Example:

```java id="h72ldc"
String url = "jdbc:mysql://localhost:3306/edutrack";
String user = "root";
String password = "your_password";
```

---

### 4️⃣ Run Application

* Run `Main.java`
* Ensure JavaFX SDK is configured properly

---

## 🧠 Key Components

* **Controllers:** Handle UI events and logic
* **DAO Layer:** Database interaction
* **Models:** Data representation (User, Attendance, Grades)
* **Utilities:** DB connection & security handling

---

## ✨ Final Note

EDUTRACK reflects a structured approach to building real-world applications using clean architecture principles and modular design. It demonstrates how user roles, data handling, and intuitive interfaces can come together to form a cohesive and functional system.

---
