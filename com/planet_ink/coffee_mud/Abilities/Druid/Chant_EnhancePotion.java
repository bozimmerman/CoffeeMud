package com.planet_ink.coffee_mud.Abilities.Druid;
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

import java.io.IOException;
import java.util.*;

/*
   Copyright 2024-2024 Bo Zimmerman

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
public class Chant_EnhancePotion extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_KnowAnimal";
	}

	private final static String	localizedName	= CMLib.lang().L("Enhance Potion");

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
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_NATURELORE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA;
	}

	private volatile int finalLevel = -1;

	@Override
	public int overrideMana()
	{
		return 50 + ((finalLevel > 30)?((finalLevel-30)*5):0);
	}

	public boolean isMagical(final List<Ability> spells)
	{
		if(spells.size()==0)
			return false;
		for(final Ability A : spells)
		{
			final int acode = A.classificationCode()&Ability.ALL_ACODES;
			if((acode != Ability.ACODE_CHANT)
			&&(acode != Ability.ACODE_PRAYER)
			&&(acode != Ability.ACODE_SPELL))
				return false;
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room roomR=mob.location();
		if(roomR==null)
			return false;
		int levels=1;
		if((commands.size()>1)&&(CMath.isInteger(commands.get(commands.size()-1))))
			levels=CMath.s_int(commands.remove(commands.size()-1));
		if(levels<0)
		{
			mob.tell(L("You can't 'enhance' the potion like that."));
			return false;
		}
		final Item target = this.getTarget(mob, null, givenTarget, commands, Wearable.FILTER_ANY);
		if(target == null)
			return false;

		if(!(target instanceof Potion))
		{
			mob.tell(L("@x1 doesn't seem to be a potion.",target.name(mob)));
			return false;
		}

		if(!isMagical(((Potion)target).getSpells()))
		{
			mob.tell(L("@x1 doesn't seem to be a potion you can enhance.",target.name(mob)));
			return false;
		}
		if(target.phyStats().level()>=mob.phyStats().level() )
		{
			mob.tell(L("You can't enhance @x1 any more.",target.name(mob)));
			return false;
		}
		if(target.phyStats().level()+levels>=mob.phyStats().level() )
		{
			mob.tell(L("You can't enhance @x1 quite that much.",target.name(mob)));
			return false;
		}
		finalLevel = target.phyStats().level() + levels;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		final Room room=mob.location();
		final CMMsg msg=CMClass.getMsg(mob,target,this,super.verbalCastCode(mob, target, auto),auto?"":L("^S<S-NAME> chant(s) quietly over <T-NAMESELF>.^?"));
		if(room.okMessage(mob,msg))
		{
			room.send(mob,msg);
			target.basePhyStats().setLevel(target.basePhyStats().level()+levels);
			target.recoverPhyStats();
			room.show(mob, target, CMMsg.MSG_OK_VISUAL, L("^S<T-NAME> glows brightly!^?"));
		}
		else
			mob.location().show(mob,target,this,super.verbalCastCode(mob, target, auto),L("<S-NAME> chant(s) over <T-NAME> but nothing happens."));
		return success;
	}

}
