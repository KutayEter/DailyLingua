package com.example.dailylingua.ui.mistakes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dailylingua.R

class MistakesAdapter(private val items: List<MistakeItem>) : RecyclerView.Adapter<MistakesAdapter.VH>() {
    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvWord: TextView = view.findViewById(R.id.tv_mistake_word)
        val tvChosen: TextView = view.findViewById(R.id.tv_mistake_chosen)
        val tvCorrect: TextView = view.findViewById(R.id.tv_mistake_correct)
        val tvExample: TextView = view.findViewById(R.id.tv_mistake_example)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_mistake, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val it = items[position]
        holder.tvWord.text = it.word
        holder.tvChosen.text = "Seçilen: ${it.chosen}"
        holder.tvCorrect.text = "Doğru: ${it.correct}"
        holder.tvExample.text = it.example
    }

    override fun getItemCount(): Int = items.size
}
