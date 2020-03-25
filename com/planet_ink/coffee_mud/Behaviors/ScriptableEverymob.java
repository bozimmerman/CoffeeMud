package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.ScriptParseException;
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
   Copyright 2005-2020 Bo Zimmerman

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
public class ScriptableEverymob extends StdBehavior implements ScriptingEngine
{
	@Override
	public String ID()
	{
		return "ScriptableEverymob";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_ROOMS|Behavior.CAN_AREAS;
	}

	private boolean started=false;
	private Scriptable sampleB=null;

	@Override
	public String accountForYourself()
	{
		return "complex triggered behaving";
	}

	private void giveUpTheScript(final Area metroA, final MOB M)
	{
		if((M==null)
		||(!M.isMonster())
		||(M.getStartRoom()==null)
		||(metroA==null)
		||(!metroA.inMyMetroArea(M.getStartRoom().getArea()))
		||(M.fetchBehavior("Scriptable")!=null))
			return;
		final Scriptable S=new Scriptable();
		S.setParms(getParms());
		S.setSavable(false);
		M.addBehavior(S);
		S.setSavable(false);
		sampleB=S;
	}

	private Area determineArea(final Environmental forMe)
	{
		if(forMe instanceof Room)
			return ((Room)forMe).getArea();
		else
		if(forMe instanceof Area)
			return (Area)forMe;
		return null;
	}

	private Enumeration<Room> determineRooms(final Environmental forMe)
	{
		if(forMe instanceof Room)
			return new XVector<Room>((Room)forMe).elements();
		else
		if(forMe instanceof Area)
			return ((Area)forMe).getMetroMap();
		return null;
	}

	private void giveEveryoneTheScript(final Environmental forMe)
	{
		if((CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
		&&(!started))
		{
			started = true;
			final Enumeration<Room> rooms=determineRooms(forMe);
			final Area A=determineArea(forMe);
			if((A!=null)&&(rooms!=null))
			{
				Room R=null;
				for(;rooms.hasMoreElements();)
				{
					R=rooms.nextElement();
					for(int m=0;m<R.numInhabitants();m++)
						giveUpTheScript(A,R.fetchInhabitant(m));
				}
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((!started)&&(ticking instanceof Environmental))
			giveEveryoneTheScript((Environmental)ticking);
		return super.tick(ticking, tickID);
	}

	@Override
	public void startBehavior(final PhysicalAgent forMe)
	{
		giveEveryoneTheScript(forMe);
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		if((msg.target() instanceof Room)
		&&(msg.targetMinor()==CMMsg.TYP_LOOK))
			giveUpTheScript(determineArea(host),msg.source());
		super.executeMsg(host,msg);
	}

	@Override
	public String defaultQuestName()
	{
		return (sampleB==null)?"":sampleB.defaultQuestName();
	}

	@Override
	public void dequeResponses()
	{
		if(sampleB!=null)
			sampleB.dequeResponses();
	}

	@Override
	public List<String> externalFiles()
	{
		return (sampleB==null)?null:sampleB.externalFiles();
	}

	@Override
	public boolean endQuest(final PhysicalAgent hostObj, final MOB mob, final String quest)
	{
		return (sampleB==null)?false:sampleB.endQuest(hostObj, mob, quest);
	}

	@Override
	public boolean eval(final PhysicalAgent scripted, final MOB source,
						final Environmental target, final MOB monster, final Item primaryItem,
						final Item secondaryItem, final String msg, final Object[] tmp, final String[][] eval,
						final int startEval)
	{
		return (sampleB==null)?false:sampleB.eval(scripted, source, target, monster, primaryItem, secondaryItem, msg, tmp, eval, startEval);
	}

	@Override
	public String execute(final PhysicalAgent scripted, final MOB source,
						  final Environmental target, final MOB monster, final Item primaryItem,
						  final Item secondaryItem, final DVector script, final String msg, final Object[] tmp)
	{
		return (sampleB==null)?"":sampleB.execute(scripted, source, target, monster, primaryItem, secondaryItem, script, msg, tmp);
	}

	@Override
	public String callFunc(final String named, final String parms, final PhysicalAgent scripted, final MOB source, final Environmental target,
			   final MOB monster, final Item primaryItem, final Item secondaryItem, final String msg, final Object[] tmp)
	{
		return (sampleB==null)?null:sampleB.callFunc(named, parms, scripted, source, target, monster, primaryItem, secondaryItem, msg, tmp);
	}

	@Override
	public boolean isFunc(final String named)
	{
		return (sampleB==null)?false:sampleB.engine().isFunc(named);
	}

	@Override
	public String getLocalVarXML()
	{
		return (sampleB==null)?"":sampleB.getLocalVarXML();
	}

	@Override
	public MOB getMakeMOB(final Tickable ticking)
	{
		return (sampleB==null)?null:sampleB.getMakeMOB(ticking);
	}

	@Override
	public String getScript()
	{
		return (sampleB==null)?"":sampleB.getScript();
	}

	@Override
	public String getScriptResourceKey()
	{
		return (sampleB==null)?"":sampleB.getScriptResourceKey();
	}

	@Override
	public String getVar(final String context, final String variable)
	{
		return (sampleB==null)?"":sampleB.getVar(context, variable);
	}

	@Override
	public String getVarScope()
	{
		return (sampleB==null)?"":sampleB.getVarScope();
	}

	@Override
	public boolean isVar(final String context, final String variable)
	{
		return (sampleB==null)?false:sampleB.isVar(context, variable);
	}

	@Override
	public String[] parseEval(final String evaluable) throws ScriptParseException
	{
		return (sampleB==null)?new String[0]:sampleB.parseEval(evaluable);
	}

	@Override
	public void setLocalVarXML(final String xml)
	{
		if(sampleB!=null)
			sampleB.setLocalVarXML(xml);
	}

	@Override
	public void setScript(final String newParms)
	{
		if(sampleB!=null)
			sampleB.setScript(newParms);
	}

	@Override
	public void setVar(final String context, final String variable, final String value)
	{
		if(sampleB!=null)
			sampleB.setVar(context, variable, value);
	}

	@Override
	public void setVarScope(final String scope)
	{
		if(sampleB!=null)
			sampleB.setVarScope(scope);
	}

	@Override
	public String varify(final MOB source, final Environmental target,
						 final PhysicalAgent scripted, final MOB monster, final Item primaryItem,
						 final Item secondaryItem, final String msg, final Object[] tmp, final String varifyable)
	{
		return (sampleB==null)?"":sampleB.varify(source, target, scripted, monster, primaryItem, secondaryItem, msg, tmp, varifyable);
	}

	@Override
	public String functify(final PhysicalAgent scripted, final MOB source, final Environmental target, final MOB monster,
						   final Item primaryItem, final Item secondaryItem, final String msg, final Object[] tmp,
						   final String evaluable)
	{
		return (sampleB==null)?"":sampleB.functify(scripted, source, target, monster, primaryItem, secondaryItem, msg, tmp, evaluable);
	}
}
