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
public class Fighter_CaravanCommander extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_CaravanCommander";
	}

	private final static String localizedName = CMLib.lang().L("Caravan Commander");

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
	protected int canAffectCode()
	{
		return CAN_MOBS|CAN_AREAS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_COMBATLORE;
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

	protected boolean started = false;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(affected instanceof Area)
		{
			if((invoker() != null)
			&&(CMLib.map().areaLocation(invoker())!=affected))
				affected.delEffect(this);
			return true;
		}
		else
		if((!started)
		&&(affected instanceof MOB))
		{
			final Area A = CMLib.map().areaLocation(affected);
			if(A != null)
			{
				started = true;
				if(A instanceof Boardable)
				{
					if((((Boardable)A).getBoardableItem() instanceof NavigableItem)
					&&((((NavigableItem)((Boardable)A).getBoardableItem()).navBasis()==Basis.LAND_BASED)
						||(((NavigableItem)((Boardable)A).getBoardableItem()).navBasis()==Basis.WAGON))
					&&(A.fetchEffect(ID())==null)
					&&(CMLib.law().doesHavePriviledgesHere((MOB)affected, A.getRandomProperRoom())))
					{
						final Ability effA = (Ability)this.copyOf();
						effA.setSavable(false);
						effA.setInvoker((MOB)affected);
						A.addNonUninvokableEffect(effA);
						effA.setSavable(false);
						if(CMLib.dice().rollPercentage()==1)
							helpProficiency((MOB)affected, 0);
					}
				}
			}
		}
		return false;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((affected instanceof MOB)
		&&(msg.source()==affected)
		&&(msg.target() instanceof Boardable)
		&&((msg.targetMinor()==CMMsg.TYP_ENTER)||(msg.targetMinor()==CMMsg.TYP_SIT)))
		{
			final Area A = ((Boardable)msg.target()).getArea();
			if((((Boardable)A).getBoardableItem() instanceof NavigableItem)
			&&((((NavigableItem)((Boardable)A).getBoardableItem()).navBasis()==Basis.LAND_BASED)
				||(((NavigableItem)((Boardable)A).getBoardableItem()).navBasis()==Basis.WAGON))
			&&(A.fetchEffect(ID())==null)
			&&(CMLib.law().doesHavePriviledgesHere(msg.source(), A.getRandomProperRoom())))
			{
				final Ability effA = (Ability)this.copyOf();
				effA.setSavable(false);
				effA.setInvoker(msg.source());
				A.addNonUninvokableEffect(effA);
				effA.setSavable(false);
				if(CMLib.dice().rollPercentage()==1)
					helpProficiency(msg.source(), 0);
			}
		}
		super.executeMsg(myHost, msg);
	}

	@Override
	public boolean bubbleAffect()
	{
		return affected instanceof Area;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(bubbleAffect()
		&&(affected instanceof MOB)
		&&(((MOB)affected).isInCombat())
		&&(invoker() != null)
		&&(invoker().getGroupMembers(new XTreeSet<MOB>()).contains(affected)))
		{
			final double fpct = CMath.div(proficiency(),100.0);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(int)Math.round(CMath.mul(20,fpct)));
			final double armorBonus = CMath.mul(((adjustedLevel(invoker(),0)/5)+super.getXLEVELLevel(invoker())),fpct);
			affectableStats.setArmor(affectableStats.armor()+(int)Math.round(armorBonus));
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool() instanceof AmmunitionWeapon)
		&&(CMLib.combat().isASiegeWeapon((Item)msg.tool()))
		&&(affected instanceof Area)
		&&(affected==CMLib.map().areaLocation(msg.tool()))
		&&(invoker() != null)
		&&(msg.value()>0))
		{
			if(proficiency()>=100)
				msg.setValue((int)Math.round(CMath.mul(msg.value(),1.2+(0.02*super.getXLEVELLevel(invoker())))));
			else
			{
				final double fpct = CMath.div(proficiency(),100.0);
				final double bonus = 0.2 + (0.02*super.getXLEVELLevel(invoker()));
				final double fbonus = 1.0 + (bonus + fpct);
				msg.setValue((int)Math.round(CMath.mul(msg.value(),fbonus)));
			}
			if(CMLib.dice().rollPercentage()<10)
				helpProficiency((MOB)affected, 0);
		}
		return true;
	}
}
