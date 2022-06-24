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
 Copyright 2013-2022 Bo Zimmerman

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
public class ShipDiagProgram extends GenShipProgram implements ArchonOnly
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
	protected volatile long					diagCompletionMs	= 0;
	protected final StringBuffer			scr					= new StringBuffer("");

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

	protected void decache()
	{
		components	= null;
		showUpdatedDamage = false;
		scr.setLength(0);
	}

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
	public String getParentMenu()
	{
		return "";
	}

	@Override
	public String getInternalName()
	{
		return "DIAGNOSTICS";
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
	public void onDeactivate(final MOB mob, final String message)
	{
		shutdown();
		super.addScreenMessage("Diagnostic window closed.");
	}

	@Override
	public boolean isCommandString(String word, final boolean isActive)
	{
		word = word.toUpperCase();
		return (word.startsWith("DIAG ") || word.equals("DAMAGE"));
	}

	@Override
	public String getActivationMenu()
	{
		return "DAMAGE      : Damage Control Software\n\r"
			  +"DIAG [LEVEL]: Diagnostics Software";
	}

	protected void shutdown()
	{
		currentScreen = "";
		synchronized (this)
		{
		}
	}

	@Override
	public boolean checkDeactivate(final MOB mob, final String message)
	{
		shutdown();
		return true;
	}

	@Override
	public boolean checkTyping(final MOB mob, final String message)
	{
		return true;
	}

	@Override
	public boolean checkPowerCurrent(final int value)
	{
		nextPowerCycleTmr = System.currentTimeMillis() + (8 * 1000);
		return true;
	}

	public char getConditionColor(final int cond)
	{
		if(cond>=100)
			return 'G';
		else
		if(cond>=65)
			return 'Y';
		else
		if(cond>=35)
			return 'r';
		else
			return 'R';
	}

	@Override
	public String getCurrentScreenDisplay()
	{
		if(showUpdatedDamage)
		{
			scr.setLength(0);
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
				scr.append(CMStrings.padRight(Long.toString(condPct)+"%",8));
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
					scr.append(CMStrings.padRight(Long.toString(condPct)+"%",8));
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
		else
			return scr.toString();
	}

	@Override
	public boolean checkActivate(final MOB mob, final String message)
	{
		if(!super.checkActivate(mob, message))
			return false;
		return true;
	}

	@Override
	public void onActivate(final MOB mob, final String message)
	{
		super.onActivate(mob, message);
		onTyping(mob, message);
	}

	@Override
	public void onTyping(final MOB mob, String message)
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
			if(uword.startsWith("DIAG"))
			{
				final String word2=(parsed.size()>1)?parsed.get(1).toUpperCase():"";
				final int level = CMath.s_int(word2);
				final String system = (parsed.size()>2)?CMParms.combine(parsed,2):"";
				if((!CMath.isInteger(word2))
				||(level > 3))
				{
					super.addScreenMessage(L("Diag error: Invalid diagnostics level '@x1'",word2));
					return;
				}
			}
		}
	}

	@Override
	public void onPowerCurrent(final int value)
	{
		if (System.currentTimeMillis() > nextPowerCycleTmr)
		{
			this.shutdown();
		}
	}
}
