package org.metrostate.ics.ordertrackingappkotlin

import java.io.File
import java.nio.file.Paths

/**
 * The Directory Enum holds all data directories
 */
enum class Directory(path: String) {
    savedOrders("orderFiles/savedOrders"),
    testOrders("orderFiles/testOrders"),
    importOrders("orderFiles/importOrders");

    /**
     * holds the path after src/main/
     */
    val path: String?

    init {
        this.path = path
    }

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
    }
}
