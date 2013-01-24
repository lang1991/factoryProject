package NonAgent;

import java.io.Serializable;

import gui.GPart;

public class Part implements Serializable{
	 public String partType; 
	 public enum myState {good, bad}; 
	 public myState state; 
	 public Part(Part p){
		 this.partType=p.partType;
		 this.state=p.state;
	 }
	 public Part(){
		 
	 }
	 public Part(String string, boolean partBad) {
		partType=string;
		if(partBad)
			state = myState.bad;
		else
			state = myState.good;
	}
	 
	 public Part (String string){
		 partType = string;
	 }
	GPart gui;
	
	public boolean isBad()
	{
		return state == myState.bad;
	}
	
	public boolean isGood()
	{
		return state == myState.good;
	}
	 
}

