package mocks;

import java.util.List;

import controllers.PartRobotController;



import NonAgent.Kit;
import NonAgent.Part;
import interfaces.IPartRobotController;
import interfaces.KitRobot;
import interfaces.Nest;
//import interfaces.KitRobot;
//import interfaces.Nest;
import interfaces.PartRobot;
import interfaces.Vision;

public class MockPartRobot extends MockAgent implements PartRobot{
	public EventLog log = new EventLog();
	
	public MockPartRobot(String name){
		super(name);
	}
	
	public void msgGiveConfig(List<Part> config, String name, int number) {
		log.add(new LoggedEvent(
				"Received message msgGiveConfig of name "+name+"."));
	}

	public void msgHereIsEmptyKit(Kit kit) {
		log.add(new LoggedEvent(
				"Received message msgHereIsEmptyKit for kit "+kit.toString()+"."));
	}

	public void msgPickUpParts(Part part, Nest nest, int i) {
		log.add(new LoggedEvent(
				"Received message msgPickUpParts for part "+part.toString()+" for Nest "+nest+"."));
	}

	public void setKitRobot(KitRobot kitrobot) {
		// TODO Auto-generated method stub
		
	}


	public void setVision(Vision vision) {
		// TODO Auto-generated method stub
		
	}

	public void setNests(List<Nest> nests) {
		// TODO Auto-generated method stub
		
	}

	public void didAnimation() {
		// TODO Auto-generated method stub
		
	}
	
	public void setController(IPartRobotController controller) {
		// TODO Auto-generated method stub
		
	}

	public void DoMakeBadKit() {
		// TODO Auto-generated method stub
		
	}

	public void msgNoGoodPartFound(int nestNum) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgMakeBadKit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgFixKit(Kit kit)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setController(PartRobotController controller) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void purgeNest(int nestNum) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void purgeNestOnly(int nestNumber)
	{
		// TODO Auto-generated method stub
		
	}

}
