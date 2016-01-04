package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2016 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class StdPortal extends StdContainer implements Rideable, Exit
{
	@Override public String ID(){    return "StdPortal";}
	public StdPortal()
	{
		super();
		setName("a portal");
		setDisplayText("a portal is here.");
		setDescription("It's difficult to see where it leads.  Try ENTER and find out!");
		basePhyStats().setWeight(10000);
		recoverPhyStats();
		capacity=10000;
		material=RawMaterial.RESOURCE_NOTHING;
	}


	// common item/mob stuff
	@Override public boolean isMobileRideBasis(){return false;}
	@Override public int rideBasis(){return Rideable.RIDEABLE_ENTERIN;}
	@Override public void setRideBasis(int basis){}
	@Override public int riderCapacity(){ return 1;}
	@Override public void setRiderCapacity(int newCapacity){}
	@Override public int numRiders(){return 0;}
	@Override public Iterator<Rider> riders(){return EmptyIterator.INSTANCE;}
	@Override public Rider fetchRider(int which){return null;}
	@Override public void addRider(Rider mob){}
	@Override public void delRider(Rider mob){}
	@Override public void recoverPhyStats(){CMLib.flags().setReadable(this,false); super.recoverPhyStats();}

	@Override public Set<MOB> getRideBuddies(Set<MOB> list){return list;}

	@Override public boolean mobileRideBasis(){return false;}
	@Override public String stateString(Rider R){    return "in";}
	@Override public String putString(Rider R){ return "in";}
	@Override public String mountString(int commandType, Rider R){ return "enter(s)";}
	@Override public String dismountString(Rider R){	return "emerge(s) from";}
	@Override public String stateStringSubject(Rider R){return "occupied by";    }
	@Override public short exitUsage(short change){ return 0;}
	@Override public String displayText(){return displayText;}
	@Override public boolean amRiding(Rider mob){ return false;}
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
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
					if(getDestinationRoom()==null)
					{
						msg.source().tell(L("This portal is broken.. nowhere to go!"));
						return false;
					}
					if(hasADoor()&&(!isOpen()))
					{
						msg.source().tell(L("@x1 is closed.",name()));
						return false;
					}
					msg.modify(msg.source(),msg.target(),msg.tool(),
							   msg.sourceMajor()|CMMsg.TYP_ENTER,msg.sourceMessage(),
							   msg.targetMajor()|CMMsg.TYP_ENTER,msg.targetMessage(),
							   msg.othersMajor()|CMMsg.TYP_ENTER,null);
					return true;
				}
				msg.source().tell(L("You cannot sit on @x1.",name()));
				return false;
			}
			break;
		case CMMsg.TYP_SLEEP:
			if(msg.amITarget(this))
			{
				msg.source().tell(L("You cannot sleep on @x1.",name()));
				return false;
			}
			break;
		case CMMsg.TYP_MOUNT:
			if(msg.amITarget(this))
			{
				msg.source().tell(L("You cannot mount @x1, try Enter.",name()));
				return false;
			}
			break;
		}
		return true;
	}

	protected Room getDestinationRoom()
	{
		Room R=null;
		final List<String> V=CMParms.parseSemicolons(readableText(),true);
		if(V.size()>0)
			R=CMLib.map().getRoom(V.get(CMLib.dice().roll(1,V.size(),-1)));
		return R;
	}

	@Override public Room lastRoomUsedFrom() { return getDestinationRoom(); }

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
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
					final Room thisRoom=msg.source().location();
					Room R=getDestinationRoom();
					if(R==null)
						R=thisRoom;
					final Exit E=CMClass.getExit("OpenNameable");
					E.setMiscText(name());
					synchronized(("GATE_"+CMLib.map().getExtendedTwinRoomIDs(thisRoom,R)).intern())
					{
						final Exit oldE=thisRoom.getRawExit(Directions.GATE);
						final Room oldR=thisRoom.rawDoors()[Directions.GATE];
						final Exit oldE2=R.getRawExit(Directions.GATE);
						thisRoom.rawDoors()[Directions.GATE]=R;
						thisRoom.setRawExit(Directions.GATE,E);
						final Exit E2=CMClass.getExit("OpenNameable");
						E2.basePhyStats().setDisposition(PhyStats.IS_NOT_SEEN);
						R.setRawExit(Directions.GATE,E2);
						CMLib.tracking().walk(msg.source(),Directions.GATE,false,false,false);
						thisRoom.rawDoors()[Directions.GATE]=oldR;
						thisRoom.setRawExit(Directions.GATE,oldE);
						R.setRawExit(Directions.GATE,oldE2);
						E.destroy();
						E2.destroy();
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

	@Override public boolean hasADoor(){return super.hasADoor();}
	@Override public boolean defaultsLocked(){return super.hasALock();}
	@Override public boolean defaultsClosed(){return super.hasADoor();}
	@Override
	public void setDoorsNLocks(boolean hasADoor,
							   boolean isOpen,
							   boolean defaultsClosed,
							   boolean hasALock,
							   boolean isLocked,
							   boolean defaultsLocked)
	{ super.setDoorsNLocks(hasADoor,isOpen,defaultsClosed,hasALock,isLocked,defaultsLocked);}

	@Override public boolean isReadable(){return false;}
	@Override public void setReadable(boolean isTrue){}

	private static final StringBuilder empty=new StringBuilder("");

	@Override
	public StringBuilder viewableText(MOB mob, Room myRoom)
	{
		final List<String> V=CMParms.parseSemicolons(readableText(),true);
		Room room=myRoom;
		if(V.size()>0)
			room=CMLib.map().getRoom(V.get(CMLib.dice().roll(1,V.size(),-1)));
		if(room==null)
			return empty;
		final StringBuilder Say=new StringBuilder("");
		if(mob.isAttributeSet(MOB.Attrib.SYSOPMSGS))
		{
			Say.append("^H("+CMLib.map().getExtendedRoomID(room)+")^? "+room.displayText(mob)+CMLib.flags().getDispositionBlurbs(room,mob)+" ");
			Say.append("via ^H("+ID()+")^? "+(isOpen()?name():closedText()));
		}
		else
		if(( CMLib.flags().canBeSeenBy(this,mob)|| (isOpen()&&hasADoor()))
		&&(CMLib.flags().isSeeable(this)))
			if(isOpen())
			{
				if(!CMLib.flags().canBeSeenBy(room,mob))
					Say.append("darkness");
				else
					Say.append(name()+CMLib.flags().getDispositionBlurbs(this,mob));
			}
			else
			if((CMLib.flags().canBeSeenBy(this,mob))&&(closedText().trim().length()>0))
				Say.append(closedText()+CMLib.flags().getDispositionBlurbs(this,mob));
		return Say;
	}

	protected String doorName="";
	@Override public String doorName(){return doorName;}
	protected String closedText="";
	@Override public String closedText(){return closedText;}

	@Override public String closeWord(){return "close";}
	@Override public String openWord(){return "open";}
	@Override
	public void setExitParams(String newDoorName,
							  String newCloseWord,
							  String newOpenWord,
							  String newClosedText)
							  {
		doorName=newDoorName;
		closedText=newClosedText;
	}


	@Override public int openDelayTicks(){return 0;}
	@Override public void setOpenDelayTicks(int numTicks){}
	@Override public String temporaryDoorLink(){return "";}
	@Override public void setTemporaryDoorLink(String link){}
}
