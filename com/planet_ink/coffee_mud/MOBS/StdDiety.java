package com.planet_ink.coffee_mud.MOBS;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class StdDiety extends StdMOB implements Diety
{
	protected int xpwrath=100;
	protected String clericReqs="";
	protected String worshipReqs="";
	
	public StdDiety()
	{
		super();
		Username="a Mighty Diety";
		setDescription("He looks benevolent enough..");
		setDisplayText("A Mighty Diety stands here!");
		baseEnvStats().setWeight(700);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new StdDiety();
	}
	
	public String getClericRequirements(){return clericReqs;}
	public void setClericRequirements(String reqs){clericReqs=reqs;}
	public String getWorshipRequirements(){return worshipReqs;}
	public void setWorshipRequirements(String reqs){worshipReqs=reqs;}
	
	public void destroy()
	{
		super.destroy();
		CMMap.delDiety(this);
	}
	public void bringToLife(Room newLocation, boolean resetStats)
	{
		super.bringToLife(newLocation,resetStats);
		CMMap.addDiety(this);
	}
	
	public boolean okAffect(Affect msg)
	{
		if(!super.okAffect(msg)) 
			return false;
		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case Affect.TYP_SERVE:
			if(msg.source().getMyDiety()==this)
			{
				msg.source().tell("You already worship "+name()+".");
				return false;
			}
			if(msg.source().getMyDiety()!=null)
			{
				msg.source().tell("You already worship "+msg.source().getMyDiety().name()+".");
				return false;
			}
			if(msg.source().charStats().getCurrentClass().baseClass().equalsIgnoreCase("Cleric"))
			{
				if(!ExternalPlay.zapperCheck(getClericRequirements(),msg.source()))
				{
					msg.source().tell("You are unworthy of serving "+name()+".");
					return false;
				}
			}
			else
			if(!ExternalPlay.zapperCheck(getWorshipRequirements(),msg.source()))
			{
				msg.source().tell("You are unworthy of "+name()+".");
				return false;
			}
			break;
		case Affect.TYP_REBUKE:
			if(!msg.source().getWorshipCharID().equals(name()))
			{
				msg.source().tell("You do not worship "+name()+".");
				return false;
			}
			break;
		}
		return true;
	}
	
	public void affect(Affect msg)
	{
		super.affect(msg);
		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case Affect.TYP_SERVE:
			msg.source().setWorshipCharID(name());
			break;
		case Affect.TYP_REBUKE:
			msg.source().setWorshipCharID("");
			msg.source().tell(name()+" takes "+xpwrath+" of experience from you.");
			msg.source().charStats().getCurrentClass().loseExperience(msg.source(),xpwrath);
			break;
		}
	}
	
}
