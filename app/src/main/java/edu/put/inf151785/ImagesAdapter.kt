package edu.put.inf151785

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

// Adapter do recyclera własnych zdjęć
class ImagesAdapter(imagePaths: List<String>, imageIds: List<Int>) : RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {
    private var imagePaths: List<String>
    private var imageIds: List<Int>
    lateinit var imRecListener: ImRecListener

    init {
        this.imagePaths = imagePaths
        this.imageIds = imageIds
    }

    class ViewHolder(cardView: CardView) : RecyclerView.ViewHolder(cardView) {
        var cardView: CardView

        init {
            this.cardView = cardView
        }
    }

    interface ImRecListener { // do recyclera
        fun onClick(position: Int)
        fun onLongClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cv: CardView = (LayoutInflater.from(parent.context).inflate(R.layout.card_image, parent, false)) as CardView
        return ViewHolder(cv)
    }

    override fun getItemCount(): Int {
        return this.imagePaths.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cardView = holder.cardView
        val imageView = cardView.findViewById<ImageView>(R.id.card_image)
        val imgFile = File(imagePaths[position])
        val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
        imageView.setImageBitmap(bitmap)

        cardView.setOnClickListener{
            imRecListener.onClick(position)
        }

        cardView.setOnLongClickListener{
            imRecListener.onLongClick(position)
            return@setOnLongClickListener true // konsumujemy zdarzenie, żeby nie odpalić również OnClick
        }
    }
}