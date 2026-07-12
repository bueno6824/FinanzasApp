package com.example.finanzasapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.finanzasapp.MainActivity // Asegúrate de que este sea el nombre de tu actividad principal
import com.example.finanzasapp.R

class NotificationWorker (context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        showNotification()
        return Result.success()
    }

    private fun showNotification() {
        val channelId = "recordatorios_finanzas"
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Crear el Canal (Solo para Android 8.0+)
        val channel = NotificationChannel(
            channelId, "Recordatorios de Gastos",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)


        // 2. Configurar la acción al hacer clic
        // Esto abrirá la actividad principal de tu app
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE // Obligatorio en versiones modernas de Android
        )

        // 3. Construir la notificación con el clic incluido
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("¿Olvidaste anotar algo?")
            .setContentText("Toca aquí para registrar tus movimientos de hoy.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // <--- Aquí vinculamos el clic
            .setAutoCancel(true) // Hace que la notificación desaparezca al tocarla

        notificationManager.notify(1, builder.build())
    }
}