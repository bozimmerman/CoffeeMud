package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.Items.Basic.StdFood;
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
   Copyright 2000-2010 Bo Zimmerman

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
public class StdPill extends StdFood implements Pill
{
	public String ID(){	return "StdPill";}
	protected Ability theSpell;

	public StdPill()
	{
		super();

		setName("a pill");
		baseEnvStats.setWeight(1);
		setDisplayText("A strange pill lies here.");
		setDescription("Large and round, with strange markings.");
		secretIdentity="Surely this is a potent pill!";
		baseGoldValue=200;
		recoverEnvStats();
		material=RawMaterial.RESOURCE_CORN;
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
				Ability A=CMClass.getAbility(thisOne);
				if((A!=null)&&((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_ARCHON))
				{
					A=(Ability)A.copyOf();
					baseValue+=(100*CMLib.ableMapper().lowestQualifyingLevel(A.ID()));
					theSpells.addElement(A);
				}
			}
			names=names.substring(del+1);
			del=names.indexOf(";");
		}
		if((names.length()>0)&&(!names.equals(";")))
		{
			Ability A=CMClass.getAbility(names);
			if(A!=null)
			{
				A=(Ability)A.copyOf();
				baseValue+=(100*CMLib.ableMapper().lowestQualifyingLevel(A.ID()));
				theSpells.addElement(A);
			}
		}
		me.recoverEnvStats();
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
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),msg.target(),msg.tool(),CMMsg.NO_EFFECT,null,msg.targetCode(),msg.targetMessage(),CMMsg.NO_EFFECT,null));
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
