package com.planet_ink.coffee_mud.Abilities.Prayers;
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
public class Prayer_Avatar extends Prayer
{
	public String ID() { return "Prayer_Avatar"; }
	public String name(){ return "Avatar";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_SELF;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	public String displayText(){
		if((invoker()!=null)&&(invoker().getWorshipCharID().length()>0))
			return "(You are the AVATAR of "+invoker().getWorshipCharID()+")";
		return "(You are the AVATAR of the gods)";
	}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your unholy alliance has been severed.");
	}

	public void affectCharState(MOB affectedMOB, CharState affectedState)
	{
		super.affectCharState(affectedMOB,affectedState);
		affectedState.setHitPoints(affectedState.getHitPoints()+200);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectedStats)
	{
		super.affectEnvStats(affected,affectedStats);
		int xlvl=2+(int)Math.round(CMath.div(adjustedLevel(invoker(),0),1.5));
		affectedStats.setArmor(affectedStats.armor()-(xlvl));
		affectedStats.setSpeed(affectedStats.speed()+1.0+CMath.mul(0.25,super.getXLEVELLevel(invoker())));
		affectedStats.setAttackAdjustment(affectedStats.attackAdjustment()+xlvl);
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(mob.getMyDeity()!=null)
				affectedStats.setName(mob.name()+" the Avatar of "+mob.getMyDeity().name());
			else
				affectedStats.setName(mob.name()+" the Avatar");
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);

		if(!super.tick(ticking,tickID))
			return false;
		MOB mob=(MOB)affected;
		if(mob.location()!=null)
		{
			if(mob.isInCombat())
			{
				MOB newvictim=mob.location().fetchInhabitant(CMLib.dice().roll(1,mob.location().numInhabitants(),-1));
				if(newvictim!=mob) mob.setVictim(newvictim);
			}
			else
			{
				MOB attack=null;
				Room R=mob.location();
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB M=R.fetchInhabitant(m);
					if((M!=null)&&(M!=mob)&&(mob.mayPhysicallyAttack(M)))
					{ attack=M; break;}
				}
				if(attack==null)
				{
					int dir=-1;
					Vector<Integer> dirs=new Vector<Integer>();
					
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						Room R2=R.getRoomInDir(d);
						if((R2!=null)
						&&(R.getExitInDir(d)!=null)
						&&(R.getExitInDir(d).isOpen()))
							dirs.addElement(Integer.valueOf(d));
					}
					while(dirs.size()>0)
					{
						int d=((Integer)dirs.remove(CMLib.dice().roll(1, dirs.size(), -1))).intValue();
						Room R2=R.getRoomInDir(d);
						if(R2!=null)
						{
							if((dir<0)||(dir==Directions.UP))
								dir=d;
							for(int m=0;m<R2.numInhabitants();m++)
							{
								MOB M=R2.fetchInhabitant(m);
								if((M!=null)&&(M!=mob)&&(mob.mayPhysicallyAttack(M)))
								{ attack=M; break;}
							}
						}
					}
					if(dir>=0)
					{
						String godName=mob.getWorshipCharID().length()==0?"Your god":mob.getWorshipCharID();
						mob.tell(godName+" directs you "+Directions.getInDirectionName(dir)+".");
						CMLib.tracking().move(mob,dir,false,false);
					}
				}
				if(attack!=null)
					CMLib.combat().postAttack(mob,attack,mob.fetchWieldedItem());
			}
		}
		return super.tick(ticking,tickID);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already the AVATAR.");
			return false;
		}

		int levels=mob.charStats().getClassLevel("Avatar");
		if(levels<0) 
            levels=mob.envStats().level();
		else
		if(!mob.charStats().getCurrentClass().ID().equals("Avatar"))
		{
			mob.tell("You have lost this ability for all time.");
			return false;
		}
        else
            levels=adjustedLevel(mob,asLevel);


		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> "+prayWord(mob)+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> become(s) the AVATAR!");
				beneficialAffect(mob,target,asLevel,levels);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+", but nothing happens.");


		// return whether it worked
		return success;
	}
}
