package com.example.e_toma;

public class Participante implements Comparable<Participante>{
	
	public String nome;
	public String tag;
	public String fotoPath;
	public long rowId;
	
	public Participante(String n, String t, String f){
		nome = n;
		tag = t;
		fotoPath = f;
	}

	@Override
	public int compareTo(Participante another) {
		return this.nome.compareTo(another.nome);
	}
}
