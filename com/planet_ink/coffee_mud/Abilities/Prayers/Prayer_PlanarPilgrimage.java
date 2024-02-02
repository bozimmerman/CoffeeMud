package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.MiniJSON.MJSONException;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.PlanarAbility.PlanarVar;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.Align;
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
   Copyright 2020-2024 Bo Zimmerman

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
public class Prayer_PlanarPilgrimage extends Prayer
{

	@Override
	public String ID()
	{
		return "Prayer_PlanarPilgrimage";
	}

	private final static String	localizedName	= CMLib.lang().L("Planar Pilgrimage");

	@Override
	public String name()
	{
		return localizedName;
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
	protected int overrideMana()
	{
		return Ability.COST_ALL;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Planar Pilgrimage)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_INFLUENTIAL;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NOORDERING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
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
				((MOB)affected).tell("You have failed the planar pilgrimage.");
		}
		else
		if(affected instanceof MOB)
			((MOB)affected).tell("You have failed the planar pilgrimage.");
		super.unInvoke();
	}

	protected final static LimitedTreeSet<String> lastUsed = new LimitedTreeSet<String>(TimeManager.MILI_DAY, 1000, true);

	protected Quest			quest1		= null;
	protected String		planeName	= null;
	protected String		deityName	= null;
	protected QuestTemplate	template	= null;

	protected volatile long timeToNextCast=0;

	@Override
	protected long getTimeOfNextCast()
	{
		return timeToNextCast;
	}

	@Override
	protected int getTicksBetweenCasts()
	{
		return (int)CMProps.getTicksPerDay();
	}

	@Override
	protected void setTimeOfNextCast(final long absoluteTime)
	{
		timeToNextCast=absoluteTime;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		final Physical affected=this.affected;
		if(!(affected instanceof MOB))
			return tickUninvoke();
		final MOB mob=(MOB)affected;
		if(quest1 == null)
		{
			if((planeName == null)||(deityName==null)||(template==null)||(invoker()==null))
				return tickUninvoke();
			final Area A=CMLib.map().areaLocation(mob);
			if((A==null)||(!planeName.equalsIgnoreCase(CMLib.flags().getPlaneOfExistence(A))))
				return true;
			final Map<String,Object> definedIDs = new Hashtable<String,Object>();
			final Quest q1=deviseAndStartQuest(invoker(), mob, definedIDs,this.template);
			if(q1 == null)
			{
				mob.tell(L("The pilgrimage has failed entirely"));
				return tickUninvoke();
			}
			quest1=q1;
			return true;
		}
		else
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
			mob.tell("You have successfully completed the planar pilgrimage.");
			final MOB invokerM=invoker();
			if(invokerM!=null)
			{
				final Prayer_PlanarPilgrimage realA=(Prayer_PlanarPilgrimage)invokerM.fetchAbility(ID());
				if(realA!=null)
					lastUsed.add(mob.Name());
				// final award
				final Ability awardA=new StdAbility()
				{
					final String dName = deityName;
					@Override
					public String ID()
					{
						return "FavoredOf"+CMStrings.capitalizeAllFirstLettersAndLower(dName).replace(' ','_');
					}

					@Override
					public String name()
					{
						return "Favored of "+dName;
					}

					@Override
					public String Name()
					{
						return name();
					}

					@Override
					public String displayText()
					{
						return "(Favored of "+dName+")";
					}

					@Override
					public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
					{
					}

					@Override
					public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
					{
						for(final int stat : CharStats.CODES.BASECODES())
							affectableStats.setStat(stat, affectableStats.getStat(stat)+2);
					}

					@Override
					public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
					{
						affectableMaxState.setHitPoints(affectableMaxState.getHitPoints()+(10*affectedMob.phyStats().level()));
						affectableMaxState.setMana(affectableMaxState.getMana()+(5*affectedMob.phyStats().level()));
						affectableMaxState.setMovement(affectableMaxState.getMovement()+(5*affectedMob.phyStats().level()));
					}
				};
				awardA.startTickDown(invokerM, mob, (int)CMProps.getTicksPerDay());
			}
			mob.delEffect(this);
			this.setAffectedOne(null);
			mob.recoverCharStats();
			mob.recoverMaxState();
			mob.recoverPhyStats();

			return tickUninvoke();
		}
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
		if(newMiscText.length()>0)
		{
			planeName = CMParms.getParmStr(newMiscText, "PLANE", this.planeName);
			deityName = CMParms.getParmStr(newMiscText, "DEITY", this.deityName);
			final String code = CMParms.getParmStr(newMiscText, "TEMPLATE", (this.template == null) ? null : this.template.name());
			template = (QuestTemplate)CMath.s_valueOf(QuestTemplate.class, code);
		}
	}

	protected Integer findMaxMat(final Area parentArea)
	{
		final int[] acceptableMaterials = new int[] {
				RawMaterial.MATERIAL_METAL, RawMaterial.MATERIAL_WOODEN, RawMaterial.MATERIAL_VEGETATION,
				RawMaterial.MATERIAL_ROCK, RawMaterial.MATERIAL_LIQUID
		};
		final Map<Integer,int[]> materialCounts=new TreeMap<Integer,int[]>();
		for(final Enumeration<Room> e=parentArea.getFilledProperMap();e.hasMoreElements();)
		{
			final Room R=e.nextElement();
			if((R!=null)
			&&(R.myResource()>0))
			{
				final Integer material = Integer.valueOf(R.myResource()&RawMaterial.MATERIAL_MASK);
				if(CMParms.contains(acceptableMaterials, material.intValue()))
				{
					if(!materialCounts.containsKey(material))
						materialCounts.put(material, new int[1]);
					materialCounts.get(material)[0]++;
				}
			}
		}
		int materialCount=0;
		Integer mat = null;
		for(final Integer key : materialCounts.keySet())
		{
			if(materialCounts.get(key)[0]>materialCount)
			{
				materialCount=materialCounts.get(key)[0];
				mat=key;
			}
		}
		return mat;
	}

	public Quest deviseAndStartQuest(final MOB mob, final MOB targetM, final Map<String,Object> definedIDs, final QuestTemplate template)
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
				final Area planeArea=CMLib.map().areaLocation(targetM);
				final Area parentArea=(planeArea instanceof SubArea)?((SubArea)planeArea).getSuperArea():null;
				if((planeArea==null)||(parentArea==null))
				{
					Log.errOut("Unable to generate a quest for "+targetM.name()+" because no parent area");
					return null;
				}
				final List<XMLLibrary.XMLTag> xmlRoot = CMLib.xml().parseAllXML(xml);
				if(!definedIDs.containsKey("QUEST_CRITERIA"))
					definedIDs.put("QUEST_CRITERIA", "-NAME \"+"+targetM.Name()+"\" -NPC");
				definedIDs.put("DURATION", ""+tickDown);
				definedIDs.put("EXPIRATION", ""+tickDown);
				definedIDs.put("TARGETAREA_NAME", planeArea.Name());
				definedIDs.put("TARGET_AREA_NAME", this.planeName);
				final String name1Code = deityName.toUpperCase().trim().replace(' ', '_');
				final Faction deity1F=CMLib.factions().getFaction("DEITY_"+name1Code);
				if(deity1F!=null)
				{
					definedIDs.put("NUMFACTION", "1000");
					definedIDs.put("FACTION", deity1F.factionID());
				}
				else
				{
					definedIDs.put("NUMFACTION", "");
					definedIDs.put("FACTION", "");
				}
				planeName=CMStrings.capitalizeAllFirstLettersAndLower(planeName);
				if(mob != targetM)
				{
					definedIDs.put("DEITYNAME", deityName);
					definedIDs.put("MULTIAREA", "YES");
					definedIDs.put("SOURCE_NAME", mob.Name());
					definedIDs.put("AREA_NAME", CMLib.map().areaLocation(mob).Name());
					switch(template)
					{
					case COLLECT_GROUND: //done
						definedIDs.put("TEMPLATE", "normal_collect2");
						definedIDs.put("HOLDER_AREAS", "\""+planeArea.Name()+"\"");
						definedIDs.put("HOLDER_ROOMS", "ALL");
						definedIDs.put("itemname".toUpperCase(), L("a fragment of @x1",deityName));
						definedIDs.put("item_level".toUpperCase(), "1");
						definedIDs.put("item_material".toUpperCase(), "ENERGY");
						break;
					case COLLECT_MOBS: //done
						definedIDs.put("TEMPLATE", "normal_collect1");
						definedIDs.put("HOLDERS_MASK","-HOME \"+"+planeArea.Name()+"\" -PLAYER");
						definedIDs.put("target_level".toUpperCase(), ""+planeArea.getIStat(Area.Stats.MED_LEVEL));
						definedIDs.put("target_name".toUpperCase(), L("a denizen of @x1",planeName));
						definedIDs.put("itemname".toUpperCase(), L("a fragment of @x1",deityName));
						definedIDs.put("item_level".toUpperCase(), "1");
						definedIDs.put("item_material".toUpperCase(), "ENERGY");
						// ------ problem because it requires scanning the area to select mobs to collect from
						definedIDs.put("AGGRESSION", "YES");
						definedIDs.put("target_is_aggressive".toUpperCase(), "YES");
						break;
					/*
					case COLLECT_RESOURCES: // done
					{
						definedIDs.put("TEMPLATE", "normal_collect4");
						final Integer mat=findMaxMat(parentArea);
						if(mat==null)
						{
							Log.errOut("Unable to generate a quest for "+targetM.name()+" because no material found");
							return null;
						}
						switch(mat.intValue())
						{
						case RawMaterial.MATERIAL_METAL:
							definedIDs.put("TARGETSKILL", "Mining"); break;
						case RawMaterial.MATERIAL_WOODEN:
							definedIDs.put("TARGETSKILL", "Chopping"); break;
						case RawMaterial.MATERIAL_VEGETATION:
							definedIDs.put("TARGETSKILL", "Foraging"); break;
						case RawMaterial.MATERIAL_ROCK:
							definedIDs.put("TARGETSKILL", "Mining"); break;
						case RawMaterial.MATERIAL_LIQUID:
							definedIDs.put("TARGETSKILL", "Drilling"); break;
						}
						break;
					}
					*/
					case DEFEAT_CAPTURE: // done
						definedIDs.put("TEMPLATE", "normal_capture1");
						definedIDs.put("targetname".toUpperCase(), L("a denizen of @x1",planeName));
						definedIDs.put("CAPTURABLES_MASK","-HOME \"+"+planeArea.Name()+"\" -PLAYER");
						definedIDs.put("CAPTUREABLES_AREAS","\""+planeArea.Name()+"\"");
						definedIDs.put("target_level".toUpperCase(), ""+planeArea.getIStat(Area.Stats.MED_LEVEL));
						definedIDs.put("AGGRESSION", "YES");
						definedIDs.put("target_is_aggressive".toUpperCase(), "YES");
						break;
					case DELIVERY: // done
						definedIDs.put("TEMPLATE", "normal_delivery1");
						definedIDs.put("target_name".toUpperCase(), L("a denizen of @x1",planeName));
						definedIDs.put("DELIVEREE_MASK","-HOME \"+"+planeArea.Name()+"\" -PLAYER");
						definedIDs.put("itemname".toUpperCase(), L("a divine decree of @x1",deityName));
						definedIDs.put("item_level".toUpperCase(), "1");
						definedIDs.put("item_material".toUpperCase(), "PAPER");
						break;
					case DISPEL: // done
						definedIDs.put("TEMPLATE", "normal_dispel1");
						definedIDs.put("target_name".toUpperCase(), L("a denizen of @x1",deityName));
						definedIDs.put("target_level".toUpperCase(), ""+planeArea.getIStat(Area.Stats.MED_LEVEL));
						definedIDs.put("HELPABLES_MASK","-HOME \"+"+planeArea.Name()+"\" -PLAYER");
						break;
					/*
					case ESCORT: // done
						definedIDs.put("TEMPLATE", "normal_escort2");
						definedIDs.put("target_name".toUpperCase(), L("a missionary of @x1",deityName));
						definedIDs.put("attackername".toUpperCase(), L("an enemy of @x1",deityName));
						definedIDs.put("ATTACKER_PCT_CHANCE", "10");
						definedIDs.put("AGGRESSION", "YES");
						definedIDs.put("target_is_aggressive".toUpperCase(), "YES");
						break;
					*/
					case KILL_ELITE: // done
						definedIDs.put("TEMPLATE", "normal_killer1");
						definedIDs.put("AGGRESSION", "YES");
						definedIDs.put("target_is_aggressive".toUpperCase(), "YES");
						definedIDs.put("target_level".toUpperCase(), ""+planeArea.getIStat(Area.Stats.MED_LEVEL));
						definedIDs.put("num_targets".toUpperCase(), "1");
						definedIDs.put("KILLABLES_MASK","-HOME \"+"+planeArea.Name()+"\" -PLAYER -EFFECTS +Prop_ShortEffects");
						definedIDs.put("target_name".toUpperCase(), L("an elite denizen of @x1",planeName));
						break;
					case KILL_OFFICER: // done
						definedIDs.put("TEMPLATE", "normal_killer1");
						definedIDs.put("AGGRESSION", "YES");
						definedIDs.put("target_is_aggressive".toUpperCase(), "YES");
						definedIDs.put("KILLABLES_MASK","-HOME \"+"+planeArea.Name()+"\" -PLAYER");
						definedIDs.put("target_level".toUpperCase(), ""+planeArea.getIStat(Area.Stats.MED_LEVEL));
						definedIDs.put("target_name".toUpperCase(), L("a denizen of @x1",planeName));
						break;
					case PEACEFUL_CAPTURE:
						definedIDs.put("TEMPLATE", "normal_capture1");
						definedIDs.put("targetname".toUpperCase(), L("a missionary of @x1",deityName));
						definedIDs.put("CAPTUREABLES_AREAS","\""+planeArea.Name()+"\"");
						definedIDs.put("CAPTURABLES_MASK","-HOME \"+"+planeArea.Name()+"\" -NAME \"+"+L("a missionary of @x1",deityName)+"\"");
						definedIDs.put("AGGRESSION", "NO");
						definedIDs.put("NUM_TARGETS", "1");
						definedIDs.put("target_is_aggressive".toUpperCase(), "NO");
						break;
					/*
					case TRAVEL: //DONE
					{
						final List<Room> choices=new ArrayList<Room>();
						for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
						{
							final Room R1=targetM.location().getRoomInDir(d);
							final Exit E1=targetM.location().getExitInDir(d);
							if((R1!=null)&&(E1!=null)&&(R1.roomID().length()>0)&&(R1.getArea()==planeArea))
								choices.add(R1);
						}
						if(choices.size()==0)
						{
							Log.errOut("Unable to generate a quest for "+targetM.name()+" because no exits found");
							return null;
						}
						final Room targetR=choices.get(CMLib.dice().roll(1, choices.size(), -1));
						final String choiceID=targetR.roomID();
						definedIDs.put("TEMPLATE", "normal_travel1");
						definedIDs.put("TARGET_ROOM_ID", choiceID);
						definedIDs.put("TARGETROOMID", choiceID);
						definedIDs.put("TARGET_NAME", targetR.displayText(targetM));
						break;
					}
					*/
					default:
						break;

					}
					definedIDs.put("QUEST_TEMPLATE", definedIDs.get("TEMPLATE"));
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
					String s=CMLib.percolator().buildQuestScript(piece, definedIDs, mob);
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
					final StringBuilder entry=new StringBuilder("");
					entry.append(CMStrings.padRight(L("Quest"), 10)).append(": ").append(Q.displayName()).append("\n\r");
					final MOB questGiverM=Q.getQuestMob(1);
					if(questGiverM!=null)
					{
						final Room questGiverR=questGiverM.location();
						entry.append(CMStrings.padRight(L("Giver"), 10)).append(": ").append(questGiverM.name())
								.append(L(" at")).append(": ").append(questGiverR.displayText(mob)).append("\n\r");
						if(questGiverR.getArea()!=null)
							entry.append(CMStrings.padRight(L("   In"), 10)).append(": ").append(questGiverR.getArea().name(mob)).append("\n\r");
					}
					if(Q.instructions().length()>0)
						entry.append(CMStrings.padRight(L("Descrip."), 10)).append(": ").append(Q.instructions()).append("\n\r");
					targetM.tell(entry.toString());
					Q.acceptQuest(targetM);
					return Q;
				}
				else
				{
					switch(template)
					{
					case COLLECT_GROUND: //done
						definedIDs.put("TEMPLATE", "auto_collect2");
						definedIDs.put("HOLDER_AREAS", "\""+planeArea.Name()+"\"");
						definedIDs.put("HOLDER_ROOMS", "ALL");
						definedIDs.put("itemname".toUpperCase(), L("a fragment of @x1",deityName));
						definedIDs.put("item_level".toUpperCase(), "1");
						definedIDs.put("item_material".toUpperCase(), "ENERGY");
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("@x1 wants you to collect the lost fragments of @x1 from @x2. ${reason_short}.",deityName,planeName));
						break;
					case COLLECT_MOBS: //done
						definedIDs.put("TEMPLATE", "auto_collect1");
						definedIDs.put("HOLDERS_MASK","-HOME \"+"+planeArea.Name()+"\" -PLAYER");
						definedIDs.put("target_name".toUpperCase(), L("a denizen of @x1",planeName));
						definedIDs.put("target_level".toUpperCase(), ""+planeArea.getIStat(Area.Stats.MED_LEVEL));
						definedIDs.put("itemname".toUpperCase(), L("a fragment of @x1",deityName));
						definedIDs.put("item_level".toUpperCase(), "1");
						definedIDs.put("item_material".toUpperCase(), "ENERGY");
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("@x1 wants you to collect the lost fragments of @x1 from denizens of @x2. ${reason_short}.",deityName,planeName));
						// ------ problem because it requires scanning the area to select mobs to collect from
						definedIDs.put("AGGRESSION", "YES");
						definedIDs.put("target_is_aggressive".toUpperCase(), "YES");
						break;
					/*
					case COLLECT_RESOURCES: // done
					{
						definedIDs.put("TEMPLATE", "auto_collect4");
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("@x1 wants you to gather valuable resources from @x2. ${reason_short}.",deityName,planeName));
						final Integer mat=findMaxMat(parentArea);
						if(mat==null)
						{
							Log.errOut("Unable to generate a quest for "+targetM.name()+" because no material found");
							return null;
						}
						switch(mat.intValue())
						{
						case RawMaterial.MATERIAL_METAL:
							definedIDs.put("TARGETSKILL", "Mining"); break;
						case RawMaterial.MATERIAL_WOODEN:
							definedIDs.put("TARGETSKILL", "Chopping"); break;
						case RawMaterial.MATERIAL_VEGETATION:
							definedIDs.put("TARGETSKILL", "Foraging"); break;
						case RawMaterial.MATERIAL_ROCK:
							definedIDs.put("TARGETSKILL", "Mining"); break;
						case RawMaterial.MATERIAL_LIQUID:
							definedIDs.put("TARGETSKILL", "Drilling"); break;
						}
						break;
					}
					*/
					case DEFEAT_CAPTURE: // done
						definedIDs.put("TEMPLATE", "auto_capture1");
						definedIDs.put("targetname".toUpperCase(), L("a denizen of @x1",planeName));
						definedIDs.put("CAPTURABLES_MASK","-HOME \"+"+planeArea.Name()+"\" -PLAYER");
						definedIDs.put("CAPTUREABLES_AREAS","\""+planeArea.Name()+"\"");
						definedIDs.put("target_level".toUpperCase(), ""+planeArea.getIStat(Area.Stats.MED_LEVEL));
						definedIDs.put("AGGRESSION", "YES");
						definedIDs.put("target_is_aggressive".toUpperCase(), "YES");
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("@x1 wants you to capture denizens of @x2 for interrogation. ${reason_short}.",deityName,planeName));
						break;
					case DELIVERY: // done
						definedIDs.put("TEMPLATE", "auto_delivery1");
						definedIDs.put("target_name".toUpperCase(), L("a denizen of @x1",planeName));
						definedIDs.put("DELIVEREE_MASK","-HOME \"+"+planeArea.Name()+"\" -PLAYER");
						definedIDs.put("itemname".toUpperCase(), L("a divine decree of @x1",deityName));
						definedIDs.put("item_level".toUpperCase(), "1");
						definedIDs.put("item_material".toUpperCase(), "PAPER");
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("@x1 wants you to deliver a divine decree to loyal denizens of @x2. ${reason_short}.",deityName,planeName));
						break;
					case DISPEL: // done
						definedIDs.put("TEMPLATE", "auto_dispel1");
						definedIDs.put("target_name".toUpperCase(), L("a denizen of @x1",deityName));
						definedIDs.put("target_level".toUpperCase(), ""+planeArea.getIStat(Area.Stats.MED_LEVEL));
						definedIDs.put("HELPABLES_MASK","-HOME \"+"+planeArea.Name()+"\" -PLAYER");
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("@x1 wants you to aid the pious denizens of @x2. ${reason_short}.",deityName,planeName));
						break;
					/*
					case ESCORT: // done
						definedIDs.put("TEMPLATE", "auto_escort2");
						definedIDs.put("target_name".toUpperCase(), L("a missionary of @x1",deityName));
						definedIDs.put("attackername".toUpperCase(), L("an enemy of @x1",deityName));
						definedIDs.put("ATTACKER_PCT_CHANCE", "10");
						definedIDs.put("AGGRESSION", "YES");
						definedIDs.put("target_is_aggressive".toUpperCase(), "YES");
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("@x1 wants you to escort the new missionary to @x2. ${reason_short}.",deityName,planeName));
						break;
					*/
					case KILL_ELITE: // done
						definedIDs.put("TEMPLATE", "auto_killer1");
						definedIDs.put("AGGRESSION", "YES");
						definedIDs.put("target_is_aggressive".toUpperCase(), "YES");
						definedIDs.put("num_targets".toUpperCase(), "1");
						definedIDs.put("KILLABLES_MASK","-HOME \"+"+planeArea.Name()+"\" -PLAYER -EFFECTS +Prop_ShortEffects");
						definedIDs.put("target_level".toUpperCase(), ""+planeArea.getIStat(Area.Stats.MED_LEVEL));
						definedIDs.put("target_name".toUpperCase(), L("an elite denizen of @x1",planeName));
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("@x1 wants you to destroy a powerful enemy in @x2. ${reason_short}.",deityName,planeName));
						break;
					case KILL_OFFICER: // done
						definedIDs.put("TEMPLATE", "auto_killer1");
						definedIDs.put("AGGRESSION", "YES");
						definedIDs.put("target_is_aggressive".toUpperCase(), "YES");
						definedIDs.put("KILLABLES_MASK","-HOME \"+"+planeArea.Name()+"\" -PLAYER");
						definedIDs.put("target_name".toUpperCase(), L("a denizen of @x1",planeName));
						definedIDs.put("target_level".toUpperCase(), ""+planeArea.getIStat(Area.Stats.MED_LEVEL));
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("@x1 wants you to thin the ranks of enemies in @x2. ${reason_short}.",deityName,planeName));
						break;
					case PEACEFUL_CAPTURE:
						definedIDs.put("TEMPLATE", "auto_capture1");
						definedIDs.put("targetname".toUpperCase(), L("a missionary of @x1",deityName));
						definedIDs.put("CAPTURABLES_MASK","-HOME \"+"+planeArea.Name()+"\" -NAME \"+"+L("a missionary of @x1",deityName)+"\"");
						definedIDs.put("CAPTUREABLES_AREAS","\""+planeArea.Name()+"\"");
						definedIDs.put("AGGRESSION", "NO");
						definedIDs.put("NUM_TARGETS", "1");
						definedIDs.put("target_is_aggressive".toUpperCase(), "NO");
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("@x1 wants you to find and collect the missionary to @x2 and return them. ${reason_short}.",deityName,planeName));
						break;
					/*
					case TRAVEL: //DONE
					{
						final List<Room> choices=new ArrayList<Room>();
						for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
						{
							final Room R1=targetM.location().getRoomInDir(d);
							final Exit E1=targetM.location().getExitInDir(d);
							if((R1!=null)&&(E1!=null)&&(R1.roomID().length()>0)&&(R1.getArea()==planeArea))
								choices.add(R1);
						}
						if(choices.size()==0)
						{
							Log.errOut("Unable to generate a quest for "+targetM.name()+" because no exits found");
							return null;
						}
						final Room targetR=choices.get(CMLib.dice().roll(1, choices.size(), -1));
						final String choiceID=targetR.roomID();
						definedIDs.put("TEMPLATE", "auto_travel1");
						definedIDs.put("TARGET_ROOM_ID", choiceID);
						definedIDs.put("TARGETROOMID", choiceID);
						definedIDs.put("TARGET_NAME", targetR.displayText(targetM));
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("@x1 wants you to travel to @x2 and make your presence known. ${reason_short}.",deityName,planeName));
						break;
					}
					*/
					default:
						break;

					}
					definedIDs.put("QUEST_TEMPLATE", definedIDs.get("TEMPLATE"));
					final Map<String,Object> preDefined=new XHashtable<String,Object>(definedIDs);
					CMLib.percolator().buildDefinedIDSet(xmlRoot,definedIDs, preDefined.keySet());
					final String idName = "ALL_QUESTS";
					if((!(definedIDs.get(idName) instanceof XMLTag))
					||(!((XMLTag)definedIDs.get(idName)).tag().equalsIgnoreCase("quest")))
					{
						Log.errOut(L("The quest id '@x1' has not been defined in the data file for @x2.",idName,affected.name()));
						return null;
					}
					final XMLTag piece=(XMLTag)definedIDs.get(idName);
					try
					{
						CMLib.percolator().checkRequirements(piece, definedIDs);
					}
					catch(final CMException cme)
					{
						Log.errOut(L("Required ids for @x1 were missing: @x2: for @x3",idName,cme.getMessage(),affected.name()));
						return null;
					}
					final Modifiable obj = null;
					final String s=CMLib.percolator().buildQuestScript(piece, definedIDs, obj);
					if(s.length()==0)
						throw new CMException("Failed to create any sort of quest at all! WTF!!");
					CMLib.percolator().postProcess(definedIDs);
					if((!definedIDs.containsKey("QUEST_ID"))
					||(!(definedIDs.get("QUEST_ID") instanceof String)))
						throw new CMException("Unable to create your quest because a quest_id was not generated");
					final Quest Q=(Quest)CMClass.getCommon("DefaultQuest");
					Q.setScript(s,true);
					if((Q.name().trim().length()==0)||(Q.duration()<0))
						throw new CMException("Unable to create your quest.  Please consult the log.");
					//mob.tell("Generated quest '"+Q.name()+"'");
					final Quest badQ=CMLib.quests().fetchQuest(Q.name());
					if(badQ!=null)
						throw new CMException("Unable to create your quest.  One of that name already exists!");
					Log.sysOut("Generate",affected.Name()+" created quest '"+Q.name()+"' via "+ID());
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
					return Q;
				}
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

	protected int getXPCost(final Ability imbuePrayerA)
	{
		int experienceToLose=1000;
		experienceToLose+=(100*CMLib.ableMapper().lowestQualifyingLevel(imbuePrayerA.ID()));
		return experienceToLose;
	}

	private enum QuestReq
	{
		SAME, SIMILAR, DISSIMILAR, OPPOSED,
		OUTER, INNER
	}

	private enum QuestTemplate
	{
		PEACEFUL_CAPTURE(QuestReq.SIMILAR),
		DEFEAT_CAPTURE(QuestReq.DISSIMILAR),
		COLLECT_GROUND(QuestReq.OUTER),
		COLLECT_MOBS(QuestReq.OUTER,QuestReq.DISSIMILAR),
		//COLLECT_RESOURCES(QuestReq.INNER),
		DELIVERY(QuestReq.INNER),
		DISPEL(QuestReq.OUTER,QuestReq.SIMILAR),
		//ESCORT(QuestReq.SIMILAR),
		KILL_ELITE(QuestReq.OUTER,QuestReq.DISSIMILAR),
		KILL_OFFICER(QuestReq.OUTER,QuestReq.DISSIMILAR),
		//TRAVEL
		;

		public final QuestReq[] req;
		private QuestTemplate(final QuestReq... req)
		{
			this.req=req;
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB targetM=this.getTarget(mob, commands, null);
		if(targetM==null)
			return false;
		if(targetM==mob)
		{
			//special case
			//mob.tell(L("Your deity will not allow you to send yourself on such a quest."));
			//return false;
		}
		if(!targetM.isPlayer())
		{
			mob.tell(L("@x1 does not look ready for such a task.",targetM.Name()));
			return false;
		}
		if(lastUsed.contains(targetM.Name()))
		{
			mob.tell(L("@x1 must wait a bit before being ready again for such a task.",targetM.Name()));
			return false;
		}

		final String deityName=mob.charStats().getWorshipCharID();
		if((deityName.length()==0)||(mob.charStats().getMyDeity()==null))
		{
			mob.tell(L("You must worship a deity to begin the planar pilgrimage.",targetM.Name()));
			return false;
		}
		if(!targetM.charStats().getWorshipCharID().equals(deityName))
		{
			mob.tell(L("@x1 must worship also worship @x2 to begin the planar pilgrimage.",targetM.Name(),deityName));
			return false;
		}

		final List<String> alignSamePlanes=new ArrayList<String>();
		final List<String> alignSimilarPlanes=new ArrayList<String>();
		final List<String> alignOpposedPlanes=new ArrayList<String>();
		final List<String> alignDissimilarPlanes=new ArrayList<String>();

		final Deity deityM=mob.charStats().getMyDeity();
		final PlanarAbility planarA=(PlanarAbility)CMClass.getAbility("StdPlanarAbility");
		final Faction alignF=CMLib.factions().getFaction(CMLib.factions().getAlignmentID());
		final Faction incliF=CMLib.factions().getFaction(CMLib.factions().getInclinationID());

		final int deityAlignNum=deityM.fetchFaction(CMLib.factions().getAlignmentID());
		Faction.FRange deityAlignRange=null;
		if((alignF!=null)&&(deityAlignNum != Integer.MAX_VALUE))
			deityAlignRange=alignF.fetchRange(deityAlignNum);
		Align deityAlignEquiv=null;
		if(deityAlignRange!=null)
			deityAlignEquiv=deityAlignRange.alignEquiv();
		final int deityIncliNum=deityM.fetchFaction(CMLib.factions().getInclinationID());
		Faction.FRange deityIncliRange=null;
		if((incliF!=null)&&(deityIncliNum != Integer.MAX_VALUE))
			deityIncliRange=incliF.fetchRange(deityIncliNum);
		Align deityIncliEquiv=null;
		if(deityIncliRange!=null)
			deityIncliEquiv=deityIncliRange.alignEquiv();

		final int alignNum=targetM.fetchFaction(CMLib.factions().getAlignmentID());
		Faction.FRange myAlignRange=null;
		if((alignF!=null)&&(alignNum != Integer.MAX_VALUE))
			myAlignRange=alignF.fetchRange(alignNum);
		Align myAlignEquiv=null;
		if(myAlignRange!=null)
			myAlignEquiv=myAlignRange.alignEquiv();
		if((myAlignEquiv != null)
		&&(myAlignRange!=null)
		&&(alignF!=null))
		{
			Faction.FRange planeRange = null;
			for(final String planeID : planarA.getAllPlaneKeys())
			{
				final Map<String,String> planeVars = planarA.getPlanarVars(planeID);
				final String alignNumStr = planeVars.get(PlanarVar.ALIGNMENT.toString());
				if((alignNumStr != null)&&(CMath.isInteger(alignNumStr)))
					planeRange=alignF.fetchRange(CMath.s_int(alignNumStr));
				else
				{
					for(final Enumeration<Faction> f=CMLib.factions().factions();f.hasMoreElements();)
					{
						final Faction F=f.nextElement();
						String facNumStr = planeVars.get(F.factionID());
						if((facNumStr == null)||(!CMath.isInteger(facNumStr)))
							facNumStr = planeVars.get(F.name().toUpperCase());
						if((facNumStr == null)||(!CMath.isInteger(facNumStr)))
							continue;
						planeRange=F.fetchRange(CMath.s_int(facNumStr));
						if((planeRange!=null)
						&&((planeRange.alignEquiv()==Align.GOOD)
							||(planeRange.alignEquiv()==Align.EVIL)
							||(planeRange.alignEquiv()==Align.NEUTRAL)))
							break;
					}
				}
				if((planeRange != null)
				&&(planeRange.alignEquiv()!=null))
				{
					if((planeRange == myAlignRange)
					&&(myAlignRange==deityAlignRange))
						alignSamePlanes.add(planeID);
					if((planeRange.alignEquiv()==myAlignEquiv)
					&&(planeRange.alignEquiv()==deityAlignEquiv))
						alignSimilarPlanes.add(planeID);
					else
					if(deityAlignRange!=null)
					{
						if((CMath.absDiff(planeRange.med(), deityAlignRange.med())<10)
						&&(CMath.absDiff(planeRange.med(), myAlignRange.med())<10))
							alignOpposedPlanes.add(planeID);
						else
						if((planeRange.alignEquiv()!=myAlignEquiv)
						&&(planeRange.alignEquiv()!=deityAlignEquiv))
							alignDissimilarPlanes.add(planeID);
					}
				}
			}
		}

		final int incliNum=targetM.fetchFaction(CMLib.factions().getInclinationID());
		Faction.FRange myIncliRange=null;
		if((incliF!=null)&&(incliNum != Integer.MAX_VALUE))
			myIncliRange=incliF.fetchRange(incliNum);
		Align myIncliEquiv=null;
		if(myIncliRange!=null)
			myIncliEquiv=myIncliRange.alignEquiv();
		if((myIncliEquiv != null)
		&&(myIncliRange!=null)
		&&(incliF!=null))
		{
			Faction.FRange planeRange = null;
			for(final String planeID : planarA.getAllPlaneKeys())
			{
				final Map<String,String> planeVars = planarA.getPlanarVars(planeID);
				for(final Enumeration<Faction> f=CMLib.factions().factions();f.hasMoreElements();)
				{
					final Faction F=f.nextElement();
					String facNumStr = planeVars.get(F.factionID());
					if((facNumStr == null)||(!CMath.isInteger(facNumStr)))
						facNumStr = planeVars.get(F.name().toUpperCase());
					if((facNumStr == null)||(!CMath.isInteger(facNumStr)))
						continue;
					planeRange=F.fetchRange(CMath.s_int(facNumStr));
					if((planeRange!=null)
					&&((planeRange.alignEquiv()==Align.LAWFUL)
						||(planeRange.alignEquiv()==Align.CHAOTIC)
						||(planeRange.alignEquiv()==Align.MODERATE)))
						break;
				}
				if((planeRange != null)
				&&(planeRange.alignEquiv()!=null))
				{
					if((planeRange == myIncliRange)
					&&(myIncliRange==deityIncliRange))
					{
						alignOpposedPlanes.remove(planeID);
						alignDissimilarPlanes.remove(planeID);
					}
					if((planeRange.alignEquiv()==myIncliEquiv)
					&&(planeRange.alignEquiv()==deityIncliEquiv))
					{
						alignSamePlanes.remove(planeID);
						alignOpposedPlanes.remove(planeID);
						alignDissimilarPlanes.remove(planeID);
					}
					else
					if(deityIncliRange!=null)
					{
						if((CMath.absDiff(planeRange.med(), deityIncliRange.med())<10)
						&&(CMath.absDiff(planeRange.med(), myIncliRange.med())<10))
						{
							alignSamePlanes.remove(planeID);
							alignSimilarPlanes.remove(planeID);
							alignDissimilarPlanes.remove(planeID);
						}
						else
						if((planeRange.alignEquiv()!=myIncliEquiv)
						&&(planeRange.alignEquiv()!=deityIncliEquiv))
						{
							alignSamePlanes.remove(planeID);
							alignSimilarPlanes.remove(planeID);
							alignOpposedPlanes.remove(planeID);
						}
					}
				}
			}
		}

		final PairList<String,QuestTemplate> finalChoices = new PairArrayList<String,QuestTemplate>();
		// plane/template choice
		for(final String planeID : planarA.getAllPlaneKeys())
		{
			final Map<String,String> planeVars = planarA.getPlanarVars(planeID);
			for(final QuestTemplate T : QuestTemplate.values())
			{
				int times=0;
				if(T.req.length==0)
				{
					finalChoices.add(planeID,T);
					continue;
				}
				for(final QuestReq req : T.req)
				{
					boolean pass=false;
					switch(req)
					{
					case DISSIMILAR:
						if(alignOpposedPlanes.contains(planeID)
						||alignDissimilarPlanes.contains(planeID))
						{
							pass=true;
							times+=3;
						}
						break;
					case INNER:
					{
						final String catStr=planeVars.get(PlanarVar.CATEGORY.toString());
						if((catStr != null)
						&&CMParms.parseCommas(catStr.toUpperCase(), true).contains("INNER"))
						{
							pass=true;
							times+=2;
						}
						break;
					}
					case OPPOSED:
						if(alignOpposedPlanes.contains(planeID))
						{
							pass=true;
							times+=4;
						}
						break;
					case OUTER:
					{
						final String catStr=planeVars.get(PlanarVar.CATEGORY.toString());
						if((catStr != null)
						&&CMParms.parseCommas(catStr.toUpperCase(), true).contains("OUTER"))
						{
							pass=true;
							times+=2;
						}
						break;
					}
					case SAME:
						if(alignSamePlanes.contains(planeID))
						{
							pass=true;
							times+=4;
						}
						break;
					case SIMILAR:
						if(alignSamePlanes.contains(planeID)
						||alignSimilarPlanes.contains(planeID))
						{
							pass=true;
							times+=3;
						}
						break;
					default:
						break;
					}
					if(!pass)
					{
						times=0;
						break;
					}
				}
				for(int i=0;i<times;i++)
					finalChoices.add(planeID,T);
			}
		}

		if(finalChoices.size()==0)
		{
			mob.tell(L("The planes of existence to not, actually, exist."));
			return false;
		}

		final Pair<String,QuestTemplate> winner=finalChoices.get(CMLib.dice().roll(1, finalChoices.size(), -1));

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,targetM,this,verbalCastCode(mob,targetM,auto),L("^S<S-NAME> @x1 to grant <T-NAMESELF> a planar pilgrimmage.^?",super.prayWord(mob),targetM.name()));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Ability effA=beneficialAffect(mob,targetM,asLevel,(int)(CMProps.getTicksPerHour()*2));
				if(effA!=null)
				{
					effA.setMiscText("PLANE=\""+winner.first+"\" DEITY=\""+deityName+"\" TEMPLATECODE=\""+winner.second.name()+"\"");
					setTimeOfNextCast(mob);
					final String sayStr=L("@x1 requires you to make a pilgrimage to the plane of @x2 and await further instructions.",deityName,CMStrings.capitalizeAllFirstLettersAndLower(winner.first));
					if(targetM==mob)
						mob.tell(sayStr);
					else
						CMLib.commands().postSay(mob, targetM, sayStr);
				}
			}
		}
		else
		{
			beneficialWordsFizzle(mob,targetM,L("<S-NAME> @x1 on behalf of <T-NAMESELF>, but <S-IS-AREA> not answered.",super.prayWord(mob),targetM.Name()));
		}
		// return whether it worked
		return success;
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if(code.equalsIgnoreCase("NEXTCAST"))
			lastUsed.clear();
		super.setStat(code, val);
	}
}
