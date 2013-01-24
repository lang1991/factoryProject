package mocks;

import interfaces.Gantry;
import interfaces.IGantryController;
import interfaces.Server;

public class MockGantryController implements IGantryController
{

	Gantry gantry;
	
	public MockGantryController(Gantry gantry)
	{
		this.gantry = gantry;
	}
	
	public void DoPickUpNewBin(String partType)
	{
		animDone();
	}

	public void DoDeliverBinToFeeder(int feederNumber)
	{
		animDone();
	}

	public void DoDropBin()
	{
		animDone();
	}

	public void DoPickUpPurgedBin(int feederNumber)
	{
		animDone();
	}

	public void DoReleaseBinToGantry(int feederNumber)
	{
		// TODO Auto-generated method stub
		
	}

	public void DoDeliverBinToRefill()
	{
		animDone();
	}

	public void animDone()
	{
		gantry.msgDoneWithAnim();
		
	}

	public void DoPlaceBin(int feederNumber)
	{
		animDone();
	}

	public void setServer(Server server)
	{
		// TODO Auto-generated method stub
		
	}



	public void setFeederDelayButtonsEnabled(int position1, int position2) {
		// TODO Auto-generated method stub
		
	}

	public void setFeederDelayButtonsDisabled(int position1, int position2) {
		// TODO Auto-generated method stub
		
	}

}
