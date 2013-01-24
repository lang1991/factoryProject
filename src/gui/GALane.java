package gui;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

public class GALane implements ActionListener 
{
  /* Coordinates of the lane */
  int x, y;
  
  /* Holds the arrows for the conveyor */
  Lane[] laneArrows;
 
  /* Arrow images */
  static String[] pics = {
    "src/resources/Lane/laneOnFlash0.png",
    "src/resources/Lane/laneOnFlash1.png",
    "src/resources/Lane/laneOnFlash2.png",
    "src/resources/Lane/laneOnFlash3.png",
    "src/resources/Lane/laneOnFlash4.png",
    "src/resources/Lane/laneStop.png" };
  
  /* Animation variables */
  boolean running;
  int aCounter;
  int flashArrow;
  int flashFrame;
  int normalSpeed=-1;
  int speed;
  int unjamSpeed=-20;
  Timer timer;

  public GALane(int posX, int posY, int flashNum)
  {
	timer=new Timer(1000,this);
    x = posX;
    y = posY;
    speed=normalSpeed;
    
    laneArrows = new Lane[5];
    
    for (int i = 0; i < laneArrows.length; i++) {
      laneArrows[i] = new Lane(x + i*50, y + 3, pics, x);
    }
    
    // Animation variable initialization
    running = true;
    aCounter = 0;
    flashFrame = 0;
    flashArrow = 0;
  }
  
  public void paint(Graphics g, int offX, int offY)
  {
    // Animate every other frame
    if(aCounter++ > 1 && running) {
      for (int i = 0; i < laneArrows.length; i++) {
        laneArrows[i].animate();
      }
      aCounter = 0;
    }
    
    for (int i = 0; i < laneArrows.length; i++) {
      laneArrows[i].paint(g,offX,offY);
    }
  }
  
  public void on()
  {
    running = true;
    for (int i = 0; i < laneArrows.length; i++) {
      laneArrows[i].start();
    }
  }
  
  public void off()
  {
    running = false;
    for (int i = 0; i < laneArrows.length; i++) {
      laneArrows[i].stop();
    }
  }
  
  public void flash()
  {
    for (int i = 0; i < laneArrows.length; i++)
    {
      if (running)
        laneArrows[i].flash();
    }
  }
  
  public void unjam()
  {
	  speed=unjamSpeed;
	  timer.restart();
  }
  
  class Lane extends GAnimationObject {
    /* Animation variables */
    int flashCounter;
    int leftLaneBoundary;
    
    public Lane(int posX, int posY, String[] imageAddresses, int startX) {
      super(posX, posY, imageAddresses);      
      flashCounter = 0;
      leftLaneBoundary = startX;
    }
    
    public void paint(Graphics g, int offX, int offY)
    {
      super.paintObject(g, offX, offY);
    }
    
    public void animate()
    {
      if (super.getX() < (leftLaneBoundary - 50)) {
        super.setX(leftLaneBoundary + 200);
      }
      super.moveX(speed);
    }
    
    public void start()
    {
      currentImage = 0;
    }
    
    public void flash()
    {
      if (++currentImage >= 5) currentImage = 0;
    }
    
    public void stop()
    {
      currentImage = 5;
    }
    
  }

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		speed=normalSpeed;
		for (int i = 0; i < laneArrows.length; i++) {
		      laneArrows[i] = new Lane(x + i*50, y + 3, pics, x);
		    }
		timer.stop();
	}
}
