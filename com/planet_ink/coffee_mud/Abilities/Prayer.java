package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Prayer extends StdAbility
{
	protected boolean isNeutral=false;

	public Prayer()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a Prayer";
		displayText="(in the holy dominion of the gods)";
		miscText="";
		triggerStrings.addElement("PRAY");
		triggerStrings.addElement("PR");
		
		malicious=false;
		
		canBeUninvoked=true;
		isAutoinvoked=false;
		isNeutral=false;
	}
	
	public int classificationCode()
	{
		return Ability.PRAYER;
	}
	
	public Environmental newInstance()
	{
		return new Prayer();
	}
	
	public boolean appropriateToMyAlignment(MOB mob)
	{
		if(isNeutral) return true;
		if((malicious)&&(mob.getAlignment()>650))
			return false;
		else
		if((!malicious)&&(mob.getAlignment()<350))
			return false;
		return true;
	}
	
	public boolean isNeutral()
	{
		return isNeutral;
	}
	
	public boolean invoke(MOB mob, Vector commands)
	{
		if(!super.invoke(mob,commands))
			return false;
		
		if(!Sense.canSpeak(mob))
		{
			mob.tell("You can't speak!");
			return false;
		}
		
		if(!isNeutral)
		{
			if((!malicious)&&(mob.getAlignment()<(1000-(envStats().level()*2))))
				mob.setAlignment(mob.getAlignment()+(envStats().level()*2));
			else
			if((!malicious)&&(mob.getAlignment()>((envStats().level()*2))))
				mob.setAlignment(mob.getAlignment()-(envStats().level()*2));
		}
		
		return true;
	}
}
