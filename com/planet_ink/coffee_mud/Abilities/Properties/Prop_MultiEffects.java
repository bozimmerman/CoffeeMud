package com.planet_ink.coffee_mud.Abilities.Properties;
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
   Copyright 2021-2021 Bo Zimmerman

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
public class Prop_MultiEffects extends Property
{
	@Override
	public String ID()
	{
		return "Prop_MultiEffects";
	}

	@Override
	public String name()
	{
		return "Multi Effects";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS|CAN_ROOMS|CAN_EXITS|CAN_ITEMS;
	}

	protected Ability[]	props				= new Ability[0];
	protected int		classificationCode	= Ability.ACODE_PROPERTY;
	protected boolean	bubbleEffect		= false;
	protected int		abstractQuality		= Ability.QUALITY_INDIFFERENT;
	protected long		flags				= 0;

	private enum MPState
	{
		START,
		IN_ID,
		POST_ID,
		IN_PAREN
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		props=new Ability[0];
		classificationCode=Ability.ACODE_PROPERTY;
		abstractQuality = Ability.QUALITY_INDIFFERENT;
		bubbleEffect = false;
		flags=0;
		if(newMiscText.length()>0)
		{
			final List<Ability> newProps=new ArrayList<Ability>();
			if(newMiscText.trim().startsWith("<"))
			{
				final List<XMLLibrary.XMLTag> all=CMLib.xml().parseAllXML(newMiscText);
				for(final XMLLibrary.XMLTag tag : all)
				{
					if(tag.tag().equals("PROP"))
					{
						final String ID=tag.getParmValue("ID");
						if((ID != null)&&(ID.length()>0))
						{
							final Ability A=CMClass.getAbility(ID);
							if(A!=null)
							{
								A.setAffectedOne(this.affecting());
								A.setMiscText(tag.value());
								newProps.add(A);
							}
						}
					}
				}
			}
			else //user format
			{
				MPState state=MPState.START;
				int stackSize=0;
				int lastStart=-1;
				Ability A=null;
				for(int i=0;i<newMiscText.length();i++)
				{
					final char c=newMiscText.charAt(i);
					switch(c)
					{
					case ',':
						switch(state)
						{
						case START:
						case IN_PAREN:
							break;
						case POST_ID:
							lastStart=-1;
							A=null;
							state=MPState.START;
							break;
						case IN_ID:
						{
							if(A!=null)
							{
								Log.errOut(ID(),"Malformed properties @"+i+": "+newMiscText);
								return;
							}
							final String ID=newMiscText.substring(lastStart,i);
							A=CMClass.getAbility(ID);
							if(A==null)
							{
								Log.errOut(ID(),"Unknown Ability ID "+ID+"@"+i+": "+newMiscText);
								return;
							}
							A.setAffectedOne(affecting());
							state=MPState.START;
							newProps.add(A);
							break;
						}
						default:
							Log.errOut(ID(),"Unknown state "+state+" @ "+i+": "+newMiscText);
							return;
						}
						break;
					case ' ':
						switch(state)
						{
						case START:
						case IN_PAREN:
						case POST_ID:
							break;
						case IN_ID:
						{
							if(A!=null)
							{
								Log.errOut(ID(),"Malformed properties @"+i+": "+newMiscText);
								return;
							}
							final String ID=newMiscText.substring(lastStart,i);
							A=CMClass.getAbility(ID);
							if(A==null)
							{
								Log.errOut(ID(),"Unknown Ability ID "+ID+"@"+i+": "+newMiscText);
								return;
							}
							A.setAffectedOne(affecting());
							state=MPState.POST_ID;
							newProps.add(A);
							break;
						}
						default:
							Log.errOut(ID(),"Unknown state "+state+" @ "+i+": "+newMiscText);
							return;
						}
						break;
					case '(':
						switch(state)
						{
						case IN_PAREN:
							stackSize++;
							break;
						case START:
							Log.errOut(ID(),"Malformed properties @"+i+": Unexpected OParen: "+newMiscText);
							return;
						case POST_ID:
							state=MPState.IN_PAREN;
							stackSize=1;
							lastStart=i+1;
							break;
						case IN_ID:
							if(A!=null)
							{
								Log.errOut(ID(),"Unpected OParen: "+state+" @ "+i+": "+newMiscText);
								return;
							}
							final String ID=newMiscText.substring(lastStart,i);
							A=CMClass.getAbility(ID);
							if(A==null)
							{
								Log.errOut(ID(),"Unknown Ability ID "+ID+"@"+i+": "+newMiscText);
								return;
							}
							A.setAffectedOne(affecting());
							newProps.add(A);
							stackSize=1;
							lastStart=i+1;
							state=MPState.IN_PAREN;
							break;
						default:
							Log.errOut(ID(),"Unknown state "+state+" @ "+i+": "+newMiscText);
							return;
						}
						break;
					case ')':
						switch(state)
						{
						case IN_ID:
						case POST_ID:
						case START:
							Log.errOut(ID(),"Unpected CParen: "+state+" @ "+i+": "+newMiscText);
							return;
						case IN_PAREN:
							if(A==null)
							{
								Log.errOut(ID(),"Missing Ability in CParen: "+state+" @ "+i+": "+newMiscText);
								return;
							}
							if(--stackSize <=0)
							{
								A.setMiscText(newMiscText.substring(lastStart,i));
								A=null;
								lastStart=i+1;
								state=MPState.START;
							}
							break;
						default:
							Log.errOut(ID(),"Unknown state "+state+" @ "+i+": "+newMiscText);
							return;
						}
						break;
					default:
						switch(state)
						{
						case IN_PAREN:
							break; // all good
						case IN_ID:
							if(Character.isLetterOrDigit(c)||(c=='_'))
								break;
							Log.errOut(ID(),"Malformed properties @"+i+": "+newMiscText);
							return;
						case POST_ID:
							if(Character.isLetter(c))
							{
								lastStart=i;
								state=MPState.IN_ID;
								break;
							}
							Log.errOut(ID(),"Malformed properties @"+i+": "+newMiscText);
							return;
						case START:
							if(Character.isLetter(c))
							{
								lastStart=i;
								state=MPState.IN_ID;
								break;
							}
							Log.errOut(ID(),"Malformed properties @"+i+": "+newMiscText);
							return;
						}
						break;
					}
				}
			}
			props=newProps.toArray(new Ability[0]);
			if(props.length>0)
			{
				classificationCode=props[0].classificationCode();
				for(final Ability A : props)
				{
					if(A.bubbleAffect())
						bubbleEffect = true;
				}
				for(final Ability A : props)
				{
					if(A.abstractQuality()==Ability.QUALITY_MALICIOUS)
					{
						abstractQuality = Ability.QUALITY_MALICIOUS;
						break;
					}
					if(A.abstractQuality()!=Ability.QUALITY_INDIFFERENT)
						abstractQuality=A.abstractQuality();
				}
				for(final Ability A : props)
					flags |=A.flags();
			}
		}
		super.setMiscText(newMiscText);
	}

