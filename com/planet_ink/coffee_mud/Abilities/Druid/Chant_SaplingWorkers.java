package com.planet_ink.coffee_mud.Abilities.Druid;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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

@SuppressWarnings({"unchecked","rawtypes"})
public class Chant_SaplingWorkers extends Chant
{
	@Override public String ID() { return "Chant_SaplingWorkers"; }
	@Override public String name(){ return "Sapling Workers";}
	@Override public String displayText(){return "(Sapling Workers)";}
	@Override public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTCONTROL;}
	@Override public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	@Override public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override protected int canTargetCode(){return 0;}
	@Override public long flags(){return Ability.FLAG_SUMMONING;}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(invoker!=null))
			{
				final MOB mob=(MOB)affected;
				if(((mob.amFollowing()==null)
				||(mob.amDead())
				||(mob.location()!=invoker.location())))
					unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)))
		{
			if(msg.sourceMinor()==CMMsg.TYP_DEATH)
			{
				unInvoke();
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void unInvoke()
	{
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.location()!=null)
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,_("<S-NAME> grow(s) still and tree-like."));
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing()))
		&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
		{
			unInvoke();
			if(msg.source().playerStats()!=null) msg.source().playerStats().setLastUpdated(0);
		}
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			final Room R=mob.location();
			if(R!=null)
			{
				if((R.domainType()!=Room.DOMAIN_OUTDOORS_WOODS)
				&&((R.myResource()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN)
				&&(R.domainType()!=Room.DOMAIN_OUTDOORS_JUNGLE))
					return Ability.QUALITY_INDIFFERENT;
			}
			if(target instanceof MOB)
			{
				if(((MOB)target).isInCombat())
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((mob.location().domainType()!=Room.DOMAIN_OUTDOORS_WOODS)
		&&((mob.location().myResource()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_JUNGLE))
		{
			mob.tell(_("This magic will not work here."));
			return false;
		}
		int material=RawMaterial.RESOURCE_OAK;
		if((mob.location().myResource()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN)
			material=mob.location().myResource();
		else
		{
			final List<Integer> V=mob.location().resourceChoices();
			final Vector V2=new Vector();
			if(V!=null)
			for(int v=0;v<V.size();v++)
			{
				if((V.get(v).intValue()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN)
					V2.addElement(V.get(v));
			}
			if(V2.size()>0)
				material=((Integer)V2.elementAt(CMLib.dice().roll(1,V2.size(),-1))).intValue();
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> chant(s) to the trees.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final MOB target = determineMonster(mob, material);
				beneficialAffect(mob,target,asLevel,0);
				CMLib.commands().postFollow(target,mob,true);
				if(target.amFollowing()!=mob)
					mob.tell(target.name(mob)+" seems unwilling to follow you.");
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s), but nothing happens.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int material)
	{
		final MOB newMOB=CMClass.getMOB("GenMOB");
		int level=adjustedLevel(caster,0)-6;
		if(level<1) level=1;
		newMOB.basePhyStats().setLevel(level);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("TreeGolem"));
		final String resourceName=RawMaterial.CODES.NAME(material).toLowerCase();
		String name=resourceName+" sapling";
		name=CMLib.english().startWithAorAn(name).toLowerCase();
		newMOB.setName(name);
		Ability A=null;
		boolean start=false;
		switch(CMLib.dice().roll(1,7,0))
		{
		case 1:
			newMOB.setDisplayText(name+" has an eye for foraging.");
			A=CMClass.getAbility("Foraging");
			start=true;
			break;
		case 2:
			newMOB.setDisplayText(name+" is a humble farmer.");
			A=CMClass.getAbility("Farming");
			break;
		case 3:
			newMOB.setDisplayText(name+" is an accomplished tailor.");
			A=CMClass.getAbility("Tailoring");
			break;
		case 4:
			newMOB.setDisplayText(name+" has some leather tools.");
			A=CMClass.getAbility("LeatherWorking");
			break;
		case 5:
			newMOB.setDisplayText(name+" is ready to butcher a corpse.");
			A=CMClass.getAbility("Butchering");
			break;
		case 6:
			newMOB.setDisplayText(name+" knows scrimshawing.");
			A=CMClass.getAbility("ScrimShaw");
			break;
		case 7:
			newMOB.setDisplayText(name+" has some sculpting tools.");
			A=CMClass.getAbility("Sculpting");
			break;
		}
		if(A!=null)
		{
			A.setProficiency(100);
			newMOB.addAbility(A);
		}
		newMOB.setDescription("");
		CMLib.factions().setAlignment(newMOB,Faction.Align.NEUTRAL);
		newMOB.recoverPhyStats();
		newMOB.recoverCharStats();
		newMOB.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
		newMOB.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
		newMOB.basePhyStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'N');
		newMOB.basePhyStats().setSensesMask(newMOB.basePhyStats().sensesMask()|PhyStats.CAN_SEE_DARK);
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		newMOB.setLocation(caster.location());
		newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		newMOB.setMiscText(newMOB.text());
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(caster.location(),true);
		CMLib.beanCounter().clearZeroMoney(newMOB,null);
		newMOB.setBitmap(MOB.ATT_AUTOASSIST);
		newMOB.setStartRoom(null);
		newMOB.location().show(newMOB,null,CMMsg.MSG_OK_ACTION,_("<S-NAME> start(s) looking around!"));
		if((start)&&(A!=null)) A.invoke(newMOB,null,false,0);
		return(newMOB);
	}
}
