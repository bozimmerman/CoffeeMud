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

/*
   Copyright 2008-2023 Bo Zimmerman

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
public class Prayer_SenseResistances extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_SenseResistances";
	}

	private final static String localizedName = CMLib.lang().L("Sense Resistances");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int overrideMana()
	{
		return 100;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
					auto?"":(mob==target)?"^S<S-NAME> close(s) <T-HIS-HER> eyes and peer(s) into <T-HIS-HER> own aura.^?":"^S<S-NAME> peer(s) into the aura of <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final StringBuilder str=new StringBuilder("");
				final List<String> codes = new ArrayList<String>();
				for(final int saveCode : CharStats.CODES.SAVING_THROWS())
				{
					if(target.charStats().getStat(saveCode)>0)
						codes.add(CMStrings.capitalizeAllFirstLettersAndLower(CharStats.CODES.NAME(saveCode))+"("+target.charStats().getStat(saveCode)+"%)");
				}
				if(codes.size()>0)
					str.append(CMLib.english().toEnglishStringList(codes.toArray(new String[0]))).append(". ");
				for(final Enumeration<Ability> e=target.effects();e.hasMoreElements();)
				{
					final Ability A=e.nextElement();
					if((A!=null)
					&&(CMath.bset(A.flags(), Ability.FLAG_RESISTER)))
						str.append(A.accountForYourself()).append(" ");
				}
				if(str.toString().length()==0)
					mob.tell(mob,target,null,L("<T-NAME> seem(s) like <T-HE-SHE> <T-HAS-HAVE> no special resistances."));
				else
					mob.tell(mob,target,null,L("<T-NAME> seem(s) like <T-HE-SHE> <T-HAS-HAVE> the following resistances: @x1",str.toString()));
			}
		}
		else
		if(mob==target)
			beneficialWordsFizzle(mob,target,auto?"":L("<S-NAME> close(s) <T-HIS-HER> eyes and peer(s) into <T-HIS-HER> own aura, but then blink(s)."));
		else
			beneficialWordsFizzle(mob,target,auto?"":L("<S-NAME> peer(s) into the aura of <T-NAMESELF>, but then blink(s)."));

		// return whether it worked
		return success;
	}
}
