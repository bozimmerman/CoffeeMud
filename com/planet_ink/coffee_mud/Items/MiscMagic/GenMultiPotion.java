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
public class GenMultiPotion extends GenDrink implements Potion
{
	public String ID(){	return "GenMultiPotion";}

	public GenMultiPotion()
	{
		super();

		material=EnvResource.RESOURCE_GLASS;
		setName("a flask");
		baseEnvStats.setWeight(1);
		setDisplayText("A flask sits here.");
		setDescription("A strange flask with stranger markings.");
		secretIdentity="";
		baseGoldValue=200;
		recoverEnvStats();
	}


	public boolean isGeneric(){return true;}
	public int liquidType(){return EnvResource.RESOURCE_DRINKABLE;}

	public boolean isDrunk(){return (readableText.toUpperCase().indexOf(";DRUNK")>=0);}
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

	public String secretIdentity()
	{
		return StdScroll.makeSecretIdentity("potion",super.secretIdentity(),"",getSpells());
	}

	public int value()
	{
		if(isDrunk())
			return 0;
		else
			return super.value();
	}

	public String getSpellList()
	{ return readableText;}
	public void setSpellList(String list){readableText=list;}
	public Vector getSpells()
	{
		return StdPotion.getSpells(this);
	}
	public void setReadableText(String text){
		readableText=text;
		setSpellList(readableText);
	}
	

	public void drinkIfAble(MOB mob)
	{
		Vector spells=getSpells();
		if(mob.isMine(this))
		{
			if((!isDrunk())&&(spells.size()>0))
			{
				for(int i=0;i<spells.size();i++)
				{
					Ability thisOne=(Ability)((Ability)spells.elementAt(i)).copyOf();
					thisOne.invoke(mob,mob,true);
				}
			}

			if((liquidRemaining()<=thirstQuenched())&&(!isDrunk()))
				setDrunk(true);
		}

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
					if(isDrunk())
					{
						mob.tell(name()+" vanishes!");
						destroy();
					}
				}
				else
				{
					msg.addTrailerMsg(new FullMsg(msg.source(),this,msg.tool(),msg.NO_EFFECT,null,msg.targetCode(),msg.targetMessage(),msg.NO_EFFECT,null));
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
	// stats handled by gendrink, spells by readabletext
}
