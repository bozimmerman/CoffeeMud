package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_SilentGold extends ThiefSkill
{
	public String ID() { return "Thief_SilentGold"; }
	public String displayText() {return "(Silent AutoGold)";}
	public String name(){ return "Silent AutoGold";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	private static final String[] triggerStrings = {"SILENTGOLD"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_SilentGold();}

	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);
		if((affected!=null)&&(affected instanceof MOB))
		{
			if((affect.sourceMinor()==Affect.TYP_DEATH)
			&&(affect.source()!=affected)
			&&(Sense.canBeSeenBy(affect.source(),(MOB)affected))
			&&(affect.source().location()==((MOB)affected).location())
			&&((affect.source().getMoney()/10)>0))
			{
				Item C=CMClass.getItem("StdCoins");
				int amount=affect.source().getMoney()/10;
				C.baseEnvStats().setAbility(amount);
				C.recoverEnvStats();
				affect.source().setMoney(affect.source().getMoney()-amount);
				affect.source().recoverEnvStats();
				MOB mob=(MOB)affected;
				mob.location().addItemRefuse(C,Item.REFUSE_MONSTER_EQ);
				mob.location().recoverRoomStats();
				MOB victim=mob.getVictim();
				mob.setVictim(null);
				FullMsg msg=new FullMsg(mob,C,this,Affect.MSG_THIEF_ACT,"You silently autoloot "+(amount)+" gold from the corpse of "+affect.source().name(),Affect.MSG_THIEF_ACT,null,Affect.NO_EFFECT,null);
				if(mob.location().okAffect(mob,msg))
				{
					mob.location().send(mob,msg);
					ExternalPlay.get(mob,null,C,true);
				}
				if(victim!=null) mob.setVictim(victim);
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