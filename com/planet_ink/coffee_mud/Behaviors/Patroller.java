package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Patroller extends ActiveTicker
{
	public String ID(){return "Patroller";}
	protected int canImproveCode(){return Behavior.CAN_MOBS|Behavior.CAN_ITEMS;}
	public long flags(){return Behavior.FLAG_MOBILITY;}

	private int step=0;
	private boolean rideOk=false;

	public Patroller()
	{
		super();
		minTicks=5; maxTicks=10; chance=100;
		rideOk=false;
		tickReset();
	}
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		rideOk=Util.getParmStr(newParms,"rideok","false").equalsIgnoreCase("true");
	}


	private Vector getSteps()
	{
		Vector V=new Vector();
		String path=getParms().trim();
		int x=path.indexOf(";");
		if(x<0) return V;
		path=path.substring(x+1).trim();
		x=path.indexOf(";");
		String s=null;
		while(x>=0)
		{
			s=path.substring(0,x).trim();
			if(s.length()>0) V.addElement(s);
			path=path.substring(x+1).trim();
			x=path.indexOf(";");
		}
		if(path.length()>0)
			V.addElement(path);
		if(V.size()>1)
			for(int i=V.size()-1;i>=0;i--)
			{
				s=(String)V.elementAt(i);
				if(s.equalsIgnoreCase("RESTART")) break;
				int dir=Directions.getGoodDirectionCode(s);
				if(dir>=0)
					V.addElement(Directions.getDirectionName(Directions.getOpDirectionCode(dir)));
				else
				if(i<(V.size()-1))
					V.addElement(V.elementAt(i));
			}
		return V;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(canAct(ticking,tickID))
		{
			Room thisRoom=null;
			if(ticking instanceof MOB) thisRoom=((MOB)ticking).location();
			else
			if((ticking instanceof Item)
			&&(((Item)ticking).owner() instanceof Room)
			&&(!((Item)ticking).amDestroyed()))
				thisRoom=(Room)((Item)ticking).owner();
			if(thisRoom instanceof GridLocale)
			{
				Vector V=((GridLocale)thisRoom).getAllRooms();
				Room R=(Room)(V.elementAt(Dice.roll(1,V.size(),-1)));
				if(R!=null) 
				{
					if(ticking instanceof Item)
						R.bringItemHere((Item)ticking,-1);
					else
					if(ticking instanceof MOB)
						R.bringMobHere((MOB)ticking,true);
				}
				thisRoom=R;
			}
			if(thisRoom==null) return true;
			
			Room thatRoom=null;
			Vector steps=getSteps();
			if(steps.size()==0) return true;
			if((step<0)||(step>=steps.size())) step=0;
			String nxt=(String)steps.elementAt(step);

			if((nxt.equalsIgnoreCase("RESTART"))&&(step>0))
			{
				step=0;
				nxt=(String)steps.elementAt(step);
			}

			if(nxt.equalsIgnoreCase("."))
			{
				step++;
				return true;
			}

			int direction=Directions.getGoodDirectionCode(nxt);
			if(direction<0)
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Room R=thisRoom.getRoomInDir(d);
					if((R!=null)&&(R.roomID().toUpperCase().endsWith(nxt.toUpperCase())))
					{
						thatRoom=R;
						direction=d;
						break;
					}
				}
			else
				thatRoom=thisRoom.getRoomInDir(direction);

			if((direction<0)||(thatRoom==null))
				return true;
			Exit E=thisRoom.getExitInDir(direction);
			if(E==null) return true;
			
			for(int m=0;m<thisRoom.numInhabitants();m++)
			{
				MOB inhab=thisRoom.fetchInhabitant(m);
				if((inhab!=null)
				&&(CMSecurity.isAllowed(inhab,thisRoom,"CMDMOBS")
				   ||CMSecurity.isAllowed(inhab,thisRoom,"CMDROOMS")))
					return false;
			}
			if(!rideOk)
			{
				if(((ticking instanceof Rideable)&&(((Rideable)ticking).numRiders()>0))
				||((ticking instanceof MOB)&&(((MOB)ticking).amFollowing()!=null)&&(((MOB)ticking).location()==((MOB)ticking).amFollowing().location())))
					return true;
			}
			
			if(ticking instanceof Item)
			{
				Item I=(Item)ticking;
				
				thisRoom.showHappens(CMMsg.MSG_OK_ACTION,I,"<S-NAME> goes "+Directions.getDirectionName(direction)+".");
				thatRoom.bringItemHere(I,-1);
				if(I.owner()==thatRoom)
				{
					step++;
					thatRoom.showHappens(CMMsg.MSG_OK_ACTION,I,"<S-NAME> arrives from "+Directions.getFromDirectionName(Directions.getOpDirectionCode(direction))+".");
				}
				else
				if(I.owner()==thisRoom)
					tickDown=0;
			}
			else
			if(ticking instanceof MOB)
			{
				// ridden things dont wander!
				MOB mob=(MOB)ticking;

				// handle doors!
				if(E.hasADoor()&&(!E.isOpen()))
				{
					if((E.hasALock())&&(E.isLocked()))
					{
						FullMsg msg=new FullMsg(mob,E,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
						if(mob.location().okMessage(mob,msg))
						{
							msg=new FullMsg(mob,E,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_UNLOCK,CMMsg.MSG_OK_VISUAL,"<S-NAME> unlock(s) <T-NAMESELF>.");
							CoffeeUtensils.roomAffectFully(msg,thisRoom,direction);
						}
					}
					FullMsg msg=new FullMsg(mob,E,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
					if(mob.location().okMessage(mob,msg))
					{
						msg=new FullMsg(mob,E,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OPEN,CMMsg.MSG_OK_VISUAL,"<S-NAME> "+E.openWord()+"(s) <T-NAMESELF>.");
						CoffeeUtensils.roomAffectFully(msg,thisRoom,direction);
					}
				}
				if(!E.isOpen()) return true;

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
					CharState oldState=mob.curState().cloneCharState();
					A.invoke(mob,V,null,false);
					mob.curState().setMana(oldState.getMana());
					mob.curState().setMovement(oldState.getMovement());
				}
				else
					MUDTracker.move(mob,direction,false,false);

				if(mob.location()==thatRoom)
					step++;
				else
				if(mob.location()==thisRoom)
					tickDown=0;
			}
		}
		return true;
	}
}
