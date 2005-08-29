package com.planet_ink.coffee_mud.Items.Basic;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class StdPortal extends StdContainer implements Rideable, Exit
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
		if(!super.okMessage(myHost,msg))
			return false;
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
				msg.source().tell("You cannot sit on "+name()+".");
				return false;
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
		return true;
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
					Vector V=Util.parseSemicolons(readableText(),true);
					Room R=null;
					if(V.size()>0)
						R=CMMap.getRoom((String)V.elementAt(Dice.roll(1,V.size(),-1)));
					if(R==null) R=thisRoom;
                    Exit E=CMClass.getExit("OpenNameable");
                    E.setMiscText(name());
                    Exit oldE=thisRoom.rawExits()[Directions.GATE];
                    Room oldR=thisRoom.rawDoors()[Directions.GATE];
                    Exit oldE2=R.rawExits()[Directions.GATE];
					thisRoom.rawDoors()[Directions.GATE]=R;
					thisRoom.rawExits()[Directions.GATE]=E;
                    R.rawExits()[Directions.GATE]=E;
					MUDTracker.move(msg.source(),Directions.GATE,false,false,false);
					thisRoom.rawDoors()[Directions.GATE]=oldR;
					thisRoom.rawExits()[Directions.GATE]=oldE;
                    R.rawExits()[Directions.GATE]=oldE2;
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
	
	public boolean hasADoor(){return super.hasALid();}
	public boolean defaultsLocked(){return super.hasALock();}
	public boolean defaultsClosed(){return super.hasALid();}
	public void setDoorsNLocks(boolean hasADoor,
							   boolean isOpen,
							   boolean defaultsClosed,
							   boolean hasALock,
							   boolean isLocked,
							   boolean defaultsLocked)
	{ super.setLidsNLocks(hasADoor,isOpen,hasALock,isLocked);}
	 
	public boolean isReadable(){return false;}
	public void setReadable(boolean isTrue){}

	private static final StringBuffer empty=new StringBuffer("");
	
	public StringBuffer viewableText(MOB mob, Room myRoom)
	{
		Vector V=Util.parseSemicolons(readableText(),true);
		Room room=myRoom;
		if(V.size()>0)
		    room=CMMap.getRoom((String)V.elementAt(Dice.roll(1,V.size(),-1)));
		if(room==null) return empty;
		StringBuffer Say=new StringBuffer("");
		if(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
		{
			if(room==null)
				Say.append("^Z(null)^.^? ");
			else
				Say.append("^H("+CMMap.getExtendedRoomID(room)+")^? "+room.roomTitle()+Sense.colorCodes(room,mob)+" ");
			Say.append("via ^H("+ID()+")^? "+(isOpen()?name():closedText()));
		}
		else
		if(((Sense.canBeSeenBy(this,mob))||(isOpen()&&hasADoor()))
		&&(Sense.isSeen(this)))
			if(isOpen())
			{
				if((room!=null)&&(!Sense.canBeSeenBy(room,mob)))
					Say.append("darkness");
				else
					Say.append(name()+Sense.colorCodes(this,mob));
			}
			else
			if((Sense.canBeSeenBy(this,mob))&&(closedText().trim().length()>0))
				Say.append(closedText()+Sense.colorCodes(this,mob));
		return Say;
	}
	
	private String doorName="";
	public String doorName(){return doorName;}
	private String closedText="";
	public String closedText(){return closedText;}
	
	public String closeWord(){return "close";}
	public String openWord(){return "open";}
	public void setExitParams(String newDoorName,
							  String newCloseWord,
							  String newOpenWord,
							  String newClosedText){
	    doorName=newDoorName;
	    closedText=newClosedText;
	}

	
	public int openDelayTicks(){return 0;}
	public void setOpenDelayTicks(int numTicks){}
	public String temporaryDoorLink(){return "";}
	public void setTemporaryDoorLink(String link){}
}
