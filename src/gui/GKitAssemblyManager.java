/* by Tian(Sky) Lan & Mingyu(Heidi) Qiao
 */

package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import NonAgent.Conveyor;
import NonAgent.EnteringConveyor;
import NonAgent.ExitingConveyor;
import NonAgent.Kit;
import NonAgent.Part;
import gui.GKitAssemblyGraphicsPanel;
import interfaces.Kittable;

public class GKitAssemblyManager extends JFrame implements ActionListener
{
	private int offset_X;
	private int offset_Y;
	private boolean fpmRunning;
	GKitAssemblyGraphicsPanel KAMPanel;

	ArrayList<JButton> testButtons;
	JPanel controlPanel;
	Object conveyorPassedIn;
	Kit exitingKit;
	Timer timer; // this is basically just to check the animation done state
	GKit tempKit;

	// //////// CLIENT NETWORK VARIABLES //////////
	/* Copied from the CLient Code Snippet */
	Socket s;
	UpdateChecker updateChecker;
	ObjectOutputStream oos;
	ObjectInputStream ois;

	public class UpdateChecker implements Runnable
	{
		private Socket mySocket;
		private ObjectInputStream ois;

		public UpdateChecker(Socket s, ObjectInputStream obis)
		{
			mySocket = s;
			ois = obis;
		}

