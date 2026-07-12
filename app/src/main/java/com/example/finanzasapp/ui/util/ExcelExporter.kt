package com.example.finanzasapp.ui.util
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import com.example.finanzasapp.data.model.Movimiento
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

object ExcelExporter {


    fun exportarAMediastore(context: Context, lista: List<Movimiento>, nombreArchivo: String) {
        try {

            // 1. Crear el libro de trabajo (.xlsx)
            val workbook = XSSFWorkbook()

            // 2. CREAR LA HOJA (Aquí estaba el error, ahora es directo)
            val sheet = workbook.createSheet("Movimientos")


            // 2.1 Crear el formato de moneda
            val formatHelper = workbook.createDataFormat()
            val moneyStyle = workbook.createCellStyle().apply {
                // Este formato pone el símbolo de moneda local, separador de miles y 2 decimales
                // En la mayoría de Excel se traduce automáticamente a $ #,##0.00
                dataFormat = formatHelper.getFormat("$#,##0.00")
            }

            // (Aquí va todo tu código anterior de crear filas y celdas...)
            // ...

            // 3. Crear Estilos
            val headerFont = workbook.createFont().apply {
                setBold(true) // CAMBIO: Usamos el método setter explícito
                color = org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex()
            }

            val headerStyle = workbook.createCellStyle().apply {
                setFont(headerFont)
                fillForegroundColor = org.apache.poi.ss.usermodel.IndexedColors.BLUE_GREY.getIndex()
                fillPattern = org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND
            }

            // 4. Crear Encabezado
            val headerRow = sheet.createRow(0)
            val columnas = listOf("ID", "Descripción", "Monto", "Categoría", "Fecha")
            columnas.forEachIndexed { index, titulo ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(titulo)
                cell.cellStyle = headerStyle
            }

            // 5. Llenar Datos
            lista.forEachIndexed { index, mov ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(mov.id.toDouble())
                row.createCell(1).setCellValue(mov.descripcion)
                val cellMonto = row.createCell(2)
                cellMonto.setCellValue(mov.monto) // Asegúrate que mov.monto sea Double o Float
                cellMonto.cellStyle = moneyStyle // Aplicamos el estilo de moneda
                row.createCell(3).setCellValue(mov.categoria)
                // Puedes formatear la fecha aquí si lo prefieres
                row.createCell(4).setCellValue(java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(mov.fecha))
            }

            // 6. Configurar ancho de columnas manualmente (Evita el error de AWT)
            // El valor 256 * 15 significa aproximadamente 15 caracteres de ancho
            sheet.setColumnWidth(0, 256 * 8)   // ID
            sheet.setColumnWidth(1, 256 * 25)  // Descripción
            sheet.setColumnWidth(2, 256 * 12)  // Monto
            sheet.setColumnWidth(3, 256 * 15)  // Categoría
            sheet.setColumnWidth(4, 256 * 15)  // Fecha

            // LÓGICA DE GUARDADO CON MEDIASTORE
            val resolver = context.contentResolver

            // Seleccionamos la colección dependiendo de la versión de Android
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Downloads.EXTERNAL_CONTENT_URI
            } else {
                // Para Android 8 y 9, usamos la tabla de archivos general
                MediaStore.Files.getContentUri("external")
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$nombreArchivo.xlsx")
                put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+ permite carpetas relativas
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/FinanzasApp")
                }
            }

            val uri = resolver.insert(collection, contentValues)

            uri?.let { outputUri ->
                resolver.openOutputStream(outputUri).use { outputStream ->
                    workbook.write(outputStream)
                }
                workbook.close()

                // Toast de confirmación
                Toast.makeText(context, "Archivo guardado en Descargas", Toast.LENGTH_SHORT).show()

                // --- EL TIP: Lanzar el menú de compartir ---
                compartirArchivo(context, outputUri)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al exportar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun compartirArchivo(context: Context, uri: android.net.Uri) {
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartir reporte mediante:"))
    }
}