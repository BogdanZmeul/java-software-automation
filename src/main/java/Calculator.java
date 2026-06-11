public class Calculator {

    public int add(int a, int b) {
        return a + b;
    }

    public double divide(double a, double b) {
        if (b == 0) {
            throw new IllegalArgumentException("Can't devide by zero!");
        }
        return a / b;
    }

    public boolean isEven(int number) {
        return number % 2 == 0;
    }

    public long factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Negative numbers are not allowed for factorial!");
        }
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
}