package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.PlanarAbility.PlanarVar;
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
public class Spell_Imprisonment extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Imprisonment";
	}

	private final static String localizedName = CMLib.lang().L("Imprisonment");

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
	public long flags()
	{
		return Ability.FLAG_TRANSPORTING;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int overrideMana()
	{
		return 200;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_COSMOLOGY;
	}

	protected volatile Room prevRoom = null;

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(mob.isMonster())
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected instanceof MOB)
		&&(((MOB)affected).isMonster()))
		{
			final MOB mob=(MOB)affected;
			if(msg.source().getVictim()==mob)
				msg.source().setVictim(null);
			if(mob.isInCombat())
			{
				final MOB victim=mob.getVictim();
				if(victim!=null)
					victim.makePeace(true);
				mob.makePeace(true);
			}
			mob.recoverMaxState();
			mob.resetToMaxState();
			mob.curState().setHunger(1000);
			mob.curState().setThirst(1000);
			mob.recoverCharStats();
			mob.recoverPhyStats();

			// when this spell is on a MOBs Affected list,
			// it should consistantly prevent the mob
			// from trying to do ANYTHING except sleep
			if(msg.amISource(mob))
			{
				if((!msg.sourceMajor(CMMsg.MASK_ALWAYS))
				&&(msg.sourceMajor()>0))
				{
					mob.tell(L("Statues can't do that."));
					return false;
				}
			}
		}
		if(!super.okMessage(myHost,msg))
			return false;

		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if(msg.source().getVictim()==affected)
				msg.source().setVictim(null);
			if(mob.isInCombat())
			{
				final MOB victim=mob.getVictim();
				if(victim!=null)
					victim.makePeace(true);
				mob.makePeace(true);
			}
		}
		return true;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((affected instanceof MOB)
		&&(((MOB)affected).isMonster()))
		{
			//affectableStats.setReplacementName("a statue of "+affected.name());
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_MOVE);
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_HEAR);
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_SMELL);
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_SPEAK);
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_TASTE);
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_NOT_SEEN);
		}
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				mob.tell(L("Your imprisonment expires."));
				if((prevRoom != null)
				&&(CMLib.flags().getPlaneOfExistence(mob)!=null))
				{
					prevRoom.bringMobHere(mob, true);
					if(mob.isPlayer())
						CMLib.commands().postLook(mob, true);
				}
			}
		}
		super.unInvoke();
	}

	protected int getPlanarDiff(final String planeName, final boolean doAlignment, final int alignment, final boolean doInclination, final int inclination)
	{
		int diff=0;
		final PlanarAbility A=(PlanarAbility)CMClass.getAbility("StdPlanarAbility");
		final Map<String,String> vars = A.getPlanarVars(planeName);
		if(doAlignment && vars.containsKey(PlanarVar.ALIGNMENT.name()))
		{
			final int aval = CMath.s_int(vars.get(PlanarVar.ALIGNMENT.name()));
			diff += CMath.abs(alignment - aval);
		}
		if(doInclination)
		{
			A.setMiscText(planeName);
			if(A.getFactionList() != null)
			{
				for(final Iterator<Pair<String,String>> k = A.getFactionList().iterator();k.hasNext();)
				{
					final Pair<String,String> p=k.next();
					final String key=p.first;
					if(key.equals("*"))
						continue;
					Faction F=null;
					if(CMLib.factions().isFactionID(key))
						F=CMLib.factions().getFaction(key);
					if(F==null)
						F=CMLib.factions().getFactionByName(key);
					if((F!=null)&&(F.factionID().equalsIgnoreCase(CMLib.factions().getInclinationID())))
					{
						final int aval = CMath.s_int(p.second);
						diff += CMath.abs(inclination - aval);
						break;
					}
				}
			}
		}
		return diff;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final Room R=CMLib.map().roomLocation(target);
		final boolean success=proficiencyCheck(mob,0,auto);

		if((success)
		&&(R!=null)
		&&(CMLib.flags().getPlaneOfExistence(R)==null))
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?"":L("^S<S-NAME> swipe(s) <S-HIS-HER> hands at <T-NAMESELF>.^?"));
			final CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_SOMANTIC|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),null);
			final CMMsg msg3=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_SOMANTIC|CMMsg.TYP_GENERAL|(auto?CMMsg.MASK_ALWAYS:0),null);
			if((R.okMessage(mob,msg))
			&&(R.okMessage(mob,msg2))
			&&(R.okMessage(mob,msg3)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				mob.location().send(mob,msg3);
				if(msg.value()<=0)
				{
					final Faction inclinationF=CMLib.factions().getFaction(CMLib.factions().getInclinationID());
					final boolean doInclination = (inclinationF!=null)
											&& (target.fetchFaction(CMLib.factions().getInclinationID())!=Integer.MAX_VALUE);
					final boolean doAlignment = (CMLib.factions().getFaction(CMLib.factions().getAlignmentID())!=null)
											&& (target.fetchFaction(CMLib.factions().getAlignmentID())!=Integer.MAX_VALUE);
					final int inclination = (doInclination)?target.fetchFaction(CMLib.factions().getInclinationID()):0;
					final int alignment = (doAlignment)?target.fetchFaction(CMLib.factions().getAlignmentID()):0;
					final PlanarAbility A=(PlanarAbility)CMClass.getAbility("StdPlanarAbility");
					int biggestAlignmentDiffs = 0;
					String biggestDiffPlane = null;
					for(final String planeName : A.getAllPlaneKeys())
					{
						final List<String> cats = A.getCategories();
						if((cats==null)
						||(!CMParms.containsIgnoreCase(cats,"Outer")))
							continue;
						final int diff = this.getPlanarDiff(planeName, doAlignment, alignment, doInclination, inclination);
						if(diff > biggestAlignmentDiffs)
						{
							biggestAlignmentDiffs = diff;
							biggestDiffPlane = planeName;
						}
					}
					if(biggestDiffPlane == null)
					{
						for(final String planeName : A.getAllPlaneKeys())
						{
							final int diff = this.getPlanarDiff(planeName, doAlignment, alignment, doInclination, inclination);
							if((diff > biggestAlignmentDiffs)
							||(biggestDiffPlane == null))
							{
								biggestAlignmentDiffs = diff;
								biggestDiffPlane = planeName;
							}
						}
					}
					if(biggestDiffPlane!=null)
					{
						final Vector<String> V=new XVector<String>(biggestDiffPlane);
						target.location().show(target,null,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> <S-IS-ARE> being drawn into @x1.",biggestDiffPlane));
						if(target.isMonster())
						{
							final Spell_Imprisonment aP = (Spell_Imprisonment)super.maliciousAffect(mob, target, asLevel, 0, -1);
							if(aP != null)
							{
								target.location().show(target,null,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> fade(s) away."));
								aP.prevRoom = R;
							}
						}
						else
						{
							A.invoke(target, V, target, true, asLevel);
							if(biggestDiffPlane.equalsIgnoreCase(CMLib.flags().getPlaneOfExistence(target)))
							{
								final Spell_Imprisonment aP = (Spell_Imprisonment)super.maliciousAffect(mob, target, asLevel, 0, -1);
								if(aP != null)
								{
									aP.prevRoom = R;
									final Area planeA=CMLib.map().areaLocation(target);
									if((planeA!=null)
									&&(planeA.numEffects()>0))
									{
										for(final Enumeration<Ability> a=planeA.effects();a.hasMoreElements();)
										{
											final Ability eA=a.nextElement();
											if(eA instanceof PlanarAbility)
											{
												final int tickDowns = CMath.s_int(eA.getStat("TICKDOWN"));
												if((tickDowns > 0)&&(tickDowns < CMath.s_int(aP.getStat("TICKDOWN"))))
													eA.setStat("TICKDOWN", aP.getStat("TICKDOWN"));
											}
										}
									}
								}
							}
							else
								return maliciousFizzle(mob,target,L("<S-NAME> swipe(s) <S-HIS-HER> hands at <T-NAMESELF>, but the spell fades."));
						}
					}
					else
						return maliciousFizzle(mob,target,L("<S-NAME> swipe(s) <S-HIS-HER> hands at <T-NAMESELF>, but the spell fades."));
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> swipe(s) <S-HIS-HER> hands at <T-NAMESELF>, but the spell fails."));
		// return whether it worked
		return success;
	}
}
