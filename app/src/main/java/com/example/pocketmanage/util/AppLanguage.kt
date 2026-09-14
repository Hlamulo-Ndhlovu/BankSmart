package com.example.pocketmanage.util

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.pocketmanage.R

/**
 * Supported in-app languages. Backed by AppCompat per-app locales, so the
 * selection persists across launches (see AppLocalesMetadataHolderService in the
 * manifest) and works on every supported API level.
 */
enum class AppLanguage(val tag: String, @StringRes val labelRes: Int) {
    ENGLISH("en", R.string.language_english),
    ZULU("zu", R.string.language_zulu),
    SEPEDI("nso", R.string.language_sepedi),
    AFRIKAANS("af", R.string.language_afrikaans),
    TSONGA("ts", R.string.language_tsonga);

    companion object {
        fun current(): AppLanguage {
            val locales = AppCompatDelegate.getApplicationLocales()
            val tag = if (locales.isEmpty) null else locales[0]?.language
            return entries.firstOrNull { it.tag == tag } ?: ENGLISH
        }

        fun apply(language: AppLanguage) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
        }
    }
}
