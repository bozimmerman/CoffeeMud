package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;
public class MagicShelter extends StdRoom
{
	public String ID(){return "MagicShelter";}
	public MagicShelter()
	{
		super();
		name="the shelter";
		displayText="Magic Shelter";
		setDescription("You are in a domain of complete void and peace.");
		baseEnvStats.setWeight(0);
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_MAGIC;
		domainCondition=Room.CONDITION_NORMAL;
		Ability A=CMClass.getAbility("Prop_PeaceMaker");
		if(A!=null)
		{
			A.setBorrowed(this,true);
			addEffect(A);
		}
		A=CMClass.getAbility("Prop_NoRecall");
		if(A!=null)
		{
			A.setBorrowed(this,true);
			addEffect(A);
		}
		A=CMClass.getAbility("Prop_NoSummon");
		if(A!=null)
		{
			A.setBorrowed(this,true);
			addEffect(A);
		}
		A=CMClass.getAbility("Prop_NoTeleport");
		if(A!=null)
		{
			A.setBorrowed(this,true);
			addEffect(A);
		}
		A=CMClass.getAbility("Prop_NoTeleportOut");
		if(A!=null)
		{
			A.setBorrowed(this,true);
			addEffect(A);
		}
	}



	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(Sense.isSleeping(this))
			return true;
		if((msg.sourceMinor()==CMMsg.TYP_RECALL)
		||(msg.sourceMinor()==CMMsg.TYP_LEAVE))
		{
			msg.source().tell("You can't leave the shelter that way.  You'll have to revoke it.");
			return false;
		}
		return true;
	}
}