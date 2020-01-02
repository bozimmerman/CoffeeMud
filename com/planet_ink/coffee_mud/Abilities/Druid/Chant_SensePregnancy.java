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
public class Chant_SensePregnancy extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_SensePregnancy";
	}

	private final static String localizedName = CMLib.lang().L("Sense Pregnancy");

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
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) over <T-YOUPOSS> stomach.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Ability A=target.fetchEffect("Pregnancy");
				if((A==null)||(A.displayText().length()==0))
					mob.tell(L("@x1 is not pregnant.",target.name(mob)));
				else
				{
					String s=A.displayText();
					if(s.startsWith("("))
						s=s.substring(1);
					if(s.endsWith(")"))
						s=s.substring(0,s.length()-1);
					final StringBuilder info=new StringBuilder("");
					info.append(L("@x1 is @x2.  ",target.name(mob),s));
					if(super.getXLEVELLevel(mob)>0)
					{
						final int numBabies=CMath.s_int(A.getStat("NUMBABIES"));
						if(numBabies>1)
						{
							switch(numBabies)
							{
							case 2:
								info.append(L("There will be twins.  "));
								break;
							case 3:
								info.append(L("There will be triplets.  "));
								break;
							case 4:
								info.append(L("There will be quadruplets.  "));
								break;
							case 5:
								info.append(L("There will be quintuplets.  "));
								break;
							case 6:
								info.append(L("There will be sextuplets.  "));
								break;
							case 7:
								info.append(L("There will be septuplets.  "));
								break;
							case 8:
								info.append(L("There will be octuplets.  "));
								break;
							default:
								if(numBabies>8)
									info.append(L("There will be way too many babies.  "));
								else
									info.append(L("The baby will be deformed.  "));
								break;
							}
						}
					}

					if(super.getXLEVELLevel(mob)>1)
					{
						final String mothersRace=A.getStat("MOTHERRACE");
						Race mR=CMClass.getRace(mothersRace);
						if(mR == null)
							mR=CMClass.findRace(mothersRace);
						if(mR!=null)
						{
							final TimeClock C = CMLib.time().localClock(target);
							int birthmonths = (int) Math.round(CMath.mul((mR.getAgingChart()[1] - mR.getAgingChart()[0]) * C.getMonthsInYear(), 0.75));
							if (birthmonths <= 0)
								birthmonths = 5;
							info.append(L("These sorts of pregnancies usually last about @x1 months.  ", ""+birthmonths));
						}
					}

					if(super.getXLEVELLevel(mob)>3)
					{
						final String mothersRace=A.getStat("MOTHERRACE");
						Race mR=CMClass.getRace(mothersRace);
						if(mR == null)
							mR=CMClass.findRace(mothersRace);
						if(mR!=null)
						{
							if(CMLib.flags().isEggLayer(mR))
								info.append(L("This will be an egg-laying birth.  "));
							else
								info.append(L("This will be a live birth.  "));
						}
					}

					if(super.getXLEVELLevel(mob)>4)
					{
						final String mothersRace=A.getStat("MOTHERRACE");
						final String fathersRace=A.getStat("FATHERRACE");
						if((mothersRace != null)
						&&(mothersRace.length()>0))
						{
							Race mR=CMClass.getRace(mothersRace);
							if(mR == null)
								mR=CMClass.findRace(mothersRace);
							if(mR!=null)
							{
								if((fathersRace != null)
								&&(fathersRace.length()>0)
								&&(!mothersRace.equalsIgnoreCase(fathersRace)))
								{
									Race fR=CMClass.getRace(fathersRace);
									if(fR == null)
										fR=CMClass.findRace(fathersRace);
									if((fR!=null)
									&&(fR!=mR))
										info.append(L("The baby will be a cross between a(n) @x1 and a(n) @x2.  ",mR.name(),fR.name()));
									else
										info.append(L("The baby will be a(n) @x1.  ",mR.name()));
								}
								else
									info.append(L("The baby will be a(n) @x1.  ",mR.name()));
							}
						}
					}

					if(super.getXLEVELLevel(mob)>6)
					{
						final String fathersName=A.getStat("FATHERNAME");
						if((fathersName != null)
						&&(fathersName.length()>0))
						{
							info.append(L("The father was called '@x1'.  ",fathersName));
						}
					}

					if(super.getXLEVELLevel(mob)>7)
					{
						//final long startMs=CMath.s_long(A.getStat("PREGSTART"));
						final long endMs=CMath.s_long(A.getStat("PREGEND"));
						if(endMs > 0)
						{
							TimeClock C = CMLib.time().localClock(mob.getStartRoom());
							final long realEndMs = endMs - (7L * CMProps.getTickMillis() * CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY));
							C=C.deriveClock(realEndMs );
							info.append(L("The baby should arrive around @x1.  ",C.getShortTimeDescription()));
							if(System.currentTimeMillis() > realEndMs)
								info.append(L("You expect labor any second now."));
						}
					}
					mob.tell(info.toString());
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) over <T-YOUPOSS> stomach, but the magic fades."));

		// return whether it worked
		return success;
	}
}
