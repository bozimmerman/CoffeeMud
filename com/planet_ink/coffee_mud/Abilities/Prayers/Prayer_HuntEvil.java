package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Prayer_HuntEvil extends Prayer
{
	public String ID() { return "Prayer_HuntEvil"; }
	public String name(){ return "Hunt Evil";}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_TRACKING;}
	public String displayText(){return "(Hunting Evil)";}
	protected String word(){return "evil";}

	private Vector theTrail=null;
	public int nextDirection=-2;

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

			if(nextDirection==999)
			{
				mob.tell("The hunt seems to pause here.");
				nextDirection=-2;
				unInvoke();
			}
			else
			if(nextDirection==-1)
			{
				mob.tell("The hunt dries up here.");
				nextDirection=-999;
				unInvoke();
			}
			else
			if(nextDirection>=0)
			{
				mob.tell("The hunt seems to continue "+Directions.getDirectionName(nextDirection)+".");
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
			nextDirection=MUDTracker.trackNextDirectionFromHere(theTrail,mob.location(),false);
	}

	protected MOB gameHere(Room room)
	{
		if(room==null) return null;
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB mob=room.fetchInhabitant(i);
			if(Sense.isEvil(mob))
				return mob;
		}
		return null;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.fetchEffect(this.ID())!=null)
		{
			mob.tell("You are already trying to hunt "+word()+".");
			return false;
		}
		Vector V=Sense.flaggedAffects(mob,Ability.FLAG_TRACKING);
		for(int v=0;v<V.size();v++)	((Ability)V.elementAt(v)).unInvoke();

		theTrail=null;
		nextDirection=-2;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(gameHere(mob.location())!=null)
		{
			mob.tell("Try 'look'.");
			return false;
		}

		boolean success=profficiencyCheck(mob,0,auto);

		Vector rooms=new Vector();
		for(int i=0;i<1000;i++)
		{
			Room R=mob.location().getArea().getRandomProperRoom();
			if((gameHere(R)!=null)&&(!rooms.contains(R)))
			{
				rooms.addElement(R);
				break;
			}
		}

		if(rooms.size()<=0)
		for(Enumeration r=mob.location().getArea().getProperMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(gameHere(R)!=null)
				rooms.addElement(R);
		}

		if(rooms.size()<=0)
		{
			for(int i=0;i<1000;i++)
			{
				Room R=CMMap.getRandomRoom();
				if((gameHere(R)!=null)&&(!rooms.contains(R)))
				{
					rooms.addElement(R);
					break;
				}
			}
			if(rooms.size()<=0)
			{
			    try
			    {
					for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
					{
						Room R=(Room)r.nextElement();
						if(Sense.canAccess(mob,R))
							if(gameHere(R)!=null)
								rooms.addElement(R);
					}
			    }catch(NoSuchElementException e){}
			}
		}

		if(rooms.size()>0)
			theTrail=MUDTracker.findBastardTheBestWay(mob.location(),rooms,false,false,false,false,false,50);

		MOB target=null;
		if((theTrail!=null)&&(theTrail.size()>0))
			target=gameHere((Room)theTrail.firstElement());

		if((success)&&(theTrail!=null)&&(target!=null))
		{
			theTrail.addElement(mob.location());
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"^S<S-NAME> "+prayWord(mob)+" for the trail to "+word()+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Prayer_HuntEvil newOne=(Prayer_HuntEvil)this.copyOf();
				if(mob.fetchEffect(newOne.ID())==null)
					mob.addEffect(newOne);
				mob.recoverEnvStats();
				newOne.nextDirection=MUDTracker.trackNextDirectionFromHere(newOne.theTrail,mob.location(),false);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for the trail to "+word()+", but nothing happens.");


		// return whether it worked
		return success;
	}
}
