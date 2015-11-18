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

public class TlBasicAgent extends TlAgent{
	private static final long serialVersionUID = 1L;
	public TlBasicAgent(String tlID) {
		super(tlID);
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
				int diff =verticalHasMoreCars(); 
				if(diff>0){	
					if(tl.getId().equals("5")){
						System.out.println("VERTICAL");
					}
					if(!tl.getState().equals(verticalGreen))
						changeState(horizontalYellow, verticalGreen);
				}else if(diff<0){
					if(tl.getId().equals("5")){
						System.out.println("HORIZONTAL");
					}
					if(!tl.getState().equals(horizontalGreen))
						changeState(verticalYellow,horizontalGreen);
				}
			} catch (UnimplementedMethod e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}

		@Override
		public boolean done() {
			return false;
		}
	}
}

