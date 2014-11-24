package br.org.reembolsofacil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import br.org.reembolsofacil.adapter.ViagensArrayAdapter;
import br.org.reembolsofacil.pojo.EntDespesa;
import br.org.reembolsofacil.pojo.EntUsuario;
import br.org.reembolsofacil.pojo.EntViagem;
import br.org.reembolsofacil.text.MyTextWatcher;
import br.org.reembolsofacil.util.HttpClientImpl;
import br.org.reembolsofacil.util.UtlCrypto;

import com.admob.android.ads.AdListener;
import com.admob.android.ads.AdManager;
import com.admob.android.ads.AdView;

public class ReembolsoFacil extends BaseActivity implements AdListener {
	
	public static final int MENU_OPT_CONFIG = Menu.FIRST + 1;
	//public static final int MENU_OPT_DESPESAS = Menu.FIRST + 2;
	private static final int MENU_OPT_INFO = Menu.FIRST + 3;
	private static final int MENU_OPT_LOGIN = Menu.FIRST + 4;
	private static final int MENU_OPT_SAIR = Menu.FIRST + 5;
	private static final int MENU_OPT_VIAGENS = Menu.FIRST + 6;
	
	private Handler handler;//atualiza a view por outras threads
	
	ArrayList<EntViagem> viagensList = new ArrayList<EntViagem>();

	private String idViagem;
	private boolean restoreInsertView;//informa qdo deve restaurar a view do estado upadate para insert
	private EntUsuario usuario;
	private EntDespesa despesa;
	
	private String[] tipos = new String[]{"Alimentação","Taxi","Hotel","Ônibus","Pedágio","Combustível","Aluguel de Carro","Outros"};
	
	private ProgressDialog prgsDialogSendingDespesa;
	private ProgressDialog prgsDialog;
	
	private Button btnDtDespesa;
	private Button btnSendDespesa;
	private Button btnExcluirDespesa;
	private Button btnCancelar;
	private EditText txtDescricao;
	private EditText txtValor;
	private EditText editTextLogin;
	private EditText editTextSenha;
	private Spinner spinViagens;
	private Spinner spinTipos;
	private CheckBox chkBxSalvar;
	private AdView adView;
	
