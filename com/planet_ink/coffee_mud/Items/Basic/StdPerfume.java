package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2008 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class StdPerfume extends StdDrink implements Perfume
{
	public String ID(){	return "StdPerfume";}

	Vector smellList=new Vector();
	
	public StdPerfume()
	{
		super();
		setName("a bottle of perfume");
		setDisplayText("a bottle of perfume sits here.");

		material=RawMaterial.RESOURCE_GLASS;
		amountOfThirstQuenched=1;
		amountOfLiquidHeld=10;
		amountOfLiquidRemaining=10;
		disappearsAfterDrinking=true;
		liquidType=RawMaterial.RESOURCE_PERFUME;
		capacity=0;
		baseGoldValue=100;
		setRawProperLocationBitmap(Item.WORN_WIELD|Item.WORN_ABOUT_BODY|Item.WORN_FLOATING_NEARBY|Item.WORN_HELD|Item.WORN_ARMS|Item.WORN_BACK|Item.WORN_EARS|Item.WORN_EYES|Item.WORN_FEET|Item.WORN_HANDS|Item.WORN_HEAD|Item.WORN_LEFT_FINGER|Item.WORN_RIGHT_FINGER|Item.WORN_LEGS|Item.WORN_LEFT_WRIST|Item.WORN_MOUTH|Item.WORN_NECK|Item.WORN_RIGHT_WRIST|Item.WORN_TORSO|Item.WORN_WAIST);
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
	{smellList=CMParms.parseSemicolons(list,true);}
	
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
				s="expires=50 "+s;
			if(s.toUpperCase().trim().startsWith("SMELL "))
				E.setMiscText(s);
			else
				E.setMiscText("SMELL "+s);
			mob.addNonUninvokableEffect(E);
			E.setSavable(false);
		}
	}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(msg.target()==this)
		{
			if(msg.targetMinor()==CMMsg.TYP_WEAR)
				return true;
			if(!super.okMessage(myHost,msg))
				return false;
			if(msg.targetMinor()==CMMsg.TYP_DRINK)
			{
				msg.source().tell("You don't want to be drinking that.");
				return false;
			}
			return true;
		}
		return super.okMessage(myHost,msg);
	}
	
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.target()==this)
		{
			if(msg.targetMinor()==CMMsg.TYP_WEAR)
			{
				// the order that these things are checked in should
				// be holy, and etched in stone.
				for(int b=0;b<numBehaviors();b++)
				{
					Behavior B=fetchBehavior(b);
					if(B!=null)
						B.executeMsg(this,msg);
				}

				for(int a=0;a<numEffects();a++)
				{
					Ability A=fetchEffect(a);
					if(A!=null)
						A.executeMsg(this,msg);
				}
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
