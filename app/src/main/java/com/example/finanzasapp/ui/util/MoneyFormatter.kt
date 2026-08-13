package com.example.finanzasapp.ui.util

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

object MoneyFormatter {

    private val locale =
        Locale.forLanguageTag("es-MX")

    private fun formatoMoneda(): NumberFormat {
        return NumberFormat.getCurrencyInstance(
            locale
        )
    }

    /**
     * Para cantidades normales:
     *
     * 2000   -> $2,000.00
     * -200   -> -$200.00
     */
    fun moneda(
        monto: Double
    ): String {

        val formato =
            formatoMoneda()

        return if (monto < 0) {

            "-${formato.format(abs(monto))}"

        } else {

            formato.format(monto)
        }
    }

    /**
     * Para movimientos:
     *
     * ingreso -> +$2,000.00
     * gasto   -> -$200.00
     */
    fun movimiento(
        monto: Double,
        tipo: String
    ): String {

        val montoFormateado =
            formatoMoneda().format(
                abs(monto)
            )

        return when (
            tipo.lowercase()
        ) {

            "ingreso" ->
                "+$montoFormateado"

            "gasto" ->
                "-$montoFormateado"

            else ->
                montoFormateado
        }
    }
}