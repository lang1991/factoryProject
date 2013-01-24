package gui;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class GLaneSystemManager extends JFrame implements ActionListener, ChangeListener
{
	private int offset_X;	//offset to shift the position of the animation
	private int offset_Y;
	private GLaneSystemGraphicsPanel graphicsPanel;
	private JPanel controlPanel;
	//Current unit number
	private int Unit_Number=0;
		
	//frame rate control parameters
	private final int MIN_FRAME_RATE=30;
	private final int MAX_FRAME_RATE=300;
	private final int INITIAL_FRAME_RATE=30;
		
	//all the components in the button panel
	private JLabel frameLabel;
	private JSlider frameRateSlider;
	private JLabel laneUnitLabel;
	private JComboBox unitCombox;
	private String[] comboBoxOptions={"Unit#1","Unit#2","Unit#3","Unit#4"};
	
	private JButton delayDiverter;
	private JButton delayFeeder;
	private JButton jamLane1;
	private JButton jamLane2;

	//////////CLIENT NETWORK VARIABLES //////////
	private Socket s;
	//private Socket r;
	private UpdateChecker updateChecker;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	
	public GLaneSystemManager(int offset_X, int offset_Y, boolean in_lane_manager)
	{
		this.offset_X=offset_X;
		this.offset_Y=offset_Y;
		try 
		{
			s = new Socket("localhost", 63432);
			System.out.println( "s connected");
			oos = new ObjectOutputStream(s.getOutputStream());
			System.out.println( "oos created");
			ois = new ObjectInputStream(s.getInputStream());
			System.out.println("Client Ready");	
			updateChecker = new UpdateChecker(s, ois);
			new Thread(updateChecker).start();		
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(in_lane_manager)
			graphicsPanel = new GLaneSystemGraphicsPanel(this, offset_X, offset_Y);
		else
			graphicsPanel = new GLaneSystemGraphicsPanel(null, offset_X, offset_Y);
		
		setLayout(new BoxLayout(this.getContentPane(),BoxLayout.X_AXIS));
		
		validate();
		controlPanel=new JPanel();
		controlPanel.setMaximumSize(new Dimension(200,800));
		controlPanel.setMinimumSize(new Dimension(200,800));
		controlPanel.setLayout(new GridBagLayout());
		GridBagConstraints p = new GridBagConstraints();		p.fill = GridBagConstraints.HORIZONTAL;
		p.gridx = 0;
		p.gridy = 0;
		frameLabel=new JLabel("Frame Rate");
		controlPanel.add(frameLabel,p);
		p.gridx = 0;
		p.gridy = 1;
		frameRateSlider=new JSlider(JSlider.HORIZONTAL,MIN_FRAME_RATE,MAX_FRAME_RATE,INITIAL_FRAME_RATE);
		controlPanel.add(frameRateSlider,p);
		p.gridx = 0;
		p.gridy = 2;
		laneUnitLabel=new JLabel("Lane Unit Number");
		controlPanel.add(laneUnitLabel,p);
		unitCombox=new JComboBox(comboBoxOptions);
		unitCombox.setLocation(100,0);
		unitCombox.setMaximumSize(new Dimension(200,50));
		unitCombox.setMinimumSize(new Dimension(200,50));
		p.gridx = 0;
		p.gridy = 3;
		controlPanel.add(unitCombox,p);
		p.gridx = 0;
		p.gridy = 4;
		JLabel nonNormativeLabel = new JLabel ("Non-normatives: ");
		controlPanel.add(nonNormativeLabel,p);
		p.gridx = 0;
		p.gridy = 5;
		delayDiverter=new JButton("Delay Diverter");
		controlPanel.add(delayDiverter,p);
		p.gridx = 0;
		p.gridy = 6;
		delayFeeder=new JButton("Delay Feeder");
		delayFeeder.setEnabled(false);
		controlPanel.add(delayFeeder,p);
		p.gridx = 0;
		p.gridy = 7;
		jamLane1=new JButton("Jam Lane #1");
		controlPanel.add(jamLane1,p);
		p.gridx = 0;
		p.gridy = 8;
		jamLane2=new JButton("Jam Lane #2");
		controlPanel.add(jamLane2,p);
		
		initialize();
		
		
		
		//add everything to the swing panel
		add(graphicsPanel);
		add(controlPanel);	
				
	}
	
	public void initialize()
	{
		//Determine the layout and add listeners to the buttons
		unitCombox.addActionListener(this);
		delayDiverter.addActionListener(this);
//		delayDiverter.setEnabled(false);
		delayFeeder.addActionListener(this);
		jamLane1.addActionListener(this);
		jamLane2.addActionListener(this);
		jamLane1.addActionListener(this);
		jamLane2.addActionListener(this);

		//set up the JSlider
		//frameRateSlider.setMajorTickSpacing(90);
		//frameRateSlider.setPaintTicks(true);
		//frameRateSlider.setPaintLabels(true);
		//frameRateSlider.addChangeListener(this);
		Font font = new Font("Serif", Font.BOLD, 18);
		frameRateSlider.setFont(font);
		frameRateSlider.addChangeListener(this);

		
		//add everything to the button panel
		//gbc.fill=GridBagConstraints.HORIZONTAL;
//		controlPanel.add(frameLabel);
//		controlPanel.add(frameRateSlider);
//		controlPanel.add(laneUnitLabel);
//		controlPanel.add(unitCombox);
//		controlPanel.add(releaseBinToRobot);
//		controlPanel.add(placeBin);
//		controlPanel.add(runLane1);
//		controlPanel.add(runLane2);
//		controlPanel.add(purgeLane1);
//		controlPanel.add(purgeLane2);
//		controlPanel.add(takePic1);
//		controlPanel.add(takePic2);
//		controlPanel.add(givePart1);
//		controlPanel.add(givePart2);
//		controlPanel.add(jamLane1);
//		controlPanel.add(jamLane2);
//		controlPanel.add(unJamLane1);
//		controlPanel.add(unJamLane2);
	}
	
	public ObjectOutputStream get_oos()
	{
		return oos;
	}
	
public static void main(String[] args){
		
	int offset_X=0;
	int offset_Y=0;
		GLaneSystemManager window = new GLaneSystemManager(offset_X, offset_Y,false);
		window.setSize(800, 800);
		window.setTitle("GLaneSystemManager");
		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}
	
	
	
	//the inner class that handles the communication
	class UpdateChecker implements Runnable
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
			try
			{
				while (true)
				{
					msgObject message = (msgObject) ois.readObject();
					System.out
							.println("GLaneSystemManager: Received Message: "
									+ message.getCommand());
					String command = message.getCommand();
//						System.err.println("Got the Message");
					//checks what command is sent from the server
					if (command.equals("GantryRobot_ReleaseBinToGantry"))
					{
						Integer feeder_index = (Integer) message
								.popObject();
						graphicsPanel.getSystem()
								.DoPurgeToBin(feeder_index);

						msgObject temp = new msgObject();
						temp.setCommand("GantryRobot_ReleaseBinToGantry_Done");
						oos.writeObject(temp);
						oos.reset();
					} 
					else if (command.equals("Feeder_PlaceBin"))
					{
						Integer feeder_index = (Integer) message
								.popObject();
						graphicsPanel.getSystem().DoPlaceBin(feeder_index);

						msgObject temp = new msgObject();
						temp.setCommand("Feeder_PlaceBin_Done");
						oos.writeObject(temp);
						oos.reset();
					}
					else if (command.equals("Lane_RunLane"))
					{
						Integer lane_index = (Integer) message.popObject();
						String part_type = (String) message.popObject();
						Integer num = (Integer) message.popObject();
						System.out.println("Part Type " + part_type);
						if (lane_index.intValue() % 2 == 0)
							graphicsPanel.DoDiverter(
									lane_index.intValue() / 2, true);
						else
							graphicsPanel.DoDiverter(
									lane_index.intValue() / 2, false);
						graphicsPanel.feedLane(lane_index.intValue(),
								part_type, num.intValue());
					} 
					/*
					else if(command.equals("Feeder_TotalFeed"))
					{
						Integer lane_index=(Integer) message.popObject();
						Integer number_to_feed=(Integer) message.popObject();
						graphicsPanel.getSystem().getFeeders().get(lane_index.intValue()/2).set_parts(number_to_feed.intValue());
						
					}
					*/
					else if (command.equals("Lane_PurgeLane"))
					{
						Integer lane_index = (Integer) message.popObject();
						graphicsPanel.getSystem().DoPurgeToBin_WithoutRemoval(lane_index.intValue());

						
					} 
					else if (command.equals("Camera_Shoot"))
					{
						Integer nest_index = (Integer) message.popObject();
						int remainder = nest_index % 2;
						if (remainder == 0)
						{
							graphicsPanel.getSystem().getLanes()
									.get(nest_index.intValue()).get_nest()
									.takePicture();
							graphicsPanel.getSystem().getLanes()
									.get(nest_index.intValue() + 1)
									.get_nest().takePicture();
						} 
						else
						{
							graphicsPanel.getSystem().getLanes()
									.get(nest_index.intValue()).get_nest()
									.takePicture();
							graphicsPanel.getSystem().getLanes()
									.get(nest_index.intValue() - 1)
									.get_nest().takePicture();
						}
					} 
					else if (command.equals("PartRobot_PickUpParts_Done"))
					{
						Integer nest_index = (Integer) message.popObject();
						graphicsPanel.getSystem().DoTakePart(nest_index);
//							msgObject temp = new msgObject();
//							temp.setCommand("PartRobot_AnimationDone2");
//							System.out
//									.println("Before The Lane Manager sends the animation_Done");
//							oos.writeObject(temp);
//							oos.reset();
//							System.out
//									.println("After The Lane Manager sends the animation_Done");
					}
					else if(command.equals("AgentInfo_JamLane"))
					{
						Integer lane_index=(Integer)message.popObject();
						graphicsPanel.getSystem().getLanes().get(lane_index.intValue()).jam_lane();
					}
					else if(command.equals("AgentInfo_UnjamLane"))
					{
						Integer lane_index=(Integer)message.popObject();
						graphicsPanel.getSystem().getLanes().get(lane_index.intValue()).unjam_lane();
					}
					else if(command.equals("AgentInfo_PurgeLane"))
					{
						Integer lane_index=(Integer) message.popObject();
						graphicsPanel.getSystem().DoPurgeToBin_WithoutRemoval(lane_index.intValue());
					}
					else if(command.equals("AgentInfo_PurgeNestOnly"))
					{
						Integer nest_index=(Integer) message.popObject();
						graphicsPanel.getSystem().getLanes().get(nest_index).get_nest().purgeNest();
						for(int i=0;i<graphicsPanel.getSystem().getLanes().get(nest_index).getParts().size();i++)
						{
							graphicsPanel.getSystem().getLanes().get(nest_index).getParts().get(i).setCanMove(true);
						}
					}
					else if (command.equals("FeederButtonEnable"))
					{
						Integer feeder_index = (Integer) message.popObject();
						Integer feeder_index2 = (Integer) message.popObject();
						if (unitCombox.getSelectedIndex() == feeder_index/2)
						{
							delayFeeder.setEnabled(true);
						}
					}
					else if (command.equals("FeederButtonDisable"))
					{
						Integer feeder_index = (Integer) message.popObject();
						Integer feeder_index2 = (Integer) message.popObject();
						if (unitCombox.getSelectedIndex() == feeder_index/2)
						{
							delayFeeder.setEnabled(false);
						}
					}
					else if(command.equals("Diverter_DelayDiverter"))
					{
						Integer diverter_index=(Integer) message.popObject();
//						graphicsPanel.getSystem().getFeeders().get(diverter_index).set_parts(graphicsPanel.getSystem().getFeeders().get(diverter_index).get_parts()+10);
					}
				}
			} 
			catch(EOFException e) 
			{
				e.printStackTrace();
			} 
			catch(IOException e) 
			{
				System.out.println("GLaneManager: this is the exception " + e.getMessage());
				e.printStackTrace();
			} 
			catch(Exception e) 
			{
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
	
	//the listener for the JSlider
	public void stateChanged(ChangeEvent ce)
	{
		JSlider source = (JSlider)ce.getSource();
		int fps=(int)source.getValue();
		graphicsPanel.frameRateChanged(fps);
	}

	//no longer in use since the agents would send the signals, not the buttons
	public void actionPerformed(ActionEvent ae)
	{
		
		if(ae.getSource()==unitCombox)
		{
			if(unitCombox.getSelectedItem().equals("Unit#1"))
			{
				Unit_Number=0;
			}
			else if(unitCombox.getSelectedItem().equals("Unit#2"))
			{
				Unit_Number=1;
			}
			else if(unitCombox.getSelectedItem().equals("Unit#3"))
			{
				Unit_Number=2;
			}
			else if(unitCombox.getSelectedItem().equals("Unit#4"))
			{
				Unit_Number=3;
			}
			
//			if(graphicsPanel.getSystem().getFeeders().get(Unit_Number).ifFeederOccupied())
//				delayDiverter.setEnabled(true);
//			else
//				delayDiverter.setEnabled(false);
		}
		else if(ae.getSource()==delayDiverter)
		{
			msgObject message=new msgObject();
			message.setCommand("Diverter_DelayDiverter");
			message.addParameters(Unit_Number);
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
		else if(ae.getSource()==delayFeeder)
		{
			msgObject message=new msgObject();
			message.setCommand("Feeder_DelayFeeder");
			message.addParameters(Unit_Number);
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
		else if(ae.getSource()==jamLane1)
		{
			if(!this.getGraphicsPanel().getSystem().getLanes().get(Unit_Number*2).ifLaneEmpty())
			{
				msgObject message=new msgObject();
				message.setCommand("Lane_JamLane");
				message.addParameters(Unit_Number*2);
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
		else if(ae.getSource()==jamLane2)
		{
			if(!this.getGraphicsPanel().getSystem().getLanes().get(Unit_Number*2+1).ifLaneEmpty())
			{
				msgObject message=new msgObject();
				message.setCommand("Lane_JamLane");
				message.addParameters(Unit_Number*2+1);
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
	}
	
	public GLaneSystemGraphicsPanel getGraphicsPanel(){
		return graphicsPanel;
	}
	
}
