package interfaces;
import NonAgent.*;

public interface Feeder {
	
	void msgWhatIsWrong();
	
	void msgHereIsBin(Bin bin);
	
	void msgNeedParts(Lane lane, Part part);
	
	void msgDoneFeeding(Lane lane);
	
	void msgPurgeFeeder(Lane lane);
	
	void msgDonePurging(Lane lane);
}
