package org.fossify.keyboard.helpers

import android.content.Context
import org.fossify.commons.helpers.BaseConfig
import org.fossify.keyboard.extensions.isDeviceLocked
import org.fossify.keyboard.extensions.safeStorageContext
import java.util.Locale

class Config(context: Context) : BaseConfig(context) {
    companion object {
        fun newInstance(context: Context) = Config(context.safeStorageContext)
    }

    var vibrateOnKeypress: Boolean
        get() = prefs.getBoolean(VIBRATE_ON_KEYPRESS, true)
        set(vibrateOnKeypress) = prefs.edit().putBoolean(VIBRATE_ON_KEYPRESS, vibrateOnKeypress).apply()

    var soundOnKeypress: Int
        get() = prefs.getInt(SOUND_ON_KEYPRESS, SOUND_SYSTEM)
        set(soundOnKeypress) = prefs.edit().putInt(SOUND_ON_KEYPRESS, soundOnKeypress).apply()

    var showPopupOnKeypress: Boolean
        get() = prefs.getBoolean(SHOW_POPUP_ON_KEYPRESS, true)
        set(showPopupOnKeypress) = prefs.edit().putBoolean(SHOW_POPUP_ON_KEYPRESS, showPopupOnKeypress).apply()

    var enableSentencesCapitalization: Boolean
        get() = prefs.getBoolean(SENTENCES_CAPITALIZATION, true)
        set(enableCapitalization) = prefs.edit().putBoolean(SENTENCES_CAPITALIZATION, enableCapitalization).apply()

    var showEmojiKey: Boolean
        get() = prefs.getBoolean(SHOW_EMOJI_KEY, true)
        set(showEmojiKey) = prefs.edit().putBoolean(SHOW_EMOJI_KEY, showEmojiKey).apply()

    var showLanguageSwitchKey: Boolean
        get() = prefs.getBoolean(SHOW_LANGUAGE_SWITCH_KEY, false)
        set(showLanguageSwitchKey) = prefs.edit().putBoolean(SHOW_LANGUAGE_SWITCH_KEY, showLanguageSwitchKey).apply()

    var showKeyBorders: Boolean
        get() = prefs.getBoolean(SHOW_KEY_BORDERS, true)
        set(showKeyBorders) = prefs.edit().putBoolean(SHOW_KEY_BORDERS, showKeyBorders).apply()

    var showKeyPressAnimation: Boolean
        get() = prefs.getBoolean(SHOW_KEY_PRESS_ANIMATION, true)
        set(showKeyPressAnimation) = prefs.edit().putBoolean(SHOW_KEY_PRESS_ANIMATION, showKeyPressAnimation).apply()

    var useAmoledMode: Boolean
        get() = prefs.getBoolean(AMOLED_MODE, false)
        set(useAmoledMode) = prefs.edit().putBoolean(AMOLED_MODE, useAmoledMode).apply()

    var keyboardPaletteStyle: Int
        get() = prefs.getInt(KEYBOARD_PALETTE_STYLE, KEYBOARD_PALETTE_DEFAULT)
        set(keyboardPaletteStyle) = prefs.edit().putInt(KEYBOARD_PALETTE_STYLE, keyboardPaletteStyle).apply()

    var customKeyColor: Int
        get() = prefs.getInt(CUSTOM_KEY_COLOR, 0)
        set(customKeyColor) = prefs.edit().putInt(CUSTOM_KEY_COLOR, customKeyColor).apply()

    var customKeyboardBackgroundColor: Int
        get() = prefs.getInt(CUSTOM_KEYBOARD_BACKGROUND_COLOR, 0)
        set(value) = prefs.edit().putInt(CUSTOM_KEYBOARD_BACKGROUND_COLOR, value).apply()

    var customKeyboardTextColor: Int
        get() = prefs.getInt(CUSTOM_KEYBOARD_TEXT_COLOR, 0)
        set(value) = prefs.edit().putInt(CUSTOM_KEYBOARD_TEXT_COLOR, value).apply()

    var customKeyboardAccentColor: Int
        get() = prefs.getInt(CUSTOM_KEYBOARD_ACCENT_COLOR, 0)
        set(value) = prefs.edit().putInt(CUSTOM_KEYBOARD_ACCENT_COLOR, value).apply()

