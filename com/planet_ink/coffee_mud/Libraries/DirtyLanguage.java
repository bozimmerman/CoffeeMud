package com.planet_ink.coffee_mud.Libraries;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.LanguageLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
   Copyright 2006-2025 Bo Zimmerman

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
public class DirtyLanguage extends StdLibrary implements LanguageLibrary
{
	@Override
	public String ID()
	{
		return "DirtyLanguage";
	}

	protected String	language		= "";
	protected String	country			= "";
	protected Locale	currentLocale	= null;

	private enum Command
	{
		REPLACE,
		REPLACEALL,
		REPLACEEXACT,
		REPLACEWHOLE,
		IGNORE,
		IGNOREALL,
		IGNOREWHOLE,
		AUTOIGNORE,
		DEFINE
	}

	@Override
	public void setLocale(final String lang, final String state)
	{
		if((lang!=null)
		&&(state!=null)
		&&(lang.length()>0)
		&&(state.length()>0))
		{
			if((!lang.equals(language))&&(!state.equals(country)))
			{
				country=state;
				language=lang;
				currentLocale = new Locale(language, country);
				clear();
			}
		}
	}

	@Override
	public void propertiesLoaded()
	{
		setLocale(CMLib.props().getStr("LANGUAGE"),CMLib.props().getStr("COUNTRY"));
	}

	public String replaceWithDefinitions(final PairList<String,String> global, final PairList<String,String> local, String str)
	{
		for(int v=0;v<local.size();v++)
			str=CMStrings.replaceAll(str,local.getFirst(v),local.getSecond(v));
		for(int v=0;v<global.size();v++)
			str=CMStrings.replaceAll(str,global.getFirst(v),global.getSecond(v));
		return str;
	}

	protected String filterString(final String str)
	{
		final StringBuffer buf=new StringBuffer(str);
		for(int i=0;i<buf.length();i++)
			switch(buf.charAt(i))
			{
			case '\\':
				buf.insert(i,'\\');
				i++;
				break;
			case '\t':
				buf.setCharAt(i,'t');
				buf.insert(i,'\\');
				i++;
				break;
			case '\r':
				buf.setCharAt(i,'r');
				buf.insert(i,'\\');
				i++;
				break;
			case '\n':
				buf.setCharAt(i,'n');
				buf.insert(i,'\\');
				i++;
				break;
			case '\"':
				buf.insert(i,'\\');
				i++;
				break;
			}
		return buf.toString();
	}

	protected String unFilterString(final String str)
	{
		final StringBuffer buf=new StringBuffer(str);
		for(int i=0;i<buf.length()-1;i++)
		if(buf.charAt(i)=='\\')
			switch(buf.charAt(i+1))
			{
			case '\\':
				buf.deleteCharAt(i);
				break;
			case 't':
				buf.deleteCharAt(i);
				buf.setCharAt(i,'\t');
				break;
			case 'r':
				buf.deleteCharAt(i);
				buf.setCharAt(i,'\r');
				break;
			case 'n':
				buf.deleteCharAt(i);
				buf.setCharAt(i,'\n');
				break;
			case '\"':
				buf.deleteCharAt(i);
				break;
			}
		return buf.toString();
	}

	protected class ParseCmd
	{
		Command cmd;
		Pair<Object,Object> p;
		public ParseCmd(final Command c, final Object o)
		{
			cmd = c;
			p = new Pair<Object,Object>(o,o);
		}
		public ParseCmd(final Command c, final Object o1, final Object o2)
		{
			cmd = c;
			p = new Pair<Object,Object>(o1,o2);
		}
	}

	protected class ParserSection extends Vector<ParseCmd>
	{
		private static final long serialVersionUID = -46886478059227632L;
		public void addElement(final Command c, final Object o)
		{
			super.add(new ParseCmd(c,o));
		}
		public void addElement(final Command c, final Object o1, final Object o2)
		{
			super.add(new ParseCmd(c,o1,o2));
		}
	}

	protected class ParserSections extends Hashtable<String,ParserSection>
	{
		private static final long serialVersionUID = -6971508263354514900L;

