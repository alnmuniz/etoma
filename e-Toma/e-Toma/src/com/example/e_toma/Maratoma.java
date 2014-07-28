package com.example.e_toma;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Maratoma implements Comparable<Maratoma> {

	public String nomeEvento;
	public Date dataEvento;
	public long rowId;

	private HashMap<Participante, List<MedicaoBafometro>> medicoesOrdemValor = new HashMap<Participante, List<MedicaoBafometro>>();
	private HashMap<Participante, List<MedicaoBafometro>> medicoesOrdemData = new HashMap<Participante, List<MedicaoBafometro>>();

	public List<ItemRanking> ranking = new ArrayList<ItemRanking>();
	private HashMap<Participante, ItemRanking> rankingParticipante = new HashMap<Participante, ItemRanking>();

	private HashMap<Participante,File> screenshots = new HashMap<Participante, File>();
	
	public Maratoma(String n, Date d) {
		nomeEvento = n;
		dataEvento = d;
	}

	@Override
	public int compareTo(Maratoma another) {
		if (this.dataEvento.before(another.dataEvento)) {
			return -1;
		} else if (this.dataEvento.after(another.dataEvento)) {
			return 1;
		}
		return 0;
	}

	public void registrarMedicao(Participante p, MedicaoBafometro mb) {

		if (p == null || mb == null) {
			return;
		}

		// por ordem de data
		List<MedicaoBafometro> medicoesData = medicoesOrdemData.get(p);

		if (medicoesData == null) {
			medicoesData = new ArrayList<MedicaoBafometro>();
		}

		medicoesData.add(mb);

		medicoesOrdemData.put(p, medicoesData);

		// por ordem de valor
		List<MedicaoBafometro> medicoesValor = medicoesOrdemValor.get(p);

		if (medicoesValor == null) {
			medicoesValor = new ArrayList<MedicaoBafometro>();
		}

		medicoesValor.add(mb);
		Collections.sort(medicoesValor);

		medicoesOrdemValor.put(p, medicoesValor);

		atualizarRanking();

	}

	private void atualizarRanking() {

		ranking.clear();

		for (Participante p : medicoesOrdemValor.keySet()) {

			ArrayList<MedicaoBafometro> medicoesValor = (ArrayList<MedicaoBafometro>) medicoesOrdemValor
					.get(p);
			
			//--Ranking agora vai ser calculado com base apenas no maior valor obtido e não mais pela diferença.
			
			//ArrayList<MedicaoBafometro> medicoesData = (ArrayList<MedicaoBafometro>) medicoesOrdemData.get(p);

			//int vini = medicoesData.get(0).valorMedicao; //primeira medição do participante
			//int vmax = medicoesValor.get(medicoesValor.size() - 1).valorMedicao; //maior valor obtido pelo participante
			
			
			//--Ranking agora pela média! 16/07/2014
			//
			
			int vmedia = 0;
			
			for (MedicaoBafometro medicaoBafometro : medicoesValor) {
				vmedia += medicaoBafometro.valorMedicao;						
			}
			
			if (medicoesValor.size()>0){
				vmedia = vmedia/medicoesValor.size();
			}

			ItemRanking ir = new ItemRanking();

			ir.p = p;
			ir.valor = vmedia;//vmax; // - vini;

			ranking.add(ir);
		}

		Collections.sort(ranking);

		rankingParticipante.clear();

		int posicao = 0;
		int valorAnterior = -1;

		for (ItemRanking ir : ranking) {

			if (ir.valor != valorAnterior) {
				posicao++;
			}

			ir.posicao = posicao;
			valorAnterior = ir.valor;

			rankingParticipante.put(ir.p, ir);
		}
	}

	public int obterPosicao(Participante p) {
		if (rankingParticipante.containsKey(p)) {
			return rankingParticipante.get(p).posicao;
		} else {
			return 0;
		}
	}

	public List<MedicaoBafometro> obterMedicoesData(Participante p) {
		if (medicoesOrdemData.containsKey(p)) {
			return medicoesOrdemData.get(p);
		} else {
			return new ArrayList<MedicaoBafometro>();
		}

	}
	
	public void alterarParticipante(Participante anterior, Participante novo){
		
		if (medicoesOrdemData.containsKey(anterior)){
			medicoesOrdemData.put(novo, medicoesOrdemData.get(anterior));
			medicoesOrdemData.remove(anterior);
		}
		
		if(medicoesOrdemValor.containsKey(anterior)){
			medicoesOrdemValor.put(novo, medicoesOrdemValor.get(anterior));
			medicoesOrdemValor.remove(anterior);
		}
		
		atualizarRanking();
		
	}

	public void excluirParticipante(Participante p){
		
		if (medicoesOrdemData.containsKey(p)){
			medicoesOrdemData.remove(p);
		}
		
		if(medicoesOrdemValor.containsKey(p)){
			medicoesOrdemValor.remove(p);
		}
		
		atualizarRanking();
		
	}
	
	public void setListaMedicoesParticipante(Participante p, ArrayList<MedicaoBafometro> medicoes){		
		medicoesOrdemData.put(p, medicoes);
		
		@SuppressWarnings("unchecked")
		ArrayList<MedicaoBafometro> listaValor = (ArrayList<MedicaoBafometro>) medicoes.clone();		
		Collections.sort(listaValor);		
		
		medicoesOrdemValor.put(p, listaValor);
		
		if (medicoes.size()>0){
			atualizarRanking();
		}
	}
	
	
	public void atualizarScreenshot(Participante p , File f){
		
		if (screenshots.containsKey(p)){
			
			try {
				File old = screenshots.get(p);				
				old.delete();
				screenshots.remove(p);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		screenshots.put(p, f);
		
	}

	
}
