import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import org.apache.logging.log4j.kotlin.Logging
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.measureTimeMillis

object InMemoryDirectory : Logging {
  fun loadDirectory(path: Path): FileSystem {
    val fileSystem = Jimfs.newFileSystem(Configuration.unix())
    val baseJimfsPath = fileSystem.getPath("")

    logger.info { "Loading directory '${path}' recursively to memory" }

    val millis = measureTimeMillis {
      Files.walk(path)
          .forEach { realPath ->
            val subpath = path.relativize(realPath)

            // Manually resolve subpath using each individual component
            var jimfsPath = baseJimfsPath
            for (index in 0 until subpath.nameCount) {
              val part = subpath.getName(index).toString()
              if (index == 0 && part.isEmpty()) {
                continue
              }

              jimfsPath = jimfsPath.resolve(part)
            }

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