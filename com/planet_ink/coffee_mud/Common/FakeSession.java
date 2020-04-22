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
import com.planet_ink.coffee_mud.Common.interfaces.Session.SessionFilter;
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
   Copyright 2008-2020 Bo Zimmerman

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
	protected CMFile				theFile	= null;
	protected ByteArrayOutputStream	bout	= null;
	protected MOB					mob		= null;
	protected Vector<String>		inputV	= new Vector<String>();

	public boolean tick(final Tickable ticking, final int tickID)
	{
		return false;
	}

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
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	public long getTickStatus()
	{
		return 0;
	}

	@Override
	public void initializeSession(final Socket s, final String groupName, final String introTextStr)
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
	public void setGroupName(final String group)
	{
	}

	@Override
	public long getStartTime()
	{
		return 0;
	}

	@Override
	public boolean addSessionFilter(final SessionFilter filter)
	{
		return false;
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
	public void logout(final boolean b1)
	{
	}

	@Override
	public String getTerminalType()
	{
		return "Fake";
	}

	@Override
	public void negotiateTelnetMode(final int code)
	{
	}

	@Override
	public boolean isAllowedMxp(final String tag)
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
	public boolean sendGMCPEvent(final String eventName, final String json)
	{
		return false;
	}

	@Override
	public void setFakeInput(final String input)
	{
		inputV.add(input);
	}

	@Override
	public String[] getColorCodes()
	{
		return new String[255];
	}

	@Override
	public void onlyPrint(final String msg, final boolean noCache)
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
	public void onlyPrint(final String msg)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void rawOut(final String msg)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void rawPrintln(final String msg)
	{
		onlyPrint(msg + "\n", false);
	}

	@Override
	public void safeRawPrintln(final String msg)
	{
		onlyPrint(msg + "\n", false);
	}

	public void rawPrintln(final String msg, final int pageBreak)
	{
		onlyPrint(msg + "\n", false);
	}

	@Override
	public void rawPrint(final String msg)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void safeRawPrint(final String msg)
	{
		onlyPrint(msg, false);
	}

	public void rawPrint(final String msg, final int pageBreak)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void stdPrint(final String msg)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void stdPrint(final Physical Source, final Environmental Target, final Environmental Tool, final String msg)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void stdPrintln(final String msg)
	{
		onlyPrint(msg + "\n", false);
	}

	@Override
	public void stdPrintln(final Physical Source, final Environmental Target, final Environmental Tool, final String msg)
	{
		onlyPrint(msg + "\n", false);
	}

	@Override
	public void rawCharsOut(final char[] c)
	{
		onlyPrint(new String(c), false);
	}

	@Override
	public void print(final String msg)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void promptPrint(final String msg)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void print(final Physical Source, final Environmental Target, final Environmental Tool, final String msg)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void println(final String msg)
	{
		onlyPrint(msg + "\n", false);
	}

	@Override
	public void println(final Physical Source, final Environmental Target, final Environmental Tool, final String msg)
	{
		onlyPrint(msg + "\n", false);
	}

	@Override
	public void wraplessPrintln(final String msg)
	{
		onlyPrint(msg + "\n", false);
	}

	@Override
	public void wraplessPrint(final String msg)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void colorOnlyPrintln(final String msg, final boolean noCache)
	{
		onlyPrint(msg + "\n", false);
	}

	@Override
	public void colorOnlyPrint(final String msg, final boolean noCache)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void colorOnlyPrintln(final String msg)
	{
		onlyPrint(msg + "\n", false);
	}

	@Override
	public void colorOnlyPrint(final String msg)
	{
		onlyPrint(msg, false);
	}

	@Override
	public void setPromptFlag(final boolean truefalse)
	{
	}

	@Override
	public char hotkey(final long maxWait)
	{
		return ' ';
	}

	@Override
	public String prompt(final String Message, final String Default)
	{
		onlyPrint(Message, false);
		final String msg = readlineContinue();
		if (msg.length() == 0)
			return Default;
		return msg;
	}

	@Override
	public void prompt(final InputCallback callBack)
	{
		callBack.showPrompt();
		callBack.setInput(readlineContinue());
		callBack.callBack();
	}

	@Override
	public String prompt(final String Message, final String Default, final long maxTime)
	{
		return prompt(Message, Default);
	}

	@Override
	public String prompt(final String Message)
	{
		return prompt(Message, "");
	}

	@Override
	public String prompt(final String Message, final long maxTime)
	{
		return prompt(Message, "");
	}

	@Override
	public boolean confirm(final String Message, String Default)
	{
		if (Default.toUpperCase().startsWith("T"))
			Default = "Y";
		final String YN = choose(Message, "YN", Default, 0);
		return (YN.equals("Y")) ? true : false;
	}

	@Override
	public boolean confirm(final String Message, final String Default, final long maxTime)
	{
		return confirm(Message, Default, 0);
	}

	@Override
	public String choose(final String Message, final String Choices, final String Default)
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
	public String choose(final String Message, final String Choices, final String Default, final long maxTime, final List<String> paramsOut) throws IOException
	{
		return choose(Message, Choices, Default);
	}

	@Override
	public String choose(final String Message, final String Choices, final String Default, final long maxTime)
	{
		return choose(Message, Choices, Default);
	}

	@Override
	public String blockingIn(final long timeoutMillis, final boolean filter)
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
	public void setBeingSnoopedBy(final Session session, final boolean onOff)
	{
	}

	@Override
	public boolean isBeingSnoopedBy(final Session S)
	{
		return S == this;
	}

	@Override
	public int snoopSuspension(final int x)
	{
		return 0;
	}

	public void cmdExit(final MOB mob, final List<String> commands) throws Exception
	{
	}

	@Override
	public void stopSession(final boolean t1, final boolean t2, final boolean t3)
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
	public void setAfkFlag(final boolean truefalse)
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
	public void setAFKMessage(final String str)
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
	public void setMob(final MOB newmob)
	{
		mob = newmob;
	}

	@Override
	public void setAccount(final PlayerAccount account)
	{
	}

	public int convertEscape(final StringBuffer str, final int index)
	{
		return index + 1;
	}

	@Override
	public void setCurrentColor(final ColorState newcolor)
	{
	}

	@Override
	public void setLastColor(final ColorState newColor)
	{
	}

	@Override
	public void pushMarkedColor(final ColorState newcolor)
	{
	}

	@Override
	public ColorState getCurrentColor()
	{
		return CMLib.color().getNormalColor();
	}

	@Override
	public ColorState popMarkedColor()
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
	public void setStatus(final SessionStatus newStatus)
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
	public void setServerTelnetMode(final int telnetCode, final boolean onOff)
	{
	}

	@Override
	public boolean getServerTelnetMode(final int telnetCode)
	{
		return false;
	}

	@Override
	public void setClientTelnetMode(final int telnetCode, final boolean onOff)
	{
	}

	@Override
	public boolean getClientTelnetMode(final int telnetCode)
	{
		return false;
	}

	@Override
	public void changeTelnetMode(final int telnetCode, final boolean onOff)
	{
	}

	@Override
	public void initTelnetMode(final int mobbitmap)
	{
	}

	@Override
	public int getSaveStatIndex()
	{
		return 0;
	}

	@Override
	public String getStat(final String code)
	{
		return null;
	}

	@Override
	public String[] getStatCodes()
	{
		return new String[0];
	}

	@Override
	public boolean isStat(final String code)
	{
		return false;
	}

	@Override
	public void setStat(final String code, final String val)
	{
	}

	@Override
	public long activeTimeMillis()
	{
		return 0;
	}

	@Override
	public boolean isAllowedMcp(final String packageName, final float version)
	{
		return false;
	}

	@Override
	public boolean sendMcpCommand(final String packageCommand, final String parms)
	{
		return false;
	}

	@Override
	public boolean autoLogin(final String name, final String password)
	{
		return false;
	}
}
