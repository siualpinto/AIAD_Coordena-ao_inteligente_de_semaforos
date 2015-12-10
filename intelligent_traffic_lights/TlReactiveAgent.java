package intelligent_traffic_lights;

/**
 * 		Funcionamento deste agente:
 * 			1-> verifica qual a direção que tem mais carros
 * 			2-> fica verde para essa direção
 * 			3-> espera 5s e volta a 1
 */

import trasmapi.genAPI.exceptions.UnimplementedMethod;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class TlReactiveAgent extends TlAgent{
	private static final long serialVersionUID = 1L;
	private int lastTimeStoped;
	public TlReactiveAgent(String tlID) {
		super(tlID);
		lastTimeStoped=0;
	}

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
		addBehaviour(new TlReactiveBehaviour(this));
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


	// ==================================================================================
	//    NestedClass
	// ==================================================================================
	public class TlReactiveBehaviour extends SimpleBehaviour {
		private static final long serialVersionUID = 1L;

		public TlReactiveBehaviour(Agent a) {
			super(a);
		}

		@Override
		public void action() {
//			try {
//				int numCarrosVH[] = verticalHasMoreCars();
//				int diff=numCarrosVH[0]-numCarrosVH[1]; 
//				if(diff>0){	
//					if(!tl.getState().equals(verticalGreen))
//						changeState(horizontalYellow, verticalGreen);
//
//				}else if(diff<0){
//					if(!tl.getState().equals(horizontalGreen))
//						changeState(verticalYellow,horizontalGreen);
//				}
//			} catch (UnimplementedMethod e) {
//				e.printStackTrace();
//			}
//			try {
//				Thread.sleep(SLEEP_TIME);
//			} catch (InterruptedException e) {
//				//e.printStackTrace();
//			}
			chooseDirection();
		}

		@Override
		public boolean done() {
			return false;
		}
	}
	public void chooseDirection(){
		try {
			int numCarrosVH[] = verticalHasMoreCars();
			int diff=numCarrosVH[0]-numCarrosVH[1]; 
			if(diff>0){	// mais carros na vertical
				if(!tl.getState().equals(verticalGreen)){ // está na horizontal
					lastTimeStoped = numCarrosVH[0];
					changeState(horizontalYellow, verticalGreen);
					print("muda vertical");
				}else{ // manter vertical
					if(lastTimeStoped <= numCarrosVH[0]){ // esteve vertical mas ninguem passou
						lastTimeStoped = numCarrosVH[1];
						changeState(verticalYellow,horizontalGreen); // logo troca
						print("muda horizontal, ninguem passou vertical");
					}else print("mantem vertical");
				}
			}else if(diff<0){ // mais carros na horizontal
				if(!tl.getState().equals(horizontalGreen)){ // está vertical
					lastTimeStoped = numCarrosVH[1];
					changeState(verticalYellow,horizontalGreen);
					print("muda horizontal");
				}else {// manter horizontal
					if(lastTimeStoped <= numCarrosVH[1]){ // esteve horizontal mas ninguem passou
						lastTimeStoped = numCarrosVH[0];
						changeState(horizontalYellow, verticalGreen); // logo troca
						print("muda vertical, ninguem passou horizontal");
					}else print("mantem horizontal");
				}
			} else { // tem numero igual de carros em cada direção
				if(tl.getState().equals(verticalGreen)){// está vertical
					if(lastTimeStoped <= numCarrosVH[0]){ // esteve vertical mas ninguem passou
						lastTimeStoped = numCarrosVH[1];
						changeState(verticalYellow,horizontalGreen); // logo troca
						print("muda horizontal, ninguem passou vertical #2");
					}else print("mantem #1");
				}else if(tl.getState().equals(horizontalGreen)){ // está horizontal
					if(lastTimeStoped <= numCarrosVH[1]){ // esteve horizontal mas ninguem passou
						lastTimeStoped = numCarrosVH[0];
						changeState(horizontalYellow, verticalGreen); // logo troca
						print("muda vertical, ninguem passou horizontal #2");
					}else print("mantem #2");
				}
			}
		} catch (UnimplementedMethod e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e) {
			//e.printStackTrace();
		}
	}
}

