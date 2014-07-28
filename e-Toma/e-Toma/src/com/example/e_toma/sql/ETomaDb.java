package com.example.e_toma.sql;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.e_toma.Maratoma;
import com.example.e_toma.MedicaoBafometro;
import com.example.e_toma.Participante;
import com.example.e_toma.sql.ETomaContract.TMaratoma;
import com.example.e_toma.sql.ETomaContract.TMedicaoBafometro;
import com.example.e_toma.sql.ETomaContract.TParticipante;

public class ETomaDb {
	
    private static final String FORMATO_DATA = "yyyyMMdd_HHmmss";
	ETomaDbHelper mDbHelper;
    
    public ETomaDb (Context context){
    	mDbHelper = new ETomaDbHelper(context);
    }
    
    public long insertMaratoma(Maratoma m){
    	
    	if (mDbHelper == null)
    		return 0;
    	
    	// Gets the data repository in write mode
    	SQLiteDatabase db = mDbHelper.getWritableDatabase();

    	// Create a new map of values, where column names are the keys
    	ContentValues values = new ContentValues();
    	values.put(TMaratoma.COL_NOME_EVENTO, m.nomeEvento);
    	values.put(TMaratoma.COL_DATA_EVENTO, formataDataString(m.dataEvento));

    	// Insert the new row, returning the primary key value of the new row
    	long newRowId;
    	newRowId = db.insert(
    			TMaratoma.TABLE_NAME,
    	        null,
    	         values);
    	m.rowId=newRowId;
    	
    	return newRowId;
    }
    
    public long insertParticipante(Participante p){
    	
    	if (mDbHelper == null)
    		return 0;
    	
    	// Gets the data repository in write mode
    	SQLiteDatabase db = mDbHelper.getWritableDatabase();

    	// Create a new map of values, where column names are the keys
    	ContentValues values = new ContentValues();
    	values.put(TParticipante.COL_NOME_PARTICIPANTE, p.nome);
    	values.put(TParticipante.COL_TAG, p.tag);
    	values.put(TParticipante.COL_FOTO_PATH, p.fotoPath);

    	// Insert the new row, returning the primary key value of the new row
    	long newRowId;
    	newRowId = db.insert(
    			TParticipante.TABLE_NAME,
    	        null,
    	         values);
    	p.rowId=newRowId;
    	
    	return newRowId;
    }
    
    public long insertMedicaoBafometro(MedicaoBafometro mb){
    	
    	if (mDbHelper == null)
    		return 0;
    	
    	// Gets the data repository in write mode
    	SQLiteDatabase db = mDbHelper.getWritableDatabase();

    	// Create a new map of values, where column names are the keys
    	ContentValues values = new ContentValues();
    	values.put(TMedicaoBafometro.COL_FKID_MARATOMA, mb.m.rowId);
    	values.put(TMedicaoBafometro.COL_FKID_PARTICIPANTE, mb.p.rowId);
    	values.put(TMedicaoBafometro.COL_DATA_HORA_MEDICAO, formataDataString(mb.dataHoraMedicao));
    	values.put(TMedicaoBafometro.COL_VALOR_MEDICAO, mb.valorMedicao);

    	// Insert the new row, returning the primary key value of the new row
    	long newRowId;
    	newRowId = db.insert(
    			TMedicaoBafometro.TABLE_NAME,
    	        null,
    	         values);
    	mb.rowId=newRowId;
    	
    	return newRowId;
    }	
    
    public ArrayList<Maratoma> obterMaratomas(){	    	
    	String table = TMaratoma.TABLE_NAME;
    	String[] projection = {
    			TMaratoma._ID,
    			TMaratoma.COL_NOME_EVENTO,
    			TMaratoma.COL_DATA_EVENTO
    	};
    	String sortOrder = TMaratoma.COL_DATA_EVENTO + " ASC";
    	
    	Cursor c = selectAll(table,projection,sortOrder);
    	
    	ArrayList<Maratoma> result = new ArrayList<Maratoma>();
    	
    	while (c.moveToNext()){
    		Maratoma m = new Maratoma(
    				c.getString(c.getColumnIndex(TMaratoma.COL_NOME_EVENTO)), 
    				formataStringData(c.getString(c.getColumnIndex(TMaratoma.COL_DATA_EVENTO))));
    		m.rowId = c.getLong(c.getColumnIndex(TMaratoma._ID));	    		
    		result.add(m);
    	}
    	
    	return result;
    	
    }
    
