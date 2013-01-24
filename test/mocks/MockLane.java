package mocks;

import java.util.List;

import NonAgent.Part;
import interfaces.Lane;
import interfaces.Server;

public class MockLane implements Lane {

	public EventLog log = new EventLog();
	
	public MockLane() {
		// TODO Auto-generated constructor stub
	}

	public void msgJamLane(){
		
	}
	
	public void msgHereAreParts(List<Part> parts) {
		log.add(new LoggedEvent("Received msgHereAreParts"));
		
	}

	public void msgNeedParts(Part part) {
		// TODO Auto-generated method stub

	}

	public void msgPartPutInNest() {
		// TODO Auto-generated method stub

	}

	public void SetServer(Server sever) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgHereIsPart(Part part) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgPurgeLane() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgDonePurging() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgReset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgWhatIsWrong() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgPurgeNestOnly(int nestID)
	{
		// TODO Auto-generated method stub
		
	}

}
