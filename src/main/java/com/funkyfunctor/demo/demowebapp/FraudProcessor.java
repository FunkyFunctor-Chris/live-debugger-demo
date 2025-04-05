package com.funkyfunctor.demo.demowebapp;

public interface FraudProcessor {
    int THRESHOLD();

    boolean isSuspiciousAmount(int amount);
}
