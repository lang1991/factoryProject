///// DONE BY MICHAEL BORKE /////
package gui;

import interfaces.Feeder;
import interfaces.Kittable;
import interfaces.Server;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.List;

import javax.swing.*;

import NonAgent.Conveyor;
import NonAgent.EnteringConveyor;
import NonAgent.ExitingConveyor;
import NonAgent.Kit;
import NonAgent.Part;
import NonAgent.Part.myState;
import NonAgent.Stand;
import agents.KitRobotAgent;
import agents.LaneAgent;
import controllers.ConveyorSystemController;
import controllers.GantryController;
import controllers.KitRobotController;
import controllers.LanesysController;
import controllers.PartRobotController;
import controllers.VisionController;

import java.io.*;
import java.net.*;

public class GUIServer implements Server
{
	int runLaneCalls = 0;
	// //////// SAVE FILE VARIABLES //////////
	FileOutputStream fOut;
	FileInputStream fIn;
	ObjectOutputStream obOut;
	ObjectInputStream obIn;

	// //////// NETWORKING VARIABLES //////////
	ServerSocket ss;
	List<Handler> Handlers = Collections.synchronizedList(new ArrayList<Handler>());
	volatile msgObject message;
	volatile String command;

	// //////// FACTORY GUI VARIABLES //////////
	volatile ArrayList<GKit> kitTypeList;
	volatile ArrayList<GPart> partList;
	volatile GKit chosenKit;

	// //////// FACTORY CONTROLLER VARIABLES //////////
	volatile KitRobotController kitRobotController;
	volatile PartRobotController partRobotController;
	volatile GantryController gantryController;
	volatile LanesysController lanesysController;
	volatile VisionController visionController;
	volatile ConveyorSystemController conveyorSystemController;


