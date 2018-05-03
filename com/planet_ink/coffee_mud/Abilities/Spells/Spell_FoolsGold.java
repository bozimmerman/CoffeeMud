package com.planet_ink.coffee_mud.Abilities.Spells;
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

public class Spell_FoolsGold extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_FoolsGold";
	}

	private final static String	localizedName	= CMLib.lang().L("Fools Gold");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	boolean	destroyOnNextTick	= false;

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_ILLUSION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!destroyOnNextTick)
			return super.tick(ticking,tickID);
		((Item)affected).destroy();
		destroyOnNextTick=false;
		return false;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)&&(affected instanceof Item))
		{
			if((msg.amITarget(affected))&&(msg.targetMinor()==CMMsg.TYP_GET)&&(msg.source()!=invoker))
				destroyOnNextTick=true;
			else
			if((msg.tool()!=null)&&(msg.tool()==affected)&&(msg.targetMinor()==CMMsg.TYP_GIVE))
				destroyOnNextTick=true;
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((commands.size()==0)||(CMath.s_int(CMParms.combine(commands,0))==0))
		{
			mob.tell(L("You must specify how big of a pile of gold to create."));
			return false;
		}
		final int amount=CMath.s_int(CMParms.combine(commands,0));
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),auto?"":L("^S<S-NAME> wave(s) <S-HIS-HER> arms around dramatically.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Item gold=CMClass.getItem("GenItem");
				switch(amount)
				{
				case 1:
					gold.setName(L("a gold coin"));
					gold.setDisplayText(L("a gold coin sits here"));
					break;
				case 2:
					gold.setName(L("two gold coins"));
					gold.setDisplayText(L("two gold coins sit here"));
					break;
				default:
					gold.setName(L("a pile of @x1 gold coins",""+amount));
					gold.setDisplayText(L("@x1 sit here",gold.name()));
					break;
				}
				gold.basePhyStats().setWeight(0);
				gold.basePhyStats().setDisposition(gold.basePhyStats().disposition()|PhyStats.IS_BONUS);
				gold.recoverPhyStats();
				mob.addItem(gold);
				mob.location().show(mob,null,gold,CMMsg.MSG_OK_ACTION,L("Suddenly, <S-NAME> hold(s) <O-NAME>."));
				destroyOnNextTick=false;
				beneficialAffect(mob,gold,asLevel,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> wave(s) <S-HIS-HER> arms around dramatically, but fizzle(s) the spell."));

		// return whether it worked
		return success;
	}
}
