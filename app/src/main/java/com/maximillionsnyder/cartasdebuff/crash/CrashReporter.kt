package com.maximillionsnyder.cartasdebuff.crash

import android.content.Context
import android.os.Build
import android.util.Log
import com.maximillionsnyder.cartasdebuff.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReporteCrash(val nombre: String, val fecha: Long, val contenido: String) {
    val titulo: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(fecha))
}

/* Guarda los crashes no capturados en filesDir/crashes y los expone para
   verlos/compartirlos desde Ajustes. */
object CrashReporter {

    private const val DIRECTORIO = "crashes"
    private const val MAXIMO = 10
    private val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun instalar(context: Context) {
        val previo = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { hilo, error ->
            runCatching { guardar(context.applicationContext, hilo, error) }
            previo?.uncaughtException(hilo, error)
        }
    }

    fun listar(context: Context): List<ReporteCrash> {
        val dir = File(context.filesDir, DIRECTORIO)
        return dir.listFiles()
            ?.filter { it.isFile }
            ?.sortedByDescending { it.name }
            ?.map { archivo ->
                ReporteCrash(
                    nombre = archivo.name,
                    fecha = archivo.lastModified(),
                    contenido = runCatching { archivo.readText() }.getOrDefault(""),
                )
            }
            .orEmpty()
    }

    fun borrarTodo(context: Context) {
        File(context.filesDir, DIRECTORIO).listFiles()?.forEach { it.delete() }
    }

    fun reporteDePrueba(context: Context) {
        val error = IllegalStateException("Reporte de prueba generado desde Ajustes")
        guardar(context.applicationContext, Thread.currentThread(), error)
    }

    private fun guardar(context: Context, hilo: Thread, error: Throwable) {
        val dir = File(context.filesDir, DIRECTORIO).apply { mkdirs() }
        val fecha = System.currentTimeMillis()
        val archivo = File(dir, "crash-$fecha.txt")
        archivo.writeText(texto(hilo, error, fecha))
        dir.listFiles()
            ?.sortedByDescending { it.name }
            ?.drop(MAXIMO)
            ?.forEach { it.delete() }
    }

    private fun texto(hilo: Thread, error: Throwable, fecha: Long): String = buildString {
        appendLine("Cartas Debuff — reporte de crash")
        appendLine("Fecha: ${formato.format(Date(fecha))}")
        appendLine("Versión: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        appendLine("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
        appendLine("Dispositivo: ${Build.MANUFACTURER} ${Build.MODEL}")
        appendLine("Hilo: ${hilo.name}")
        appendLine()
        appendLine(Log.getStackTraceString(error))
    }
}
