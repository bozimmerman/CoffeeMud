package com.planet_ink.coffee_mud.Libraries;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.LanguageLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class DirtyLanguage extends StdLibrary implements LanguageLibrary
{
    public String ID(){return "DirtyLanguage";}
	
	protected String language="en";
    protected String country="TX";
    protected Locale currentLocale=null;
    protected static final int CMD_REPLACE=0;
    protected static final int CMD_REPLACEWHOLE=1;
    protected static final int CMD_IGNORE=2;
    protected static final int CMD_IGNOREWHOLE=3;
    protected static final int CMD_AUTOIGNORE=4;
    protected static final int CMD_DEFINE=5;
    protected static final int CMD_REPLACEALL=6;
    protected Hashtable HASHED_CMDS=CMStrings.makeNumericHash(
            new String[]{"REPLACE","REPLACEWHOLE","IGNORE","IGNOREWHOLE","AUTOIGNORE","DEFINE","REPLACEALL"});
	
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
        StringBuffer buf=new StringBuffer(str);
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
        StringBuffer buf=new StringBuffer(str);
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
    
    protected Hashtable loadFileSections(String filename)
    {
        Hashtable parserSections=new Hashtable();
        CMFile F=new CMFile(filename,null,false,true);
        if(!F.exists()){ Log.errOut("Language file "+filename+" not found! This mud is in deep doo-doo!"); return null;}
        StringBuffer alldata=F.text();
        Vector V=Resources.getFileLineVector(alldata);
        String s=null;
        DVector currentSection=null;
        DVector globalDefinitions=new DVector(2);
        DVector localDefinitions=new DVector(2);
        Hashtable currentSectionReplaceStrs=new Hashtable();
        HashSet currentSectionIgnoreStrs=new HashSet();
        DVector sectionIndexes=new DVector(2);
        DVector wholeFile=new DVector(2);
        for(int v=0;v<V.size();v++)
        {
            wholeFile.addElement(filename,(String)V.elementAt(v));
            s=((String)V.elementAt(v)).trim();
            if((s.startsWith("#"))||(s.trim().length()==0)) continue;
            if(s.startsWith("["))
            {
                int x=s.lastIndexOf("]");
                if((currentSectionReplaceStrs.size()>0)
                &&(currentSection!=null))
                    currentSection.addElement("REPLACEWHOLE",currentSectionReplaceStrs,currentSectionReplaceStrs);
                if((currentSectionIgnoreStrs.size()>0)
                &&(currentSection!=null))
                    currentSection.addElement("IGNOREWHOLE",currentSectionIgnoreStrs,currentSectionIgnoreStrs);
                currentSection=new DVector(3);
                currentSectionReplaceStrs=new Hashtable();
                currentSectionIgnoreStrs=new HashSet();
                parserSections.put(s.substring(1,x).toUpperCase(),currentSection);
                sectionIndexes.addElement(s.substring(1,x).toUpperCase(),Integer.valueOf(v));
                localDefinitions.clear();
            }
            else
            if(s.toUpperCase().startsWith("AUTOIGNORE"))
            {
                int x=s.indexOf(' ');
                if(x<0){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                Integer I=Integer.valueOf(CMath.s_int(s.substring(x+1).trim()));
                if(currentSection!=null)
                    currentSection.addElement("AUTOIGNORE",I,s.substring(x+1).trim());
            }
            else
            if(s.toUpperCase().startsWith("DEFINE"))
            {
                int regstart=s.indexOf('"');
                if(regstart<0){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                int regend=s.indexOf('"',regstart+1);
                while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
                    regend=s.indexOf('"',regend+1);
                if(regend<0){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                String variable=s.substring(regstart+1,regend).toUpperCase();
                s=s.substring(regend+1).trim();
                if(!s.toUpperCase().startsWith("AS")){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                regstart=s.indexOf('"');
                if(regstart<0){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                regend=s.indexOf('"',regstart+1);
                while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
                    regend=s.indexOf('"',regend+1);
                if(regend<0){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
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
                int regstart=s.indexOf('"');
                if(regstart<0){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                int regend=s.indexOf('"',regstart+1);
                while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
                    regend=s.indexOf('"',regend+1);
                if(regend<0){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                String expression=unFilterString(s.substring(regstart+1,regend));
                currentSectionIgnoreStrs.add(expression.toLowerCase());
            }
            else
            if(s.toUpperCase().startsWith("REPLACEWHOLE"))
            {
                int regstart=s.indexOf('"');
                if(regstart<0){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                int regend=s.indexOf('"',regstart+1);
                while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
                    regend=s.indexOf('"',regend+1);
                if(regend<0){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                String expression=unFilterString(s.substring(regstart+1,regend));
                s=s.substring(regend+1).trim();
                if(!s.toUpperCase().startsWith("WITH")){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                regstart=s.indexOf('"');
                if(regstart<0){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                regend=s.indexOf('"',regstart+1);
                while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
                    regend=s.indexOf('"',regend+1);
                if(regend<0){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                String replacement=unFilterString(s.substring(regstart+1,regend));
                currentSectionReplaceStrs.put(expression.toLowerCase(),replacement);
            }
            else
            if(s.toUpperCase().startsWith("REPLACEALL"))
            {
                String cmd="REPLACEALL";
                int regstart=s.indexOf('"');
                if(regstart<0){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                int regend=s.indexOf('"',regstart+1);
                while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
                    regend=s.indexOf('"',regend+1);
                if(regend<0){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                String expression=unFilterString(s.substring(regstart+1,regend));
                s=s.substring(regend+1).trim();
                if(!s.toUpperCase().startsWith("WITH")){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                regstart=s.indexOf('"');
                if(regstart<0){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                regend=s.indexOf('"',regstart+1);
                while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
                    regend=s.indexOf('"',regend+1);
                if(regend<0){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                String replacement=unFilterString(s.substring(regstart+1,regend));
                if(currentSection!=null)
	                currentSection.addElement(cmd,expression,replacement);
                currentSectionReplaceStrs.put(expression.toLowerCase(),replacement);
            }
            else
            if(s.toUpperCase().startsWith("REPLACE")||s.toUpperCase().startsWith("IGNORE"))
            {
                String cmd=s.toUpperCase().startsWith("REPLACE")?"REPLACE":"IGNORE";
                int regstart=s.indexOf('"');
                if(regstart<0){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                int regend=s.indexOf('"',regstart+1);
                while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
                    regend=s.indexOf('"',regend+1);
                if(regend<0){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                String expression=s.substring(regstart+1,regend);
                expression=replaceWithDefinitions(globalDefinitions,localDefinitions,expression);
                s=s.substring(regend+1).trim();
                String replacement=null;
                if(cmd.equals("REPLACE"))
                {
                    if(!s.toUpperCase().startsWith("WITH")){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                    regstart=s.indexOf('"');
                    if(regstart<0){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                    regend=s.indexOf('"',regstart+1);
                    while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
                        regend=s.indexOf('"',regend+1);
                    if(regend<0){ Log.errOut("Scripts","Syntax error in '"+filename+"', line "+(v+1)); continue;}
                    replacement=s.substring(regstart+1,regend);
                    replacement=replaceWithDefinitions(globalDefinitions,localDefinitions,replacement);
                }
                try
                {
                    Pattern expPattern=Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
                    if(currentSection!=null)
	                    currentSection.addElement(cmd,expPattern,replacement);
                }
                catch(Exception e)
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
        if((currentSectionIgnoreStrs.size()>0)
        &&(currentSection!=null))
            currentSection.addElement("IGNOREWHOLE",currentSectionIgnoreStrs,currentSectionIgnoreStrs);
        parserSections.put("INDEXES",sectionIndexes);
        parserSections.put("WHOLEFILE",wholeFile);
        return parserSections;
    }
    
	
    public DVector getLanguageParser(String parser)
    {
        Hashtable parserSections=(Hashtable)Resources.getResource("PARSER_"+language.toUpperCase()+"_"+country.toUpperCase());
    	if(parserSections==null)
        {
            parserSections=loadFileSections("resources/parser_"+language.toUpperCase()+"_"+country.toUpperCase()+".properties");
            Resources.submitResource("PARSER_"+language.toUpperCase()+"_"+country.toUpperCase(),parserSections);
        }
    	return (DVector)parserSections.get(parser);
    }
	
    public DVector getLanguageTranslator(String parser)
    {
        Hashtable translationSections=(Hashtable)Resources.getResource("TRANSLATION_"+language.toUpperCase()+"_"+country.toUpperCase());
        if(translationSections==null)
        {
            translationSections=loadFileSections("resources/translation_"+language.toUpperCase()+"_"+country.toUpperCase()+".properties");
            Resources.submitResource("TRANSLATION_"+language.toUpperCase()+"_"+country.toUpperCase(),translationSections);
        }
        return (DVector)translationSections.get(parser);
    }
    
	
	public void clear()
	{
        Resources.removeResource("TRANSLATION_"+language.toUpperCase()+"_"+country.toUpperCase());
        Resources.removeResource("PARSER_"+language.toUpperCase()+"_"+country.toUpperCase());
	}
	
    public boolean insertExpansion(Vector MORE_CMDS, String str, int m, int strLen, boolean nothingDone)
    {
        Vector expansion=CMParms.parseAny(CMStrings.replaceAll(str,"\\t","\t"),"\n",false);
        MORE_CMDS.setElementAt(expansion.elementAt(0),m);
        String expStr=(String)expansion.elementAt(0);
        if(expStr.length()<=strLen) nothingDone=false;
        boolean insert=m<MORE_CMDS.size()-1;
        for(int e=1;e<expansion.size();e++)
        {
            expStr=(String)expansion.elementAt(e);
            if(expStr.length()<=strLen) nothingDone=false;
            if(insert)
                MORE_CMDS.insertElementAt(expStr,m+e);
            else
                MORE_CMDS.addElement(expStr);
        }
        return nothingDone;
    }
    
    public Vector preCommandParser(Vector CMDS)
    {
        Vector MORE_CMDS=new Vector();
        String combinedWithTabs=CMParms.combineWithTabs(CMDS,0);
        MORE_CMDS.addElement(combinedWithTabs);
        DVector parser=CMLib.lang().getLanguageParser("COMMAND-PRE-PROCESSOR");
        if((parser==null)||(CMDS==null)){ MORE_CMDS.setElementAt(CMDS,0); return MORE_CMDS;}
        Pattern pattern=null;
        Matcher matcher=null;
        Integer I=null;
        String str=null;
        String rep=null;
        String wit=null;
        int strLen=-1;
        int autoIgnoreLen=0;
        HashSet ignoreSet=null;
        for(int p=0;p<parser.size();p++)
        {
            I=(Integer)HASHED_CMDS.get(parser.elementAt(p,1));
            if(I!=null)
            switch(I.intValue())
            {
            case CMD_REPLACE:
            {
                pattern=(Pattern)parser.elementAt(p,2);
                boolean nothingDone=false;
                while(!nothingDone)
                {
                    nothingDone=true;
                    for(int m=0;m<MORE_CMDS.size();m++)
                    {
                        str=(String)MORE_CMDS.elementAt(m);
                        strLen=str.length();
                        matcher=pattern.matcher(str);
                        if(matcher.find())
                        {
                            str=(String)parser.elementAt(p,3);
                            for(int i=0;i<=matcher.groupCount();i++)
                                str=CMStrings.replaceAll(str,"\\"+i,matcher.group(i));
                            if(!((String)MORE_CMDS.elementAt(m)).equals(str))
                                nothingDone=insertExpansion(MORE_CMDS,str,m,strLen,nothingDone);
                         }
                    }
                }
                break;
            }
            case CMD_REPLACEWHOLE:
            {
                rep=(String)((Hashtable)parser.elementAt(p,2)).get(combinedWithTabs.toLowerCase());
                if(rep!=null)
                {
                    insertExpansion(MORE_CMDS,rep,0,combinedWithTabs.length(),true);
                    p=parser.size();
                }
                break;
            }
            case CMD_REPLACEALL:
            {
                rep=(String)parser.elementAt(p,2);
                if(rep.length()==0) break;
                for(int m=0;m<MORE_CMDS.size();m++)
                {
                    str=(String)MORE_CMDS.elementAt(m);
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
            case CMD_IGNORE:
            {
                pattern=(Pattern)parser.elementAt(p,2);
                matcher=pattern.matcher(combinedWithTabs);
                if(matcher.find()) return CMDS;
                break;
            }
            case CMD_IGNOREWHOLE:
            {
                ignoreSet=(HashSet)parser.elementAt(p,2);
                if(ignoreSet.contains(combinedWithTabs.toLowerCase()))
                    return CMDS;
                break;
            }
            case CMD_AUTOIGNORE:
                autoIgnoreLen=((Integer)parser.elementAt(p,2)).intValue();
                if(autoIgnoreLen==0) autoIgnoreLen=100;
                break;
            }
        }
        if((MORE_CMDS.size()==1)
        &&(((String)MORE_CMDS.firstElement()).equals(combinedWithTabs)))
        {
            if((autoIgnoreLen>0)&&(str!=null)&&(str.length()<=autoIgnoreLen))
            {
                if(ignoreSet==null)
                {
                    ignoreSet=new HashSet();
                    parser.addElement("IGNOREWHOLE",ignoreSet,ignoreSet);
                }
                ignoreSet.add(combinedWithTabs.toLowerCase());
                DVector fileData=getLanguageParser("WHOLEFILE");
                DVector fileIndexes=getLanguageParser("INDEXES");
                addAutoIgnoredString(combinedWithTabs,fileData,fileIndexes,"COMMAND-PRE-PROCESSOR");
            }
        }
        for(int m=0;m<MORE_CMDS.size();m++)
            MORE_CMDS.setElementAt(CMParms.parseTabs((String)MORE_CMDS.elementAt(m),false),m);
        return MORE_CMDS;
    }
    
    protected String basicParser(String str, String section, boolean nullIfLonger, boolean isParser)
    {
        if(str==null) return null;
        DVector parser=isParser?getLanguageParser(section):getLanguageTranslator(section);
        if(parser==null) return null;
        Pattern pattern=null;
        Matcher matcher=null;
        String oldStr=str;
        int autoIgnoreLen=0;
        Integer I=null;
        HashSet ignoreSet=null;
        String rep=null;
        String wit=null;
        for(int p=0;p<parser.size();p++)
        {
            I=(Integer)HASHED_CMDS.get(parser.elementAt(p,1));
            if(I!=null)
            switch(I.intValue())
            {
            case CMD_REPLACE:
            {
                pattern=(Pattern)parser.elementAt(p,2);
                matcher=pattern.matcher(str);
                if(matcher.find())
                    for(int i=0;i<=matcher.groupCount();i++)
                        str=CMStrings.replaceAll(str,"\\"+i,matcher.group(i));
                break;
            }
            case CMD_REPLACEWHOLE:
            {
                rep=(String)((Hashtable)parser.elementAt(p,2)).get(str.toLowerCase());
                if(rep!=null) return rep;
                break;
            }
            case CMD_REPLACEALL:
            {
                rep=(String)parser.elementAt(p,2);
                if(rep.length()==0) break;
                int x=str.toLowerCase().indexOf(rep);
                while(x>=0)
                {
                    wit=(String)parser.elementAt(p,3);
                    str=str.substring(0,x)+wit+str.substring(x+rep.length());
                    x=str.toLowerCase().indexOf(rep,x+wit.length());
                }
                break;
            }
            case CMD_IGNORE:
            {
                pattern=(Pattern)parser.elementAt(p,2);
                matcher=pattern.matcher(str);
                if(matcher.find()) return null;
                break;
            }
            case CMD_IGNOREWHOLE:
            {
                ignoreSet=(HashSet)parser.elementAt(p,2);
                if(ignoreSet.contains(str.toLowerCase()))
                    return null;
                break;
            }
            case CMD_AUTOIGNORE:
                autoIgnoreLen=((Integer)parser.elementAt(p,2)).intValue();
                if(autoIgnoreLen==0) autoIgnoreLen=100;
                break;
            }
        }
        if(str.equals(oldStr))
        {
            if((autoIgnoreLen>0)&&(str.length()<=autoIgnoreLen))
            {
                if(ignoreSet==null)
                {
                    ignoreSet=new HashSet();
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
        if((fileData==null)||(str==null)||(fileData.size()<1)) return;
        String filename=(String)fileData.elementAt(0,1);
        if(fileIndexes==null) return;
        int index=fileIndexes.indexOf(sectionName.toUpperCase().trim());
        if(index<0) return;
        index=((Integer)fileIndexes.elementAt(index,2)).intValue();
        for(int f=0;f<fileIndexes.size();f++)
            if(((Integer)fileIndexes.elementAt(f,2)).intValue()>index)
                fileIndexes.setElementAt(f,2,Integer.valueOf(((Integer)fileIndexes.elementAt(f,2)).intValue()+1));
        str=filterString(str);
        String newStr="IGNOREWHOLE \""+str+"\"";
        if(index==fileData.size()-1)
            fileData.addElement(filename,newStr);
        else
            fileData.insertElementAt(index+1,filename,newStr);
        StringBuffer buf=new StringBuffer("");
        for(int f=0;f<fileData.size();f++)
            buf.append(((String)fileData.elementAt(f,2))+"\n\r");
        CMFile F=new CMFile(filename,null,false,true);
        if((F.exists())&&(F.canWrite()))
            F.saveText(buf);
    }
    
    public String preItemParser(String item)
    {
        return basicParser(item,"ITEM-PRE-PROCESSOR",true,true);
    }
    public String failedItemParser(String item)
    {
        return basicParser(item,"ITEM-FAIL-PROCESSOR",true,true);
    }
    public String filterTranslation(String item)
    {
        return basicParser(item,"FILTER-TRANSLATION",false,false);
    }
    public String sessionTranslation(String item)
    {
        return basicParser(item,"SESSION-TRANSLATION",false,false);
    }
    public String finalTranslation(String item)
    {
        return basicParser(item,"FINAL-TRANSLATION",false,false);
    }
}
