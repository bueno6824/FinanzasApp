package com.example.finanzasapp.ui.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupManager {
    private const val TAG = "BackupManager"

    fun exportarBaseDeDatos(context: Context, nombreBaseDatos: String) {
        try {
            // 1. Cerrar la base de datos para asegurar que los datos se escriban en el archivo .db
            com.example.finanzasapp.data.local.AppDatabase.cerrarDatabase()

            val dbFile: File = context.getDatabasePath(nombreBaseDatos)
            if (!dbFile.exists()) {
                Toast.makeText(context, "No hay datos para respaldar", Toast.LENGTH_SHORT).show()
                return
            }

            val fecha = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val nombreBackup = "Backup_Finanzas_$fecha.db"

            // 🔥 CORRECCIÓN AQUÍ: Usar la carpeta pública de Descargas
            val carpetaDescargas = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            val destino = File(carpetaDescargas, nombreBackup)

            // 2. Copiar los datos
            FileInputStream(dbFile).use { input ->
                FileOutputStream(destino).use { output ->
                    input.copyTo(output)
                }
            }

            // 3. Notificar al sistema para que el archivo sea VISIBLE en el explorador
            android.media.MediaScannerConnection.scanFile(
                context,
                arrayOf(destino.absolutePath),
                null
            ) { path, uri ->
                Log.d(TAG, "Archivo escaneado y visible en: $path")
            }

            Toast.makeText(context, "Respaldo creado en Descargas", Toast.LENGTH_LONG).show()

            // 4. Compartir usando FileProvider
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                destino
            )
            compartirBackup(context, uri)

        } catch (e: Exception) {
            Log.e(TAG, "Error en exportar: ${e.message}")
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // La función de restaurar se mantiene igual, pero recuerda cerrar la DB antes de llamar a restaurar
    fun restaurarBaseDeDatos(context: Context, uriSeleccionada: Uri, nombreBaseDatos: String): Boolean {
        return try {
            com.example.finanzasapp.data.local.AppDatabase.cerrarDatabase()
            val dbFile = context.getDatabasePath(nombreBaseDatos)

            context.contentResolver.openInputStream(uriSeleccionada)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Borrar temporales
            File(dbFile.path + "-wal").delete()
            File(dbFile.path + "-shm").delete()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun compartirBackup(context: Context, uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Guardar respaldo en:"))
    }
}