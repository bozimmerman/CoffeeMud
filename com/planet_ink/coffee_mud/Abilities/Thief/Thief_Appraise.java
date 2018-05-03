package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2018 Bo Zimmerman

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

public class Thief_Appraise extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Appraise";
	}

	private final static String localizedName = CMLib.lang().L("Appraise");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"APPRAISE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public boolean disregardsArmorCheck(MOB mob)
	{
		return true;
	}

	public int code=0;

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STREETSMARTS;
	}

	@Override
	public int abilityCode()
	{
		return code;
	}

	@Override
	public void setAbilityCode(int newCode)
	{
		code=newCode;
	}

	public String getAppraisal(final MOB mob, final Item target, boolean success)
	{
		double realValue=0.0;
		if(target instanceof Coins)
			realValue = ((Coins)target).getTotalValue();
		else
			realValue=target.value();
		int materialCode=target.material();
		int weight=target.basePhyStats().weight();
		int height=target.basePhyStats().height();
		int allWeight=target.phyStats().weight();
		if(!success)
		{
			final double deviance=CMath.div(CMLib.dice().roll(1,100,0)+50,100);
			realValue=CMath.mul(realValue,deviance);
			materialCode=CMLib.dice().roll(1,RawMaterial.CODES.TOTAL(),-1);
			weight=(int)Math.round(CMath.mul(weight,deviance));
			height=(int)Math.round(CMath.mul(height,deviance));
			allWeight=(int)Math.round(CMath.mul(allWeight,deviance));
		}
		final StringBuffer str=new StringBuffer("");
		str.append(L("@x1 is made of @x2",target.name(mob),RawMaterial.CODES.NAME(materialCode)));
		str.append(L(" is worth about @x1.",CMLib.beanCounter().nameCurrencyShort(mob,realValue)));
		if(target instanceof Armor)
			str.append(L("\n\r@x1 is a size @x2.",target.name(mob),""+height));
		if(weight!=allWeight)
			str.append(L("\n\rIt weighs @x1 pounds empty and @x2 pounds right now.",""+weight,""+allWeight));
		else
			str.append(L("\n\rIt weighs @x1 pounds.",""+weight));
		return str.toString();
	}
	
	public String getWorth(final MOB mob, final Item target, boolean success)
	{
		double realValue=0.0;
		if(target instanceof Coins)
			realValue = ((Coins)target).getTotalValue();
		else
			realValue=target.value();
		if(!success)
		{
			final double deviance=CMath.div(CMLib.dice().roll(1,100,0)+50,100);
			realValue=CMath.mul(realValue,deviance);
		}
		final StringBuffer str=new StringBuffer("");
		str.append(L("@x1 is worth about @x2.",target.name(),CMLib.beanCounter().nameCurrencyShort(mob,realValue)));
		return str.toString();
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((givenTarget instanceof Item)&&(auto)&&(asLevel==-1))
		{
			if(commands.size()==1)
			{
				int levelDiff=givenTarget.phyStats().level()-(mob.phyStats().level()+abilityCode()+(2*getXLEVELLevel(mob)));
				if(levelDiff<0)
					levelDiff=0;
				levelDiff*=5;
				final boolean success=proficiencyCheck(mob,-levelDiff,auto);
				if(commands.get(0).equals("MSG"))
				{
					commands.clear();
					commands.add(this.getAppraisal(mob, (Item)givenTarget, success));
					return true;
				}
				else
				if(commands.get(0).equals("WORTH"))
				{
					commands.clear();
					commands.add(this.getWorth(mob, (Item)givenTarget, success));
					return true;
				}
			}
		}
		if(commands.size()<1)
		{
			mob.tell(L("What would you like to appraise?"));
			return false;
		}
		final Item target=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,commands.get(0));
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(L("You don't see '@x1' here.",(commands.get(0))));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+abilityCode()+(2*getXLEVELLevel(mob)));
		if(levelDiff<0)
			levelDiff=0;
		levelDiff*=5;
		final boolean success=proficiencyCheck(mob,-levelDiff,auto);

		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,L("<S-NAME> appraise(s) <T-NAMESELF>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			final String str=getAppraisal(mob,target,success);
			mob.tell(str);
		}
		return success;
	}

}
