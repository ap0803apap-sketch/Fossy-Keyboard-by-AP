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
import org.fossify.commons.extensions.isDynamicTheme
import org.fossify.commons.extensions.isSystemInDarkMode
import org.fossify.commons.extensions.lightenColor
import org.fossify.keyboard.R
import org.fossify.keyboard.helpers.KEYBOARD_PALETTE_EXPRESSIVE
import org.fossify.keyboard.helpers.KEYBOARD_PALETTE_KEY_COLOR_ONLY
import org.fossify.keyboard.helpers.KEYBOARD_PALETTE_TONAL_SPOT

fun Context.getKeyboardBackgroundColor(): Int {
    if (config.useAmoledMode) {
        return Color.BLACK
    }

    val color = if (isDynamicTheme()) {
        resources.getColor(R.color.you_keyboard_background_color, theme)
    } else {
        getProperBackgroundColor().darkenColor(2)
    }

    // use darker background color when key borders are enabled
    if (config.showKeyBorders) {
        val darkerColor = color.darkenColor(2)
        return if (darkerColor == Color.WHITE) {
            resources.getColor(R.color.md_grey_200, theme)
        } else {
            darkerColor
        }
    }

    return color
}

fun Context.getKeyboardKeyColor(): Int {
    val backgroundColor = getKeyboardBackgroundColor()
    val paletteColor = when (config.keyboardPaletteStyle) {
        KEYBOARD_PALETTE_TONAL_SPOT -> ColorUtils.blendARGB(getProperPrimaryColor(), backgroundColor, if (config.useAmoledMode) 0.42f else 0.58f)
        KEYBOARD_PALETTE_EXPRESSIVE -> ColorUtils.blendARGB(baseConfig.accentColor, backgroundColor, if (config.useAmoledMode) 0.35f else 0.52f)
        KEYBOARD_PALETTE_KEY_COLOR_ONLY -> if (config.customKeyColor != 0) config.customKeyColor else getProperPrimaryColor()
        else -> {
            val lighterColor = backgroundColor.lightenColor()
            if (!isDynamicTheme() && backgroundColor == Color.BLACK) {
                backgroundColor.getContrastColor().adjustAlpha(0.12f)
            } else {
                lighterColor
            }
        }
    }

    return if (config.useAmoledMode) {
        ColorUtils.blendARGB(paletteColor, Color.WHITE, 0.12f)
    } else {
        paletteColor
    }
}

fun Context.getStrokeColor(): Int {
    return if (config.useAmoledMode) {
        resources.getColor(R.color.md_grey_800, theme)
    } else if (isDynamicTheme()) {
        if (isSystemInDarkMode()) {
            resources.getColor(R.color.md_grey_800, theme)
        } else {
            resources.getColor(R.color.md_grey_400, theme)
        }
    } else {
        val lighterColor = safeStorageContext.getProperBackgroundColor().lightenColor()
        if (lighterColor == Color.WHITE || lighterColor == Color.BLACK) {
            resources.getColor(R.color.divider_grey, theme)
        } else {
            lighterColor
        }
    }
}
