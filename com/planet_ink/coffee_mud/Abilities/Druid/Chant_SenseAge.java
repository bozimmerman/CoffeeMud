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

import java.util.*;

/*
   Copyright 2003-2020 Bo Zimmerman

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
public class Chant_SenseAge extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_SenseAge";
	}

	private final static String localizedName = CMLib.lang().L("Sense Age");

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
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_BREEDING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	protected int overrideMana()
	{
		return 5;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) over <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Ability A=target.fetchEffect("Age");
				final StringBuilder info=new StringBuilder("");
				int ageYears = -1;
				MOB M=null;
				boolean destroyM = false;
				if(target instanceof MOB)
				{
					M=(MOB)target;
					if(M.charStats().getStat(CharStats.STAT_AGE)>0)
						ageYears=M.charStats().getStat(CharStats.STAT_AGE);
				}
				else
				if(target instanceof CagedAnimal)
				{
					M=((CagedAnimal)target).unCageMe();
					destroyM=true;
				}
				if((A!=null)
				&&(A.displayText().length()>0))
				{
					String s=A.displayText();
					if(s.startsWith("("))
						s=s.substring(1);
					if(s.endsWith(")"))
						s=s.substring(0,s.length()-1);
					info.append(L("@x1 is @x2.  ",target.name(mob),s));
					if(ageYears < 0)
						ageYears = CMath.s_int(A.getStat("AGEYEARS"));
				}
				else
				if(ageYears >= 0)
					info.append(L("@x1 is aged @x2 years.  ",target.name(), ""+ageYears));
				else
					info.append(L("You have no way to determining the age of @x1.  ",target.name(mob)));

				success = ageYears >= 0;

				if((super.getXLEVELLevel(mob)>0)
				&&(ageYears>=0)
				&&(M!=null))
					info.append(L("This is "+M.charStats().hisher()+" @x1 stage.  ",M.charStats().ageName()));

				if((super.getXLEVELLevel(mob)>1)
				&&(M!=null))
				{
					final CharStats curStats = (CharStats)M.baseCharStats().copyOf();
					M.charStats().getMyRace().agingAffects(M, M.baseCharStats(), curStats);
					final StringBuilder mods=new StringBuilder("");
					for(final int code : CharStats.CODES.ALLCODES())
					{
						if(curStats.getStat(code) != M.baseCharStats().getStat(code))
						{
							mods.append(CharStats.CODES.DESC(code));
							if(curStats.getStat(code) > M.baseCharStats().getStat(code))
								mods.append("+").append(curStats.getStat(code)-M.baseCharStats().getStat(code));
							else
								mods.append(curStats.getStat(code)-M.baseCharStats().getStat(code));
							mods.append(" ");
						}
					}
					if(mods.length()>0)
						info.append(L("This age has the following effects: @x1.  ",mods.toString()));
				}

				if((super.getXLEVELLevel(mob)>2)
				&&(M!=null))
				{
					info.append(L("Adventuring typically begins around age @x1.  ",""+M.charStats().getMyRace().getAgingChart()[Race.AGE_YOUNGADULT]));
				}

				if((super.getXLEVELLevel(mob)>3)
				&&(M!=null))
				{
					info.append(L("Emerging from infancy to walking typically occurs around age @x1.  ",""+M.charStats().getMyRace().getAgingChart()[Race.AGE_TODDLER]));
				}

				if((super.getXLEVELLevel(mob)>4)
				&&(M!=null))
				{
					if(M.charStats().getMyRace().getAgingChart()[Race.AGE_ANCIENT] == Race.YEARS_AGE_LIVES_FOREVER)
						info.append(L(M.charStats().HeShe()+" can expect to live forever.  "));
					else
						info.append(L(M.charStats().HeShe()+" can expect to live to around age @x1.  ",
								""+M.charStats().getMyRace().getAgingChart()[Race.AGE_ANCIENT]));
				}

				if((super.getXLEVELLevel(mob)>5)
				&&(ageYears>=0)
				&&(M!=null))
				{
					if(M.charStats().ageCategory() == Race.AGE_ANCIENT)
						info.append(L(M.charStats().HeShe()+" can look forward to death.  "));
					else
					{
						final int ageCat=M.charStats().ageCategory()+1;
						final Race R=M.charStats().getMyRace();
						final String[] desc=Race.AGE_DESCS;
						if(R.getAgingChart()[ageCat] == Race.YEARS_AGE_LIVES_FOREVER)
						{
							info.append(L(M.charStats().HeShe()+" will remain "+desc[ageCat-1].toLowerCase()+" forever.  "));
						}
						else
						{
							info.append(L(M.charStats().HeShe()+" can look forward to "+desc[ageCat].toLowerCase()+" around age @x1.  ",
									""+R.getAgingChart()[ageCat]));
						}
					}
				}

				mob.tell(info.toString());
				if(destroyM && (M!=null))
					M.destroy();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) over <T-NAMESELF>, but the magic fades."));

		// return whether it worked
		return success;
	}
}
