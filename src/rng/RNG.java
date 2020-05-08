package rng;

import java.math.BigInteger;

public class RNG {
    public static final int MULTIPLIER = 0x41C64E61;
    public static final int ADDEND = 0x6073;
    public static final long MODULUS = 0x100000000L;
    public static final int MULTIPLIER_INVERSE;

    // The following are helpful to calculate RNG in O(log n)
    private static final int[] a;
    private static final int[] b;

    private int value;
    private int frame;

    static {
        a = new int[32];
        b = new int[32];
        MULTIPLIER_INVERSE = genLCGConstants();
    }

    public RNG(int v) {
        this.value = v;
        this.frame = 0;
    }

    public RNG(Seed seed) {
        this.value = seed.getValue();
        this.frame = 0;
    }

    public RNG(int v, int f) {
        this.value = v;
        this.frame = f;
    }

    public void advance() {
        this.value = this.value * MULTIPLIER + ADDEND;
        this.frame++;
    }

    public int getAndAdvance() {
        this.advance();
        return this.getTop();
    }

    public void advance(int n) {
        this.frame += n;
        int i = 0;
        while (n > 0) {
            if (n % 2 != 0)
                this.value = this.value * a[i] + b[i];
            n = n >> 1;
            i++;
            if (i >= 32)
                break;
        }
    }

    public void decrease() {
        this.value = (this.value - ADDEND) * MULTIPLIER_INVERSE;
        this.frame--;
    }

    public void decrease(int n) {
        for (int i = 0; i < n; i++) {
            this.decrease();
        }
    }

    public void gotoFrame(int n) {
        int diff = n - frame;
        if (diff >= 0) {
            this.advance(diff);
        } else {
            for (int i = 0; i < diff; i++) {
                this.value = (this.value - ADDEND) * MULTIPLIER_INVERSE; // backwards formula
            }
        }
        this.frame = n;
    }

    public RNG getCopy() {
        return new RNG(this.getValue(), this.getFrame());
    }

    public void setValue(int v) {
        this.value = v;
    }

    public int getValue() {
        return this.value;
    }

    public int getFrame() {
        return this.frame;
    }

    public int getTop() {
        return (this.value >> 16) & 0xFFFF;
    }

    public int moduloCheck(int check) {
        return this.getTop() % check;
    }

    public static int genLCGConstants() {
        BigInteger multiplier = BigInteger.valueOf(MULTIPLIER);
        BigInteger addend = BigInteger.valueOf(ADDEND);
        BigInteger modulus = BigInteger.valueOf(MODULUS);
        BigInteger multiplierMinusOne = BigInteger.valueOf(MULTIPLIER - 1);
        BigInteger multiplierMinusOneTimesModulus = multiplierMinusOne.multiply(modulus);
        
        for (int i = 0; i < 32; i++) {
            BigInteger n = BigInteger.valueOf(1 << i);
            RNG.a[i] = (int)multiplier.modPow(n, modulus).longValueExact();
            RNG.b[i] = (int)multiplier.modPow(n, multiplierMinusOneTimesModulus).subtract(BigInteger.ONE).divide(multiplierMinusOne).multiply(addend).longValueExact();
        }
        return (int)multiplier.modInverse(modulus).longValueExact();
    }

    public void copy(RNG rng1) {
        this.value = rng1.getValue();
        this.frame = rng1.getFrame();
    }
}
