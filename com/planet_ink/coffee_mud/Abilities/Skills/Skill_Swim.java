package com.planet_ink.coffee_mud.Abilities.Skills;
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
public class Skill_Swim extends StdAbility
{
	public String ID() { return "Skill_Swim"; }
	public String name(){ return "Swim";}
	public String displayText(){ return "(Swimming)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"SWIM"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	protected int trainsRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_COMMONTRAINCOST);}
	protected int practicesRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_COMMONPRACCOST);}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean placeToSwim(Room r2)
	{
		if((r2==null)
		||((r2.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
		&&(r2.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
		&&(r2.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
		&&(r2.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)))
			return false;
		return true;
	}
	public boolean placeToSwim(Environmental E)
	{ return placeToSwim(CoffeeUtensils.roomLocation(E));}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SWIMMING);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		int dirCode=Directions.getDirectionCode(Util.combine(commands,0));
		if(dirCode<0)
		{
			mob.tell("Swim where?");
			return false;
		}
        Room r=mob.location().getRoomInDir(dirCode);
		if(!placeToSwim(mob.location()))
		{
			if(!placeToSwim(r))
			{
				mob.tell("There is no water to swim on that way.");
				return false;
			}
		}
        else
		if((r!=null)
		&&(r.domainType()==Room.DOMAIN_OUTDOORS_AIR)
		&&(r.domainType()==Room.DOMAIN_INDOORS_AIR))
        {
            mob.tell("There is no water to swim on that way.");
            return false;
        }

		if((mob.riding()!=null)
		&&(mob.riding().rideBasis()!=Rideable.RIDEABLE_WATER)
		&&(mob.riding().rideBasis()!=Rideable.RIDEABLE_AIR))
		{
			mob.tell("You need to get off "+mob.riding().name()+" first!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,null);
		Room R=mob.location();
		if((R!=null)
		&&(R.okMessage(mob,msg)))
		{
			R.send(mob,msg);
			success=profficiencyCheck(mob,0,auto);
			if(!success)
				R.show(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> struggle(s) against the water, making no progress.");
			else
			{
				if(mob.fetchEffect(ID())==null)
					mob.addEffect(this);
				mob.recoverEnvStats();

				MUDTracker.move(mob,dirCode,false,false);
			}
			mob.delEffect(this);
			mob.recoverEnvStats();
			if(mob.location()!=R)
				mob.location().show(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,null);
		}
		return success;
	}
}