	static final int DIALOG_DATE  = 0;
	static final int DIALOG_LOGIN = 1;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.view_main);
		
		AdManager.setTestDevices( new String[] {  
			      AdManager.TEST_EMULATOR} );
		
		adView = (AdView) findViewById(R.id.ad);
		//adView.requestFreshAd();
		adView.setAdListener(this);
		//((AdView) findViewById(R.id.ad)).setAdListener(this);
		//adView.setRequestInterval(30);

		handler = new Handler();
		
		txtDescricao 	 = (EditText) findViewById(R.id.EditTextDescricao);
		txtValor 		 = (EditText) findViewById(R.id.EditTextValor);
		btnDtDespesa 	 = (Button)   findViewById(R.id.pickDate);
		btnSendDespesa 	 = (Button)   findViewById(R.id.btnSendDepesa);
		btnExcluirDespesa= (Button)   findViewById(R.id.btnExcluirDespesa);
		btnCancelar 	 = (Button)   findViewById(R.id.btnCancelar);
		spinViagens		 = (Spinner)  findViewById(R.id.spinViagens);
		spinTipos 		 = (Spinner)  findViewById(R.id.SpinnerTipo);
		
		despesa = new EntDespesa();
		
		txtValor.addTextChangedListener(new MyTextWatcher());
		
		setSpinnerTiposGastos();
		setBtnSendDespesa();
		setDataDespesa();
		setBtnDtDespesa();
		
		//se existe usuario cadastrado carrega viagens
		if(loadPreferences()){
			loadViagens();
		}else{//senao abre tela login/senha
			showDialog(DIALOG_LOGIN);
		}
	}
	private boolean loadPreferences(){
		
		String idUsuario=null;
		String emailUsuario=null;
		String login=null;
		String tipoUsuario=null;
		String senha=null;
		
		if((idUsuario = preferences.getString("idUsuario", null)) != null && 
			(emailUsuario = preferences.getString("emailUsuario", null)) != null && 
			(login = preferences.getString("login", null)) != null && 
			(tipoUsuario = preferences.getString("tipoUsuario", null)) != null && 
			(senha = preferences.getString("senha", null)) != null){
			
			usuario = new EntUsuario(idUsuario, emailUsuario, tipoUsuario, login, senha);
			return true;
		}
		return false;
	}
	
	private void loadViagens(){
		if(isOnline()){
			
			prgsDialogGettingViagens = ProgressDialog.show(
					ReembolsoFacil.this, "Sincronizando", "Carregando suas viagens, aguarde...",true,true);

			new Thread(new GetViagensRunnable()).start();
		}else{
			avisoLong("Seu dispositivo está sem acesso a Internet");
		}
	}
	
	private void setBtnDtDespesa(){
		btnDtDespesa.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_DATE);
			}
		});
	}

	protected class GetViagensRunnable implements Runnable{
		
		public void run() {
			viagensList.clear();
			Map<String, String> mapParams = new HashMap<String, String>();
			mapParams.put("login", usuario.getLogin());
			mapParams.put("senha", usuario.getSenha());
			mapParams.put("action", GET_VIAGENS);
			String response = new HttpClientImpl().doPost(URL,mapParams,3400);
			
			if(response.startsWith("{") || response.startsWith("[")){
				
				try {
					JSONArray jsonArray = new JSONArray(response);
					//primeira posição sempre é o status de retorno
					JSONObject jsonObject = jsonArray.getJSONObject(0);
					if(jsonObject.get(RETURN_STATUS).equals(STATUS_OK)){//despesa salva
						jsonArray = jsonArray.getJSONArray(1);
						for(int i=0; i<jsonArray.length();i++){
							jsonObject = jsonArray.getJSONObject(i);
							
							viagensList.add(new EntViagem(jsonObject.getString("idViagem"),
									jsonObject.getString("motivoViagem"),jsonObject.getString("dataInicViagem"),jsonObject.getString("dataFimViagem"),
									jsonObject.getString("aberta"), jsonObject.getString("adiantamento"),
									jsonObject.getString("totalDespesas"),jsonObject.getString("saldo")));
						}
						
					}else if(jsonObject.get(RETURN_STATUS).equals(STATUS_EMPTY_LIST)){
						//handler.post(new AvisoLongRunnable(ReembolsoFacil.this, "Não há viagens cadastradas"));
					}else{
						handler.post(new AvisoLongRunnable(ReembolsoFacil.this, jsonObject.getString(RETURN_MESSAGE)));
					}
										
				} catch (JSONException e) {
					Log.e("ERRO", e.getMessage());
					handler.post(new AvisoLongRunnable(ReembolsoFacil.this, "Não foi possível converter os dados das viagens"));
				}
				
			}else{
				handler.post(new AvisoLongRunnable(ReembolsoFacil.this, "Não foi possível recuperar as viagens, o retorno foi:\n"+response));
			}
			//atualiza spinner de viagens, tendo ou não sido recuperado
			//pois qdo é alterado o usuário e o novo não tem viagens, deve-se limpar o spinner
			handler.post(new Runnable() {	
				public void run() {
					ViagensArrayAdapter adapter = new ViagensArrayAdapter(ReembolsoFacil.this, viagensList);
					spinViagens.setAdapter(adapter);
				}
			});
			prgsDialogGettingViagens.dismiss();
		}
		
	}

	private class SendDespesaRunnable implements Runnable{
		
		String response ="";
		JSONObject jsonObject;
		public void run() {
		
			despesa.setDescricaoDespesa(txtDescricao.getText().toString().trim());
			despesa.setValorDespesa(txtValor.getText().toString().trim().replace(",", "."));
			
			Map<String, String> mapParams = new HashMap<String, String>();
			if(despesa.getIdDespesa() !=null && despesa.getIdDespesa().length()>0){
				mapParams.put("idDespesa", despesa.getIdDespesa());
				restoreInsertView=true;
			}
			mapParams.put("action", SEND_DESPESA);
			mapParams.put("login", usuario.getLogin());
			mapParams.put("senha", usuario.getSenha());
			mapParams.put("idViagem", idViagem);
			mapParams.put("dataDespesa", despesa.getDataDespesa());
			mapParams.put("descricaoDespesa", despesa.getDescricaoDespesa());
			mapParams.put("tipoDespesa", despesa.getTipoDespesa());
			mapParams.put("valorDespesa", despesa.getValorDespesa());
						
			response = new HttpClientImpl().doPost(URL,mapParams,3400);
			
			if(response.startsWith("{") || response.startsWith("[")){
				
				try {
					JSONArray jsonArray = new JSONArray(response);
					//primeira posição sempre é o status de retorno
					jsonObject = jsonArray.getJSONObject(0);
					if(jsonObject.get(RETURN_STATUS).equals(STATUS_OK)){//despesa salva
						handler.post(new Runnable() {
							String msg = jsonObject.getString(RETURN_MESSAGE);
							public void run() {
								txtDescricao.getText().clear();
								txtValor.getText().clear();
								restoreInsertView();
								aviso(msg);
							}
						});
					}else{
						handler.post(new AvisoLongRunnable(ReembolsoFacil.this, jsonObject.getString(RETURN_MESSAGE)));
					}
				} catch (JSONException e) {
					handler.post(new AvisoLongRunnable(ReembolsoFacil.this, "Não foi possível converter os dados da despesa"));
				}
			}else{
				handler.post(new AvisoLongRunnable(ReembolsoFacil.this, "Não foi possível salvar a despesa, o retorno foi:\n"+response));
			}
			prgsDialogSendingDespesa.dismiss();
		}
	}
	
	private void setBtnSendDespesa(){
		btnSendDespesa.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				
				if(usuario==null){
					new AlertDialog.Builder(ReembolsoFacil.this).setTitle("Aviso").setMessage(
							"Você não está logado, faça login em menu -> login ou para cadastrar vá em menu -> criar usuário").setNeutralButton(
							"OK", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dlg, int sumthin) {
								}
							}).show();
					return;
				}
				if (spinViagens.getSelectedItem() == null){
					new AlertDialog.Builder(ReembolsoFacil.this).setTitle("Aviso").setMessage(
					"Você não possui viagens cadastradas, para cadastrar vá em menu -> viagens -> menu -> cadastrar").setNeutralButton(
					"OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dlg, int sumthin) {
						}
					}).show();
					return;
				}
				if (txtValor.getText().toString().equals("")) {
					aviso("Preencha o valor");
				} else {
					if(isOnline()){
						idViagem = ((EntViagem)spinViagens.getSelectedItem()).getIdViagem();
						prgsDialogSendingDespesa = ProgressDialog.show(
								ReembolsoFacil.this, "Enviando", "Enviando sua despesa, aguarde...",true,true);
						new Thread(new SendDespesaRunnable()).start();
						
					}else{
						avisoLong("Seu dispositivo está sem acesso a Internet");
					}	
				}
			}
		});
	}
	
	private void setDataDespesa(){
		Calendar c = Calendar.getInstance();
		despesa.setDataDespesa(c.get(Calendar.DAY_OF_MONTH)+"/"+(c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.YEAR));
		btnDtDespesa.setText(despesa.getDataDespesa());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_OPT_LOGIN, 0, "Login").setIcon(R.drawable.ic_menu_login);
		menu.add(0, MENU_OPT_CONFIG, 0, "Criar usuário").setIcon(R.drawable.ic_menu_invite);
		menu.add(0, MENU_OPT_VIAGENS, 0, "Viagens").setIcon(android.R.drawable.ic_menu_agenda);
		//menu.add(0, MENU_OPT_DESPESAS, 0, "Despesas").setIcon(android.R.drawable.ic_menu_agenda);
		menu.add(0, MENU_OPT_INFO, 0, "Sobre").setIcon(android.R.drawable.ic_menu_info_details);
		menu.add(0, MENU_OPT_SAIR, 0, "Sair").setIcon(android.R.drawable.ic_menu_close_clear_cancel);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_OPT_CONFIG:
			Intent i1 = new Intent(this,UsuarioActivity.class);
			i1.putExtra("usuario", usuario);
			startActivityForResult(i1, VIEW_USUARIO);
			
			return true;
		
		/*case MENU_OPT_DESPESAS:
			if(usuario == null){
				avisoLong("Você não possui usuário, faça login em menu -> login ou cadastre um em menu -> configurações");
			}else if(spinViagens.getAdapter() == null || spinViagens.getAdapter().isEmpty())
				avisoLong("Você não possui viagens, cadastre uma em menu -> viagens");
			else{
				idViagem = ((EntViagem)spinViagens.getSelectedItem()).getIdViagem();
				Intent i2 = new Intent(this,DespesasActivity.class);
				
				i2.putExtra("idViagem", idViagem);
				i2.putExtra("usuario", usuario);
				startActivity(i2);
			}
			return true;*/
			
		case MENU_OPT_VIAGENS:
			if(usuario == null)
				avisoLong("Você não possui usuário, faça login em menu -> login ou cadastre um em menu -> configurações");
			else{
				Intent i3 = new Intent(this,ViagensActivity.class);
				i3.putExtra("usuario", usuario);
				startActivityForResult(i3, VIEW_VIAGENS);
			}
			return true;
		case MENU_OPT_INFO:
			final TextView message = new TextView(this);
			message.setText(R.string.about);
			message.setLinkTextColor(ColorStateList.valueOf(Color.GRAY));
			message.setPadding(5, 5, 5, 5);
			Linkify.addLinks(message, Linkify.WEB_URLS);
			message.setMovementMethod(LinkMovementMethod.getInstance());
			new AlertDialog.Builder(this).setTitle("Sobre").setView(message).setNeutralButton(
					"OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dlg, int sumthin) {
						}
					}).show();
			return true;
			
		case MENU_OPT_LOGIN:

			showDialog(DIALOG_LOGIN);
			return true;

		case MENU_OPT_SAIR:

			aviso("Saindo...");
			finish();
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
		case VIEW_USUARIO:
			if(resultCode == RESULT_OK){
				//List<EntViagem> viagensList = (List<EntViagem>)data.getSerializableExtra("br.org.reembolsofacil.viagensList");
				usuario = (EntUsuario) data.getSerializableExtra("br.org.reembolsofacil.usuario");
				loadViagens();
				//ViagensArrayAdapter adapter = new ViagensArrayAdapter(this, viagensList);
				//spinViagens.setAdapter(adapter);
			}
			break;

		case VIEW_VIAGENS:
			if(resultCode == RESULT_OK){
				//TODO melhorar, para ao retornar nao ter q chamar o servidor novamente
				loadViagens();
				/*List<EntViagem> viagensList = (List<EntViagem>)data.getSerializableExtra("br.org.reembolsofacil.viagensList");
				List<EntViagem> vs = new ArrayList<EntViagem>();
				for(EntViagem v:viagensList)
					if(v.getAberta().equals("true"))
						vs.add(v);
				
				ViagensArrayAdapter adapter = new ViagensArrayAdapter(ReembolsoFacil.this, vs);
				spinViagens.setAdapter(adapter);*/
			}
			break;
			
		default:
			break;
		}
	}

	// the callback received when the user "sets" the date in the dialog
	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			despesa.setDataDespesa(dayOfMonth+"/"+monthOfYear+"/"+year);
			btnDtDespesa.setText(despesa.getDataDespesa());
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_DATE:
				final Calendar c = Calendar.getInstance();
				return new DatePickerDialog(this, mDateSetListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
						c.get(Calendar.DAY_OF_MONTH));
				
			case DIALOG_LOGIN:
				LayoutInflater factory = LayoutInflater.from(this);
	            final View textEntryView = factory.inflate(R.layout.alert_dialog_login, null);
	            
	            return new AlertDialog.Builder(this)
	                .setIcon(android.R.drawable.ic_dialog_info)
	                .setTitle("Informe login e senha")
	                .setView(textEntryView)
	                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                    	
	                    	editTextLogin	 = (EditText) textEntryView.findViewById(R.id.editTextLogin);
	                		editTextSenha	 = (EditText) textEntryView.findViewById(R.id.editTextSenha);
	                		chkBxSalvar 	 = (CheckBox) textEntryView.findViewById(R.id.chkBxSalvar);
	                		
	                		prgsDialog = ProgressDialog.show(
            						ReembolsoFacil.this, "Autenticando", "Logando, aguarde...",true,true);
            				new Thread(new GetUsuarioRunnable()).start();
	                    }
	                })
	                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {}
	                }).create();
		}
		return null;
	}

	//qdo uma activity chama novamente esta, que é apenas colocada
	//no topo da pilha, sem ser recriada
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if(intent.getSerializableExtra("despesa") != null){
			despesa = (EntDespesa) intent.getSerializableExtra("despesa");
			btnCancelar.setVisibility(Button.VISIBLE);
			btnExcluirDespesa.setVisibility(Button.VISIBLE);
		}else{
			despesa.setDescricaoDespesa("");
			despesa.setIdDespesa("");
			despesa.setValorDespesa("");
			//pode ocorrer de a tela ter ficado ja no estado de edicao, entao aqui deve ser resetado
			btnCancelar.setVisibility(Button.GONE);
			btnExcluirDespesa.setVisibility(Button.GONE);
		}
		idViagem = intent.getStringExtra("idViagem");
		setScreenWithDespesa();
		
		btnExcluirDespesa.setOnClickListener(new OnClickListener() {	
			public void onClick(View v) {
				new AlertDialog.Builder(ReembolsoFacil.this).setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Confirmação")
				.setMessage("Confirma exclusão desta despesa?").setNeutralButton(
				"OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int sumthin) {

						if(isOnline()){
							
							prgsDialog = ProgressDialog.show(
									ReembolsoFacil.this, "Excluindo", "Excluindo despesa, aguarde...",true,true);
							restoreInsertView=true;
							new Thread(new DeleteDespesaRunnable()).start();
						}else{
							avisoLong("Seu dispositivo está sem acesso a Internet, não é possível excluir viagem");
						}
					}
				}).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int sumthin) {}
				}).show();
			}
		});
		
		btnCancelar.setOnClickListener(new OnClickListener() {	
			public void onClick(View v) {
				restoreInsertView=true;
				restoreInsertView();
			}
		});
	}
	
	public void restoreInsertView(){
		if(restoreInsertView){
			restoreInsertView=false;
			btnCancelar.setVisibility(Button.GONE);
			btnExcluirDespesa.setVisibility(Button.GONE);
			despesa.setIdDespesa("");
			loadViagens();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void setScreenWithDespesa(){
		txtDescricao.setText(despesa.getDescricaoDespesa()==null?"":despesa.getDescricaoDespesa());
		txtValor.setText(despesa.getValorDespesa()==null?"":despesa.getValorDespesa());
		btnDtDespesa.setText(despesa.getDataDespesa());
		for(int i=0; i<tipos.length;i++){
			if(despesa.getTipoDespesa().equalsIgnoreCase(tipos[i])){
				spinTipos.setSelection(i);
				break;
			}
		}
		ArrayAdapter<EntViagem> arrAdpt=(ArrayAdapter<EntViagem>) spinViagens.getAdapter();
		for(EntViagem v:viagensList){
			if(v.getIdViagem().equals(idViagem)){
				spinViagens.setSelection(arrAdpt.getPosition(v));
				break;
			}
		}
	}
	
	/**
	 * Configura o spinner de tipos de gasto (cria e adiciona listener)
	 */
	private void setSpinnerTiposGastos(){
		
		ArrayAdapter<String> adapterTipoDespesas = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, tipos);
		
		adapterTipoDespesas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinTipos.setAdapter(adapterTipoDespesas);

		spinTipos.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long id) {
				
				despesa.setTipoDespesa(tipos[position]);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	protected class GetUsuarioRunnable implements Runnable{
		
		String login    = editTextLogin.getText().toString();
		String senhaMD5 = UtlCrypto.criptografaSenha(editTextSenha.getText().toString());
		String response = null;
		JSONObject jsonObject = null;
		public void run() {
			
			if(editTextLogin.getText().length()==0 || editTextSenha.getText().length()==0){
				callOkAlertDialogInHandler("Login","Informe login/senha");
			}else{
				Map<String, String> mapParams = new HashMap<String, String>();
				mapParams.put("login", login);
				mapParams.put("senha", senhaMD5);
				mapParams.put("action", GET_USUARIO);
				response = new HttpClientImpl().doPost(URL,mapParams,3400);
				
				if(response.startsWith("{") || response.startsWith("[")){
					
					try {
						JSONArray jsonArray = new JSONArray(response);
						//primeira posição sempre é o status de retorno
						jsonObject = jsonArray.getJSONObject(0);
						if(jsonObject.get(RETURN_STATUS).equals(STATUS_OK)){
							//se OK, usuário está na posição 1
							jsonObject = jsonArray.getJSONObject(1);
							
							usuario = new EntUsuario(jsonObject.getString("idUsuario"),jsonObject.getString("emailUsuario"),
									jsonObject.getString("tipoUsuario"),jsonObject.getString("login"), senhaMD5);
							
							//se checkBox está marcado, salvar login/senha no sistema
							if(chkBxSalvar != null && chkBxSalvar.isChecked()){
								preferences.edit().putString("idUsuario", usuario.getIdUsuario()).commit();
								preferences.edit().putString("login", usuario.getLogin()).commit();
								preferences.edit().putString("emailUsuario", usuario.getEmailUsuario()).commit();
								preferences.edit().putString("tipoUsuario", usuario.getTipoUsuario()).commit();
								preferences.edit().putString("senha", usuario.getSenha()).commit();
								
							}else{
								preferences.edit().clear().commit();
							}
							
							handler.post(new Runnable() {
								public void run() {
									//limpa dialog login
									editTextLogin.getText().clear();
			                		editTextSenha.getText().clear();
									
									prgsDialogGettingViagens = ProgressDialog.show(
											ReembolsoFacil.this, "Sincronizando", "Carregando suas viagens, aguarde...",true,true);
								}
							});
							new Thread(new GetViagensRunnable()).start();
						}else{
							String msg = jsonObject.getString(RETURN_MESSAGE);
							callOkAlertDialogInHandler("Login",msg);
						}
						
					} catch (JSONException e) {
						handler.post(new AvisoLongRunnable(ReembolsoFacil.this, "Não foi possível converter os dados do usuário"));
					}	
				}else{
					callOkAlertDialogInHandler("Login","Não foi possível logar, o retorno foi:\n"+response);
				}
			}
			prgsDialog.dismiss();
		}
		private void callOkAlertDialogInHandler(final String title, final String msg){
			handler.post(new Runnable() {
				public void run() {
					new AlertDialog.Builder(ReembolsoFacil.this).setTitle(title).setMessage(
							msg).setNeutralButton(
							"OK", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dlg, int sumthin) {
									showDialog(DIALOG_LOGIN);
								}
							}).show();
				}
			});
		}
	}
	
	protected class DeleteDespesaRunnable implements Runnable{
		JSONObject jsonObject;
		public void run() {
			
			Map<String, String> mapParams = new HashMap<String, String>();
			mapParams.put("login", usuario.getLogin());
			mapParams.put("senha", usuario.getSenha());
			mapParams.put("idDespesa", despesa.getIdDespesa());
			mapParams.put("action", DELETE_DESPESA);
			String response = new HttpClientImpl().doPost(URL,mapParams,TIMEOUT_CONNECTION);
			
			if(response.startsWith("{") || response.startsWith("[")){	
				try {
					JSONArray jsonArray = new JSONArray(response);
					
					//primeira posição sempre é o status de retorno
					jsonObject = jsonArray.getJSONObject(0);
					if(jsonObject.get(RETURN_STATUS).equals(STATUS_OK)){//viagem excluida
						handler.post(new Runnable() {
							String msg = jsonObject.getString(RETURN_MESSAGE);
							public void run() {
								avisoLong(msg);
								txtDescricao.getText().clear();
								txtValor.getText().clear();
								restoreInsertView();
							}
						});
					}else{
						handler.post(new AvisoLongRunnable(ReembolsoFacil.this, jsonObject.getString(RETURN_MESSAGE)));
					}
				} catch (JSONException e) {
					handler.post(new AvisoLongRunnable(ReembolsoFacil.this, "Não foi possível converter os dados da despesa"));
				}
			}else{
				handler.post(new AvisoLongRunnable(ReembolsoFacil.this, "Não foi possível excluir a despesa, o retorno foi:\n"+response));
			}
			prgsDialog.dismiss();
		}
	}

	public void onFailedToReceiveAd(AdView adView) {
		Log.i("AdMobSDK", "onFailedToReceiveAd? "+this.adView.hasAd());;
	}
	public void onFailedToReceiveRefreshedAd(AdView adView) {
		Log.i("AdMobSDK", "onFailedToReceiveRefreshedAd? "+this.adView.hasAd());;
	}
	public void onReceiveAd(AdView adView) {
		/*boolean b = this.adView==adView;
		this.adView.setVisibility(View.VISIBLE);*/
		Log.i("AdMobSDK", "onReceiveAd? "+this.adView.hasAd());
	}
	public void onReceiveRefreshedAd(AdView adView) {
		Log.i("AdMobSDK", "onReceiveRefreshedAd? "+this.adView.hasAd());;
	}
}