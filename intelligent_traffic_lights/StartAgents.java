package intelligent_traffic_lights;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jade.BootProfileImpl;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import trasmapi.genAPI.Simulator;
import trasmapi.genAPI.TraSMAPI;
import trasmapi.genAPI.exceptions.TimeoutException;
import trasmapi.genAPI.exceptions.UnimplementedMethod;
import trasmapi.sumo.Sumo;
import trasmapi.sumo.SumoCom;
import trasmapi.sumo.SumoTrafficLight;

public class StartAgents {


	static boolean JADE_GUI = true;
	private static ProfileImpl profile;
	private static ContainerController mainContainer;

	public StartAgents(String mode){	
		if(!mode.equals("justSumo")){
			if(JADE_GUI){
				List<String> params = new ArrayList<String>();
				params.add("-gui");
				profile = new BootProfileImpl(params.toArray(new String[0]));
			} else
				profile = new ProfileImpl();

			Runtime rt = Runtime.instance();
			mainContainer = rt.createMainContainer(profile);
		}
		TraSMAPI api = new TraSMAPI(); 

		//Create SUMO
		Simulator sumo = new Sumo("guisim");
		List<String> params = new ArrayList<String>();
		params.add("-c=bin\\map\\hello.sumo.cfg");
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
				if(mode.equals("basicAgents")){
					mainContainer.acceptNewAgent("BasicTL#"+id, new TlBasicAgent(id)).start();
				}else if(mode.equals("proAgents")){
					mainContainer.acceptNewAgent("ProTL#"+id, new TlProAgent(id)).start();
				}
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
		}
		Thread t = new Thread(){
			public void run(){
				while(true){
					try {
						if(!api.simulationStep(0) || SumoCom.getAllVehiclesIds().size()<=0){
							sumo.close();
							break;
						}
					} catch (UnimplementedMethod e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
	}	
}
