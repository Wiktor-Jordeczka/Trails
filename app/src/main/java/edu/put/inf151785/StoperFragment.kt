package edu.put.inf151785

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.provider.BaseColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import edu.put.inf151785.databinding.FragmentStoperBinding


// Fragment stopera, używany we fragmencie TrailDetailFragment
class StoperFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentStoperBinding? = null
    private val binding get() = _binding!!
    private var seconds = 0
    private var running = false
    private var wasRunning = false
    private var stopTime: Long = 0
    private lateinit var dbHelper: TrailsDBHelper
    private lateinit var db: SQLiteDatabase
    var trailID: Int? = null
    val handler = Handler()
    private var trailTimes: MutableList<String> = mutableListOf()
    private var timesIds: MutableList<Int> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            seconds = savedInstanceState.getInt("seconds");
            running = savedInstanceState.getBoolean("running");
            wasRunning = savedInstanceState.getBoolean("wasRunning");
            trailID = savedInstanceState.getInt("trailID")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoperBinding.inflate(layoutInflater, container, false)
        runStoper()
        binding.startButton.setOnClickListener(this)
        binding.stopButton.setOnClickListener(this)
        binding.resetButton.setOnClickListener(this)
        binding.saveButton.setOnClickListener(this)
        binding.timesListView.isNestedScrollingEnabled = true // fix scroll
        binding.timesListView.setOnItemLongClickListener { _, _, position, _ ->
            deleteTime(timesIds[position]) // usuwamy czas z tabeli
            updateAdapter() // odświeżamy adapter
            true// konsumujemy zdarzenie dla poprawności
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        updateAdapter() // odświeżamy adapter
    }

    override fun onPause() {
        wasRunning = running
        running = false
        stopTime = SystemClock.elapsedRealtime()/1000
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (wasRunning) {
            running = true
            seconds += ((SystemClock.elapsedRealtime()/1000) - stopTime).toInt()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("seconds", seconds);
        outState.putBoolean("running", running);
        outState.putBoolean("wasRunning", wasRunning);
        trailID?.let { outState.putInt("trailID", it) }
    }

    private fun onClickStart() {
        running = true
    }

    private fun onClickStop() {
        running = false
    }

    private fun onClickReset() {
        running = false
        seconds = 0
    }

    // zapisujemy czas do bazy danych
    private fun onClickSave() {
        dbHelper = context?.let { TrailsDBHelper(it) }!! // create helper
        db = dbHelper.writableDatabase // get the db
        val values = ContentValues().apply {
            put(DBContract.TimesTable.COLUMN_NAME_TRAIL_TIME, seconds)
            put(DBContract.TimesTable.COLUMN_NAME_TRAIL_ID_FK, trailID)
        }
        db.insert(
            DBContract.TimesTable.TABLE_NAME,
            null,
            values
        )
        dbHelper.close()
        updateAdapter() // odświeżamy adapter
    }

    // handler stopera
    private fun runStoper() {
        val timeView: TextView = binding.timeView
        handler.post(object : Runnable {
            private var prevSeconds = 0
            override fun run() {
                if (seconds != prevSeconds){
                    // Bugfix dla układu tabletowego, gdy odpalimy kilka stoperów naraz
                    handler.removeCallbacksAndMessages(null)
                }
                val hours = seconds / 3600
                val minutes = seconds % 3600 / 60
                val secs = seconds % 60
                val time = String.format("%d:%02d:%02d", hours, minutes, secs)
                timeView.text = time
                if (running) {
                    seconds++
                    prevSeconds = seconds
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.start_button -> onClickStart()
            R.id.stop_button -> onClickStop()
            R.id.reset_button -> onClickReset()
            R.id.save_button -> onClickSave()
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null) // dla poprawności
        super.onDestroy()
    }

    // odświeżamy dane adaptera listy czasów
    private fun updateAdapter(){
        trailTimes.clear()
        timesIds.clear()
        trailID?.let { getTimes(it) }
        binding.timesListView.adapter =
            activity?.let { ArrayAdapter(it.applicationContext, R.layout.time_list_element, trailTimes) }
        (binding.timesListView.adapter as ArrayAdapter<*>?)?.notifyDataSetChanged()
    }

    // pobieramy czasy z bazy danych
    private fun getTimes(id: Int){
        // db connection
        dbHelper = context?.let { TrailsDBHelper(it) }!! // create helper
        db = dbHelper.readableDatabase // get the db
        val table = DBContract.TimesTable.TABLE_NAME
        val projection = arrayOf(BaseColumns._ID, DBContract.TimesTable.COLUMN_NAME_TRAIL_TIME)
        val selection = "${DBContract.TimesTable.COLUMN_NAME_TRAIL_ID_FK} = ?"
        val selectionArgs = arrayOf(id.toString())
        val groupBy = null
        val having = null
        val sortOrder = "${DBContract.TimesTable.COLUMN_NAME_TRAIL_TIME} ASC"
        val cursor = db.query(
            table,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
            groupBy,                // group the rows
            having,                 // filter by row groups
            sortOrder               // The sort order
        )
        var pos = 1
        with(cursor){
            while(moveToNext()){
                val row = getInt(getColumnIndexOrThrow(BaseColumns._ID))
                val seconds = getInt(getColumnIndexOrThrow(DBContract.TimesTable.COLUMN_NAME_TRAIL_TIME))
                val hours = seconds / 3600
                val minutes = seconds % 3600 / 60
                val secs = seconds % 60
                val time = String.format("#%03d:  %d:%02d:%02d", pos, hours, minutes, secs)
                timesIds.add(row)
                trailTimes.add(time)
                pos++
            }
        }
        cursor.close()
        dbHelper.close()
    }

    // usuwamy czas z bazy danych
    private fun deleteTime(id: Int) {
        // db connection
        dbHelper = context?.let { TrailsDBHelper(it) }!! // create helper
        db = dbHelper.writableDatabase // get the db
        val table = DBContract.TimesTable.TABLE_NAME
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(id.toString())
        db.delete(table, selection, selectionArgs)
        dbHelper.close()
    }
}