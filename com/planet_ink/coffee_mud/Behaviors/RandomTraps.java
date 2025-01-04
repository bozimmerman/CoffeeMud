package com.planet_ink.coffee_mud.Behaviors;
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
   Copyright 2003-2025 Bo Zimmerman

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
public class RandomTraps extends ActiveTicker
{
	@Override
	public String ID()
	{
		return "RandomTraps";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_ROOMS|Behavior.CAN_AREAS;
	}

	protected List<Physical>	maintained			= new Vector<Physical>();
	protected int				minTraps			= 1;
	protected int				maxTraps			= 1;
	protected int				avgTraps			= 1;
	protected boolean			doAnyItems			= false;
	protected boolean			doAnyContainers		= false;
	protected boolean			doDooredContainers	= false;
	protected boolean			doLockedContainers	= false;
	protected boolean			doAnyDoors			= false;
	protected boolean			doAnyLockedDoors	= false;
	protected boolean			doRooms				= false;

	protected Set<Integer>	restrictedLocales	= null;
	private int				tickStatus			= Tickable.STATUS_NOT;

	@Override
	public int getTickStatus()
	{
		return tickStatus;
	}

	public RandomTraps()
	{
		super();
		tickReset();
	}

	@Override
	public String accountForYourself()
	{
		return "random trapping";
	}

	@Override
	public void setParms(final String newParms)
	{
		maintained=new Vector<Physical>();
		doAnyItems=false;
		doAnyContainers=false;
		doDooredContainers=false;
		doLockedContainers=false;
		doAnyDoors=false;
		doAnyLockedDoors=true;
		doRooms=false;
		final int x=newParms.indexOf(';');
		String oldParms=newParms;
		restrictedLocales=null;
		if(x>=0)
		{
			oldParms=newParms.substring(0,x).trim();
			String p=CMParms.getParmStr(oldParms,"ROOMS","NO").toUpperCase().trim();
			if(p.startsWith("Y"))
				doRooms=true;
			p=CMParms.getParmStr(oldParms,"ITEMS","NO").toUpperCase().trim();
			if(p.startsWith("Y"))
			{
				doAnyItems=true;
				doAnyContainers=true;
				doDooredContainers=true;
				doLockedContainers=true;
			}
			else
			if(p.startsWith("CONT"))
			{
				doAnyItems=false;
				doAnyContainers=true;
				doDooredContainers=true;
				doLockedContainers=true;
			}
			else
			if(p.startsWith("LID"))
			{
				doAnyItems=false;
				doAnyContainers=false;
				doDooredContainers=true;
				doLockedContainers=true;
			}
			else
			if(p.startsWith("LOCK"))
			{
				doAnyItems=false;
				doAnyContainers=false;
				doDooredContainers=false;
				doLockedContainers=true;
			}
			else
			if(p.startsWith("NO"))
			{
				doAnyItems=false;
				doAnyContainers=false;
				doDooredContainers=false;
				doLockedContainers=false;
			}

			p=CMParms.getParmStr(oldParms,"EXITS","LOCKED").toUpperCase().trim();
			if((p.startsWith("Y"))
			||(p.startsWith("DOOR")))
			{
				doAnyDoors=true;
				doAnyLockedDoors=true;
			}
			else
			if(p.startsWith("LOCK"))
			{
				doAnyDoors=false;
				doAnyLockedDoors=true;
			}
			else
			if(p.startsWith("NO"))
			{
				doAnyDoors=false;
				doAnyLockedDoors=false;
			}

			final Vector<String> V=CMParms.parse(oldParms);
			for(int v=0;v<V.size();v++)
			{
				String s=V.elementAt(v);
				if((s.startsWith("+")||(s.startsWith("-")))&&(s.length()>1))
				{
					if(restrictedLocales==null)
						restrictedLocales=new TreeSet<Integer>();
					if(s.equalsIgnoreCase("+ALL"))
						restrictedLocales.clear();
					else
					if(s.equalsIgnoreCase("-ALL"))
					{
						restrictedLocales.clear();
						for(int i=0;i<Room.DOMAIN_INDOORS_DESCS.length;i++)
							restrictedLocales.add(Integer.valueOf(Room.INDOORS+i));
						for(int i=0;i<Room.DOMAIN_OUTDOOR_DESCS.length;i++)
							restrictedLocales.add(Integer.valueOf(i));
					}
					else
					{
						final char c=s.charAt(0);
						s=s.substring(1).toUpperCase().trim();
						int code=-1;
						for(int i=0;i<Room.DOMAIN_INDOORS_DESCS.length;i++)
						{
							if(Room.DOMAIN_INDOORS_DESCS[i].startsWith(s))
								code=Room.INDOORS+i;
						}
						if(code>=0)
						{
							if((c=='+')&&(restrictedLocales.contains(Integer.valueOf(code))))
								restrictedLocales.remove(Integer.valueOf(code));
							else
							if((c=='-')&&(!restrictedLocales.contains(Integer.valueOf(code))))
								restrictedLocales.add(Integer.valueOf(code));
						}
						code=-1;
						for(int i=0;i<Room.DOMAIN_OUTDOOR_DESCS.length;i++)
						{
							if(Room.DOMAIN_OUTDOOR_DESCS[i].startsWith(s))
								code=i;
						}
						if(code>=0)
						{
							if((c=='+')&&(restrictedLocales.contains(Integer.valueOf(code))))
								restrictedLocales.remove(Integer.valueOf(code));
							else
							if((c=='-')&&(!restrictedLocales.contains(Integer.valueOf(code))))
								restrictedLocales.add(Integer.valueOf(code));
						}
					}
				}
			}
		}
		super.setParms(oldParms);
		minTraps=CMParms.getParmInt(oldParms,"mintraps",1);
		maxTraps=CMParms.getParmInt(oldParms,"maxtraps",1);
		if(maxTraps<minTraps)
			maxTraps=minTraps;
		avgTraps=CMLib.dice().roll(1,maxTraps-minTraps,minTraps);
		parms=newParms;
		if((restrictedLocales!=null)&&(restrictedLocales.size()==0))
			restrictedLocales=null;
	}

