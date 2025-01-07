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
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2024 Bo Zimmerman

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
public class Disease_Vampirism extends Disease
{
	@Override
	public String ID()
	{
		return "Disease_Vampirism";
	}

	private final static String	localizedName	= CMLib.lang().L("Vampirism");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Vampirism)");

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
		return CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY) * 6;
	}

	@Override
	protected int DISEASE_DELAY()
	{
		return CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY);
	}

	@Override
	protected String DISEASE_DONE()
	{
		return L("Your vampirism lifts.");
	}

	@Override
	protected String DISEASE_START()
	{
		return L("^G<S-NAME> seem(s) pale and cold.^?");
	}

	@Override
	protected String DISEASE_AFFECT()
	{
		return "";
	}

	@Override
	public int spreadBitmap()
	{
		return DiseaseAffect.SPREAD_CONSUMPTION;
	}

	@Override
	public int difficultyLevel()
	{
		return 9;
	}

	protected final static String cancelID="Spell_DarkSensitivity";

	protected volatile int	prevEat			= 1;
	protected int			chaBonus		= 0;
	protected boolean		bloodHeal		= true;
	protected boolean		bloodThirst		= true;
	protected boolean		lightSensitive	= true;
	protected boolean		noSwim			= true;
	protected boolean		drinkMobs		= true;

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		chaBonus = CMParms.getParmInt(newMiscText, "CHABONUS", -1);
		bloodHeal = CMParms.getParmBool(newMiscText, "BLOODHEAL", true);
		bloodThirst = CMParms.getParmBool(newMiscText, "BLOODTHIRST", true);
		lightSensitive = CMParms.getParmBool(newMiscText, "LOWLIGHT", true);
		noSwim = CMParms.getParmBool(newMiscText, "NOSWIM", true);
		drinkMobs = CMParms.getParmBool(newMiscText, "DRINKMOBS", true);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((!(affected instanceof MOB))||(!lightSensitive))
			return;
		if(((MOB)affected).location()==null)
			return;
		if(CMLib.flags().isInDark(((MOB)affected).location()))
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_DARK);
		else
		{
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-50);
			affectableStats.setArmor(affectableStats.armor()+50);
		}
	}

	protected boolean isLightBlind(final MOB M)
	{
		if(!lightSensitive)
			return false;
		final Room R=M.location();
		if(R==null)
			return true;
		return !CMLib.flags().isInDark(R);
	}

	protected boolean isCharmedBy(final MOB M, final MOB mob)
	{
		final List<Ability> chAs = CMLib.flags().flaggedAffects(M, Ability.FLAG_CHARMING);
		if(chAs == null)
			return false;
		for(final Ability A : chAs)
		{
			if(A.invoker()==mob)
				return true;
		}
		return false;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(msg.source()==affected)
		{
			if((noSwim)
			&&(msg.tool() instanceof Ability)
			&&(msg.tool().ID().equals("Skill_Swim")))
			{
				msg.source().tell(L("You can't swim!"));
				return false;
			}
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_DRINK:
			{
				if(msg.target() instanceof Drink)
					this.prevEat = ((Drink)msg.target()).thirstQuenched();
				else
				if((msg.target() instanceof MOB) && (drinkMobs))
				{
						final MOB mob=msg.source();
						final Room R=mob.location();
					final MOB M=(MOB)msg.target();
					if((M!=null)
					&&(CMLib.flags().canBeSeenBy(M, mob))
					&&(M!=mob))
					{
						if(CMLib.flags().isGolem(M) || CMLib.flags().isAPlant(M))
							mob.tell(L("@x1 is bloodless.",M.name(mob)));
						else
						if((!CMLib.flags().isSleeping(M))
						&&(!CMLib.flags().isBoundOrHeld(M))
						&&(!isCharmedBy(M,mob)))
							mob.tell(L("@x1 won't let you.",M.name(mob)));
						else
						{
							final Drink dr = (Drink)CMClass.getItem("GenLiquidResource");
							dr.setLiquidType(RawMaterial.RESOURCE_BLOOD);
							final int heal = CMLib.dice().roll(1, M.phyStats().level(), 4);
							dr.setLiquidHeld(heal);
							dr.setLiquidRemaining(heal);
							dr.setThirstQuenched(heal);
							((Item)dr).setMaterial(RawMaterial.RESOURCE_BLOOD);
							((Item)dr).basePhyStats().setWeight(heal);
							((Item)dr).phyStats().setWeight(heal);
							final CMMsg dmsg = CMClass.getMsg(mob, dr, M,
									CMMsg.MASK_MALICIOUS|CMMsg.MSG_DRINK,
									CMMsg.MASK_MALICIOUS|CMMsg.MSG_DRINK,
									CMMsg.MASK_MALICIOUS|CMMsg.MSG_DRINK,
									L("<S-NAME> drink(s) <O-YOUPOSS> blood."));
							if(R.okMessage(mob, dmsg))
							{
								R.send(mob, dmsg);
								final Map<MOB,MOB> vicMap = new HashMap<MOB,MOB>();
								for(final Enumeration<MOB> m = R.inhabitants();m.hasMoreElements();)
								{
									final MOB M1=m.nextElement();
									if(M1!=null)
										vicMap.put(M1, M1.getVictim());
								}
								final int dmg = CMLib.dice().roll(1, Math.min(16, M.phyStats().level()), Math.min(16, M.phyStats().level())/4);
								CMLib.combat().postDamage(mob, M, null, dmg, CMMsg.TYP_JUSTICE, Weapon.TYPE_NATURAL, null);
								for(final MOB M1 : vicMap.keySet())
								{
									final MOB vicM = vicMap.get(M1);
									if(M1.getVictim()!=vicM)
										M1.setVictim(vicM);
								}
								if(CMLib.flags().isSleeping(M))
									CMLib.commands().postStand(M, true, true);
							}
						}
						return false;
					}
				}
				break;
			}
			case CMMsg.TYP_EXAMINE:
				if(isLightBlind(msg.source())
				&& (!(msg.target() instanceof Room))
				&&(msg.source().fetchEffect(cancelID)==null))
				{
					msg.source().tell(L("You can't seem to make it out that well in this bright light."));
					return false;
				}
				break;
			case CMMsg.TYP_JUSTICE:
			{
				if(!msg.targetMajor(CMMsg.MASK_DELICATE))
					return true;
			}
				//$FALL-THROUGH$
			case CMMsg.TYP_DELICATE_HANDS_ACT:
			case CMMsg.TYP_CAST_SPELL:
			{
				if((msg.target()!=null)
				&&(msg.target()!=msg.source())
				&&(!(msg.target() instanceof Room))
				&&(isLightBlind(msg.source()))
				&&(msg.source().fetchEffect(cancelID)==null))
				{
					msg.source().tell(msg.source(),msg.target(),null,L("You can't seem to make out <T-NAME> in this bright light."));
					return false;
				}
				break;
			}
			case CMMsg.TYP_EAT:
			{
				if(msg.target() instanceof Food)
				{
					final Food fI=(Food)msg.target();
					int amountEaten=fI.bite();
					if((amountEaten<1)||(amountEaten>fI.nourishment()))
						amountEaten=fI.nourishment();
					this.prevEat = amountEaten;
				}
				break;
			}
			/*
			case CMMsg.TYP_DRINK:
			{
				if(msg.target() instanceof Drink)
					this.prevEat = ((Drink)msg.target()).thirstQuenched();
				break;
			}
			*/
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.okMessage(myHost, msg);
		if(((msg.targetMinor()==CMMsg.TYP_EAT)
			||(msg.targetMinor()==CMMsg.TYP_DRINK))
		&&(msg.source()==affected)
		&&(affected instanceof MOB)
		&&(msg.target() instanceof Item))
		{
			final MOB mob=msg.source();
			final Item I=(Item)msg.target();
			if((I.material()==RawMaterial.RESOURCE_BLOOD)
			||((I instanceof Drink)&&(((Drink)I).liquidType()==RawMaterial.RESOURCE_BLOOD)))
			{
				if (I.fetchEffect("Poison_Rotten")==null)
				{
					if(bloodThirst)
					{
						if(I instanceof Food)
							mob.curState().adjThirst(prevEat*2,mob.maxState().maxThirst(mob.baseWeight()));
						else
						if(I instanceof Drink)
							mob.curState().adjHunger(prevEat/2,mob.maxState().maxHunger(mob.baseWeight()));
					}
					if(bloodHeal)
					{
						final CMMsg msg2 = CMClass.getMsg(msg.source(),mob,this,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,CMMsg.MSG_HEALING,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,null);
						msg2.setValue(prevEat);
						final Room R=CMLib.map().roomLocation(mob);
						if((R!=null)
						&&(R.okMessage(mob,msg2)))
							R.send(mob, msg2);
					}
				}
			}
			else
			if(bloodThirst)
			{
				if(I instanceof Food)
					mob.curState().adjHunger(-prevEat,mob.maxState().maxHunger(mob.baseWeight()));
				else
				if(I instanceof Drink)
					mob.curState().adjThirst(-prevEat,mob.maxState().maxThirst(mob.baseWeight()));
			}
		}
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if((affected==null)||(chaBonus==0))
			return;
		affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)+chaBonus);
	}
}
