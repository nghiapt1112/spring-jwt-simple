# Loyalty Rewards Wallet API

This is a RESTful API for a loyalty rewards wallet application. The API allows users to:

1. Earn reward points from transactions
2. Redeem reward points for discounts
3. Check their current reward points balance
4. View transaction history

## Design Patterns Implemented

This application implements several design patterns to improve extensibility, maintainability, and testability:

### 1. Repository Pattern
- **Purpose**: Abstracts data access logic from business logic
- **Implementation**: `UserRewardPointsRepository` interface with `InMemoryUserRewardPointsRepository` implementation
- **Benefits**: Easily switch storage mechanisms (e.g., from in-memory to database) without changing business logic

### 2. Strategy Pattern
- **Purpose**: Allows different algorithms for point calculation
- **Implementation**: `PointCalculationStrategy` interface with `StandardPointCalculationStrategy` and `PremiumPointCalculationStrategy` implementations
- **Benefits**: Add new calculation strategies (e.g., for special promotions) without modifying existing code

### 3. Factory Pattern
- **Purpose**: Encapsulates object creation logic
- **Implementation**: `UserRewardPointsFactory` interface with `StandardUserRewardPointsFactory` implementation
- **Benefits**: Centralize creation logic, making it easier to modify and extend

### 4. Command Pattern
- **Purpose**: Encapsulates a request as an object
- **Implementation**: `RewardCommand` interface with `EarnPointsCommand` and `RedeemPointsCommand` implementations
- **Benefits**: Records transaction history, enables undo/redo (if implemented), and simplifies testing

### 5. Observer Pattern
- **Purpose**: Decouples event handling from business logic
- **Implementation**: `RewardEventListener` interface with `LoggingRewardEventListener` implementation, along with `RewardEventPublisher`
- **Benefits**: Add new event handlers (e.g., email notifications, analytics) without modifying business logic

## API Endpoints

### Authentication

```
POST /login
```

Parameters:
- `username`: User's username
- `password`: User's password

Returns:
- JWT token for authentication

### Reward Points Management

All endpoints require authentication via Bearer token.

#### Earn Points

```
POST /rewards/earn
```

Parameters:
- `userId`: User's ID
- `transactionAmount`: Transaction amount (must be positive)

Returns:
- Points earned
- New balance

#### Redeem Points

```
POST /rewards/redeem
```

Parameters:
- `userId`: User's ID
- `points`: Points to redeem (must be positive)

Returns:
- Points redeemed
- New balance

#### Check Balance

```
GET /rewards/balance
```

Parameters:
- `userId`: User's ID

Returns:
- Current points balance

#### Transaction History

```
GET /rewards/transactions
```

Parameters:
- `userId`: User's ID

Returns:
- List of transactions with type, points, description, and timestamp

## Security Features

- Token-based authentication using JWT
- Input validation (positive numbers, integer points, etc.)
- Rate limiting to prevent abuse (default: 20 requests per minute)
- Exception handling with appropriate HTTP status codes
- Thread-safe implementation for concurrent requests

## Benefits of Design Patterns

1. **Extensibility**: Add new features with minimal changes to existing code
2. **Testability**: Components are decoupled and easier to test in isolation
3. **Maintainability**: Clear separation of concerns makes code easier to understand and modify
4. **Flexibility**: Swap out implementations at runtime based on configuration
5. **Code Reuse**: Common logic is centralized and reused

## Running the Application

```bash
./gradlew bootRun
```

The application will start on port 8080 by default.

## Testing

### Sample Login Request

```bash
curl -X POST "http://localhost:8080/login?username=user123&password=password"
```

### Sample Reward Operation

```bash
# First, get a token from the login endpoint
TOKEN="your_jwt_token"

# Earn points
curl -X POST "http://localhost:8080/rewards/earn?userId=user123&transactionAmount=100" \
  -H "Authorization: Bearer $TOKEN"

# Check balance
curl -X GET "http://localhost:8080/rewards/balance?userId=user123" \
  -H "Authorization: Bearer $TOKEN"

# Redeem points
curl -X POST "http://localhost:8080/rewards/redeem?userId=user123&points=500" \
  -H "Authorization: Bearer $TOKEN"
  
# View transaction history
curl -X GET "http://localhost:8080/rewards/transactions?userId=user123" \
  -H "Authorization: Bearer $TOKEN"
```
