package controllers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import controllers.ConveyorSystemController.ReadingThread;

import gui.msgObject;
import interfaces.Gantry;
import interfaces.IGantryController;
import interfaces.Server;

public class GantryController implements IGantryController
{
	private Gantry gantry;
	private Server server;
	private Socket s;
	private ObjectOutputStream oos;

	public GantryController(Gantry gantry)
	{
		this.gantry = gantry;
	}

	public void connect()
	{
		try
		{
			s = new Socket("localhost", 63432);
			new Thread(new ReadingThread(s)).start();
			oos = new ObjectOutputStream(s.getOutputStream());
			oos.flush();
			System.out.println("GantryController Client Ready");
		} catch (UnknownHostException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	public void setFeederDelayButtonsEnabled(int position1, int position2){
		server.doSetFeederDelayButtonsEnabled(position1, position2);
		
	}
	
	public void setFeederDelayButtonsDisabled(int position1, int position2){
		server.doSetFeederDelayButtonsDisabled(position1, position2);
	}

/*
	public void setDiverterDelayButtonsEnabled() {
		server.doSetDiverterDelayButtonEnabled();
	}

	public void setDiverterDelayButtonsDisabled() {
		server.doSetDiverterDelayButtonDisabled();
	}
	*/

	public void setServer(Server guiServer)
	{
		server = guiServer;
		//for (int i = 0 ; i < 7; i += 2)
		//	server.doSetFeederDelayButtonsDisabled(i, i+1);
		System.out.println("Gantry controller:server set " + server );
	}

	public void DoPickUpNewBin(String partType)
	{
		server.doPickUpNewBin(partType);
//		try{
//			msgObject temp=new msgObject();
//			temp.setCommand("GantryRobot_DoPickUpNewBin");
//			temp.addParameters(partType);
//
//			oos.writeObject(temp);
//			oos.reset();
//			
//		} catch (IOException e)
//		{
//			e.printStackTrace();
//		}
	}

	public void DoDeliverBinToFeeder(int feederNumber)
	{
		server.doDeliverBinToFeeder(feederNumber);
//		try{
//			msgObject temp=new msgObject();
//			temp.setCommand("GantryRobot_DoDeliverBinToFeeder");
//			temp.addParameters(new Integer(feederNumber));
//
//			oos.writeObject(temp);
//			oos.reset();
//			
//		} catch (IOException e)
//		{
//			e.printStackTrace();
//		}
	}

	public void DoDropBin()
	{
		server.doDropBin();
	}

	public void DoPickUpPurgedBin(int feederNumber)
	{
		server.doPickUpPurgedBin(feederNumber);
	}
	
	
	public void DoDeliverBinToRefill()
	{
		server.doDeliverBinToRefill();
//		try{
//			//System.out.println("GANTRY CONTROLLER : DODELIVERBINTOREFILL IS SENT FROM CONTROLLER");
//			msgObject temp=new msgObject();
//			temp.setCommand("GantryRobot_DoDeliverBinToRefill");
//			oos.writeObject(temp);
//			oos.reset();
//			
//		} catch (IOException e)
//		{
//			e.printStackTrace();
//		}
	}
	

	public void animDone()
	{
		gantry.msgDoneWithAnim();
	}
	

	public void DoReleaseBinToGantry(int feederNumber)
	{
		server.doReleaseBinToGantry(feederNumber);
//		try{
//			msgObject temp=new msgObject();
//			temp.setCommand("GantryRobot_ReleaseBinToGantry");
//			temp.addParameters((Integer)feederNumber);
//			oos.writeObject(temp);
//			oos.reset();
//			
//		} catch (IOException e)
//		{
//			e.printStackTrace();
//		}
	}


	public void DoPlaceBin(int feederNumber) {
		server.doPlaceBin(feederNumber);
//		try{
//			msgObject temp=new msgObject();
//			temp.setCommand("Feeder_PlaceBin");
//			temp.addParameters((Integer)feederNumber);
//			oos.writeObject(temp);
//			oos.reset();
//			
//		} catch (IOException e)
//		{
//			e.printStackTrace();
//		}
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
