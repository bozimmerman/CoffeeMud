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
		setRawProperLocationBitmap(Item.WIELD|Item.ABOUT_BODY|Item.FLOATING_NEARBY|Item.HELD|Item.ON_ARMS|Item.ON_BACK|Item.ON_EARS|Item.ON_EYES|Item.ON_FEET|Item.ON_HANDS|Item.ON_HEAD|Item.ON_LEFT_FINGER|Item.ON_RIGHT_FINGER|Item.ON_LEGS|Item.ON_LEFT_WRIST|Item.ON_MOUTH|Item.ON_NECK|Item.ON_RIGHT_WRIST|Item.ON_TORSO|Item.ON_WAIST);
		recoverEnvStats();
	}

	public Vector getSmellEmotes(Perfume me)
	{	return smellList;}
	public String getSmellList()
	{
		StringBuffer list=new StringBuffer("");
		for(int i=0;i<smellList.size();i++)
			list.append(((String)smellList.elementAt(i))+";");
		return list.toString();
	}
	public void setSmellList(String list)
	{smellList=Util.parseSemicolons(list,true);}
	
	public void wearIfAble(MOB mob, Perfume me)
	{
		Ability E=mob.fetchEffect("Prop_MOBEmoter");
		if(E!=null)
			mob.tell("You can't put any perfume on right now.");
		else
		{
			E=CMClass.getAbility("Prop_MOBEmoter");
			String s=getSmellList();
			if(s.toUpperCase().indexOf("EXPIRES")<0)
				s="expires=100 "+s;
			E.setMiscText("SMELL "+s);
			mob.addNonUninvokableEffect(E);
			E.setBorrowed(mob,true);
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
