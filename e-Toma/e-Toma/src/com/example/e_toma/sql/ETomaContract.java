package com.example.e_toma.sql;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.example.e_toma.Maratoma;
import com.example.e_toma.MedicaoBafometro;
import com.example.e_toma.Participante;
import com.example.e_toma.Principal;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public final class ETomaContract {
	// To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public ETomaContract() {}

    /* Inner class that defines the table contents */
    public static abstract class TMaratoma implements BaseColumns {
        public static final String TABLE_NAME = "maratoma";
        public static final String COL_NOME_EVENTO = "nomeEvento";
        public static final String COL_DATA_EVENTO = "dataEvento";
    }
    
    public static abstract class TParticipante implements BaseColumns {
        public static final String TABLE_NAME = "participante";
        public static final String COL_NOME_PARTICIPANTE = "nome";
        public static final String COL_FOTO_PATH = "fotoPath";
        public static final String COL_TAG = "tag";
    }
    
    /*public static abstract class PA implements BaseColumns {
        public static final String TABLE_NAME = "pa";
        public static final String COL_NOME_PA = "nome_pa";
        public static final String COL_LAT = "lat";
        public static final String COL_LONG = "long";
    }*/
    
    public static abstract class TMedicaoBafometro implements BaseColumns {
        public static final String TABLE_NAME = "medicao_bafometro";
        public static final String COL_FKID_MARATOMA = "fkid_maratoma";
        public static final String COL_FKID_PARTICIPANTE = "fkid_participante";
        //public static final String COL_FKID_PA = "FKID_PA";
        public static final String COL_DATA_HORA_MEDICAO = "dataHoraMedicao";
        public static final String COL_VALOR_MEDICAO = "valorMedicao";
    }
    
    /*public static abstract class Ranking implements BaseColumns {
        public static final String TABLE_NAME = "ranking";
        public static final String COL_FKID_MARATOMA = "FKID_MARATOMA";
        public static final String COL_FKID_PARTICIPANTE = "FKID_PARTICIPANTE";
        public static final String COL_POSICAO = "POSICAO";
        public static final String COL_VALOR_INICIAL = "VALOR_INICIAL";
        public static final String COL_VALOR_MAXIMO = "VALOR_MAXIMO";
    }*/
    


    
    
    
    

}
