package com.funkyfunctor.demo

import com.funkyfunctor.demo.demowebapp.FraudProcessor

class WithdrawalFraudProcessor extends FraudProcessor {
  val THRESHOLD: Int =
    10_000_000; //Value in cents - if the transaction is more than $100 000 dollars, we should be suspicious

  /** This method checks if the amount is suspicious.
    * In this case, we consider an amount suspicious if it is greater than 1 million dollars.
    *
    * @param amount The amount to check
    * @return true if the amount is suspicious, false otherwise
    */
  def isSuspiciousAmount(amount: Int): Boolean = amount > THRESHOLD
}
