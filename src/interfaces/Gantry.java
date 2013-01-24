package interfaces;
import NonAgent.Part;

public interface Gantry {
	public abstract void msgNeedPart(Feeder f, Part p);
	public abstract void msgDoneWithAnim();
	public abstract void msgDoneFeeding(Feeder f);
	//public abstract void msgTellGuiEnableDiverterDelay();
	//public abstract void msgTellGuiDisableDiverterDelay();
	public abstract void msgTellGuiDisableFeederDelayButtons(Feeder f);

	
}
