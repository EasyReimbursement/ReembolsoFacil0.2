package br.org.reembolsofacil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import br.org.reembolsofacil.adapter.ListViagensArrayAdapter;
import br.org.reembolsofacil.pojo.EntUsuario;
import br.org.reembolsofacil.pojo.EntViagem;
import br.org.reembolsofacil.util.HttpClientImpl;

public class ViagensActivity extends BaseActivity {
	
	public static final int MENU_OPT_ADD = Menu.FIRST + 1;
	
	private ListView listViewViagens;
	private ProgressDialog prgsDialogGettingViagens;
	private Handler handler;//atualiza a view por outras threads
	private EntUsuario usuario;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.view_viagens);
		
		listViewViagens = (ListView) findViewById(R.id.listViewViagens);
		handler = new Handler();
		
		listViewViagens.setOnItemClickListener(new OnItemClickListener() {
			EntViagem v;
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				v = (EntViagem)listViewViagens.getAdapter().getItem(position);
				
				new AlertDialog.Builder(ViagensActivity.this).setTitle("Viagem").setMessage(
				"Início: "+v.getDataInicViagem() + "\nFim: "+v.getDataFimViagem()
				+ "\nAberta: "+(v.getAberta().equals("true")?"sim":"não")+"\nMotivo: " + v.getMotivoViagem()
				+"\nAdiantamento: " + v.getAdiantamento()+"\nTotal de despesas: " + v.getTotalDespesas()
				+"\nSaldo: " + v.getSaldo())
				.setPositiveButton(
				"OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int sumthin) {
					}
				}).setNeutralButton("Editar", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int sumthin) {
						
						Intent i1 = new Intent(ViagensActivity.this,ViagemActivity.class);
						i1.putExtra("usuario", usuario);
						i1.putExtra("viagem", v);
						startActivityForResult(i1, VIEW_VIAGEM);
					}
				}).setNegativeButton("Despesas", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int sumthin) {
												
						Intent i2 = new Intent(ViagensActivity.this,DespesasActivity.class);
						
						i2.putExtra("viagem", v);
						i2.putExtra("usuario", usuario);
						startActivity(i2);
						
					}}).show();
			}
		});
		
		Intent i = getIntent();
		if(i != null){
			usuario = (EntUsuario)i.getSerializableExtra("usuario");
			
			if(isOnline()){
				
				prgsDialogGettingViagens = ProgressDialog.show(
						this, "Carregando", "Buscando suas viagens, aguarde...",true,true);

				new Thread(new GetViagensRunnable()).start();
			}else{
				avisoLong("Seu dispositivo está sem acesso a Internet");
			}
			
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_OPT_ADD, 0, "Adicionar").setIcon(android.R.drawable.ic_menu_add);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_OPT_ADD:
				Intent i1 = new Intent(this,ViagemActivity.class);
				i1.putExtra("usuario", usuario);
				startActivityForResult(i1, VIEW_VIAGEM);
				return true;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
		case VIEW_VIAGEM:
			if(resultCode == RESULT_OK){
				if(isOnline()){
					
					prgsDialogGettingViagens = ProgressDialog.show(
							this, "Carregando", "Buscando suas viagens, aguarde...",true,true);

					new Thread(new GetViagensRunnable()).start();
				}else{
					avisoLong("Seu dispositivo está sem acesso a Internet");
				}
			}else if(resultCode == RESULT_CANCELED){
				
			}
			break;
			
		default:
			break;
		}
	}

	protected class GetViagensRunnable implements Runnable{
		
		ArrayList<EntViagem> viagensList = new ArrayList<EntViagem>();
		
		public void run() {
			Map<String, String> mapParams = new HashMap<String, String>();
			mapParams.put("login", usuario.getLogin());
			mapParams.put("senha", usuario.getSenha());
			mapParams.put("action", GET_ALL_VIAGENS);
			
			String response = new HttpClientImpl().doPost(URL,mapParams,TIMEOUT_CONNECTION);
			
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
						handler.post(new Runnable() {
							public void run() {
								listViewViagens.setAdapter(new ListViagensArrayAdapter(ViagensActivity.this,GetViagensRunnable.this.viagensList));
							}
						});
						
						Intent i = new Intent();
						i.putExtra("br.org.reembolsofacil.viagensList", viagensList);
						setResult(RESULT_OK,i);
					}else if(jsonObject.get(RETURN_STATUS).equals(STATUS_EMPTY_LIST)){
						handler.post(new AvisoLongRunnable(ViagensActivity.this, "Não há viagens cadastradas"));
					}else{
						handler.post(new AvisoLongRunnable(ViagensActivity.this, jsonObject.getString(RETURN_MESSAGE)));
					}
				} catch (JSONException e) {
					Log.e("ERRO", e.getMessage());
					handler.post(new AvisoLongRunnable(ViagensActivity.this, "Não foi possível converter os dados das viagens"));
				}
				
			}else{
				handler.post(new AvisoLongRunnable(ViagensActivity.this, "Não foi possível recuperar as viagens, o retorno foi:\n"+response));
			}
			
			prgsDialogGettingViagens.dismiss();
		}
		
	}
}
