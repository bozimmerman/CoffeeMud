package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Spell_WeaknessCold extends Spell
{
	public String ID() { return "Spell_WeaknessCold"; }
	public String name(){return "Weakness to Cold";}
	public String displayText(){return "(Weakness to Cold)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your cold weakness is now gone.");

		super.unInvoke();

	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.SAVE_COLD,affectedStats.getStat(CharStats.SAVE_COLD)-100);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(tickID!=MudHost.TICK_MOB) return false;
		if((affecting()!=null)&&(affecting() instanceof MOB))
		{
			MOB dummy=(MOB)affecting();
			Room room=dummy.location();
			if(room!=null)
			{
				if((room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_WINDY)
				&&((room.getArea().climateType()&Area.CLIMASK_COLD)>0)
				&&(Dice.rollPercentage()>dummy.charStats().getSave(CharStats.SAVE_COLD)))
					MUDFight.postDamage(invoker,dummy,null,1,CMMsg.MASK_GENERAL|CMMsg.TYP_COLD,Weapon.TYPE_FROSTING,"The cold biting wind <DAMAGE> <T-NAME>!");
				else
				if((room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_WINTER_COLD)
				&&(Dice.rollPercentage()>dummy.charStats().getSave(CharStats.SAVE_COLD)))
					MUDFight.postDamage(invoker,dummy,null,1,CMMsg.MASK_GENERAL|CMMsg.TYP_COLD,Weapon.TYPE_FROSTING,"The biting cold <DAMAGE> <T-NAME>!");
				else
				if((room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_SNOW)
				&&(Dice.rollPercentage()>dummy.charStats().getSave(CharStats.SAVE_COLD)))
				{
					int damage=Dice.roll(1,8,0);
					MUDFight.postDamage(invoker,dummy,null,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_COLD,Weapon.TYPE_FROSTING,"The blistering snow <DAMAGE> <T-NAME>!");
				}
				else
				if((room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_BLIZZARD)
				&&(Dice.rollPercentage()>dummy.charStats().getSave(CharStats.SAVE_COLD)))
				{
					int damage=Dice.roll(1,16,0);
					MUDFight.postDamage(invoker,dummy,null,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_COLD,Weapon.TYPE_FROSTING,"The blizzard <DAMAGE> <T-NAME>!");
				}
				else
				if((room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_HAIL)
				&&(Dice.rollPercentage()>dummy.charStats().getSave(CharStats.SAVE_COLD)))
				{
					int damage=Dice.roll(1,8,0);
					MUDFight.postDamage(invoker,dummy,null,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_COLD,Weapon.TYPE_FROSTING,"The biting hail <DAMAGE> <T-NAME>!");
				}
			}
		}
		return true;
	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		   &&(msg.sourceMinor()==CMMsg.TYP_COLD))
		{
			int recovery=(int)Math.round(Util.mul((msg.value()),1.5));
			msg.setValue(msg.value()+recovery);
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"A shimmering frost absorbing field appears around <T-NAMESELF>.":"^S<S-NAME> invoke(s) a shimmering frost absorbing field around <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				success=maliciousAffect(mob,target,asLevel,0,-1);
			}
		}
		else
			maliciousFizzle(mob,target,"<S-NAME> attempt(s) to invoke weakness to cold, but fail(s).");

		return success;
	}
}
