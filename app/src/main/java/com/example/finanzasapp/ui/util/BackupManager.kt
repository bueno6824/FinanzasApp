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
        // EXPORTAR (Ahorra usa MediaStore como el ExcelExporter)
    fun exportarBaseDeDatos(context: Context, nombreBaseDatos: String) {
        try {
            // Ya no cerramos la DB Room permite copiar el archivo mientras está abierto
            // Si la app está escribiendo, Room usa bloqueos para evitar corrupción
            val dbFile: File = context.getDatabasePath(nombreBaseDatos)
            if (!dbFile.exists()) {
                Toast.makeText(context, "No hay datos para respaldar", Toast.LENGTH_SHORT).show()
                return
            }


            val fecha = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val nombreBackup = "Backup_Finanzas_$fecha.db"

            // Usamos MediaStore (igual que en Excel)
            val resolver = context.contentResolver

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Downloads.EXTERNAL_CONTENT_URI
            } else {
                // Para android 9 o menos, este es un fallback pero necesitaras permisos WRITE_EXTERNAL_STORAGE
                MediaStore.Files.getContentUri("external")
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, nombreBackup)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/FinanzasApp")
                }
            }

            val uri = resolver.insert(collection, contentValues)

            uri?.let { outputUri ->
                resolver.openOutputStream(outputUri)?.use { outputStream ->
                    FileInputStream(dbFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Toast.makeText(
                    context,
                    "✅ Respaldo guardado en Descargas/FinanzasApp",
                    Toast.LENGTH_LONG
                ).show()
                //Opcional: compartir
                compartirBackup(context, outputUri)
            } ?: run {
                Toast.makeText(
                    context,
                    "❎ Error al crear el archivo en MediaStore",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en exportar: ${e.message}", e)
            Toast.makeText(context, "❎ Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    // RESTAURAR (Corregida: borrará archivos viejos ANTES de copiar)
    fun restaurarBaseDeDatos(context: Context, uriSeleccionada: Uri, nombreBaseDatos: String): Boolean {
        return try {
            // Cerramos la DB para liberar el archivo (Necesario al restaurar)
            com.example.finanzasapp.data.local.AppDatabase.cerrarDatabase()
            val dbFile = context.getDatabasePath(nombreBaseDatos)

            // Eliminar los archivos viejos ANTES de copiar
            if(dbFile.exists()){
                dbFile.delete()
            }

            // Copiamos en nuevo archivo
            context.contentResolver.openInputStream(uriSeleccionada)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                    output.flush()
                }
            }?: return false

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al restaurar: ${e.message}", e)
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