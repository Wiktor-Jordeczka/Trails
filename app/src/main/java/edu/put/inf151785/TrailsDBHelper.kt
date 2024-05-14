package edu.put.inf151785

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


// DBHelper, tworzy bazę danych i ją usuwa gdy jest taka potrzeba
class TrailsDBHelper(context: Context) : SQLiteOpenHelper(context, DBContract.DATABASE_NAME, null, DBContract.DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DBContract.TrailsTable.CREATE_TABLE)
        db.execSQL(DBContract.StagesTable.CREATE_TABLE)
        db.execSQL(DBContract.TimesTable.CREATE_TABLE)
        db.execSQL(DBContract.PicturesTable.CREATE_TABLE)

        // Wstawiamy rekordy
        for(trail in DBData.trails){
            val trailValues = ContentValues().apply {
                put(DBContract.TrailsTable.COLUMN_NAME_TRAIL_NAME, trail.name)
                put(DBContract.TrailsTable.COLUMN_NAME_TRAIL_DESC, trail.description)
                put(DBContract.TrailsTable.COLUMN_NAME_TRAIL_IMG, trail.imgResourceId)
                put(DBContract.TrailsTable.COLUMN_NAME_TRAIL_LENGTH, trail.length)
                put(DBContract.TrailsTable.COLUMN_NAME_TRAIL_FAV, trail.fav)
            }
            val newTrailId = db.insert(DBContract.TrailsTable.TABLE_NAME, null, trailValues)
            for(stage in trail.stages) {
                val stageValues = ContentValues().apply {
                    put(DBContract.StagesTable.COLUMN_NAME_STAGE_NAME, stage)
                    put(DBContract.StagesTable.COLUMN_NAME_TRAIL_ID_FK, newTrailId)
                }
                db.insert(DBContract.StagesTable.TABLE_NAME, null, stageValues)
            }
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL(DBContract.TrailsTable.DELETE_TABLE)
        db.execSQL(DBContract.StagesTable.DELETE_TABLE)
        db.execSQL(DBContract.TimesTable.DELETE_TABLE)
        db.execSQL(DBContract.PicturesTable.DELETE_TABLE)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db,oldVersion,newVersion)
    }
}