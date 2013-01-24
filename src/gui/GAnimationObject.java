package gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class GAnimationObject {
  /* Coordinates of object */
  int x, y;
  
  /* Images used for animation and the index of the current image */
  BufferedImage[] images;
  int currentImage;
  
  private GAnimationObject() { };
  
  public GAnimationObject(int posX, int posY, String[] imageAddresses )
  {
    x = posX;
    y = posY;
    
    // Initialize the array to the number of images
    images = new BufferedImage[imageAddresses.length];
    
    // Load each image into the images array
    for (int i = 0; i < images.length; i++)
    {
      try {
        images[i] = ImageIO.read(new File(imageAddresses[i]));
      } catch (IOException e) {
        System.out.println("Image (" + imageAddresses[i] + ") does not exist");
        e.printStackTrace();
      }
    }
    currentImage = 0;
  }
  
  public void paintObject(Graphics g, int offsetX, int offsetY)
  {
    Graphics2D g2 = (Graphics2D)g;
    
    // Paint the current image
    g2.drawImage(images[currentImage],null,x+offsetX,y+offsetY);
  }
  
  public void setX(int newX)
  {
    x = newX;
  }
  
  public int getX()
  {
    return x;
  }
  
  public void moveX(int valX)
  {
    x += valX;
  }
}
