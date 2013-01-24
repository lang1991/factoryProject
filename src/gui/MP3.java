
package gui;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import javazoom.jl.player.Player;


public class MP3 
{
    private String filename;
    private Player player; 

    //construct a MP3 object that contains the name of the song to play
    public MP3(String filename) 
    {
        this.filename = filename;
        play();
    }
    
    //close(end) the play of the song
    public void close() { if (player != null) player.close(); }

    //play the song
    public void play() 
    {
        try 
        {
          FileInputStream fis = new FileInputStream(this.filename);
          BufferedInputStream bis = new BufferedInputStream(fis);
     
          this.player = new Player(bis);
        } catch (Exception e) {
            System.err.printf("%sn", e.getMessage());
        }
     
        try {
          player.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
          
      }
    
    //loop the song
    //static function because it should be able to call itself
    public static void playLooping() 
    {
    	new Thread()
    	{
    		public void run()
    		{
    			MP3 mp3 = new MP3("src/resources/backGround_Music.mp3");
    		     
    	        mp3.play();
    	     
    	        while (true) {
    	          if (mp3.player.isComplete()) {
    	            mp3.close();
    	            mp3.play();
    	          }
    		}
    	}
        }.start();
      }
    }

