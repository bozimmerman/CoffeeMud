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
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

/*
   Copyright 2019-2020 Bo Zimmerman

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
public class Archon_CRecord extends ArchonSkill
{
	@Override
	public String ID()
	{
		return "Archon_CRecord";
	}

	private final static String localizedName = CMLib.lang().L("Combat Record");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
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

	private static final String[] triggerStrings = I(new String[] { "CRECORD" });

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

	protected boolean	isRecorder	= false;
	protected long		triggerTime	= 0;
	protected int		minLevel	= 10;
	protected String	myArchon	= "";
	protected String	recordingDir= DEFAULT_RECORDING_DIR;
	protected String	lastStr		= "";

	protected final List<String>	myPlayers	= new ArrayList<String>();
	protected volatile long			lastWrite	= System.currentTimeMillis();
	protected final StringBuffer	buffer		= new StringBuffer();

	protected final static String	DEFAULT_RECORDING_DIR = "::/resources/clogs/";
	protected final static long		FLUSH_THRESHOLD	= 65536;
	protected final static long		CANCEL_THRESHOLD= 65536 * 1024;

	private static final SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMdd.HHmm.ss");

	@Override
	public boolean isAutoInvoked()
	{
		return true;//!isRecorder;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
		{
			if(mob.session()==null)
				mob.setSession(null);
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(isRecorder)
			checkFlush();
		return true;
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		if(newMiscText.length()==0)
		{
			//Log.errOut("Unable to start CRecording: "+newMiscText);
		}
		else
		{
			isRecorder = CMParms.getParmBool(newMiscText, "ISRECORDER", false);
			triggerTime = CMParms.getParmLong(newMiscText, "TRIGGERTIME", 0);
			if(!isRecorder)
			{
				final String playerList = CMParms.getParmStr(newMiscText, "PLAYERS", "");
				recordingDir = CMParms.getParmStr(newMiscText, "FILENAME", "::/resources/crecordings/");
				minLevel = CMParms.getParmInt(newMiscText, "MINLEVEL", 10);
				myPlayers.clear();
				myPlayers.addAll(CMParms.parseSpaces(playerList, true));
				if(triggerTime > 0)
					CMLib.map().addGlobalHandler(this, CMMsg.TYP_LEVEL);
			}
			else
			{
				recordingDir = CMParms.getParmStr(newMiscText, "FILENAME", "");
				myArchon = CMParms.getParmStr(newMiscText, "BY", "");
				minLevel = CMParms.getParmInt(newMiscText, "MINLEVEL", 0);
				if((recordingDir.length()==0)
				||(triggerTime==0)
				||(myArchon.length()==0)
				||(minLevel==0))
				{
					Log.errOut("Unable to start CRecording: "+newMiscText);
					flushBuffer();
					affected.delEffect(this);
				}
				else
					isRecorder=true;
			}
		}
	}

	@Override
	public String text()
	{
		final StringBuilder str=new StringBuilder("");
		if(affected != null || isSavable())
		{
			str.append("FILENAME=\""+recordingDir+"\" ISRECORDER="+isRecorder+" TRIGGERTIME="+triggerTime+" MINLEVEL="+minLevel);
			if(isRecorder)
				str.append(" BY="+myArchon);
			else
				str.append(" PLAYERS=\""+CMParms.combine(myPlayers)+"\"");
		}
		return str.toString();
	}

	protected void startRecording(final MOB  mob, final MOB targetM)
	{
		final String dir = (DEFAULT_RECORDING_DIR.endsWith("/")?DEFAULT_RECORDING_DIR:(DEFAULT_RECORDING_DIR+"/"));
		final CMFile F=new CMFile(dir,null,0);
		if(!F.exists())
			F.mkdir();
		final String filename=dir+targetM.Name()+dateFormat.format(Long.valueOf(System.currentTimeMillis()))+".log";
		final CMFile file=new CMFile(filename,null,CMFile.FLAG_LOGERRORS);
		if(!file.canWrite())
		{
			mob.tell("Failed to start c-recording "+targetM.name()+": "+filename);
			Log.sysOut("Failed to start c-recording "+targetM.name()+": "+filename);
		}
		else
		{
			Log.sysOut("C-recording started on "+targetM.name()+": "+filename);
			mob.tell("C-recording started on "+targetM.name()+": "+filename);
			final Archon_CRecord A2=(Archon_CRecord)copyOf();
			A2.setSavable(true);
			targetM.addNonUninvokableEffect(A2);
			A2.setMiscText("FILENAME=\""+filename+"\" ISRECORDER=TRUE TRIGGERTIME="+triggerTime+" MINLEVEL="+minLevel+" BY="+mob.Name());
			final Archon_CRecord iA=(Archon_CRecord)mob.fetchEffect(ID());
			if((iA!=null)&&(!iA.myPlayers.contains(targetM.Name())))
				iA.myPlayers.add(targetM.Name());
			if(!myPlayers.contains(targetM.Name()))
				myPlayers.add(targetM.Name());
			final MOB M=targetM;
			final Session sess=M.session();
			addToBuffer("--------------- start ----------------\n\r");
			if(sess != null)
				addToBuffer("Port: "+sess.getGroupName()+"\n\r");
			addToBuffer(CMStrings.removeColors(CMLib.commands().getScore(M).toString()));
			addToBuffer("--------------- end ----------------\n\r");
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;

			if((msg.sourceMinor()==CMMsg.TYP_LEVEL)
			&&(!msg.source().isMonster())
			&&(msg.source().isPlayer())
			&&((isRecorder) || (triggerTime > 0))
			&&(msg.source().basePhyStats().level()>=minLevel))
			{
				final PlayerStats pStats=msg.source().playerStats();
				final long now=System.currentTimeMillis();
				final int curLevel = msg.source().basePhyStats().level();
				final long time = now-pStats.leveledDateTime(curLevel);
				if((time>1000)
				&&(time<triggerTime))
				{
					final Archon_CRecord A=(Archon_CRecord)msg.source().fetchEffect(ID());
					if((A==null)
					&&(!isRecorder))
						startRecording(mob,msg.source());
				}
				else
				if(isRecorder)
				{
					if(msg.source() == mob)
					{
						this.flushBuffer();
						if(myArchon.length()>0)
						{
							MOB arcM=CMLib.players().getLoadPlayer(myArchon);
							if(arcM==null)
								arcM=CMLib.players().getPlayerAllHosts(myArchon);
							if(arcM!=null)
							{
								final Archon_CRecord cA=(Archon_CRecord)arcM.fetchEffect(ID());
								if(cA!=null)
								{
									arcM.tell("C-recording ended: "+msg.source().name());
									Log.sysOut("C-recording ended: "+msg.source().name());
									cA.myPlayers.remove(msg.source().Name());

								}
								final Archon_CRecord aA=(Archon_CRecord)arcM.fetchAbility(ID());
								if(aA!=null)
									aA.myPlayers.remove(msg.source().Name());
							}
						}
						msg.source().delEffect(this);
					}
				}
				else
				if(myPlayers.contains(msg.source().Name()))
				{
					mob.tell("C-recording ended: "+msg.source().name());
					Log.sysOut("C-recording ended: "+msg.source().name());
					final Archon_CRecord iA=(Archon_CRecord)mob.fetchEffect(ID());
					if(iA!=null)
						iA.myPlayers.remove(msg.source().Name());
					final Archon_CRecord aA=(Archon_CRecord)mob.fetchAbility(ID());
					if(aA!=null)
						aA.myPlayers.remove(msg.source().Name());
					final Archon_CRecord hA=(Archon_CRecord)msg.source().fetchEffect(ID());
					if(hA!=null)
						msg.source().delEffect(hA);
					final Archon_CRecord aaA=(Archon_CRecord)msg.source().fetchAbility(ID());
					if(aaA!=null)
						aaA.myPlayers.remove(msg.source().Name());
				}
			}
			else
			if(isRecorder)
			{
				if((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
				&&(msg.source()==mob))
				{
					addToBuffer(dateFormat.format(Long.valueOf(System.currentTimeMillis()))
							+" XP:"
							+" Target("+((msg.target()==null)?"":msg.target().name())+") "
							+" Level("+(mob.phyStats().level())+") "
							+" Amt("+(msg.value())+") "
							+"\n\r");
				}
				else
				if((CMath.bset(msg.sourceMajor(), CMMsg.MASK_MALICIOUS))
				||(CMath.bset(msg.targetMajor(), CMMsg.MASK_MALICIOUS))
				||(mob.isInCombat()))
				{
					if((msg.othersMessage()!=null)
					&&(msg.othersMessage().length()>0)
					&&(!CMath.isBool(msg.othersMessage()))
					&&(msg.sourceMinor()!=CMMsg.TYP_FACTIONCHANGE))
					{
						final String noColor = CMStrings.removeColors(msg.othersMessage()).trim();
						addToBuffer(dateFormat.format(Long.valueOf(System.currentTimeMillis()))
								+" "
								+CMStrings.removeColors(CMLib.coffeeFilter().fullOutFilter(null, mob, msg.source(), msg.target(), msg.tool(), noColor, false))
								+"\n\r");
					}
				}
			}
		}
	}

	protected void checkFlush()
	{
		if((buffer.length()>FLUSH_THRESHOLD)
		||((System.currentTimeMillis()-this.lastWrite)>(60 * 10 * 1000)))
		{
			flushBuffer();
			if(affected instanceof MOB)
			{
				final MOB M=(MOB)affected;
				final Session sess=M.session();
				buffer.append("--------------- start spot check ----------------\n\r");
				if(sess != null)
					buffer.append("Port: "+sess.getGroupName()+"\n\r");
				buffer.append(CMStrings.removeColors(CMLib.commands().getScore(M).toString()));
				buffer.append("--------------- end spot check ----------------\n\r");
			}
			this.lastWrite=System.currentTimeMillis();
		}
	}

	protected void addToBuffer(final String s)
	{
		if((s!=null)
		&&(s.length()>0)
		&&(!s.equals(lastStr)))
		{
			lastStr=s;
			final String line=CMStrings.removeColors(s).trim();
			if(line.length()>0)
			{
				synchronized(buffer)
				{
					buffer.append(line).append("\n\r");
				}
				checkFlush();
				this.lastWrite=System.currentTimeMillis();
			}
		}
	}

	protected void flushBuffer()
	{
		if(recordingDir.equals(DEFAULT_RECORDING_DIR))
		{
			buffer.setLength(0);
			lastWrite=System.currentTimeMillis();
			return;
		}
		final StringBuffer buf=new StringBuffer("");
		if(buffer.length() >0)
		{
			synchronized(buffer)
			{
				if(buffer.length() >0)
				{
					buf.append(buffer);
					buffer.setLength(0);
				}
			}
		}
		if(buf.length()>0)
		{
			final CMFile F=new CMFile(recordingDir, null, 0);
			if(F.canWrite())
			{
				F.saveText(buf,true);
				if(F.length() >= CANCEL_THRESHOLD)
				{
					final String name = (affected != null)?affected.Name():"?";
					Log.errOut("Cancelling CRecord due to size. @"+name);
					if(affected != null)
						affected.delEffect(this);
					isRecorder=false;
				}
			}
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final String args=CMParms.combine(commands,0);
		if((mob.isPlayer() && mob.isMine(this) && (CMLib.ableMapper().qualifiesByAnyCharClass(ID()))))
			super.setSavable(true);

		final Archon_CRecord A=(Archon_CRecord)mob.fetchEffect(ID());
		if(A==null)
		{
			mob.tell(L("CRecord is uninvoked.  All information lost/unavailable."));
			return false;
		}
		final String fw=(commands.size()==0)?"":commands.get(0).toUpperCase();
		if((args.length()==0)||(commands.size()==0)||("LIST".startsWith(commands.get(0).toUpperCase())))
		{
			final StringBuilder str=new StringBuilder();
			if(A.triggerTime>0)
			{
				str.append(L("CRecord level listener is ACTIVE.\n\r"));
				str.append(L("Trigger Time: @x1\n\r",CMLib.time().date2BestShortEllapsedTime(A.triggerTime)));
				str.append(L("File path   : @x1\n\r",A.recordingDir));
				str.append(L("Min Level   : @x1\n\r",""+A.minLevel));
			}
			else
				str.append(L("CRecord level listener is INACTIVE.\n\r"));
			for(final Iterator<String> i=A.myPlayers.iterator();i.hasNext();)
			{
				final String name = i.next();
				final MOB M=CMLib.players().getPlayerAllHosts(name);
				if((M!=null)
				&&(M.fetchEffect(ID())==null))
					i.remove();
			}
			str.append(L("Recording   : @x1\n\r",CMLib.utensils().niceCommaList(A.myPlayers, true)));
			mob.tell(str.toString());
			return false;
		}
		else
		if("MINLEVEL".startsWith(fw))
		{
			if((commands.size()<2)||(!CMath.isInteger(commands.get(1))))
				mob.tell(L("You need to specify a number of levels."));
			else
			{
				A.minLevel=CMath.s_int(commands.get(1));
				this.minLevel=CMath.s_int(commands.get(1));
				mob.tell(L("CRecord minimum level is now @x1.",""+A.minLevel));
			}
			return false;
		}
		else
		if("ADD".startsWith(fw))
		{
			if(commands.size()<2)
				mob.tell(L("You need to specify a player name."));
			else
			{
				MOB M=CMLib.players().getPlayerAllHosts(commands.get(1));
				if(M==null)
					M=CMLib.players().getLoadPlayer(commands.get(1));
				if(M==null)
					mob.tell(L("Unknown player @x1",commands.get(1)));
				else
				if(M.fetchEffect(ID())!=null)
					mob.tell(L("Player @x1 is already being recorded.",commands.get(1)));
				else
					A.startRecording(mob,M);
			}
			return false;
		}
		else
		if(fw.equals("STOP"))
		{
			if(A.triggerTime==0)
				mob.tell(L("CRecord level listener was already stopped."));
			else
			{
				A.triggerTime=0;
				this.triggerTime=0;
				CMLib.map().delGlobalHandler(A, CMMsg.TYP_LEVEL);
				mob.tell(L("CRecord level listener is now stopped."));
			}
			return false;
		}
		else
		if("DEL".startsWith(fw))
		{
			if(commands.size()<2)
				mob.tell(L("You need to specify a player name."));
			else
			{
				MOB M=CMLib.players().getPlayerAllHosts(commands.get(1));
				if(M==null)
					M=CMLib.players().getLoadPlayer(commands.get(1));
				if(M==null)
				{
					boolean done=false;
					for(final String s : A.myPlayers)
					{
						if(s.equalsIgnoreCase(commands.get(1)))
						{
							A.myPlayers.remove(s);
							myPlayers.remove(s);
							done=true;
							break;
						}
					}
					if(done)
						mob.tell(L("Recording on @x1 has been stopped.",CMStrings.capitalizeAllFirstLettersAndLower(commands.get(1))));
					else
						mob.tell(L("Unknown player @x1",commands.get(1)));
				}
				else
				if(!A.myPlayers.contains(M.Name()) && (!myPlayers.contains(M.Name())))
					mob.tell(L("Player @x1 is not being recorded by YOU.",commands.get(1)));
				else
				if(M.fetchEffect(ID())==null)
				{
					A.myPlayers.remove(M.Name());
					myPlayers.remove(M.Name());
					mob.tell(L("Player @x1 is not being recorded.",commands.get(1)));
				}
				else
				{
					final Archon_CRecord A2=(Archon_CRecord)M.fetchEffect(ID());
					if(A2!=null)
					{
						A2.flushBuffer();
						M.delEffect(A2);
						A.myPlayers.remove(M.Name());
						myPlayers.remove(M.Name());
						mob.tell(L("Recording on @x1 has been stopped.",M.Name()));
					}
				}
			}
			return false;
		}
		else
		if(fw.equals("START"))
		{
			if(A.triggerTime>0)
				mob.tell(L("CRecord level listener was already started."));
			else
			if((commands.size()<2)||(!CMath.isInteger(commands.get(1))))
				mob.tell(L("You need to specify a number of minutes to trigger after."));
			else
			{
				if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
					return false;
				final boolean success=proficiencyCheck(mob,0,auto);
				if(success)
				{
					final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MASK_ALWAYS|CMMsg.TYP_ACTIVATE,L("CRecord level listener is now started."),null,null);
					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
						A.triggerTime=CMath.s_int(commands.get(1)) * 60 * 1000;
						this.triggerTime=A.triggerTime;
						CMLib.map().addGlobalHandler(A, CMMsg.TYP_LEVEL);
					}
				}
				else
					mob.tell(L("You failed."));
				return success;
			}
			return false;
		}
		else
		{
			mob.tell(L("Unknown argument '@x1': Try LIST, START [MINUTES], STOP, MINLEVEL [LEVEL], ADD [PLAYER], DEL [PLAYER].",CMParms.combine(commands)));
			return false;
		}
	}
}
