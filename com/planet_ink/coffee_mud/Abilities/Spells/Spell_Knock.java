package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Knock extends Spell
{

	public Spell_Knock()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Knock";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Knock Spell)";


		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(3);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Knock();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.SPELL_ALTERATION;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		String whatToOpen=Util.combine(commands,0);
		Environmental openThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatToOpen);
		if(dirCode>=0)
			openThis=mob.location().exits()[dirCode];
		if(openThis==null)
			openThis=getTarget(mob,mob.location(),givenTarget,commands);
		if(openThis==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		int levelDiff=openThis.envStats().level()-mob.envStats().level();

		boolean success=profficiencyCheck(-(levelDiff*5),auto);

		if(!success)
			beneficialWordsFizzle(mob,openThis,"<S-NAME> point(s) at "+openThis.name()+" and shouts incoherantly, but nothing happens.");
		else
		{
			mob.location().show(mob,null,affectType,auto?openThis.name()+" begin(s) to glow!":"<S-NAME> point(s) at "+openThis.name());
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

			FullMsg msg=new FullMsg(mob,openThis,null,affectType,"<S-NAME> point(s) at <T-NAMESELF>");
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