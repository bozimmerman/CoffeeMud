package com.planet_ink.coffee_mud.Abilities.Druid;
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
public class Druid_Krakenform extends StdAbility
{
	@Override
	public String ID()
	{
		return "Druid_Krakenform";
	}

	private final static String	localizedName	= CMLib.lang().L("Krakenform");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_SHAPE_SHIFTING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[]	triggerStrings	= I(new String[] { "KRAKENFORM" });

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
	public String displayText()
	{
		return "(in Kraken form)";
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setName("a kraken");
		affectableStats.setHeight(420);
		affectableStats.setWeight(5000);
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setRaceName("Kraken");
	}

	protected final static String ammoType="tentacle";

	protected SailingShip ship = null;
	protected Language	krakenSpeak = null;
	
	protected SailingShip getShip()
	{
		if(ship == null)
		{
			final Physical affected=this.affected;
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				final Room R=mob.location();
				ship=(SailingShip)CMClass.getItem("GenSailingShip");
				ship.basePhyStats().setAbility((int)Math.round(Math.ceil(CMath.div(adjustedLevel(mob,0),15.0))));
				ship.recoverPhyStats();
				ship.setSavable(false);
				ship.setAnchorDown(false);
				ship.setUsesRemaining(100);
				ship.setName("a kraken");
				ship.setDisplayText("a kraken is here");
				ship.setStat("SPECIAL_NOUN_SHIP", "kraken");
				ship.setStat("SPECIAL_VERB_SAIL","swim");
				ship.setStat("SPECIAL_VERB_SAILING","swimming");
				ship.setStat("SPECIAL_DISABLE_CMDS", "anchor,throw,tender");
				ship.setStat("SPECIAL_HEAD_OFFTHEDECK", "");
				((Exit)ship).setDoorsNLocks(true, false, true, true, true, true);
				((Exit)ship).setKeyName(""+Math.random());
				//((Exit)ship).setExitParams("", newCloseWord, newOpenWord, newClosedText);
				if(ship instanceof PrivateProperty)
					((PrivateProperty)ship).setOwnerName(mob.Name());
				final Area A=ship.getShipArea();
				A.setName("Krakenform_"+mob.Name());
				final Room deckR=CMClass.getLocale("ShipDeck");
				deckR.setRoomID(A.getNewRoomID(R,-1));
				deckR.setArea(A);
				deckR.bringMobHere(mob, false);
				final AmmunitionWeapon weap1=(AmmunitionWeapon)CMClass.getWeapon("GenSiegeWeapon");
				weap1.setName("a tentacle");
				weap1.setAmmoCapacity(1);
				weap1.setAmmoRemaining(1);
				weap1.setAmmunitionType(ammoType);
				weap1.setRanges(0, 3+(super.getXLEVELLevel(mob)/2));
				weap1.basePhyStats().setDamage(20);
				weap1.recoverPhyStats();
				CMLib.flags().setGettable(weap1, false);
				deckR.addItem(weap1);
				final AmmunitionWeapon weap2=(AmmunitionWeapon)weap1.copyOf();
				deckR.addItem(weap2);
				final int numRooms = (int)Math.round(Math.ceil(CMath.div(mob.phyStats().level(),9)))-1;
				// this is dumb, but it's the only way to give hit points
				for(int i=0;i<numRooms;i++)
				{
					final Room hpR=CMClass.getLocale("WoodRoom");
					hpR.setRoomID(A.getNewRoomID(R,-1));
					hpR.setArea(A);
				}
				R.addItem(ship);
				CMLib.map().registerWorldObjectLoaded(A, R, ship);
			}
		}
		return ship;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(msg.target() instanceof Room)
		{
			final Physical P=affected;
			if((P instanceof MOB)
			&&(((MOB)P).location()==msg.target()))
				msg.setTarget(CMLib.map().roomLocation(getShip()));
		}
		else
		if(msg.source()==affected)
		{
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_SPEAK:
			{
				final Room R= CMLib.map().roomLocation(getShip());
				if(krakenSpeak == null)
				{
					krakenSpeak=(Language)CMClass.getAbility("Krakenspeak");
					krakenSpeak.setProficiency(100);
					krakenSpeak.setMiscText("ALWAYS=true SPOKEN=true");
				}
				if(msg.tool() instanceof Language)
					msg.setTool(null);
				krakenSpeak.setAffectedOne(msg.source());
				if(krakenSpeak.okMessage(msg.source(), msg))
				{
					krakenSpeak.executeMsg(msg.source(), msg);
					R.send(msg.source(), msg);
				}
				return false;
			}
			case CMMsg.TYP_HUH:
			{
				if(msg.targetMessage()!=null)
				{
					final List<String> cmds=CMParms.parse(msg.targetMessage());
					if(cmds.size()==0)
						return true;
					final String word=cmds.get(0).toUpperCase();
					final int dir=CMLib.directions().getDirectionCode(word);
					if(dir >= 0)
					{
						final SailingShip ship=getShip();
						if(ship != null)
						{
							msg.setTargetMessage("SAIL "+word);
							if(ship.okMessage(myHost, msg))
								ship.executeMsg(myHost, msg);
							msg.setTargetMessage("do nothing");
							return false;
						}
					}
					else
					if(word.equalsIgnoreCase("lower"))
						return false;
				}
				break;
			}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((msg.target()==ship)
		&&(msg.targetMinor()==CMMsg.TYP_CAUSESINK)
		&&(affected instanceof MOB))
		{
			final MOB mob=(MOB)affected;
			if(mob != null)
			{
				unInvoke(); // revert them
				CMLib.combat().postDeath(msg.source(), mob, msg);
			}
		}
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob.location()!=null))
		{
			if(ship != null)
			{
				final Room shipR=CMLib.map().roomLocation(ship);
				if(shipR != null)
				{
					shipR.bringMobHere(mob, true);
					ship.destroy();
					ship=null;
				}
			}
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> revert(s) to @x1 form.",mob.charStats().raceName().toLowerCase()));
		}
	}

	public static boolean isShapeShifted(final MOB mob)
	{
		if(mob==null)
			return false;
		for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&((A instanceof Druid_ShapeShift)||(A instanceof Druid_Krakenform)))
				return true;
		}
		return false;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if((((MOB)target).isInCombat())
				&&(!Druid_ShapeShift.isShapeShifted((MOB)target)))
				{
				}
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		final SailingShip ship=this.getShip();
		if(ship != null)
		{
			ship.setAnchorDown(false);
			if(ship.isInCombat())
			{
				final Area A=ship.getShipArea();
				if(A!=null)
				{
					for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
					{
						final Room R = r.nextElement();
						for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
						{
							final Item I=i.nextElement();
							if((I instanceof AmmunitionWeapon)
							&&(ammoType.equalsIgnoreCase(((AmmunitionWeapon)I).ammunitionType()))
							&&(!CMLib.flags().isGettable(I)))
								((AmmunitionWeapon)I).setAmmoRemaining(((AmmunitionWeapon)I).ammunitionCapacity());
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		for(final Enumeration<Ability> a=mob.personalEffects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&((A instanceof Druid_ShapeShift)||(A instanceof Druid_Krakenform)))
			{
				A.unInvoke();
				return true;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if((!appropriateToMyFactions(mob))&&(!auto))
		{
			if((CMLib.dice().rollPercentage()<50))
			{
				mob.tell(L("Extreme emotions disrupt your change."));
				return false;
			}
		}

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_OK_ACTION,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> take(s) on Kraken form."));
				final Druid_Krakenform form = (Druid_Krakenform)beneficialAffect(mob,mob,asLevel,Ability.TICKS_FOREVER);
				if(form != null)
					form.getShip();
				mob.recoverCharStats();
				mob.recoverPhyStats();
			}
		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> chant(s) to <S-HIM-HERSELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
