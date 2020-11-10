package com.planet_ink.coffee_mud.Abilities.Skills;
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
   Copyright 2020-2020 Bo Zimmerman

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
public class Skill_Whipsmack extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Whipsmack";
	}

	private final static String localizedName = CMLib.lang().L("Whipsmack");

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
		return Ability.QUALITY_MALICIOUS;
	}

	private static final String[] triggerStrings =I(new String[] {"WHIPSMACK"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_MARTIALLORE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			final Item thisWhip=getWhip(mob,true);
			if(thisWhip==null)
				return Ability.QUALITY_INDIFFERENT;
			if((CMLib.flags().isSitting(target)||CMLib.flags().isSleeping(target)))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	public Item getWhip(final MOB mob, final boolean quiet)
	{
		final Item w=mob.fetchWieldedItem();
		if((w==null)
		||(!(w instanceof Weapon))
		||(((Weapon)w).weaponClassification()!=Weapon.CLASS_FLAILED)
		||((((Weapon)w).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LEATHER))
		{
			if(!quiet)
				mob.tell(L("You need a leather flailed weapon to perform a whipsmack!"));
			return null;
		}
		return w;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		final Item thisWhip=getWhip(mob,true);
		if(thisWhip==null)
		{
			mob.tell(L("You must have a flailed leather weapon to perform a whipsmack."));
			return false;
		}

		if((CMLib.flags().isSitting(target)||CMLib.flags().isSleeping(target)))
		{
			mob.tell(L("@x1 must stand up first!",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		String str=null;
		if(success)
		{
			final int topDamage=thisWhip.phyStats().damage()+(adjustedLevel(mob,asLevel)/2);
			int damage=CMLib.dice().roll(1,topDamage,2);
			final Room R=mob.location();
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),null);
			if((R!=null)
			&&(R.okMessage(mob,msg))
			&&(CMLib.combat().rollToHit(mob, target)))
			{
				R.send(mob,msg);
				if(msg.value()>0)
					damage = (int)Math.round(CMath.div(damage,2.0));
				str=auto?L("<T-NAME> is whipsmacked!"):L("^F^<FIGHT^><S-NAME> <DAMAGES> <T-NAMESELF> with a whsipsmack from @x1!^</FIGHT^>^?",thisWhip.name());
				CMLib.color().fixSourceFightColor(msg);
				CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.MASK_SOUND|CMMsg.MASK_MOVE|CMMsg.TYP_JUSTICE,Weapon.TYPE_BASHING,str);
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to whipsmack <T-NAMESELF>, but end(s) up looking silly."));

		return success;
	}

}
