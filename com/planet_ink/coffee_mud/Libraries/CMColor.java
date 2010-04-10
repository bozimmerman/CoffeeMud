package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class CMColor extends StdLibrary implements ColorLibrary
{
    public String ID(){return "CMColor";}
    
	public String[] clookup=null;
    public String[] htlookup=null;
	
	public int translateSingleCMCodeToANSIOffSet(String code)
	{
	    if(code.length()==0) return -1;
	    if(!code.startsWith("^")) return -1;
	    int i=code.length()-1;
	    while(i>=0)
	        if(Character.isLetter(code.charAt(i)))
	            return "krgybpcw".indexOf(Character.toLowerCase(code.charAt(i)));
	    return 3;
	}
	
	public String translateCMCodeToANSI(String code)
	{
	    if(code.length()==0) return code;
	    if(!code.startsWith("^")) return code;
	    int background=code.indexOf("|");
	    int bold=0;
	    for(int i=0;i<code.length();i++)
	        if(Character.isLowerCase(code.charAt(i)))
	            bold=1;
	    if(background>0)
	        return "\033["+(40+translateSingleCMCodeToANSIOffSet(code.substring(0,background)))+";"+bold+";"+(30+translateSingleCMCodeToANSIOffSet(code.substring(background+1)))+"m";
        return "\033["+bold+";"+(30+translateSingleCMCodeToANSIOffSet(code))+"m";
	}
	
	public String translateANSItoCMCode(String code)
	{
	    if(code.length()==0) return code;
	    if(code.indexOf("^")==0) return code;
	    if(code.indexOf("|")>0) return code;
	    String code1=null;
	    String code2=null;
	    boolean bold=(code.indexOf(";1;")>0)||(code.indexOf("[1;")>0);
	    for(int i=0;i<COLOR_CODELETTERSINCARDINALORDER.length;i++)
	    {
	        if((code1==null)&&(code.indexOf(""+(40+i))>0))
	            code1="^"+Character.toUpperCase(COLOR_CODELETTERSINCARDINALORDER[i].charAt(0));
	        if((code2==null)&&(code.indexOf(""+(30+i))>0))
	            code2="^"+(bold?COLOR_CODELETTERSINCARDINALORDER[i]:(""+Character.toUpperCase(COLOR_CODELETTERSINCARDINALORDER[i].charAt(0))));
	    }
	    if((code1!=null)&&(code2!=null))
	        return code1+"|"+code2;
	    else
	    if((code1==null)&&(code2!=null))
	        return code2;
	    else
	    if((code1!=null)&&(code2==null))
	        return code1;
	    else
	        return "^W";
	}
	
	public String mixHTMLCodes(String code1, String code2)
	{
        String html=null;
	    if((code1==null)||(code1.length()==0))
            html=code2;
        else
	    if((code2==null)||(code2.length()==0)) 
            html=code1;
        else
        if(code1.startsWith(" ")&&(code2.startsWith("<FONT")))
            html=code2+code1;
        else
        if(code2.startsWith(" ")&&(code1.startsWith("<FONT")))
            html=code1+code2;
        else
        if(code1.startsWith("<")&&(code2.startsWith("<")))
            html=code1+">"+code2;
        else
        if(!code1.startsWith("<"))
            html=code2;
        else
            html=code1;
        if(html.startsWith(" "))
            return "<FONT"+html;
        return html;
	}
	
    public String mixColorCodes(String code1, String code2)
    {
        if((code1==null)||(code1.length()==0)) return code2;
        if((code2==null)||(code2.length()==0)) return code1;
        if(code1.charAt(code1.length()-1)!=code2.charAt(code2.length()-1))
            return code1+code2;
        if(code2.startsWith("\033["))code2=code2.substring("\033[".length());
        return code1.substring(0,code1.length()-1)+";"+code2;
    }
    
    public CMMsg fixSourceFightColor(CMMsg msg)
    {
        if(msg.sourceMessage()!=null)
            msg.setSourceMessage(CMStrings.replaceAll(msg.sourceMessage(),"^F","^f"));
        if(msg.targetMessage()!=null)
            msg.setTargetMessage(CMStrings.replaceAll(msg.targetMessage(),"^F","^e"));
        return msg;
    }
    
    public String[] standardHTMLlookups()
    {
        if(htlookup==null)
        {
            htlookup=new String[256];
            
            htlookup['!']=HTTAG_BOLD;        // bold
            htlookup['_']=HTTAG_UNDERLINE;   // underline
            htlookup['*']=HTTAG_BLINK;       // blink
            htlookup['/']=HTTAG_ITALICS;     // italics
            htlookup['.']=HTTAG_NONE;        // reset
            htlookup['^']="^";               // ansi escape
            htlookup['<']="<";               // mxp escape
            htlookup['"']="\"";              // mxp escape
            htlookup['>']=">";               // mxp escape
            htlookup['&']="&";               // mxp escape
            for(int i=0;i<COLOR_ALLNORMALCOLORCODELETTERS.length;i++)
                htlookup[COLOR_ALLNORMALCOLORCODELETTERS[i].charAt(0)]=COLOR_ALLHTTAGS[i];
            
            // default color settings:
            htlookup[COLORCODE_HIGHLIGHT]=HTTAG_LIGHTCYAN;
            htlookup[COLORCODE_YOU_FIGHT]=HTTAG_LIGHTPURPLE;
            htlookup[COLORCODE_FIGHT_YOU]=HTTAG_LIGHTRED;
            htlookup[COLORCODE_FIGHT]=HTTAG_RED;
            htlookup[COLORCODE_SPELL]=HTTAG_YELLOW;
            htlookup[COLORCODE_EMOTE]=HTTAG_LIGHTPURPLE;
            htlookup[COLORCODE_WEATHER]=HTTAG_WHITE;
            htlookup[COLORCODE_TALK]=HTTAG_LIGHTBLUE;
            htlookup[COLORCODE_TELL]=HTTAG_CYAN;
            htlookup[COLORCODE_CHANNEL]=mixHTMLCodes(HTTAG_LIGHTCYAN,HTTAG_BGBLUE);
            htlookup[COLORCODE_CHANNELFORE]=HTTAG_LIGHTCYAN;
            htlookup[COLORCODE_IMPORTANT1]=mixHTMLCodes(HTTAG_LIGHTCYAN,HTTAG_BGBLUE);
            htlookup[COLORCODE_IMPORTANT2]=mixHTMLCodes(HTTAG_YELLOW,HTTAG_BGBLUE);
            htlookup[COLORCODE_IMPORTANT3]=mixHTMLCodes(HTTAG_YELLOW,HTTAG_BGRED);
            htlookup[COLORCODE_ROOMTITLE]=HTTAG_LIGHTCYAN;
            htlookup[COLORCODE_ROOMDESC]=HTTAG_WHITE;
            htlookup[COLORCODE_DIRECTION]=mixHTMLCodes(HTTAG_LIGHTCYAN,HTTAG_BGBLUE);
            htlookup[COLORCODE_DOORDESC]=HTTAG_LIGHTBLUE;
            htlookup[COLORCODE_ITEM]=HTTAG_LIGHTGREEN;
            htlookup[COLORCODE_MOB]=HTTAG_LIGHTPURPLE;
            htlookup[COLORCODE_HITPOINTS]=HTTAG_LIGHTCYAN;
            htlookup[COLORCODE_MANA]=HTTAG_LIGHTCYAN;
            htlookup[COLORCODE_MOVES]=HTTAG_LIGHTCYAN;
            htlookup[COLORCODE_UNEXPDIRECTION]=mixHTMLCodes(HTTAG_CYAN,HTTAG_BGBLUE);
            htlookup[COLORCODE_UNEXPDOORDESC]=HTTAG_LIGHTBLUE;
            Vector schemeSettings=CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_COLORSCHEME),true);
            for(int i=0;i<schemeSettings.size();i++)
            {
                String s=(String)schemeSettings.elementAt(i);
                int x=s.indexOf("=");
                if(x>0)
                {
                    String key=s.substring(0,x).trim();
                    String value=s.substring(x+1).trim();
                    char codeChar=' ';
                    for(int ii=0;ii<COLORCODE_ALLCODENAMES.length;ii++)
                        if(key.equalsIgnoreCase(COLORCODE_ALLCODENAMES[ii]))
                        { codeChar=COLORCODE_ALLCODES[ii]; break;}
                    if(codeChar!=' ')
                    {
                        String newVal=null;
                        String addColor=null;
                        String addCode=null;
                        while(value.length()>0)
                        {
                            x=value.indexOf("+");
                            if(x<0)
                            {
                                addColor=value;
                                value="";
                            }
                            else
                            {
                                addColor=value.substring(0,x).trim();
                                value=value.substring(x+1).trim();
                            }
                            addCode=null;
                            for(int ii=0;ii<COLOR_ALLCOLORNAMES.length;ii++)
                                if(addColor.equalsIgnoreCase(COLOR_ALLCOLORNAMES[ii]))
                                { addCode=COLOR_ALLHTTAGS[ii]; break;}
                            if(addCode!=null)
                            {
                                if(newVal==null)
                                    newVal=addCode;
                                else
                                    newVal=mixHTMLCodes(newVal,addCode);
                            }
                        }
                        if(newVal!=null)
                            htlookup[codeChar]=newVal;
                    }
                }
            }

            for(int i=0;i<htlookup.length;i++)
            {
                String s=htlookup[i];
                if((s!=null)&&(s.startsWith("^"))&&(s.length()>1))
                    htlookup[i]=htlookup[s.charAt(1)];
            }
            htlookup[COLORCODE_NORMAL]=HTTAG_NONE;
        }
        return htlookup;
    }
    
    public void clearLookups(){clookup=null;}
	public String[] standardColorLookups()
	{
		if(clookup==null)
		{
			clookup=new String[256];
			clookup['!']=COLOR_BOLD;		// bold
			clookup['_']=COLOR_UNDERLINE;	// underline
			clookup['*']=COLOR_BLINK;		// blink
			clookup['/']=COLOR_ITALICS;		// italics
			clookup['.']=COLOR_NONE;		// reset
			clookup['^']="^";				// ansi escape
			clookup['<']="<";				// mxp escape
			clookup['"']="\"";				// mxp escape
			clookup['>']=">";				// mxp escape
			clookup['&']="&";				// mxp escape
			clookup['@']=null;				// ** special 256 color code
			for(int i=0;i<COLOR_ALLNORMALCOLORCODELETTERS.length;i++)
			    clookup[COLOR_ALLNORMALCOLORCODELETTERS[i].charAt(0)]=COLOR_ALLCOLORS[i];
			
			// default color settings:
			clookup[COLORCODE_NORMAL]=COLOR_GREY;
			clookup[COLORCODE_HIGHLIGHT]=COLOR_LIGHTCYAN;
            clookup[COLORCODE_YOU_FIGHT]=COLOR_LIGHTPURPLE;
            clookup[COLORCODE_FIGHT_YOU]=COLOR_LIGHTRED;
			clookup[COLORCODE_FIGHT]=COLOR_RED;
			clookup[COLORCODE_SPELL]=COLOR_YELLOW;
			clookup[COLORCODE_EMOTE]=COLOR_LIGHTPURPLE;
            clookup[COLORCODE_WEATHER]=COLOR_WHITE;
			clookup[COLORCODE_TALK]=COLOR_LIGHTBLUE;
			clookup[COLORCODE_TELL]=COLOR_CYAN;
			clookup[COLORCODE_CHANNEL]=mixColorCodes(COLOR_LIGHTCYAN,COLOR_BGBLUE);
			clookup[COLORCODE_CHANNELFORE]=COLOR_LIGHTCYAN;
			clookup[COLORCODE_IMPORTANT1]=mixColorCodes(COLOR_LIGHTCYAN,COLOR_BGBLUE);
			clookup[COLORCODE_IMPORTANT2]=mixColorCodes(COLOR_YELLOW,COLOR_BGBLUE);
			clookup[COLORCODE_IMPORTANT3]=mixColorCodes(COLOR_YELLOW,COLOR_BGRED);
			clookup[COLORCODE_ROOMTITLE]=COLOR_LIGHTCYAN;
			clookup[COLORCODE_ROOMDESC]=COLOR_WHITE;
			clookup[COLORCODE_DIRECTION]=mixColorCodes(COLOR_LIGHTCYAN,COLOR_BGBLUE);
			clookup[COLORCODE_DOORDESC]=COLOR_LIGHTBLUE;
			clookup[COLORCODE_ITEM]=COLOR_LIGHTGREEN;
			clookup[COLORCODE_MOB]=COLOR_LIGHTPURPLE;
			clookup[COLORCODE_HITPOINTS]=COLOR_LIGHTCYAN;
			clookup[COLORCODE_MANA]=COLOR_LIGHTCYAN;
			clookup[COLORCODE_MOVES]=COLOR_LIGHTCYAN;
            clookup[COLORCODE_UNEXPDIRECTION]=mixColorCodes(COLOR_CYAN,COLOR_BGBLUE);
            clookup[COLORCODE_UNEXPDOORDESC]=COLOR_LIGHTBLUE;
			Vector schemeSettings=CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_COLORSCHEME),true);
			for(int i=0;i<schemeSettings.size();i++)
			{
			    String s=(String)schemeSettings.elementAt(i);
			    int x=s.indexOf("=");
			    if(x>0)
			    {
			        String key=s.substring(0,x).trim();
			        String value=s.substring(x+1).trim();
			        char codeChar=' ';
			        for(int ii=0;ii<COLORCODE_ALLCODENAMES.length;ii++)
			            if(key.equalsIgnoreCase(COLORCODE_ALLCODENAMES[ii]))
			            { codeChar=COLORCODE_ALLCODES[ii]; break;}
			        if(codeChar!=' ')
			        {
			            String newVal=null;
			            String addColor=null;
			            String addCode=null;
			            while(value.length()>0)
			            {
			                x=value.indexOf("+");
			                if(x<0)
			                {
			                    addColor=value;
			                    value="";
			                }
			                else
			                {
			                    addColor=value.substring(0,x).trim();
			                    value=value.substring(x+1).trim();
			                }
			                addCode=null;
					        for(int ii=0;ii<COLOR_ALLCOLORNAMES.length;ii++)
					            if(addColor.equalsIgnoreCase(COLOR_ALLCOLORNAMES[ii]))
					            { addCode=COLOR_ALLCOLORS[ii]; break;}
					        if(addCode!=null)
					        {
					            if(newVal==null)
					                newVal=addCode;
					            else
					                newVal=mixColorCodes(newVal,addCode);
					        }
			            }
			            if(newVal!=null)
			                clookup[codeChar]=newVal;
			        }
			    }
			}
			
				

			for(int i=0;i<clookup.length;i++)
			{
				String s=clookup[i];
				if((s!=null)&&(s.startsWith("^"))&&(s.length()>1))
					clookup[i]=clookup[s.charAt(1)];
			}
		}
		return clookup;
	}
}
