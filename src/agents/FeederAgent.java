package agents;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.concurrent.Semaphore;

import interfaces.*;
import NonAgent.*;
import javax.swing.Timer;

import mocks.*;

import agents.LaneAgent.LaneEvent;

public class FeederAgent extends agent.Agent implements Feeder
{

	public Bin bin;
	public Lane top, bottom;
	public Gantry gantry;
	public List<LaneObject> lanes = Collections.synchronizedList(new ArrayList<LaneObject>());
    private java.util.Timer feederTimer = new java.util.Timer();
    public boolean testingBool = false;
    public boolean nonNormScenarioSet = false;
    boolean somethingsWrong = false;
    


	public boolean diverterDelayed, feederDelayed, feederTimerStarted;
	
	public enum LaneState
	{
		Idle, PartsRequested, WaitingForBin, BinArrived, Feeding, ToldToPurge, Purging
	};

	public class LaneObject
	{

		public Part partType;
		public LaneState laneState = LaneState.Idle;
		public Lane lane;
		int partsFed;
		public boolean badPart = false;
		public int badCount = 0;

		public LaneObject(Part parttype, LaneState laneState, Lane lane)
		{
			this.partType = parttype;
			this.laneState = laneState;
			this.lane = lane;
			partsFed = 0;
		}

		public LaneObject(Lane lane)
		{
			this.lane = lane;
			this.laneState = LaneState.Idle;
		}
	}

	private LaneObject currentLane;
	
