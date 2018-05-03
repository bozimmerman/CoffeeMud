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

public class Spell_Immunity extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Immunity";
	}

	private final static String localizedName = CMLib.lang().L("Immunity");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return L("(Immunity to "+immunityName+")");
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ABJURATION;
	}

	protected int immunityCode=-1;
	protected int immunityType=-1;
	protected String immunityName="";

	@Override
	public String text()
	{
		if((immunityCode>=0)
		&&(affected!=null)
		&&((!this.unInvoked)||(!this.canBeUninvoked))
		&&(this.isSavable()))
			return Integer.toString(immunityCode);
		return "";
	}
	
	@Override
	public void setMiscText(final String misc)
	{
		if((misc!=null)
		&&(misc.length()>0)
		&&(CMath.isInteger(misc)))
		{
			setImmunityVars(CMath.s_int(misc));
		}
	}

	public void setImmunityVars(final int code)
	{
		immunityCode=code;
		switch(immunityCode)
		{
		case 1:
			immunityType=CMMsg.TYP_ACID;
			immunityName="acid";
			break;
		case 2:
			immunityType=CMMsg.TYP_FIRE;
			immunityName="fire";
			break;
		case 3:
			immunityType=CMMsg.TYP_GAS;
			immunityName="gas";
			break;
		case 4:
			immunityType=CMMsg.TYP_COLD;
			immunityName="cold";
			break;
		case 5:
			immunityType=CMMsg.TYP_ELECTRIC;
			immunityName="electricity";
			break;
		}
	}
	
	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell(L("Your immunity has passed."));

		super.unInvoke();

	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&((CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))||(msg.targetMinor()==CMMsg.TYP_DAMAGE))
		&&(msg.sourceMinor()==immunityType)
		&&(!mob.amDead())
		&&((mob.fetchAbility(ID())==null)||proficiencyCheck(null,0,false)))
		{
			mob.location().show(mob,msg.source(),CMMsg.MSG_OK_VISUAL,L("<S-NAME> seem(s) immune to @x1 attack from <T-NAME>.",immunityName));
			return false;
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> attain(s) an immunity barrier."):L("^S<S-NAME> invoke(s) an immunity barrier around <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				immunityCode=-1;
				final Spell_Immunity A=(Spell_Immunity)beneficialAffect(mob,target,asLevel,0);
				if(A!=null)
					setImmunityVars(CMLib.dice().roll(1,5,0));
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to invoke an immunity barrier, but fail(s)."));

		return success;
	}
}
