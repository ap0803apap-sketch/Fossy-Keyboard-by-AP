package org.fossify.keyboard.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import android.os.Bundle
import org.fossify.commons.dialogs.FilePickerDialog
import org.fossify.commons.dialogs.RadioGroupDialog
import org.fossify.commons.extensions.beVisibleIf
import org.fossify.commons.extensions.handlePermission
import org.fossify.commons.extensions.getProperPrimaryColor
import org.fossify.commons.extensions.toast
import org.fossify.commons.extensions.updateTextColors
import org.fossify.commons.extensions.viewBinding
import org.fossify.commons.helpers.NavigationIcon
import org.fossify.commons.helpers.PERMISSION_READ_STORAGE
import org.fossify.commons.helpers.isQPlus
import org.fossify.commons.helpers.isTiramisuPlus
import org.fossify.commons.models.RadioItem
import org.fossify.keyboard.R
import org.fossify.keyboard.databinding.ActivitySettingsBinding
import org.fossify.keyboard.dialogs.ManageKeyboardLanguagesDialog
import org.fossify.keyboard.extensions.config
import org.fossify.keyboard.extensions.getCurrentVoiceInputMethod
import org.fossify.keyboard.extensions.getKeyboardLanguageText
import org.fossify.keyboard.extensions.getKeyboardLanguagesRadioItems
import org.fossify.keyboard.extensions.getVoiceInputMethods
import org.fossify.keyboard.extensions.getVoiceInputRadioItems
import org.fossify.keyboard.helpers.KEYBOARD_HEIGHT_100_PERCENT
import org.fossify.keyboard.helpers.LearnedDataManager
import org.fossify.keyboard.helpers.KEYBOARD_HEIGHT_120_PERCENT
import org.fossify.keyboard.helpers.KEYBOARD_HEIGHT_140_PERCENT
import org.fossify.keyboard.helpers.KEYBOARD_HEIGHT_160_PERCENT
import org.fossify.keyboard.helpers.KEYBOARD_HEIGHT_70_PERCENT
import org.fossify.keyboard.helpers.KEYBOARD_HEIGHT_80_PERCENT
import org.fossify.keyboard.helpers.KEYBOARD_HEIGHT_90_PERCENT
import org.fossify.keyboard.helpers.SOUND_ALWAYS
import org.fossify.keyboard.helpers.SOUND_NONE
import org.fossify.keyboard.helpers.SOUND_SYSTEM
import java.util.Locale
import kotlin.system.exitProcess

class SettingsActivity : SimpleActivity() {
    companion object {
        private const val PICK_IMPORT_LEARNED_DATA_INTENT = 41
    }

