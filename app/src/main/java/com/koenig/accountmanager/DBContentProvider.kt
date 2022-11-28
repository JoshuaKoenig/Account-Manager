package com.koenig.accountmanager

import android.content.*
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import java.lang.IllegalArgumentException

class DBContentProvider: ContentProvider() {

    companion object
    {
        // PROVIDER DATA
        const val PROVIDER_NAME = "com.koenig.accountManager"
        const val DB_URI = "content://$PROVIDER_NAME/db"
        const val MASTER_URI = "content://$PROVIDER_NAME/master"

        val DB_CONTENT_URI = Uri.parse(DB_URI)
        val MASTER_CONTENT_URI = Uri.parse(MASTER_URI)

        // DB TABLE DATA
        const val id = "id"
        const val accountName = "accountName"
        const val password = "password"
        const val TABLE_NAME = "Accounts"

        // MASTER TABLE DATA
        const val MASTER_TABLE_NAME = "Master"
        const val masterPassword = "masterPassword"

        // DB DATA
        const val DATABASE_NAME = "AppData"
        const val DATABASE_VERSION = 5
        const val DbUriCode = 1
        const val MasterUriCode = 2

        var uriMatcher: UriMatcher? = null
        private val values: HashMap<String, String>? = null

        // CREATE TABLES
        const val CREATE_DB_TABLE = (
                " CREATE TABLE " + TABLE_NAME
                        + "(id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "accountName TEXT NOT NULL, "
                        + "password TEXT NOT NULL);")

        const val CREATE_MASTER_TABLE = (
                " CREATE TABLE " + MASTER_TABLE_NAME
                + "(id INTEGER PRIMARY KEY, "
                + "masterPassword TEXT NOT NULL);"
                )

        // INIT
        init
        {
            uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

            // ADD DB TABLE
            uriMatcher!!.addURI(PROVIDER_NAME, "db", DbUriCode)
            uriMatcher!!.addURI(PROVIDER_NAME, "db/*", DbUriCode)

            // ADD MASTER TABLE
            uriMatcher!!.addURI(PROVIDER_NAME, "master", MasterUriCode)
            uriMatcher!!.addURI(PROVIDER_NAME, "master/*", MasterUriCode)
        }
    }

    override fun onCreate(): Boolean
    {
        // GET THE DATABASE
        val context = context
        val dbHelper = DatabaseHelper(context)
        db = dbHelper.writableDatabase
        return db != null
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor?
    {
        var sortOrder = sortOrder
        val qb = SQLiteQueryBuilder()

        // SELECT THE REQUESTED TABLE
        when(uriMatcher!!.match(uri))
        {
            DbUriCode ->
            {
                qb.tables = TABLE_NAME
                qb.projectionMap = values
            }
            MasterUriCode ->
            {
                qb.tables = MASTER_TABLE_NAME
                qb.projectionMap = values
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }

        // SET THE SORT ORDER
        if (sortOrder == null || sortOrder === "")
        {
            sortOrder = id
        }

        // BUILD THE REQUESTED QUERY
        val cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder)

        // APPLY
        cursor.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    override fun getType(uri: Uri): String?
    {
        return when (uriMatcher!!.match(uri)) {
            DbUriCode -> "vnd.android.cursor.dir/db"
            MasterUriCode -> "vnd.android.cursor.dir/master"
            else -> throw  IllegalArgumentException("Unsupported URI: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri?
    {
        // SELECT THE REQUESTED TABLE
        when (uriMatcher!!.match(uri))
        {
            // INSERT IN ACCOUNT TABLE
            DbUriCode ->
            {
                val rowId = db!!.insert(TABLE_NAME, "", values)

                if(rowId > 0)
                {
                    val _uri = ContentUris.withAppendedId(DB_CONTENT_URI, rowId)
                    context!!.contentResolver.notifyChange(_uri, null)
                }
            }
            // INSERT IN MASTER TABLE
            MasterUriCode ->
            {
                val rowId = db!!.insert(MASTER_TABLE_NAME, "", values)

                if(rowId > 0)
                {
                    val _uri = ContentUris.withAppendedId(MASTER_CONTENT_URI, rowId)
                    context!!.contentResolver.notifyChange(_uri, null)
                }
            }
            else ->
            {
                throw SQLException("Failed to insert row into " + uri)
            }
        }
        return uri
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int
    {
        var amountDeletedRows = 0

        // DELETE ALL SELECTED ROWS
        amountDeletedRows = when(uriMatcher!!.match(uri)) {
            DbUriCode -> db!!.delete(TABLE_NAME, selection, selectionArgs)
            else -> throw IllegalArgumentException("Unkown URI $uri")
        }

        context!!.contentResolver.notifyChange(uri, null)
        return amountDeletedRows
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int
    {
        var amountUpdatedRows = 0

        // UPDATE ALL SELECTED ROWS
        amountUpdatedRows = when (uriMatcher!!.match(uri)) {
            DbUriCode -> db!!.update(TABLE_NAME, values, selection, selectionArgs)
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }

        context!!.contentResolver.notifyChange(uri, null)
        return amountUpdatedRows
    }

    // DATABASE HELPER
    var db: SQLiteDatabase? = null

    class DatabaseHelper
    internal constructor(context: Context?) : SQLiteOpenHelper(
        context,
        DATABASE_NAME,
        null,
        DATABASE_VERSION)
    {
        // CREATING DB AND MASTER TABLE IN DATABASE
        override fun onCreate(db: SQLiteDatabase)
        {
            db.execSQL(CREATE_DB_TABLE)
            db.execSQL(CREATE_MASTER_TABLE)
        }

        override fun onUpgrade(
            db: SQLiteDatabase,
            oldVersion: Int,
            newVersion: Int)
        {
            // DROP SAME TABLES IF EXISTING
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            db.execSQL("DROP TABLE IF EXISTS $MASTER_TABLE_NAME")
            onCreate(db)
        }
    }
}