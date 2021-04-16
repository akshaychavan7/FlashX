package com.akshaychavan.flashx.utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.akshaychavan.flashx.pojo.CardPojo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Akshay Chavan on 11,April,2021
 */
public class MyDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "FlashXDatabase.db";
    public static final String DB_LOCATION = "/data/data/com.akshaychavan.flashx/databases/";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "Words_List";

    //            "_id"
//            "Deck_Name"
//            "Word"
//            "Definition"
//            "Class"
//            "Synonyms"
//            "Examples"
//            "Mnemonic"
//            "Image_URL"
//            "Last_Five_Scores"
//            "Score"
    private static final String COLUMN_DECKNAME = "Deck_Name";
    private static final String COLUMN_WORD = "Word";
    private static final String COLUMN_DEFINITION = "Definition";
    private static final String COLUMN_CLASS = "Class";
    private static final String COLUMN_SYNONYMS = "Synonyms";
    private static final String COLUMN_EXAMPLES = "Examples";
    private static final String COLUMN_MNEMONIC = "Mnemonic";
    private static final String COLUMN_IMAGE_URL = "Image_URL";
    private static final String COLUMN_LAST_FIVE_SCORES = "Last_Five_Scores";
    private static final String COLUMN_SCORE = "Score";
    private final String TAG = "MyDatabaseHelper";
    private SQLiteDatabase myDataBase;
    private Context context;

    public static MyDatabaseHelper mInstance = null;

    public MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }


    public static synchronized MyDatabaseHelper getInstance(Context context) {
        if(mInstance==null) {
            mInstance = new MyDatabaseHelper(context);
        }
        return mInstance;
    }

    public static String getTableName() {
        return TABLE_NAME;
    }

    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();
        if (dbExist) {
        } else {
            this.getWritableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            String myPath = DB_LOCATION + DATABASE_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        } catch (SQLiteException e) {
        }
        if (checkDB != null) {
            checkDB.close();
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }

    private void copyDataBase() throws IOException {
        InputStream myInput = context.getAssets().open(DATABASE_NAME);
        String outFileName = DB_LOCATION + DATABASE_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[10];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public void openDataBase() throws SQLException {
        String myPath = DB_LOCATION + DATABASE_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {
        if (myDataBase != null)
            myDataBase.close();
        super.close();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion)
            try {
                copyDataBase();
            } catch (IOException e) {
                e.printStackTrace();

            }
    }

    public void fetchDeckCards(String deckName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

//        cv.put();
    }


//            "_id"
//            "Deck_Name"
//            "Word"
//            "Definition"
//            "Class"
//            "Synonyms"
//            "Examples"
//            "Mnemonic"
//            "Image_URL"
//            "Last_Five_Scores"
//            "Score"

    public void addCardToDeck( String deckName, CardPojo wordDetails) {
        //SQLiteDatabase db = this.getWritableDatabase();
//        openDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_DECKNAME, deckName);
        cv.put(COLUMN_WORD, wordDetails.getWord());
        cv.put(COLUMN_CLASS, wordDetails.getClass_());
        cv.put(COLUMN_DEFINITION, wordDetails.getMeaning());
        cv.put(COLUMN_SYNONYMS, wordDetails.getSynonyms());
        cv.put(COLUMN_EXAMPLES, wordDetails.getExample());
        cv.put(COLUMN_MNEMONIC, wordDetails.getMnemonic());
        cv.put(COLUMN_IMAGE_URL, wordDetails.getImageURL());
        cv.put(COLUMN_LAST_FIVE_SCORES, "0,0,0,1,1");
        cv.put(COLUMN_SCORE, 2);

        if(!myDataBase.isOpen()) {
            openDataBase();
        }

        long result = myDataBase.insert(TABLE_NAME, null, cv);

        if (result == -1) {
            Toast.makeText(context, "Failed to add card", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show();
        }

        closeDatabase();
    }


        public void closeDatabase() {
        if (myDataBase != null) {
            myDataBase.close();
        }
    }

    public SQLiteDatabase getDatabase() {
        if(myDataBase!=null)
            return myDataBase;
        Log.e(TAG, "ERROR! >>> myDatabase hasn't been initialized yet!");
        return null;
    }

//    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
//        return myDataBase.query("PLEASE CHANGE TO YOUR TABLE NAME", null, null, null, null, null, null);
//    }


//    @Override
//    public void onCreate(SQLiteDatabase db) {
//
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
////        db.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME);
////        onCreate(db);
//    }
//
//
//    // NOTE other database operations
//
//
//    public void openDatabase() {
//        String dbPath = context.getDatabasePath(DATABASE_NAME).getPath();
//        Log.e(TAG, "DB Path>>"+ dbPath);
//
//        if (mDatabase != null && mDatabase.isOpen()) {
//            Log.e(TAG, "Database not opened");
//            return;
//        }
//
//        mDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
//        Cursor c = mDatabase.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
//
//        if (c.moveToFirst()) {
//            while ( !c.isAfterLast() ) {
//                Log.e(TAG, "Table name>> "+c.getString( c.getColumnIndex("name")) );
//                c.moveToNext();
//            }
//        }
//
//        Log.e(TAG, "Database opened");
//    }
//
//    public void closeDatabase() {
//        if (mDatabase != null) {
//            mDatabase.close();
//        }
//    }
//
//
//    public void fetchDeckCards(String deckName) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues cv = new ContentValues();
//
////        cv.put();
//    }
//
//    public void addCardToDeck(String deckName, CardPojo wordDetails) {
//        //SQLiteDatabase db = this.getWritableDatabase();
//        openDatabase();
//        ContentValues cv = new ContentValues();
//
//        cv.put(COLUMN_DECKNAME, deckName);
//        cv.put(COLUMN_WORD, wordDetails.getWord());
//        cv.put(COLUMN_CLASS, wordDetails.getClass_());
//        cv.put(COLUMN_DEFINITION, wordDetails.getMeaning());
//        cv.put(COLUMN_SYNONYMS, wordDetails.getSynonyms());
//        cv.put(COLUMN_EXAMPLES, wordDetails.getExample());
//        cv.put(COLUMN_MNEMONIC, wordDetails.getMnemonic());
//        cv.put(COLUMN_IMAGE_URL, wordDetails.getImageURL());
//        cv.put(COLUMN_IS_MASTERED, "No");
//
//        long result = mDatabase.insert(TABLE_NAME, null, cv);
//
//        if (result == -1) {
//            Toast.makeText(context, "Failed to add card", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show();
//        }
//
//        closeDatabase();
//    }


}
