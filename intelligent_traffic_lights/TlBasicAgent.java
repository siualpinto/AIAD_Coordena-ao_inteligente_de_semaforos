package intelligent_traffic_lights;

/**
 * 			FORMATO DO ESTADO DO SEMAFORO / ordem das cores :
 * 			[CIMA,DIREITA,BAIXO,ESQUERDA]
 * 			se o semaforo tiver 4 estradas
 * 
 * 		Funcionamento deste agente:
 * 			1-> verifica qual a direção que tem mais carros
 * 			2-> fica verde para essa direção
 * 			3-> espera 5s e volta a 1
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import trasmapi.genAPI.TrafficLight;
import trasmapi.genAPI.exceptions.UnimplementedMethod;
import trasmapi.sumo.SumoEdge;
import trasmapi.sumo.SumoTrafficLight;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class TlBasicAgent extends Agent{
	private static final long serialVersionUID = 1L;
	private String id;
	public TrafficLight tl;
	public ArrayList<String> controlledLanes;
	public ArrayList<SumoEdge> controlledEgdes;
	
	/**
	 * Atenção que o uso de vertical e horizontal no nome das seguintes variáveis é só para orientação
	 * porque na realidade as direções podem estar trocadas, todo depende do numero de estradas que cruzam no semaforo
	 * e em que posição. 
	 * 
	 */
	public String horizontalGreen, verticalGreen, horizontalYellow, verticalYellow;
	public ArrayList<Integer> verticalIndex,horizontalIndex;

	public TlBasicAgent(String tlID) {
		super();
		try {

			System.out.println("TlAgent id: "+this.id);

			tl = new SumoTrafficLight(tlID);

			controlledLanes = tl.getControlledLanes();
			//remover duplicados
			Set<String> setItems = new LinkedHashSet<String>(controlledLanes);
			controlledLanes.clear();
			controlledLanes.addAll(setItems);
			//
			
			// ========= definição das cores para verde horizontal ou vertical
			int numCoresPorSemaforo = tl.getState().length()/controlledLanes.size();
			String red = stringOfSize(numCoresPorSemaforo, 'r');
			String green = stringOfSize(numCoresPorSemaforo, 'g');
			String yellow = stringOfSize(numCoresPorSemaforo, 'y');
			verticalGreen=verticalYellow=horizontalGreen=horizontalYellow="";
			verticalIndex= new ArrayList<Integer>();
			horizontalIndex= new ArrayList<Integer>();
			String st = tl.getState();
			st=st.replace('G', 'g');
			st=st.replace('R', 'r');
			char color = st.charAt(0);
			int index=0;
			for (int i = 0; i < st.length(); i+=numCoresPorSemaforo) {
				if(st.charAt(i)==color){
					verticalGreen+=green;
					verticalYellow+=yellow;
					horizontalGreen+=red;
					horizontalYellow+=red;
					verticalIndex.add(index);
				}else {
					verticalGreen+=red;
					verticalYellow+=red;
					horizontalGreen+=green;
					horizontalYellow+=yellow;
					horizontalIndex.add(index);
				}
				index++;
			}

			controlledEgdes = new ArrayList<SumoEdge>();
			for (String edgeId : controlledLanes) {
				controlledEgdes.add(new SumoEdge(edgeId.split("_")[0]));
			}
			tl.setState(horizontalGreen);

		} catch (UnimplementedMethod e) {
			e.printStackTrace();
		}
	}


	/* (non-Javadoc)
	 * @see jade.core.Agent#setup()
	 */
	@Override
	protected void setup() {
		DFAgentDescription ad = new DFAgentDescription();
		ad.setName(getAID()); //agentID
		System.out.println("AID: "+ad.getName());

		ServiceDescription sd = new ServiceDescription();
		sd.setName(getName()); //nome do agente    
		System.out.println("Nome: "+sd.getName());

		sd.setType("TrafficLight");
		System.out.println("Tipo: "+sd.getType()+"\n\n\n");
		addBehaviour(new TlBasicBehaviour(this));
		ad.addServices(sd); 

		try {
			DFService.register(this, ad);
		} catch(FIPAException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void takeDown() {
		try {
			DFService.deregister(this);  
		} catch(FIPAException e) {
			e.printStackTrace();
		}
		super.takeDown();
	}

	/**
	 * Troca a direção que está verde
	 * @param yellow
	 * @param green
	 */
	private void changeState(String yellow, String green){
		try {
			tl.setState(yellow);
			Thread.sleep(1000);
			tl.setState(green);
		} catch (UnimplementedMethod e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Obter a direção que tem mais carros
	 * @return retorna true se tem mais carros na vertical do que na horizontal
	 */
	public boolean verticalHaveMoreCars(){
		int sumHorizontal=0,sumVertical=0;
		for (int i : verticalIndex) {
			sumVertical+=controlledEgdes.get(i).getNumVehicles();
		}
		for (int i : horizontalIndex) {
			sumHorizontal+=controlledEgdes.get(i).getNumVehicles();
		}
		return (sumVertical>=sumHorizontal);
	}

	public String stringOfSize(int size, char ch)
	{
		final char[] array = new char[size];
		Arrays.fill(array, ch);
		return new String(array);
	}

	// ==================================================================================
	//    NestedClass
	// ==================================================================================
	public class TlBasicBehaviour extends SimpleBehaviour {
		private static final long serialVersionUID = 1L;

		public TlBasicBehaviour(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			try {
				if(verticalHaveMoreCars()){	
					if(!tl.getState().equals(verticalGreen))
						changeState(horizontalYellow, verticalGreen);
				}else{
					if(!tl.getState().equals(horizontalGreen))
						changeState(verticalYellow,horizontalGreen);
				}
			} catch (UnimplementedMethod e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		@Override
		public boolean done() {
			return false;
		}
	}
}

