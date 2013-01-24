package gui;

import java.awt.Graphics;

public class GKitRobotPeach extends GRobot {

  static String[] pics = { 
    "src/resources/KitRobot/peach/peach0.png",
    "src/resources/KitRobot/peach/peach1.png" };
  
  /* Hold the value of the current frames passed */
  int animationFrames;
  int animationDuration;
  
  public GKitRobotPeach(int x, int y) {
    super(x, y, pics);
    
    // Initialize the animation and force the image to update
    animationFrames = 0;
    animate();
  }

  public void paint(Graphics g, int posX, int posY, int offX, int offY)
  {
    // Update position
    x = posX;
    y = posY;
    
    //Update animation
    animate();
    
    // Paint object based on offset
    super.paintObject(g, offX, offY);
  }
  
  /* Update the current image to create animation and update position */
  void animate()
  {
     //Hold each animation frame for 15 frames
    if (++animationFrames >= 30) {
      animationFrames = 0;
      
      if (currentImage == 1) {
        currentImage = 0;
        
        // Alternate Peach's side
        if (flipped) flipped = false;
        if (!flipped) flipped = true;
      }
      else currentImage = 1;
    }
  }
}
