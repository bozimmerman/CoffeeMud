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
   Copyright 2003-2018 Bo Zimmerman

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

public class Spell_FutureDeath extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_FutureDeath";
	}

	private final static String localizedName = CMLib.lang().L("Future Death");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(getXLEVELLevel(mob)));
		if((!target.mayIFight(mob))||(levelDiff>=(3+((mob.phyStats().level()+(getXLEVELLevel(mob)))/10))))
		{
			mob.tell(L("@x1 looks too powerful.",target.charStats().HeShe()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		levelDiff +=6;
		final boolean success=proficiencyCheck(mob,-((target.charStats().getStat(CharStats.STAT_WISDOM)*2)+(levelDiff*15)),auto);
		if(success)
		{
			String str=auto?"":L("^S<S-NAME> incant(s) at <T-NAMESELF>^?");
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),str);
			final CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
				{
					str=null;
					switch(CMLib.dice().roll(1,10,0))
					{
					case 1:
						str=L("<S-NAME> grab(s) at <S-HIS-HER> throat and choke(s) to death!");
						break;
					case 2:
						str=L("<S-NAME> wave(s) <S-HIS-HER> arms and look(s) down as if falling. Then <S-HE-SHE> hit(s).");
						break;
					case 3:
						str=L("<S-NAME> defend(s) <S-HIM-HERSELF> from unseen blows, then fall(s) dead.");
						break;
					case 4:
						str=L("<S-NAME> gasp(s) for breathe, as if underwater, and drown(s).");
						break;
					case 5:
						str=L("<S-NAME> kneel(s) and lower(s) <S-HIS-HER> head, as if on the block.  In one last whimper, <S-HE-SHE> die(s).");
						break;
					case 6:
						str=L("<S-NAME> jerk(s) as if being struck by a thousand arrows, and die(s).");
						break;
					case 7:
						str=L("<S-NAME> writhe(s) as if being struck by a powerful electric spell, and die(s).");
						break;
					case 8:
						str=L("<S-NAME> lie(s) on the ground, take(s) on a sickly expression, and die(s).");
						break;
					case 9:
						str=L("<S-NAME> grab(s) at <S-HIS-HER> heart, and then it stops.");
						break;
					case 10:
						str=L("<S-NAME> stand(s) on <S-HIS-HER> toes, stick(s) out <S-HIS-HER> tongue, and die(s).");
						break;
					}
					target.location().show(target,null,CMMsg.MSG_OK_VISUAL,str);
					CMLib.combat().postDeath(mob,target,null);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> incant(s) at <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