		public synchronized void run()
		{
			// This catches the command messages sent by the server
			try
			{
				while (true)
				{
					msgObject message=(msgObject) ois.readObject();
					String command = message.getCommand();
					///// Part Robot Commands /////
					if (command.equals("PartRobot_MoveToNest"))
					{
						// Integer passed in to determine which nest to move to
						KAMPanel.DoMoveToNest((Integer)message.popObject());
					}

					else if (command.equals("PartRobot_MoveToKit"))
					{
						// Kit passed in to determine which kit to move to
						Kit myKit = (Kit) message.popObject();
						Integer standNumber = myKit.getStand();
						
						// 0 conveyorin, 1 left stand, 2 right stand, 3 inspection stand, 4 conveyorOut
						
						KAMPanel.DoMove(standNumber);
					}

					else if (command.equals("PartRobot_PickUpParts"))
					{
						// Integer passed in as the nest number to pick from
						Integer tempNestNumber = (Integer)message.popObject();
						KAMPanel.DoPickUp(tempNestNumber);
						if (fpmRunning) {
							msgObject pickUpDoneMessage = new msgObject();
							pickUpDoneMessage.setCommand("PartRobot_PickUpParts_Done");
							pickUpDoneMessage.addParameters(tempNestNumber);
							oos.writeObject(pickUpDoneMessage);
							oos.reset();
						}

					}

					else if (command.equals("PartRobot_DropPartsInKit"))
					{
						// Part Robot puts parts down to kit
						KAMPanel.DoPutDownToKit();
					}
					
					else if (command.equals("PartRobot_DropPartsToGround"))
					{
						KAMPanel.doDropDownToGround();
					}

					////// Nest Commands /////
					else if (command.equals("Camera_Shoot"))
					{
						// Take a picture of the nest according to integer
						// passed in as the nest number
						Integer integer = (Integer) message.popObject();
						KAMPanel.DoShoot(integer);
						sendUpdate("Nest_TakePicture_Done");
					}
					else if (command.equals("Nest_PartFed"))
					{
						Integer nestNumber = (Integer) message.popObject();
						String partImageAddress = (String) message.popObject();
						KAMPanel.nest.get(nestNumber).addPartToNest(partImageAddress);
					}

					///// Conveyor Commands /////
					else if (command.equals("Conveyor_MoveKit"))
					{

						
						Kit kit = (Kit)message.popObject();
						Conveyor conveyor = (Conveyor)message.popObject();
						
						if (conveyor instanceof EnteringConveyor) {
							
							KAMPanel.DoKitIn(kit);
						}
						else if (conveyor instanceof ExitingConveyor) {
							KAMPanel.DoKitOut(kit);
							exitingKit = kit;
						}
					}

					////// Kit Robot Commands /////
					else if (command.equals("KitRobot_PutKit"))
					{
						Kittable kitRobotOrigin = (Kittable) message.popObject();
						Kittable kitRobotDestination = (Kittable) message.popObject();
						Kit kitRobotKit = (Kit) message.popObject();
						
						GKittable GOrigin = null;
						GKittable GDestination = null;
						
						if (kitRobotOrigin.getGui() instanceof GConveyorIn) {
							GOrigin = KAMPanel.conveyorIn;
						}
						else if (kitRobotOrigin.getGui() instanceof GKittingStand) {
							if (kitRobotOrigin.getGui().getY() == 550) {
								GOrigin = KAMPanel.rightWorkingStand;
							}
							else if (kitRobotOrigin.getGui().getY() == 350) {
								GOrigin = KAMPanel.leftWorkingStand;
							}
							else if (kitRobotOrigin.getGui().getY() == 100) {
								GOrigin = KAMPanel.inspectionStand;
							}
						}
						
						
						if (kitRobotDestination.getGui() instanceof GConveyorOut) {
							GDestination = KAMPanel.conveyorOut;
						}
						else if (kitRobotDestination.getGui() instanceof GKittingStand) {
							if (kitRobotDestination.getGui().getY() == 550) {
								GDestination = KAMPanel.rightWorkingStand;
							}
							else if (kitRobotDestination.getGui().getY() == 350) {
								GDestination = KAMPanel.leftWorkingStand;
							}
							else if (kitRobotDestination.getGui().getY() == 100) {
								GDestination = KAMPanel.inspectionStand;
							}
						}
						
						KAMPanel.doPutKit(GOrigin,GDestination,kitRobotKit);
					}

					///// Stand Commands /////
					else if (command.equals("Camera_ShootKit"))
					{
						KAMPanel.shootKit();
						sendUpdate("Kit_TakePicture_Done");
					}
					
					///// Lane Commands /////
					
					else if(command.equals("AgentInfo_PurgeLane"))
					{
						Integer nest_index=(Integer) message.popObject();
						KAMPanel.nest.get(nest_index).purgeNest();
					}
					else if(command.equals("AgentInfo_PurgeNestOnly"))
					{
						Integer nest_index=(Integer) message.popObject();
						KAMPanel.nest.get(nest_index).purgeNest();
					}
					
					///// Unrelated Commands /////
					else
					{
						// Not recognizing the command.
					}
				}
			} catch (EOFException e)
			{
			} catch (IOException e)
			{
				e.printStackTrace();
			} catch (Exception e)
			{
				e.printStackTrace();
			} finally
			{
				try
				{
					mySocket.close();
					s.close();
					//r.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	// Constructor
	public GKitAssemblyManager(int offset_X, int offset_Y, boolean fpmRunning)
	{

		this.offset_X = offset_X;
		this.offset_Y = offset_Y;
		this.fpmRunning = fpmRunning;
		// Establish Connection
		try
		{
			s = new Socket("localhost", 63432);
			
			oos = new ObjectOutputStream(s.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(s.getInputStream());
			
			System.out.println("GKitAssembleManager: Client Ready");
			updateChecker = new UpdateChecker(s, ois);
			new Thread(updateChecker).start();
		} catch (UnknownHostException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		KAMPanel = new GKitAssemblyGraphicsPanel(this, offset_X, offset_Y);
		setSize(500, 800);

		setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));

		add(KAMPanel);
		validate();

		timer = new Timer(500, this);
		timer.start();

	}

	public static void main(String[] args)
	{

		int offset_X = 0;
		int offset_Y = 0;
		GKitAssemblyManager window = new GKitAssemblyManager(offset_X, offset_Y, false);
		window.setSize(500, 800);
		window.setTitle("KitAssemblyManager");
		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}

	public synchronized void sendUpdate(String messageToServer)
	{
		msgObject temp = new msgObject();
		temp.setCommand(messageToServer);
		
		if (temp.getCommand().equals("ConveyorOut_AnimationDone")) {
			temp.addParameters(exitingKit);
		}
		if (fpmRunning) {
			try {
				oos.writeObject(temp);
				oos.reset();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

	}
	
	public boolean getFPMRunning() {
		return fpmRunning;
	}

	public void actionPerformed(ActionEvent ae)
	{		
		if (KAMPanel.conveyorIn.conveyorInAnimationDone)
		{
			sendUpdate("ConveyorIn_AnimationDone");
			KAMPanel.conveyorIn.conveyorInAnimationDone = false;

		}
		if (KAMPanel.conveyorOut.conveyorOutAnimationDone)
		{

			sendUpdate("ConveyorOut_AnimationDone");
			KAMPanel.conveyorOut.conveyorOutAnimationDone = false;

		}
		if (KAMPanel.kitRobot.movingState == 4)
		{
			sendUpdate("KitRobot_AnimationDone");
			KAMPanel.kitRobot.movingState = 0;
		}
		if (KAMPanel.partRobot.ifArrive)
		{
			sendUpdate("PartRobot_AnimationDone");
			KAMPanel.partRobot.ifArrive = false;
		}

	}

	public GKitAssemblyGraphicsPanel getGraphicsPanel()
	{
		return KAMPanel;
	}
}
