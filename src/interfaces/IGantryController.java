package interfaces;

import mocks.MockServer;

public interface IGantryController {

	public void DoPickUpNewBin(String partType);
	public void DoDeliverBinToFeeder(int feederNumber);
	public void DoDropBin();
	public void DoPickUpPurgedBin(int feederNumber);
	public void DoReleaseBinToGantry(int feederNumber);
	public void DoDeliverBinToRefill();
	public void animDone();
	public void DoPlaceBin(int feederNumber);
	public void setServer(Server server);
	public void setFeederDelayButtonsEnabled(int position1, int position2);
	public void	setFeederDelayButtonsDisabled(int position1, int position2);
	//public void setDiverterDelayButtonsEnabled();
	//public void	setDiverterDelayButtonsDisabled();
}
