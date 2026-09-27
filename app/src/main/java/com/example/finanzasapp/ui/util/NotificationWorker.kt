package com.example.finanzasapp.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.finanzasapp.MainActivity
import com.example.finanzasapp.R
import androidx.navigation.NavDeepLinkBuilder

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(
    context,
    workerParams
) {

    companion object {

        private const val CHANNEL_ID =
            "recordatorios_finanzas"

        fun crearCanal(
            context: Context
        ) {
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O
            ) {
                val channel =
                    NotificationChannel(
                        CHANNEL_ID,
                        "Recordatorios de gastos",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description =
                            "Recordatorios diarios para registrar movimientos"
                    }

                val notificationManager =
                    context.getSystemService(
                        Context.NOTIFICATION_SERVICE
                    ) as NotificationManager

                notificationManager
                    .createNotificationChannel(
                        channel
                    )
            }
        }
    }

    override fun doWork(): Result {
        if (!puedeMostrarNotificaciones()) {
            return Result.success()
        }

        crearCanal(
            applicationContext
        )

        mostrarNotificacion()

        return Result.success()
    }

    private fun puedeMostrarNotificaciones(): Boolean {
        return (
                Build.VERSION.SDK_INT <
                        Build.VERSION_CODES.TIRAMISU ||
                        ContextCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                )
    }

    private fun mostrarNotificacion() {
        val notificationManager =
            applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val pendingIntent =
            NavDeepLinkBuilder(
                applicationContext
            )
                .setGraph(
                    R.navigation.nav_graph
                )
                .setDestination(
                    R.id.agregarMovimientoFragment
                )
                .setComponentName(
                    MainActivity::class.java
                )
                .createPendingIntent()

        val notification =
            NotificationCompat.Builder(
                applicationContext,
                CHANNEL_ID
            )
                .setSmallIcon(
                    R.drawable.ic_stat_name
                )
                .setContentTitle(
                    "¿Olvidaste anotar algo?"
                )
                .setContentText(
                    "Toca aquí para registrar tus movimientos de hoy."
                )
                .setPriority(
                    NotificationCompat.PRIORITY_DEFAULT
                )
                .setCategory(
                    NotificationCompat.CATEGORY_REMINDER
                )
                .setContentIntent(
                    pendingIntent
                )
                .setAutoCancel(true)
                .build()

        notificationManager.notify(
            1,
            notification
        )
    }
}