package com.example.dailylingua.ui.dictionary

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dailylingua.R
import com.example.dailylingua.data.repository.WordRepository
import java.util.Locale

class DictionaryActivity : AppCompatActivity() {

    private lateinit var adapter: DictionaryAdapter
    private var tts: TextToSpeech? = null
    private var currentLang: String = "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dictionary)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_dict)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.dictionary_title)

        // Spinner for language selection
    val spinner = findViewById<Spinner>(R.id.spinner_language)
    val langs = listOf("en", "de", "ru")
    val labels = listOf("English", "Deutsch", "Русский")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        // RecyclerView
        val rv = findViewById<RecyclerView>(R.id.rv_dictionary)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = DictionaryAdapter(emptyList()) { word ->
            speakWord(word)
        }
        rv.adapter = adapter

        // Load initial language from shared prefs
        val prefs = getSharedPreferences("daily_prefs", MODE_PRIVATE)
        val selected = prefs.getString("selectedLanguage", "en") ?: "en"
        val initialIndex = langs.indexOf(selected).coerceAtLeast(0)
        spinner.setSelection(initialIndex)

        fun loadForLang(langCode: String) {
            val repo = WordRepository(this, langCode)
            val all = repo.allWords().sortedBy { it.word }
            adapter.update(all)
            // set TTS language when loading but DO NOT show toast if language missing
            currentLang = langCode
            setTtsLocale(langCode, false)
        }

        // initial load
        loadForLang(selected)

        spinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val lang = langs[position]
                prefs.edit().putString("selectedLanguage", lang).apply()
                loadForLang(lang)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun setTtsLocale(lang: String, showToastOnFailure: Boolean = false) {
        try {
            val locale = when (lang) {
                "en" -> Locale.ENGLISH
                "de" -> Locale.GERMAN
                "ru" -> Locale("ru")
                "tr" -> Locale("tr")
                else -> Locale.ENGLISH
            }

            if (tts == null) {
                // create and set language when ready. Use showToastOnFailure to control whether to notify user.
                tts = TextToSpeech(this) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        try {
                            val res = tts?.setLanguage(locale)
                            if ((res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) && showToastOnFailure) {
                                // show toast only if caller requested failures to be shown
                                Toast.makeText(this, "TTS dili eksik/desteklenmiyor: $lang", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            // ignore
                        }
                    } else {
                        // initialization failed
                        tts = null
                    }
                }
            } else {
                // already initialized — set immediately and show toast only if requested
                val res = tts?.setLanguage(locale)
                if ((res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) && showToastOnFailure) {
                    Toast.makeText(this, "TTS dili eksik/desteklenmiyor: $lang", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            tts = null
        }
    }

    private fun speakWord(word: String) {
        try {
            // Ensure TTS language is available and show a toast if it isn't (user-initiated)
            setTtsLocale(currentLang, true)
            if (tts != null) {
                tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "DICT_${word.hashCode()}")
            } else {
                Toast.makeText(this, "TTS kullanılamıyor.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "TTS hatası: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            // ignore
        }
    }
}
