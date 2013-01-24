package agents;

import interfaces.*;
import mocks.*;
import NonAgent.*;
import java.util.*;

public class NestAgent extends agent.Agent implements Nest
{

	List<Part> parts = Collections.synchronizedList(new ArrayList<Part>());
	Lane lane;
	Part partType;
	NestEvent event = NestEvent.None;
	NestState state = NestState.Empty;
	Vision vision;
	public int nestID;

	// TEMPORARY//
	public MockNestGUI mockgui;

	//////////////

	enum NestEvent
	{
		None, PartsRequested, ToldToPurge, ReceivingParts
	};

	enum NestState
	{
		Empty, Waiting, Stocked, Purging, Purge_Nest_Only
	};

	public NestAgent()
	{

	}

	/********************
	//////Messages///////
	********************/
	
	public void msgWhatIsWrong() {
		System.out.println("Nest " + nestID + ": Notified that something is wrong, asking lane 'what is wrong?'.");
		lane.msgWhatIsWrong();
		stateChanged();
	}

	public void msgPurgeNest(){
		System.out.println("Nest " + nestID + ": Instructed to purge.");
		this.event = NestEvent.ToldToPurge;
		stateChanged();
	}
	
	public void msgReset(){
		System.out.println("Nest " + nestID + ": Reset and ready to receive parts requests.");
		if(parts.size() > 0){ parts.clear(); }
		this.state = NestState.Empty;
		this.event = NestEvent.None;
		stateChanged();
	}
	
	public void msgPutPart(Part part)
	{
		System.out.println("Nest: Received a part. " + part.partType);
		parts.add(part);
		state = NestState.Stocked;
		vision.msgNestHas(this, part, nestID);
		stateChanged();
	}

	public void msgNeedParts(Part part)
	{
		if(state == NestState.Purging) { System.out.println("Problem: Nest was asked for parts before it completed purging!"); return; }
		System.out.println("Nest: Received parts request.");
		this.partType = part;
		event = NestEvent.PartsRequested;
		stateChanged();
	}
	
	public void msgPurgeNestOnly()
	{
		PurgeNestOnly();	
	}

	public void msgPartRemoved()
	{
		parts.remove(0);
		if (parts.size() == 0)
		{
			state = NestState.Empty;
		}

		stateChanged();
	}

	/*******************
	//////Scheduler/////
	*******************/

	@Override
	protected boolean pickAndExecuteAnAction()
	{
		if ((state == NestState.Empty && event == NestEvent.PartsRequested) || (state == NestState.Stocked && event == NestEvent.PartsRequested && parts.size() < 4))
		{
			AskLaneForPart();
			return true;
		}
		if(state == NestState.Stocked && event == NestEvent.PartsRequested && parts.size() == 4)
		{
			for(int i = 0; i < 4; i++)
			{
				if(parts.get(i).isGood())
				{
					for(int j = 0; j < 4; j++)
					{
						vision.msgNestHas(this, parts.get(i), nestID);						
					}
					break;
				}
			}
			event = NestEvent.ReceivingParts;
			return true;
		}
		if(this.event == NestEvent.ToldToPurge)
		{
			Purge();
			return true;
		}
		

		return false;
	}

	/******************
	 * //////Actions//////
	 ******************/

	private void Purge(){
		System.out.println("Nest " + nestID + ": Initiated purging.");
		event = NestEvent.None;
		state = NestState.Purging;
		parts.clear();
		lane.msgPurgeLane();
		stateChanged();
	}
	
	private void PurgeNestOnly()
	{
		print("Nest " + nestID + " is purging nest only");
		event = NestEvent.PartsRequested;
		state = NestState.Stocked;
		if(parts.size() == 4)
		{
			for(int i = 0; i < 4; i++)
			{
				parts.remove(0);
			}
		}
		lane.msgPurgeNestOnly(nestID);
		stateChanged();
	}
	
	private void AskLaneForPart()
	{
		System.out.println("Nest: Sent parts request to Lane.");
		lane.msgNeedParts(partType);
		state = NestState.Waiting;
		event = NestEvent.ReceivingParts;
		stateChanged();
	}

	/******************
	//////Setters//////
	******************/

	public void SetLane(Lane lane)
	{
		this.lane = lane;
	}

	public void TestInit()
	{
		for (int i = 0; i < 8; i++)
		{
			this.parts.add(new Part());
		}
	}

	public void SetVision(Vision vision)
	{
		this.vision = vision;
	}


}
