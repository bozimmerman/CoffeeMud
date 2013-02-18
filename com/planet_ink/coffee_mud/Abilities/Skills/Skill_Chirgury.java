package com.planet_ink.coffee_mud.Abilities.Skills;
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
   Copyright 2000-2013 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Skill_Chirgury extends StdSkill
{
	public String ID() { return "Skill_Chirgury"; }
	public String name(){ return "Chirurgy";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS|Ability.CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"CHIRURGY"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){ return Ability.ACODE_SKILL|Ability.DOMAIN_ANATOMY;}
	public static final Object[][] parts={{"FETUS"},
										  {"BLOOD"},
										  {"HEART",Integer.valueOf(Race.BODY_TORSO)},
										  {"LUNGS",Integer.valueOf(Race.BODY_TORSO)},
										  {"STOMACH",Integer.valueOf(Race.BODY_TORSO)},
										  {"PANCREAS",Integer.valueOf(Race.BODY_TORSO)},
										  {"SPLEEN",Integer.valueOf(Race.BODY_TORSO)},
										  {"BRAIN",Integer.valueOf(Race.BODY_HEAD)},
										  {"LIVER",Integer.valueOf(Race.BODY_TORSO)},
										  {"INTESTINES",Integer.valueOf(Race.BODY_TORSO)},
										  {"TONGUE",Integer.valueOf(Race.BODY_MOUTH)},
										  {"EYES",Integer.valueOf(Race.BODY_EYE)},
										  {"BLADDER",Integer.valueOf(Race.BODY_TORSO)}};
	
	public static final String[] badRaceCats={"Earth Elemental","Fire Elemental", "Water Elemental", "Air Elemental", "Unique", 
											  "Slime", "Vegetation", "Metal Golem", "Wood Golem", "Electricity Elemental", "Stone Golem",
											  "Stone Golem", "Unknown"};

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()==0)
		{
			mob.tell("Remove what from whom? Parts include: "+CMParms.toStringList(parts));
			return false;
			
		}
		String part=(String)commands.firstElement();
		commands.removeElementAt(0);
		int partCode=-1;
		Object[] partSet=new Object[1];
		for(int i=0;i<parts.length;i++)
		{
			if(parts[i][0].toString().startsWith(part.toUpperCase()))
			{
				partCode=i;
				partSet=parts[i];
				break;
			}
		}
		if(partCode<0)
		{
			StringBuilder str=new StringBuilder("");
			for(Object[] o : parts)
				str.append(", ").append(o[0].toString());
			mob.tell("'"+part+"' is not a valid part to remove.  Try one of these: "+str.toString().substring(2));
			return false;
		}
		Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY,false,true);
		if(target==null) return false;

		CharStats C=null;
		if(target instanceof MOB) C=((MOB)target).charStats();
		if(target instanceof DeadBody) C=((DeadBody)target).charStats();

		if((partSet[1] instanceof Integer)
		&&(C!=null) && (C.getMyRace().bodyMask()[((Integer)partSet[1]).intValue()]<=0))
		{
			mob.tell(target.name()+" doesn't have a "+part);
			return false;
		}

		if(C!=null)
			for(String raceCat : badRaceCats)
				if(C.getMyRace().racialCategory().equalsIgnoreCase(raceCat))
				{
					mob.tell(target.name()+" doesn't have a "+part);
					return false;
				}

		if((partCode==0)&&((!(target instanceof MOB))||(target.fetchEffect("Pregnancy")==null)))
		{
			mob.tell("A baby can not be removed from "+target.name()+".");
			return false;
		}

		if((target instanceof MOB)&&((!CMLib.flags().isBoundOrHeld(target))||(!CMLib.flags().isSleeping(target))))
		{
			mob.tell(((MOB)target).charStats().HeShe()+" must be bound, and asleep on an operating bed before you can perform chirurgy.");
			return false;
		}

		if((partCode>1)&&(!(target instanceof DeadBody)))
		{
			mob.tell("That can only be removed from a corpse.");
			return false;
		}

		Ability oldChirge=target.fetchEffect(ID());
		if((oldChirge!=null)&&(oldChirge.text().indexOf(";"+parts[partCode])>=0))
		{
			mob.tell("That has already been removed from "+target.name()+".");
			return false;
		}

		Item w=mob.fetchWieldedItem();
		Weapon ww=null;
		if((w==null)||(!(w instanceof Weapon)))
		{
			mob.tell("You cannot perform chirurgy without a weapon!");
			return false;
		}

		ww=(Weapon)w;
		if((ww.weaponType()!=Weapon.TYPE_PIERCING)&&(ww.weaponType()!=Weapon.TYPE_SLASHING))
		{
			mob.tell("You cannot perform chirurgy with a "+ww.name()+"!");
			return false;
		}

		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to try that!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT|(auto?CMMsg.MASK_ALWAYS:0),auto?"":"^S<S-NAME> carefully perform(s) chirurgy upon <T-NAME>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(oldChirge==null)
				{
					oldChirge=(Ability)copyOf();
					oldChirge.setMiscText("");
					if(target instanceof Item)
						target.addNonUninvokableEffect(oldChirge);
				}
				if(partCode>0)
				{
					oldChirge.setMiscText(oldChirge.text()+";"+parts[partCode]);
					Item meat=null;
					int amt=1;
					if(partCode>1)
					{
						meat=CMClass.getItem("GenFoodResource");
						meat.setMaterial(RawMaterial.RESOURCE_MEAT);
					}
					else
					{
						meat=CMClass.getItem("GenLiquidResource");
						meat.setMaterial(RawMaterial.RESOURCE_BLOOD);
						((Drink)meat).setLiquidType(RawMaterial.RESOURCE_BLOOD);
						amt=target.phyStats().weight()/10;
						if(amt<1) amt=1;
						meat.basePhyStats().setWeight(1);
						((Drink)meat).setLiquidHeld(10);
						((Drink)meat).setLiquidRemaining(10);
						if(target instanceof MOB)
							CMLib.combat().postDamage(mob,(MOB)target,this,amt*3,CMMsg.MASK_ALWAYS|CMMsg.TYP_DISEASE,-1,"The bleeding <DAMAGE> <T-NAME>!");
					}
					meat.setName("the "+parts[partCode][0].toString().toLowerCase()+" of "+target.Name());
					if((parts[partCode][0].toString().endsWith("S"))&&(!parts[partCode][0].toString().equalsIgnoreCase("PANCREAS")))
						meat.setDisplayText("the "+parts[partCode][0].toString().toLowerCase()+" of "+target.Name()+" lie here.");
					else
						meat.setDisplayText("the "+parts[partCode][0].toString().toLowerCase()+" of "+target.Name()+" lies here.");
					CMLib.materials().addEffectsToResource(meat);
					meat.recoverPhyStats();
					meat.text();
					for(int i=0;i<amt;i++)
					{
						meat=(Item)meat.copyOf();
						meat.recoverPhyStats();
						meat.text();
						mob.location().addItem(meat,ItemPossessor.Expire.Player_Drop);
						mob.location().show(mob,meat,null,CMMsg.MSG_GET,(i==0)?"<S-NAME> remove(s) <T-NAME> from "+target.name()+".":null);
					}
				}
				else
				{
					Ability preg=target.fetchEffect("Pregnancy");
					if(preg!=null)
					{
						preg.unInvoke();
						target.delEffect(preg);
						preg.setAffectedOne(null);
						DeadBody baby=(DeadBody)CMClass.getItem("GenCorpse");
						baby.setName(target.Name()+"'s bloody fetus");
						baby.setDisplayText(target.Name()+"'s bloody fetus is lying here.");
						baby.setTimeOfDeath(System.currentTimeMillis());
						baby.setDestroyAfterLooting(false);
						baby.setKillerName(mob.Name());
						baby.setKillingTool(ww);
						baby.setKillerPlayer(!mob.isMonster());
						baby.setMobPKFlag(false);
						baby.setMobName(baby.Name());
						baby.setPlayerCorpse(false);
						baby.basePhyStats().setWeight(1);
						baby.charStats().setStat(CharStats.STAT_GENDER,(CMLib.dice().rollPercentage()>50)?'F':'M');
						for(int i: CharStats.CODES.BASE())
							baby.charStats().setStat(i,1);
						for(int i: CharStats.CODES.MAX())
							baby.charStats().setStat(i,1);
						baby.charStats().setMyRace(((MOB)target).charStats().getMyRace());
						baby.recoverPhyStats();
						baby.setDescription(CMStrings.capitalizeAndLower(baby.charStats().hisher())+" body parts can be faintly made out in the twisted and mangled flesh.");
						baby.setMobDescription(baby.description());
						baby.text();
						mob.location().addItem(baby,ItemPossessor.Expire.Player_Drop);
						mob.location().show(mob,baby,null,CMMsg.MSG_GET,"<S-NAME> remove(s) <T-NAME> from "+target.name()+".");
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,auto?"":"<S-NAME> attempt(s) to cut open <T-NAME>, but lose(s) concentration.");


		// return whether it worked
		return success;
	}
}
