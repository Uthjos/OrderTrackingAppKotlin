package org.metrostate.ics.ordertrackingappkotlin

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class DirectoryTest {
    @Test
    fun getDirectoryTest() {

    }

    /**
     * Override user.dir so that Directory.getDirectory()
     * writes inside the temporary folder instead of src/main.
     */
    @Test
    fun `getDirectory creates the folder if missing`(@TempDir tempDir: File) {
        //Override User.dir so actual files don't get affected
        val originalUserDir = System.getProperty("user.dir")
        System.setProperty("user.dir", tempDir.absolutePath)

        try {
            val dirPath = Directory.getDirectory(Directory.savedOrders)
            val dirFile = File(dirPath)

            assertTrue(dirFile.exists())
            assertTrue(dirFile.isDirectory)
        } finally {
            //sets System.getProperty("user.dir") back to normal
            System.setProperty("user.dir", originalUserDir)
        }
    }

    @Test
    fun `deleteFile removes file successfully` (@TempDir tempDir: File) {
        //Override User.dir so actual files don't get affected
        val originalUserDir = System.getProperty("user.dir")
        System.setProperty("user.dir", tempDir.absolutePath)

        try {
            val dirPath = Directory.getDirectory(Directory.savedOrders)
            val testFile = File(dirPath)

            //checks to see if file exists
            assertTrue(testFile.exists())

            //deletes the file
            Directory.deleteFile(testFile)

            //testFile should no longer exist
            assertFalse(testFile.exists())
        } finally {
            //sets System.getProperty("user.dir") back to normal
            System.setProperty("user.dir", originalUserDir)
        }

    }

    @Test
    fun `deleteFilesInDirectory deletes all files`(@TempDir tempDir: File) {
        //Override User.dir so actual files don't get affected
        val originalUserDir = System.getProperty("user.dir")
        System.setProperty("user.dir", tempDir.absolutePath)

        try {
            val dirPath = Directory.getDirectory(Directory.importOrders)
            val testFile1 = File(dirPath, "test1.txt").apply { writeText("test1") }
            val testFile2 = File(dirPath, "test2.txt").apply { writeText("test2") }

            assertTrue(testFile1.exists())
            assertTrue(testFile2.exists())

            Directory.deleteFilesInDirectory(Directory.importOrders)

            assertFalse(testFile1.exists())
            assertFalse(testFile2.exists())

        } finally {
            //sets System.getProperty("user.dir") back to normal
            System.setProperty("user.dir", originalUserDir)
        }
    }
}