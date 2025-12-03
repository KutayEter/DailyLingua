package com.example.dailylingua.ui.progress

import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.dailylingua.R

class ProgressActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        // Toolbar'ı ayarla ve Up düğmesini etkinleştir
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "QUIZ'E DÖN."
            setDisplayHomeAsUpEnabled(true)
        }

    val prefs = getSharedPreferences("daily_prefs", MODE_PRIVATE)
    // Show per-selected-language totals (fallback to 0 if not present)
    val selectedLang = prefs.getString("selectedLanguage", "en") ?: "en"
    val totalQuestions = prefs.getInt("total_questions_" + selectedLang, 0)
    val correctAnswers = prefs.getInt("total_correct_" + selectedLang, 0)

        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        val tvPercent = findViewById<TextView>(R.id.tv_percent)
    val tvCorrectCount = findViewById<TextView>(R.id.tv_correct_count)
    val tvWrongCount = findViewById<TextView>(R.id.tv_wrong_count)

    // Calculate overall percent based on total questions
    val percent = if (totalQuestions == 0) 0 else (correctAnswers * 100 / totalQuestions)
        progressBar.progress = percent
        tvPercent.text = getString(R.string.progress_percent, percent)

    // Show counts: correct and wrong
    val wrongAnswers = if (totalQuestions - correctAnswers >= 0) totalQuestions - correctAnswers else 0
    tvCorrectCount.text = getString(R.string.correct_count, correctAnswers)
    tvWrongCount.text = getString(R.string.wrong_count, wrongAnswers)

        // Hatalarımı Gör butonuna tıklandığında MistakesActivity'yi aç
        findViewById<android.widget.Button>(R.id.btn_view_mistakes).setOnClickListener {
            startActivity(android.content.Intent(this, com.example.dailylingua.ui.mistakes.MistakesActivity::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // Up düğmesine basıldığında önceki ekrana dön
        finish()
        return true
    }
}
