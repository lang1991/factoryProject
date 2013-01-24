package interfaces;

import agents.ConveyorSystemAgent;
import NonAgent.Conveyor;
import NonAgent.Kit;

public interface IConveyorSystemController
{
	public void doAnim(Conveyor conveyor, Kit kit);
	public void animDone();
	public void setConveyorSystem(ConveyorSystemAgent conveyorSystem);
}
