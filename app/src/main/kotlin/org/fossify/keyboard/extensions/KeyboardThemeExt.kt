package org.fossify.keyboard.extensions

import android.content.Context
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import org.fossify.commons.extensions.adjustAlpha
import org.fossify.commons.extensions.baseConfig
import org.fossify.commons.extensions.darkenColor
import org.fossify.commons.extensions.getContrastColor
import org.fossify.commons.extensions.getProperBackgroundColor
import org.fossify.commons.extensions.getProperPrimaryColor
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.commons.extensions.isDynamicTheme
import org.fossify.commons.extensions.isSystemInDarkMode
import org.fossify.commons.extensions.lightenColor
import org.fossify.keyboard.R
import org.fossify.keyboard.helpers.KEYBOARD_PALETTE_CUSTOM
import org.fossify.keyboard.helpers.KEYBOARD_PALETTE_EXPRESSIVE
import org.fossify.keyboard.helpers.KEYBOARD_PALETTE_TONAL_SPOT

private fun Context.getBaseKeyboardBackground(): Int {
    return if (config.useAmoledMode) {
        Color.BLACK
    } else if (isDynamicTheme()) {
        resources.getColor(R.color.you_keyboard_background_color, theme)
    } else {
        getProperBackgroundColor().darkenColor(2)
    }
}

fun Context.getKeyboardBackgroundColor(): Int {
    val color = when (config.keyboardPaletteStyle) {
        KEYBOARD_PALETTE_TONAL_SPOT -> ColorUtils.blendARGB(getBaseKeyboardBackground(), getProperPrimaryColor(), 0.08f)
        KEYBOARD_PALETTE_EXPRESSIVE -> ColorUtils.blendARGB(getBaseKeyboardBackground(), baseConfig.accentColor, 0.12f)
        KEYBOARD_PALETTE_CUSTOM -> if (config.customKeyboardBackgroundColor != 0) config.customKeyboardBackgroundColor else getBaseKeyboardBackground()
        else -> getBaseKeyboardBackground()
    }

    if (config.showKeyBorders && !config.useAmoledMode) {
        val darkerColor = color.darkenColor(2)
        return if (darkerColor == Color.WHITE) resources.getColor(R.color.md_grey_200, theme) else darkerColor
    }
    return color
}

fun Context.getKeyboardPrimaryColor(): Int {
    return when (config.keyboardPaletteStyle) {
        KEYBOARD_PALETTE_EXPRESSIVE -> ColorUtils.blendARGB(baseConfig.accentColor, getProperPrimaryColor(), 0.35f)
        KEYBOARD_PALETTE_CUSTOM -> if (config.customKeyboardAccentColor != 0) config.customKeyboardAccentColor else getProperPrimaryColor()
        else -> getProperPrimaryColor()
    }
}

fun Context.getKeyboardTextColor(): Int {
    return when (config.keyboardPaletteStyle) {
        KEYBOARD_PALETTE_CUSTOM -> if (config.customKeyboardTextColor != 0) config.customKeyboardTextColor else getProperTextColor()
        else -> getProperTextColor()
    }
}

fun Context.getKeyboardKeyColor(): Int {
    val backgroundColor = getKeyboardBackgroundColor()
    val paletteColor = when (config.keyboardPaletteStyle) {
        KEYBOARD_PALETTE_TONAL_SPOT -> ColorUtils.blendARGB(getKeyboardPrimaryColor(), backgroundColor, if (config.useAmoledMode) 0.35f else 0.55f)
        KEYBOARD_PALETTE_EXPRESSIVE -> ColorUtils.blendARGB(baseConfig.accentColor, backgroundColor, if (config.useAmoledMode) 0.28f else 0.48f)
        KEYBOARD_PALETTE_CUSTOM -> if (config.customKeyColor != 0) config.customKeyColor else getKeyboardPrimaryColor()
        else -> {
            val lighterColor = backgroundColor.lightenColor()
            if (!isDynamicTheme() && backgroundColor == Color.BLACK) backgroundColor.getContrastColor().adjustAlpha(0.14f) else lighterColor
        }
    }

    return if (config.useAmoledMode) ColorUtils.blendARGB(paletteColor, Color.WHITE, 0.12f) else paletteColor
}

fun Context.getStrokeColor(): Int {
    return if (config.useAmoledMode) {
        ColorUtils.blendARGB(Color.WHITE, Color.BLACK, 0.82f)
    } else if (config.keyboardPaletteStyle == KEYBOARD_PALETTE_CUSTOM && config.customKeyboardTextColor != 0) {
        ColorUtils.blendARGB(config.customKeyboardTextColor, getKeyboardBackgroundColor(), 0.75f)
    } else if (isDynamicTheme()) {
        if (isSystemInDarkMode()) resources.getColor(R.color.md_grey_800, theme) else resources.getColor(R.color.md_grey_400, theme)
    } else {
        val lighterColor = safeStorageContext.getProperBackgroundColor().lightenColor()
        if (lighterColor == Color.WHITE || lighterColor == Color.BLACK) resources.getColor(R.color.divider_grey, theme) else lighterColor
    }
}
