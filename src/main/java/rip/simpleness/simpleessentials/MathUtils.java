package rip.simpleness.simpleessentials;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MathUtils {

    public static double round(double aDouble) {
        return new BigDecimal(aDouble).setScale(2, RoundingMode.DOWN).doubleValue();
    }
}
