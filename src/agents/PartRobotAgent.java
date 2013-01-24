/*test gui: GuiServer change to 1, GKitAssemblyGraphicsPanel uncomment initilize nest, run kitassemblymanager integration
 *then kitassembly manager
4 * @author Sean Sharma
 */
package agents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Semaphore;

import controllers.PartRobotController;

import NonAgent.Kit;
import NonAgent.Part;
import NonAgent.Kit.condition;
import agent.Agent;
import gui.GPartRobotGraphicsPanel;
import interfaces.IPartRobotController;
import interfaces.KitRobot;
import interfaces.Nest;
import interfaces.PartRobot;
import interfaces.Vision;

public class PartRobotAgent extends Agent implements PartRobot
{
	public boolean make_a_bad_kit = false;
	public enum State
	{
		pending, done
	};

	public enum KitState
	{
		pending, ready, done
	};

	public class MyConfiguration
	{
		List<Part> config = Collections.synchronizedList(new ArrayList<Part>());
		String name;
		String general_name;
		State state = State.pending;

		public MyConfiguration(List<Part> config, String name, String general_name)
		{
			this.config = config;
			this.name = name;
			this.general_name = general_name;
		}

		public boolean equals(Object thisConfig)
		{
			if (thisConfig instanceof MyConfiguration)
			{
				if (((MyConfiguration) thisConfig).name.equalsIgnoreCase(this.name))
					return true;
			}
			return false;
		}
	}

	public class MyKit
	{
		Kit kit;
		KitState state = KitState.pending;
		private List<Part> partsNeeded = Collections.synchronizedList(new ArrayList<Part>());
		private List<Part> partsNeededMemory = Collections.synchronizedList(new ArrayList<Part>());

		public MyKit(Kit kit)
		{
			this.kit = kit;
			// find the parts needed
			boolean found = false;
			synchronized (configs)
			{
				for (MyConfiguration config : configs)
				{
					if (config.name.equalsIgnoreCase(kit.configAssociated))
					{
						partsNeeded = new ArrayList<Part>();
						for (int z = 0; z < config.config.size(); z++)
						{
							Part currentPart = new Part(config.config.get(z));
							partsNeeded.add(currentPart);
						}
						// partsNeeded = config.config;
						boolean correct = true;

						for (int i = 0; i < kit.parts.size(); i++) {
							Part checkPart=kit.parts.get(i);
							
								print("checking part");
								for (int j = 0; j < partsNeeded.size(); j++) {
									if ((checkPart.partType
											.equalsIgnoreCase(partsNeeded.get(j).partType))) {

										partsNeeded.remove(j);
										correct = true;
										break;
									}
								}
							

						}
						// deep copy
						for (int i = 0; i < partsNeeded.size(); i++)
						{
							partsNeededMemory.add(new Part(partsNeeded.get(i)));
						}
						//

						found = true;
						break;
					}
				}
				if (!found)
					System.out
							.println("Error in parts Robot: Can't associate a MyKit with a configuration.");

			}

		}

		public void addPart(Part p)
		{
			kit.add(p);
			synchronized (partsNeeded)
			{
				for (int i = partsNeeded.size() - 1; i >= 0; i--)
				{
					if (p.partType.equalsIgnoreCase(partsNeeded.get(i).partType))
					{
						Part currentlyCheckedPart = partsNeeded.get(i);
						// System.out.println()
						if (partsNeeded.remove(currentlyCheckedPart))
						{

							System.out.println("Removing part " + currentlyCheckedPart.partType +
									"from kit " + this.toString());
						}
						;

						break;
					}
				}
			}
			for (Part partNeeded : partsNeeded)
			{
				System.out.println("Part needed:" + partNeeded.partType);
			}
			System.out.println("Parts Needed for Kit remaining:" + partsNeeded.size());
			if (partsNeeded.isEmpty())
			{
				tellKitRobotKitReady(this);
			}
		}

	}

	class MyPart
	{
		State state = State.pending;
		Part part;
		Nest nest;
		int nestNumber;

