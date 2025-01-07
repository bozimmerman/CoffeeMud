package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.MiniJSON.MJSONException;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.TimeManager;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2024-2024 Bo Zimmerman

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
public class Prayer_DivineQuest extends Prayer
{

	@Override
	public String ID()
	{
		return "Prayer_DivineQuest";
	}

	private final static String	localizedName	= CMLib.lang().L("Divine Quest");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	protected int overrideMana()
	{
		return 1;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Divine Quest)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_BLESSING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NEUTRAL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public void unInvoke()
	{
		if((quest1 != null)
		&&(quest1.running()
			||(CMLib.quests().fetchQuest(quest1.name())==quest1)))
		{
			quest1.stopQuest();
			quest1.enterDormantState();
			CMLib.quests().delQuest(quest1);
			if(affected instanceof MOB)
				((MOB)affected).tell("You have failed the divine quest.");
		}
		else
		if(affected instanceof MOB)
			((MOB)affected).tell("You have failed the divine quest.");
		super.unInvoke();
	}

	protected Quest quest1 = null;
	protected MiniJSON.JSONObject data=null;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		final Physical affected=this.affected;
		if(!(affected instanceof MOB))
			return tickUninvoke();
		final MOB mob=(MOB)affected;
		if(quest1 != null)
		{
			if(!quest1.wasWinner(mob.Name()))
			{
				if(!quest1.running())
					return tickUninvoke();
				// not won, but still running, so keep going...
				return true;
			}
			quest1.stopQuest();
			quest1.enterDormantState();
			CMLib.quests().delQuest(quest1);
			mob.tell("You have successfully completed the divine quest.");
			mob.delEffect(this);
			this.setAffectedOne(null);
			return tickUninvoke();
		}
		else
			return tickUninvoke();
	}

	protected boolean tickUninvoke()
	{
		unInvoke();
		return false;
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		if((newMiscText.length()>0)
		&&(newMiscText.startsWith("{")))
		{
			try
			{
				data=new MiniJSON().parseObject(newMiscText);
				quest1=CMLib.quests().fetchQuest(data.getCheckedString("quest1"));
			}
			catch (final MJSONException e)
			{
				Log.errOut(e);
			}
		}
	}

	public Quest deviseAndStartQuest(final MOB mob, final MOB targetM, final Map<String,Object> definedIDs)
	{
		final Map<String,Object> origDefined=new XHashtable<String,Object>(definedIDs);
		int maxAttempts=5;
		while((--maxAttempts)>=0)
		{
			try
			{
				definedIDs.clear();
				definedIDs.putAll(origDefined);
				final StringBuffer xml = Resources.getFileResource("randareas/example.xml", true);
				if((xml==null)||(xml.length()==0))
				{
					Log.errOut("Unable to generate a quest for "+targetM.name()+" because file not found: randareas/example.xml");
					return null;
				}
				final List<XMLLibrary.XMLTag> xmlRoot = CMLib.xml().parseAllXML(xml);
				if(!definedIDs.containsKey("QUEST_CRITERIA"))
					definedIDs.put("QUEST_CRITERIA", "-NAME \"+"+targetM.Name()+"\" -NPC");
				definedIDs.put("DURATION", ""+CMProps.getTicksPerHour());
				definedIDs.put("EXPIRATION", ""+CMProps.getTicksPerHour());
				definedIDs.put("MULTIAREA", "YES");
				final Map<String,Object> preDefined=new XHashtable<String,Object>(definedIDs);
				CMLib.percolator().buildDefinedIDSet(xmlRoot,definedIDs, preDefined.keySet());
				final String idName = "ALL_QUESTS";
				if((!(definedIDs.get(idName) instanceof XMLTag))
				||(!((XMLTag)definedIDs.get(idName)).tag().equalsIgnoreCase("quest")))
				{
					Log.errOut(L("The quest id '@x1' has not been defined in the data file for @x2.",idName,targetM.name()));
					return null;
				}
				final XMLTag piece=(XMLTag)definedIDs.get(idName);
				try
				{
					CMLib.percolator().checkRequirements(piece, definedIDs);
				}
				catch(final CMException cme)
				{
					Log.errOut(L("Required ids for @x1 were missing: @x2: for @x3",idName,cme.getMessage(),targetM.name()));
					return null;
				}
				String s=CMLib.percolator().buildQuestScript(piece, definedIDs, targetM);
				if(s.length()==0)
					throw new CMException("Failed to create any sort of quest at all! WTF!!");
				CMLib.percolator().postProcess(definedIDs);
				if((!definedIDs.containsKey("QUEST_ID"))
				||(!(definedIDs.get("QUEST_ID") instanceof String)))
					throw new CMException("Unable to create your quest because a quest_id was not generated");
				final Quest Q=(Quest)CMClass.getCommon("DefaultQuest");
				s=CMStrings.replaceFirst(s, "set mob reselect", "set pcmob reselect");
				s=CMStrings.replaceFirst(s, "give script LOAD=", "give script PLAYEROK LOAD=");
				Q.setScript(s,true);
				if((Q.name().trim().length()==0)||(Q.duration()<0))
					throw new CMException("Unable to create your quest.  Please consult the log.");
				//mob.tell("Generated quest '"+Q.name()+"'");
				final Quest badQ=CMLib.quests().fetchQuest(Q.name());
				if(badQ!=null)
					throw new CMException("Unable to create your quest.  One of that name already exists!");
				Log.sysOut("Generate",targetM.Name()+" created quest '"+Q.name()+"' via "+ID());
				CMLib.quests().addQuest(Q);
				if(!Q.running())
				{
					if(!Q.startQuest())
					{
						CMLib.quests().delQuest(Q);
						throw new CMException("Unable to start the quest.  Something went wrong.  Perhaps the problem was logged?");
					}
				}
				Q.setCopy(true);
				final CMMsg msg=CMClass.getMsg(targetM, targetM.location(),null, CMMsg.MSG_ENTER, null);
				targetM.location().send(targetM, msg);
				return Q;
			}
			catch(final CMException cme)
			{
				if(Log.debugChannelOn() && CMSecurity.isDebugging(DbgFlag.MUDPERCOLATOR))
					Log.debugOut(cme);
				else
					Log.errOut(cme.getMessage());
			}
		}
		Log.errOut(L("Failed to finish creating a quest for @x1",targetM.name()));
		return null;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final List<String> mobCommands=new XVector<String>(commands.get(commands.size()-1));
		final MOB targetM=this.getTarget(mob, mobCommands, givenTarget);
		if(targetM==null)
			return false;
		if(!targetM.isPlayer())
		{
			mob.tell(L("@x1 does not look ready for such a task.",targetM.Name()));
			return false;
		}
		final String deityName;
		if(mob instanceof Deity)
			deityName = mob.Name();
		else
		if(!auto)
		{
			deityName=mob.charStats().getWorshipCharID();
			if(deityName.length()==0)
			{
				mob.tell(L("You must worship a deity to begin the imbuing.",targetM.Name()));
				return false;
			}
			if(!targetM.charStats().getWorshipCharID().equals(deityName))
			{
				mob.tell(L("@x1 must also worship @x2 to begin the imbuing.",targetM.Name(),deityName));
				return false;
			}
		}
		else
			deityName = CMLib.map().deity().Name();

		// lose all the mana!
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,targetM,this,verbalCastCode(mob,targetM,auto),
					((mob!=targetM)&&(mob instanceof Deity))?L("^S<S-NAME> point(s) at <T-NAMESELF>.^?"):
						auto?"":L("^S<S-NAME> @x1 while pointing at <T-NAMESELF>.^?",super.prayWord(mob)));
			if(targetM.location().okMessage(mob,msg))
			{
				targetM.location().send(mob,msg);
				final MiniJSON.JSONObject obj = new MiniJSON.JSONObject();
				final Map<String,Object> definedIDs = new Hashtable<String,Object>();
				definedIDs.put("AREA_NAME", targetM.location().getArea().Name());
				definedIDs.put("target_level".toUpperCase(), ""+targetM.phyStats().level());
				definedIDs.put("AGGRESSION", "YES");
				definedIDs.put("target_is_aggressive".toUpperCase(), "YES");
				definedIDs.put("TEMPLATE", "auto");
				definedIDs.put("DEITYNAME", deityName);
				final Quest q1=deviseAndStartQuest(mob, targetM, definedIDs);
				if(q1 == null)
				{
					beneficialWordsFizzle(mob,targetM,L("<T-NAME> fail(s) entirely."));
					return false;
				}
				obj.putString("template1", definedIDs.get("TEMPLATE").toString());
				obj.putString("quest1", q1.name());
				final Prayer_DivineQuest dA=(Prayer_DivineQuest)beneficialAffect(mob,targetM,asLevel,(int)(CMProps.getTicksPerHour()*2));
				if(dA!=null)
					dA.setMiscText(obj.toString());
				/*
				for(final Enumeration<ScriptingEngine> e= targetM.scripts();e.hasMoreElements();)
				{
					final ScriptingEngine E = e.nextElement();
					if((E!=null)&&(E.defaultQuestName().equals(q1.name())))
					{
						E.tick(targetM, Tickable.TICKID_MOB);
						final String instr = E.getVar(targetM.Name(), "INSTRUCTIONS");
						CMLib.commands().postSay(mob, targetM, L("@x1 has a task for you! @x2",deityName,instr));
					}
				}
				*/
			}
		}
		else
			beneficialWordsFizzle(mob,targetM,L("<S-NAME> @x1 while pointing at <T-NAMESELF>, and looking very frustrated.",super.prayWord(mob)));
		// return whether it worked
		return success;
	}
}
