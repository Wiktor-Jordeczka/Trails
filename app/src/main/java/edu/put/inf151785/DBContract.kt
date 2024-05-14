package edu.put.inf151785

import android.provider.BaseColumns

// Definicje bazy danych
object DBContract {
    const val DATABASE_VERSION = 8
    const val DATABASE_NAME = "trails_database.db"
    private const val TEXT_TYPE = " TEXT"
    private const val INT_TYPE = " INTEGER"
    private const val FLOAT_TYPE = " REAL"
    private const val COMMA_SEP = ","

    //Tabela szlaków
    object TrailsTable : BaseColumns {
        const val TABLE_NAME = "trails"
        const val COLUMN_NAME_TRAIL_NAME = "trail_name"
        const val COLUMN_NAME_TRAIL_DESC = "trail_description"
        const val COLUMN_NAME_TRAIL_IMG = "trail_img_id"
        const val COLUMN_NAME_TRAIL_LENGTH = "trail_length"
        const val COLUMN_NAME_TRAIL_FAV = "is_favorite"
        const val CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY," +
                COLUMN_NAME_TRAIL_NAME + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_TRAIL_DESC + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_TRAIL_IMG + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_TRAIL_FAV + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_TRAIL_LENGTH + FLOAT_TYPE + " );"
        const val DELETE_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME;"
    }

    //Tabela etapów
    object StagesTable : BaseColumns {
        const val TABLE_NAME = "trails_stages"
        const val COLUMN_NAME_STAGE_NAME = "stage_name"
        const val COLUMN_NAME_TRAIL_ID_FK = "trail_id_fk"
        const val CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                BaseColumns._ID + " $INT_TYPE PRIMARY KEY," +
                COLUMN_NAME_STAGE_NAME + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_TRAIL_ID_FK + INT_TYPE + COMMA_SEP +
                "FOREIGN KEY($COLUMN_NAME_TRAIL_ID_FK) REFERENCES ${TrailsTable.TABLE_NAME}(${BaseColumns._ID})" + " );"
        const val DELETE_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME;"
    }

    //Tabela czasów
    object TimesTable : BaseColumns {
        const val TABLE_NAME = "trails_times"
        const val COLUMN_NAME_TRAIL_TIME = "time"
        const val COLUMN_NAME_TRAIL_ID_FK = "trail_id_fk"
        const val CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                BaseColumns._ID + " $INT_TYPE PRIMARY KEY," +
                COLUMN_NAME_TRAIL_TIME + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_TRAIL_ID_FK + INT_TYPE + COMMA_SEP +
                "FOREIGN KEY($COLUMN_NAME_TRAIL_ID_FK) REFERENCES ${TrailsTable.TABLE_NAME}(${BaseColumns._ID})" + " );"
        const val DELETE_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME;"
    }

    //Tabela zdjęć
    object PicturesTable : BaseColumns {
        const val TABLE_NAME = "trails_pictures"
        const val COLUMN_NAME_IMG_PATH = "image"
        const val CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                BaseColumns._ID + " $INT_TYPE PRIMARY KEY," +
                COLUMN_NAME_IMG_PATH + TEXT_TYPE + " );"
        const val DELETE_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME;"
    }
}