package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Swamp extends StdRoom
{
	public String ID(){return "Swamp";}
	public Swamp()
	{
		super();
		name="the swamp";
		baseEnvStats.setWeight(3);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_SWAMP;
		domainCondition=Room.CONDITION_WET;
	}
	public Environmental newInstance()
	{
		return new Swamp();
	}
	public void affect(Environmental myHost, Affect msg)
	{
		if((msg.amITarget(this)||(msg.targetMinor()==Affect.TYP_ADVANCE)||(msg.targetMinor()==Affect.TYP_RETREAT))
		   &&(!msg.source().isMonster())
		   &&(msg.source().curState().getHitPoints()<msg.source().maxState().getHitPoints())
		   &&(Dice.rollPercentage()==1)
		   &&(Dice.rollPercentage()==1))
		{
			Ability A=null;
			if(Dice.rollPercentage()>50)
				A=CMClass.getAbility("Disease_Chlamydia");
			else
				A=CMClass.getAbility("Disease_Malaria");
			if((A!=null)&&(msg.source().fetchAffect(A.ID())==null))
				A.invoke(msg.source(),msg.source(),true);
		}
		super.affect(myHost,msg);
	}
	
	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_JADE),
		new Integer(EnvResource.RESOURCE_SCALES),
		new Integer(EnvResource.RESOURCE_COCOA),
		new Integer(EnvResource.RESOURCE_COAL),
		new Integer(EnvResource.RESOURCE_SUGAR),
		new Integer(EnvResource.RESOURCE_CLAY),
	};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return Swamp.roomResources;}
}
