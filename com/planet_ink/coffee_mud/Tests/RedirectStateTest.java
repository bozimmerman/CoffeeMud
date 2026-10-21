package com.planet_ink.coffee_mud.Tests;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.SessionPing;
import com.planet_ink.coffee_mud.Common.interfaces.Session.SessionStatus;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.CharCreationLibrary.LoginResult;
import com.planet_ink.coffee_mud.Libraries.interfaces.CharCreationLibrary.LoginSession;
import com.planet_ink.coffee_mud.Libraries.interfaces.ProtocolLibrary.InProto;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.*;
import java.net.*;
import java.util.*;
/*
	Copyright 2026-2026 Bo Zimmerman
	
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
 * Tests that a login pending an MPCP session redirect does not hold the
 * session thread hostage, that socket closure is not treated as success,
 * and that a failed redirect falls back to a normal login.
 */
public class RedirectStateTest extends StdTest
{
	@Override
	public String ID()
	{
		return "RedirectStateTest";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] { "all", "core" };
	}

	private static class RedirectTestSession extends FakeSession
	{
		private SessionStatus	status	= SessionStatus.LOGIN;
		private SessionPing		lastPing=null;
		private Object			lastPingObj=null;

		@Override
		public boolean isRunning()
		{
			// Simulate being called from inside Session.run(): the session
			// thread really is active at this point.
			return true;
		}

		@Override
		public SessionStatus getStatus()
		{
			return status;
		}

		@Override
		public void setStatus(final SessionStatus newStatus)
		{
			status=newStatus;
		}

		@Override
		public void doPing(final SessionPing ping, final Object obj)
		{
			lastPing=ping;
			lastPingObj=obj;
		}

		@Override
		public void setInputLoopTime()
		{
		}

		@Override
		public void setMob(final MOB newmob)
		{
			this.mob=newmob;
		}
	}

	private static class TestCharCreation extends CharCreation
	{
		public boolean callTransferRedirect(final Session session, final MOB mob)
		{
			return transferRedirect(session, mob, null);
		}
	}

	private static class RedirectingLoginSession implements LoginSession
	{
		public int		loginCalls		= 0;
		public boolean	stayRedirecting	= false;

		@Override
		public String login()
		{
			return "RedirectTestMob";
		}

		@Override
		public LoginResult loginSystem(final Session session)
		{
			loginCalls++;
			return stayRedirecting ? LoginResult.INPUT_REQUIRED : LoginResult.NORMAL_LOGIN;
		}

		@Override
		public void logoutLoginSession()
		{
		}

		@Override
		public boolean reset()
		{
			return false;
		}

		@Override
		public boolean skipInputThisTime()
		{
			return true;
		}

		@Override
		public String acceptInput(final Session session)
		{
			return null;
		}
	}

	private static class TestDefaultSession extends DefaultSession
	{
		public void setTestConnection(final Socket socket) throws IOException
		{
			this.sock=socket;
			this.rawin=new BufferedInputStream(socket.getInputStream());
		}

		public void setTestLoginSession(final LoginSession session)
		{
			this.loginSession=session;
		}
	}

	private static String redirectIdFromTransfer(final String transferJson) throws Exception
	{
		return new MiniJSON().parseObject(transferJson).getCheckedString("redirect_id");
	}

	private static void callProcessSwapResult(final String mobname, final String redirectId, final boolean ok) throws Exception
	{
		final MiniJSON.JSONObject payload=new MiniJSON.JSONObject();
		payload.put("swap_result", mobname);
		payload.put("redirect_id", redirectId);
		payload.put("ok", Boolean.valueOf(ok));
		final java.lang.reflect.Method m=CMProtocols.class.getDeclaredMethod("processMPCPSwapResult", MiniJSON.JSONObject.class);
		m.setAccessible(true);
		m.invoke(CMLib.protocol(), payload);
	}

	private static class TestProtocolSession extends TestDefaultSession
	{
		public String lastTransfer=null;

		@Override
		public boolean sendInlineCommand(final InProto protocol, final String packageCommand, final String parms)
		{
			if("Transfer".equalsIgnoreCase(packageCommand))
				lastTransfer=parms;
			return true;
		}
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		final RedirectTestSession session=new RedirectTestSession();
		session.setClientTelnetMode(Session.TELNET_MPCP, true);

		final MOB testMob=CMClass.getMOB("StdMOB");
		testMob.setName("RedirectTestMob");
		final PlayerStats pStats=(PlayerStats)CMClass.getCommon("DefaultPlayerStats");
		pStats.setIMudTransferData(new PlayerStats.IMudTransferData("target.example", 5555, System.currentTimeMillis(), true));
		testMob.setPlayerStats(pStats);

		final long start=System.currentTimeMillis();
		final boolean redirected=new TestCharCreation().callTransferRedirect(session, testMob);
		final long elapsed=System.currentTimeMillis()-start;

		if(!redirected)
			return "Error#1: transferRedirect() returned false";
		if(elapsed>2000)
			return "Error#2: transferRedirect() blocked the session thread for "+elapsed+"ms";
		if(session.lastPing!=SessionPing.SESSIONSWAP)
			return "Error#3: SESSIONSWAP ping was not sent";
		if(!(session.lastPingObj instanceof Pair<?,?>))
			return "Error#4: SESSIONSWAP ping target was missing";
		final Pair<?,?> target=(Pair<?,?>)session.lastPingObj;
		if((!target.first.equals("target.example"))||(!target.second.equals(Integer.valueOf(5555))))
			return "Error#5: SESSIONSWAP ping target was wrong: "+target.first+":"+target.second;
		if(session.getStatus()!=SessionStatus.LOGIN)
			return "Error#6: transferRedirect changed the session status (was "+session.getStatus().name()+")";
		testMob.destroy();

		final RedirectTestSession plainSession=new RedirectTestSession();
		final MOB plainMob=CMClass.getMOB("StdMOB");
		plainMob.setName("RedirectFallbackMob");
		final PlayerStats plainStats=(PlayerStats)CMClass.getCommon("DefaultPlayerStats");
		plainStats.setIMudTransferData(new PlayerStats.IMudTransferData("target.example", 5555, System.currentTimeMillis(), true));
		plainMob.setPlayerStats(plainStats);
		if(new TestCharCreation().callTransferRedirect(plainSession, plainMob))
			return "Error#8: impossible redirect was not declined";
		if(plainMob.playerStats().getIMudTransferData()!=null)
			return "Error#9: declined redirect left the transfer marker in place";
		if(plainSession.getStatus()!=SessionStatus.LOGIN)
			return "Error#10: declined redirect changed the session status";
		plainMob.destroy();

		ServerSocket serverSock=null;
		Socket clientSock=null;
		Socket serverSide=null;
		try
		{
			serverSock=new ServerSocket(0);
			clientSock=new Socket("127.0.0.1", serverSock.getLocalPort());
			serverSide=serverSock.accept();

			final RedirectingLoginSession loginSession=new RedirectingLoginSession();
			loginSession.stayRedirecting=true;
			final TestDefaultSession liveSession=new TestDefaultSession();
			liveSession.setTestConnection(serverSide);
			liveSession.setTestLoginSession(loginSession);
			liveSession.setStatus(SessionStatus.LOGIN);
			liveSession.run();

			if(loginSession.loginCalls==0)
				return "Error#11: redirecting login session was not advanced";
			if(liveSession.getStatus()!=SessionStatus.LOGIN)
				return "Error#12: session left its login state before a result was reported";

			clientSock.close();
			liveSession.run();
			if(liveSession.isStopped())
				return "Error#13: connection closure was treated as redirect success";
			if(liveSession.getStatus()!=SessionStatus.LOGIN)
				return "Error#14: connection closure changed the session state";

			final MOB successMob=CMClass.getMOB("StdMOB");
			successMob.setName("RedirectSuccessMob");
			final PlayerStats successStats=(PlayerStats)CMClass.getCommon("DefaultPlayerStats");
			successStats.setIMudTransferData(new PlayerStats.IMudTransferData("target.example", 5555, System.currentTimeMillis(), true));
			successMob.setPlayerStats(successStats);
			final TestProtocolSession successSession=new TestProtocolSession();
			successSession.setClientTelnetMode(Session.TELNET_MPCP, true);
			successSession.setMob(successMob);
			CMLib.protocol().doMPCPCommand(successSession, SessionPing.SESSIONSWAP,
					new Pair<String,Integer>("target.example", Integer.valueOf(5555)));
			final String successId=redirectIdFromTransfer(successSession.lastTransfer);
			successSession.setMob(null);
			callProcessSwapResult("RedirectSuccessMob", successId, true);
			if(!successSession.isStopped())
				return "Error#15: reported success did not end the redirecting session";
			successMob.destroy();

			final MOB failureMob=CMClass.getMOB("StdMOB");
			failureMob.setName("RedirectFailureMob");
			final PlayerStats failureStats=(PlayerStats)CMClass.getCommon("DefaultPlayerStats");
			failureStats.setIMudTransferData(new PlayerStats.IMudTransferData("target.example", 5555, System.currentTimeMillis(), true));
			failureMob.setPlayerStats(failureStats);
			final TestProtocolSession failureSession=new TestProtocolSession();
			failureSession.setClientTelnetMode(Session.TELNET_MPCP, true);
			failureSession.setMob(failureMob);
			CMLib.protocol().doMPCPCommand(failureSession, SessionPing.SESSIONSWAP,
					new Pair<String,Integer>("target.example", Integer.valueOf(5555)));
			final String failureId=redirectIdFromTransfer(failureSession.lastTransfer);
			failureSession.setMob(null);
			callProcessSwapResult("RedirectFailureMob", failureId, false);
			if(failureSession.isStopped())
				return "Error#17: reported failure disconnected instead of waiting for login fallback";
			if(failureMob.playerStats().getIMudTransferData()!=null)
				return "Error#18: reported failure left the transfer marker in place";
			failureMob.destroy();
		}
		catch(final Exception e)
		{
			return "Error#20: "+e.getMessage();
		}
		finally
		{
			try { if(serverSide!=null) serverSide.close(); } catch(final Exception e) {}
			try { if(clientSock!=null) clientSock.close(); } catch(final Exception e) {}
			try { if(serverSock!=null) serverSock.close(); } catch(final Exception e) {}
		}

		return null;
	}
}
