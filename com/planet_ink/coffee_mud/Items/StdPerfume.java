package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdPerfume extends StdDrink implements Perfume
{
	public String ID(){	return "StdPerfume";}

	Vector smellList=new Vector();
	
	public StdPerfume()
	{
		super();
		setName("a bottle of perfume");
		setDisplayText("a bottle of perfume sits here.");

		material=EnvResource.RESOURCE_GLASS;
		amountOfThirstQuenched=1;
		amountOfLiquidHeld=10;
		amountOfLiquidRemaining=10;
		disappearsAfterDrinking=true;
		liquidType=EnvResource.RESOURCE_PERFUME;
		capacity=0;
		baseGoldValue=100;
		recoverEnvStats();
	}

	public Vector getSmellEmotes(Perfume me)
	{	return smellList;}
	public String getSmellList()
	{
		StringBuffer list=new StringBuffer("");
		for(int i=0;i<smellList.size();i++)
			list.append(";"+((String)smellList.elementAt(i)));
		return list.toString();
	}
	public void setSmellList(String list)
	{smellList=Util.parseSemicolons(list,true);}
	
	public void wearIfAble(MOB mob, Perfume me)
	{
		Behavior E=mob.fetchBehavior("Emoter");
		if(E!=null)
			mob.tell("The perfume wouldn't do you any good right now anyway.");
		else
		{
			E=CMClass.getBehavior("Emoter");
			mob.addBehavior(E);
			String s=getSmellList();
			if(s.toUpperCase().indexOf("EXPIRES")<0)
				s="expires=100 "+s;
			E.setParms("SMELL "+s);
		}
	}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(msg.target()==this)
		{
			if(msg.targetMinor()==CMMsg.TYP_WEAR)
				return true;
			else
			{
				if(!super.okMessage(myHost,msg))
					return false;
				if(msg.targetMinor()==CMMsg.TYP_DRINK)
				{
					msg.source().tell("You don't want to be drinking that.");
					return false;
				}
				return true;
			}
		}
		else
			return super.okMessage(myHost,msg);
	}
	
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.target()==this)
		{
			if(msg.targetMinor()==CMMsg.TYP_WEAR)
			{
				amountOfLiquidRemaining-=amountOfThirstQuenched;
				wearIfAble(msg.source(),this);
				if(disappearsAfterDrinking)
					destroy();
				return;
			}
		}
		super.executeMsg(myHost,msg);
	}
}