	// //////// SERVER DEFAULT CONSTRUCTOR //////////
	public GUIServer()
	{
		// /// DATA INITIALIZATION /////

		kitTypeList = new ArrayList<GKit>();
		partList = new ArrayList<GPart>();
		chosenKit = new GKit();

		// /// SAVED DATA SETUP /////
		try
		{
			fIn = new FileInputStream("FACTORY_SAVE_FILE.sav");
			obIn = new ObjectInputStream(fIn);
			kitTypeList = (ArrayList<GKit>) obIn.readObject();
			partList = (ArrayList<GPart>) obIn.readObject();
		} catch (FileNotFoundException e)
		{
			System.out.println("GUIServer: No save file found");
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				fIn.close();
				obIn.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		// /// SERVER NETWORKING SETUP /////
		try
		{
			ss = new ServerSocket(63432);
			ss.setReuseAddress(true);
			System.out.println("GUIServer: Waiting for connections...");
			for (int i = 0; i < 1; i++)
			{
				// Modify the above number to test
				Socket s = ss.accept();
				System.out.println("GUIServer: Connection Successful");
				Handlers.add(new Handler(s));
				new Thread(Handlers.get(Handlers.size() - 1)).start();
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	//////// SERVER CONSTRUCTION FOR INTEGRATION /////////
	public GUIServer(KitRobotController kitRobotController,
			ConveyorSystemController conveyorController, GantryController gantryController,
			LanesysController laneSysController, PartRobotController partRobotController,
			VisionController visionController)
	{
		// SETTING CONTROLLERS
		this.kitRobotController = kitRobotController;
		this.conveyorSystemController = conveyorController;
		this.gantryController = gantryController;
		this.visionController = visionController;
		this.lanesysController = laneSysController;
		this.partRobotController = partRobotController;

		// SETTING SERVER TO CONTROLLER
		this.kitRobotController.setServer(this);
		this.conveyorSystemController.setServer(this);
		this.gantryController.setServer(this);
		this.visionController.setServer(this);
		this.partRobotController.setServer(this);
		this.lanesysController.server = this;
		this.lanesysController.SetServerRefs();

		// /// DATA INITIALIZATION /////
		kitTypeList = new ArrayList<GKit>();
		partList = new ArrayList<GPart>();
		chosenKit = new GKit();

		// /// SAVED DATA SETUP /////
		try
		{
			fIn = new FileInputStream("FACTORY_SAVE_FILE.sav");
			obIn = new ObjectInputStream(fIn);
			kitTypeList = (ArrayList<GKit>) obIn.readObject();
			System.out.println("GUIServer: Heidi::: kitTypeList size when reading"+kitTypeList.size());
			partList = (ArrayList<GPart>) obIn.readObject();
		} catch (FileNotFoundException e)
		{
			System.out.println("GUIServer: No save file found");
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}

		// /// SERVER NETWORKING SETUP /////
		try
		{
			ss = new ServerSocket(63432);
			ss.setReuseAddress(true);
			System.out.println("GUIServer: Waiting for connections...");
			
			for (int i = 0; i < 20; i++)
			{
				Socket s = ss.accept();
				System.out.println("GUIServer: Connection Successful");
				Handlers.add(new Handler(s));
				new Thread(Handlers.get(Handlers.size() - 1)).start();
			}
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	// //////// CONTROLLER METHODS //////////
	
	// CONVEYOR CONTROLLER//
	public void doMove(Kit kit, Conveyor conveyor)
	{
		try
		{
			msgObject temp=new msgObject();
			temp.setCommand("Conveyor_MoveKit");
			temp.addParameters(kit);
			temp.addParameters(conveyor);
			synchronized (Handlers)
			{
				for (Handler h : Handlers)
				{
					h.oos.writeObject(temp);
					h.oos.flush();
					h.oos.reset();
				}
			}
		}catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	// KITROBOT CONTROLLER//
	public void doPutKit(Kittable origin, Kittable destination, Kit kit)
	{
		msgObject temp=new msgObject();
		temp.setCommand("KitRobot_PutKit");
		temp.addParameters(origin);
		temp.addParameters(destination);
		temp.addParameters(kit);
		synchronized (Handlers)
		{
			for (Handler h : Handlers)
			{
				try
				{
					h.oos.writeObject(temp);
					h.oos.flush();
					h.oos.reset();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	// PARTROBOT CONTROLLER //
	public void doPickUpFromNest(int nestNumber)
	{
		try {
			msgObject temp=new msgObject();
			temp.setCommand("PartRobot_PickUpParts");
			temp.addParameters((Integer) nestNumber);
			synchronized (Handlers)
			{
				for (Handler h : Handlers)
				{
					h.oos.writeObject(temp);
					h.oos.flush();
					h.oos.reset();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void doMoveToNest(int nestNumber)
	{
		try {
			msgObject temp=new msgObject();
			temp.setCommand("PartRobot_MoveToNest");
			temp.addParameters((Integer)nestNumber);
			synchronized (Handlers)
			{
				for (Handler h : Handlers)
				{
					h.oos.writeObject(temp);
					h.oos.flush();
					h.oos.reset();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void doMoveToKit(Kit kit)
	{
		try {
			msgObject temp=new msgObject();
			temp.setCommand("PartRobot_MoveToKit");
			temp.addParameters(kit);
			synchronized (Handlers)
			{
				for (Handler h : Handlers)
				{
					h.oos.writeObject(temp);
					h.oos.flush();
					h.oos.reset();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public void doDropPartsInKit()
	{
		try {
			msgObject temp=new msgObject();
			temp.setCommand("PartRobot_DropPartsInKit");
			synchronized (Handlers)
			{
				for (Handler h : Handlers)
				{
					h.oos.writeObject(temp);
					h.oos.flush();
					h.oos.reset();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void doDropPartsToGround() {
		try {
			msgObject temp=new msgObject();
			temp.setCommand("PartRobot_DropPartsToGround");
			synchronized (Handlers)
			{
				for (Handler h : Handlers)
				{
					h.oos.writeObject(temp);
					h.oos.flush();
					h.oos.reset();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// GANTRY SYSTEM CONTROLLER //
	public void doReleaseBinToGantry(int feederNumber)
	{
		msgObject message = new msgObject();
		message.setCommand("GantryRobot_ReleaseBinToGantry");
		message.addParameters(feederNumber);
		synchronized (Handlers)
		{
			for (Handler h : Handlers)
			{
				try
				{
					h.oos.writeObject(message);
					h.oos.flush();
					h.oos.reset();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
	}

	public void doPurgeToBin(int feederNumber)
	{
		msgObject message=new msgObject();
		message.setCommand("Feeder_PurgeToBin");
		message.addParameters(feederNumber);
		
		synchronized (Handlers)
		{
			for (Handler h : Handlers)
			{
				try
				{
					h.oos.writeObject(message);
					h.oos.flush();
					h.oos.reset();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public void doDiverter(int feederNumber, boolean divert)
	{

	}

	public void doPlaceBin(int feederNumber)
	{

		msgObject message = new msgObject();
		message.setCommand("Feeder_PlaceBin");
		message.addParameters(feederNumber);
		synchronized (Handlers)
		{
			for (Handler h : Handlers)
			{
				try
				{
					h.oos.writeObject(message);
					h.oos.flush();
					h.oos.reset();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public void doSetFeederDelayButtonsEnabled(int position, int position2)
	{
		msgObject message = new msgObject();
		message.setCommand("FeederButtonEnable");
		message.addParameters(position);
		message.addParameters(position2);
		synchronized (Handlers)
		{
			for (Handler h : Handlers)
			{
				try
				{
					h.oos.writeObject(message);
					h.oos.flush();
					h.oos.reset();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public void doSetFeederDelayButtonsDisabled(int position, int position2)
	{
		msgObject message = new msgObject();
		message.setCommand("FeederButtonDisable");
		message.addParameters(position);
		message.addParameters(position2);
		synchronized (Handlers)
		{
			for (Handler h : Handlers)
			{
				try
				{
					h.oos.writeObject(message);
					h.oos.flush();
					h.oos.reset();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	// NEST //
	public void doPurgeLane(int nestNumber)
	{
		msgObject message=new msgObject();
		message.setCommand("AgentInfo_PurgeLane");
		message.addParameters(new Integer(nestNumber));
		synchronized (Handlers)
		{
			for (Handler h : Handlers)
			{
				try
				{
					h.oos.writeObject(message);
					h.oos.flush();
					h.oos.reset();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}


	
	public void doPickUpNewBin(String partType)
	{
		msgObject message = new msgObject();
		message.setCommand("GantryRobot_DoPickUpNewBin");
		message.addParameters(partType);
		synchronized (Handlers)
		{
			for (Handler h : Handlers)
			{
				try
				{
					h.oos.writeObject(message);
					h.oos.flush();
					h.oos.reset();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public void doDeliverBinToFeeder(int feederNumber)
	{
		msgObject message = new msgObject();
		message.setCommand("GantryRobot_DoDeliverBinToFeeder");
		message.addParameters(feederNumber);
		synchronized (Handlers)
		{
			for (Handler h : Handlers)
			{
				try
				{
					h.oos.writeObject(message);
					h.oos.flush();
					h.oos.reset();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public void doDropBin()
	{

		msgObject message = new msgObject();
		message.setCommand("GantryRobot_DoDropBin");
		synchronized (Handlers)
		{
			for (Handler h : Handlers)
			{
				try
				{
					h.oos.writeObject(message);
					h.oos.flush();
					h.oos.reset();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
	}

	public void doRemoveBin(int feederNumber)
	{

	}

	public void doPickUpPurgedBin(int feederNumber)
	{
		msgObject message = new msgObject();
		message.setCommand("GantryRobot_DoPickUpPurgedBin");
		message.addParameters(feederNumber);
		synchronized (Handlers)
		{
			for (Handler h : Handlers)
			{
				try
				{
					h.oos.writeObject(message);
					h.oos.flush();
					h.oos.reset();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public void doDeliverBinToRefill()
	{

		msgObject message = new msgObject();
		message.setCommand("GantryRobot_DoDeliverBinToRefill");
		synchronized (Handlers)
		{
			for (Handler h : Handlers)
			{
				try
				{
					h.oos.writeObject(message);
					h.oos.flush();
					h.oos.reset();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	// LANE SYSTEM CONTROLLER//
	public void doRunLane(int laneNumber, Part part, int numParts)
	{
		try
		{			
			msgObject temp=new msgObject();
			temp.setCommand("Lane_RunLane");
			temp.addParameters((Integer)laneNumber);
			//temp.addParameters(convertToAddress(part.partType));
		
			if(part.state == myState.good)
			{
				temp.addParameters(convertToAddress(part.partType));
			}
			else if(part.state == myState.bad)
			{
				System.out.println("try to feed a bad part");
				temp.addParameters(new String("src/resources/badPart.png"));
			}
			
			temp.addParameters((Integer)numParts);
			synchronized (Handlers)
			{
				for (Handler h : Handlers)
				{
					h.oos.writeObject(temp);
					h.oos.reset();
				}
			}
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public void DoJamLane(int laneID) 
	{
		try
		{
			msgObject temp=new msgObject();
			temp.setCommand("AgentInfo_JamLane");
			temp.addParameters(new Integer(laneID));
			synchronized (Handlers)
			{
				for (Handler h : Handlers)
				{
					h.oos.writeObject(temp);
					h.oos.reset();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void DoUnjamLane(int laneID) 
	{
		try
		{
			msgObject temp=new msgObject();
			temp.setCommand("AgentInfo_UnjamLane");
			temp.addParameters(new Integer(laneID));
			synchronized (Handlers)
			{
				for (Handler h : Handlers)
				{
					h.oos.writeObject(temp);
					h.oos.reset();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void purgeNestOnly(int nestID)
	{
		System.out.println("GUIServer purgeNestOnly called with " + nestID);
		try
		{
			msgObject message=new msgObject();
			message.setCommand("AgentInfo_PurgeNestOnly");
			message.addParameters(new Integer(nestID));
			synchronized (Handlers)
			{
				for (Handler h : Handlers)
				{
					h.oos.writeObject(message);
					h.oos.reset();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void doGivePart(int nestNumber)
	{
		
	}
	
	// VISION //
	public void doShoot(int nestNumber)
	{
		try
		{
			msgObject temp=new msgObject();
			temp.setCommand("Camera_Shoot");
			temp.addParameters((Integer)nestNumber);
			synchronized (Handlers)
			{
				for (Handler h : Handlers)
				{
					h.oos.writeObject(temp);
					h.oos.flush();
					h.oos.reset();
				}
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void doShootKit()
	{
		try
		{
			msgObject temp=new msgObject();
			temp.setCommand("Camera_ShootKit");
			synchronized (Handlers)
			{
				for (Handler h : Handlers)
				{
					h.oos.writeObject(temp);
					h.oos.flush();
					h.oos.reset();
				}
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}

	// SETTERS //
	public void setKitRobotController(KitRobotController kitRobotController)
	{
		this.kitRobotController = kitRobotController;
	}

	public void setPartRobotController(PartRobotController partRobotController)
	{
		System.out.println("GUIServer: PartRobotController is : " + partRobotController);
		this.partRobotController = partRobotController;
	}

	public void setVisionController(VisionController visionController)
	{
		this.visionController = visionController;
	}

	public void setConveyorSystemController(ConveyorSystemController conveyorSystemController)
	{
		this.conveyorSystemController = conveyorSystemController;
	}

	public void setGantryController(GantryController gantryController)
	{
		this.gantryController = gantryController;
	}

	public void setLaneAgent(LanesysController laneAgent)
	{
		this.lanesysController = laneAgent;
	}

	///////CLASS//////
	public class Handler implements Runnable
	{
		private Socket mySocket;
		private ObjectOutputStream oos;
		private ObjectInputStream ois;

		public Handler(Socket s)
		{
			mySocket = s;
			try
			{
				oos = new ObjectOutputStream(mySocket.getOutputStream());
				oos.flush();
				ois = new ObjectInputStream(mySocket.getInputStream());
			} catch (Exception e)
			{
				//System.out.println("GUIServer: Problems");
			}
		}

		public synchronized void run()
		{
			try
			{
				while (true)
				{
					//here is the message receiving part
					msgObject message=(msgObject) ois.readObject();
					command = message.getCommand();
					
					// FACTORY COMMANDS //
					if (command.equals("UpdatePartsList"))
					{
						partList = (ArrayList<GPart>)message.getObject();
						synchronized (Handlers) {
							for (Handler sst : Handlers) {
								sst.oos.writeObject(message);
								sst.oos.flush();
								sst.oos.reset();
							}
						}
					}

					else if (command.equals("Kits"))
					{
						message.addParameters(kitTypeList);
						synchronized (Handlers) {
							for (Handler sst : Handlers) {
								sst.oos.writeObject(message);
								sst.oos.flush();
								sst.oos.reset();
							}
						}
					}
					//FPM MANAGER COMMANDS//
					else if (command.equals("Kit_Chosen"))
					{
						chosenKit = (GKit) message.getObject();
						chosenKit.setPartsList();
						partRobotController.msgGiveConfig(chosenKit.partsKit,
								chosenKit.kitName, chosenKit.kitNumber);
					}
					else if(command.equals("Delay_Lane"))
					{
						Integer lane_index=(Integer) message.popObject();						
						visionController.findProblem(lane_index);
						lanesysController.DoJamLane(lane_index);
					}
					
					else if (command.equals("Kit_TakePicture_Done")) {
						visionController.animDone();
					}

					else if (command.equals("FPM_Kits"))
					{
						message.addParameters(kitTypeList);
						synchronized (Handlers) {
							for (Handler sst : Handlers) {
								sst.oos.writeObject(message);
								sst.oos.flush();
								sst.oos.reset();
							}
						}
					}
					else if (command.equals("Factory_DoMakeBadKit")) {
						System.out.println("GUIServer: msgMakeBadKit Called!!!");

						partRobotController.msgMakeBadKit();
					}
					else if (command.equals("Factory_DoMakeBadParts"))
					{
						Integer feederNumber = (Integer)message.popObject();
						lanesysController.doFeedBadParts(feederNumber);
						//TODO: tell the agent to produce the bad part
						
					}
					else if (command.equals("RequestPartsList"))
					{
						message.addParameters(partList);
						synchronized (Handlers) {
							for (Handler sst : Handlers) {
								sst.oos.writeObject(message);
								sst.oos.flush();
								sst.oos.reset();
							}
						}
					}
					else if(command.equals("GPartManager_RequestPartsList"))
					{
						message.addParameters(partList);
						synchronized (Handlers) {
							for (Handler sst : Handlers) {
								sst.oos.writeObject(message);
								sst.oos.flush();
								sst.oos.reset();
							}
						}
						
					}
				
						
					// KIT MANAGER COMMANDS //
					else if (command.equals("UpdateKitList"))
					{
						kitTypeList = (ArrayList<GKit>)message.getObject();
						synchronized (Handlers) {
							for (Handler sst : Handlers) {
								sst.oos.writeObject(message);
								sst.oos.flush();
								sst.oos.reset();
							}
						}
					}
					// CONVEYOR COMMANDS //
					else if (command.equals("ConveyorIn_AnimationDone"))
					{
						synchronized(Handlers)
						{
							System.out.println("GuiServer: handlers' size"+ Handlers.size());
							for(Handler h:Handlers)
							{
								System.out.
								println("GUIServer: h: " + h + " and handler.this = " + Handler.this);
								if(h != Handler.this)
								{
									System.out.println("GuiServer: in message sending to clients "
								+ message.getCommand());
									h.oos.writeObject(message);
									System.out
									.println("GuiServer: message written to clients "
											+ message.getCommand());
									h.oos.flush();
									h.oos.reset();
									System.out
											.println("GuiServer: message sent to clients "
													+ message.getCommand());
									
								}

							}
							System.out.println("Out of the for loop");
						}

						conveyorSystemController.animDone();
					}
					
					else if (command.equals("ConveyorOut_AnimationDone")) {
						conveyorSystemController.kitIsOutOfCell((Kit) message.popObject());
					}
					
					// KITROBOT COMMANDS //
					else if (command.equals("KitRobot_AnimationDone"))
					{
						kitRobotController.animDone();
					}

					// CONVEYOR COMMANDS //
					else if (command.equals("ConveyorIn_AnimationDone"))
					{
						conveyorSystemController.animDone();
					}
					
					// PARTROBOT COMMANDS
					else if (command.equals("PartRobot_PickUpParts_Done")) {
						synchronized (Handlers)
						{
							for (Handler h : Handlers)
							{
								h.oos.writeObject(message);
								h.oos.flush();
								h.oos.reset();
							}
						}	
					}
					

					else if (command.equals("PartRobot_AnimationDone"))
					{
						partRobotController.animDone();
					}

					
					else if (command.equals("Feeder_ReleaseBinToGantry_Done"))
					{
						// partRobotController.animDone();
					}
					else if(command.equals("Feeder_DelayFeeder"))
					{
						Integer feeder_index=(Integer) message.popObject();
						lanesysController.doDelayFeeder(feeder_index);
						visionController.findProblem((2*feeder_index)+1);
					}
					else if(command.equals("Diverter_DelayDiverter"))
					{
						synchronized (Handlers) {
							for (Handler sst : Handlers) {
								sst.oos.writeObject(message);
								sst.oos.flush();
								sst.oos.reset();
							}
						}
						Integer diverter_index=(Integer) message.popObject();
						lanesysController.doDelayDiverter(diverter_index.intValue());
						visionController.findProblem(diverter_index.intValue());
					}

					else if (command.equals("Feeder_PlaceBin_Done"))
					{
						// partRobotController.animDone();
					}
					else if (command.equals("Feeder_ChangeDiverter_Done"))
					{
						// partRobotController.animDone();
					}

					// LANE COMMANDS //

					else if (command.equals("Lane_RunLane_Done"))
					{
						// lanesysController.MessageLane((Integer)
						// ois.readObject());
					}
					else if(command.equals("Lane_JamLane"))
					{
						Integer lane_index=(Integer) message.popObject();
						lanesysController.doJamLane(lane_index);
						visionController.findProblem(lane_index.intValue());
					}
					else if(command.equals("Lane_UnjamLane"))
					{
						int lane_index=(Integer) message.popObject();
						lanesysController.DoUnjamLane(lane_index);
					}
					else if(command.equals("Lane_PurgeLane"))
					{
						int lane_index=(Integer) message.popObject();
						lanesysController.DoPurge(lane_index);
					}
					else if(command.equals("Nest_AllBad"))
					{
						System.out.println("GUIServer: received Nest_AllBad");
						int nest_index=(Integer) message.popObject();
						partRobotController.msgPurgeNestOnly(nest_index);
					}

					// NEST COMMANDS //
					else if (command.equals("Nest_TakePicture_Done"))
					{
						visionController.animDone();
					}
					else if (command.equals("Nest_PartFed")) 
					{
						Integer targetNestNumber = (Integer) message.getObject();
						lanesysController.MessageLane(targetNestNumber);
						synchronized (Handlers) {
							for (Handler sst : Handlers) {
								sst.oos.writeObject(message);
								sst.oos.flush();
								sst.oos.reset();
							}
						}
					}
					else if(command.equals("LanePurge_Done"))
					{
						Integer nest_that_finished_purging=(Integer) message.popObject();
						lanesysController.msgLanePurgeDone(nest_that_finished_purging.intValue());
					}
					// GANTRY COMMANDS //

					else if (command.equals("GantryRobot_DoPickUpNewBin_Done"))
					{
						gantryController.animDone();
					}

					else if (command.equals("GantryRobot_DoDeliverBinToFeeder_Done"))
					{
						gantryController.animDone();
					}

					else if (command.equals("GantryRobot_DoDropBin_Done"))
					{
						gantryController.animDone();
					}

					else if (command.equals("GantryRobot_DoPickUpPurgedBin_Done"))
					{
						gantryController.animDone();
					}

					else if (command.equals("GantryRobot_DoDeliverBinToRefill_Done"))
					{
						gantryController.animDone();
					}
					

					// CLOSE COMMAND //
					else if (command.equals("close"))
					{
						try
						{
							fOut = new FileOutputStream("FACTORY_SAVE_FILE.sav");
							obOut = new ObjectOutputStream(fOut);
							obOut.writeObject(kitTypeList);
							obOut.reset();
							obOut.writeObject(partList);
							obOut.reset();
							obOut.close();
							//System.out.println("Factory saved");
						} catch (FileNotFoundException e)
						{
							e.printStackTrace();
						} catch (IOException e)
						{
							e.printStackTrace();
						}
						break;
					}
				}

			} catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}

	}


		public void close()
		{
			try
			{
				mySocket.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}


	}

	String convertToAddress(String partType)
	{
		String temp = "";
		for (int i = 0; i < partList.size(); i++)
		{
			if (partList.get(i).typeName.equals(partType))
			{
				temp = partList.get(i).imageAddress;
			}
		}
		return temp;
	}

	// //////// MAIN //////////
	public static void main(String[] args)
	{
		GUIServer s = new GUIServer();
	}


}