package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_DeepPit extends Trap_RoomPit
{
	public String ID() { return "Trap_DeepPit"; }
	public String name(){ return "deep pit";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 14;}
	public String requiresToSet(){return "";}
	public Environmental newInstance(){	return new Trap_DeepPit();}

	public void finishSpringing(MOB target)
	{
		if((!invoker().mayIFight(target))||(target.envStats().weight()<5))
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> float(s) gently into the pit!");
		else
		{
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> hit(s) the pit floor with a THUMP!");
			int damage=Dice.roll(trapLevel(),15,1);
			ExternalPlay.postDamage(invoker(),target,this,damage,CMMsg.MSG_OK_VISUAL,-1,null);
		}
		ExternalPlay.look(target,null,true);
	}
}
