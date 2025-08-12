package service.adapters.demanding.restapi

import java.math.BigDecimal
import java.math.RoundingMode

fun BigDecimal.toCents(): Int = multiply(BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).toInt()
