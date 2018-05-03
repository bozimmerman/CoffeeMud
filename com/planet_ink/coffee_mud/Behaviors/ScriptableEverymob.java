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
   Copyright 2005-2018 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
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

	private void giveUpTheScript(Area metroA, MOB M)
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

	private Area determineArea(Environmental forMe)
	{
		if(forMe instanceof Room)
			return ((Room)forMe).getArea();
		else
		if(forMe instanceof Area)
			return (Area)forMe;
		return null;
	}

	private Enumeration<Room> determineRooms(Environmental forMe)
	{
		if(forMe instanceof Room)
			return new XVector<Room>((Room)forMe).elements();
		else
		if(forMe instanceof Area)
			return ((Area)forMe).getMetroMap();
		return null;
	}

	private void giveEveryoneTheScript(Environmental forMe)
	{
		if((CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
		&&(!started))
		{
			started = true;
			final Enumeration rooms=determineRooms(forMe);
			final Area A=determineArea(forMe);
			if((A!=null)&&(rooms!=null))
			{
				Room R=null;
				for(;rooms.hasMoreElements();)
				{
					R=(Room)rooms.nextElement();
					for(int m=0;m<R.numInhabitants();m++)
						giveUpTheScript(A,R.fetchInhabitant(m));
				}
			}
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((!started)&&(ticking instanceof Environmental))
			giveEveryoneTheScript((Environmental)ticking);
		return super.tick(ticking, tickID);
	}

	@Override
	public void startBehavior(PhysicalAgent forMe)
	{
		giveEveryoneTheScript(forMe);
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
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
	public boolean endQuest(PhysicalAgent hostObj, MOB mob, String quest)
	{
		return (sampleB==null)?false:sampleB.endQuest(hostObj, mob, quest);
	}

	@Override
	public boolean eval(PhysicalAgent scripted, MOB source,
			Environmental target, MOB monster, Item primaryItem,
			Item secondaryItem, String msg, Object[] tmp, String[][] eval,
			int startEval)
			{
		return (sampleB==null)?false:sampleB.eval(scripted, source, target, monster, primaryItem, secondaryItem, msg, tmp, eval, startEval);
	}

	@Override
	public String execute(PhysicalAgent scripted, MOB source,
			Environmental target, MOB monster, Item primaryItem,
			Item secondaryItem, DVector script, String msg, Object[] tmp)
			{
		return (sampleB==null)?"":sampleB.execute(scripted, source, target, monster, primaryItem, secondaryItem, script, msg, tmp);
	}

	@Override
	public String getLocalVarXML()
	{
		return (sampleB==null)?"":sampleB.getLocalVarXML();
	}

	@Override
	public MOB getMakeMOB(Tickable ticking)
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
	public String getVar(String context, String variable)
	{
		return (sampleB==null)?"":sampleB.getVar(context, variable);
	}

	@Override
	public String getVarScope()
	{
		return (sampleB==null)?"":sampleB.getVarScope();
	}

	@Override
	public boolean isVar(String context, String variable)
	{
		return (sampleB==null)?false:sampleB.isVar(context, variable);
	}

	@Override
	public String[] parseEval(String evaluable) throws ScriptParseException {
		return (sampleB==null)?new String[0]:sampleB.parseEval(evaluable);
	}

	@Override
	public void setLocalVarXML(String xml)
	{
		if(sampleB!=null)
			sampleB.setLocalVarXML(xml);
	}

	@Override
	public void setScript(String newParms)
	{
		if(sampleB!=null)
			sampleB.setScript(newParms);
	}

	@Override
	public void setVar(String context, String variable, String value)
	{
		if(sampleB!=null)
			sampleB.setVar(context, variable, value);
	}

	@Override
	public void setVarScope(String scope)
	{
		if(sampleB!=null)
			sampleB.setVarScope(scope);
	}

	@Override
	public String varify(MOB source, Environmental target,
		PhysicalAgent scripted, MOB monster, Item primaryItem,
		Item secondaryItem, String msg, Object[] tmp, String varifyable)
	{
		return (sampleB==null)?"":sampleB.varify(source, target, scripted, monster, primaryItem, secondaryItem, msg, tmp, varifyable);
	}

	@Override
	public String functify(PhysicalAgent scripted, MOB source, Environmental target, MOB monster, Item primaryItem,
							Item secondaryItem, String msg, Object[] tmp, String evaluable)
							{
		return (sampleB==null)?"":sampleB.functify(scripted, source, target, monster, primaryItem, secondaryItem, msg, tmp, evaluable);
	}
}
