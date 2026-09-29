package com.colorroot.app

import java.io.File
import java.util.Locale

class RootColorEngine {
    private val backup = File("/data/local/tmp/colorroot_backup.txt")

    private val candidates = listOf(
        "/sys/class/backlight/panel0-backlight",
        "/sys/class/backlight/backlight",
        "/sys/class/graphics/fb0"
    )

    fun rootStatus(): String {
        val r = sh("id")
        return if (r.contains("uid=0")) "✓ Root access detected"
        else "✗ Root not available"
    }

    fun applyPreset(p: Preset): String {
        if (!hasRoot()) return "Root permission was not granted."

        // Backup any writable vendor controls we can identify before first change.
        backupIfNeeded()

        // These settings are intentionally device-discovered. Android does not
        // expose one universal saturation sysfs path across OEMs.
        val paths = discoverColorNodes()
        if (paths.isEmpty()) {
            return "Root works, but no supported color-control node was found on this device. No system value was changed."
        }

        var changed = 0
        for (path in paths) {
            val value = when {
                path.endsWith("saturation") -> p.saturation.toString()
                path.endsWith("contrast") -> p.contrast.toString()
                path.endsWith("color_temperature") -> (6500 + p.warmth * 100).toString()
                else -> continue
            }
            if (write(path, value)) changed++
        }

        return if (changed > 0)
            "Applied ${p.name.lowercase(Locale.US)} to $changed supported control(s)."
        else
            "Controls were found but rejected the requested values. Nothing unsafe was forced."
    }

    fun restore(): String {
        if (!hasRoot()) return "Root permission was not granted."
        if (!backup.exists()) return "No ColorRoot backup exists. Nothing was changed."
        val lines = backup.readLines()
        var restored = 0
        for (line in lines) {
            val i = line.indexOf('=')
            if (i <= 0) continue
            val path = line.substring(0, i)
            val value = line.substring(i + 1)
            if (write(path, value)) restored++
        }
        return "Normal restored $restored saved control(s)."
    }

    private fun backupIfNeeded() {
        if (backup.exists()) return
        val nodes = discoverColorNodes()
        if (nodes.isEmpty()) return
        val content = nodes.mapNotNull { p ->
            val v = read(p) ?: return@mapNotNull null
            "$p=$v"
        }
        if (content.isNotEmpty()) {
            sh("mkdir -p /data/local/tmp && chmod 600 ${backup.path}")
            backup.writeText(content.joinToString("\n"))
        }
    }

    private fun discoverColorNodes(): List<String> {
        val result = mutableListOf<String>()
        val roots = listOf(
            "/sys/class",
            "/sys/devices"
        )
        val patterns = listOf("saturation", "contrast", "color_temperature")
        for (root in roots) {
            val out = sh("""find $root -type f \( -name saturation -o -name contrast -o -name color_temperature \) 2>/dev/null | head -n 80""")
            out.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.forEach { path ->
                if (patterns.any { path.endsWith(it) } && isWritable(path)) result += path
            }
        }
        return result.distinct()
    }

    private fun isWritable(path: String): Boolean =
        sh("test -w '$path'; echo $?").trim() == "0"

    private fun read(path: String): String? {
        val r = sh("cat '$path' 2>/dev/null")
        return r.trim().takeIf { it.isNotEmpty() }
    }

    private fun write(path: String, value: String): Boolean =
        sh("printf '%s' '$value' > '$path' 2>/dev/null; echo $?").trim() == "0"

    private fun hasRoot(): Boolean = sh("id").contains("uid=0")

    private fun sh(command: String): String {
        return try {
            val p = ProcessBuilder("su", "-c", command)
                .redirectErrorStream(true)
                .start()
            val out = p.inputStream.bufferedReader().readText()
            p.waitFor()
            out
        } catch (_: Exception) { "" }
    }
}
