package com.planet_ink.coffee_mud.Items.Software;

import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.TimeMs;
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

import java.net.*;
import java.io.*;
import java.util.*;

/*
 Copyright 2022-2025 Bo Zimmerman

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
public class ShipDiagProgram extends GenShipProgram
{
	@Override
	public String ID()
	{
		return "ShipDiagProgram";
	}

	protected volatile long					nextPowerCycleTmr	= System.currentTimeMillis() + (8 * 1000);
	protected volatile List<TechComponent>	components			= null;
	protected volatile boolean				showUpdatedDamage	= false;
	protected volatile TechComponent		diagTargetT			= null;
	protected volatile Integer				diagTargetL			= null;
	protected volatile long					diagMasterMs		= 0;
	protected volatile long					diagCompletionMs	= 0;
	protected volatile Pair<Area,Ability>	diagWatcherA		= null;
	protected final StringBuffer			scr					= new StringBuffer("");

	protected final Map<Electronics, Pair<CMMsg, Long>>		last= new Hashtable<Electronics, Pair<CMMsg, Long>>();
	protected final Map<Electronics, Pair<long[], long[]>>	rpt	= new Hashtable<Electronics, Pair<long[], long[]>>();

	public ShipDiagProgram()
	{
		super();
		setName("a diagnostics disk");
		setDisplayText("a small disk sits here.");
		setDescription("It appears to be a diagnostics program.");

		material = RawMaterial.RESOURCE_STEEL;
		baseGoldValue = 1000;
		recoverPhyStats();
	}

	protected boolean isDiagRunning()
	{
		if((diagTargetL!=null)
		&&(diagCompletionMs>0)
		&&(System.currentTimeMillis()<diagCompletionMs))
			return true;
		return false;
	}

	protected boolean isDiagAvailable()
	{
		if((diagTargetL!=null)
		&&(!isDiagRunning()))
			return true;
		return false;
	}

	protected void cancelRunningDiag()
	{
		if(diagWatcherA!=null)
		{
			final ExtendableAbility xA=(ExtendableAbility)diagWatcherA.second;
			diagWatcherA.first.delEffect(xA);
			diagWatcherA=null;
		}
		this.last.clear();
		this.rpt.clear();
		diagCompletionMs = 0;
	}

	@Override
	protected void decache()
	{
		components	= null;
		showUpdatedDamage = false;
		cancelRunningDiag();
		scr.setLength(0);
		diagTargetT = null;
		diagTargetL = null;
	}

	protected final MsgListener listener = new MsgListener()
	{
		@Override
		public void executeMsg(final Environmental myHost, final CMMsg msg)
		{
			if((msg.targetMinor()==CMMsg.TYP_POWERCURRENT)
			&&(msg.target() instanceof Electronics)
			&&(last.containsKey(msg.target())))
			{
				final Pair<CMMsg,Long> prevValue=last.get(msg.target());
				if(prevValue.first==msg)
				{
					final long amountTaken = prevValue.second.longValue() - msg.value();
					if(!rpt.containsKey(msg.target()))
						rpt.put((Electronics)msg.target(), new Pair<long[],long[]>(new long[] {0},new long[] {0}));
					final Pair<long[],long[]> p = rpt.get(msg.target());
					p.first[0]++;
					p.second[0]+=amountTaken;
				}
			}
		}

		@Override
		public boolean okMessage(final Environmental myHost, final CMMsg msg)
		{
			if((msg.targetMinor()==CMMsg.TYP_POWERCURRENT)
			&&(msg.target() instanceof Electronics))
			{
				final Long value=Long.valueOf(msg.value());
				last.put((Electronics)msg.target(), new Pair<CMMsg,Long>(msg,value));
			}
			return true;
		}
	};

	protected ExtendableAbility makePowerWatcher()
	{
		final ExtendableAbility A=(ExtendableAbility)CMClass.getAbility("ExtAbility");
		if(A!=null)
		{
			A.setSavable(false);
			A.setExpirationDate(System.currentTimeMillis()+600000L);
			this.last.clear();
			this.rpt.clear();
			A.setMsgListener(this.listener);
		}
		return A;
	}

	@Override
	protected synchronized List<TechComponent> getTechComponents()
	{
		if(components == null)
		{
			if(circuitKey.length()==0)
				components=new Vector<TechComponent>(0);
			else
			{
				final List<Electronics> electronics=CMLib.tech().getMakeRegisteredElectronics(circuitKey);
				components=new Vector<TechComponent>(1);
				for(final Electronics E : electronics)
				{
					if(E instanceof TechComponent)
						components.add((TechComponent)E);
				}
			}
		}
		return components;
	}

	@Override
	public boolean isActivationString(final String word)
	{
		return isCommandString(word, false);
	}

	@Override
	public boolean isDeActivationString(final String word)
	{
		return isCommandString(word, false);
	}

	@Override
	protected void onDeactivate(final MOB mob, final String message)
	{
		super.onDeactivate(mob, message);
		shutdown();
		super.addScreenMessage("Diagnostic window closed.");
	}

	@Override
	public boolean isCommandString(String word, final boolean isActive)
	{
		word = word.toUpperCase();
		return (word.startsWith("DIAG") || word.equals("DAMAGE") || (word.startsWith("HELP")));
	}

	@Override
	public String getActivationMenu()
	{
		return "^wDAMAGE                 ^N: Damage Control Software\n\r"
			  +"^wDIAG [LEVEL]           ^N: Diagnostics Software";
	}

	protected void shutdown()
	{
		decache();
	}

	@Override
	protected boolean checkDeactivate(final MOB mob, final String message)
	{
		shutdown();
		return true;
	}

	@Override
	protected boolean checkTyping(final MOB mob, final String message)
	{
		return true;
	}

	@Override
	protected boolean checkPowerCurrent(final int value)
	{
		nextPowerCycleTmr = System.currentTimeMillis() + (8 * 1000);
		return true;
	}

	public String getConditionStr(final int cond)
	{
		if(cond>=100)
			return "*****";
		else
		if(cond>=90)
			return "****-";
		else
		if(cond>=75)
			return "**** ";
		else
		if(cond>=50)
			return "***  ";
		else
		if(cond>=25)
			return "**   ";
		else
			return "*    ";
	}

	public char getConditionColor(final int cond)
	{
		if(cond>=100)
			return 'g';
		else
		if(cond>=90)
			return 'G';
		else
		if(cond>=75)
			return 'y';
		else
		if(cond>=50)
			return 'Y';
		else
		if(cond>=25)
			return 'r';
		else
			return 'R';
	}

	public String getDamageControl()
	{
		final StringBuilder scr=new StringBuilder("");
		final boolean damageFound=false;
		final SpaceObject spaceObject=CMLib.space().getSpaceObject(this,true);
		final SpaceShip ship=(spaceObject instanceof SpaceShip)?(SpaceShip)spaceObject:null;
		final SpaceObject shipSpaceObject=(ship==null)?null:ship.getShipSpaceObject();
		if(shipSpaceObject instanceof Item)
		{
			int condPct=100;
			if(((Item)shipSpaceObject).subjectToWearAndTear())
				condPct = ((Item)shipSpaceObject).usesRemaining();
			scr.append("^H");
			scr.append(CMStrings.padRight(L("^gA"),2));
			scr.append("^W").append(CMStrings.padRight(L("  "),6));
			scr.append('^').append(getConditionColor(condPct));
			scr.append(CMStrings.padRight(getConditionStr(condPct),8));
			scr.append("^H").append(CMStrings.padRight(L("Ship Hull"),48));
			scr.append("^.^N\n\r");
		}
		for(final TechComponent C : this.getTechComponents())
		{
			if(C instanceof Item)
			{
				int condPct=100;
				if(((Item)C).subjectToWearAndTear())
					condPct = ((Item)C).usesRemaining();
				scr.append("^H");
				scr.append(CMStrings.padRight(C.activated()?L("^gA"):L("^rI"),2));
				scr.append("^W").append(CMStrings.padRight(L("  "),6));
				scr.append('^').append(getConditionColor(condPct));
				scr.append(CMStrings.padRight(getConditionStr(condPct),8));
				scr.append("^H").append(CMStrings.padRight(L(C.name()),48));
				scr.append("^.^N\n\r");
			}
		}

		final StringBuilder header=new StringBuilder("^.^N");
		if(damageFound)
			header.append("^~r");
		else
			header.append("^X");
		header.append(CMStrings.centerPreserve(L(" -- Damage Control -- "),60)).append("^.^N\n\r");
		scr.insert(0, header.toString());
		return scr.toString();
	}

	public String getDiagLevel1()
	{
		final StringBuilder scr=new StringBuilder("");
		final boolean damageFound=false;
		final SpaceObject spaceObject=CMLib.space().getSpaceObject(this,true);
		final SpaceShip ship=(spaceObject instanceof SpaceShip)?(SpaceShip)spaceObject:null;
		final SpaceObject shipSpaceObject=(ship==null)?null:ship.getShipSpaceObject();
		if((shipSpaceObject instanceof Item)
		&&((diagTargetT==null)||(diagTargetT==shipSpaceObject)))
		{
			int condPct=100;
			if(((Item)shipSpaceObject).subjectToWearAndTear())
				condPct = ((Item)shipSpaceObject).usesRemaining();
			scr.append("^H");
			scr.append(CMStrings.padRight(L("^gA"),2));
			scr.append("^W").append(CMStrings.padRight(L("  "),6));
			scr.append('^').append(getConditionColor(condPct));
			scr.append(CMStrings.padRight(""+condPct+"%",8));
			scr.append("^H").append(CMStrings.padRight(L("Ship Hull"),48));
			scr.append("^.^N\n\r");
		}
		for(final TechComponent C : this.getTechComponents())
		{
			if((C instanceof Item)
			&&((diagTargetT==null)||(diagTargetT==C)))
			{
				int condPct=100;
				if(((Item)C).subjectToWearAndTear())
					condPct = ((Item)C).usesRemaining();
				scr.append("^H");
				scr.append(CMStrings.padRight(C.activated()?L("^gA"):L("^rI"),2));
				scr.append("^W").append(CMStrings.padRight(L("  "),6));
				scr.append('^').append(getConditionColor(condPct));
				scr.append(CMStrings.padRight(""+condPct+"%",8));
				scr.append("^H").append(CMStrings.padRight(L(C.name()),48));
				scr.append("^.^N\n\r");
			}
		}

		final StringBuilder header=new StringBuilder("^.^N");
		if(damageFound)
			header.append("^~r");
		else
			header.append("^X");
		final TimeClock C = CMLib.time().localClock(this);
		final String time = C.getShortestTimeDescription();
		header.append(CMStrings.centerPreserve(L(" -- Level 1 Diagnostic Report " + time+" -- "),60)).append("^.^N\n\r");
		scr.insert(0, header.toString());
		return scr.toString();
	}

	public String getDiagLevel2()
	{
		final StringBuilder scr=new StringBuilder("");
		final boolean damageFound=false;
		final SpaceObject spaceObject=CMLib.space().getSpaceObject(this,true);
		final SpaceShip ship=(spaceObject instanceof SpaceShip)?(SpaceShip)spaceObject:null;
		final SpaceObject shipSpaceObject=(ship==null)?null:ship.getShipSpaceObject();
		if((shipSpaceObject instanceof Item)
		&&((diagTargetT==null)||(diagTargetT==shipSpaceObject)))
		{
			int condPct=100;
			if(((Item)shipSpaceObject).subjectToWearAndTear())
				condPct = ((Item)shipSpaceObject).usesRemaining();
			scr.append("^H");
			scr.append(CMStrings.padRight(L("^gA"),2));
			scr.append("^W").append(CMStrings.padRight(L("Cond: "),6));
			scr.append('^').append(getConditionColor(condPct));
			scr.append(CMStrings.padRight(""+condPct+"%",5));
			final int powerPerTick = 0;
			scr.append("^W").append(CMStrings.padRight(L("Pow: "),5));
			scr.append(CMStrings.padRight(""+powerPerTick,5));
			scr.append(" ");
			scr.append("^H").append(CMStrings.padRight(L("Ship Hull"),41));
			scr.append("^.^N\n\r");
		}
		for(final TechComponent C : this.getTechComponents())
		{
			if((C instanceof Item)
			&&((diagTargetT==null)||(diagTargetT==C)))
			{
				int condPct=100;
				if(((Item)C).subjectToWearAndTear())
					condPct = ((Item)C).usesRemaining();
				scr.append("^H");
				scr.append(CMStrings.padRight(C.activated()?L("^gA"):L("^rI"),2));
				scr.append("^W").append(CMStrings.padRight(L("Cond: "),6));
				scr.append('^').append(getConditionColor(condPct));
				scr.append(CMStrings.padRight(""+condPct+"%",5));
				int powerPerTick = 0;
				if(rpt.containsKey(C))
				{
					final Pair<long[],long[]> p=rpt.get(C);
					powerPerTick = (int)Math.round(Math.ceil(CMath.div(p.second[0], p.first[0])));
				}
				scr.append("^W").append(CMStrings.padRight(L("Pow: "),5));
				scr.append(CMStrings.padRight(""+powerPerTick,5));
				scr.append(" ");
				scr.append("^H").append(CMStrings.padRight(L(C.name()),41));
				scr.append("^.^N\n\r");
			}
		}

		final StringBuilder header=new StringBuilder("^.^N");
		if(damageFound)
			header.append("^~r");
		else
			header.append("^X");
		final TimeClock C = CMLib.time().localClock(this);
		final String time = C.getShortestTimeDescription();
		header.append(CMStrings.centerPreserve(L(" -- Level 2 Diagnostic Report " + time+" -- "),60)).append("^.^N\n\r");
		scr.insert(0, header.toString());
		return scr.toString();
	}

	public String getDiagLevel3()
	{
		final StringBuilder scr=new StringBuilder("");
		final boolean damageFound=false;
		final SpaceObject spaceObject=CMLib.space().getSpaceObject(this,true);
		final SpaceShip ship=(spaceObject instanceof SpaceShip)?(SpaceShip)spaceObject:null;
		final SpaceObject shipSpaceObject=(ship==null)?null:ship.getShipSpaceObject();
		if((shipSpaceObject instanceof Item)
		&&((diagTargetT==null)||(diagTargetT==shipSpaceObject)))
		{
			int condPct=100;
			if(((Item)shipSpaceObject).subjectToWearAndTear())
				condPct = ((Item)shipSpaceObject).usesRemaining();
			scr.append("^H");
			scr.append(CMStrings.padRight(L("^gA"),2));
			scr.append("^W").append(CMStrings.padRight(L("Cond: "),6));
			scr.append('^').append(getConditionColor(condPct));
			scr.append(CMStrings.padRight(""+condPct+"%",5));
			final int powerPerTick = 0;
			scr.append("^W").append(CMStrings.padRight(L("Pow: "),5));
			scr.append(CMStrings.padRight("^w"+powerPerTick+"^N",5));
			scr.append(" ");
			final Manufacturer man = (shipSpaceObject instanceof Technical)?((Technical)shipSpaceObject).getFinalManufacturer():null;
			final double installFactor = (shipSpaceObject instanceof TechComponent)?((TechComponent)shipSpaceObject).getInstalledFactor():1.0;
			final int efficiencyPct = (int)Math.round(100.0 * installFactor
													  * CMath.div(condPct, 100.0)
													  * (man==null?1.0:(man.getEfficiencyPct()*man.getReliabilityPct())));
			scr.append("^W").append(CMStrings.padRight(L("Eff: "),5));
			scr.append('^').append(getConditionColor(efficiencyPct));
			scr.append(CMStrings.padRight(""+efficiencyPct+"%",5));
			scr.append(" ");
			scr.append("^H").append(CMStrings.padRight(L("Ship Hull"),41));
			scr.append("^.^N\n\r");
		}
		for(final TechComponent C : this.getTechComponents())
		{
			if((C instanceof Item)
			&&((diagTargetT==null)||(diagTargetT==C)))
			{
				int condPct=100;
				if(((Item)C).subjectToWearAndTear())
					condPct = ((Item)C).usesRemaining();
				scr.append("^H");
				scr.append(CMStrings.padRight(C.activated()?L("^gA"):L("^rI"),2));
				scr.append("^W").append(CMStrings.padRight(L("Cond: "),6));
				scr.append('^').append(getConditionColor(condPct));
				scr.append(CMStrings.padRight(""+condPct+"%",5));
				int powerPerTick = 0;
				if(rpt.containsKey(C))
				{
					final Pair<long[],long[]> p=rpt.get(C);
					powerPerTick = (int)Math.round(Math.ceil(CMath.div(p.second[0], p.first[0])));
				}
				scr.append("^W").append(CMStrings.padRight(L("Pow: "),5));
				scr.append(CMStrings.padRight("^w"+powerPerTick+"^N",5));
				scr.append(" ");
				final Manufacturer man = (C instanceof Technical)?((Technical)C).getFinalManufacturer():null;
				final double installFactor = (C instanceof TechComponent)?C.getInstalledFactor():1.0;
				final int efficiencyPct = (int)Math.round(100.0 * installFactor
													* CMath.div(condPct, 100.0)
													* (man==null?1.0:(man.getEfficiencyPct()*man.getReliabilityPct())));
				scr.append("^W").append(CMStrings.padRight(L("Eff: "),5));
				scr.append('^').append(getConditionColor(efficiencyPct));
				scr.append(CMStrings.padRight(""+efficiencyPct+"%",5));
				scr.append(" ");
				scr.append("^H").append(CMStrings.padRight(L(C.name()),41));
				scr.append("^.^N\n\r");
			}
		}

		final StringBuilder header=new StringBuilder("^.^N");
		if(damageFound)
			header.append("^~r");
		else
			header.append("^X");
		final TimeClock C = CMLib.time().localClock(this);
		final String time = C.getShortestTimeDescription();
		header.append(CMStrings.centerPreserve(L(" -- Level 3 Diagnostic Report " + time+" -- "),60)).append("^.^N\n\r");
		scr.insert(0, header.toString());
		return scr.toString();
	}

	@Override
	public String getCurrentScreenDisplay()
	{
		final StringBuilder str = new StringBuilder("^X");
		str.append(CMStrings.centerPreserve(L(" -- Diagnostics System -- "),60)).append("^.^N\n\r");
		if(showUpdatedDamage)
			str.append(getDamageControl());
		else
			str.append(scr.toString());
		return str.toString();
	}

	@Override
	protected boolean checkActivate(final MOB mob, final String message)
	{
		if(!super.checkActivate(mob, message))
			return false;
		return true;
	}

	@Override
	protected void onActivate(final MOB mob, final String message)
	{
		super.onActivate(mob, message);
		if((message!=null)&&(message.length()>0))
			onTyping(mob, message);
	}

	@Override
	protected void onTyping(final MOB mob, String message)
	{
		synchronized(this)
		{
			message = message.toUpperCase();
			final Vector<String> parsed=CMParms.parse(message);
			final String uword=(parsed.size()>0)?parsed.get(0).toUpperCase():"";
			if(uword.equalsIgnoreCase("DAMAGE"))
			{
				showUpdatedDamage = true;
				super.addScreenMessage(getCurrentScreenDisplay());
				super.forceNewMessageScan();
			}
			else
			if(uword.equals("HELP"))
			{
				super.addScreenMessage(this.getActivationMenu() + "^.^N");
			}
			else
			if(uword.startsWith("DIAG"))
			{
				final String word2=(parsed.size()>1)?parsed.get(1).toUpperCase():"";
				if((diagTargetL!=null)
				&&(word2.length()==0)
				&&(scr.length()>0))
				{
					showUpdatedDamage = false;
					super.addScreenMessage(getCurrentScreenDisplay());
					super.forceNewMessageScan();
					return;
				}
				final int level = CMath.s_int(word2);
				final String system = (parsed.size()>2)?CMParms.combine(parsed,2):"";
				if((!CMath.isInteger(word2))
				||(level > 3))
				{
					super.addScreenMessage(L("Diag syntax error: Invalid diagnostics level '@x1'",word2));
					return;
				}
				if(isDiagRunning())
					super.addScreenMessage(L("Notice: Previous diagnostic job cancelled."));
				cancelRunningDiag();
				diagTargetL=null;
				diagTargetT=null;
				TechComponent T=null;
				final List<TechComponent> all=this.getTechComponents();
				if(system.length()>0)
				{
					T=(TechComponent)CMLib.english().fetchEnvironmental(all, system, true);
					if(T==null)
						T=(TechComponent)CMLib.english().fetchEnvironmental(all, system, false);
					if(T==null)
					{
						super.addScreenMessage(L("Diag syntax error: Invalid system '@x1'",system));
						return;
					}
				}
				final long multiPlier = CMProps.getTickMillis();
				diagTargetL = Integer.valueOf(level);
				diagTargetT=T;
				final long itemCt = (T!=null)?1:(1+all.size());
				final SpaceObject myObj = CMLib.space().getSpaceObject(this, true);
				switch(level)
				{
				case 1:
					diagMasterMs=(itemCt *multiPlier);
					diagCompletionMs=System.currentTimeMillis()+diagMasterMs;
					break;
				case 2:
				{
					if(myObj instanceof SpaceShip)
					{
						final Area A=((SpaceShip)myObj).getArea();
						final ExtendableAbility xA=makePowerWatcher();
						if((A!=null)&&(xA!=null))
						{
							A.addEffect(xA);
							this.diagWatcherA=new Pair<Area,Ability>(A,xA);
						}
					}
					diagMasterMs=(itemCt *multiPlier * 3);
					diagCompletionMs=System.currentTimeMillis()+diagMasterMs;
					break;
				}
				case 3:
					if(myObj instanceof SpaceShip)
					{
						final Area A=((SpaceShip)myObj).getArea();
						final ExtendableAbility xA=makePowerWatcher();
						if((A!=null)&&(xA!=null))
						{
							A.addEffect(xA);
							this.diagWatcherA=new Pair<Area,Ability>(A,xA);
						}
					}
					diagMasterMs=(itemCt *multiPlier * 9);
					diagCompletionMs=System.currentTimeMillis()+diagMasterMs;
					break;
				}
				scr.setLength(0);
				scr.append(L("Processing diagnostic level @x1",""+level));
				super.addScreenMessage(L("Processing diagnostic level @x1",""+level));
			}
		}
	}

	@Override
	protected void onPowerCurrent(final int value)
	{
		super.onPowerCurrent(value);
		if (System.currentTimeMillis() > nextPowerCycleTmr)
		{
			this.shutdown();
		}
		if ((diagCompletionMs != 0)
		&& (diagTargetL != null)
		&& (System.currentTimeMillis() >= diagCompletionMs))
		{
			scr.setLength(0);
			switch(diagTargetL.intValue())
			{
			case 1:
				scr.append(this.getDiagLevel1());
				break;
			case 2:
				scr.append(this.getDiagLevel2());
				break;
			case 3:
				scr.append(this.getDiagLevel3());
				break;
			}
			this.cancelRunningDiag();
			showUpdatedDamage=false;
			super.addScreenMessage(L("Diagnostic level @x1 completed.",""+diagTargetL.intValue()));
		}
		else
		if(this.isDiagRunning())
		{
			final long diff = diagCompletionMs - System.currentTimeMillis();
			final long pct=Math.round(CMath.div(diagMasterMs-diff, diagMasterMs)*100.0);
			scr.setLength(0);
			scr.append(L("Processing diagnostic level @x1 (@x2%)",""+this.diagTargetL.intValue(),""+pct));
		}
	}
}
