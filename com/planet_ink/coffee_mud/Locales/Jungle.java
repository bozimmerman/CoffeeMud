package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Jungle extends StdRoom
{
	public String ID(){return "Jungle";}
	public Jungle()
	{
		super();
		name="the jungle";
		baseEnvStats.setWeight(3);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_JUNGLE;
		domainCondition=Room.CONDITION_HOT;
	}
	public Environmental newInstance()
	{
		return new Jungle();
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
				A=CMClass.getAbility("Disease_Gonorrhea");
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
		new Integer(EnvResource.RESOURCE_HEMP),
		new Integer(EnvResource.RESOURCE_SILK),
		new Integer(EnvResource.RESOURCE_FRUIT),
		new Integer(EnvResource.RESOURCE_APPLES),
		new Integer(EnvResource.RESOURCE_BERRIES),
		new Integer(EnvResource.RESOURCE_ORANGES),
		new Integer(EnvResource.RESOURCE_COFFEEBEANS),
		new Integer(EnvResource.RESOURCE_HERBS),
		new Integer(EnvResource.RESOURCE_VINE),
		new Integer(EnvResource.RESOURCE_LEMONS),
		new Integer(EnvResource.RESOURCE_FUR),
		new Integer(EnvResource.RESOURCE_FEATHERS)
	};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return Jungle.roomResources;}
}
