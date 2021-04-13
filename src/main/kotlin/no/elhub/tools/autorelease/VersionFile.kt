package no.elhub.tools.autorelease

import java.nio.file.Files
import java.nio.file.Files.createTempFile
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.readLines
import kotlin.io.path.writeLines

object VersionFile {

    @OptIn(ExperimentalPathApi::class)
    fun setVersion(file: Path, versionPattern: Regex, newVersion: String) {
        val tempFile = createTempFile(Paths.get("."), null, null)
        val lines = file.readLines().map {  line ->
            when {
                versionPattern.matches(line) -> newVersion
                else -> line
            }
        }
        tempFile.writeLines(lines)
        Files.delete(file)
        Files.move(tempFile, file)
    }

}
