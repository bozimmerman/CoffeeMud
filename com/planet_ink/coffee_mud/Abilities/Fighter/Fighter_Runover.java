package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.Rideable.Basis;
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
public class Fighter_Runover extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_Runover";
	}

	private final static String	localizedName	= CMLib.lang().L("Runover");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "RUNOVER" });

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
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
		return Ability.CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_TRAVEL;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public String displayText()
	{
		if(text().length()>0)
			return CMLib.lang().L("(Targeting: @x1)",text());
		return "";
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)&&(target instanceof MOB))
		{
			if(mob.isInCombat()||(mob.location()==((MOB)target).location()))
				return Ability.QUALITY_INDIFFERENT;
			final Room R = mob.location();
			if(!(R.getArea() instanceof Boardable))
				return Ability.QUALITY_INDIFFERENT;
			final Item I = ((Boardable)(R.getArea())).getBoardableItem();
			if((I == null)
			||((((Rideable)I).rideBasis() != Rideable.Basis.LAND_BASED)
				&&(((Rideable)I).rideBasis() != Rideable.Basis.WAGON)))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.source().riding() instanceof NavigableItem)
		&&(msg.targetMinor()==CMMsg.TYP_LEAVE)
		&&(msg.target() instanceof Room)
		&&(msg.source().isMonster())
		&&(affected instanceof MOB))
		{
			final MOB mob = (MOB)affected;
			final Room R = mob.location();
			if((R!=null)
			&&(R.getArea() instanceof Boardable)
			&&(((NavigableItem)msg.source().riding()).getBoardableItem()==msg.source().riding()))
			{
				final NavigableItem navI = (NavigableItem)msg.source().riding();
				if(text().length()>0)
				{
					final MOB M = ((Room)msg.target()).fetchInhabitant(text());
					if(M != null)
					{
						if(((Room)msg.target()).show(mob, M, msg.source().riding(),
							CMMsg.MASK_MALICIOUS|CMMsg.MSG_OK_ACTION, L("<O-NAME> run(s) over <T-NAME>!")))
						{
							final Set<MOB> pullers = ((Rideable)navI.getBoardableItem()).getRideBuddies(new HashSet<MOB>());
							int weights = navI.getBoardableItem().phyStats().weight();
							for(final MOB P : pullers)
								weights += P.phyStats().weight();
							weights = weights / M.phyStats().weight();
							final int damage = CMLib.dice().roll(1, 6  + super.getXLEVELLevel(mob), 0) * weights;
							CMLib.combat().postDamage(mob, M, navI.getBoardableItem(), damage,
									CMMsg.MASK_ALWAYS|CMMsg.MASK_SOUND|CMMsg.MASK_MOVE|CMMsg.TYP_JUSTICE,
									Weapon.TYPE_BASHING,
									L("<O-NAME> <DAMAGES> <T-NAME> as it rolls over <T-HIM-HER>!"));
						}
					}
				}
				unInvoke();
				//CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.MASK_SOUND|CMMsg.MASK_MOVE|CMMsg.TYP_JUSTICE,Weapon.TYPE_BASHING,L("^F^<FIGHT^><S-NAME> <DAMAGE> <T-NAME> with a ferocious KICK!^</FIGHT^>^?@x1",CMLib.protocol().msp("bashed1.wav",30)));
			}
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_LEAVE)
		&&(msg.source()==affected))
			unInvoke();
		super.executeMsg(myHost, msg);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room R = mob.location();
		if(R==null)
			return false;
		Item boardI = null;
		Area boardA = null;
		if(R.getArea() instanceof Boardable)
		{
			final Item I = ((Boardable)(R.getArea())).getBoardableItem();
			if((I != null)
			&&((((Rideable)I).rideBasis() == Rideable.Basis.LAND_BASED)
					||(((Rideable)I).rideBasis() == Rideable.Basis.WAGON)))
			{
				boardI = I;
				boardA = R.getArea();
			}
		}
		if((boardA == null) || (boardI == null))
		{
			mob.tell(L("You must be driving a caravan to run someone over."));
			return false;
		}
		if(mob.isInCombat()&&(!auto))
		{
			mob.tell(L("You are too busy to do that!"));
			return false;
		}
		final Ability othA = mob.fetchEffect(ID());
		if(othA != null)
		{
			othA.unInvoke();
			mob.delEffect(othA);
		}
		final Room offR = CMLib.map().roomLocation(boardI);
		final MOB offM = CMClass.getFactoryMOB(mob.name(), mob.phyStats().level(), offR);
		MOB target = null;
		try
		{
			offM.setAttributesBitmap(mob.getAttributesBitmap());
			offM.setSession(mob.session());
			target=this.getTarget(offM,commands,givenTarget);
		}
		finally
		{
			offM.setSession(null);
			mob.session().setMob(mob);
			offM.destroy();
		}
		if(target==null)
			return false;

		if(!((Boardable)boardI).securityCheck(mob))
		{
			mob.tell(L("You aren't allowed to do that."));
			return false;
		}

		int smallest=boardI.phyStats().weight();
		final Set<MOB> pullers = ((Rideable)boardI).getRideBuddies(new HashSet<MOB>());
		for(final MOB P : pullers)
		{
			if(P.phyStats().weight()<smallest)
				smallest=P.phyStats().weight();
		}
		if(target.phyStats().weight()>smallest/2)
		{
			mob.tell(L("@x1 is too large to run over.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),
					L("<S-NAME> aim(s) @x1 at <T-NAME>.",boardI.name(mob)));
			if(offR.okMessage(mob,msg))
			{
				offR.send(mob,msg);
				final Ability A = this.beneficialAffect(mob, mob, asLevel, 0);
				if(A!=null)
					A.setMiscText(offR.getContextName(target));
				else
					return false;
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> fail(s) to runover <T-NAMESELF>."));

		// return whether it worked
		return success;
	}
}
