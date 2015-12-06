package intelligent_traffic_lights;
import trasmapi.genAPI.exceptions.UnimplementedMethod;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class TlTimerAgent extends TlAgent{
	private static final long serialVersionUID = 1L;
	public TlTimerAgent(String tlID) {
		super(tlID);
		
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
		addBehaviour(new TlTimerBehaviour(this));
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
	public class TlTimerBehaviour extends SimpleBehaviour {
		private static final long serialVersionUID = 1L;

		public TlTimerBehaviour(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			try {
				if(!tl.getState().equals(verticalGreen)){
					changeState(horizontalYellow, verticalGreen);
				}else if(!tl.getState().equals(horizontalGreen))
					changeState(verticalYellow,horizontalGreen);
			} catch (UnimplementedMethod e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {}
		}

		@Override
		public boolean done() {
			return false;
		}
	}
}

