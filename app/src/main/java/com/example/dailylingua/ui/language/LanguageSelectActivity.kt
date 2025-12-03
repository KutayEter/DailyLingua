package com.example.dailylingua.ui.language

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.dailylingua.R

class LanguageSelectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_select)

        val btnEn = findViewById<Button>(R.id.btn_en)
        val btnDe = findViewById<Button>(R.id.btn_de)
        val btnRu = findViewById<Button>(R.id.btn_ru)

        val prefs = getSharedPreferences("daily_prefs", MODE_PRIVATE)

        val setupLanguageButton = { button: Button, langCode: String ->
            button.setOnClickListener {
                prefs.edit().putString("selectedLanguage", langCode).apply()
                
                // Eğer aktivite yığınında DailyWordActivity varsa, onu yenile
                val intent = Intent(this, com.example.dailylingua.ui.daily.DailyWordActivity::class.java)
                // işaret ekle: dil seçiminden geldiğimizi belirt
                intent.putExtra("fromLanguageSelect", true)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
        }

        setupLanguageButton(btnEn, "en")
        setupLanguageButton(btnDe, "de")
        setupLanguageButton(btnRu, "ru")
    }
}
