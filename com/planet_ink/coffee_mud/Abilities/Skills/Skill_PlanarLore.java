package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.PlanarAbility;
import com.planet_ink.coffee_mud.Abilities.PlanarAbility.PlanarSpecFlag;
import com.planet_ink.coffee_mud.Abilities.PlanarAbility.PlanarVar;
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
   Copyright 2017-2018 Bo Zimmerman

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

public class Skill_PlanarLore extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_PlanarLore";
	}

	private final static String	localizedName	= CMLib.lang().L("Planar Lore");

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
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "PLANARLORE", "PLORE" });

	protected long lastFail = 0;
	
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_EDUCATIONLORE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA;
	}

	@Override
	public boolean preInvoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining)
	{
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		final Area A=R.getArea();
		if(A==null)
			return false;
		String currPlane = A.getBlurbFlag("PLANEOFEXISTENCE");
		if((currPlane != null)&&(currPlane.startsWith("{"))&&(currPlane.endsWith("}")))
			currPlane=currPlane.substring(1,currPlane.length()-1);
		if(currPlane != null)
		{
			currPlane = CMStrings.capitalizeAllFirstLettersAndLower(currPlane);
			mob.tell(L("\n\rYou are clearly on the @x1 plane.",currPlane));
		}
		else
		{
			currPlane="Prime Material";
			mob.tell(L("\n\rYou are clearly on the Prime Material plane."));
		}
		
		final Map<String,Map<String,String>> pmap = PlanarAbility.getPlaneMap();
		if((commands.size()==0)
		||((commands.size()==1)&&(commands.get(0).equalsIgnoreCase("LIST"))))
		{
			List<String> names=new ArrayList<String>();
			for(String key : pmap.keySet())
				names.add(CMStrings.capitalizeAllFirstLettersAndLower(key));
			mob.tell(L("Recall information about which plane of existence?  These include: @x1",CMLib.english().toEnglishStringList(names)));
			return false;
		}
		boolean report=false;
		if((commands.size()>1)&&(commands.get(commands.size()-1).equalsIgnoreCase("REPORT")))
		{
			commands.remove(commands.size()-1);
			report=true;
		}
		
		if((System.currentTimeMillis() - lastFail) < 10000)
		{
			mob.tell(L("You still can't recall.  Give yourself some more time to think first."));
			return false;
		}
		
		String possplanname = CMParms.combine(commands);
		if(possplanname.startsWith("the "))
			possplanname = possplanname.substring(4).trim();
		if(possplanname.endsWith(" plane"))
			possplanname = possplanname.substring(0,possplanname.length()-5).trim();
		String planeName = null;
		String planeKey = null;
		for(String key : pmap.keySet())
		{
			if(key.equalsIgnoreCase(possplanname))
			{
				planeKey = key;
				planeName = CMStrings.capitalizeAllFirstLettersAndLower(key);
				break;
			}
		}
		if(planeName == null)
		{
			for(String key : pmap.keySet())
			{
				if(key.startsWith(possplanname.toUpperCase()))
				{
					planeKey = key;
					planeName = CMStrings.capitalizeAllFirstLettersAndLower(key);
					break;
				}
			}
		}
		
		if(planeName == null)
		{
			List<String> names=new ArrayList<String>();
			for(String key : pmap.keySet())
				names.add(CMStrings.capitalizeAllFirstLettersAndLower(key));
			mob.tell(L("You have never heard of a plane called @x1.  You might try one of these: @x2",
					CMParms.combine(commands),CMLib.english().toEnglishStringList(names)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		
		final boolean success=proficiencyCheck(mob,currPlane.equalsIgnoreCase(planeName)?25:0,auto);
		if(!success)
		{
			lastFail = System.currentTimeMillis();
			beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to recall something about @x1, but can't.",planeName));
			return false;
		}
		final Room room=mob.location();
		final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_THINK,L("<S-NAME> <S-IS-ARE> recalling something about @x1 plane.",planeName));
		if(room.okMessage(mob,msg))
		{
			room.send(mob,msg);
			List<String> tidbits = new ArrayList<String>();
			final int expertise = super.getXLEVELLevel(mob);
			Map<String,String> planeVars = pmap.get(planeKey);
			if(planeVars.containsKey(PlanarAbility.PlanarVar.MOBRESIST.toString()))
			{
				String arg=planeVars.get(PlanarAbility.PlanarVar.MOBRESIST.toString());
				final Ability A1=CMClass.getAbility("Prop_Resistance");
				if(A1!=null)
				{
					final String listStr=A1.getStat("TIDBITS="+arg);
					final List<String> list= CMParms.parseAny(listStr, "\n\r", true);
					for(String l : list)
						tidbits.add(L("Creatures there are @x1.",l));
				}
			}
			if(planeVars.containsKey(PlanarAbility.PlanarVar.PREFIX.toString()))
			{
				final List<String> prefixes = CMParms.parseCommas(planeVars.get(PlanarAbility.PlanarVar.PREFIX.toString()), true);
				for(String prefix : prefixes)
				{
					tidbits.add(L("Everything there is very @x1.",prefix));
				}
			}
			if(planeVars.containsKey(PlanarAbility.PlanarVar.WEAPONMAXRANGE.toString()))
			{
				int maxRange=CMath.s_int(planeVars.get(PlanarVar.WEAPONMAXRANGE.toString()));
				tidbits.add(L("Weapons there have a maximum range of @x1.",""+maxRange));
			}
			if(planeVars.containsKey(PlanarAbility.PlanarVar.ATMOSPHERE.toString()))
			{
				final String atmo = planeVars.get(PlanarVar.ATMOSPHERE.toString());
				tidbits.add(L("That plane has an atmosphere of @x1.",atmo));
			}
			
			if(expertise > 0)
			{
				if(planeVars.containsKey(PlanarAbility.PlanarVar.MIXRACE.toString()))
				{
					String mixRace = planeVars.get(PlanarVar.MIXRACE.toString());
					Race firstR=CMClass.getRace(mixRace);
					if(firstR!=null)
						tidbits.add(L("Everyone there is some sort of @x1 hybrid.",firstR.name()));
				}
				String specflags = planeVars.get(PlanarVar.SPECFLAGS.toString());
				if(specflags != null)
				{
					for(String s : CMParms.parse(specflags))
					{
						PlanarSpecFlag flag=(PlanarSpecFlag)CMath.s_valueOf(PlanarSpecFlag.class, s);
						if(flag != null)
						{
							switch(flag)
							{
							case ALLBREATHE:
								tidbits.add(L("Everyone can always breathe there without problems."));
								break;
							case BADMUNDANEARMOR:
								tidbits.add(L("Mundane armor has no benefits there."));
								break;
							case NOINFRAVISION:
								tidbits.add(L("Infravision is useless there."));
								break;
							}
						}
					}
				}
			}
			if(expertise > 1)
			{
				int mobCopy=CMath.s_int(planeVars.get(PlanarVar.MOBCOPY.toString()));
				if(mobCopy>0)
				{
					if(mobCopy == 1)
						tidbits.add(L("There are twice as many creatures there as normal."));
					else
						tidbits.add(L("There are @x1 times as many creatures there as normal.",""+(mobCopy)));
				}
				String aeffects = planeVars.get(PlanarVar.AEFFECT.toString());
				if(aeffects!=null)
				{
					List<Pair<String,String>> affectList=CMParms.parseSpaceParenList(aeffects);
					if(affectList!=null)
					{
						for(Pair<String,String> p : affectList)
						{
							if(p.first.equalsIgnoreCase("ResourceOverride"))
								tidbits.add(L("There is a super abundance of @x1 there.",p.second.toLowerCase()));
						}
					}
				}
			}
			if(expertise > 2)
			{
				String aeffects = planeVars.get(PlanarVar.AEFFECT.toString());
				if(aeffects!=null)
				{
					List<Pair<String,String>> affectList=CMParms.parseSpaceParenList(aeffects);
					if(affectList!=null)
					{
						for(Pair<String,String> p : affectList)
						{
							if(p.first.equalsIgnoreCase("Prop_Weather"))
							{
								List<String> parms=CMParms.parse(p.second);
								if(text().length()>0)
								{
									for(String parm : parms)
									{
										parm = parm.toUpperCase();
										if(CMParms.containsIgnoreCase(Climate.WEATHER_DESCS,parm))
											tidbits.add(L("It is very @x1 there.",parm.toLowerCase()));
										else
										if(parm.startsWith("CLIMASK_")||parm.startsWith("CLIMATE_"))
										{
											if(CMParms.containsIgnoreCase(Places.CLIMATE_DESCS,parm.substring(8)))
												tidbits.add(L("It is very @x1 there.",parm.substring(8).toLowerCase()));
										}
										else
										if(CMParms.containsIgnoreCase(Places.CLIMATE_DESCS,parm))
											tidbits.add(L("It is very @x1 there.",parm.toLowerCase()));
									}
									tidbits.add(L("There is a super abundance of @x1 there.",p.second.toLowerCase()));
								}
							}
						}
					}
				}
				String adjSize = planeVars.get(PlanarVar.ADJSIZE.toString());
				if(adjSize != null)
				{
					int height = CMParms.getParmInt(adjSize, "HEIGHT", 0);
					int weight = CMParms.getParmInt(adjSize, "WEIGHT", 0);
					if(height != 0)
					{
						if(height < 0)
							tidbits.add(L("Everything there is @x1 times shorter than normal.",""+height));
						else
							tidbits.add(L("Everything there is @x1 times taller than normal.",""+height));
					}
					if(weight != 0)
					{
						if(weight < 0)
							tidbits.add(L("Everything there is @x1 times lighter than normal.",""+weight));
						else
							tidbits.add(L("Everything there is @x1 times heavier than normal.",""+weight));
					}
				}
			}
			if(expertise > 3)
			{
				String lvlSize = planeVars.get(PlanarVar.LEVELADJ.toString());
				if(lvlSize != null)
				{
					if(CMath.s_int(lvlSize)<0)
						tidbits.add(L("Everything there is @x1 level(s) less powerful than you are.",""+lvlSize.substring(1)));
					else
						tidbits.add(L("Everything there is @x1 level(s) more powerful than you are.",""+lvlSize));
				}
			}
			if(expertise > 4)
			{
				String enables = planeVars.get(PlanarVar.ENABLE.toString());
				if(enables!=null)
				{
					List<Pair<String,String>> enableAs=CMParms.parseSpaceParenList(enables);
					for(Pair<String,String> P : enableAs)
					{
						Ability A1=CMClass.getAbility(P.first);
						if(A1!=null)
							tidbits.add(L("You'll need to worry about creatures using @x1 there.",A1.name()));
						else
							tidbits.add(L("You'll need to worry about creatures using their @x1 powers against you there.",P.first.toLowerCase()));
					}
				}
				String bonusDamageStat = planeVars.get(PlanarVar.BONUSDAMAGESTAT.toString());
				if(bonusDamageStat!=null)
				{
					tidbits.add(L("Those with high @x1 are more deadly there.",bonusDamageStat.toLowerCase()));
				}
			}
			if(expertise > 5)
			{
				int eliteLevel=0;
				if(planeVars.containsKey(PlanarVar.ELITE.toString()))
					eliteLevel=CMath.s_int(planeVars.get(PlanarVar.ELITE.toString()));
				if(eliteLevel == 1)
					tidbits.add(L("Creatures there will be more powerful than you are."));
				else
				if(eliteLevel > 1)
					tidbits.add(L("Creatures there will be MUCH more powerful than you are."));
				String reqWeapons = planeVars.get(PlanarVar.REQWEAPONS.toString());
				if(reqWeapons != null)
				{
					for(String weap : CMParms.parseSpaces(reqWeapons, true))
						tidbits.add(L("Creatures there are almost unkillable, but @x1 weapons will hurt them.",weap.toLowerCase()));
				}
			}
			if(expertise > 6)
			{
				String absorbStr = planeVars.get(PlanarVar.ABSORB.toString());
				if(absorbStr != null)
				{
					Ability A1=CMClass.getAbility("Prop_AbsorbDamage");
					String stats=A1.getStat("TIDBITS="+absorbStr);
					List<String> statV=CMParms.parseAny(stats, "\n\r", true);
					for(String stat : statV)
						tidbits.add(L("The creatures there @x1.",stat));
				}
			}
			if(expertise > 7)
			{
				String hours = planeVars.get(PlanarVar.HOURS.toString());
				if((hours != null)&&(CMath.isInteger(hours)))
					tidbits.add(L("That plane has @x1 hour days.",hours));
				String alignNumStr = planeVars.get(PlanarVar.ALIGNMENT.toString());
				if((alignNumStr != null)&&(CMath.isInteger(alignNumStr)))
				{
					Faction F=CMLib.factions().getFaction(CMLib.factions().AlignID());
					if(F!=null)
					{
						String rangeName = F.fetchRangeName(CMath.s_int(alignNumStr));
						tidbits.add(L("The creatures there are @x1 aligned.",rangeName));
					}
				}
			}
			if(expertise > 8)
			{
				String numStr = planeVars.get(PlanarVar.RECOVERRATE.toString());
				if((numStr != null)&&(CMath.isInteger(numStr)))
				{
					int num=CMath.s_int(numStr);
					tidbits.add(L("Everything there recovers @x1 times faster.",""+(num+1)));
				}
				numStr = planeVars.get(PlanarVar.FATIGUERATE.toString());
				if((numStr != null)&&(CMath.isInteger(numStr)))
				{
					int num=CMath.s_int(numStr);
					tidbits.add(L("Everything there becomes fatigued @x1 times faster.",""+(num+1)));
				}
				
			}
			if(expertise > 9)
			{
				String areablurbs = planeVars.get(PlanarVar.AREABLURBS.toString());
				if(areablurbs != null)
				{
					Map<String,String> blurbSets=CMParms.parseEQParms(areablurbs);
					for(String key : blurbSets.keySet())
						tidbits.add(blurbSets.get(key));
				}
			}
			if(tidbits.size()==0)
			{
				if(report)
					CMLib.commands().postSay(mob, L("I know almost nothing about that plane of existence.  I guess it's not my area of Expertise. "));
				else
					mob.tell(L("You know almost nothing about that plane of existence.  I guess it's not your area of Expertise. "));
			}
			else
			{
				final String str=tidbits.get(CMLib.dice().roll(1, tidbits.size(), -1));
				if(report)
					CMLib.commands().postSay(mob, L("I recall that @x1",Character.toLowerCase(str.charAt(0))+str.substring(1)));
				else
					mob.tell(L("You recall that @x1",Character.toLowerCase(str.charAt(0))+str.substring(1)));
			}
		}
		else
			mob.location().show(mob,null,this,CMMsg.MSG_THINK,L("<S-NAME> get(s) frustrated over having forgotten something."));
		return success;
	}

}
