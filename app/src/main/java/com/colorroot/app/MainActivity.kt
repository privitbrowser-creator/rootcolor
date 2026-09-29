package com.colorroot.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import kotlin.math.roundToInt

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var engine: RootColorEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        engine = RootColorEngine()
        buildUi()
    }

    private fun buildUi() {
        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 42, 32, 36)
            setBackgroundColor(Color.rgb(9,10,15))
        }

        val title = TextView(this).apply {
            text = "ColorRoot"
            textSize = 30f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        box.addView(title, lp())

        val sub = TextView(this).apply {
            text = "Root Display Color Controller"
            textSize = 14f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }
        box.addView(sub, lp())

        status = TextView(this).apply {
            text = "Checking root..."
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 28, 0, 20)
        }
        box.addView(status, lp())

        addButton(box, "Normal — Restore", Color.DKGRAY) {
            status.text = engine.restore()
        }
        addButton(box, "Natural", 0xFF455A64.toInt()) {
            status.text = engine.applyPreset(Preset.NATURAL)
        }
        addButton(box, "Vivid", 0xFF5E35B1.toInt()) {
            status.text = engine.applyPreset(Preset.VIVID)
        }
        addButton(box, "Ultra Vivid", 0xFF8E24AA.toInt()) {
            status.text = engine.applyPreset(Preset.ULTRA)
        }
        addButton(box, "Cinema", 0xFF1565C0.toInt()) {
            status.text = engine.applyPreset(Preset.CINEMA)
        }
        addButton(box, "Warm", 0xFFFF8F00.toInt()) {
            status.text = engine.applyPreset(Preset.WARM)
        }
        addButton(box, "Cool", 0xFF00838F.toInt()) {
            status.text = engine.applyPreset(Preset.COOL)
        }
        addButton(box, "Extra Contrast", 0xFF2E7D32.toInt()) {
            status.text = engine.applyPreset(Preset.CONTRAST)
        }

        val info = TextView(this).apply {
            text = "\nThe app first backs up supported display-control nodes, then changes only controls it can safely detect. Unsupported controls are skipped. Normal restores the saved values."
            textSize = 13f
            setTextColor(Color.GRAY)
        }
        box.addView(info, lp())

        scroll.addView(box)
        setContentView(scroll)
        status.text = engine.rootStatus()
    }

    private fun addButton(box: LinearLayout, text: String, color: Int, action: () -> Unit) {
        val b = Button(this).apply {
            this.text = text
            setTextColor(Color.WHITE)
            setBackgroundColor(color)
            setOnClickListener { action() }
        }
        box.addView(b, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 60
        ).apply { setMargins(0, 7, 0, 7) })
    }

    private fun lp() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}

enum class Preset(
    val saturation: Int,
    val contrast: Int,
    val warmth: Int
) {
    NATURAL(100,100,0),
    VIVID(125,105,0),
    ULTRA(145,115,0),
    CINEMA(115,108,-4),
    WARM(115,105,8),
    COOL(115,105,-8),
    CONTRAST(110,125,0)
}