		public MyPart(Part part, Nest nest, int nestNumber)
		{
			this.part = part;
			this.nest = nest;
			this.nestNumber = nestNumber;
		}

		// add a equals method

	}

	class Gripper
	{
		private final int maxGrippers = 4;
		List<MyPart> partsBeingHeld = Collections.synchronizedList(new ArrayList<MyPart>());

		public Gripper()
		{

		}

		boolean isFull()
		{
			System.out.println("Gripper Size:" + partsBeingHeld.size());
			return partsBeingHeld.size() >= maxGrippers;
		}
	}

	List<MyPart> pendingParts;
	String name;
	List<MyConfiguration> configs = Collections.synchronizedList(new ArrayList<MyConfiguration>());
	List<MyKit> kits = Collections.synchronizedList(new ArrayList<MyKit>());
	List<MyPart> partsPending = Collections.synchronizedList(new ArrayList<MyPart>());
	Gripper gripper = new Gripper();
	Vision vision;
	List<Nest> nests = new ArrayList<Nest>();
	int maxGrippers = 4;
	final int number_of_nests = 8;
	int nest_number = 0;
	GPartRobotGraphicsPanel gui;
	KitRobot kitrobot;
	Semaphore animation = new Semaphore(0);
	PartRobotController controller;
	public int realGripperCount = 0;
	int uniqueIdentifier = 0;

	public PartRobotAgent(String name, PartRobotController gui)
	{
		this.name = name;
		this.controller = gui;
	}

	@Override
	public void purgeNest(int nestNum) {
		// TODO Auto-generated method stub
		nests.get(nestNum).msgPurgeNest();
		vision.msgPurgeList(nestNum);
	}
	public void purgeNestOnly(int nestNumber)
	{
		print("purgeNestOnly with " + nestNumber);
		nests.get(nestNumber).msgPurgeNestOnly();		
	}


	public void msgFixKit(Kit kit)
	{
		print("msgHereIsEmptyKit received with " + kit);
		kit.current_condition = condition.GOOD;
		kits.add(new MyKit(kit));
		stateChanged();
	}

	public void msgMakeBadKit()
	{
//		if (!kits.isEmpty())
//		{
//			MyKit kit = kits.get(0);
//			if (!kit.partsNeeded.isEmpty())
//			{
//				kit.partsNeededMemory.remove(0);
//				kit.partsNeeded.remove(0);
//				kit.kit.current_condition = condition.BAD;
//			}
//			else
//			{
//				if (kits.size() == 1)
//					{
//					//all the kits working on are finished
//					make_a_bad_kit=true;
//					kit.kit.current_condition = condition.BAD;
//					}
//				else
//				{
//					kit = kits.get(1);
//					if (!kit.partsNeeded.isEmpty())
//					{
//						kit.partsNeededMemory.remove(0);
//						kit.partsNeeded.remove(0);
//						kit.kit.current_condition = condition.BAD;
//					}
//					else
//					{
//						make_a_bad_kit=true;
//						kit.kit.current_condition = condition.BAD;
//						//print("All of the kits I'm working on have already been completed");
//					}
//				}
//			}
//		}
//		else
//		{
			make_a_bad_kit=true;
			
			//print("Part Robot not working on any kits at the moment.");
		//}
	}

	public void msgNoGoodPartFound(int nestNum)
	{
		 nests.get(nestNum).msgWhatIsWrong();
		print("Messaging nest" + nestNum + " msgWhatsWrong");
	}

	public void msgGiveConfig(List<Part> config, String name, int number)
	{
		// deep copy the list of configs
		print("LOOK HERE : " + config.size());
		for (int i = 0; i < number; i++)
		{
			List<Part> configToAdd = new ArrayList<Part>();
			for (int j = 0; j < config.size(); j++)
			{
				Part currentPart = new Part(config.get(j));
				configToAdd.add(currentPart);
			}
			configs.add(new MyConfiguration(configToAdd, name + uniqueIdentifier, name));
			stateChanged();
			uniqueIdentifier++;
		}

	}

