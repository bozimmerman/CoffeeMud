package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Thief_Assassinate extends ThiefSkill
{
	public String ID() { return "Thief_Assassinate"; }
	public String name(){ return "Assassinate";}
	private String displayText="(Tracking)";
	public String displayText(){ return displayText;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.OK_OTHERS;}
	private static final String[] triggerStrings = {"ASSASSINATE"};
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public String[] triggerStrings(){return triggerStrings;}
	public long flags(){return Ability.FLAG_TRACKING;}
	private Vector theTrail=null;
	public int nextDirection=-2;
	protected MOB tracking=null;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==MudHost.TICK_MOB)
		{
			if(nextDirection==-999)
				return true;

			if((theTrail==null)
			||(affected == null)
			||(!(affected instanceof MOB)))
				return false;

			MOB mob=(MOB)affected;


			if(mob.location()==null) return false;
			if(mob.location().isInhabitant(tracking))
			{
				if(Sense.isHidden(mob))
				{
					Ability A=mob.fetchAbility("Thief_BackStab");
					if(A!=null)
						A.invoke(mob,tracking,false,0);
				}
				else
					MUDFight.postAttack(mob,tracking,mob.fetchWieldedItem());
				return false;
			}

			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			{
				Room nextRoom=mob.location().getRoomInDir(d);
				Exit nextExit=mob.location().getExitInDir(d);
				if((nextRoom!=null)
				   &&(nextExit!=null)
				   &&(nextExit.isOpen())
				   &&(nextRoom.isInhabitant(tracking)))
				{
					nextDirection=d; break;
				}
			}

			if(nextDirection==999)
			{
				mob.tell("The trail seems to pause here.");
				nextDirection=-2;
				unInvoke();
			}
			else
			if(nextDirection==-1)
			{
				mob.tell("The trail dries up here.");
				nextDirection=-999;
				unInvoke();
			}
			else
			if(nextDirection>=0)
			{
				mob.tell("The trail seems to continue "+Directions.getDirectionName(nextDirection)+".");
				if(mob.isMonster())
				{
					Room nextRoom=mob.location().getRoomInDir(nextDirection);
					if((nextRoom!=null)&&(nextRoom.getArea()==mob.location().getArea()))
					{
						if(!nextRoom.isInhabitant(tracking))
						{
							Ability A=mob.fetchAbility("Thief_Sneak");
							if(A!=null)
							{
								int dir=nextDirection;
								nextDirection=-2;
								Vector V=new Vector();
								V.addElement(Directions.getDirectionName(dir));
								A.invoke(mob,V,null,false,0);
							}
							else
							{
								int dir=nextDirection;
								nextDirection=-2;
								MUDTracker.move(mob,dir,false,false);
							}
						}
						else
						{
							int dir=nextDirection;
							nextDirection=-2;
							MUDTracker.move(mob,dir,false,false);
						}
					}
					else
						unInvoke();
				}
				else
					nextDirection=-2;
			}

		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.amITarget(mob.location()))
		&&(Sense.canBeSeenBy(mob.location(),mob))
		&&(msg.targetMinor()==CMMsg.TYP_EXAMINESOMETHING))
			nextDirection=MUDTracker.trackNextDirectionFromHere(theTrail,mob.location(),true);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!Sense.aliveAwakeMobile(mob,false))
			return false;

		if(!Sense.canBeSeenBy(mob.location(),mob))
		{
			mob.tell("You can't see anything to track!");
			return false;
		}

		Vector V=Sense.flaggedAffects(mob,Ability.FLAG_TRACKING);
		for(int v=0;v<V.size();v++)	((Ability)V.elementAt(v)).unInvoke();
		if(V.size()>0)
		{
			mob.tell("You stop tracking.");
			if(commands.size()==0) return true;
		}

		theTrail=null;
		nextDirection=-2;

		tracking=null;
		String mobName="";
		if((mob.fetchEffect("Thief_Mark")!=null)&&(!mob.isMonster()))
		{
			Thief_Mark A=(Thief_Mark)mob.fetchEffect("Thief_Mark");
			if(A!=null) tracking=A.mark;
			if(tracking==null)
			{
				mob.tell("You'll need to Mark someone first.");
				return false;
			}
		}
		else
		{
			if(givenTarget!=null)
				mobName=givenTarget.name();
			else
				mobName=Util.combine(commands,0);
			if(mobName.length()==0)
			{
				mob.tell("Assassinate whom?");
				return false;
			}
			MOB M=mob.location().fetchInhabitant(mobName);
			if(M!=null)
			{
				MUDFight.postAttack(mob,M,mob.fetchWieldedItem());
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		boolean success=profficiencyCheck(mob,0,auto);

		Vector rooms=new Vector();
		if(tracking!=null)
		{
		    try
		    {
				for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if((Sense.canAccess(mob,R))&&(R.isInhabitant(tracking)))
					{
						rooms.addElement(R);
						break;
					}
				}
		    }catch(NoSuchElementException nse){}
		}
		else
		if(mobName.length()>0)
		{
		    try
		    {
				for(Enumeration r=mob.location().getArea().getProperMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(R.fetchInhabitant(mobName)!=null)
						rooms.addElement(R);
				}
		    }catch(NoSuchElementException nse){}

			if(rooms.size()<=0)
			{
			    try
			    {
					for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
					{
						Room R=(Room)r.nextElement();
						if((Sense.canAccess(mob,R))&&(R.fetchInhabitant(mobName)!=null))
							rooms.addElement(R);
					}
			    }catch(NoSuchElementException nse){}
			}
		}

		if(rooms.size()>0)
			theTrail=MUDTracker.findBastardTheBestWay(mob.location(),rooms,true,false,true,true,50);

		if((tracking==null)&&(theTrail!=null)&&(theTrail.size()>0))
			tracking=((Room)theTrail.firstElement()).fetchInhabitant(mobName);

		if((success)&&(theTrail!=null)&&(tracking!=null))
		{
			theTrail.addElement(mob.location());

			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,tracking,this,CMMsg.MSG_THIEF_ACT,mob.isMonster()?null:"<S-NAME> begin(s) to track <T-NAMESELF> for assassination.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
			if((mob.location().okMessage(mob,msg))&&(tracking.okMessage(tracking,msg)))
			{
				mob.location().send(mob,msg);
				tracking.executeMsg(tracking,msg);
				invoker=mob;
				displayText="(tracking "+tracking.name()+")";
				Thief_Assassinate newOne=(Thief_Assassinate)this.copyOf();
				if(mob.fetchEffect(newOne.ID())==null)
					mob.addEffect(newOne);
				mob.recoverEnvStats();
				newOne.nextDirection=MUDTracker.trackNextDirectionFromHere(theTrail,mob.location(),true);
			}
		}
		else
			return beneficialVisualFizzle(mob,tracking,"<S-NAME> attempt(s) to track <T-NAMESELF> for assassination, but fail(s).");


		// return whether it worked
		return success;
	}
}
