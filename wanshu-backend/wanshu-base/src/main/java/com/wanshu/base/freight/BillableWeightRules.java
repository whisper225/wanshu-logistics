package com.wanshu.base.freight;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 普快计费重量进位规则（与产品说明一致）。
 */
public final class BillableWeightRules {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal TEN = BigDecimal.valueOf(10);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private BillableWeightRules() {
    }

    /**
     * 不满 1kg 按 1kg；10kg 以下 0.1kg 四舍五入保留 1 位小数（若进到 ≥10 再按 10–100kg 档处理）；
     * 10–100kg：按 0.5kg 向上取档；≥100kg：四舍五入取整。
     */
    public static BigDecimal normalize(BigDecimal rawKg) {
        if (rawKg == null || rawKg.compareTo(ZERO) <= 0) {
            return ONE;
        }
        BigDecimal w = rawKg;
        if (w.compareTo(ONE) < 0) {
            w = ONE;
        }
        if (w.compareTo(TEN) < 0) {
            w = w.setScale(1, RoundingMode.HALF_UP);
            if (w.compareTo(TEN) >= 0) {
                return ceilToHalfKg(w);
            }
            return w;
        }
        if (w.compareTo(HUNDRED) < 0) {
            return ceilToHalfKg(w);
        }
        return w.setScale(0, RoundingMode.HALF_UP);
    }

    /** [10,100) ：向上取整到 0.5kg */
    private static BigDecimal ceilToHalfKg(BigDecimal w) {
        BigDecimal doubled = w.multiply(TWO);
        BigDecimal ceiled = doubled.setScale(0, RoundingMode.CEILING);
        return ceiled.divide(TWO, 1, RoundingMode.HALF_UP);
    }
}
