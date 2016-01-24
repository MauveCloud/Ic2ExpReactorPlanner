
package Ic2ExpReactorPlanner;

import java.math.BigInteger;

/**
 * Pulls values out of codes from Talonius's old reactor planner.
 * @author Brian McCloud
 */
public class TaloniusDecoder {
    private BigInteger dataStack = null;
    
    public TaloniusDecoder(final String dataCode) {
        dataStack = new BigInteger(dataCode, 36);
    }
    
    public int readInt(final int bits) {
        return readBigInteger(bits).intValue();
    }
    
    private BigInteger readBigInteger(final int bits) {
        BigInteger data = dataStack.and(BigInteger.ONE.shiftLeft(bits).subtract(BigInteger.ONE));
        dataStack = dataStack.shiftRight(bits);
        return data;
    }
}
