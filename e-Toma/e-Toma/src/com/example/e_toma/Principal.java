package com.example.e_toma;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
public class Principal extends ActionBarActivity implements
		ActionBar.TabListener {
	// private static final String TAG = "ETomaPrincipal";

	public static final int QTD_TABS = 4;
	public static final int TAB_PRINCIPAL = 0;
	public static final int TAB_PARTICIPANTES = 1;
	public static final int TAB_CADASTRO = 2;
	public static final int TAB_MONITOR = 3;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	ShareActionProvider mShareActionProvider;

	public static Camera mCamera;
	
	// Dados do participante
	private static TextView mTagId;
	private static ImageView mFoto;
	private static TextView mNomeParticipante;
	private static TextView mPosicaoParticipante;
	private static TextView mTituloMaratoma;
	private static TextView mRanking;
	private static FrameLayout mLayoutGrafico;

	// Dados da medição
	private static TextView mValorBafometro;

	// Dados do monitor
	private static TextView mDumpTextView;
	private static ScrollView mScrollView;

	// Dados do cadastro do participante
	private static EditText mTagParticipante;
	private static EditText mCadNomeParticipante;
	private static ImageView mCadFoto;
	private static String mCurrentPhotoPath;
	private static Button mCadNovoParticipante;
	private static Button mCadLimparMedicoes;
	private static Button mCadSalvarParticipante;

	// Dados cadastro Maratoma
	private static EditText mCadNomeEvento;
	private static TextView mCadDataEvento;
	private static Button mCadNovaMaratoma;

	// Path para screenshot para compartilhar.
	private static String mCurrentScreenshotPath;

	// Lista de participantes
	private static TextView mTotalParticipantes;
	private static ListView mListaParticipantes;
	private static ArrayAdapter<String> mParticipantesAdapter;

	// Lista de Maratomas
	private static TextView mTotalMaratomas;
	private static ListView mListaMaratomas;
	private static ArrayAdapter<String> mMaratomasAdapter;

	// Maratoma corrente
	private static Maratoma mMaratoma;
	private static Maratoma mMaratomaEdicao;
	private static Maratoma mMaratomaExclusao;

	// Participante corrente
	private static Participante mParticipante;
	private static Participante mParticipanteEdicao;
	private static Participante mParticipanteExclusao;

	// private ETomaSerial mSerial;
	private ETomaBT mBT;
	private static String resultInicializar = new String("");
	private int errosLeitura = 0;
	private static final int maxErrosLeitura = 3;
	private static String ultimaMsg = "MARATOMA";
	private static boolean btInicializado = false;

	// INTENT RESULT CODES
	static final int REQUEST_IMAGE_CAPTURE = 1;
	static final int IMAGE_PICKER_SELECT = 2;
	static final int REQUEST_ENABLE_BT = 3;

	ProgressDialog mProgressDialog;

	public static Principal autoRef;

	private static boolean dadosInicializados = false;

	/*
	 * private final ETomaSerial.ETomaSerialListener mListener = new
	 * ETomaSerial.ETomaSerialListener() {
	 * 
	 * @Override public void lerMensagem(String msg) {
	 * tratarMensagemRecebida(msg); }
	 * 
	 * };
	 */

	private final ETomaBT.ETomaBTListener mBTListener = new ETomaBT.ETomaBTListener() {

		@Override
		public void lerMensagem(String msg) {
			tratarMensagemRecebida(msg);
		}

	};

	private static ETomaEstados estadoAtual = ETomaEstados.MEDICAO_TAG;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_principal);

		// Set up the action bar.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}

		// mSerial = new ETomaSerial(this,mListener);
		mBT = new ETomaBT(this, mBTListener);

		autoRef = this;

		if (!dadosInicializados) {
			ETomaControle.inicializar(getApplicationContext());
			dadosInicializados = true;
		}

	}

	/*
	 * @Override protected void onPause() { super.onPause(); //
	 * mSerial.finalizar(); reqReset(null); mBT.finalizar(); finish(); }
	 */

	@Override
	protected void onDestroy() {
		// mSerial.finalizar();
		reqReset(null);
		mBT.finalizar();
		btInicializado = false;
		// finish();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!btInicializado) {
			resultInicializar = mBT.inicializar();
			if (resultInicializar.equals(ETomaBT.BT_STATUS_OK)){
				btInicializado = true;
			} else {
				startActivityForResult(new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
				btInicializado = false;
			}
		}
		//
		// reqTag(null);
	}

	private void reconectar() {
		if (btInicializado){
			mBT.finalizar();
		}
		resultInicializar = mBT.inicializar();
		if (resultInicializar.equals(ETomaBT.BT_STATUS_OK)){
			btInicializado = true;
			if (estadoAtual == ETomaEstados.MEDICAO_TAG) {
				reqTag(null);
			}
		} else {
			startActivityForResult(new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
			btInicializado = false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.principal, menu);

		// Locate MenuItem with ShareActionProvider
		MenuItem item = menu.findItem(R.id.menu_item_share);

		// Fetch and store ShareActionProvider
		mShareActionProvider = (ShareActionProvider) MenuItemCompat
				.getActionProvider(item);
		// Intent intent = new Intent(Intent.ACTION_SEND);
		// intent.setType("text/plain");
		// mShareActionProvider.setShareIntent(intent);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		/*
		 * case R.id.action_settings: Toast toast =
		 * Toast.makeText(getApplicationContext(), R.string.action_settings,
		 * Toast.LENGTH_SHORT); toast.show(); return true;
		 */
		case R.id.action_reconect:
			reconectar();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());

		switch (tab.getPosition()) {
		case TAB_PRINCIPAL: // PRINCIPAL
			estadoAtual = ETomaEstados.MEDICAO_TAG;
			exibirDadosMaratoma();
			reqReset(null);
			reqTag(null);
			break;
		case TAB_CADASTRO: // CADASTRO
			estadoAtual = ETomaEstados.MARATOMA_NOVA;
			reqReset(null);
			mostrarListaMaratomas();
			break;
		case TAB_MONITOR: // MONITOR
			estadoAtual = ETomaEstados.MONITOR;
			reqReset(null);
			break;
		case TAB_PARTICIPANTES: // LISTA_PARTICIPANTES
			limparCadastroParticipante(null);
			reqReset(null);
			mostrarListaParticipantes();
			break;
		default:
			estadoAtual = ETomaEstados.MEDICAO_TAG;
			reqReset(null);
			break;
		}

	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {

		/*
		 * switch(tab.getPosition()){ case 0: reqReset(null); break; default:
		 * break; }
		 */
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		PrincipalFragment mPrin;
		CadastroFragment mCad;
		MonitorFragment mMon;
		ParticipantesFragment mPar;

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			switch (position) {
			case TAB_PRINCIPAL:
				if (mPrin == null) {
					mPrin = PrincipalFragment.newInstance(position + 1);
				}
				return mPrin;
			case TAB_CADASTRO:
				if (mCad == null) {
					mCad = CadastroFragment.newInstance(position + 1);
				}
				return mCad;
			case TAB_MONITOR:
				if (mMon == null) {
					mMon = MonitorFragment.newInstance(position + 1);
				}
				return mMon;
			case TAB_PARTICIPANTES:
				if (mPar == null) {
					mPar = ParticipantesFragment.newInstance(position + 1);
				}
				return mPar;
			default:
				if (mPrin == null) {
					mPrin = PrincipalFragment.newInstance(position + 1);
				}
				return mPrin;
			}
		}

		@Override
		public int getCount() {
			return QTD_TABS;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case TAB_PRINCIPAL:
				return getString(R.string.title_pricinpal).toUpperCase(l);
			case TAB_CADASTRO:
				return getString(R.string.title_cadastro).toUpperCase(l);
			case TAB_MONITOR:
				return getString(R.string.title_monitor).toUpperCase(l);
			case TAB_PARTICIPANTES:
				return getString(R.string.title_participantes).toUpperCase(l);
			}
			return null;
		}
	}

	public void reqTag(View view) {
		String reqStr = new String("");
		try {
			// estadoAtual = ETomaEstados.MEDICAO_TAG;
			// reqStr = mSerial.enviarMsg("1");
			reqStr = mBT.enviarMsg("1");
			mostrarMsg(reqStr);
		} catch (Exception e) {
			e.printStackTrace();
			mostrarMsg("reqTag erro " + e.getMessage());
		}
	}

	public void reqRetr(View view) {
		String reqStr = new String("");
		try {
			// reqStr = mSerial.enviarMsg("1");
			reqStr = mBT.enviarMsg("4");
			mostrarMsg(reqStr);
		} catch (Exception e) {
			e.printStackTrace();
			mostrarMsg("reqRetr erro " + e.getMessage());
		}
	}

	public void reqBaf(View view) {
		String reqStr = new String("");
		try {
			// reqStr = mSerial.enviarMsg("2");
			reqStr = mBT.enviarMsg("2");
			// estadoAtual = ETomaEstados.MEDICAO_BAF;
			mostrarMsg(reqStr);
			mostrarAguardeBaf();
		} catch (Exception e) {
			e.printStackTrace();
			mostrarMsg("reqBaf erro " + e.getMessage());
		}
	}

	public void reqReset(View view) {
		String reqStr = new String("");
		try {
			// reqStr = mSerial.enviarMsg("3");
			reqStr = mBT.enviarMsg("3");
			mostrarMsg(reqStr);
		} catch (Exception e) {
			e.printStackTrace();
			mostrarMsg("reqReset erro " + e.getMessage());
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PrincipalFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PrincipalFragment newInstance(int sectionNumber) {
			PrincipalFragment fragment = new PrincipalFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PrincipalFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_principal,
					container, false);

			mFoto = (ImageView) rootView.findViewById(R.id.fotoParticipante);
			mNomeParticipante = (TextView) rootView.findViewById(R.id.textNome);
			mTagId = (TextView) rootView.findViewById(R.id.textTagID);
			mPosicaoParticipante = (TextView) rootView
					.findViewById(R.id.textPosicaao);
			mValorBafometro = (TextView) rootView
					.findViewById(R.id.textValorMedido);
			mTituloMaratoma = (TextView) rootView
					.findViewById(R.id.textViewMaratoma);

			mRanking = (TextView) rootView.findViewById(R.id.textRanking);

			mLayoutGrafico = (FrameLayout) rootView
					.findViewById(R.id.layoutGrafico);

			exibirDadosMaratoma();

			return rootView;
		}

		public static void mostrarId(String id) {
			if (mTagId != null) {
				mTagId.setText("ID: " + id);
			}
		}

		public static void mostrarNome(String nome) {
			if (mNomeParticipante != null) {
				mNomeParticipante.setText(nome);
			}
		}

		public static void mostrarPosicao(String pos) {
			if (mPosicaoParticipante != null) {
				mPosicaoParticipante.setText("Posição: " + pos + "º");
				// if (pos == "1") {
				// mPosicaoParticipante.getCompoundDrawables().
				// }
			}
		}

		public static void mostrarFoto(String id) {
			if (mFoto != null) {
				mFoto.setImageResource(R.drawable.ic_launcher);
			}
		}

		public static void mostrarValorBafometro(String valor) {
			if (mValorBafometro != null) {
				mValorBafometro.setText(valor);
			}
		}
	}

	public static class CadastroFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static CadastroFragment newInstance(int sectionNumber) {
			CadastroFragment fragment = new CadastroFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public CadastroFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_cadastro,
					container, false);

			mCadNomeEvento = (EditText) rootView
					.findViewById(R.id.cadNomeEvento);

			mCadDataEvento = (TextView) rootView
					.findViewById(R.id.cadDataEvento);

			mTotalMaratomas = (TextView) rootView
					.findViewById(R.id.textViewTotalMaratomas);
			mListaMaratomas = (ListView) rootView
					.findViewById(R.id.listViewMaratomas);

			mCadNovaMaratoma = (Button) rootView
					.findViewById(R.id.cadNovaMaratoma);

			// abaixo define o clique sobre a Maratoma
			mListaMaratomas.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					String nomeDataEvento = (String) ((TextView) arg1)
							.getText();

					mMaratomaEdicao = ETomaControle
							.obterMaratomaPorNomeData(nomeDataEvento);

					mCadNomeEvento.setText(mMaratomaEdicao.nomeEvento);
					mCadDataEvento.setText(new SimpleDateFormat("dd/MM/yyyy")
							.format(mMaratomaEdicao.dataEvento));

					if (mCadNovaMaratoma != null) {
						mCadNovaMaratoma.setEnabled(true);
					}

					estadoAtual = ETomaEstados.MARATOMA_EDITAR;

					// Toast.makeText(arg1.getContext(),nomeParticipante,Toast.LENGTH_SHORT).show();
				}
			});

			// abaixo define o clique demorado sobre a Maratoma
			mListaMaratomas
					.setOnItemLongClickListener(new OnItemLongClickListener() {

						@Override
						public boolean onItemLongClick(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {

							String nomeDataEvento = (String) ((TextView) arg1)
									.getText();

							mMaratomaExclusao = ETomaControle
									.obterMaratomaPorNomeData(nomeDataEvento);

							// popup de exclusão
							PopupMenu popup = new PopupMenu(autoRef, arg1);
							popup.getMenuInflater().inflate(
									R.menu.popup_participante, popup.getMenu());
							popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

								@Override
								public boolean onMenuItemClick(MenuItem item) {

									ETomaControle
											.excluirMaratoma(mMaratomaExclusao);

									limparCadastroMaratoma(null);
									mostrarListaMaratomas();
									mMaratoma = null;

									Toast.makeText(
											autoRef,
											"Maratoma "
													+ mMaratomaExclusao.nomeEvento
													+ " excluída!",
											Toast.LENGTH_SHORT).show();

									return true;
								}
							});

							popup.show();

							return true;
						}

					});

			return rootView;
		}

	}

	public static class MonitorFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static MonitorFragment newInstance(int sectionNumber) {
			MonitorFragment fragment = new MonitorFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public MonitorFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_monitor,
					container, false);

			mDumpTextView = (TextView) rootView.findViewById(R.id.saidaSerial);
			mScrollView = (ScrollView) rootView.findViewById(R.id.scrollSerial);

			mostrarMsg(resultInicializar);

			return rootView;
		}
	}

	public static class ParticipantesFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static ParticipantesFragment newInstance(int sectionNumber) {
			ParticipantesFragment fragment = new ParticipantesFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public ParticipantesFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_participantes,
					container, false);

			mTagParticipante = (EditText) rootView
					.findViewById(R.id.cadTagParticipante);

			mCadFoto = (ImageView) rootView.findViewById(R.id.cadFoto);

			mCadNomeParticipante = (EditText) rootView
					.findViewById(R.id.cadNomeParticipante);

			mTotalParticipantes = (TextView) rootView
					.findViewById(R.id.textViewTotalParticipantes);
			mListaParticipantes = (ListView) rootView
					.findViewById(R.id.listViewParticipantes);

			mCadNovoParticipante = (Button) rootView
					.findViewById(R.id.cadNovoParticipante);

			mCadLimparMedicoes = (Button) rootView.findViewById(R.id.cadLimpar);

			mCadSalvarParticipante = (Button) rootView
					.findViewById(R.id.cadSalvarParticipante);

			// abaixo define o clique sobre o participante
			mListaParticipantes
					.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int arg2, long arg3) {

							String texto = (String) ((TextView) arg1).getText();

							String nomeParticipante = texto.substring(0,
									texto.indexOf("\n"));

							mParticipanteEdicao = ETomaControle
									.obterParticipantePorNome(nomeParticipante);

							mCadNomeParticipante
									.setText(mParticipanteEdicao.nome);
							mTagParticipante.setText(mParticipanteEdicao.tag);
							mCurrentPhotoPath = mParticipanteEdicao.fotoPath;

							mostrarFoto(mCadFoto, mCurrentPhotoPath);

							if (mCadNovoParticipante != null) {
								mCadNovoParticipante.setEnabled(true);
							}
							if (mCadLimparMedicoes != null) {
								mCadLimparMedicoes.setEnabled(true);
							}

							estadoAtual = ETomaEstados.PARTICIPANTE_EDITAR;

							// Toast.makeText(arg1.getContext(),nomeParticipante,Toast.LENGTH_SHORT).show();
						}
					});

			// abaixo define o clique demorado sobre o participante
			mListaParticipantes
					.setOnItemLongClickListener(new OnItemLongClickListener() {

						@Override
						public boolean onItemLongClick(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {

							String texto = (String) ((TextView) arg1).getText();

							String nomeParticipante = texto.substring(0,
									texto.indexOf("\n"));

							mParticipanteExclusao = ETomaControle
									.obterParticipantePorNome(nomeParticipante);

							// popup de exclusão
							PopupMenu popup = new PopupMenu(autoRef, arg1);
							popup.getMenuInflater().inflate(
									R.menu.popup_participante, popup.getMenu());
							popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

								@Override
								public boolean onMenuItemClick(MenuItem item) {

									mMaratoma
											.excluirParticipante(mParticipanteExclusao);
									ETomaControle
											.excluirParticipante(mParticipanteExclusao);

									limparCadastroParticipante(null);
									mostrarListaParticipantes();

									Toast.makeText(
											autoRef,
											"Participante "
													+ mParticipanteExclusao.nome
													+ " excluído!",
											Toast.LENGTH_SHORT).show();

									return true;
								}
							});

							popup.show();

							return true;
						}

					});

			return rootView;
		}

	}

	public static void mostrarMsg(String msg) {

		if (mDumpTextView != null && mScrollView != null) {
			mDumpTextView.append(msg + "\n");
			mScrollView.smoothScrollTo(0, mDumpTextView.getBottom());
		}
	}

	private boolean carregarDadosParticipante(String tagParticipante) {

		mParticipante = ETomaControle.obterParticipantePorTag(tagParticipante);

		if (mParticipante != null) {
			PrincipalFragment.mostrarId(mParticipante.tag);
			mostrarFoto(mFoto, mParticipante.fotoPath);
			PrincipalFragment.mostrarNome(mParticipante.nome);

			mostrarPosicaoParticipante();

			return true;
		} else {
			PrincipalFragment.mostrarId(tagParticipante);
			mostrarFoto(mFoto, null);
			PrincipalFragment.mostrarNome("");
			PrincipalFragment.mostrarPosicao("");
			mostratToast("TAG " + tagParticipante + " nao cadastrada.");
			return false;
		}
	}

	private void mostrarPosicaoParticipante() {
		if (mMaratoma != null) {
			int pos = mMaratoma.obterPosicao(mParticipante);
			if (pos > 0) {
				PrincipalFragment.mostrarPosicao(String.valueOf(pos));
			} else {
				PrincipalFragment.mostrarPosicao("--");
			}
		} else {
			PrincipalFragment.mostrarPosicao("--");
		}
	}

	private void tratarMensagemRecebida(String msg) {
		mostrarMsg("Mensagem recebida: " + msg);

		if (msg.equals(ultimaMsg)) {
			ultimaMsg = "MARATOMA";
		} else {
			ultimaMsg = msg.substring(0);
			reqRetr(null);
			return;
		}

		// despreza 3 primeiros caracteres
		if (msg.length() > 3) {
			msg = msg.substring(3);
		} else {
			msg = "";
		}
		switch (estadoAtual) {
		case MEDICAO_TAG:
			if (msg.length() == 12) {
				errosLeitura = 0;
				PrincipalFragment.mostrarValorBafometro("...");
				if (carregarDadosParticipante(msg)) {
					estadoAtual = ETomaEstados.MEDICAO_BAF;
					reqBaf(null);
				}
			} else {
				errosLeitura++;
				if (errosLeitura < maxErrosLeitura) {
					reqRetr(null);
				} else {
					mostratToast("Erro de leitura da TAG.");
					mNomeParticipante.setText(R.string.string_passe_a_tag);
					reqTag(null);
				}
			}
			break;
		case PARTICIPANTE_NOVO:// mesmo comportamento do EDITAR.
		case PARTICIPANTE_EDITAR:
			if (msg.length() == 12) {
				errosLeitura = 0;
				if (mTagParticipante != null)
					mTagParticipante.setText(msg);
			} else {
				errosLeitura++;
				if (errosLeitura < maxErrosLeitura) {
					reqRetr(null);
				} else {
					mostratToast("Erro de leitura da TAG.");
				}
			}
			break;

		case PARTICIPANTE_PESQUISAR:
			if (msg.length() == 12) {
				errosLeitura = 0;

				mParticipanteEdicao = ETomaControle
						.obterParticipantePorTag(msg);

				if (mParticipanteEdicao != null) {

					mCadNomeParticipante.setText(mParticipanteEdicao.nome);
					mTagParticipante.setText(mParticipanteEdicao.tag);
					mCurrentPhotoPath = mParticipanteEdicao.fotoPath;

					mostrarFoto(mCadFoto, mCurrentPhotoPath);

					if (mCadNovoParticipante != null) {
						mCadNovoParticipante.setEnabled(true);
					}

					if (mCadLimparMedicoes != null) {
						mCadLimparMedicoes.setEnabled(true);
					}

					estadoAtual = ETomaEstados.PARTICIPANTE_EDITAR;
				} else {
					mostratToast("TAG " + msg + " não cadastrada.");
					limparCadastroParticipante(null);
					if (mTagParticipante != null)
						mTagParticipante.setText(msg);
				}

				if (mCadSalvarParticipante != null) {
					mCadSalvarParticipante.setEnabled(true);
				}

			} else {
				errosLeitura++;
				if (errosLeitura < maxErrosLeitura) {
					reqRetr(null);
				} else {
					mostratToast("Erro de leitura da TAG.");
				}
			}
			break;
		case MEDICAO_BAF:
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
			}
			try {
				Integer.valueOf(msg);
				errosLeitura = 0;
				registrarLeituraBafometro(msg);
				exibirDadosMaratoma();
				mostrarGrafico();
				estadoAtual = ETomaEstados.MEDICAO_TAG;
				reqTag(null);
				publicarBaforadaNoBlog(msg);
			} catch (Exception e) {
				errosLeitura++;
				if (errosLeitura < maxErrosLeitura) {
					reqRetr(null);
				} else {
					Toast toast = Toast
							.makeText(getApplicationContext(),
									"Erro de leitura do Bafômetro.",
									Toast.LENGTH_SHORT);
					toast.show();
					reqBaf(null);
				}
			}
			break;
		case MONITOR:
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
			}
			break;
		default:
			Toast toast = Toast.makeText(getApplicationContext(),
					"Mensagem recebida: " + msg, Toast.LENGTH_SHORT);
			toast.show();
			break;
		}
	}

	private void registrarLeituraBafometro(String msg) {
		PrincipalFragment.mostrarValorBafometro(msg);

		if (mMaratoma != null && mParticipante != null) {
			MedicaoBafometro mb = new MedicaoBafometro();

			mb.valorMedicao = Integer.valueOf(msg);
			mb.m = mMaratoma;
			mb.p = mParticipante;
			mb.dataHoraMedicao = new Date();

			mMaratoma.registrarMedicao(mParticipante, mb);
			ETomaControle.salvarMedicaoBafometro(mb);

			mostrarPosicaoParticipante();
		}
		
		// requer mais estudo para tirar uma foto secreta... vamos deixar para a próxima.
		//tirarFotoSecreta();
		
	}
	
	/*
	 * Deixar esta funcionalidade para uma próxima versão. Requer mais estudo.
	private void tirarFotoSecreta(){
		
		try {
	        mCamera = Camera.open();
	    } catch (Exception e) {
	        e.printStackTrace();
	        return;
	    }
		
		if (mCamera==null) return;
		
		Camera.PictureCallback raw = new Camera.PictureCallback() {
			
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {

		        if (data != null) {
				    FileOutputStream fos;
				    try {

				        fos = new FileOutputStream(createImageFile(null).getAbsolutePath());
				        fos.write(data);
				        fos.close();
				    }  catch (IOException e) {
				        //do something about it
				    	e.printStackTrace();
				    }
				}
				
				mCamera.release();
				
				mCamera = null;
				
				// para adicionar à galeria:
				Intent mediaScanIntent = new Intent(
						Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				File f = new File(mCurrentPhotoPath);
				Uri contentUri = Uri.fromFile(f);
				mediaScanIntent.setData(contentUri);
				autoRef.sendBroadcast(mediaScanIntent);

			}
		};
		
		
		try {
			
			// here, the unused surface view and holder
            SurfaceView dummy=new SurfaceView(autoRef.getApplicationContext());
            mCamera.setPreviewDisplay(dummy.getHolder());    
            mCamera.startPreview();             
            
			mCamera.takePicture(null, null, raw);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	*/

	private void mostrarAguardeBaf() {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(this,
					ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setTitle("Bafômetro");
			mProgressDialog.setMessage("Esperando bafo de cachaça...");
			mProgressDialog.setCancelable(true);
		}

		mProgressDialog.show();
	}

	public void takePic(View v) {

		ImagemParticipanteDialog imgDlg = new ImagemParticipanteDialog();
		imgDlg.show(getFragmentManager(), "imagem_participante");

		// Intent takePictureIntent = new
		// Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
		//
		// // Create the File where the photo should go File photoFile = null;
		// try {
		// photoFile = createImageFile(null);
		// } catch (IOException ex) {
		// // Error occurred while creating the File
		// mostratToast("Não foi possível criar arquivo de imagem.");
		// }
		//
		// if (photoFile != null) {
		// takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
		// Uri.fromFile(photoFile));
		// startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
		// }
		// }

		// acionarGaleria();
	}

	public void acionarCamera() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

			// Create the File where the photo should go
			File photoFile = null;
			try {
				photoFile = createImageFile(null);
			} catch (IOException ex) {
				// Error occurred while creating the
				// File
				mostratToast("Não foi possível criar arquivo de imagem.");
			}

			if (photoFile != null) {
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(photoFile));
				startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
			}
		}
	}

	public void acionarGaleria() {
		Intent i = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, IMAGE_PICKER_SELECT);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

			mostrarFoto(mCadFoto, mCurrentPhotoPath);

			// para adicionar à galeria:
			Intent mediaScanIntent = new Intent(
					Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			File f = new File(mCurrentPhotoPath);
			Uri contentUri = Uri.fromFile(f);
			mediaScanIntent.setData(contentUri);
			this.sendBroadcast(mediaScanIntent);

		} else if (requestCode == IMAGE_PICKER_SELECT
				&& resultCode == RESULT_OK) {

			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = autoRef.getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			mCurrentPhotoPath = cursor.getString(columnIndex);
			cursor.close();

			mostrarFoto(mCadFoto, mCurrentPhotoPath);

		} else if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == RESULT_OK){
			mBT.inicializar();
			btInicializado = true;
			if (estadoAtual == ETomaEstados.MEDICAO_TAG){
				reqTag(null);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public static void mostrarFoto(ImageView img, String arquivo) {
		if (img != null) {

			if (arquivo != null && !arquivo.isEmpty()) {

				// Get the dimensions of the View
				int targetW = 100; // img.getWidth();
				int targetH = 100; // img.getHeight();

				// Get the dimensions of the bitmap
				BitmapFactory.Options bmOptions = new BitmapFactory.Options();
				bmOptions.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(arquivo, bmOptions);
				int photoW = bmOptions.outWidth;
				int photoH = bmOptions.outHeight;

				// Determine how much to scale down the image
				int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

				// Decode the image file into a Bitmap sized to fill the View
				bmOptions.inJustDecodeBounds = false;
				bmOptions.inSampleSize = scaleFactor;
				bmOptions.inPurgeable = true;

				Bitmap imageBitmap = BitmapFactory.decodeFile(arquivo,
						bmOptions);

				img.setImageBitmap(imageBitmap);
			} else {
				img.setImageResource(R.drawable.sem_foto_peq);
			}
		}
	}

	private File createImageFile(String tag) throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_" + tag;

		File storageDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

		// String nomeCompletoArquivo = "file:" + storageDir.getAbsolutePath()
		// +"/"+tag+".jpg";

		File image = File.createTempFile(imageFileName, /* prefix */
				".jpg", /* suffix */
				storageDir /* directory */
		);

		// Save a file: path for use with ACTION_VIEW intents
		// mCurrentPhotoUri = "file:" + image.getAbsolutePath();
		mCurrentPhotoPath = image.getAbsolutePath();

		return image;
	}

	public void mostratToast(String msg) {
		Toast toast = Toast.makeText(getApplicationContext(), msg,
				Toast.LENGTH_SHORT);
		toast.show();

	}

	public static String getCadTagParticipante() {
		if (mTagParticipante != null) {
			String tag = mTagParticipante.getText().toString();
			if (tag != null) {
				tag = tag.trim();
				if (tag.isEmpty()) {
					tag = null;
				}
			}

			return tag;
		} else {
			return null;
		}
	}

	public static String getCadNomeParticipante() {
		if (mCadNomeParticipante != null) {
			return mCadNomeParticipante.getText().toString();
		} else {
			return null;
		}
	}

	private String getCadNomeEvento() {
		if (mCadNomeEvento != null) {
			return mCadNomeEvento.getText().toString();
		} else {
			return null;
		}
	}

	private Date getCadDataEvento() {
		// SimpleDateFormat format = new SimpleDateFormat("dd/M/yyyy");

		if (mCadDataEvento != null) {
			try {
				return new SimpleDateFormat("dd/M/yyyy").parse(mCadDataEvento
						.getText().toString());
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	public void salvarParticipante(View v) {

		String nome = getCadNomeParticipante();
		if (nome == null || nome.isEmpty()) {
			mostratToast("Necessário informar o Nome.");
			return;
		}

		String tag = getCadTagParticipante();

		Participante novoP = new Participante(nome, tag, mCurrentPhotoPath);

		if (estadoAtual == ETomaEstados.PARTICIPANTE_NOVO) {

			Participante velhoP = ETomaControle.obterParticipantePorNome(nome);

			if (velhoP != null) {
				ParticipanteExistenteDialogFragment d = new ParticipanteExistenteDialogFragment();
				d.setCancelable(false);
				d.show(getFragmentManager(), "participanteExistente");
				return;
			}

			if (tag != null) {

				velhoP = ETomaControle.obterParticipantePorTag(tag);

				if (velhoP != null) {
					TransporTagDialogFragment d = new TransporTagDialogFragment();
					d.setCancelable(false);
					d.setParticipantes(novoP, velhoP);
					d.show(getFragmentManager(), "transporTag");
					return;
				}
			}

			ETomaControle.novoParticipante(novoP);

			limparCadastroParticipante(null);

			mostratToast("Participante cadastrado com sucesso!");

			mostrarListaParticipantes();

		} else if (estadoAtual == ETomaEstados.PARTICIPANTE_EDITAR) {

			if (!nome.equals(mParticipanteEdicao.nome)) { // se o nome é
															// diferente do
															// antigo

				Participante outroP = ETomaControle
						.obterParticipantePorNome(nome); // verifica se tem
															// outro com esse
															// nome

				if (outroP != null) { // se tem outro, exibe alerta e não deixa
										// cadastrar.
					ParticipanteExistenteDialogFragment d = new ParticipanteExistenteDialogFragment();
					d.setCancelable(false);
					d.show(getFragmentManager(), "participanteExistente");
					return;
				}
			}

			// se o nome é igual ou é um diferente novo, pode continuar.

			if (tag != null) { // se a tag agora é diferente de null

				if (!tag.equals(mParticipanteEdicao.tag)) { // e a tag é
															// diferente da
															// antiga

					Participante outroP = ETomaControle
							.obterParticipantePorTag(tag);

					if (outroP != null) {
						TransporTagDialogFragment d = new TransporTagDialogFragment();
						d.setCancelable(false);
						d.setParticipantes(novoP, outroP);
						d.show(getFragmentManager(), "transporTag");
						return;
					}
				}
			}

			// se a tag é igual ou a tag é uma diferente nova ou se é nula, pode
			// alterar

			ETomaControle.alterarParticipante(mParticipanteEdicao, novoP);

			limparCadastroParticipante(null);

			mostratToast("Participante alterado com sucesso!");

			mostrarListaParticipantes();

		}

	}

	public static void limparCadastroParticipante(View v) {
		if (mTagParticipante != null) {
			mTagParticipante.setText("");
		}

		if (mCadNomeParticipante != null) {
			mCadNomeParticipante.setText("");
		}

		mCurrentPhotoPath = "";

		if (mCadFoto != null) {
			mostrarFoto(mCadFoto, null);
		}

		if (mCadNovoParticipante != null) {
			mCadNovoParticipante.setEnabled(false);
		}
		if (mCadLimparMedicoes != null) {
			mCadLimparMedicoes.setEnabled(false);
		}

		mParticipanteEdicao = null;

		estadoAtual = ETomaEstados.PARTICIPANTE_NOVO;
	}

	public void pesquisarParticipante(View v) {
		if (mTagParticipante != null) {
			mTagParticipante.setText("");
		}

		if (mCadNomeParticipante != null) {
			mCadNomeParticipante.setText("");
		}

		mCurrentPhotoPath = "";

		if (mCadFoto != null) {
			mostrarFoto(mCadFoto, null);
		}

		if (mCadNovoParticipante != null) {
			mCadNovoParticipante.setEnabled(false);
		}
		if (mCadLimparMedicoes != null) {
			mCadLimparMedicoes.setEnabled(false);
		}
		if (mCadSalvarParticipante != null) {
			mCadSalvarParticipante.setEnabled(false);
		}

		mParticipanteEdicao = null;

		reqTag(v);

		estadoAtual = ETomaEstados.PARTICIPANTE_PESQUISAR;

		mostratToast("Passe a tag para pesquisar.");

	}

	public void limparMedicoes(View v) {
		
		new LimparMedicoesDialog().show(getFragmentManager(), "limparMedicoes");

	}

	public static void limparCadastroMaratoma(View v) {
		if (mCadNomeEvento != null) {
			mCadNomeEvento.setText("");
		}

		if (mCadDataEvento != null) {
			mCadDataEvento.setText("");
		}

		if (mCadNovaMaratoma != null) {
			mCadNovaMaratoma.setEnabled(false);
		}

		mMaratomaEdicao = null;

		estadoAtual = ETomaEstados.MARATOMA_NOVA;
	}

	public void salvarMaratoma(View v) {

		String nomeEvento = getCadNomeEvento();
		if (nomeEvento == null || nomeEvento.isEmpty()) {
			mostratToast("Necessário informar nome do evento.");
			return;
		}

		Date dataEvento = getCadDataEvento();
		if (dataEvento == null) {
			mostratToast("Necessário informar a data do evento.");
			return;
		}

		Maratoma m = new Maratoma(nomeEvento, dataEvento);

		String nomeDataEvento = nomeEvento + "\n"
				+ new SimpleDateFormat("dd/MM/yyyy").format(dataEvento);

		Maratoma outraM = ETomaControle
				.obterMaratomaPorNomeData(nomeDataEvento);

		if (estadoAtual == ETomaEstados.MARATOMA_NOVA) {

			if (outraM != null) {
				mostratToast("Maratoma já existente!");
				return;
			}

			ETomaControle.salvarMaratoma(m);

			mostratToast("Maratoma cadastrada com sucesso!");

		} else if (estadoAtual == ETomaEstados.MARATOMA_EDITAR) {

			String nomeDataEventoEdicao = mMaratomaEdicao.nomeEvento
					+ "\n"
					+ new SimpleDateFormat("dd/MM/yyyy")
							.format(mMaratomaEdicao.dataEvento);

			if (nomeDataEvento.equals(nomeDataEventoEdicao)) {
				return;
			} else if (outraM != null) {
				mostratToast("Maratoma já existente!");
				return;
			}

			ETomaControle.alterarMaratoma(mMaratomaEdicao, m);

			mostratToast("Maratoma alterada com sucesso!");

		}

		limparCadastroMaratoma(null);
		mostrarListaMaratomas();
		mMaratoma = null;

	}

	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			// Do something with the date chosen by the user
			month++;
			if (mCadDataEvento != null) {
				mCadDataEvento.setText(day + "/" + month + "/" + year);
			}
		}

	}

	public void showDatePickerDialog(View v) {
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(this.getFragmentManager(), "datePicker");
	}

	@SuppressLint("SimpleDateFormat")
	private static void exibirDadosMaratoma() {
		
		if (mMaratoma == null){
			mMaratoma = ETomaControle.obterMaratoma();
		}
		
		if (mMaratoma != null) {
			if (mTituloMaratoma != null) {
				String dataEvento = new SimpleDateFormat("dd/M/yyyy")
						.format(mMaratoma.dataEvento);
				mTituloMaratoma.setText(mMaratoma.nomeEvento + " - "
						+ dataEvento);

			}

			if (mRanking != null) {
				mRanking.setText("");
				for (ItemRanking ir : mMaratoma.ranking) {
					mRanking.append(ir.posicao + "º " + ir.p.nome + " ("
							+ ir.valor + ")\n");
				}
			}
		}
	}

	public static void mostrarListaParticipantes() {

		if (mTotalParticipantes == null || mListaParticipantes == null) {
			return;
		}

		// carregar lista de participantes
		if (mParticipantesAdapter == null) {
			mParticipantesAdapter = new ArrayAdapter<String>(autoRef,
					android.R.layout.simple_list_item_1);
		} else {
			mParticipantesAdapter.clear();
		}

		mListaParticipantes.setAdapter(mParticipantesAdapter);

		Collection<Participante> listaParticipantes = ETomaControle
				.obterParticipanteListAll();

		if (listaParticipantes != null) {
			mTotalParticipantes.setText("Total de participantes: "
					+ listaParticipantes.size());
			for (Participante participante : listaParticipantes) {
				mParticipantesAdapter.add(participante.nome + "\nTAG: "
						+ participante.tag);
			}
		} else {
			mTotalParticipantes.setText("Total de participantes: 0");
		}

		mParticipantesAdapter.notifyDataSetChanged();
	}

	public static void mostrarListaMaratomas() {

		if (mTotalMaratomas == null || mListaMaratomas == null) {
			return;
		}

		// carregar lista de participantes
		if (mMaratomasAdapter == null) {
			mMaratomasAdapter = new ArrayAdapter<String>(autoRef,
					android.R.layout.simple_list_item_1);
		} else {
			mMaratomasAdapter.clear();
		}

		mListaMaratomas.setAdapter(mMaratomasAdapter);

		Collection<Maratoma> listaMaratomas = ETomaControle
				.obterMaratomasListAll();

		if (listaMaratomas != null) {
			mTotalMaratomas.setText("Total de maratomas: "
					+ listaMaratomas.size());
			for (Maratoma mar : listaMaratomas) {
				mMaratomasAdapter.add(mar.nomeEvento
						+ "\n"
						+ new SimpleDateFormat("dd/MM/yyyy")
								.format(mar.dataEvento));

			}
		} else {
			mTotalMaratomas.setText("Total de maratomas: 0");
		}

		mMaratomasAdapter.notifyDataSetChanged();
	}

	public void mostrarGrafico() {
		if (mLayoutGrafico == null || mMaratoma == null
				|| mParticipante == null) {
			return;
		}

		mLayoutGrafico.removeAllViews();

		try {
			ArrayList<MedicaoBafometro> medicoes = (ArrayList<MedicaoBafometro>) mMaratoma
					.obterMedicoesData(mParticipante);

			if (medicoes.size() < 2) { // pelo menos dois pontos para gerar o
										// gráfico.
				return;
			}

			// ArrayList<GraphViewData> dados = new
			// ArrayList<GraphView.GraphViewData>();

			GraphViewData[] dadosArray = new GraphViewData[medicoes.size()];
			String[] labels = new String[2];

			for (int i = 0; i < medicoes.size(); i++) {

				MedicaoBafometro med = medicoes.get(i);

				String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss")
						.format(med.dataHoraMedicao);

				if (i == 0) {
					labels[0] = new SimpleDateFormat("HH:mm")
							.format(med.dataHoraMedicao);
				} else if (i == medicoes.size() - 1) {
					labels[1] = new SimpleDateFormat("HH:mm")
							.format(med.dataHoraMedicao);
				}

				double hora = Double.valueOf(timeStamp);
				double valor = med.valorMedicao;

				dadosArray[i] = new GraphViewData(hora, valor);
			}

			// init example series data
			GraphViewSeries exampleSeries = new GraphViewSeries(dadosArray);

			GraphView graphView = new LineGraphView(this // context
					, mParticipante.nome // heading
			);
			graphView.addSeries(exampleSeries); // data
			graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.BLACK);
			graphView.getGraphViewStyle().setNumHorizontalLabels(2);
			graphView.setHorizontalLabels(labels);
			graphView.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);
			graphView.getGraphViewStyle().setNumVerticalLabels(2);
			graphView.getGraphViewStyle().setVerticalLabelsWidth(50);

			mLayoutGrafico.addView(graphView);
		} catch (Exception e) {
			mostratToast("Erro ao gerar gráfico.");
			e.printStackTrace();
		}
	}

	public void selecionarParticipante(View v) {

		String texto = (String) ((TextView) v).getText();

		mostratToast(texto);

	}

	public static class ParticipanteExistenteDialogFragment extends
			DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("Já existe participante com este nome.")
					.setNeutralButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// nada
								}
							});
			// Create the AlertDialog object and return it
			return builder.create();
		}
	}

	public static class TransporTagDialogFragment extends DialogFragment {

		private Participante nP;
		private Participante vP;

		public void setParticipantes(Participante np, Participante vp) {
			this.nP = np;
			this.vP = vp;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("Outro participante possui esta TAG. Transpor?")
					.setPositiveButton("Sim",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									//
									if (estadoAtual == ETomaEstados.PARTICIPANTE_NOVO) {
										ETomaControle
												.novoParticipanteTransporTag(
														nP, vP);
										Toast toast = Toast
												.makeText(
														getActivity()
																.getApplicationContext(),
														"Participante cadastrado com sucesso!",
														Toast.LENGTH_SHORT);
										toast.show();
									} else if (estadoAtual == ETomaEstados.PARTICIPANTE_EDITAR) {
										ETomaControle
												.alterarParticipanteTransporTag(
														mParticipanteEdicao,
														nP, vP);
										Toast toast = Toast
												.makeText(
														getActivity()
																.getApplicationContext(),
														"Participante alterado com sucesso!",
														Toast.LENGTH_SHORT);
										toast.show();
									}

									limparCadastroParticipante(null);

									mostrarListaParticipantes();

								}
							})
					.setNegativeButton("Não",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// nada
								}
							});
			// Create the AlertDialog object and return it
			return builder.create();
		}
	}

	public static class ImagemParticipanteDialog extends DialogFragment {

		String[] listaOpcoes = { "Tirar uma nova", "Escolher existente" };

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("Foto Participante").setItems(listaOpcoes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// The 'which' argument contains the index position
							// of the selected item

							switch (which) {
							case 0:
								autoRef.acionarCamera();
								break;
							case 1:
								autoRef.acionarGaleria();
								break;
							default:
								break;
							}

						}
					});
			return builder.create();
		}

	}

	public static class LimparMedicoesDialog extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("Tem certeza?")
					.setNegativeButton("Não!",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// nada
								}
							})
					.setPositiveButton("Sim",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									mMaratoma
											.excluirParticipante(mParticipanteEdicao);
									ETomaControle.limparMedicoes(
											mParticipanteEdicao, mMaratoma);
								}
							});
			// Create the AlertDialog object and return it
			return builder.create();
		}
	}

	private void publicarBaforadaNoBlog(String valorBaf) {

		if (mParticipante != null && mMaratoma != null) {

			int pos = mMaratoma.obterPosicao(mParticipante);

			String assunto = "Nova baforada de " + mParticipante.nome + "!";
			String mensagem = "Valor do bafômetro: " + valorBaf + "\n";

			if (pos > 0) {
				mensagem += "Posição atual do ranking: " + pos;
			} else {
				mensagem += "Leitura inicial.";
			}

			// publicarBlog(assunto, mensagem);
			// compartilharBaforada(assunto,mensagem);
			if (mTituloMaratoma != null) {
				mTituloMaratoma.post(printScreenThead);
			}
		}

	}

	// Call to update the share intent
	public void compartilharBaforada(String assunto, String mensagem) {

		Intent i = new Intent(Intent.ACTION_SEND);

		screenshot();

		if (mCurrentScreenshotPath != null && !mCurrentScreenshotPath.isEmpty()) {
			i.setType("image/jpeg");
			i.putExtra(Intent.EXTRA_STREAM,
					Uri.fromFile(new File(mCurrentScreenshotPath)));
		} else {
			i.setType("text/plain");
			i.putExtra(Intent.EXTRA_SUBJECT, assunto);
			i.putExtra(Intent.EXTRA_TEXT, mensagem);
		}

		if (mShareActionProvider != null) {
			mShareActionProvider.setShareIntent(i);
		}
	}

	public void publicarBlog(String assunto, String mensagem) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL,
				new String[] { "ruysilvacosta.meamaratoma@blogger.com" });
		i.putExtra(Intent.EXTRA_SUBJECT, assunto);
		i.putExtra(Intent.EXTRA_TEXT, mensagem);
		try {
			startActivity(Intent.createChooser(i, "Send mail..."));
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(this, "Não existe cliente de email instalado.",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void printScreen(View v) {
		compartilharBaforada("teste", "teste");
	}

	private Thread printScreenThead = new Thread() {
		@Override
		public void run() {
			compartilharBaforada("teste", "teste");
		};
	};

	public void screenshot() {

		if (mTituloMaratoma == null)
			return;

		// create bitmap screen capture
		Bitmap bitmap;
		View v1 = mTituloMaratoma.getRootView();
		v1.setDrawingCacheEnabled(true);
		bitmap = Bitmap.createBitmap(v1.getDrawingCache());
		v1.setDrawingCacheEnabled(false);

		try {
			OutputStream fout = null;
			File imageFile = createImageFile(mMaratoma.nomeEvento+"_"+mParticipante.nome);
			fout = new FileOutputStream(imageFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
			fout.flush();
			fout.close();
			mCurrentScreenshotPath = imageFile.getAbsolutePath();
			

			// para adicionar à galeria:
			Intent mediaScanIntent = new Intent(
					Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			mediaScanIntent.setData(Uri.fromFile(imageFile));
			this.sendBroadcast(mediaScanIntent);
			
			//controla arquivos de screenshot gravados
			mMaratoma.atualizarScreenshot(mParticipante, imageFile);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			mCurrentScreenshotPath = null;
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			mCurrentScreenshotPath = null;
			e.printStackTrace();
		}

	}

}
