package com.example.e_toma.sql;

import com.example.e_toma.sql.ETomaContract.TMaratoma;
import com.example.e_toma.sql.ETomaContract.TMedicaoBafometro;
import com.example.e_toma.sql.ETomaContract.TParticipante;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ETomaDbHelper extends SQLiteOpenHelper {
	// If you change the database schema, you must increment the database
	// version.
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "EToma.db";

	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";
	private static final String SQL_CREATE_MARATOMA = "CREATE TABLE "
			+ TMaratoma.TABLE_NAME + " (" + TMaratoma._ID
			+ " INTEGER PRIMARY KEY," + TMaratoma.COL_NOME_EVENTO + TEXT_TYPE
			+ COMMA_SEP + TMaratoma.COL_DATA_EVENTO + TEXT_TYPE + " )";
	private static final String SQL_CREATE_PARTICIPANTE = "CREATE TABLE "
			+ TParticipante.TABLE_NAME + " (" + TParticipante._ID
			+ " INTEGER PRIMARY KEY," + TParticipante.COL_NOME_PARTICIPANTE
			+ TEXT_TYPE + COMMA_SEP + TParticipante.COL_TAG + TEXT_TYPE
			+ COMMA_SEP + TParticipante.COL_FOTO_PATH + TEXT_TYPE + " )";
	private static final String SQL_CREATE_MEDICAO_BAFOMETRO = "CREATE TABLE "
			+ TMedicaoBafometro.TABLE_NAME + " (" + TMedicaoBafometro._ID
			+ " INTEGER PRIMARY KEY," + TMedicaoBafometro.COL_FKID_MARATOMA
			+ TEXT_TYPE + COMMA_SEP + TMedicaoBafometro.COL_FKID_PARTICIPANTE
			+ TEXT_TYPE + COMMA_SEP + TMedicaoBafometro.COL_DATA_HORA_MEDICAO
			+ TEXT_TYPE + COMMA_SEP + TMedicaoBafometro.COL_VALOR_MEDICAO
			+ TEXT_TYPE + " )";

	private static final String SQL_DELETE_MARATOMA = "DROP TABLE IF EXISTS "
			+ TMaratoma.TABLE_NAME;
	private static final String SQL_DELETE_PARTICIPANTE = "DROP TABLE IF EXISTS "
			+ TParticipante.TABLE_NAME;
	private static final String SQL_DELETE_MEDICAO_BAFOMETRO = "DROP TABLE IF EXISTS "
			+ TMedicaoBafometro.TABLE_NAME;

	public ETomaDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_MARATOMA);
		db.execSQL(SQL_CREATE_PARTICIPANTE);
		db.execSQL(SQL_CREATE_MEDICAO_BAFOMETRO);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade policy
		// is
		// to simply to discard the data and start over
		db.execSQL(SQL_DELETE_MEDICAO_BAFOMETRO);
		db.execSQL(SQL_DELETE_MARATOMA);
		db.execSQL(SQL_DELETE_PARTICIPANTE);
		onCreate(db);
	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}