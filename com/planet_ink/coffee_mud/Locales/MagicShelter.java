package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Exits.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;

import java.util.*;
public class MagicShelter extends StdRoom	
{
	
	
	public MagicShelter()
	{
		super();
		displayText="Magic Shelter";
		description="You are in a domain of complete void and peace.";
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_MAGIC;
		domainCondition=Room.CONDITION_NORMAL;
	}
	
	public Environmental newInstance()
	{
		return new MagicShelter();
	}
	
	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		if((affect.sourceCode()==affect.HANDS_RECALL)
		||(affect.sourceCode()==affect.MOVE_LEAVE))
		{
			affect.source().tell("You can't leave the shelter that way.  You'll have to revoke it.");
			return false;
		}
		return true;
	}
}