	public FeederAgent(Gantry g)
	{
		this.gantry = g;
		this.bin = null;
		this.feederDelayed = false;
		this.diverterDelayed = false;
		this.feederTimerStarted = false;
		
		timer.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				FeedCurrentLane();
			}
			
		});

	}

	Timer timer = new Timer(1000, null);
	
	/********************
	//////Messages///////
	********************/
	
	
	public void msgPurgeFeeder(Lane lane){
		
		LaneObject laneObj = null;
		for(int i=0; i<lanes.size(); i++)
		{
			if(lane == lanes.get(i).lane)
			{
				laneObj = lanes.get(i);
			}
		}
		if(bin != null)
			gantry.msgDoneFeeding(this);
		laneObj.laneState = LaneState.ToldToPurge;
		timer.stop();
		stateChanged();
	}
	
	public void msgDonePurging(Lane lane){
		
		System.out.println("Feeder: Notified that purging completed, will now reset the lane and nest.");
		
		LaneObject laneObj = null;
		for(int i=0; i<lanes.size(); i++)
		{
			if(lane == lanes.get(i).lane)
			{
				laneObj = lanes.get(i);
			}
		}
		
		laneObj.laneState = LaneState.Idle;
		timer.stop();
		laneObj.lane.msgReset();
		
	}
	
	public void msgHereIsBin(Bin bin)
	{
		System.out.println("Feeder: Received bin from gantry.");
		this.bin = bin;
		for (int i = 0; i < lanes.size(); i++)
		{
			if (lanes.get(i).laneState == LaneState.WaitingForBin)
			{
				lanes.get(i).laneState = LaneState.BinArrived;
			}
		}
		stateChanged();
	}

	public void msgNeedParts(Lane lane, Part part)
	{
		System.out.println("Feeder: Received parts request from lane.");
		for (int i = 0; i < lanes.size(); i++)
		{
			if (lanes.get(i).lane == lane)
			{
				lanes.get(i).laneState = LaneState.PartsRequested;
				lanes.get(i).partType = part;
			}
		}
		stateChanged();
	}

	public void msgDoneFeeding(Lane lane)
	{
		System.out.println("Feeder: Was notified that the lane has completed feeding.");
		for (int i = 0; i < lanes.size(); i++)
		{
			if (lanes.get(i).lane == lane)
			{
				lanes.get(i).laneState = LaneState.Idle;
			}
		}
		stateChanged();
	}
	
	//Non-normative scenarios, messages called by the gui
	public void msgDoDelayFeeder(){
		/*	This gui call will delay the feeder giving the next lane that requests parts the parts it needs
			
			Ex/ If the feeder is feeding lane1 and lane2 is waiting for parts, it will not feed lane2 parts until
			lane 2 asks what's wrong
			
			However, if it is already feeding a lane, it will continue until finished
		*/
		if(!diverterDelayed){
			feederDelayed = true;
			System.out.println("Feeder: Received message delay feeder");

		}
		else
			System.out.println("Feeder: Cannot delay feeder while delay lane diverter is true!");
		 stateChanged();


	}

	public void msgDoDelayLaneDiverter(){

		
		if(!feederDelayed){
			diverterDelayed = true;
			System.out.println("Feeder: Received message delay lane diverter");

			stateChanged();
		}
		else
			System.out.println("Feeder: Cannot delay lane diverter while delay feeder is true!");
		 stateChanged();

	}
	
	public void msgWhatIsWrong(){
		System.out.println("Feeder: Received message what is wrong from lane");

		if(feederDelayed){
			System.out.println("###############1: Received message what is wrong from lane but can't find anything!");

			
			DelayFeeder();
		}
		else if (diverterDelayed){
			System.out.println("###############2: Received message what is wrong from lane but can't find anything!");

			DelayDiverter();
		}
		else{
			System.out.println("###############Feeder: Received message what is wrong from lane but can't find anything!");
			somethingsWrong = true;

		}
		 stateChanged();

	}
	
	public void msgFeedBadParts(){
		 currentLane.badPart = true;
		 stateChanged();
	}
	/*******************
	 * //////Scheduler/////
	 *******************/

	public boolean pickAndExecuteAnAction()
	{

		
		for (int i = 0; i < lanes.size(); i++)
		{
			int q = 0;
			if (i == 0){q = 1;}
			if (i == 1){q = 0;}

			// Only one lane gets to request from the Gantry (!diverterDelayed) &&
			if ( (lanes.get(i).laneState == LaneState.PartsRequested && (lanes.get(q).laneState == LaneState.Idle || lanes.get(q).laneState == LaneState.PartsRequested))){
				RequestParts(lanes.get(i));
//				System.out.println("we're here");
				return true;
			}
			
			/*if(!nonNormScenarioSet && ((lanes.get(i).laneState == LaneState.Feeding && lanes.get(q).laneState == LaneState.PartsRequested) || (lanes.get(q).laneState == LaneState.Feeding && lanes.get(i).laneState == LaneState.PartsRequested))){
				nonNormScenarioSet = true;
				gantry.msgTellGuiEnableDiverterDelay();
			}*/
		}

		for (int i = 0; i < lanes.size(); i++)
		{
			if (lanes.get(i).laneState == LaneState.BinArrived && (!diverterDelayed))
			{
				FeedParts(lanes.get(i));
				lanes.get(i).laneState = LaneState.Feeding;
				return true;
			}
		}

		for( int i = 0; i< lanes.size(); i++)
		{
			if(lanes.get(i).laneState == LaneState.ToldToPurge)
			{
				PurgeLane(lanes.get(i));
				return true;
			}
		}
		
		
		return false;
	}

	/******************
	 * //////Actions//////
	 ******************/

	
	private void PurgeLane(LaneObject laneObj){
		System.out.println("Feeder: Waiting for lane and nest to purge.");
		laneObj.laneState = LaneState.Purging;
		timer.stop();
		
	}
	
	private void RequestParts(LaneObject lane)
	{
		
		System.out.println("Feeder: Requested parts from gantry.");

		gantry.msgNeedPart(this, lane.partType);
		lane.laneState = LaneState.WaitingForBin;

		
		
		// HACKY TEMPORARY THING:
	/*	if(gantry instanceof MockGantry)
		{
			this.msgHereIsBin(new Bin(lane.partType.partType));
		}*/

	}

	private void FeedParts(LaneObject lane)
	{
		System.out.println("Feeder: Feeding parts to lane.");
		currentLane = lane;
		currentLane.partsFed = 0;
		testingBool = true;
		timer.restart();
	}
	private void CheckWhatsWrong(){
		System.out.println("#######checking for problem");
		
		if(feederDelayed){
			System.out.println("#######found problem feeder");

			DelayFeeder();
		}
		else if (diverterDelayed){
			System.out.println("#######found problem diverter");

			DelayDiverter();
		}
		else
			System.out.println("#######found NO PROBLEM :[");

	}
	private void FeedCurrentLane(){
		if(!feederDelayed){

			if(currentLane.badCount > 1){
				currentLane.badPart = false;
				currentLane.badCount = 0;
			}
			
			if(currentLane.badPart)
				currentLane.badCount ++;
			currentLane.lane.msgHereIsPart(new Part(currentLane.partType.partType, currentLane.badPart));
			currentLane.partsFed++;
			System.out.println("Feeder: Fed a part to the lane " + ((LaneAgent)currentLane.lane).laneID + ".");

		
		 
			if(currentLane.partsFed == 5){
				gantry.msgTellGuiDisableFeederDelayButtons(this);
			}
		}

		if(currentLane.partsFed == 10)
		{
			/*if(nonNormScenarioSet){
				//You can only click the Delay Lane Diverter button if there are two lanes in need of 
				//parts and one of the lanes is feeding
				
				//This makes it so that the button is disabled since nonNormSet tells us it was
				//previously enabled and the current lane is done feeding so the next lane will now attempt to feed
				
				gantry.msgTellGuiDisableDiverterDelay();
				nonNormScenarioSet = false;
			}*/
			System.out.println("Feeder: Finished feeding parts to lane " + ((LaneAgent)currentLane.lane).laneID + ".");
			currentLane.laneState = LaneState.Idle;
			timer.stop();
			gantry.msgDoneFeeding(this);
			bin = null;
			stateChanged();
			
		}
		
		
	}
	
	private void DelayFeeder(){

		feederTimer.schedule(new TimerTask(){
		    public void run(){//this routine is like a message reception    
		    	feederDelayed = false;
				System.out.println("Feeder: feeder was delayed, problem fixed.");

		    	stateChanged();
		    }
		}, 3000);
	}
	private void DelayDiverter(){

		feederTimer.schedule(new TimerTask(){
		    public void run(){//this routine is like a message reception    
		    	diverterDelayed = false;
				System.out.println("Feeder: feeder's diverter was delayed, problem fixed.");
		    	stateChanged();
		    }
		}, 3000);
	}

	/******************
	//////Setters//////
	******************/

	public void SetTopLane(Lane lane)
	{
		this.top = lane;
		this.lanes.add(new LaneObject(lane));
	}

	public void SetBottomLane(Lane lane)
	{
		this.bottom = lane;
		this.lanes.add(new LaneObject(lane));
	}


	
}
