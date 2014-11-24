package br.org.reembolsofacil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Activity base com parâmetros e métodos compartilhados por todas as outras activities do projeto
 * @author fausto
 *
 */
public class BaseActivity extends Activity {

	//necessita ip da maquina mesmo local, nao serve localhost ou 127.0.0.1
	//public static final String URL = "http://192.168.1.100:8080/reembolsoFacilWeb/ServiceServlet";
	protected static final String URL = "http://4droiders.com/reembolsoFacilWeb/ServiceServlet";
	//public static final String URL = "http://192.168.1.67:8080/reembolsoFacilWeb/ServiceServlet";
	
	//Actions para chamadas ao servidor
	protected static final String GET_VIAGENS 		= "0";
	protected static final String SEND_DESPESA 		= "1";
	protected static final String GET_DESPESAS 		= "2";
	protected static final String GET_USUARIO 		= "3";
	protected static final String GET_ALL_VIAGENS	= "4";
	protected static final String ADD_USUARIO		= "5";
	protected static final String SEND_VIAGEM		= "6";
	protected static final String DELETE_VIAGEM		= "7";
	protected static final String DELETE_DESPESA	= "8";
	
	protected static final String RETURN_STATUS  = "RETURN_STATUS";
	protected static final String RETURN_MESSAGE = "RETURN_MESSAGE";
	//Return status
	protected static final int STATUS_ERROR		= 0;
	protected static final int STATUS_OK		= 1;
	protected static final int STATUS_EMPTY		= 2;
	protected static final int STATUS_EMPTY_LIST= 3;
	
	//Ids para chamadas de activities
	protected static final int VIEW_USUARIO		= 1;
	protected static final int VIEW_DESPESAS	= 2;
	protected static final int VIEW_VIAGEM		= 3;
	protected static final int VIEW_VIAGENS		= 4;
	protected static final int VIEW_MAIN		= 5;
	
	protected static final int TIMEOUT_CONNECTION	= 10000;//10segundos
	
	protected ProgressDialog prgsDialogGettingViagens;
		
	protected SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		preferences = getSharedPreferences("ReembolsoFacil.preferences", Context.MODE_PRIVATE);	
	}
	
	/**
	 * Para funcionar adicione no manifest android:configChanges na activity
	 * Impede que ao girar ou abrir teclado seja chamado onCreate
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		/*if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
			aviso("modo paisagem");
		if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
			aviso("modo retrato");
		if(newConfig.orientation == Configuration.ORIENTATION_SQUARE)
			aviso("modo square");
		if(newConfig.orientation == Configuration.ORIENTATION_UNDEFINED)
			aviso("modo indefinido");*/
	}

	/**
	 * Show a text message on screen 
	 * @param msgToast
	 */
	protected void aviso(String msgToast) {
		Toast.makeText(this, msgToast, Toast.LENGTH_SHORT).show();
	}
	
	protected void avisoLong(String msgToast) {
		Toast.makeText(this, msgToast, Toast.LENGTH_LONG).show();
	}

	public boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    //netInfo.isConnected()
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	public boolean isOnline2() {
		try {
			URL url = new URL("http://www.google.com");

			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
			urlc.setRequestProperty("User-Agent", "Android Application:");
			urlc.setRequestProperty("Connection", "close");
			urlc.setConnectTimeout(1000 * 3); // mTimeout is in seconds
			urlc.connect();
			if (urlc.getResponseCode() == 200) {
				return true;
			}
		} catch (MalformedURLException e1) {
			Log.e("Error", e1.getMessage());
		} catch (IOException e) {
			Log.e("Error", e.getMessage());
		}
		return false;
	}

	public boolean isOnline3() {
		ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		return conMgr.getActiveNetworkInfo() != null &&
        	conMgr.getActiveNetworkInfo().isAvailable() &&
        	conMgr.getActiveNetworkInfo().isConnected();
	}
	
	protected class AvisoRunnable implements Runnable{

		private String msg;
		private Context ctx;
		
		public AvisoRunnable(Context ctx, String msg) {
			super();
			this.msg = msg;
			this.ctx = ctx;
		}

		public void run() {
			Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
			
		}
		
	}
	
	protected class AvisoLongRunnable implements Runnable{

		private String msg;
		private Context ctx;
		
		public AvisoLongRunnable(Context ctx, String msg) {
			super();
			this.msg = msg;
			this.ctx = ctx;
		}

		public void run() {
			Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
			
		}	
	}
}
