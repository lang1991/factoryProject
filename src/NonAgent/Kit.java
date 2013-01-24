package NonAgent;


import java.io.Serializable;
import java.util.ArrayList;

import gui.GKit;

import java.util.List;

public class Kit implements Serializable
{

	public List<Part> parts;

	public String configAssociated;
	private GKit gui = new GKit(0,0,0,0,"FUCKER");
	private Integer onWhichStand = 0;
	public condition current_condition=condition.GOOD;
	/*
	 * onWhichStand
	 * Values and their meaning
	 * ------------------------
	 * 0 = enteringConveyor
	 * 1 = leftWorkingStand
	 * 2 = rightWorkingStand
	 * 3 = inspectionStand
	 * 4 = exitingConveyor
	 * */

	public enum KitState
	{
		EMPTY, MOVING_OUT, BEING_WORKED_ON, WAITING_FOR_INSPECTION, DONE, EXITING_CELL, BEING_INSPECTED, GETTING_FROM_CONVEYOR, NEEDS_REWORK
	}

	KitState state = KitState.EMPTY;
	
	public enum condition
	{
		GOOD,BAD;
	}
	public Kit()
	{
		parts= new ArrayList<Part>();
	}

	public Kit(String configAssociated)
	{
		
		parts= new ArrayList<Part>();
		this.configAssociated = configAssociated;
	}
	
	//make a deep copy
	//public Kit(Kit kit){
		//this.state=kit.state;
		//this.parts=kit.parts;
		//this.configAssociated=kit.configAssociated;
	//}

	public KitState getState()
	{
		return state;
	}

	public void setState(KitState state)
	{
		this.state = state;
	}

	public void add(Part part)
	{
		parts.add(part);
	}
	
	public void setGui(GKit gui) {
		this.gui = gui;
	}


	public GKit getGui()
	{
		return gui;
	}
	
	public List<Part> getParts()
	{
		return parts;
	}

	public String getConfig()
	{
		return configAssociated;
	}
	
	public void setStand(int onWhichStand)
	{
		this.onWhichStand = onWhichStand;
	}
	
	public Integer getStand()
	{
		return onWhichStand;
	}
	
	public boolean isDone()
	{
		return state == KitState.DONE;
	}
	
	public boolean needsRework()
	{
		return state == KitState.NEEDS_REWORK;
	}
	
	public boolean isWaitingForInspection()
	{
		return state == KitState.WAITING_FOR_INSPECTION;
	}
	
	public boolean isMovingOut()
	{
		return state == KitState.MOVING_OUT;
	}

	public boolean isExitingCell()
	{
		return state == KitState.EXITING_CELL;
	}
}
