package com.example.e_toma;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;

import com.example.e_toma.sql.ETomaDb;

public class ETomaControle {

	private static ETomaDb mETomaDb;

	private static HashMap<String, Participante> participantesPorTag = new HashMap<String, Participante>();
	private static HashMap<String, Participante> participantesPorNome = new HashMap<String, Participante>();

	private static List<Maratoma> maratomas;

	public static void inicializar(Context c) {

		if (mETomaDb == null) {
			mETomaDb = new ETomaDb(c);
		}

		maratomas = mETomaDb.obterMaratomas();

		ArrayList<Participante> participantes = mETomaDb.obterParticipantes();

		for (Participante participante : participantes) {
			participantesPorNome.put(participante.nome, participante);
			if (participante.tag != null) {
				participantesPorTag.put(participante.tag, participante);
			}
		}

		inicializarMedicoes();
	}

	private static void inicializarMedicoes() {
		Maratoma m = obterMaratoma();
		for (Participante participante : participantesPorNome.values()) {
			if (m != null) {
				ArrayList<MedicaoBafometro> medicoes = mETomaDb
						.obterMedicoesBafometro(m, participante);
				if (medicoes.size() > 0) {
					m.setListaMedicoesParticipante(participante, medicoes);
				}
			}
		}
	}

	public static void novoParticipante(Participante p) {

		p.rowId = mETomaDb.insertParticipante(p);

		if (participantesPorNome.containsKey(p.nome)) {
			participantesPorNome.remove(p.nome);
		}

		participantesPorNome.put(p.nome, p);

		if (p.tag != null) {
			if (participantesPorTag.containsKey(p.tag)) {
				participantesPorTag.remove(p.tag);
			}
			participantesPorTag.put(p.tag, p);
		}
	}

	public static void novoParticipanteTransporTag(Participante novoP,
			Participante velhoP) {

		novoP.rowId = mETomaDb.insertParticipante(novoP);

		participantesPorNome.put(novoP.nome, novoP);

		velhoP.tag = null;

		mETomaDb.alterarParticipante(velhoP);

		participantesPorTag.remove(novoP.tag);
		participantesPorTag.put(novoP.tag, novoP);

	}

	public static void alterarParticipante(Participante anterior,
			Participante novo) {

		participantesPorNome.remove(anterior.nome);

		if (anterior.tag != null) {
			participantesPorTag.remove(anterior.tag);
		}

		anterior.nome = novo.nome;
		anterior.tag = novo.tag;
		anterior.fotoPath = novo.fotoPath;

		mETomaDb.alterarParticipante(anterior);

		participantesPorNome.put(anterior.nome, anterior);
		if (anterior.tag != null) {
			participantesPorTag.put(anterior.tag, anterior);
		}
	}

	public static void alterarMaratoma(Maratoma anterior, Maratoma novo) {

		anterior.nomeEvento = novo.nomeEvento;
		anterior.dataEvento = novo.dataEvento;

		mETomaDb.alterarMaratoma(anterior);

	}

	public static void alterarParticipanteTransporTag(Participante anterior,
			Participante novo, Participante outro) {

		participantesPorTag.remove(outro.tag); // remove o outro da lista por
												// tag
		outro.tag = null; // limpa a tag do outro

		mETomaDb.alterarParticipante(outro);

		participantesPorNome.remove(anterior.nome); // remove a versão anterior

		if (anterior.tag != null) {
			participantesPorTag.remove(anterior.tag); // remove a versão
														// anterior
		}

		anterior.nome = novo.nome;
		anterior.tag = novo.tag;
		anterior.fotoPath = novo.fotoPath;

		mETomaDb.alterarParticipante(anterior);

		participantesPorNome.put(anterior.nome, anterior);
		if (anterior.tag != null) {
			participantesPorTag.put(anterior.tag, anterior);
		}
	}

	public static void excluirParticipante(Participante p) {

		if (participantesPorNome.containsKey(p.nome)) {
			participantesPorNome.remove(p.nome);
		}

		if (p.tag != null && participantesPorTag.containsKey(p.tag)) {
			participantesPorTag.remove(p.tag);
		}

		mETomaDb.excluirParticipante(p);
	}
	
	public static void limparMedicoes(Participante p, Maratoma m) {
		
		mETomaDb.limparMedicoes(p, m);
		
	}

	public static void excluirMaratoma(Maratoma m) {

		maratomas.remove(m);
		
		inicializarMedicoes();

		mETomaDb.excluirMaratoma(m);
	}

	public static Participante obterParticipantePorTag(String tag) {
		if (participantesPorTag.containsKey(tag)) {
			return participantesPorTag.get(tag);
		} else {
			return null;
		}
	}

	public static Participante obterParticipantePorNome(String nome) {
		if (participantesPorNome.containsKey(nome)) {
			return participantesPorNome.get(nome);
		} else {
			return null;
		}
	}

	public static Collection<Participante> obterParticipanteListAll() {
		if (participantesPorNome.isEmpty()) {
			return null;
		} else {

			ArrayList<Participante> result = new ArrayList<Participante>(
					participantesPorNome.values());

			Collections.sort(result);

			return result;
		}
	}

	public static Collection<Maratoma> obterMaratomasListAll() {
		if (maratomas == null) {
			return null;
		}

		if (maratomas.isEmpty()) {
			return null;
		} else {
			return maratomas;
		}
	}

	public static void salvarMaratoma(Maratoma m) {
		if (m != null) {
			m.rowId = mETomaDb.insertMaratoma(m);
			maratomas.add(m);
			Collections.sort(maratomas);
		}
	}

	public static Maratoma obterMaratoma() {

		if (maratomas == null) {
			return null;
		}

		if (maratomas.isEmpty()) {
			return null;
		} else {
			return maratomas.get(maratomas.size() - 1);
		}

	}

	public static void salvarMedicaoBafometro(MedicaoBafometro mb) {
		mb.rowId = mETomaDb.insertMedicaoBafometro(mb);
	}

	@SuppressLint("SimpleDateFormat")
	public static Maratoma obterMaratomaPorNomeData(String nomeData) {
		if (maratomas == null) {
			return null;
		}

		if (maratomas.isEmpty()) {
			return null;
		} else {
			for (Maratoma m : maratomas) {
				String dataStr = new SimpleDateFormat("dd/MM/yyyy")
						.format(m.dataEvento);
				if ((m.nomeEvento + "\n" + dataStr).equals(nomeData)) {
					return m;
				}
			}
			return null;
		}
	}

}
