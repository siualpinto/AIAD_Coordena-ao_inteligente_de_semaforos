/**
 * 							*****************
 * 							*	TlAgent		*
 * 							*****************
 * 								    /\
 * 									|
 *	 			--------------------+------------
 * 				|								|
 * 		**************					************									
 * 		*TlBasicAgent*					*TlProAgent*
 * 		**************					************
 *
 */

package intelligent_traffic_lights;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import jade.core.Agent;
import trasmapi.genAPI.TrafficLight;
import trasmapi.genAPI.exceptions.UnimplementedMethod;
import trasmapi.sumo.SumoEdge;
import trasmapi.sumo.SumoTrafficLight;
import trasmapi.sumo.SumoVehicle;

public class TlAgent extends Agent {
	private static final long serialVersionUID = 1L;
	public static double STOP_SPEED = 0.5;
	public static int SLEEP_TIME = 5000;
	public static String DEBUG_SEM = "6";
	private String id;
	public TrafficLight tl;
	public ArrayList<String> controlledLanes;
	public ArrayList<SumoEdge> controlledEdges, neighborEdges;
	/**
	 * Atenção que o uso de vertical e horizontal no nome das seguintes variáveis é só para orientação
	 * porque na realidade as direções podem estar trocadas, todo depende do numero de estradas que cruzam no semaforo
	 * e em que posição. 
	 * 
	 */
	public String horizontalGreen, verticalGreen, horizontalYellow, verticalYellow;
	public ArrayList<Integer> verticalIndex,horizontalIndex;

	public TlAgent(String tlID) {
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

			neighborEdges = new ArrayList<SumoEdge>();
			controlledEdges = new ArrayList<SumoEdge>();
			String edge="";
			for (String edgeId : controlledLanes) {
				edge=edgeId.split("_")[0];
				controlledEdges.add(new SumoEdge(edge));
				if(edge.charAt(edge.length()-1)== '2'){
					neighborEdges.add(new SumoEdge(edge.substring(0, 1)));
				}else neighborEdges.add(new SumoEdge(edge+"2"));
			}

			tl.setState(horizontalGreen);

		} catch (UnimplementedMethod e) {
			e.printStackTrace();
		}
	}


	/**
	 * Troca a direção que está verde
	 * @param yellow
	 * @param green
	 */
	public void changeState(String yellow, String green){
		try {
			tl.setState(yellow);
			Thread.sleep(1000);
			tl.setState(green);
		} catch (UnimplementedMethod e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			//e.printStackTrace();
		}
	}

	/**
	 * Obter a direção que tem mais carros
	 * @return retorna true se tem mais carros na vertical do que na horizontal
	 */
	public int[] verticalHasMoreCars(){

		int sumHorizontalParados=0,sumVerticalParados=0;
		for (int i : verticalIndex) {
			for(SumoVehicle c : controlledEdges.get(i).vehiclesList()){
				if(c.getSpeed() <= STOP_SPEED){
					sumVerticalParados++;
				}
			}
		}
		for (int i : horizontalIndex) {
			for(SumoVehicle c : controlledEdges.get(i).vehiclesList()){
				if(c.getSpeed() <= STOP_SPEED){
					sumHorizontalParados++;
				}
			}
		}

		//print("carros H:" +sumHorizontalParados+ "V: "+sumVerticalParados);
		return new int[] {sumVerticalParados,sumHorizontalParados};
	}

	public String stringOfSize(int size, char ch)
	{
		final char[] array = new char[size];
		Arrays.fill(array, ch);
		return new String(array);
	}
	protected void print(String s){
		if(tl.getId().equals(DEBUG_SEM)){
			System.out.println("Agent=>  "+tl.getId()+" =>  "+s);
		}
	}
}
