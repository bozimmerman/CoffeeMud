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
   Copyright 2004-2018 Bo Zimmerman

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

public class Spell_WizardsChest extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_WizardsChest";
	}

	private final static String localizedName = CMLib.lang().L("Wizards Chest");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Wizard Chest)");

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
		return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected==null)
			return true;

		if(!super.okMessage(myHost,msg))
			return false;

		final MOB mob=msg.source();
		if(((!msg.amITarget(affected))&&(msg.tool()!=affected))
		||(msg.source()==invoker())
		||((invoker()!=null)&&(invoker().Name().equals(text()))))
			return true;

		switch(msg.targetMinor())
		{
		case CMMsg.TYP_OPEN:
			mob.tell(L("@x1 appears to be magically protected.",affected.name()));
			return false;
		case CMMsg.TYP_UNLOCK:
			mob.tell(L("@x1 appears to be magically protected.",affected.name()));
			return false;
		case CMMsg.TYP_JUSTICE:
		{
			if(!msg.targetMajor(CMMsg.MASK_DELICATE))
				return true;
		}
		//$FALL-THROUGH$
		case CMMsg.TYP_DELICATE_HANDS_ACT:
			mob.tell(L("@x1 appears to be magically protected.",affected.name()));
			return false;
		default:
			break;
		}
		return true;
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((msg.target()==affected)
		&&((msg.source()==invoker())||(msg.source().Name().equals(text())))
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(msg.sourceMessage().toUpperCase().indexOf("OPEN")>=0)
		&&(affected instanceof Container))
		{
			final Container container=(Container)affected;
			container.setDoorsNLocks(container.hasADoor(),true,container.defaultsClosed(),container.hasALock(),false,container.defaultsLocked());
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),affected,null,CMMsg.MSG_OK_VISUAL,L("<T-NAME> pop(s) open!")));
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell(L("Enchant what?."));
			return false;
		}
		Physical target=null;
		target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
			return false;

		if((!(target instanceof Container))||(!((Container)target).hasALock())||(!((Container)target).hasADoor()))
		{
			mob.tell(L("You can only enchant the locks on open containers with lids."));
			return false;
		}

		if(!((Container)target).isOpen())
		{
			mob.tell(L("@x1 must be opened before this magic will work.",target.name(mob)));
			return false;
		}

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(L("@x1 is already a wizards chest!",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?"":L("^S<S-NAME> point(s) <S-HIS-HER> finger at <T-NAMESELF>, incanting.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target instanceof Container)
				{
					beneficialAffect(mob,target,asLevel,Ability.TICKS_ALMOST_FOREVER);
					final Container container=(Container)target;
					container.setDoorsNLocks(container.hasADoor(),false,container.defaultsClosed(),container.hasALock(),true,container.defaultsLocked());
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("<T-NAME> look(s) well protected!"));
				}
			}

		}
		else
			beneficialVisualFizzle(mob,target,L("<S-NAME> point(s) at <T-NAMESELF>, incanting, but nothing happens."));

		// return whether it worked
		return success;
	}
}

