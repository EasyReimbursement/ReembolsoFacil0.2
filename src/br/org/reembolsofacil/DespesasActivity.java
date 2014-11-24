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
import br.org.reembolsofacil.adapter.DespesasArrayAdapter;
import br.org.reembolsofacil.pojo.EntDespesa;
import br.org.reembolsofacil.pojo.EntUsuario;
import br.org.reembolsofacil.pojo.EntViagem;
import br.org.reembolsofacil.util.HttpClientImpl;

public class DespesasActivity extends BaseActivity {
	
	public static final int MENU_OPT_ADD = Menu.FIRST + 1;
	
	private ListView listViewDespesas;
	private EntViagem viagem;
	private ProgressDialog prgsDialogGettingDespesas;
	private Handler handler;//atualiza a view por outras threads
	private EntUsuario usuario;
	AlertDialog.Builder alertDialog=null;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.view_despesas);
		
		listViewDespesas = (ListView) findViewById(R.id.listViewDespesas);
		handler = new Handler();
		
		listViewDespesas.setOnItemClickListener(new OnItemClickListener() {
			EntDespesa d=null;
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				d = (EntDespesa)listViewDespesas.getAdapter().getItem(position);
							
				alertDialog = new AlertDialog.Builder(DespesasActivity.this).setTitle("Despesa").setMessage(
				"Data: "+d.getDataDespesa() + "\nTipo: " + d.getTipoDespesa() + 
				"\nValor: " + d.getValorDespesa() + "\nDescrição: " + d.getDescricaoDespesa()
				).setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int sumthin) {
					}
				}).setNeutralButton("Editar", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int sumthin) {
						if(!viagem.getAberta().equals("true")){
							avisoLong("Para editar despesas a viagem necessita estar aberta");
							alertDialog.show();
						}
						else{
							Intent i1 = new Intent(DespesasActivity.this,ReembolsoFacil.class);
							i1.putExtra("usuario", usuario);
							i1.putExtra("despesa", d);
							i1.putExtra("idViagem", viagem.getIdViagem());
							i1.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
							startActivity(i1);
						}
					}
				});
				alertDialog.show();
			}
		});
		
		Intent i = getIntent();
		if(i != null){
			usuario = (EntUsuario)i.getSerializableExtra("usuario");
			viagem = (EntViagem)i.getSerializableExtra("viagem");
			
			if(isOnline()){
				
				prgsDialogGettingDespesas = ProgressDialog.show(
						this, "Carregando", "Buscando suas despesas, aguarde...",true,true);

				new Thread(new GetDespesasRunnable()).start();
			}else{
				avisoLong("Seu dispositivo está sem acesso a Internet");
			}
			
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(viagem.getAberta().equals("true"))
			menu.add(0, MENU_OPT_ADD, 0, "Adicionar").setIcon(android.R.drawable.ic_menu_add);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_OPT_ADD:
				Intent i1 = new Intent(DespesasActivity.this,ReembolsoFacil.class);
				i1.putExtra("usuario", usuario);
				i1.putExtra("idViagem", viagem.getIdViagem());
				i1.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
				startActivity(i1);
				return true;
		}
		return false;
	}
	
	protected class GetDespesasRunnable implements Runnable{
		
		ArrayList<EntDespesa> despesasList = new ArrayList<EntDespesa>();
		
		public void run() {
			Map<String, String> mapParams = new HashMap<String, String>();
			mapParams.put("login", usuario.getLogin());
			mapParams.put("senha", usuario.getSenha());
			mapParams.put("idViagem", viagem.getIdViagem());
			mapParams.put("action", GET_DESPESAS);
			
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
							
							despesasList.add(new EntDespesa(jsonObject.getString("idDespesa"),
								jsonObject.getString("dataDespesa"),jsonObject.getString("tipoDespesa"),
								jsonObject.getString("valorDespesa"), jsonObject.getString("descricaoDespesa")));
						}
						handler.post(new Runnable() {
							public void run() {
								listViewDespesas.setAdapter(new DespesasArrayAdapter(DespesasActivity.this,despesasList));
							}
						});
						
					}else if(jsonObject.get(RETURN_STATUS).equals(STATUS_EMPTY_LIST)){
						handler.post(new AvisoLongRunnable(DespesasActivity.this, "Não há despesas cadastradas"));
					}else{
						handler.post(new AvisoLongRunnable(DespesasActivity.this, jsonObject.getString(RETURN_MESSAGE)));
					}
					
				} catch (JSONException e) {
					Log.e("ERRO", e.getMessage());
					handler.post(new AvisoLongRunnable(DespesasActivity.this, "Não foi possível converter os dados das viagens"));
				}
			}else{
				handler.post(new AvisoLongRunnable(DespesasActivity.this, "Não foi possível recuperar as despesas, o retorno foi:\n"+response));
			}
			prgsDialogGettingDespesas.dismiss();
		}
		
	}
}
