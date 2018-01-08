package com.planet_ink.coffee_mud.Common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import com.planet_ink.coffee_mud.Common.interfaces.PlayerAccount;
import com.planet_ink.coffee_mud.Common.interfaces.Session;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Common.interfaces.Session.SessionStatus;
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary.ColorState;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.CMFile;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.interfaces.CMObject;
import com.planet_ink.coffee_mud.core.interfaces.Environmental;
import com.planet_ink.coffee_mud.core.interfaces.Physical;
import com.planet_ink.coffee_mud.core.interfaces.Tickable;
/*
   Copyright 2008-2018 Bo Zimmerman

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
public class FakeSession implements Session
{
	protected CMFile theFile=null;
	protected ByteArrayOutputStream bout=null;
	protected MOB mob = null;
	protected Vector<String> inputV = new Vector<String>();

	public boolean tick(Tickable ticking, int tickID){return false;}

	@Override
	public String ID()
	{
		return "FakeSession";
	}

	@Override
	public String name()
	{
		return ID();
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch (final Exception e)
		{
			return new FakeSession();
		}
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			return (CMObject) this.clone();
		}
		catch (final Exception e)
		{
			return newInstance();
		}
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	public long getTickStatus()
	{
		return 0;
	}

	@Override
	public void initializeSession(Socket s, String groupName, String introTextStr)
	{
		if (introTextStr.equalsIgnoreCase("MEMORY"))
			bout = new ByteArrayOutputStream();
		else
			theFile = new CMFile(introTextStr, null, CMFile.FLAG_LOGERRORS);
	}

	@Override
	public String getGroupName()
	{
		return Thread.currentThread().getThreadGroup().getName();
	}

	@Override
	public void setGroupName(String group)
	{
	}

	@Override
	public long getStartTime()
	{
		return 0;
	}

	@Override
	public int getGroupID()
	{
		return Thread.currentThread().getThreadGroup().getName().charAt(0);
	}

	@Override
	public boolean isLockedUpWriting()
	{
		return false;
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public void run()
	{
	}

	@Override
	public boolean isRunning()
	{
		return false;
	}

	@Override
	public boolean isPendingLogin(final String otherLoginName)
	{
		return false;
	}

	@Override
	public void logout(boolean b1)
	{
	}

	@Override
	public String getTerminalType()
	{
		return "Fake";
	}

	@Override
	public void negotiateTelnetMode(int code)
	{
	}

	@Override
	public boolean isAllowedMxp(String tag)
	{
		return false;
	}

	@Override
	public boolean isFake()
	{
		return true;
	}

	@Override
	public void setIdleTimers()
	{
	}

	@Override
	public void sendGMCPEvent(final String eventName, final String json)
	{
	}

	@Override
	public void setFakeInput(String input)
	{
		inputV.add(input);
	}

	@Override
	public String[] getColorCodes()
	{
		return new String[255];
	}

	@Override
	public void onlyPrint(String msg, boolean noCache)
	{
		if (theFile != null)
		{
			synchronized (theFile)
			{
				theFile.saveText(msg, true);
			}
		}
		if (bout != null)
		{
			synchronized (bout)
			{
				try
				{
					bout.write(msg.getBytes());
				}
				catch (final Exception e)
				{
					Log.errOut("FakeSession", e);
				}
			}
		}
	}

	@Override
	public void onlyPrint(String msg)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void rawOut(String msg)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void rawPrintln(String msg)
	{
		onlyPrint(msg + "\n", false);
	}

	@Override
	public void safeRawPrintln(String msg)
	{
		onlyPrint(msg + "\n", false);
	}

	public void rawPrintln(String msg, int pageBreak)
	{
		onlyPrint(msg + "\n", false);
	}

	@Override
	public void rawPrint(String msg)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void safeRawPrint(String msg)
	{
		onlyPrint(msg, false);
	}

	public void rawPrint(String msg, int pageBreak)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void stdPrint(String msg)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void stdPrint(Physical Source, Environmental Target, Environmental Tool, String msg)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void stdPrintln(String msg)
	{
		onlyPrint(msg + "\n", false);
	}

	@Override
	public void stdPrintln(Physical Source, Environmental Target, Environmental Tool, String msg)
	{
		onlyPrint(msg + "\n", false);
	}

	@Override
	public void rawCharsOut(char[] c)
	{
		onlyPrint(new String(c), false);
	}

	@Override
	public void print(String msg)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void promptPrint(String msg)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void print(Physical Source, Environmental Target, Environmental Tool, String msg)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void println(String msg)
	{
		onlyPrint(msg + "\n", false);
	}

	@Override
	public void println(Physical Source, Environmental Target, Environmental Tool, String msg)
	{
		onlyPrint(msg + "\n", false);
	}

	@Override
	public void wraplessPrintln(String msg)
	{
		onlyPrint(msg + "\n", false);
	}

	@Override
	public void wraplessPrint(String msg)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void colorOnlyPrintln(String msg, boolean noCache)
	{
		onlyPrint(msg + "\n", false);
	}

	@Override
	public void colorOnlyPrint(String msg, boolean noCache)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void colorOnlyPrintln(String msg)
	{
		onlyPrint(msg + "\n", false);
	}

	@Override
	public void colorOnlyPrint(String msg)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void setPromptFlag(boolean truefalse)
	{
	}

	@Override
	public char hotkey(long maxWait)
	{
		return ' ';
	}

	@Override
	public String prompt(String Message, String Default)
	{
		onlyPrint(Message, false);
		final String msg = readlineContinue();
		if (msg.length() == 0)
			return Default;
		return msg;
	}

	@Override
	public void prompt(InputCallback callBack)
	{
		callBack.showPrompt();
		callBack.setInput(readlineContinue());
		callBack.callBack();
	}

	@Override
	public String prompt(String Message, String Default, long maxTime)
	{
		return prompt(Message, Default);
	}

	@Override
	public String prompt(String Message)
	{
		return prompt(Message, "");
	}

	@Override
	public String prompt(String Message, long maxTime)
	{
		return prompt(Message, "");
	}

	@Override
	public boolean confirm(String Message, String Default)
	{
		if (Default.toUpperCase().startsWith("T"))
			Default = "Y";
		final String YN = choose(Message, "YN", Default, 0);
		return (YN.equals("Y")) ? true : false;
	}

	@Override
	public boolean confirm(String Message, String Default, long maxTime)
	{
		return confirm(Message, Default, 0);
	}

	@Override
	public String choose(String Message, String Choices, String Default)
	{
		onlyPrint(Message, false);
		final String msg = readlineContinue();
		if (msg.length() == 0)
			return Default;
		if (Choices.toUpperCase().indexOf(msg.toUpperCase().trim()) >= 0)
			return msg.toUpperCase().trim();
		return Default;
	}

	@Override
	public String choose(final String Message, final String Choices, final String Default, long maxTime, List<String> paramsOut) throws IOException
	{
		return choose(Message, Choices, Default);
	}

	@Override
	public String choose(String Message, String Choices, String Default, long maxTime)
	{
		return choose(Message, Choices, Default);
	}

	@Override
	public String blockingIn(long timeoutMillis, boolean filter)
	{
		return readlineContinue();
	}

	@Override
	public String readlineContinue()
	{
		synchronized (inputV)
		{
			if (inputV.size() == 0)
				return "";
			final String input = inputV.firstElement();
			inputV.removeElementAt(0);
			return input;
		}
	}

	@Override
	public void setBeingSnoopedBy(Session session, boolean onOff)
	{
	}

	@Override
	public boolean isBeingSnoopedBy(Session S)
	{
		return S == this;
	}

	@Override
	public int snoopSuspension(int x)
	{
		return 0;
	}

	public void cmdExit(MOB mob, List<String> commands) throws Exception
	{
	}

	@Override
	public void stopSession(boolean t1, boolean t2, boolean t3)
	{
	}

	@Override
	public boolean isStopped()
	{
		return false;
	}

	@Override
	public boolean isAfk()
	{
		return false;
	}

	@Override
	public void setAfkFlag(boolean truefalse)
	{
	}

	@Override
	public String getAfkMessage()
	{
		if (bout != null)
			return new String(bout.toByteArray());
		return "";
	}

	@Override
	public void setAFKMessage(String str)
	{
	}

	@Override
	public List<String> getPreviousCMD()
	{
		return inputV;
	}

	@Override
	public MOB mob()
	{
		return mob;
	}

	@Override
	public void setMob(MOB newmob)
	{
		mob = newmob;
	}

	@Override
	public void setAccount(PlayerAccount account)
	{
	}

	public int convertEscape(final StringBuffer str, final int index)
	{
		return index + 1;
	}

	@Override
	public void setCurrentColor(ColorState newcolor)
	{
	}

	@Override
	public void setLastColor(ColorState newColor)
	{
	}

	@Override
	public ColorState getCurrentColor()
	{
		return CMLib.color().getNormalColor();
	}

	@Override
	public ColorState getLastColor()
	{
		return CMLib.color().getNormalColor();
	}

	@Override
	public int getWrap()
	{
		return 80;
	}

	@Override
	public String getAddress()
	{
		return "";
	}

	@Override
	public SessionStatus getStatus()
	{
		return SessionStatus.MAINLOOP;
	}

	@Override
	public void setStatus(SessionStatus newStatus)
	{
	}

	@Override
	public boolean isWaitingForInput()
	{
		return false;
	}

	@Override
	public long getTotalMillis()
	{
		return 0;
	}

	@Override
	public long getTotalTicks()
	{
		return 0;
	}

	@Override
	public long getIdleMillis()
	{
		return 0;
	}

	@Override
	public long getMillisOnline()
	{
		return 0;
	}

	@Override
	public long getLastPKFight()
	{
		return 0;
	}

	@Override
	public void setLastPKFight()
	{
	}

	@Override
	public long getLastNPCFight()
	{
		return 0;
	}

	@Override
	public void setLastNPCFight()
	{
	}

	@Override
	public long getInputLoopTime()
	{
		return 0;
	}

	@Override
	public void setInputLoopTime()
	{
	}

	@Override
	public List<String> getLastMsgs()
	{
		return new Vector<String>();
	}

	@Override
	public void setServerTelnetMode(int telnetCode, boolean onOff)
	{
	}

	@Override
	public boolean getServerTelnetMode(int telnetCode)
	{
		return false;
	}

	@Override
	public void setClientTelnetMode(int telnetCode, boolean onOff)
	{
	}

	@Override
	public boolean getClientTelnetMode(int telnetCode)
	{
		return false;
	}

	@Override
	public void changeTelnetMode(int telnetCode, boolean onOff)
	{
	}

	@Override
	public void initTelnetMode(int mobbitmap)
	{
	}

	@Override
	public int getSaveStatIndex()
	{
		return 0;
	}

	@Override
	public String getStat(String code)
	{
		return null;
	}

	@Override
	public String[] getStatCodes()
	{
		return new String[0];
	}

	@Override
	public boolean isStat(String code)
	{
		return false;
	}

	@Override
	public void setStat(String code, String val)
	{
	}

	@Override
	public long activeTimeMillis()
	{
		return 0;
	}

	@Override
	public boolean isAllowedMcp(String packageName, float version)
	{
		return false;
	}

	@Override
	public boolean sendMcpCommand(String packageCommand, String parms)
	{
		return false;
	}

	@Override
	public boolean autoLogin(String name, String password)
	{
		return false;
	}
}
