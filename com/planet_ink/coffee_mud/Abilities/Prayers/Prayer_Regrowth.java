package com.planet_ink.coffee_mud.Abilities.Prayers;
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

/**
 * Title: False Realities Flavored CoffeeMUD
 * Description: The False Realities Version of CoffeeMUD
 * Copyright: Copyright (c) 2004 Jeremy Vyska
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Company: http://www.falserealities.com
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */

public class Prayer_Regrowth extends Prayer implements MendingSkill
{
	@Override
	public String ID()
	{
		return "Prayer_Regrowth";
	}

	private final static String localizedName = CMLib.lang().L("Regrowth");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_HEALING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY|Ability.FLAG_HEALINGMAGIC;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL;
	}

	private static Vector<String> limbsToRegrow = null;

	public Prayer_Regrowth()
	{
		super();
		if(limbsToRegrow==null)
		{
			limbsToRegrow = new Vector<String>();
			limbsToRegrow.addElement("EYE");
			limbsToRegrow.addElement("LEG");
			limbsToRegrow.addElement("FOOT");
			limbsToRegrow.addElement("ARM");
			limbsToRegrow.addElement("HAND");
			limbsToRegrow.addElement("EAR");
			limbsToRegrow.addElement("NOSE");
			limbsToRegrow.addElement("TAIL");
			limbsToRegrow.addElement("WING");
			limbsToRegrow.addElement("ANTENEA");
		}
	}

	@Override
	public boolean supportsMending(Physical item)
	{
		if(!(item instanceof MOB))
			return false;
		return (item.fetchEffect("Amputation")!=null);
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(!supportsMending(target))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
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
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> become(s) surrounded by a bright light."):L("^S<S-NAME> @x1 over <T-NAMESELF> for restorative healing.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final LimbDamage ampuA=(LimbDamage)target.fetchEffect("Amputation");
				if(ampuA!=null)
				{
					final List<String> missing = ampuA.affectedLimbNameSet();
					String LookingFor = null;
					boolean found = false;
					String missLimb=null;
					for(int i=0;i<limbsToRegrow.size();i++)
					{
						LookingFor = limbsToRegrow.elementAt(i);
						for(int j=0;j<missing.size();j++)
						{
							missLimb = missing.get(j);
							if(missLimb.toUpperCase().indexOf(LookingFor)>=0)
							{
								found = true;
								break;
							}
						}
						if(found)
							break;
					}
					if((found)&&(missLimb!=null))
						ampuA.restoreLimb(missLimb.toLowerCase());
					target.recoverCharStats();
					target.recoverPhyStats();
					target.recoverMaxState();
				}
				mob.location().recoverRoomStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 over <T-NAMESELF>, but @x2 does not heed.",prayWord(mob),hisHerDiety(mob)));
		// return whether it worked
		return success;
	}
}
