package com.planet_ink.coffee_mud.Common.interfaces;
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
import java.io.IOException;
import java.net.Socket;
/* 
   Copyright 2000-2008 Bo Zimmerman

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
/**
 * A Session object is the key interface between the internet user 
 * and their player MOB.  In fact, the presence of an attached session
 * object to a MOB is the only difference between an NPC MOB and a player MOB.
 * This object handles input, output, and related processes.
 */
public interface Session extends CMCommon
{
    
    /**
     * 
     * @param s
     * @param introTextStr
     */
    public void initializeSession(Socket s, String introTextStr);
    
    /**
     * 
     */
    public void start();
    
    /**
     * 
     * @return
     */
    
	public String[] clookup();
    
    /**
     * 
     * @param msg
     * @param pageBreak
     * @param noCache
     */
	
	public void onlyPrint(String msg, int pageBreak, boolean noCache);
    
    /**
     * 
     * @param msg
     */
	public void onlyPrint(String msg);
    
    /**
     * 
     * @param msg
     */
	
    public void rawOut(String msg);
    
    /**
     * 
     * @param msg
     */
	public void rawPrintln(String msg);
    
    /**
     * 
     * @param msg
     * @param pageBreak
     */
	public void rawPrintln(String msg, int pageBreak);
    
    /**
     * 
     * @param msg
     */
	public void rawPrint(String msg);
    
    /**
     * 
     * @param msg
     * @param pageBreak
     */
	public void rawPrint(String msg, int pageBreak);
    
    /**
     * 
     * @param msg
     */
	public void stdPrint(String msg);
    
    /**
     * 
     * @param Source
     * @param Target
     * @param Tool
     * @param msg
     */
	public void stdPrint(Environmental Source,
						 Environmental Target,
						 Environmental Tool,
						 String msg);
    
    /**
     * 
     * @param msg
     */
	public void stdPrintln(String msg);
    
    /**
     * 
     * @param Source
     * @param Target
     * @param Tool
     * @param msg
     */
	public void stdPrintln(Environmental Source,
						   Environmental Target,
						   Environmental Tool,
						   String msg);
    
    /**
     * 
     * @param c
     */
	public void out(char[] c);
    
    /**
     * 
     * @return
     */
	public boolean isLockedUpWriting();
    
    /**
     * 
     * @param msg
     */
	public void print(String msg);
    
    /**
     * 
     * @param Source
     * @param Target
     * @param Tool
     * @param msg
     */
	public void print(Environmental Source,
					  Environmental Target,
					  Environmental Tool,
					  String msg);
    
    /**
     * 
     * @param msg
     */
	public void println(String msg);
    
    /**
     * 
     * @param Source
     * @param Target
     * @param Tool
     * @param msg
     */
	public void println(Environmental Source,
						Environmental Target,
						Environmental Tool,
						String msg);
    
    /**
     * 
     * @param truefalse
     */
	public void setPromptFlag(boolean truefalse);
    
    /**
     * 
     * @param msg
     */
	public void wraplessPrintln(String msg);
    
    /**
     * 
     * @param msg
     */
	public void wraplessPrint(String msg);
    
    /**
     * 
     * @param msg
     * @param pageBreak
     * @param noCache
     */
	public void colorOnlyPrintln(String msg, int pageBreak, boolean noCache);
    
    /**
     * 
     * @param msg
     * @param pageBreak
     * @param noCache
     */
	public void colorOnlyPrint(String msg, int pageBreak, boolean noCache);
    
    /**
     * 
     * @param msg
     */
	public void colorOnlyPrintln(String msg);
    
    /**
     * 
     * @param msg
     */
	public void colorOnlyPrint(String msg);
    
    /**
     * 
     * @param maxWait
     * @return
     */
    public char hotkey(long maxWait);
    
    /**
     * 
     * @param Message
     * @param Default
     * @return
     * @throws IOException
     */
	public String prompt(String Message, String Default)
		throws IOException;
    
    /**
     * 
     * @param Message
     * @param Default
     * @param maxTime
     * @return
     * @throws IOException
     */
	public String prompt(String Message, String Default, long maxTime)
		throws IOException;
    
    /**
     * 
     * @param Message
     * @return
     * @throws IOException
     */
	public String prompt(String Message)
		throws IOException;
    
    /**
     * 
     * @param Message
     * @param maxTime
     * @return
     * @throws IOException
     */
	public String prompt(String Message, long maxTime)
		throws IOException;
    
    /**
     * 
     * @param Message
     * @param Default
     * @return
     * @throws IOException
     */
	public boolean confirm(String Message, String Default)
	    throws IOException;
    
    /**
     * 
     * @param Message
     * @param Default
     * @param maxTime
     * @return
     * @throws IOException
     */
	public boolean confirm(String Message, String Default, long maxTime)
	    throws IOException;
    
