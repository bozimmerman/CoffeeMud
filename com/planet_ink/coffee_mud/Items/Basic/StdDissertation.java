package com.planet_ink.coffee_mud.Items.Basic;
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
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2017-2018 Bo Zimmerman

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

public class StdDissertation extends StdItem implements Scroll
{
	@Override
	public String ID()
	{
		return "StdDissertation";
	}

	protected String readableScrollBy=null;

	public StdDissertation()
	{
		super();

		setName("a dissertation");
		basePhyStats.setWeight(1);
		setDisplayText("a dissertation is rolled up here.");
		setDescription("A rolled up parchment with detailed instructions.");
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
		return super.value();
	}

	@Override
	public boolean useTheScroll(Ability A, MOB mob)
	{
		if(!A.canBeLearnedBy(null, mob))
			return false;
		return true;
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
	public String secretIdentity()
	{
		return makeSecretIdentity("dissertation",super.secretIdentity(),"",getSpells());
	}

	@Override
	public void readIfAble(final MOB mob, String spellName)
	{
		if(mob.isMine(this))
		{
			if(isReadableScrollBy(mob.Name()))
			{
				List<Ability> spellsList=getSpells();
				if(spellsList.size()==0)
					mob.tell(L("The dissertation appears to contain no useful information."));
				else
				{
					Ability thisOne=null;
					final Vector<String> params=new Vector<String>();
					if(spellName.length()>0)
					{
						spellName=spellName.trim();
						thisOne=(Ability)CMLib.english().fetchEnvironmental(spellsList,spellName,true);
						if(thisOne==null)
							thisOne=(Ability)CMLib.english().fetchEnvironmental(spellsList,spellName,false);
						while((thisOne==null)&&(spellName.length()>0))
						{

							final int t=spellName.lastIndexOf(' ');
							if(t<0)
								spellName="";
							else
							{
								params.insertElementAt(spellName.substring(t).trim(),0);
								spellName=spellName.substring(0,t);
								thisOne=(Ability)CMLib.english().fetchEnvironmental(spellsList,spellName,true);
								if(thisOne==null)
									thisOne=(Ability)CMLib.english().fetchEnvironmental(spellsList,spellName,false);
							}
						}
					}
					
					if((thisOne == null)&&(spellsList.size()==1))
						thisOne=spellsList.get(0);
					
					final Room R=mob.location();
					if((thisOne != null)
					&&(!CMLib.ableMapper().qualifiesByLevel(mob, thisOne))
					&&(R!=null))
					{
						String ableID=null;
						switch(thisOne.classificationCode()&Ability.ALL_ACODES)
						{
							case Ability.ACODE_SKILL:
								ableID="Skill_Skillcraft";
								break;
							case Ability.ACODE_SPELL:
								ableID="Skill_Spellcraft";
								break;
							case Ability.ACODE_PRAYER:
								ableID="Skill_Prayercraft";
								break;
							case Ability.ACODE_SONG:
								ableID="Skill_Songcraft";
								break;
							case Ability.ACODE_THIEF_SKILL:
								ableID="Skill_Thiefcraft";
								break;
							case Ability.ACODE_CHANT:
								ableID="Skill_Chantcraft";
								break;
							case Ability.ACODE_TRAP:
							case Ability.ACODE_PROPERTY:
							case Ability.ACODE_LANGUAGE:
							case Ability.ACODE_COMMON_SKILL:
							case Ability.ACODE_DISEASE:
							case Ability.ACODE_POISON:
							case Ability.ACODE_SUPERPOWER:
							case Ability.ACODE_TECH:
								break;
						}
						if(ableID!=null)
						{
							final Ability A=mob.fetchAbility(ableID);
							if(A!=null)
							{
								A.setMiscText(thisOne.ID());
								final MOB targetM=CMClass.getFactoryMOB();
								try
								{
									targetM.addAbility(thisOne);
									final CMMsg msg = CMClass.getMsg(mob,mob,A,CMMsg.MSG_OK_VISUAL,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
									if(R.okMessage(mob, msg))
										R.send(mob, msg);
								}
								finally
								{
									targetM.delAllAbilities();
									targetM.destroy();
								}
							}
						}
					}
					
					if((thisOne!=null)&&(useTheScroll(thisOne,mob)))
					{
						final Ability learnThisAbility=(Ability)thisOne.copyOf();
						final String name = this.name;
						final Item item = this;
						final Runnable learnIt = new Runnable()
						{
							public final Ability learnA = learnThisAbility;
							public final Room	 R 		= mob.location();
							public final String	 mobName= name;
							public final Item 	 I		= item;
							
							@Override
							public void run()
							{
								int level=phyStats().level();
								final int lowest=CMLib.ableMapper().lowestQualifyingLevel(learnA.ID());
								if(level<lowest)
									level=lowest;
								final MOB teacher = CMClass.getFactoryMOB(mobName, lowest, R);
								teacher.setAttribute(Attrib.NOTEACH, false);
								try
								{
									learnThisAbility.setProficiency(100);
									teacher.addAbility(learnA);
									CMLib.expertises().postTeach(teacher,mob,learnA);
									Ability A=mob.fetchAbility(learnA.ID());
									if(A!=null)
										A.setProficiency(0); //really? Why use these, ever?
								}
								finally
								{
									teacher.delAllAbilities();
									teacher.destroy();
								}
								if(I.usesRemaining()<2)
									I.destroy();
								else
									I.setUsesRemaining(I.usesRemaining()-1);
							}
						};
						final Session sess=mob.session();
						if((mob.isMonster())||(sess==null))
							learnIt.run();
						else
						{
							sess.prompt(new InputCallback(InputCallback.Type.CONFIRM,"N",0)
							{
								final Runnable learn = learnIt;
								
								@Override
								public void showPrompt()
								{
									sess.promptPrint(L("\n\rWould you like to learn @x1 from @x2 (y/N)?",learnThisAbility.Name(),name));
								}

								@Override
								public void timedOut()
								{
								}

								@Override
								public void callBack()
								{
									if(this.input.equals("Y"))
									{
										learn.run();
									}
								}
							});
						}
					}
					else
					if(spellName.length()>0)
						mob.tell(L("Instructions for that are not written here."));
					else
					if(!mob.isMonster())
					{
						final StringBuffer theNews=new StringBuffer("The dissertation contains instructions for the following:\n\r");
						spellsList=getSpells();
						for(int u=0;u<spellsList.size();u++)
						{
							final Ability A=spellsList.get(u);
							theNews.append("Level "+CMStrings.padRight(""+CMLib.ableMapper().lowestQualifyingLevel(A.ID()),2)+": "+A.name()+"\n\r");
						}
						mob.tell(theNews.toString());
					}
				}
			}
			else
				mob.tell(L("You don't quite understand the writing."));
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
		return (readableScrollBy == null) || (readableScrollBy.equalsIgnoreCase(name));
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
		if(!(E instanceof StdDissertation))
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
