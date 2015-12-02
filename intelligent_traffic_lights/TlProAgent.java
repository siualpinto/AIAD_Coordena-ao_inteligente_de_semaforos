package intelligent_traffic_lights;

import java.util.ArrayList;
import java.util.Iterator;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import trasmapi.genAPI.exceptions.UnimplementedMethod;
import trasmapi.sumo.SumoEdge;

public class TlProAgent extends TlAgent{
	private static final long serialVersionUID = 6095960260125307076L;

	ArrayList<DFAgentDescription> vizinhosVertical, vizinhosHorizontal;
	String direcaoEscolhida="";
	public TlProAgent(String tlID) {
		super(tlID);
	}

	/* (non-Javadoc)
	 * @see jade.core.Agent#setup()
	 */
	@Override
	protected void setup() {

		if(tl.getId().equals("13") || tl.getId().equals("14")){
			//public String horizontalGreen, verticalGreen, horizontalYellow, verticalYellow;
			String tmp = horizontalGreen;
			horizontalGreen=verticalGreen;
			verticalGreen=tmp;
			//public ArrayList<Integer> verticalIndex,horizontalIndex;

			ArrayList<Integer> tmpA = new ArrayList<Integer>();
			copyArrayList(verticalIndex, tmpA);
			copyArrayList(horizontalIndex, verticalIndex);
			copyArrayList(tmpA, horizontalIndex);

		}
		try {
			tl.setState(horizontalGreen);
			direcaoEscolhida="HORIZONTAL";
		} catch (UnimplementedMethod e1) {
			e1.printStackTrace();
		}
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
		searchVizinho(""+(id+1),vizinhosHorizontal);
		searchVizinho(""+(id-4),vizinhosVertical);
		searchVizinho(""+(id+4),vizinhosVertical);


		for(DFAgentDescription a : vizinhosHorizontal){
			System.out.println(tl.getId()+": Vizinho: "+a.getName() +" h");
		}for(DFAgentDescription a : vizinhosVertical){
			System.out.println(tl.getId()+": Vizinho: "+a.getName() +" v");
		}
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

		/*BUG HEREEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE*/
			int diff=verticalHasMoreCars(); 
			if(diff>0){	
				//						if(tl.getId().equals("5")){
				//							System.out.println("VERTICAL");
				//						}
				direcaoEscolhida="VERTICAL";
				//						if(!tl.getState().equals(verticalGreen))
				//							changeState(horizontalYellow, verticalGreen);
			}else if(diff<0){
				//						if(tl.getId().equals("5")){
				//							System.out.println("HORIZONTAL");
				//						}
				direcaoEscolhida="HORIZONTAL";
				//						if(!tl.getState().equals(horizontalGreen))
				//							changeState(verticalYellow,horizontalGreen);
			}
			print("Escolhi0: "+direcaoEscolhida);
			//			try {
			//				Thread.sleep(1000);
			//			} catch (InterruptedException e1) {
			//				e1.printStackTrace();
			//			}

			for(DFAgentDescription a : vizinhosHorizontal){
				//System.out.println(tl.getId()+": Vizinho: "+a.getName() +" h");
				// envia mensagem "pong" inicial a todos os agentes "ping"
				sendMessage(a);
			}for(DFAgentDescription a : vizinhosVertical){
				//System.out.println(tl.getId()+": Vizinho: "+a.getName() +" v");
				sendMessage(a);
			}

			int respostas=0,perguntas=0;
			int total = vizinhosVertical.size()+vizinhosHorizontal.size();
			ArrayList<String>respostasHorizontal, respostasVertical;
			respostasHorizontal= new ArrayList<>();
			respostasVertical= new ArrayList<>();
			while(!(respostas==total && perguntas==total)){
				ACLMessage msg = blockingReceive(); // bloquear até receber a mensagem
				if(msg.getPerformative() == ACLMessage.QUERY_REF) {
					print("Recebi: " + msg.getContent() +" de: "+msg.getSender().getName());
					// cria resposta
					ACLMessage reply = msg.createReply();//adicionar o receptor e emissor. cria a mensagem 
					// preenche conteúdo da mensagem
					if(msg.getContent().equals("Direcao")){
						reply.setContent(direcaoEscolhida);
						reply.setPerformative(ACLMessage.INFORM_REF);
						// envia mensagem
						send(reply);
						print("Respondi: "+direcaoEscolhida+" para: "+msg.getSender().getName());
						perguntas++;
					}else print("Não sei responder a: "+msg.getContent()+ "de: "+msg.getSender().getName());

				}else if(msg.getPerformative() == ACLMessage.INFORM_REF){
					String nome = msg.getSender().getName();
					print("resposta de: "+nome+" msg: "+msg.getContent());
					for(DFAgentDescription a : vizinhosHorizontal){
						if(a.getName().equals(nome)){
							respostasHorizontal.add(msg.getContent());
							respostas++;
						}
					}
					for(DFAgentDescription a : vizinhosVertical){
						if(a.getName().equals(nome)){
							respostasVertical.add(msg.getContent());
							respostas++;
						}
					}

				}
				print("total: "+total+" respostas: "+respostas+" perguntas: "+perguntas);
			}

			int horizontal=0;
			for(String s : respostasHorizontal){
				if(s.equals("HORIZONTAL"))
					horizontal++;
			}
			int vertical=0;
			for(String s : respostasVertical){
				if(s.equals("VERTICAL"))
					vertical++;
			}

			print("HH: "+horizontal+" VV: "+vertical);
			String direcaoFinal="";

			if(direcaoEscolhida.equals("HORIZONTAL"))
				horizontal++;
			else vertical++;

			if(total<4){
				direcaoFinal=direcaoEscolhida;
			}else {
				if(horizontal==vertical){
					direcaoFinal=direcaoEscolhida;
				}else if(horizontal>vertical){
					direcaoFinal="HORIZONTAL";
				}else if(horizontal<vertical){
					direcaoFinal="VERTICAL";
				}else direcaoFinal=direcaoEscolhida;
			}
			print("Escolhi: "+direcaoFinal);
			try {
				if(direcaoFinal.equals("HORIZONTAL")){
					if(!tl.getState().equals(horizontalGreen))
						changeState(verticalYellow,horizontalGreen);

				}else if(!tl.getState().equals(verticalGreen))
					changeState(horizontalYellow, verticalGreen);
			} catch (UnimplementedMethod e) {
				// TODO Auto-generated catch block
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

	private void sendMessage(DFAgentDescription a){
		ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
		msg.addReceiver(a.getName());// indicar quem é o receptor
		msg.setContent("Direcao");
		send(msg);
		print("Pergunta para: "+a.getName() );
	}

	private void copyArrayList(ArrayList<Integer>a , ArrayList<Integer> b){
		b = new ArrayList<Integer>();
		for(Integer i : a){
			b.add(i);
		}
	}
	private void print(String s){
		if(tl.getId().equals("14")){
			System.out.println(s);
		}
	}
}

