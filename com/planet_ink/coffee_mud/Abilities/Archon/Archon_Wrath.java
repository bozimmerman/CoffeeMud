package com.planet_ink.coffee_mud.Abilities.Archon;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.CMColor;
import com.planet_ink.coffee_mud.utils.Log;
import com.planet_ink.coffee_mud.common.*;
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

public class Archon_Wrath extends ArchonSkill
{
	boolean doneTicking=false;
	public String ID() { return "Archon_Wrath"; }
	public String name(){ return "Wrath";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"WRATH"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public int maxRange(){return 1;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTargetAnywhere(mob,commands,givenTarget,true);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MASK_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_GENERAL:0),
									auto?"<T-NAME> <T-IS-ARE> knocked out of <T-HIS-HER> shoes!!!":
										 "^F**<S-NAME> BLAST(S) <T-NAMESELF>**, knocking <T-HIM-HER> out of <T-HIS-HER> shoes!!^?");
            CMColor.fixSourceFightColor(msg);
			if(target.location().okMessage(mob,msg))
			{
				target.location().send(mob,msg);
				if(target.curState().getHitPoints()>2)
					target.curState().setHitPoints(target.curState().getHitPoints()/2);
				if(target.curState().getMana()>2)
					target.curState().setMana(target.curState().getMana()/2);
				if(target.curState().getMovement()>2)
					target.curState().setMovement(target.curState().getMovement()/2);
				Item I=target.fetchFirstWornItem(Item.ON_FEET);
				if(I!=null)
				{
					I.unWear();
					I.removeFromOwnerContainer();
					target.location().addItemRefuse(I,Item.REFUSE_PLAYER_DROP);
				}
                Log.sysOut("Banish",mob.name()+" wrathed "+target.name()+".");
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to inflict <S-HIS-HER> wrath upon <T-NAMESELF>, but fail(s).");
		return success;
	}
}
