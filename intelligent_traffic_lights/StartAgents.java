package intelligent_traffic_lights;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import jade.BootProfileImpl;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import trasmapi.genAPI.Simulator;
import trasmapi.genAPI.TraSMAPI;
import trasmapi.genAPI.Vehicle;
import trasmapi.genAPI.exceptions.TimeoutException;
import trasmapi.genAPI.exceptions.UnimplementedMethod;
import trasmapi.sumo.Sumo;
import trasmapi.sumo.SumoCom;
import trasmapi.sumo.SumoTrafficLight;
import trasmapi.sumo.SumoVehicle;

public class StartAgents {
	static boolean JADE_GUI = true;
	private static ProfileImpl profile;
	private static ContainerController mainContainer;
	private static int  numCarrros;
	public static Random rand;
	public static boolean finish;
	long finishTime = 0;
	public StartAgents(String mode, boolean flows){	
		finish=false;
		numCarrros=350;
		rand = new Random(1);
		if(JADE_GUI){
			List<String> params = new ArrayList<String>();
			params.add("-gui");
			profile = new BootProfileImpl(params.toArray(new String[0]));
		} else
			profile = new ProfileImpl();

		Runtime rt = Runtime.instance();
		mainContainer = rt.createMainContainer(profile);

		TraSMAPI api = new TraSMAPI(); 

		//Create SUMO
		Simulator sumo = new Sumo("guisim");
		List<String> params = new ArrayList<String>();
		if(flows)
			params.add("-c=bin\\map\\hello.sumo.cfg");
		else params.add("-c=bin\\map\\helloRandom.sumo.cfg");
		try {
			sumo.addParameters(params);
			sumo.addConnections("localhost", 8820);
		} catch (UnimplementedMethod e2) {
			e2.printStackTrace();
		}


		//Add Sumo to TraSMAPI
		api.addSimulator(sumo);


		//Launch and Connect all the simulators added
		try {
			api.launch();
			api.connect();
			api.start();
		} catch (IOException | UnimplementedMethod | TimeoutException e2) {
			e2.printStackTrace();
		}


		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		ArrayList<String> tlIds = new ArrayList<>();
		tlIds=SumoTrafficLight.getIdList();


		for (String id : tlIds) {
			try {
				if(mode.equals("temporizador")){
					mainContainer.acceptNewAgent("TimerTL#"+id, new TlTimerAgent(id)).start();
				}else if(mode.equals("reactAgents")){
					mainContainer.acceptNewAgent("ReactTL#"+id, new TlReactiveAgent(id)).start();
				}else if(mode.equals("delibAgents")){
					mainContainer.acceptNewAgent("DelibTL#"+id, new TlDeliberativeAgent(id)).start();
				}
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
		}
		SumoCom.createAllRoutes();
		Thread t = new Thread(){
			public void run(){
				long startTime = System.currentTimeMillis();

				while(true){
					try {
						if(!api.simulationStep(0) || (SumoCom.getAllVehiclesIds().size()<=0 && (flows ||(!flows && numCarrros<=0)))){
							finishTime = System.currentTimeMillis() - startTime;
							finish=true;
							mainContainer.kill();
							Thread.sleep(1000);
							api.close();
							System.out.println("Simulation time => " + finishTime/1000 + "s");
							System.out.println("Todos os tempos ficam guardados em logTimes.csv e em dados/");
							break;
						}
					} catch (StaleProxyException | UnimplementedMethod | IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						//e.printStackTrace();
					}
				}//step		
			}
		};
		t.start();
		if(!flows){
			Thread carros = new Thread(){
				public void run(){
					while(numCarrros>0){
						try {
							
							//carros comecam e acabam rotas sempre nas estradas laterais ao mapa
							String[] s = new String[] {"a","m","u2","j","l2","y2","p","c2"};
 							String[] f = new String[] {"a2","m2","c","p2","y","l","j2","u"};
 
 							String origin = s[rand.nextInt(s.length)];
 							String destination = f[rand.nextInt(f.length)];
							
							/*
							// carros comecam e acabam em qualquer edge
							String origin= SumoCom.edgesIDs.get(rand.nextInt(SumoCom.edgesIDs.size()));
							String destination="";
							do{
								destination = SumoCom.edgesIDs.get(rand.nextInt(SumoCom.edgesIDs.size())); 
							}while(origin.equals(destination));
							
							*/
							String vehicleType = SumoCom.vehicleTypesIDs.get(1);
							String routeId = SumoCom.getRouteId(origin, null);
							int departureTime = 0;
							double departPosition = 0;
							double departSpeed = 0;
							byte departLane = 0;

							//System.out.println("Carro: "+numCarrros+" s: "+origin+" f: "+destination);
							Vehicle vehicle = new SumoVehicle(numCarrros, vehicleType, routeId, departureTime, departPosition, departSpeed, departLane);

							SumoCom.addVehicle((SumoVehicle)vehicle);

							SumoCom.addVehicleToSimulation((SumoVehicle)vehicle);

							vehicle.changeTarget(destination);
							numCarrros--;
							Thread.sleep(300);
						} catch (UnimplementedMethod e) {
							e.printStackTrace();
						} catch (InterruptedException e) {

						}
					}
				}
			};
			carros.start();
		}

		Thread stats = new Thread(){
			public void run(){
				PrintWriter evolucaoFile = null;
				File folder = new File("dados");
				if(!folder.exists())
					folder.mkdir();
				try {
					evolucaoFile = new PrintWriter(new BufferedWriter(new FileWriter("dados/"+System.currentTimeMillis()+"_"+mode+"_"+!flows+".csv", true)));
					evolucaoFile.println(mode+";"+ !flows+ ";");
					evolucaoFile.println("tempo; nº carros na rede; nº carros parados na rede; velocidade média atual;");
				} catch (IOException e) {
					e.printStackTrace();
				}
				while(numCarrros>=350){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				float carrosNaRede=0, carrosParados=0, mediaParado=0, mediaViagem=0;
				int acc=0;
				HashMap<String, Long> tempoParado = new HashMap<>();
				HashMap<String, Long> tempoViagem = new HashMap<>();
				ArrayList<String> parados = new ArrayList<>();
				long lastT = System.currentTimeMillis();
				long startT = lastT;
				int numCarros = 0, numCarrosParados=0,velocidadeAtual=0;
				double cSpeed=0.0;
				Long entryValue=0L;
				while(!finish){
					numCarros = 0;
					numCarrosParados=0;
					velocidadeAtual=0;
					try {
						for(String v : SumoCom.getAllVehiclesIds()){
							try {
								SumoVehicle c = new SumoVehicle(v);
								cSpeed = c.getSpeed();
								numCarros++;
								velocidadeAtual+=cSpeed;
								entryValue = tempoViagem.get(v);
								if(entryValue!=null)
									tempoViagem.put(v, (entryValue + (System.currentTimeMillis()-lastT)));
								else tempoViagem.put(v, 0L);

								if(cSpeed <= TlAgent.STOP_SPEED){
									numCarrosParados++;
									if(parados.contains(v)){// se estava parado na ultima medição
										tempoParado.put(v, (tempoParado.get(v) + (System.currentTimeMillis()-lastT)));
									}else {// se estava andar e parou agora
										if(!tempoParado.containsKey(v))
											tempoParado.put(v, 0L);
										parados.add(v);
									}
								}else {
									parados.remove(v);
								}
							} catch (Exception e) {
								parados.remove(v);
								//e.printStackTrace();
							}
						}
						//System.out.println(numCarrosParados+"de"+numCarros+"parados");
						evolucaoFile.println(System.currentTimeMillis()-startT+";"+numCarros+";"+numCarrosParados+";"+(velocidadeAtual/(double)numCarros)+";");
						carrosParados+=numCarrosParados;
						carrosNaRede+=numCarros;
						lastT = System.currentTimeMillis();
						acc++;
						try {
							Thread.sleep(250);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} catch (Exception e) {// sumo fail -> try again later
						break;
					}

				}
				for(Entry<String, Long> entry : tempoParado.entrySet()) {
					mediaParado+=entry.getValue();
				}
				for(Entry<String, Long> entry : tempoViagem.entrySet()) {
					mediaViagem+=entry.getValue();
				}
				mediaParado/=tempoParado.entrySet().size();
				mediaViagem/=tempoViagem.entrySet().size();
				carrosNaRede/=acc;
				carrosParados/=acc;
				PrintWriter fileMedias;
				try {
					fileMedias = new PrintWriter(new BufferedWriter(new FileWriter("logTimes.csv", true)));
					fileMedias.println(mode+";"+ !flows+ ";"+finishTime/1000+ ";"+carrosNaRede+";"+carrosParados+";"+(mediaParado/1000.0)+";"+(mediaViagem/1000.0)+";"+(carrosParados*100/carrosNaRede)+";"+(mediaParado*100/mediaViagem)+";");
					fileMedias.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				evolucaoFile.close();
			}
		};
		stats.start();
	}	
}
