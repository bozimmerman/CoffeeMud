package com.planet_ink.coffee_mud.Commands;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.GenWrightSkill;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine.MPContext;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2025-2025 Bo Zimmerman

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
public class GenCommand extends StdCommand implements Modifiable
{
	// data should be stored in a common instance object .. something common to all genability of same id,
	// but diff to others.n  I'm thinking like a DVector, and just have
	private String			ID			= "GenCommand";
	private String[]		access		= new String[] { "COMMANDWORD" };
	private Boolean			orderok		= Boolean.TRUE;
	private String			secMaskStr	= "-PLAYER -NPC";
	private CompiledZMask	secMask		= null;
	private double			actCost		= -1;
	private double			cbtCost		= -1;
	private String			helpStr		= "";
	private String			script		= "";
	private ScriptingEngine engine		= null;

	@Override
	public String ID()
	{
		return ID;
	}

	public GenCommand()
	{
		// don't super my GenCommand!
	}

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		if(this.engine == null)
		{
			synchronized(this)
			{
				if(this.engine == null)
				{
					final ScriptingEngine S = (ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
					S.setSavable(false);
					S.setVarScope("*");
					S.setScript(script);
					S.preApproveScripts();
					this.engine = S;
				}
			}
		}
		final ScriptingEngine eng = this.engine;
		if((eng != null)
		&&(eng.isFunc("EXECUTE")))
		{
			final Object[] objs = new Object[ScriptingEngine.SPECIAL_NUM_OBJECTS];
			for(int i=0;i<commands.size();i++)
			{
				if(i<ScriptingEngine.SPECIAL_NUM_OBJECTS)
					objs[i] = commands.get(i);
				else
					objs[ScriptingEngine.SPECIAL_NUM_OBJECTS-1] += " " + commands.get(i);
			}
			for(int i=commands.size();i<ScriptingEngine.SPECIAL_NUM_OBJECTS;i++)
				objs[i] = "";
			final MPContext ctx = new MPContext(mob, mob, mob,
					mob.getVictim(), mob.fetchWieldedItem(), mob.fetchHeldItem(), mob.Name(), objs);
			return CMath.s_bool(eng.callFunc("EXECUTE", commands.size()+"", ctx));
		}
		else
			mob.tell(L("No EXECUTE function found."));
		return false;
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		if(cbtCost < 0)
			return CMProps.getCommandCombatActionCost(ID());
		return cbtCost;
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		if(actCost < 0)
			return CMProps.getCommandActionCost(ID());
		return actCost;
	}

	@Override
	public boolean canBeOrdered()
	{
		return orderok.booleanValue();
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		if(secMask == null)
			secMask = CMLib.masking().maskCompile(this.secMaskStr);
		return CMLib.masking().maskCheck(secMask, mob, true);
	}

	private static final String[] CODES = {
		"CLASS",//0
		"HELP",//1S
		"ACCESS",//2S[]
		"SCRIPT",//3S
		"ORDEROK",//4B
		"SECMASK",//5S
		"ACTCOST",//6D
		"CBTCOST",//7D
	};

	@Override
	public String[] getStatCodes()
	{
		return CODES;
	}

	protected int getCodeNum(final String code)
	{
		for(int i=0;i<CODES.length;i++)
		{
			if(code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public String getStat(final String code)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return ID();
		case 1:
			return this.helpStr;
		case 2:
			return CMParms.toListString(access);
		case 3:
			return this.script;
		case 4:
			return this.orderok.toString();
		case 5:
			return this.secMaskStr;
		case 6:
			return this.actCost+"";
		case 7:
			return this.cbtCost+"";
		default:
			if (code.equalsIgnoreCase("javaclass"))
				return "GenCommand";
			else
			if(code.equalsIgnoreCase("allxml"))
				return getAllXML();
			return "";
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		switch(getCodeNum(code))
		{
		case 0:
			if(val.trim().length()>0)
			{
				if(!ID().equals("GenCommand"))
					CMClass.delClass(CMObjectType.COMMAND,CMClass.getCommand(ID()));
				this.ID=val;
				CMClass.addClass(CMObjectType.COMMAND,this);
			}
			break;
		case 1:
			this.helpStr = val;
			break;
		case 2:
			access = CMParms.parseCommas(val.toUpperCase().trim(), true).toArray(new String[0]);
			break;
		case 3:
			this.script = val;
			this.engine = null;
			break;
		case 4:
			this.orderok = Boolean.valueOf(CMath.s_bool(val));
			break;
		case 5:
			this.secMaskStr = val;
			this.secMask = null;
			break;
		case 6:
			this.actCost = CMath.s_double(val);
			break;
		case 7:
			this.cbtCost = CMath.s_double(val);
			break;
		default:
			if(code.equalsIgnoreCase("allxml")&&ID.equalsIgnoreCase("GenCommand"))
				parseAllXML(val);
			break;
		}
	}

	@Override
	public boolean isGeneric()
	{
		return !ID.equals("GenCommand");
	}

	@Override
	public int getSaveStatIndex()
	{
		return 0;
	}

	@Override
	public boolean isStat(final String code)
	{
		return this.getCodeNum(code) >= 0;
	}

	private void parseAllXML(final String xml)
	{
		final List<XMLLibrary.XMLTag> V=CMLib.xml().parseAllXML(xml);
		if((V==null)||(V.size()==0))
			return;
		for(int c=0;c<getStatCodes().length;c++)
		{
			if(getStatCodes()[c].equals("CLASS"))
				this.ID=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(V, getStatCodes()[c]));
			else
			if(!getStatCodes()[c].equals("TEXT"))
				setStat(getStatCodes()[c],CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(V, getStatCodes()[c])));
		}
	}

	private String getAllXML()
	{
		final StringBuffer str=new StringBuffer("");
		for(int c=0;c<getStatCodes().length;c++)
		{
			if(!getStatCodes()[c].equals("TEXT"))
			{
				str.append("<"+getStatCodes()[c]+">"
						+CMLib.xml().parseOutAngleBrackets(getStat(getStatCodes()[c]))
						+"</"+getStatCodes()[c]+">");
			}
		}
		return str.toString();
	}
}
