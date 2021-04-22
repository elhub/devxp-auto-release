@file:Suppress("unused")

package no.elhub.tools.autorelease

import no.elhub.tools.autorelease.log.Logger
import java.io.File

fun Process.inputStreamAsText() = this.inputStream.bufferedReader().use { it.readText().trim() }

fun Process.inputStreamAsLines() = this.inputStream.bufferedReader().use { it.readLines().map { l -> l.trim() } }

fun Process.errorStreamAsText() = this.errorStream.bufferedReader().use { it.readText().trim() }

fun Process.errorStreamAsLines() = this.errorStream.bufferedReader().use { it.readLines().map { l -> l.trim() } }

/**
 * Class for running native processes.
 *
 * @property dir the working directory of the process
 */
class Proc(private val dir: File, private val log: Logger) {

    /**
     * Evaluates the result of [block] function invocation
     * and uses the returned string as an input for the command to run.
     *
     * @param block a function that returns a string containing the program and it's arguments
     */
    fun runCommand(block: () -> String) = runCommand(block.invoke())

    /**
     * Runs [command] as a native process and returns a new [Process] object for managing the subprocess.
     *
     * @param command a string containing the program and it's arguments
     */
    fun runCommand(command: String) = runCommand("/bin/bash", "-c", command)

    /**
     * Runs a native process and returns a new [Process] object for managing the subprocess.
     *
     * @param args a string array containing the program and its arguments
     */
    private fun runCommand(vararg args: String): Process = log.loggable(*args) {
        requireNotNull(
            ProcessBuilder()
                .command(*args)
                .directory(dir)
                .start()
                .also { log.info("Start command process with args [${args.joinToString(", ")}]") }
        ) { "Cannot start, process is null" }
    }
}
