package com.planet_ink.coffee_mud.Abilities.Fighter;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2023-2024 Bo Zimmerman

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
public class Fighter_CallSteed extends StdAbility
{
	@Override
	public String ID()
	{
		return "Fighter_CallSteed";
	}

	private final static String localizedName = CMLib.lang().L("Call Steed");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return L("(Steed)");
	}

	private static final String[] triggerStrings = I(new String[] { "CALLSTEED" });

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_ANIMALAFFINITY;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_SUMMONING;
	}

	@Override
	public void unInvoke()
	{
		final MOB mob = (MOB) affected;
		super.unInvoke();
		if ((canBeUninvoked()) && (mob != null))
		{
			if (mob.amDead())
				mob.setLocation(null);
			else
			if ((mob.location() != null)&&(CMLib.flags().isInTheGame(mob, true)))
				CMLib.tracking().wanderAway(mob, false, false);
			mob.destroy();
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if (tickID == Tickable.TICKID_MOB)
		{
			if ((affected instanceof MOB) && (invoker != null))
			{
				final MOB mob = (MOB) affected;
				if ((mob.amFollowing() == null)
				|| (mob.amDead())
				|| (mob.location() == null)
				|| ((invoker != null)
					&& ((mob.location() != invoker.location())
						|| (!CMLib.flags().isInTheGame(invoker, true))
						|| ((invoker.riding() instanceof MOB) && (invoker.riding() != affected)))))
				{
					mob.delEffect(this);
					if (mob.amDead())
						mob.setLocation(null);
					mob.destroy();
				}
			}
		}
		return super.tick(ticking, tickID);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if (affected instanceof MOB)
		{
			if((msg.sourceMinor() == CMMsg.TYP_QUIT)
			&& (msg.amISource((MOB) affected)
				|| msg.amISource(((MOB) affected).amFollowing())
				|| (msg.source() == invoker()))
			)
			{
				unInvoke();
				if (msg.source().playerStats() != null)
					msg.source().playerStats().setLastUpdated(0);
			}
			if((msg.target()==affected)
			&&(msg.tool() instanceof Ability)
			&&	((msg.amISource(((MOB) affected).amFollowing()))
				|| (msg.source() == invoker()))
			&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
			&&(msg.tool().ID().equals("Chant_AnimalCompanion")))
			{
				final MOB invoker = invoker();
				final MOB mob = (MOB)affected;
				if((invoker!=null)
				&& (mob!=null)
				&& (mob.amFollowing()==invoker))
				{
					CMLib.threads().scheduleRunnable(new Runnable() {
						final MOB iM = invoker;
						final MOB M = mob;
						@Override
						public void run()
						{
							if(!iM.isFollowedBy(mob))
							{
								final Fighter_CallSteed realA = (Fighter_CallSteed)iM.fetchAbility(ID());
								if((realA != null)
								&&(realA.lastSteedM==M))
								{
									for(int f = 0;f<iM.numFollowers();f++)
									{
										final MOB F = iM.fetchFollower(f);
										if((F!=null)
										&&(F.Name().equals(M.Name()))
										&&(F.baseCharStats().getMyRace()==M.baseCharStats().getMyRace()))
											realA.lastSteedM=F;
									}
								}
							}
						}
					}, 800);
				}
				affected.delEffect(this);
			}
		}
	}

	protected final static Pair<String,Race> defaultMount = new Pair<String,Race>("Equine",CMClass.getRace("Horse"));

	protected static Pair<String,Race> getMountChoice(final MOB mob, final String aID)
	{
		final Ability A = mob.fetchEffect(aID);
		if (A != null)
		{
			final List<String> aP = CMParms.parseCommas(A.text(), true);
			if (aP.size() == 2)
			{
				final Race R = CMClass.getRace(aP.get(1));
				final String cat = aP.get(0);
				return new Pair<String,Race>(cat,R);
			}
		}
		return null;
	}

	protected volatile MOB	lastSteedM	= null;
	protected List<MOB>		favored		= null;

	protected MOB getLastSteed(final MOB mob)
	{
		getNames(mob);
		final MOB steedM = lastSteedM;
		if (steedM != null)
		{
			if ((!steedM.amDead())
			&& (!steedM.amDestroyed())
			&& (CMLib.flags().isInTheGame(steedM, true)))
				return steedM;
			this.lastSteedM = null;
		}
		return null;
	}

	protected List<MOB> getNames(final MOB mob)
	{
		if (favored == null)
		{
			favored = new Vector<MOB>();
			if (super.text().trim().startsWith("<MOBS>"))
			{
				final List<XMLLibrary.XMLTag> pieces = CMLib.xml().parseAllXML(super.text());
				CMLib.coffeeMaker().addMOBsFromXML(pieces, favored, null);
				for (final XMLLibrary.XMLTag tag : pieces)
				{
					if (tag.tag().equals("LAST"))
					{
						final String val = CMLib.xml().restoreAngleBrackets(tag.value());
						if ((favored.size() > 0)
						&& (favored.get(favored.size() - 1).Name().equals(val)))
						{
							final MOB lastM = favored.remove(favored.size() - 1);
							if(mob != null)
							{
								for (int f = 0; f < mob.numFollowers(); f++)
								{
									final MOB fM = mob.fetchFollower(f);
									if ((fM!=null)
									&&(fM.baseCharStats().getMyRace()==lastM.baseCharStats().getMyRace()))
										this.lastSteedM = fM;
								}
								if(lastM != this.lastSteedM)
									lastM.destroy();
							}
						}
						break;
					}
				}
			}
		}
		return favored;
	}

	public boolean isValidRoom(final Race raceR, final Room R)
	{
		if (raceR.getBreathables().length > 0)
		{
			if (!CMParms.contains(raceR.getBreathables(), R.getAtmosphere()))
				return false;
		}
		if ((R.getAtmosphere() == RawMaterial.RESOURCE_AIR) && ((R.domainType() & Room.INDOORS) > 0))
			return false;
		if((!raceR.racialCategory().equals("Avian"))
		&& (R.domainType()==Room.DOMAIN_OUTDOORS_AIR))
			return false;
		return true;
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
	}

	@Override
	public String text()
	{
		if(favored == null)
			return super.text();
		final StringBuilder str = new StringBuilder("<MOBS>");
		/*
		for (final MOB M : favored)
		{
			if((M != null)
			&& (!M.amDead())
			&& (!M.amDestroyed())
			&& ((lastSteedM == null)
				|| (M.baseCharStats().getMyRace() != lastSteedM.baseCharStats().getMyRace())))
				str.append(CMLib.coffeeMaker().getMobXML(M));
		}
		*/
		final MOB M = getLastSteed(null);
		if((M != null)
		&& (!M.amDead())
		&& (!M.amDestroyed()))
		{
			str.append(CMLib.coffeeMaker().getMobXML(M));
			str.append("</MOBS>");
			if (M != null)
				str.append("<LAST>").append(CMLib.xml().parseOutAngleBrackets(M.Name())).append("</LAST>");
		}
		else
			str.append("</MOBS>");
		return str.toString();
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		super.setAffectedOne(P);
	}

	protected boolean isMySteed(final MOB mob, final MOB steedM)
	{
		if((steedM==null) || (mob==null))
			return false;
		final Ability A = steedM.fetchEffect(ID());
		if((A !=null)
		&& (A.invoker()==mob))
			return true;
		final MOB lastM = this.lastSteedM;
		if(lastM != null)
		{
			if(lastM.baseCharStats().getMyRace()==steedM.baseCharStats().getMyRace())
				return true;
		}
		return false;
	}

	protected boolean dismissLastSteed(final MOB mob, final MOB lastSteedM)
	{
		if (lastSteedM != null)
		{
			final Ability A = lastSteedM.fetchEffect(ID());
			if (A != null)
			{
				A.unInvoke();
				final Room lastSteedR = lastSteedM.location();
				if((lastSteedR != null)&&(CMLib.flags().isAliveAwakeMobileUnbound(mob, true)))
					lastSteedR.show(lastSteedM, null, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> wander(s) off."));
				CMLib.tracking().wanderAway(lastSteedM, false, false);
			}
			lastSteedM.destroy();
			this.lastSteedM = null;
			return true;
		}
		return false;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		Race raceR = null;
		MOB pickedM = null;
		final PairList<String,Race> choices = CMLib.utensils().getFavoredMounts(mob);
		final MOB lastSteedM = getLastSteed(mob);
		if (commands.size() == 0)
		{
			final Pair<String,Race> choice = Fighter_CallSteed.getMountChoice(mob, "Fighter_RacialMount");
			if (choice != null)
				raceR = choice.second;
			else
			{
				mob.tell(L("You have no default choices available.  Try LIST."));
				return false;
			}
		}
		else
		{
			final Map<String, Object> trueChoices = new HashMap<String, Object>();
			for (final Pair<String, Race> p : choices)
			{
				if ((lastSteedM == null) || (lastSteedM.baseCharStats().getMyRace() != p.second))
				{
					boolean found = false;
					for (final MOB M : this.getNames(mob))
						found = (M.baseCharStats().getMyRace()==p.second) || found;
					if(!found)
						trueChoices.put(p.second.name(), p);
				}
			}
			for (final MOB M : this.getNames(mob))
			{
				if ((lastSteedM == null) || (lastSteedM.baseCharStats().getMyRace() != M.baseCharStats().getMyRace()))
					trueChoices.put(M.Name(), M);
			}

			if (commands.get(0).equalsIgnoreCase("list"))
			{
				final StringBuilder str = new StringBuilder("^HSteed Choices: ^N\n\r");
				for (final String name : trueChoices.keySet())
				{
					final Object o = trueChoices.get(name);
					if (o instanceof Pair)
					{
						@SuppressWarnings("unchecked")
						final Pair<String, Race> p = (Pair<String, Race>) o;
						str.append("^w"+CMStrings.padRight(p.second.name(), 10) + "^N (" + p.first + ")\n\r");
					}
					else
					if (o instanceof MOB)
						str.append("^w"+CMStrings.padRight(((MOB) o).Name(), 10) + "^N (" + ((MOB) o).baseCharStats().getMyRace().racialCategory() + ")\n\r");
				}
				if(lastSteedM != null)
					str.append("^k*"+CMStrings.padRight(lastSteedM.Name(), 10) + " (" + lastSteedM.baseCharStats().getMyRace().racialCategory() + ")\n\r");
				mob.tell(str.toString());
				return false;
			}
			else if (commands.get(0).equalsIgnoreCase("dismiss"))
			{
				if (!dismissLastSteed(mob, lastSteedM))
					mob.tell(L("You have no called steeds to dismiss."));
				return false;
			}
			else
			{
				final String choice = CMParms.combine(commands, 0);
				for (final String name : trueChoices.keySet())
				{
					if (CMLib.english().containsString(name, choice))
					{
						final Object o = trueChoices.get(name);
						if (o instanceof Pair)
						{
							@SuppressWarnings("unchecked")
							final Pair<String, Race> p = (Pair<String, Race>) o;
							raceR = p.second;
							break;
						}
						else if (o instanceof MOB)
						{
							final MOB M = (MOB) o;
							raceR = M.baseCharStats().getMyRace();
							pickedM = M;
							break;
						}
					}
				}
				if (raceR == null)
				{
					mob.tell(L("'@x1' is not a valid choice.  Try LIST.", choice));
					return false;
				}

			}
		}
		final Room R = mob.location();
		if (raceR.getBreathables().length > 0)
		{
			if (!CMParms.contains(raceR.getBreathables(), R.getAtmosphere()))
			{
				mob.tell(L("This does not seem like a good place to call on your @x1.", raceR.name().toLowerCase()));
				return false;
			}
		}
		if ((R.getAtmosphere() == RawMaterial.RESOURCE_AIR) && ((R.domainType() & Room.INDOORS) > 0))
		{
			mob.tell(L("You must be outdoors to call your @x1.", raceR.name().toLowerCase()));
			return false;
		}

		final List<Integer> entryDirChoices = new ArrayList<Integer>();
		int fromDir = -1;
		for (int d = Directions.NUM_DIRECTIONS() - 1; d >= 0; d--)
		{
			final Room room = R.getRoomInDir(d);
			final Exit exit = R.getExitInDir(d);
			final Exit opExit = R.getReverseExit(d);
			if((room != null)
			&& (isValidRoom(raceR, room))
			&& ((exit != null)
			&& (exit.isOpen()))
			&& (opExit != null)
			&& (opExit.isOpen()))
				entryDirChoices.add(Integer.valueOf(d));
		}
		if (entryDirChoices.size() == 0)
		{
			mob.tell(L("You must be further outdoors to call your @x1.", raceR.name().toLowerCase()));
			return false;
		}
		fromDir = entryDirChoices.get(CMLib.dice().roll(1, entryDirChoices.size(), -1)).intValue();
		final Room newRoom = R.getRoomInDir(fromDir);
		final int opDir = R.getReverseDir(fromDir);
		if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
			return false;

		final boolean success = proficiencyCheck(mob, 0, auto);

		if((success)
		&& (newRoom != null))
		{
			invoker = mob;
			final CMMsg msg = CMClass.getMsg(mob, null, this, CMMsg.MSG_NOISYMOVEMENT, auto ? "" : L("<S-NAME> whistle(s) for <S-HIS-HER> steed."));
			if (R.okMessage(mob, msg))
			{
				R.send(mob, msg);

				dismissLastSteed(mob, lastSteedM);

				final MOB target;
				if (pickedM != null)
				{
					target = pickedM;
					this.getNames(mob).remove(pickedM);
				}
				else
				{
					target = determineMonster(mob, raceR, adjustedLevel(mob, asLevel));
				}
				target.bringToLife(newRoom, true);
				CMLib.beanCounter().clearZeroMoney(target, null);
				if (target.fetchEffect(ID())==null)
				{
					final Ability thisA = (Ability) copyOf();
					target.addNonUninvokableEffect(thisA);
					thisA.setSavable(true);
					thisA.setInvoker(mob);
				}
				target.setMoneyVariation(0);
				target.location().showOthers(target, null, CMMsg.MSG_OK_ACTION, L("<S-NAME> appears!"));
				newRoom.recoverRoomStats();
				target.setStartRoom(null);
				if (target.isInCombat())
					target.makePeace(true);
				CMLib.tracking().walk(target, opDir, false, false);
				if (target.location() == R)
				{
					if (target.isInCombat())
						target.makePeace(true);
					CMLib.commands().postFollow(target, mob, true);
					if (target.amFollowing() != mob)
						mob.tell(L("@x1 seems unwilling to follow you.", target.name(mob)));
				}
				this.lastSteedM = target;
				invoker = mob;
			}
		}
		else
			return beneficialWordsFizzle(mob, null, L("<S-NAME> whistle(s) for <S-HIS-HER> steed, but <S-IS-ARE> not answered."));

		// return whether it worked
		return success;
	}

	public MOB determineMonster(final MOB caster, final Race R, final int level)
	{

		final MOB newMOB = CMClass.getMOB("GenRideable");
		final Rideable ride = (Rideable) newMOB;
		newMOB.basePhyStats().setAbility(CMProps.getMobHPBase());// normal
		newMOB.basePhyStats().setLevel(level);
		newMOB.basePhyStats().setWeight(500);
		final int aF = caster.fetchFaction(CMLib.factions().getAlignmentID());
		final int iF = caster.fetchFaction(CMLib.factions().getInclinationID());
		if(aF != Integer.MAX_VALUE)
			newMOB.addFaction(CMLib.factions().getAlignmentID(), aF);
		if(iF != Integer.MAX_VALUE)
			newMOB.addFaction(CMLib.factions().getInclinationID(), iF);
		newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		newMOB.baseCharStats().setMyRace(R);
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER, caster.charStats().getStat(CharStats.STAT_GENDER) == 'M' ? 'M' : 'F');
		newMOB.baseCharStats().getMyRace().startRacing(newMOB, false);
		newMOB.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
		newMOB.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
		newMOB.basePhyStats().setSpeed(CMLib.leveler().getLevelMOBSpeed(newMOB));
		newMOB.basePhyStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
		newMOB.setName(L(CMLib.english().startWithAorAn(R.name().toLowerCase())));
		newMOB.setDisplayText(L("@x1 is here", CMStrings.capitalizeAndLower(newMOB.name())));
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience", "0 RIDEOK"));
		newMOB.addTattoo("SYSTEM_SUMMONED");
		newMOB.addTattoo("SUMMONED_BY:"+caster.name());
		ride.setRiderCapacity(3);
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.text();
		return (newMOB);
	}
}
