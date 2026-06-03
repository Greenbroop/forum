# Forum Application

A modern Spring Boot-based forum application that enables users to create discussions, post messages, and manage community threads with role-based access control (Admin, Moderator, User).

---

## 👥 Team Members and Roles

| Name | Role | Responsibilities |
|------|------|------------------|
| Đồng Đức Dũng | Backend Developer | API development, database design, business logic |
| Lê Nguyễn Hải Đăng | Frontend Developer | UI/UX design, Thymeleaf templates, client-side logic |

---

## 🛠️ Technology Stack

| Component | Version |
|-----------|---------|
| **Backend Framework** | Spring Boot 4.0.6 |
| **Java** | 17+ (LTS) |
| **Database** | MySQL 14+ |
| **Template Engine** | Thymeleaf 3.x |
| **ORM** | Hibernate (JPA) |
| **Security** | Spring Security 6 |
| **Build Tool** | Maven 3.6+ |
| **Authentication** | Form-based authentication |

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Java 17** or higher ([Download](https://adoptium.net/))
- **Maven 3.6+** or use the included `mvnw` script
- **MySQL 14+** or compatible database
  - Download: https://www.mysql.com/downloads/mysql/
  - Or use Docker: `docker run -d -e MYSQL_ROOT_PASSWORD=password -p 3306:3306 mysql:8`
- **Git** (for version control)
- A text editor or IDE (IntelliJ IDEA, VS Code, Eclipse)

**Verify Installation:**
```bash
java -version
mvn --version
mysql --version
```

---

## 🚀 Step-by-Step Setup Instructions

### 1. Clone the Repository
```bash
git clone <repository-url>
cd forum-main
```

### 2. Database Setup
Create a MySQL database and user (optional - the app will create the database automatically):

```sql
CREATE DATABASE forum;
CREATE USER 'forum_user'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON forum.* TO 'forum_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configure Environment Variables
Edit `src/main/resources/application.properties` with your database credentials:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/forum?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_database_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Server Configuration
server.port=8080

# Security
spring.security.user.name=admin
spring.security.user.password=admin_password

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=15MB

# Logging
logging.file.name=logs/forum-app.log
logging.level.root=INFO
```

### 4. Install Dependencies
```bash
mvn clean install
```

### 5. Run Database Migrations
The application uses Hibernate DDL auto set to `none`. Ensure your database schema is initialized. If needed, update to:
```properties
spring.jpa.hibernate.ddl-auto=update
```
Then run the application once to create tables, then revert to `none`.

---

## 🌍 Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/forum` | Database connection URL |
| `SPRING_DATASOURCE_USERNAME` | `root` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `Kazutozero09@` | Database password |
| `SERVER_PORT` | `8080` | Server port |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `none` | Hibernate DDL strategy |
| `SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE` | `10MB` | Maximum file upload size |
| `LOGGING_FILE_NAME` | `logs/forum-app.log` | Log file location |

---

## 💻 How to Run Locally

### Option 1: Using Maven (Recommended)

**Build the project:**
```bash
mvn clean package
```

**Run the application:**
```bash
mvn spring-boot:run
```

The application will start at: **http://localhost:8080**

### Option 2: Using Java JAR

**Build the JAR file:**
```bash
mvn clean package
```

**Run the JAR:**
```bash
java -jar target/forum-0.0.1-SNAPSHOT.jar
```

### Option 3: Using IDE

1. Open the project in your IDE (IntelliJ IDEA or VS Code)
2. Navigate to `ForumApplication.java`
3. Click the Run button or press `Ctrl+Shift+F10` (IntelliJ) / `F5` (VS Code)

---

## 🧪 How to Run Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=ForumApplicationTests
```

### Run Tests with Coverage
```bash
mvn test jacoco:report
```

Coverage report: `target/site/jacoco/index.html`

### Run Tests in IDE
- Right-click on test class → **Run Tests**
- Or use: `Ctrl+Shift+F10` (IntelliJ) / `Ctrl+F5` (VS Code)

---

## 🌐 Live Demo

Access the live demo here: **[Forum Application - Production](https://forum-production-028c.up.railway.app/)**

---

## 🔐 Test Account Credentials

| Role | Username | Password |
|------|----------|----------|
| **Admin** | `admin` | `123456` |
| **Moderator** | `mod` | `123456` |
| **User** | `student_dung` or `student_dang` | `123456` |

> ⚠️ **Note:** These are test credentials for demonstration purposes only. In production, use strong, unique passwords and never hardcode credentials.

---

## 📁 Project Structure

```
forum-main/
├── src/
│   ├── main/
│   │   ├── java/com/iu/forum/
│   │   │   ├── ForumApplication.java          # Spring Boot entry point
│   │   │   ├── config/                        # Configuration classes
│   │   │   │   ├── SecurityConfig.java        # Spring Security configuration
│   │   │   │   └── WebConfig.java             # Web configuration
│   │   │   ├── controller/                    # REST Controllers
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── HomeController.java
│   │   │   │   ├── ProfileController.java
│   │   │   │   ├── ThreadController.java
│   │   │   │   └── UserController.java
│   │   │   ├── model/                         # Entity classes
│   │   │   │   ├── User.java
│   │   │   │   ├── Thread.java
│   │   │   │   ├── Message.java
│   │   │   │   ├── Category.java
│   │   │   │   └── Tag.java
│   │   │   ├── repository/                    # Data access layer
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── ThreadRepository.java
│   │   │   │   └── ...
│   │   │   └── service/                       # Business logic layer
│   │   ├── resources/
│   │   │   ├── application.properties         # Application configuration
│   │   │   ├── static/                        # Static assets
│   │   │   │   ├── css/
│   │   │   │   └── js/
│   │   │   └── templates/                     # Thymeleaf templates
│   │   │       ├── common/
│   │   │       ├── admin/
│   │   │       ├── mod/
│   │   │       └── fragments/
│   └── test/
│       └── java/com/iu/forum/
│           └── ForumApplicationTests.java     # Integration tests
├── pom.xml                                    # Maven configuration
├── mvnw & mvnw.cmd                           # Maven wrapper (Windows/Unix)
└── README.md                                  # This file
```

---

## 🔑 Key Features

- ✅ **User Authentication & Authorization** - Secure login with role-based access
- ✅ **Create & Manage Threads** - Users can create discussion threads
- ✅ **Post Messages** - Reply and comment on threads
- ✅ **Category Management** - Organize discussions by categories
- ✅ **Tagging System** - Tag threads for better discoverability
- ✅ **Admin Dashboard** - Manage users, moderators, and platform settings
- ✅ **Moderator Panel** - Review and moderate content
- ✅ **User Profiles** - View and edit user information
- ✅ **Search Functionality** - Find threads and messages
- ✅ **Responsive UI** - Works on desktop and mobile devices

---

## ⚠️ Known Issues and Limitations

| Issue | Description | Status |
|-------|-------------|--------|
| **CSRF Protection** | CSRF tokens may need configuration for API endpoints | In Progress |
| **File Upload Validation** | Only basic file size validation is implemented | Known Limitation |
| **Real-time Notifications** | No WebSocket implementation; notifications are not real-time | Planned |
| **Email Verification** | Email verification token handling is basic; no email service configured | Planned |
| **Password Recovery** | Password reset functionality needs email integration | Planned |
| **Pagination** | Some views may not have proper pagination for large datasets | Enhancement |
| **Mobile UI** | UI responsiveness could be improved for smaller screens | Enhancement |

---

## 🐛 Troubleshooting

### Issue: Database Connection Error
```
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
```
**Solution:**
- Ensure MySQL is running: `systemctl start mysql` (Linux) or start MySQL from Services (Windows)
- Verify database credentials in `application.properties`
- Check database exists: `CREATE DATABASE forum;`

### Issue: Port 8080 Already in Use
```
Address already in use: bind
```
**Solution:**
- Change port in `application.properties`: `server.port=8081`
- Or kill the process: `lsof -i :8080` and `kill -9 <PID>`

### Issue: Maven Build Fails
```
[ERROR] COMPILATION ERROR
```
**Solution:**
- Verify Java 17+ is installed: `java -version`
- Clean build: `mvn clean install`
- Update Maven: `mvn --version` and upgrade if needed

### Issue: Thymeleaf Template Not Found
```
org.thymeleaf.exceptions.TemplateInputException
```
**Solution:**
- Verify templates exist in `src/main/resources/templates/`
- Check file naming conventions (lowercase with hyphens)
- Clear cache: `mvn clean`

---

## 📞 Support & Contact

For issues, bugs, or feature requests:
- Open an issue on GitHub: [Issues](https://github.com/your-repo/issues)
- Contact the development team:
  - Backend: Đồng Đức Dũng
  - Frontend: Lê Nguyễn Hải Đăng

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 🙏 Acknowledgments

- Built with [Spring Boot](https://spring.io/projects/spring-boot)
- Styled with [Bootstrap](https://getbootstrap.com/)
- Hosted on [Railway](https://railway.app/)

---

**Last Updated:** June 2026
**Version:** 0.0.1