		public final Map<String, Integer>		sectionIndexes	= new Hashtable<String, Integer>();
		public final PairList<String, String>	wholeFile		= new PairVector<String, String>();
	}

	protected ParserSections loadFileSections(final String filename)
	{
		//Bo: I know you want to get rid of these
		// DVectors.
		// It does not end well.
		final ParserSections parserSections=new ParserSections();
		final CMFile F=new CMFile(filename,null,CMFile.FLAG_FORCEALLOW);
		if(!F.exists())
		{
			Log.errOut("Language file "+filename+" not found! This mud is in deep doo-doo!");
			return null;
		}
		final StringBuffer alldata=F.text();
		final List<String> V=Resources.getFileLineVector(alldata);
		String s=null;
		ParserSection currentSection=null;
		final PairList<String,String> globalDefinitions=new PairArrayList<String,String>();
		final PairList<String,String> localDefinitions=new PairArrayList<String,String>();
		Map<String,String> currentSectionReplaceStrs=new Hashtable<String,String>();
		final Map<String,String> currentSectionReplaceExactStrs=new Hashtable<String,String>();
		Set<String> currentSectionIgnoreStrs=new HashSet<String>();
		// especially these below
		final Map<String, Integer> sectionIndexes=new Hashtable<String, Integer>();
		final PairList<String, String> wholeFile=new PairVector<String, String>();
		for(int v=0;v<V.size();v++)
		{
			wholeFile.add(filename,V.get(v));
			s=V.get(v).trim();
			if((s.startsWith("#"))||(s.trim().length()==0))
				continue;
			if(s.startsWith("["))
			{
				final int x=s.lastIndexOf(']');
				if(currentSection != null)
				{
					if(currentSectionReplaceStrs.size()>0)
						currentSection.addElement(Command.REPLACEWHOLE,currentSectionReplaceStrs);
					if(currentSectionReplaceExactStrs.size()>0)
						currentSection.addElement(Command.REPLACEEXACT,currentSectionReplaceExactStrs);
					if(currentSectionIgnoreStrs.size()>0)
						currentSection.addElement(Command.IGNOREWHOLE,currentSectionIgnoreStrs);
				}
				currentSection=new ParserSection();
				currentSectionReplaceStrs=new HashMap<String,String>();
				currentSectionIgnoreStrs=new HashSet<String>();
				String sectionName = s.substring(1,x);
				final int subDex = sectionName.indexOf(':');
				if(subDex > 0)
					sectionName = sectionName.substring(0,subDex).toUpperCase()+':'+sectionName.substring(subDex+1);
				else
					sectionName = sectionName.toUpperCase();
				parserSections.put(sectionName,currentSection);
				sectionIndexes.put(sectionName,Integer.valueOf(v));
				localDefinitions.clear();
				continue;
			}
			final int firstSpace = s.indexOf(' ');
			final String supp = (firstSpace>0)?s.substring(0,firstSpace).toUpperCase():s.toUpperCase();
			final Command C = (Command)CMath.s_valueOf(Command.class, supp);
			if(C == null)
			{
				Log.errOut("Scripts","Unknown parser command "+supp+", line "+v);
				continue;
			}
			switch(C)
			{
			case AUTOIGNORE:
			{
				final int x=s.indexOf(' ');
				if(x<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				final Integer I=Integer.valueOf(CMath.s_int(s.substring(x+1).trim()));
				if(currentSection!=null)
					currentSection.addElement(Command.AUTOIGNORE,I,s.substring(x+1).trim());
				break;
			}
			case DEFINE:
			{
				int regstart=s.indexOf('"');
				if(regstart<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				int regend=s.indexOf('"',regstart+1);
				while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
					regend=s.indexOf('"',regend+1);
				if(regend<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				final String variable=s.substring(regstart+1,regend).toUpperCase();
				s=s.substring(regend+1).trim();
				if(!s.toUpperCase().startsWith("AS"))
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				regstart=s.indexOf('"');
				if(regstart<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				regend=s.indexOf('"',regstart+1);
				while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
					regend=s.indexOf('"',regend+1);
				if(regend<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				String replacement=s.substring(regstart+1,regend);
				replacement=replaceWithDefinitions(globalDefinitions,localDefinitions,replacement);
				if(currentSection!=null)
				{
					localDefinitions.removeFirst(variable);
					localDefinitions.add(variable,replacement);
				}
				else
				{
					globalDefinitions.removeFirst(variable);
					globalDefinitions.add(variable,replacement);
				}
				break;
			}
			case IGNOREWHOLE:
			{
				final int regstart=s.indexOf('"');
				if(regstart<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				int regend=s.indexOf('"',regstart+1);
				while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
					regend=s.indexOf('"',regend+1);
				if(regend<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				final String expression=unFilterString(s.substring(regstart+1,regend));
				currentSectionIgnoreStrs.add(expression.toLowerCase());
				break;
			}
			case REPLACEWHOLE:
			case REPLACEEXACT:
			{
				int regstart=s.indexOf('"');
				if(regstart<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				int regend=s.indexOf('"',regstart+1);
				while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
					regend=s.indexOf('"',regend+1);
				if(regend<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				final String expression=unFilterString(s.substring(regstart+1,regend));
				s=s.substring(regend+1).trim();
				if(!s.toUpperCase().startsWith("WITH"))
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				regstart=s.indexOf('"');
				if(regstart<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				regend=s.indexOf('"',regstart+1);
				while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
					regend=s.indexOf('"',regend+1);
				if(regend<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				final String replacement=unFilterString(s.substring(regstart+1,regend));
				if(C == Command.REPLACEWHOLE)
					currentSectionReplaceStrs.put(expression.toLowerCase(),replacement);
				else
					currentSectionReplaceExactStrs.put(expression,replacement);
				break;
			}
			case REPLACEALL:
			{
				int regstart=s.indexOf('"');
				if(regstart<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				int regend=s.indexOf('"',regstart+1);
				while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
					regend=s.indexOf('"',regend+1);
				if(regend<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				final String expression=unFilterString(s.substring(regstart+1,regend));
				s=s.substring(regend+1).trim();
				if(!s.toUpperCase().startsWith("WITH"))
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				regstart=s.indexOf('"');
				if(regstart<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				regend=s.indexOf('"',regstart+1);
				while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
					regend=s.indexOf('"',regend+1);
				if(regend<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				final String replacement=unFilterString(s.substring(regstart+1,regend));
				if(currentSection!=null)
					currentSection.addElement(Command.REPLACEALL,expression.toLowerCase(),replacement);
				currentSectionReplaceStrs.put(expression.toLowerCase(),replacement);
				break;
			}
			case REPLACE:
			case IGNORE:
			{
				int regstart=s.indexOf('"');
				if(regstart<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				int regend=s.indexOf('"',regstart+1);
				while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
					regend=s.indexOf('"',regend+1);
				if(regend<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				String expression=s.substring(regstart+1,regend);
				expression=replaceWithDefinitions(globalDefinitions,localDefinitions,expression);
				s=s.substring(regend+1).trim();
				String replacement=null;
				if(C == Command.REPLACE)
				{
					if(!s.toUpperCase().startsWith("WITH"))
					{
						Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
						break;
					}
					regstart=s.indexOf('"');
					if(regstart<0)
					{
						Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
						break;
					}
					regend=s.indexOf('"',regstart+1);
					while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
						regend=s.indexOf('"',regend+1);
					if(regend<0)
					{
						Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
						break;
					}
					replacement=s.substring(regstart+1,regend);
					replacement=replaceWithDefinitions(globalDefinitions,localDefinitions,replacement);
				}
				try
				{
					final Pattern expPattern=Pattern.compile(expression, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
					if(currentSection!=null)
						currentSection.addElement(C,expPattern,replacement);
				}
				catch(final Exception e)
				{
					Log.errOut("Scripts",e);
				}
				break;
			}
			case IGNOREALL:
				final int regstart=s.indexOf('"');
				if(regstart<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				int regend=s.indexOf('"',regstart+1);
				while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
					regend=s.indexOf('"',regend+1);
				if(regend<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					break;
				}
				final String expression=unFilterString(s.substring(regstart+1,regend));
				if(currentSection!=null)
					currentSection.addElement(C,expression,expression);
				break;
			}
		}
		if(currentSection != null)
		{
			if(currentSectionReplaceStrs.size()>0)
				currentSection.addElement(Command.REPLACEWHOLE,currentSectionReplaceStrs,currentSectionReplaceStrs);
			if(currentSectionReplaceExactStrs.size()>0)
				currentSection.addElement(Command.REPLACEEXACT,currentSectionReplaceExactStrs,currentSectionReplaceExactStrs);
			if(currentSectionIgnoreStrs.size()>0)
				currentSection.addElement(Command.IGNOREWHOLE,currentSectionIgnoreStrs,currentSectionIgnoreStrs);
		}
		parserSections.sectionIndexes.putAll(sectionIndexes);
		parserSections.wholeFile.addAll(wholeFile);
		return parserSections;
	}

	protected final String getLanguageTranslatorKey()
	{
		return "TRANSLATION_"+language.toUpperCase()+"_"+country.toUpperCase();
	}

	protected final String getLanguageParserKey()
	{
		return "PARSER_"+language.toUpperCase()+"_"+country.toUpperCase();
	}

	protected ParserSections getLanguageParser()
	{
		final String parserKey=getLanguageParserKey();
		ParserSections parserSections=(ParserSections)Resources.getResource(parserKey);
		if(parserSections==null)
		{
			parserSections=loadFileSections("resources/parser_"+language.toUpperCase()+"_"+country.toUpperCase()+".properties");
			if(parserSections == null)
				parserSections=new ParserSections();
			Resources.submitResource(parserKey,parserSections);
		}
		return parserSections;
	}

	protected ParserSections getLanguageTranslator()
	{
		final String translatorKey=getLanguageTranslatorKey();
		ParserSections translationSections=(ParserSections)Resources.getResource(translatorKey);
		if(translationSections==null)
		{
			translationSections=loadFileSections("resources/translation_"+language.toUpperCase()+"_"+country.toUpperCase()+".properties");
			if(translationSections == null)
				translationSections=new ParserSections();
			Resources.submitResource(translatorKey,translationSections);
		}
		return translationSections;
	}

	@Override
	public void clear()
	{
		final String translatorKey=getLanguageTranslatorKey();
		final String parserKey=getLanguageParserKey();
		Resources.removeResource(translatorKey);
		Resources.removeResource(parserKey);
	}

	public boolean insertExpansion(final List<String> MORE_CMDS, final String str, final int m, final int strLen, boolean nothingDone)
	{
		final List<String> expansion=CMParms.parseAny(CMStrings.replaceAll(str,"\\t","\t"),"\\n",false);
		MORE_CMDS.set(m,expansion.get(0));
		String expStr=expansion.get(0);
		if(expStr.length()<=strLen)
			nothingDone=false;
		final boolean insert=m<MORE_CMDS.size()-1;
		for(int e=1;e<expansion.size();e++)
		{
			expStr=expansion.get(e);
			if(expStr.length()<=strLen)
				nothingDone=false;
			if(insert)
				MORE_CMDS.add(m+e,expStr);
			else
				MORE_CMDS.add(expStr);
		}
		return nothingDone;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<List<String>> preCommandParser(final List<String> commands)
	{
		final List<String> workCmdList=new XVector<String>(commands);
		StringBuilder s;
		String cs;
		for(int commandIndex=0;commandIndex<workCmdList.size();commandIndex++)
		{
			cs=workCmdList.get(commandIndex);
			if((cs.indexOf('\\')<0)&&(cs.indexOf('\t')<0))
				continue;
			s = new StringBuilder(workCmdList.get(commandIndex).toString());
			for(int i=0;i<s.length();i++)
			{
				if((s.charAt(i)=='\\') // why? turns \n into \\n, which turns into \<cr>, which is ugly
				||(s.charAt(i)=='\t'))
				{
					s.insert(i, '\\');
					i++;
				}
			}
			workCmdList.set(commandIndex, s.toString());
		}
		final String combinedWithTabs=CMParms.combineWithTabs(workCmdList,0);
		workCmdList.clear();
		workCmdList.add(combinedWithTabs);
		final ParserSection parser=getLanguageParser().get("COMMAND-PRE-PROCESSOR");
		if((parser==null)||(commands==null))
			return new XVector<List<String>>(commands);
		Pattern pattern=null;
		Matcher matcher=null;
		Command I=null;
		String str=null;
		String rep=null;
		String wit=null;
		int strLen=-1;
		int autoIgnoreLen=0;
		Set<String> ignoreSet=null;
		for(int p=0;p<parser.size();p++)
		{
			final ParseCmd P = parser.get(p);
			I=P.cmd;
			if(I!=null)
			switch(I)
			{
			case DEFINE:
				break;
			case REPLACE:
			{
				pattern=(Pattern)P.p.first;
				boolean nothingDone=false;
				while(!nothingDone)
				{
					nothingDone=true;
					matcher=pattern.matcher(combinedWithTabs);
					for(int m=0;m<workCmdList.size();m++)
					{
						str=workCmdList.get(m);
						strLen=str.length();
						matcher=pattern.matcher(str);
						if(matcher.find())
						{
							str=(String)P.p.second;
							for(int i=0;i<=matcher.groupCount();i++)
								str=CMStrings.replaceAll(str,"\\"+i,matcher.group(i));
							if(!workCmdList.get(m).equals(str))
							{
								nothingDone=insertExpansion(workCmdList,str,m,strLen,nothingDone);
							}
						 }
					}
				}
				break;
			}
			case REPLACEWHOLE:
			{
				rep=((Map<String,String>)P.p.first).get(combinedWithTabs.toLowerCase());
				if(rep!=null)
				{
					insertExpansion(workCmdList,rep,0,combinedWithTabs.length(),true);
					p=parser.size();
				}
				break;
			}
			case REPLACEEXACT:
			{
				rep=((Map<String,String>)P.p.first).get(combinedWithTabs);
				if(rep!=null)
				{
					insertExpansion(workCmdList,rep,0,combinedWithTabs.length(),true);
					p=parser.size();
				}
				break;
			}
			case REPLACEALL:
			{
				rep=(String)P.p.first;
				if(rep.length()==0)
					break;
				for(int m=0;m<workCmdList.size();m++)
				{
					str=workCmdList.get(m);
					strLen=str.length();
					int x=str.toLowerCase().indexOf(rep);
					if(x>=0)
					{
						while(x>=0)
						{
							wit=(String)P.p.second;
							str=str.substring(0,x)+wit+str.substring(x+rep.length());
							x=str.toLowerCase().indexOf(rep,x+wit.length());
						}
						insertExpansion(workCmdList,str,m,strLen,true);
					}
				}
				break;
			}
			case IGNORE:
			{
				pattern=(Pattern)P.p.first;
				matcher=pattern.matcher(combinedWithTabs);
				if(matcher.find())
					return new XVector<List<String>>();
				break;
			}
			case IGNOREALL:
			{
				if(combinedWithTabs.toLowerCase().indexOf((String)P.p.first)>=0)
					return new XVector<List<String>>();
				break;
			}
			case IGNOREWHOLE:
			{
				ignoreSet=(Set<String>)P.p.first;
				if(ignoreSet.contains(combinedWithTabs.toLowerCase()))
					return new XVector<List<String>>();
				break;
			}
			case AUTOIGNORE:
				autoIgnoreLen=((Integer)P.p.first).intValue();
				if(autoIgnoreLen==0)
					autoIgnoreLen=100;
				break;
			}
		}
		if((workCmdList.size()==1)
		&&(workCmdList.get(0).equals(combinedWithTabs)))
		{
			if((autoIgnoreLen>0)
			&&(str!=null)
			&&(str.length()<=autoIgnoreLen))
			{
				if(ignoreSet==null)
				{
					ignoreSet=new HashSet<String>();
					parser.addElement(Command.IGNOREWHOLE,ignoreSet);
				}
				ignoreSet.add(combinedWithTabs.toLowerCase());
				final PairList<String,String> fileData=getLanguageParser().wholeFile;
				final Map<String,Integer> fileIndexes=getLanguageParser().sectionIndexes;
				addAutoIgnoredString(combinedWithTabs,fileData,fileIndexes,"COMMAND-PRE-PROCESSOR");
			}
		}
		final List<List<String>> finalCmdList=new Vector<List<String>>();
		for(int m=0;m<workCmdList.size();m++)
			finalCmdList.add(CMParms.parseTabs(workCmdList.get(m),false));
		return finalCmdList;
	}

	@SuppressWarnings("unchecked")
	protected String basicParser(String str, final String section, final boolean nullIfLonger, final boolean isParser)
	{
		if(str==null)
			return null;
		final ParserSection parser=isParser?getLanguageParser().get(section):getLanguageTranslator().get(section);
		if(parser==null)
			return null;
		Pattern pattern=null;
		Matcher matcher=null;
		final String oldStr=str;
		int autoIgnoreLen=0;
		Command I=null;
		Set<String> ignoreSet=null;
		String rep=null;
		String wit=null;
		for(int p=0;p<parser.size();p++)
		{
			final ParseCmd P = parser.get(p);
			I=P.cmd;
			if(I!=null)
			switch(I)
			{
			case DEFINE:
				break;
			case REPLACE:
			{
				pattern=(Pattern)P.p.first;
				matcher=pattern.matcher(str);
				if(matcher.find())
				{
					str = (String)P.p.second;
					for(int i=0;i<=matcher.groupCount();i++)
						str=CMStrings.replaceAll(str,"\\"+i,matcher.group(i));
				}
				break;
			}
			case REPLACEWHOLE:
			{
				rep=((Map<String,String>)P.p.first).get(str.toLowerCase());
				if(rep!=null)
					return rep;
				break;
			}
			case REPLACEEXACT:
			{
				rep=((Map<String,String>)P.p.first).get(str);
				if(rep!=null)
					return rep;
				break;
			}
			case IGNOREALL:
			{
				rep=(String)P.p.first;
				if(rep.length()==0)
					break;
				final int x=str.toLowerCase().indexOf(rep);
				if(x>=0)
					return null;
				break;
			}
			case REPLACEALL:
			{
				rep=(String)P.p.first;
				if(rep.length()==0)
					break;
				int x=str.toLowerCase().indexOf(rep);
				while(x>=0)
				{
					wit=(String)P.p.second;
					str=str.substring(0,x)+wit+str.substring(x+rep.length());
					x=str.toLowerCase().indexOf(rep,x+wit.length());
				}
				break;
			}
			case IGNORE:
			{
				pattern=(Pattern)P.p.first;
				matcher=pattern.matcher(str);
				if(matcher.find())
					return null;
				break;
			}
			case IGNOREWHOLE:
			{
				ignoreSet=(Set<String>)P.p.first;
				if(ignoreSet.contains(str.toLowerCase()))
					return null;
				break;
			}
			case AUTOIGNORE:
				autoIgnoreLen=((Integer)P.p.first).intValue();
				if(autoIgnoreLen==0)
					autoIgnoreLen=100;
				break;
			}
		}
		if(str.equals(oldStr))
		{
			if((autoIgnoreLen>0)&&(str.length()<=autoIgnoreLen))
			{
				if(ignoreSet==null)
				{
					ignoreSet=new HashSet<String>();
					parser.addElement(Command.IGNOREWHOLE,ignoreSet);
				}
				ignoreSet.add(oldStr.toLowerCase());
				PairList<String,String> fileData=null;
				Map<String,Integer> fileIndexes=null;
				if(isParser)
				{
					fileData=getLanguageParser().wholeFile;
					fileIndexes=getLanguageParser().sectionIndexes;
				}
				else
				{
					fileData=getLanguageTranslator().wholeFile;
					fileIndexes=getLanguageTranslator().sectionIndexes;
				}
				addAutoIgnoredString(oldStr,fileData,fileIndexes,section);
			}
			return null;
		}
		else
		if(!nullIfLonger)
			return str;
		return str.length()>=oldStr.length()?null:str;
	}

	public void addAutoIgnoredString(String str, final PairList<String,String> fileData,
												 final Map<String,Integer> fileIndexes, final String sectionName)
	{
		if((fileData==null)||(str==null)||(fileData.size()<1))
			return;
		final String filename=fileData.get(0).first;
		if(fileIndexes==null)
			return;
		final String sectionNameU = sectionName.toUpperCase().trim();
		if(!fileIndexes.containsKey(sectionNameU))
			return;
		final int index=fileIndexes.get(sectionName).intValue();
		for(final String key : fileIndexes.keySet())
		{
			final Integer I = fileIndexes.get(key);
			if(I.intValue()>index)
				fileIndexes.put(key,Integer.valueOf(I.intValue()+1));
		}
		str=filterString(str);
		final String newStr="IGNOREWHOLE \""+str+"\"";
		if(index==fileData.size()-1)
			fileData.add(filename,newStr);
		else
			fileData.add(index+1,new Pair<String,String>(filename,newStr));
		final StringBuffer buf=new StringBuffer("");
		for(int f=0;f<fileData.size();f++)
			buf.append(fileData.get(f).second+"\n\r");
		final CMFile F=new CMFile(filename,null,CMFile.FLAG_FORCEALLOW);
		if((F.exists())&&(F.canWrite()))
			F.saveText(buf);
	}

	@Override
	public String preItemParser(final String item)
	{
		return basicParser(item,"ITEM-PRE-PROCESSOR",true,true);
	}

	@Override
	public String failedItemParser(final String item)
	{
		return basicParser(item,"ITEM-FAIL-PROCESSOR",true,true);
	}

	@Override
	public String rawInputParser(final String words)
	{
		final String parsed = basicParser(words,"RAW-INPUT-PROCESSOR",true,true);
		return (parsed==null)?words:parsed;
	}

	@Override
	public String filterTranslation(final String item)
	{
		return basicParser(item,"FILTER-TRANSLATION",false,false);
	}

	@Override
	public String sessionTranslation(Class<?> clazz, final String item)
	{
		while(clazz != null)
		{
			final String resp = basicParser(item,"SESSION-TRANSLATION:"+clazz.getCanonicalName(),false,false);
			if(resp != null)
				return resp;
			clazz = clazz.getSuperclass();
		}
		return basicParser(item,"SESSION-TRANSLATION",false,false);
	}

	@Override
	public String finalTranslation(final String item)
	{
		return basicParser(item,"FINAL-TRANSLATION",false,false);
	}

	@Override
	public String fullSessionTranslation(final Class<?> clazz, final String str, final String ... xs)
	{
		if((str==null)||(str.length()==0))
			return str;
		final String sessionStr=sessionTranslation(clazz, str);
		return CMStrings.replaceVariables((sessionStr==null)?str:sessionStr, xs);
	}

	protected String[] sessionTranslations(final Class<?> clazz, final String[] items)
	{
		if((items==null)||(items.length==0))
			return items;
		for(int i=0;i<items.length;i++)
		{
			final String s=items[i];
			if(s!=null)
			{
				final String sessionStr=sessionTranslation(clazz, s);
				if(sessionStr!=null)
					items[i]=sessionStr;
			}
		}
		return items;
	}

	@Override
	public String commandWordTranslation(final String str)
	{
		final String commandStr=basicParser(str,"COMMAND-WORD-PROCESSOR",false,true);
		return (commandStr==null)?str:commandStr;
	}

	@Override
	public String L(final String str, final String ... xs)
	{
		if((str==null)||(str.length()==0))
			return str;
		final String sessionStr=sessionTranslation(FINDER.getCaller(), str);
		if(xs.length==0)
			return (sessionStr==null)?str:sessionStr;
		return CMStrings.replaceVariables((sessionStr==null)?str:sessionStr, xs);
	}


	@Override
	public String L(final Class<?> clazz, final String str, final String ... xs)
	{
		if((str==null)||(str.length()==0))
			return str;
		final String sessionStr=sessionTranslation(clazz, str);
		if(xs.length==0)
			return (sessionStr==null)?str:sessionStr;
		return CMStrings.replaceVariables((sessionStr==null)?str:sessionStr, xs);
	}
}
