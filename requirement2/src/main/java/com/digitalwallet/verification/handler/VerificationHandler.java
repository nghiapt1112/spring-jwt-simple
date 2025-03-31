package com.digitalwallet.verification.handler;

import com.digitalwallet.service.VerificationApiResult;

import java.util.concurrent.CompletableFuture;

public interface VerificationHandler {
  VerificationHandler setNextHandler(VerificationHandler handler);

  CompletableFuture<VerificationApiResult> verify(VerificationContext context);
}
