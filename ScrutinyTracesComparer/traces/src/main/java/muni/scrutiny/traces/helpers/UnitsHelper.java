package muni.scrutiny.traces.helpers;

import java.math.BigDecimal;

public class UnitsHelper {
    public static BigDecimal getInvertedTimeUnitConstant(String unit) {
        String unitToLower = unit.toLowerCase();
        if (unitToLower.contains("ms")) {
            return BigDecimal.valueOf(1000);
        }

        return new BigDecimal(1);
    }
}
