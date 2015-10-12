package intelligent_traffic_lights;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GUI extends JFrame {
	private static final long serialVersionUID = 1L;
	public GUI(){
		super();
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Launch Options");
		pack();
		setSize(100, 200);
		setResizable(false);
		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
		JButton justSumo = new JButton("Just Sumo");
		justSumo.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				new StartAgents("justSumo");
			}
		});
		getContentPane().add(justSumo);
		JButton basicAgent = new JButton("Basic Agents");
		basicAgent.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				new StartAgents("basicAgents");
			}
		});
		getContentPane().add(basicAgent);
		JButton proAgent = new JButton("Pro Agents");
		proAgent.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				new StartAgents("proAgents");
			}
		});
		getContentPane().add(proAgent);
		setVisible(true);
	}
	public static void main(String[] args) {
		new GUI();		
	}

}
