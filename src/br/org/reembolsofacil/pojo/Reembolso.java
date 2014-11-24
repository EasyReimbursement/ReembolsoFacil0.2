package br.org.reembolsofacil.pojo;

import java.util.Calendar;

public class Reembolso {

	private String descrição;
	private double valor;
	private String tipo;
	private String dataDespesa;

	
	
	public Reembolso() {
		final Calendar c = Calendar.getInstance();
		dataDespesa= c.get(Calendar.DAY_OF_MONTH)+"-"+ c.get(Calendar.MONTH)+"-"+c.get(Calendar.YEAR);
	}

	public String getDescrição() {
		return descrição;
	}

	public void setDescrição(String descrição) {
		this.descrição = descrição;
	}

	public double getValor() {
		return valor;
	}

	public void setValor(double valor) {
		this.valor = valor;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getDataDespesa() {
		return dataDespesa;
	}

	public void setDataDespesa(String dataDespesa) {
		this.dataDespesa = dataDespesa;
	}

	@Override
	public String toString() {
		return "Reembolso [dataDespesa=" + dataDespesa + ", descrição="
				+ descrição + ", tipo=" + tipo + ", valor=" + valor + "]";
	}

}
