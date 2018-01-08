package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
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

public class StdScroll extends StdItem implements MiscMagic, Scroll
{
	@Override
	public String ID()
	{
		return "StdScroll";
	}

	protected String readableScrollBy=null;

	public StdScroll()
	{
		super();

		setName("a scroll");
		basePhyStats.setWeight(1);
		setDisplayText("a scroll is rolled up here.");
		setDescription("A rolled up parchment marked with mystical symbols.");
		secretIdentity="";
		material=RawMaterial.RESOURCE_PAPER;
		baseGoldValue=200;
		setUsesRemaining(0);
		recoverPhyStats();
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

	@Override
	public int value()
	{
		if(usesRemaining()<=0)
			return 0;
		return super.value();
	}

	@Override
	public boolean useTheScroll(Ability A, MOB mob)
	{
		int manaRequired=5;
		final int q=CMLib.ableMapper().qualifyingLevel(mob,A);
		if(q>0)
		{
			if(q<CMLib.ableMapper().qualifyingClassLevel(mob,A))
				manaRequired=0;
			else
				manaRequired=5;
		}
		else
			manaRequired=25;
		if(manaRequired>mob.curState().getMana())
		{
			mob.tell(L("You don't have enough mana."));
			return false;
		}
		mob.curState().adjMana(-manaRequired,mob.maxState());
		return true;
	}

	@Override
	public String secretIdentity()
	{
		return StdScroll.makeSecretIdentity("scroll",super.secretIdentity()," Charges: "+usesRemaining(),getSpells());
	}

	public static String makeSecretIdentity(String thang, String id, String more, List<Ability> V)
	{
		final StringBuffer add=new StringBuffer("");
		for(int v=0;v<V.size();v++)
		{
			if(v==0)
				add.append("A "+thang+" of ");
			final Ability A=V.get(v);
			if(V.size()==1)
				add.append(A.name());
			else
			if(v==(V.size()-1))
				add.append("and "+A.name());
			else
				add.append(A.name()+", ");
		}
		if(add.length()>0)
		{
			add.append(more+"\n");
		}
		add.append(id);
		return add.toString();
	}

	@Override
	public void readIfAble(MOB mob, String spellName)
	{
		if(mob.isMine(this))
		{
			int addedExpertise = 0;
			final boolean readingMagic=(mob.fetchEffect("Spell_ReadMagic")!=null);
			if(readingMagic)
			{
				mob.tell(L("@x1 glows softly.",name()));
				final Ability A=mob.fetchAbility("Spell_ReadMagic");
				if(A!=null)
					addedExpertise=CMLib.expertises().getExpertiseLevel(mob, A.ID(), ExpertiseLibrary.Flag.LEVEL);
				setReadableScrollBy(mob.Name());
			}
			if(isReadableScrollBy(mob.Name()))
			{
				if(me.usesRemaining()<=0)
					mob.tell(L("The markings have been read off the parchment, and are no longer discernable."));
				else
				{
					List<Ability> Spells=getSpells();
					if(Spells.size()==0)
						mob.tell(L("The scroll appears to contain no discernable information."));
					else
					{
						Ability thisOne=null;
						final Vector<String> params=new Vector<String>();
						if(spellName.length()>0)
						{
							spellName=spellName.trim();
							thisOne=(Ability)CMLib.english().fetchEnvironmental(Spells,spellName,true);
							if(thisOne==null)
								thisOne=(Ability)CMLib.english().fetchEnvironmental(Spells,spellName,false);
							while((thisOne==null)&&(spellName.length()>0))
							{

								final int t=spellName.lastIndexOf(' ');
								if(t<0)
									spellName="";
								else
								{
									params.insertElementAt(spellName.substring(t).trim(),0);
									spellName=spellName.substring(0,t);
									thisOne=(Ability)CMLib.english().fetchEnvironmental(Spells,spellName,true);
									if(thisOne==null)
										thisOne=(Ability)CMLib.english().fetchEnvironmental(Spells,spellName,false);
								}
							}
						}

						if((thisOne!=null)&&(useTheScroll(thisOne,mob)))
						{
							thisOne=(Ability)thisOne.copyOf();
							int level=phyStats().level() + addedExpertise;
							final int lowest=CMLib.ableMapper().lowestQualifyingLevel(thisOne.ID());
							if(level<lowest)
								level=lowest;
							thisOne.invoke(mob,params,null,true,level);
							setUsesRemaining(usesRemaining()-1);
						}
						else
						if(spellName.length()>0)
							mob.tell(L("That is not written on the scroll."));
						else
						if(!mob.isMonster())
						{
							final StringBuffer theNews=new StringBuffer("The scroll contains the following spells:\n\r");
							Spells=getSpells();
							for(int u=0;u<Spells.size();u++)
							{
								final Ability A=Spells.get(u);
								theNews.append("Level "+CMStrings.padRight(""+CMLib.ableMapper().lowestQualifyingLevel(A.ID()),2)+": "+A.name()+"\n\r");
							}
							mob.tell(theNews.toString());
						}
					}
				}
			}
			else
				mob.tell(L("The markings look magical, and are unknown to you."));
		}
	}

	@Override
	public List<Ability> getSpells()
	{
		int baseValue=200;
		final List<Ability> theSpells=new Vector<Ability>();
		final String names=getSpellList();
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
				baseValue+=(100*CMLib.ableMapper().lowestQualifyingLevel(A.ID()));
				theSpells.add(A);
			}
		}
		setBaseValue(baseValue);
		recoverPhyStats();
		return theSpells;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			final MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_READ:
				if((msg.sourceMessage()==null)&&(msg.othersMessage()==null))
					readIfAble(mob,msg.targetMessage());
				else
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),msg.target(),msg.tool(),CMMsg.NO_EFFECT,null,msg.targetCode(),msg.targetMessage(),CMMsg.NO_EFFECT,null));
				return;
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public void setMiscText(String newText)
	{
		miscText=newText;
		setSpellList(newText);
	}
	
	@Override
	public boolean isReadableScrollBy(String name)
	{
		return (readableScrollBy != null) && (readableScrollBy.equalsIgnoreCase(name));
	}

	@Override
	public void setReadableScrollBy(String name)
	{
		readableScrollBy = name;
	}

	protected static String[]	CODES	= { "CLASS", "LEVEL", "ABILITY", "TEXT" };

	@Override
	public String getStat(String code)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return ID();
		case 1:
			return "" + basePhyStats().ability();
		case 2:
			return "" + basePhyStats().level();
		case 3:
			return text();
		}
		return "";
	}

	@Override
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return;
		case 1:
			basePhyStats().setLevel(CMath.s_parseIntExpression(val));
			break;
		case 2:
			basePhyStats().setAbility(CMath.s_parseIntExpression(val));
			break;
		case 3:
			setMiscText(val);
			break;
		}
	}

	@Override
	public String[] getStatCodes()
	{
		return CODES;
	}

	@Override
	protected int getCodeNum(String code)
	{
		for(int i=0;i<CODES.length;i++)
		{
			if(code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdScroll))
			return false;
		final String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
		{
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		}
		return true;
	}
}
