package controllers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import controllers.ConveyorSystemController.ReadingThread;

import NonAgent.Part;
import agents.*;
import gui.*;
import interfaces.*;

public class LanesysController
{

	protected LaneAgent lane0; //these are protected for a test
	protected LaneAgent lane1;
	LaneAgent lane2;
	LaneAgent lane3;
	LaneAgent lane4;
	LaneAgent lane5;
	LaneAgent lane6;
	LaneAgent lane7;
	List <FeederAgent> feeders = new ArrayList<FeederAgent>();
	List <LaneAgent> lanes = new ArrayList<LaneAgent>();
	public Server server;
	private Socket s;
	private ObjectOutputStream oos;

	// I realize a list would simplify this, but I don't entirely trust the
	// ordering
	
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

	public void DoPurge(int laneID){
		server.doPurgeLane(laneID);
	}
	
	public void DoJamLane(int laneID){
		server.DoJamLane(laneID);
	}
	
	public void DoUnjamLane(int laneID)
	{		
		server.DoUnjamLane(laneID);
	}
	
	public void purgeNestOnly(int nestID)
	{
		System.out.println("LanesysController: sending message to server: " + nestID);
		server.purgeNestOnly(nestID);
	}
	
	public void setFeeders(List<FeederAgent> f)
	{
		for(int m = 0; m < f.size() ; m++){
			feeders.add(f.get(m));
		}
	}
	public void SetLane(LaneAgent lane, int i)
	{
		if (i == 0)
		{
			lane0 = lane;
			lanes.add(lane0);
		}
		if (i == 1)
		{
			lane1 = lane;
			lanes.add(lane1);
		}
		if (i == 2)
		{
			lane2 = lane;
			lanes.add(lane2);
		}
		if (i == 3)
		{
			lane3 = lane;
			lanes.add(lane3);
		}
		if (i == 4)
		{
			lane4 = lane;
			lanes.add(lane4);
		}
		if (i == 5)
		{
			lane5 = lane;
			lanes.add(lane5);
		}
		if (i == 6)
		{
			lane6 = lane;
			lanes.add(lane6);
		}
		if (i == 7)
		{
			lane7 = lane;
			lanes.add(lane7);
		}

	}
	public void MessageLane(int i)
	{
		if (i == 0)
		{
			lane0.msgPartPutInNest();
		}
		if (i == 1)
		{
			lane1.msgPartPutInNest();
		}
		if (i == 2)
		{
			lane2.msgPartPutInNest();
		}
		if (i == 3)
		{
			lane3.msgPartPutInNest();
		}
		if (i == 4)
		{
			lane4.msgPartPutInNest();
		}
		if (i == 5)
		{
			lane5.msgPartPutInNest();
		}
		if (i == 6)
		{
			lane6.msgPartPutInNest();
		}
		if (i == 7)
		{
			lane7.msgPartPutInNest();
		}
	}

	public void msgLanePurgeDone(int i){
		
		if (i == 0)
		{
			lane0.msgDonePurging();
		}
		if (i == 1)
		{
			lane1.msgDonePurging();
		}
		if (i == 2)
		{
			lane2.msgDonePurging();
		}
		if (i == 3)
		{
			lane3.msgDonePurging();
		}
		if (i == 4)
		{
			lane4.msgDonePurging();
		}
		if (i == 5)
		{
			lane5.msgDonePurging();
		}
		if (i == 6)
		{
			lane6.msgDonePurging();
		}
		if (i == 7)
		{
			lane7.msgDonePurging();
		}
		
	}
	
	public void SetServerRefs()
	{
		lane0.SetServer(server);
		lane1.SetServer(server);
		lane2.SetServer(server);
		lane3.SetServer(server);
		lane4.SetServer(server);
		lane5.SetServer(server);
		lane6.SetServer(server);
		lane7.SetServer(server);
	}


	public void doRunLane(Integer laneID, Part part, int numberOfParts)
	{
		server.doRunLane(laneID, part, (Integer) numberOfParts);
	}
	
	public void doDelayDiverter(int i){
		feeders.get(i).msgDoDelayLaneDiverter();
	}
	
	public void doDelayFeeder(int i){
		feeders.get(i).msgDoDelayFeeder();
	}
	
	public void doFeedBadParts(int i){
	
		feeders.get(i).msgFeedBadParts();
	}
	
	public void doJamLane(int laneNumber)
	{	
		lanes.get(laneNumber).msgJamLane();
	}
	
	public void setServer(Server server)
	{
		this.server = server;
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
