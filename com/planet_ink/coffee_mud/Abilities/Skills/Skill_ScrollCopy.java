package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
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

		addQualifyingClass("Mage",1);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_ScrollCopy();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if(commands.size()<2)
		{
			mob.tell("Copy what from what?");
			return false;
		}
		Item target=mob.fetchCarried(null,Util.combine(commands,1));
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+Util.combine(commands,1)+"' here.");
			return false;
		}

		if(!(target instanceof Scroll))
		{
			mob.tell("You can't copy from that.");
			return false;
		}

		if(((Scroll)target).usesRemaining()<1)
		{
			mob.tell("The scroll appears to be faded.");
			return false;
		}

		Vector theSpells=((Scroll)target).getSpells();
		Ability thisSpell=null;
		for(int a=0;a<theSpells.size();a++)
		{
			Ability A=(Ability)theSpells.elementAt(a);
			if(CoffeeUtensils.containsString(A.name().toUpperCase(),((String)commands.elementAt(0)).toUpperCase()))
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
		MOB T=(MOB)CMClass.getMOB("Teacher").newInstance();
		T.setName(target.name());
		T.charStats().setGender('N');
		while(T.numAbilities()>0)
			T.delAbility(T.fetchAbility(0));
		thisSpell.setProfficiency(50);
		T.addAbility(thisSpell);
		if(!thisSpell.canBeLearnedBy(T,mob))
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			mob.location().show(mob,null,Affect.MSG_HANDS,"<S-NAME> copy(s) '"+thisSpell.name()+"' from "+target.name()+".");
			thisSpell.teach(T,mob);
		}
		else
			mob.location().show(mob,null,Affect.MSG_HANDS,"<S-NAME> attempt(s) to copy '"+thisSpell.name()+"' from "+target.name()+", but fail(s).");
		return success;
	}

}