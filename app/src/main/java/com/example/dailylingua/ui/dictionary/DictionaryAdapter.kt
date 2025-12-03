package com.example.dailylingua.ui.dictionary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dailylingua.R
import com.example.dailylingua.data.model.Word

class DictionaryAdapter(
    private var items: List<Word>,
    private val onSpeak: (String) -> Unit
) : RecyclerView.Adapter<DictionaryAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvWord: TextView = itemView.findViewById(R.id.tv_item_word)
        val tvTranslation: TextView = itemView.findViewById(R.id.tv_item_translation)
        val tvSpeak: TextView = itemView.findViewById(R.id.tv_speak_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_dictionary_word, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val w = items[position]
        holder.tvWord.text = w.word
        holder.tvTranslation.text = w.translation
        holder.tvSpeak.setOnClickListener {
            onSpeak(w.word)
        }
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<Word>) {
        items = newItems
        notifyDataSetChanged()
    }
}
