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
   Copyright 2004-2018 Bo Zimmerman

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

public class Prop_LangTranslator extends Property implements Language
{
	@Override
	public String ID()
	{
		return "Prop_LangTranslator";
	}

	@Override
	public String name()
	{
		return "Language Translator";
	}

	@Override
	public String writtenName()
	{
		return "Language Translator";
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS | CAN_ITEMS | CAN_ROOMS;
	}

	protected PairVector<String,Integer> langs=new PairVector<String,Integer>();

	@Override
	public String accountForYourself()
	{
		return "Translates spoken language";
	}

	@Override
	public void setMiscText(String text)
	{
		super.setMiscText(text);
		final Vector<String> V=CMParms.parse(text);
		langs.clear();
		int lastpct=100;
		for(int v=0;v<V.size();v++)
		{
			String s=V.elementAt(v);
			if(s.endsWith("%"))
				s=s.substring(0,s.length()-1);
			if(CMath.isNumber(s))
				lastpct=CMath.s_int(s);
			else
			{
				final Ability A=CMClass.getAbility(s);
				if(A!=null)
					langs.addElement(A.ID(),Integer.valueOf(lastpct));
			}
		}
	}

	@Override
	public List<String> languagesSupported()
	{
		return Arrays.asList(langs.toArrayFirst(new String[0]));
	}

	@Override
	public boolean translatesLanguage(String language)
	{
		for(int i=0;i<langs.size();i++)
		{
			try
			{
				Pair<String,Integer> p = langs.get(i);
				if(p.first.equalsIgnoreCase(language))
					return true;
			}
			catch(Exception e)
			{
				return false;
			}
		}
		return false;
	}

	@Override
	public int getProficiency(String language)
	{
		for(int i=0;i<langs.size();i++)
		{
			if(langs.get(i).first.equalsIgnoreCase(language))
				return langs.get(i).second.intValue();
		}
		return 0;
	}

	@Override
	public boolean beingSpoken(String language)
	{
		return true;
	}

	@Override
	public void setBeingSpoken(String language, boolean beingSpoken)
	{
	}

	@Override
	public Map<String, String> translationHash(String language)
	{
		return new Hashtable<String, String>();
	}

	@Override
	public List<String[]> translationVector(String language)
	{
		return new Vector<String[]>();
	}

	@Override
	public String translate(String language, String word)
	{
		return word;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(msg.tool() instanceof Ability)
		{
			if(text().length()>0)
			{
				final int t=langs.indexOfFirst(msg.tool().ID());
				if(t<0)
					return;
				final Integer I=langs.get(t).second;
				if(CMLib.dice().rollPercentage()>I.intValue())
					return;
			}
			if((msg.tool().ID().equals("Fighter_SmokeSignals"))
			&&(msg.sourceMinor()==CMMsg.NO_EFFECT)
			&&(msg.targetMinor()==CMMsg.NO_EFFECT)
			&&(msg.othersMessage()!=null))
				CMLib.commands().postSay(msg.source(),null,L("The smoke signals seem to say '@x1'.",msg.othersMessage()),false,false);
			else
			if(((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
			   ||(msg.sourceMinor()==CMMsg.TYP_ORDER)
			   ||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL)))
			&&(msg.sourceMessage()!=null)
			&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE))
			{
				final String str=CMStrings.getSayFromMessage(msg.sourceMessage());
				if(str!=null)
				{
					Environmental target=null;
					final String sourceName = affected.name();
					if(msg.target() instanceof MOB)
						target=msg.target();
					if(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL))
						msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_NOISE|CMMsg.MASK_ALWAYS,L("@x1 say(s) '@x2 said \"@x3\" in @x4'",sourceName,msg.source().name(),str,msg.tool().name())));
					else
					if((target==null)&&(msg.targetMessage()!=null))
						msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_NOISE|CMMsg.MASK_ALWAYS,L("@x1 say(s) '@x2 said \"@x3\" in @x4'",sourceName,msg.source().name(),str,msg.tool().name())));
					else
					if(msg.othersMessage()!=null)
						msg.addTrailerMsg(CMClass.getMsg(msg.source(),target,null,CMMsg.MSG_NOISE|CMMsg.MASK_ALWAYS,L("@x1 say(s) '@x2 said \"@x3\" in @x4'",sourceName,msg.source().name(),str,msg.tool().name())));
				}
			}
		}
	}
}
