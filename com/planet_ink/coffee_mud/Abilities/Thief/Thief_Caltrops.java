package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.Abilities.Traps.Trap_Trap;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Caltrops extends ThiefSkill implements Trap
{
	public String ID() { return "Thief_Caltrops"; }
	public String name(){ return "Caltrops";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return Ability.CAN_ROOMS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"CALTROPS"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Caltrops();}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}

	public boolean isABomb(){return false;}
	public void activateBomb(){}
	public boolean disabled(){return false;}
	public void disable(){ unInvoke();}
	public void setReset(int Reset){}
	public int getReset(){return 0;}
	public boolean maySetTrap(MOB mob, int asLevel){return false;}
	public boolean canSetTrapOn(MOB mob, Environmental E){return false;}
	public String requiresToSet(){return "";}
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{maliciousAffect(mob,E,0,-1); return (Trap)E.fetchEffect(ID());}

	public boolean sprung(){return false;}
	public void spring(MOB mob)
	{
		if((!invoker().mayIFight(mob))||(Dice.rollPercentage()<mob.charStats().getSave(CharStats.SAVE_TRAPS)))
			mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> avoid(s) some caltrops on the floor.");
		else
			MUDFight.postDamage(invoker(),mob,null,Dice.roll(1,5,0),CMMsg.MASK_MALICIOUS|CMMsg.MSG_OK_ACTION,Weapon.TYPE_PIERCING,"The caltrops on the ground <DAMAGE> <T-NAME>.");
		// does not set sprung flag -- as this trap never goes out of use
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(affected==null) return super.okMessage(myHost,msg);
		if(!(affected instanceof Room)) return super.okMessage(myHost,msg);
		if(invoker()==null) return super.okMessage(myHost,msg);
		Room room=(Room)affected;
		if((msg.amITarget(room)||room.isInhabitant(msg.source()))
		&&(!msg.amISource(invoker()))
		&&((msg.sourceMinor()==CMMsg.TYP_ENTER)
			||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
			||(msg.sourceMinor()==CMMsg.TYP_FLEE)
			||(msg.sourceMinor()==CMMsg.TYP_ADVANCE)
			||(msg.sourceMinor()==CMMsg.TYP_RETREAT)))
				spring(msg.source());
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.location()==null) return false;
		if(mob.location().fetchEffect(ID())!=null)
		{
			mob.tell("Caltrops have already been tossed down here.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		Environmental target=mob.location();
		if(success)
		{
			if(mob.location().show(mob,target,(auto?CMMsg.MASK_GENERAL:0)|CMMsg.MSG_THIEF_ACT,"<S-NAME> throw(s) down caltrops!"))
				maliciousAffect(mob,target,0,-1);
			else
				success=false;
		}
		else
			maliciousFizzle(mob,target,"<S-NAME> fail(s) to throw down <S-HIS-HER> caltrops properly.");
		return success;
	}
}
