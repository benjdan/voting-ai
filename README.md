# Generic Rest/Springboot Voting Application
## 🎯 What You'll Build
A Generic Spring Boot voting application with blockchain-inspired immutable ledger, hexagonal architecture, and AI capabilities.

🧑‍🍳 Features
- **Hexagonal Architecture**: Clean separation of concerns with domain, application, infrastructure, and API layers
  - Capable of integrating to different programming languages and ui frameworks
- 🔐**Email-based Registration & Authentication**: Simple JWT-based authentication system
- 📝**CRUD Voting System**: Create, read, and participate in voting polls
- 🔐**Blockchain-Inspired Ledger**: Immutable vote records using SHA-256 hash chaining
- 🔄**AI Integration**: 
  - OpenAI GPT for vote description enhancement and insights
  - xAI Grok for advanced vote analysis
- 🏠**RESTful API**: Designed for React Native/React app consumption
- 🗑️**API Documentation**: Interactive Swagger/OpenAPI documentation
- 🗑️**Tamper-Proof Verification**: Blockchain integrity verification endpoint

🧠 What You’ll Learn
- Implement authentication & email verification
- Build a springboot backend services(CRUD style)
- Integration of AI and blockchain technologies
  
## Technology Stack
- **Framework**: Spring Boot 3.2.0 with AI
- **Language**: Java 19 / Java 21 (Docker)
- **Database**: H2 (in-memory for development)
- **Security**: Spring Security with JWT
- **AI Services**: OpenAI API, xAI API
- **Documentation**: SpringDoc OpenAPI 3
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito, Spring Boot Test

## Architecture

### Hexagonal Architecture (Ports & Adapters)
```
src/main/java/com/voting/
├── domain/              # Core business logic
│   ├── model/          # Entities (User, Vote, VoteOption, BlockchainRecord)
│   ├── port/           # Interfaces (Repositories, Services)
│   └── valueobject/    # Value objects (VoteRecord)
├── application/         # Use cases
│   └── usecase/        # Business workflows
├── infrastructure/      # External adapters
│   ├── persistence/    # JPA repositories
│   ├── ai/            # AI service implementations
│   ├── blockchain/    # Blockchain service
│   └── security/      # JWT and authentication
└── api/                # HTTP interface
    ├── controller/    # REST controllers
    ├── dto/          # Data transfer objects
    └── config/       # Configuration classes
```

## Setup & Running

### Prerequisites
- Java 19 or higher (Java 21 recommended for Docker deployments)
- Maven 3.6+
- (Optional) OpenAI API key for AI features
- (Optional) xAI API key for Grok integration

**Note**: The project is configured to use Java 19, but the Docker image uses Java 21 for containerized deployments. Both versions are fully supported.

### Environment Variables
Create a `.env` file or set these environment variables:

```bash
SESSION_SECRET=your-super-secret-jwt-key-min-32-characters
OPENAI_API_KEY=your-openai-api-key (optional)
XAI_API_KEY=your-xai-api-key (optional)
```

### Running Locally
```bash
# Install dependencies and build
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8085`

### Running with Docker
```bash
# Build the Docker image
docker build -t blockchain-voting:latest .

# Run the container
docker run -p 8085:8085 \
  -e SESSION_SECRET=your-secret-key \
  -e OPENAI_API_KEY=your-openai-key \
  -e XAI_API_KEY=your-xai-key \
  blockchain-voting:latest
```

## API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8085/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8085/v3/api-docs

### Key Endpoints

#### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token

#### Voting
- `POST /api/votes` - Create a new vote
- `GET /api/votes` - Get all votes
- `GET /api/votes/active` - Get active votes
- `GET /api/votes/{id}` - Get specific vote
- `POST /api/votes/{id}/cast` - Cast a vote
- `GET /api/votes/{id}/history` - Get blockchain history for a vote

#### AI Features
- `GET /api/ai/insights/{voteId}` - Get AI-powered vote insights

#### Blockchain
- `GET /api/blockchain/verify` - Verify blockchain integrity

## Blockchain Ledger

The application uses a blockchain-inspired immutable ledger:

- Each vote creates a block with SHA-256 hash
- Blocks are chained using previous block's hash
- Genesis block uses a predefined hash
- Vote tampering can be detected via `/api/blockchain/verify`

### Block Structure

```json
{
  "blockNumber": 0,
  "previousHash": "0000...0000",
  "currentHash": "a1b2c3...",
  "timestamp": "2024-01-01T12:00:00",
  "data": "User:1 voted for option:2 in vote:1",
  "nonce": "1234567890"
}
```

## Development

### Running Tests
The project includes comprehensive unit and integration tests covering all major components:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=VoteControllerIntegrationTest

# Run tests in continuous mode
mvn test -Dmaven.test.failure.ignore=true
```

**Test Coverage:**
- **Unit Tests**: Domain entities, use cases, and blockchain service
- **Integration Tests**: REST API endpoints with authentication

Tests can use JUnit 5, Mockito for mocking, and Spring Boot Test for integration testing.

### H2 Console

Access the H2 database console at: http://localhost:8085/h2-console

- JDBC URL: `jdbc:h2:mem:votingdb`
- Username: `sa`
- Password: (empty)

### Testing the API

Use the Swagger UI or tools like Postman/cURL:

```bash
# Register a user
curl -X POST http://localhost:8085/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123","name":"John Doe"}'

# Login
curl -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# Create a vote (with JWT token)
curl -X POST http://localhost:8085/api/votes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "title":"Favorite Programming Language",
    "description":"Vote for your favorite programming language",
    "options":["Java"],
    "startDate":"2024-01-01T00:00:00",
    "endDate":"2026-12-31T23:59:59",
    "useAIEnhancement":false
  }'
```

## AI Features

### OpenAI Integration
- Enhances vote descriptions for clarity and engagement
- Provides insights on voting patterns
- Requires `OPENAI_API_KEY` environment variable

### xAI/Grok Integration
- Alternative AI provider for vote analysis
- Requires `XAI_API_KEY` environment variable

## Deployment

### Docker Deployment
The application includes a multi-stage Dockerfile optimized for production:

Build stage: Compiles the application with Maven
Runtime stage: Runs the application with minimal JRE

### Environment-Specific Configuration
For production deployments, consider:

1. Using PostgreSQL instead of H2
2. Enabling HTTPS/TLS
3. Implementing rate limiting
4. Setting up monitoring and logging


No Copyright No Rights Reserved No Attribution Required For more information, see the MIT license.
