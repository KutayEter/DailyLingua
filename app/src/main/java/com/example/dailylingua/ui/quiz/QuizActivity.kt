package com.example.dailylingua.ui.quiz

import android.graphics.Color
import android.os.Bundle
import android.content.res.Resources
import android.media.AudioAttributes
import android.media.SoundPool
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.Locale
import android.widget.Button
import org.json.JSONArray
import org.json.JSONObject
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.dailylingua.R
import com.example.dailylingua.data.repository.WordRepository

class QuizActivity : AppCompatActivity() {
    private var currentIndex = 0
    private var score = 0
    private lateinit var quizWords: List<com.example.dailylingua.data.model.Word>
    // SoundPool for short sound effects
    private var soundPool: SoundPool? = null
    private var soundCorrectId: Int = 0
    private var soundWrongId: Int = 0
    // TextToSpeech for pronouncing questions
    private var tts: TextToSpeech? = null
    private var reportedSoundMissing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // Initialize SoundPool (API level safe)
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            soundPool = SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build()

            // Load sound resources if they exist. Add files to res/raw: correct.mp3 and wrong.mp3
            try {
                soundCorrectId = soundPool?.load(this, R.raw.correct, 1) ?: 0
            } catch (e: Resources.NotFoundException) {
                soundCorrectId = 0
            }

