package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_ContinualLight extends Spell
{
	public String ID() { return "Spell_ContinualLight"; }
	public String name(){return "Continual Light";}
	public String displayText(){return "(Continual Light)";}
	public int quality(){ return OK_SELF;}
	protected int canTargetCode(){return CAN_MOBS|CAN_ITEMS;}
	protected int canAffectCode(){return CAN_MOBS|CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_ContinualLight();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_EVOCATION;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(!(affected instanceof Room))
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_LIGHTSOURCE);
		if(Sense.isInDark(affected))
			affectableStats.setDisposition(affectableStats.disposition()-EnvStats.IS_DARK);
	}
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		Room room=((MOB)affected).location();
		if(canBeUninvoked())
			room.show(mob,null,Affect.MSG_OK_VISUAL,"The light above <S-NAME> dims.");
		super.unInvoke();
		if(canBeUninvoked())
			room.recoverRoomStats();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=null;
		if(commands.size()==0) target=mob;
		else
		target=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_UNWORNONLY);
		
		if(target==null) return false;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		String str="^S<S-NAME> invoke(s) a continual light toward(s) <T-NAMESELF>!^?";
		if(!(target instanceof MOB))
			str="^S<S-NAME> invoke(s) a continual light into <T-NAME>!^?";
		FullMsg msg=new FullMsg(mob,target,this,affectType(auto),str);
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,Integer.MAX_VALUE-100);
			mob.location().recoverRoomStats(); // attempt to handle followers
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke light, but fail(s).");

		return success;
	}
}
