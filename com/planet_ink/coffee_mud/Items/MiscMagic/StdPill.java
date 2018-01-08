package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.Items.Basic.StdFood;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "StdPill";
	}

	protected Ability	theSpell;

	public StdPill()
	{
		super();

		setName("a pill");
		basePhyStats.setWeight(1);
		setDisplayText("A strange pill lies here.");
		setDescription("Large and round, with strange markings.");
		secretIdentity="Surely this is a potent pill!";
		baseGoldValue=200;
		recoverPhyStats();
		material=RawMaterial.RESOURCE_CORN;
	}

	@Override
	public String secretIdentity()
	{
		return StdScroll.makeSecretIdentity("pill",super.secretIdentity(),"",getSpells(this));
	}

	@Override
	public void eatIfAble(MOB mob)
	{
		final List<Ability> spells=getSpells();
		if((mob.isMine(this))&&(spells.size()>0))
		{
			final MOB caster=CMLib.map().getFactoryMOB(mob.location());
			boolean destroyCaster = true;
			for(int i=0;i<spells.size();i++)
			{
				final Ability thisOneA=(Ability)spells.get(i).copyOf();
				int level=phyStats().level();
				final int lowest=CMLib.ableMapper().lowestQualifyingLevel(thisOneA.ID());
				if(level<lowest)
					level=lowest;
				caster.basePhyStats().setLevel(level);
				caster.phyStats().setLevel(level);
				thisOneA.invoke(caster,mob,true,level);
				Ability effectA=mob.fetchEffect(thisOneA.ID());
				if((effectA!=null) && (effectA.invoker() == caster))
					destroyCaster =  false; 
			}
			if(destroyCaster)
				caster.destroy();
		}
	}

	@Override
	public String getSpellList()
	{
		return miscText;
	}

	@Override
	public void setSpellList(String list)
	{
		miscText = list;
	}

	public static Vector<Ability> getSpells(SpellHolder me)
	{
		final Vector<Ability> theSpells=new Vector<Ability>();
		final String names=me.getSpellList();
		final List<String> parsedSpells=CMParms.parseSemicolons(names, true);
		for(String thisOne : parsedSpells)
		{
			thisOne=thisOne.trim();
			String parms="";
			final int x=thisOne.indexOf('(');
			if((x>0)&&(thisOne.endsWith(")")))
			{
				parms=thisOne.substring(x+1,thisOne.length()-1);
				thisOne=thisOne.substring(0,x).trim();
			}
			Ability A=CMClass.getAbility(thisOne);
			if((A!=null)&&((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_ARCHON))
			{
				A=(Ability)A.copyOf();
				A.setMiscText(parms);
				theSpells.addElement(A);
			}
		}
		me.recoverPhyStats();
		return theSpells;
	}

	@Override
	public List<Ability> getSpells()
	{
		return getSpells(this);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			final MOB mob=msg.source();
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
