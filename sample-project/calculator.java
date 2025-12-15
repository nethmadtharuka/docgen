package com.example.util;

/**
 * Calculator - A simple utility class for mathematical operations.
 *
 * Demonstrates various method types for documentation generation.
 */
public class Calculator {

    /**
     * Add two numbers
     *
     * @param a First number
     * @param b Second number
     * @return Sum of a and b
     */
    public int add(int a, int b) {
        return a + b;
    }

    /**
     * Subtract b from a
     *
     * @param a Number to subtract from
     * @param b Number to subtract
     * @return Difference (a - b)
     */
    public int subtract(int a, int b) {
        return a - b;
    }

    /**
     * Multiply two numbers
     *
     * @param a First number
     * @param b Second number
     * @return Product of a and b
     */
    public int multiply(int a, int b) {
        return a * b;
    }

    /**
     * Divide a by b
     *
     * @param a Dividend
     * @param b Divisor (must not be zero)
     * @return Quotient (a / b)
     * @throws ArithmeticException if b is zero
     */
    public double divide(double a, double b) {
        if (b == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return a / b;
    }

    /**
     * Calculate the factorial of n
     *
     * @param n Non-negative integer
     * @return Factorial of n (n!)
     * @throws IllegalArgumentException if n is negative
     */
    public long factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Factorial not defined for negative numbers");
        }
        if (n <= 1) {
            return 1;
        }
        return n * factorial(n - 1);
    }

    /**
     * Check if a number is prime
     *
     * @param n Number to check
     * @return true if n is prime, false otherwise
     */
    public boolean isPrime(int n) {
        if (n <= 1) {
            return false;
        }
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }
}