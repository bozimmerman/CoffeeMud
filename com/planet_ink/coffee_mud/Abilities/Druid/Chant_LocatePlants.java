package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Chant_LocatePlants extends Chant
{
	public String ID() { return "Chant_LocatePlants"; }
	public String name(){ return "Locate Plants";}
	public String displayText(){return "(Locating Plants)";}
	public long flags(){return Ability.FLAG_TRACKING;}

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
				mob.tell(plantsHere(mob,mob.location()));
				nextDirection=-2;
				unInvoke();
			}
			else
			if(nextDirection==-1)
			{
				if(plantsHere(mob,mob.location()).length()==0)
					mob.tell("The plant life trail fizzles out here.");
				nextDirection=-999;
				unInvoke();
			}
			else
			if(nextDirection>=0)
			{
				mob.tell("Your sense plant life "+Directions.getDirectionName(nextDirection)+".");
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

	public String plantsHere(MOB mob, Room R)
	{
		StringBuffer msg=new StringBuffer("");
		if(R==null) return msg.toString();
		Room room=(Room)R;
		if((room.domainType()==Room.DOMAIN_OUTDOORS_WOODS)
		||(room.domainType()==Room.DOMAIN_OUTDOORS_PLAINS)
		||(room.domainType()==Room.DOMAIN_OUTDOORS_HILLS)
		||((room.myResource()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN)
		||((room.myResource()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_VEGETATION)
		||(room.domainType()==Room.DOMAIN_OUTDOORS_JUNGLE)
		||(room.domainType()==Room.DOMAIN_OUTDOORS_SWAMP))
			msg.append("There seem to be a large number of plants all around you!\n\r");
		return msg.toString();
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already trying to find plant life.");
			return false;
		}
		Vector V=Sense.flaggedAffects(mob,Ability.FLAG_TRACKING);
		for(int v=0;v<V.size();v++)	((Ability)V.elementAt(v)).unInvoke();

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		String here=plantsHere(target,target.location());
		if(here.length()>0)
		{
			target.tell(here);
			return true;
		}

		boolean success=profficiencyCheck(mob,0,auto);

		Vector rooms=new Vector();
		for(Enumeration r=target.location().getArea().getProperMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(plantsHere(mob,R).length()>0)
				rooms.addElement(R);
		}

		if(rooms.size()<=0)
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(Sense.canAccess(mob,R))
				if(plantsHere(target,R).length()>0)
					rooms.addElement(R);
		}

		if(rooms.size()>0)
			theTrail=MUDTracker.findBastardTheBestWay(target.location(),rooms,false);

		if((success)&&(theTrail!=null))
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> begin(s) to sense plant life!":"^S<S-NAME> chant(s) for a route to plant life.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Chant_LocatePlants newOne=(Chant_LocatePlants)this.copyOf();
				if(target.fetchEffect(newOne.ID())==null)
					target.addEffect(newOne);
				target.recoverEnvStats();
				newOne.nextDirection=MUDTracker.trackNextDirectionFromHere(newOne.theTrail,target.location(),false);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) to find plant life, but fail(s).");

		return success;
	}
}