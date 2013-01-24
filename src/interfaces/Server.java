package interfaces;

//import gui.GUIServer.ServerSendThread;
import NonAgent.Conveyor;
import NonAgent.Kit;
import NonAgent.Part;

public interface Server 
{
	//////////AGENT METHODS //////////
	// CONVEYOR //
	public void doMove(Kit kit, Conveyor conveyor);
	
	// KITS//
	public void doPutKit(Kittable origin, Kittable destination, Kit kit);
	
	// PARTS //
	public void doPickUpFromNest(int nestNumber);
	
	public void doMoveToNest(int nestNumber);
	
	public void doMoveToKit(Kit kit);
	
	public void doDropPartsInKit();

	
	//VISION//
	public void doShoot(int nestId);
	public void doShootKit();

	// GANTRY
	public void doPickUpNewBin(String partType);
	public void doDeliverBinToFeeder(int feederNumber);
	public void doDropBin();
	public void doPickUpPurgedBin(int feederNumber);
	public void doDeliverBinToRefill();
	
	//LANE
	public void doRunLane(int laneid, Part part, int numparts);
	public void doPurgeLane(int laneID);
	
	//FEEEDERGCGFCH
	public void doPlaceBin(int feederNumber);
	public void doSetFeederDelayButtonsEnabled(int position, int position2);
	public void doSetFeederDelayButtonsDisabled(int position, int position2);
	public void doReleaseBinToGantry(int feederNumber);

	public void DoJamLane(int laneID);

	public void DoUnjamLane(int laneID);

	public void doDropPartsToGround();

	public void purgeNestOnly(int nestID);
	
}
