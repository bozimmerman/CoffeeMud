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
   Copyright 2000-2006 Bo Zimmerman

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
public interface Session extends CMObject
{
    public void initializeSession(Socket s, String introTextStr);
    public void start();
    
	public String[] clookup();
	
	public void onlyPrint(String msg, int pageBreak, boolean noCache);
	public void onlyPrint(String msg);
	
	public void rawPrintln(String msg);
	public void rawPrintln(String msg, int pageBreak);
	public void rawPrint(String msg);
	public void rawPrint(String msg, int pageBreak);
	
	public void stdPrint(String msg);
	public void stdPrint(Environmental Source,
						 Environmental Target,
						 Environmental Tool,
						 String msg);
	public void stdPrintln(String msg);
	public void stdPrintln(Environmental Source,
						   Environmental Target,
						   Environmental Tool,
						   String msg);
	
	public void out(char[] c);
	public boolean isLockedUpWriting();
	
	public void print(String msg);
	public void print(Environmental Source,
					  Environmental Target,
					  Environmental Tool,
					  String msg);
	public void println(String msg);
	public void println(Environmental Source,
						Environmental Target,
						Environmental Tool,
						String msg);
	
	public void setPromptFlag(boolean truefalse);
	
	
	public void wraplessPrintln(String msg);
	public void wraplessPrint(String msg);
	
	public void colorOnlyPrintln(String msg, int pageBreak);
	public void colorOnlyPrint(String msg, int pageBreak);
	public void colorOnlyPrintln(String msg);
	public void colorOnlyPrint(String msg);
	
    public char hotkey(long maxWait);
	public String prompt(String Message, String Default)
		throws IOException;
	public String prompt(String Message, String Default, long maxTime)
		throws IOException;
	public String prompt(String Message)
		throws IOException;
	public String prompt(String Message, long maxTime)
		throws IOException;
	public boolean confirm(String Message, String Default)
	throws IOException;
	public boolean confirm(String Message, String Default, long maxTime)
	throws IOException;
	public String choose(String Message, String Choices, String Default)
	throws IOException;
	public String choose(String Message, String Choices, String Default, long maxTime)
	throws IOException;
	
	public void startBeingSnoopedBy(Session S);
	public void stopBeingSnoopedBy(Session S);
	public boolean amBeingSnoopedBy(Session S);
	
	public void cmdExit(MOB mob, Vector commands)
		throws Exception;
	public void logoff();
	public boolean killFlag();
	public void setKillFlag(boolean truefalse);
	
	public boolean afkFlag();
	public void setAfkFlag(boolean truefalse);
    public String afkMessage();
    public void setAFKMessage(String str);
	
	public String blockingIn()
		throws IOException;
	public String readlineContinue()
		throws IOException;
	
	public Vector previousCMD();
	
	public MOB mob();
	public void setMob(MOB newmob);
	
	public String makeEscape(int c);
	public int getColor(char c);
	public int currentColor();
    public int lastColor();
	public int getWrap();
	
	public String getAddress();
	public int getStatus();
	public long getTotalMillis();
	public long getTotalTicks();
	public long getIdleMillis();
    public long getMillisOnline();
    public long getLastPKFight();
    public void setLastPKFight();
    public long getLastNPCFight();
    public void setLastNPCFight();
    public long lastLoopTime();
    public Vector getLastMsgs();
	
    public void setServerTelnetMode(int telnetCode, boolean onOff);
    public boolean serverTelnetMode(int telnetCode);
    public void setClientTelnetMode(int telnetCode, boolean onOff);
    public boolean clientTelnetMode(int telnetCode);
    public void changeTelnetMode(int telnetCode, boolean onOff);
    public void initTelnetMode(int mobbitmap);
    