    /**
     * 
     * @param Message
     * @param Choices
     * @param Default
     * @return
     * @throws IOException
     */
	public String choose(String Message, String Choices, String Default)
	    throws IOException;
    
    /**
     * 
     * @param Message
     * @param Choices
     * @param Default
     * @param maxTime
     * @return
     * @throws IOException
     */
	public String choose(String Message, String Choices, String Default, long maxTime)
	    throws IOException;
    
    /**
     * 
     * @param S
     */
	public void startBeingSnoopedBy(Session S);
    
    /**
     * 
     * @param S
     */
	public void stopBeingSnoopedBy(Session S);
    
    /**
     * 
     * @param S
     * @return
     */
	public boolean amBeingSnoopedBy(Session S);
    
    /**
     * 
     * @param change
     * @return
     */
	public int snoopSuspension(int change);
    
    /**
     * 
     * @param mob
     * @param commands
     * @throws Exception
     */
	public void cmdExit(MOB mob, Vector commands)
		throws Exception;
    
    /**
     * 
     * @param killThread
     */
	public void logoff(boolean killThread);
    
    /**
     * 
     * @return
     */
	public boolean killFlag();
    
    /**
     * 
     * @param truefalse
     */
	public void setKillFlag(boolean truefalse);
    
    /**
     * 
     * @return
     */
	public boolean afkFlag();
    
    /**
     * 
     * @param truefalse
     */
	public void setAfkFlag(boolean truefalse);
    
    /**
     * 
     * @return
     */
    public String afkMessage();
    
    /**
     * 
     * @param str
     */
    public void setAFKMessage(String str);
    
    /**
     * 
     * @return
     * @throws IOException
     */
	public String blockingIn()
		throws IOException;
    
    /**
     * 
     * @return
     * @throws IOException
     */
	public String readlineContinue()
		throws IOException;
    
    /**
     * 
     * @return
     */
	public Vector previousCMD();
    
    /**
     * 
     * @return
     */
	public MOB mob();
    
    /**
     * 
     * @param newmob
     */
	public void setMob(MOB newmob);
    
    /**
     * 
     * @param c
     * @return
     */
	public String makeEscape(int c);
    
    /**
     * 
     * @param c
     * @return
     */
	public int getColor(char c);
    
    /**
     * 
     * @return
     */
	public int currentColor();
    
    /**
     * 
     * @return
     */
    public int lastColor();
    
    /**
     * 
     * @return
     */
	public int getWrap();
    
    /**
     * 
     * @return
     */
	public String getAddress();
    
    /**
     * 
     * @return
     */
	public int getStatus();
    
    /**
     * 
     * @return
     */
	public long getTotalMillis();
    
    /**
     * 
     * @return
     */
	public long getTotalTicks();
    
    /**
     * 
     * @return
     */
	public long getIdleMillis();
    
    /**
     * 
     * @return
     */
    public long getMillisOnline();
    
    /**
     * 
     * @return
     */
    public long getLastPKFight();
    
    /**
     * 
     */
    public void setLastPKFight();
    
    /**
     * 
     * @return
     */
    public long getLastNPCFight();
    
    /**
     * 
     */
    public void setLastNPCFight();
    
    /**
     * 
     * @return
     */
    public long lastLoopTime();
    
    /**
     * 
     * @return
     */
    public Vector getLastMsgs();

    /**
     * 
     * @param telnetCode
     * @param onOff
     */
    public void setServerTelnetMode(int telnetCode, boolean onOff);
    
    /**
     * 
     * @param telnetCode
     * @return
     */
    public boolean serverTelnetMode(int telnetCode);
    
    /**
     * 
     * @param telnetCode
     * @param onOff
     */
    public void setClientTelnetMode(int telnetCode, boolean onOff);
    
    /**
     * 
     * @param telnetCode
     * @return
     */
    public boolean clientTelnetMode(int telnetCode);
    
    /**
     * 
     * @param telnetCode
     * @param onOff
     */
    public void changeTelnetMode(int telnetCode, boolean onOff);
    
    /**
     * 
     * @param mobbitmap
     */
    public void initTelnetMode(int mobbitmap);
    
