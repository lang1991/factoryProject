package controllers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import controllers.ConveyorSystemController.ReadingThread;

import NonAgent.Kit;
import gui.GKitRobot;
import gui.GKitManager.UpdateChecker;
import interfaces.IKitRobotController;
import interfaces.KitRobot;
import interfaces.Kittable;
import interfaces.Server;
//By Sean Sharma
import NonAgent.Kit;
import gui.GKitRobot;
import gui.GPartRobotGraphicsPanel;
import gui.msgObject;
import interfaces.IKitRobotController;
import interfaces.IPartRobotController;
import interfaces.IVisionController;
import interfaces.PartRobot;
import interfaces.Server;
import interfaces.Vision;

public class VisionController implements IVisionController
{
	private Server server;
	private Vision vision;
	private Socket s;
	private ObjectOutputStream oos;


	public VisionController(Vision vision, Server server)
	{
		this.vision=vision;
		this.server=server;
	}

	public void connect()
	{
		try
		{
			s = new Socket("localhost", 63432);
			new Thread(new ReadingThread(s)).start();
			oos = new ObjectOutputStream(s.getOutputStream());
			oos.flush();
			System.out.println("VisionController Client Ready");
		} catch (UnknownHostException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void findProblem(int nestNumber){
		vision.msgLaneJammed(nestNumber);
	}
	public void doShoot(int nestNumber)
	{
		server.doShoot(nestNumber);
//		try
//		{
//			msgObject temp=new msgObject();
//			temp.setCommand("Camera_Shoot");
//			temp.addParameters((Integer)nestNumber);
//			oos.writeObject(temp);
//			oos.reset();
//		} catch (IOException e)
//		{
//			e.printStackTrace();
//		}
	}

	public void animDone()
	{
		vision.didAnimation();
	}

	public void doShootKit(){
		server.doShootKit();
//		try
//		{
//			msgObject temp=new msgObject();
//			temp.setCommand("Camera_ShootKit");
//			oos.writeObject(temp);
//			oos.reset();
//		} catch (IOException e)
//		{
//			e.printStackTrace();
//		}
	}

	public void setVision(Vision vision){
		this.vision=vision;
	}


	public void setServer(Server server) {
		// TODO Auto-generated method stub
		this.server=server;
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