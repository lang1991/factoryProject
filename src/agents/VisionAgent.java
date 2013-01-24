//By Sean Sharma
package agents;

import gui.GPartRobotGraphicsPanel;
import interfaces.IVisionController;
import interfaces.KitRobot;
import interfaces.Nest;
import interfaces.PartRobot;
import interfaces.Vision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import NonAgent.Kit;
import NonAgent.Kit.condition;
import NonAgent.Part.myState;
import NonAgent.Part;
import agent.Agent;

public class VisionAgent extends Agent implements Vision {
	enum State {
		pending, pendingPickUp, done
	};

	enum visionState {
		ready, takingPicture
	};

	public visionState state = visionState.ready;
	IVisionController controller;
	TimerTask timerTask;
	Timer timer = new Timer();

	class MyPart {
		Part part;
		State state = State.pending;
		Nest owner;
		int ownerId;

		public MyPart(Nest nest, Part part, int ownerId) {
			this.part = part;
			this.owner = nest;
			this.ownerId = ownerId;
		}
	}

	class MyKit {
		Kit kit;
		State state = State.pending;

		public MyKit(Kit kit) {
			this.kit = kit;
		}
	}

	String name;
	List<ArrayList<MyPart>> parts = Collections.synchronizedList(new ArrayList<ArrayList<MyPart>>());
	List<MyKit> kits = new ArrayList<MyKit>();

	PartRobot partRobot;
	KitRobot kitRobot;
	// VisionGui gui;
	// GNest gNest;
	GPartRobotGraphicsPanel gui;
	public int[] nestCount = new int[8];

	Semaphore animation = new Semaphore(0);

	public VisionAgent(String name, IVisionController gui) {
		this.name = name;
		this.controller = gui;
		for(int i=0; i<8;i++){
			parts.add(new ArrayList<MyPart>());
		}
	}

	public void msgNestHas(Nest nest, Part part, int nestId) {
		print("msgNestHas received");
		parts.get(nestId).add(new MyPart(nest, part, nestId));
		stateChanged();
	}

	public void msgTellCameraInspect(Kit kit) {
		print("kits size: " + kits.size());
		kits.add(new MyKit(kit));
		print("kits size: " + kits.size());
		stateChanged();
	}

	public void didAnimation() {
		animation.release();
	}
	
	public void msgPurgeList(int num){
		while(!parts.get(num).isEmpty()){
			parts.get(num).remove(0);
		}
	}

	@Override
	public boolean pickAndExecuteAnAction() {
		try {
			if (!parts.isEmpty()) {
				synchronized (parts) {
					for(ArrayList<MyPart> partList : parts){
						if(partList.size()>=4){
							ShootPart(partList);
							return true;
						}
					}

					/*for (MyPart p : parts) {
						if (p.state == State.pending) {
							print("Shooting part");
							ShootPart(p);
							return true;
						}
					}*/
				}
			}
			if (!kits.isEmpty()) {
				synchronized (kits) {
					for (MyKit k : kits) {
						if (k.state == State.pending) {
							print("Shooting kit");
							ShootKit(k);
							return true;
						}
					}
				}
			}

		} catch (ConcurrentModificationException e) {
			return true;
		}
		// TODO Auto-generated method stub
		return false;
	}

	private void ShootPart(ArrayList<MyPart> p) {
	synchronized(p){
		int size= p.size();
		//p.state = State.done;
		//Part part = p.part;
		// take a picture of
		// the part
		// gui.DoShoot(part);
		// Animation.acquire();
		// gui.takePicture(p.ownerId);
		// boolean notCompletelyInformed=false;

		if (state == visionState.ready) {
			state = visionState.takingPicture;

			try {
				if (controller != null) {
					//nestCount[p.ownerId]++;
					//int remainder = p.ownerId % 2;
//					if (remainder == 0) {
//						if (nestCount[p.ownerId] % 4 == 1) {
//							controller.doShoot(new Integer(p.ownerId));
//							controller.doShoot(new Integer(p.ownerId + 1));
//							animation.acquire();
//							animation.acquire();
//						}
//					} else {
//						if (nestCount[p.ownerId] % 4 == 1) {
//							controller.doShoot(new Integer(p.ownerId));
//							controller.doShoot(new Integer(p.ownerId - 1));
//							animation.acquire();
//							animation.acquire();
//						}
//					}
					//if(nestCount[p.ownerId]%4==0){
					// controller.doShoot(p.ownerId);
					// animation.acquire();
					//}
					controller.doShoot(p.get(0).ownerId);
				}
			} catch (Exception e) {

			}

			state = visionState.ready;
		}
		for(int i=0; i< size;i++){
			MyPart currentPart= p.remove(0);
			if(currentPart.part.state== myState.good){
			print("Messagine parts robot to pick up part part: " + currentPart.part.partType
					+ " p.owner: " + currentPart.owner + " p.ownerId: " + currentPart.ownerId);
			partRobot.msgPickUpParts(currentPart.part, currentPart.owner, currentPart.ownerId);
			}
		}
//		print("Messagine parts robot to pick up part part: " + part.partType
//				+ " p.owner: " + p.owner + " p.ownerId: " + p.ownerId);
//		partRobot.msgPickUpParts(p.part, p.owner, p.ownerId);
		// no condition since only
		// normative
	}
		stateChanged();
		System.out.println("Vision: Taking a picture of Nest");
	}

	private void ShootKit(MyKit k) {
		k.state = State.done;
		Kit kit = k.kit;
		// take a picture of
		// the kit
		// Gui.DoShoot(kit);
		// Animation.acquire();

		// no condition since only
		// normative
		// kitRobot.msgKitIsGood(new Kit(kit));
		if(controller != null)
		{
			controller.doShootKit();
			try
			{
				animation.acquire();
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(kit.current_condition==condition.GOOD)
		kitRobot.msgKitIsGood(kit);
		else kitRobot.msgKitIsBad(kit);
		System.out.println("Telling kit robot kit is good");
		stateChanged();
	}

	// set things
	public void setPartRobot(PartRobot partrobot) {
		this.partRobot = partrobot;
	}

	public void setKitRobot(KitRobot kitrobot) {
		this.kitRobot = kitrobot;
	}

	public void setController(IVisionController controller) {
		this.controller = controller;
	}
	
	public void msgLaneJammed(int i){
		final int nestnum=i;
		timerTask = new TimerTask(){
			public void run(){
				print("nest num: " + nestnum);
			partRobot.msgNoGoodPartFound(nestnum);
			controller.doShoot(nestnum);
			print("telling part robot to find problem.");
			};
		};
		timer.schedule(timerTask, 5000);
	}

}