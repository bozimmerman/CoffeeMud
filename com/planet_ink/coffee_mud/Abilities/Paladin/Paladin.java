package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.Prayers.Prayer;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Paladin extends StdAbility
{
	protected Vector paladinsGroup=null;
	
	public Paladin()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Paladin's Amazement";
		displayText="";
		miscText="";

		quality=Ability.OK_OTHERS;
		canBeUninvoked=false;
		isAutoinvoked=true;

		baseEnvStats().setLevel(18);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Paladin();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
			return false;
		if(invoker==null) invoker=(MOB)affected;
		if(invoker.getAlignment()<650)
			return false;
		if(paladinsGroup!=null)
		{
			Hashtable h=new Hashtable();
			((MOB)affected).getGroupMembers(h);
			for(Enumeration e=h.elements();e.hasMoreElements();)
			{
				MOB mob=(MOB)e.nextElement();
				if(!paladinsGroup.contains(mob))
					paladinsGroup.addElement(mob);
			}
			for(int i=paladinsGroup.size()-1;i>=0;i--)
			{
				MOB mob=(MOB)paladinsGroup.elementAt(i);
				if((!h.contains(mob))
				||(mob.location()!=invoker.location()))
					paladinsGroup.removeElement(mob);
			}
		}
		if(Dice.rollPercentage()==1) 
			helpProfficiency(invoker);
		return true;
	}
}
