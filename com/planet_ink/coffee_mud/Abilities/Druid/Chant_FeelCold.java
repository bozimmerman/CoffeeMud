package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_FeelCold extends Chant
{
	public String ID() { return "Chant_FeelCold"; }
	public String name(){ return "Feel Cold";}
	public String displayText(){return "(Feel Cold)";}
	public int quality(){return Ability.MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Chant_FeelCold();}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your cold feeling is gone.");

		super.unInvoke();

	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		   &&(affect.sourceMinor()==Affect.TYP_COLD))
		{
			int recovery=(int)Math.round(Util.mul((affect.targetCode()-Affect.MASK_HURT),2.0));
			affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode()+recovery,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
		}
		return true;
	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.SAVE_COLD,affectedStats.getStat(CharStats.SAVE_COLD)-100);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
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
					ExternalPlay.postDamage(invoker,dummy,null,1,Affect.MASK_GENERAL|Affect.TYP_COLD,Weapon.TYPE_FROSTING,"The cold biting wind <DAMAGE> <T-NAME>!");
				else
				if((room.getArea().weatherType(room)==Area.WEATHER_WINTER_COLD)
				&&(Dice.rollPercentage()>dummy.charStats().getSave(CharStats.SAVE_COLD)))
					ExternalPlay.postDamage(invoker,dummy,null,1,Affect.MASK_GENERAL|Affect.TYP_COLD,Weapon.TYPE_FROSTING,"The biting cold <DAMAGE> <T-NAME>!");
				else
				if((room.getArea().weatherType(room)==Area.WEATHER_SNOW)
				&&(Dice.rollPercentage()>dummy.charStats().getSave(CharStats.SAVE_COLD)))
				{
					int damage=Dice.roll(1,8,0);
					ExternalPlay.postDamage(invoker,dummy,null,damage,Affect.MASK_GENERAL|Affect.TYP_COLD,Weapon.TYPE_FROSTING,"The blistering snow <DAMAGE> <T-NAME>!");
				}
				else
				if((room.getArea().weatherType(room)==Area.WEATHER_BLIZZARD)
				&&(Dice.rollPercentage()>dummy.charStats().getSave(CharStats.SAVE_COLD)))
				{
					int damage=Dice.roll(1,16,0);
					ExternalPlay.postDamage(invoker,dummy,null,damage,Affect.MASK_GENERAL|Affect.TYP_COLD,Weapon.TYPE_FROSTING,"The blizzard <DAMAGE> <T-NAME>!");
				}
				else
				if((room.getArea().weatherType(room)==Area.WEATHER_HAIL)
				&&(Dice.rollPercentage()>dummy.charStats().getSave(CharStats.SAVE_COLD)))
				{
					int damage=Dice.roll(1,8,0);
					ExternalPlay.postDamage(invoker,dummy,null,damage,Affect.MASK_GENERAL|Affect.TYP_COLD,Weapon.TYPE_FROSTING,"The biting hail <DAMAGE> <T-NAME>!");
				}
			}
		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> feel(s) very cold");
					maliciousAffect(mob,target,0,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades.");
		// return whether it worked
		return success;
	}
}
