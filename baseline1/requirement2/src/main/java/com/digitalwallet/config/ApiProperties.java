package com.digitalwallet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Configuration properties for API integration.
 */
@Component
@ConfigurationProperties(prefix = "app.api")
public class ApiProperties {
    
    private String verificationBaseUrl;
    private String verificationApiKey;
    private String verificationApiSecret;
    private String encryptionSecret;
    private BigDecimal highRiskThreshold = new BigDecimal("1000.00");

    public String getVerificationBaseUrl() {
        return verificationBaseUrl;
    }

    public void setVerificationBaseUrl(String verificationBaseUrl) {
        this.verificationBaseUrl = verificationBaseUrl;
    }

    public String getVerificationApiKey() {
        return verificationApiKey;
    }

    public void setVerificationApiKey(String verificationApiKey) {
        this.verificationApiKey = verificationApiKey;
    }

    public String getVerificationApiSecret() {
        return verificationApiSecret;
    }

    public void setVerificationApiSecret(String verificationApiSecret) {
        this.verificationApiSecret = verificationApiSecret;
    }

    public String getEncryptionSecret() {
        return encryptionSecret;
    }

    public void setEncryptionSecret(String encryptionSecret) {
        this.encryptionSecret = encryptionSecret;
    }

    public BigDecimal getHighRiskThreshold() {
        return highRiskThreshold;
    }

    public void setHighRiskThreshold(BigDecimal highRiskThreshold) {
        this.highRiskThreshold = highRiskThreshold;
    }
}