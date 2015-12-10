package intelligent_traffic_lights;

import java.util.ArrayList;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import trasmapi.genAPI.exceptions.UnimplementedMethod;

public class TlDeliberativeAgent extends TlAgent{
	private static final long serialVersionUID = 6095960260125307076L;

	ArrayList<DFAgentDescription> vizinhosVertical, vizinhosHorizontal;
	String direcaoEscolhida="";
	public TlDeliberativeAgent(String tlID) {
		super(tlID);
	}


	@Override
	protected void setup() {

		if(tl.getId().equals("13") || tl.getId().equals("14")){// semaforos do canto de cima, tem direcoes trocadas
			String tmp = horizontalGreen;
			horizontalGreen=verticalGreen;
			verticalGreen=tmp;

			ArrayList<Integer> tmpA = new ArrayList<Integer>();
			tmpA= horizontalIndex;
			horizontalIndex=verticalIndex;
			verticalIndex=tmpA;
		}
		try {
			if(tl.getState().equals(horizontalGreen)){
				direcaoEscolhida="HORIZONTAL";
			}else direcaoEscolhida="VERTICAL";			
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
	public class TlProBehaviour extends SimpleBehaviour {
		private static final long serialVersionUID = 1L;
		private int lastTimeStoped;

		public TlProBehaviour(Agent a) {
			super(a);
			lastTimeStoped=0;
		}

		@Override
		public void action() {


			int numCarrosVH[] = verticalHasMoreCars();
			chooseDirection(numCarrosVH);
//			int diff=numCarrosVH[0]-numCarrosVH[1]; 
//			if(diff>0){	
//				direcaoEscolhida="VERTICAL";
//			}else if(diff<0){
//				direcaoEscolhida="HORIZONTAL";
//			}

			print("Tenho mais Carros em: "+direcaoEscolhida);

			for(DFAgentDescription a : vizinhosHorizontal){
				sendMessage(a);
			}for(DFAgentDescription a : vizinhosVertical){
				sendMessage(a);
			}

			int respostas=0,perguntas=0;
			int total = vizinhosVertical.size()+vizinhosHorizontal.size();
			ArrayList<String>respostasHorizontal, respostasVertical;
			respostasHorizontal= new ArrayList<>();
			respostasVertical= new ArrayList<>();
			while(!(respostas==total && perguntas==total)){// enquanto não receber respostas e perguntas dos vizinhos
				ACLMessage msg = blockingReceive(); // bloquear até receber a mensagem
				if(msg.getPerformative() == ACLMessage.QUERY_REF) {
					print("Pergunta: " + msg.getContent() +" de: "+msg.getSender().getName());
					// cria resposta
					ACLMessage reply = msg.createReply();//adicionar o receptor e emissor. cria a mensagem 
					// preenche conteúdo da mensagem
					if(msg.getContent().equals("Direcao")){
						if(numCarrosVH[0]==0 && numCarrosVH[1]==0)
							reply.setContent("NENHUMA");
						else reply.setContent(direcaoEscolhida);
						reply.setPerformative(ACLMessage.INFORM_REF);
						// envia mensagem
						send(reply);
						print("Respondi: "+direcaoEscolhida+" para: "+msg.getSender().getName());
						perguntas++;
					}else print("=====Não sei responder a: "+msg.getContent()+ "de: "+msg.getSender().getName()+"======");

				}else if(msg.getPerformative() == ACLMessage.INFORM_REF){
					String nome = msg.getSender().getName();
					print("Resposta de: "+nome+" msg: "+msg.getContent());
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

			print("Votos => HH: "+horizontal+" VV: "+vertical);
			String direcaoFinal="";

			if(direcaoEscolhida.equals("HORIZONTAL"))
				horizontal++;
			else if(direcaoEscolhida.equals("VERTICAL"))
				vertical++;

			if(horizontal==vertical){
				direcaoFinal=direcaoEscolhida;
			}else if(horizontal>vertical){
				direcaoFinal="HORIZONTAL";
			}else if(horizontal<vertical){
				direcaoFinal="VERTICAL";
			}

			print("RESULTADO: "+direcaoFinal);
			try {
				if(direcaoFinal.equals("HORIZONTAL")){
					if(!tl.getState().equals(horizontalGreen))
						changeState(verticalYellow,horizontalGreen);
				}else if(!tl.getState().equals(verticalGreen))
					changeState(horizontalYellow, verticalGreen);
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

		public void chooseDirection(int numCarrosVH[]){
			try {
				int diff=numCarrosVH[0]-numCarrosVH[1]; 
				if(diff>0){	// mais carros na vertical
					if(!tl.getState().equals(verticalGreen)){ // está na horizontal
						lastTimeStoped = numCarrosVH[0];
						direcaoEscolhida="VERTICAL";
						print("muda vertical");
					}else{ // manter vertical
						if(lastTimeStoped <= numCarrosVH[0]){ // esteve vertical mas ninguem passou
							lastTimeStoped = numCarrosVH[1];
							direcaoEscolhida="HORIZONTAL";
							print("muda horizontal, ninguem passou vertical");
						}else print("mantem vertical");
					}
				}else if(diff<0){ // mais carros na horizontal
					if(!tl.getState().equals(horizontalGreen)){ // está vertical
						lastTimeStoped = numCarrosVH[1];
						direcaoEscolhida="HORIZONTAL";
						print("muda horizontal");
					}else {// manter horizontal
						if(lastTimeStoped <= numCarrosVH[1]){ // esteve horizontal mas ninguem passou
							lastTimeStoped = numCarrosVH[0];
							direcaoEscolhida="VERTICAL";
							print("muda vertical, ninguem passou horizontal");
						}else print("mantem horizontal");
					}
				} else { // tem numero igual de carros em cada direção
					if(tl.getState().equals(verticalGreen)){// está vertical
						if(lastTimeStoped <= numCarrosVH[0]){ // esteve vertical mas ninguem passou
							lastTimeStoped = numCarrosVH[1];
							direcaoEscolhida="HORIZONTAL";
							print("muda horizontal, ninguem passou vertical #2");
						}else print("mantem #1");
					}else if(tl.getState().equals(horizontalGreen)){ // está horizontal
						if(lastTimeStoped <= numCarrosVH[1]){ // esteve horizontal mas ninguem passou
							lastTimeStoped = numCarrosVH[0];
							direcaoEscolhida="VERTICAL";
							print("muda vertical, ninguem passou horizontal #2");
						}else print("mantem #2");
					}
				}
			} catch (UnimplementedMethod e) {
				e.printStackTrace();
			}
		}
	}


	private void sendMessage(DFAgentDescription a){
		ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
		msg.addReceiver(a.getName());// indicar quem é o receptor
		msg.setContent("Direcao");
		send(msg);
		print("Pergunta para: "+a.getName() );
	}
}

