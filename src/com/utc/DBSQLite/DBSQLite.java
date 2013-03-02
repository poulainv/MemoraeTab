package com.utc.DBSQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBSQLite extends SQLiteOpenHelper{

	private static volatile DBSQLite instance= null;
	
	private static final String TABLE_POSTIT = "POSTIT";
	private static final String COL_ID = "ID";
	private static final String COL_TEXT = "TEXT";
	private static final String COL_TAGS = "TAGS";
	private static final String COL_TIME = "TIME";
	private static final String COL_SYNCHRO = "SYNCHRO";
	 
 
	private static final String CREATE_BDD = "CREATE TABLE " + TABLE_POSTIT + " ("
	+ COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_TEXT + " TEXT NOT NULL, "
	+ COL_TIME + " TEXT NOT NULL, " + COL_TAGS + " TEXT, "+COL_SYNCHRO+" VARCHAR(10) );";
 
	private DBSQLite(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}
 
	static public DBSQLite getInstance(Context context, String name, CursorFactory factory, int version){
		if (DBSQLite.instance == null) {
            synchronized(DBSQLite.class) {
              if (DBSQLite.instance == null) {
            	  DBSQLite.instance = new DBSQLite(context,name,factory,version);
              }
            }
         }
         return DBSQLite.instance;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		//on crée la table à partir de la requête écrite dans la variable CREATE_BDD
		db.execSQL(CREATE_BDD);
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE " + TABLE_POSTIT + ";");
		onCreate(db);
	}
	
}
