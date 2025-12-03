package com.example.dailylingua.ui.mistakes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dailylingua.R
import org.json.JSONArray

class MistakesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mistakes)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Hatalarım"
            setDisplayHomeAsUpEnabled(true)
        }

        val rv = findViewById<RecyclerView>(R.id.rv_mistakes)
        rv.layoutManager = LinearLayoutManager(this)

        val prefs = getSharedPreferences("daily_prefs", MODE_PRIVATE)
        val langPref = prefs.getString("selectedLanguage", "en") ?: "en"
        val mistakesStr = prefs.getString("mistakes_json", "[]")
        val arr = JSONArray(mistakesStr)

        val list = mutableListOf<MistakeItem>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val lang = obj.optString("lang", "en")
            if (lang != langPref) continue // show only current language
            val word = obj.optString("word")
            val correct = obj.optString("correct")
            val chosen = obj.optString("chosen")
            val example = obj.optString("example")
            list.add(MistakeItem(word, correct, chosen, example))
        }

        val adapter = MistakesAdapter(list)
        rv.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

data class MistakeItem(
    val word: String,
    val correct: String,
    val chosen: String,
    val example: String
)
