/* by Kevin Nguyen & Mingyu(Heidi) Qiao
 */
package gui;

import gui.ClientCodeSnippits.UpdateChecker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.awt.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class GantryRobotManager extends JFrame implements ActionListener
{
	private int offset_X;
	private int offset_Y;
	GGantrySystem gantrySystemPanel;
	// ArrayList <JButton> test;
	// JPanel controlPanel;

	// ////////CLIENT NETWORK VARIABLES //////////
	Socket s;
	//Socket r;
	UpdateChecker updateChecker;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	Timer timer;
	boolean ifLocal;

	public GantryRobotManager(int offset_x, int offset_y, boolean ifLocal)

	{
		this.ifLocal = ifLocal;
		this.offset_X = offset_X;
		this.offset_Y = offset_Y;
		try
		{
			s = new Socket("localhost", 63432);
			System.out.println("Gantry Robot Manager:s connected");
			//r = new Socket("localhost", 63432);
			System.out.println("Gantry Robot Manager:r connected");
			oos = new ObjectOutputStream(s.getOutputStream());
			oos.flush();
			System.out.println("Gantry Robot Manager:oos created");
			ois = new ObjectInputStream(s.getInputStream());
			System.out.println("Gantry Robot Manager:Client Ready");
			updateChecker = new UpdateChecker(s, ois);
			new Thread(updateChecker).start();
		} catch (UnknownHostException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		gantrySystemPanel = new GGantrySystem(this,offset_x, offset_y);
		// setLayout(new BoxLayout(this.getContentPane(),BoxLayout.X_AXIS));
		//
		// test = new ArrayList <JButton>();
		// controlPanel= new JPanel();
		// controlPanel.setLayout(new BoxLayout(controlPanel,BoxLayout.Y_AXIS));
		// test.add (new JButton("DoPickUpNewBin(partType)"));
		// test.add (new
		// JButton("DoDeliverBinToFeeder(feederNumber, partType)"));
		// test.add (new JButton("DoDropBin"));
		// test.add (new JButton("DoPickUpPurgedBin(feederNumber)"));
		// test.add (new JButton("DoDeliverBinToRefill"));
		// for(int i=0;i<5;i++)
		// {
		// test.get(i).addActionListener(this);
		// controlPanel.add(test.get(i));
		//
		// }
		// add(controlPanel);
		add(gantrySystemPanel);
		timer = new Timer(20, this);
		timer.start();

	}

	public static void main(String[] args)
	{

		GantryRobotManager window = new GantryRobotManager(0, 0, false);
		window.setSize(275, 800);
		window.setTitle("GantryRobotManager");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}

	public void actionPerformed(ActionEvent ae)
	{
		try
		{
			if (gantrySystemPanel != null && gantrySystemPanel.doPickUpNewBinDone && ifLocal) // need server to catch it
			{
				msgObject temp=new msgObject();
				temp.setCommand("GantryRobot_DoPickUpNewBin_Done");
				oos.writeObject(temp);
				oos.reset();
				System.out.println("Gantry Robot Manager:doPickUpNewBinDone is true");
				gantrySystemPanel.doPickUpNewBinDone = false;
			}

			if (gantrySystemPanel != null && gantrySystemPanel.doDeliverBinToFeederDone && ifLocal) // need server to catch it
			{
				msgObject temp=new msgObject();
				temp.setCommand("GantryRobot_DoDeliverBinToFeeder_Done");
				oos.writeObject(temp);
				oos.reset();
				System.out.println("Gantry Robot Manager:doDeliverBinToFeederDone is true");
				gantrySystemPanel.doDeliverBinToFeederDone = false;
			}

			if (gantrySystemPanel != null && gantrySystemPanel.doDropBinDone && ifLocal) // need server to catch it
			{
				msgObject temp=new msgObject();
				temp.setCommand("GantryRobot_DoDropBin_Done");
				oos.writeObject(temp);
				oos.reset();
				System.out.println("Gantry Robot Manager:doDropBinDone is true");
				gantrySystemPanel.doDropBinDone = false;
			}

			if (gantrySystemPanel != null && gantrySystemPanel.robotReadyToPickUp && ifLocal)// need server to catch its
			{
				msgObject temp=new msgObject();
				temp.setCommand("GantryRobot_FeederDropDownBin");
				temp.addParameters((Integer) gantrySystemPanel.lastFeederAt);
				oos.writeObject(temp);
				oos.reset();
				System.out.println("Gantry Robot Manager:robotReadyToPickUp is true");
				gantrySystemPanel.robotReadyToPickUp = false;
			}

			if (gantrySystemPanel != null && gantrySystemPanel.doPickUpPurgedBinDone && ifLocal) // need server to catch it
			{
				msgObject temp=new msgObject();
				temp.setCommand("GantryRobot_DoPickUpPurgedBin_Done");
				oos.writeObject(temp);
				oos.reset();
				System.out.println("Gantry Robot Manager:doPickUpPurgedBinDone is true");
				gantrySystemPanel.doPickUpPurgedBinDone = false;
			}

			if (gantrySystemPanel != null && gantrySystemPanel.doDeliverBinToRefillDone && ifLocal) // need server to catch it
			{
				msgObject temp=new msgObject();
				temp.setCommand("GantryRobot_DoDeliverBinToRefill_Done");
				oos.writeObject(temp);
				oos.reset();
				System.out.println("Gantry Robot Manager:DoDeliverBinToRefillDone is true");
				gantrySystemPanel.doDeliverBinToRefillDone = false;
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		// if(ae.getSource()==test.get(0))
		// {
		// //gantrySystemPanel.DoPickUpNewBin("enemy1");
		// try {
		// oos.writeObject("GantryRobot_DoPickUpNewBin");
		// oos.reset();
		// oos.writeObject("enemy1");
		// oos.reset();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		//
		// }
		// else if(ae.getSource()==test.get(1))
		// {
		// //gantrySystemPanel.DoDeliverBinToFeeder(1);
		// try {
		// oos.writeObject("GantryRobot_DoDeliverBinToFeeder");
		// oos.reset();
		// oos.writeObject((Integer)1);
		// oos.reset();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		//
		// }
		//
		// else if(ae.getSource()==test.get(2))
		// {
		// //gantrySystemPanel.DoDropBin();
		// try {
		// oos.writeObject("GantryRobot_DoDropBin");
		// oos.reset();
		// oos.writeObject(gantrySystemPanel.gantryRobot.binInGantry.myPartType);
		// oos.reset();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		//
		// }
		// else if(ae.getSource()==test.get(3))
		// {
		// //gantrySystemPanel.DoPickUpPurgedBin(1);
		// try {
		// oos.writeObject("GantryRobot_DoPickUpPurgedBin");
		// oos.reset();
		// oos.writeObject((Integer)1);
		// oos.reset();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		//
		// }
		//
		// else if(ae.getSource()==test.get(4))
		// //gantrySystemPanel.DoDeliverBinToRefill();
		// try {
		// oos.writeObject("GantryRobot_DoDeliverBinToRefill");
		// oos.reset();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

	}

	public class UpdateChecker implements Runnable
	{
		private Socket mySocket;
		private ObjectInputStream ois;

		public UpdateChecker(Socket s, ObjectInputStream obis)
		{
			mySocket = s;
			ois = obis;
		}

		public void run()
		{
			try
			{
				while (true)
				{

					// FACTORY COMMANDS //
					msgObject message=(msgObject)ois.readObject();
					String command = message.getCommand();
					/*
					 * if(command.equals("Kits")) { gantrySystemPanel.kitList =
					 * (ArrayList<GKit>) ois.readObject(); } else
					 */if (command.equals("Parts"))
					{
						gantrySystemPanel.partList = (ArrayList<GPart>) message.popObject();
					}
					else if (command.equals("ChosenKit"))
					{
						gantrySystemPanel.chosenKit = (GKit) message.popObject();
					}
					// CATCH IRRELEVANT COMMANDS //
					else if (command.equals("GantryRobot_DoPickUpNewBin"))
					{
						gantrySystemPanel.DoPickUpNewBin((String) message.popObject());
						System.out.println("Gantry Robot Manager: Server message received.");
					}
					else if (command.equals("GantryRobot_DoDeliverBinToFeeder"))
					{
						gantrySystemPanel.DoDeliverBinToFeeder((Integer) message.popObject());
					}
					else if (command.equals("GantryRobot_DoDropBin"))
					{
						System.out.println("Gantry Robot Manager:Bin drop command received");

						gantrySystemPanel.DoDropBin();
					}
					else if (command.equals("GantryRobot_DoPickUpPurgedBin"))
					{
						gantrySystemPanel.DoPickUpPurgedBin((Integer) message.popObject());
					}
					else if (command.equals("GantryRobot_DoDeliverBinToRefill"))
					{
						gantrySystemPanel.DoDeliverBinToRefill();
						System.out.println("Gantry Robot Manager::DoDeliverBinToRefill RECEIVED AT MANAGER");

					}
					else
					{
//						System.out.println(command);
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

	public GGantrySystem getGraphicsPanel()
	{
		return gantrySystemPanel;
	}

}
