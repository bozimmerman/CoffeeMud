package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdPortal extends StdContainer implements Rideable
{
	public String ID(){	return "StdPortal";}
	public StdPortal()
	{
		super();
		setName("a portal");
		setDisplayText("a portal is here.");
		setDescription("It's difficult to see where it leads.  Try ENTER and find out!");
		baseEnvStats().setWeight(10000);
		recoverEnvStats();
		capacity=10000;
		material=EnvResource.RESOURCE_NOTHING;
	}
	public Environmental newInstance()
	{
		return new StdPortal();
	}

	// common item/mob stuff
	public int rideBasis(){return Rideable.RIDEABLE_ENTERIN;}
	public void setRideBasis(int basis){}
	public int riderCapacity(){ return 1;}
	public void setRiderCapacity(int newCapacity){}
	public int numRiders(){return 0;}
	public Rider fetchRider(int which){return null;}
	public void addRider(Rider mob){}
	public void delRider(Rider mob){}
	public void recoverEnvStats(){Sense.setReadable(this,false); super.recoverEnvStats();}

	public HashSet getRideBuddies(HashSet list){return list;}

	public boolean mobileRideBasis(){return false;}
	public String stateString(Rider R){	return "in";}
	public String putString(Rider R){ return "in";}
	public String mountString(int commandType, Rider R){ return "enter(s)";}
	public String dismountString(Rider R){	return "emerge(s) from";}
	public String stateStringSubject(Rider R){return "occupied by";	}

	public String displayText(){return displayText;}
	public boolean amRiding(Rider mob){ return false;}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_DISMOUNT:
			if(msg.amITarget(this))
			{
				// protects from standard item rejection
				return true;
			}
			break;
		case CMMsg.TYP_SIT:
			if(msg.amITarget(this))
			{
				if(msg.sourceMessage().indexOf(mountString(CMMsg.TYP_SIT,msg.source()))>0)
				{
					if((msg.source().location().rawDoors()[Directions.GATE]!=null)
					||(msg.source().location().rawExits()[Directions.GATE]!=null))
					{
						msg.source().tell("There is already another portal here.");
						return false;
					}
					Vector V=Util.parseSemicolons(readableText(),true);
					if(V.size()==0)
					{
						msg.source().tell("This portal is broken.. nowhere to go!");
						return false;
					}
					if(hasALid()&&(!isOpen()))
					{
						msg.source().tell(name()+" is closed.");
						return false;
					}
					msg.modify(msg.source(),msg.target(),msg.tool(),
							   msg.sourceMajor()|CMMsg.TYP_ENTER,msg.sourceMessage(),
							   msg.targetMajor()|CMMsg.TYP_ENTER,msg.targetMessage(),
							   msg.othersMajor()|CMMsg.TYP_ENTER,msg.othersMessage());
					return true;
				}
				else
				{
					msg.source().tell("You cannot sit on "+name()+".");
					return false;
				}
			}
			break;
		case CMMsg.TYP_SLEEP:
			if(msg.amITarget(this))
			{
				msg.source().tell("You cannot sleep on "+name()+".");
				return false;
			}
			break;
		case CMMsg.TYP_MOUNT:
			if(msg.amITarget(this))
			{
				msg.source().tell("You cannot mount "+name()+", try Enter.");
				return false;
			}
			break;
		}
		return super.okMessage(myHost,msg);
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_DISMOUNT:
			break;
		case CMMsg.TYP_ENTER:
			if(msg.amITarget(this))
			{
				if(msg.sourceMessage().indexOf(mountString(CMMsg.TYP_SIT,msg.source()))>0)
				{
					Room thisRoom=msg.source().location();
					if((thisRoom.rawDoors()[Directions.GATE]==null)
					   &&(thisRoom.rawExits()[Directions.GATE]==null))
					{
						Vector V=Util.parseSemicolons(readableText(),true);
						Room R=null;
						if(V.size()>0)
							R=CMMap.getRoom((String)V.elementAt(Dice.roll(1,V.size(),-1)));
						if(R==null) R=thisRoom;
						thisRoom.rawDoors()[Directions.GATE]=R;
						thisRoom.rawExits()[Directions.GATE]=CMClass.getExit("Open");
						MUDTracker.move(msg.source(),Directions.GATE,false,false,false);
						thisRoom.rawDoors()[Directions.GATE]=null;
						thisRoom.rawExits()[Directions.GATE]=null;
					}
				}
			}
			break;
		case CMMsg.TYP_LEAVE:
		case CMMsg.TYP_FLEE:
		case CMMsg.TYP_SLEEP:
		case CMMsg.TYP_MOUNT:
		case CMMsg.TYP_SIT:
			break;
		}
	}
}
