package mocks;

import NonAgent.Kit;
import NonAgent.Stand;
import interfaces.ConveyorSystem;
import interfaces.Kittable;

public class MockConveyorSystem extends MockAgent implements ConveyorSystem
{
	public EventLog log;

	public MockConveyorSystem(String name)
	{
		super(name);
		log = new EventLog();
	}

	public void msgINeedEmptyKit(String config)
	{
		log.add(new LoggedEvent("msgINeedEmptyKit received with " + config));
		
	}

	public void msgKitPickedUp(Kit kit)
	{
		log.add(new LoggedEvent("msgKitPickedUp recevied with kit: " + kit));
	}

	public void msgKitDone(Kit kit)
	{
		log.add(new LoggedEvent("msgKitDone received with kit: " + kit));
	}

	public Kittable getExitingConveyor()
	{
		return new Stand();
	}

	public Kittable getEnteringConveyor()
	{
		return null;
	}

	public void msgAnimDone()
	{
		log.add(new LoggedEvent("msgAnimDone"));
	}
	
	public void msgKitOutOfCell(Kit kit)
	{
		log.add(new LoggedEvent("msgKitOutOfCell with " + kit));		
	}

}
