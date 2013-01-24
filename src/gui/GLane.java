package gui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class GLane extends GObject implements ActionListener
{ 
	private ArrayList<GPart> parts_on_lane;	//all the parts on the lane
	private ArrayList<GPart> parts_in_diverter;	//the parts in the diverter that has not been into the lane
	private GNest nest;	
	private ImageIcon laneImage=new ImageIcon("src/resources/lane_belt.png");
	private ImageIcon nestImage=new ImageIcon("src/resources/nest.png");
	private ImageIcon nest_purgeImage=new ImageIcon("src/resources/nest_purge.png");
	private int speed;	//the speed of the lane
	private GFeeder feeder;	
	private ObjectOutputStream oos;	//the output stream so that the server would be informed "part fed"
	private Timer dump_interval;	//the time interval for part entering the lane
	private int lane_index;	//the lane index
  boolean purging;  //true while the lane is being purged
  int purgeNestAnimationCounter;  //control animation of nest
  boolean paintAltNestFrame;  //decide which frame of the animation to paint
  GALane conveyor;
	
	public GLane(int x,int y, ObjectOutputStream fromServer, int index)
	{
		super(x, y, "src/resources/lane_belt.png");
		lane_index=index;
		oos=fromServer;
		dump_interval=new Timer(1000,this); //this helps move the parts
		parts_in_diverter=new ArrayList<GPart>();
		parts_on_lane=new ArrayList<GPart>();
		speed=2;

		nest=new GNest(x-nestImage.getIconWidth(),y,this); //a nest is created with every GUILane
		purging = false;
		purgeNestAnimationCounter = 0;
		
		// Set up the conveyor
		conveyor = new GALane(x,y, index);
	}
		
	public ArrayList<GPart> getParts(){return parts_on_lane;}
	
	public ArrayList<GPart> getPartsInDiverter(){return parts_in_diverter;}
	
	public int getSpeed(){return speed;}
	
	public void setSpeed(int new_speed){speed=new_speed;}
	
	public Timer getTimer(){return dump_interval;}
	
	public void turn_off(){
	  speed=0;
	  conveyor.off();
	}
	
	public void turn_on(){
	  speed=2;
	  conveyor.on();
	}
	
	public void flash()
	{
	  conveyor.flash();
	}
	
	//paint everything in the lane
	public void paintLane(Graphics g, int offset_X, int offset_Y)
	{
		g.drawImage(this.getImage(),this.getX()+offset_X,this.getY()+offset_Y,null);
		
		conveyor.paint(g,offset_X,offset_Y);
		
		//Determine nest paint based on the purging state
		if (!nest.ifPurging())
		  nest.paintObject(g, offset_X,offset_Y);
		else  //if purging use the counter
		{
			g.drawImage(nest_purgeImage.getImage(),nest.getX()+offset_X,nest.getY()+offset_Y,null);
		}
		
		for(int i=0;i<parts_on_lane.size();i++)
		{
			parts_on_lane.get(i).paintObject(g, offset_X,offset_Y);
		}
	}
	
	public Image getImage(){return laneImage.getImage();}
  
  public int getImageHeight(){return laneImage.getIconHeight();}
  
  public int getImageWidth(){return laneImage.getIconWidth();}
	
	public GNest get_nest()
	{
		return nest;
	}
	
	//update the position of all the parts on the lane
	public void update_parts()
	{
		nest.update_parts();

			for (int i = 0; i < this.getParts().size(); i++)
			{
				if (this.getParts().get(i).getX() < this.getX()
						&& !nest.isFull())
				{
					nest.addPart(getParts().get(i));
					parts_on_lane.remove(i); //the part has gone into the nest
				} else if (nest.isFull()
						&& this.getParts().get(i).getX() <= this.getX() + i
								* this.getParts().get(i).getIconWidth())
				{
					this.getParts().get(i).setCanMove(false);
				}
			}
			for (int i = 0; i < this.getParts().size(); i++)
			{
				this.getParts().get(i).moveOnLane(speed);
			}
	}
	
	public boolean ifLaneClean()
	{
		return (nest.ifEmpty()&&parts_on_lane.size()==0&&!feeder.ifFeederOccupied());
	}
	
	public boolean ifLaneEmpty()
	{
		return parts_on_lane.size()==0;
	}
	
	//give the part when one is taken from the nest
	public synchronized GPart take_part()
	{
		GPart temp=nest.pushParts();

		for (int i = 0; i < this.getParts().size(); i++)
		{
			this.getParts().get(i).setCanMove(true);
		}
		
		return temp;
	}
	
	//remove all the parts in the diverter
	public void clearDiverterParts()
	{
		parts_in_diverter.clear();
	}
	
	//purge the nest and remove all the parts in it
	public void purgeNest()
	{
	  purging = true;
		nest.purgeNest();

			for (int i = 0; i < this.getParts().size(); i++)
			{
				this.getParts().get(i).setCanMove(true);
			}
		
	}
	
	//inform the server that a part is fed
	public msgObject DidFeedPart() 
	{
		msgObject temp=new msgObject();
		temp.setCommand("Nest_PartFed");
		temp.addParameters(new Integer(lane_index));

		return temp;
	}
	
	//feed the lane with specific typeof part to a specific feeder with a specific number
	public void feed_lane(String part_type,int number, GFeeder gf)
	{
		for(int i=0;i<number;i++)
		{
			parts_in_diverter.add(new GPart(this.getX()+laneImage.getIconWidth()-15,this.getY()+laneImage.getIconHeight()/2-10,part_type));
			parts_in_diverter.get(parts_in_diverter.size()-1).setCanMove(true);
		}
		feeder=gf;
		dump_interval.start();
	}
	
	public int getLaneIndex()
	{
		return lane_index;
	}
	
	
	public void jam_lane()
	{
		speed=0;
		turn_off();
	}
	
	public void unjam_lane()
	{
		speed=2;
		conveyor.unjam();
		turn_on();
	}
	
	public ObjectOutputStream getOos()
	{
		return this.oos;
	}
	
	public GFeeder getFeeder()
	{
		return feeder;
	}

	public void actionPerformed(ActionEvent ae)
	{
//		if(speed!=0)
//		{
			if(parts_in_diverter.size()!=0)
			{
				parts_on_lane.add(parts_in_diverter.remove(0));
				feeder.decrement_parts();
			}
//		}
	}
}
