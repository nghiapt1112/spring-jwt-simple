//package com.example.demo.service;
//
//import com.example.demo.infrastructure.exception.InsufficientPointsException;
//import com.example.demo.infrastructure.exception.InvalidTransactionException;
//import com.example.demo.infrastructure.factory.StandardUserRewardPointsFactory;
//import com.example.demo.infrastructure.factory.UserRewardPointsFactory;
//import com.example.demo.infrastructure.command.RewardCommand;
//import com.example.demo.model.PointTransaction;
//import com.example.demo.model.TransactionType;
//import com.example.demo.infrastructure.observer.RewardEvent;
//import com.example.demo.infrastructure.observer.RewardEventListener;
//import com.example.demo.infrastructure.observer.RewardEventPublisher;
//import com.example.demo.infrastructure.observer.RewardEventType;
//import com.example.demo.repository.InMemoryUserRewardPointsRepository;
//import com.example.demo.repository.UserRewardPointsRepository;
//import com.example.demo.strategy.PointCalculationStrategy;
//import com.example.demo.strategy.StandardPointCalculationStrategy;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//class RewardServiceTest {
//
//    private RewardService rewardService;
//    private UserRewardPointsRepository repository;
//    private PointCalculationStrategy calculationStrategy;
//    private UserRewardPointsFactory factory;
//    private CommandExecutorService commandExecutorService;
//    private RewardEventPublisher eventPublisher;
//    private RewardEventListener eventListener;
//
//    private final String testUserId = "testUser";
//
//    @BeforeEach
//    void setUp() {
//        repository = new InMemoryUserRewardPointsRepository();
//        calculationStrategy = new StandardPointCalculationStrategy();
//        factory = new StandardUserRewardPointsFactory();
//        commandExecutorService = new CommandExecutorService();
//
//        // Set up event publisher with mock listener
//        eventPublisher = new RewardEventPublisher();
//        eventListener = mock(RewardEventListener.class);
//        eventPublisher.addListener(eventListener);
//
//        rewardService = new RewardService(
//                repository,
//                calculationStrategy,
//                factory,
//                commandExecutorService,
//                eventPublisher);
//    }
//
//    @Test
//    void testEarnPoints() {
//        // Test earning points with valid transaction
//        int pointsEarned = rewardService.earnPoints(testUserId, 100.0);
//
//        // Verify points earned
//        assertEquals(1000, pointsEarned); // 100 * 10 points
//        assertEquals(1500, rewardService.getBalance(testUserId)); // 500 initial + 1000 earned
//
//        // Verify event was published
//        ArgumentCaptor<RewardEvent> eventCaptor = ArgumentCaptor.forClass(RewardEvent.class);
//        verify(eventListener).onRewardEvent(eventCaptor.capture());
//
//        RewardEvent capturedEvent = eventCaptor.getValue();
//        assertEquals(RewardEventType.POINTS_EARNED, capturedEvent.getType());
//        assertEquals(testUserId, capturedEvent.getUserId());
//        assertEquals(1000, capturedEvent.getPoints());
//    }
//
//    @Test
//    void testEarnPointsWithInvalidAmount() {
//        // Test with negative amount
//        assertThrows(InvalidTransactionException.class, () ->
//            rewardService.earnPoints(testUserId, -50.0));
//
//        // Test with zero amount
//        assertThrows(InvalidTransactionException.class, () ->
//            rewardService.earnPoints(testUserId, 0.0));
//
//        // Verify no event was published for invalid amounts
//        verify(eventListener, never()).onRewardEvent(any());
//    }
//
//    @Test
//    void testRedeemPoints() {
//        // Ensure user exists with initial balance
//        assertTrue(rewardService.earnPoints(testUserId, 10.0) > 0);
//
//        // Reset mock to clear the earn event
//        reset(eventListener);
//
//        // Test successful redemption
//        assertTrue(rewardService.redeemPoints(testUserId, 100));
//        assertEquals(500, rewardService.getBalance(testUserId)); // 500 initial + 100 earned - 100 redeemed
//
//        // Verify event was published
//        ArgumentCaptor<RewardEvent> eventCaptor = ArgumentCaptor.forClass(RewardEvent.class);
//        verify(eventListener).onRewardEvent(eventCaptor.capture());
//
//        RewardEvent capturedEvent = eventCaptor.getValue();
//        assertEquals(RewardEventType.POINTS_REDEEMED, capturedEvent.getType());
//        assertEquals(testUserId, capturedEvent.getUserId());
//        assertEquals(100, capturedEvent.getPoints());
//    }
//
//    @Test
//    void testRedeemPointsInsufficientBalance() {
//        // Ensure user exists with initial balance
//        assertTrue(rewardService.earnPoints(testUserId, 10.0) > 0);
//
//        // Reset mock to clear the earn event
//        reset(eventListener);
//
//        // Test insufficient balance
//        assertThrows(InsufficientPointsException.class, () ->
//            rewardService.redeemPoints(testUserId, 1000));
//
//        // Verify no event was published for failed redemption
//        verify(eventListener, never()).onRewardEvent(any());
//    }
//
//    @Test
//    void testRedeemPointsInvalidAmount() {
//        // Ensure user exists
//        assertTrue(rewardService.earnPoints(testUserId, 10.0) > 0);
//
//        // Reset mock to clear the earn event
//        reset(eventListener);
//
//        // Test with negative amount
//        assertThrows(InvalidTransactionException.class, () ->
//            rewardService.redeemPoints(testUserId, -50));
//
//        // Test with zero amount
//        assertThrows(InvalidTransactionException.class, () ->
//            rewardService.redeemPoints(testUserId, 0));
//
//        // Verify no event was published for invalid amounts
//        verify(eventListener, never()).onRewardEvent(any());
//    }
//
//    @Test
//    void testGetBalance() {
//        // Test with new user (should create automatically with 500 points)
//        assertEquals(500, rewardService.getBalance("newUser"));
//
//        // Test after earning points
//        rewardService.earnPoints("newUser", 50.0);
//        assertEquals(1000, rewardService.getBalance("newUser")); // 500 initial + 500 earned
//    }
//
//    @Test
//    void testUserExists() {
//        // Initial user should not exist
//        assertFalse(rewardService.userExists("nonExistentUser"));
//
//        // After getting balance, user should exist
//        rewardService.getBalance("nonExistentUser");
//        assertTrue(rewardService.userExists("nonExistentUser"));
//    }
//
//    @Test
//    void testTransactionHistory() {
//        // Perform some transactions
//        rewardService.earnPoints(testUserId, 100.0);
//        rewardService.redeemPoints(testUserId, 200);
//
//        // Get transaction history
//        List<PointTransaction> transactions = rewardService.getTransactionHistory(testUserId);
//
//        // Should have 3 transactions: initial + earn + redeem
//        assertEquals(3, transactions.size());
//
//        // Check transaction types
//        assertEquals(TransactionType.INITIAL, transactions.get(0).getType());
//        assertEquals(TransactionType.EARN, transactions.get(1).getType());
//        assertEquals(TransactionType.REDEEM, transactions.get(2).getType());
//
//        // Check transaction points
//        assertEquals(500, transactions.get(0).getPoints());
//        assertEquals(1000, transactions.get(1).getPoints());
//        assertEquals(200, transactions.get(2).getPoints());
//    }
//
//    @Test
//    void testCommandExecutorTracksCommands() {
//        // Perform some transactions
//        rewardService.earnPoints(testUserId, 100.0);
//        rewardService.redeemPoints(testUserId, 200);
//
//        // Get executed commands
//        List<RewardCommand> commands = commandExecutorService.getExecutedCommands();
//
//        // Should have 2 commands: earn + redeem
//        assertEquals(2, commands.size());
//
//        // Check command types
//        assertEquals(RewardCommand.CommandType.EARN, commands.get(0).getType());
//        assertEquals(RewardCommand.CommandType.REDEEM, commands.get(1).getType());
//    }
//}
