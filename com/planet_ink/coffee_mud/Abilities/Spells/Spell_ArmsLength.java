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
   Copyright 2011-2018 Bo Zimmerman

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

public class Spell_ArmsLength extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_ArmsLength";
	}

	private final static String localizedName = CMLib.lang().L("Arms Length");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Arms Length)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;
	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.tell(mob,null,null,L("<S-YOUPOSS> arms length magic fades."));
	}

  @Override
public int castingQuality(MOB mob, Physical target)
  {
	  if(mob!=null)
	  {
		  if((!mob.isInCombat())||(mob.rangeToTarget()==0))
			  return Ability.QUALITY_INDIFFERENT;
	  }
	  return super.castingQuality(mob,target);
  }

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((affected instanceof MOB)
		&&(msg.target()==affected)
		&&(msg.sourceMinor()==CMMsg.TYP_ADVANCE))
		{
			final MOB mob=(MOB)affected;
			if((mob.getVictim()==msg.source())
			&&(mob.location()!=null))
			{
		final CMMsg msg2=CMClass.getMsg(mob,mob.getVictim(),CMMsg.MSG_RETREAT,L("<S-NAME> predict(s) <T-YOUPOSS> advance and retreat(s)."));
		if(mob.location().okMessage(mob,msg2))
			mob.location().send(mob,msg2);
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> already <S-IS-ARE> keeping enemies at arms length."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> begin(s) keeping <T-HIS-HER> enemies at arms length!"):L("^S<S-NAME> incant(s) distantly!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int ticks=3 + Math.round(super.getXLEVELLevel(mob)/3);
				if(!mob.isInCombat())
					ticks++;
				beneficialAffect(mob,target,asLevel,ticks);
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> incant(s) distantly, but the spell fizzles."));

		return success;
	}
}
