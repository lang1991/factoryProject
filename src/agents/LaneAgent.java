package agents;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
//import Part3.*;
import java.util.concurrent.Semaphore;

import interfaces.*;
import NonAgent.*;

import java.util.Timer;

import mocks.MockServer;

import controllers.LanesysController;

public class LaneAgent extends agent.Agent implements Lane
{

	Feeder feeder;
	Nest nest;
	Part partType;
	Part part;
	List<Part> parts = Collections.synchronizedList(new ArrayList<Part>());
	Semaphore partSem = new Semaphore(0); // Used in sending parts to the Nest
	LaneState state = LaneState.Idle;
	LaneEvent event = LaneEvent.None;
	Server server;
	public int laneID;
	private LanesysController controller;
	


	enum LaneState
	{
		Idle, WaitingForParts, Feeding, Purging, Jammed
	};

	enum LaneEvent
	{
		None, PartsRequested, PartsReceived, ToldToPurge
	};

	public LaneAgent(LanesysController controller)
	{
		this.controller = controller;
	}

	/********************
	//////Messages///////
	********************/
	
	public void msgWhatIsWrong() {
		System.out.println("Lane " + laneID + ": Asked 'what is wrong?'.");
		if(state == LaneState.Jammed)
		{
			System.out.println("Lane " + laneID + ": Discovered that it was jammed and fixed the problem.");
			unjamLane();
		}
		else
		{
			System.out.println("Lane " + laneID + ": Found nothing wrong, asked Feeder 'what is wrong?'.");
			feeder.msgWhatIsWrong();
		}
		
	}

	public void msgJamLane() {
		
		if(state == LaneState.Feeding)
		{
			state = LaneState.Jammed;
			controller.DoJamLane(laneID);
		}
		else
		{
			System.out.println("Problem! Lane " + laneID + " was told to jam, but it is not feeding.");
		}
		
	}
	
	public void msgPurgeLane(){
		event = LaneEvent.ToldToPurge;
		stateChanged();
	}
	

	public void msgPurgeNestOnly(int nestID)
	{
		controller.purgeNestOnly(nestID);
	}
	
	public void msgDonePurging(){
		System.out.println("Lane " + laneID + ": Completed purging, notified feeder.");
		feeder.msgDonePurging(this);
	}
	
	public void msgReset(){
		System.out.println("Lane " + laneID + ": Reset and ready to operate again.");
		event = LaneEvent.None;
		state = LaneState.Idle;
		if(parts.size() > 0) { parts.clear(); }
		nest.msgReset();
		stateChanged();
	}
	
	public void msgHereAreParts(List<Part> parts)
	{
		System.out.println("Lane: Feeding parts from the feeder.");
		this.parts = parts;
		event = LaneEvent.PartsReceived;
		stateChanged();
	}
	
	public void msgHereIsPart(Part part)
	{
		this.parts.add(part);
		event = LaneEvent.PartsReceived;
		RunGUI(part);
		stateChanged();
	}

	public void msgNeedParts(Part part)
	{
		System.out.println("Lane: Received parts request from Nest.");
		this.part = part;
		event = LaneEvent.PartsRequested;
		stateChanged();
	}

	public void msgPartPutInNest()
	{
		GivePart();
	}

	/*******************
	 * //////Scheduler/////
	 *******************/

	
	protected boolean pickAndExecuteAnAction()
	{

		if(state == LaneState.Jammed)
		{
			state = LaneState.Feeding;
			new Timer().schedule(new TimerTask(){
				public void run()
				{
					unjamLane();
				}
			}, 3000);
			return true;
		}
		if (state == LaneState.Idle && event == LaneEvent.PartsRequested)
		{
			AskFeederForParts();
			return true;
		}

		if (state == LaneState.WaitingForParts && event == LaneEvent.PartsReceived)
		{
			state = LaneState.Feeding;
			//FeedToNest();
			return true;
		}
		
		if(event == LaneEvent.ToldToPurge)
		{
			Purge();
			return true;
		}
		

		return false;
	}

	

	/******************
	//////Actions//////
	******************/
	
	private void unjamLane() {
		controller.DoUnjamLane(laneID);
		state = LaneState.Feeding;
	}

	private void Purge(){
		System.out.println("Lane " + laneID + ": Initiated purging.");
		state = LaneState.Purging;
		event = LaneEvent.None;
		if(parts.size() > 0){ parts.clear(); }
		controller.DoPurge(laneID);
		feeder.msgPurgeFeeder(this);
	}
	
	
	private void AskFeederForParts()
	{
		System.out.println("Lane: Asked feeder for parts.");
		feeder.msgNeedParts(this, part);
		state = LaneState.WaitingForParts;
	}

	private void RunGUI(Part part){
		controller.doRunLane(new Integer(laneID), part, 1);
	}
	
	private void FeedToNest()
	{
		
		controller.doRunLane(new Integer(laneID), parts.get(0), parts.size());
		
		
		//REAL VERSION:
//		server.doRunLane(new Integer(laneID), parts.get(0), parts.size());		
		/*
		 * Eytan: I added this instanceof to run the agentTest with no server
		 * */
//		if(!(server instanceof MockServer))
//			server.doRunLane(new Integer(laneID), parts.get(0), parts.size());
//		else
//		{
//			//TEMP FOR TESTING:
//			((NestAgent) nest).mockgui.runStuff(parts.size());
//		}
		
		
//		gui.DoRunLane(this, part, parts.size());

		/*
		 * for(int i=0;i<parts.size();i++) { try { partSem.acquire(); } catch
		 * (InterruptedException e) { e.printStackTrace(); }
		 * 
		 * nest.msgPutPart(parts.get(0)); parts.remove(0); }
		 * 
		 * feeder.msgDoneFeeding(this); state = LaneState.Idle; event =
		 * LaneEvent.None;
		 */

		// make timer that calls FeedingTimedOut

	}

	private void GivePart()
	{
		nest.msgPutPart(parts.get(0));
		parts.remove(0);
		
		if(parts.size() == 0)
		{
			state = LaneState.Idle;
			event = LaneEvent.None;
		}
		
	}



	/******************
	//////Setters//////
	******************/

	public void SetFeeder(Feeder feeder)
	{
		this.feeder = feeder;
	}

	public void SetNest(Nest nest)
	{
		this.nest = nest;
	}

	public void SetServer(Server server)
	{
		this.server = server;
	}




	
}