    private val binding by viewBinding(ActivitySettingsBinding::inflate)
    private val learnedDataManager by lazy { LearnedDataManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            setupEdgeToEdge(padBottomSystem = listOf(settingsNestedScrollview))
            setupMaterialScrollListener(binding.settingsNestedScrollview, binding.settingsAppbar)
        }
    }

    override fun onResume() {
        super.onResume()
        setupTopAppBar(binding.settingsAppbar, NavigationIcon.Arrow)

        setupCustomizeColors()
        setupUseEnglish()
        setupLanguage()
        setupManageClipboardItems()
        setupVibrateOnKeypress()
        setupSoundOnKeypress()
        setupShowPopupOnKeypress()
        setupShowKeyBorders()
        setupShowKeyPressAnimation()
        setupManageKeyboardLanguages()
        setupKeyboardLanguage()
        setupKeyboardHeightMultiplier()
        setupKeySpacing()
        setupShowEmojiKey()
        setupShowLanguageSwitchKey()
        setupShowClipboardContent()
        setupSentencesCapitalization()
        setupShowNumbersRow()
        setupEnableLearning()
        setupEnableTextPrediction()
        setupVoiceInputMethod()
        setupExportLearnedData()
        setupImportLearnedData()

        binding.apply {
            updateTextColors(settingsNestedScrollview)

            arrayOf(
                settingsColorCustomizationSectionLabel,
                settingsGeneralSettingsLabel,
                settingsLayoutAppearanceLabel,
                settingsKeypressLabel,
                settingsTypingInputLabel,
                settingsClipboardSettingsLabel
            ).forEach {
                it.setTextColor(getProperPrimaryColor())
            }
        }
    }

    private fun setupCustomizeColors() {
        binding.apply {
            settingsColorCustomizationHolder.setOnClickListener {
                startCustomizationActivity()
            }
        }
    }

    private fun setupUseEnglish() {
        binding.apply {
            settingsUseEnglishHolder.beVisibleIf((config.wasUseEnglishToggled || Locale.getDefault().language != "en") && !isTiramisuPlus())
            settingsUseEnglish.isChecked = config.useEnglish
            settingsUseEnglishHolder.setOnClickListener {
                settingsUseEnglish.toggle()
                config.useEnglish = settingsUseEnglish.isChecked
                exitProcess(0)
            }
        }
    }

    private fun setupLanguage() {
        binding.apply {
            settingsLanguage.text = Locale.getDefault().displayLanguage
            settingsLanguageHolder.beVisibleIf(isTiramisuPlus())
            settingsLanguageHolder.setOnClickListener {
                launchChangeAppLanguageIntent()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == PICK_IMPORT_LEARNED_DATA_INTENT && resultCode == Activity.RESULT_OK && resultData?.data != null) {
            contentResolver.openInputStream(resultData.data!!)?.use { inputStream ->
                try {
                    learnedDataManager.replaceFromImport(inputStream)
                    toast(R.string.learned_keyboard_data_imported)
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            } ?: toast(R.string.unknown_error_occurred)
        }
    }

    private fun setupManageClipboardItems() {
        binding.settingsManageClipboardItemsHolder.setOnClickListener {
            Intent(this, ManageClipboardItemsActivity::class.java).apply {
                startActivity(this)
            }
        }
    }

    private fun setupVibrateOnKeypress() {
        binding.apply {
            settingsVibrateOnKeypress.isChecked = config.vibrateOnKeypress
            settingsVibrateOnKeypressHolder.setOnClickListener {
                settingsVibrateOnKeypress.toggle()
                config.vibrateOnKeypress = settingsVibrateOnKeypress.isChecked
            }
        }
    }

    private fun setupSoundOnKeypress() {
        binding.apply {
            settingsSoundOnKeypress.text = getSoundOnKeypressText(config.soundOnKeypress)
            settingsSoundOnKeypressHolder.setOnClickListener {
                val items = arrayListOf(
                    RadioItem(SOUND_NONE, getString(R.string.sound_none)),
                    RadioItem(SOUND_SYSTEM, getString(R.string.sound_system)),
                    RadioItem(SOUND_ALWAYS, getString(R.string.sound_always))
                )
                RadioGroupDialog(
                    activity = this@SettingsActivity,
                    items = items,
                    checkedItemId = config.soundOnKeypress
                ) {
                    config.soundOnKeypress = it as Int
                    settingsSoundOnKeypress.text = getSoundOnKeypressText(config.soundOnKeypress)
                }
            }
        }
    }

    private fun getSoundOnKeypressText(mode: Int): String = getString(
        when (mode) {
            SOUND_SYSTEM -> R.string.sound_system
            SOUND_ALWAYS -> R.string.sound_always
            else -> R.string.sound_none
        }
    )

    private fun setupShowPopupOnKeypress() {
        binding.apply {
            settingsShowPopupOnKeypress.isChecked = config.showPopupOnKeypress
            settingsShowPopupOnKeypressHolder.setOnClickListener {
                settingsShowPopupOnKeypress.toggle()
                config.showPopupOnKeypress = settingsShowPopupOnKeypress.isChecked
            }
        }
    }

    private fun setupShowKeyBorders() {
        binding.apply {
            settingsShowKeyBorders.isChecked = config.showKeyBorders
            settingsShowKeyBordersHolder.setOnClickListener {
                settingsShowKeyBorders.toggle()
                config.showKeyBorders = settingsShowKeyBorders.isChecked
            }
        }
    }

    private fun setupShowKeyPressAnimation() {
        binding.apply {
            settingsShowKeyPressAnimation.isChecked = config.showKeyPressAnimation
            settingsShowKeyPressAnimationHolder.setOnClickListener {
                settingsShowKeyPressAnimation.toggle()
                config.showKeyPressAnimation = settingsShowKeyPressAnimation.isChecked
            }
        }
    }

    private fun setupManageKeyboardLanguages() {
        binding.apply {
            settingsManageKeyboardLanguagesHolder.setOnClickListener {
                ManageKeyboardLanguagesDialog(this@SettingsActivity) {
                    settingsKeyboardLanguage.text = getKeyboardLanguageText(config.keyboardLanguage)
                }
            }
        }
    }

    private fun setupKeyboardLanguage() {
        binding.apply {
            settingsKeyboardLanguage.text = getKeyboardLanguageText(config.keyboardLanguage)
            settingsKeyboardLanguageHolder.setOnClickListener {
                val items = getKeyboardLanguagesRadioItems()
                RadioGroupDialog(this@SettingsActivity, items, config.keyboardLanguage) {
                    config.keyboardLanguage = it as Int
                    settingsKeyboardLanguage.text = getKeyboardLanguageText(config.keyboardLanguage)
                }
            }
        }
    }

    private fun setupKeyboardHeightMultiplier() {
        binding.apply {
            settingsKeyboardHeightMultiplier.text =
                getKeyboardHeightPercentageText(config.keyboardHeightPercentage)
            settingsKeyboardHeightMultiplierHolder.setOnClickListener {
                val items = arrayListOf(
                    RadioItem(
                        id = KEYBOARD_HEIGHT_70_PERCENT,
                        title = getKeyboardHeightPercentageText(KEYBOARD_HEIGHT_70_PERCENT)
                    ),
                    RadioItem(
                        id = KEYBOARD_HEIGHT_80_PERCENT,
                        title = getKeyboardHeightPercentageText(KEYBOARD_HEIGHT_80_PERCENT)
                    ),
                    RadioItem(
                        id = KEYBOARD_HEIGHT_90_PERCENT,
                        title = getKeyboardHeightPercentageText(KEYBOARD_HEIGHT_90_PERCENT)
                    ),
                    RadioItem(
                        id = KEYBOARD_HEIGHT_100_PERCENT,
                        title = getKeyboardHeightPercentageText(KEYBOARD_HEIGHT_100_PERCENT)
                    ),
                    RadioItem(
                        id = KEYBOARD_HEIGHT_120_PERCENT,
                        title = getKeyboardHeightPercentageText(KEYBOARD_HEIGHT_120_PERCENT)
                    ),
                    RadioItem(
                        id = KEYBOARD_HEIGHT_140_PERCENT,
                        title = getKeyboardHeightPercentageText(KEYBOARD_HEIGHT_140_PERCENT)
                    ),
                    RadioItem(
                        id = KEYBOARD_HEIGHT_160_PERCENT,
                        title = getKeyboardHeightPercentageText(KEYBOARD_HEIGHT_160_PERCENT)
                    ),
                )

                RadioGroupDialog(this@SettingsActivity, items, config.keyboardHeightPercentage) {
                    config.keyboardHeightPercentage = it as Int
                    settingsKeyboardHeightMultiplier.text =
                        getKeyboardHeightPercentageText(config.keyboardHeightPercentage)
                }
            }
        }
    }

    private fun setupKeySpacing() {
        binding.settingsKeySpacingSlider.apply {
            value = config.keySpacing.toFloat()
            addOnChangeListener { _, value, _ ->
                config.keySpacing = value.toInt()
            }
        }
    }

    private fun getKeyboardHeightPercentageText(keyboardHeightPercentage: Int): String =
        "$keyboardHeightPercentage%"

    private fun setupShowClipboardContent() {
        binding.apply {
            settingsShowClipboardContent.isChecked = config.showClipboardContent
            settingsShowClipboardContentHolder.setOnClickListener {
                settingsShowClipboardContent.toggle()
                config.showClipboardContent = settingsShowClipboardContent.isChecked
            }
        }
    }

    private fun setupSentencesCapitalization() {
        binding.apply {
            settingsStartSentencesCapitalized.isChecked = config.enableSentencesCapitalization
            settingsStartSentencesCapitalizedHolder.setOnClickListener {
                settingsStartSentencesCapitalized.toggle()
                config.enableSentencesCapitalization = settingsStartSentencesCapitalized.isChecked
            }
        }
    }

    private fun setupExportLearnedData() {
        binding.settingsExportLearnedDataHolder.setOnClickListener {
            try {
                if (learnedDataManager.getLearnedWords().isEmpty()) {
                    toast(R.string.no_learned_keyboard_data)
                } else {
                    learnedDataManager.exportToDownloads()
                    toast(R.string.learned_keyboard_data_exported)
                }
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }

    private fun setupImportLearnedData() {
        binding.settingsImportLearnedDataHolder.setOnClickListener {
            importLearnedData()
        }
    }

    private fun importLearnedData() {
        if (isQPlus()) {
            Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                try {
                    startActivityForResult(this, PICK_IMPORT_LEARNED_DATA_INTENT)
                } catch (e: ActivityNotFoundException) {
                    toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }
        } else {
            handlePermission(PERMISSION_READ_STORAGE) { granted ->
                if (granted) {
                    FilePickerDialog(this) { path ->
                        try {
                            learnedDataManager.replaceFromImport(java.io.File(path).inputStream())
                            toast(R.string.learned_keyboard_data_imported)
                        } catch (e: Exception) {
                            showErrorToast(e)
                        }
                    }
                }
            }
        }
    }

    private fun setupShowEmojiKey() {
        binding.apply {
            settingsShowEmojiKeyHolder.setOnClickListener {
                settingsShowEmojiKey.toggle()
                config.showEmojiKey = settingsShowEmojiKey.isChecked
                if (settingsShowEmojiKey.isChecked) {
                    config.showLanguageSwitchKey = false
                    settingsShowLanguageSwitchKey.isChecked = false
                }
            }
            settingsShowEmojiKey.isChecked = config.showEmojiKey
        }
    }

    private fun setupShowLanguageSwitchKey() {
        binding.apply {
            settingsShowLanguageSwitchKeyHolder.setOnClickListener {
                settingsShowLanguageSwitchKey.toggle()
                config.showLanguageSwitchKey = settingsShowLanguageSwitchKey.isChecked
                if (settingsShowLanguageSwitchKey.isChecked) {
                    config.showEmojiKey = false
                    settingsShowEmojiKey.isChecked = false
                }
            }
            settingsShowLanguageSwitchKey.isChecked = config.showLanguageSwitchKey
        }
    }

    private fun setupShowNumbersRow() {
        binding.apply {
            settingsShowNumbersRow.isChecked = config.showNumbersRow
            settingsShowNumbersRowHolder.setOnClickListener {
                settingsShowNumbersRow.toggle()
                config.showNumbersRow = settingsShowNumbersRow.isChecked
            }
        }
    }


    private fun setupEnableLearning() {
        binding.apply {
            settingsEnableLearning.isChecked = config.enableLearning
            settingsEnableLearningHolder.setOnClickListener {
                settingsEnableLearning.toggle()
                config.enableLearning = settingsEnableLearning.isChecked
            }
        }
    }

    private fun setupEnableTextPrediction() {
        binding.apply {
            settingsEnableTextPrediction.isChecked = config.enableTextPrediction
            settingsEnableTextPredictionHolder.setOnClickListener {
                settingsEnableTextPrediction.toggle()
                config.enableTextPrediction = settingsEnableTextPrediction.isChecked
            }
        }
    }

    private fun setupVoiceInputMethod() {
        binding.apply {
            settingsVoiceInputMethodValue.text =
                getCurrentVoiceInputMethod()?.first?.loadLabel(packageManager)
                    ?: getString(R.string.none)
            settingsVoiceInputMethodHolder.setOnClickListener {
                val inputMethods = getVoiceInputMethods()
                if (inputMethods.isEmpty()) {
                    toast(R.string.no_app_found)
                    return@setOnClickListener
                }

                RadioGroupDialog(
                    activity = this@SettingsActivity,
                    items = getVoiceInputRadioItems(),
                    checkedItemId = inputMethods.indexOf(getCurrentVoiceInputMethod(inputMethods))
                ) {
                    config.voiceInputMethod = inputMethods.getOrNull(it as Int)?.first?.id.orEmpty()
                    settingsVoiceInputMethodValue.text =
                        getCurrentVoiceInputMethod(inputMethods)?.first?.loadLabel(packageManager)
                            ?: getString(R.string.none)
                }
            }
        }
    }
}
