package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Sneak extends ThiefSkill
{

	public Thief_Sneak()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Sneak";
		displayText="(in a dark realm of thievery)";
		miscText="";

		triggerStrings.addElement("SNEAK");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(4);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Sneak();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		int dirCode=Directions.getGoodDirectionCode(Util.combine(commands,0));
		if(dirCode<0)
		{
			mob.tell("Sneak where?");
			return false;
		}

		if((mob.location().doors()[dirCode]==null)||(mob.location().exits()[dirCode]==null))
		{
			mob.tell("Sneak where?");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=false;
		FullMsg msg=new FullMsg(mob,null,null,auto?Affect.MSG_OK_VISUAL:Affect.MSG_DELICATE_HANDS_ACT,"You quietly sneak "+Directions.getDirectionName(dirCode)+".",Affect.NO_EFFECT,null,Affect.NO_EFFECT,null);
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			success=profficiencyCheck(0,auto);

			if(success)
				mob.envStats().setDisposition(mob.envStats().disposition()|EnvStats.IS_SNEAKING);
			ExternalPlay.move(mob,dirCode,false);
			if(success)
			{
				Ability toHide=mob.fetchAbility(new Thief_Hide().ID());
				if(toHide!=null)
					toHide.invoke(mob,new Vector(),null,true);
			}
			if(Sense.isSneaking(mob))
				mob.envStats().setDisposition(mob.envStats().disposition()-EnvStats.IS_SNEAKING);
		}
		return success;
	}

}
