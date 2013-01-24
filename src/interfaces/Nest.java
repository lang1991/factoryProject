package interfaces;
import NonAgent.*;

public interface Nest {
	
	void msgWhatIsWrong();

	void msgPutPart(Part part);
	
	void msgNeedParts(Part part);
	
	void msgPartRemoved();
	
	void SetVision(Vision vision);
	
	void msgPurgeNest();
	void msgReset();

	void msgPurgeNestOnly();
	
}