    public ArrayList<Participante> obterParticipantes(){
    	
    	ArrayList<Participante> result = new ArrayList<Participante>();
    	
    	String table = TParticipante.TABLE_NAME;
    	String[] projection = {
    			TParticipante._ID,
    			TParticipante.COL_NOME_PARTICIPANTE,
    			TParticipante.COL_TAG,
    			TParticipante.COL_FOTO_PATH
    	};
    	String sortOrder = TParticipante.COL_NOME_PARTICIPANTE + " ASC";
    	
    	Cursor c = selectAll(table,projection,sortOrder);
    	
    	while( c.moveToNext() ){
    		
    		Participante p = new Participante(
    				c.getString(c.getColumnIndex(TParticipante.COL_NOME_PARTICIPANTE)),
    				c.getString(c.getColumnIndex(TParticipante.COL_TAG)),
    				c.getString(c.getColumnIndex(TParticipante.COL_FOTO_PATH))
    				);
    		
    		p.rowId = c.getLong(
    			    c.getColumnIndex(TParticipante._ID)
    				);
    		
    		result.add(p);
    	}
    	
    	
    	return result;	    	
    }
    
    public ArrayList<MedicaoBafometro> obterMedicoesBafometro(Maratoma m, Participante p){
    	
    	ArrayList<MedicaoBafometro> result = new ArrayList<MedicaoBafometro>();
    	
    	String table = TMedicaoBafometro.TABLE_NAME;
    	String[] projection = {
    			TMedicaoBafometro._ID,
    			TMedicaoBafometro.COL_DATA_HORA_MEDICAO,
    			TMedicaoBafometro.COL_VALOR_MEDICAO
    	};
    	String sortOrder = TMedicaoBafometro.COL_DATA_HORA_MEDICAO + " ASC";
    	String where = TMedicaoBafometro.COL_FKID_MARATOMA +"="+m.rowId+" AND "+TMedicaoBafometro.COL_FKID_PARTICIPANTE+" = "+p.rowId;
    	
    	Cursor c = selectWhere(table, projection, sortOrder, where);
    	
    	while( c.moveToNext() ){
    		
    		MedicaoBafometro mb = new MedicaoBafometro();
    		
    		mb.m = m;
    		mb.p = p;
    		mb.dataHoraMedicao = formataStringData(c.getString(c.getColumnIndex(TMedicaoBafometro.COL_DATA_HORA_MEDICAO)));
    		mb.valorMedicao = Integer.valueOf(c.getString(c.getColumnIndex(TMedicaoBafometro.COL_VALOR_MEDICAO)));
    		
    		mb.rowId = c.getLong(
    			    c.getColumnIndex(TMedicaoBafometro._ID)
    				);
    		
    		result.add(mb);
    	}
    	
    	
    	return result;	
    	
    }
    
    public void excluirParticipante(Participante p){
    	
    	if (mDbHelper == null)
    		return;

    	// Gets the data repository in write mode
    	SQLiteDatabase db = mDbHelper.getWritableDatabase();

    	// apaga primeiro as medições.
    	// Define 'where' part of query.
    	String selection = TMedicaoBafometro.COL_FKID_PARTICIPANTE + " = ?";
    	// Specify arguments in placeholder order.
    	String[] selectionArgs = { String.valueOf(p.rowId) };
    	// Issue SQL statement.
    	db.delete(TMedicaoBafometro.TABLE_NAME, selection, selectionArgs);    	

    	// apaga em seguida o participante
    	// Define 'where' part of query.
    	selection = TParticipante._ID + " = ?";
    	// Issue SQL statement.
    	db.delete(TParticipante.TABLE_NAME, selection, selectionArgs);    	

    	
    }

