package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class RandomTraps extends ActiveTicker
{
	public String ID(){return "RandomTraps";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS|Behavior.CAN_AREAS;}

	protected Vector maintained=new Vector();
	protected int minTraps=1;
	protected int maxTraps=1;
	protected boolean doAnyItems=false;
	protected boolean doAnyContainers=false;
	protected boolean doDooredContainers=false;
	protected boolean doLockedContainers=false;
	protected boolean doAnyDoors=false;
	protected boolean doAnyLockedDoors=false;
	protected boolean doRooms=false;
											 
	protected Vector restrictedLocales=null;
	
	public RandomTraps()
	{
		tickReset();
	}
	public Behavior newInstance()
	{
		return new RandomTraps();
	}
	
	public void setParms(String newParms)
	{
		doAnyItems=false;
		doAnyContainers=false;
		doDooredContainers=false;
		doLockedContainers=false;
		doAnyDoors=false;
		doAnyLockedDoors=true;
		doRooms=false;
		int x=newParms.indexOf(";");
		String oldParms=newParms;
		restrictedLocales=null;
		if(x>=0)
		{
			oldParms=newParms.substring(0,x).trim();
			String p=getParmStr(oldParms,"ROOMS","NO").toUpperCase().trim();
			if(p.startsWith("Y")) doRooms=true;
			p=getParmStr(oldParms,"ITEMS","NO").toUpperCase().trim();
			if(p.startsWith("Y")){
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
			
			p=getParmStr(oldParms,"EXITS","LOCKED").toUpperCase().trim();
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
			
			Vector V=Util.parse(oldParms);
			for(int v=0;v<V.size();v++)
			{
				String s=(String)V.elementAt(v);
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
		}
		super.setParms(oldParms);
		minTraps=getParmVal(oldParms,"mintraps",1);
		maxTraps=getParmVal(oldParms,"maxtraps",1);
		parms=newParms;
		if((restrictedLocales!=null)&&(restrictedLocales.size()==0))
			restrictedLocales=null;
	}

	private void makeRoomElligible(Room R, Vector elligible)
	{
		if(R==null) return;
		if((restrictedLocales!=null)
		&&(restrictedLocales.contains(new Integer(R.domainType()))))
		   return;
		
		if(R instanceof GridLocale)
		{
			Vector map=((GridLocale)R).getAllRooms();
			if(map.size()==0)
				elligible.addElement(((Room)R));
			else
			for(Enumeration m=((GridLocale)R).getAllRooms().elements();m.hasMoreElements();)
				elligible.addElement(m.nextElement());
		}
		else
			elligible.addElement(((Room)R));
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		for(int i=maintained.size()-1;i>=0;i--)
			if(CoffeeUtensils.fetchMyTrap((Environmental)maintained.elementAt(i))==null)
				maintained.removeElementAt(i);
		if(maintained.size()>=maxTraps)
			return true;
		if((canAct(ticking,tickID))||(maintained.size()<minTraps))
		{
			int num=minTraps;
			if(maintained.size()>=minTraps) 
				num=maintained.size()+1;
			if(num>maxTraps) num=maxTraps;
			Vector allTraps=new Vector();
			if(maintained.size()<num)
				for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
				{
					Ability A=(Ability)e.nextElement();
					if(A instanceof Trap)
						allTraps.addElement(A);
				}
			
			while(maintained.size()<num)
			{
				Vector elligible=new Vector();
				if(ticking instanceof Room)
					makeRoomElligible((Room)ticking,elligible);
				else
				if((ticking instanceof Area)&&(((Area)ticking).mapSize()>0))
				{
					for(Enumeration m=((Area)ticking).getMap();m.hasMoreElements();)
						makeRoomElligible((Room)m.nextElement(),elligible);
				}
				else
					break;
				
				if(elligible.size()==0)
					break;
				
				int oldSize=elligible.size();
				for(int r=0;r<oldSize;r++)
				{
					Room R=(Room)elligible.elementAt(r);
					if((doAnyDoors)||(doAnyLockedDoors))
					for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
					{
						Exit E=R.getExitInDir(d);
						if((R.getRoomInDir(d)!=null)
						&&(E!=null)
						&&((E.hasADoor())
						&&(!E.isOpen()))
						&&(!elligible.contains(E))
						&&((R.getReverseExit(d)==null)
						   ||(!elligible.contains(R.getReverseExit(d)))))
						{
							if(E.hasALock()&&(E.isLocked())&&(doAnyLockedDoors))
								elligible.addElement(E);
							else
							if(doAnyDoors)
								elligible.addElement(E);
						}
					}
					
					if((doAnyItems)||(doAnyContainers)||(doDooredContainers)||(doLockedContainers))
					for(int i=0;i<R.numItems();i++)
					{
						Item I=R.fetchItem(i);
						if((I.isGettable())
						&&(!elligible.contains(I))
						&&(!I.ID().endsWith("Wallpaper")))
						{
							if(I instanceof Container)
							{
								Container C=(Container)I;
								if(C.hasALid()&&(!C.isOpen()))
								{
									if(C.hasALock()&&C.isLocked()&&(doLockedContainers))
										elligible.addElement(I);
									else
									if(doDooredContainers)
										elligible.addElement(I);
								}
								else
								if(doAnyContainers)
									elligible.addElement(I);
							}
							else
							if(doAnyItems)
								elligible.addElement(I);
						}
					}
				}
				
				if(!doRooms)
				while((elligible.size()>0)&&(elligible.firstElement() instanceof Room))
					elligible.removeElementAt(0);
				
				for(int e=elligible.size()-1;e>=0;e--)
				{
					if((maintained.contains(elligible.elementAt(e)))
					||(CoffeeUtensils.fetchMyTrap((Environmental)elligible.elementAt(e))!=null))
						elligible.removeElementAt(e);
				}
				
				if(elligible.size()==0)
					break;
				
				Environmental E=(Environmental)elligible.elementAt(Dice.roll(1,elligible.size(),-1));
				
				Vector elligibleTraps=new Vector();
				for(int t=0;t<allTraps.size();t++)
					if(((Trap)allTraps.elementAt(t)).canSetTrapOn(null,E))
						elligibleTraps.addElement(allTraps.elementAt(t));
				
				if(elligibleTraps.size()==0)
					break;
				
				Trap T=(Trap)elligibleTraps.elementAt(Dice.roll(1,elligibleTraps.size(),-1));
				T=(Trap)T.copyOf();
				T.setProfficiency(100);
				T.makeLongLasting();
				T.setBorrowed(E,true);
				/*
					Room R=CoffeeUtensils.roomLocation(E);
					String rname=(R!=null)?CMMap.getExtendedRoomID(R):"";
					if((E instanceof Exit)&&(ticking instanceof Area))
					{
						for(Enumeration r=((Area)ticking).getMap();r.hasMoreElements();)
						{
							Room R2=(Room)r.nextElement();
							for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
								if(R2.rawExits()[d]==E)
								{ rname=CMMap.getExtendedRoomID(R2)+" "+Directions.getDirectionName(d); break;}
							if(rname.length()>0) break;
						}
					}
					Log.sysOut("RandomTraps",E.name()+" in "+rname+" had "+T.name()+" set.");
				*/
				E.addAffect(T);
				maintained.addElement(E);
			}
		}
		return true;
	}
}