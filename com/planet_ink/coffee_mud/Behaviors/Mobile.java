package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Mobile extends ActiveTicker
{
	public String ID(){return "Mobile";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	public boolean grantsMobility(){return true;}
	protected boolean wander=false;
	protected Vector restrictedLocales=null;
	
	public Mobile()
	{
		super();
		minTicks=10; maxTicks=30; chance=100;
		wander=false;
		restrictedLocales=null;
		tickReset();
	}
	public Behavior newInstance()
	{
		return new Mobile();
	}
	
	public boolean okRoomForMe(Room currentRoom, Room newRoom)
	{
		if(currentRoom==null) return false;
		if(newRoom==null) return false;
		if((!wander)&&(!currentRoom.getArea().name().equals(newRoom.getArea().name())))
		   return false;
		if(restrictedLocales==null) return true;
		return !restrictedLocales.contains(new Integer(newRoom.domainType()));
	}
	
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		wander=false;
		restrictedLocales=null;
		Vector V=Util.parse(newParms);
		for(int v=0;v<V.size();v++)
		{
			String s=(String)V.elementAt(v);
			if(s.equalsIgnoreCase("WANDER"))
				wander=true;
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
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			// ridden things dont wander!
			MOB mob=(MOB)ticking;
			if(((mob instanceof Rideable)&&(((Rideable)mob).numRiders()>0))
			||((mob.amFollowing()!=null)&&(mob.location()==mob.amFollowing().location())))
				return true;
			
			Room thisRoom=mob.location();
			if(thisRoom instanceof GridLocale)
			{
				Vector V=((GridLocale)thisRoom).getAllRooms();
				Room R=(Room)(V.elementAt(Dice.roll(1,V.size(),-1)));
				if(R!=null) R.bringMobHere(mob,true);
				thisRoom=mob.location();
			}

			int tries=0;
			int direction=-1;
			while((tries++<10)&&(direction<0))
			{
				direction=(int)Math.round(Math.floor(Math.random()*6));
				Room otherRoom=thisRoom.getRoomInDir(direction);
				Exit otherExit=thisRoom.getExitInDir(direction);
				if((otherRoom!=null)&&(otherExit!=null))
				{
					Exit opExit=otherRoom.getExitInDir(Directions.getOpDirectionCode(direction));
					for(int a=0;a<otherExit.numAffects();a++)
					{
						Ability aff=otherExit.fetchAffect(a);
						if((aff!=null)&&(aff instanceof Trap))
							direction=-1;
					}

					if(opExit!=null)
					{
						for(int a=0;a<opExit.numAffects();a++)
						{
							Ability aff=opExit.fetchAffect(a);
							if((aff!=null)&&(aff instanceof Trap))
								direction=-1;
						}
					}

					if(!okRoomForMe(thisRoom,otherRoom))
						direction=-1;
					else
						break;
				}
				else
					direction=-1;
			}

			if(direction<0)
				return true;

			boolean move=true;
			for(int m=0;m<thisRoom.numInhabitants();m++)
			{
				MOB inhab=thisRoom.fetchInhabitant(m);
				if((inhab!=null)&&(inhab.isASysOp(thisRoom)))
					move=false;
			}
			if(move)
			{
				Ability A=mob.fetchAbility("Thief_Sneak");
				if(A!=null)
				{
					Vector V=new Vector();
					V.add(Directions.getDirectionName(direction));
					if(A.profficiency()<50)
					{
						A.setProfficiency(Dice.roll(1,50,A.adjustedLevel(mob)*15));
						Ability A2=mob.fetchAbility("Thief_Hide");
						if(A2!=null)
							A2.setProfficiency(Dice.roll(1,50,A.adjustedLevel(mob)*15));
					}
					int oldMana=mob.curState().getMana();
					A.invoke(mob,V,null,false);
					mob.curState().setMana(oldMana);
				}
				else
					ExternalPlay.move(mob,direction,false,false);

				if(mob.location()==thisRoom)
					tickDown=0;
			}
		}
		return true;
	}
}
