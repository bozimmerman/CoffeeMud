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
   Copyright 2006-2018 Bo Zimmerman

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

	protected String language="en";
	protected String country="TX";
	protected Locale currentLocale=null;
	
	private enum Command
	{
		REPLACE,
		REPLACEWHOLE,
		IGNORE,
		IGNOREWHOLE,
		AUTOIGNORE,
		DEFINE,
		REPLACEALL,
		REPLACEEXACT
	}

	@Override
	public void setLocale(String lang, String state)
	{
		if((lang!=null)&&(state!=null)&&(lang.length()>0)&&(state.length()>0))
		{
			country=state;
			language=lang;
		}
		currentLocale = new Locale(language, country);
		clear();
	}

	@Override
	public void propertiesLoaded()
	{
		setLocale(CMLib.props().getStr("LANGUAGE"),CMLib.props().getStr("COUNTRY"));
	}

	public String replaceWithDefinitions(DVector global, DVector local, String str)
	{
		for(int v=0;v<local.size();v++)
			str=CMStrings.replaceAll(str,(String)local.elementAt(v,1),(String)local.elementAt(v,2));
		for(int v=0;v<global.size();v++)
			str=CMStrings.replaceAll(str,(String)global.elementAt(v,1),(String)global.elementAt(v,2));
		return str;
	}

	protected String filterString(String str)
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

	protected String unFilterString(String str)
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

	protected Hashtable<String,DVector> loadFileSections(String filename)
	{
		final Hashtable<String,DVector> parserSections=new Hashtable<String,DVector>();
		final CMFile F=new CMFile(filename,null,CMFile.FLAG_FORCEALLOW);
		if(!F.exists())
		{
			Log.errOut("Language file "+filename+" not found! This mud is in deep doo-doo!");
			return null;
		}
		final StringBuffer alldata=F.text();
		final List<String> V=Resources.getFileLineVector(alldata);
		String s=null;
		DVector currentSection=null;
		final DVector globalDefinitions=new DVector(2);
		final DVector localDefinitions=new DVector(2);
		Hashtable<String,String> currentSectionReplaceStrs=new Hashtable<String,String>();
		Hashtable<String,String> currentSectionReplaceExactStrs=new Hashtable<String,String>();
		HashSet<String> currentSectionIgnoreStrs=new HashSet<String>();
		final DVector sectionIndexes=new DVector(2);
		final DVector wholeFile=new DVector(2);
		for(int v=0;v<V.size();v++)
		{
			wholeFile.addElement(filename,V.get(v));
			s=V.get(v).trim();
			if((s.startsWith("#"))||(s.trim().length()==0))
				continue;
			if(s.startsWith("["))
			{
				final int x=s.lastIndexOf(']');
				if((currentSectionReplaceStrs.size()>0)
				&&(currentSection!=null))
					currentSection.addElement("REPLACEWHOLE",currentSectionReplaceStrs,currentSectionReplaceStrs);
				if((currentSectionReplaceExactStrs.size()>0)
				&&(currentSection!=null))
					currentSection.addElement("REPLACEEXACT",currentSectionReplaceExactStrs,currentSectionReplaceExactStrs);
				if((currentSectionIgnoreStrs.size()>0)
				&&(currentSection!=null))
					currentSection.addElement("IGNOREWHOLE",currentSectionIgnoreStrs,currentSectionIgnoreStrs);
				currentSection=new DVector(3);
				currentSectionReplaceStrs=new Hashtable<String,String>();
				currentSectionIgnoreStrs=new HashSet<String>();
				parserSections.put(s.substring(1,x).toUpperCase(),currentSection);
				sectionIndexes.addElement(s.substring(1,x).toUpperCase(),Integer.valueOf(v));
				localDefinitions.clear();
			}
			else
			if(s.toUpperCase().startsWith("AUTOIGNORE"))
			{
				final int x=s.indexOf(' ');
				if(x<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					continue;
				}
				final Integer I=Integer.valueOf(CMath.s_int(s.substring(x+1).trim()));
				if(currentSection!=null)
					currentSection.addElement("AUTOIGNORE",I,s.substring(x+1).trim());
			}
			else
			if(s.toUpperCase().startsWith("DEFINE"))
			{
				int regstart=s.indexOf('"');
				if(regstart<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					continue;
				}
				int regend=s.indexOf('"',regstart+1);
				while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
					regend=s.indexOf('"',regend+1);
				if(regend<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					continue;
				}
				final String variable=s.substring(regstart+1,regend).toUpperCase();
				s=s.substring(regend+1).trim();
				if(!s.toUpperCase().startsWith("AS"))
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					continue;
				}
				regstart=s.indexOf('"');
				if(regstart<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					continue;
				}
				regend=s.indexOf('"',regstart+1);
				while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
					regend=s.indexOf('"',regend+1);
				if(regend<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					continue;
				}
				String replacement=s.substring(regstart+1,regend);
				replacement=replaceWithDefinitions(globalDefinitions,localDefinitions,replacement);
				if(currentSection!=null)
				{
					localDefinitions.removeElement(variable);
					localDefinitions.addElement(variable,replacement);
				}
				else
				{
					globalDefinitions.removeElement(variable);
					globalDefinitions.addElement(variable,replacement);
				}
			}
			else
			if(s.toUpperCase().startsWith("IGNOREWHOLE"))
			{
				final int regstart=s.indexOf('"');
				if(regstart<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					continue;
				}
				int regend=s.indexOf('"',regstart+1);
				while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
					regend=s.indexOf('"',regend+1);
				if(regend<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					continue;
				}
				final String expression=unFilterString(s.substring(regstart+1,regend));
				currentSectionIgnoreStrs.add(expression.toLowerCase());
			}
			else
			if(s.toUpperCase().startsWith("REPLACEWHOLE")||s.toUpperCase().startsWith("REPLACEEXACT"))
			{
				int regstart=s.indexOf('"');
				if(regstart<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					continue;
				}
				int regend=s.indexOf('"',regstart+1);
				while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
					regend=s.indexOf('"',regend+1);
				if(regend<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					continue;
				}
				String expression=unFilterString(s.substring(regstart+1,regend));
				s=s.substring(regend+1).trim();
				if(!s.toUpperCase().startsWith("WITH"))
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					continue;
				}
				regstart=s.indexOf('"');
				if(regstart<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					continue;
				}
				regend=s.indexOf('"',regstart+1);
				while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
					regend=s.indexOf('"',regend+1);
				if(regend<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					continue;
				}
				final String replacement=unFilterString(s.substring(regstart+1,regend));
				if(s.toUpperCase().startsWith("REPLACEWHOLE"))
					currentSectionReplaceStrs.put(expression.toLowerCase(),replacement);
				else
					currentSectionReplaceExactStrs.put(expression,replacement);
			}
			else
			if(s.toUpperCase().startsWith("REPLACEALL"))
			{
				final String cmd="REPLACEALL";
				int regstart=s.indexOf('"');
				if(regstart<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					continue;
				}
				int regend=s.indexOf('"',regstart+1);
				while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
					regend=s.indexOf('"',regend+1);
				if(regend<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					continue;
				}
				final String expression=unFilterString(s.substring(regstart+1,regend));
				s=s.substring(regend+1).trim();
				if(!s.toUpperCase().startsWith("WITH"))
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					continue;
				}
				regstart=s.indexOf('"');
				if(regstart<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					continue;
				}
				regend=s.indexOf('"',regstart+1);
				while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
					regend=s.indexOf('"',regend+1);
				if(regend<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					continue;
				}
				final String replacement=unFilterString(s.substring(regstart+1,regend));
				if(currentSection!=null)
					currentSection.addElement(cmd,expression.toLowerCase(),replacement);
				currentSectionReplaceStrs.put(expression.toLowerCase(),replacement);
			}
			else
			if(s.toUpperCase().startsWith("REPLACE")||s.toUpperCase().startsWith("IGNORE"))
			{
				final String cmd=s.toUpperCase().startsWith("REPLACE")?"REPLACE":"IGNORE";
				int regstart=s.indexOf('"');
				if(regstart<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					continue;
				}
				int regend=s.indexOf('"',regstart+1);
				while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
					regend=s.indexOf('"',regend+1);
				if(regend<0)
				{
					Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
					continue;
				}
				String expression=s.substring(regstart+1,regend);
				expression=replaceWithDefinitions(globalDefinitions,localDefinitions,expression);
				s=s.substring(regend+1).trim();
				String replacement=null;
				if(cmd.equals("REPLACE"))
				{
					if(!s.toUpperCase().startsWith("WITH"))
					{
						Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
						continue;
					}
					regstart=s.indexOf('"');
					if(regstart<0)
					{
						Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
						continue;
					}
					regend=s.indexOf('"',regstart+1);
					while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
						regend=s.indexOf('"',regend+1);
					if(regend<0)
					{
						Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1));
						continue;
					}
					replacement=s.substring(regstart+1,regend);
					replacement=replaceWithDefinitions(globalDefinitions,localDefinitions,replacement);
				}
				try
				{
					final Pattern expPattern=Pattern.compile(expression, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
					if(currentSection!=null)
						currentSection.addElement(cmd,expPattern,replacement);
				}
				catch(final Exception e)
				{
					Log.errOut("Scripts",e);
				}
			}
			else
				Log.errOut("Scripts","Unknown parser command, line "+v);
		}
		if((currentSectionReplaceStrs.size()>0)
		&&(currentSection!=null))
			currentSection.addElement("REPLACEWHOLE",currentSectionReplaceStrs,currentSectionReplaceStrs);
		if((currentSectionReplaceExactStrs.size()>0)
		&&(currentSection!=null))
			currentSection.addElement("REPLACEEXACT",currentSectionReplaceExactStrs,currentSectionReplaceExactStrs);
		if((currentSectionIgnoreStrs.size()>0)
		&&(currentSection!=null))
			currentSection.addElement("IGNOREWHOLE",currentSectionIgnoreStrs,currentSectionIgnoreStrs);
		parserSections.put("INDEXES",sectionIndexes);
		parserSections.put("WHOLEFILE",wholeFile);
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

	@Override
	@SuppressWarnings("unchecked")
	public DVector getLanguageParser(String parser)
	{
		final String parserKey=getLanguageParserKey();
		Hashtable<String,DVector> parserSections=(Hashtable<String,DVector>)Resources.getResource(parserKey);
		if(parserSections==null)
		{
			parserSections=loadFileSections("resources/parser_"+language.toUpperCase()+"_"+country.toUpperCase()+".properties");
			if(parserSections == null)
				parserSections=new Hashtable<String,DVector>();
			Resources.submitResource(parserKey,parserSections);
		}
		return parserSections.get(parser);
	}

	@Override
	@SuppressWarnings("unchecked")
	public DVector getLanguageTranslator(String parser)
	{
		final String translatorKey=getLanguageTranslatorKey();
		Hashtable<String,DVector> translationSections=(Hashtable<String,DVector>)Resources.getResource(translatorKey);
		if(translationSections==null)
		{
			translationSections=loadFileSections("resources/translation_"+language.toUpperCase()+"_"+country.toUpperCase()+".properties");
			if(translationSections == null)
				translationSections=new Hashtable<String,DVector>();
			Resources.submitResource(translatorKey,translationSections);
		}
		return translationSections.get(parser);
	}

	@Override
	public void clear()
	{
		final String translatorKey=getLanguageTranslatorKey();
		final String parserKey=getLanguageParserKey();
		Resources.removeResource(translatorKey);
		Resources.removeResource(parserKey);
	}

	public boolean insertExpansion(List<String> MORE_CMDS, String str, int m, int strLen, boolean nothingDone)
	{
		final List<String> expansion=CMParms.parseAny(CMStrings.replaceAll(str,"\\t","\t"),'\n',false);
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
	public List<List<String>> preCommandParser(List<String> CMDS)
	{
		final List<String> MORE_CMDS=new Vector<String>();
		final String combinedWithTabs=CMParms.combineWithTabs(CMDS,0);
		MORE_CMDS.add(combinedWithTabs);
		final DVector parser=CMLib.lang().getLanguageParser("COMMAND-PRE-PROCESSOR");
		if((parser==null)||(CMDS==null))
		{
			return new XVector<List<String>>(CMDS);
		}
		Pattern pattern=null;
		Matcher matcher=null;
		Command I=null;
		String str=null;
		String rep=null;
		String wit=null;
		int strLen=-1;
		int autoIgnoreLen=0;
		HashSet<String> ignoreSet=null;
		for(int p=0;p<parser.size();p++)
		{
			I=(Command)CMath.s_valueOf(Command.class,(String)parser.elementAt(p,1));
			if(I!=null)
			switch(I)
			{
			case DEFINE:
				break;
			case REPLACE:
			{
				pattern=(Pattern)parser.elementAt(p,2);
				boolean nothingDone=false;
				while(!nothingDone)
				{
					nothingDone=true;
					for(int m=0;m<MORE_CMDS.size();m++)
					{
						str=MORE_CMDS.get(m);
						strLen=str.length();
						matcher=pattern.matcher(str);
						if(matcher.find())
						{
							str=(String)parser.elementAt(p,3);
							for(int i=0;i<=matcher.groupCount();i++)
								str=CMStrings.replaceAll(str,"\\"+i,matcher.group(i));
							if(!MORE_CMDS.get(m).equals(str))
								nothingDone=insertExpansion(MORE_CMDS,str,m,strLen,nothingDone);
						 }
					}
				}
				break;
			}
			case REPLACEWHOLE:
			{
				rep=((Hashtable<String,String>)parser.elementAt(p,2)).get(combinedWithTabs.toLowerCase());
				if(rep!=null)
				{
					insertExpansion(MORE_CMDS,rep,0,combinedWithTabs.length(),true);
					p=parser.size();
				}
				break;
			}
			case REPLACEEXACT:
			{
				rep=((Hashtable<String,String>)parser.elementAt(p,2)).get(combinedWithTabs);
				if(rep!=null)
				{
					insertExpansion(MORE_CMDS,rep,0,combinedWithTabs.length(),true);
					p=parser.size();
				}
				break;
			}
			case REPLACEALL:
			{
				rep=(String)parser.elementAt(p,2);
				if(rep.length()==0)
					break;
				for(int m=0;m<MORE_CMDS.size();m++)
				{
					str=MORE_CMDS.get(m);
					strLen=str.length();
					int x=str.toLowerCase().indexOf(rep);
					if(x>=0)
					{
						while(x>=0)
						{
							wit=(String)parser.elementAt(p,3);
							str=str.substring(0,x)+wit+str.substring(x+rep.length());
							x=str.toLowerCase().indexOf(rep,x+wit.length());
						}
						insertExpansion(MORE_CMDS,str,m,strLen,true);
					}
				}
				break;
			}
			case IGNORE:
			{
				pattern=(Pattern)parser.elementAt(p,2);
				matcher=pattern.matcher(combinedWithTabs);
				if(matcher.find())
					return new XVector<List<String>>();
				break;
			}
			case IGNOREWHOLE:
			{
				ignoreSet=(HashSet<String>)parser.elementAt(p,2);
				if(ignoreSet.contains(combinedWithTabs.toLowerCase()))
					return new XVector<List<String>>();
				break;
			}
			case AUTOIGNORE:
				autoIgnoreLen=((Integer)parser.elementAt(p,2)).intValue();
				if(autoIgnoreLen==0)
					autoIgnoreLen=100;
				break;
			}
		}
		if((MORE_CMDS.size()==1)
		&&(MORE_CMDS.get(0).equals(combinedWithTabs)))
		{
			if((autoIgnoreLen>0)&&(str!=null)&&(str.length()<=autoIgnoreLen))
			{
				if(ignoreSet==null)
				{
					ignoreSet=new HashSet<String>();
					parser.addElement("IGNOREWHOLE",ignoreSet,ignoreSet);
				}
				ignoreSet.add(combinedWithTabs.toLowerCase());
				final DVector fileData=getLanguageParser("WHOLEFILE");
				final DVector fileIndexes=getLanguageParser("INDEXES");
				addAutoIgnoredString(combinedWithTabs,fileData,fileIndexes,"COMMAND-PRE-PROCESSOR");
			}
		}
		final List<List<String>> FINAL_CMDS=new Vector<List<String>>();
		for(int m=0;m<MORE_CMDS.size();m++)
			FINAL_CMDS.add(CMParms.parseTabs(MORE_CMDS.get(m),false));
		return FINAL_CMDS;
	}

	@SuppressWarnings("unchecked")
	protected String basicParser(String str, String section, boolean nullIfLonger, boolean isParser)
	{
		if(str==null)
			return null;
		final DVector parser=isParser?getLanguageParser(section):getLanguageTranslator(section);
		if(parser==null)
			return null;
		Pattern pattern=null;
		Matcher matcher=null;
		final String oldStr=str;
		int autoIgnoreLen=0;
		Command I=null;
		HashSet<String> ignoreSet=null;
		String rep=null;
		String wit=null;
		for(int p=0;p<parser.size();p++)
		{
			I=(Command)CMath.s_valueOf(Command.class,(String)parser.elementAt(p,1));
			if(I!=null)
			switch(I)
			{
			case DEFINE:
				break;
			case REPLACE:
			{
				pattern=(Pattern)parser.elementAt(p,2);
				matcher=pattern.matcher(str);
				if(matcher.find())
				{
					for(int i=0;i<=matcher.groupCount();i++)
						str=CMStrings.replaceAll(str,"\\"+i,matcher.group(i));
				}
				break;
			}
			case REPLACEWHOLE:
			{
				rep=((Hashtable<String,String>)parser.elementAt(p,2)).get(str.toLowerCase());
				if(rep!=null)
					return rep;
				break;
			}
			case REPLACEEXACT:
			{
				rep=((Hashtable<String,String>)parser.elementAt(p,2)).get(str);
				if(rep!=null)
					return rep;
				break;
			}
			case REPLACEALL:
			{
				rep=(String)parser.elementAt(p,2);
				if(rep.length()==0)
					break;
				int x=str.toLowerCase().indexOf(rep);
				while(x>=0)
				{
					wit=(String)parser.elementAt(p,3);
					str=str.substring(0,x)+wit+str.substring(x+rep.length());
					x=str.toLowerCase().indexOf(rep,x+wit.length());
				}
				break;
			}
			case IGNORE:
			{
				pattern=(Pattern)parser.elementAt(p,2);
				matcher=pattern.matcher(str);
				if(matcher.find())
					return null;
				break;
			}
			case IGNOREWHOLE:
			{
				ignoreSet=(HashSet<String>)parser.elementAt(p,2);
				if(ignoreSet.contains(str.toLowerCase()))
					return null;
				break;
			}
			case AUTOIGNORE:
				autoIgnoreLen=((Integer)parser.elementAt(p,2)).intValue();
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
					parser.addElement("IGNOREWHOLE",ignoreSet,ignoreSet);
				}
				ignoreSet.add(oldStr.toLowerCase());
				DVector fileData=null;
				DVector fileIndexes=null;
				if(isParser)
				{
					fileData=getLanguageParser("WHOLEFILE");
					fileIndexes=getLanguageParser("INDEXES");
				}
				else
				{
					fileData=getLanguageTranslator("WHOLEFILE");
					fileIndexes=getLanguageTranslator("INDEXES");
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

	public void addAutoIgnoredString(String str, DVector fileData, DVector fileIndexes, String sectionName)
	{
		if((fileData==null)||(str==null)||(fileData.size()<1))
			return;
		final String filename=(String)fileData.elementAt(0,1);
		if(fileIndexes==null)
			return;
		int index=fileIndexes.indexOf(sectionName.toUpperCase().trim());
		if(index<0)
			return;
		index=((Integer)fileIndexes.elementAt(index,2)).intValue();
		for(int f=0;f<fileIndexes.size();f++)
		{
			if(((Integer)fileIndexes.elementAt(f,2)).intValue()>index)
				fileIndexes.setElementAt(f,2,Integer.valueOf(((Integer)fileIndexes.elementAt(f,2)).intValue()+1));
		}
		str=filterString(str);
		final String newStr="IGNOREWHOLE \""+str+"\"";
		if(index==fileData.size()-1)
			fileData.addElement(filename,newStr);
		else
			fileData.insertElementAt(index+1,filename,newStr);
		final StringBuffer buf=new StringBuffer("");
		for(int f=0;f<fileData.size();f++)
			buf.append(((String)fileData.elementAt(f,2))+"\n\r");
		final CMFile F=new CMFile(filename,null,CMFile.FLAG_FORCEALLOW);
		if((F.exists())&&(F.canWrite()))
			F.saveText(buf);
	}

	@Override
	public String preItemParser(String item)
	{
		return basicParser(item,"ITEM-PRE-PROCESSOR",true,true);
	}

	@Override
	public String failedItemParser(String item)
	{
		return basicParser(item,"ITEM-FAIL-PROCESSOR",true,true);
	}

	@Override
	public String rawInputParser(String words)
	{
		final String parsed = basicParser(words,"RAW-INPUT-PROCESSOR",true,true);
		return (parsed==null)?words:parsed;
	}

	@Override
	public String filterTranslation(String item)
	{
		return basicParser(item,"FILTER-TRANSLATION",false,false);
	}

	@Override
	public String sessionTranslation(String item)
	{
		return basicParser(item,"SESSION-TRANSLATION",false,false);
	}

	@Override
	public String finalTranslation(String item)
	{
		return basicParser(item,"FINAL-TRANSLATION",false,false);
	}

	@Override
	public String fullSessionTranslation(final String str, final String ... xs)
	{
		if((str==null)||(str.length()==0))
			return str;
		final String sessionStr=sessionTranslation(str);
		return CMStrings.replaceVariables((sessionStr==null)?str:sessionStr, xs);
	}

	@Override
	public String[] sessionTranslation(final String[] str)
	{
		if((str==null)||(str.length==0))
			return str;
		for(int i=0;i<str.length;i++)
		{
			final String s=str[i];
			if(s!=null)
			{
				final String sessionStr=sessionTranslation(s);
				if(sessionStr!=null)
					str[i]=sessionStr;
			}
		}
		return str;
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
		final String sessionStr=sessionTranslation(str);
		return CMStrings.replaceVariables((sessionStr==null)?str:sessionStr, xs);
	}
}
