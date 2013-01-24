package controllers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import controllers.ConveyorSystemController.ReadingThread;

import NonAgent.Kit;
import gui.GKitRobot;
import gui.msgObject;
import gui.GKitManager.UpdateChecker;
import interfaces.IKitRobotController;
import interfaces.KitRobot;
import interfaces.Kittable;
import interfaces.Server;

public class KitRobotController implements IKitRobotController
{
	private KitRobot kitRobot;
	private Server server;
	private Socket s;
	private ObjectOutputStream oos;

	public KitRobotController(KitRobot kitRobot, GKitRobot gui)
	{
		this.kitRobot = kitRobot;
	}

	public void connect()
	{
		try
		{
			s = new Socket("localhost", 63432);
			new Thread(new ReadingThread(s)).start();
			oos = new ObjectOutputStream(s.getOutputStream());
			oos.flush();
			System.out.println("KitRobotController Client Ready");
		} catch (UnknownHostException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void setServer(Server server)
	{
		this.server = server;
	}

	public void doAnim(Kittable origin, Kittable destination, Kit kit)
	{
//		try
//		{
//			msgObject temp=new msgObject();
//			temp.setCommand("KitRobot_PutKit");
//			temp.addParameters(origin);
//			temp.addParameters(destination);
//			temp.addParameters(kit);
//			oos.writeObject(temp);
//			oos.reset();
//		} catch (IOException e)
//		{
//			e.printStackTrace();
//		}
		
		server.doPutKit(origin, destination, kit);
	}

	public void animDone()
	{
		kitRobot.msgAnimDone();
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
