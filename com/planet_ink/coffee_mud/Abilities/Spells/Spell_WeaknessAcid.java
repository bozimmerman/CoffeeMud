package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_WeaknessAcid extends Spell
{

	public Spell_WeaknessAcid()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Weakness to Acid";
		displayText="(Weakness to Acid)";
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
		return new Spell_WeaknessAcid();
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
		mob.tell("Your acidic weakness is now gone.");

		super.unInvoke();

	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.SAVE_ACID,affectedStats.getStat(CharStats.SAVE_ACID)-100);
	}


	public boolean okAffect(Affect msg)
	{
		if((affecting()!=null)
		&&(affecting() instanceof MOB)
		&&(Util.bset(msg.targetCode(),Affect.MASK_HURT))
		&&(msg.amITarget(affecting()))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Ability))
		{
		}
		return true;
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
				if(((room.getArea().weatherType(room)==Area.WEATHER_RAIN)
				||(room.getArea().weatherType(room)==Area.WEATHER_THUNDERSTORM))
				&&(Dice.rollPercentage()>dummy.charStats().getSave(CharStats.SAVE_ACID)))
					ExternalPlay.postDamage(invoker,dummy,null,1,Affect.ACT_GENERAL|Affect.TYP_ACID,Weapon.TYPE_BURNING,"The biting rain dissolves and <DAMAGE> <T-NAME>!");
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

		FullMsg msg=new FullMsg(mob,target,this,affectType,"<S-NAME> invoke(s) a shimmering absorbing field of weakness around <T-NAMESELF>.");
		if((success)&&(mob.location().okAffect(msg)))
		{
			mob.location().send(mob,msg);
			success=maliciousAffect(mob,target,0,-1);
		}
		else
			maliciousFizzle(mob,target,"<S-NAME> attempt(s) to invoke weakness to acid, but fail(s).");

		return success;
	}
}
