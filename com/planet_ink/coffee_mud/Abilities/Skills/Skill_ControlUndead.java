package com.planet_ink.coffee_mud.Abilities.Skills;
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
public class Skill_ControlUndead extends StdSkill
{
	public String ID() { return "Skill_ControlUndead"; }
	public String name(){ return "Control Undead";}
	public String displayText(){ return "(Controlled)";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_DEATHLORE;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"CONTROL"};
	public String[] triggerStrings(){return triggerStrings;}

    protected MOB charmer=null;
    protected MOB getCharmer()
    {
        if(charmer!=null) return charmer;
        if((invoker!=null)&&(invoker!=affected))
            charmer=invoker;
        else
        if((text().length()>0)&&(affected instanceof MOB))
        {
            Room R=((MOB)affected).location();
            if(R!=null)
                charmer=R.fetchInhabitant(text());
        }
        if(charmer==null) return invoker;
        return charmer;
    }


    public void executeMsg(Environmental myHost, CMMsg msg)
    {
        super.executeMsg(myHost,msg);
        if((affected!=null)
        &&(affected instanceof MOB)
        &&(!((MOB)affected).isMonster())
        &&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing()))
        &&(msg.sourceMinor()==CMMsg.TYP_QUIT))
        {
            unInvoke();
            if(msg.source().playerStats()!=null) msg.source().playerStats().setLastUpdated(0);
        }
    }
    
    public boolean okMessage(Environmental myHost, CMMsg msg)
    {
        if((affected==null)||(!(affected instanceof MOB)))
            return true;

        MOB mob=(MOB)affected;

        // when this spell is on a MOBs Affected list,
        // it should consistantly prevent the mob
        // from trying to do ANYTHING except sleep
        if((msg.amITarget(mob))
        &&(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
        &&(msg.amISource(mob.amFollowing())))
            unInvoke();
        else
        if((msg.amISource(mob))
        &&(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
        &&(msg.amITarget(mob.amFollowing())))
        {
            if((!invoker().isInCombat())&&(msg.source().getVictim()!=invoker()))
            {
                msg.source().tell("You're too submissive towards "+invoker().name());
                if(invoker().getVictim()==msg.source())
                {
                    invoker().makePeace();
                    invoker().setVictim(null);
                }
            }
            else
                msg.source().tell("You're too submissive towards "+invoker().name());
            return false;
        }
        else
        if(!mob.isMonster())
        {
            if((msg.amISource(mob))
            &&(!mob.isMonster())
            &&(msg.target() instanceof Room)
            &&((msg.targetMinor()==CMMsg.TYP_LEAVE)||(msg.sourceMinor()==CMMsg.TYP_RECALL))
            &&(mob.amFollowing()!=null)
            &&(((Room)msg.target()).isInhabitant(mob.amFollowing())))
            {
                mob.tell("You don't want to leave your master.");
                return false;
            }
            else
            if((msg.amISource(mob))
            &&(mob.amFollowing()!=null)
            &&(msg.sourceMinor()==CMMsg.TYP_NOFOLLOW))
            {
                msg.source().tell("You're too submissive towards "+invoker().name());
                return false;
            }
        }

        return super.okMessage(myHost,msg);
    }

    public boolean tick(Tickable ticking, int tickID)
    {
        if((affecting()==null)||(!(affecting() instanceof MOB)))
            return false;
        MOB mob=(MOB)affecting();
        
        if(mob.isMonster()) 
            return super.tick(ticking,tickID);
        
        if((getCharmer()!=null)
        &&(!CMLib.flags().isInTheGame(getCharmer(),false)))
            unInvoke();
        else
        if((affected==mob)
        &&(mob!=getCharmer())
        &&((mob.amFollowing()==null)||(mob.amFollowing()!=getCharmer()))
        &&(mob.location()!=null)
        &&(mob.location().isInhabitant(getCharmer())))
            CMLib.commands().postFollow(mob,getCharmer(),true);
        return super.tick(ticking,tickID);
    }

    public void unInvoke()
    {
        // undo the affects of this spell
        if((affected==null)||(!(affected instanceof MOB)))
            return;
        MOB mob=(MOB)affected;

        super.unInvoke();

        if((canBeUninvoked()&&(!mob.amDead())))
        {
            mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> free-will returns.");
            if(mob.amFollowing()!=null)
                CMLib.commands().postFollow(mob,null,false);
            CMLib.commands().postStand(mob,true);
            if(mob.isMonster())
            {
                if((CMLib.dice().rollPercentage()>50)
                ||((mob.getStartRoom()!=null)
                    &&(mob.getStartRoom().getArea()!=mob.location().getArea())
                    &&(CMLib.flags().isAggressiveTo(mob,null)||(invoker==null)||(!mob.location().isInhabitant(invoker)))))
                    CMLib.tracking().wanderAway(mob,true,true);
                else
                if((invoker!=null)&&(invoker!=mob))
                    mob.setVictim(invoker);
            }
        }
    }

    public int castingQuality(MOB mob, Environmental target)
    {
        if((mob!=null)&&(target!=null))
        {
            if(!(target instanceof MOB)) return Ability.QUALITY_INDIFFERENT;
            MOB targetM=(MOB)target;
            if((targetM.baseCharStats().getMyRace()==null)
            ||(!targetM.baseCharStats().getMyRace().racialCategory().equals("Undead")))
                return Ability.QUALITY_INDIFFERENT;
            if(CMLib.flags().isGood(mob))
                return Ability.QUALITY_INDIFFERENT;
            if((mob.isMonster())&&(((MOB)target).isMonster()))
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((target.baseCharStats().getMyRace()==null)
		||(!target.baseCharStats().getMyRace().racialCategory().equals("Undead")))
		{
			mob.tell(auto?"Only the undead can be controlled.":"You can only control the undead.");
			return false;
		}

		if(CMLib.flags().isGood(mob))
		{
			mob.tell("Only the wicked may control the undead.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,((mob.envStats().level()+(2*getXLEVELLevel(mob)))-target.envStats().level())*30,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_CAST_ATTACK_SOMANTIC_SPELL|(auto?CMMsg.MASK_ALWAYS:0),auto?"<T-NAME> seem(s) controlled.":"^S<S-NAME> control(s) <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					if((mob.envStats().level()-target.envStats().level())>6)
					{
                        if(!target.isMonster())
                            success=maliciousAffect(mob,target,asLevel,0,CMMsg.MSK_CAST_VERBAL|CMMsg.TYP_MIND|CMMsg.MASK_ALWAYS);
                        if(success) {
    						mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> now controlled.");
    						target.makePeace();
    						CMLib.commands().postFollow(target,mob,false);
    						CMLib.combat().makePeaceInGroup(mob);
    						invoker=mob;
    						if(target.amFollowing()!=mob)
    							mob.tell(target.name()+" seems unwilling to obey you.");
                        }
					}
					else
					{
						mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) submissive!");
						target.makePeace();
						beneficialAffect(mob,target,asLevel,5);
					}

				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to control <T-NAMESELF>, but fail(s).");


		// return whether it worked
		return success;
	}
}
