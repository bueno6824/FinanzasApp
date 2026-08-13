package com.example.finanzasapp.ui.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

data class DateRange(
    val desde: Long,
    val hasta: Long
)

object DateRangeUtils {

    private val zoneId: ZoneId
        get() = ZoneId.systemDefault()

    private fun rango(
        inicio: LocalDate,
        finExclusivo: LocalDate
    ): DateRange {

        val desde = inicio
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()

        val hasta = finExclusivo
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli() - 1

        return DateRange(desde, hasta)
    }

    fun hoy(): DateRange {
        val hoy = LocalDate.now()

        return rango(
            inicio = hoy,
            finExclusivo = hoy.plusDays(1)
        )
    }

    fun semanaActual(): DateRange {
        val hoy = LocalDate.now()

        val lunes = hoy.with(
            TemporalAdjusters.previousOrSame(
                DayOfWeek.MONDAY
            )
        )

        return rango(
            inicio = lunes,
            finExclusivo = lunes.plusWeeks(1)
        )
    }

    fun mesActual(): DateRange {
        val mes = YearMonth.now()

        return rango(
            inicio = mes.atDay(1),
            finExclusivo = mes.plusMonths(1).atDay(1)
        )
    }

    fun mes(anio: Int, mes: Int): DateRange {
        val yearMonth = YearMonth.of(
            anio,
            mes + 1
        )

        return rango(
            inicio = yearMonth.atDay(1),
            finExclusivo = yearMonth
                .plusMonths(1)
                .atDay(1)
        )
    }

    fun anio(anio: Int): DateRange {
        val inicio = LocalDate.of(
            anio,
            1,
            1
        )

        return rango(
            inicio = inicio,
            finExclusivo = inicio.plusYears(1)
        )
    }
}