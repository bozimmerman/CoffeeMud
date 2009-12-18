package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Druid_PackCall extends StdAbility
{
	public String ID() { return "Druid_PackCall"; }
	public String name(){ return "Pack Call";}
	public String displayText(){return "(Pack Call)";}
	private static final String[] triggerStrings = {"PACKCALL"};
	public String[] triggerStrings(){return triggerStrings;}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_ANIMALAFFINITY;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(invoker!=null))
			{
				MOB mob=(MOB)affected;
				if(((mob.amFollowing()==null)
				||(mob.amDead())
				||(!mob.isInCombat())
				||(mob.location()!=invoker.location())))
					unInvoke();
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
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> wander(s) off.");
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
		}
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
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
                ||(R.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT))
                    return Ability.QUALITY_INDIFFERENT;
            }
            if(target instanceof MOB)
            {
                if(!(((MOB)target).isInCombat()))
                    return Ability.QUALITY_INDIFFERENT;
                
                if(!Druid_ShapeShift.isShapeShifted((MOB)target))
                    return Ability.QUALITY_INDIFFERENT;
                
                if(((MOB)target).totalFollowers()>=((MOB)target).maxFollowers())
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob,target);
    }

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors to call your pack.");
			return false;
		}
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT))
		{
			mob.tell("You must be in the wild to call your pack.");
			return false;
		}
		if(!mob.isInCombat())
		{
			mob.tell("Only the anger of combat can call your pack.");
			return false;
		}
		Druid_ShapeShift D=null;
		for(int a=0;a<mob.numAllEffects();a++)
		{
			Ability A=mob.fetchEffect(a);
			if((A!=null)&&(A instanceof Druid_ShapeShift))
				D=(Druid_ShapeShift)A;
		}
		if(D==null)
		{
			mob.tell("You must be in your animal form to call the pack.");
			return false;
		}

		if(mob.totalFollowers()>=mob.maxFollowers())
		{
			mob.tell("You can't have any more followers!");
			return false;
		}

		Vector choices=new Vector();
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			Room R=mob.location().getRoomInDir(d);
			Exit E=mob.location().getExitInDir(d);
			if((R!=null)&&(E!=null)&&(E.isOpen())&&(d!=Directions.UP))
				choices.addElement(Integer.valueOf(d));
		}

		if(choices.size()==0)
		{
			mob.tell("Your call would not be heard here.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISE,auto?"":"^S<S-NAME> call(s) for help from <S-HIS-HER> pack!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int levelsRemaining=90+(10*getXLEVELLevel(mob));
				while((mob.totalFollowers()<mob.maxFollowers())&&(levelsRemaining>0))
				{
					MOB victim=mob.getVictim();
					MOB newMOB=CMClass.getMOB("GenMOB");
					int MOBRaceCode=D.myRaceCode;
					if(D.raceName==null) D.setRaceName(mob);
					int level=1;
					while(!D.raceName.equals(D.getRaceName(level,MOBRaceCode)))
						level++;
					level--;
					newMOB.baseEnvStats().setLevel(level);
					levelsRemaining-=level;
					if(levelsRemaining<0) break;
					newMOB.baseCharStats().setMyRace(D.getRace(level,MOBRaceCode));
					String raceName=D.getRaceName(level,MOBRaceCode).toLowerCase();
					String name=CMLib.english().startWithAorAn(raceName).toLowerCase();
					newMOB.setName(name);
					newMOB.setDisplayText("a loyal "+raceName+" is here");
					newMOB.setDescription("");
					newMOB.copyFactions(mob);
					Ability A=CMClass.getAbility("Fighter_Rescue");
					A.setProficiency(100);
					newMOB.addAbility(A);
					newMOB.setVictim(victim);
					newMOB.setLocation(mob.location());
					newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
					newMOB.recoverEnvStats();
					newMOB.baseEnvStats().setAbility(newMOB.baseEnvStats().ability()*2);
					newMOB.baseEnvStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
					newMOB.baseEnvStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
					newMOB.baseEnvStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
					newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
					newMOB.setMiscText(newMOB.text());
					newMOB.recoverEnvStats();
					newMOB.recoverCharStats();
					newMOB.recoverMaxState();
					newMOB.resetToMaxState();
					newMOB.bringToLife(mob.location(),true);
					CMLib.beanCounter().clearZeroMoney(newMOB,null);
					if(victim.getVictim()!=newMOB) victim.setVictim(newMOB);
					int dir=((Integer)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1))).intValue();
                    if(newMOB.getVictim()!=victim) newMOB.setVictim(victim);
					newMOB.location().showOthers(newMOB,victim,CMMsg.MSG_OK_ACTION,"<S-NAME> arrive(s) "+Directions.getFromDirectionName(dir)+" and attack(s) <T-NAMESELF>!");
					newMOB.setStartRoom(null); // keep before postFollow for Conquest
					CMLib.commands().postFollow(newMOB,mob,true);
					if(newMOB.amFollowing()!=mob)
					{
						newMOB.destroy();
						break;
					}
					beneficialAffect(mob,newMOB,asLevel,0);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> call(s) for help from <S-HIS-HER> pack, but nothing happens.");

		// return whether it worked
		return success;
	}
}
