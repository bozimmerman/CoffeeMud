package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_WeaknessCold extends Spell
{

	public Spell_WeaknessCold()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Weakness to Cold";
		displayText="(Weakness to Cold)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(2);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_WeaknessCold();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		mob.tell("Your cold weakness is now gone.");

		super.unInvoke();

	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.SAVE_COLD,affectedStats.getStat(CharStats.SAVE_COLD)-100);
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID)) return false;
		if(tickID!=Host.MOB_TICK) return false;
		if((affecting()!=null)&&(affecting() instanceof MOB))
		{
			MOB dummy=(MOB)affecting();
			Room room=dummy.location();
			if(room!=null)
			{
				if((room.getArea().weatherType(room)==Area.WEATHER_WINDY)
				&&((room.getArea().climateType()&Area.CLIMASK_COLD)>0)
				&&(Dice.rollPercentage()>dummy.charStats().getSave(CharStats.SAVE_COLD)))
				{
					int damage=1;
					room.show(invoker,dummy,Affect.MSG_OK_ACTION,"The cold biting wind "+ExternalPlay.standardHitWord(Weapon.TYPE_BURNING,damage)+" <S-NAME>!");
					ExternalPlay.postDamage(invoker,dummy,null,damage);
				}
				else
				if((room.getArea().weatherType(room)==Area.WEATHER_SNOW)
				&&(Dice.rollPercentage()>dummy.charStats().getSave(CharStats.SAVE_COLD)))
				{
					int damage=Dice.roll(1,8,0);
					room.show(invoker,dummy,Affect.MSG_OK_ACTION,"The blistering snow "+ExternalPlay.standardHitWord(Weapon.TYPE_BURNING,damage)+" <S-NAME>!");
					ExternalPlay.postDamage(invoker,dummy,null,damage);
				}
				else
				if((room.getArea().weatherType(room)==Area.WEATHER_HAIL)
				&&(Dice.rollPercentage()>dummy.charStats().getSave(CharStats.SAVE_COLD)))
				{
					int damage=Dice.roll(1,8,0);
					room.show(invoker,dummy,Affect.MSG_OK_ACTION,"The biting hail "+ExternalPlay.standardHitWord(Weapon.TYPE_BURNING,damage)+" <S-NAME>!");
					ExternalPlay.postDamage(invoker,dummy,null,damage);
				}
			}
		}
		return true;
	}
	

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		FullMsg msg=new FullMsg(mob,target,this,affectType,"<S-NAME> invoke(s) a shimmering frost absorbing field around <T-NAMESELF>.");
		if((success)&&(mob.location().okAffect(msg)))
		{
			mob.location().send(mob,msg);
			success=maliciousAffect(mob,target,0,-1);
		}
		else
			maliciousFizzle(mob,target,"<S-NAME> attempt(s) to invoke weakness to cold, but fail(s).");

		return success;
	}
}
