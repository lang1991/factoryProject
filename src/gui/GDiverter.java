package gui;

import java.awt.Graphics;

public class GDiverter extends GAnimationObject {
  int animationType;
  int aCounter;
  static String[] pics;
  
  public GDiverter(int seed, String[] images)
  {
    super(0,0,images);
    
    animationType = seed;
  }
  
  public void paint(Graphics g, int newX, int newY)
  {
    // Update animation
    if (aCounter++ > 50) {
      aCounter = 0;
      if (++currentImage > 1)
        currentImage = 0;
    }
    
    x = newX;
    y = newY;
    super.paintObject(g, 0, 0);
  }
}
