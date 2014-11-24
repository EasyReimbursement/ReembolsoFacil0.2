package br.org.reembolsofacil;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import br.org.reembolsofacil.pojo.EntUsuario;
import br.org.reembolsofacil.util.HttpClientImpl;
import br.org.reembolsofacil.util.UtlCrypto;

public class UsuarioActivity extends BaseActivity {

	private EditText textLogin;
	private EditText textEmail;
	private EditText textSenha;
	private Button btnCadUsu;
	private Handler handler;//atualiza a view por outras threads
	protected ProgressDialog prgsDialog;
			
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.view_usuario);

		handler   = new Handler();
		textLogin = (EditText) findViewById(R.id.editTextLogin);
		textEmail = (EditText) findViewById(R.id.editTextEmail);
		textSenha = (EditText) findViewById(R.id.editTextSenha);
		btnCadUsu = (Button)   findViewById(R.id.btnCadUsu);
		
		setBtnAddUsuario();
	}
	
	private void setBtnAddUsuario(){
		btnCadUsu.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				if(textLogin.getText().length()==0 || textEmail.getText().length()==0 || textSenha.getText().length()==0){
					aviso("Preencha todos os campos");
				}else{
					if(isOnline()){
						
						prgsDialog = ProgressDialog.show(
								UsuarioActivity.this, "Enviando", "Enviando usuário, aguarde...",true,true);
	
						new Thread(new AddUsuarioRunnable()).start();
					}else{
						avisoLong("Seu dispositivo está sem acesso a Internet, não é possível cadastrar usuário");
					}
				}
				
			}
		});
	}
	
	protected class AddUsuarioRunnable implements Runnable{
		
		public void run() {
			
			String login    	= textLogin.getText().toString();
			String senhaMD5 	= UtlCrypto.criptografaSenha(textSenha.getText().toString());
			String emailUsuario = textEmail.getText().toString();
			
			Map<String, String> mapParams = new HashMap<String, String>();
			mapParams.put("login", login);
			mapParams.put("senha", senhaMD5);
			mapParams.put("emailUsuario", emailUsuario);
			mapParams.put("action", ADD_USUARIO);
			String response = new HttpClientImpl().doPost(URL,mapParams,TIMEOUT_CONNECTION);
			
			if(response.startsWith("{") || response.startsWith("[")){
				
				try {
					JSONObject jsonObject;
					JSONArray jsonArray = new JSONArray(response);
					//primeira posição sempre é o status de retorno
					jsonObject = jsonArray.getJSONObject(0);
					if(jsonObject.get(RETURN_STATUS).equals(STATUS_OK)){
						//se OK, usuário está na posição 1
						jsonObject = jsonArray.getJSONObject(1);
						
						EntUsuario usuario = new EntUsuario(jsonObject.getString("idUsuario"),jsonObject.getString("emailUsuario"),
								jsonObject.getString("tipoUsuario"),jsonObject.getString("login"), senhaMD5);
						handler.post(new AvisoLongRunnable(UsuarioActivity.this, "Usuário cadastrado"));
						Intent it = new Intent();
						it.putExtra("br.org.reembolsofacil.usuario", usuario);
						setResult(RESULT_OK, it);
						finish();
					}else{
						handler.post(new AvisoLongRunnable(UsuarioActivity.this, jsonObject.getString(RETURN_MESSAGE)));
					}
				} catch (JSONException e) {
					handler.post(new AvisoLongRunnable(UsuarioActivity.this, "Não foi possível converter os dados do usuário"));
				}
			}else{
				handler.post(new AvisoLongRunnable(UsuarioActivity.this, "Não foi possível cadastrar, o retorno foi:\n"+response));
			}
			
			prgsDialog.dismiss();
		}
	}
}
