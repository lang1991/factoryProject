/* 
 * @Authored by Tian(Sky) Lan
 */
package gui;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

import controllers.ConveyorSystemController;
import controllers.KitRobotController;

import agents.ConveyorSystemAgent;
import agents.KitRobotAgent;

import NonAgent.Conveyor;
import NonAgent.EnteringConveyor;
import NonAgent.ExitingConveyor;
import NonAgent.Kit;
import NonAgent.Stand;

public class GKitAssemblyGraphicsPanel extends JPanel implements ActionListener
{
	Timer timer;
	private int offset_X;
	private int offset_Y;
	
	////////// FACTORY VARIABLES //////////
	GKitAssemblyManager kam;
	ArrayList<GPart> partList;
	GKittingStand leftWorkingStand, rightWorkingStand, inspectionStand;
	GConveyorIn conveyorIn;
	GConveyorOut conveyorOut;
	private ImageIcon backgroundImage = new ImageIcon("src/resources/potentialBackground.jpg");

	
	//////////   Kit Robot //////////
	
	GKitRobot kitRobot;
	
	/////////    Part Robot ////////
	GPartRobot partRobot;
	ArrayList <GNest> nest;
	int lastNestAt;
	GKit lastKitAt;
	int lastAgentKitAt;
	int step;
	
/////////////////////// 201 Objects ///////////
	Stand lws, rws, is;
	EnteringConveyor entConveyor;
	ExitingConveyor exitConveyor;
	Kit myKit;
	KitRobotAgent kitRobotAgent;
	KitRobotController controller;
	ConveyorSystemController conveyorSystemController;

	
	// Constructor
	public GKitAssemblyGraphicsPanel(GKitAssemblyManager k, int offset_X, int offset_Y)
	{
		this.offset_X=offset_X;
		this.offset_Y=offset_Y;
		this.kam = k;
		kitRobot = new GKitRobot();

		leftWorkingStand = new GKittingStand(210,350);
		rightWorkingStand = new GKittingStand(210,550);
		inspectionStand = new GKittingStand(210,100);
		conveyorIn = new GConveyorIn();
		conveyorOut = new GConveyorOut();
		
		partRobot = new GPartRobot(360,600);
		nest = new ArrayList<GNest>();
		lastNestAt = -1;
		step=-1;
		initializeNests();

		lws = new Stand(leftWorkingStand);
		rws = new Stand(rightWorkingStand);
		is = new Stand(inspectionStand);
		entConveyor = new EnteringConveyor(conveyorIn);
		exitConveyor = new ExitingConveyor(conveyorOut);
		

		timer = new Timer(5, this);
		timer.start();
	}
	
	//kitRobot Movement has been called directly from the KAM
	
	// Conveyor Movements
	public void DoKitIn(Kit k) {
		conveyorIn.DoMove(k);
	}
	
	public void DoKitOut(Kit k) {
		conveyorOut.DoMove();
	}
	
	
	public void initializeNests()	
	{		
		for(int i = 0; i < 8; i++ )
		{
		if(i%2==0)
			nest.add(new GNest(410,70+75*i+10*i));
		else
			nest.add(new GNest(410,70+75+(75+10)*(i-1)));
		}
	}

	// Parts Robot Movements
	public void DoPickUp(int nestNumber)	//the robot picks up a part from the nest based on the nest number
	{
		GPart temp=nest.get(nestNumber).pushParts();
		partRobot.pickUp(temp);
		
	}
	
	
	public void DoPutDownToKit()		//the robot drops down all the parts in its arms to the kit
	{
		
		lastKitAt.addAPart(partRobot.dropDown());

		
	}
	
	public void DoMoveToNest(int nestNumber)	//the robot moves to the nest based on the nestNumber
	{
		partRobot.moveToLeftOf(nest.get(nestNumber));
		lastNestAt=nestNumber;
		step=1;
	}
	