	public void msgHereIsEmptyKit(Kit kit)
	{
		print("msgHereIsEmptyKit received with " + kit);
		kit.current_condition = condition.GOOD;
		kits.add(new MyKit(kit));
		if(make_a_bad_kit){
			
		kits.get(kits.size()-1).partsNeeded.remove(0);
		kits.get(kits.size()-1).partsNeededMemory.remove(0);
		make_a_bad_kit=false;
		kits.get(kits.size()-1).kit.current_condition = condition.BAD;
		
		}
		stateChanged();
	}

	public void msgPickUpParts(Part part, Nest nest, int nestNumber)
	{
		boolean found = false;
		synchronized (kits)
		{
			for (MyKit kit : kits)
			{
				synchronized (kit.partsNeededMemory)
				{
					for (Part partNeeded : kit.partsNeededMemory)
					{
						if (partNeeded.partType.equalsIgnoreCase(part.partType))
						{
							kit.partsNeededMemory.remove(partNeeded);
							print("Removing part from kit");
							found = true;
							break;
						}
					}
				}
				if (found)
					break;
			}
		}
		if (found)
		{
			if (!gripper.isFull())
			{
				gripper.partsBeingHeld.add(new MyPart(part, nest, nestNumber));
				System.out.println("Adding part: " + part.partType +
						" to gripper memory.(Not picked up yet)");
				// if(pendingParts.isEmpty())stateChanged();

			}
			else
			{
				partsPending.add(new MyPart(part, nest, nestNumber));
				System.out.println("Adding part: " + part.partType +
						" to memory.(Not picked up yet)");
			}
			stateChanged();
		}
	}

	public void didAnimation()
	{
		animation.release();
		print("Animation Done received.");
	}

	/*
	 * scheduler
	 * 
	 * @see agent.Agent#pickAndExecuteAnAction()
	 */

