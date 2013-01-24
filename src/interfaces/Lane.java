package interfaces;
import java.util.*;
import NonAgent.*;

public interface Lane {

	void msgWhatIsWrong();
	
	void msgHereAreParts(List<Part> parts);
	
	void msgHereIsPart(Part part);
	
	void msgNeedParts(Part part);

	void msgPartPutInNest();
	
	void SetServer(Server sever);
	
	void msgPurgeLane();
	
	void msgDonePurging();
	
	void msgReset();
	
	void msgJamLane();

	void msgPurgeNestOnly(int nestID);
	
}
