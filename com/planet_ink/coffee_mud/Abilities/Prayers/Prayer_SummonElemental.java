package com.planet_ink.coffee_mud.Abilities.Prayers;

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
public class Prayer_SummonElemental extends Prayer
{
	public String ID() { return "Prayer_SummonElemental"; }
	public String name(){return "Elemental Aid";}
	public String displayText(){return "(Elemental Aid)";}
	public int quality(){return BENEFICIAL_SELF;};
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==MudHost.TICK_MOB)
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if(mob.amFollowing()!=invoker())
					unInvoke();
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


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for elemental assistance.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB myMonster = determineMonster(mob, mob.envStats().level());
				if(myMonster.isInCombat()) myMonster.makePeace();
				CommonMsgs.follow(myMonster,mob,true);
				invoker=mob;
				beneficialAffect(mob,myMonster,0);
				if(myMonster.amFollowing()!=mob)
					mob.tell(myMonster.name()+" seems unwilling to follow you.");
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for elemental assistance, but is not answered.");

		// return whether it worked
		return success;
	}

	protected final static String types[]={"EARTH","FIRE","AIR","WATER"};

	public MOB determineMonster(MOB caster, int level)
	{
		MOB newMOB=(MOB)CMClass.getMOB("GenRideable");
		Rideable ride=(Rideable)newMOB;
		newMOB.baseEnvStats().setAbility(13);
		newMOB.baseEnvStats().setLevel(level/2);
		newMOB.setAlignment(500);
		newMOB.baseEnvStats().setWeight(850);
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.baseEnvStats().setDamage(caster.envStats().damage()/2);
		newMOB.baseEnvStats().setAttackAdjustment(caster.envStats().attackAdjustment()/2);
		newMOB.baseEnvStats().setArmor(caster.envStats().armor()/2);
		newMOB.baseEnvStats().setSpeed(1);
		newMOB.baseCharStats().setStat(CharStats.STRENGTH,25);
		newMOB.baseCharStats().setStat(CharStats.DEXTERITY,25);
		newMOB.baseCharStats().setStat(CharStats.CONSTITUTION,25);
		newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'M');
		int type=-1;
		for(int i=0;i<types.length;i++)
			if(text().toUpperCase().indexOf(types[i])>=0)
				type=i;
		if(type<0) type=Dice.roll(1,types.length,-1);
		switch(type)
		{
		case 0:
			newMOB.baseCharStats().setMyRace(CMClass.getRace("EarthElemental"));
			newMOB.setName("a hideous rock beast");
			newMOB.setDisplayText("a hideous rock beast is stomping around here");
			newMOB.setDescription("This enormous hunk of rock is roughly the shape of a humanoid.");
			ride.setRiderCapacity(2);
			break;
		case 1:
			newMOB.baseCharStats().setMyRace(CMClass.getRace("FireElemental"));
			newMOB.setName("a creature of flame and smoke ");
			newMOB.setDisplayText("a creature of flame and smoke is here");
			newMOB.setDescription("This enormous burning ember is roughly the shape of a humanoid.");
			ride.setRiderCapacity(0);
			break;
		case 2:
			newMOB.baseCharStats().setMyRace(CMClass.getRace("AireElemental"));
			newMOB.setName("a swirling air elemental");
			newMOB.setDisplayText("a swirling air elemental spins around here");
			newMOB.setDescription("This enormous swirling code of air is roughly the shape of a humanoid.");
			ride.setRiderCapacity(0);
			break;
		case 3:
			newMOB.baseCharStats().setMyRace(CMClass.getRace("WaterElemental"));
			newMOB.setName("a hideous ice beast");
			newMOB.setDisplayText("a hideous ice beast is stomping around here");
			newMOB.setDescription("This enormous hunk of ice is roughly the shape of a humanoid.");
			ride.setRiderCapacity(2);
			break;
		}
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));

		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.text();
		newMOB.bringToLife(caster.location(),true);
		newMOB.setMoney(0);
		newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
		caster.location().recoverRoomStats();
		newMOB.setStartRoom(null);
		return(newMOB);


	}
}
