package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.Items.MiscMagic.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Skill_ScrollCopy extends StdAbility
{

	public Skill_ScrollCopy()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Scroll Copy";
		displayText="(in the mystical realm of magic)";
		miscText="";

		triggerStrings.addElement("COPY");
		triggerStrings.addElement("SCROLLCOPY");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(1);

		addQualifyingClass(new Mage().ID(),1);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_ScrollCopy();
	}

	public boolean invoke(MOB mob, Vector commands)
	{

		if(commands.size()<2)
		{
			mob.tell("Copy what from what?");
			return false;
		}
		Item target=mob.fetchInventory(CommandProcessor.combine(commands,1));
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see that here.");
			return false;
		}

		if(!(target instanceof ScrollSpell))
		{
			mob.tell("You can't copy from that.");
			return false;
		}

		if(((ScrollSpell)target).usesRemaining()<1)
		{
			mob.tell("The scroll appears to be faded.");
			return false;
		}

		Vector theSpells=((ScrollSpell)target).getSpells();
		Ability thisSpell=null;
		for(int a=0;a<theSpells.size();a++)
		{
			Ability A=(Ability)theSpells.elementAt(a);
			if(Util.containsString(A.name().toUpperCase(),((String)commands.elementAt(0)).toUpperCase()))
			{
				thisSpell=A;
				break;
			}
		}

		if(thisSpell==null)
		{
			mob.tell("That is not written on "+target.name()+".");
			return false;
		}

		thisSpell=(Ability)thisSpell.copyOf();
		Teacher T=new Teacher();
		while(T.numAbilities()>0)
			T.delAbility(T.fetchAbility(0));
		thisSpell.setProfficiency(50);
		T.addAbility(thisSpell);
		if(!thisSpell.canBeLearnedBy(T,mob))
			return false;

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			mob.location().show(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> copy(s) '"+thisSpell.name()+"' from "+target.name()+".");
			thisSpell.teach(T,mob);
		}
		else
			mob.location().show(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> attempt(s) to copy '"+thisSpell.name()+"' from "+target.name()+", but fail(s).");
		return success;
	}

}