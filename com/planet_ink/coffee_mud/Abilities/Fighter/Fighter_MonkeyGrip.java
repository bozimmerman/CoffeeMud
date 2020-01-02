package com.planet_ink.coffee_mud.Abilities.Fighter;
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

import java.io.IOException;
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
public class Fighter_MonkeyGrip extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_MonkeyGrip";
	}

	private final static String localizedName = CMLib.lang().L("Monkey Grip");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"MONKEYGRIP","MGRIP"});

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
		return Ability.CAN_ITEMS;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_MARTIALLORE;
	}

	@Override
	public void setMiscText(final String newText)
	{
		super.setMiscText(newText);
	}

	protected volatile int monkeyItemLevels=-1;
	protected volatile int monkeyExpertise=-1;

	protected final void cleanItemOfMonkeytude(final Item I)
	{
		if(I!=null)
		{
			final Ability A=I.fetchEffect(ID());
			if(A!=null)
			{
				I.delEffect(A);
				I.setRawLogicalAnd(true);
				I.recoverPhyStats();
				I.setRawWornCode(0);
			}
		}
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		super.executeMsg(host,msg);
		if(msg.source()==affected)
		{
			if((msg.targetMinor()==CMMsg.TYP_WIELD)||(msg.targetMinor()==CMMsg.TYP_HOLD))
				monkeyItemLevels=-1;
			else
			if((msg.targetMinor()==CMMsg.TYP_REMOVE))
				monkeyItemLevels=-1;
			else
			if((msg.sourceMinor()==CMMsg.TYP_QUIT)
			||(msg.sourceMinor()==CMMsg.TYP_SHUTDOWN))
			{
				cleanItemOfMonkeytude(msg.source().fetchWieldedItem());
				cleanItemOfMonkeytude(msg.source().fetchHeldItem());
			}
		}
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof Weapon)
		{
			final Weapon W=(Weapon)affected;
			final MOB M=invoker();
			if(W.amWearingAt(Item.IN_INVENTORY)
			||W.amDestroyed()
			||(M==null)
			||(!(W.owner() instanceof MOB)))
				cleanItemOfMonkeytude(W);
			else
			{
				final double pctLoss=0.75 - CMath.mul(.50 + (.05 * super.getXLEVELLevel(M)), CMath.div(proficiency(),100));
				affectableStats.setDamage(affectableStats.damage()-(int)Math.round(CMath.div(affectableStats.damage(), pctLoss)));
				if(affectableStats.attackAdjustment()>0)
					affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(int)Math.round(CMath.div(affectableStats.attackAdjustment(), pctLoss)));
			}
		}
		else
		if(affected instanceof MOB)
		{
			final MOB M=(MOB)affected;
			if(monkeyItemLevels < 0)
			{
				monkeyExpertise=0;
				int levels=0;
				final Item I1 = M.fetchWieldedItem();
				if(I1 != null)
				{
					if(I1.fetchEffect(ID())!=null)
					{
						levels += I1.phyStats().level();
						monkeyExpertise += super.getXLEVELLevel(M);
					}
				}

				final Item I2 = M.fetchHeldItem();
				if(I2 != null)
				{
					if(I2.fetchEffect(ID())!=null)
					{
						levels += I2.phyStats().level();
						monkeyExpertise += super.getXLEVELLevel(M);
					}
				}
				monkeyItemLevels=levels;
			}
			final int levels=monkeyItemLevels;
			if(levels>0)
			{
				final int totalLoss = levels - (5*monkeyExpertise);
				if(totalLoss > 0)
					affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-totalLoss);
			}
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{

		final Item weapon=super.getTarget(mob,null,givenTarget,null,commands,Wearable.FILTER_UNWORNONLY);
		if(weapon==null)
			return false;
		if((!(weapon instanceof Weapon))
		||(!((Weapon)weapon).rawLogicalAnd()))
		{
			mob.tell(L("@x1 does not need to be monkey gripped.",weapon.name()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final String str=auto?L("@x1 is monkey gripped by <S-NAME>!",weapon.name()):L("<S-NAME> monkey grip(s) <T-NAMESELF>.");
			final CMMsg msg=CMClass.getMsg(mob,weapon,this,CMMsg.MSG_NOISYMOVEMENT,str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Fighter_MonkeyGrip gA=(Fighter_MonkeyGrip)this.copyOf();
				gA.tickDown=0;
				gA.canBeUninvoked=false;
				gA.invoker=msg.source();
				((Weapon)weapon).setRawLogicalAnd(false);
				final Command C;
				if(mob.fetchWieldedItem()!=null)
					C=CMClass.getCommand("Hold");
				else
					C=CMClass.getCommand("Wield");
				if(C!=null)
				{
					try
					{
						C.executeInternal(mob, 0, weapon, Boolean.TRUE);
					}
					catch (final IOException e)
					{
					}
				}
				if(weapon.amBeingWornProperly())
				{
					((Weapon)weapon).addNonUninvokableEffect(gA);
					mob.location().recoverRoomStats();
				}
				else
					((Weapon)weapon).setRawLogicalAnd(true);
			}
		}
		else
			return beneficialVisualFizzle(mob,weapon,L("<S-NAME> attempt(s) to tweak <T-NAME>, but just can't get it quite right."));
		return success;
	}
}
