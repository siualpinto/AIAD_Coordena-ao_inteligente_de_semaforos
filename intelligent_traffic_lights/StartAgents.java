package intelligent_traffic_lights;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
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
	public StartAgents(String mode, boolean flows){	
		finish=false;
		numCarrros=350;
		rand = new Random(50);
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
				long finishTime = 0;
				while(true){
					try {
						if(!api.simulationStep(0) || (SumoCom.getAllVehiclesIds().size()<=0 && (flows ||(!flows && numCarrros<=0)))){
							finishTime = System.currentTimeMillis() - startTime;
							mainContainer.kill();
							api.close();
							System.out.println("Simulation time => " + finishTime/1000 + "s");
							System.out.println("Todos os tempos ficam guardados em logTimes.txt");
							PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("logTimes.txt", true)));
							out.println("Mode: "+mode+"\t random: "+ !flows+ "\t duration: "+finishTime/1000);
							out.close();
							finish=true;
							break;
						}
					} catch (StaleProxyException | UnimplementedMethod | IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
		if(!flows){
			Thread carros = new Thread(){
				public void run(){
					while(numCarrros>0){
						try {

							//carros comecam e acabam rotas sempre nas estradas laterais ao mapa
							String[] s = new String[] {"a","m","n","j","l2","y2","p","c2"};
							String[] f = new String[] {"a2","m2","c","p2","y","l","j2","n2"};

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
							Thread.sleep(150);
						} catch (UnimplementedMethod e) {
							e.printStackTrace();
						} catch (InterruptedException e) {

						}
					}
				}
			};
			carros.start();
		}
	}	
}
