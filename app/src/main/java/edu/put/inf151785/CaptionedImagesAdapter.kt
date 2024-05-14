package edu.put.inf151785

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

// Adapter do recyclera szlaków
class CaptionedImagesAdapter(captions: List<String>, imageIds: List<Int>, favTrails: List<Int>) : RecyclerView.Adapter<CaptionedImagesAdapter.ViewHolder>() {
    private var captions: List<String>
    private var imageIds: List<Int>
    private var favTrails: List<Int>
    lateinit var recListener: RecListener

    init {
        this.captions = captions
        this.imageIds = imageIds
        this.favTrails = favTrails
    }

    class ViewHolder(cardView: CardView) : RecyclerView.ViewHolder(cardView) {
        var cardView: CardView

        init {
            this.cardView = cardView
        }
    }

    interface RecListener { // do recyclera
        fun onClick(position: Int)
        fun onLongClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cv: CardView = (LayoutInflater.from(parent.context).inflate(R.layout.card_captioned_image, parent, false)) as CardView
        return ViewHolder(cv)
    }

    override fun getItemCount(): Int {
        return this.captions.size
    }

    // ustawiamy wygląd karty
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cardView = holder.cardView
        val imageView = cardView.findViewById<ImageView>(R.id.card_image)
        val drawable = ContextCompat.getDrawable(cardView.context, imageIds[position])
        imageView.setImageDrawable(drawable)
        imageView.contentDescription = captions[position]
        val textView = cardView.findViewById<TextView>(R.id.card_text)
        textView.text = captions[position]
        // jeżeli szlak należy do ulubionych to wyświetlamy serduszko
        val heart = cardView.findViewById<ImageView>(R.id.card_heart)
        if (favTrails[position] == 0){
            heart.visibility = View.INVISIBLE
        }else{
            heart.visibility = View.VISIBLE
        }

        cardView.setOnClickListener{
            recListener.onClick(position)
        }

        cardView.setOnLongClickListener{
            recListener.onLongClick(position)
            return@setOnLongClickListener true // konsumujemy zdarzenie, żeby nie odpalić również OnClick
        }
    }
}