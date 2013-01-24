package mocks;
import java.util.Timer;
import java.util.TimerTask;
import NonAgent.Part;
import controllers.LanesysController;

public class MockLanesysController extends LanesysController {

	Timer timer = new Timer();
	
	public MockLanesysController(){
		
	}
	
	public void doRunLane(Integer laneID, Part part, int numberOfParts){
		
		if(laneID == 0){
			timer.schedule(new TimerTask() {
			    public void run() {
			          messageLane(0);
			    }
			}, 2000);
			
		}
		if(laneID == 1){
			timer.schedule(new TimerTask() {
			    public void run() {
			          messageLane(1);
			    }
			}, 2000);
		}
	}
	
	public void DoPurge(int laneID){
		if(laneID == 0){
			timer.schedule(new TimerTask() {
				public void run(){
					messageLanePurgingFinished(0);
				}
			}, 5000);
		}
		
		if(laneID == 1){
			timer.schedule(new TimerTask() {
				public void run(){
					messageLanePurgingFinished(1);
				}
			}, 5000);
		}
	}
	
	void messageLane(int laneNum){
		if(laneNum == 0){ this.lane0.msgPartPutInNest(); }
		if(laneNum == 1){ this.lane1.msgPartPutInNest(); }
	}
	
	
	void messageLanePurgingFinished(int laneNum){
		if(laneNum == 0) { this.lane0.msgDonePurging(); }
		if(laneNum == 1) { this.lane1.msgDonePurging(); }
	}
	
	
}
