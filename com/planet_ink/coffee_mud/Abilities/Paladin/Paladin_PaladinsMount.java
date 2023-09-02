package com.planet_ink.coffee_mud.Abilities.Paladin;
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
   Copyright 2014-2023 Bo Zimmerman

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
public class Paladin_PaladinsMount extends PaladinSkill
{
	@Override
	public String ID()
	{
		return "Paladin_PaladinsMount";
	}

	private final static String localizedName = CMLib.lang().L("Paladin`s Mount");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_ANIMALAFFINITY;
	}

	protected boolean pass=false;
	protected static final long allGAlignFlags = Ability.FLAG_HOLY   + Ability.FLAG_LAW;
	protected static final long allEAlignFlags = Ability.FLAG_UNHOLY + Ability.FLAG_CHAOS;
	protected static final long allAlignFlags  = allGAlignFlags      + allEAlignFlags;

	public Paladin_PaladinsMount()
	{
		super();
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		final Physical affected=super.affected;
		final MOB invoker = super.invoker();
		if((affected instanceof Rideable)
		&&(affected instanceof MOB)
		&&(invoker!=null)
		&&(affected != invoker))
		{
			final MOB affectedMob=(MOB)affected;
			if((!((Rideable)affected).amRiding(invoker))
			||(affectedMob.amDead())
			||(!appropriateToMyFactions(invoker)))
			{
				affected.delEffect(this);
				affected.recoverPhyStats();
				affectedMob.recoverCharStats();
				affectedMob.recoverMaxState();
			}
		}
		else
		if((affected == invoker)
		&&(invoker!=null)
		&&(appropriateToMyFactions(invoker))
		&&(invoker.riding()!=null)
		&&(invoker.riding() instanceof MOB)
		&&(invoker.riding().fetchEffect(ID())==null))
		{
			final PrivateProperty P = CMLib.law().getPropertyRecord((MOB)invoker.riding());
			if((P!=null)
			&&(P.getOwnerName().equals(invoker.Name()))
			&&(P instanceof Paladin_SummonMount))
			{
				invoker.riding().addEffect((Paladin_PaladinsMount)copyOf());
				invoker.riding().recoverPhyStats();
				((MOB)invoker.riding()).recoverCharStats();
				((MOB)invoker.riding()).recoverMaxState();
			}
		}
		return true;
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if((invoker!=null)
		&&(affected!=invoker))
		{
			affectableStats.setStat(CharStats.STAT_SAVE_DISEASE,affectableStats.getStat(CharStats.STAT_SAVE_DISEASE)+50+proficiency()+(5*getXLEVELLevel(invoker())));
			affectableStats.setStat(CharStats.STAT_SAVE_POISON,affectableStats.getStat(CharStats.STAT_SAVE_POISON)+50+proficiency()+(5*getXLEVELLevel(invoker())));
			final int amount=(int)Math.round(CMath.mul(CMath.div(proficiency(),100.0),affected.phyStats().level()+(2*getXLEVELLevel(invoker))));
			for(final int i : CharStats.CODES.SAVING_THROWS())
				affectableStats.setStat(i,affectableStats.getStat(i)+amount);
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(msg.target()==affected)
		{
			if(msg.tool() instanceof Ability)
			{
				if((invoker==null)
				||(!appropriateToMyFactions(invoker))
				||(affected==null)
				||(affected==invoker)
				||(!(affected instanceof MOB)))
					return true;

				final long ableFlags = ((Ability)msg.tool()).flags();
				if((CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
				&&(msg.targetMinor()==CMMsg.TYP_CAST_SPELL)
				&&((ableFlags & allAlignFlags)>0)
				&&(proficiencyCheck(invoker,0,false)))
				{
					if(CMath.bset(ableFlags, Ability.FLAG_FEARING))
					{
						msg.source().location().show((MOB)msg.target(),null,CMMsg.MSG_OK_VISUAL,L("The field around <S-NAME> protect(s) <S-HIM-HER> from the fearful magic attack of @x1.",msg.source().name()));
						return false;
					}
					if((!CMath.bset(ableFlags, Ability.FLAG_NEUTRAL))
					&&(!CMath.bset(ableFlags, Ability.FLAG_MODERATE))
					&&((PaladinSkill.isPaladinGoodSide(invoker)&&((ableFlags & allEAlignFlags)>0))
						||(PaladinSkill.isPaladinAntiSide(invoker)&&((ableFlags & allGAlignFlags)>0)))
					&&(proficiencyCheck(invoker,0,false)))
					{
						final MOB mob=(MOB)msg.target();
						mob.location().show(invoker,mob,CMMsg.MSG_OK_VISUAL,L("<T-YOUPOSS> courage protects <T-NAME> from the @x1 attack.",msg.tool().name()));
						return false;
					}
				}
			}
			if(((msg.targetMinor()==CMMsg.TYP_POISON)||(msg.targetMinor()==CMMsg.TYP_DISEASE))
			&&(proficiencyCheck(invoker,0,false)))
				return false;
		}
		return true;
	}
}
