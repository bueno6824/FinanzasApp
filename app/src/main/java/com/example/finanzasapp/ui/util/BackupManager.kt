package com.example.finanzasapp.ui.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.finanzasapp.data.model.Movimiento
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupManager {

    fun exportar(
        context: Context,
        movimientos: List<Movimiento>
    ): Boolean {

        return try {

            val movimientosJson =
                JSONArray()

            movimientos.forEach { movimiento ->

                val item =
                    JSONObject().apply {

                        put(
                            "id",
                            movimiento.id
                        )

                        put(
                            "fecha",
                            movimiento.fecha
                        )

                        put(
                            "categoria",
                            movimiento.categoria
                        )

                        put(
                            "descripcion",
                            movimiento.descripcion
                        )

                        put(
                            "tipo",
                            movimiento.tipo
                        )

                        put(
                            "monto",
                            movimiento.monto
                        )
                    }

                movimientosJson.put(item)
            }

            val backup =
                JSONObject().apply {

                    put(
                        "version",
                        1
                    )

                    put(
                        "fechaBackup",
                        System.currentTimeMillis()
                    )

                    put(
                        "movimientos",
                        movimientosJson
                    )
                }

            val fecha =
                SimpleDateFormat(
                    "yyyyMMdd_HHmmss",
                    Locale.getDefault()
                ).format(Date())

            val nombreArchivo =
                "Backup_Finanzas_$fecha.json"

            val resolver =
                context.contentResolver

            val collection =
                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.Q
                ) {

                    MediaStore
                        .Downloads
                        .EXTERNAL_CONTENT_URI

                } else {

                    MediaStore.Files
                        .getContentUri(
                            "external"
                        )
                }

            val values =
                ContentValues().apply {

                    put(
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        nombreArchivo
                    )

                    put(
                        MediaStore.MediaColumns.MIME_TYPE,
                        "application/json"
                    )

                    if (
                        Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.Q
                    ) {

                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            "Download/FinanzasApp"
                        )
                    }
                }

            val uri =
                resolver.insert(
                    collection,
                    values
                ) ?: return false

            resolver
                .openOutputStream(uri)
                ?.bufferedWriter()
                ?.use { writer ->

                    writer.write(
                        backup.toString(2)
                    )
                }
                ?: return false

            true

        } catch (e: Exception) {

            false
        }
    }

    fun leerBackup(
        context: Context,
        uri: Uri
    ): List<Movimiento>? {

        return try {

            val contenido =
                context
                    .contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { reader ->

                        reader.readText()
                    }
                    ?: return null

            val backup =
                JSONObject(contenido)

            val version =
                backup.optInt(
                    "version",
                    -1
                )

            if (version != 1) {
                return null
            }

            val array =
                backup.getJSONArray(
                    "movimientos"
                )

            val movimientos =
                mutableListOf<Movimiento>()

            for (
            i in 0 until array.length()
            ) {

                val item =
                    array.getJSONObject(i)

                movimientos.add(
                    Movimiento(
                        id =
                            item.getInt("id"),

                        fecha =
                            item.getLong("fecha"),

                        categoria =
                            item.getString(
                                "categoria"
                            ),

                        descripcion =
                            item.getString(
                                "descripcion"
                            ),

                        tipo =
                            item.getString(
                                "tipo"
                            ),

                        monto =
                            item.getDouble(
                                "monto"
                            )
                    )
                )
            }

            movimientos

        } catch (e: Exception) {

            null
        }
    }





}