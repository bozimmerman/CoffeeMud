package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_SpellTurning extends Spell
{
	public String ID() { return "Spell_SpellTurning"; }
	public String name(){return "Spell Turning";}
	public String displayText(){return "(Spell Turning)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){return new Spell_SpellTurning();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ABJURATION;}
	private boolean oncePerRound=false;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> reflective protection dissipates.");

		super.unInvoke();

	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&(!oncePerRound)
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(msg.targetMinor()==CMMsg.TYP_CAST_SPELL)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==Ability.SPELL)
		&&(!mob.amDead())
		&&(mob!=msg.source())
		&&((mob.fetchAbility(ID())==null)||profficiencyCheck(null,mob.envStats().level()-(msg.source().envStats().level()*3),false)))
		{
			oncePerRound=true;
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"The field around <S-NAME> reflects the spell!");
			Ability A=(Ability)msg.tool();
			A.invoke(mob,msg.source(),true);
			return false;
		}
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		oncePerRound=false;
		return super.tick(ticking,tickID);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"A reflective barrier appears around <T-NAMESELF>.":"^S<S-NAME> invoke(s) an reflective barrier of protection around <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a reflective spell, but fail(s).");

		return success;
	}
}