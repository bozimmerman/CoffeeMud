package com.planet_ink.coffee_mud.Items.MiscMagic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdFood;
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
public class StdPill extends StdFood implements Pill
{
	public String ID(){	return "StdPill";}
	protected Ability theSpell;

	public StdPill()
	{
		super();

		setName("a pill");
		baseEnvStats.setWeight(1);
		setDisplayText("An strange pill lies here.");
		setDescription("Large and round, with strange markings.");
		secretIdentity="Surely this is a potent pill!";
		baseGoldValue=200;
		recoverEnvStats();
		material=EnvResource.RESOURCE_CORN;
	}



	public String secretIdentity()
	{
		return StdScroll.makeSecretIdentity("pill",super.secretIdentity(),"",getSpells(this));
	}

	public void eatIfAble(MOB mob)
	{
		Vector spells=getSpells();
		if((mob.isMine(this))&&(spells.size()>0))
			for(int i=0;i<spells.size();i++)
			{
				Ability thisOne=(Ability)((Ability)spells.elementAt(i)).copyOf();
				thisOne.invoke(mob,mob,true,envStats().level());
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
			((Item)me).recoverEnvStats();
		return theSpells;
	}
	
	public Vector getSpells(){ return getSpells(this);}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_EAT:
				if((msg.sourceMessage()==null)&&(msg.othersMessage()==null))
				{
					eatIfAble(mob);
					super.executeMsg(myHost,msg);
				}
				else
					msg.addTrailerMsg(new FullMsg(msg.source(),msg.target(),msg.tool(),msg.NO_EFFECT,null,msg.targetCode(),msg.targetMessage(),msg.NO_EFFECT,null));
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