	@Override
	public String text()
	{
		final StringBuilder str=new StringBuilder();
		for(final Ability A : props)
			str.append("<PROP ID=\""+A.ID()+"\">"+CMLib.xml().parseOutAngleBrackets(A.text())+"</PROP>");
		return str.toString();
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		for(final Ability A : props)
		{
			if(!A.tick(ticking, tickID))
				return false;
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final Prop_MultiEffects pA =this.getClass().newInstance();
			pA.setMiscText(text());
			return pA;
		}
		catch (final Exception e)
		{
			Log.errOut(ID(), e);
		}
		return new Prop_MultiEffects();
	}

	@Override
	public String accountForYourself()
	{
		final StringBuilder str=new StringBuilder("");
		for(final Ability A : props)
			str.append(A.accountForYourself()).append("\n\r");
		return str.toString().trim();
	}

	@Override
	public int classificationCode()
	{
		return classificationCode;
	}

	@Override
	public String displayText()
	{
		final StringBuilder str = new StringBuilder("");
		for(final Ability A : props)
			str.append(A.displayText());
		return str.toString();
	}

	@Override
	public int abstractQuality()
	{
		return abstractQuality;
	}

	@Override
	public long flags()
	{
		return flags;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null)
			return;
		for(final Ability A : props)
			A.affectPhyStats(affected, affectableStats);
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected, affectableStats);
		if(affected==null)
			return;
		for(final Ability A : props)
			A.affectCharStats(affected, affectableStats);
	}

	@Override
	public void affectCharState(final MOB affected, final CharState affectableStats)
	{
		super.affectCharState(affected, affectableStats);
		if(affected==null)
			return;
		for(final Ability A : props)
			A.affectCharState(affected, affectableStats);
	}

	@Override
	public boolean bubbleAffect()
	{
		return bubbleEffect;
	}

	@Override
	public void unInvoke()
	{
		if(affected==null)
			return;
		for(final Ability A : props)
			A.unInvoke();
		super.unInvoke();
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		super.setAffectedOne(P);
		for(final Ability A : props)
			A.setAffectedOne(P);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		for(final Ability A : props)
		{
			if(!A.okMessage(myHost, msg))
				return false;
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		for(final Ability A : props)
			A.executeMsg(myHost, msg);
	}
}

