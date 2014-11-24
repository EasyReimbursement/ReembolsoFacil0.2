package br.org.reembolsofacil.util;

import java.math.BigInteger;
import java.security.MessageDigest;

public class UtlCrypto {

	public static String criptografaSenha(String senha) {
		String s=null;
		try{
		MessageDigest md = MessageDigest.getInstance("MD5");
		BigInteger hash = new BigInteger(1, md.digest(senha.getBytes("UTF-8")));
		s = hash.toString(16);
		if (s.length() % 2 != 0)
			s = "0" + s;
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
		return s;
	}
}
