package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
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

		addQualifyingClass(new Thief().ID(),4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Sneak();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		int dirCode=Directions.getDirectionCode(CommandProcessor.combine(commands,0));
		if(dirCode<0)
		{
			mob.tell("Sneak where?");
			return false;
		}
		
		if((mob.location().getRoom(dirCode)==null)||(mob.location().getExit(dirCode)==null))
		{
			mob.tell("Sneak where?");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		boolean success=false;
		FullMsg msg=new FullMsg(mob,null,null,Affect.HANDS_DELICATE,"You quietly sneak "+Directions.getDirectionName(dirCode)+".",Affect.NO_EFFECT,null,Affect.NO_EFFECT,null);
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			success=profficiencyCheck(0);

			if(success)
				mob.envStats().setDisposition(mob.envStats().disposition()|Sense.IS_SNEAKING);
			Movement.move(mob,dirCode,false);
			if(success)
			{
				int oldMana=mob.curState().getMana();
				mob.curState().setMana(500);
				Ability toHide=mob.fetchAbility(new Thief_Hide().ID());
				if(toHide!=null)
					toHide.invoke(mob,new Vector());
				mob.curState().setMana(oldMana);
			}
			if(Sense.isSneaking(mob))
				mob.envStats().setDisposition(mob.envStats().disposition()-Sense.IS_SNEAKING);
		}
		return success;
	}

}
