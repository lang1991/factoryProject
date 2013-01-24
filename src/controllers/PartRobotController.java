//By Sean Sharma
package controllers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import agents.PartRobotAgent.MyKit;

import controllers.ConveyorSystemController.ReadingThread;

import NonAgent.Kit;
import NonAgent.Part;
import NonAgent.Kit.condition;
import gui.GKitRobot;
import gui.GPartRobotGraphicsPanel;
import gui.msgObject;
import interfaces.IKitRobotController;
import interfaces.IPartRobotController;
import interfaces.PartRobot;
import interfaces.Server;

public class PartRobotController implements IPartRobotController
{
	private PartRobot partRobot;
	private GPartRobotGraphicsPanel gui;
	private Server server;
	private Socket s;
	private ObjectOutputStream oos;

	public PartRobotController(PartRobot partRobot, Server server)
	{
		//this.partRobot=partRobot;
		//this.gui = gui;
		this.partRobot=partRobot;
		this.server=server;
	}
//add 
	
	public void connect()
	{
		try
		{
			s = new Socket("localhost", 63432);
			new Thread(new ReadingThread(s)).start();
			oos = new ObjectOutputStream(s.getOutputStream());
			oos.flush();
			System.out.println("PartRobotController Client Ready");
		} catch (UnknownHostException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void pickUpFromNest(int nestNumber)
	{
		//gui.doPickUpFromNest(nestNumber);
		server.doPickUpFromNest(nestNumber);
		
//		try {
//			msgObject temp=new msgObject();
//			temp.setCommand("PartRobot_PickUpParts");
//			temp.addParameters((Integer) nestNumber);
//			oos.writeObject(temp);
//			oos.reset();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	public void moveToNest(int nestNumber)
	{
		//gui.doMoveToNest(nestNumber);
		server.doMoveToNest(nestNumber);
//		try {
//			msgObject temp=new msgObject();
//			temp.setCommand("PartRobot_MoveToNest");
//			temp.addParameters((Integer)nestNumber);
//			oos.writeObject(temp);
//			oos.reset();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	public void moveToKit(Kit kit){
		//gui.doMoveToKit();
		server.doMoveToKit(kit);
//		try {
//			msgObject temp=new msgObject();
//			temp.setCommand("PartRobot_MoveToKit");
//			temp.addParameters(kit);
//			oos.writeObject(temp);
//			oos.reset();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
//	public void dropDownPartsInKit(){
//		//gui.moveToKit();
//		//gui.doDropDownPartsToKit();
////		server.doDropPartsInKit();
//		try {
//			oos.writeObject("PartRobot_DropPartsInKit");
//			oos.reset();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	public void animDone()
	{
		partRobot.didAnimation();
	}
	

	public void setPartRobot(PartRobot partRobot){
		this.partRobot=partRobot;
	}
	public void setGui(GPartRobotGraphicsPanel gui){
		this.gui=gui;
	}

	public void dropDownPartsToKit() {
		server.doDropPartsInKit();
	}

	public void dropDownPartsToGround() {
		server.doDropPartsToGround();
	}
	
	public void purgeNest(int num){
		partRobot.purgeNest(num);
	}
	
	public void setServer(Server server) {
		// TODO Auto-generated method stub
		this.server=server;
	}
	public void msgGiveConfig(List<Part> config, String name, int number)
	{
		this.partRobot.msgGiveConfig(config, name, number);
	}
	public void msgMakeBadKit() {
		partRobot.msgMakeBadKit();
	}
	
	public void msgPurgeNestOnly(int nestNumber)
	{
		System.out.println("PartRobotController: msgPurgeNestOnly called with " + nestNumber);
		partRobot.purgeNestOnly(nestNumber);
	}

	class ReadingThread implements Runnable
	{
		ObjectInputStream ois;
		public ReadingThread(Socket s)
		{
			try
			{
				ois = new ObjectInputStream(s.getInputStream());
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void run()
		{
			while(true)
			{
				try
				{
					Object temp=ois.readObject();
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}