            try {
                soundWrongId = soundPool?.load(this, R.raw.wrong, 1) ?: 0
            } catch (e: Resources.NotFoundException) {
                soundWrongId = 0
            }
            // If both sound effect ids are zero, warn the user once
            if ((soundCorrectId == 0 && soundWrongId == 0) && !reportedSoundMissing) {
                Toast.makeText(this, "Ses efektleri yüklenemedi. app/src/main/res/raw içinde correct/wrong dosyalarını kontrol edin.", Toast.LENGTH_LONG).show()
                reportedSoundMissing = true
            }
        } catch (e: Exception) {
            // If SoundPool initialization fails for any reason, null it and continue silently
            soundPool = null
            soundCorrectId = 0
            soundWrongId = 0
        }

        // Toolbar'ı ayarla
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "Quiz"
            setDisplayHomeAsUpEnabled(true)
        }

        val prefs = getSharedPreferences("daily_prefs", MODE_PRIVATE)
        val lang = prefs.getString("selectedLanguage", "en") ?: "en"

    val repo = WordRepository(this, lang)
    // Increase quiz length from 5 to 10
    quizWords = repo.randomQuizWords(10)

        showQuestion()

        // Initialize TTS after we know the selected language
        try {
            tts = TextToSpeech(this) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val locale = when (lang) {
                        "en" -> Locale.ENGLISH
                        "de" -> Locale.GERMAN
                        "ru" -> Locale("ru")
                        "tr" -> Locale("tr")
                        else -> Locale.ENGLISH
                    }
                    val res = tts?.setLanguage(locale)
                    // if language not supported, notify user
                    if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(this, "TTS dili desteklenmiyor veya veri eksik. Cihaz TTS ayarlarından dil paketini kontrol edin.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: Exception) {
            tts = null
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            soundPool?.release()
            soundPool = null
        } catch (e: Exception) {
            // ignore
        }
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun showQuestion() {
        if (currentIndex >= quizWords.size) {
            showResult()
            return
        }

        val q = quizWords[currentIndex]
        val tvQuestion = findViewById<TextView>(R.id.tv_question)
        val btnA = findViewById<Button>(R.id.btn_a)
        val btnB = findViewById<Button>(R.id.btn_b)
        val btnC = findViewById<Button>(R.id.btn_c)
        val btnD = findViewById<Button>(R.id.btn_d)
    val btnNext = findViewById<Button>(R.id.btn_next)
    val btnSpeak = findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_speak)

        tvQuestion.text = q.example

        // Speak the question/example using TTS (if available)
        try {
            if (tts != null) {
                val textToSpeak = q.example
                // use utteranceId to identify if needed
                tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "QUESTION_${currentIndex}")
            }
        } catch (e: Exception) {
            // ignore TTS errors
        }

        val options = q.options.shuffled()
        btnA.text = options.getOrNull(0) ?: ""
        btnB.text = options.getOrNull(1) ?: ""
        btnC.text = options.getOrNull(2) ?: ""
        btnD.text = options.getOrNull(3) ?: ""

    val optionButtons = listOf(btnA, btnB, btnC, btnD)
        optionButtons.forEach { btn ->
            btn.setBackgroundColor(Color.LTGRAY)
            btn.isEnabled = true
            btn.setOnClickListener {
                optionButtons.forEach { it.isEnabled = false }
                if (btn.text.toString() == q.correct) {
                    btn.setBackgroundColor(Color.parseColor("#4CAF50"))
                    score++
                    // play correct sound if available
                    if (soundPool != null && soundCorrectId != 0) {
                        soundPool?.play(soundCorrectId, 1f, 1f, 1, 0, 1f)
                    }
                } else {
                    btn.setBackgroundColor(Color.RED)
                    // highlight correct
                    optionButtons.find { it.text.toString() == q.correct }?.setBackgroundColor(Color.parseColor("#4CAF50"))
                    // Save mistake to shared prefs as JSON array
                    try {
                        val prefs = getSharedPreferences("daily_prefs", MODE_PRIVATE)
                        val langPref = prefs.getString("selectedLanguage", "en") ?: "en"
                        val mistakesStr = prefs.getString("mistakes_json", "[]")
                        val arr = JSONArray(mistakesStr)
                        val obj = JSONObject()
                        obj.put("word", q.word)
                        obj.put("correct", q.correct)
                        obj.put("chosen", btn.text.toString())
                        obj.put("example", q.example)
                        obj.put("lang", langPref)
                        arr.put(obj)
                        prefs.edit().putString("mistakes_json", arr.toString()).apply()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    // play wrong sound if available
                    if (soundPool != null && soundWrongId != 0) {
                        soundPool?.play(soundWrongId, 1f, 1f, 1, 0, 1f)
                    }
                }
                
                // Cümlenin çevirisini göster
                tvQuestion.text = "${q.example}\n\n${q.exampleTranslation}"
            }
        }

        btnNext.setOnClickListener {
            currentIndex++
            showQuestion()
        }

        // Replay TTS button
        btnSpeak.setOnClickListener {
            try {
                if (tts != null) {
                    val textToSpeak = q.example
                    tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "QUESTION_REPLAY_${currentIndex}")
                } else {
                    Toast.makeText(this, "TTS kullanılamıyor.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "TTS oynatılırken hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showResult() {
        val prefs = getSharedPreferences("daily_prefs", MODE_PRIVATE)
        // Bu quiz'in başarı yüzdesini hesapla
        val quizPercentage = (score.toFloat() / quizWords.size * 100).toInt()

        // Genel istatistikleri güncelle (global)
        val totalQuestions = prefs.getInt("total_questions", 0) + quizWords.size
        val totalCorrect = prefs.getInt("total_correct", 0) + score

        // Yeni genel istatistikleri kaydet
        val editor = prefs.edit()
        editor.putInt("total_questions", totalQuestions)
        editor.putInt("total_correct", totalCorrect)

        // Ayrıca seçili dile göre istatistikleri de tut: total_questions_<lang> ve total_correct_<lang>
        val lang = prefs.getString("selectedLanguage", "en") ?: "en"
        val langQKey = "total_questions_" + lang
        val langCKey = "total_correct_" + lang
        val totalQuestionsLang = prefs.getInt(langQKey, 0) + quizWords.size
        val totalCorrectLang = prefs.getInt(langCKey, 0) + score
        editor.putInt(langQKey, totalQuestionsLang)
        editor.putInt(langCKey, totalCorrectLang)

        editor.apply()

        val customDialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .create()
            
        val message = buildString {
            append("Bu Quiz Sonucun: $score/${quizWords.size} (%$quizPercentage)\n\n")
            append("Genel Başarı Durumun:\n")
            append("Toplam Doğru: $totalCorrect\n")
            append("Toplam Soru: $totalQuestions")
        }
            
        customDialog.apply {
            setTitle("Quiz Tamamlandı!")
            setMessage(message)
            
            setButton(AlertDialog.BUTTON_POSITIVE, "Tamam") { _, _ ->
                finish()
            }
            
            setCancelable(false)
            
            // Dialog'u göster
            show()
            
            // Dialog görünür olduktan sonra stil ayarları
            getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                setTextColor(getColor(R.color.primary))
            }
            
            window?.setBackgroundDrawableResource(R.drawable.dialog_background)
            
            // Başlık ve mesaj renklerini ayarla
            findViewById<TextView>(android.R.id.title)?.apply {
                setTextColor(getColor(R.color.text_primary))
                textSize = 20f  // Başlık boyutunu artır
            }
            
            findViewById<TextView>(android.R.id.message)?.apply {
                setTextColor(getColor(R.color.text_primary))
                textSize = 16f  // Mesaj boyutunu artır
                setLineSpacing(0f, 1.2f)  // Satır aralığını artır
            }
        }
    }
}
