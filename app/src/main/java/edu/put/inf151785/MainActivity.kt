package edu.put.inf151785

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import edu.put.inf151785.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.util.UUID


class MainActivity : AppCompatActivity(){
    lateinit var binding: ActivityMainBinding
    lateinit var dbHelper: TrailsDBHelper
    lateinit var db: SQLiteDatabase
    private lateinit var mPagerAdapter: SectionsPagerAdapter
    private lateinit var currentPhotoPath: String
    private val startingFragmentPosition = 1
    private val REQUEST_IMAGE_CAPTURE = 2

    companion object { // do uprawnień
        private const val CAMERA_PERMISSION_CODE = 100
    }

    // Sprawdzamy i prosimy o uprawnienia
    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        }
    }

    // sprawdzamy czy dostaliśmy permisję
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this@MainActivity, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
                finish() // >:-)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // połączenie z bazą danych
        dbHelper = TrailsDBHelper(applicationContext) // create helper
        db = dbHelper.writableDatabase // get the db

        // ustawiamy toolbar
        val toolbar = binding.toolbar.toolbar
        setSupportActionBar(toolbar)

        // hamburger szuflady nawigacyjnej
        val drawer = binding.drawerLayout
        val toggle = ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        // elementy szuflady nawigacyjnej
        val navigationView = binding.navView
        navigationView.setNavigationItemSelectedListener { item ->
            lateinit var url: String
            val id = item.itemId
            when (id) {
                R.id.nav_link1 -> url = "https://github.com/Wiktor-Jordeczka"
                R.id.nav_link2 -> url =
                    "https://www.c-and-a.com/pl/pl/shop/przygotowania-do-wedrowki"

                R.id.nav_link3 -> url =
                    "https://blog.gopass.travel/pl/bezpieczne-wedrowki-jak-cieszyc-sie-wycieczka-bez-stresu/"
            }
            val defaultBrowser =
                Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
            defaultBrowser.setData(Uri.parse(url))
            startActivity(defaultBrowser)
            true
        }

        // ustawiamy adapter do ViewPagera
        mPagerAdapter = SectionsPagerAdapter(supportFragmentManager, this)
        val pager = binding.pager
        pager.adapter = mPagerAdapter
        pager.setCurrentItem(startingFragmentPosition)

        // pasek nawigacyjny ViewPagera
        val tabLayout = binding.tabs
        tabLayout.setupWithViewPager(pager)

        // Tablet only - przycisk fab
        binding.tabletFab?.setOnClickListener {
            dispatchTakePictureIntent()
        }

        // back button szuflady ORAZ backStackFix współczesnym sposobem!
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START)
                } else {
                    val fm = supportFragmentManager
                    if (fm.backStackEntryCount > 0) {
                        fm.popBackStack()
                    } else {
                        finish()
                    }
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // sprawdzamy permisję do robienia zdjęć
        checkPermission(
            android.Manifest.permission.CAMERA,
            CAMERA_PERMISSION_CODE
        )
    }

    override fun onDestroy() {
        dbHelper.close() // zamykamy db
        super.onDestroy()
    }

    // Adapter ViewPagera
    class SectionsPagerAdapter(fragmentManager: FragmentManager, val context: Context) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val numOfViewPagerFrags = 3

        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            super.setPrimaryItem(container, position, `object`)
        }

        override fun getCount(): Int {
            return numOfViewPagerFrags
        }

        override fun getItem(position: Int): Fragment {
            lateinit var fragment: Fragment
            when (position) {
                0 -> fragment = ImagesFragment()
                1 -> fragment = TrailsFragment()
                2 -> fragment = FavTrailsFragment()
            }
            return fragment
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return context.resources.getString(R.string.home_tab)
                1 -> return context.resources.getText(R.string.kat1_tab)
                2 -> return context.resources.getText(R.string.kat2_tab)
            }
            return null
        }
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