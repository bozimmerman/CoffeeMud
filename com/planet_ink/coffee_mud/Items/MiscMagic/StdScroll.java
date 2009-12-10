package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
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
public class StdScroll extends StdItem implements MiscMagic, Scroll
{
	public String ID(){	return "StdScroll";}
	protected String readableScrollBy=null;

	public StdScroll()
	{
		super();

		setName("a scroll");
		baseEnvStats.setWeight(1);
		setDisplayText("a scroll is rolled up here.");
		setDescription("A rolled up parchment marked with mystical symbols.");
		secretIdentity="";
		material=RawMaterial.RESOURCE_PAPER;
		baseGoldValue=200;
		recoverEnvStats();
	}



	public String getSpellList()
	{ return miscText;}
	public void setSpellList(String list){miscText=list;}

	public int value()
	{
		if(usesRemaining()<=0)
			return 0;
		return super.value();
	}
	public boolean useTheScroll(Ability A, MOB mob)
	{
		int manaRequired=5;
		int q=CMLib.ableMapper().qualifyingLevel(mob,A);
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
			mob.tell("You don't have enough mana.");
			return false;
		}
		mob.curState().adjMana(-manaRequired,mob.maxState());
		return true;
	}

	public String secretIdentity()
	{
		return StdScroll.makeSecretIdentity("scroll",super.secretIdentity()," Charges: "+usesRemaining(),getSpells(this));
	}

	public static String makeSecretIdentity(String thang, String id, String more, Vector V)
	{
		StringBuffer add=new StringBuffer("");
		for(int v=0;v<V.size();v++)
		{
			if(v==0)
				add.append("A "+thang+" of ");
			Ability A=(Ability)V.elementAt(v);
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
	
	public void readIfAble(MOB mob, Scroll me, String spellName)
	{
		if(mob.isMine(me))
		{
			boolean readingMagic=(mob.fetchEffect("Spell_ReadMagic")!=null);
			if(readingMagic)
			{
				mob.tell(name()+" glows softly.");
				me.setReadableScrollBy(mob.Name());
			}
			if(me.isReadableScrollBy(mob.Name()))
			{
				if(me.usesRemaining()<=0)
					mob.tell("The markings have been read off the parchment, and are no longer discernable.");
				else
				{
					Vector Spells=me.getSpells();
					if(Spells.size()==0)
						mob.tell("The scroll appears to contain no discernable information.");
					else
					{
						Ability thisOne=null;
						Vector params=new Vector();
						if(spellName.length()>0)
						{
							spellName=spellName.trim();
							thisOne=(Ability)CMLib.english().fetchEnvironmental(Spells,spellName,true);
							if(thisOne==null)
								thisOne=(Ability)CMLib.english().fetchEnvironmental(Spells,spellName,false);
							while((thisOne==null)&&(spellName.length()>0))
							{

								int t=spellName.lastIndexOf(" ");
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

						if((thisOne!=null)&&(me.useTheScroll(thisOne,mob)))
						{
							thisOne=(Ability)thisOne.copyOf();
							thisOne.invoke(mob,params,null,true,envStats().level());
							me.setUsesRemaining(me.usesRemaining()-1);
						}
						else
						if(spellName.length()>0)
							mob.tell("That is not written on the scroll.");
						else
						if(!mob.isMonster())
						{
							StringBuffer theNews=new StringBuffer("The scroll contains the following spells:\n\r");
							Spells=me.getSpells();
							for(int u=0;u<Spells.size();u++)
							{
								Ability A=(Ability)Spells.elementAt(u);
								theNews.append("Level "+CMStrings.padRight(""+CMLib.ableMapper().lowestQualifyingLevel(A.ID()),2)+": "+A.name()+"\n\r");
							}
							mob.tell(theNews.toString());
						}
					}
				}
			}
			else
				mob.tell("The markings look magical, and are unknown to you.");
		}
	}

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
		me.setBaseValue(baseValue);
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
			case CMMsg.TYP_READ:
				if((msg.sourceMessage()==null)&&(msg.othersMessage()==null))
					readIfAble(mob,this,msg.targetMessage());
				else
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),msg.target(),msg.tool(),CMMsg.NO_EFFECT,null,msg.targetCode(),msg.targetMessage(),CMMsg.NO_EFFECT,null));
				return;
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}
	public void setMiscText(String newText)
	{
		miscText=newText;
		setSpellList(newText);
	}
	public boolean isReadableScrollBy(String name){return (readableScrollBy!=null) && (readableScrollBy.equalsIgnoreCase(name));}
	public void setReadableScrollBy(String name){readableScrollBy=name;}
	protected static String[] CODES={"CLASS","LEVEL","ABILITY","TEXT"};
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return ""+baseEnvStats().ability();
		case 2: return ""+baseEnvStats().level();
		case 3: return text();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: baseEnvStats().setLevel(CMath.s_parseIntExpression(val)); break;
		case 2: baseEnvStats().setAbility(CMath.s_parseIntExpression(val)); break;
		case 3: setMiscText(val); break;
		}
	}
	public String[] getStatCodes(){return CODES;}
	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
    public boolean sameAs(Environmental E)
    {
        if(!(E instanceof StdScroll)) return false;
        String[] codes=getStatCodes();
        for(int i=0;i<codes.length;i++)
            if(!E.getStat(codes[i]).equals(getStat(codes[i])))
                return false;
        return true;
    }
}
