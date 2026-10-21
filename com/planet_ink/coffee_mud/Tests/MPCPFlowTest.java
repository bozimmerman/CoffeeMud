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
import com.planet_ink.coffee_mud.Libraries.interfaces.ProtocolLibrary.InProto;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.reflect.*;
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
 * Exercises the MPCP inter-mud flows that are reachable in a single process:
 * transfer/offer export packets, offer validation and verdict replies, offer
 * responses, and swap-result reporting and resolution.
 */
public class MPCPFlowTest extends StdTest
{
	@Override
	public String ID()
	{
		return "MPCPFlowTest";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] { "all", "core" };
	}

	private static class CaptureSession extends FakeSession
	{
		final List<String>	transferCmds	= new ArrayList<String>();
		final List<String>	forwardCmds		= new ArrayList<String>();
		final List<String>	otherCmds		= new ArrayList<String>();
		boolean				stopped			= false;

		@Override
		public boolean sendInlineCommand(final InProto protocol, final String packageCommand, final String parms)
		{
			if("Transfer".equalsIgnoreCase(packageCommand))
				transferCmds.add(parms);
			else
			if("Forward".equalsIgnoreCase(packageCommand))
				forwardCmds.add(parms);
			else
				otherCmds.add(packageCommand+":"+parms);
			return true;
		}

		@Override
		public void doPing(final SessionPing ping, final Object obj)
		{
		}

		@Override
		public void stopSession(final boolean disconnect, final boolean t1, final boolean t2, final boolean t3)
		{
			stopped=true;
		}

		@Override
		public boolean isStopped()
		{
			return stopped;
		}
	}

	private static MOB newMob(final String name)
	{
		final MOB M=CMClass.getFactoryMOB();
		M.setName(name);
		final PlayerStats pStats=(PlayerStats)CMClass.getCommon("DefaultPlayerStats");
		pStats.setPassword("pass");
		pStats.setEmail(name.toLowerCase()+"@example.com");
		pStats.setLastDateTime(System.currentTimeMillis());
		M.setPlayerStats(pStats);
		return M;
	}

	private static String b64(final MOB mob)
	{
		return B64Encoder.B64encodeAndCompressString(CMLib.coffeeMaker().getPlayerTransferXML(mob));
	}

	private static MiniJSON.JSONObject parse(final String json) throws Exception
	{
		return new MiniJSON().parseObject(json);
	}

	private static void callProcessSwapResult(final MiniJSON.JSONObject payload) throws Exception
	{
		final Method m=CMProtocols.class.getDeclaredMethod("processMPCPSwapResult", MiniJSON.JSONObject.class);
		m.setAccessible(true);
		m.invoke(CMLib.protocol(), payload);
	}

	private static String callMergeForeignPlayerXML(final String xmlB64, final String host, final int port, final boolean returned) throws Exception
	{
		final Method m=CMProtocols.class.getDeclaredMethod("mergeForeignMPCPPlayerXML", String.class, String.class, int.class, boolean.class);
		m.setAccessible(true);
		return (String)m.invoke(CMLib.protocol(), xmlB64, host, Integer.valueOf(port), Boolean.valueOf(returned));
	}

	private static String redirectIdFromSwapTransfer(final CaptureSession session) throws Exception
	{
		final MiniJSON.JSONObject transfer=parse(session.transferCmds.get(session.transferCmds.size()-1));
		return transfer.getCheckedString("redirect_id");
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		try
		{
			String err;

			err=testOfferExport();
			if(err != null)
				return "offer-export: "+err;

			err=testOfferValidation();
			if(err != null)
				return "offer-validation: "+err;

			err=testOfferResponse();
			if(err != null)
				return "offer-response: "+err;

			err=testTransferExport();
			if(err != null)
				return "transfer-export: "+err;

			err=testReturnMerge();
			if(err != null)
				return "return-merge: "+err;

			err=testOutboundPlayerUpdate();
			if(err != null)
				return "update-out: "+err;

			err=testInboundPlayerUpdate();
			if(err != null)
				return "update-in: "+err;

			err=testSwapResultLateArrival();
			if(err != null)
				return "swap-result-late: "+err;

			err=testSwapResultEmission();
			if(err != null)
				return "swap-result-emit: "+err;
		}
		catch(final Exception e)
		{
			return "exception: "+e.getMessage();
		}
		return null;
	}

	private String testOfferExport() throws Exception
	{
		final CaptureSession session=new CaptureSession();
		session.setClientTelnetMode(Session.TELNET_MPCP, true);
		final MOB offerMob=newMob("OfferExportMob");
		offerMob.setStartRoom(CMClass.getLocale("StdRoom"));
		session.setMob(offerMob);

		CMLib.protocol().doMPCPCommand(session, SessionPing.PLAYERTRANSFER,
				new Pair<String,Integer>("relay.example", Integer.valueOf(6000)));

		if(session.forwardCmds.size()!=1)
			return "expected 1 forward packet, got "+session.forwardCmds.size();
		final MiniJSON.JSONObject doc=parse(session.forwardCmds.get(0));
		final MiniJSON.JSONObject offer=parse(doc.getCheckedString("payload"));
		if(!offer.containsKey("mpcp_offer"))
			return "packet is not an offer";
		if(!offer.containsKey("offer_id"))
			return "offer has no offer_id";
		if(!offer.getCheckedString("mobname").equals("OfferExportMob"))
			return "offer mobname wrong: "+offer.getCheckedString("mobname");
		if(!doc.getCheckedString("target_host").equals("relay.example"))
			return "offer target_host wrong";
		if((doc.getCheckedLong("target_port").longValue())!=6000)
			return "offer target_port wrong";
		if(offer.getCheckedString("mob_xml").length()==0)
			return "offer carried no player xml";
		offerMob.destroy();
		return null;
	}

	private String testOfferValidation() throws Exception
	{
		final CaptureSession targetSession=new CaptureSession();
		targetSession.setClientTelnetMode(Session.TELNET_MPCP, true);
		final MOB incoming=newMob("OfferAcceptMob");

		final MiniJSON.JSONObject offer=new MiniJSON.JSONObject();
		offer.put("mpcp_offer", Boolean.TRUE);
		offer.put("offer_id", "offer-abc");
		offer.put("mobname", "OfferAcceptMob");
		offer.put("origin_room", "");
		offer.put("mob_xml", b64(incoming));

		final MiniJSON.JSONObject forward=new MiniJSON.JSONObject();
		forward.put("source_ip", "home.example");
		forward.put("source_port", Long.valueOf(7000));
		forward.put("payload", offer.toString());

		CMLib.protocol().processMPCPTransfer(targetSession, "forward", forward);

		if(targetSession.forwardCmds.size()!=1)
			return "expected 1 verdict, got "+targetSession.forwardCmds.size();
		final MiniJSON.JSONObject respDoc=parse(targetSession.forwardCmds.get(0));
		final MiniJSON.JSONObject resp=parse(respDoc.getCheckedString("payload"));
		if(!resp.containsKey("mpcp_offer_response"))
			return "reply is not an offer_response";
		if(!resp.getCheckedString("offer_id").equals("offer-abc"))
			return "reply offer_id wrong";
		if(!resp.getCheckedBoolean("ok").booleanValue())
			return "fresh player offer should have been accepted";
		if(!respDoc.getCheckedString("target_host").equals("home.example"))
			return "verdict was not sent back to the source";
		incoming.destroy();

		final CaptureSession targetSession2=new CaptureSession();
		targetSession2.setClientTelnetMode(Session.TELNET_MPCP, true);
		final MiniJSON.JSONObject badOffer=new MiniJSON.JSONObject();
		badOffer.put("mpcp_offer", Boolean.TRUE);
		badOffer.put("offer_id", "offer-bad");
		badOffer.put("mobname", "OfferNoXmlMob");
		badOffer.put("mob_xml", "");
		final MiniJSON.JSONObject forward2=new MiniJSON.JSONObject();
		forward2.put("source_ip", "home.example");
		forward2.put("source_port", Long.valueOf(7000));
		forward2.put("payload", badOffer.toString());
		CMLib.protocol().processMPCPTransfer(targetSession2, "forward", forward2);
		if(targetSession2.forwardCmds.size()!=1)
			return "expected 1 rejection verdict for missing xml";
		final MiniJSON.JSONObject resp2=parse(parse(targetSession2.forwardCmds.get(0)).getCheckedString("payload"));
		if(resp2.getCheckedBoolean("ok").booleanValue())
			return "offer with no xml should have been rejected";
		if(!resp2.containsKey("reason"))
			return "rejection verdict has no reason";
		return null;
	}

	private String testOfferResponse() throws Exception
	{
		final CaptureSession session=new CaptureSession();
		session.setClientTelnetMode(Session.TELNET_MPCP, true);
		final MOB offerMob=newMob("OfferRespMob");
		offerMob.setStartRoom(CMClass.getLocale("StdRoom"));
		session.setMob(offerMob);

		CMLib.protocol().doMPCPCommand(session, SessionPing.PLAYERTRANSFER,
				new Pair<String,Integer>("relay.example", Integer.valueOf(6000)));
		if(session.forwardCmds.size()!=1)
			return "no offer was emitted";
		final String offerId=parse(parse(session.forwardCmds.get(0)).getCheckedString("payload")).getCheckedString("offer_id");

		final MiniJSON.JSONObject accept=new MiniJSON.JSONObject();
		accept.put("mpcp_offer_response", Boolean.TRUE);
		accept.put("offer_id", offerId);
		accept.put("mobname", "OfferRespMob");
		accept.put("ok", Boolean.TRUE);
		final MiniJSON.JSONObject acceptForward=new MiniJSON.JSONObject();
		acceptForward.put("payload", accept.toString());
		CMLib.protocol().processMPCPTransfer(session, "forward", acceptForward);

		if(session.transferCmds.size()!=1)
			return "accepted offer did not trigger a transfer, got "+session.transferCmds.size();
		final MiniJSON.JSONObject transfer=parse(session.transferCmds.get(0));
		if(!transfer.getCheckedString("task").equalsIgnoreCase("transfer"))
			return "accepted offer transfer had wrong task: "+transfer.getCheckedString("task");
		if(!transfer.getCheckedString("mobname").equals("OfferRespMob"))
			return "accepted offer transfer mobname wrong";
		final PlayerStats.IMudTransferData marker=offerMob.playerStats().getIMudTransferData();
		if((marker==null)||(!marker.isLocal)||(!marker.host.equals("relay.example"))||(marker.port!=6000))
			return "accepted offer did not set the outbound transfer marker";
		offerMob.destroy();

		final CaptureSession session2=new CaptureSession();
		session2.setClientTelnetMode(Session.TELNET_MPCP, true);
		final MOB offerMob2=newMob("OfferRejectMob");
		offerMob2.setStartRoom(CMClass.getLocale("StdRoom"));
		session2.setMob(offerMob2);
		CMLib.protocol().doMPCPCommand(session2, SessionPing.PLAYERTRANSFER,
				new Pair<String,Integer>("relay.example", Integer.valueOf(6000)));
		final String offerId2=parse(parse(session2.forwardCmds.get(0)).getCheckedString("payload")).getCheckedString("offer_id");
		final MiniJSON.JSONObject reject=new MiniJSON.JSONObject();
		reject.put("mpcp_offer_response", Boolean.TRUE);
		reject.put("offer_id", offerId2);
		reject.put("mobname", "OfferRejectMob");
		reject.put("ok", Boolean.FALSE);
		reject.put("reason", "nope");
		final MiniJSON.JSONObject rejectForward=new MiniJSON.JSONObject();
		rejectForward.put("payload", reject.toString());
		CMLib.protocol().processMPCPTransfer(session2, "forward", rejectForward);
		if(session2.transferCmds.size()!=0)
			return "rejected offer should not produce a transfer";
		if(offerMob2.playerStats().getIMudTransferData()!=null)
			return "rejected offer should not leave a marker";
		offerMob2.destroy();
		return null;
	}

	private String testTransferExport() throws Exception
	{
		final CaptureSession session=new CaptureSession();
		session.setClientTelnetMode(Session.TELNET_MPCP, true);
		final MOB returnMob=newMob("ReturnExportMob");
		returnMob.setStartRoom(CMClass.getLocale("StdRoom"));
		returnMob.playerStats().setIMudTransferData(
			new PlayerStats.IMudTransferData("relay.example", 6000, System.currentTimeMillis(), false));
		session.setMob(returnMob);

		CMLib.protocol().doMPCPCommand(session, SessionPing.PLAYERTRANSFER,
				new Pair<String,Integer>("home.example", Integer.valueOf(7000)));

		if(session.transferCmds.size()!=1)
			return "foreign mob did not trigger a return transfer, got "+session.transferCmds.size();
		final MiniJSON.JSONObject transfer=parse(session.transferCmds.get(0));
		if(!transfer.getCheckedString("task").equalsIgnoreCase("return"))
			return "foreign mob export should use task=return, got "+transfer.getCheckedString("task");
		if(!transfer.getCheckedString("mobname").equals("ReturnExportMob"))
			return "return transfer mobname wrong";
		if((transfer.getCheckedLong("target_port").longValue())!=7000)
			return "return transfer target_port wrong";
		returnMob.destroy();
		return null;
	}

	private String testReturnMerge() throws Exception
	{
		final String name=CMStrings.capitalizeAndLower("MpcpReturnTestMob");
		try
		{
			final MOB local=CMClass.getMOB("StdMOB");
			local.setName(name);
			final PlayerStats lStats=(PlayerStats)CMClass.getCommon("DefaultPlayerStats");
			lStats.setPassword("origpassword");
			lStats.setEmail(name.toLowerCase()+"@local.example");
			lStats.setLastDateTime(System.currentTimeMillis()-60000);
			lStats.setIMudTransferData(
				new PlayerStats.IMudTransferData("relay.example", 6000, System.currentTimeMillis(), true));
			local.setPlayerStats(lStats);
			CMLib.players().addPlayer(local);
			CMLib.players().savePlayer(local);
			final String origPass=lStats.getPasswordStr();

			if(!CMLib.players().playerExists(name))
				return "test setup: local player was not registered (exists="+CMLib.players().playerExists(name)+")";
			if(CMLib.players().getLoadPlayer(name)==null)
				return "test setup: local player could not be loaded back";

			final MOB incoming=newMob(name);
			incoming.playerStats().setIMudTransferData(null);
			final String err=callMergeForeignPlayerXML(b64(incoming), "relay.example", 6000, true);
			incoming.destroy();
			if(err.length()>0)
				return "return merge reported an error: "+err;

			final MOB merged=CMLib.players().getLoadPlayer(name);
			if(merged==null)
				return "local player was lost during the return merge";
			if(merged.playerStats().getIMudTransferData()!=null)
				return "return merge did not clear the local transfer marker";
			if(!merged.playerStats().getEmail().equals(name.toLowerCase()+"@local.example"))
				return "return merge did not preserve the local account data";
			if(!merged.playerStats().getPasswordStr().equals(origPass))
				return "return merge did not preserve the local password";
			merged.destroy();
		}
		finally
		{
			try { CMLib.players().delPlayer(CMLib.players().getPlayer(name)); } catch(final Exception e) {}
			try { CMLib.database().DBDeletePlayerOnly(name); } catch(final Exception e) {}
		}
		return null;
	}

	private String testOutboundPlayerUpdate() throws Exception
	{
		final CaptureSession session=new CaptureSession();
		session.setClientTelnetMode(Session.TELNET_MPCP, true);
		final MOB remoteMob=newMob("RemoteUpdateMob");
		remoteMob.playerStats().setIMudTransferData(
			new PlayerStats.IMudTransferData("home.example", 7000, System.currentTimeMillis(), false));
		session.setMob(remoteMob);

		if(!CMLib.protocol().sendMPCPPlayerUpdate(remoteMob, session))
			return "update was not sent for a foreign player";

		if(session.forwardCmds.size()!=1)
			return "expected 1 forward update packet, got "+session.forwardCmds.size();
		final MiniJSON.JSONObject doc=parse(session.forwardCmds.get(0));
		if(!doc.getCheckedString("target_host").equals("home.example"))
			return "update was sent to the wrong host";
		if((doc.getCheckedLong("target_port").longValue())!=7000)
			return "update was sent to the wrong port";
		if(doc.getCheckedString("payload").length()==0)
			return "update carried no payload";

		final CaptureSession plainSession=new CaptureSession();
		final MOB localMob=newMob("LocalUpdateMob");
		if(CMLib.protocol().sendMPCPPlayerUpdate(localMob, plainSession))
			return "update should not have been sent for a local player";
		localMob.destroy();
		remoteMob.destroy();
		return null;
	}

	private String testInboundPlayerUpdate() throws Exception
	{
		final String name=CMStrings.capitalizeAndLower("MpcpUpdateTestMob");
		try
		{
			final MOB local=CMClass.getMOB("StdMOB");
			local.setName(name);
			final PlayerStats lStats=(PlayerStats)CMClass.getCommon("DefaultPlayerStats");
			lStats.setPassword("origpassword");
			lStats.setEmail(name.toLowerCase()+"@local.example");
			lStats.setLastDateTime(System.currentTimeMillis()-60000);
			local.setPlayerStats(lStats);
			CMLib.players().addPlayer(local);
			CMLib.players().savePlayer(local);

			final MOB incoming=newMob(name);
			incoming.playerStats().setEmail(name.toLowerCase()+"@remote.example");
			incoming.playerStats().setLastDateTime(System.currentTimeMillis());
			incoming.playerStats().setIMudTransferData(null);
			final String xml=b64(incoming);
			incoming.destroy();

			final CaptureSession session=new CaptureSession();
			final MiniJSON.JSONObject forward=new MiniJSON.JSONObject();
			forward.put("source_ip", "relay.example");
			forward.put("source_port", Long.valueOf(6000));
			forward.put("payload", xml);
			CMLib.protocol().processMPCPTransfer(session, "forward", forward);

			final MOB merged=CMLib.players().getLoadPlayer(name);
			if(merged==null)
				return "local player was lost during the inbound update";
			final PlayerStats.IMudTransferData marker=merged.playerStats().getIMudTransferData();
			if(marker==null)
				return "inbound update did not set a transfer marker";
			if(!marker.isLocal)
				return "inbound update marker should mark the player as local to this mud";
			if((!marker.host.equals("relay.example"))||(marker.port!=6000))
				return "inbound update marker points at the wrong mud";
			if(!merged.playerStats().getEmail().equals(name.toLowerCase()+"@local.example"))
				return "inbound update did not preserve the local account data";

			CMLib.players().delPlayer(merged);
			CMLib.database().DBDeletePlayerOnly(name);

			final MOB staleLocal=CMClass.getMOB("StdMOB");
			staleLocal.setName(name);
			final PlayerStats sStats=(PlayerStats)CMClass.getCommon("DefaultPlayerStats");
			sStats.setPassword("origpassword");
			sStats.setEmail(name.toLowerCase()+"@local.example");
			sStats.setLastDateTime(System.currentTimeMillis());
			staleLocal.setPlayerStats(sStats);
			CMLib.players().addPlayer(staleLocal);
			CMLib.players().savePlayer(staleLocal);

			final MOB staleIncoming=newMob(name);
			staleIncoming.playerStats().setEmail(name.toLowerCase()+"@stale.example");
			staleIncoming.playerStats().setLastDateTime(System.currentTimeMillis()-120000);
			staleIncoming.playerStats().setIMudTransferData(null);
			final String staleXml=b64(staleIncoming);
			staleIncoming.destroy();

			final CaptureSession session2=new CaptureSession();
			final MiniJSON.JSONObject staleForward=new MiniJSON.JSONObject();
			staleForward.put("source_ip", "relay.example");
			staleForward.put("source_port", Long.valueOf(6000));
			staleForward.put("payload", staleXml);
			CMLib.protocol().processMPCPTransfer(session2, "forward", staleForward);

			final MOB notStale=CMLib.players().getLoadPlayer(name);
			if(notStale==null)
				return "local player was lost during the stale update";
			if(!notStale.playerStats().getEmail().equals(name.toLowerCase()+"@local.example"))
				return "inbound update should have ignored the older remote data";
			if(notStale.playerStats().getIMudTransferData()!=null)
				return "ignored stale update should not have set a marker";
			notStale.destroy();
		}
		finally
		{
			try { CMLib.players().delPlayer(CMLib.players().getPlayer(name)); } catch(final Exception e) {}
			try { CMLib.database().DBDeletePlayerOnly(name); } catch(final Exception e) {}
		}
		return null;
	}

	private String testSwapResultLateArrival() throws Exception
	{
		final CaptureSession session=new CaptureSession();
		session.setClientTelnetMode(Session.TELNET_MPCP, true);
		final MOB swapMob=newMob("LateSwapMob");
		swapMob.playerStats().setIMudTransferData(
			new PlayerStats.IMudTransferData("home.example", 7000, System.currentTimeMillis(), true));
		session.setMob(swapMob);

		CMLib.protocol().doMPCPCommand(session, SessionPing.SESSIONSWAP,
				new Pair<String,Integer>("home.example", Integer.valueOf(7000)));
		final String redirectId=redirectIdFromSwapTransfer(session);
		session.setMob(null);

		@SuppressWarnings("unchecked")
		final Map<Session,Object> swaps=(Map<Session,Object>)getPrivateField(CMLib.protocol(), "mpcpSessionSwaps");
		if(swaps==null)
			return "could not reach mpcpSessionSwaps";
		synchronized(swaps)
		{
			if(!swaps.containsKey(session))
				return "swap state was not recorded for the session";
		}

		final MiniJSON.JSONObject result=new MiniJSON.JSONObject();
		result.put("swap_result", "LateSwapMob");
		result.put("redirect_id", redirectId);
		result.put("ok", Boolean.TRUE);
		callProcessSwapResult(result);

		if(!session.isStopped())
			return "a reported late success should still end the source session";
		swapMob.destroy();
		return null;
	}

	private String testSwapResultEmission() throws Exception
	{
		final Session swapSession=new CaptureSession();
		final MOB swapMob=newMob("EmitSwapMob");
		swapMob.setSession(swapSession);
		((CaptureSession)swapSession).setMob(swapMob);
		((CaptureSession)swapSession).setClientTelnetMode(Session.TELNET_MPCP, true);

		@SuppressWarnings("unchecked")
		final Map<Session,Object> status=(Map<Session,Object>)getPrivateField(CMLib.protocol(), "mpcpSwapInfos");
		if(status==null)
			return "could not reach mpcpSwapInfos";
		final MiniJSON.JSONObject recorded=new MiniJSON.JSONObject();
		recorded.put("mobname", "EmitSwapMob");
		recorded.put("source_ip", "home.example");
		recorded.put("source_port", Long.valueOf(7000));
		recorded.put("redirect_id", "redir-1");
		synchronized(status)
		{
			status.put(swapSession, recorded);
		}

		CMLib.protocol().sendMPCPSwapResult(swapSession, false, "test reason");
		if(((CaptureSession)swapSession).forwardCmds.size()!=1)
			return "expected 1 swap result packet, got "+((CaptureSession)swapSession).forwardCmds.size();
		final MiniJSON.JSONObject doc=parse(((CaptureSession)swapSession).forwardCmds.get(0));
		final MiniJSON.JSONObject payload=parse(doc.getCheckedString("payload"));
		if(!payload.getCheckedString("swap_result").equals("EmitSwapMob"))
			return "swap result name wrong";
		if(payload.getCheckedBoolean("ok").booleanValue())
			return "swap result reported success for a failure";
		if(!payload.getCheckedString("reason").equals("test reason"))
			return "swap result reason wrong";
		if(!doc.getCheckedString("target_host").equals("home.example"))
			return "swap result sent to wrong host";
		swapMob.destroy();
		swapSession.setMob(null);
		return null;
	}

	private static Object getPrivateField(final Object target, final String field) throws Exception
	{
		Class<?> c=target.getClass();
		while(c != null)
		{
			try
			{
				final Field f=c.getDeclaredField(field);
				f.setAccessible(true);
				return f.get(target);
			}
			catch(final NoSuchFieldException e)
			{
				c=c.getSuperclass();
			}
		}
		return null;
	}
}
