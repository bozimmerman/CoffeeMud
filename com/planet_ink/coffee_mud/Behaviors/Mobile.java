package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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
		if(restrictedLocales==null) return true;
		if(leash>0)
		{
			if(leashHash==null)	leashHash=new Hashtable();
			Integer DISTNOW=(Integer)leashHash.get(currentRoom);
			Integer DISTTHEN=(Integer)leashHash.get(newRoom);
			if(DISTNOW==null)
			{
				DISTNOW=new Integer(0);
				leashHash.put(currentRoom,DISTNOW);
			}
			if(DISTTHEN==null)
			{
				DISTTHEN=new Integer(DISTNOW.intValue()+1);
				leashHash.put(newRoom,DISTNOW);
			}
			if(DISTTHEN.intValue()>(DISTNOW.intValue()+1))
			{
				DISTTHEN=new Integer(DISTNOW.intValue()+1);
				leashHash.remove(newRoom);
				leashHash.put(newRoom,DISTTHEN);
			}
			if(DISTTHEN.intValue()>leash)
				return false;
		}
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
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))
		&&(ticking instanceof MOB)
		&&(!((MOB)ticking).isInCombat())
		&&(!CMSecurity.isDisabled("MOBILITY")))
		{
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
			Room oldRoom=mob.location();
			MUDTracker.beMobile((MOB)ticking,dooropen,wander,false,objections!=null,objections);
			if(mob.location()==oldRoom)
				tickDown=0;
		}
		return true;
	}
}
