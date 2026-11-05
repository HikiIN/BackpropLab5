package org.example;

public class HopfieldNetwork {
    private final int n;
    private final int[][] w;
    private int lastIterations;

    public HopfieldNetwork(int n) {
        this.n = n;
        this.w = new int[n][n];
    }

    public static int[] toBipolar(int[] binary) {
        int[] res = new int[binary.length];
        for (int i = 0; i < binary.length; i++) {
            res[i] = (binary[i] > 0) ? 1 : -1;
        }
        return res;
    }

    public static int[] toBinary(int[] bipolar) {
        int[] res = new int[bipolar.length];
        for (int i = 0; i < bipolar.length; i++) {
            res[i] = (bipolar[i] > 0) ? 1 : 0;
        }
        return res;
    }

    public void train(int[][] binaryPatterns) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                w[i][j] = 0;
            }
        }
        for (int[] pat : binaryPatterns) {
            int[] x = toBipolar(pat);
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        w[i][j] += x[i] * x[j];
                    }
                }
            }
        }
    }

    public int[] recognize(int[] binaryInput, int maxIterations) {
        int[] y = toBipolar(binaryInput);
        lastIterations = 0;

        for (int iter = 1; iter <= maxIterations; iter++) {
            boolean changed = false;
            for (int j = 0; j < n; j++) {
                int s = 0;
                for (int i = 0; i < n; i++) {
                    if (i != j) {
                        s += y[i] * w[i][j];
                    }
                }
                int nextVal = (s > 0) ? 1 : ((s < 0) ? -1 : y[j]);
                if (nextVal != y[j]) {
                    y[j] = nextVal;
                    changed = true;
                }
            }
            lastIterations = iter;
            if (!changed) {
                break;
            }
        }
        return toBinary(y);
    }

    public double getMaxCapacity() {
        return n / (2.0 * Math.log(n) + Math.log(Math.log(n)));
    }

    public int getLastIterations() {
        return lastIterations;
    }

    public int getSize() {
        return n;
    }

    public int[][] getWeights() {
        return w;
    }
}
