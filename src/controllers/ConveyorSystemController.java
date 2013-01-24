package controllers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import agents.ConveyorSystemAgent;

import mocks.MockServer;
import interfaces.ConveyorSystem;
import interfaces.IConveyorSystemController;
import interfaces.Server;
import NonAgent.Conveyor;
import NonAgent.Kit;
import gui.GUIServer;
import gui.msgObject;

public class ConveyorSystemController implements IConveyorSystemController
{
	private ConveyorSystem conveyorSystem;
	private Server server;
	private Socket s;
	private ObjectOutputStream oos;

	public ConveyorSystemController()
	{}

	public ConveyorSystemController(ConveyorSystem conveyorSystem)
	{
		this.conveyorSystem = conveyorSystem;
	}


	public void connect()
	{
		try
		{
			s = new Socket("localhost", 63432);
			new Thread(new ReadingThread(s)).start();
			oos = new ObjectOutputStream(s.getOutputStream());
			oos.flush();
			System.out.println("ConveyorSystemController Client Ready");
		} catch (UnknownHostException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}


	public ConveyorSystemController(Server server)
	{
		this.server = server;
	}

	public void setConveyorSystem(ConveyorSystem conveyorSystem)
	{
		this.conveyorSystem = conveyorSystem;
	}

	public void doAnim(Conveyor conveyor, Kit kit)
	{
		server.doMove(kit, conveyor);

//		try
//		{
//			msgObject temp=new msgObject();
//			temp.setCommand("Conveyor_MoveKit");
//			temp.addParameters(kit);
//			temp.addParameters(conveyor);
//			oos.writeObject(temp);
//			oos.reset();
//			
////			System.out.println("The size of the kit in the controller"+kit.parts.size());
////			System.out.println("ConveyorSystemController:oos: " + oos);
//		}catch (IOException e)
//		{
//			e.printStackTrace();
//		}

	}

	public void animDone()
	{
		conveyorSystem.msgAnimDone();
	}

	public void kitIsOutOfCell(Kit kit)
	{
		conveyorSystem.msgKitOutOfCell(kit);
	}

	public void setServer(Server server)
	{
		this.server = server;
	}

	public void setConveyorSystem(ConveyorSystemAgent conveyorSystem)
	{
		this.conveyorSystem = conveyorSystem;	
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