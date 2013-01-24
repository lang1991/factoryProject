package mocks;

import NonAgent.Part;
import interfaces.Feeder;
import interfaces.Gantry;

public class MockGantry implements Gantry{

	public EventLog log = new EventLog();

	public void msgNeedPart(Feeder f, Part p) {
		log.add(new LoggedEvent("Received msgNeedParts of " + p.partType));
	}

	
	public void msgDoneWithAnim() {
		log.add(new LoggedEvent ("Received msgDoneWithAnim from gui"));
	}

	public void msgDoneFeeding(Feeder f){
		log.add(new LoggedEvent("Received msgDoneFeeding() from feeder"));
	}
/*
	public void msgTellGuiEnableDiverterDelay() {
		// TODO Auto-generated method stub
		
	}

	public void msgTellGuiDisableDiverterDelay() {
		// TODO Auto-generated method stub
		
	}
*/
	public void msgTellGuiDisableFeederDelayButtons(Feeder f) {
		// TODO Auto-generated method stub
		
	}
}