    var enableLearning: Boolean
        get() = prefs.getBoolean(ENABLE_LEARNING, true)
        set(enableLearning) = prefs.edit().putBoolean(ENABLE_LEARNING, enableLearning).apply()

    var enableTextPrediction: Boolean
        get() = prefs.getBoolean(ENABLE_TEXT_PREDICTION, true)
        set(enableTextPrediction) = prefs.edit().putBoolean(ENABLE_TEXT_PREDICTION, enableTextPrediction).apply()

    var learnedKeyboardData: String
        get() = prefs.getString(LEARNED_KEYBOARD_DATA, "")!!
        set(learnedKeyboardData) = prefs.edit().putString(LEARNED_KEYBOARD_DATA, learnedKeyboardData).apply()

    var lastExportedClipsFolder: String
        get() = prefs.getString(LAST_EXPORTED_CLIPS_FOLDER, "")!!
        set(lastExportedClipsFolder) = prefs.edit().putString(LAST_EXPORTED_CLIPS_FOLDER, lastExportedClipsFolder).apply()

    var keyboardLanguage: Int
        get() = prefs.getInt(KEYBOARD_LANGUAGE, getDefaultLanguage())
        set(keyboardLanguage) = prefs.edit().putInt(KEYBOARD_LANGUAGE, keyboardLanguage).apply()

    var keyboardHeightPercentage: Int
        get() = prefs.getInt(HEIGHT_PERCENTAGE, 100)
        set(keyboardHeightMultiplier) = prefs.edit().putInt(HEIGHT_PERCENTAGE, keyboardHeightMultiplier).apply()

    var keySpacing: Int
        get() = prefs.getInt(KEY_SPACING, 0)
        set(keySpacing) = prefs.edit().putInt(KEY_SPACING, keySpacing).apply()

    var showClipboardContent: Boolean
        get() = prefs.getBoolean(SHOW_CLIPBOARD_CONTENT, true)
        set(showClipboardContent) = prefs.edit().putBoolean(SHOW_CLIPBOARD_CONTENT, showClipboardContent).apply()

    var showNumbersRow: Boolean
        get() = if (context.isDeviceLocked) {
            true
        } else {
            prefs.getBoolean(SHOW_NUMBERS_ROW, false)
        }
        set(showNumbersRow) = prefs.edit().putBoolean(SHOW_NUMBERS_ROW, showNumbersRow).apply()

    var voiceInputMethod: String
        get() = prefs.getString(VOICE_INPUT_METHOD, "")!!
        set(voiceInputMethod) = prefs.edit().putString(VOICE_INPUT_METHOD, voiceInputMethod).apply()

    var selectedLanguages: MutableSet<Int>
        get() {
            val defaultLanguage = getDefaultLanguage().toString()
            val stringSet = prefs.getStringSet(SELECTED_LANGUAGES, hashSetOf(defaultLanguage))!!
            return stringSet.map { it.toInt() }.toMutableSet()
        }
        set(selectedLanguages) {
            val stringSet = selectedLanguages.map { it.toString() }.toSet()
            prefs.edit().putStringSet(SELECTED_LANGUAGES, stringSet).apply()
        }

    fun getDefaultLanguage(): Int {
        val conf = context.resources.configuration
        return if (conf.locale.toString().lowercase(Locale.getDefault()).startsWith("ru_")) {
            LANGUAGE_RUSSIAN
        } else {
            LANGUAGE_ENGLISH_QWERTY
        }
    }

    var recentlyUsedEmojis: List<String>
        get() = prefs.getString(RECENTLY_USED_EMOJIS, "\uD83D\uDC30")!!.split("|").filter { it.isNotEmpty() }
        set(recentlyUsedEmojis) = prefs.edit().putString(
            RECENTLY_USED_EMOJIS, recentlyUsedEmojis.joinToString("|")
        ).apply()

    fun addRecentEmoji(emoji: String) {
        val recentEmojis = recentlyUsedEmojis.toMutableList()
        recentEmojis.remove(emoji)
        recentEmojis.add(0, emoji)
        recentlyUsedEmojis = recentEmojis.take(RECENT_EMOJIS_LIMIT)
    }
}
