package com.planet_ink.coffee_mud.Items.Software;

import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject.BoundedCube;
import com.planet_ink.coffee_mud.core.threads.TimeMs;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.MiniJSON.JSONObject;
import com.planet_ink.coffee_mud.core.MiniJSON.MJSONException;
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
 Copyright 2022-2022 Bo Zimmerman

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
public class DatabaseProgram extends GenShipProgram
{
	@Override
	public String ID()
	{
		return "DatabaseProgram";
	}

	protected volatile long			nextPowerCycleTmr	= System.currentTimeMillis() + (8 * 1000);
	protected final StringBuffer	scr					= new StringBuffer("");
	protected JSONObject			data				= new JSONObject();
	protected BoundedCube			spaceCube			= null;
	
	public DatabaseProgram()
	{
		super();
		setName("a database disk");
		setDisplayText("a small disk sits here.");
		setDescription("It appears to be a database program.");

		material = RawMaterial.RESOURCE_STEEL;
		baseGoldValue = 1000;
		basePhyStats().setWeight(100); // weight shall be how many entries can be held
		phyStats().setWeight(100);
		recoverPhyStats();
	}

	protected void decache()
	{
		scr.setLength(0);
	}

	@Override
	public String getParentMenu()
	{
		return "";
	}

	@Override
	public String getSettings()
	{
		return data.toString();
	}
	
	@Override
	public void setSettings(final String var)
	{
		if(var.length()>0)
		{
			spaceCube=null;
			try
			{
				data=new MiniJSON().parseObject(var);
				if(data.containsKey("SPACECUBE"))
				{
					JSONObject cubeData = data.getCheckedJSONObject("SPACECUBE");
					String coordStr=cubeData.getCheckedString("COORDS");
					String radiuStr=cubeData.getCheckedString("RADIUS");
					final long[] coords = convertStringToCoords(coordStr);
					Long radiusL=CMLib.english().parseSpaceDistance(radiuStr);
					if((coords!=null)&&(radiusL!=null))
						spaceCube = new BoundedCube(coords,radiusL.longValue());
				}
			}
			catch (MJSONException e)
			{
				Log.errOut(e);
			}
		}
		settings=var;
	}

	@Override
	public String getInternalName()
	{
		return "DATABASE"+this;
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
		super.addScreenMessage("Database browser closed.");
	}

	@Override
	public boolean isCommandString(String word, final boolean isActive)
	{
		word = word.toUpperCase();
		return (word.equals("DATABASE") || word.equals("DB"));
	}

	@Override
	public String getActivationMenu()
	{
		return "DATABASE    : Database Query Software";
	}

	protected void shutdown()
	{
		decache();
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

	@Override
	public String getCurrentScreenDisplay()
	{
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
	
	protected void addLineToReadableScreen(final String s)
	{
		while(scr.length()>1024)
		{
			int x=scr.indexOf("\n\r");
			if(x>0)
				scr.delete(0, x+1);
			else
				break;
		}
		scr.append(s).append("\n\r");
	}

	protected long[] convertStringToCoords(final String coordStr)
	{
		final List<String> coordCom = CMParms.parseCommas(coordStr,true);
		if(coordCom.size()==3)
		{
			final long[] coords=new long[3];
			for(int i=0;i<coordCom.size();i++)
			{
				final Long coord=CMLib.english().parseSpaceDistance(coordCom.get(i));
				if(coord != null)
					coords[i]=coord.longValue();
				else
					return null;
			}
			return coords;
		}
		return null;
	}
	
	@Override
	public String getStat(final String code)
	{
		if(code!=null)
		{
			if(code.startsWith("QUERY:"))
			{
				final String query=code.substring(5).trim();
				if(data.size()==0)
					return "";
				final String key=query.toUpperCase().trim();
				if(!data.containsKey(key))
					return "";
				return data.get(key).toString();
			}
			else
			if(code.startsWith("ID:"))
			{
				final String idStr=code.substring(3).trim();
				if(data.size()==0)
					return "";
				final String key=idStr.toUpperCase().trim();
				if(!data.containsKey(key))
					return "";
				final Object o = data.get(key);
				if(o instanceof String)
					return (String)o;
				if(o instanceof JSONObject)
				{
					final JSONObject jobj=(JSONObject)o;
					if(jobj.containsKey("ID"))
						return jobj.get("ID").toString();
				}
				return "";
			}
		}
		return super.getStat(code);
	}
	
	@Override
	public void onTyping(final MOB mob, String message)
	{
		synchronized(this)
		{
			message = message.toUpperCase().trim();
			final Vector<String> parsed=CMParms.parse(message);
			String uword=(parsed.size()>0)?parsed.get(0).toUpperCase():"";
			if(uword.equalsIgnoreCase("DATABASE")||uword.equalsIgnoreCase("DB"))
			{
				parsed.remove(0);
				uword=(parsed.size()>0)?parsed.get(0).toUpperCase():"";
				message=CMParms.combine(parsed,0);
			}
			if((parsed.size()==0)||(message.equalsIgnoreCase("HELP")))
			{
				super.addScreenMessage("");
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
