package integrationTests;
import java.util.Timer;
import java.util.TimerTask;

import NonAgent.*;
import agents.*;
import interfaces.*;
import mocks.*;

public class FeederLaneNestTest {

	MockLanesysController laneSys = new MockLanesysController();
	FeederAgent feeder = new FeederAgent(new MockGantry());
	LaneAgent lane0 = new LaneAgent(laneSys);
	LaneAgent lane1 = new LaneAgent(laneSys);
	NestAgent nest0 = new NestAgent();
	NestAgent nest1 = new NestAgent();
	Vision vision = new MockVision("Mock vision");
	
	
	
	public FeederLaneNestTest(){
	
		//Set references:
		laneSys.SetLane(lane0, 0);
		laneSys.SetLane(lane1, 1);
		
		lane0.laneID = 0;
		lane1.laneID = 1;
		
		feeder.SetTopLane(lane0);
		feeder.SetBottomLane(lane1);
		
		lane0.SetFeeder(feeder);
		lane0.SetNest(nest0);
		lane1.SetFeeder(feeder);
		lane1.SetNest(nest1);
		
		nest0.SetLane(lane0);
		nest1.SetLane(lane1);
		nest0.SetVision(vision);
		nest1.SetVision(vision);
		
		nest0.nestID = 0;
		nest1.nestID = 1;
		
		
		//Start up the threads:
		feeder.startThread();
		lane0.startThread();
		lane1.startThread();
		nest0.startThread();
		nest1.startThread();
		
		
		//Start the simulation:
		nest0.msgNeedParts(new Part("Goomba"));
		nest1.msgNeedParts(new Part("Koopa"));
		
		Timer timer = new Timer();
		
		timer.schedule(new TimerTask(){
			public void run() {
				nest0.msgPurgeNest();
				nest1.msgPurgeNest();
			}
		}, 25000);

		timer.schedule(new TimerTask(){
			public void run() {
				nest0.msgNeedParts(new Part("Koopa"));
				nest1.msgNeedParts(new Part("Goomba"));
			}
		}, 35000);
		
	}
	
	
	
	
	public static void main(String args[]){
		
		FeederLaneNestTest flnt = new FeederLaneNestTest();
		
	}
	
}
