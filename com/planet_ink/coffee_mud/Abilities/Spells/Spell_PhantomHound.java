package com.planet_ink.coffee_mud.Abilities.Spells;
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
public class Spell_PhantomHound extends Spell
{
	public String ID() { return "Spell_PhantomHound"; }
	public String name(){return "Phantom Hound";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){ return MALICIOUS;}
	private MOB victim=null;
	private int pointsLeft=0;
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ILLUSION;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==MudHost.TICK_MOB)
		{
			if(((affected==null)
			||(unInvoked)
			||(!(affected instanceof MOB)))
				&&(canBeUninvoked()))
				unInvoke();
			else
			{
				MOB beast=(MOB)affected;
				int a=0;
				while(a<beast.numEffects())
				{
					Ability A=beast.fetchEffect(a);
					if(A!=null)
					{
						int n=beast.numEffects();
						if(A.ID().equals(ID()))
							a++;
						else
						{
							A.unInvoke();
							if(beast.numEffects()==n)
								a++;
						}
					}
					else
						a++;
				}
				if((!beast.isInCombat())||(beast.getVictim()!=victim))
				{
					if(beast.amDead()) beast.setLocation(null);
					beast.destroy();
				}
				else
				{
					pointsLeft-=(victim.charStats().getStat(CharStats.INTELLIGENCE));
					pointsLeft-=victim.envStats().level();
					int pointsLost=beast.baseState().getHitPoints()-beast.curState().getHitPoints();
					if(pointsLost>0)
						pointsLeft-=pointsLost/4;
					if(pointsLeft<0)
					{
						if(beast.amDead()) beast.setLocation(null);
						beast.destroy();
					}
				}
			}

		}
		return super.tick(ticking,tickID);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing()))
		&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
			unInvoke();
	}

	public void unInvoke()
	{
		MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
		}
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
			msg.setValue(0);
		return super.okMessage(myHost,msg);

	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!mob.isInCombat())
		{
			mob.tell("You must be in combat to cast this spell!");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> invoke(s) a ferocious phantom assistant.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB beast=CMClass.getMOB("GenMOB");
				beast.setName("the phantom hound");
				beast.setDisplayText("the phantom hound is here");
				beast.setStartRoom(null);
				beast.setDescription("This is the most ferocious beast you have ever seen.");
				beast.baseEnvStats().setAttackAdjustment(mob.envStats().attackAdjustment()+100);
				beast.baseEnvStats().setArmor(mob.baseEnvStats().armor()-20);
				beast.baseEnvStats().setDamage(75);
				beast.baseEnvStats().setLevel(mob.envStats().level());
				beast.baseEnvStats().setSensesMask(EnvStats.CAN_SEE_DARK|EnvStats.CAN_SEE_HIDDEN|EnvStats.CAN_SEE_INVISIBLE|EnvStats.CAN_SEE_SNEAKERS);
				beast.baseCharStats().setMyRace(CMClass.getRace("Dog"));
				beast.baseCharStats().getMyRace().startRacing(beast,false);
				beast.baseCharStats().setStat(CharStats.SAVE_MAGIC,200);
				beast.baseCharStats().setStat(CharStats.SAVE_MIND,200);
				beast.baseCharStats().setStat(CharStats.SAVE_JUSTICE,200);
				beast.baseCharStats().setStat(CharStats.SAVE_PARALYSIS,200);
				beast.baseCharStats().setStat(CharStats.SAVE_POISON,200);
				beast.baseCharStats().setStat(CharStats.SAVE_UNDEAD,200);
				beast.baseCharStats().setStat(CharStats.SAVE_DISEASE,200);
				beast.baseCharStats().setStat(CharStats.SAVE_FIRE,200);
				beast.baseCharStats().setStat(CharStats.SAVE_ACID,200);
				beast.baseCharStats().setStat(CharStats.SAVE_COLD,200);
				beast.baseCharStats().setStat(CharStats.SAVE_ELECTRIC,200);
				beast.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
				beast.baseEnvStats().setAbility(100);
				beast.baseState().setMana(100);
				beast.baseState().setMovement(1000);
				beast.recoverEnvStats();
				beast.recoverCharStats();
				beast.recoverMaxState();
				beast.resetToMaxState();
				beast.text();
				beast.bringToLife(mob.location(),true);
				beast.setMoney(0);
				beast.location().showOthers(beast,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
				beast.setStartRoom(null);
				victim=mob.getVictim();
				if(victim!=null)
				{
					victim.setVictim(beast);
					beast.setVictim(victim);
				}
				pointsLeft=130;
				beneficialAffect(mob,beast,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to invoke a spell, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
