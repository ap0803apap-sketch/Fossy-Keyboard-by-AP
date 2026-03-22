package org.fossify.keyboard.helpers

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.fossify.keyboard.R
import org.fossify.keyboard.extensions.config
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.LinkedHashMap
import java.util.Locale

data class LearnedDataExport(
    val version: Int = 1,
    val words: Map<String, Int> = emptyMap()
)

class LearnedDataManager(private val context: Context) {
    private val gson = Gson()

    fun getLearnedWords(): MutableMap<String, Int> {
        val json = context.config.learnedKeyboardData
        if (json.isBlank()) return LinkedHashMap()

        return try {
            val exportType = object : TypeToken<LearnedDataExport>() {}.type
            val export = gson.fromJson<LearnedDataExport>(json, exportType)
            LinkedHashMap(export?.words.orEmpty())
        } catch (_: Exception) {
            try {
                val legacyType = object : TypeToken<LinkedHashMap<String, Int>>() {}.type
                gson.fromJson<LinkedHashMap<String, Int>>(json, legacyType) ?: LinkedHashMap()
            } catch (_: Exception) {
                LinkedHashMap()
            }
        }
    }

    fun recordWord(word: String) {
        val normalized = normalizeWord(word) ?: return
        val words = getLearnedWords()
        words[normalized] = (words[normalized] ?: 0) + 1
        saveLearnedWords(words)
    }

    fun getPredictions(prefix: String, limit: Int = 3): List<String> {
        val normalizedPrefix = normalizeWord(prefix) ?: return emptyList()
        return getLearnedWords()
            .filterKeys { it.startsWith(normalizedPrefix) && it != normalizedPrefix }
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(limit)
            .map { it.key }
    }

    fun replaceFromImport(inputStream: InputStream) {
        val importedWords = parseImport(inputStream)
        saveLearnedWords(importedWords)
    }

    fun exportTo(outputStream: OutputStream) {
        outputStream.bufferedWriter().use { writer ->
            writer.write(gson.toJson(LearnedDataExport(words = getLearnedWords())))
        }
    }

    fun exportToDownloads(): String {
        val fileName = buildExportFileName()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + context.getString(R.string.app_launcher_name))
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            val uri = resolver.insert(collection, values) ?: error("Unable to create export file")
            resolver.openOutputStream(uri)?.use { exportTo(it) } ?: error("Unable to open export file")
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            uri.toString()
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val appDir = File(downloadsDir, context.getString(R.string.app_launcher_name)).apply { mkdirs() }
            val file = File(appDir, fileName)
            file.outputStream().use { exportTo(it) }
            file.absolutePath
        }
    }

    private fun parseImport(inputStream: InputStream): MutableMap<String, Int> {
        val json = inputStream.bufferedReader().use { it.readText() }
        val exportType = object : TypeToken<LearnedDataExport>() {}.type
        val export = gson.fromJson<LearnedDataExport>(json, exportType)
        return LinkedHashMap(export?.words.orEmpty().mapNotNull { (key, value) ->
            normalizeWord(key)?.let { it to value.coerceAtLeast(0) }
        }.toMap())
    }

    private fun saveLearnedWords(words: Map<String, Int>) {
        context.config.learnedKeyboardData = gson.toJson(LearnedDataExport(words = words.filterValues { it > 0 }.toSortedMap()))
    }

    private fun buildExportFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "${timestamp}_${context.getString(R.string.app_launcher_name)} Keyboard learned data.kbap"
    }

    private fun normalizeWord(word: String?): String? {
        val normalized = word
            ?.trim()
            ?.lowercase(Locale.getDefault())
            ?.replace(Regex("^[^\\p{L}\\p{N}]+|[^\\p{L}\\p{N}]+$"), "")
        return normalized?.takeIf { it.isNotBlank() }
    }
}
