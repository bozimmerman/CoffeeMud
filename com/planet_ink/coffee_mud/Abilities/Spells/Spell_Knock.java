package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Knock extends Spell
{
	public String ID() { return "Spell_Knock"; }
	public String name(){return "Knock";}
	public String displayText(){return "(Knock Spell)";}
	protected int canTargetCode(){return CAN_ITEMS|CAN_EXITS;}
	public Environmental newInstance(){	return new Spell_Knock();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_EVOCATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		String whatToOpen=Util.combine(commands,0);
		Environmental openThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatToOpen);
		if(dirCode>=0)
			openThis=mob.location().getExitInDir(dirCode);
		if(openThis==null)
			openThis=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(openThis==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		int levelDiff=openThis.envStats().level()-mob.envStats().level();

		boolean success=profficiencyCheck(-(levelDiff*5),auto);

		if(!success)
			beneficialWordsFizzle(mob,openThis,"<S-NAME> point(s) at "+openThis.name()+" and shouts incoherantly, but nothing happens.");
		else
		{
			mob.location().show(mob,null,affectType(auto),auto?openThis.name()+" begin(s) to glow!":"^S<S-NAME> point(s) at "+openThis.name()+".^?");
			for(int a=0;a<openThis.numAffects();a++)
			{
				Ability A=openThis.fetchAffect(a);
				if((A!=null)&&(A.ID().equalsIgnoreCase("Spell_WizardLock"))&&(A.invoker()!=null)&&(A.invoker().envStats().level()<mob.envStats().level()+3))
				{
					A.unInvoke();
					mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"A spell around "+openThis.name()+" seems to fade.");
					break;
				}
			}

			FullMsg msg=new FullMsg(mob,openThis,null,affectType(auto),"^S<S-NAME> point(s) at <T-NAMESELF>.^?");
			if(mob.location().okAffect(msg))
			{
				msg=new FullMsg(mob,openThis,null,Affect.MSG_UNLOCK,null);
				ExternalPlay.roomAffectFully(msg,mob.location(),dirCode);
				msg=new FullMsg(mob,openThis,null,Affect.MSG_OPEN,"<T-NAME> opens.");
				ExternalPlay.roomAffectFully(msg,mob.location(),dirCode);
			}
		}

		return success;
	}
}