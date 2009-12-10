package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;


/*
   Copyright 2000-2010 Bo Zimmerman

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
public class Prayer_Position extends Prayer
{
	public String ID() { return "Prayer_Position"; }
	public String name(){ return "Position";}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;}
	public Room lastPosition=null;

	protected int getRoomDirection(Room R, Room toRoom, Vector ignore)
	{
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			if((R.getRoomInDir(d)==toRoom)
			&&(R!=toRoom)
			&&(!ignore.contains(R)))
				return d;
		return -1;
	}
	public String trailTo(Room R1, Room R2)
	{
		Vector set=new Vector();
		TrackingLibrary.TrackingFlags flags;
		flags = new TrackingLibrary.TrackingFlags()
				.add(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS);
		CMLib.tracking().getRadiantRooms(R1,set,flags,R2,Integer.MAX_VALUE,null);
		int foundAt=-1;
		for(int i=0;i<set.size();i++)
		{
			Room R=(Room)set.elementAt(i);
			if(R==R2){ foundAt=i; break;}
		}
		if(foundAt<0) return "You can't get to '"+R2.roomID()+"' from here.";
		Room checkR=R2;
		Vector trailV=new Vector();
		trailV.addElement(R2);
		boolean didSomething=false;
		while(checkR!=R1)
		{
			didSomething=false;
			for(int r=0;r<foundAt;r++)
			{
				Room R=(Room)set.elementAt(r);
				if(getRoomDirection(R,checkR,trailV)>=0)
				{
					trailV.addElement(R);
					foundAt=r;
					checkR=R;
					didSomething=true;
					break;
				}
			}
			if(!didSomething)
				return "No trail was found?!";
		}
		Vector theDirTrail=new Vector();
		Vector empty=new Vector();
		for(int s=trailV.size()-1;s>=1;s--)
		{
			Room R=(Room)trailV.elementAt(s);
			Room RA=(Room)trailV.elementAt(s-1);
			theDirTrail.addElement(Character.toString(Directions.getDirectionName(getRoomDirection(R,RA,empty)).charAt(0))+" ");
		}
		StringBuffer theTrail=new StringBuffer("");
		char lastDir='\0';
		int lastNum=0;
		while(theDirTrail.size()>0)
		{
			String s=(String)theDirTrail.elementAt(0);
			if(lastNum==0)
			{
				lastDir=s.charAt(0);
				lastNum=1;
			}
			else
			if(s.charAt(0)==lastDir)
				lastNum++;
			else
			{
				if(lastNum==1)
					theTrail.append(Character.toString(lastDir)+" ");
				else
					theTrail.append(Integer.toString(lastNum)+Character.toString(lastDir)+" ");
				lastDir=s.charAt(0);
				lastNum=1;
			}
			theDirTrail.removeElementAt(0);
		}
		if(lastNum==1)
			theTrail.append(Character.toString(lastDir));
		else
		if(lastNum>0)
			theTrail.append(Integer.toString(lastNum)+Character.toString(lastDir));
		return theTrail.toString();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(lastPosition==null) lastPosition=mob.getStartRoom();
		if(lastPosition==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for a position check.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.tell("The trail from "+lastPosition.name()+" to here is: "+trailTo(lastPosition,mob.location()));
				lastPosition=mob.location();
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for a position check, but fail(s).");

		return success;
	}
}
