@file:Suppress("unused")

package no.elhub.tools.autorelease.log

import java.io.File
import java.time.LocalDateTime

/**
 * A standalone dedicated logger for the application.
 * The main motivation is to avoid extra dependencies and configuration for logging.
 *
 * @property verbose an array of `--verbose` options which sets the verbosity level
 * based on array's size (only true values are counted).
 */
@Suppress("ArrayPrimitive")
class Logger(private val verbose: Array<Boolean>) {

    private companion object {
        val outDir by lazy { File("./out").apply { if (!exists()) mkdir() } }
    }

    /**
     * Creates a new file with the specified fileName and appended current timestamp in millis.
     * The fileName can contain an extension name, which will be appended after the timestamp.
     */
    private val newFile: (fileName: String) -> File = { fileName: String ->
        val name = fileName.substringBeforeLast(".")
        val ext = when (val extension = fileName.substringAfterLast(".", "")) {
            "" -> ""
            else -> ".$extension"
        }

        File(outDir, "$name-${System.currentTimeMillis()}$ext")
    }

    /**
     * Prints the [msg] to stdout.
     * These messages are always printed,
     * irrespective of whether `--verbose` option is used or not.
     */
    fun always(msg: String) {
        println("${Color.PURPLE}[${LocalDateTime.now()}]: $msg${Color.NONE}")
    }

    /**
     * Prints DEBUG [msg] to stdout. Debug messages are logged if verbosity level >= 3
     */
    @Suppress("MagicNumber")
    fun debug(msg: String) {
        if (verbose.size >= 3) {
            println("${Color.WHITE}[DEBUG] [${LocalDateTime.now()}]: $msg${Color.NONE}")
        }
    }

    /**
     * Prints INFO [msg] to stdout. Informational messages are logged if verbosity level >= 2
     */
    fun info(msg: String) {
        if (verbose.size >= 2) {
            println("${Color.BLUE}[INFO] [${LocalDateTime.now()}]: $msg${Color.NONE}")
        }
    }

    /**
     * Prints WARN [msg] to stdout. Warnings are logged if verbosity level >= 1
     */
    fun warn(msg: String) {
        if (verbose.isNotEmpty()) {
            println("${Color.YELLOW}[WARN] [${LocalDateTime.now()}]: $msg${Color.NONE}")
        }
    }

    /**
     * Prints an ERROR [msg]. Errors are always logged.
     */
    fun error(msg: String) {
        println("${Color.RED}[ERROR] [${LocalDateTime.now()}]: $msg${Color.NONE}")
    }

    @Suppress("TooGenericExceptionCaught")
    fun <R : Any?> loggable(block: () -> R): R {
        val methodName = block.javaClass.enclosingMethod.name

        val result = try {
            if (methodName != "invoke") debug("Invoke function $methodName()")
            block.invoke()
        } catch (e: Exception) {
            e.message?.let { debug(it) }
            e.printStackTrace()
            throw e
        } catch (e: Error) {
            e.message?.let { debug(it) }
            e.printStackTrace()
            throw e
        }

        return result.also {
            if (methodName != "invoke") debug("Function $methodName() finished with return => $it")
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun <V : Any?, R : Any?> loggable(vararg args: V, block: () -> R): R {
        val methodName = block.javaClass.enclosingMethod.name

        val result = try {
            if (methodName != "invoke") debug("Invoke function $methodName() with args [${args.joinToString(", ")}]")
            block.invoke()
        } catch (e: Exception) {
            e.message?.let { debug(it) }
            e.printStackTrace()
            throw e
        } catch (e: Error) {
            e.message?.let { debug(it) }
            e.printStackTrace()
            throw e
        }

        return result.also {
            if (methodName != "invoke") debug("Function $methodName() finished with return => $it")
        }
    }
}
