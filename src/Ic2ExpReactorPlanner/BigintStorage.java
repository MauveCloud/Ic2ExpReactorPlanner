package Ic2ExpReactorPlanner;

import java.math.BigInteger;
import java.util.Base64;

/**
 * Stores numbers of varying size inside a BigInteger, expecting each to have
 * a defined limit (which need not be an exact power of 2).  Numbers are to be
 * extracted in reverse order they were stored, and special values can be used
 * to make certain values optional for inclusion (the calling class is
 * responsible for handling this logic, though).
 * @author Brian McCloud
 */
public class BigintStorage {
    private BigInteger storedValue = BigInteger.ZERO;
    
    /**
     * Stores the specified value.  Requires that 0 &lt;= value &lt;= max.
     * @param value the value to store.
     * @param max the expected maximum for the value.
     */
    public void store(int value, int max) {
        if (value < 0 || value > max) {
            throw new IllegalArgumentException();
        }
        storedValue = storedValue.multiply(BigInteger.valueOf(max + 1)).add(BigInteger.valueOf(value));
    }
    
    /**
     * Extracts a value based on the specified maximum.
     * @param max the expected maximum for the value.
     * @return the extracted value.
     */
    public int extract(int max) {
        BigInteger[] values = storedValue.divideAndRemainder(BigInteger.valueOf(max + 1));
        storedValue = values[0];
        return values[1].intValue();
    }
    
    /**
     * Takes input of a Base64 string, and converts it to a BigintStorage.
     * @param code the Base64-encoded string (presumed to be from @outputBase64)
     * @return the converted storage object.
     */
    public static BigintStorage inputBase64(String code) {
        BigintStorage result = new BigintStorage();
        byte[] temp = Base64.getDecoder().decode(code);
        result.storedValue = new BigInteger(temp);
        return result;
    }
    
    /**
     * Outputs the current value of this BigintStorage as a Base64-encoded string.
     * @return the Base64-encoded string.
     */
    public String outputBase64() {
        byte[] temp = storedValue.toByteArray();
        return Base64.getEncoder().encodeToString(temp);
    }
}
