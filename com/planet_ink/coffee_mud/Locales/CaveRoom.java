package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class CaveRoom extends StdRoom
{
	public String ID(){return "CaveRoom";}
	public CaveRoom()
	{
		super();
		name="the cave";
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_DARK);
		baseEnvStats.setWeight(2);
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_CAVE;

		domainCondition=Room.CONDITION_NORMAL;
	}

	public int getMaxRange()
	{
		if(maxRange>=0) return maxRange;
		return 5;
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.amITarget(this)||(msg.targetMinor()==CMMsg.TYP_ADVANCE)||(msg.targetMinor()==CMMsg.TYP_RETREAT))
		   &&(!msg.source().isMonster())
		   &&(msg.source().curState().getHitPoints()<msg.source().maxState().getHitPoints())
		   &&(Dice.rollPercentage()==1)
		   &&(Dice.rollPercentage()==1))
		{
			Ability A=CMClass.getAbility("Disease_Syphilis");
			if((A!=null)&&(msg.source().fetchEffect(A.ID())==null))
				A.invoke(msg.source(),msg.source(),true);
		}
		super.executeMsg(myHost,msg);
	}
	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_GRANITE),
		new Integer(EnvResource.RESOURCE_OBSIDIAN),
		new Integer(EnvResource.RESOURCE_MARBLE),
		new Integer(EnvResource.RESOURCE_STONE),
		new Integer(EnvResource.RESOURCE_IRON),
		new Integer(EnvResource.RESOURCE_LEAD),
		new Integer(EnvResource.RESOURCE_GOLD),
		new Integer(EnvResource.RESOURCE_SILVER),
		new Integer(EnvResource.RESOURCE_ZINC),
		new Integer(EnvResource.RESOURCE_COPPER),
		new Integer(EnvResource.RESOURCE_TIN),
		new Integer(EnvResource.RESOURCE_MITHRIL),
		new Integer(EnvResource.RESOURCE_MUSHROOMS),
		new Integer(EnvResource.RESOURCE_GEM),
		new Integer(EnvResource.RESOURCE_PERIDOT),
		new Integer(EnvResource.RESOURCE_DIAMOND),
		new Integer(EnvResource.RESOURCE_LAPIS),
		new Integer(EnvResource.RESOURCE_BLOODSTONE),
		new Integer(EnvResource.RESOURCE_MOONSTONE),
		new Integer(EnvResource.RESOURCE_ALEXANDRITE),
		new Integer(EnvResource.RESOURCE_GEM),
		new Integer(EnvResource.RESOURCE_SCALES),
		new Integer(EnvResource.RESOURCE_CRYSTAL),
		new Integer(EnvResource.RESOURCE_PLATINUM)};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return CaveRoom.roomResources;}
}
