# Loyalty Rewards Wallet API

This is a RESTful API for a loyalty rewards wallet application. The API allows users to:

1. Earn reward points from transactions
2. Redeem reward points for discounts
3. Check their current reward points balance

## Security Features

- Token-based authentication using JWT
- Input validation (positive numbers, integer points, etc.)
- Rate limiting to prevent abuse (default: 20 requests per minute)
- Exception handling with appropriate HTTP status codes
- Thread-safe implementation for concurrent requests

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

## Implementation Details

### Data Management

- In-memory storage using `ConcurrentHashMap` for thread safety
- Each user starts with 500 reward points
- Users are created automatically if they don't exist

### Points Calculation

- Earning: 10 points per unit of currency
- Redemption: Direct deduction of requested points

### Error Handling

- `InvalidTransactionException`: For invalid transaction amounts
- `InsufficientPointsException`: When trying to redeem more points than available
- Various validation errors: Missing parameters, incorrect types, etc.

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
```