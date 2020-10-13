package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.CompiledFormula;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.core.exceptions.CoffeeMudException;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.PlanarAbility.PlanarVar;
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

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/*
   Copyright 2020-2020 Bo Zimmerman

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
public class InstanceArea extends StdAbility
{
	@Override
	public String ID()
	{
		return "InstanceArea";
	}

	private final static String	localizedName	= CMLib.lang().L("Area Instancing Ability");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public long flags()
	{
		return 0;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL - 90;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	protected volatile long				lastCasting		= 0;
	protected WeakReference<Room>		oldRoom			= null;
	protected Area						targetArea		= null;
	protected Area						instArea		= null;
	protected Map<String, String>		instVars		= null;
	protected WeakArrayList<Room>		roomsDone		= new WeakArrayList<Room>();
	protected int						instanceLevel		= 1;
	protected String					instTypeID		= "";
	protected String					colorPrefix		= null;
	protected PairList<Integer,String>	promotions		= null;
	protected List<String>				categories		= null;
	protected PairList<String, String>	behavList		= null;
	protected PairList<String, String>	reffectList		= null;
	protected PairList<String, String>	factionList		= null;
	protected int						bonusDmgStat	= -1;
	protected Set<String>				reqWeapons		= null;
	protected int						recoverRate		= 0;
	protected int						fatigueRate		= 0;
	protected volatile int				recoverTick		= 0;
	protected Set<InstSpecFlag>			specFlags		= null;
	protected int						hardBumpLevel	= 0;
	protected CompiledFormula			levelFormula	= null;
	protected final Map<String,long[]>	recentVisits	= new TreeMap<String,long[]>();

	protected final static long			hardBumpTimeout	= (60L * 60L * 1000L);

	protected PairList<Pair<Integer,Integer>,PairList<String,String>> enableList=null;

	protected static final AtomicInteger instIDNum = new AtomicInteger(0);

	/**
	 * The definitions variables for the attributes of each instance
	 *
	 * @author Bo Zimmerman
	 *
	 */
	protected static enum InstVar
	{
		ID,
		ALIGNMENT,
		PREFIX,
		LEVELADJ,
		MOBRESIST,
		SETSTAT,
		BEHAVAFFID,
		ADJSTAT,
		ADJSIZE,
		ADJUST,
		MOBCOPY,
		BEHAVE,
		ENABLE,
		WEAPONMAXRANGE,
		BONUSDAMAGESTAT,
		REQWEAPONS,
		ATMOSPHERE,
		AREABLURBS,
		ABSORB,
		HOURS,
		RECOVERRATE,
		FATIGUERATE,
		REFFECT,
		AEFFECT,
		SPECFLAGS,
		MIXRACE,
		ELITE,
		ROOMCOLOR,
		ROOMADJS,
		FACTIONS,
		CATEGORY,
		PROMOTIONS,
		LIKE,
		DESCRIPTION
	}

	/**
	 * The special attribute flags for instances
	 *
	 * @author Bo Zimmerman
	 *
	 */
	protected static enum InstSpecFlag
	{
		NOINFRAVISION,
		BADMUNDANEARMOR,
		ALLBREATHE
	}

	protected final Room getOldRoom()
	{
		return (oldRoom != null) ? oldRoom.get() : null;
	}

	protected final void setOldRoom(final Room oldRoom)
	{
		if(oldRoom == null)
			this.oldRoom = null;
		else
			this.oldRoom = new WeakReference<Room>(oldRoom);
	}

	protected void setInstaceTypeID(final String instTypeID)
	{
		setMiscText(instTypeID);
	}

	protected void clearVars()
	{
		instArea = null;
		instanceLevel=1;
		roomsDone=new WeakArrayList<Room>();
		instVars=null;
		promotions=null;
		categories=null;
		colorPrefix=null;
		this.instTypeID="";
		this.behavList=null;
		this.enableList=null;
		this.reffectList=null;
		this.factionList=null;
		this.levelFormula=null;
		bonusDmgStat=-1;
		this.reqWeapons=null;
		this.recoverTick=-1;
		recoverRate		= 0;
		fatigueRate		= 0;
	}

	protected String addOrEditInstanceType(final String instTypeID, final String rule)
	{
		final Map<String,Map<String,String>> map = getAllInstanceTypesMap();
		final Map<String,String> instParms = CMParms.parseEQParms(rule);
		for(final String key : instParms.keySet())
		{
			if((CMath.s_valueOf(InstVar.class, key)==null)
			&&(CMLib.factions().getFaction(key)==null)
			&&(CMLib.factions().getFactionByName(key)==null))
				return "ERROR: Unknown instance var: "+key;
		}
		instParms.put(InstVar.ID.toString(), instTypeID);
		if(!map.containsKey(instTypeID.trim().toUpperCase()))
		{
			String previ="";
			for(String i="";!i.equals(".9");i=("."+(Math.round(CMath.s_double(i)*10)+1)))
			{
				final CMFile F=new CMFile(Resources.makeFileResourceName("skills/areainstancetypes.txt"+i), null);
				if(!F.exists())
					break;
				previ=i;
			}
			final CMFile F=new CMFile(Resources.makeFileResourceName("skills/areainstancetypes.txt"+previ), null);
			final StringBuffer old=F.text();
			if((!old.toString().endsWith("\n"))
			&&(!old.toString().endsWith("\r")))
				old.append("\r\n");
			old.append("\"").append(instTypeID).append("\" ").append(rule).append("\r\n");
			F.saveText(old.toString());
			map.put(instTypeID.toUpperCase().trim(), instParms);
			return null;
		}
		else
		{
			final Map<String,String> oldInstance = map.get(instTypeID.trim().toUpperCase());
			final StringBuilder changes = new StringBuilder("");
			for(final String oldKey : oldInstance.keySet())
			{
				if(!instParms.containsKey(oldKey))
					changes.append("REMOVED: ").append(oldKey).append("\n\r");
				else
				{
					final String oldVal = oldInstance.get(oldKey);
					final String newVal = instParms.get(oldKey);
					if(!oldVal.equals(newVal))
					{
						changes.append("CHANGED: ").append(oldKey).append(": '").append(oldVal)
								.append("' TO '").append(newVal).append("'").append("\n\r");
					}
				}
			}
			for(final String newKey : instParms.keySet())
			{
				if(!oldInstance.containsKey(newKey))
					changes.append("ADDED: ").append(newKey).append("\n");
			}
			if(changes.length()==0)
				return "";
			for(String i="";!i.equals(".9");i=("."+(Math.round(CMath.s_double(i)*10)+1)))
			{
				if(alterInstanceLine(instTypeID, Resources.makeFileResourceName("skills/areainstancetypes.txt"+i), rule))
				{
					map.put(instTypeID.toUpperCase().trim(), instParms);
					return changes.toString();
				}
			}
			return "ERROR: Not Found!";
		}
	}

	protected boolean alterInstanceLine(final String instTypeID, final String fileName, final String rule)
	{
		final CMFile F=new CMFile(fileName,null);
		if(!F.exists())
			return false;
		final List<String> lines = Resources.getFileLineVector(F.text());
		for(int i=0;i<lines.size();i++)
		{
			final String line=lines.get(i).trim();
			String instname=null;
			if(line.startsWith("\""))
			{
				final int x=line.indexOf("\"",1);
				if(x>1)
					instname=line.substring(1,x);
			}
			if((instname != null)
			&&(instname.equalsIgnoreCase(instTypeID)))
			{
				if(rule == null)
					lines.remove(i);
				else
					lines.set(i,"\""+instTypeID+"\" "+rule);
				final StringBuilder newFile = new StringBuilder("");
				for(final String fline : lines)
					newFile.append(fline).append("\r\n");
				Resources.removeResource("SKILL_AREA_INSTANCE_TYPES");
				Resources.removeResource(fileName);
				F.saveText(newFile.toString());
				return true;
			}
		}
		return false;
	}

	protected boolean deleteInstance(final String instTypeID)
	{
		final Map<String,Map<String,String>> map = getAllInstanceTypesMap();
		if(!map.containsKey(instTypeID.trim().toUpperCase()))
			return false;
		for(String i="";!i.equals(".9");i=("."+(Math.round(CMath.s_double(i)*10)+1)))
		{
			if(alterInstanceLine(instTypeID, Resources.makeFileResourceName("skills/areainstancetypes.txt"+i), null))
			{
				map.remove(instTypeID.trim().toUpperCase());
				return true;
			}
		}
		return false;
	}

	@Override
	public void setMiscText(final String newText)
	{
		super.setMiscText(newText);
		//TODO: is it an effect setting, is it an id, or is it variables inline?
		clearVars();
		if(newText.length()>0)
		{
			this.instTypeID=newText;
			this.instVars=getInstVars(newText);
			if(this.instVars==null)
			{
				if(newText.equalsIgnoreCase("DEFAULT_NEW"))
					this.instVars=new Hashtable<String,String>();
				else
					throw new IllegalArgumentException("Unknown: "+newText);
			}
			this.roomsDone=new WeakArrayList<Room>();
			this.colorPrefix=instVars.get(InstVar.PREFIX.toString());
			if(instVars.containsKey(InstVar.CATEGORY.toString()))
			{
				final String catStr=instVars.get(InstVar.CATEGORY.toString());
				if(catStr != null)
					this.categories=CMParms.parseCommas(catStr, true);
			}
			this.recoverRate = CMath.s_int(instVars.get(InstVar.RECOVERRATE.toString()));
			this.fatigueRate = CMath.s_int(instVars.get(InstVar.FATIGUERATE.toString()));
			this.recoverTick=1;
			if((colorPrefix!=null)&&(colorPrefix.indexOf(',')>0))
			{
				final List<String> choices=CMParms.parseCommas(colorPrefix, true);
				colorPrefix=choices.get(CMLib.dice().roll(1, choices.size(), -1));
			}
			if(affected instanceof Area)
			{
				instArea=(Area)affected;
				int medianLevel=instArea.getPlayerLevel();
				if(medianLevel <= 0)
					medianLevel=instArea.getAreaIStats()[Area.Stats.MED_LEVEL.ordinal()];
				instanceLevel=medianLevel;
			}
			this.specFlags = null;
			final String specflags = instVars.get(InstVar.SPECFLAGS.toString());
			if(specflags != null)
			{
				for(final String s : CMParms.parse(specflags))
				{
					final InstSpecFlag flag=(InstSpecFlag)CMath.s_valueOf(InstSpecFlag.class, s);
					if(flag == null)
						Log.errOut("InstanceArea","Unknown spec flag "+s);
					else
					{
						if(this.specFlags==null)
							this.specFlags=new HashSet<InstSpecFlag>();
						this.specFlags.add(flag);
					}
				}
			}
			this.behavList = null;
			final String behaves = instVars.get(InstVar.BEHAVE.toString());
			if(behaves!=null)
				this.behavList=new PairVector<String,String>(CMParms.parseSpaceParenList(behaves));
			this.reffectList = null;
			final String reffects = instVars.get(InstVar.REFFECT.toString());
			if(reffects!=null)
				this.reffectList=new PairVector<String,String>(CMParms.parseSpaceParenList(reffects));
			this.factionList = null;
			final String factions = instVars.get(InstVar.FACTIONS.toString());
			if(factions!=null)
				this.factionList=new PairVector<String,String>(CMParms.parseSpaceParenList(factions));
			String levelFormulaStr = instVars.get(InstVar.LEVELADJ.toString());
			if((levelFormulaStr == null)||(levelFormulaStr.trim().length()==0))
				levelFormulaStr = "(@x3 - (@x1 - @x2) + 0) > 1";
			else
			if(CMath.isInteger(levelFormulaStr.trim()))
				levelFormulaStr = "(@x3 - (@x1 - @x2) + "+levelFormulaStr+") > 1";
			this.levelFormula = CMath.compileMathExpression(levelFormulaStr);
			final String enables = instVars.get(InstVar.ENABLE.toString());
			this.enableList=null;
			if(enables!=null)
			{
				final List<Pair<String,String>> enableAs=CMParms.parseSpaceParenList(enables);
				final List<Triad<String,String,Pair<Integer,Integer>>> enableMidList = new Vector<Triad<String,String,Pair<Integer,Integer>>>();
				final Integer defaultPerLevel=Integer.valueOf(CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL));
				final Integer defaultNumSkills=Integer.valueOf(Integer.MAX_VALUE);
				final Pair<Integer,Integer> defaultLimit = new Pair<Integer,Integer>(defaultPerLevel,defaultNumSkills);
				for(final Iterator<Pair<String,String>> p=enableAs.iterator();p.hasNext();)
				{
					final Pair<String,String> P=p.next();
					if(P.first.toLowerCase().equals("number"))
					{
						p.remove();
						final String parms=P.second;
						final int x=parms.indexOf('/');
						final Pair<Integer,Integer> newLimit = new Pair<Integer,Integer>(defaultPerLevel,defaultNumSkills);
						if(x<0)
							newLimit.first=Integer.valueOf(CMath.s_int(parms.trim()));
						else
						{
							newLimit.first=Integer.valueOf(CMath.s_int(parms.substring(0,x).trim()));
							newLimit.second=Integer.valueOf(CMath.s_int(parms.substring(x+1).trim()));
						}
						for(final Iterator<Triad<String,String,Pair<Integer,Integer>>> k = enableMidList.iterator();k.hasNext();)
						{
							final Triad<String,String,Pair<Integer,Integer>> K=k.next();
							if(K.third == defaultLimit)
								K.third=newLimit;
						}
					}
					else
						enableMidList.add(new Triad<String,String,Pair<Integer,Integer>>(P.first,P.second,defaultLimit));
				}
				if(enableMidList.size()>0)
				{
					this.enableList=new PairVector<Pair<Integer,Integer>,PairList<String,String>>();
					for(final Iterator<Triad<String,String,Pair<Integer,Integer>>> p = enableMidList.iterator();p.hasNext();)
					{
						final Triad<String,String,Pair<Integer,Integer>> P=p.next();
						final PairList<String,String> addThese = new PairVector<String,String>();
						Ability A=CMClass.getAbility(P.first);
						if(A==null)
						{
							p.remove();
							boolean foundSomething=false;
							long flag=CMParms.indexOf(Ability.FLAG_DESCS, P.first);
							if(flag >=0 )
								flag=CMath.pow(2, flag);
							int domain=CMParms.indexOf(Ability.DOMAIN_DESCS, P.first);
							if(domain > 0)
								domain = domain << 5;
							final int acode=CMParms.indexOf(Ability.ACODE_DESCS, P.first);
							for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
							{
								A=a.nextElement();
								if((A.Name().toUpperCase().equals(P.first))
								||((flag>0)&&(CMath.bset(A.flags(),flag)))
								||((domain>0)&&(A.classificationCode()&Ability.ALL_DOMAINS)==domain)
								||((acode>=0)&&(A.classificationCode()&Ability.ALL_ACODES)==acode)
								)
								{
									if(!addThese.containsFirst(A.ID().toUpperCase()))
									{
										addThese.add(A.ID().toUpperCase(), P.second);
										foundSomething=true;
									}
								}
							}
							if(!foundSomething)
								Log.errOut("InstanceArea","Unknown skill type/domain/flag: "+P.first);
						}
						else
							addThese.add(new Pair<String,String>(P.first,P.second));
						this.enableList.add(P.third,addThese);
					}
				}
			}
			final String bonusDamageStat = instVars.get(InstVar.BONUSDAMAGESTAT.toString());
			this.bonusDmgStat = -1;
			if(bonusDamageStat!=null)
				this.bonusDmgStat=CMParms.indexOf(CharStats.CODES.BASENAMES(), bonusDamageStat.toUpperCase().trim());
			final String reqWeapons = instVars.get(InstVar.REQWEAPONS.toString());
			this.reqWeapons = null;
			if(reqWeapons != null)
				this.reqWeapons = new HashSet<String>(CMParms.parse(reqWeapons.toUpperCase().trim()));
			this.promotions=null;
			if(instVars.containsKey(InstVar.PROMOTIONS.toString()))
			{
				final List<String> bits=CMParms.parseCommas(instVars.get(InstVar.PROMOTIONS.toString()), true);
				this.promotions=new PairVector<Integer,String>();
				for(final String bit : bits)
				{
					Integer pctChance = Integer.valueOf(10);
					final int x=bit.indexOf('(');
					String rank=bit;
					if((x>0)&&(bit.endsWith(")")))
					{
						rank=bit.substring(0,x).trim();
						pctChance=Integer.valueOf(CMath.s_int(bit.substring(x+1,bit.length()-1)));
					}
					if(rank.trim().length()>0)
						this.promotions.add(pctChance, rank.trim());
				}
			}

			final Area instArea=this.instArea;
			if(instArea == null)
				return;
			final String areablurbs = instVars.get(InstVar.AREABLURBS.toString());
			if((areablurbs!=null)&&(areablurbs.length()>0))
			{
				final Map<String,String> blurbSets=CMParms.parseEQParms(areablurbs);
				for(final String key : blurbSets.keySet())
					instArea.addBlurbFlag(key.toUpperCase().trim().replace(' ', '_')+" "+blurbSets.get(key));
			}
			final String atmosphere = instVars.get(InstVar.ATMOSPHERE.toString());
			if(atmosphere!=null)
			{
				if(atmosphere.length()==0)
					this.instArea.setAtmosphere(Integer.MIN_VALUE);
				else
				{
					final int atmo=RawMaterial.CODES.FIND_IgnoreCase(atmosphere);
					this.instArea.setAtmosphere(atmo);
				}
			}
			final String absorb = instVars.get(InstVar.ABSORB.toString());
			if(absorb != null)
				reEffect(instArea,"Prop_AbsorbDamage",absorb);
			final TimeClock C=(TimeClock)CMLib.time().globalClock().copyOf();
			C.setDayOfMonth(1);
			C.setYear(1);
			C.setMonth(1);
			C.setHourOfDay(0);
			final String hours = instVars.get(InstVar.HOURS.toString());
			if((hours != null)&&(CMath.isInteger(hours)))
			{
				final double mul=CMath.div(CMath.s_int(hours),CMLib.time().globalClock().getHoursInDay());
				if(mul != 1.0)
				{
					final int newHours = (int)Math.round(CMath.mul(C.getHoursInDay(),mul));
					C.setHoursInDay(newHours);
					C.setDawnToDusk((int)Math.round(CMath.mul(C.getDawnToDusk()[0],mul))
									, (int)Math.round(CMath.mul(C.getDawnToDusk()[1],mul))
									, (int)Math.round(CMath.mul(C.getDawnToDusk()[2],mul))
									, (int)Math.round(CMath.mul(C.getDawnToDusk()[3],mul)));
				}
			}
			instArea.setTimeObj(C);
			for(final CMObject O : getAreaEffectsBehavs())
			{
				if(O instanceof Ability)
					instArea.addNonUninvokableEffect((Ability)O);
				else
				if(O instanceof Behavior)
				{
					if(instArea.fetchBehavior(O.ID())==null)
						instArea.addBehavior((Behavior)O);
				}
			}
		}
	}

	protected List<CMObject> getAreaEffectsBehavs()
	{
		final List<CMObject> aeffectbehavs = new Vector<CMObject>();
		{
			final Ability A=CMClass.getAbility("Prop_NoTeleportOut");
			A.setMiscText("exceptions=instancearea");
			aeffectbehavs.add(A);
		}
		{
			final Ability A=CMClass.getAbility("Prop_NoTeleport");
			A.setMiscText("exceptions=instancearea");
			aeffectbehavs.add(A);
		}
		aeffectbehavs.add(CMClass.getAbility("Prop_NoRecall"));
		final String aeffects = instVars.get(InstVar.AEFFECT.toString());
		if(aeffects!=null)
		{
			final List<Pair<String,String>> affectList=CMParms.parseSpaceParenList(aeffects);
			if(affectList!=null)
			{
				for(final Pair<String,String> p : affectList)
				{
					final Behavior B=CMClass.getBehavior(p.first);
					if(B==null)
					{
						final Ability A=CMClass.getAbility(p.first);
						if(A==null)
							Log.errOut("InstanceArea","Unknown behavior : "+p.first);
						else
						{
							A.setMiscText(p.second);
							aeffectbehavs.add(A);
						}
					}
					else
					{
						B.setParms(p.second);
						aeffectbehavs.add(B);
					}
				}
			}
		}
		return aeffectbehavs;
	}

	protected void reEffect(final Physical M, final String ID, final String parms)
	{
		if(M!=null)
		{
			Ability A=M.fetchEffect(ID);
			if(A!=null)
				M.delEffect(A);
			else
				A=CMClass.getAbility(ID);
			if(A!=null)
			{
				M.addNonUninvokableEffect(A);
				A.setMiscText((parms+" "+A.text()).trim());
			}
		}
	}

	protected boolean isInstanceMob(final MOB M)
	{
		final String badTattooName = "NOAINST "+this.instTypeID.toUpperCase().trim();
		return (M!=null)
			 &&(M.isMonster())
			 &&(M.getStartRoom()!=null)
			 &&(M.findTattoo(badTattooName)==null)
			 &&(M.findTattoo("NOAINST")==null);
	}

	protected void doInstanceRoomColoring(final Room room)
	{
		if(instVars.containsKey(InstVar.ROOMCOLOR.toString()))
		{
			String prefix="";
			String displayText = room.displayText();
			if(displayText.toUpperCase().startsWith("<VARIES>"))
			{
				prefix="<VARIES>";
				displayText=displayText.substring(prefix.length());
			}
			String color=instVars.get(InstVar.ROOMCOLOR.toString());
			if(color.startsWith("UP "))
			{
				color=color.substring(3).trim();
				displayText=displayText.toUpperCase();
			}
			room.setDisplayText(prefix+color+displayText+"^N");
		}
		if(instVars.containsKey(InstVar.ROOMADJS.toString()))
		{
			String wordStr=instVars.get(InstVar.ROOMADJS.toString());
			String prefix="";
			String desc = room.description();
			if(wordStr.startsWith("UP "))
			{
				wordStr=wordStr.substring(3).trim();
				desc=desc.toUpperCase();
			}
			int chance=30;
			final int x=wordStr.indexOf(' ');
			if((x>0)&&(CMath.isInteger(wordStr.substring(0, x))))
			{
				chance=CMath.s_int(wordStr.substring(0, x));
				wordStr=wordStr.substring(x+1).trim();
			}
			final String[] words= wordStr.split(",");
			if(desc.toUpperCase().startsWith("<VARIES>"))
			{
				prefix="<VARIES>";
				desc=desc.substring(prefix.length());
			}
			room.setDescription(prefix+CMLib.english().insertAdjectives(desc, words, chance));
		}
	}

	protected void applyMobPrefix(final MOB M, final int[] eliteBump)
	{
		String colorPrefix = this.colorPrefix;
		if((this.promotions!=null)&&(this.promotions.size()>0))
		{
			final int randomRoll = CMLib.dice().rollPercentage();
			Pair<Integer,String> bestAvail = null;
			for(int index=0;index<this.promotions.size();index++)
			{
				if(randomRoll <= this.promotions.getFirst(index).intValue())
					bestAvail = new Pair<Integer,String>(Integer.valueOf(index),this.promotions.getSecond(index));
			}
			if(bestAvail != null)
			{
				if(colorPrefix == null)
					colorPrefix = "";
				colorPrefix = (bestAvail.second+" "+colorPrefix).trim();
				if(eliteBump != null)
					eliteBump[0] += 1+(bestAvail.first.intValue()*2);
			}
		}
		if((colorPrefix!=null)&&(colorPrefix.length()>0))
		{
			final String oldName=M.Name();
			int x;
			if(oldName.toLowerCase().indexOf(colorPrefix.toLowerCase())<0)
			{
				if(CMLib.english().startsWithAnArticle(M.Name()))
				{
					final String Name = M.Name().substring(M.Name().indexOf(' ')).trim();
					M.setName(CMLib.english().startWithAorAn(colorPrefix+" "+Name));
				}
				else
				{
					M.setName(CMStrings.capitalizeFirstLetter(colorPrefix)+" "+M.Name());
				}
				if((x=M.displayText().toLowerCase().indexOf(oldName.toLowerCase()))>=0)
				{
					M.setDisplayText(M.displayText().substring(0,x)+M.Name()+M.displayText().substring(x+oldName.length()));
				}
				else
				if(CMLib.english().startsWithAnArticle(M.displayText()))
				{
					final String Name = M.displayText().substring(M.displayText().indexOf(' ')).trim();
					M.setDisplayText(CMLib.english().startWithAorAn(colorPrefix+" "+Name));
				}
				else
				if((x=M.displayText().toLowerCase().indexOf(M.charStats().getMyRace().name().toLowerCase()))>=0)
				{
					final int len=M.charStats().getMyRace().name().toLowerCase().length();
					M.setDisplayText(M.displayText().substring(0,x)+colorPrefix+M.Name()+M.displayText().substring(x+len));
				}
			}
		}
	}

	protected synchronized void fixRoom(final Room room)
	{
		try
		{
			room.toggleMobility(false);
			CMLib.threads().suspendResumeRecurse(room, false, true);
			for(int i=0;i<Directions.NUM_DIRECTIONS();i++)
			{
				final Room R=room.rawDoors()[i];
				if((R!=null)&&(R.getArea()!=instArea))
					room.rawDoors()[i]=null;
			}
			int eliteLevel=0;
			if(instVars.containsKey(InstVar.ELITE.toString()))
				eliteLevel=CMath.s_int(instVars.get(InstVar.ELITE.toString()));
			if(instVars.containsKey(InstVar.ATMOSPHERE.toString()))
				room.setAtmosphere(instArea.getAtmosphere());
			doInstanceRoomColoring(room);
			if(this.reffectList!=null)
			{
				for(final Pair<String,String> p : this.reffectList)
				{
					if(room.fetchBehavior(p.first)==null)
					{
						final Behavior B=CMClass.getBehavior(p.first);
						if(B==null)
						{
							final Ability A=CMClass.getAbility(p.first);
							if(A==null)
								Log.errOut("InstanceArea","Unknown behavior : "+p.first);
							else
							{
								A.setMiscText(p.second);
								room.addNonUninvokableEffect(A);
							}
						}
						else
						{
							B.setParms(p.second);
							room.addBehavior(B);
						}
					}
				}
			}

			if(CMLib.law().getLandTitle(room)!=null)
			{
				final List<Physical> destroyMe=new ArrayList<Physical>();
				final Set<Rider> protSet = new HashSet<Rider>();
				for(final Enumeration<MOB> m=room.inhabitants();m.hasMoreElements();)
				{
					final MOB M=m.nextElement();
					if((M!=null)&&(M.isPlayer()))
					{
						protSet.add(M);
						M.getGroupMembersAndRideables(protSet);
					}
				}
				for(final Enumeration<MOB> m=room.inhabitants();m.hasMoreElements();)
				{
					final MOB M=m.nextElement();
					if((M!=null) && (!M.isPlayer()) && (!protSet.contains(M)))
						destroyMe.add(M);
				}
				for(final Enumeration<Item> i=room.items();i.hasMoreElements();)
				{
					final Item I=i.nextElement();
					if((I!=null)&&(!protSet.contains(I)))
						destroyMe.add(I);
				}
				for(final Physical P : destroyMe)
					P.destroy();
			}
			final List<Item> delItems=new ArrayList<Item>(0);
			for(final Enumeration<Item> i=room.items();i.hasMoreElements();)
			{
				final Item I=i.nextElement();
				if(I==null)
					continue;
				if((I instanceof Exit)&&((I instanceof BoardableShip)))
				{
					for(int x=0;x<100;x++)
					{
						final Room R2=((Exit)I).lastRoomUsedFrom(room);
						if((R2!=null)&&(R2.getArea()!=instArea))
						{
							delItems.add(I);
							break;
						}
					}
				}
				else
				if(I instanceof Exit)
					I.setReadableText("");
				else
				if((invoker!=null)&&((I instanceof Weapon)||(I instanceof Armor)))
				{
					final double[] vars=new double[] {instanceLevel, I.phyStats().level(), invoker.phyStats().level() } ;
					final int newILevel = (int)CMath.round(CMath.parseMathExpression(this.levelFormula, vars, 0.0));
					CMLib.itemBuilder().itemFix(I, newILevel, null);
					I.basePhyStats().setLevel(newILevel);
					I.phyStats().setLevel(newILevel);
					CMLib.itemBuilder().balanceItemByLevel(I);
					if((I instanceof Weapon)
					&&(this.reqWeapons!=null)
					&&(this.reqWeapons.contains("MAGICAL")))
					{
						I.basePhyStats().setDisposition(I.basePhyStats().disposition()|PhyStats.IS_BONUS);
						I.phyStats().setDisposition(I.phyStats().disposition()|PhyStats.IS_BONUS);
					}
					I.text();
				}
			}
			for(final Item I : delItems)
				I.destroy();
			for(final Enumeration<MOB> m=room.inhabitants();m.hasMoreElements();)
			{
				final MOB M=m.nextElement();
				if((M!=null)
				&&(invoker!=null)
				&&(isInstanceMob(M))
				&&(M.getStartRoom().getArea()==instArea))
				{
					if(instVars.containsKey(InstVar.MIXRACE.toString()))
					{
						final String mixRace = instVars.get(InstVar.MIXRACE.toString());
						final Race firstR=CMClass.getRace(mixRace);
						if(firstR==null)
							Log.errOut("InstanceArea","Unknown mixrace: "+mixRace);
						else
						{
							final Race secondR=M.charStats().getMyRace();
							final Race R=CMLib.utensils().getMixedRace(firstR.ID(),secondR.ID(), false);
							if(R!=null)
							{
								M.baseCharStats().setMyRace(R);
								M.charStats().setMyRace(R);
								M.charStats().setWearableRestrictionsBitmap(M.charStats().getWearableRestrictionsBitmap()|M.charStats().getMyRace().forbiddenWornBits());
							}
						}
					}

					if(instVars.containsKey(InstVar.ATMOSPHERE.toString()))
						M.baseCharStats().setBreathables(new int[]{room.getAtmosphere()});
					final double[] vars=new double[] {instanceLevel, M.phyStats().level(), invoker.phyStats().level() } ;
					final int newLevel = (int)CMath.round(CMath.parseMathExpression(this.levelFormula, vars, 0.0));
					final int[] eliteBump = new int[1];
					this.applyMobPrefix(M, eliteBump);
					eliteLevel += eliteBump[0];
					M.basePhyStats().setLevel(newLevel);
					M.phyStats().setLevel(newLevel);
					CMLib.leveler().fillOutMOB(M,M.basePhyStats().level()+hardBumpLevel);
					M.basePhyStats().setLevel(newLevel);
					M.phyStats().setLevel(newLevel);
					final String align=instVars.get(InstVar.ALIGNMENT.toString());
					if(align!=null)
					{
						M.removeFaction(CMLib.factions().getAlignmentID());
						M.addFaction(CMLib.factions().getAlignmentID(), CMath.s_int(align));
					}
					if(this.factionList!=null)
					{
						for(final Pair<String,String> p : this.factionList)
						{
							final String factionName = (p.first.equals("*")
									?("AINST_"+this.instTypeID.toUpperCase().trim())
									:p.first);
							Faction F=null;
							if(CMLib.factions().isFactionID(factionName))
								F=CMLib.factions().getFaction(factionName);
							if(F==null)
								F=CMLib.factions().getFactionByName(factionName);
							if(F!=null)
							{
								if(p.second.length()==0)
								{
									M.removeFaction(F.factionID());
									M.addFaction(F.factionID(), F.findAutoDefault(M));
								}
								else
								if(CMath.isInteger(p.second))
								{
									M.removeFaction(F.factionID());
									M.addFaction(F.factionID(), CMath.s_int(p.second));
								}
								else
								{
									final Faction.FRange FR = F.fetchRange(p.second);
									if(FR != null)
									{
										M.removeFaction(F.factionID());
										M.addFaction(F.factionID(), FR.random());
									}
								}
							}
						}
					}
					for(final Enumeration<Item> mi=M.items();mi.hasMoreElements();)
					{
						final Item mI=mi.nextElement();
						if((mI!=null)&&(invoker!=null))
						{
							final double[] ivars=new double[] {instanceLevel, mI.phyStats().level(), invoker.phyStats().level() } ;
							final int newILevel = (int)CMath.round(CMath.parseMathExpression(this.levelFormula, ivars, 0.0));
							mI.basePhyStats().setLevel(newILevel);
							mI.phyStats().setLevel(newILevel);
							CMLib.itemBuilder().balanceItemByLevel(mI);
							if((mI instanceof Weapon)
							&&(this.reqWeapons!=null)
							&&(this.reqWeapons.contains("MAGICAL")))
							{
								mI.basePhyStats().setDisposition(mI.basePhyStats().disposition()|PhyStats.IS_BONUS);
								mI.phyStats().setDisposition(mI.phyStats().disposition()|PhyStats.IS_BONUS);
							}
							mI.text();
						}
					}
					final String resistWeak = instVars.get(InstVar.MOBRESIST.toString());
					if(resistWeak != null)
						reEffect(M,"Prop_Resistance",resistWeak);
					else
					if(this.hardBumpLevel>0)
						reEffect(M,"Prop_Resistance","magic holy disease poison evil weapons "+(5*hardBumpLevel)+"% ");
					final String setStat = instVars.get(InstVar.SETSTAT.toString());
					if(setStat != null)
						reEffect(M,"Prop_StatTrainer",setStat);
					final String behavaffid=instVars.get(InstVar.BEHAVAFFID.toString());
					if(behavaffid!=null)
					{
						String changeToID;
						for(final Enumeration<Behavior> b=M.behaviors();b.hasMoreElements();)
						{
							final Behavior B=b.nextElement();
							if((B!=null)&&((changeToID=CMParms.getParmStr(behavaffid, B.ID(), "")).length()>0))
							{
								boolean copyParms=false;
								if(changeToID.startsWith("*"))
								{
									copyParms=true;
									changeToID=changeToID.substring(1);
								}
								M.delBehavior(B);
								final Behavior B2=CMClass.getBehavior(changeToID);
								if(B2 != null)
								{
									if(copyParms)
										B2.setParms(B.getParms());
									M.addBehavior(B2);
								}
							}
						}
					}
					final String adjStat = instVars.get(InstVar.ADJSTAT.toString());
					if(adjStat != null)
						reEffect(M,"Prop_StatAdjuster",adjStat);
					if(eliteLevel > 0)
					{
						reEffect(M,"Prop_Adjuster", "multiplych=true "
								+ "hitpoints+"+(200+((eliteLevel-1)*50))+" "
								+ "multiplyph=true "
								+ "attack+"+(125+((eliteLevel-1)*12))+" "
								+ "damage+"+(110+((eliteLevel-1)*5))+" "
								+ "armor+"+(120+((eliteLevel-1)*20))+" "
								+ "ALLSAVES+"+(20+((eliteLevel-1)*5)));
						reEffect(M,"Prop_ShortEffects", "");
						reEffect(M,"Prop_ModExperience","*"+Math.round((eliteLevel+3)/2));
						final String adjSize = instVars.get(InstVar.ADJSIZE.toString());
						if(adjSize != null)
						{
							final double heightAdj = CMParms.getParmDouble(adjSize, "HEIGHT", Double.MIN_VALUE);
							if(heightAdj > Double.MIN_VALUE)
								reEffect(M,"Prop_Adjuster","height+"+(100+(heightAdj*100)));
						}
					}
					else
					{
						final String adjust = instVars.get(InstVar.ADJUST.toString());
						if(adjust != null)
							reEffect(M,"Prop_Adjuster",adjust);
						final String adjSize = instVars.get(InstVar.ADJSIZE.toString());
						if(adjSize != null)
						{
							final double heightAdj = CMParms.getParmDouble(adjSize, "HEIGHT", Double.MIN_VALUE);
							if(heightAdj > Double.MIN_VALUE)
								reEffect(M,"Prop_Adjuster","height+"+(int)Math.round(CMath.mul(M.basePhyStats().height(),heightAdj)));
						}
					}
					final String adjSize = instVars.get(InstVar.ADJSIZE.toString());
					if(adjSize != null)
					{
						final double weightAdj = CMParms.getParmDouble(adjSize, "WEIGHT", Double.MIN_VALUE);
						if(weightAdj > Double.MIN_VALUE)
							reEffect(M,"Prop_StatAdjuster","weightadj="+(int)Math.round(CMath.mul(M.baseWeight(),weightAdj)));
					}
					if(this.behavList!=null)
					{
						for(final Pair<String,String> p : this.behavList)
						{
							if(M.fetchBehavior(p.first)==null)
							{
								final Behavior B=CMClass.getBehavior(p.first);
								if(B==null)
									Log.errOut("InstanceArea","Unknown behavior : "+p.first);
								else
								{
									B.setParms(p.second);
									M.addBehavior(B);
								}
							}
						}
					}
					if(this.enableList != null)
					{
						for(final Pair<Pair<Integer,Integer>,PairList<String,String>> P : enableList)
						{
							final Pair<Integer,Integer> lv=P.first;
							final PairList<String,String> unused = P.second;
							for(int l=0;l<M.phyStats().level() && (unused.size()>0);l+=lv.second.intValue())
							{
								for(int a=0;a<lv.first.intValue()  && (unused.size()>0);a++)
								{
									final int aindex=CMLib.dice().roll(1, unused.size(), -1);
									final Pair<String,String> U=unused.remove(aindex);
									final Ability A=CMClass.getAbility(U.first);
									if(M.fetchAbility(A.ID())==null)
									{
										A.setMiscText(U.second);
										M.addAbility(A);
									}
								}
							}
						}
					}
					M.text();
					M.recoverCharStats();
					M.recoverMaxState();
					M.recoverPhyStats();
					M.recoverCharStats();
					M.recoverMaxState();
					M.recoverPhyStats();
					M.resetToMaxState();
				}
			}
			final int mobCopy=CMath.s_int(instVars.get(InstVar.MOBCOPY.toString()));
			if(mobCopy>0)
			{
				final List<MOB> list=new ArrayList<MOB>(room.numInhabitants());
				for(final Enumeration<MOB> m=room.inhabitants();m.hasMoreElements();)
				{
					final MOB M=m.nextElement();
					if((M!=null)
					&&(M.isMonster())
					&&(M.getStartRoom()!=null)
					&&(M.getStartRoom().getArea()==instArea))
					{
						list.add(M);
					}
				}
				for(final MOB M : list)
				{
					for(int i=0;i<mobCopy;i++)
					{
						final MOB M2=(MOB)M.copyOf();
						M2.text();
						M2.setSavable(M.isSavable());
						M2.bringToLife(room, true);
						M2.recoverCharStats();
						M2.recoverMaxState();
						M2.recoverPhyStats();
					}
				}
			}
		}
		catch(final Exception e)
		{
			Log.errOut(e);
		}
		finally
		{
			room.recoverRoomStats();
			CMLib.threads().suspendResumeRecurse(room, false, false);
			room.toggleMobility(true);
			room.recoverRoomStats();
		}
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected, affectableStats);
		if((this.affected instanceof Area)
		&&(this.specFlags!=null)
		&&(CMath.bset(((Area)this.affected).flags(), Area.FLAG_INSTANCE_CHILD)))
		{
			if(this.specFlags.contains(InstSpecFlag.ALLBREATHE))
				affectableStats.setBreathables(new int[]{});
		}
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		if((this.affected instanceof Area)
		&&(CMath.bset(((Area)this.affected).flags(), Area.FLAG_INSTANCE_CHILD)))
		{
			if(affected instanceof MOB)
			{
				if(this.bonusDmgStat>=0)
					affectableStats.setDamage(affectableStats.damage() + (((((MOB)affected).charStats().getStat(this.bonusDmgStat))-10)/2));
				if(this.specFlags!=null)
				{
					if(this.specFlags.contains(InstSpecFlag.NOINFRAVISION))
						affectableStats.setSensesMask(CMath.unsetb(affectableStats.sensesMask(), PhyStats.CAN_SEE_INFRARED));
					if(this.specFlags.contains(InstSpecFlag.BADMUNDANEARMOR))
					{
						final MOB M=(MOB)affected;
						int neg=0;
						for(final Enumeration<Item> i=M.items();i.hasMoreElements();)
						{
							final Item I=i.nextElement();
							if((I instanceof Armor)
							&&(!I.amWearingAt(Wearable.IN_INVENTORY))
							&&((!I.amWearingAt(Wearable.WORN_FLOATING_NEARBY))||(I.fitsOn(Wearable.WORN_FLOATING_NEARBY)))
							&&((!I.amWearingAt(Wearable.WORN_HELD))||(this instanceof Shield))
							&&(!CMLib.flags().isABonusItems(I))
							&&(I.phyStats().ability()<=0))
							{
								neg += I.phyStats().armor();
							}
						}
						affectableStats.setArmor(affectableStats.armor()+neg);
					}
				}
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if((ticking instanceof Area)
		&&(tickID == Tickable.TICKID_AREA)
		&&(this.affected instanceof Area)
		&&(CMath.bset(((Area)this.affected).flags(), Area.FLAG_INSTANCE_CHILD)))
		{
			if(((this.recoverRate>0)||(this.fatigueRate>0))
			&&(--this.recoverTick <= 0)
			&& (this.instArea!=null))
			{
				this.recoverTick = CMProps.getIntVar(CMProps.Int.RECOVERRATE) * CharState.REAL_TICK_ADJUST_FACTOR;
				for(final Enumeration<Room> r=instArea.getFilledProperMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if((R!=null)&&(R.numPCInhabitants()>0))
					{
						for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
						{
							final MOB M=m.nextElement();
							for(int i=0;i<this.recoverRate;i++)
								CMLib.combat().recoverTick(M);
							if(this.fatigueRate>100)
							{
								M.curState().setHunger(M.maxState().maxHunger(M.baseWeight()));
								M.curState().setThirst(M.maxState().maxThirst(M.baseWeight()));
								M.curState().setFatigue(0);
							}
							else
							for(int i=0;i<(this.recoverTick * this.fatigueRate);i++)
								CMLib.combat().expendEnergy(M, false);
						}
					}
				}
			}
		}
		return true;
	}

	protected boolean roomDone(final Room R)
	{
		synchronized(roomsDone)
		{
			return (this.roomsDone.contains(R));
		}
	}

	protected synchronized void doneRoom(final Room R)
	{
		synchronized(roomsDone)
		{
			this.roomsDone.add(R);
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.targetMinor()==CMMsg.TYP_NEWROOM)
		{
			if((msg.target() instanceof Room)
			&&(!roomDone((Room)msg.target()))
			&&(((Room)msg.target()).getArea()==instArea))
			{
				doneRoom((Room)msg.target());
				fixRoom((Room)msg.target());
			}
		}
		super.executeMsg(myHost, msg);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((affected instanceof Area)
		&&(CMath.bset(((Area)affected).flags(), Area.FLAG_INSTANCE_CHILD)))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_WEAPONATTACK:
				if((msg.tool() instanceof AmmunitionWeapon)
				&&(((AmmunitionWeapon)msg.tool()).requiresAmmunition())
				&&(msg.target() instanceof MOB)
				&&(instVars!=null)
				&&(instVars.containsKey(InstVar.WEAPONMAXRANGE.toString()))
				&&((msg.source().rangeToTarget()>0)||(((MOB)msg.target()).rangeToTarget()>0)))
				{
					final int maxRange=CMath.s_int(instVars.get(InstVar.WEAPONMAXRANGE.toString()));
					if(((msg.source().rangeToTarget()>maxRange)||(((MOB)msg.target()).rangeToTarget()>maxRange)))
					{
						final String ammo=((AmmunitionWeapon)msg.tool()).ammunitionType();
						final String msgOut=L("The @x1 fired by <S-NAME> from <O-NAME> at <T-NAME> stops moving!",ammo);
						final Room R=msg.source().location();
						if(R!=null)
							R.show(msg.source(), msg.target(), msg.tool(), CMMsg.MSG_OK_VISUAL, msgOut);
						return false;
					}
				}
				break;
			case CMMsg.TYP_DAMAGE:
				if((msg.tool() instanceof Weapon)
				&&(this.reqWeapons!=null)
				&&(msg.value()>0))
				{
					if((CMLib.flags().isABonusItems((Weapon)msg.tool()) && (this.reqWeapons.contains("MAGICAL")))
					||(this.reqWeapons.contains(Weapon.CLASS_DESCS[((Weapon)msg.tool()).weaponClassification()]))
					||(this.reqWeapons.contains(Weapon.TYPE_DESCS[((Weapon)msg.tool()).weaponDamageType()])))
					{ // pass
					}
					else
						msg.setValue(0);
				}
				break;
			}
		}
		else
		{
			if((msg.sourceMinor()==CMMsg.TYP_ENTER)
			&&(msg.target() instanceof Room)
			&&((((Room)msg.target()).getArea()==affected)
				||((((Room)msg.target()).getArea()==targetArea)&&(targetArea!=null))))
			{
				//TODO: finish this too
				if(instArea == null)
				{

				}
				/*
				 * TODO: alter the existing stats to what they would be if this were a real area:
				int[] statData=(int[])Resources.getResource("STATS_"+Name().toUpperCase());
				if(statData!=null)
					return statData;
				 */
			}
		}
		return true;
	}

	protected List<String> getAllInstanceKeys()
	{
		final Map<String,Map<String,String>> map = getAllInstanceTypesMap();
		final List<String> ids=new ArrayList<String>(map.size());
		for(final String key : map.keySet())
			ids.add(key);
		return ids;
	}

	protected String listOfInstanceIDs()
	{
		final Map<String,Map<String,String>> map = getAllInstanceTypesMap();
		final StringBuilder str=new StringBuilder();
		for(final String key : map.keySet())
		{
			final Map<String,String> entry=map.get(key);
			str.append(entry.get(InstVar.ID.toString())).append(", ");
		}
		if(str.length()<2)
			return "";
		return str.toString();
	}

	protected static Map<String,Map<String,String>> getAllInstanceTypesMap()
	{
		@SuppressWarnings("unchecked")
		Map<String,Map<String,String>> map = (Map<String,Map<String,String>>)Resources.getResource("SKILL_AREA_INSTANCE_TYPES");
		if(map == null)
		{
			map = new TreeMap<String,Map<String,String>>();
			final List<String> lines = new ArrayList<String>();
			for(String i="";!i.equals(".9");i=("."+(Math.round(CMath.s_double(i)*10)+1)))
			{
				final CMFile F=new CMFile(Resources.makeFileResourceName("skills/areainstancetypes.txt"+i), null);
				if(!F.exists())
					break;
				lines.addAll(Resources.getFileLineVector(F.text()));
			}
			for(String line : lines)
			{
				line=line.trim();
				String instname=null;
				if(line.startsWith("\""))
				{
					final int x=line.indexOf("\"",1);
					if(x>1)
					{
						instname=line.substring(1,x);
						line=line.substring(x+1).trim();
					}
				}
				if(instname != null)
				{
					final Map<String,String> instParms = CMParms.parseEQParms(line);
					for(final String key : instParms.keySet())
					{
						if((CMath.s_valueOf(InstVar.class, key)==null)
						&&(CMLib.factions().getFaction(key)==null)
						&&(CMLib.factions().getFactionByName(key)==null))
							Log.errOut("InstanceArea","Unknown instance var: "+key);
					}
					instParms.put(InstVar.ID.toString(), instname);
					map.put(instname.toUpperCase(), instParms);
				}
			}
			// do the "LIKE" matching, and build factions
			for(final String key : map.keySet())
			{
				final Map<String,String> parms=map.get(key);
				if(parms.containsKey(InstVar.LIKE.toString()))
				{
					final Map<String,String> otherMap = map.get(parms.get(InstVar.LIKE.toString()).trim().toUpperCase());
					if(otherMap != null)
					{
						for(final String var : otherMap.keySet())
						{
							if(!parms.containsKey(var))
								parms.put(var, otherMap.get(var));
						}
					}
				}
			}
			Resources.submitResource("SKILL_AREA_INSTANCE_TYPES", map);
		}
		return map;
	}

	protected Map<String,String> getInstVars(String instTypeID)
	{
		final Map<String,Map<String,String>> map = getAllInstanceTypesMap();
		instTypeID=instTypeID.trim().toUpperCase();
		if(map.containsKey(instTypeID))
			return map.get(instTypeID);
		for(final String key : map.keySet())
		{
			if(key.startsWith(instTypeID))
				return map.get(key);
		}
		for(final String key : map.keySet())
		{
			if(key.indexOf(instTypeID)>=0)
				return map.get(key);
		}
		for(final String key : map.keySet())
		{
			if(key.endsWith(instTypeID))
				return map.get(key);
		}
		return null;
	}

	protected void destroyInstance(final Area instA)
	{
		if((instA != null)
		&&(CMath.bset(instA.flags(), Area.FLAG_INSTANCE_CHILD)))
		{
			Area parentArea = null;
			int x=instA.Name().indexOf('_');
			if(x<0)
				x=instA.Name().indexOf(' ');
			if(x>=0)
				parentArea = CMLib.map().getArea(Name().substring(x+1));

			for(final Enumeration<Room> r=instA.getFilledProperMap();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if(R!=null)
				{
					if(R.numInhabitants()>0)
						R.showHappens(CMMsg.MSG_OK_ACTION, L("This instance is fading away..."));
					for(final Enumeration<MOB> i=R.inhabitants();i.hasMoreElements();)
					{
						final MOB M=i.nextElement();
						if((M!=null)
						&&(M.isPlayer()))
						{
							Room oldRoom = (this.oldRoom!=null) ? CMLib.map().getRoom(this.oldRoom.get()) : null;
							if((oldRoom==null)
							||(oldRoom.amDestroyed())
							||(oldRoom.getArea()==null)
							||(!oldRoom.getArea().isRoom(oldRoom)))
								oldRoom=M.getStartRoom();
							for(int i1=0; (i1<50) && (oldRoom != R) && (R.isInhabitant(M) || M.location()==R);i1++)
							{
								oldRoom.bringMobHere(M, true);
								CMLib.commands().postLook(M,true);
								R.delInhabitant(M);
							}
						}
					}
					for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
					{
						final Item I=i.nextElement();
						if((I instanceof DeadBody)
						&&(((DeadBody)I).isPlayerCorpse()))
						{
							if((parentArea != null)
							&&(R.roomID().length()>0)
							&&(R.roomID().indexOf(parentArea.Name()+"#")>=0)
							&&(parentArea.getRoom(parentArea.Name()+R.roomID().substring(R.roomID().lastIndexOf('#'))))!=null)
							{
								final Room sendR=parentArea.getRoom(parentArea.Name()+R.roomID().substring(R.roomID().lastIndexOf('#')));
								sendR.moveItemTo(I);
							}
							else
							{
								MOB M=((DeadBody)I).getSavedMOB();
								if(M==null)
									M=CMLib.players().getPlayerAllHosts(((DeadBody)I).getMobName());
								if(M!=null)
								{
									if(M.location()!=null)
										M.location().moveItemTo(I);
									else
									if(M.getStartRoom()!=null)
										M.getStartRoom().moveItemTo(I);
								}
							}
						}
					}
				}
			}
			final MOB mob=CMClass.getFactoryMOB();
			try
			{
				final CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_EXPIRE,null);
				final LinkedList<Room> propRooms = new LinkedList<Room>();
				for(final Enumeration<Room> r=instA.getFilledProperMap();r.hasMoreElements();)
					propRooms.add(r.nextElement());
				// sends everyone home
				for(final Iterator<Room> r=propRooms.iterator();r.hasNext();)
				{
					final Room R=r.next();
					try
					{
						CMLib.map().emptyRoom(R, null, true);
					}
					catch(final Exception e)
					{
						Log.errOut(e);
					}
				}
				// msgs only, handles saves and stuff, but ignores grid rooms!!
				for(final Iterator<Room> r=propRooms.iterator();r.hasNext();)
				{
					final Room R=r.next();
					try
					{
						try
						{
							R.clearSky();
							msg.setTarget(R);
							R.executeMsg(mob,msg);
						}
						catch(final Exception e)
						{
							Log.errOut(e);
						}
						R.destroy(); // destroys the mobs and items.  the Deadly Thing.
					}
					catch(final Exception e)
					{
						Log.errOut(e);
					}
				}
				propRooms.clear();
				CMLib.map().delArea(instA);
				instA.destroy();
			}
			finally
			{
				mob.destroy();
			}
			instA.destroy();
		}
	}

	protected void destroyInstance()
	{
		destroyInstance(instArea);
		this.instArea=null;
	}

	protected String getStrippedRoomID(final String roomID)
	{
		final int x=roomID.indexOf('#');
		if(x<0)
			return null;
		return roomID.substring(x);
	}

	protected String convertToMyArea(final String Name, final String roomID)
	{
		final String strippedID=getStrippedRoomID(roomID);
		if(strippedID==null)
			return null;
		return Name+strippedID;
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		super.setAffectedOne(P);
		if(invoker != null)
		{
			final MOB mob=invoker;
			final Room R=mob.location();
			if(R!=null)
			{
				final InstanceArea currentShift = getInstanceArea(R.getArea());
				final Room oldRoom = (currentShift != null) ? currentShift.getOldRoom() : null;
				if(oldRoom != null)
					this.oldRoom=new WeakReference<Room>(oldRoom);
				else
				if(currentShift != null)
					this.oldRoom=new WeakReference<Room>(mob.getStartRoom());
				else
					this.oldRoom=new WeakReference<Room>(R);
			}
		}
	}

	protected InstanceArea getInstanceArea(final Physical P)
	{
		for(final Enumeration<Ability> a=P.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A instanceof InstanceArea)
				return (InstanceArea)A;
		}
		return null;
	}

	protected String castingMessage(final MOB mob, final boolean auto)
	{
		return auto?L(""):L("^S<S-NAME> conjur(s) a powerful transformation!^?");
	}

	protected String failMessage(final MOB mob, final boolean auto)
	{
		return L("^S<S-NAME> attempt(s) to conjure a powerful transformation, and fails.");
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			destroyInstance();
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		oldRoom = null;
		clearVars();

		if(commands.size()<1)
		{
			mob.tell(L("Transform to what?"));
			mob.tell(L("Known types: @x1",listOfInstanceIDs()));
			return false;
		}
		String instTypeID=CMParms.combine(commands,0).trim().toUpperCase();
		int instTypeIDCt=0;
		while((getInstVars(instTypeID)==null)&&(commands.size()>instTypeIDCt))
			instTypeID=CMParms.combine(commands,++instTypeIDCt).trim().toUpperCase();
		final Map<String,String> instFound = getInstVars(instTypeID);
		if(instFound == null)
		{
			mob.tell(L("There is no known type '@x1'.",instTypeID));
			mob.tell(L("Known types: @x1",listOfInstanceIDs()));
			return false;
		}
		instTypeID = instFound.get(InstVar.ID.toString()).toUpperCase().trim();

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		oldRoom=new WeakReference<Room>(mob.location());
		Area doCloneArea = mob.location().getArea();
		final Area mobArea = doCloneArea;
		String cloneRoomID=CMLib.map().getExtendedRoomID(mob.location());
		final InstanceArea currentInstanceAbility = getInstanceArea(mobArea);

		final boolean success=proficiencyCheck(mob,0,auto);
		if((currentInstanceAbility!=null)
		&&(currentInstanceAbility.text().equalsIgnoreCase(instTypeID)))
		{
			this.beneficialVisualFizzle(mob, null, failMessage(mob, auto));
			return false;
		}

		if(currentInstanceAbility != null)
		{
			final String areaName = doCloneArea.Name();
			final int x=areaName.indexOf('_');
			if((x>0)&&(CMath.isNumber(areaName.substring(0, x))))
			{
				final Area newCloneArea=CMLib.map().getArea(areaName.substring(x+1));
				if(newCloneArea!=null)
				{
					doCloneArea=newCloneArea;
					if(cloneRoomID.startsWith(areaName)
					&&(doCloneArea.getRoom(cloneRoomID.substring(x+1))!=null))
						cloneRoomID=cloneRoomID.substring(x+1);
					else
					{
						for(int i=0;i<100;i++)
						{
							final Room R=doCloneArea.getRandomProperRoom();
							if((R!=null)
							&&(!CMLib.flags().isHidden(R))
							&&(CMLib.map().getExtendedRoomID(R).length()>0))
							{
								cloneRoomID=CMLib.map().getExtendedRoomID(R);
								break;
							}
						}
					}
				}
			}
		}

		if(!success)
		{
			if(CMLib.dice().rollPercentage()>5)
			{
				this.beneficialVisualFizzle(mob, null, failMessage(mob, auto));
				return false;
			}
		}

		final String instCodeString = instTypeID + "_" + doCloneArea.Name();
		int hardBumpLevel = 0;
		if(recentVisits.containsKey(instCodeString)
		&&((recentVisits.get(instCodeString)[0]+hardBumpTimeout)>System.currentTimeMillis()))
		{
			final long[] data = this.recentVisits.get(instCodeString);
			data[0]=System.currentTimeMillis();
			if(data[1]==0)
				data[1]++;
			else
				data[1]*=2;
			hardBumpLevel=(int)data[1];
		}
		else
			this.recentVisits.put(instCodeString, new long[] {System.currentTimeMillis(),0});
		final String newInstanceName = instIDNum.addAndGet(1)+"_"+doCloneArea.Name();
		Area instArea = CMClass.getAreaType("SubThinInstance");
		instArea.setName(newInstanceName);
		instArea.addBlurbFlag("AREAINSTANCE {"+instTypeID+"}");
		CMLib.map().addArea(instArea);
		instArea.setAreaState(Area.State.ACTIVE); // starts ticking
		Room target=CMClass.getLocale("StdRoom");
		String newRoomID=this.convertToMyArea(newInstanceName,cloneRoomID);
		if(newRoomID==null)
			newRoomID=cloneRoomID;
		target.setRoomID(newRoomID);
		target.setDisplayText("Between Realities");
		target.setDescription("You are a floating consciousness between realiities...");
		target.setArea(instArea);

		//CMLib.map().delArea(this.instArea);
		final Area oldInstanceArea=instArea;
		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOVE|verbalCastCode(mob,target,auto),castingMessage(mob, auto));
		if((mob.location().okMessage(mob,msg))
		&&(target.okMessage(mob,msg)))
		{
			mob.location().send(mob,msg);
			target=(Room)msg.target();
			instArea = ((Room)msg.target()).getArea();

			final List<MOB> h=properTargetList(mob,givenTarget,false);
			if(h==null)
				return false;

			this.lastCasting=System.currentTimeMillis();
			this.instArea = instArea;
			final InstanceArea A;
			if((instArea!=oldInstanceArea)
			&&(instArea.fetchEffect(ID())!=null))
			{
				oldInstanceArea.destroy();
				CMLib.map().delArea(oldInstanceArea);
				A=(InstanceArea)instArea.fetchEffect(ID());
			}
			else
			{
				A=(InstanceArea)this.beneficialAffect(mob, instArea, asLevel, 0);
				if(A!=null)
				{
					A.hardBumpLevel= hardBumpLevel;
					A.setMiscText(instTypeID);
				}
			}

			final Room thisRoom=mob.location();
			for (final MOB follower : h)
			{
				final boolean invisible = !CMLib.flags().isSeeable(follower);
				final CMMsg enterMsg=CMClass.getMsg(follower,target,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,
						invisible?"":("<S-NAME> fade(s) into view.")+CMLib.protocol().msp("appear.wav",10));
				final CMMsg leaveMsg=CMClass.getMsg(follower,thisRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,
						invisible?"":L("<S-NAME> fade(s) away."));
				if(thisRoom.okMessage(follower,leaveMsg)
				&&(follower.okMessage(follower, enterMsg))
				&&target.okMessage(follower,enterMsg))
				{
					if(follower.isInCombat())
					{
						CMLib.commands().postFlee(follower,("NOWHERE"));
						follower.makePeace(false);
					}
					thisRoom.send(follower,leaveMsg);
					((Room)enterMsg.target()).bringMobHere(follower,false);
					follower.tell(L("\n\r\n\r"));
					((Room)enterMsg.target()).send(follower,enterMsg);
					CMLib.commands().postLook(follower,true);
				}
				else
				if(follower==mob)
					break;
			}
			instArea.addBlurbFlag("AREAINSTANCE {"+instTypeID+"}");
		}
		// return whether it worked
		return success;
	}
}
