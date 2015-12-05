package intelligent_traffic_lights;

import javax.swing.JFrame;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class GUI extends JFrame {
	private static final long serialVersionUID = 1L;
	private JTextField txtCarros;
	public GUI(){
		super();
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Launch Options");
		pack();
		setSize(300, 200);
		setResizable(false);
		getContentPane().setLayout(new GridLayout(0, 2, 0, 0));
		JButton justSumo = new JButton("Temporizador");
		ButtonGroup group = new ButtonGroup();
		JRadioButton flows = new JRadioButton("Flows");
		JRadioButton random = new JRadioButton("random");
		justSumo.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				new StartAgents("temporizador",flows.isSelected());
			}
		});
		getContentPane().add(justSumo);
		JButton basicAgent = new JButton("Agente Reativo");
		basicAgent.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				new StartAgents("basicAgents",flows.isSelected());
			}
		});
		
		txtCarros = new JTextField();
		txtCarros.setHorizontalAlignment(SwingConstants.CENTER);
		txtCarros.setEditable(false);
		txtCarros.setText("Carros:");
		getContentPane().add(txtCarros);
		txtCarros.setColumns(10);
		getContentPane().add(basicAgent);
		JButton proAgent = new JButton("Onda Verde");
		proAgent.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				new StartAgents("proAgents",flows.isSelected());
			}
		});
	
		flows.setSelected(true);
		getContentPane().add(flows);
		group.add(flows);
		getContentPane().add(proAgent);
		getContentPane().add(random);
		group.add(random);
		setVisible(true);
	}
	public static void main(String[] args) {
		new GUI();		
	}

}
