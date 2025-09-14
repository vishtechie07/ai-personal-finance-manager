# Personal Finance Manager

A comprehensive personal finance management application built with Spring Boot (Java) backend and Vue.js frontend. This project helps users track spending, manage budgets, and gain insights into their financial habits through smart categorization and data visualization.

## Features

### Core Functionality
- **User Authentication**: Secure login and registration system with JWT tokens
- **Transaction Management**: Add, view, update, and delete financial transactions
- **Budget Management**: Create and monitor budgets for various spending categories
- **Data Visualization**: Interactive charts and graphs for financial insights

### Smart Features
- **Intelligent Transaction Categorization**: Automatically suggests spending categories based on transaction descriptions
- **Spending Pattern Detection**: Identifies unusual spending patterns and provides alerts
- **Financial Insights**: Generates spending analysis and budget recommendations

## Tech Stack

### Backend
- **Java 17** with **Spring Boot 3.2.0**
- **Spring Security** with JWT authentication
- **Spring Data JPA** with **Hibernate**
- **H2 Database** (development) / **PostgreSQL** (production ready)
- **Maven** for dependency management

### Frontend
- **Vue.js 3** with Composition API
- **Vue Router** for navigation
- **Pinia** for state management
- **Tailwind CSS** for styling
- **Chart.js** for data visualization
- **Vite** for build tooling

## Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- Maven 3.6 or higher

## Setup Instructions

### Backend Setup

1. **Navigate to backend directory:**
   ```bash
   cd backend
   ```

2. **Set environment variables:**
   ```bash
   # Windows
   set JWT_SECRET=your_super_secret_jwt_key_here
   
   # Linux/Mac
   export JWT_SECRET=your_super_secret_jwt_key_here
   ```

3. **Build and run the application:**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **Access the application:**
   - API: http://localhost:8080/api
   - H2 Console: http://localhost:8080/h2-console
   - Database URL: `jdbc:h2:mem:financedb`
   - Username: `sa`
   - Password: `password`

### Frontend Setup

1. **Navigate to frontend directory:**
   ```bash
   cd frontend
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Start development server:**
   ```bash
   npm run dev
   ```

4. **Access the application:**
   - Frontend: http://localhost:3000
   - API Proxy: http://localhost:3000/api (proxies to backend)

### Production Build

1. **Build the frontend:**
   ```bash
   cd frontend
   npm run build
   ```

2. **The built files will be in `frontend/dist/`**

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `GET /api/auth/me` - Get current user info

### Transactions
- `GET /api/transactions` - Get all transactions
- `POST /api/transactions` - Create new transaction
- `PUT /api/transactions/{id}` - Update transaction
- `DELETE /api/transactions/{id}` - Delete transaction
- `GET /api/transactions/statistics` - Get transaction statistics
- `GET /api/transactions/trends` - Get spending trends

### Budgets
- `GET /api/budgets` - Get all budgets
- `POST /api/budgets` - Create new budget
- `PUT /api/budgets/{id}` - Update budget
- `DELETE /api/budgets/{id}` - Delete budget

## Project Structure

```
personal-finance-manager/
├── backend/
│   ├── src/main/java/com/finance/manager/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── model/          # JPA entities
│   │   ├── repository/     # Data access layer
│   │   └── service/        # Business logic
│   └── src/main/resources/
│       └── application.yml
├── frontend/
│   ├── src/
│   │   ├── assets/         # Static assets
│   │   ├── components/     # Vue components
│   │   ├── router/         # Vue router configuration
│   │   ├── stores/         # Pinia stores
│   │   └── views/          # Page components
│   ├── public/             # Public assets
│   └── package.json
└── README.md
```

## Development

### Backend Development
- The application uses H2 in-memory database for development
- JPA DDL is set to `create-drop` for easy development
- Spring Security is configured with JWT authentication
- Smart categorization uses keyword-based pattern matching

### Frontend Development
- Vue 3 Composition API for modern Vue development
- Tailwind CSS for utility-first styling
- Pinia for state management
- Vue Router for navigation
- Chart.js for data visualization

## Database Schema

The application uses a simple but effective database design:
- **Users**: Basic user information and authentication
- **Transactions**: Financial transactions with categories and amounts
- **Budgets**: Budget categories with spending limits and tracking

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For support and questions, please open an issue in the GitHub repository.
