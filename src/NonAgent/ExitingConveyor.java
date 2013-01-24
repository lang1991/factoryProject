package NonAgent;

import gui.GConveyorOut;
import gui.GKittable;
import interfaces.Kittable;

public class ExitingConveyor extends Conveyor implements Kittable
{
	private GConveyorOut gui;
	private Kit currentKit;

	public ExitingConveyor()
	{
		super();
	}
	
	public ExitingConveyor(GConveyorOut gui)
	{
		super();
		this.gui = gui;
	}
	
	public void addKit(Kit kit)
	{
		if(getKits().isEmpty())
			currentKit = kit;
		super.addKit(kit);
	}
	
	public void removeKit(Kit kit)
	{
		super.removeKit(kit);
		if(getKits().isEmpty())
			currentKit = null;
		else
			currentKit = getKits().get(0);
	}
	
	public Kit getCurrentKit()
	{
		return currentKit;
	}

	public GConveyorOut getGui()
	{
		return gui;
	}

	public void setGui(GKittable gui)
	{
		this.gui = (GConveyorOut) gui;
	}

	public void setCurrentKit(Kit kit)
	{
		currentKit = kit;
	}

}
