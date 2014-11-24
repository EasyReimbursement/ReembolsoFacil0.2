package br.org.reembolsofacil.pojo;

import java.io.Serializable;


public class EntViagem implements Serializable {
	private static final long serialVersionUID = 1L;

	private String idViagem;

	private String dataFimViagem;

	private String dataHora;

	private String dataInicViagem;

	private String motivoViagem;
	
	private String aberta;
	
	private String adiantamento;
	
	private String totalDespesas;
	
	private String saldo;

    public EntViagem() {
    }

	public EntViagem(String idViagem, String motivoViagem,
			String dataInicViagem, String dataFimViagem, String aberta,
			String adiantamento,String totalDespesas,String saldo) {
		super();
		this.idViagem = idViagem;
		this.motivoViagem = motivoViagem;
		this.dataInicViagem = dataInicViagem;
		this.dataFimViagem = dataFimViagem;
		this.aberta = aberta;
		this.adiantamento = adiantamento;
		this.totalDespesas = totalDespesas;
		this.saldo = saldo;
	}

	public String getIdViagem() {
		return idViagem;
	}

	public void setIdViagem(String idViagem) {
		this.idViagem = idViagem;
	}

	public String getDataFimViagem() {
		return dataFimViagem;
	}

	public void setDataFimViagem(String dataFimViagem) {
		this.dataFimViagem = dataFimViagem;
	}

	public String getDataHora() {
		return dataHora;
	}

	public void setDataHora(String dataHora) {
		this.dataHora = dataHora;
	}

	public String getDataInicViagem() {
		return dataInicViagem;
	}

	public void setDataInicViagem(String dataInicViagem) {
		this.dataInicViagem = dataInicViagem;
	}

	public String getMotivoViagem() {
		return motivoViagem;
	}

	public void setMotivoViagem(String motivoViagem) {
		this.motivoViagem = motivoViagem;
	}

	public String getAberta() {
		return aberta;
	}

	public void setAberta(String aberta) {
		this.aberta = aberta;
	}

	public String getAdiantamento() {
		return adiantamento;
	}

	public void setAdiantamento(String adiantamento) {
		this.adiantamento = adiantamento;
	}

	public String getTotalDespesas() {
		return totalDespesas;
	}

	public void setTotalDespesas(String totalDespesas) {
		this.totalDespesas = totalDespesas;
	}

	public String getSaldo() {
		return saldo;
	}

	public void setSaldo(String saldo) {
		this.saldo = saldo;
	}

	@Override
	public String toString() {
		return "EntViagem [idViagem=" + idViagem + ", aberta=" + aberta
				+ ", dataInicViagem=" + dataInicViagem + ", dataFimViagem="
				+ dataFimViagem + ", adiantamento=" + adiantamento
				+ ", motivoViagem=" + motivoViagem  
				+ ", totalDespesas=" + totalDespesas 
				+ ", saldo=" + saldo +", dataHora=" + dataHora
				+ "]";
	}

}