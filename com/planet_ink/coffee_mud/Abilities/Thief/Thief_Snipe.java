package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Snipe extends ThiefSkill
{
	public String ID() { return "Thief_Snipe"; }
	public String name(){ return "Snipe";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.OK_OTHERS;}
	private static final String[] triggerStrings = {"SNIPE"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Snipe();}
	protected int overrideMana(){return 100;}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.isInCombat())
		{
			mob.tell("Not while in combat!");
			return false;
		}
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		
		if(!Sense.aliveAwakeMobile(mob,true))
		{
			mob.tell("You need to stand up!");
			return false;
		}
		if(Sense.canBeSeenBy(mob,target))
		{
			mob.tell(target.name()+" is watching you too closely.");
			return false;
		}
		Item w=mob.fetchWieldedItem();
		if((w==null)
		||(!(w instanceof Weapon)))
		{
			mob.tell("You need a weapon to snipe.");
			return false;
		}
		Weapon ww=(Weapon)w;
		if(((ww.weaponClassification()!=Weapon.CLASS_RANGED)&&(ww.weaponClassification()!=Weapon.CLASS_THROWN))
		||(w.maxRange()<=0))
		{
			mob.tell("You need a ranged weapon to snipe.");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		int code=Affect.MASK_MALICIOUS|Affect.MSG_THIEF_ACT;
		String str=auto?"":"<S-NAME> strike(s) <T-NAMESELF> from the shadows!";
		int otherCode=success?code:Affect.NO_EFFECT;
		String otherStr=success?str:null;
		FullMsg msg=new FullMsg(mob,target,this,code,str,otherCode,otherStr,otherCode,otherStr);
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			Ability hide=mob.fetchAffect("Thief_Hide");
			ExternalPlay.postAttack(mob,target,w);
			if(success)
			{
				MOB oldVictim=target.getVictim();
				MOB oldVictim2=mob.getVictim();
				if(oldVictim==mob) target.makePeace();
				if(oldVictim2==target) mob.makePeace();
				if((hide!=null)&&(mob.fetchAffect(hide.ID())==null))
				{
					hide.invoke(mob,null,false);
					if(Sense.canBeSeenBy(mob,target))
					{
						target.setVictim(oldVictim);
						mob.setVictim(oldVictim2);
					}
				}
			}
		}
		return success;
	}
}