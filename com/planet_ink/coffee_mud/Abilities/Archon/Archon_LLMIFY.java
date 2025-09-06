package com.planet_ink.coffee_mud.Abilities.Archon;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.SessionStatus;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.CharCreationLibrary.LoginResult;
import com.planet_ink.coffee_mud.Libraries.interfaces.ProtocolLibrary.LLMSession;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;

/*
   Copyright 2017-2025 Bo Zimmerman

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
public class Archon_LLMIFY extends ArchonSkill
{
	@Override
	public String ID()
	{
		return "Archon_LLMIFY";
	}

	private final static String localizedName = CMLib.lang().L("LLMify");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	private static final String[] triggerStrings = I(new String[] { "LLMIFY" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_ARCHON;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(1);
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	public class LLMSocket extends Socket
	{
		private final PipedInputStream inputStream = new PipedInputStream()
		{
			public void maybeSubmit() throws IOException
			{
				if((mob==null)||(mob.amDead()))
				{
					close();
					return;
				}
				if((outputBuffer.size()==0)||(!activated)||(expire<0)||(System.currentTimeMillis()<expire)||thinking)
					return; // Skip empty flushes
				expire = -1;
				thinking=true;
				CMLib.threads().executeRunnable(new Runnable() {
					@Override
					public void run()
					{
						try
						{
							final String mudOutput = outputBuffer.toString();
							outputBuffer.reset();
							expire = -1;
							log("MUD Output: " + mudOutput);
							final String command = llmSession.chat(mudOutput);
							log("LLM Response: " + command);
							inputWriter.write((command + "\r\n").getBytes(StandardCharsets.UTF_8)); // Add CRLF for typical MUD input
							inputWriter.flush();
						}
						catch (final IOException e)
						{
						}
						finally
						{
							thinking=false;
						}
					}
				});
			}

			@Override
			public synchronized int available() throws IOException
			{
				maybeSubmit();
				return super.available();
			}

			@Override
			public synchronized int read() throws IOException
			{
				final int x= super.read();
				maybeSubmit();
				return x;
			}

			@Override
			public synchronized int read(final byte[] b, final int off, final int len) throws IOException
			{
				final int x = super.read(b, off, len);
				maybeSubmit();
				return x;
			}
		};
		private final PipedOutputStream inputWriter = new PipedOutputStream(inputStream);
		private final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
		private final LLMSession llmSession;
		private boolean activated = false;
		private boolean closed = false;
		private MOB mob = null;
		private Session sess = null;
		private boolean thinking=false;
		private volatile long expire = -1;

		public LLMSocket(final LLMSession llmSession) throws IOException {
			this.llmSession = llmSession;
		}

		@Override
		public InetAddress getInetAddress()
		{
			return InetAddress.getLoopbackAddress();
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return inputStream;
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return new OutputStream() {
				@Override
				public void write(final int b) throws IOException {
					if(activated)
					{
						outputBuffer.write(b);
						expire=System.currentTimeMillis()+1000;
					}
				}

				@Override
				public void write(final byte[] b) throws IOException {
					if(activated)
					{
						outputBuffer.write(b);
						expire=System.currentTimeMillis()+1000;
					}
				}

				@Override
				public void write(final byte[] b, final int off, final int len) throws IOException {
					if(activated)
					{
						outputBuffer.write(b, off, len);
						expire=System.currentTimeMillis()+1000;
					}
				}

				@Override
				public void flush() throws IOException
				{
					super.flush();
				}

				@Override
				public void close() throws IOException {
					inputWriter.close();
				}
			};
		}

		private void log(final String msgStr)
		{
			if(mob != null)
				Log.sysOut("LLMIFY", mob.Name() + ": " + msgStr);
		}

		// Stub overrides for basic Socket behavior (expand as needed)
		@Override
		public boolean isConnected() {
			return true;
		}

		public void activate(final MOB mob)
		{
			if (!activated)
			{
				activated=true;
				this.mob=mob;
				this.sess=mob.session();
			}
		}

		@Override
		public boolean isClosed()
		{
			return closed;
		}

		@Override
		public synchronized void close() throws IOException
		{
			if(!closed)
			{
				inputStream.close();
				inputWriter.close();
				outputBuffer.close();
				closed=true;
				if(sess != null)
				{
					sess.setMob(null);
					mob.setSession(null);
					sess.stopSession(false, false, true, true);
					log("LLM session closed.");
				}
			}
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell(L("Infect whom with what LLM prompt?"));
			return false;
		}
		final List<String> whom=new XVector<String>(commands.remove(0));
		MOB target=CMLib.players().getPlayerAllHosts(whom.get(0));
		if((target==null)||(target.isMonster())||(!target.isPlayer())||(!CMLib.flags().isInTheGame(target, true)))
			target=getTargetAnywhere(mob,whom,givenTarget,false,true,true);
		if(target==null)
			return false;
		if(target.isPlayer())
		{
			mob.tell(L("You cannot LLMify @x1.",target.name(mob)));
			return false;
		}
		final Session sess = target.session();
		if(sess!=null)
		{
			if(target.soulMate()!=null)
			{
				mob.tell(L("You cannot LLMify @x1.",target.name(mob)));
				return false;
			}
			sess.setMob(null);
			target.setSession(null);
			mob.tell(L("@x1 is losing the LLM.",target.name(mob)));
			sess.stopSession(false, false, true, true);
			return true;
		}

		final String prompt = CMParms.combine(commands);

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success && CMLib.protocol().isLLMInstalled())
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),auto?L("An AI cloud surrounds <T-NAME>!"):L("^F<S-NAME> infect(s) <T-NAMESELF> with an LLM.^?"));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final LLMSession llmSession = CMLib.protocol().createLLMSession(prompt, Integer.valueOf(128));
				if (llmSession == null)
				{
					mob.tell(L("The LLM service is not available."));
					return false;
				}
				try
				{
					final LLMSocket sock = new LLMSocket(llmSession);
					final Session s = (Session)CMClass.getCommon("DefaultSession");
					s.setMob(target);
					target.setSession(s);
					CMLib.threads().executeRunnable(new Runnable()
					{
						@Override
						public void run()
						{
							s.setIdleTimers();
							s.initializeSession(sock, Thread.currentThread().getThreadGroup().getName(), "");
							s.setIdleTimers();
						}
					});
					s.setStatus(SessionStatus.MAINLOOP);
					CMLib.s_sleep(100);
					s.setIdleTimers();
					s.setStatus(SessionStatus.MAINLOOP);
					s.setClientTelnetMode(Session.TELNET_ANSI, false);
					sock.activate(target);
					CMLib.sessions().add(s);
					s.setIdleTimers();
					target.setAttribute(Attrib.AUTOEXITS, true);
					target.setAttribute(Attrib.BRIEF, true);
					target.enqueCommand(new XVector<String>("LOOK"),0, 0);
				}
				catch(final IOException e)
				{
					mob.tell(L("The LLM service is not available."));
					return false;
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to infect <T-NAMESELF> with an AI, but fail(s)."));
		return success;
	}
}
