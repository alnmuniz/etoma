package com.example.e_toma;

public class ItemRanking implements Comparable<ItemRanking>{
	public Participante p;
	public int valor;
	public int posicao;

	
	@Override
	public int compareTo(ItemRanking another) {
		if (this.valor < another.valor){
			return 1;
		} else if (this.valor > another.valor){
			return -1;
		}
		return 0;
	}	
}
