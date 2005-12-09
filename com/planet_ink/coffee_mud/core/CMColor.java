package com.planet_ink.coffee_mud.utils;

import java.util.*;

import com.planet_ink.coffee_mud.common.CommonStrings;
import com.planet_ink.coffee_mud.interfaces.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class CMColor
{
    private CMColor(){};
    
	public static String[] clookup=null;
    public static String[] htlookup=null;
	
	public static final String COLOR_WHITE="\033[1;37m";
	public static final String COLOR_LIGHTGREEN="\033[1;32m";
	public static final String COLOR_LIGHTBLUE="\033[1;34m";
	public static final String COLOR_LIGHTRED="\033[1;31m";
	public static final String COLOR_YELLOW="\033[1;33m";
	public static final String COLOR_LIGHTCYAN="\033[1;36m";
	public static final String COLOR_LIGHTPURPLE="\033[1;35m";
	public static final String COLOR_GREY="\033[0;37m";
	public static final String COLOR_GREEN="\033[0;32m";
	public static final String COLOR_BLUE="\033[0;34m";
	public static final String COLOR_RED="\033[0;31m";
	public static final String COLOR_BROWN="\033[0;33m";
	public static final String COLOR_CYAN="\033[0;36m";
	public static final String COLOR_PURPLE="\033[0;35m";
	public static final String COLOR_DARKGREY="\033[1;30m";
	public static final String COLOR_BLACK="\033[0;30m";
	public static final String COLOR_NONE="\033[0;0m";
	public static final String COLOR_BOLD="\033[1m";
	public static final String COLOR_UNDERLINE="\033[4m";
	public static final String COLOR_BLINK="\033[5m";
	public static final String COLOR_ITALICS="\033[6m";
	public static final String COLOR_BGWHITE="\033[47m";
	public static final String COLOR_BGGREEN="\033[42m";
	public static final String COLOR_BGBLUE="\033[44m";
	public static final String COLOR_BGRED="\033[41m";
	public static final String COLOR_BGYELLOW="\033[43m";
	public static final String COLOR_BGCYAN="\033[46m";
	public static final String COLOR_BGPURPLE="\033[45m";
	public static final String COLOR_BGBLACK="\033[40m";
	public static final String COLOR_BGDEFAULT="\033[49m";
	
    public static final String HTTAG_WHITE="<FONT COLOR=WHITE";
    public static final String HTTAG_LIGHTGREEN="<FONT COLOR=LIGHTGREEN";
    public static final String HTTAG_LIGHTBLUE="<FONT COLOR=BLUE";
    public static final String HTTAG_LIGHTRED="<FONT COLOR=RED";
    public static final String HTTAG_YELLOW="<FONT COLOR=YELLOW";
    public static final String HTTAG_LIGHTCYAN="<FONT COLOR=CYAN";
    public static final String HTTAG_LIGHTPURPLE="<FONT COLOR=VIOLET";
    public static final String HTTAG_GREY="<FONT COLOR=LIGHTGREY";
    public static final String HTTAG_GREEN="<FONT COLOR=GREEN";
    public static final String HTTAG_BLUE="<FONT COLOR=#000099";
    public static final String HTTAG_RED="<FONT COLOR=#993300";
    public static final String HTTAG_BROWN="<FONT COLOR=#999966";
    public static final String HTTAG_CYAN="<FONT COLOR=DARKCYAN";
    public static final String HTTAG_PURPLE="<FONT COLOR=PURPLE";
    public static final String HTTAG_DARKGREY="<FONT COLOR=GRAY";
    public static final String HTTAG_BLACK="<FONT COLOR=BLACK";
    public static final String HTTAG_NONE="</FONT";
    public static final String HTTAG_BOLD="<B";
    public static final String HTTAG_UNDERLINE="<U";
    public static final String HTTAG_BLINK="<BLINK";
    public static final String HTTAG_ITALICS="<I";
    public static final String HTTAG_BGWHITE=" style=\"background-color: white\"";
    public static final String HTTAG_BGGREEN=" style=\"background-color: green\"";
    public static final String HTTAG_BGBLUE=" style=\"background-color: #000099\"";
    public static final String HTTAG_BGRED=" style=\"background-color: #993300\"";
    public static final String HTTAG_BGYELLOW=" style=\"background-color: #999966\"";
    public static final String HTTAG_BGCYAN=" style=\"background-color: darkcyan\"";
    public static final String HTTAG_BGPURPLE=" style=\"background-color: purple\"";
    public static final String HTTAG_BGBLACK=" style=\"background-color: black\"";
    public static final String HTTAG_BGDEFAULT=" style=\"background-color: white\"";
    
	public static final String[] COLOR_CODELETTERSINCARDINALORDER={
		"k","r","g","y","b","p","c","w",null,null
	};
	public static final String[] COLOR_ALLCOLORS={
		COLOR_WHITE,COLOR_LIGHTGREEN,COLOR_LIGHTBLUE,COLOR_LIGHTRED,
		COLOR_YELLOW,COLOR_LIGHTCYAN,COLOR_LIGHTPURPLE,COLOR_GREY,
		COLOR_GREEN,COLOR_BLUE,COLOR_RED,COLOR_BROWN,
		COLOR_CYAN,COLOR_PURPLE,COLOR_DARKGREY,COLOR_BLACK,COLOR_NONE,
		COLOR_BOLD,COLOR_UNDERLINE,COLOR_BLINK,COLOR_ITALICS,
		COLOR_BGWHITE,COLOR_BGGREEN,COLOR_BGBLUE,COLOR_BGRED,
		COLOR_BGYELLOW,COLOR_BGCYAN,COLOR_BGPURPLE,COLOR_BGBLACK,
		COLOR_BGDEFAULT
	};
    public static final String[] COLOR_ALLHTTAGS={
        HTTAG_WHITE,HTTAG_LIGHTGREEN,HTTAG_LIGHTBLUE,HTTAG_LIGHTRED,
        HTTAG_YELLOW,HTTAG_LIGHTCYAN,HTTAG_LIGHTPURPLE,HTTAG_GREY,
        HTTAG_GREEN,HTTAG_BLUE,HTTAG_RED,HTTAG_BROWN,
        HTTAG_CYAN,HTTAG_PURPLE,HTTAG_DARKGREY,HTTAG_BLACK,HTTAG_NONE,
        HTTAG_BOLD,HTTAG_UNDERLINE,HTTAG_BLINK,HTTAG_ITALICS,
        HTTAG_BGWHITE,HTTAG_BGGREEN,HTTAG_BGBLUE,HTTAG_BGRED,
        HTTAG_BGYELLOW,HTTAG_BGCYAN,HTTAG_BGPURPLE,HTTAG_BGBLACK,
        HTTAG_BGDEFAULT
    };
	public static final String[] COLOR_ALLCOLORNAMES={
		"WHITE","LIGHTGREEN","LIGHTBLUE","LIGHTRED",
		"YELLOW","LIGHTCYAN","LIGHTPURPLE","GREY",
		"GREEN","BLUE","RED","BROWN",
		"CYAN","PURPLE","DARKGREY","BLACK","NONE",
		"BOLD","UNDERLINE","BLINK","ITALICS",
		"BGWHITE","BGGREEN","BGBLUE","BGRED",
		"BGYELLOW","BGCYAN","BGPURPLE","BGBLACK",
		"BGDEFAULT"
	};
    
	public static final String[] COLOR_ALLNORMALCOLORCODELETTERS={
		"w","g","b","r",
		"y","c","p","W",
		"G","B","R","Y",
		"C","P","k"
	};
	public static final String[] COLOR_ALLEXTENDEDCOLORCODELETTERS={
		"w","g","b","r",
		"y","c","p","W",
		"G","B","R","Y",
		"C","P","k","K"
	};
    //remaining=aijlnoszAJV
    public static final char COLORCODE_YOU_FIGHT='f';
    public static final char COLORCODE_FIGHT_YOU='e';
	public static final char COLORCODE_FIGHT='F';
	public static final char COLORCODE_SPELL='S';
	public static final char COLORCODE_EMOTE='E';
    public static final char COLORCODE_WEATHER='J';
	public static final char COLORCODE_TALK='T';
	public static final char COLORCODE_TELL='t';
	public static final char COLORCODE_CHANNEL='Q';
	public static final char COLORCODE_CHANNELFORE='q';
	public static final char COLORCODE_IMPORTANT1='x';
	public static final char COLORCODE_IMPORTANT2='X';
	public static final char COLORCODE_IMPORTANT3='Z';
	public static final char COLORCODE_ROOMTITLE='O';
	public static final char COLORCODE_ROOMDESC='L';
	public static final char COLORCODE_DIRECTION='D';
	public static final char COLORCODE_DOORDESC='d';
	public static final char COLORCODE_ITEM='I';
	public static final char COLORCODE_MOB='M';
	public static final char COLORCODE_HITPOINTS='h';
	public static final char COLORCODE_MANA='m';
	public static final char COLORCODE_MOVES='v';
	public static final char COLORCODE_NORMAL='N';
	public static final char COLORCODE_HIGHLIGHT='H';
    public static final char COLORCODE_UNEXPDIRECTION='U';
    public static final char COLORCODE_UNEXPDOORDESC='u';
	public static final char[] COLORCODE_ALLCODES={
	    COLORCODE_YOU_FIGHT,COLORCODE_FIGHT_YOU,COLORCODE_FIGHT,COLORCODE_SPELL,
        COLORCODE_EMOTE,COLORCODE_TALK,COLORCODE_TELL,COLORCODE_CHANNEL,
        COLORCODE_CHANNELFORE,COLORCODE_IMPORTANT1,COLORCODE_IMPORTANT2,
        COLORCODE_IMPORTANT3,COLORCODE_ROOMTITLE,COLORCODE_ROOMDESC,
	    COLORCODE_DIRECTION,COLORCODE_DOORDESC,COLORCODE_ITEM,COLORCODE_MOB,
	    COLORCODE_HITPOINTS,COLORCODE_MANA,COLORCODE_MOVES,COLORCODE_NORMAL,
	    COLORCODE_HIGHLIGHT,COLORCODE_UNEXPDIRECTION,COLORCODE_UNEXPDOORDESC,
        COLORCODE_WEATHER
	};
	public static final String[] COLORCODE_ALLCODENAMES={
	    "YOU-FIGHT","FIGHT-YOU","FIGHT","SPELL","EMOTE","TALK",
	    "TELL","CHANNEL","CHANNELFORE","IMPORTANT1",
	    "IMPORTANT2","IMPORTANT3","ROOMTITLE","ROOMDESC",
	    "DIRECTION","DOORDESC","ITEM","MOB",
	    "HITPOINTS","MANA","MOVES","NORMAL",
	    "HIGHLIGHT","UNEXPDIRECTION","UNEXPDOORDESC","WEATHER"
	};

	
	public static int translateSingleCMCodeToANSIOffSet(String code)
	{
	    if(code.length()==0) return -1;
	    if(!code.startsWith("^")) return -1;
	    int i=code.length()-1;
	    while(i>=0)
	        if(Character.isLetter(code.charAt(i)))
	            return "krgybpcw".indexOf(Character.toLowerCase(code.charAt(i)));
	    return 3;
	}
	
	public static String translateCMCodeToANSI(String code)
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
	
	public static String translateANSItoCMCode(String code)
	{
	    if(code.length()==0) return code;
	    if(code.indexOf("^")==0) return code;
	    if(code.indexOf("|")>0) return code;
	    String code1=null;
	    String code2=null;
	    boolean bold=(code.indexOf(";1;")>0)||(code.indexOf("[1;")>0);
	    for(int i=0;i<=9;i++)
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
	
	public static String mixHTMLCodes(String code1, String code2)
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
	
    public static String mixColorCodes(String code1, String code2)
    {
        if((code1==null)||(code1.length()==0)) return code2;
        if((code2==null)||(code2.length()==0)) return code1;
        if(code1.charAt(code1.length()-1)!=code2.charAt(code2.length()-1))
            return code1+code2;
        if(code2.startsWith("\033["))code2=code2.substring("\033[".length());
        return code1.substring(0,code1.length()-1)+";"+code2;
    }
    
    public static CMMsg fixSourceFightColor(CMMsg msg)
    {
        if(msg.sourceMessage()!=null)
            msg.setSourceMessage(Util.replaceAll(msg.sourceMessage(),"^F","^f"));
        if(msg.targetMessage()!=null)
            msg.setTargetMessage(Util.replaceAll(msg.targetMessage(),"^F","^e"));
        return msg;
    }
    
    public static String[] standardHTMLlookups()
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
            htlookup[COLORCODE_NORMAL]=HTTAG_GREY;
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
            Vector schemeSettings=Util.parseCommas(CommonStrings.getVar(CommonStrings.SYSTEM_COLORSCHEME),true);
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
        }
        return htlookup;
    }
    
	public static String[] standardColorLookups()
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
			Vector schemeSettings=Util.parseCommas(CommonStrings.getVar(CommonStrings.SYSTEM_COLORSCHEME),true);
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
