package br.org.reembolsofacil.pojo;

import java.io.Serializable;


public class EntUsuario implements Serializable {
	private static final long serialVersionUID = 1L;

	private String idUsuario;

	private String chaveAppUsuario;

	private String emailUsuario;

	private String tipoUsuario;

	private String login;
	
	private String senha;

    public EntUsuario() {
    }

	public EntUsuario(String idUsuario, String emailUsuario,
			String tipoUsuario, String login, String senha) {
		super();
		this.idUsuario = idUsuario;
		this.emailUsuario = emailUsuario;
		this.tipoUsuario = tipoUsuario;
		this.login = login;
		this.senha = senha;
	}

	public String getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(String idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getChaveAppUsuario() {
		return chaveAppUsuario;
	}

	public void setChaveAppUsuario(String chaveAppUsuario) {
		this.chaveAppUsuario = chaveAppUsuario;
	}

	public String getEmailUsuario() {
		return emailUsuario;
	}

	public void setEmailUsuario(String emailUsuario) {
		this.emailUsuario = emailUsuario;
	}

	public String getTipoUsuario() {
		return tipoUsuario;
	}

	public void setTipoUsuario(String tipoUsuario) {
		this.tipoUsuario = tipoUsuario;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	@Override
	public String toString() {
		return "EntUsuario [idUsuario=" + idUsuario + ", tipoUsuario="
				+ tipoUsuario + ", chaveAppUsuario=" + chaveAppUsuario
				+ ", emailUsuario=" + emailUsuario + ", login=" + login
				+ ", senha=" + senha + "]";
	}


}