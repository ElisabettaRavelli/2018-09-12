package it.polito.tdp.poweroutages.model;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;

public class Simulatore {
	
	//Modello (lo stato del sistema)
	private Graph<Nerc,DefaultWeightedEdge> grafo;
	private List<PowerOutage> powerOutages; //lista delle interruzioni di corrente
	private Map<Nerc, Set<Nerc>> prestiti; //il nerc (chiave) che ha prestato energia ai suoi vicini
											//(Set<Nerc>) che ora gli sono debitori
	
	//parametri della simulazione 
	private int k;
	
	//valori in output
	private int CATASTROFI;
	private Map<Nerc,Long> bonus; //un Nerc quando presta energia acquista un bonus
	
	//coda
	private PriorityQueue<Evento> queue;
	
	//METODO DI INIZIALIZZAZIONE
	public void init(int k, List<PowerOutage> powerOutage, NercIdMap nercMap, Graph<Nerc,DefaultWeightedEdge> grafo) {
		// si creano tutte le strutture dati che mi interessano
		// (se queste venissero inizializzate nel costruttore della classe simulatore e se il simulatore fosse usato
		// più volte per simulare il programma queste strutture dati potrebbero rimanere "sporche" di dati precedenti 
		// mentre se vengono messe nel metodo init() ogni volta che viene chiamato queste vengono distrutte e ricreate)
		this.queue = new PriorityQueue<Evento>();
		this.bonus = new HashMap<Nerc, Long>();
		this.prestiti = new HashMap<Nerc, Set<Nerc>>();
		
		//devo iniziliazzare anche i valori che ci sono dentro alla mappa (bonus=0 e il Set<Nerc> deve essere vuoto)
		for(Nerc n: nercMap.values()) {
			this.bonus.put(n, Long.valueOf(0));
			this.prestiti.put(n, new HashSet<>());
			
		}
		
		this.CATASTROFI = 0;
		//ricevo i parametri e me li salvo negli attributi interni
		this.k = k;
		this.powerOutages = powerOutage;
		this.grafo = grafo;
		
		//inserisco gli eventi iniziali
		/*
		 * Inserisco nella coda tutti gli eventi di inizio interruzione perchè allo scaternarsi di ogni 
		 * inizio devo vedere che ci sia un vicino disponibile a prestare energia 
		 */
				for(PowerOutage po : this.powerOutages) {
					Evento e = new Evento(Evento.TIPO.INIZIO_INTERRUZIONE, 
							po.getNerc(),null, po.getInizio(),po.getInizio(),po.getFine());
					queue.add(e); //creo un evento di inizio interruzione e lo aggiungo alla coda prioritaria
				}
		
	}
	//METODO CHE ESEGUE LA SIMULAZIONE
	public void run() {
		//prendo ogni volta un evento dalla coda che ho creato finchè questa non risulta vuota
		Evento e;
		while((e = queue.poll()) != null) {
			//switch per simulare tutti i tipi di evento
			switch(e.getTipo()) {
				case INIZIO_INTERRUZIONE:
					Nerc nerc = e.getNerc(); // il Nerc per cui c'è interruzione lo recupero dall'evento
					System.out.println("INIZIO INTERRUZIONE NERC: " + nerc);

					//cerco se c'è un donatore, altrimenti ... CATASTROFE
					Nerc donatore = null;
					//cerco il donatore tra "debitori" del nerc e se non ne ha cerco tra i suoi vicini
					if(this.prestiti.get(nerc).size() > 0) {
						//scelgo tra i miei debitori quello con peso dell'arco minore
						double min = Long.MAX_VALUE;
						for(Nerc n : this.prestiti.get(nerc)) {
							DefaultWeightedEdge edge = this.grafo.getEdge(nerc, n);
							if(this.grafo.getEdgeWeight(edge) < min) {
								if(!n.getStaPrestando()) {
									donatore = n;
									min = this.grafo.getEdgeWeight(edge);
								}
							}
						}
					} else {
						//prendo tra i vicini al nerc quello con peso dell' arco minore 
						double min = Long.MAX_VALUE;
						List<Nerc> neighbors = Graphs.neighborListOf(this.grafo, nerc); //metodo di Graphs prende i vicini del nerc
						for(Nerc n : neighbors) {
							DefaultWeightedEdge edge = this.grafo.getEdge(nerc, n);
							if(this.grafo.getEdgeWeight(edge) < min) {
								if(!n.getStaPrestando()) {
									donatore = n;
									min = this.grafo.getEdgeWeight(edge);
								}
							}
						}
					}
					if(donatore != null) {
						System.out.println("\tTROVATO DONATORE: " + donatore);
						donatore.setStaPrestando(true); //il donatore sta prestando quindi non potrà essere scelto finchè non avrà finito di prestare
						Evento fine = new Evento(Evento.TIPO.FINE_INTERRUZIONE, e.getNerc(),
								donatore,e.getDataFine(), e.getDataInizio(), e.getDataFine());
						queue.add(fine);
						//devo terner traccia del prestito dentro alla mappa prestiti
						this.prestiti.get(donatore).add(e.getNerc()); //aggiungo il Nerc a cui il donatore ha donato
						Evento cancella  = new Evento(Evento.TIPO.CANCELLA_PRESTITO,
								e.getNerc(),donatore,e.getData().plusMonths(k),
								e.getDataInizio(),e.getDataFine());
						this.queue.add(cancella);
					} else {
						//CATASTROFE!!!
						System.out.println("\tCATASTROFE!!!!");
						this.CATASTROFI ++;
					}
					break;
				case FINE_INTERRUZIONE:
					System.out.println("FINE INTERRUZIONE NERC: " + e.getNerc());

					//assegnare un bonus al donatore
					if(e.getDonatore() != null)
						//devo aggiornare il bonus del donatore (bonus=durata in giorni del disservizio)
						this.bonus.put(e.getDonatore(), bonus.get(e.getDonatore()) + 
								Duration.between(e.getDataInizio(), e.getDataFine()).toDays());
					//dire che il donatore non sta più prestando
					e.getDonatore().setStaPrestando(false);
					
					break;
				case CANCELLA_PRESTITO:
					System.out.println("CANCELLAZIONE PRESTITO: " + e.getDonatore() + "-" + e.getNerc());
					//dalla mappa prestiti devo cancellare il Nerc del donatore ovvero il Nerc dentro al Set<Nerc>
					this.prestiti.get(e.getDonatore()).remove(e.getNerc());
					break;
			}
		}
	}
	
	
	public int getCatastrofi() {
		return this.CATASTROFI;
	}
	
	public Map<Nerc,Long> getBonus(){
		return this.bonus;
	}
	

}
