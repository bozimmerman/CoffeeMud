package com.planet_ink.coffee_mud.Abilities.Skills;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Skill_Chirgury extends StdAbility
{
	public String ID() { return "Skill_Chirgury"; }
	public String name(){ return "Chirgury";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS|Ability.CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"CHIRGURY"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public static final String[] parts={"FETUS","BLOOD","HEART","LUNGS","STOMACH","PANCREAS","SPLEEN","BRAIN","LIVER","INTESTINES","TONGUE","EYES","BLADDER"};

	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		if(student.fetchAbility("Butchering")==null)
		{
			teacher.tell(student.name()+" has not yet learned Butchering.");
			student.tell("You need to learn Butchering before you can learn "+name()+".");
			return false;
		}

		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
	    if(commands.size()==0)
	    {
	        mob.tell("Remove what from whom? Parts include: "+Util.toStringList(parts));
	        return false;
	        
	    }
	    String part=(String)commands.firstElement();
	    commands.removeElementAt(0);
	    int partCode=-1;
	    for(int i=0;i<parts.length;i++)
	    {
	        if(parts[i].startsWith(part.toUpperCase()))
	            partCode=i;
	    }
	    if(partCode<0)
	    {
	        mob.tell("'"+part+"' is not a valid part to remove.  Try one of these: "+Util.toStringList(parts));
	        return false;
	    }
	    
		Environmental target=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_UNWORNONLY,false,true);
		if(target==null) return false;

		if((partCode==0)&&((!(target instanceof MOB))||(target.fetchEffect("Pregnancy")==null)))
		{
		    mob.tell("A baby can not be removed from "+target.name()+".");
		    return false;
		}
		if((target instanceof MOB)&&((!Sense.isBoundOrHeld(target))||(!Sense.isSleeping(target))))
		{
			mob.tell(((MOB)target).charStats().HeShe()+" must be bound, and asleep on an operating bed before you can perform chirgury.");
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
			mob.tell("You cannot perform chirgury without a weapon!");
			return false;
		}
		ww=(Weapon)w;
		if((ww.weaponType()!=Weapon.TYPE_PIERCING)&&(ww.weaponType()!=Weapon.TYPE_SLASHING))
		{
			mob.tell("You cannot perform chirgury with a "+ww.name()+"!");
			return false;
		}
		
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to try that!");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT|(auto?CMMsg.MASK_GENERAL:0),auto?"":"^S<S-NAME> carefully perform(s) chirgury upon <T-NAME>.^?");
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
				        meat.setMaterial(EnvResource.RESOURCE_MEAT);
				    }
				    else
				    {
				        meat=CMClass.getItem("GenLiquidResource");
				        meat.setMaterial(EnvResource.RESOURCE_BLOOD);
				        ((Drink)meat).setLiquidType(EnvResource.RESOURCE_BLOOD);
				        amt=target.envStats().weight()/10;
				        if(amt<1) amt=1;
				        meat.baseEnvStats().setWeight(1);
				        ((Drink)meat).setLiquidHeld(10);
				        ((Drink)meat).setLiquidRemaining(10);
				        if(target instanceof MOB)
							MUDFight.postDamage(mob,(MOB)target,this,amt*3,CMMsg.MASK_GENERAL|CMMsg.TYP_DISEASE,-1,"The bleeding <DAMAGE> <T-NAME>!");
				    }
				    meat.setName("the "+parts[partCode].toLowerCase()+" of "+target.Name());
				    if((parts[partCode].endsWith("S"))&&(!parts[partCode].equalsIgnoreCase("PANCREAS")))
					    meat.setDisplayText("the "+parts[partCode].toLowerCase()+" of "+target.Name()+" lie here.");
				    else
					    meat.setDisplayText("the "+parts[partCode].toLowerCase()+" of "+target.Name()+" lies here.");
				    meat.recoverEnvStats();
				    meat.text();
				    for(int i=0;i<amt;i++)
				    {
				        meat=(Item)meat.copyOf();
					    meat.recoverEnvStats();
					    meat.text();
				        mob.location().addItemRefuse(meat,Item.REFUSE_PLAYER_DROP);
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
				        baby.baseEnvStats().setWeight(1);
				        baby.charStats().setStat(CharStats.GENDER,(Dice.rollPercentage()>50)?'F':'M');
				        for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
				            baby.charStats().setStat(i,1);
				        for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
				            baby.charStats().setStat(CharStats.MAX_STRENGTH_ADJ+i,1);
				        baby.charStats().setMyRace(((MOB)target).charStats().getMyRace());
				        baby.recoverEnvStats();
				        baby.setDescription(Util.capitalize(baby.charStats().hisher())+" body parts can be faintly made out in the twisted and mangled flesh.");
				        baby.setMobDescription(baby.description());
				        baby.text();
				        mob.location().addItemRefuse(baby,Item.REFUSE_PLAYER_DROP);
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
