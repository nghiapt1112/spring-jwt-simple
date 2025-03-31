package com.example.demo.model;

public class UserRewardPoints {
    private String userId;
    private int rewardPoints;

    public UserRewardPoints(String userId) {
        this.userId = userId;
        this.rewardPoints = 500; // Initial points
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(int rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    // Add or deduct reward points
    public void addPoints(int points) {
        this.rewardPoints += points;
    }

    public boolean deductPoints(int points) {
        if (this.rewardPoints >= points) {
            this.rewardPoints -= points;
            return true;
        }
        return false;
    }
}
