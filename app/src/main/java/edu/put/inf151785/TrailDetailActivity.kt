package edu.put.inf151785

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.BaseColumns
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.put.inf151785.databinding.ActivityTrailDetailBinding
import java.io.File
import java.io.IOException
import java.util.UUID


class TrailDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTrailDetailBinding
    private lateinit var detailFrag: TrailDetailFragment
    private val detailFragTag: String = "detailFrag"
    private lateinit var dbHelper: TrailsDBHelper
    private lateinit var db: SQLiteDatabase
    private val REQUEST_IMAGE_CAPTURE = 2
    private lateinit var currentPhotoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrailDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // pasek aplikacji
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // strzałeczka wstecz na pasku aplikacji

        // do wywołania kamery i zapisania zdjęcia
        val fab: FloatingActionButton = binding.fab
        fab.setOnClickListener {
            dispatchTakePictureIntent()
        }

        // pobranie fragmentu z zapisanego stanu lub utworzenie
        detailFrag = if (savedInstanceState == null) {
            TrailDetailFragment()
        } else {
            supportFragmentManager.findFragmentByTag(detailFragTag) as TrailDetailFragment
        }
        val ft = supportFragmentManager.beginTransaction()
        val trailId = intent.getIntExtra("id", 0)
        detailFrag.trailID = trailId
        ft.replace(R.id.detailFrag, detailFrag, detailFragTag)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.commit()

        // łączymy się z bd, pobieramy id obrazka
        dbHelper = TrailsDBHelper(applicationContext)
        db = dbHelper.readableDatabase
        val projection = arrayOf(BaseColumns._ID, DBContract.TrailsTable.COLUMN_NAME_TRAIL_IMG)
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(trailId.toString())
        val groupBy = null
        val having = null
        val sortOrder = null
        val cursor = db.query(
            DBContract.TrailsTable.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
            groupBy,                // don't group the rows
            having,                 // don't filter by row groups
            sortOrder               // The sort order
        )
        var drawableId = 0
        with(cursor){
            while(moveToNext()){
                drawableId = getInt(getColumnIndexOrThrow(DBContract.TrailsTable.COLUMN_NAME_TRAIL_IMG))
            }
        }
        cursor.close()
        // ustawiamy obrazek zwijający się do toolbara
        binding.trailImage.setImageDrawable(ContextCompat.getDrawable(this, drawableId))
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }

    // Tworzy plik do zdjęcia
    private fun createImageFile(): File {
        // Create an image file name
        val uid = UUID.randomUUID()
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "image_${uid}_", /* prefix */
            ".png", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    // wywołuje aplikację aparatu i prosi o zapisanie zdjęcia
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "$packageName.provider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    // jeśli zdjęcie zostało zapisane do pliku, to zapiszmy ścieżkę do pliku w bazie
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){
            val values = ContentValues().apply {
                put(DBContract.PicturesTable.COLUMN_NAME_IMG_PATH, currentPhotoPath)
            }
            db.insert(
                DBContract.PicturesTable.TABLE_NAME,
                null,
                values
            )
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}