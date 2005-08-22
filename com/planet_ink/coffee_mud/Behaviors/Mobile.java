package com.planet_ink.coffee_mud.Behaviors;

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
public class Mobile extends ActiveTicker
{
	public String ID(){return "Mobile";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	public long flags(){return Behavior.FLAG_MOBILITY;}
	protected boolean wander=false;
	protected boolean dooropen=false;
	protected int leash=0;
	protected Hashtable leashHash=null;
	protected Vector restrictedLocales=null;
    protected long tickStatus=Tickable.STATUS_NOT;
    public long getTickStatus(){return tickStatus;}

	public Mobile()
	{
		super();
		minTicks=10; maxTicks=30; chance=100;
		leash=0;
		wander=false;
		dooropen=false;
		restrictedLocales=null;
		tickReset();
	}


	public boolean okRoomForMe(Room currentRoom, Room newRoom)
	{
		if(currentRoom==null) return false;
		if(newRoom==null) return false;
		if(leash>0)
		{
			if(leashHash==null)	leashHash=new Hashtable();
			Integer DISTNOW=(Integer)leashHash.get(currentRoom);
			Integer DISTLATER=(Integer)leashHash.get(newRoom);
			if(DISTNOW==null)
			{
				DISTNOW=new Integer(0);
				leashHash.put(currentRoom,DISTNOW);
			}
			if(DISTLATER==null)
			{
				DISTLATER=new Integer(DISTNOW.intValue()+1);
				leashHash.put(newRoom,DISTLATER);
			}
			if(DISTLATER.intValue()>(DISTNOW.intValue()+1))
			{
				DISTLATER=new Integer(DISTNOW.intValue()+1);
				leashHash.remove(newRoom);
				leashHash.put(newRoom,DISTLATER);
			}
			if(DISTLATER.intValue()>leash)
				return false;
		}
		if(restrictedLocales==null) return true;
		return !restrictedLocales.contains(new Integer(newRoom.domainType()));
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		wander=false;
		dooropen=false;
		leash=0;
		leashHash=null;
		restrictedLocales=null;
		leash=Util.getParmInt(newParms,"LEASH",0);
		Vector V=Util.parse(newParms);
		for(int v=0;v<V.size();v++)
		{
			String s=(String)V.elementAt(v);
			if(s.equalsIgnoreCase("WANDER"))
				wander=true;
			else
			if(s.equalsIgnoreCase("OPENDOORS"))
				dooropen=true;
			else
			if((s.startsWith("+")||(s.startsWith("-")))&&(s.length()>1))
			{
				if(restrictedLocales==null)
					restrictedLocales=new Vector();
				if(s.equalsIgnoreCase("+ALL"))
					restrictedLocales.clear();
				else
				if(s.equalsIgnoreCase("-ALL"))
				{
					restrictedLocales.clear();
					for(int i=0;i<Room.indoorDomainDescs.length;i++)
						restrictedLocales.addElement(new Integer(Room.INDOORS+i));
					for(int i=0;i<Room.outdoorDomainDescs.length;i++)
						restrictedLocales.addElement(new Integer(i));
				}
				else
				{
					char c=s.charAt(0);
					s=s.substring(1).toUpperCase().trim();
					int code=-1;
					for(int i=0;i<Room.indoorDomainDescs.length;i++)
						if(Room.indoorDomainDescs[i].startsWith(s))
							code=Room.INDOORS+i;
					if(code>=0)
					{
						if((c=='+')&&(restrictedLocales.contains(new Integer(code))))
							restrictedLocales.removeElement(new Integer(code));
						else
						if((c=='-')&&(!restrictedLocales.contains(new Integer(code))))
							restrictedLocales.addElement(new Integer(code));
					}
					code=-1;
					for(int i=0;i<Room.outdoorDomainDescs.length;i++)
						if(Room.outdoorDomainDescs[i].startsWith(s))
							code=i;
					if(code>=0)
					{
						if((c=='+')&&(restrictedLocales.contains(new Integer(code))))
							restrictedLocales.removeElement(new Integer(code));
						else
						if((c=='-')&&(!restrictedLocales.contains(new Integer(code))))
							restrictedLocales.addElement(new Integer(code));
					}

				}
			}
		}
		if((restrictedLocales!=null)&&(restrictedLocales.size()==0))
			restrictedLocales=null;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
        tickStatus=Tickable.STATUS_MISC2+0;
		super.tick(ticking,tickID);
        tickStatus=Tickable.STATUS_MISC2+1;
		if((canAct(ticking,tickID))
		&&(ticking instanceof MOB)
		&&(!((MOB)ticking).isInCombat())
		&&(!CMSecurity.isDisabled("MOBILITY")))
		{
            tickStatus=Tickable.STATUS_MISC2+2;
			Vector objections=null;
			MOB mob=(MOB)ticking;
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			{
				Room R=mob.location().getRoomInDir(d);
				if((R!=null)&&(!okRoomForMe(mob.location(),R)))
				{
					if(objections==null) objections=new Vector();
					objections.addElement(R);
				}
			}
            tickStatus=Tickable.STATUS_MISC2+3;
            Ability A=null;
            for(int i=0;i<mob.numEffects();i++)
            {
                A=mob.fetchEffect(i);
                if((A.canBeUninvoked())
                &&(!A.isAutoInvoked())
                &&((A.classificationCode()&Ability.ALL_CODES)==Ability.COMMON_SKILL)
                &&(Dice.roll(1,10,0)>1))
                {
                    tickDown=0;
                    return true;
                }
            }
            tickStatus=Tickable.STATUS_MISC2+4;
			Room oldRoom=mob.location();
			MUDTracker.beMobile((MOB)ticking,dooropen,wander,false,objections!=null,objections);
            tickStatus=Tickable.STATUS_MISC2+5;
			if(mob.location()==oldRoom)
				tickDown=0;
		}
        tickStatus=Tickable.STATUS_NOT;
		return true;
	}
}
