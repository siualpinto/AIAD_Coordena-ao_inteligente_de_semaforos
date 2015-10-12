package intelligent_traffic_lights;


import java.util.ArrayList;
import trasmapi.genAPI.TrafficLight;
import trasmapi.genAPI.exceptions.UnimplementedMethod;
import trasmapi.sumo.SumoTrafficLight;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class TlProAgent extends Agent{
	private static final long serialVersionUID = 6095960260125307076L;
	private String id;
	public TrafficLight tl;
	public ArrayList<String> controlledLanes;

	public TlProAgent(String tlID) {

		super();
		try {
			
			System.out.println("TlAgent id: "+this.id);

			tl = new SumoTrafficLight(tlID);
			//tl.setState("gggg");
			//System.out.println("state: "+tl.getState());
			controlledLanes = tl.getControlledLanes();
//			for (String string : controlledLanes) {
//				System.out.println("\tRua: "+string);
//			}
			
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

}

