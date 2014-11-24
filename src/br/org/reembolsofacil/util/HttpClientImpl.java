package br.org.reembolsofacil.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

public class HttpClientImpl {

	public final String downloadArquivo(String url) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(url);

			HttpResponse response = httpclient.execute(httpget);

			HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream in = entity.getContent();
				String texto = readString(in);
				return texto;
			}
		} catch (Exception e) {
		}
		return null;
	}
	
	public final byte[] downloadImagem(String url) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(url);

			HttpResponse response = httpclient.execute(httpget);

			HttpEntity entity = response.getEntity();

			if (entity != null) {

				InputStream in = entity.getContent();
				byte[] bytes = readBytes(in);
				
				return bytes;
			}
		} catch (Exception e) {
		}
		return null;
	}

	public final String doPost(String url, Map map, int timeout) {
		HttpResponse response = null;
		try {
			HttpClient httpclient = new DefaultHttpClient();
			httpclient.getParams().setParameter("httpclient.useragent", "Android");
			httpclient.getParams().setParameter("http.socket.timeout", timeout);
			httpclient.getParams().setParameter("http.connection.timeout", timeout);
			HttpPost httpPost = new HttpPost(url);

			// cria os par�metros
			List<NameValuePair> params = getParams(map);
			// seta os parametros para enviar
			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

			response = httpclient.execute(httpPost);

			if(response.getStatusLine().getStatusCode() != 200){//200=SC_OK
				return "Erro na conexão, "+response.getStatusLine();
			}
			
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream in = entity.getContent();
				String texto = readString(in);
				return texto;
			}
		} catch (Exception e) {
			return "Erro conexão, "+e.getMessage();
		}
		return null;
	}

	private byte[] readBytes(InputStream in) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				bos.write(buf, 0, len);
			}

			byte[] bytes = bos.toByteArray();
			return bytes;
		} finally {
			bos.close();
		}
	}

	private String readString(InputStream in) throws IOException {
		byte[] bytes = readBytes(in);
		String texto = new String(bytes);
		return texto;
	}

	private List<NameValuePair> getParams(Map map) throws IOException {
		if (map == null || map.size() == 0) {
			return null;
		}

		List<NameValuePair> params = new ArrayList<NameValuePair>();

		Iterator e = (Iterator) map.keySet().iterator();
		while (e.hasNext()) {
			String name = (String) e.next();
			Object value = map.get(name);
			params.add(new BasicNameValuePair(name, URLEncoder.encode(String.valueOf(value), "UTF-8")));
		}

		return params;
	}
}
