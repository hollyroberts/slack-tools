import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import org.apache.logging.log4j.kotlin.Logging
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.measureTimeMillis

object InMemoryDirectory : Logging {
    fun loadDirectory(path: Path): FileSystem {
        val fileSystem = Jimfs.newFileSystem(Configuration.windows())

        logger.info { "Loading directory '${path}' recursively to memory" }

        val millis = measureTimeMillis {
            Files.walk(path)
                    .forEach { realPath ->
                        val subpath = path.relativize(realPath)
                        val jimfsPath = fileSystem.getPath(subpath.toString())

                        if (subpath.toString().isEmpty()) {
                            return@forEach
                        }

                        if (Files.isDirectory(realPath)) {
                            logger.debug { "Creating directory: $subpath" }
                            Files.createDirectory(jimfsPath)
                        }
                        if (Files.isRegularFile(realPath)) {
                            logger.debug { "Loading and creating file: $subpath" }
                            Files.write(jimfsPath, Files.readAllBytes(realPath))
                        }
                    }
        }

        logger.info { String.format("Loaded directory to memory in %,dms", millis) }

        return fileSystem
    }
}