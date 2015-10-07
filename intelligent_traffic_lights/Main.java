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
import trasmapi.sumo.SumoTrafficLight;

public class Main {


	static boolean JADE_GUI = true;
	private static ProfileImpl profile;
	private static ContainerController mainContainer;

	public static void main(String[] args) throws UnimplementedMethod, InterruptedException, IOException, TimeoutException {	

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
		params.add("-c=bin\\map\\hello.sumo.cfg");
		sumo.addParameters(params);
		sumo.addConnections("localhost", 8820);

		//Add Sumo to TraSMAPI
		api.addSimulator(sumo);

		//Launch and Connect all the simulators added
		api.launch();
		api.connect();
		api.start();

		Thread.sleep(1000);

		ArrayList<String> tlIds = new ArrayList<>();
		tlIds=SumoTrafficLight.getIdList();
		System.out.println("num traffic ligths: "+tlIds.size());
		for (String id : tlIds) {
			try {
				mainContainer.acceptNewAgent("TrafficLight#"+id, new TlAgent(id)).start();
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
		}

		while(true)
			if(!api.simulationStep(0))
				break;
	}	
}