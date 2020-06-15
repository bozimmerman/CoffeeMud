package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.collections.Converter;
import com.planet_ink.coffee_mud.core.collections.ConvertingEnumeration;
import com.planet_ink.coffee_mud.core.collections.IteratorEnumeration;
import com.planet_ink.coffee_mud.core.collections.Pair;
import com.planet_ink.coffee_mud.core.collections.PairList;
import com.planet_ink.coffee_mud.core.collections.PairVector;
import com.planet_ink.coffee_mud.core.collections.XVector;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.Races.interfaces.Race;

import java.util.*;

/*
   Copyright 2015-2020 Bo Zimmerman

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
public class Prop_ItemSlotFiller extends Property implements AbilityContainer
{
	@Override
	public String ID()
	{
		return "Prop_ItemSlotFiller";
	}

	@Override
	public String name()
	{
		return "Provides for enhanced item slots.";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	protected int 			slotCount	= 1;
	protected String		slotType	= "";
	protected Ability[] 	affects		= new Ability[0];
	protected Physical		affected2	= null;
	protected List<String>	skips		= new Vector<String>(0);
	protected static Item 	fakeItem	= null;

	protected PairList<String, String>	adds		= new PairVector<String, String>(0);


	@Override
	public CMObject copyOf()
	{
		final Prop_ItemSlotFiller pA = (Prop_ItemSlotFiller)super.copyOf();
		if(skips != null)
			pA.skips = new XVector<String>(skips);
		if(adds != null)
		{
			pA.adds = new PairVector<String, String>();
			pA.adds.addAll(adds);
		}
		if(affects != null)
		{
			pA.affects = new Ability[affects.length];
			for(int i=0;i<affects.length;i++)
			{
				if(affects[i]!=null)
					pA.affects[i] = (Ability)affects[i].copyOf();
			}
		}
		return pA;
	}

	@Override
	public String accountForYourself()
	{
		if(numAbilities()==0)
			return "";
		final StringBuilder str=new StringBuilder("Adds the following effects: ");
		for(final Enumeration<Ability> a=abilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A!=null)
				str.append(A.accountForYourself()+", ");
		}
		if(str.length()>2)
			str.delete(str.length()-2,str.length());
		str.append(".  ");
		return str.toString();
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		if((P==this.affected)||(affected==null))
		{
			super.setAffectedOne(P);
		}
		else
		{
			affected2 = P;
			for(final Ability A : getAffects())
			{
				if((A!=null)&&(!A.ID().equals("Prop_ItemSlot")))
					A.setAffectedOne(P);
			}
		}
	}

	protected Physical getAffected()
	{
		if(affected2 instanceof Item)
			return affected2;
		if(fakeItem == null)
		{
			fakeItem=CMClass.getBasicItem("StdItem");
		}
		return fakeItem;
	}

	protected Ability[] getAffects()
	{
		if((affects==null)
		&&(this.affecting()!=null))
		{
			final List<Ability> newAffects=new LinkedList<Ability>();
			if(affecting().numEffects()>1)
			{
				for(final Enumeration<Ability> a=affecting().effects();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((A!=this)
					&&(!skips.contains(A.ID().toUpperCase())))
					{
						A.setAffectedOne(getAffected());
						newAffects.add(A); // not the copy!
					}
				}
			}
			for(final Enumeration<Ability> a=this.abilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				A.setAffectedOne(getAffected());
				newAffects.add(A); // not the copy!
			}
			affects=newAffects.toArray(new Ability[0]);
		}
		return affects;
	}

	@Override
	public void setMiscText(final String text)
	{
		slotCount = CMParms.getParmInt(text, "NUM", 1);
		slotType= CMParms.getParmStr(text, "TYPE", "");
		skips.clear();
		skips.addAll(CMParms.parseCommas(CMParms.getParmStr(text, "SKIPS", "").toUpperCase(), true));
		adds.clear();
		int addState=0;
		final String addStr=CMParms.getParmStr(text, "ADDS", "");
		int lastDex=0;
		Pair<String,String> p=new Pair<String,String>("","");
		for(int i=0;i<addStr.length();i++)
		{
			final char c=addStr.charAt(i);
			switch(addState)
			{
			case 0:
				if((c=='(')&&(lastDex!=i))
				{
					p.first=addStr.substring(lastDex,i).trim();
					lastDex=i+1;
					addState=1;
				}
				else
				if((c==',')&&(lastDex!=i))
				{
					p.first=addStr.substring(lastDex,i).trim();
					lastDex=i+1;
					if((p.first.length()>0)&&(CMClass.getAbilityPrototype(p.first)!=null))
					{
						adds.add(p);
						p=new Pair<String,String>("","");
					}
				}
				break;
			case 1:
				if(c==')')
				{
					if(lastDex!=i)
						p.second=addStr.substring(lastDex,i);
					addState=2;
				}
				break;
			case 2:
				if(c==',')
				{
					if((p.first.length()>0)&&(CMClass.getAbilityPrototype(p.first)!=null))
					{
						adds.add(p);
						p=new Pair<String,String>("","");
					}
					lastDex=i+1;
					addState=0;
				}
				break;
			}
		}
		if((p.first.length()>0)&&(CMClass.getAbilityPrototype(p.first)!=null))
		{
			adds.add(p);
			if(addState == 1)
				p.second=addStr.substring(lastDex);
		}
		affects=null;
		super.setMiscText(text);
	}

	protected boolean isSlotted()
	{
		final Physical affected=this.affected;
		if(affected == null)
			return true;
		if(!(affected instanceof Item))
			return true;
		final Item I=(Item)affected;
		if(I.owner() == null) // this is the key "tell"
			return true;
		return false;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(isSlotted()
		&&(myHost instanceof Physical))
		{
			final Physical P=(affected2 != null)?affected2:(Physical)myHost;
			for(final Ability A : getAffects())
			{
				if((A!=null)&&(!A.ID().equals("Prop_ItemSlot")))
				{
					if(!A.okMessage(P, msg))
						return false;
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(isSlotted()
		&&(myHost instanceof Physical))
		{
			final Physical P=(affected2 != null)?affected2:(Physical)myHost;
			for(final Ability A : getAffects())
			{
				if((A!=null)&&(!A.ID().equals("Prop_ItemSlot")))
				{
					A.executeMsg(P, msg);
				}
			}
		}
		super.executeMsg(myHost, msg);
	}

	@Override
	public void affectPhyStats(final Physical host, final PhyStats affectableStats)
	{
		if(isSlotted())
		{
			final Physical P=(affected2 != null)?affected2:host;
			for(final Ability A : getAffects())
			{
				if((A!=null)&&(!A.ID().equals("Prop_ItemSlot")))
				{
					A.affectPhyStats(P, affectableStats);
				}
			}
		}
		super.affectPhyStats(host,affectableStats);
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectedStats)
	{
		if(isSlotted())
		{
			for(final Ability A : getAffects())
			{
				if((A!=null)&&(!A.ID().equals("Prop_ItemSlot")))
				{
					A.affectCharStats(affectedMOB, affectedStats);
				}
			}
		}
		super.affectCharStats(affectedMOB,affectedStats);
	}

	@Override
	public void affectCharState(final MOB affectedMOB, final CharState affectedState)
	{
		if(isSlotted())
		{
			for(final Ability A : getAffects())
			{
				if((A!=null)&&(!A.ID().equals("Prop_ItemSlot")))
				{
					A.affectCharState(affectedMOB, affectedState);
				}
			}
		}
		super.affectCharState(affectedMOB,affectedState);
	}

	@Override
	public void addAbility(final Ability to)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public void delAbility(final Ability to)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public int numAbilities()
	{
		return adds.size();
	}

	@Override
	public Ability fetchAbility(final int index)
	{
		if((index<0)||(index>=adds.size()))
			return null;
		final Ability A=CMClass.getAbility(adds.getFirst(index));
		if(A!=null)
			A.setMiscText(adds.getSecond(index));
		return A;
	}

	@Override
	public Ability fetchAbility(final String ID)
	{
		for(int i=0;i<adds.size();i++)
		{
			if(adds.getFirst(i).equalsIgnoreCase(ID))
			{
				final Ability A=CMClass.getAbility(adds.getFirst(i));
				if(A!=null)
					A.setMiscText(adds.getSecond(i));
				return A;
			}
		}
		return null;
	}

	@Override
	public Ability fetchRandomAbility()
	{
		if(adds.size()==0)
			return null;
		return this.fetchAbility(CMLib.dice().roll(1, adds.size(), -1));
	}

	@Override
	public Enumeration<Ability> abilities()
	{
		return new ConvertingEnumeration<Pair<String,String>, Ability>(
				new IteratorEnumeration<Pair<String,String>>(adds.iterator()),
				new Converter<Pair<String,String>, Ability>()
		{
			@Override
			public Ability convert(final Pair<String, String> obj)
			{
				if(obj==null)
					return null;
				final Ability A=CMClass.getAbility(obj.first);
				if(A!=null)
					A.setMiscText(obj.second);
				return A;
			}
		});
	}

	@Override
	public void delAllAbilities()
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public int numAllAbilities()
	{
		return adds.size();
	}

	@Override
	public Enumeration<Ability> allAbilities()
	{
		return abilities();
	}
}
