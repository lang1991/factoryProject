package agents;

import interfaces.Feeder;
import interfaces.Gantry;
import interfaces.IGantryController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import controllers.GantryController;

import NonAgent.Bin;
import NonAgent.Part;
import agent.Agent;

public class GantryAgent extends Agent implements Gantry
{

	/******* DATA ********/
	public enum feederState
	{
		noAction, needFillPart, needClearPurged
	}
	public String name;
	public class myFeeder
	{
		public Feeder feeder; // can be an agent or a mock
		public feederState state; // current state of the feeder
		public int position; // the feeder number
		public Part currentPart;
		public Part requestedPart;
		public boolean needFillToo;

		public myFeeder(Feeder f, int pos)
		{
			feeder = f;
			currentPart = null;
			requestedPart = null;
			position = pos;
			state = feederState.noAction;
			needFillToo = false;
		}
	}

	public List<myFeeder> feeders = new ArrayList<myFeeder>();
	public IGantryController controller;
	private Semaphore s = new Semaphore(0);
	

	/******* CONSTRUCTOR ********/

	public GantryAgent(String Name){//GantryGui gui) { 
		this.name = Name;
	}

	/******* MESSAGES ********/

	/**Feeder sends this message to request parts
	 * @param feeder1 feeder sending the message
	 * @param p part needed my the feeder
	 */
	public void msgNeedPart(Feeder feeder1, Part p) 
	{ 
		
		for(myFeeder f: feeders)
		{
			if(f.feeder == feeder1)
			{
				if(f.currentPart != null){
					f.state = feederState.needClearPurged;
					f.needFillToo = true;
				}
				else if(f.state == feederState.needClearPurged)
					f.needFillToo = true;
				else
					f.state = feederState.needFillPart;

				f.requestedPart = p;
				System.out.println("Gantry: Received message need part from feeder " + f.position);
				
			}
		}
		stateChanged();	
	}

	public void msgDoneWithAnim(){
		s.release();
		stateChanged();
	}
	/*
	public void msgTellGuiEnableDiverterDelay() {
		controller.setDiverterDelayButtonsEnabled();
	}

	public void msgTellGuiDisableDiverterDelay() {
		controller.setDiverterDelayButtonsDisabled();		
	}
	*/
	public void msgTellGuiDisableFeederDelayButtons(Feeder f){
		int laneNum = 0;
		for(myFeeder f1: feeders){
			if(f1.feeder == f){
				if(f1.position == 0)
					laneNum = 0;
				else
					laneNum = f1.position * 2;
				
				controller.setFeederDelayButtonsDisabled((laneNum), (laneNum + 1));

			}
		}
	}
	
	public void msgDoneFeeding(Feeder feeder1){
		for(myFeeder f: feeders){
			if(f.feeder == feeder1){
				System.out.println("***********************************Gantry: Received message done Feeding from feeder " + f.position);
				f.state = feederState.needClearPurged;
				
			}
		}
		stateChanged();
	}

	/******* SCHEDULER ********/
	public boolean pickAndExecuteAnAction()
	{
		synchronized (feeders){
		for (myFeeder f : feeders)
		{
			if (f.state == feederState.needFillPart){
				try {
				fillFeeder(f);
				} catch (InterruptedException e) {e.printStackTrace();}
				return true;
			}
			
			if (f.state == feederState.needClearPurged){
				try {
				clearPurgedFeeder(f);
				} catch (InterruptedException e) {e.printStackTrace();}
				return true;
			}
			
		}
		}
		return false;
	}

	/******* ACTIONS 
	 * @throws InterruptedException ********/
	private void fillFeeder(myFeeder f) throws InterruptedException
	{ 
		int laneNum = 0;
		if(f.position == 0)
			laneNum = 0;
		else
			laneNum = f.position * 2;
		
		//System.out.println("**********************Gantry:Filling feeder " + f.position + " with " + f.requestedPart.partType);
		f.state = feederState.noAction;
		controller.DoPickUpNewBin(f.requestedPart.partType);
		controller.setFeederDelayButtonsEnabled((laneNum), (laneNum + 1));
		s.acquire();
		controller.DoDeliverBinToFeeder(f.position);
		s.acquire();
		controller.DoDropBin();
		controller.DoPlaceBin(f.position);
		s.acquire();
		f.currentPart = f.requestedPart;
		f.requestedPart = null;
		f.feeder.msgHereIsBin(new Bin(f.currentPart.partType));
		stateChanged();
		f.needFillToo = false;
	}

	private void clearPurgedFeeder(myFeeder f) throws InterruptedException
	{
		System.out.println("Gantry: Removing bin from feeder " + f.position);
		f.state = feederState.noAction;
		controller.DoPickUpPurgedBin(f.position);
		s.acquire();
		controller.DoReleaseBinToGantry(f.position);
		controller.DoDeliverBinToRefill();
		s.acquire();
//		gui.DoDropBin();
//		s.acquire();
		f.currentPart = null;
		stateChanged();
		System.out.println("Gantry: feeder1 state - " + f.state);
		if(f.needFillToo)
			f.state = feederState.needFillPart;
	}
	
	public void setFeeders(List<Feeder> feederList){
		for (int i = 0; i < feederList.size(); i++) { 
			feeders.add(new myFeeder(feederList.get(i), i)); 	
		}
	}

	public void setController(IGantryController gantryController)
	{
		this.controller = gantryController;
	}


}
