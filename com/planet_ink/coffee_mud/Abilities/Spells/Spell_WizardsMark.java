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
   Copyright 2023-2023 Bo Zimmerman

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
public class Spell_WizardsMark extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_WizardsMark";
	}

	private final static String localizedName = CMLib.lang().L("Wizards Mark");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Wizards Mark)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	protected long expirationDate = 0;
	protected String wizardName = null;

	@Override
	public void setMiscText(final String newMiscText)
	{
		expirationDate = System.currentTimeMillis() + 16000;
		super.setMiscText(newMiscText);
		if((newMiscText!=null)&&(newMiscText.length()>0))
		{
			expirationDate = CMParms.getParmLong(newMiscText, "EXPIRE", expirationDate);
			wizardName = CMParms.getParmStr(newMiscText, "WIZARD", "");
		}
		if(affected instanceof Item)
			((Item)affected).setExpirationDate(expirationDate);
	}

	@Override
	public void setAffectedOne(final Physical affected)
	{
		super.setAffectedOne(affected);
		if(affected instanceof Item)
			((Item)affected).setExpirationDate(expirationDate);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof Item)
			((Item)affected).setExpirationDate(expirationDate);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(msg.targetMinor() == CMMsg.TYP_SELL)
		{
			final Physical affected=this.affected;
			if((msg.tool()==affected)
			||((affected instanceof Container)
				&&(msg.tool() instanceof Item)
				&&(((Item)msg.tool()).ultimateContainer(affected)==affected)))
			{
				msg.source().tell(L("You can't sell that."));
				return false;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(affected instanceof Item)
		{
			if((msg.targetMinor()==CMMsg.TYP_DROP)
			&&(msg.target() instanceof Item)
			&&(msg.target()==affected))
				((Item)msg.target()).setExpirationDate(expirationDate);
			if((expirationDate > 0)
			&&(System.currentTimeMillis()> expirationDate))
			{
				unInvoke();
				affected.delEffect(this);
			}
		}
	}

	@Override
	public void unInvoke()
	{
		if(affected instanceof Item)
		{
			final Item I = (Item)affected;
			if((wizardName != null) && (wizardName.length()>0))
			{
				final MOB M=CMLib.players().getLoadPlayerAllHosts(wizardName);
				if((M!= null)&&(M.playerStats()!=null))
					M.playerStats().getExtItems().delItem(I);
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell(L("Put the Wizard`s Mark on what?"));
			return false;
		}
		Item target=null;
		target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
			return false;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(L("@x1 is already magically marked!",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> point(s) <S-HIS-HER> finger at <T-NAMESELF>, incanting.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Ability A = this.beneficialAffect(mob, target, asLevel, asLevel);
				if(A != null)
				{
					final int duraDays = adjustedLevel(mob, asLevel) + (10*super.getXLEVELLevel(mob));
					A.setMiscText("EXPIRE="+(duraDays * TimeManager.MILI_DAY)+" WIZARD=\""+mob.name()+"\"");
					if(mob.playerStats()!= null)
						mob.playerStats().getExtItems().addItem(target);
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> point(s) at <T-NAMESELF>, incanting, but nothing happens."));

		// return whether it worked
		return success;
	}
}
