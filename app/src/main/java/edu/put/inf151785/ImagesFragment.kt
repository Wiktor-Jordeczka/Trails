package edu.put.inf151785

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.provider.BaseColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import edu.put.inf151785.databinding.FragmentImagesBinding
import java.io.File


// fragment wyświetlający zdjęcia zrobione poprzez aplikację
class ImagesFragment : Fragment() {
    private var _binding: FragmentImagesBinding? = null
    private val binding get() = _binding!!
    var ids: MutableList<Int> = mutableListOf()
    var imgPaths: MutableList<String> = mutableListOf()
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireContext()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImagesBinding.inflate(layoutInflater, container, false)

        // pobieramy zdjęcia z bazy i ustawiamy adapter
        getImagesFromDB((activity as MainActivity).db)
        val adapter = ImagesAdapter(imgPaths, ids)
        binding.imagesTabRecycler.adapter = adapter
        val layoutManager = GridLayoutManager(activity, 2)
        binding.imagesTabRecycler.layoutManager = layoutManager

        // onclick recyclera
        val obj = object : ImagesAdapter.ImRecListener {
            // wyświetlamy zdjęcie w fullscreenie
            override fun onClick(position: Int) {
                val intent = Intent(activity, FullscreenImageActivity::class.java)
                intent.putExtra("path",imgPaths[position])
                startActivity(intent)
            }

            // usuwamy zdjęcie (plik oraz wpis w bd)
            override fun onLongClick(position: Int) {
                val db = (activity as MainActivity).db
                val selection = "${BaseColumns._ID} = ?"
                val selectionArgs = arrayOf(ids[position].toString())
                db.delete(
                    DBContract.PicturesTable.TABLE_NAME,
                    selection,
                    selectionArgs
                )
                // usuwamy plik
                val file = File(imgPaths[position])
                file.delete()
                refreshAdapter(db) // odświeżamy dane adaptera
            }
        }
        adapter.imRecListener = obj

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        refreshAdapter((activity as MainActivity).db)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshAdapter(db: SQLiteDatabase){
        ids.clear()
        imgPaths.clear()
        getImagesFromDB(db)
        binding.imagesTabRecycler.adapter?.notifyDataSetChanged()
    }

    // Pobieramy ścieżki do zdjęć z bazy danych
    private fun getImagesFromDB(db: SQLiteDatabase){
        val projection = arrayOf(BaseColumns._ID, DBContract.PicturesTable.COLUMN_NAME_IMG_PATH)
        val selection = null
        val selectionArgs = null
        val groupBy = null
        val having = null
        val sortOrder = null
        val cursor = db.query(
            DBContract.PicturesTable.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
            groupBy,                // group the rows
            having,                 // filter by row groups
            sortOrder               // The sort order
        )
        with(cursor){
            while(moveToNext()){
                val id = getInt(getColumnIndexOrThrow(BaseColumns._ID))
                val path = getString(getColumnIndexOrThrow(DBContract.PicturesTable.COLUMN_NAME_IMG_PATH))
                ids.add(id)
                imgPaths.add(path)
            }
        }
        cursor.close()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}