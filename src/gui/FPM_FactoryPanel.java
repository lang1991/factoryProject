package gui;

import java.awt.*;

import javax.swing.*;
import java.awt.event.*;

public class FPM_FactoryPanel extends JPanel implements ActionListener
{

	/*
	 * This class will hold every JPanel, including the swing panel. The initial
	 * idea is to have instances of the three graphics panels and one instance
	 * of the swing panel starting from the left and flowing towards the right.
	 */
	// /////// VARIABLES /////////
	private FPM_Manager fpm; // passed in JFrame
	private ImageIcon backgroundImage = new ImageIcon("src/resources/potentialBackground.jpg");
	private Timer timer;
	private double angle=0;
	private int frame_counter=0;
	private double angular_speed=1;
	private boolean rotating=false;

	// /////// MANAGERS /////////
	private GKitAssemblyManager kitAssemblyManager;
	private GLaneSystemManager laneSystemManager;
	private GantryRobotManager gantryRobotManager;

	// ////// GRAPHICS PANELS ////////
	private GKitAssemblyGraphicsPanel kitAssemblyGraphics;
	private GLaneSystemGraphicsPanel laneSystemGraphics;
	private GGantrySystem gantryGraphics;

	public FPM_FactoryPanel(FPM_Manager manager)
	{
		fpm = manager; // grabs the FPM_Manager JFrame
		setupManagers();
		setMaximumSize(new Dimension(1000, 800));
		setMinimumSize(new Dimension(1000, 800));
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponents(g);
		Graphics2D g2d = (Graphics2D)g;
		if(rotating)
		{
			g2d.rotate(Math.toRadians(angle+=angular_speed),this.getWidth()/2 , this.getHeight()/2);
			frame_counter++;
			if(frame_counter%100==0)
			{
				angular_speed+=2;
				frame_counter=0;
			}
		}

		g2d.drawImage(backgroundImage.getImage(), -500, -400, 1500, 1200, null);
		gantryGraphics.paintComponent(g2d);
		laneSystemGraphics.paintComponent(g2d); //using lane System nest and feeder
		kitAssemblyGraphics.paintComponent(g2d); 
	}

	public void actionPerformed(ActionEvent ae)
	{
		repaint();
	}

	public void setupManagers()
	{
		timer = new Timer(10, this);


		kitAssemblyManager = new GKitAssemblyManager(0, 0, true);
		laneSystemManager = new GLaneSystemManager(235, 0, true);
		gantryRobotManager = new GantryRobotManager(343, 0, true);

		kitAssemblyGraphics = kitAssemblyManager.getGraphicsPanel();
		laneSystemGraphics = laneSystemManager.getGraphicsPanel();
		gantryGraphics = gantryRobotManager.getGraphicsPanel();
		/*
		 * kitManager = new GKitManager(); partsManager = new GPartManager();
		 */
		add(kitAssemblyGraphics);
		add(laneSystemGraphics);
		add(gantryGraphics);
		timer.start();
	}
	
	public GLaneSystemGraphicsPanel getLaneGraphicsPanel()
	{
		return laneSystemGraphics;
	}
	
	public void startRotating()
	{
		if(rotating)
			stopRotating();
		else
			rotating=true;
	}
	
	public void stopRotating()
	{
		rotating=false;
		angle=0;
		frame_counter=0;
		angular_speed=1;
	}
}