package gui;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;

import NonAgent.Part;


public class GFeeder extends GObject
{
	private ImageIcon feederIndicator_ON = new ImageIcon("src/resources/feederLight_ON.png");
	private ImageIcon feederIndicator_LOW = new ImageIcon("src/resources/feederLight_LOW.png");
	private ImageIcon feederIndicator_OFF = new ImageIcon("src/resources/feederLight_OFF.png");
	private ImageIcon feederDiverter = new ImageIcon("src/resources/feederDiverter.png");
	private List<GPart> parts_in_feeder;
	private GPartBin myBin;
	private int feeder_size;
	GDiverter diverter;

	
	public GFeeder(int x, int y, int laneNum)
	{
		super(x,y,"src/resources/feeder.png");
		parts_in_feeder = new ArrayList<GPart>();
		feeder_size = 0;
		myBin=null;
		initDiverter(laneNum);
	}
	
	void initDiverter(int seed)
	{
	  String[] pics = new String[2];
    
    // Load the 2 images based on the seed
    if (seed == 0) {
      pics[0] = "src/resources/Diverter/diverter00.png";
      pics[1] = "src/resources/Diverter/diverter01.png";
    } else if (seed == 1) {
      pics[0] = "src/resources/Diverter/diverter30.png";
      pics[1] = "src/resources/Diverter/diverter31.png";
    } else if (seed == 2) {
      pics[0] = "src/resources/Diverter/diverter10.png";
      pics[1] = "src/resources/Diverter/diverter11.png";
    } else if (seed == 3) {
      pics[0] = "src/resources/Diverter/diverter20.png";
      pics[1] = "src/resources/Diverter/diverter21.png";
    }
    
	  diverter = new GDiverter(seed,pics);
	}
		
	public void decrement_parts(){
		feeder_size--;
	}
	
	public void set_parts(int number_of_parts){ //this is simulating adding parts to the bin
		feeder_size = number_of_parts;
	}
	
	public int get_parts()
	{
		return feeder_size;
	}
	
	//receive a bin from the robot
	public void receiveBin(GPartBin myBin)
	{
		System.out.println("bin receiverd");
		this.myBin = myBin;
		feeder_size=10;
	}
	
	//check if there is a bin in the feeder
	public boolean ifFeederOccupied()
	{
		return !(myBin==null);
	}
	
	public GPartBin dropBin()
	{
		GPartBin temp = myBin;
		myBin=null;
		feeder_size=0;
		System.out.println("dropBin called");
		return temp;
	}
	
	public void purgeBin()
	{
		feeder_size=0;
		System.out.println("purgeBin called");
	}
	
	//indicate if the part is low or the feeder is empty or the part is still plenty
	public void paintFeeder(Graphics g,int offset_X, int offset_Y)
	{
		this.paintObject(g, offset_X,offset_Y);
		if(myBin != null)
		{
			myBin.setX(this.getX());
			myBin.setY(this.getY()+this.getIconHeight()/4);
			myBin.paintObject(g, offset_X,offset_Y);
//			System.out.println("Bin painted");
//			System.exit(0);
		}
//		else if(myBin == null)
//		{
//			System.out.println("NULL bin in feeder");
//		}
		if(feeder_size>=5){
			g.drawImage(feederIndicator_ON.getImage(), this.getX()+5+offset_X,  this.getY()+68+offset_Y, null);
		}
		if(feeder_size<5&&feeder_size>0){
			g.drawImage(feederIndicator_LOW.getImage(), this.getX()+5+offset_X,  this.getY()+68+offset_Y, null);
		}
		if(feeder_size == 0){
//			dropBin();
			g.drawImage(feederIndicator_OFF.getImage(), this.getX()+5+offset_X,  this.getY()+68+offset_Y, null);
		}
		
	}
	
	//paint the direction of the feeder
	public void paintButton(Graphics g, boolean top_lane,int offset_X,int offset_Y){
		if(top_lane){
		  diverter.paint(g, this.getX() + 7+offset_X,  this.getY() + 20+offset_Y);
			//g.drawImage(feederDiverter.getImage(), this.getX() + 7+offset_X,  this.getY() + 20+offset_Y, null);
		}
		else{
		  diverter.paint(g, this.getX() + 5+offset_X,  this.getY() + 95+offset_Y);
			//g.drawImage(feederDiverter.getImage(), this.getX() + 5+offset_X,  this.getY() + 95+offset_Y, null);
		}
	}
}