	public void DoMove(Integer i)					//the robot moves to the kit
	{
		if(i==1)
		{
			partRobot.moveToRightOf(rightWorkingStand);	
			lastKitAt=rightWorkingStand.getGKit();	
		}
		else if(i==2)
		{    partRobot.moveToRightOf(leftWorkingStand);
		     lastKitAt=leftWorkingStand.getGKit();
		}
			
		lastAgentKitAt = i;
		step=2;
	}
	
	public void DoMove(GKit k) {
		partRobot.moveStraight(k.getX()+k.getIconWidth()+13, k.getY());
		lastKitAt = k;
		step = 2;
	}
	
	public void doDropDownToGround() {
		partRobot.dropDown();
	}
	
	
	public void DoShoot(int i)  //shoot a picture on nest i
	{
		nest.get(i).takePicture();
	}
	
	
	// Picturing a kit on the inspection stand
	public void shootKit()
	{
		inspectionStand.getGKit().takePicture();
	}
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		if (!kam.getFPMRunning()) {
			g.drawImage(backgroundImage.getImage(),0,0,null);
		}
		leftWorkingStand.paintObject(g,offset_X, offset_Y);
		rightWorkingStand.paintObject(g,offset_X, offset_Y);
		inspectionStand.paintObject(g,offset_X, offset_Y);
		conveyorIn.paintObject(g,offset_X, offset_Y);
		conveyorOut.paintObject(g,offset_X, offset_Y);
		kitRobot.paintObject(g,offset_X, offset_Y);
		
//Don't paint the nests in the kit graphics panel, paint it in the lane system
		if (!kam.getFPMRunning()) {
			for (int i = 0;i<8;i++)
			{
				nest.get(i).paintObject(g,offset_X, offset_Y);
			}
		}
		
    partRobot.paintObject(g,offset_X, offset_Y);
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		if (conveyorIn.conveyorMovementIn) {
			conveyorIn.moveKit();
		}
		if (conveyorOut.conveyorMovementOut) {
			conveyorOut.moveKit();
		}
		kitRobot.paintMovement();
		
		  if(!partRobot.ifArrive()&&step==1)
		    {
		    	DoMoveToNest(lastNestAt);    
		    }
		    else if(partRobot.ifArrive()&&step==1)
		    {
		    	step=-1;
		    }
		    else if(!partRobot.ifArrive()&&step==2)
		    {
		    	if(lastAgentKitAt==1)
					partRobot.moveToRightOf(rightWorkingStand);		
				else if(lastAgentKitAt ==2)
				    partRobot.moveToRightOf(leftWorkingStand);
		    }
		    else if(partRobot.ifArrive()&&step==2)
		    {

		    	step=-1;
		    }
		
		
		repaint();
	}

//	public Stand getLws()
//	{
//		return lws;
//	}
//
//	public Stand getRws()
//	{
//		return rws;
//	}
//
//	public Stand getIs()
//	{
//		return is;
//	}
//
//	public EnteringConveyor getEntConveyor()
//	{
//		return entConveyor;
//	}
//
//	public ExitingConveyor getExitConveyor()
//	{
//		return exitConveyor;
//	}
//
//	public void setKitRobotAgent(KitRobotAgent kitRobotAgent)
//	{
//		kitRobotAgent.setGui(kitRobot);
//	}
//
//	public void setConveyorAgent(ConveyorSystemAgent conveyorSystemAgent)
//	{
//		conveyorIn.setConveyorSystem(conveyorSystemAgent);
//	}
//
//	public void setConveyorController(ConveyorSystemController conveyorSystemController)
//	{
//		this.conveyorSystemController = conveyorSystemController;
//	}
//
//	public void setKitRobotContoller(KitRobotController kitRobotController)
//	{
//		this.controller = kitRobotController;
//		kitRobot.setController(controller);
//	}

	public void doPutKit(GKittable gOrigin, GKittable gDestination, Kit tempKit) {
		kitRobot.DoPutKit(gOrigin, gDestination, tempKit.getGui());
		
	}

	
}