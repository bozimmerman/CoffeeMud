package com.planet_ink.coffee_mud.Abilities.Prayers;
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
public class Prayer_AnimateLimb extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_AnimateLimb";
	}

	private final static String	localizedName	= CMLib.lang().L("Animate Limb");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_DEATHLORE;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	private final static String	localizedDiplayText	= CMLib.lang().L("Newly animated limb");

	@Override
	public String displayText()
	{
		return localizedDiplayText;
	}

	@Override
	public void unInvoke()
	{
		final Physical P=affected;
		super.unInvoke();
		if((P instanceof MOB)&&(this.canBeUninvoked)&&(this.unInvoked))
		{
			if((!P.amDestroyed())
			&&(((MOB)P).amFollowing()==null))
			{
				final Room R=CMLib.map().roomLocation(P);
				if(!CMLib.law().doesHavePriviledgesHere(invoker(), R))
				{
					if((R!=null)&&(!((MOB)P).amDead()))
						R.showHappens(CMMsg.MSG_OK_ACTION, P,L("<S-NAME> wander(s) off."));
					P.destroy();
				}
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		final int tickSet = super.tickDown;
		if(!super.tick(ticking, tickID))
			return false;
		if(ticking instanceof MOB)
		{
			final MOB mob=(MOB)ticking;
			if(mob.amFollowing() != null)
				super.tickDown = tickSet;
		}
		return true;
	}

	public int getUndeadLevel(final MOB mob, final double baseLvl, final double corpseLevel)
	{
		final ExpertiseLibrary exLib=CMLib.expertises();
		final double deathLoreExpertiseLevel = super.getXLEVELLevel(mob);
		final double appropriateLoreExpertiseLevel = super.getX1Level(mob);
		final double charLevel = mob.phyStats().level();
		final double maxDeathLoreExpertiseLevel = exLib.getHighestListableStageBySkill(mob,ID(),ExpertiseLibrary.XType.LEVEL);
		final double maxApproLoreExpertiseLevel = exLib.getHighestListableStageBySkill(mob,ID(),ExpertiseLibrary.XType.X1);
		double lvl = 0;
		if ((maxApproLoreExpertiseLevel > 0)
		&& (maxDeathLoreExpertiseLevel > 0))
		{
			lvl = (charLevel * (10 + appropriateLoreExpertiseLevel) / (10 + maxApproLoreExpertiseLevel))
					-(baseLvl+4+(2*maxDeathLoreExpertiseLevel));
		}
		if(lvl < 0.0)
			lvl = 0.0;
		lvl += baseLvl + (2*deathLoreExpertiseLevel);
		if(lvl > corpseLevel)
			lvl = corpseLevel;
		return (int)Math.round(lvl);
	}

	protected MOB makeUndeadFrom(final Room roomR, final FalseLimb limb, final MOB mob, final int level)
	{
		String description=limb.description();
		final String undeadDesc=L("In undeath, it has decayed to a great degree.");
		if(description.trim().length()==0)
			description=undeadDesc;
		else
			description+="\n\r"+undeadDesc;

		final Race zombieR = CMClass.getRace("Zombie");
		final String raceID = "Zombie" + CMStrings.capitalizeAndLower(Race.BODYPARTSTR[limb.getBodyPartCode()]);
		if(CMClass.getRace(raceID) == null)
		{
			final Race R = (Race)CMClass.getRace("Zombie").makeGenRace().copyOf();
			R.setStat("ID", raceID);
			R.setStat("NAME", "Zombie " + CMStrings.capitalizeAndLower(Race.BODYPARTSTR[limb.getBodyPartCode()]));
			R.setStat("CAT", zombieR.racialCategory());
			R.setStat("BWEIGHT", ""+limb.basePhyStats().weight());
			R.setStat("VWEIGHT", "1");
			R.setStat("BHEIGHT", "5");
			R.setStat("FHEIGHT", "5");
			R.setStat("MHEIGHT", "5");
			switch(limb.getBodyPartCode())
			{
			case Race.BODY_ANTENNA:
			case Race.BODY_TAIL:
			case Race.BODY_GILL:
				R.setStat("ARRIVE", "slithers in");
				R.setStat("LEAVE", "slithers");
				break;
			case Race.BODY_WING:
				R.setStat("ARRIVE", "flaps in");
				R.setStat("LEAVE", "flaps");
				break;
			case Race.BODY_EYE:
			case Race.BODY_EAR:
			case Race.BODY_HEAD:
			case Race.BODY_NECK:
			case Race.BODY_NOSE:
				R.setStat("ARRIVE", "rolls in");
				R.setStat("LEAVE", "rolls");
				break;
			case Race.BODY_ARM:
			case Race.BODY_HAND:
			case Race.BODY_MOUTH:
				R.setStat("ARRIVE", "crawls in");
				R.setStat("LEAVE", "crawls");
				break;
			case Race.BODY_TORSO:
			case Race.BODY_WAIST:
				R.setStat("ARRIVE", "drags in");
				R.setStat("LEAVE", "drags");
				break;
			case Race.BODY_LEG:
			case Race.BODY_FOOT:
				R.setStat("ARRIVE", "hops in");
				R.setStat("LEAVE", "hops");
				break;
			}

			R.setStat("WEAPONRACE","");
			final Weapon natWeapon = CMClass.getWeapon("GenWeapon");
			natWeapon.setWeaponDamageType(Weapon.TYPE_BASHING);
			natWeapon.setWeaponClassification(Weapon.CLASS_NATURAL);
			switch(limb.getBodyPartCode())
			{
			case Race.BODY_ANTENNA:
			case Race.BODY_TAIL:
			case Race.BODY_WING:
				natWeapon.setName(L("@x1 whip",limb.Name()));
				break;
			case Race.BODY_EYE:
			case Race.BODY_EAR:
			case Race.BODY_HEAD:
			case Race.BODY_NECK:
			case Race.BODY_ARM:
			case Race.BODY_TORSO:
			case Race.BODY_NOSE:
			case Race.BODY_GILL:
			case Race.BODY_WAIST:
				natWeapon.setName(L("@x1 slam",limb.Name()));
				break;
			case Race.BODY_HAND:
				natWeapon.setName(L("a dirty claw"));
				break;
			case Race.BODY_LEG:
			case Race.BODY_FOOT:
				natWeapon.setName(L("a dirty kick"));
				break;
			case Race.BODY_MOUTH:
				natWeapon.setName(L("a nasty bite"));
				natWeapon.setWeaponDamageType(Weapon.TYPE_PIERCING);
				break;
			}
			final StringBuilder wstr = new StringBuilder("");
			wstr.append("<ITEMS>");
			wstr.append(CMLib.coffeeMaker().getItemXML(natWeapon));
			wstr.append("</ITEMS>");
			R.setStat("WEAPONXML", wstr.toString());
			final int[] bodyParts = Arrays.copyOf(R.bodyMask(), R.bodyMask().length);
			Arrays.fill(bodyParts, -1);
			final StringBuffer str=new StringBuffer("");
			for(int i=0;i<bodyParts.length;i++)
				str.append(bodyParts+";");
			R.setStat("BODY",str.toString());
			final long wearBits = ~Race.BODY_WEARVECTOR[limb.getBodyPartCode()];
			R.setStat("WEAR",wearBits+"");
			CMClass.addRace(R);
			CMLib.database().DBCreateRace(R.ID(),R.racialParms());
		}
		final Race undeadR = CMClass.getRace(raceID);
		final String undeadMobClassId = "GenUndead";
		final MOB newMOB=CMClass.getMOB(undeadMobClassId);
		newMOB.setDescription(description);
		newMOB.basePhyStats().setLevel(level);
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'N');
		newMOB.setName(CMLib.english().startWithAorAn(undeadR.name()));
		newMOB.setDisplayText(L("@x1 is here",newMOB.name()));
		newMOB.baseCharStats().setMyRace(undeadR);
		newMOB.charStats().setMyRace(undeadR);
		undeadR.startRacing(newMOB, false);
		newMOB.recoverCharStats();
		newMOB.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
		newMOB.basePhyStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
		CMLib.factions().setAlignment(newMOB,Faction.Align.EVIL);
		newMOB.baseState().setHitPoints(25*newMOB.basePhyStats().level());
		newMOB.baseState().setMovement(30);
		newMOB.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
		newMOB.baseState().setMana(0);
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience","0"));
		newMOB.addTattoo("SYSTEM_SUMMONED");
		newMOB.addTattoo("SUMMONED_BY:"+mob.name());
		newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		Behavior B=CMClass.getBehavior("CombatAbilities");
		if(B!=null)
			newMOB.addBehavior(B);
		B=CMClass.getBehavior("Aggressive");
		if((B!=null)&&(mob!=null))
		{
			B.setParms("CHECKLEVEL +NAMES \"-"+mob.Name()+"\"");
			newMOB.addBehavior(B);
		}
		newMOB.setMiscText(newMOB.text());
		newMOB.bringToLife(roomR,true);
		CMLib.beanCounter().clearZeroMoney(newMOB,null);
		newMOB.setMoneyVariation(0);
		newMOB.setStartRoom(null);
		return newMOB;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;

		if(target==mob)
		{
			mob.tell(L("@x1 doesn't look dead yet.",target.name(mob)));
			return false;
		}
		if((!(target instanceof FalseLimb))
		||((target instanceof Item)&&((((Item)target).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_FLESH)))
		{
			mob.tell(L("You can't animate @x1 with this magic.",target.name(mob)));
			return false;
		}

		final FalseLimb limb=(FalseLimb)target;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
					auto?"":L("^S<S-NAME> @x1 to animate <T-NAMESELF>.^?",prayForWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final int undeadLevel = getUndeadLevel(mob,2,limb.phyStats().level());
				final MOB newMOB = this.makeUndeadFrom(mob.location(), limb, mob, undeadLevel);
				limb.destroy();
				beneficialAffect(mob,newMOB,0,0);
				mob.location().show(newMOB,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> begin(s) to rise!"));
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 to animate <T-NAMESELF>, but fail(s) miserably.",prayForWord(mob)));

		// return whether it worked
		return success;
	}
}
