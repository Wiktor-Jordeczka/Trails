package edu.put.inf151785

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.provider.BaseColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import edu.put.inf151785.databinding.FragmentTab1Binding


// Fragment wyświetlający wszystkie szlaki, zawsze w MainActivity
class Tab1Fragment : Fragment() {
    private var _binding: FragmentTab1Binding? = null
    private val binding get() = _binding!!
    var ids: MutableList<Int> = mutableListOf()
    private var trailNames: MutableList<String> = mutableListOf()
    private var trailImgIds: MutableList<Int> = mutableListOf()
    var favTrails: MutableList<Int> = mutableListOf()
    lateinit var mContext: Context
    var searchedTrail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireContext()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTab1Binding.inflate(layoutInflater, container, false)

        //search box
        binding.searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchedTrail = s.toString()
                refreshAdapter((activity as MainActivity).db)
            }
        })

        // pobieramy dane szlaków do adaptera RecyclerView
        getDataFromDB((activity as MainActivity).db)
        val adapter = CaptionedImagesAdapter(trailNames, trailImgIds, favTrails)
        binding.tab1recycler.adapter = adapter
        val layoutManager = GridLayoutManager(activity, 2)
        binding.tab1recycler.layoutManager = layoutManager

        // onclick-i recyclera
        val obj = object : CaptionedImagesAdapter.RecListener {
            // otwieramy szczegóły szlaku
            override fun onClick(position: Int) {
                val fragmentContainer = (activity as MainActivity).binding.fragmentContainer
                if (fragmentContainer != null){ // układ tabletu
                    val detailFrag = TrailDetailFragment()
                    val ft = parentFragmentManager.beginTransaction()
                    detailFrag.trailID = ids[position]
                    ft.replace(R.id.fragment_container, detailFrag)
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    ft.addToBackStack(null)
                    ft.commit()
                }else{ // układ smartfona
                    val intent = Intent(activity, TrailDetailActivity::class.java)
                    intent.putExtra("id",ids[position])
                    startActivity(intent)
                }
            }

            // dodaj / usuń szlak z ulubionych w bd
            override fun onLongClick(position: Int) {
                val db = (activity as MainActivity).db
                var isFav = favTrails[position]
                isFav = if (isFav == 0){
                    1
                }else{
                    0
                }
                val values = ContentValues().apply {
                    put(DBContract.TrailsTable.COLUMN_NAME_TRAIL_FAV, isFav)
                }
                val selection = "${BaseColumns._ID} = ?"
                val selectionArgs = arrayOf(ids[position].toString())
                db.update(
                    DBContract.TrailsTable.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs
                )
                refreshAdapter(db)
            }
        }
        adapter.recListener = obj
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        refreshAdapter((activity as MainActivity).db)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshAdapter(db: SQLiteDatabase){
        ids.clear()
        trailNames.clear()
        trailImgIds.clear()
        favTrails.clear()
        if (searchedTrail.isEmpty()){
            getDataFromDB(db)
        }else{
            getFilteredDataFromDB(db, searchedTrail)
        }
        binding.tab1recycler.adapter?.notifyDataSetChanged()
    }

    // pobieramy dane o szlakach z bazy
    private fun getDataFromDB(db: SQLiteDatabase){
        val projection = arrayOf(BaseColumns._ID, DBContract.TrailsTable.COLUMN_NAME_TRAIL_NAME, DBContract.TrailsTable.COLUMN_NAME_TRAIL_DESC, DBContract.TrailsTable.COLUMN_NAME_TRAIL_IMG, DBContract.TrailsTable.COLUMN_NAME_TRAIL_LENGTH, DBContract.TrailsTable.COLUMN_NAME_TRAIL_FAV)
        val selection = null
        val selectionArgs = null
        val groupBy = null
        val having = null
        val sortOrder = "${DBContract.TrailsTable.COLUMN_NAME_TRAIL_NAME} ASC"
        val cursor = db.query(
            DBContract.TrailsTable.TABLE_NAME,   // The table to query
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
                val name = getString(getColumnIndexOrThrow(DBContract.TrailsTable.COLUMN_NAME_TRAIL_NAME))
                val imgId = getInt(getColumnIndexOrThrow(DBContract.TrailsTable.COLUMN_NAME_TRAIL_IMG))
                val isFav = getInt(getColumnIndexOrThrow(DBContract.TrailsTable.COLUMN_NAME_TRAIL_FAV))
                ids.add(id)
                trailNames.add(name)
                trailImgIds.add(imgId)
                favTrails.add(isFav)
            }
        }
        cursor.close()
    }

    // pobieramy dane o szlakach z bazy, z uwzględnieniem pola wyszukiwania
    private fun getFilteredDataFromDB(db: SQLiteDatabase, search: String){
        val projection = arrayOf(BaseColumns._ID, DBContract.TrailsTable.COLUMN_NAME_TRAIL_NAME, DBContract.TrailsTable.COLUMN_NAME_TRAIL_DESC, DBContract.TrailsTable.COLUMN_NAME_TRAIL_IMG, DBContract.TrailsTable.COLUMN_NAME_TRAIL_LENGTH, DBContract.TrailsTable.COLUMN_NAME_TRAIL_FAV)
        val selection = "${DBContract.TrailsTable.COLUMN_NAME_TRAIL_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$search%")
        val groupBy = null
        val having = null
        val sortOrder = "${DBContract.TrailsTable.COLUMN_NAME_TRAIL_NAME} ASC"
        val cursor = db.query(
            DBContract.TrailsTable.TABLE_NAME,   // The table to query
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
                val name = getString(getColumnIndexOrThrow(DBContract.TrailsTable.COLUMN_NAME_TRAIL_NAME))
                val imgId = getInt(getColumnIndexOrThrow(DBContract.TrailsTable.COLUMN_NAME_TRAIL_IMG))
                val isFav = getInt(getColumnIndexOrThrow(DBContract.TrailsTable.COLUMN_NAME_TRAIL_FAV))
                ids.add(id)
                trailNames.add(name)
                trailImgIds.add(imgId)
                favTrails.add(isFav)
            }
        }
        cursor.close()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}