package edu.put.inf151785

import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.put.inf151785.databinding.ActivityFullscreenImageBinding
import java.io.File


// Aktywność do wyświetlania obrazka w pełnych wymiarach
class FullscreenImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFullscreenImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullscreenImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imgPath = intent.getStringExtra("path")!!
        val imageView = binding.fullscreenImage
        val imgFile = File(imgPath)
        val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
        imageView.setImageBitmap(bitmap)

        // centrujemy obrazek i ładnie zmieniamy jego wymiary by sięgał aż do brzegów ekranu
        val drawable = imageView.drawable
        val w = drawable.intrinsicWidth.toFloat()
        val h = drawable.intrinsicHeight.toFloat()
        val imageRectF = RectF(0F,0F,w,h)
        val newW = getScreenWidth(this@FullscreenImageActivity).toFloat()
        val newH = getScreenHeight(this@FullscreenImageActivity).toFloat()
        val viewRectF = RectF(0F,0F,newW,newH)
        val m: Matrix = imageView.imageMatrix
        m.setRectToRect(imageRectF, viewRectF, Matrix.ScaleToFit.CENTER)
        imageView.imageMatrix = m
        imageView.myMatrix = m // potrzebne wewnątrz
    }
}