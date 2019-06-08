package it.polito.tdp.poweroutages.model;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class Evento implements Comparable<Evento>{
	
	public enum TIPO{
		INIZIO_INTERRUZIONE,
		FINE_INTERRUZIONE,
		CANCELLA_PRESTITO
	}
	
	private TIPO tipo;
	private Nerc nerc;
	private Nerc donatore;
	private LocalDateTime data; //data in cui si verifica l'evento
	
	private LocalDateTime dataInizio; //data di inizio dell'evento
	private LocalDateTime dataFine; //data fine dell'evento
	
	public Evento(TIPO tipo, Nerc nerc, Nerc donatore, LocalDateTime data, LocalDateTime dataInizio,
			LocalDateTime dataFine) {
		super();
		this.tipo = tipo;
		this.nerc = nerc;
		this.donatore = donatore;
		this.data = data;
		this.dataInizio = dataInizio;
		this.dataFine = dataFine;
	}
	
	public TIPO getTipo() {
		return tipo;
	}

	public void setTipo(TIPO tipo) {
		this.tipo = tipo;
	}

	public Nerc getNerc() {
		return nerc;
	}

	public void setNerc(Nerc nerc) {
		this.nerc = nerc;
	}

	public Nerc getDonatore() {
		return donatore;
	}

	public void setDonatore(Nerc donatore) {
		this.donatore = donatore;
	}

	public LocalDateTime getData() {
		return data;
	}

	public void setData(LocalDateTime data) {
		this.data = data;
	}

	public LocalDateTime getDataInizio() {
		return dataInizio;
	}

	public void setDataInizio(LocalDateTime dataInizio) {
		this.dataInizio = dataInizio;
	}

	public LocalDateTime getDataFine() {
		return dataFine;
	}

	public void setDataFine(LocalDateTime dataFine) {
		this.dataFine = dataFine;
	}

	//usiamo il campo data per ordinare gli eventi (ordine cronologico degli eventi)
	@Override
	public int compareTo(Evento o) {
		return this.data.compareTo(o.getData());
	}
	
	
	
}

