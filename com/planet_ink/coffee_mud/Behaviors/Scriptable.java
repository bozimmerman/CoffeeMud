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
   Copyright 2001-2018 Bo Zimmerman

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
	public void registerDefaultQuest(String questName)
	{
		engine().registerDefaultQuest(questName);
	}

	@Override
	public MOB getMakeMOB(Tickable ticking)
	{
		return engine().getMakeMOB(ticking);
	}

	@Override
	public boolean endQuest(PhysicalAgent hostObj, MOB mob, String quest)
	{
		engine().endQuest(hostObj, mob, quest);
		return false;
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
	public String[] parseEval(String evaluable) throws ScriptParseException
	{
		return engine().parseEval(evaluable);
	}

	@Override
	public void setParms(String newParms)
	{
		engine().setScript(newParms);
		super.setParms("");
	}

	@Override
	public String getVar(String context, String variable)
	{
		return engine().getVar(context, variable);
	}

	@Override
	public boolean isVar(String context, String variable)
	{
		return engine().isVar(context, variable);
	}

	@Override
	public void setVar(String context, String variable, String value)
	{
		engine().setVar(context, variable, value);
	}

	@Override
	public String defaultQuestName()
	{
		return engine().defaultQuestName();
	}

	@Override
	public void setVarScope(String scope)
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
	public void setLocalVarXML(String xml)
	{
		if(engine().getVarScope().length()>0)
			engine().setLocalVarXML(xml);
	}

	@Override
	public boolean eval(PhysicalAgent scripted,
						MOB source,
						Environmental target,
						MOB monster,
						Item primaryItem,
						Item secondaryItem,
						String msg,
						Object[] tmp,
						String[][] eval,
						int startEval)
	{
		return engine().eval(scripted, source, target, monster, primaryItem, secondaryItem, msg, tmp, eval, startEval);
	}

	@Override
	public String getScript()
	{
		return engine().getScript();
	}

	@Override
	public void setScript(String newParms)
	{
		engine().setScript(newParms);
	}

	@Override
	public String execute(PhysicalAgent scripted,
						  MOB source,
						  Environmental target,
						  MOB monster,
						  Item primaryItem,
						  Item secondaryItem,
						  DVector script,
						  String msg,
						  Object[] tmp)
	{
		return engine().execute(scripted, source, target, monster, primaryItem, secondaryItem, script, msg, tmp);
	}

	@Override
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		engine().executeMsg(affecting, msg);
	}

	@Override
	public boolean okMessage(Environmental affecting, CMMsg msg)
	{
		if(!super.okMessage(affecting, msg))
			return false;
		return engine().okMessage(affecting, msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return false;
		return engine().tick(ticking, tickID);
	}

	@Override
	public void dequeResponses()
	{
		engine().dequeResponses();
	}

	@Override
	public String varify(MOB source, Environmental target,
		PhysicalAgent scripted, MOB monster, Item primaryItem,
		Item secondaryItem, String msg, Object[] tmp, String varifyable)
	{
		return engine().varify(source, target, scripted, monster, primaryItem, secondaryItem, msg, tmp, varifyable);
	}

	@Override
	public String functify(PhysicalAgent scripted, MOB source, Environmental target, MOB monster, Item primaryItem,
							Item secondaryItem, String msg, Object[] tmp, String evaluable)
							{
		return engine().functify(scripted, source, target, monster, primaryItem, secondaryItem, msg, tmp, evaluable);
	}
}
