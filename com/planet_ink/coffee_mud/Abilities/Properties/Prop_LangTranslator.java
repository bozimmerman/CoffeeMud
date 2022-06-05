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
   Copyright 2004-2022 Bo Zimmerman

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

	protected Map<String, Pair<Integer,List<String>>>	langs	= new Hashtable<String, Pair<Integer,List<String>>>();

	protected final Set<String> trusted		= new HashSet<String>();
	protected Set<String>		ints		= new XHashSet<String>("Common");
	protected boolean			passive		= false;
	protected final String[]	lastLang	= new String[] { "" };

	@Override
	public String accountForYourself()
	{
		return "Translates spoken language";
	}

	@Override
	public boolean isANaturalLanguage()
	{
		return true;
	}

	protected void logError(final String msg)
	{
		final String aname = (affected!=null)?affected.Name():"null";
		final String rname = CMLib.map().getApproximateExtendedRoomID(CMLib.map().roomLocation(affected));
		Log.errOut("Prop_LangTranslator: "+msg+": "+aname+": "+rname);
	}
	
	@Override
	public void setMiscText(final String text)
	{
		super.setMiscText(text);
		final Vector<String> V=CMParms.parse(text);
		langs.clear();
		ints.clear();
		trusted.clear();
		int lastpct=100;
		List<String> words=new ArrayList<String>(1);
		for(int v=0;v<V.size();v++)
		{
			String s=V.elementAt(v);
			if(s.endsWith("%"))
				s=s.substring(0,s.length()-1);
			if(CMath.isNumber(s))
				lastpct=CMath.s_int(s);
			else
			if(s.startsWith("'")||s.startsWith("`"))
			{
				String wds=s.substring(1).trim().toUpperCase();
				if(wds.length()>0)
					words.add(wds);
			}
			else
			if(s.startsWith("#"))
			{
				String nm=s.substring(1).trim().toUpperCase();
				if(nm.length()>0)
					trusted.add(nm);
			}
			else
			if(s.equalsIgnoreCase("notranslate"))
				passive=true;
			else
			{
				final Ability A=CMClass.getAbility(s);
				if(A!=null)
				{
					langs.put(A.ID().toUpperCase(),new Pair<Integer,List<String>>(Integer.valueOf(lastpct),words));
					ints.add(A.ID());
					words=new ArrayList<String>(1);
				}
				else
					logError("Bad parm: '"+s+"'");
			}
		}
	}

	@Override
	public Set<String> languagesSupported()
	{
		return ints;
	}

	protected boolean wordMatch(String words, final List<String> allMatchWords)
	{
		if((allMatchWords == null)||(allMatchWords.size()==0)||(allMatchWords.contains("*")))
			return true;
		if(words==null)
			return false;
		words=words.trim().toUpperCase();
		if(words.length()==0)
			return false;
		words=" "+words+" ";
		for(final String s : allMatchWords)
		{
			if(s.startsWith("*"))
			{
				if(s.endsWith("*"))
				{
					if(words.indexOf(s.substring(1,s.length()-1))>=0)
						return true;
				}
				else
				if(words.indexOf(s.substring(1)+" ")>=0)
					return true;
			}
			else
			if(s.endsWith("*"))
			{
				if(words.indexOf(" "+s.substring(0,s.length()-1))>=0)
					return true;
			}
			else
			if(s.startsWith("^"))
			{
				if(words.startsWith(" "+s.substring(1)))
					return true;
			}
			else
			if(words.indexOf(" "+s+" ")>=0)
				return true;
		}
		return false;
	}
	
	@Override
	public boolean translatesLanguage(final String language, final String words)
	{
		final Pair<Integer,List<String>> p = langs.get(language.toUpperCase());
		if(p==null)
			return false;
		if(wordMatch(words, p.second))
		{
			synchronized(lastLang)
			{
				lastLang[0] = language;
			}
			return true;
		}
		return false;
	}

	@Override
	public int getProficiency(String language)
	{
		if(language.equalsIgnoreCase(ID()))
		{
			synchronized(lastLang)
			{
				language=lastLang[0];
			}
		}
		final Pair<Integer,List<String>> p = langs.get(language.toUpperCase());
		if(p==null)
			return 0;
		return p.first.intValue();
	}

	@Override
	public boolean beingSpoken(final String language)
	{
		return !passive;
	}

	@Override
	public void setBeingSpoken(final String language, final boolean beingSpoken)
	{
	}

	@Override
	public Map<String, String> translationHash(final String language)
	{
		return new Hashtable<String, String>();
	}

	@Override
	public List<String[]> translationLists(final String language)
	{
		return new Vector<String[]>();
	}

	@Override
	public String translate(final String language, final String word)
	{
		return word;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(passive)
		{
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			&&(msg.target()==affected)
			&&(affected instanceof MOB)
			&&((trusted.contains(msg.source().Name().toUpperCase().trim()))
				||(trusted.size()==0))
			&&(msg.sourceMessage()!=null))
			{
				Language langL;
				if(msg.tool() instanceof Language)
					langL=(Language)msg.tool();
				else
				{
					langL=CMLib.utensils().getLanguageSpoken(msg.source());
					if(langL == null)
						langL=(Language)CMClass.getAbility("Common");
				}
				if(langs.containsKey(langL.ID().toUpperCase()))
				{
					final String spokenMsg = CMStrings.getSayFromMessage(msg.sourceMessage());
					final Pair<Integer,List<String>> p = langs.get(langL.ID().toUpperCase());
					if(wordMatch(spokenMsg, p.second))
					{
						final MOB M=(MOB)affected;
						List<String> parsedInput=CMParms.parse(spokenMsg);
						int metaFlags = MUDCmdProcessor.METAFLAG_INORDER;
						if((!M.isPlayer())&&(M.session()!=null))
							metaFlags|=MUDCmdProcessor.METAFLAG_POSSESSED;
						final List<List<String>> MORE_CMDS=CMLib.lang().preCommandParser(parsedInput);
						for(int m=0;m<MORE_CMDS.size();m++)
							((MOB)affected).enqueCommand(MORE_CMDS.get(m),metaFlags,0);
					}
				}
			}
		}
		else
		if((msg.tool() instanceof Ability)
		&&(msg.sourceMinor()!=CMMsg.TYP_TEACH))
		{
			if(text().length()>0)
			{
				final Pair<Integer,List<String>> p = langs.get(msg.tool().ID().toUpperCase());
				if(p==null)
					return;
				if(CMLib.dice().rollPercentage()>p.first.intValue())
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
	
	protected String[] parseParms(final String code,final String val)
	{
		if(code.startsWith("+")||code.startsWith("-"))
		{
			String lang=code.substring(1);
			String parms="";
			if((val != null)&&(val.length()>0))
				parms=val;
			return new String[] {lang, parms};
		}
		return null;
	}
	
	public Pair<Integer,List<String>> parseEquate(final Integer defI, final String arg)
	{
		Integer amt = (defI == null) ? Integer.valueOf(100) : defI;
		List<String> words=new ArrayList<String>();
		for(String s : CMParms.parse(arg))
		{
			if(s.endsWith("%"))
				s=s.substring(0,s.length()-1);
			if(CMath.isNumber(s))
				amt=Integer.valueOf(CMath.s_int(s));
			else
			if(s.startsWith("'")||s.startsWith("`"))
			{
				String wds=s.substring(1).trim().toUpperCase();
				if(wds.length()>0)
					words.add(wds);
			}
		}
		return new Pair<Integer,List<String>>(amt,words);
	}
	
	public void rebuildMiscText()
	{
		StringBuilder str=new StringBuilder("");
		if(passive)
			str.append("NOTRANSLATE ");
		for(final String t : trusted)
			str.append("#"+t).append(" ");
		for(String ID : ints)
		{
			final Pair<Integer,List<String>> p = langs.get(ID.toUpperCase().trim());
			if(p!=null)
			{
				str.append(p.first.intValue()).append(" ");
				for(final String s : p.second)
				{
					if(s.indexOf(" ")>0)
						str.append("\"`").append(s).append("\" ");
					else
						str.append("`").append(s).append(" ");
				}
			}
			str.append(ID).append(" ");
		}
		super.miscText = str.toString().trim();
	}
	
	@Override
	public String getStat(final String code)
	{
		int x=code.indexOf(':');
		if((x>0)
		&&(code.substring(0, x).toUpperCase().equals("EXISTS")))
		{
			final String allParms=code.substring(x+1).trim();
			x=allParms.indexOf(' ');
			final String lang = (x<0)?allParms:allParms.substring(0, x).trim();
			if(lang.startsWith("#"))
				return trusted.contains(lang.toUpperCase().trim())?"true":"false";
			final Pair<Integer,List<String>> p=(x<0)?null:parseEquate(Integer.valueOf(100),allParms.substring(x+1).trim());
			final Pair<Integer,List<String>> dat=langs.get(lang.toUpperCase().trim());
			if(dat == null)
				return "false";
			if(p==null)
				return "true";
			for(final String s : p.second)
			{
				if(!dat.second.contains(s.toUpperCase()))
					return "false";
			}
			return "true";
		}
		else
			return super.getStat(code);
	}
	
	@Override
	public void setStat(final String code, final String val)
	{
		if(code.startsWith("+"))
		{
			String[] args = parseParms(code,val);
			if(args[0].equalsIgnoreCase("PASSIVE")&& (!passive))
			{
				passive=true;
				rebuildMiscText();
			}
			else
			if(args[0].equalsIgnoreCase("TRUSTED"))
			{
				if(!trusted.contains(val.toUpperCase()))
				{
					trusted.add(val.toUpperCase());
					rebuildMiscText();
				}
			}
			else
			{
				final Ability A=CMClass.findAbility(args[0]);
				if(A==null)
					return;
				if(!langs.containsKey(A.ID().toUpperCase()))
				{
					langs.put(A.ID().toUpperCase(), parseEquate(null,args[1]));
					ints.add(A.ID());
					rebuildMiscText();
				}
				else
				{
					List<String> old=langs.get(A.ID().toUpperCase()).second;
					Integer I=langs.get(A.ID().toUpperCase()).first;
					langs.put(A.ID().toUpperCase(), parseEquate(I,args[1]));
					langs.get(A.ID().toUpperCase()).second.addAll(old);
					rebuildMiscText();
				}
			}
		}
		else
		if(code.startsWith("-"))
		{
			String[] args = parseParms(code,val);
			if(args[0].equalsIgnoreCase("PASSIVE")&& (passive))
			{
				passive=false;
				rebuildMiscText();
			}
			else
			if(args[0].equalsIgnoreCase("TRUSTED"))
			{
				if(trusted.contains(val.toUpperCase()))
				{
					trusted.remove(val.toUpperCase());
					rebuildMiscText();
				}
			}
			else
			{
				final Ability A=CMClass.findAbility(args[0]);
				if(A==null)
					return;
				if(args[1].length()==0)
				{
					if(langs.containsKey(A.ID().toUpperCase()))
					{
						langs.remove(A.ID().toUpperCase());
						ints.remove(A.ID());
						rebuildMiscText();
					}
				}
				else
				if(langs.containsKey(A.ID().toUpperCase()))
				{
					final Pair<Integer,List<String>> p = langs.get(A.ID().toUpperCase());
					if((p!=null)&&(p.second.size()>0))
					{
						p.second.removeAll(parseEquate(null,args[1]).second);
						rebuildMiscText();
					}
				}
			}
		}
		else
			super.setStat(code, val);
	}
}
