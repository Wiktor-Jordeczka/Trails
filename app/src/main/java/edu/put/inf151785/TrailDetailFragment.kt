package edu.put.inf151785

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.provider.BaseColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.appbar.CollapsingToolbarLayout
import edu.put.inf151785.databinding.FragmentTrailDetailBinding


class TrailDetailFragment : Fragment() {

    private var _binding: FragmentTrailDetailBinding? = null
    private val binding get() = _binding!!
    var trailID: Int? = null
    private lateinit var dbHelper: TrailsDBHelper
    private lateinit var db: SQLiteDatabase
    var speed = 0F
    private var trailLengthKm = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null){
            trailID = savedInstanceState.getInt("trailID")
            speed = savedInstanceState.getFloat("speed")
            trailLengthKm = savedInstanceState.getFloat("trailLengthKm")
        }else{
            val stoper = StoperFragment()
            stoper.trailID = trailID
            val ft: FragmentTransaction = childFragmentManager.beginTransaction()
            ft.add(R.id.stoper_container, stoper)
            ft.addToBackStack(null)
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            ft.commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        trailID?.let { outState.putInt("trailID", it) }
        speed.let { outState.putFloat("speed", it) }
        trailLengthKm.let { outState.putFloat("trailLengthKm", it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTrailDetailBinding.inflate(layoutInflater, container, false)
        binding.stagesListView.isNestedScrollingEnabled = true // fix scroll

        // input kalkulatora czasu podróży
        binding.speedEdit.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotEmpty()){ // czy cokolwiek jest wpisane
                    try { // czy możemy odczytać input jako float
                        val tmp = (s.toString()).toFloat()
                        if(tmp>0){ // czy prędkość dodatnia
                            speed = tmp
                            calculateTime()
                        }
                    } catch (ex: NumberFormatException) {
                        // Not a float
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        trailID?.let {
            displayDetails(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // obliczamy czas podróży i wyświetlamy go
    private fun calculateTime(){
        val time = trailLengthKm/speed
        val hrsString = "%02d".format(time.toInt())
        val minString = "%02.0f".format((time % 1)*60)
        val text = "km\\h: podróż zajmie zajmie ${hrsString}:${minString} h"
        binding.speedtext.text = text
    }

    // pobieramy sczegóły szlaku oraz etapy i wyświetlamy je
    private fun displayDetails(id: Int){
        // db connection
        dbHelper = context?.let { TrailsDBHelper(it) }!! // create helper
        db = dbHelper.writableDatabase // get the db
        val table = "${DBContract.TrailsTable.TABLE_NAME} trails INNER JOIN ${DBContract.StagesTable.TABLE_NAME} stages on trails.${BaseColumns._ID} = stages.${DBContract.StagesTable.COLUMN_NAME_TRAIL_ID_FK}"
        val projection = arrayOf(DBContract.TrailsTable.COLUMN_NAME_TRAIL_NAME, DBContract.TrailsTable.COLUMN_NAME_TRAIL_DESC, DBContract.TrailsTable.COLUMN_NAME_TRAIL_IMG, DBContract.TrailsTable.COLUMN_NAME_TRAIL_LENGTH, DBContract.TrailsTable.COLUMN_NAME_TRAIL_FAV, DBContract.StagesTable.COLUMN_NAME_STAGE_NAME)
        val selection = "trails.${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(id.toString())
        val groupBy = null
        val having = null
        val sortOrder = null
        val cursor = db.query(
            table,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
            groupBy,                // don't group the rows
            having,                 // don't filter by row groups
            sortOrder               // The sort order
        )
        var trailName = ""
        var trailDescription = ""
        val trailStages: MutableList<String> = mutableListOf()
        with(cursor){
            while(moveToNext()){
                trailName = getString(getColumnIndexOrThrow(DBContract.TrailsTable.COLUMN_NAME_TRAIL_NAME))
                trailDescription = getString(getColumnIndexOrThrow(DBContract.TrailsTable.COLUMN_NAME_TRAIL_DESC))
                trailLengthKm = getFloat(getColumnIndexOrThrow(DBContract.TrailsTable.COLUMN_NAME_TRAIL_LENGTH))
                val stage = getString(getColumnIndexOrThrow(DBContract.StagesTable.COLUMN_NAME_STAGE_NAME))
                trailStages.add(stage)
            }
        }
        cursor.close()
        dbHelper.close()
        binding.stagesListView.adapter =
            activity?.let { ArrayAdapter(it.applicationContext, R.layout.stage_list_element, trailStages) }

        // alternatywny sposób zrobienia różnych layoutów dla tabletu i smartfona
        // Wyświetlamy tytuł szlaku tylko w wersji tabletowej
        if(isAdded){
            if (activity?.javaClass?.simpleName == "TrailDetailActivity"){
                binding.trailName.visibility = View.GONE
            }else{
                binding.trailName.text = trailName
            }
        }
        binding.trailDescription.text = trailDescription
        val distanceText = "Dystans: $trailLengthKm km"
        binding.trailDistance.text = distanceText

        // Nadpisuje tekst "trail detail" nazwą szlaku - tylko dla układu smartfona
        val tb = activity?.findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar_layout)
        if (tb != null) {
            tb.title = trailName
        }
    }
}