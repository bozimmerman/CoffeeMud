package com.planet_ink.coffee_mud.Items.MiscMagic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdDrink;
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
public class StdPotion extends StdDrink implements Potion
{
	public String ID(){	return "StdPotion";}
	public StdPotion()
	{
		super();

		setName("a potion");
		baseEnvStats.setWeight(1);
		setDisplayText("An empty potion sits here.");
		setDescription("An empty potion with strange residue.");
		secretIdentity="What was once a powerful potion.";
		baseGoldValue=200;
		material=EnvResource.RESOURCE_GLASS;
		recoverEnvStats();
	}



	public int liquidType(){return EnvResource.RESOURCE_DRINKABLE;}
	public boolean isDrunk(){return (getSpellList().toUpperCase().indexOf(";DRUNK")>=0);}
	public int value()
	{
		if(isDrunk())
			return 0;
		else
			return super.value();
	}

	public void setDrunk(boolean isTrue)
	{
		if(isTrue&&isDrunk()) return;
		if((!isTrue)&&(!isDrunk())) return;
		if(isTrue)
			setSpellList(getSpellList()+";DRUNK");
		else
		{
			String list="";
			Vector theSpells=getSpells();
			for(int v=0;v<theSpells.size();v++)
				list+=((Ability)theSpells.elementAt(v)).ID()+";";
			setSpellList(list);
		}
	}

	public void drinkIfAble(MOB mob)
	{
		Vector spells=getSpells();
		if(mob.isMine(this))
			if((!isDrunk())&&(spells.size()>0))
				for(int i=0;i<spells.size();i++)
				{
					Ability thisOne=(Ability)((Ability)spells.elementAt(i)).copyOf();
					thisOne.invoke(mob,mob,true,envStats().level());
					setDrunk(true);
					setLiquidRemaining(0);
				}
	}

	public String getSpellList()
	{ return miscText;}
	public void setSpellList(String list){miscText=list;}
	
	public static Vector getSpells(SpellHolder me)
	{
		int baseValue=200;
		Vector theSpells=new Vector();
		String names=me.getSpellList();
		int del=names.indexOf(";");
		while(del>=0)
		{
			String thisOne=names.substring(0,del);
			if((thisOne.length()>0)&&(!thisOne.equals(";")))
			{
				Ability A=(Ability)CMClass.getAbility(thisOne);
				if(A!=null)
				{
					A=(Ability)A.copyOf();
					baseValue+=(100*CMAble.lowestQualifyingLevel(A.ID()));
					theSpells.addElement(A);
				}
			}
			names=names.substring(del+1);
			del=names.indexOf(";");
		}
		if((names.length()>0)&&(!names.equals(";")))
		{
			Ability A=(Ability)CMClass.getAbility(names);
			if(A!=null)
			{
				A=(Ability)A.copyOf();
				baseValue+=(100*CMAble.lowestQualifyingLevel(A.ID()));
				theSpells.addElement(A);
			}
		}
		if(me instanceof Item)
		{
			((Item)me).setBaseValue(baseValue);
			((Item)me).recoverEnvStats();
		}
		return theSpells;
	}
	
	public Vector getSpells(){ return getSpells(this);}

	public String secretIdentity()
	{
		return StdScroll.makeSecretIdentity("potion",super.secretIdentity(),"",getSpells(this));
	}

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
					drinkIfAble(mob);
					mob.tell(name()+" vanishes!");
					destroy();
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

}
