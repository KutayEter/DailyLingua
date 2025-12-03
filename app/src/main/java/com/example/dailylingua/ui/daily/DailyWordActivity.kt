package com.example.dailylingua.ui.daily

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.dailylingua.R
import com.example.dailylingua.data.repository.WordRepository
import com.example.dailylingua.ui.language.LanguageSelectActivity
import androidx.appcompat.widget.Toolbar
 

class DailyWordActivity : AppCompatActivity() {
    private lateinit var repo: WordRepository
    private lateinit var tvWord: TextView
    private lateinit var tvTranslation: TextView
    private lateinit var tvExample: TextView
    private var mainLogo: android.widget.ImageView? = null
    private var mainCard: com.google.android.material.card.MaterialCardView? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_word)

        // View'ları başlat
        initializeViews()
        
        // Repository'yi başlat
        val prefs = getSharedPreferences("daily_prefs", MODE_PRIVATE)
        val lang = prefs.getString("selectedLanguage", "en") ?: "en"
        repo = WordRepository(this, lang)

        // Kelime verilerini yükle
        // Eğer aktiviteler arası gelen intent dil seçiminden gelindiğini işaretliyorsa,
        // başlangıçta hiçbir cümle göstermiyoruz. Aksi halde verileri yükle.
        val fromLangSelect = intent.getBooleanExtra("fromLanguageSelect", false)
        if (!fromLangSelect) {
            loadWordData()
            // ensure card is visible if not coming from language select
            mainCard?.visibility = android.view.View.VISIBLE
            // keep logo visible
            mainLogo?.visibility = android.view.View.VISIBLE
        } else {
            // temizle and hide only the card (keep logo visible)
            tvWord.text = ""
            tvTranslation.text = ""
            tvExample.text = ""
            mainCard?.visibility = android.view.View.GONE
        }

        // Buton tıklama olaylarını ayarla
        setupButtonClickListeners()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Eğer yeni intent dil seçiminden geldiyse, temizle; aksi halde yükle
        val fromLangSelect = intent.getBooleanExtra("fromLanguageSelect", false)
        if (fromLangSelect) {
            tvWord.text = ""
            tvTranslation.text = ""
            tvExample.text = ""
            // hide only the card when returning from language select; keep logo visible
            mainCard?.visibility = android.view.View.GONE
        } else {
            loadWordData()
            mainLogo?.visibility = android.view.View.VISIBLE
            mainCard?.visibility = android.view.View.VISIBLE
        }
    }

    private fun initializeViews() {
        // Toolbar'ı ayarla
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "DailyLingua"

        // TextView'ları başlat
        tvWord = findViewById(R.id.tv_word)
        tvTranslation = findViewById(R.id.tv_translation)
        tvExample = findViewById(R.id.tv_example)

        // Main logo and card
        mainLogo = findViewById(R.id.main_logo_bg)
        mainCard = findViewById(com.example.dailylingua.R.id.main_card)
    }

    private fun setupButtonClickListeners() {
        findViewById<Button>(R.id.btn_quiz).setOnClickListener {
            startActivity(Intent(this, com.example.dailylingua.ui.quiz.QuizActivity::class.java))
        }

        // Dictionary navigation button (placed under Quiz)
        findViewById<Button>(R.id.btn_dictionary_nav).setOnClickListener {
            startActivity(Intent(this, com.example.dailylingua.ui.dictionary.DictionaryActivity::class.java))
        }

        findViewById<Button>(R.id.btn_progress).setOnClickListener {
            startActivity(Intent(this, com.example.dailylingua.ui.progress.ProgressActivity::class.java))
        }
        // Open MistakesActivity directly from main screen
        findViewById<Button>(R.id.btn_view_mistakes_main).setOnClickListener {
            startActivity(Intent(this, com.example.dailylingua.ui.mistakes.MistakesActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_change_language -> {
                startActivity(Intent(this, LanguageSelectActivity::class.java))
                true
            }
            R.id.action_home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadWordData() {
        val word = repo.todaysWord()
        if (word != null) {
            tvWord.text = word.word
            tvTranslation.text = word.translation
            tvExample.text = word.example
        } else {
            tvWord.text = getString(R.string.no_words)
            tvTranslation.text = ""
            tvExample.text = ""
        }
    }
}
