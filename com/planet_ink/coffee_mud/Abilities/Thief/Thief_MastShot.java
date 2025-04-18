package com.planet_ink.coffee_mud.Abilities.Thief;
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
   Copyright 2016-2025 Bo Zimmerman

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
public class Thief_MastShot extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_MastShot";
	}

	private final static String localizedName = CMLib.lang().L("Mast Shot");

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
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_SEATRAVEL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings = I(new String[] { "MASTSHOT"});

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	protected int	code		= 0;

	@Override
	public int abilityCode()
	{
		return code;
	}

	@Override
	public void setAbilityCode(final int newCode)
	{
		code = newCode;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		if(affected instanceof Boardable)
		{
			affectableStats.setAbility(affectableStats.ability() - abilityCode());
		}
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		if(CMath.isInteger(newMiscText))
			setAbilityCode(CMath.s_int(newMiscText));
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		final Physical affected=this.affected;
		if(affected instanceof Boardable)
		{

		}
		else
		if(affected instanceof AmmunitionWeapon)
		{
			if((msg.source().riding() instanceof SiegableItem)
			&&(msg.tool()==affected)
			&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(msg.target() instanceof Rideable)
			&&(msg.target() instanceof NavigableItem)
			&&(((NavigableItem)msg.target()).navBasis()==Rideable.Basis.WATER_BASED)
			&&(msg.target() instanceof Physical)
			)
			{
				if(CMLib.dice().rollPercentage()<(10 + abilityCode()))
				{
					final Room room=CMLib.map().roomLocation(msg.target());
					final AmmunitionWeapon weapon = (AmmunitionWeapon)affected;
					final String mastString="^F^<FIGHT^>"+
											L("<O-NAME> fired from <S-NAME> shreds <T-NAME>'s mast.")
											+"^</FIGHT^>^?";
					final CMMsg msg2=CMClass.getMsg(msg.source(),
													msg.target(),
													weapon,
													CMMsg.MSG_ATTACKMISS,
													mastString);
					CMLib.color().fixSourceFightColor(msg2);
					if((room!=null)
					&&(room.okMessage(msg.source(),msg2)))
					{
						room.send(msg.source(),msg2);
						final Ability oldA=((Physical)msg.target()).fetchEffect(ID());
						if(oldA!=null)
						{
							oldA.setAbilityCode(oldA.abilityCode()+1);
						}
						else
						{
							final MOB mob=(invoker != null) ? invoker : msg.source();
							final Ability A=beneficialAffect(mob, (Physical)msg.target(), 0, 0);
							if(A!=null)
								A.setAbilityCode(1);
						}
					}
					if(this.canBeUninvoked())
						unInvoke();
					return false;
				}
				else
				{
					CMLib.combat().postSiegeWeaponAttackResult(msg.source(), (NavigableItem)msg.source().riding(), (Rideable)msg.target(), (Weapon)affected, false);
					if(this.canBeUninvoked())
						unInvoke();
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		if((!(R.getArea() instanceof Boardable))
		||(!(((Boardable)R.getArea()).getBoardableItem() instanceof SiegableItem))
		||((R.domainType()&Room.INDOORS)!=0))
		{
			mob.tell(L("You must be able to man siege engines to fire a mast shot."));
			return false;
		}
		final Boardable myShip=(Boardable)R.getArea();
		final NavigableItem myShipItem=(NavigableItem)myShip.getBoardableItem();
		/*
		if((myShipItem==null)
		||(!(myShipItem.owner() instanceof Room))
		||(!CMLib.flags().isWateryRoom((Room)myShipItem.owner())))
		{
			mob.tell(L("Your ship must be at sea to fire a mast shot."));
			return false;
		}
		*/
		NavigableItem enemyShip=null;
		final PhysicalAgent combatant=myShipItem.getCombatant();
		if(combatant != null)
		{
			if((combatant instanceof NavigableItem)
			&&(((NavigableItem)combatant).navBasis() == Rideable.Basis.WATER_BASED))
				enemyShip=(NavigableItem)combatant;
		}

		if(enemyShip == null)
		{
			mob.tell(L("You must be in combat with a big sailing ship to fire a mast shot."));
			return false;
		}

		final Item enemyShipItem=enemyShip.getBoardableItem();

		if(commands.size()==0)
		{
			mob.tell(L("You must specify a siege weapon to make the mast shot with."));
			return false;
		}

		final String weaponName=CMParms.combine(commands);

		if(!CMLib.flags().isStanding(mob)&&(!auto))
		{
			mob.tell(L("You need to stand up!"));
			return false;
		}

		final Item siegeItem = R.findItem(null, weaponName);
		if(((siegeItem)==null)
		||(!CMLib.flags().canBeSeenBy(siegeItem, mob))
		||(!CMLib.combat().isASiegeWeapon(siegeItem)))
		{
			mob.tell(L("You don't see a siege weapon called '@x1' here.",weaponName));
			return false;
		}

		if(siegeItem.fetchEffect(ID())!=null)
		{
			mob.tell(L("That weapon is already aimed at the masts."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,siegeItem,this,auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_THIEF_ACT,auto?"":L("<S-NAME> aim(s) <T-NAME> at @x1's masts!",enemyShipItem.Name()));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				final Ability A=beneficialAffect(mob, siegeItem, asLevel, 0);
				A.setAbilityCode(2 * super.getXLEVELLevel(mob));
			}
		}
		else
			super.beneficialVisualFizzle(mob, siegeItem, L("<S-NAME> tr(ys) to aim <T-NAME> at @x1's masts, but fail(s).",enemyShipItem.Name()));
		return success;
	}
}
