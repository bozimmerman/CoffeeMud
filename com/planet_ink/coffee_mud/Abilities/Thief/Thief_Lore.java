package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Lore extends ThiefSkill
{
	public String ID() { return "Thief_Lore"; }
	public String name(){ return "Lore";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"LORE"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Lore();}
	protected boolean disregardsArmorCheck(MOB mob){return true;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_DELICATE_HANDS_ACT,auto?"":"<S-NAME> stud(ys) <T-NAMESELF> and consider(s) for a moment.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				String identity=((Item)target).secretIdentity();
				mob.tell(identity);

			}

		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> stud(ys) <T-NAMESELF>, but can't remember a thing.");
		return success;
	}
}