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
    importOrders("orderFiles/importOrders"), //new orders to import
    historyOrders("orderFiles/historyOrders"); //archive directory

    companion object {
        /**
         * Will create the directory if it doesn't exist
         * @param directory
         * @return
         */
        private fun createDirectory(directory: String): String {
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
            var fileDir: File = File(getDirectory(directory))
            var files: Array<File>? = fileDir.listFiles()

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
            // try deleting original up to 4 times with longer delay each time
            var deleted = false
            for (attempt in 1..4) {
                try {
                    deleted = Files.deleteIfExists(file.toPath())
                    if (deleted || !file.exists()) break
                } catch (ioe: IOException) {
                    // ignore and retry
                }


                try {
                    Thread.sleep((100 * attempt).toLong())
                } catch (ie: InterruptedException) {
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
                val dest: File = File(targetDir, f.getName())
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