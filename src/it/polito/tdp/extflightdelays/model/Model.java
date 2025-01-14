package it.polito.tdp.extflightdelays.model;

import java.nio.channels.NonWritableChannelException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {
	
	ExtFlightDelaysDAO dao;
	List<Airport> vertex;
	SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	
	List<Airport> best;
	private double bestVal;
	
	public Model(){
		
		dao = new ExtFlightDelaysDAO();

	}

	public void creaGrafo(int n) {
		// TODO Auto-generated method stub
		grafo = new SimpleWeightedGraph<Airport, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		vertex = new ArrayList<Airport>();
		
		for(Airport airport : dao.loadAllAirports()) {
			if(dao.superaNumero(n,airport)) {
				vertex.add(airport);
				System.out.println("Aggiunto vertice!");
			}
		}
		
		Graphs.addAllVertices(grafo, vertex);
		
		for(Airport a1 : vertex) {
			for(Airport a2 : vertex) {
				double a;
				if(!a1.equals(a2)) {
				if((a = dao.isEdge(a1,a2)) > 0 && grafo.getEdge(a1, a2) == null && grafo.getEdge(a2, a1) == null) {
					Graphs.addEdge(grafo,a1 ,a2 ,a);
					System.out.println("Aggiunto!");
					System.out.println("arco tra: "+a1+" e "+a2);
				}
				}else System.out.println("Stesso aeroporto!");
			}
		}
		
		System.out.println("#vertici: "+grafo.vertexSet().size());
		System.out.println("#archi: "+grafo.edgeSet().size());
		
	}

	public List<Airport> getVertex() {
		// TODO Auto-generated method stub
		return this.vertex;
	}

	public List<String> getConnessi(Airport airport) {
		// TODO Auto-generated method stub
		List<String> string = new ArrayList<String>();
		
		/*List<DefaultWeightedEdge> outgoing = new ArrayList<DefaultWeightedEdge>(grafo.outgoingEdgesOf(airport));
		
		Collections.sort(outgoing, new Comparator<DefaultWeightedEdge>() {

			@Override
			public int compare(DefaultWeightedEdge o1, DefaultWeightedEdge o2) {
				// TODO Auto-generated method stub
				return (int)(grafo.getEdgeWeight(o1) - grafo.getEdgeWeight(o2));
			}
		});
		
		for(DefaultWeightedEdge edge : outgoing) {
			
			System.out.println(grafo.getEdgeTarget(edge));
			Airport a = grafo.getEdgeTarget(edge);
			if(airport.equals(a))
				a = grafo.getEdgeSource(edge);
			string.add(a+" con peso: "+grafo.getEdgeWeight(edge)+"\n");
		}
		
		for(Airport airport2 : Graphs.neighborListOf(grafo, airport))
			System.out.println(airport2);*/
		
		List<Airport> vicini = Graphs.neighborListOf(grafo, airport);
		
		Collections.sort(vicini,new Comparator<Airport>() {

			@Override
			public int compare(Airport o1, Airport o2) {
				// TODO Auto-generated method stub
				DefaultWeightedEdge edge1 = grafo.getEdge(airport, o1);
				DefaultWeightedEdge edge2 = grafo.getEdge(airport, o2);
				return (int)(grafo.getEdgeWeight(edge1)-grafo.getEdgeWeight(edge2));
			}

		});
		
		for(Airport airport2 : vicini) {
			
			DefaultWeightedEdge edge = grafo.getEdge(airport, airport2);
			
			string.add(airport2.getAirportName()+" con peso: "+grafo.getEdgeWeight(edge)+"\n");
		}
		
		return string;
	}

	public List<Airport> cercaItinerario(double n, Airport airport) {
		// TODO Auto-generated method stub
		List<Airport> parziale = new ArrayList<Airport>();
		
		best = new ArrayList<Airport>();
		
		cerca(parziale,n,airport);
		
		for(Airport airport2 : best)
			System.out.println(airport2.getAirportName());
		return best;
	}

	private void cerca(List<Airport> parziale, double n, Airport airport) {
		// TODO Auto-generated method stub
		System.out.println("CERCO");
		
		System.out.println(getOre(parziale, airport,null)+" ore!");
		System.out.println(getOre(best, airport,null)+" ore!");
			
			if(getOre(best, airport,null) < getOre(parziale, airport,null)) {
			best = new ArrayList<Airport>(parziale);
			System.out.println("NEW BEST");
			}
			
		List<Airport> vicini = Graphs.neighborListOf(grafo, airport);
		
		Collections.sort(vicini,new Comparator<Airport>() {

			@Override
			public int compare(Airport o1, Airport o2) {
				// TODO Auto-generated method stub
				DefaultWeightedEdge edge1 = grafo.getEdge(airport, o1);
				DefaultWeightedEdge edge2 = grafo.getEdge(airport, o2);
				return (int)(grafo.getEdgeWeight(edge1)-grafo.getEdgeWeight(edge2));
			}
		});
		
		for(Airport airport2 : vicini) {
			
			if(!parziale.contains(airport2) && getOre(parziale,airport,airport2) <= n) {
				
				for(Airport a : parziale)
					System.out.println(a.getAirportName()+"\n");
				parziale.add(airport2);
				System.out.println("AGGIUNGO");
				cerca(parziale, n, airport);
				parziale.remove(parziale.size()-1);
			} else {
				System.out.println("ESCO");
				return;
			}
		}
	}

	private double getOre(List<Airport> best2, Airport airport, Airport airport2) {
		// TODO Auto-generated method stub
		double somma = 0.0;
		
		for(Airport a: best2) {
			
			DefaultWeightedEdge edge = grafo.getEdge(airport, a);
			somma += grafo.getEdgeWeight(edge) * 2;
			
		}
		
		if(airport2 != null) {
			DefaultWeightedEdge edge = grafo.getEdge(airport, airport2);
			somma += grafo.getEdgeWeight(edge) * 2;
		}
		
		return somma;
	}
}
