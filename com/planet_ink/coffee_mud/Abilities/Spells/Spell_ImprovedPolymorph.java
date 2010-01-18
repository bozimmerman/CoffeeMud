package com.planet_ink.coffee_mud.Abilities.Spells;
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
public class Spell_ImprovedPolymorph extends Spell
{
	public String ID() { return "Spell_ImprovedPolymorph"; }
	public String name(){return "Improved Polymorph";}
	public String displayText(){return "(Improved Polymorph)";}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_TRANSMUTATION;}
    public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}

	Race newRace=null;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(newRace!=null)
		{
			if(affected.name().indexOf(" ")>0)
				affectableStats.setName("a "+newRace.name()+" called "+affected.name());
			else
				affectableStats.setName(affected.name()+" the "+newRace.name());
			int oldAdd=affectableStats.weight()-affected.baseEnvStats().weight();
			newRace.setHeightWeight(affectableStats,'M');
			if(oldAdd>0) affectableStats.setWeight(affectableStats.weight()+oldAdd);
		}
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(newRace!=null)
		{
		    int oldCat=affected.baseCharStats().ageCategory();
			affectableStats.setMyRace(newRace);
			if(affected.baseCharStats().getStat(CharStats.STAT_AGE)>0)
				affectableStats.setStat(CharStats.STAT_AGE,newRace.getAgingChart()[oldCat]);
		}
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> morph(s) back to <S-HIS-HER> normal form.");
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(target instanceof MOB)
            {
                if((mob.getVictim()==target)&&(target.fetchEffect(ID())==null))
                    return Ability.QUALITY_MALICIOUS;
            }
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()==0)
		{
			mob.tell("You need to specify what to turn your target into!");
			return false;
		}
		String race=(String)commands.lastElement();
		commands.removeElement(commands.lastElement());
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if((target==mob)&&(!auto))
		{
			mob.tell("You cannot hold enough energy to cast this on yourself.");
			return false;
		}
		Race R=CMClass.getRace(race);
		if((R==null)&&(!auto))
		{
		    if(mob.isMonster())
		        R=CMClass.randomRace();
		    else
		    {
    			mob.tell("You can't turn "+target.name()+" into a '"+race+"'!");
    			return false;
		    }
		}
		else
		if(R==null)
			R=CMClass.randomRace();

		if(target.baseCharStats().getMyRace() != target.charStats().getMyRace())
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already polymorphed.");
			return false;
		}
		
		if((R!=null)&&(!CMath.bset(R.availabilityCode(),Area.THEME_FANTASY)))
		{
			mob.tell("You can't turn "+target.name()+" into a '"+R.name()+"'!");
			return false;
		}
		
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int targetStatTotal=0;
		MOB fakeMOB=CMClass.getMOB("StdMOB");
		for(int s: CharStats.CODES.BASE())
		{
			targetStatTotal+=target.baseCharStats().getStat(s);
			fakeMOB.baseCharStats().setStat(s,target.baseCharStats().getStat(s));
		}
		fakeMOB.baseCharStats().setMyRace(R);
		fakeMOB.recoverCharStats();
		fakeMOB.recoverEnvStats();
		fakeMOB.recoverMaxState();
		fakeMOB.recoverCharStats();
		fakeMOB.recoverEnvStats();
		fakeMOB.recoverMaxState();
		int fakeStatTotal=0;
		for(int s: CharStats.CODES.BASE())
			fakeStatTotal+=fakeMOB.charStats().getStat(s);

		int statDiff=targetStatTotal-fakeStatTotal;
        if(CMLib.flags().canMove(fakeMOB)!=CMLib.flags().canMove(target)) statDiff+=100;
        if(CMLib.flags().canBreathe(fakeMOB)!=CMLib.flags().canBreathe(target)) statDiff+=50;
        if(CMLib.flags().canSee(fakeMOB)!=CMLib.flags().canSee(target)) statDiff+=25;
        if(CMLib.flags().canHear(fakeMOB)!=CMLib.flags().canHear(target)) statDiff+=10;
        if(CMLib.flags().canSpeak(fakeMOB)!=CMLib.flags().canSpeak(target)) statDiff+=25;
        if(CMLib.flags().canSmell(fakeMOB)!=CMLib.flags().canSmell(target)) statDiff+=5;
        fakeMOB.destroy();
        
		if(statDiff<0) statDiff=statDiff*-1;
		int levelDiff=((mob.envStats().level()+(2*getXLEVELLevel(mob)))-target.envStats().level());
		boolean success=proficiencyCheck(mob,levelDiff-statDiff,auto);
		if(success&&(!auto)&&(!mob.mayIFight(target))&&(mob!=target)&&(!mob.getGroupMembers(new HashSet()).contains(target)))
		{
			mob.tell(target.name()+" is a player, so you must be group members, or your playerkill flags must be on for this to work.");
			success=false;
		}
		
		if((success)&&((auto)||((levelDiff-statDiff)>-100)))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> form(s) an improved spell around <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					newRace=R;
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> become(s) a "+newRace.name()+"!");
					success=beneficialAffect(mob,target,asLevel,0);
					target.recoverCharStats();
					CMLib.utensils().confirmWearability(target);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> form(s) an improved spell around <T-NAMESELF>, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
