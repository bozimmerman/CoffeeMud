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
   Copyright 2001-2018 Bo Zimmerman

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

public class Thief_Bribe extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Bribe";
	}

	private final static String localizedName = CMLib.lang().L("Bribe");

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
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	private static final String[] triggerStrings =I(new String[] {"BRIBE"});
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

	protected MOB lastChecked=null;

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_INFLUENTIAL;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell(L("Bribe whom?"));
			return false;
		}
		final Vector<String> V=new Vector<String>();
		V.addElement(commands.get(0));
		final MOB target=this.getTarget(mob,V,givenTarget);
		if(target==null)
			return false;

		commands.remove(0);

		if((!target.mayIFight(mob))
		||(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)<3)
		||(!target.isMonster()))
		{
			mob.tell(L("You can't bribe @x1.",target.name(mob)));
			return false;
		}

		if(commands.size()<1)
		{
			mob.tell(L("Bribe @x1 to do what?",target.charStats().himher()));
			return false;
		}

		CMObject O=CMLib.english().findCommand(target,commands);
		if(O instanceof Command)
		{
			if((!((Command)O).canBeOrdered())||(!((Command)O).securityCheck(mob)))
			{
				mob.tell(L("You can't bribe someone into doing that."));
				return false;
			}
		}
		else
		{
			if(O instanceof Ability)
				O=CMLib.english().getToEvoke(target,new XVector<String>(commands));
			if(O instanceof Ability)
			{
				if(CMath.bset(((Ability)O).flags(),Ability.FLAG_NOORDERING))
				{
					mob.tell(L("You can't bribe @x1 to do that.",target.name(mob)));
					return false;
				}
			}
		}

		if(commands.get(0).toUpperCase().startsWith("FOL"))
		{
			mob.tell(L("You can't bribe someone to following you."));
			return false;
		}

		final int oldProficiency=proficiency();

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final double amountRequired=CMLib.beanCounter().getTotalAbsoluteNativeValue(target)
						+((double)((100l-((mob.charStats().getStat(CharStats.STAT_CHARISMA)+(2l*getXLEVELLevel(mob)))*2)))*target.phyStats().level());

		final String currency=CMLib.beanCounter().getCurrency(target);
		boolean success=proficiencyCheck(mob,0,auto);

		if((!success)||(CMLib.beanCounter().getTotalAbsoluteValue(mob,currency)<amountRequired))
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,L("^T<S-NAME> attempt(s) to bribe <T-NAMESELF> to '@x1', but no deal is reached.^?",CMParms.combine(commands,0)));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			if(CMLib.beanCounter().getTotalAbsoluteValue(mob,currency)<amountRequired)
			{
				final String costWords=CMLib.beanCounter().nameCurrencyShort(currency,amountRequired);
				mob.tell(L("@x1 requires @x2 to do this.",target.charStats().HeShe(),costWords));
			}
			success=false;
		}
		else
		{
			final String costWords=CMLib.beanCounter().nameCurrencyShort(target,amountRequired);
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,L("^T<S-NAME> bribe(s) <T-NAMESELF> to '@x1' for @x2.^?",CMParms.combine(commands,0),costWords));
			CMLib.beanCounter().subtractMoney(mob,currency,amountRequired);
			mob.recoverPhyStats();
			final CMMsg omsg=CMClass.getMsg(mob,target,null,CMMsg.MSG_ORDER,null);
			if((mob.location().okMessage(mob,msg))
			&&(mob.location().okMessage(mob,omsg)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,omsg);
				if(omsg.sourceMinor()==CMMsg.TYP_ORDER)
					target.doCommand(commands,MUDCmdProcessor.METAFLAG_FORCED|MUDCmdProcessor.METAFLAG_ORDER);
			}
			CMLib.beanCounter().addMoney(mob,currency,amountRequired);
			target.recoverPhyStats();
		}
		if(target==lastChecked)
			setProficiency(oldProficiency);
		lastChecked=target;
		return success;
	}

}