	@Override
	public boolean pickAndExecuteAnAction()
	{
		try
		{
			if (!kits.isEmpty())
			{
				synchronized (kits)
				{

					for (MyKit k : kits)
					{
						if (k.state == KitState.pending && !k.partsNeeded.isEmpty())
						{
							tellNestNeedPart(k);
							return true;
						}
					}
				}
			}

			// if (gripper.isFull()) {
			// tellKitRobotPartsReady();
			// return true;
			// }
			if (!configs.isEmpty())
			{
				synchronized (configs)
				{
					for (MyConfiguration c : configs)
					{
						if (c.state == State.pending)
						{
							tellKitRobotNeedKit(c);
							return true;
						}
					}
				}
			}

			/*
			 * if (!partsPending.isEmpty() && !gripper.isFull()) { synchronized
			 * (partsPending) { for (MyPart p : partsPending) { if (p.state ==
			 * State.pending) { addToGripper(p); return true; } } } }
			 */

			if (!gripper.partsBeingHeld.isEmpty())
			{
				synchronized (gripper.partsBeingHeld)
				{
					for (MyPart p : gripper.partsBeingHeld)
					{
						if (p.state == State.pending)
						{
							pickUpWithGripper(p);
							// pickUpEverything(p);
							return true;
						}
					}
				}
			}
			synchronized (kits)
			{
				for (MyKit k : kits)
				{
					if (k.state == KitState.ready && k.partsNeeded.isEmpty())
					{
						tellKitRobotKitReady(k);
						return true;
					}
				}
			}
			/*
			 * if(partsPending.isEmpty()&& !gripper.partsBeingHeld.isEmpty()) {
			 * putDownParts(gripper.partsBeingHeld.size()); return true; }
			 */

		} catch (ConcurrentModificationException e)
		{
			return true;
		}
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * actions
	 */

	private void tellKitRobotNeedKit(MyConfiguration c)
	{
		kitrobot.msgNeedEmptyKit(c.name);
		c.state = State.done;
	}

	private void tellNestNeedPart(MyKit k)
	{
		// Kit currentKit = k.kit;
		// the action conditions already checked if k.partsNeeded is empty
		Nest nest;
		Part p = null;
		for (int i = 0; i < k.partsNeeded.size(); i++)
		{
			if (!nests.isEmpty())
			{
				p = k.partsNeeded.get(i);
				nest = nests.get(nest_number);
				nest.msgNeedParts(p);
				print("Asking for Part Type:" + p.partType);
				nest_number++;
				if (nest_number == number_of_nests)
				{
					// go back to index 0
					nest_number = 0;
				}
			}
			else
			{
				System.out.println("Error: Forgot to setNests the Part Robot's nests from gui.");
			}
		}
		// if(!n1.isEmpty()){
		// n1.msgNeedPart(new Part(p));
		// }
		// else{
		// n2.msgNeedPart(new Part(p));
		// }
		System.out.println("Part Robot: telling nestNeedPart. " + p.partType);
		k.state = KitState.ready;
		stateChanged();
	}

	// private void addToGripper(MyPart p)
	// {
	// // move to nest and pickup
	// if (!gripper.isFull())
	// {
	// pickUpWithGripper(p);
	// partsPending.remove(p);
	// }
	// else
	// {
	// partsPending.add(p);
	// }
	//
	// stateChanged();
	// }

	// this should be called 4 times separately
	private void pickUpWithGripper(MyPart p)
	{
		p.state = State.done;
		// p.state = State.done;
		Part part = p.part;
		// move to the indicated nest
		// gui.DoMove(p.nest);
		// Animation.acquire();
		// pick up the indicated part
		// use whatever gripper you want
		// System.out.println("Called.");
		try
		{
			System.out.println("Moving to Nest to pick up part, nest:" + p.nestNumber);
			if (controller != null)
			{
				controller.moveToNest(new Integer(p.nestNumber));

				animation.acquire();
				controller.pickUpFromNest(new Integer(p.nestNumber));
				((VisionAgent) vision).nestCount[p.nestNumber]--;
				// animation.acquire();

			}
			realGripperCount++;
			nests.get(p.nestNumber).msgPartRemoved();
			// animation.acquire();
		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		stateChanged();
		int sum = 0;
		int number = 4;
		for (int i = 0; i < kits.size(); i++)
		{
			sum = sum + kits.get(i).partsNeeded.size();
		}
		if (sum < number)
		{
			number = sum;
		}

		// tellKitRobot doesn't actually tell the kit robot it just puts down
		// parts
		// gui and real-time testing is different here
		if (controller != null)
		{
			// <<<<<<< HEAD
			// if ((realGripperCount == 4)||(realGripperCount>=sum))
			// moveToKitAndDropDownParts(number);
			// } else {
			// if (gripper.isFull()||(gripper.partsBeingHeld.size()>=sum))
			// =======

			if ((realGripperCount == 4) || (realGripperCount >= sum))
				moveToKitAndDropDownParts(number);
		}
		else
		{
			if (gripper.isFull() || (gripper.partsBeingHeld.size() >= sum))
				moveToKitAndDropDownParts(number);
		}

	}

	private void tellKitRobotKitReady(MyKit k)
	{
		Kit kit = k.kit;
		kitrobot.msgKitReadyForInspection(kit);
		System.out.println("Part Robot: Telling Kit Robot kitReadyForInspection.");
		// msgKitReady(kit);
		k.state = KitState.done;
		kits.remove(kit);
		print("kits size:"+kits.size());
		boolean alldone=true;
		for(MyKit currentKit: kits){
			if(currentKit.state!=KitState.done){
				alldone=false;
				break;
			}
		}
		if(alldone){
		for(int i=0; i<8 ;i++){
			
			this.purgeNest(i);
		}
		}
		stateChanged();
	}

	// private void putDownParts(int number)
	// {
	//
	// for (int i = number - 1; 0 <= i; i--)
	// {
	// MyPart part = gripper.partsBeingHeld.get(i);
	// MyKit currentKit = null;
	// boolean found = false;
	// for (MyKit kit : kits)
	// {
	// for (Part partNeeded : kit.partsNeeded)
	// {
	// if (partNeeded.partType.equalsIgnoreCase(part.part.partType))
	// {
	// currentKit = kit;
	// found = true;
	// break;
	//
	// }
	// }
	// if (found)
	// break;
	// }
	// if (currentKit == null)
	// System.out
	// .println("Error In Parts Robot: tellKitRobotKitsReady can't find kit to add part to");
	// // do move and do put in kit
	//
	// try
	// {
	// controller.moveToKit(currentKit.kit);
	// animation.acquire();
	// controller.dropDownPartsToKit();
	// // animation.acquire();
	// } catch (InterruptedException e)
	// {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// currentKit.addPart(part.part);
	// gripper.partsBeingHeld.remove(part);
	//
	// System.out.println("Putting " + part.part.partType + " in kit");
	// }
	// int pickupCount = 0;
	// while (!partsPending.isEmpty() || pickupCount == 4)
	// {
	// MyPart partMoving = partsPending.get(0);
	// gripper.partsBeingHeld.add(partMoving);
	// partsPending.remove(partMoving);
	// pickUpWithGripper(partMoving);
	// pickupCount++;
	// }
	// // go put them down if the gripper has any parts repeat
	// if (!gripper.partsBeingHeld.isEmpty())
	// {
	// putDownParts(pickupCount);
	// }
	// }

	private void moveToKitAndDropDownParts(int number)
	{
		// System.out.println("Print me");
		synchronized (gripper.partsBeingHeld)
		{
			for (int i = number - 1; 0 <= i; i--)
			{
				MyPart part = gripper.partsBeingHeld.get(i);
				MyKit currentKit = null;
				// boolean found = false;
				synchronized (kits)
				{
					outerloop: for (MyKit kit : kits)
					{
						synchronized (kit.partsNeeded)
						{
							for (Part partNeeded : kit.partsNeeded)
							{
								print("PART NEEDED IN KIT" + kit + " Part:" + partNeeded);
								if (partNeeded.partType.equalsIgnoreCase(part.part.partType))
								{
									currentKit = kit;
									print("FOUND KIT:" + currentKit);
									// found = true;
									break outerloop;

								}
							}
						}
						// print("looking at kit number 2 now");
					}
				}
				if (currentKit != null)
				{
					if (controller != null)
					{
						try
						{
							// if (i == maxGrippers - 1) {
							print("Before moving to Kit" + currentKit);
							controller.moveToKit(currentKit.kit);

							animation.acquire();
							print("After moving to Kit");

							// }
							// do move and do put in kit
							controller.dropDownPartsToKit();
							this.realGripperCount--;
							print("Putting down a part in server.");

							// animation.acquire();
						} catch (InterruptedException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					System.out.println("current kit " + currentKit);
					currentKit.addPart(part.part);
				}
				else{
					if(controller!=null){
					controller.dropDownPartsToGround();
					this.realGripperCount--;
					}
				}
				gripper.partsBeingHeld.remove(part);
				System.out.println("Putting " + part.part.partType + " in kit");
			}
		}

		int pickupCount = 0;
		while (!partsPending.isEmpty() && pickupCount != 4)
		{

			System.out.println("picking up a pending part");
			MyPart partMoving = partsPending.get(0);
			gripper.partsBeingHeld.add(partMoving);
			partsPending.remove(partMoving);
			pickUpWithGripper(partMoving);
			pickupCount++;
		}

		
		/*
		 * //go put them down if the gripper has any parts repeat
		 * if(!gripper.partsBeingHeld.isEmpty()){ putDownParts(pickupCount);
		 * this.realGripperCount--; }
		 */

		stateChanged();
	}

	// set things
	public void setNests(List<Nest> nests)
	{
		this.nests = nests;
	}

	public void setKitRobot(KitRobot kitrobot)
	{
		this.kitrobot = kitrobot;
	}

	public void setVision(Vision vision)
	{
		this.vision = vision;
	}

	public void setController(PartRobotController controller)
	{
		this.controller = controller;
	}

	public String toString()
	{
		return "PartsRobotAgent " + name;
	}

	public List<MyKit> getKits()
	{
		return kits;
	}

	

}
