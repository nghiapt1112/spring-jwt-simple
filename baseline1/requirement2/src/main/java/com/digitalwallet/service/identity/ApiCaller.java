package com.digitalwallet.service.identity;

import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Interface to abstract API calls
 * This breaks the circular dependency between the service and providers
 */
public interface ApiCaller {
    ResponseEntity<String> callVerificationApi(String endpoint, Object requestBody, Map<String, String> headers);
}