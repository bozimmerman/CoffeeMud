package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.Items.MiscMagic.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Skill_Write extends StdAbility
{

	public Skill_Write()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Write";
		displayText="(in the mystical realm of magic)";
		miscText="";

		triggerStrings.addElement("WRITE");
		triggerStrings.addElement("WR");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(1);

		addQualifyingClass(new Mage().ID(),1);
		addQualifyingClass(new Cleric().ID(),1);
		addQualifyingClass(new Fighter().ID(),1);
		addQualifyingClass(new Paladin().ID(),1);
		addQualifyingClass(new Thief().ID(),1);
		addQualifyingClass(new Bard().ID(),1);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Write();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public boolean invoke(MOB mob, Vector commands)
	{

		if(mob.charStats().getIntelligence()<8)
		{
			mob.tell("You are too stupid to actually write anything.");
			return false;
		}
		if(commands.size()<2)
		{
			mob.tell("You must specify what you want to write on and then what you want to write.");
			return false;
		}
		Item target=mob.fetchInventory((String)commands.elementAt(0));
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see that here.");
			return false;
		}

		Item item=null;
		if(target instanceof Item)
			item=(Item)target;
		if((item==null)||((item!=null)&&(!item.isReadable())))
		{
			mob.tell("You can't write on that.");
			return false;
		}
		
		if(item instanceof ScrollSpell)
		{
			mob.tell("You can't write on a scroll.");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,null,Affect.HANDS_DELICATE,Affect.VISUAL_ONLY,Affect.VISUAL_ONLY,"<S-NAME> write(s) on <T-NAME>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				item.setReadableText(CommandProcessor.combine(commands,1));
			}
		}
		else
			mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<S-NAME> attempt(s) to write on <T-NAME>, but mess(es) up.");
		return success;
	}

}