	protected void makeRoomEligible(final Room R, final List<Physical> eligible)
	{
		if(R==null)
			return;
		if((restrictedLocales!=null)
		&&(restrictedLocales.contains(Integer.valueOf(R.domainType()))))
			return;

		if(R instanceof GridLocale)
		{
			final List<Room> map=((GridLocale)R).getAllRooms();
			if(map.size()==0)
				eligible.add(R);
			else
			for (final Room room : ((GridLocale)R).getAllRooms())
				eligible.add(room);
		}
		else
			eligible.add(R);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		tickStatus=Tickable.STATUS_START;
		super.tick(ticking,tickID);
		tickStatus=Tickable.STATUS_MISC+0;
		for(int i=maintained.size()-1;i>=0;i--)
		{
			if(CMLib.utensils().fetchMyTrap(maintained.get(i))==null)
				maintained.remove(i);
		}
		if(maintained.size()>=maxTraps)
		{
			tickStatus=Tickable.STATUS_NOT;
			return true;
		}
		tickStatus=Tickable.STATUS_MISC+1;
		if((canAct(ticking,tickID))||(maintained.size()<minTraps))
		{
			tickStatus=Tickable.STATUS_MISC+2;
			final List<Trap> allTraps=new ArrayList<Trap>();
			if(maintained.size()<avgTraps)
			{
				for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
				{
					final Ability A=e.nextElement();
					if((A instanceof Trap)
					&&(!(A.ID().equals("StdTrap")))
					&&(!(A.ID().equals("GenTrap"))))
						allTraps.add((Trap)A);
				}
			}

			tickStatus=Tickable.STATUS_MISC+3;

			if(maintained.size()<avgTraps)
			{
				final List<Physical> eligible=new Vector<Physical>();
				if(ticking instanceof Room)
				{
					tickStatus=Tickable.STATUS_MISC+4;
					makeRoomEligible((Room)ticking,eligible);
				}
				else
				if((ticking instanceof Area)&&(((Area)ticking).metroSize()>0))
				{
					tickStatus=Tickable.STATUS_MISC+5;
					for(final Enumeration<Room> m=((Area)ticking).getMetroMap();m.hasMoreElements();)
						makeRoomEligible(m.nextElement(),eligible);
				}
				else
					return true;

				tickStatus=Tickable.STATUS_MISC+6;
				if(eligible.size()==0)
					return true;

				final int oldSize=eligible.size();
				for(int r=0;r<oldSize;r++)
				{
					tickStatus=Tickable.STATUS_MISC+7;
					Room R=null;
					try
					{
						if(eligible.get(r) instanceof Room)
							R = (Room)eligible.get(r);
					}
					catch (final IndexOutOfBoundsException e)
					{
					}
					if(R==null)
						continue;

					if((doAnyDoors)||(doAnyLockedDoors))
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						tickStatus=Tickable.STATUS_MISC+8;
						if(((R.rawDoors()[d]!=null) // important optimization
						&&(R.rawDoors()[d].roomID().length()>0)))
						{
							final Exit E=R.getExitInDir(d);
							if((R.getRoomInDir(d)!=null)
							&&(E!=null)
							&&((E.hasADoor())
							&&(!E.isOpen()))
							&&(!eligible.contains(E))
							&&((R.getReverseExit(d)==null)
							   ||(!eligible.contains(R.getReverseExit(d)))))
							{
								tickStatus=Tickable.STATUS_MISC+9;
								if(E.hasALock()&&(E.isLocked())&&(doAnyLockedDoors))
									eligible.add(E);
								else
								if(doAnyDoors)
									eligible.add(E);
							}
							tickStatus=Tickable.STATUS_MISC+10;
						}
					}

					tickStatus=Tickable.STATUS_MISC+21;
					if((doAnyItems)||(doAnyContainers)||(doDooredContainers)||(doLockedContainers))
					for(int i=0;i<R.numItems();i++)
					{
						tickStatus=Tickable.STATUS_MISC+22;
						final Item I=R.getItem(i);
						if((CMLib.flags().isGettable(I))
						&&(!eligible.contains(I))
						&&(!I.ID().endsWith("Wallpaper")))
						{
							tickStatus=Tickable.STATUS_MISC+23;
							if(I instanceof Container)
							{
								tickStatus=Tickable.STATUS_MISC+24;
								final Container C=(Container)I;
								if(C.hasADoor()&&(!C.isOpen()))
								{
									if(C.hasALock()&&C.isLocked()&&(doLockedContainers))
										eligible.add(I);
									else
									if(doDooredContainers)
										eligible.add(I);
								}
								else
								if(doAnyContainers)
									eligible.add(I);
							}
							else
							if(doAnyItems)
								eligible.add(I);
						}
						tickStatus=Tickable.STATUS_MISC+25;
					}
					tickStatus=Tickable.STATUS_MISC+26;
				}

				tickStatus=Tickable.STATUS_MISC+27;
				if(!doRooms)
				while((eligible.size()>0)&&(eligible.get(0) instanceof Room))
					eligible.remove(0);

				tickStatus=Tickable.STATUS_MISC+28;
				for(int e=eligible.size()-1;e>=0;e--)
				{
					tickStatus=Tickable.STATUS_MISC+29;
					if((maintained.contains(eligible.get(e)))
					||(CMLib.utensils().fetchMyTrap(eligible.get(e))!=null))
						eligible.remove(e);
					tickStatus=Tickable.STATUS_MISC+30;
				}

				if(eligible.size()==0)
					return true;

				tickStatus=Tickable.STATUS_MISC+31;
				final Physical P=eligible.get(CMLib.dice().roll(1,eligible.size(),-1));

				final List<Trap> eligibleTraps=new ArrayList<Trap>();
				for(int t=0;t<allTraps.size();t++)
				{
					if(allTraps.get(t).canSetTrapOn(null,P)
					&&(P.fetchEffect(allTraps.get(t).ID())==null))
						eligibleTraps.add(allTraps.get(t));
				}

				if(eligibleTraps.size()==0)
					return true;

				tickStatus=Tickable.STATUS_MISC+32;
				Trap T=eligibleTraps.get(CMLib.dice().roll(1,eligibleTraps.size(),-1));
				T=(Trap)T.copyOf();
				T.setProficiency(100);
				final Area A=CMLib.map().areaLocation(ticking);
				if(A!=null)
					T.setMiscText(":"+A.getIStat(Area.Stats.MED_ALIGNMENT)+":");
				T.makeLongLasting();
				T.setSavable(false);
				P.addEffect(T);
				maintained.add(P);
				tickStatus=Tickable.STATUS_MISC+33;
			}
		}
		tickStatus=Tickable.STATUS_NOT;
		return true;
	}
}