    public void limparMedicoes(Participante p, Maratoma m){
    	
    	if (mDbHelper == null)
    		return;

    	// Gets the data repository in write mode
    	SQLiteDatabase db = mDbHelper.getWritableDatabase();

    	// apaga primeiro as medições.
    	// Define 'where' part of query.
    	String selection = TMedicaoBafometro.COL_FKID_MARATOMA +"= ? AND "+TMedicaoBafometro.COL_FKID_PARTICIPANTE+" = ? ";
    	// Specify arguments in placeholder order.
    	String[] selectionArgs = { String.valueOf(m.rowId), String.valueOf(p.rowId) };
    	// Issue SQL statement.
    	db.delete(TMedicaoBafometro.TABLE_NAME, selection, selectionArgs);    	

    	
    }

    
    public void excluirMaratoma(Maratoma m){
    	
    	if (mDbHelper == null)
    		return;

    	// Gets the data repository in write mode
    	SQLiteDatabase db = mDbHelper.getWritableDatabase();

    	// apaga primeiro as medições.
    	// Define 'where' part of query.
    	String selection = TMedicaoBafometro.COL_FKID_MARATOMA + " = ?";
    	// Specify arguments in placeholder order.
    	String[] selectionArgs = { String.valueOf(m.rowId) };
    	// Issue SQL statement.
    	db.delete(TMedicaoBafometro.TABLE_NAME, selection, selectionArgs);    	

    	// apaga em seguida a maratoma
    	// Define 'where' part of query.
    	selection = TMaratoma._ID + " = ?";
    	// Issue SQL statement.
    	db.delete(TMaratoma.TABLE_NAME, selection, selectionArgs);    	

    	
    }    
    
    public void alterarParticipante(Participante p){
    	
    	SQLiteDatabase db = mDbHelper.getReadableDatabase();

    	// New value for one column
    	ContentValues values = new ContentValues();
    	values.put(TParticipante.COL_NOME_PARTICIPANTE, p.nome);
    	values.put(TParticipante.COL_TAG, p.tag);
    	values.put(TParticipante.COL_FOTO_PATH, p.fotoPath);

    	// Which row to update, based on the ID
    	String selection = TParticipante._ID + " = ?";
    	String[] selectionArgs = { String.valueOf(p.rowId) };

    	db.update(
    	    TParticipante.TABLE_NAME,
    	    values,
    	    selection,
    	    selectionArgs);
    	
    }
    
    public void alterarMaratoma(Maratoma m){
    	
    	SQLiteDatabase db = mDbHelper.getReadableDatabase();

    	// New value for one column
    	ContentValues values = new ContentValues();
    	values.put(TMaratoma.COL_NOME_EVENTO, m.nomeEvento);
    	values.put(TMaratoma.COL_DATA_EVENTO, formataDataString(m.dataEvento));

    	// Which row to update, based on the ID
    	String selection = TMaratoma._ID + " = ?";
    	String[] selectionArgs = { String.valueOf(m.rowId) };

    	db.update(
    	    TMaratoma.TABLE_NAME,
    	    values,
    	    selection,
    	    selectionArgs);
    	
    }

    
    private Cursor selectAll(String table, String[] projection, String sortOrder){
    	
    	if (mDbHelper == null)
    		return null;
    	
    	SQLiteDatabase db = mDbHelper.getReadableDatabase();
    	
    	return db.query(
    			table,  // The table to query
    		    projection,                               // The columns to return
    		    null,                                // The columns for the WHERE clause
    		    null,                            // The values for the WHERE clause
    		    null,                                     // don't group the rows
    		    null,                                     // don't filter by row groups
    		    sortOrder                                 // The sort order
    		    );
    }

    private Cursor selectWhere(String table, String[] projection, String sortOrder, String where){

    	if (mDbHelper == null)
    		return null;
    	
    	SQLiteDatabase db = mDbHelper.getReadableDatabase();
    	
    	return db.query(
    			table,  // The table to query
    		    projection,                               // The columns to return
    		    where,                                // The columns for the WHERE clause
    		    null,                            // The values for the WHERE clause
    		    null,                                     // don't group the rows
    		    null,                                     // don't filter by row groups
    		    sortOrder                                 // The sort order
    		    );
    }
    
    
    @SuppressLint("SimpleDateFormat")
	private String formataDataString(Date data){
    	return new SimpleDateFormat(FORMATO_DATA).format(data);
    }
    
    @SuppressLint("SimpleDateFormat")
	private Date formataStringData(String data){
    	try {
			return new SimpleDateFormat(FORMATO_DATA).parse(data);
		} catch (ParseException e) {
			return null;
		}
    }
}