    public static final int TELNET_BINARY=0;
    public static final int TELNET_ECHO=1;
    public static final int TELNET_SUPRESS_GO_AHEAD=3;
    public static final int TELNET_TERMTYPE=24;
    public static final int TELNET_NAWS=31;
    public static final int TELNET_TOGGLE_FLOW_CONTROL=33;
    public static final int TELNET_LINEMODE=34;
    public static final int TELNET_COMPRESS=85;
    public static final int TELNET_COMPRESS2=86;
    public static final int TELNET_MSP=90;
	public static final int TELNET_MXP=91;
	public static final int TELNET_SE=240;
    public static final int TELNET_AYT=246;
    public static final int TELNET_EC=247;
    public static final int TELNET_SB=250;
	public static final int TELNET_WILL=251;
	public static final int TELNET_WONT=252;
	public static final int TELNET_DO=253;
    public static final int TELNET_ANSI=253; // 253 doubles as fake ansi telnet code
	public static final int TELNET_DONT=254;
	public static final int TELNET_IAC=255;
    public static final String[] TELNET_DESCS=
    { 
        "BINARY","ECHO","","SUPRESS GO AHEAD","","","","","","", //0-9
        "","","","","","","","","","", //10-19
        "","","","","TERMTYPE","","","","","", //20-29
        "","NAWS","","FLOWCONTROL","LINEMODE","","","","","", //30-39
        "","","","","","","","","","", //40-49
        "","","","","","","","","","", //50-59
        "","","","","","","","","","", //60-69
        "","","","","","","","","","", //70-79
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
    
    public static final int MAX_PREVMSGS=100;
    
    public static final int TELNET_LINEMODE_MODE=1;
    public static final int TELNET_LINEMODE_MODEMASK_EDIT=1;
    public static final int TELNET_LINEMODE_MODEMASK_TRAPSIG=2;
    public static final int TELNET_LINEMODE_MODEMASK_ACK=4;
    public static final int TELNET_LINEMODE_SLC=3;
    public static final int TELNET_LINEMODE_SLC_DEFAULT=3;
    public static final int TELNET_LINEMODE_SLC_VALUE=2;
    public static final int TELNET_LINEMODE_SLC_CANTCHANGE=1;
    public static final int TELNET_LINEMODE_SLC_NOSUPPORT=0;
    public static final int TELNET_LINEMODE_SLC_ACK=128;
    public static final int TELNET_LINEMODE_SLC_XON=15;
    public static final int TELNET_LINEMODE_SLC_XOFF=16;
    public static final int TELNET_LINEMODE_SLC_EOF=8;
    public static final int TELNET_LINEMODE_SLC_SUSP=9;
    public static final int TELNET_LINEMODE_SLC_BRK=2;
    public static final int TELNET_LINEMODE_SLC_IP=3;
    public static final int TELNET_LINEMODE_SLC_AO=4;
    public static final int TELNET_LINEMODE_SLC_AYT=5;
    public static final int TELNET_LINEMODE_SLC_EOR=6;
    
	public static final int STATUS_OK=0;
	public static final int STATUS_LOGIN=1;
	public static final int STATUS_LOGIN1=2;
	public static final int STATUS_LOGIN2=3;
	public static final int STATUS_LOGOUT=4;
	public static final int STATUS_LOGOUT1=5;
	public static final int STATUS_LOGOUT2=6;
	public static final int STATUS_LOGOUT3=7;
	public static final int STATUS_LOGOUT4=8;
	public static final int STATUS_LOGOUT5=9;
	public static final int STATUS_LOGOUT6=10;
	public static final int STATUS_LOGOUT7=11;
	public static final int STATUS_LOGOUT8=12;
	public static final int STATUS_LOGOUT9=13;
	public static final int STATUS_LOGOUT10=14;
	public static final int STATUS_LOGOUT11=15;
	public static final int STATUS_LOGOUT12=16;
	public static final int STATUS_LOGOUTFINAL=17;
	public static final String[] STATUS_STR={"OPEN","LOGIN-S","LOGIN-1","LOGIN-2",
											"LOGOUT-S","LOGOUT-1","LOGOUT-2","LOGOUT-3",
											"LOGOUT-4","LOGOUT-5","LOGOUT-6","LOGOUT-7",
											"LOGOUT-8","LOGOUT-9","LOGOUT-10","LOGOUT-11",
											"LOGOUT-12","CLOSED"};
	
}
