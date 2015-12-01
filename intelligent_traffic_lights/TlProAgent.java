package intelligent_traffic_lights;

import java.util.ArrayList;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import trasmapi.sumo.SumoEdge;

public class TlProAgent extends TlAgent{
	private static final long serialVersionUID = 6095960260125307076L;

	ArrayList<DFAgentDescription> vizinhosVertical, vizinhosHorizontal;
	
	public TlProAgent(String tlID) {
		super(tlID);
	}

	/* (non-Javadoc)
	 * @see jade.core.Agent#setup()
	 */
	@Override
	protected void setup() {
		vizinhosVertical = new ArrayList<>();
		vizinhosHorizontal = new ArrayList<>();
		DFAgentDescription ad = new DFAgentDescription();
		ad.setName(getAID()); //agentID
		System.out.println("AID: "+ad.getName());

		ServiceDescription sd = new ServiceDescription();
		sd.setName(getName()); //nome do agente    
		System.out.println("Nome: "+sd.getName());

		sd.setType("TrafficLight");
		System.out.println("Tipo: "+sd.getType()+"\n\n\n");
		ad.addServices(sd); 
		ServiceDescription sd2 = new ServiceDescription();
		sd2.setName(getName()); //nome do agente 
		sd2.setType("id: " + tl.getId());
		ad.addServices(sd2);
//		for(String edge : controlledLanes){
//			ServiceDescription sd2 = new ServiceDescription();
//			sd2.setName(getName()); //nome do agente 
//			sd2.setType("Lane: " + edge);
//			ad.addServices(sd2);
//		}

		try {
			DFService.register(this, ad);
		} catch(FIPAException e) {
			e.printStackTrace();
		}
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
//		for(SumoEdge edge : controlledEdges){
//			if(edge.getName().equals("horizontal")){
//				 // pesquisa DF
//		         searchVizinho(edge.getId(), vizinhosHorizontal);
//		         System.out.println(tl.getId()+": Vizinho: "+vizinhosHorizontal.get(vizinhosHorizontal.size()-1) +"h");
//			}else {
//				searchVizinho(edge.getId(), vizinhosVertical);
//				System.out.println(tl.getId()+": Vizinho: "+vizinhosVertical.get(vizinhosVertical.size()-1) +"v");
//			}
//		}
		
		int id = Integer.parseInt(tl.getId());
		searchVizinho(""+(id-1),vizinhosHorizontal);
		//System.out.println(tl.getId()+": Vizinho: "+vizinhosHorizontal.get(vizinhosHorizontal.size()-1) +" h");
		searchVizinho(""+(id+1),vizinhosHorizontal);
		//System.out.println(tl.getId()+": Vizinho: "+vizinhosHorizontal.get(vizinhosHorizontal.size()-1) +" h");
		searchVizinho(""+(id-4),vizinhosVertical);
		//System.out.println(tl.getId()+": Vizinho: "+vizinhosVertical.get(vizinhosVertical.size()-1) +" v");
		searchVizinho(""+(id+4),vizinhosVertical);
		//System.out.println(tl.getId()+": Vizinho: "+vizinhosVertical.get(vizinhosVertical.size()-1) +" v");
		
//		for(DFAgentDescription a : vizinhosHorizontal){
//			System.out.println(tl.getId()+": Vizinho: "+a +" h");
//		}for(DFAgentDescription a : vizinhosVertical){
//			System.out.println(tl.getId()+": Vizinho: "+a.getName() +" v");
//		}
		addBehaviour(new TlProBehaviour(this));
	}
	
	private void searchVizinho(String id, ArrayList<DFAgentDescription> vizinhos){
		DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType("id: "+id);
        template.addServices(sd1);
        try {
           DFAgentDescription[] result = DFService.search(this, template); // tendo os agentes registados podemos perguntar ao df quem é que tem um certo serviço que estejamos à procura
           for(int i=0; i<result.length; ++i){
           	vizinhos.add(result[i]);
           }
           /*
           // envia mensagem "pong" inicial a todos os agentes "ping"
           ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
           for(int i=0; i<result.length; ++i)
              msg.addReceiver(result[i].getName());// indicar quem é o receptor
           msg.setContent("pong");
           send(msg);*/
        } catch(FIPAException e) {
        	e.printStackTrace();
        }catch (Exception e) {
			System.err.println("POOOOOOOOOOOOOOOOOOOOOM");
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
		public class TlProBehaviour extends SimpleBehaviour {
			private static final long serialVersionUID = 1L;

			public TlProBehaviour(Agent a) {
				super(a);
			}

			@Override
			public void action() {
//				try {
//					int diff=verticalHasMoreCars(); 
//					if(diff>0){	
////						if(tl.getId().equals("5")){
////							System.out.println("VERTICAL");
////						}
//						if(!tl.getState().equals(verticalGreen))
//							changeState(horizontalYellow, verticalGreen);
//					}else if(diff<0){
////						if(tl.getId().equals("5")){
////							System.out.println("HORIZONTAL");
////						}
//						if(!tl.getState().equals(horizontalGreen))
//							changeState(verticalYellow,horizontalGreen);
//					}
//				} catch (UnimplementedMethod e) {
//					e.printStackTrace();
//				}
//				try {
//					Thread.sleep(5000);
//				} catch (InterruptedException e) {
//					//e.printStackTrace();
//				}
			}

			@Override
			public boolean done() {
				return false;
			}
		}
}

