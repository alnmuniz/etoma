package com.example.e_toma;

import java.util.Date;

public class MedicaoBafometro implements Comparable<MedicaoBafometro>{
	
	public Maratoma m;
	public Participante p;
	public Date dataHoraMedicao;
	public int valorMedicao;
	public long rowId;
	
	
	@Override
	public int compareTo(MedicaoBafometro another) {
		if (this.valorMedicao > another.valorMedicao){
			return 1;
		} else if (this.valorMedicao < another.valorMedicao){
			return -1;
		}
		return 0;
	}
	

}