    /** TELNET CODE: transmit binary */
    public static final int TELNET_BINARY=0;
    /** TELNET CODE: echo */
    public static final int TELNET_ECHO=1;
    /** TELNET CODE: supress go ahead*/
    public static final int TELNET_SUPRESS_GO_AHEAD=3;
    /** TELNET CODE: sending terminal type*/
    public static final int TELNET_TERMTYPE=24;
    /** TELNET CODE: Negotiate About Window Size.*/
    public static final int TELNET_NAWS=31;
    /** TELNET CODE: Remote Flow Control.*/
    public static final int TELNET_TOGGLE_FLOW_CONTROL=33;
    /** TELNET CODE: Linemode*/
    public static final int TELNET_LINEMODE=34;
    /** TELNET CODE: text compression, protocol 1*/
    public static final int TELNET_COMPRESS=85;
    /** TELNET CODE: text compression, protocol 2*/
    public static final int TELNET_COMPRESS2=86;
    /** TELNET CODE: MSP SOund protocol*/
    public static final int TELNET_MSP=90;
    /** TELNET CODE: MXP Extended protocol*/
	public static final int TELNET_MXP=91;
    /** TELNET CODE: End of subnegotiation parameters*/
	public static final int TELNET_SE=240;
    /** TELNET CODE: Are You There*/
    public static final int TELNET_AYT=246;
    /** TELNET CODE: Erase character*/
    public static final int TELNET_EC=247;
    /** TELNET CODE: Indicates that what follows is subnegotiation of the indicated option*/
    public static final int TELNET_SB=250;
    /** TELNET CODE: Indicates the desire to begin performing, or confirmation that you are now performing, the indicated option*/
	public static final int TELNET_WILL=251;
    /** TELNET CODE: Indicates the refusal to perform, or continue performing, the indicated option*/
	public static final int TELNET_WONT=252;
    /** TELNET CODE: Indicates the request that the other party perform, or confirmation that you are expecting the other party to perform, the indicated option*/
	public static final int TELNET_DO=253;
    /** TELNET CODE: 253 doubles as fake ansi telnet code*/
    public static final int TELNET_ANSI=253;
    /** TELNET CODE: Indicates the demand that the other party stop performing, or confirmation that you are no longer expecting the other party to perform, the indicated option.*/
	public static final int TELNET_DONT=254;
    /** TELNET CODE: IAC*/
	public static final int TELNET_IAC=255;
    /** Array String-friendly descriptions of the various telnet codes.  Indexed by code id 0-255*/
    public static final String[] TELNET_DESCS=
    { 
        "BINARY","ECHO","2","SUPRESS GO AHEAD","4","5","6","7","8","9", //0-9
        "10","11","12","13","14","15","16","17","18","19", //10-19
        "20","21","22","23","TERMTYPE","25","26","27","28","29", //20-29
        "30","NAWS","32","FLOWCONTROL","LINEMODE","35","36","37","38","39", //30-39
        "40","41","42","43","44","45","46","47","48","49", //40-49
        "50","51","52","53","54","55","56","57","58","59", //50-59
        "60","61","62","63","64","65","66","67","68","69", //60-69
        "70","71","72","73","74","75","76","77","78","79", //70-79
        "","","","","","COMPRESS","COMPRESS2","","","", //80-89
        "MSP","MXP","","","","","","","","", //90-99
        "","","","","","","","","","", //100-109
        "","","","","","","","","","", //110-119
        "","","","","","","","","","", //120-129
        "","","","","","","","","","", //130-139
        "","","","","","","","","","", //140-149
        "","","","","","","","","","", //150-159
        "","","","","","","","","","", //160-169
        "","","","","","","","","","", //170-179
        "","","","","","","","","","", //180-189
        "","","","","","","","","","", //190-199
        "","","","","","","","","","", //200-209
        "","","","","","","","","","", //210-219
        "","","","","","","","","","", //220-229
        "","","","","","","","","","", //230-239
        "SE","","","","","","AYT","EC","","", //240-249
        "SB","","","ANSI","",""              //250-255
    };
    
    /** for REPLY command, constant for maximum number of saved strings */
    public static final int MAX_PREVMSGS=100;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_MODE=1;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_MODEMASK_EDIT=1;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_MODEMASK_TRAPSIG=2;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_MODEMASK_ACK=4;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC=3;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_DEFAULT=3;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_VALUE=2;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_CANTCHANGE=1;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_NOSUPPORT=0;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_ACK=128;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_XON=15;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_XOFF=16;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_EOF=8;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_SUSP=9;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_BRK=2;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_IP=3;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_AO=4;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_AYT=5;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_EOR=6;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_OK=0;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGIN=1;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGIN1=2;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGIN2=3;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT=4;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT1=5;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT2=6;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT3=7;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT4=8;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT5=9;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT6=10;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT7=11;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT8=12;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT9=13;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT10=14;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT11=15;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT12=16;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUTFINAL=17;
    /** Status strings indexed by the various STATUS_ constants.  Descriptive strings */
	public static final String[] STATUS_STR={"OPEN","LOGIN-S","LOGIN-1","LOGIN-2",
											"LOGOUT-S","LOGOUT-1","LOGOUT-2","LOGOUT-3",
											"LOGOUT-4","LOGOUT-5","LOGOUT-6","LOGOUT-7",
											"LOGOUT-8","LOGOUT-9","LOGOUT-10","LOGOUT-11",
											"LOGOUT-12","CLOSED"};
}
