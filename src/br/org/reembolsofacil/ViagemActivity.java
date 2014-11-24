package br.org.reembolsofacil;

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
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import br.org.reembolsofacil.pojo.EntUsuario;
import br.org.reembolsofacil.pojo.EntViagem;
import br.org.reembolsofacil.util.HttpClientImpl;
import br.org.reembolsofacil.util.UtlCalendar;

public class ViagemActivity extends BaseActivity {
	
	private Button btnSendViagem;
	private Button btnExcluirViagem;
	private Button btnCancelar;
	private Button btnDtInicio;
	private Button btnDtFim;
	private EditText editTextMotivo;
	private EditText editTextAdiantamento;
	private CheckBox chkBxAberta;
	private DatePickerDialog dtPickerInicio;
	private DatePickerDialog dtPickerFim;
	
	static final int DIALOG_DATE_INICIO  = 1;
	static final int DIALOG_DATA_FIM	 = 2;
	
	private EntViagem viagem;
	private EntUsuario usuario;
	
	protected ProgressDialog prgsDialog;

	private Handler handler;//atualiza a view por outras threads
			
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.view_viagem);

		handler   = new Handler();
		
		btnSendViagem 		= (Button) findViewById(R.id.btnSendViagem);
		btnExcluirViagem	= (Button) findViewById(R.id.btnExcluirViagem);
		btnCancelar			= (Button) findViewById(R.id.btnCancelar);
		btnDtInicio 		= (Button) findViewById(R.id.btnDtInicio);
		btnDtFim 			= (Button) findViewById(R.id.btnDtFim);
		editTextMotivo 		= (EditText) findViewById(R.id.editTextMotivo);
		editTextAdiantamento= (EditText) findViewById(R.id.editTextAdiantamento);
		chkBxAberta			= (CheckBox) findViewById(R.id.chkBxAberta);
		
		setBtnSendViagem();
		setDtPickerInicioFim();
				
		Intent i = getIntent();
		if(i!=null){
			usuario = (EntUsuario)i.getSerializableExtra("usuario");
			viagem = (EntViagem)i.getSerializableExtra("viagem");
			if(viagem == null){//nova viagem
				viagem = new EntViagem();
				Calendar c = Calendar.getInstance();
				viagem.setDataInicViagem(c.get(Calendar.DAY_OF_MONTH)+"/"+(c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.YEAR));
				viagem.setDataFimViagem(c.get(Calendar.DAY_OF_MONTH)+"/"+(c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.YEAR));
			}else{//editar viagem
				setBtnExcluirCancelarViagem();
				editTextMotivo.setText(viagem.getMotivoViagem());
				editTextAdiantamento.setText(viagem.getAdiantamento());
				chkBxAberta.setChecked(Boolean.parseBoolean(viagem.getAberta()));
				btnDtInicio.setText(viagem.getDataInicViagem());
				btnDtFim.setText(viagem.getDataFimViagem());
				Calendar c = UtlCalendar.getCalendarFromString(viagem.getDataInicViagem());
				dtPickerInicio.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
				c = UtlCalendar.getCalendarFromString(viagem.getDataFimViagem());
				dtPickerFim.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
			}
			
		}
		setBtnDtInicioFim();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_DATE_INICIO:
				return dtPickerInicio;
				
			case DIALOG_DATA_FIM:
				return dtPickerFim;
		}
		return super.onCreateDialog(id);
	}
	
	private void setDtPickerInicioFim(){
		final Calendar c1 = Calendar.getInstance();
		dtPickerInicio = new DatePickerDialog(this, new OnDateSetListener() {
			
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {

				viagem.setDataInicViagem(dayOfMonth+"/"+(monthOfYear+1)+"/"+year);
				btnDtInicio.setText(dayOfMonth+"/"+(monthOfYear+1)+"/"+year);
				
			}
		}, c1.get(Calendar.YEAR), c1.get(Calendar.MONTH),
				c1.get(Calendar.DAY_OF_MONTH));
		
		final Calendar c2 = Calendar.getInstance();
		dtPickerFim = new DatePickerDialog(this, new OnDateSetListener() {
			
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {

				viagem.setDataFimViagem(dayOfMonth+"/"+(monthOfYear+1)+"/"+year);
				btnDtFim.setText(dayOfMonth+"/"+(monthOfYear+1)+"/"+year);
				
			}
		}, c2.get(Calendar.YEAR), c2.get(Calendar.MONTH),
				c2.get(Calendar.DAY_OF_MONTH));
	}

	private void setBtnDtInicioFim(){
		
		btnDtInicio.setText(viagem.getDataInicViagem());
		btnDtFim.setText(viagem.getDataFimViagem());
		
		btnDtInicio.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_DATE_INICIO);
			}
		});
		
		btnDtFim.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_DATA_FIM);
			}
		});
	}
	
	private void setBtnSendViagem(){
		btnSendViagem.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				if(editTextMotivo.getText().length()==0){
					aviso("Preencha todos os campos");
				}else{
					if(isOnline()){
						
						prgsDialog = ProgressDialog.show(
								ViagemActivity.this, "Enviando", "Enviando viagem, aguarde...",true,true);
						
						viagem.setAberta(Boolean.toString(chkBxAberta.isChecked()));
						viagem.setMotivoViagem(editTextMotivo.getText().toString());
						viagem.setAdiantamento(editTextAdiantamento.getText().toString().trim().replace(",", "."));
	
						new Thread(new SendViagemRunnable()).start();
					}else{
						avisoLong("Seu dispositivo está sem acesso a Internet, não é possível cadastrar viagem");
					}
				}
			}
		});
	}
	
	private void setBtnExcluirCancelarViagem(){

		btnExcluirViagem.setVisibility(Button.VISIBLE);
		btnCancelar.setVisibility(Button.VISIBLE);
		
		btnExcluirViagem.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				new AlertDialog.Builder(ViagemActivity.this).setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Confirmação")
				.setMessage("Confirma exclusão desta viagem e todas suas despesas?").setNeutralButton(
				"OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int sumthin) {

						if(isOnline()){
							
							prgsDialog = ProgressDialog.show(
									ViagemActivity.this, "Excluindo", "Excluindo viagem, aguarde...",true,true);
		
							new Thread(new DeleteViagemRunnable()).start();
						}else{
							avisoLong("Seu dispositivo está sem acesso a Internet, não é possível excluir viagem");
						}
					}
				}).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int sumthin) {
					}
				}).show();
			}
		});
		
		btnCancelar.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}
	
	protected class SendViagemRunnable implements Runnable{
		
		public void run() {
			
			Map<String, String> mapParams = new HashMap<String, String>();
			mapParams.put("login", usuario.getLogin());
			mapParams.put("senha", usuario.getSenha());
			if(viagem.getIdViagem()!=null)
				mapParams.put("idViagem", viagem.getIdViagem());
			mapParams.put("aberta", viagem.getAberta());
			mapParams.put("dataInicViagem", viagem.getDataInicViagem());
			mapParams.put("dataFimViagem", viagem.getDataFimViagem());
			mapParams.put("motivoViagem", viagem.getMotivoViagem());
			mapParams.put("adiantamento", viagem.getAdiantamento());
			
			mapParams.put("action", SEND_VIAGEM);
			String response = new HttpClientImpl().doPost(URL,mapParams,TIMEOUT_CONNECTION);
			
			if(response.startsWith("{") || response.startsWith("[")){
				
				try {
					JSONArray jsonArray = new JSONArray(response);
					JSONObject jsonObject;
					//primeira posição sempre é o status de retorno
					jsonObject = jsonArray.getJSONObject(0);
					if(jsonObject.get(RETURN_STATUS).equals(STATUS_OK)){//viagem salva
						handler.post(new AvisoLongRunnable(ViagemActivity.this, jsonObject.getString(RETURN_MESSAGE)));
						setResult(RESULT_OK);
						finish();
					}else{
						handler.post(new AvisoLongRunnable(ViagemActivity.this, jsonObject.getString(RETURN_MESSAGE)));
					}
				} catch (JSONException e) {
					handler.post(new AvisoLongRunnable(ViagemActivity.this, "Não foi possível converter os dados da viagem"));
				}
			}else{
				handler.post(new AvisoLongRunnable(ViagemActivity.this, "Não foi possível salvar a viagem, o retorno foi:\n"+response));
			}

			prgsDialog.dismiss();
		}
	}
	
	protected class DeleteViagemRunnable implements Runnable{
		
		public void run() {
			
			Map<String, String> mapParams = new HashMap<String, String>();
			mapParams.put("login", usuario.getLogin());
			mapParams.put("senha", usuario.getSenha());
			mapParams.put("idViagem", viagem.getIdViagem());
			mapParams.put("action", DELETE_VIAGEM);
			String response = new HttpClientImpl().doPost(URL,mapParams,TIMEOUT_CONNECTION);
			
			if(response.startsWith("{") || response.startsWith("[")){	
				try {
					JSONArray jsonArray = new JSONArray(response);
					JSONObject jsonObject;
					//primeira posição sempre é o status de retorno
					jsonObject = jsonArray.getJSONObject(0);
					if(jsonObject.get(RETURN_STATUS).equals(STATUS_OK)){//viagem excluida
						handler.post(new AvisoLongRunnable(ViagemActivity.this, jsonObject.getString(RETURN_MESSAGE)));
						setResult(RESULT_OK);
						finish();
					}else{
						handler.post(new AvisoLongRunnable(ViagemActivity.this, jsonObject.getString(RETURN_MESSAGE)));
					}
				} catch (JSONException e) {
					handler.post(new AvisoLongRunnable(ViagemActivity.this, "Não foi possível converter os dados da viagem"));
				}
			}else{
				handler.post(new AvisoLongRunnable(ViagemActivity.this, "Não foi possível excluir a viagem, o retorno foi:\n"+response));
			}
			prgsDialog.dismiss();
		}
	}
}
