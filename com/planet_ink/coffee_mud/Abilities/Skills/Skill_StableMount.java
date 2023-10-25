package com.planet_ink.coffee_mud.Abilities.Skills;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.Rideable.Basis;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.Follower;
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
   Copyright 2023-2023 Bo Zimmerman

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
public class Skill_StableMount extends StdAbility
{
	@Override
	public String ID()
	{
		return "Skill_StableMount";
	}

	private final static String localizedName = CMLib.lang().L("Stable Mount");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings = I(new String[] { "STABLEMOUNT", "STABLE" });

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
		return 0;
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
		return 0;
	}

	protected Map<String, List<MOB>>	stables		= null;

	protected List<MOB> getLocalStable(final MOB mob, final Area A)
	{
		if(A==null)
			return new ArrayList<MOB>(1);
		List<MOB> stable = null;
		if (stables != null)
		{
			stable = stables.get(A.Name());
			if(stable == null)
			{
				stable = new Vector<MOB>();
				stables.put(A.Name(), stable);
			}
			return stable;
		}
		else
			stables = new Hashtable<String,List<MOB>>();
		if (!super.text().trim().startsWith("<STABLE>"))
		{
			stable = new Vector<MOB>();
			stables.put(A.Name(), stable);
			return stable;
		}
		final List<XMLLibrary.XMLTag> pieces = CMLib.xml().parseAllXML(super.text());
		for (final XMLLibrary.XMLTag tag : pieces)
		{
			if(!tag.tag().equalsIgnoreCase("STABLE"))
				continue;
			final String area = tag.getParmValue("AREA");
			if(area == null)
				continue;
			stable = new Vector<MOB>();
			final List<XMLLibrary.XMLTag> mobs = CMLib.xml().getPiecesFromPieces(tag.contents(), "MOBS");
			if(mobs == null)
				continue;
			stables.put(CMLib.xml().restoreAngleBrackets(area), stable);
			CMLib.coffeeMaker().addMOBsFromXML(mobs, stable, null);
			for(final MOB M : stable)
				M.setLocation(null);
		}
		return stables.get(A.Name());
	}

	public boolean isValidRoom(final Race raceR, final Room R)
	{
		if (raceR.getBreathables().length > 0)
		{
			if (!CMParms.contains(raceR.getBreathables(), R.getAtmosphere()))
				return false;
		}
		if ((R.getAtmosphere() == RawMaterial.RESOURCE_AIR)
		&& ((R.domainType() & Room.INDOORS) > 0))
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
		if (stables == null)
			return super.text();
		else
		{
			final StringBuilder str = new StringBuilder("");
			for (final String area : stables.keySet())
			{
				str.append("<STABLE AREA=\""+CMLib.xml().parseOutAngleBrackets(area)+"\"><MOBS>");
				final List<MOB> mobs = stables.get(area);
				for(final MOB M : mobs)
				{
					if((M != null)
					&& (!M.amDead())
					&& (!M.amDestroyed()))
						str.append(CMLib.coffeeMaker().getMobXML(M));
				}
				str.append("</MOBS></STABLE>");
			}
			return str.toString();
		}
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		super.setAffectedOne(P);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room R = mob.location();
		if(!CMLib.law().isACity(R.getArea())
		&&(!CMLib.law().doesHavePriviledgesHere(mob, R))
		&&(!auto))
		{
			mob.tell(L("There are no stables around here."));
			return false;
		}
		final Map<String, MOB> trueChoices = new HashMap<String, MOB>();
		final List<MOB> localStable = this.getLocalStable(mob, R.getArea());
		for (final MOB M : localStable)
			trueChoices.put(M.Name(), M);
		if (commands.get(0).equalsIgnoreCase("list"))
		{
			final StringBuilder str = new StringBuilder(L("^H@x1 Stable: ^N\n\r",R.getArea().Name()));
			for (final String name : trueChoices.keySet())
			{
				final MOB M = trueChoices.get(name);
				str.append("^w"+CMStrings.padRight(M.Name(), 10) + "^N (" + M.baseCharStats().getMyRace().racialCategory() + ")\n\r");
			}
			mob.tell(str.toString());
			return false;
		}
		MOB pickedM = null;
		final String choice = CMParms.combine(commands, 0);
		for(final Enumeration<Pair<MOB,Short>> f = mob.followers();f.hasMoreElements();)
		{
			final MOB M = f.nextElement().first;
			if((M!=null)
			&&(M.location()==R)
			&&(M.name().equals(choice)))
			{
				pickedM = M;
				break;
			}
		}
		if((pickedM == null)
		||(trueChoices.containsKey(pickedM.Name())))
		{
			for (final String name : trueChoices.keySet())
			{
				if (name.equalsIgnoreCase(choice))
				{
					pickedM = trueChoices.get(name);
					break;
				}
			}
		}
		if(pickedM == null)
		{
			for(final Enumeration<Pair<MOB,Short>> f = mob.followers();f.hasMoreElements();)
			{
				final MOB M = f.nextElement().first;
				if((M!=null)
				&&(M.location()==R)
				&&(CMLib.english().containsString(M.name(), choice)))
				{
					pickedM = M;
					break;
				}
			}
		}
		if(pickedM == null)
		{
			for (final String name : trueChoices.keySet())
			{
				if (CMLib.english().containsString(name, choice))
				{
					pickedM = trueChoices.get(name);
					break;
				}
			}
		}
		if (pickedM == null)
		{
			mob.tell(L("'@x1' is not a valid choice.  Try LOOK or STABLE LIST.", choice));
			return false;
		}
		if(CMLib.flags().isInTheGame(pickedM, true))
		{
			if((!(pickedM instanceof Rideable))
			||(((Rideable)pickedM).rideBasis() != Basis.LAND_BASED)
			||(!CMLib.flags().isAnimalIntelligence(pickedM))
			||(!pickedM.isMonster()))
			{
				mob.tell(L("@x1 is not elligible to be stabled.",pickedM.name(mob)));
				return false;
			}
			if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
				return false;
			final boolean success = proficiencyCheck(mob, 0, auto);
			if(success)
			{
				invoker = mob;
				final CMMsg msg = CMClass.getMsg(mob, pickedM, this, CMMsg.MSG_NOISE,
						auto ? "" : L("<S-NAME> whistle(s) for a stable boy to pick up <T-NAME>."));
				if (R.okMessage(mob, msg))
				{
					R.send(mob, msg);
					final MOB target = pickedM;
					pickedM.setFollowing(null);
					mob.delFollower(pickedM);
					for(final Enumeration<Pair<MOB,Short>> f = pickedM.followers();f.hasMoreElements();)
						pickedM.delFollower(f.nextElement().first);
					target.location().showOthers(target, null, CMMsg.MSG_OK_ACTION, L("<S-NAME> <S-IS-ARE> taken away to a stable."));
					CMLib.tracking().wanderAway(pickedM, false, false);
					CMLib.threads().suspendResumeRecurse(pickedM, false, true);
					localStable.add(pickedM);
					final Room pickedR = pickedM.location();
					pickedR.delInhabitant(pickedM);
					invoker = mob;
				}
			}
			else
				return beneficialWordsFizzle(mob, null, L("<S-NAME> whistle(s) for a stable boy, but <S-IS-ARE> not answered."));
			return success;
		}

		if (pickedM.charStats().getMyRace().getBreathables().length > 0)
		{
			if (!CMParms.contains(pickedM.charStats().getMyRace().getBreathables(), R.getAtmosphere()))
			{
				mob.tell(L("This does not seem like a good place to bring in @x1.", pickedM.name(mob)));
				return false;
			}
		}
		if ((R.getAtmosphere() == RawMaterial.RESOURCE_AIR)
		&& ((R.domainType() & Room.INDOORS) > 0))
		{
			mob.tell(L("You must be outdoors to have @x1 brought in.", pickedM.name(mob)));
			return false;
		}

		if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
			return false;

		final boolean success = proficiencyCheck(mob, 0, auto);

		if(success)
		{
			invoker = mob;
			final CMMsg msg = CMClass.getMsg(mob, pickedM, this, CMMsg.MSG_NOISE,
					auto ? "" : L("<S-NAME> whistle(s) for a stable boy to bring in <T-NAME>."));
			if (R.okMessage(mob, msg))
			{
				R.send(mob, msg);
				final MOB target = pickedM;
				if(target.location()==null)
					target.bringToLife(R, true);
				else
				{
					CMLib.threads().suspendResumeRecurse(target, true, false);
					R.bringMobHere(target, false);
				}
				CMLib.beanCounter().clearZeroMoney(target, null);
				target.setMoneyVariation(0);
				target.location().showOthers(target, null, CMMsg.MSG_OK_ACTION, L("<S-NAME> is brought in."));
				target.setStartRoom(null);
				if (target.isInCombat())
					target.makePeace(true);
				CMLib.commands().postFollow(target, mob, true);
				if (target.amFollowing() != mob)
					mob.tell(L("@x1 seems unwilling to follow you.", target.name(mob)));
				localStable.remove(pickedM);
				invoker = mob;
			}
		}
		else
			return beneficialWordsFizzle(mob, null, L("<S-NAME> whistle(s) for a stable boy, but <S-IS-ARE> not answered."));

		// return whether it worked
		return success;
	}
}
