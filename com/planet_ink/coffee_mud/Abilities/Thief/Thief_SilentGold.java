package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_SilentGold extends ThiefSkill
{
	public String ID() { return "Thief_SilentGold"; }
	public String displayText() {return "(Silent Autogold)";}
	public String name(){ return "Silent AutoGold";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	private static final String[] triggerStrings = {"SILENTGOLD"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_SilentGold();}
	
	public void affect(Affect affect)
	{
		super.affect(affect);
		if((affected!=null)&&(affected instanceof MOB))
		{
			if((affect.sourceMinor()==Affect.TYP_DEATH)
			&&(affect.source()!=affected)
			&&((affect.source().getMoney()/10)>0))
				affect.addTrailerMsg(new FullMsg((MOB)affected,affect.source(),this,Affect.MSG_THIEF_ACT|Affect.MASK_MALICIOUS,"You silently autoloot "+(affect.source().getMoney()/10)+" gold from the corpse of "+affect.source().name(),Affect.NO_EFFECT,null,Affect.NO_EFFECT,null));
			else
			if((affect.sourceMinor()==Affect.TYP_DELICATE_HANDS_ACT)
			&&(affect.tool()==this)
			&&(affect.target()!=null)
			&&(affect.target() instanceof MOB)
			&&(affect.amISource((MOB)affected)))
			{
				MOB mob=(MOB)affect.source();
				MOB target=(MOB)affect.target();
				int gold=((MOB)affect.target()).getMoney()/10;
				mob.setMoney(mob.getMoney()+gold);
				target.setMoney(target.getMoney()-gold);
				mob.recoverEnvStats();
				target.recoverEnvStats();
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.fetchAffect(ID())!=null))
		{
			mob.tell("You are no longer automatically looting gold from corpses silently.");
			mob.delAffect(mob.fetchAffect(ID()));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			mob.tell("You will now automatically loot gold from corpses silently.");
			beneficialAffect(mob,mob,0);
			Ability A=mob.fetchAffect(ID());
			if(A!=null) A.makeLongLasting();
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to start silently looting gold from corpses, but fail(s).");
		return success;
	}

}