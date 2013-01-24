package gui;
import javax.swing.*;

import controllers.LanesysController;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class FPM_Manager extends JFrame implements ActionListener{
	////////// CLIENT NETWORK VARIABLES //////////
	Socket s;
	//Socket r;
	UpdateChecker updateChecker;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	
	////////// FACTORY VARIABLES //////////
	ArrayList<GKit> kitList;
	ArrayList<GPart> partList;
	GKit chosenKit;
	String chosenKitString;
	
	FPM_FactoryPanel fp;
	JPanel swingPanel;
	JButton produceButton;
	JTextField amountInput;
	JComboBox kitDropDownList;
	
	JComboBox laneNumbers;
	int selectedLane=0;
	int selectedFeeder=0;
	JLabel michaelJLabel;

	JButton doDelayFeederButton;
	JButton doDelayDiverterButton;
	//JButton doDelayLaneButton;
	JButton doJamLaneButton;
//	JButton doUnjamLaneButton;
	JButton doMakeBadKit;
//	JButton doPurgeLane;
	JButton doMakeABadPart;
	JButton Mayhem;
	
	public FPM_Manager(){
		setupSockets(); //sets up sockets with server
		setLayout(new BoxLayout(this.getContentPane(),BoxLayout.X_AXIS));
		//kitList = new ArrayList<GKit>();
		fp = new FPM_FactoryPanel(this);	
		add(fp);
		try{
			msgObject temp=new msgObject();
			temp.setCommand("FPM_Kits");
			oos.writeObject(temp);
			oos.reset();
		}
		catch (IOException ie){
		}
		//setupSwingPanel();
	}
	
	public static void main(String[] args){
		FPM_Manager fpm = new FPM_Manager();
		fpm.setSize(1200,800);
		fpm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fpm.setVisible(true);
		MP3.playLooping();
	}
	
	private void setupSwingPanel() {
		swingPanel = new JPanel();
		swingPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		kitDropDownList = new JComboBox();
		kitDropDownList.addItem("Please select a kit");
		for (int i=0; i<kitList.size(); i++) {
			kitDropDownList.addItem(kitList.get(i).kitName);
		}
		amountInput = new JTextField();
		amountInput.setMaximumSize(new Dimension(200,30));
		amountInput.setMinimumSize(new Dimension(200,30));
		kitDropDownList.setMaximumSize(new Dimension(200,30));
		kitDropDownList.setMinimumSize(new Dimension(200,30));
		produceButton = new JButton("Produce");
		ImageIcon buttonIcon = new ImageIcon("src/resources/blue_button.png");
		ImageIcon buttonIconHover = new ImageIcon("src/resources/blue_button_hover.png");
		ImageIcon buttonIconPressed= new ImageIcon("src/resources/blue_button_hover.png");
		produceButton.setIcon(buttonIcon);
		
		produceButton.addActionListener(this);
		produceButton.setEnabled(false);
		kitDropDownList.addActionListener(this);
		
		laneNumbers = new JComboBox();
		for(int i = 0 ; i < 8 ; i++){
			laneNumbers.addItem("lane " + i + " / feeder " + i/2);
		}
		laneNumbers.setMaximumSize(new Dimension(200,30));
		laneNumbers.setMinimumSize(new Dimension(200,30));
		ArrayList<JButton> buttonList = new ArrayList<JButton>();
		Mayhem=new JButton("Mayhem");
		Mayhem.setHorizontalAlignment(SwingConstants.LEFT);
		buttonList.add(produceButton);
//		Mayhem.setIcon(buttonIcon);
//		Mayhem.setPressedIcon(buttonIconHover);
		Mayhem.addActionListener(this);
		buttonList.add(Mayhem);
		doDelayFeederButton=new JButton("Do Delay Feeder");
		doDelayFeederButton.setEnabled(false);
		doDelayFeederButton.setHorizontalAlignment(SwingConstants.LEFT);
		doDelayFeederButton.addActionListener(this);
//		doDelayFeederButton.setIcon(buttonIcon);
//		doDelayFeederButton.setPressedIcon(buttonIconHover);
		buttonList.add(doDelayFeederButton);
		doDelayDiverterButton=new JButton("Do Delay Diverter");
		doDelayDiverterButton.setHorizontalAlignment(SwingConstants.LEFT);
		doDelayDiverterButton.addActionListener(this);
//		doDelayDiverterButton.setEnabled(false);
//		doDelayDiverterButton.setIcon(buttonIcon);
//		doDelayDiverterButton.setPressedIcon(buttonIconHover);
		buttonList.add(doDelayDiverterButton);
//		doDelayLaneButton=new JButton("Do Delay Lane");
//		doDelayLaneButton.setHorizontalAlignment(SwingConstants.LEFT);
//		doDelayLaneButton.addActionListener(this);
//		buttonList.add(doDelayLaneButton);
//		doDelayLaneButton.setIcon(buttonIcon);
//		doDelayLaneButton.setPressedIcon(buttonIconHover);
		doJamLaneButton=new JButton("Do Jam Lane");
		doJamLaneButton.setHorizontalAlignment(SwingConstants.LEFT);
		doJamLaneButton.addActionListener(this);
		buttonList.add(doJamLaneButton);
//		doJamLaneButton.setIcon(buttonIcon);
//		doJamLaneButton.setPressedIcon(buttonIconHover);
//		doUnjamLaneButton=new JButton("Do Unjam Lane");
//		doUnjamLaneButton.addActionListener(this);
		doMakeABadPart = new JButton ("Make bad parts");
		doMakeABadPart.setHorizontalAlignment(SwingConstants.LEFT);
		doMakeABadPart.addActionListener(this);
//		doMakeABadPart.setIcon(buttonIcon);
//		doMakeABadPart.setPressedIcon(buttonIconHover);
		buttonList.add(doMakeABadPart);
		doMakeBadKit=new JButton("Do Make Bad Kit");
		doMakeBadKit.setHorizontalAlignment(SwingConstants.LEFT);
		doMakeBadKit.addActionListener(this);
		buttonList.add(doMakeBadKit);
//		doMakeBadKit.setIcon(buttonIcon);
//		doMakeBadKit.setPressedIcon(buttonIconHover);
//		doPurgeLane=new JButton("Do Purge Lane");
//		doPurgeLane.setHorizontalAlignment(SwingConstants.LEFT);
//		doPurgeLane.addActionListener(this);
//		doPurgeLane.setIcon(buttonIcon);
//		doPurgeLane.setPressedIcon(buttonIconHover);
//		buttonList.add(doPurgeLane);
		laneNumbers.addActionListener(this);
		for(JButton button : buttonList){
			button.setIcon(buttonIcon);
			button.setRolloverIcon(buttonIconHover);
			button.setPressedIcon(buttonIconPressed);
		}
		
		JLabel topBackground = new JLabel(new ImageIcon("src/resources/SWGPanelBG.png"));
		
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0,50,0,50);
		michaelJLabel = new JLabel(new ImageIcon("src/resources/jackson_2.gif"));
		JLabel jl = new JLabel("Kit Configuration");
		gbc.gridx = 0; gbc.gridy = -1;
		swingPanel.add(michaelJLabel, gbc);
		gbc.gridx = 0; gbc.gridy = 0;
		swingPanel.add(topBackground, gbc);
		gbc.gridx = 0; gbc.gridy = 1; 
		swingPanel.add(jl, gbc);
		gbc.gridx = 0; gbc.gridy = 2; 
		swingPanel.add(kitDropDownList, gbc);
		JLabel jl2 = new JLabel("Amount");
		gbc.gridx = 0; gbc.gridy = 3;
		swingPanel.add(jl2,gbc);
		gbc.gridx = 0; gbc.gridy = 4; 
		swingPanel.add(amountInput, gbc);
		gbc.gridx = 0; gbc.gridy = 5; 
		swingPanel.add(produceButton, gbc);
		
		JLabel jl3 = new JLabel("<html>Choose lane # for non-normative</html>");
		gbc.gridx = 0; gbc.gridy = 6; 		
		gbc.ipady = 20;
		swingPanel.add(jl3, gbc);
		gbc.ipady = 0;
		gbc.gridx = 0; gbc.gridy = 7; 
		swingPanel.add(laneNumbers, gbc);
		gbc.gridx = 0; gbc.gridy = 8; 
		swingPanel.add(doDelayFeederButton, gbc);
		gbc.gridx = 0; gbc.gridy = 9; 
		swingPanel.add(doDelayDiverterButton, gbc);
//		gbc.gridx = 0; gbc.gridy = 10; 
//		swingPanel.add(doDelayLaneButton, gbc);
		gbc.gridx = 0; gbc.gridy = 10; 
		swingPanel.add(doJamLaneButton, gbc);
		gbc.gridx = 0; gbc.gridy = 11; 
//		swingPanel.add(doUnjamLaneButton, gbc);
//		gbc.gridx = 0; gbc.gridy = 12; 
//		swingPanel.add(doPurgeLane, gbc);
		gbc.gridx = 0; gbc.gridy = 12; 
		swingPanel.add(doMakeBadKit,gbc);
		gbc.gridx = 0; gbc.gridy = 13; 
		swingPanel.add(doMakeABadPart,gbc);
		gbc.gridx = 0; gbc.gridy = 14; 
		swingPanel.add(Mayhem,gbc);
		swingPanel.setMaximumSize(new Dimension(200,800));
		swingPanel.setMinimumSize(new Dimension(200,800));
		add(swingPanel);
		validate();
		repaint();
	}
	
	public void updateList()
	{
		kitDropDownList.removeAllItems();
		kitDropDownList.addItem("Please select a kit");
		for (int i=0; i<kitList.size(); i++) {
			kitDropDownList.addItem(kitList.get(i).kitName);
		}
		swingPanel.validate();
		swingPanel.repaint();
	}

