//package com.digitalwallet.verification.handler;
//
//import com.digitalwallet.service.VerificationApiResult;
//import com.digitalwallet.service.VerificationOptions;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.concurrent.CompletableFuture;
//
///**
// * Transaction limit verification handler.
// */
//@Slf4j
//public class TransactionLimitHandler extends AbstractVerificationHandler {
//    @Override
//    public CompletableFuture<VerificationApiResult> verify(VerificationContext context) {
//        context.addProcessedStep("Transaction Limit Verification");
//
//        VerificationOptions options = context.getOptions();
//
//        // Explicitly check for high-risk transaction with amount > 10000
//        if (options != null &&
//            options.isHighRiskTransaction() &&
//            options.getTransactionAmount() > 10000) {
//
//            context.addFailureReason("Transaction amount exceeds limit");
//            log.warn("High-risk transaction detected: Amount {}",
//                options.getTransactionAmount());
//
//            return CompletableFuture.completedFuture(
//                createFailedVerification(
//                    "TRANSACTION_LIMIT_EXCEEDED",
//                    "Transaction amount requires additional verification"
//                )
//            );
//        }
//
//        return processNext(context);
//    }
//}
