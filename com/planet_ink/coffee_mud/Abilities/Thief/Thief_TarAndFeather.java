package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Thief_TarAndFeather extends ThiefSkill
{
	public String ID() { return "Thief_TarAndFeather"; }
	public String name(){ return "Tar And Feather";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.OK_OTHERS;}
	private static final String[] triggerStrings = {"TARANDFEATHER","TAR"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int overrideMana(){return 100;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}

	public void affectEnvStats(Environmental host, EnvStats stats)
	{
	    if((affected==null)||(!(affected instanceof Item)))
            return;
	    if((((Item)affected).amWearingAt(Item.INVENTORY))||(((Item)affected).amDestroyed()))
	    {
	        affected.delEffect(this);
	        setAffectedOne(null);
	        ((Item)affected).destroy();
	    }
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat())
		{
			mob.tell("Not while in combat!");
			return false;
		}
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!Sense.aliveAwakeMobile(mob,true))
		{
			mob.tell("You need to stand up!");
			return false;
		}
		if((!auto)&&(!Sense.isBound(target))&&(!Sense.isSleeping(target)))
		{
			mob.tell(target.name()+" must be prone or bound first.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_THIEF_ACT,"<S-NAME> tar(s) and feather(s) <T-NAMESELF>!");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			Item I=CMClass.getArmor("GenArmor");
			if(I!=null)
			{
			    I.setName("a coating of tar and feathers");
			    I.setDisplayText("a pile of tar and feathers sits here.");
			    I.baseEnvStats().setSensesMask(EnvStats.SENSE_ITEMNOREMOVE);
			    I.envStats().setSensesMask(EnvStats.SENSE_ITEMNOREMOVE);
			    I.addNonUninvokableEffect((Ability)this.copyOf());
			    mob.addInventory(I);
			    long wearCode=0;
			    I.setRawWornCode(wearCode);
			}
		}
		else
		    maliciousFizzle(mob,target,"<S-NAME> attempt(s) to tar and feather <T-NAMESELF>, but fail(s).");
		return success;
	}
}
