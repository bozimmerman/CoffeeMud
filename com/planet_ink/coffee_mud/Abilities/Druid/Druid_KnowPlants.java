package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Druid_KnowPlants extends StdAbility
{
	public String ID() { return "Druid_KnowPlants"; }
	public String name(){ return "Know Plants";}
	public int quality(){return Ability.OK_SELF;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	private static final String[] triggerStrings = {"KNOWPLANT"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}

	public static boolean isPlant(Item I)
	{
		if((I!=null)&&(I.rawSecretIdentity().length()>0))
		{
			for(int a=0;a<I.numEffects();a++)
			{
				Ability A=I.fetchEffect(a);
				if((A!=null)&&(A.invoker()!=null)&&(A instanceof Chant_SummonPlants))
					return true;
			}
		}
		return false;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item I=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(I==null) return false;
		if(((I.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_VEGETATION)
		&&((I.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_WOODEN))
		{
			mob.tell("Your plant knowledge can tell you nothing about "+I.name()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		boolean success=profficiencyCheck(mob,0,auto);

		if(!success)
			mob.tell("Your plant senses fail you.");
		else
		{
			FullMsg msg=new FullMsg(mob,I,null,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT|CMMsg.MASK_MAGIC,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				StringBuffer str=new StringBuffer("");
				str.append(I.name()+" is a kind of "+EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK].toLowerCase()+".  ");
				if(isPlant(I))
					str.append("It was summoned by "+I.rawSecretIdentity()+".");
				else
					str.append("It is either processed by hand, or grown wild.");
				mob.tell(str.toString());
			}
		}
		return success;
	}
}

