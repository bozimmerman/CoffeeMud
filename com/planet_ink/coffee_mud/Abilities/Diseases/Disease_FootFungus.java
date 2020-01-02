package com.planet_ink.coffee_mud.Abilities.Diseases;
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
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2019-2020 Bo Zimmerman

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
public class Disease_FootFungus extends Disease
{
	@Override
	public String ID()
	{
		return "Disease_FootFungus";
	}

	private final static String localizedName = CMLib.lang().L("Foot Fungus");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Foot Fungus)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
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

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	protected int DISEASE_TICKS()
	{
		return getTicksPerDay() * CMLib.dice().roll(1, 3, 4);
	}

	@Override
	protected int DISEASE_DELAY()
	{
		return 75;
	}

	@Override
	protected String DISEASE_DONE()
	{
		return L("Your foot fungus clears up.");
	}

	@Override
	protected String DISEASE_START()
	{
		return L("^G<S-NAME> contract(s) a foot fungus!^?");
	}

	@Override
	protected String DISEASE_AFFECT()
	{
		return L("<S-NAME> smell(s) rotten.");
	}

	@Override
	public int spreadBitmap()
	{
		return 0;
	}

	@Override
	public int difficultyLevel()
	{
		return 4;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(affected==null)
			return false;
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if(mob != null)
		{
			if((mob.baseCharStats().getMyRace().forbiddenWornBits()&Wearable.WORN_FEET)==Wearable.WORN_FEET)
			{
				unInvoke();
				mob.recoverCharStats();
				mob.recoverMaxState();
			}
			if((!mob.amDead())&&((--diseaseTick)<=0))
			{
				diseaseTick=DISEASE_DELAY();
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,DISEASE_AFFECT());
				Ability gA=mob.fetchEffect("Gait");
				if(gA==null)
				{
					gA=CMClass.getAbility("Gait");
					if(gA!=null)
					{
						mob.addNonUninvokableEffect(gA);
						mob.recoverCharStats();
						CMLib.utensils().confirmWearability(mob);
					}
				}
				if(gA!=null)
					gA.setMiscText("ARRIVE=\"limp(s)\" LEAVE=\"limp(s)\" STATE=\"limping\"");
				return true;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.amITarget(affected))
		&&(msg.targetMinor()==CMMsg.TYP_SNIFF)
		&&(CMLib.flags().canSmell(msg.source())))
			msg.source().tell(msg.source(),affected,null,L("<T-YOUPOSS> feet smell horrid!!!"));
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.source()==affected)
		&&((msg.sourceMinor()==CMMsg.TYP_ENTER)
			||(msg.sourceMinor()==CMMsg.TYP_LEAVE))
		&&(msg.target() instanceof Room)
		&&(msg.source().isAttributeSet(Attrib.AUTORUN)))
		{
			msg.source().tell(L("You can not run in your condition."));
			return false;
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		final Physical P=affected;
		super.unInvoke();
		if((P != null)&&(this.unInvoked))
		{
			final Ability gA=P.fetchEffect("Gait");
			if((gA!=null)
			&&(gA.text().indexOf("limping")>0))
			{
				gA.unInvoke();
				P.delEffect(gA);
			}
		}
	}

	@Override
	public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
	{
		super.affectCharState(affectedMob, affectableMaxState);
		if(affected==null)
			return;
		if((affectedMob.baseCharStats().getMyRace().forbiddenWornBits()&Wearable.WORN_FEET)==0)
			affectableMaxState.setMovement(affectableMaxState.getMovement()/2);
	}

	@Override
	public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMob, affectableStats);
		if(affected==null)
			return;
		affectableStats.setWearableRestrictionsBitmap(affectableStats.getWearableRestrictionsBitmap()|Wearable.WORN_FEET);
		//affectableStats.setStat(CharStats.STAT_CHARISMA, affectableStats.getStat(CharStats.STAT_CHARISMA)-3);
	}
}