//	private String addWhiteSpaces(String add){
//		String returnString="";
//		int number=25-add.length();
//		for(int i=0; i<number;i++){
//			returnString+=" ";
//		}
//		returnString+=add;
//		return returnString;
//	}
	
	public FPM_FactoryPanel returnFactoryPanel(){
		return fp;
	}
	
	public ObjectOutputStream getOOS(){
		return oos;
	}
	
	public ObjectInputStream getOIS(){
		return ois;
	}
	
	public void setupSockets(){
		try 
		{
			s = new Socket("localhost", 63432);
			oos = new ObjectOutputStream(s.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(s.getInputStream());
			System.out.println("FPM_Manager: Client Ready");	
			updateChecker = new UpdateChecker(s, ois);
			new Thread(updateChecker).start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public class UpdateChecker implements Runnable{
		private Socket mySocket;
		private ObjectInputStream ois;

		public UpdateChecker(Socket s, ObjectInputStream obis){
		    mySocket = s;
		    ois = obis;
		}
		
		public void run() {
			try{
				while(true)
				{
					// FACTORY COMMANDS //
					msgObject message=(msgObject) ois.readObject();
					String command = message.getCommand();
					if(command.equals("FPM_Kits"))
					{
						kitList = (ArrayList<GKit>) message.popObject();
						setupSwingPanel();
					} 
					else if(command.equals("UpdateKitList"))
					{
						kitList = (ArrayList<GKit>) message.popObject();
						updateList();
					} 
					else if(command.equals("Parts"))
					{
						partList = (ArrayList<GPart>) message.popObject();
					} 
					else if(command.equals("ChosenKit")) {
						chosenKit = (GKit) ois.readObject();
					} 
					else if (command.equals("FeederButtonEnable"))
					{
						Integer feeder_index = (Integer) message.popObject();
						Integer feeder_index2 = (Integer) message.popObject();
						if (laneNumbers.getSelectedIndex() == feeder_index || laneNumbers.getSelectedIndex() == feeder_index2)
						{
							doDelayFeederButton.setEnabled(true);
						}
					}
					else if (command.equals("FeederButtonDisable"))
					{
						Integer feeder_index = (Integer) message.popObject();
						Integer feeder_index2 = (Integer) message.popObject();
						if (laneNumbers.getSelectedIndex() == feeder_index || laneNumbers.getSelectedIndex() == feeder_index2)
						{
							doDelayFeederButton.setEnabled(false);
						}
					}
					// CATCH IRRELEVANT COMMANDS //
					else {
						System.out.println("FPM_Manager irrelevant command: " + command);
					}
				}
			} 
			catch(EOFException e) {
			} 
			catch(IOException e) {
				e.printStackTrace();
			} 
			catch(Exception e) {
				e.printStackTrace();
			} 
			finally 
			{
				try 
				{
					mySocket.close();
					s.close();
					//r.close();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}			
		}
	}

	public void actionPerformed(ActionEvent ae)
	{
		int kitIndex=0; // tells which kit is chosen
		if(ae.getSource()==produceButton)
		{
			boolean can_go=false;
			try
			{
				kitIndex=Integer.parseInt(amountInput.getText());
				can_go=true;
			}
			catch(NumberFormatException e)
			{
				amountInput.setText("Invalid Input");
				can_go=false;
			}
			if(can_go)
			{
				chosenKit.kitNumber = kitIndex;
				try
				{
					msgObject temp_message=new msgObject();
					temp_message.setCommand("Kit_Chosen");
					temp_message.addParameters(chosenKit);
					oos.writeObject(temp_message);
					oos.reset();

				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		else if(ae.getSource() == doDelayFeederButton)
		{
			msgObject message=new msgObject();
			message.setCommand("Feeder_DelayFeeder");
			message.addParameters(selectedFeeder);
			try
			{
				oos.writeObject(message);
				oos.flush();
				oos.reset();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else if(ae.getSource()==doDelayDiverterButton)
		{
			msgObject message=new msgObject();
			message.setCommand("Diverter_DelayDiverter");
			message.addParameters(selectedFeeder);
			
			try
			{
				oos.writeObject(message);
				oos.flush();
				oos.reset();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else if(ae.getSource() == doJamLaneButton)
		{
			if(!fp.getLaneGraphicsPanel().getSystem().getLanes().get(selectedLane).ifLaneEmpty())
			{
				msgObject message=new msgObject();
				message.setCommand("Lane_JamLane");
				message.addParameters(selectedLane);
				try
				{
					oos.writeObject(message);
					oos.flush();
					oos.reset();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
//		else if(ae.getSource() == doUnjamLaneButton)
//		{
//			msgObject message=new msgObject();
//			message.setCommand("Lane_UnjamLane");
//			message.addParameters(selectedLane);
//			try
//			{
//				oos.writeObject(message);
//				oos.flush();
//				oos.reset();
//			}
//			catch(Exception e)
//			{
//				e.printStackTrace();
//			}
//		}
//		else if(ae.getSource()==doPurgeLane)
//		{
//			msgObject message=new msgObject();
//			message.setCommand("Lane_PurgeLane");
//			message.addParameters(selectedLane);
//			try
//			{
//				oos.writeObject(message);
//				oos.flush();
//				oos.reset();
//			}
//			catch(Exception e)
//			{
//				e.printStackTrace();
//			}
//		}
		else if(ae.getSource() == doMakeBadKit) {
			try {
				msgObject temp = new msgObject();
				temp.setCommand("Factory_DoMakeBadKit");
				System.out.println("FPM_Manager : doMakeBadKit clicked!!!");
				oos.writeObject(temp);
				oos.reset();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if(ae.getSource()==kitDropDownList)
		{
			if(kitDropDownList.getSelectedIndex()==0)
			{
				amountInput.setText("You have to select a kit first");
				produceButton.setEnabled(false);
			}
			else if(kitDropDownList.getSelectedIndex()!=-1)
			{
				amountInput.setText("");
				chosenKit=kitList.get(kitDropDownList.getSelectedIndex()-1);
				produceButton.setEnabled(true);
			}
			else 
			{
				System.out.println("FPM_Manager: The Combobox is rouge");
			}
		}
		else if(ae.getSource()==laneNumbers)
		{
			selectedLane=laneNumbers.getSelectedIndex();
			selectedFeeder=selectedLane/2;
//			if(fp.getLaneGraphicsPanel().getSystem().getFeeders().get(selectedFeeder).ifFeederOccupied())
//				doDelayDiverterButton.setEnabled(true);
//			else
//				doDelayDiverterButton.setEnabled(false);
//			System.out.println("Selected feeder is: " + selectedFeeder);
		}
		else if(ae.getSource() == doMakeABadPart)
		{			
			try {
//			System.out.println("FPM_Manager: from gui : Factory_DoMakeBadParts");
			msgObject temp = new msgObject();
			temp.setCommand("Factory_DoMakeBadParts");
			temp.addParameters(selectedFeeder);
			oos.writeObject(temp);
			oos.reset();
			
			} catch (IOException e) {
			e.printStackTrace();
			}
		}
		else if(ae.getSource()==Mayhem)
		{
			fp.startRotating();
		}
	}	
}
