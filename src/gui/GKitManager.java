package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class GKitManager extends JFrame implements ActionListener
{

	// //////// CLASS VARIABLES //////////

	// CLIENT NETWORK VARIABLES
	Socket s;
	//Socket r;
	UpdateChecker updateChecker;
	ObjectOutputStream oos;
	ObjectInputStream ois;

	// FACTORY VARIABLES
	ArrayList<GPart> partList;	//to store all the parts in the kit we are currently making
	ArrayList<GKit> kitTypeList;	//to store all the different kits we have created since histroy
	GKit chosenKit;		//refer to the kit we choose to send to the factory

	// GKITMANAGER VARIABLES
	ArrayList<JComboBox> cbxArrayList; // a container to hold all the comboboxes
										// used in the manager
	ArrayList<JLabel> jlabelList;
	// NEW SWING VARIABLES
	JComboBox partsBox, howManyBox, kitsBox;
	JButton selectButton, send2ServerButton, clearContent, delete, save;
	JTextArea textArea;
	StringBuffer TAString;
	JTextField configNametf, configNumtf;
	int partsInKit = 0;
	
	JButton exitButton;

	// //////// CONSTRUCTOR //////////
	public GKitManager()
	{
		initializeFactory(); // initializes Factory Variables
		initializeNewSwing();
		connectToServer(); // sets up sending and receiving sockets
		requestGPartsList(); // requests for the GParts list to build kit
		requestKitList();
	}
	// //////// MAIN //////////
	public static void main(String[] args)
	{
		GKitManager app = new GKitManager();
		app.setSize(400, 400);
		app.setTitle("Kit Manager");
		app.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		app.setVisible(true);
	}
	
	// //////// INITIALIZES FACTORY VARIABLES //////////
	public void initializeFactory()
	{
		chosenKit = new GKit();
	}

	// //////// CREATE NEW SWING PANELS //////////
	public void initializeNewSwing()
	{
		// CREATE THE PANEL TO WORK ON//
		JPanel g = new JPanel();
		g.setLayout(new GridLayout(1, 2));
		add(g);

		// LEFT SIDE PANEL //
		GridBagConstraints gbc = new GridBagConstraints();
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		g.add(p);
		
		JLabel kitsLabel = new JLabel("KITS LIST");
		gbc.gridx = 0;
		gbc.gridy = 0;
		p.add(kitsLabel, gbc);
		
		kitsBox = new JComboBox();
		kitsBox.setMaximumSize(new Dimension(150, 20));
		kitsBox.setMinimumSize(new Dimension(100, 20));
		kitsBox.setPreferredSize(new Dimension(100, 20));
		gbc.gridx = 0;
		gbc.gridy = 1;
		p.add(kitsBox, gbc);
		kitsBox.addActionListener(this);

		JLabel partsLabel = new JLabel("PARTS LIST");
		gbc.gridx = 0;
		gbc.gridy = 2;

		p.add(partsLabel, gbc);
		partsBox = new JComboBox();
		partsBox.setMaximumSize(new Dimension(150, 20));
		partsBox.setMinimumSize(new Dimension(100, 20));
		partsBox.setPreferredSize(new Dimension(100, 20));

		gbc.gridx = 0;
		gbc.gridy = 3;
		p.add(partsBox, gbc);

		JLabel howManyLabel = new JLabel("NUMBER OF SELETED PART");
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 0;
		gbc.weighty = 0;
		p.add(howManyLabel, gbc);
		howManyBox = new JComboBox();
		howManyBox.setMaximumSize(new Dimension(150, 20));
		howManyBox.setMinimumSize(new Dimension(100, 20));
		howManyBox.setPreferredSize(new Dimension(100, 20));
		for (int i = 0; i < 9; i++)
		{
			howManyBox.addItem(i);
		}
		gbc.gridx = 0;
		gbc.gridy = 5;
		p.add(howManyBox, gbc);

		JLabel configName = new JLabel("CONFIGURATION NAME");
		gbc.gridx = 0;
		gbc.gridy = 6;
		p.add(configName, gbc);
		configNametf = new JTextField();
		configNametf.setMaximumSize(new Dimension(150, 20));
		configNametf.setMinimumSize(new Dimension(100, 20));
		configNametf.setPreferredSize(new Dimension(100, 20));
		gbc.gridx = 0;
		gbc.gridy = 7;
		p.add(configNametf, gbc);

		JLabel configNum = new JLabel("CONFIGURATION NUMBER");
		gbc.gridx = 0;
		gbc.gridy = 8;
		p.add(configNum, gbc);
		configNumtf = new JTextField();
		configNumtf.setMaximumSize(new Dimension(150, 20));
		configNumtf.setMinimumSize(new Dimension(100, 20));
		configNumtf.setPreferredSize(new Dimension(100, 20));
		gbc.gridx = 0;
		gbc.gridy = 9;
		p.add(configNumtf, gbc);

		selectButton = new JButton("Set Selection");
		selectButton.setPreferredSize(new Dimension(200,20));
		gbc.insets = new Insets (80,0,0,0);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 10;
		// gbc.weightx = 0; gbc.weighty = 1;
		p.add(selectButton, gbc);
		selectButton.addActionListener(this);
		
		gbc.gridx = 0;
		gbc.gridy = 11;
		gbc.insets = new Insets (0,0,0,0);
		save = new JButton("Save Current Kit");
		save.setPreferredSize(new Dimension(200,20));
		save.addActionListener(this);
		save.setEnabled(false);
		p.add(save, gbc);
		gbc.gridx = 0;
		gbc.gridy = 12;
		delete = new JButton("Delete Selected Kit");
		delete.setPreferredSize(new Dimension(200,20));
		delete.addActionListener(this);
		p.add(delete, gbc);

		// RIGHT SIDE PANEL //
		JPanel q = new JPanel(); // right side panel.
		q.setLayout(new GridBagLayout());
		g.add(q);
		gbc.insets = new Insets (0,0,0,10);
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setMaximumSize(new Dimension(200, 250));
		textArea.setMinimumSize(new Dimension(200, 250));
		textArea.setPreferredSize(new Dimension(200, 250));
		q.add(textArea);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets (10,0,0,10);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		clearContent = new JButton("Clear Kit Contents");
		clearContent.setPreferredSize(new Dimension(200,20));
		clearContent.addActionListener(this);
		q.add(clearContent, gbc);
		send2ServerButton = new JButton("Send List to Server");
		send2ServerButton.setPreferredSize(new Dimension(200,20));
		send2ServerButton.addActionListener(this);
		gbc.insets = new Insets (0,0,0,10);
		gbc.gridx = 0;
		gbc.gridy = 3;
		q.add(send2ServerButton, gbc);
		send2ServerButton.setEnabled(false);
		
		exitButton = new JButton ("Exit");
		exitButton.setPreferredSize(new Dimension(200,20));
		exitButton.addActionListener(this);
		gbc.gridx = 0;
		gbc.gridy = 4;
		q.add(exitButton, gbc);
		exitButton.setEnabled(true);
		
		TAString = new StringBuffer("Chosen Parts \n\n");
		textArea.setText(TAString.toString());
	}
	
	// //////// CONNECTS TO SERVER //////////
		public void connectToServer()
		{
			try
			{
				s = new Socket("localhost", 63432);
				//r = new Socket("localhost", 63432);
				oos = new ObjectOutputStream(s.getOutputStream());
				ois = new ObjectInputStream(s.getInputStream());
				System.out.println("Client Ready");
				updateChecker = new UpdateChecker(s, ois);
				new Thread(updateChecker).start();
			} catch (UnknownHostException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		// //////// REQUESTS FOR GPARTSLIST FROM SERVER //////////
		public void requestGPartsList()
		{
			try
			{
				msgObject temp = new msgObject();
				temp.setCommand("RequestPartsList");
				oos.writeObject(temp);
				oos.reset();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		public void requestKitList()
		{
			try
			{
				msgObject temp = new msgObject();
				temp.setCommand("Kits");
				oos.writeObject(temp);
				oos.reset();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}


		// //////// CHANGES JLABELS ABOVE COMBOBOXES //////////
		public void changeJLabels()
		{
			/*
			 * This portion will essentially change the placeholder names to the
			 * actual names of the part passed to the KitManager
			 */
			for (int i = 0; i < partList.size(); i++)
			{
				jlabelList.get(i).setText("Quantity of " + partList.get(i).getTypeName());
			}
		}

		// //////// SEND KIT CONFIGURATION TO SERVER //////////
		public void sendListToServer()
		{
			try
			{
				msgObject temp=new msgObject();
				temp.setCommand("UpdateKitList");
				temp.addParameters(kitTypeList);
				oos.writeObject(temp);
				oos.reset();
			} catch (IOException e)
			{
				e.getStackTrace();
			}
			kitsBox.setSelectedIndex(0);
			partsInKit = 0;
			textArea.setText(TAString.toString());
			configNametf.setText("");
			configNumtf.setText("");
			TAString = new StringBuffer("Chosen Parts \n\n");
			chosenKit=new GKit();
		}
		
		//send database to server///
		public void exitAndSave()
		{
			try {
				msgObject temp=new msgObject();
				temp.setCommand("close");
				oos.writeObject(temp);
				oos.reset();
				s.close();
				//r.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(0);
		}

	// //////// ACTION PERFORMED //////////
	public void actionPerformed(ActionEvent ae)
	{
		/*
		 * Adds the numbers selected from all 8 combo boxes. If the total is
		 * greater than 8, then the manager will not allow the user to send.
		 */
		if (ae.getSource() == selectButton)
		{

			if (partsInKit + howManyBox.getSelectedIndex() <= 8 &&partsInKit + howManyBox.getSelectedIndex() >= 0)
			{
				if(howManyBox.getSelectedIndex()!=0)
				{
					for(int i = 0; i < howManyBox.getSelectedIndex(); i ++)
					{
						TAString.append(partsBox.getSelectedItem() + "\n");
					}
					textArea.setText(TAString.toString());
					for (int i = 0; i < howManyBox.getSelectedIndex(); i++)
					{ // sets a kit to work with just in the KitManager
						chosenKit.partsInKit.add(new GPart(0, 0, partList.get(
							partsBox.getSelectedIndex()).getImageAddress(), partList.get(
							partsBox.getSelectedIndex()).getTypeNumber(), partList.get(
							partsBox.getSelectedIndex()).getTypeName()));
					}
					if ((partsInKit + howManyBox.getSelectedIndex()) <= 8)
					{
						partsInKit += howManyBox.getSelectedIndex();
					}
				}
				if (partsInKit >= 4)
				{
					save.setEnabled(true);
					send2ServerButton.setEnabled(true);
				}
				else
				{
					save.setEnabled(false);
					send2ServerButton.setEnabled(false);
				}
			}
			else
			{
				save.setEnabled(false);
				send2ServerButton.setEnabled(false);
			}
		}
		else if (ae.getSource() == send2ServerButton)
		{
			sendListToServer();
			kitsBox.removeAllItems();
			kitsBox.addItem("New Kit");
			for (int i = 0; i < kitTypeList.size(); i++)
			{
				kitsBox.addItem(kitTypeList.get(i).kitName);
			}	
		}
		else if (ae.getSource() == exitButton)
		{
			exitAndSave();
		}
		else if (ae.getSource() == kitsBox)
		{
			JComboBox cb = (JComboBox) ae.getSource();
			String selectedKit = (String)cb.getSelectedItem();
			if (cb.getSelectedIndex() > 0)
			{
				for (int i = 0; i < kitTypeList.size(); i++)
				{
					if (selectedKit.equals(kitTypeList.get(i).kitName))
					{
						chosenKit = kitTypeList.get(i);
						partsInKit = chosenKit.partsInKit.size();
						System.out.println(chosenKit.kitName);
						break;
					}
				}
				if (partsInKit > 3 && partsInKit < 9)
				{
					send2ServerButton.setEnabled(true);
					save.setEnabled(true);
				}
				configNametf.setText(chosenKit.kitName);
				configNumtf.setText(Integer.toString(chosenKit.kitNumber));

				TAString.delete(0, TAString.length());
				TAString.append("Chosen Parts \n\n");
				textArea.setText(TAString.toString());
				for (int i = 0; i < chosenKit.partsInKit.size(); i++)
				{
					TAString.append(chosenKit.partsInKit.get(i).getTypeName() + "\n");
				}
				textArea.setText(TAString.toString());
				
			}
			else
			{
				partsInKit = 0;
				textArea.setText(TAString.toString());
				configNametf.setText("");
				configNumtf.setText("");
				TAString = new StringBuffer("Chosen Parts \n\n");
				textArea.setText(TAString.toString());
				chosenKit=new GKit();
			}
		}
		else if (ae.getSource() == clearContent)
		{
			chosenKit.partsInKit.clear();
			partsInKit = 0;
			TAString.delete(0, TAString.length());
			TAString.append("Chosen Parts \n\n");
			textArea.setText(TAString.toString());
			send2ServerButton.setEnabled(false);
			save.setEnabled(false);
		}
		else if (ae.getSource() == delete)
		{
			String selectedKit = (String)kitsBox.getSelectedItem();
			for (int i = 0; i < kitTypeList.size(); i++)
			{
				if (selectedKit.equals(kitTypeList.get(i).kitName))
				{
					kitTypeList.remove(i);
				}
			}
			kitsBox.removeAllItems();
			kitsBox.addItem("New Kit");
			for (int i = 0; i < kitTypeList.size(); i++)
			{
				kitsBox.addItem(kitTypeList.get(i).kitName);
			}			
		}
		else if (ae.getSource() == save)
		{
			
			String selectedKit = (String)kitsBox.getSelectedItem();
			if (selectedKit.equals("New Kit"))
			{
				chosenKit.kitName = configNametf.getText();
				chosenKit.kitNumber = Integer.parseInt(configNumtf.getText());
				kitTypeList.add(chosenKit);				
			}
			else
			{
				chosenKit.kitName = configNametf.getText();
				chosenKit.kitNumber = Integer.parseInt(configNumtf.getText());
				for(int i = 0; i < kitTypeList.size(); i++)
				{
					if (selectedKit.equals(kitTypeList.get(i).kitName))
					{
						kitTypeList.set(i, chosenKit);
						break;
					}
				}
			}
			kitsBox.removeAllItems();
			kitsBox.addItem("New Kit");
			for (int i = 0; i < kitTypeList.size(); i++)
			{
				kitsBox.addItem(kitTypeList.get(i).kitName);
			}
		}
	}

	// //////// UPDATE CHECKER //////////
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

					if (command.equals("RequestPartsList"))
					{
						partList = (ArrayList<GPart>) message.popObject();
						partsBox.removeAllItems();
						for (int i = 0; i < partList.size(); i++)
						{
							partsBox.addItem(partList.get(i).getTypeName());
						}
						
						msgObject temp = new msgObject();
						temp.setCommand("Kits");
						oos.writeObject(temp);
						oos.reset();
					}
					else if (command.equals("UpdatePartsList"))
					{
						partList = (ArrayList<GPart>) message.popObject();
						partsBox.removeAllItems();
						for (int i = 0; i < partList.size(); i++)
						{
							partsBox.addItem(partList.get(i).getTypeName());
						}
					}
					else if (command.equals("UpdateKitList"))
					{
						kitTypeList = (ArrayList<GKit>) message.popObject();
						System.out.println("Kit Manager: kitTypeList is set, it is" + kitTypeList);
						kitsBox.removeAllItems();
						kitsBox.addItem("New Kit");
						for (int i = 0; i < kitTypeList.size(); i++)
						{
							kitsBox.addItem(kitTypeList.get(i).kitName);
						}
					}
					else if (command.equals("Kits"))
					{
						kitTypeList = (ArrayList<GKit>) message.popObject();
						kitsBox.removeAllItems();
						kitsBox.addItem("New Kit");
						for (int i = 0; i < kitTypeList.size(); i++)
						{
							kitsBox.addItem(kitTypeList.get(i).kitName);
						}
					}

					// CATCH IRRELEVANT COMMANDS //
					else
					{
						System.out.println(command);
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
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

}
