package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.GenDrink;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class GenPotion extends GenDrink implements Potion
{
	public String ID(){	return "GenPotion";}
	protected Ability theSpell;

	public GenPotion()
	{
		super();

		setName("a potion");
		baseEnvStats.setWeight(1);
		setDisplayText("A potion sits here.");
		setDescription("A strange potion with stranger markings.");
		secretIdentity="";
		baseGoldValue=200;
		recoverEnvStats();
		material=EnvResource.RESOURCE_GLASS;
	}



	public boolean isGeneric(){return true;}
	public int liquidType(){return EnvResource.RESOURCE_DRINKABLE;}

	public boolean isDrunk(){return (readableText.toUpperCase().indexOf(";DRUNK")>=0);}
	public void setDrunk(Potion me, boolean isTrue)
	{ new StdPotion().setDrunk(this,isTrue);}

	public String secretIdentity()
	{
		return StdScroll.makeSecretIdentity("potion",super.secretIdentity(),"",getSpells(this));
	}

	public int value()
	{
		if(isDrunk())
			return 0;
		else
			return super.value();
	}

	public void drinkIfAble(MOB mob, Potion me)
	{new StdPotion().drinkIfAble(mob,me);}

	public String getSpellList()
	{ return readableText;}
	public void setSpellList(String list){readableText=list;}
	public Vector getSpells(Potion me)
	{	return new StdPotion().getSpells(me);}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((msg.amITarget(this))
		   &&(msg.targetMinor()==CMMsg.TYP_DRINK)
		   &&(msg.othersMessage()==null)
		   &&(msg.sourceMessage()==null))
				return true;
		return super.okMessage(myHost,msg);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DRINK:
				if((msg.sourceMessage()==null)&&(msg.othersMessage()==null))
				{
					drinkIfAble(mob,this);
					mob.tell(name()+" vanishes!");
					this.destroy();
				}
				else
				{
					msg.addTrailerMsg(new FullMsg(msg.source(),msg.target(),msg.tool(),msg.NO_EFFECT,null,msg.targetCode(),msg.targetMessage(),msg.NO_EFFECT,null));
					super.executeMsg(myHost,msg);
				}
				break;
			default:
				super.executeMsg(myHost,msg);
				break;
			}
		}
		else
			super.executeMsg(myHost,msg);
	}
	// stats handled by GenDrink, spells by readable text
}
