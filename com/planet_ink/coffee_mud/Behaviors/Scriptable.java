package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.exceptions.ScriptParseException;
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
import java.util.*;

/*
   Copyright 2001-2025 Bo Zimmerman

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
public class Scriptable extends StdBehavior implements ScriptingEngine
{
	@Override
	public String ID()
	{
		return "Scriptable";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_MOBS|Behavior.CAN_ITEMS|Behavior.CAN_ROOMS;
	}

	protected ScriptingEngine engine = null;
	protected ScriptingEngine engine()
	{
		if(engine==null)
			engine=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
		return engine;
	}

	@Override
	public String accountForYourself()
	{
		return "complex triggered behaving";
	}

	@Override
	public int getTickStatus()
	{
		final Tickable T=engine();
		if(T!=null)
			return T.getTickStatus();
		return Tickable.STATUS_NOT;
	}

	@Override
	public void registerDefaultQuest(final Object questName)
	{
		engine().registerDefaultQuest(questName);
	}

	@Override
	public MOB getMakeMOB(final Tickable ticking)
	{
		return engine().getMakeMOB(ticking);
	}

	@Override
	public boolean endQuest(final PhysicalAgent hostObj, final MOB mob, final String quest)
	{
		engine().endQuest(hostObj, mob, quest);
		return false;
	}

	@Override
	public boolean stepQuest(final PhysicalAgent hostObj, final MOB mob, final String quest)
	{
		engine().stepQuest(hostObj, mob, quest);
		return false;
	}

	@Override
	public void preApproveScripts()
	{
		if(engine() != null)
			engine().preApproveScripts();
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final Scriptable B=(Scriptable)this.clone();
			if(B.engine!=null)
				B.engine=(ScriptingEngine)engine.copyOf();
			return B;
		}
		catch(final CloneNotSupportedException e)
		{
			return new Scriptable();
		}
	}

	@Override
	public List<String> externalFiles()
	{
		return engine().externalFiles();
	}

	@Override
	public String getScriptResourceKey()
	{
		return engine().getScriptResourceKey();
	}

	@Override
	public String getParms()
	{
		return engine().getScript();
	}

	@Override
	public String[] parseEval(final String evaluable) throws ScriptParseException
	{
		return engine().parseEval(evaluable);
	}

	@Override
	public void setParms(final String newParms)
	{
		engine().setScript(newParms);
		super.setParms("");
	}

	@Override
	public String getVar(final String context, final String variable)
	{
		return engine().getVar(context, variable);
	}

	@Override
	public boolean isVar(final String context, final String variable)
	{
		return engine().isVar(context, variable);
	}

	@Override
	public void setVar(final String context, final String variable, final String value)
	{
		engine().setVar(context, variable, value);
	}

	@Override
	public String defaultQuestName()
	{
		return engine().defaultQuestName();
	}

	@Override
	public void setVarScope(final String scope)
	{
		engine().setVarScope(scope);
	}

	@Override
	public String getVarScope()
	{
		return engine().getVarScope();
	}

	@Override
	public String getLocalVarXML()
	{
		return engine().getLocalVarXML();
	}

	@Override
	public void setLocalVarXML(final String xml)
	{
		if(engine().getVarScope().length()>0)
			engine().setLocalVarXML(xml);
	}

	@Override
	public boolean eval(final MPContext ctx, final String[][] eval, final int startEval)
	{
		return engine().eval(ctx, eval, startEval);
	}

	@Override
	public String getScript()
	{
		return engine().getScript();
	}

	@Override
	public void setScript(final String newParms)
	{
		engine().setScript(newParms);
	}

	@Override
	public String execute(final MPContext ctx)
	{
		return engine().execute(ctx);
	}

	@Override
	public String callFunc(final String named, final String parms, final MPContext ctx)
	{
		return engine().callFunc(named, parms, ctx);
	}

	@Override
	public boolean isFunc(final String named)
	{
		return engine().isFunc(named);
	}

	@Override
	public void executeMsg(final Environmental affecting, final CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		engine().executeMsg(affecting, msg);
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(!super.okMessage(host, msg))
			return false;
		return engine().okMessage(host, msg);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		super.tick(ticking,tickID);
		if(!CMProps.isState(CMProps.HostState.RUNNING))
			return false;
		return engine().tick(ticking, tickID);
	}

	@Override
	public void dequeResponses(final Object[] objects)
	{
		engine().dequeResponses(null);
	}

	@Override
	public String varify(final MPContext ctx, final String varifyable)
	{
		return engine().varify(ctx, varifyable);
	}

	@Override
	public String functify(final MPContext ctx, final String evaluable)
	{
		return engine().functify(ctx, evaluable);
	}
}
