package br.org.reembolsofacil.pojo;

import java.io.Serializable;

public class EntDespesa implements Serializable {
	private static final long serialVersionUID = 1L;

	private String idDespesa;

	private String dataDespesa;

	private String tipoDespesa;

	private String valorDespesa;
	
	private String descricaoDespesa;

    public EntDespesa() {
    }
    
	public EntDespesa(String idDespesa, String dataDespesa, String tipoDespesa,
			String valorDespesa, String descricaoDespesa) {
		super();
		this.idDespesa = idDespesa;
		this.dataDespesa = dataDespesa;
		this.tipoDespesa = tipoDespesa;
		this.valorDespesa = valorDespesa;
		this.descricaoDespesa = descricaoDespesa;
	}


	public String getIdDespesa() {
		return idDespesa;
	}

	public void setIdDespesa(String idDespesa) {
		this.idDespesa = idDespesa;
	}

	public String getDataDespesa() {
		return dataDespesa;
	}

	public void setDataDespesa(String dataDespesa) {
		this.dataDespesa = dataDespesa;
	}

	public String getDescricaoDespesa() {
		return descricaoDespesa;
	}

	public void setDescricaoDespesa(String descricaoDespesa) {
		this.descricaoDespesa = descricaoDespesa;
	}

	public String getTipoDespesa() {
		return tipoDespesa;
	}

	public void setTipoDespesa(String tipoDespesa) {
		this.tipoDespesa = tipoDespesa;
	}

	public String getValorDespesa() {
		return valorDespesa;
	}

	public void setValorDespesa(String valorDespesa) {
		this.valorDespesa = valorDespesa;
	}

	@Override
	public String toString() {
		return "EntDespesa [idDespesa=" + idDespesa +
		", dataDespesa=" + dataDespesa +
		", tipoDespesa=" + tipoDespesa +
		", valorDespesa=" + valorDespesa +
		", descricaoDespesa="+ descricaoDespesa + "]";
	}

}