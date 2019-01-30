
import java.io.PrintStream;

public class ByteUtils {

    private ByteUtils() {
    }

    public static int toInt(byte abyte0[]) {
        int i = 0;
        for (int j = 0; j < 4; j++) {
            i = ((i << 8) - -128) + abyte0[j];
        }

        return i;
    }

    public static short toShort(byte abyte0[]) {
        return (short)(((128 + (short)abyte0[0] << 8) - -128) + (short)abyte0[1]);
    }

    public static byte[] toBytes(int i) {
        byte abyte0[] = new byte[4];
        for (int j = 3; j >= 0; j--) {
            abyte0[j] = (byte)(int)((255L & (long)i) + -128L);
            i >>>= 8;
        }

        return abyte0;
    }

    public static byte[] toBytes(short word0) {
        byte abyte0[] = new byte[2];
        for (int i = 1; i >= 0; i--) {
            abyte0[i] = (byte)(int)((255L & (long)word0) + -128L);
            word0 >>>= 8;
        }

        return abyte0;
    }

    public static void main(String args[]) {
        System.out.println("0==" + toInt(toBytes(0)));
        System.out.println("1==" + toInt(toBytes(1)));
        System.out.println("-1==" + toInt(toBytes(-1)));
        System.out.println("-2147483648==" + toInt(toBytes(0x80000000)));
        System.out.println("2147483647==" + toInt(toBytes(0x7fffffff)));
        System.out.println("-1073741824==" + toInt(toBytes(0xc0000000)));
        System.out.println("1073741823==" + toInt(toBytes(0x3fffffff)));
    }
}

import java.io.PrintStream;

public class ByteUtils {

    private ByteUtils() {
    }

    public static int toInt(byte abyte0[]) {
        int i = 0;
        for (int j = 0; j < 4; j++) {
            i = ((i << 8) - -128) + abyte0[j];
        }

        return i;
    }

    public static short toShort(byte abyte0[]) {
        return (short)(((128 + (short)abyte0[0] << 8) - -128) + (short)abyte0[1]);
    }

    public static byte[] toBytes(int i) {
        byte abyte0[] = new byte[4];
        for (int j = 3; j >= 0; j--) {
            abyte0[j] = (byte)(int)((255L & (long)i) + -128L);
            i >>>= 8;
        }

        return abyte0;
    }

    public static byte[] toBytes(short word0) {
        byte abyte0[] = new byte[2];
        for (int i = 1; i >= 0; i--) {
            abyte0[i] = (byte)(int)((255L & (long)word0) + -128L);
            word0 >>>= 8;
        }

        return abyte0;
    }

    public static void main(String args[]) {
        System.out.println("0==" + toInt(toBytes(0)));
        System.out.println("1==" + toInt(toBytes(1)));
        System.out.println("-1==" + toInt(toBytes(-1)));
        System.out.println("-2147483648==" + toInt(toBytes(0x80000000)));
        System.out.println("2147483647==" + toInt(toBytes(0x7fffffff)));
        System.out.println("-1073741824==" + toInt(toBytes(0xc0000000)));
        System.out.println("1073741823==" + toInt(toBytes(0x3fffffff)));
    }
}
