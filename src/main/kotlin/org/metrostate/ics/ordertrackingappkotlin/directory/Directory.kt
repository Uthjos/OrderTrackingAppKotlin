package org.metrostate.ics.ordertrackingappkotlin.directory

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * The Directory Enum holds all data directories
 */
enum class Directory(
    /**
     * holds the path after src/main/
     */
    val path: String
) {
    savedOrders("orderFiles/savedOrders"), //current saved orders
    importOrders("orderFiles/importOrders"),
    historyOrders("orderFiles/historyOrders"); //new orders to import

    companion object {
        /**
         * Will create the directory if it doesn't exist
         * @param directory
         * @return
         */
        fun createDirectory(directory: String): String {
            val f = File(directory)
            if (!f.exists()) {
                f.mkdirs()
            }

            return directory
        }

        /**
         * will return the directory of the enum Directory
         * @param directory
         * @return
         */
        fun getDirectory(directory: Directory): String {
            val dirPath = Paths.get(System.getProperty("user.dir"), "src", "main")
                .resolve(directory.path)
            createDirectory(dirPath.toString())
            return dirPath.toString()
        }


        /**
         * will Delete all files in the directory in argument
         */
        fun deleteFilesInDirectory(directory: Directory) {
            val fileDir = File(getDirectory(directory))
            val files: Array<File>? = fileDir.listFiles()

            if (files != null) {
                for (f in files) {
                    if (!f.name.endsWith(".txt", ignoreCase = true)) {
                        deleteFile(f)
                    }
                }
            }
        }

        /**
         * will Delete specific File in argument
         */
        fun deleteFile(file: File) {
            // try deleting regularly first
            try {
                if (Files.deleteIfExists(file.toPath()) || !file.exists()) {
                    return
                }
            } catch (_: IOException) {

            }
            //didn't work, try several times with delay
            var deleted = false
            for (attempt in 1..4) {
                try {
                    deleted = Files.deleteIfExists(file.toPath())
                    if (deleted || !file.exists()) break
                } catch (_: IOException) {
                    // ignore and retry
                }


                try {
                    Thread.sleep((10 * attempt).toLong())
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    break
                }
            }

            if (!deleted) {
                System.err.println("Failed to delete: " + file.getAbsolutePath())
            }
        }

        /**
         * will Copy all Files in a Directory to another
         */
        fun backupFilesInDirectory(copyFromDir: Directory, destDir: Directory) {
            val sourceDir = File(getDirectory(copyFromDir))
            val targetDir = File(getDirectory(destDir))

            val files = sourceDir.listFiles() ?: return


            for (f in files) {
                val dest = File(targetDir, f.getName())
                try {
                    // copy (overwrite if exists)
                    Files.copy(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING)
                } catch (e: IOException) {
                    System.err.println("Failed to copy " + f.getAbsolutePath() + " to " + dest.getAbsolutePath() + ": " + e.message)
                    continue
                }
            }
        }

    }



}