# SpendSense

SpendSense is a personal finance app with a Spring Boot backend and Vue.js frontend. Track spending, manage budgets and bills, and get AI-assisted category suggestions—with optional platform-hosted OpenAI for easy trials on Render.

## Features

### Core Functionality
- **User Authentication**: JWT-based login and registration; passwords stored with BCrypt
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
- **JWT authentication** (Spring Security) and BCrypt password hashing
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

2. **Set environment variables (local):**
   ```bash
   # Windows
   set SPRING_PROFILES_ACTIVE=local
   
   # Linux/Mac
   export SPRING_PROFILES_ACTIVE=local
   ```

3. **Build and run the application:**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **Access the application:**
   - API: http://localhost:8080/api
   - H2 Console: http://localhost:8080/api/h2-console
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

### Trial login & sample data

With the **`local`** profile (default for `mvn spring-boot:run`), the API seeds a single trial account and **multi-month** sample transactions and budgets (this month plus the previous two). Users who **register** through the UI get the same sample dataset for their new account.

| Field    | Value            |
|----------|------------------|
| Username | `spendsense`     |
| Password | `TrySpend2026!`  |

Full details: **[docs/DEMO_CREDENTIALS.md](docs/DEMO_CREDENTIALS.md)**

**Postgres / Docker already has old seed users?** With `local`, consolidation defaults to **on**: if the seed user is missing but other users exist, startup **clears all users, transactions, and budgets** and recreates the trial account. You can also set **`APP_SEED_CONSOLIDATE_LEGACY_USERS=true`** once on Railway (or wipe the DB volume), restart, then sign in with the credentials above.

### Production Build

1. **Build the frontend:**
   ```bash
   cd frontend
   npm run build
   ```

2. **The built files will be in `frontend/dist/`**

### Deploy on Render (recommended)

Step-by-step instructions: **[docs/RENDER_DEPLOY.md](docs/RENDER_DEPLOY.md)**

Quick checklist:

1. Create **PostgreSQL** on Render, then a **Web Service** from this repo (**Docker**, root `Dockerfile`).
2. Set **port `80`** and health check **`/api/actuator/health`**.
3. Environment:
   - `SPRING_PROFILES_ACTIVE=render`
   - `JWT_SECRET` (≥ 32 characters)
   - `DATABASE_HOST`, `DATABASE_PORT`, `DATABASE_NAME`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` (from the linked Postgres **Internal** values)
4. Sign in with **`spendsense`** / **`TrySpend2026!`** after the first deploy (see [docs/DEMO_CREDENTIALS.md](docs/DEMO_CREDENTIALS.md)).

Optional: use **`render.yaml`** (Blueprint) to provision DB + web service. Copy **`.env.example`** → **`.env`** and run **`docker compose up --build`** to test the same stack locally on http://localhost:8080.

### OpenAI (optional)

Hosted trials can use a platform **`OPENAI_API_KEY`**; users may also save their own key in **Settings**. See **[docs/OPENAI.md](docs/OPENAI.md)**.

### Deploy on Railway (Docker + Postgres)

Same Docker image as Render. Set `SPRING_PROFILES_ACTIVE=railway` and `DATABASE_URL=jdbc:postgresql://...` plus credentials. Expose port **80**; health check `/api/actuator/health`. See `application-railway.yml` for variable names.

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
spendsense/   # repository folder name may differ
├── backend/
│   ├── src/main/java/com/finance/manager/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── model/          # JPA entities
│   │   ├── repository/     # Data access layer
│   │   └── service/        # Business logic
│   └── src/main/resources/
│       ├── application.yml
│       ├── application-local.yml
│       ├── application-railway.yml
│       └── application-render.yml
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
