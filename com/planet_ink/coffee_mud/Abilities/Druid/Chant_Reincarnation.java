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
public class Chant_Reincarnation extends Chant
{
	public String ID() { return "Chant_Reincarnation"; }
	public String name(){ return "Reincarnation";}
	public String displayText(){return "(Reincarnation Geas)";}
    public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_BREEDING;}
	public int abstractQuality(){return Ability.QUALITY_OK_OTHERS;}
	public boolean canBeUninvoked(){return false;}
	protected int overrideMana(){return 200;}

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
			affectableStats.setMyRace(newRace);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)
		&&(tickDown!=Integer.MAX_VALUE)
		&&((--tickDown)<0))
		{
			tickDown=-1;

			// undo the affects of this spell
			if((affected==null)||(!(affected instanceof MOB)))
				return super.tick(ticking,tickID);
			MOB mob=(MOB)affected;
			mob.tell("Your reincarnation geas is lifted as your form solidifies.");
			if(newRace!=null)
				mob.baseCharStats().setMyRace(newRace);
			mob.delEffect(this);
			if(mob.location()!=null)
				mob.location().recoverRoomStats();
			else
			{
				mob.recoverEnvStats();
				mob.recoverCharStats();
				mob.recoverMaxState();
			}
			return false;
		}
		return super.tick(ticking,tickID);
	}

	public boolean isGolem(Race R)
	{
		MOB M=(MOB)CMClass.sampleMOB().copyOf();
		R.affectEnvStats(M,M.envStats());
		return CMLib.flags().isGolem(M);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if((msg.sourceMinor()==CMMsg.TYP_DEATH)
		   &&(msg.amISource(mob)))
		{
			newRace=null;
			while((newRace==null)
			||(isGolem(newRace))
			||(!newRace.fertile())
			||(!CMath.bset(newRace.availabilityCode(),Area.THEME_FANTASY))
			||(newRace.ID().equals("StdRace")))
				newRace=CMClass.randomRace();
			if(newRace!=null)
				mob.tell("You are being reincarnated as a "+newRace.name()+"!!");
			msg.source().recoverCharStats();
			msg.source().recoverEnvStats();
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget,false,true);
		if(target==null) return false;
        if(target.fetchEffect(ID())!=null)
        {
            if(mob.location().show(mob,target,null,CMMsg.MSG_CAST,"<S-NAME> lift(s) the reincarnation geas on <T-NAMESELF>."))
                target.delEffect(target.fetchEffect(ID()));
            else
                mob.location().show(mob,target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> fail(s) to lift the reincarnation geas on <T-NAMESELF>.");
            return false;
        }
		if(target.isMonster())
		{
			mob.tell("Your chant would have no effect on such a creature.");
			return false;
		}

        boolean success=proficiencyCheck(mob,0,auto);
        if(success&&(!auto)&&(mob!=target)&&(!mob.mayIFight(target))&&(!mob.getGroupMembers(new HashSet()).contains(target)))
        {
            mob.tell(target.name()+" is a player, so you must be group members, or your playerkill flags must be on for this to work.");
            success=false;
        }
        
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(success)
		{
			int modifier=0;
			if(target!=mob) modifier=CMMsg.MASK_MALICIOUS;
			CMMsg msg=CMClass.getMsg(mob,target,this,modifier|verbalCastCode(mob,target,auto),(auto?"^S<S-NAME> get(s) put under a reincarnation geas!^?":"^S<S-NAME> chant(s) a reincarnation geas upon <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,1800);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) for a reincarnation geas, but nothing happens.");

		return success;
	}
}
