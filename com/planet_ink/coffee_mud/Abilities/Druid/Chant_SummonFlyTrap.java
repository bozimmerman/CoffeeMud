package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class Chant_SummonFlyTrap extends Chant
{
	public String ID() { return "Chant_SummonFlyTrap"; }
	public String name(){ return "Summon FlyTrap";}
	public String displayText(){return "(Summon FlyTrap)";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTGROWTH;}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public long flags(){return Ability.FLAG_SUMMONING;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(((MOB)affected).location()!=null))
			{
				MOB mob=(MOB)affected;
				Room R=mob.location();
				for(int r=0;r<R.numItems();r++)
				{
					Item I=R.fetchItem(r);
					if((I!=null)
					&&(I instanceof DeadBody)
					&&(((DeadBody)I).charStats()!=null)
					&&(((DeadBody)I).charStats().getMyRace()!=null))
					{
						String raceCat=((DeadBody)I).charStats().getMyRace().racialCategory();
						if(raceCat.equals("Insect")||raceCat.equals("Arachnid"))
						{
							if(R.show(mob,I,CMMsg.MSG_HANDS|CMMsg.MASK_SOUND,"<S-NAME> devour(s) <T-NAMESELF>."))
							{
								I.destroy();
								break;
							}
						}
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.location()!=null)
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> shrivels up and dies.");
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
		}
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            Room R=mob.location();
            if(R!=null)
            {
                if((R.domainType()&Room.INDOORS)>0)
                    return Ability.QUALITY_INDIFFERENT;
                if((R.domainType()==Room.DOMAIN_OUTDOORS_CITY)
                ||(R.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
                ||(R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
                ||(R.domainType()==Room.DOMAIN_OUTDOORS_AIR)
                ||(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
                    return Ability.QUALITY_INDIFFERENT;
                if(!mob.isInCombat())
                    return Ability.QUALITY_INDIFFERENT;
                
            }
        }
        return super.castingQuality(mob,target);
    }

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((!auto)&&(mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}

		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell("This magic will not work here.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> chant(s) to the fertile ground.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB target = determineMonster(mob);
				beneficialAffect(mob,target,asLevel,0);
				CMLib.commands().postFollow(target,mob,true);
				if(target.amFollowing()!=mob)
					mob.tell(target.name()+" seems unwilling to follow you.");
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s), but nothing happens.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster)
	{
		MOB newMOB=CMClass.getMOB("GenMOB");
		int level=adjustedLevel(caster,0);
		if(level<1) level=1;
		newMOB.baseEnvStats().setLevel(level);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Vine"));
		newMOB.setName("a large flytrap");
		newMOB.setDisplayText(newMOB.Name()+" is planted here");
		newMOB.setDescription("");
		CMLib.factions().setAlignment(newMOB,Faction.ALIGN_NEUTRAL);
		newMOB.recoverEnvStats();
		newMOB.recoverCharStats();
		Behavior B=CMClass.getBehavior("Aggressive");
		B.setParms("mobkiller -RACE +Insect +Arachnid");
		newMOB.addBehavior(B);
		newMOB.baseEnvStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB)-(10*super.getX1Level(caster)));
		newMOB.baseEnvStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
		newMOB.baseEnvStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
		newMOB.baseEnvStats().setSpeed(CMLib.leveler().getLevelMOBSpeed(newMOB));
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'N');
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		newMOB.baseEnvStats().setSensesMask(newMOB.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_DARK);
		newMOB.setLocation(caster.location());
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.setBitmap(MOB.ATT_AUTOASSIST);
		newMOB.setMiscText(newMOB.text());
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(caster.location(),true);
		CMLib.beanCounter().clearZeroMoney(newMOB,null);
		newMOB.location().show(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> grow(s) from the ground.");
		newMOB.setStartRoom(null);
		return(newMOB);
	}
}
