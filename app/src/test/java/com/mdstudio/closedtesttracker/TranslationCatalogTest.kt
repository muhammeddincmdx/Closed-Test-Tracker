package com.mdstudio.closedtesttracker

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TranslationCatalogTest {
    private val addedLanguages = listOf(AppLanguage.DE, AppLanguage.JA, AppLanguage.PT, AppLanguage.ID)
    private val universalTerms = setOf("Play Store")

    @Test
    fun extendedUiCatalog_hasRealTranslationsForEveryAddedLanguage() {
        extendedUiEnglishKeys().forEach { english ->
            addedLanguages.forEach { language ->
                val translated = extendedUiCopy(language, english)
                assertNotNull("Missing $language translation for: $english", translated)
                if (english !in universalTerms) {
                    assertNotEquals("$language falls back to English: $english", english, translated)
                }
                assertFalse("Broken character in $language: $english", translated!!.contains('\uFFFD'))
            }
        }
    }

    @Test
    fun proCatalog_hasRealTranslationsForEveryAddedLanguage() {
        advancedProEnglishKeys().forEach { english ->
            addedLanguages.forEach { language ->
                val translated = advancedProCopy(language, english)
                assertNotNull("Missing $language Pro translation for: $english", translated)
                assertNotEquals("$language Pro copy falls back to English: $english", english, translated)
                assertFalse("Broken character in $language Pro copy: $english", translated!!.contains('\uFFFD'))
            }
        }
    }
}
