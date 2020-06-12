package com.planet_ink.coffee_mud.Abilities;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.CompiledFormula;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.core.exceptions.CoffeeMudException;
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

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/*
   Copyright 2016-2020 Bo Zimmerman

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
public class StdPlanarAbility extends StdAbility implements PlanarAbility
{
	@Override
	public String ID()
	{
		return "StdPlanarAbility";
	}

	private final static String	localizedName	= CMLib.lang().L("Planar Shifting Ability");

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
	protected Area						planeArea		= null;
	protected Map<String, String>		planeVars		= null;
	protected WeakArrayList<Room>		roomsDone		= new WeakArrayList<Room>();
	protected int						planarLevel		= 1;
	protected String					planarName		= "";
	protected String					planarPrefix	= null;
	protected PairList<Integer,String>	promotions		= null;
	protected List<String>				categories		= null;
	protected List<String>				opposed			= null;
	protected PairList<String, String>	behavList		= null;
	protected PairList<String, String>	reffectList		= null;
	protected PairList<String, String>	factionList		= null;
	protected int						bonusDmgStat	= -1;
	protected Set<String>				reqWeapons		= null;
	protected int						recoverRate		= 0;
	protected int						fatigueRate		= 0;
	protected volatile int				recoverTick		= 0;
	protected Set<PlanarSpecFlag>		specFlags		= null;
	protected int						hardBumpLevel	= 0;
	protected CompiledFormula			levelFormula	= null;
	protected final Map<String,long[]>	recentVisits	= new TreeMap<String,long[]>();

	protected final static long			hardBumpTimeout	= (60L * 60L * 1000L);

	protected PairList<Pair<Integer,Integer>,PairList<String,String>> enableList=null;

	protected static final AtomicInteger planeIDNum = new AtomicInteger(0);

	/**
	 * @return the oldRoom
	 */
	@Override
	public final Room getOldRoom()
	{
		return (oldRoom != null) ? oldRoom.get() : null;
	}

	/**
	 * @param oldRoom the oldRoom to set
	 */
	@Override
	public final void setOldRoom(final Room oldRoom)
	{
		if(oldRoom == null)
			this.oldRoom = null;
		else
			this.oldRoom = new WeakReference<Room>(oldRoom);
	}

	/**
	 * @return the planarPrefix
	 */
	@Override
	public final String getPlanarPrefix()
	{
		return planarPrefix;
	}

	/**
	 * @return the hardBumpLevel
	 */
	@Override
	public final int getHardBumpLevel()
	{
		return hardBumpLevel;
	}

	/**
	 * @param hardBumpLevel the hardBumpLevel to set
	 */
	@Override
	public final void setHardBumpLevel(final int hardBumpLevel)
	{
		this.hardBumpLevel = hardBumpLevel;
	}

	/**
	 * @return the planeVars
	 */
	@Override
	public final Map<String, String> getPlaneVars()
	{
		return planeVars;
	}

	/**
	 * @return the planarLevel
	 */
	@Override
	public final int getPlanarLevel()
	{
		return planarLevel;
	}

	/**
	 * @param level the new planarLevel
	 */
	@Override
	public final void setPlanarLevel(final int level)
	{
		planarLevel = level;
	}

	/**
	 * @return the planarName
	 */
	@Override
	public final String getPlanarName()
	{
		return planarName;
	}

	@Override
	public void setPlanarName(final String planeName)
	{
		setMiscText(planeName);
	}

	/**
	 * @return the promotions
	 */
	@Override
	public final PairList<Integer, String> getPromotions()
	{
		return promotions;
	}

	/**
	 * @return the categories
	 */
	@Override
	public final List<String> getCategories()
	{
		return categories;
	}

	/**
	 * @return the opposed planes
	 */
	@Override
	public final List<String> getOpposed()
	{
		return opposed;
	}

	/**
	 * @return the behavList
	 */
	@Override
	public final PairList<String, String> getBehavList()
	{
		return behavList;
	}

	/**
	 * @return the reffectList
	 */
	@Override
	public final PairList<String, String> getReffectList()
	{
		return reffectList;
	}

	/**
	 * @return the factionList
	 */
	@Override
	public final PairList<String, String> getFactionList()
	{
		return factionList;
	}

	/**
	 * @return the bonusDmgStat
	 */
	@Override
	public final int getBonusDmgStat()
	{
		return bonusDmgStat;
	}

	/**
	 * @return the reqWeapons
	 */
	@Override
	public final Set<String> getReqWeapons()
	{
		return reqWeapons;
	}

	/**
	 * @return the recoverRate
	 */
	@Override
	public final int getRecoverRate()
	{
		return recoverRate;
	}

	/**
	 * @return the fatigueRate
	 */
	@Override
	public final int getFatigueRate()
	{
		return fatigueRate;
	}

	/**
	 * @return the specFlags
	 */
	@Override
	public final Set<PlanarSpecFlag> getSpecFlags()
	{
		return specFlags;
	}

	/**
	 * @return the levelFormula
	 */
	@Override
	public final CompiledFormula getLevelFormula()
	{
		return levelFormula;
	}

	/**
	 * @return the enableList
	 */
	@Override
	public final PairList<Pair<Integer, Integer>, PairList<String, String>> getEnableList()
	{
		return enableList;
	}

	public void clearVars()
	{
		planeArea = null;
		planarLevel=1;
		roomsDone=new WeakArrayList<Room>();
		planeVars=null;
		promotions=null;
		categories=null;
		opposed=null;
		planarPrefix=null;
		this.planarName="";
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



	@Override
	public String addOrEditPlane(final String planeName, final String rule)
	{
		final Map<String,Map<String,String>> map = getAllPlanesMap();
		final Map<String,String> planeParms = CMParms.parseEQParms(rule);
		for(final String key : planeParms.keySet())
		{
			if((CMath.s_valueOf(PlanarVar.class, key)==null)
			&&(CMLib.factions().getFaction(key)==null)
			&&(CMLib.factions().getFactionByName(key)==null))
				return "ERROR: Unknown planar var: "+key;
		}
		planeParms.put(PlanarVar.ID.toString(), planeName);
		if(!map.containsKey(planeName.trim().toUpperCase()))
		{
			String previ="";
			for(String i="";!i.equals(".9");i=("."+(Math.round(CMath.s_double(i)*10)+1)))
			{
				final CMFile F=new CMFile(Resources.makeFileResourceName("skills/planesofexistence.txt"+i), null);
				if(!F.exists())
					break;
				previ=i;
			}
			final CMFile F=new CMFile(Resources.makeFileResourceName("skills/planesofexistence.txt"+previ), null);
			final StringBuffer old=F.text();
			if((!old.toString().endsWith("\n"))
			&&(!old.toString().endsWith("\r")))
				old.append("\r\n");
			old.append("\"").append(planeName).append("\" ").append(rule).append("\r\n");
			F.saveText(old.toString());
			map.put(planeName.toUpperCase().trim(), planeParms);
			return null;
		}
		else
		{
			final Map<String,String> oldPlane = map.get(planeName.trim().toUpperCase());
			final StringBuilder changes = new StringBuilder("");
			for(final String oldKey : oldPlane.keySet())
			{
				if(!planeParms.containsKey(oldKey))
					changes.append("REMOVED: ").append(oldKey).append("\n\r");
				else
				{
					final String oldVal = oldPlane.get(oldKey);
					final String newVal = planeParms.get(oldKey);
					if(!oldVal.equals(newVal))
					{
						changes.append("CHANGED: ").append(oldKey).append(": '").append(oldVal)
								.append("' TO '").append(newVal).append("'").append("\n\r");
					}
				}
			}
			for(final String newKey : planeParms.keySet())
			{
				if(!oldPlane.containsKey(newKey))
					changes.append("ADDED: ").append(newKey).append("\n");
			}
			if(changes.length()==0)
				return "";
			for(String i="";!i.equals(".9");i=("."+(Math.round(CMath.s_double(i)*10)+1)))
			{
				if(alterPlaneLine(planeName, Resources.makeFileResourceName("skills/planesofexistence.txt"+i), rule))
				{
					map.put(planeName.toUpperCase().trim(), planeParms);
					return changes.toString();
				}
			}
			return "ERROR: Not Found!";
		}
	}

	protected boolean alterPlaneLine(final String planeName, final String fileName, final String rule)
	{
		final CMFile F=new CMFile(fileName,null);
		if(!F.exists())
			return false;
		final List<String> lines = Resources.getFileLineVector(F.text());
		for(int i=0;i<lines.size();i++)
		{
			final String line=lines.get(i).trim();
			String planename=null;
			if(line.startsWith("\""))
			{
				final int x=line.indexOf("\"",1);
				if(x>1)
					planename=line.substring(1,x);
			}
			if((planename != null)
			&&(planename.equalsIgnoreCase(planeName)))
			{
				if(rule == null)
					lines.remove(i);
				else
					lines.set(i,"\""+planeName+"\" "+rule);
				final StringBuilder newFile = new StringBuilder("");
				for(final String fline : lines)
					newFile.append(fline).append("\r\n");
				Resources.removeResource("SKILL_PLANES_OF_EXISTENCE");
				Resources.removeResource(fileName);
				F.saveText(newFile.toString());
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean deletePlane(final String planeName)
	{
		final Map<String,Map<String,String>> map = getAllPlanesMap();
		if(!map.containsKey(planeName.trim().toUpperCase()))
			return false;
		for(String i="";!i.equals(".9");i=("."+(Math.round(CMath.s_double(i)*10)+1)))
		{
			if(alterPlaneLine(planeName, Resources.makeFileResourceName("skills/planesofexistence.txt"+i), null))
			{
				map.remove(planeName.trim().toUpperCase());
				return true;
			}
		}
		return false;
	}

	@Override
	public void setMiscText(final String newText)
	{
		super.setMiscText(newText);
		clearVars();
		if(newText.length()>0)
		{
			this.planarName=newText;
			this.planeVars=getPlanarVars(newText);
			if(this.planeVars==null)
			{
				if(newText.equalsIgnoreCase("DEFAULT_NEW"))
					this.planeVars=new Hashtable<String,String>();
				else
					throw new IllegalArgumentException("Unknown: "+newText);
			}
			this.roomsDone=new WeakArrayList<Room>();
			this.planarPrefix=planeVars.get(PlanarVar.PREFIX.toString());
			if(planeVars.containsKey(PlanarVar.CATEGORY.toString()))
			{
				final String catStr=planeVars.get(PlanarVar.CATEGORY.toString());
				if(catStr != null)
				{
					this.categories=CMParms.parseCommas(catStr, true);
					if((!planeVars.containsKey(PlanarVar.TRANSITIONAL.toString()))
					&&(catStr.toLowerCase().indexOf("transitional")>=0))
						planeVars.put(PlanarVar.TRANSITIONAL.toString(), "true");
				}
			}
			if(planeVars.containsKey(PlanarVar.OPPOSED.toString()))
			{
				final String catStr=planeVars.get(PlanarVar.OPPOSED.toString());
				this.opposed=CMParms.parseCommas(catStr, true);
			}
			this.recoverRate = CMath.s_int(planeVars.get(PlanarVar.RECOVERRATE.toString()));
			this.fatigueRate = CMath.s_int(planeVars.get(PlanarVar.FATIGUERATE.toString()));
			this.recoverTick=1;
			if((planarPrefix!=null)&&(planarPrefix.indexOf(',')>0))
			{
				final List<String> choices=CMParms.parseCommas(planarPrefix, true);
				planarPrefix=choices.get(CMLib.dice().roll(1, choices.size(), -1));
			}
			if(affected instanceof Area)
			{
				planeArea=(Area)affected;
				int medianLevel=planeArea.getPlayerLevel();
				if(medianLevel <= 0)
					medianLevel=planeArea.getAreaIStats()[Area.Stats.MED_LEVEL.ordinal()];
				planarLevel=medianLevel;
			}
			this.specFlags = null;
			final String specflags = planeVars.get(PlanarVar.SPECFLAGS.toString());
			if(specflags != null)
			{
				for(final String s : CMParms.parse(specflags))
				{
					final PlanarSpecFlag flag=(PlanarSpecFlag)CMath.s_valueOf(PlanarSpecFlag.class, s);
					if(flag == null)
						Log.errOut("Spell_Planeshift","Unknown spec flag "+s);
					else
					{
						if(this.specFlags==null)
							this.specFlags=new HashSet<PlanarSpecFlag>();
						this.specFlags.add(flag);
					}
				}
			}
			this.behavList = null;
			final String behaves = planeVars.get(PlanarVar.BEHAVE.toString());
			if(behaves!=null)
				this.behavList=new PairVector<String,String>(CMParms.parseSpaceParenList(behaves));
			this.reffectList = null;
			final String reffects = planeVars.get(PlanarVar.REFFECT.toString());
			if(reffects!=null)
				this.reffectList=new PairVector<String,String>(CMParms.parseSpaceParenList(reffects));
			this.factionList = null;
			final String factions = planeVars.get(PlanarVar.FACTIONS.toString());
			if(factions!=null)
				this.factionList=new PairVector<String,String>(CMParms.parseSpaceParenList(factions));
			String levelFormulaStr = planeVars.get(PlanarVar.LEVELADJ.toString());
			if((levelFormulaStr == null)||(levelFormulaStr.trim().length()==0))
				levelFormulaStr = "(@x3 - (@x1 - @x2) + 0) > 1";
			else
			if(CMath.isInteger(levelFormulaStr.trim()))
				levelFormulaStr = "(@x3 - (@x1 - @x2) + "+levelFormulaStr+") > 1";
			this.levelFormula = CMath.compileMathExpression(levelFormulaStr);
			final String autoReactionTypeStr=CMProps.getVar(CMProps.Str.AUTOREACTION).toUpperCase().trim();
			if((autoReactionTypeStr.indexOf("PLANAR")>=0)
			&&(this.factionList!=null)
			&&(this.factionList.containsFirst("*")))
			{
				final String nameCode=newText.toUpperCase().trim();
				Faction F=CMLib.factions().getFaction("PLANE_"+nameCode);
				if(F==null)
					F=CMLib.factions().makeReactionFaction("PLANE_","CLASSID",newText,nameCode,"examples/planarreaction.ini");
			}
			final String enables = planeVars.get(PlanarVar.ENABLE.toString());
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
								Log.errOut("Spell_Planeshift","Unknown skill type/domain/flag: "+P.first);
						}
						else
							addThese.add(new Pair<String,String>(P.first,P.second));
						this.enableList.add(P.third,addThese);
					}
				}
			}
			final String bonusDamageStat = planeVars.get(PlanarVar.BONUSDAMAGESTAT.toString());
			this.bonusDmgStat = -1;
			if(bonusDamageStat!=null)
				this.bonusDmgStat=CMParms.indexOf(CharStats.CODES.BASENAMES(), bonusDamageStat.toUpperCase().trim());
			final String reqWeapons = planeVars.get(PlanarVar.REQWEAPONS.toString());
			this.reqWeapons = null;
			if(reqWeapons != null)
				this.reqWeapons = new HashSet<String>(CMParms.parse(reqWeapons.toUpperCase().trim()));
			this.promotions=null;
			if(planeVars.containsKey(PlanarVar.PROMOTIONS.toString()))
			{
				final List<String> bits=CMParms.parseCommas(planeVars.get(PlanarVar.PROMOTIONS.toString()), true);
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

			final Area planeArea=this.planeArea;
			if(planeArea == null)
				return;
			final String areablurbs = planeVars.get(PlanarVar.AREABLURBS.toString());
			if((areablurbs!=null)&&(areablurbs.length()>0))
			{
				final Map<String,String> blurbSets=CMParms.parseEQParms(areablurbs);
				for(final String key : blurbSets.keySet())
					planeArea.addBlurbFlag(key.toUpperCase().trim().replace(' ', '_')+" "+blurbSets.get(key));
			}
			final String atmosphere = planeVars.get(PlanarVar.ATMOSPHERE.toString());
			if(atmosphere!=null)
			{
				if(atmosphere.length()==0)
					this.planeArea.setAtmosphere(Integer.MIN_VALUE);
				else
				{
					final int atmo=RawMaterial.CODES.FIND_IgnoreCase(atmosphere);
					this.planeArea.setAtmosphere(atmo);
				}
			}
			final String absorb = planeVars.get(PlanarVar.ABSORB.toString());
			if(absorb != null)
				reEffect(planeArea,"Prop_AbsorbDamage",absorb);
			final TimeClock C=(TimeClock)CMLib.time().globalClock().copyOf();
			C.setDayOfMonth(1);
			C.setYear(1);
			C.setMonth(1);
			C.setHourOfDay(0);
			final String hours = planeVars.get(PlanarVar.HOURS.toString());
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
			planeArea.setTimeObj(C);
			for(final CMObject O : getAreaEffectsBehavs())
			{
				if(O instanceof Ability)
					planeArea.addNonUninvokableEffect((Ability)O);
				else
				if(O instanceof Behavior)
				{
					if(planeArea.fetchBehavior(O.ID())==null)
						planeArea.addBehavior((Behavior)O);
				}
			}
		}
	}

	@Override
	public List<CMObject> getAreaEffectsBehavs()
	{
		final List<CMObject> aeffectbehavs = new Vector<CMObject>();
		{
			final Ability A=CMClass.getAbility("Prop_NoTeleportOut");
			A.setMiscText("exceptions=planarability");
			aeffectbehavs.add(A);
		}
		{
			final Ability A=CMClass.getAbility("Prop_NoTeleport");
			A.setMiscText("exceptions=planarability");
			aeffectbehavs.add(A);
		}
		aeffectbehavs.add(CMClass.getAbility("Prop_NoRecall"));
		final String aeffects = planeVars.get(PlanarVar.AEFFECT.toString());
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
							Log.errOut("Spell_Planeshift","Unknown behavior : "+p.first);
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

	@Override
	public boolean isPlanarMob(final MOB M)
	{
		final String badTattooName = "NOPLANE "+this.planarName.toUpperCase().trim();
		return (M!=null)
			 &&(M.isMonster())
			 &&(M.getStartRoom()!=null)
			 &&(M.findTattoo(badTattooName)==null)
			 &&(M.findTattoo("NOPLANE")==null);
	}

	@Override
	public void doPlanarRoomColoring(final Room room)
	{
		if(planeVars.containsKey(PlanarVar.ROOMCOLOR.toString()))
		{
			String prefix="";
			String displayText = room.displayText();
			if(displayText.toUpperCase().startsWith("<VARIES>"))
			{
				prefix="<VARIES>";
				displayText=displayText.substring(prefix.length());
			}
			String color=planeVars.get(PlanarVar.ROOMCOLOR.toString());
			if(color.startsWith("UP "))
			{
				color=color.substring(3).trim();
				displayText=displayText.toUpperCase();
			}
			room.setDisplayText(prefix+color+displayText+"^N");
		}
		if(planeVars.containsKey(PlanarVar.ROOMADJS.toString()))
		{
			String wordStr=planeVars.get(PlanarVar.ROOMADJS.toString());
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

	@Override
	public void applyMobPrefix(final MOB M, final int[] eliteBump)
	{
		String planarPrefix = this.planarPrefix;
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
				if(planarPrefix == null)
					planarPrefix = "";
				planarPrefix = (bestAvail.second+" "+planarPrefix).trim();
				if(eliteBump != null)
					eliteBump[0] += 1+(bestAvail.first.intValue()*2);
			}
		}
		if((planarPrefix!=null)&&(planarPrefix.length()>0))
		{
			final String oldName=M.Name();
			int x;
			if(oldName.toLowerCase().indexOf(planarPrefix.toLowerCase())<0)
			{
				if(CMLib.english().startsWithAnArticle(M.Name()))
				{
					final String Name = M.Name().substring(M.Name().indexOf(' ')).trim();
					M.setName(CMLib.english().startWithAorAn(planarPrefix+" "+Name));
				}
				else
				{
					M.setName(CMStrings.capitalizeFirstLetter(planarPrefix)+" "+M.Name());
				}
				if((x=M.displayText().toLowerCase().indexOf(oldName.toLowerCase()))>=0)
				{
					M.setDisplayText(M.displayText().substring(0,x)+M.Name()+M.displayText().substring(x+oldName.length()));
				}
				else
				if(CMLib.english().startsWithAnArticle(M.displayText()))
				{
					final String Name = M.displayText().substring(M.displayText().indexOf(' ')).trim();
					M.setDisplayText(CMLib.english().startWithAorAn(planarPrefix+" "+Name));
				}
				else
				if((x=M.displayText().toLowerCase().indexOf(M.charStats().getMyRace().name().toLowerCase()))>=0)
				{
					final int len=M.charStats().getMyRace().name().toLowerCase().length();
					M.setDisplayText(M.displayText().substring(0,x)+planarPrefix+M.Name()+M.displayText().substring(x+len));
				}
			}
		}
	}

	public synchronized void fixRoom(final Room room)
	{
		try
		{
			room.toggleMobility(false);
			CMLib.threads().suspendResumeRecurse(room, false, true);
			for(int i=0;i<Directions.NUM_DIRECTIONS();i++)
			{
				final Room R=room.rawDoors()[i];
				if((R!=null)&&(R.getArea()!=planeArea))
					room.rawDoors()[i]=null;
			}
			int eliteLevel=0;
			if(planeVars.containsKey(PlanarVar.ELITE.toString()))
				eliteLevel=CMath.s_int(planeVars.get(PlanarVar.ELITE.toString()));
			if(planeVars.containsKey(PlanarVar.ATMOSPHERE.toString()))
				room.setAtmosphere(planeArea.getAtmosphere());
			doPlanarRoomColoring(room);
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
								Log.errOut("Spell_Planeshift","Unknown behavior : "+p.first);
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
						if((R2!=null)&&(R2.getArea()!=planeArea))
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
					final double[] vars=new double[] {planarLevel, I.phyStats().level(), invoker.phyStats().level() } ;
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
				&&(isPlanarMob(M))
				&&(M.getStartRoom().getArea()==planeArea))
				{
					if(planeVars.containsKey(PlanarVar.MIXRACE.toString()))
					{
						final String mixRace = planeVars.get(PlanarVar.MIXRACE.toString());
						final Race firstR=CMClass.getRace(mixRace);
						if(firstR==null)
							Log.errOut("StdPlanarAbility","Unknown mixrace: "+mixRace);
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

					if(planeVars.containsKey(PlanarVar.ATMOSPHERE.toString()))
						M.baseCharStats().setBreathables(new int[]{room.getAtmosphere()});
					final double[] vars=new double[] {planarLevel, M.phyStats().level(), invoker.phyStats().level() } ;
					final int newLevel = (int)CMath.round(CMath.parseMathExpression(this.levelFormula, vars, 0.0));
					final int[] eliteBump = new int[1];
					this.applyMobPrefix(M, eliteBump);
					eliteLevel += eliteBump[0];
					M.basePhyStats().setLevel(newLevel);
					M.phyStats().setLevel(newLevel);
					CMLib.leveler().fillOutMOB(M,M.basePhyStats().level()+hardBumpLevel);
					M.basePhyStats().setLevel(newLevel);
					M.phyStats().setLevel(newLevel);
					final String align=planeVars.get(PlanarVar.ALIGNMENT.toString());
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
									?("PLANE_"+this.planarName.toUpperCase().trim())
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
							final double[] ivars=new double[] {planarLevel, mI.phyStats().level(), invoker.phyStats().level() } ;
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
					final String resistWeak = planeVars.get(PlanarVar.MOBRESIST.toString());
					if(resistWeak != null)
						reEffect(M,"Prop_Resistance",resistWeak);
					else
					if(this.hardBumpLevel>0)
						reEffect(M,"Prop_Resistance","magic holy disease poison evil weapons "+(5*hardBumpLevel)+"% ");
					final String setStat = planeVars.get(PlanarVar.SETSTAT.toString());
					if(setStat != null)
						reEffect(M,"Prop_StatTrainer",setStat);
					final String behavaffid=planeVars.get(PlanarVar.BEHAVAFFID.toString());
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
					final String adjStat = planeVars.get(PlanarVar.ADJSTAT.toString());
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
						final String adjSize = planeVars.get(PlanarVar.ADJSIZE.toString());
						if(adjSize != null)
						{
							final double heightAdj = CMParms.getParmDouble(adjSize, "HEIGHT", Double.MIN_VALUE);
							if(heightAdj > Double.MIN_VALUE)
								reEffect(M,"Prop_Adjuster","height+"+(100+(heightAdj*100)));
						}
					}
					else
					{
						final String adjust = planeVars.get(PlanarVar.ADJUST.toString());
						if(adjust != null)
							reEffect(M,"Prop_Adjuster",adjust);
						final String adjSize = planeVars.get(PlanarVar.ADJSIZE.toString());
						if(adjSize != null)
						{
							final double heightAdj = CMParms.getParmDouble(adjSize, "HEIGHT", Double.MIN_VALUE);
							if(heightAdj > Double.MIN_VALUE)
								reEffect(M,"Prop_Adjuster","height+"+(int)Math.round(CMath.mul(M.basePhyStats().height(),heightAdj)));
						}
					}
					final String adjSize = planeVars.get(PlanarVar.ADJSIZE.toString());
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
									Log.errOut("Spell_Planeshift","Unknown behavior : "+p.first);
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
			final int mobCopy=CMath.s_int(planeVars.get(PlanarVar.MOBCOPY.toString()));
			if(mobCopy>0)
			{
				final List<MOB> list=new ArrayList<MOB>(room.numInhabitants());
				for(final Enumeration<MOB> m=room.inhabitants();m.hasMoreElements();)
				{
					final MOB M=m.nextElement();
					if((M!=null)
					&&(M.isMonster())
					&&(M.getStartRoom()!=null)
					&&(M.getStartRoom().getArea()==planeArea))
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
		if(this.specFlags!=null)
		{
			if(this.specFlags.contains(PlanarSpecFlag.ALLBREATHE))
				affectableStats.setBreathables(new int[]{});
		}
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		if(affected instanceof MOB)
		{
			if(this.bonusDmgStat>=0)
				affectableStats.setDamage(affectableStats.damage() + (((((MOB)affected).charStats().getStat(this.bonusDmgStat))-10)/2));
			if(this.specFlags!=null)
			{
				if(this.specFlags.contains(PlanarSpecFlag.NOINFRAVISION))
					affectableStats.setSensesMask(CMath.unsetb(affectableStats.sensesMask(), PhyStats.CAN_SEE_INFRARED));
				if(this.specFlags.contains(PlanarSpecFlag.BADMUNDANEARMOR))
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

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if((ticking instanceof Area)&&(tickID == Tickable.TICKID_AREA))
		{
			if(((this.recoverRate>0)||(this.fatigueRate>0)) &&(--this.recoverTick <= 0) && (this.planeArea!=null))
			{
				this.recoverTick = CMProps.getIntVar(CMProps.Int.RECOVERRATE) * CharState.REAL_TICK_ADJUST_FACTOR;
				for(final Enumeration<Room> r=planeArea.getFilledProperMap();r.hasMoreElements();)
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
			&&(((Room)msg.target()).getArea()==planeArea))
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
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_WEAPONATTACK:
			if((msg.tool() instanceof AmmunitionWeapon)
			&&(((AmmunitionWeapon)msg.tool()).requiresAmmunition())
			&&(msg.target() instanceof MOB)
			&&(planeVars!=null)
			&&(planeVars.containsKey(PlanarVar.WEAPONMAXRANGE.toString()))
			&&((msg.source().rangeToTarget()>0)||(((MOB)msg.target()).rangeToTarget()>0)))
			{
				final int maxRange=CMath.s_int(planeVars.get(PlanarVar.WEAPONMAXRANGE.toString()));
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
		return true;
	}

	@Override
	public List<String> getAllPlaneKeys()
	{
		final Map<String,Map<String,String>> map = getAllPlanesMap();
		final List<String> transitions=new ArrayList<String>(map.size());
		for(final String key : map.keySet())
			transitions.add(key);
		return transitions;
	}

	protected static List<String> getTransitionPlaneKeys()
	{
		final Map<String,Map<String,String>> map = getAllPlanesMap();
		final List<String> transitions=new ArrayList<String>(2);
		for(final String key : map.keySet())
		{
			final Map<String,String> entry=map.get(key);
			if(CMath.s_bool(entry.get(PlanarVar.TRANSITIONAL.toString())))
				transitions.add(key);
		}
		return transitions;
	}

	@Override
	public String listOfPlanes()
	{
		final Map<String,Map<String,String>> map = getAllPlanesMap();
		final StringBuilder str=new StringBuilder();
		for(final String key : map.keySet())
		{
			final Map<String,String> entry=map.get(key);
			str.append(entry.get(PlanarVar.ID.toString())).append(", ");
		}
		if(str.length()<2)
			return "";
		return str.toString();
	}

	public static Map<String,Map<String,String>> getAllPlanesMap()
	{
		@SuppressWarnings("unchecked")
		Map<String,Map<String,String>> map = (Map<String,Map<String,String>>)Resources.getResource("SKILL_PLANES_OF_EXISTENCE");
		if(map == null)
		{
			map = new TreeMap<String,Map<String,String>>();
			final List<String> lines = new ArrayList<String>();
			for(String i="";!i.equals(".9");i=("."+(Math.round(CMath.s_double(i)*10)+1)))
			{
				final CMFile F=new CMFile(Resources.makeFileResourceName("skills/planesofexistence.txt"+i), null);
				if(!F.exists())
					break;
				lines.addAll(Resources.getFileLineVector(F.text()));
			}
			for(String line : lines)
			{
				line=line.trim();
				String planename=null;
				if(line.startsWith("\""))
				{
					final int x=line.indexOf("\"",1);
					if(x>1)
					{
						planename=line.substring(1,x);
						line=line.substring(x+1).trim();
					}
				}
				if(planename != null)
				{
					final Map<String,String> planeParms = CMParms.parseEQParms(line);
					for(final String key : planeParms.keySet())
					{
						if((CMath.s_valueOf(PlanarVar.class, key)==null)
						&&(CMLib.factions().getFaction(key)==null)
						&&(CMLib.factions().getFactionByName(key)==null))
							Log.errOut("Spell_Planeshift","Unknown planar var: "+key);
					}
					planeParms.put(PlanarVar.ID.toString(), planename);
					map.put(planename.toUpperCase(), planeParms);
				}
			}
			final boolean createFactions=CMProps.getVar(CMProps.Str.AUTOREACTION).toUpperCase().trim().indexOf("PLANAR")>=0;
			// do the "LIKE" matching, and build factions
			for(final String key : map.keySet())
			{
				final Map<String,String> parms=map.get(key);
				if(parms.containsKey(PlanarVar.LIKE.toString()))
				{
					final Map<String,String> otherMap = map.get(parms.get(PlanarVar.LIKE.toString()).trim().toUpperCase());
					if(otherMap != null)
					{
						for(final String var : otherMap.keySet())
						{
							if(!parms.containsKey(var))
								parms.put(var, otherMap.get(var));
						}
					}
				}
				if(createFactions
				&&(parms.containsKey(PlanarVar.FACTIONS.toString()))
				&&(parms.get(PlanarVar.FACTIONS.toString()).indexOf('*')>=0))
				{
					final String planename = parms.get(PlanarVar.ID.toString());
					final String nameCode=key.toUpperCase().trim();
					final Faction rF=CMLib.factions().getFaction("PLANE_"+nameCode);
					if(rF==null)
						CMLib.factions().makeReactionFaction("PLANE_","CLASSID",planename,nameCode,"examples/planarreaction.ini");
				}
			}
			Resources.submitResource("SKILL_PLANES_OF_EXISTENCE", map);
		}
		return map;
	}

	@Override
	public Map<String,String> getPlanarVars(String planeName)
	{
		final Map<String,Map<String,String>> map = getAllPlanesMap();
		planeName=planeName.trim().toUpperCase();
		if(map.containsKey(planeName))
			return map.get(planeName);
		for(final String key : map.keySet())
		{
			if(key.startsWith(planeName))
				return map.get(key);
		}
		for(final String key : map.keySet())
		{
			if(key.indexOf(planeName)>=0)
				return map.get(key);
		}
		for(final String key : map.keySet())
		{
			if(key.endsWith(planeName))
				return map.get(key);
		}
		return null;
	}

	@Override
	public void destroyPlane(final Area planeA)
	{
		if((planeA != null)
		&&(CMath.bset(planeA.flags(), Area.FLAG_INSTANCE_CHILD)))
		{
			Area parentArea = null;
			int x=planeA.Name().indexOf('_');
			if(x<0)
				x=planeA.Name().indexOf(' ');
			if(x>=0)
				parentArea = CMLib.map().getArea(Name().substring(x+1));

			for(final Enumeration<Room> r=planeA.getFilledProperMap();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if(R!=null)
				{
					if(R.numInhabitants()>0)
						R.showHappens(CMMsg.MSG_OK_ACTION, L("This plane is fading away..."));
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
				for(final Enumeration<Room> r=planeA.getFilledProperMap();r.hasMoreElements();)
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
				CMLib.map().delArea(planeA);
				planeA.destroy();
			}
			finally
			{
				mob.destroy();
			}
			planeA.destroy();
		}
	}

	protected void destroyPlane()
	{
		destroyPlane(planeArea);
		this.planeArea=null;
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
				final PlanarAbility currentShift = getPlanarAbility(R.getArea());
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

	protected PlanarAbility getPlanarAbility(final Physical P)
	{
		for(final Enumeration<Ability> a=P.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A instanceof PlanarAbility)
				return (PlanarAbility)A;
		}
		return null;
	}

	protected String castingMessage(final MOB mob, final boolean auto)
	{
		return auto?L(""):L("^S<S-NAME> conjur(s) a powerful planar connection!^?");
	}

	protected String failMessage(final MOB mob, final boolean auto)
	{
		return L("^S<S-NAME> attempt(s) to conjure a powerful planar connection, and fails.");
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			destroyPlane();
		}
		super.unInvoke();
	}

	protected boolean alwaysRandomArea=false;

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		oldRoom = null;
		clearVars();

		if(commands.size()<1)
		{
			mob.tell(L("Go where?"));
			mob.tell(L("Known planes: @x1",listOfPlanes()+L("Prime Material")));
			return false;
		}
		String planeName=CMParms.combine(commands,0).trim().toUpperCase();
		int planeNameCt=0;
		if(planeName.toLowerCase().endsWith("prime material"))
			planeName="Prime Material";
		else
		while((getPlanarVars(planeName)==null)&&(commands.size()>planeNameCt))
			planeName=CMParms.combine(commands,++planeNameCt).trim().toUpperCase();
		oldRoom=new WeakReference<Room>(mob.location());
		Area cloneArea = mob.location().getArea();
		final Area mobArea = cloneArea;
		String cloneRoomID=CMLib.map().getExtendedRoomID(mob.location());
		final PlanarAbility currentShift = getPlanarAbility(mobArea);
		if(planeName.equalsIgnoreCase("Prime Material"))
		{
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			if(currentShift == null)
			{
				mob.tell(L("You are already on the prime material plane."));
				return false;
			}
			final boolean success=proficiencyCheck(mob,0,auto);
			if(success)
			{
				final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MASK_MOVE|verbalCastCode(mob,null,auto),castingMessage(mob, auto));
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					currentShift.unInvoke();
				}
			}
			else
			{
				this.beneficialVisualFizzle(mob, null, failMessage(mob, auto));
			}
			return true;
		}
		else
		if(this.lastCasting > (System.currentTimeMillis() - (10 * TimeManager.MILI_MINUTE)))
		{
			final Ability A=CMClass.getAbility("Disease_PlanarInstability");
			if((A!=null)
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE))
			&&(!CMSecurity.isAbilityDisabled(A.ID())))
				A.invoke(mob, mob, true, 0);
		}
		Map<String,String> planeFound = getPlanarVars(planeName);
		if(planeFound == null)
		{
			mob.tell(L("There is no known plane '@x1'.",planeName));
			mob.tell(L("Known planes: @x1",listOfPlanes()+L("Prime Material")));
			return false;
		}
		planeName = planeFound.get(PlanarVar.ID.toString()).toUpperCase().trim();

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if((currentShift!=null)&&(currentShift.text().equalsIgnoreCase(planeName)))
		{
			this.beneficialVisualFizzle(mob, null, failMessage(mob, auto));
			return false;
		}

		if(currentShift != null)
		{
			final String areaName = cloneArea.Name();
			final int x=areaName.indexOf('_');
			if((x>0)&&(CMath.isNumber(areaName.substring(0, x))))
			{
				final Area newCloneArea=CMLib.map().getArea(areaName.substring(x+1));
				if(newCloneArea!=null)
				{
					cloneArea=newCloneArea;
					if(cloneRoomID.startsWith(areaName)
					&&(cloneArea.getRoom(cloneRoomID.substring(x+1))!=null))
					{
						cloneRoomID=cloneRoomID.substring(x+1);
					}
					else
					{
						for(int i=0;i<100;i++)
						{
							final Room R=cloneArea.getRandomProperRoom();
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

		boolean randomPlane=false;
		boolean randomTransitionPlane=false;
		boolean randomArea=alwaysRandomArea;
		if(((cloneArea.flags()&Area.FLAG_INSTANCE_CHILD)==Area.FLAG_INSTANCE_CHILD)
		&&(currentShift == null))
			randomArea=true;
		if(!success)
		{
			if(CMLib.dice().rollPercentage()>5)
			{
				this.beneficialVisualFizzle(mob, null, failMessage(mob, auto));
				return false;
			}
			else
			{
				if(proficiency()<50)
				{
					randomPlane=true;
					randomArea=true;
				}
				else
				if(proficiency()<75)
				{
					randomTransitionPlane=true;
					randomArea=true;
				}
				else
				if(proficiency()<100)
				{
					randomTransitionPlane=true;
				}
				else
					randomArea=true;
			}
		}
		else
		if(proficiencyCheck(mob,-95,auto))
		{
			// kaplah!
		}
		else
		if(proficiencyCheck(mob,-50,auto))
		{
			if(proficiency()<75)
			{
				randomTransitionPlane=true;
				randomArea=true;
			}
			else
			if(proficiency()<100)
			{
				randomTransitionPlane=true;
			}
		}
		else
		{
			if(proficiency()<75)
			{
				randomTransitionPlane=true;
				randomArea=true;
			}
			else
			if(proficiency()<100)
			{
				randomArea=true;
			}
			else
				randomTransitionPlane=true;
		}

		final List<String> transitionalPlaneKeys = getTransitionPlaneKeys();
		if(currentShift!=null)
		{
			if(transitionalPlaneKeys.contains(currentShift.text().toUpperCase().trim()))
			{
				if(randomTransitionPlane)
					randomTransitionPlane=false;
				else
				if(randomPlane)
				{
					randomPlane=false;
					randomTransitionPlane=true;
					randomArea=true;
				}
			}
		}

		if(randomArea)
		{
			int tries=0;
			while(((++tries)<10000))
			{
				final Room room=CMLib.map().getRandomRoom();
				if((room!=null)
				&&(CMLib.flags().canAccess(mob,room))
				&&(CMLib.map().getExtendedRoomID(room).length()>0)
				&&(room.getArea().numberOfProperIDedRooms()>2))
				{
					cloneArea=room.getArea();
					cloneRoomID=CMLib.map().getExtendedRoomID(room);
					break;
				}
			}
		}
		if(randomTransitionPlane)
		{
			planeName = transitionalPlaneKeys.get(CMLib.dice().roll(1, transitionalPlaneKeys.size(), -1));
			planeFound = getPlanarVars(planeName);
		}
		if(randomPlane)
		{
			final List<String> allPlaneKeys = getAllPlaneKeys();
			planeName = allPlaneKeys.get(CMLib.dice().roll(1, allPlaneKeys.size(), -1));
			planeFound = getPlanarVars(planeName);
		}

		final String planeCodeString = planeName + "_" + cloneArea.Name();
		int hardBumpLevel = 0;
		if(recentVisits.containsKey(planeCodeString)
		&&((recentVisits.get(planeCodeString)[0]+hardBumpTimeout)>System.currentTimeMillis()))
		{
			final long[] data = this.recentVisits.get(planeCodeString);
			data[0]=System.currentTimeMillis();
			if(data[1]==0)
				data[1]++;
			else
				data[1]*=2;
			hardBumpLevel=(int)data[1];
		}
		else
			this.recentVisits.put(planeCodeString, new long[] {System.currentTimeMillis(),0});
		final String newPlaneName = planeIDNum.addAndGet(1)+"_"+cloneArea.Name();
		Area planeArea = CMClass.getAreaType("SubThinInstance");
		planeArea.setName(newPlaneName);
		planeArea.addBlurbFlag("PLANEOFEXISTENCE {"+planeName+"}");
		CMLib.map().addArea(planeArea);
		planeArea.setAreaState(Area.State.ACTIVE); // starts ticking
		Room target=CMClass.getLocale("StdRoom");
		String newRoomID=this.convertToMyArea(newPlaneName,cloneRoomID);
		if(newRoomID==null)
			newRoomID=cloneRoomID;
		target.setRoomID(newRoomID);
		target.setDisplayText("Between The Planes of Existence");
		target.setDescription("You are a floating consciousness between the planes of existence...");
		target.setArea(planeArea);

		//CMLib.map().delArea(this.planeArea);
		final Area oldPlaneArea=planeArea;
		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOVE|verbalCastCode(mob,target,auto),castingMessage(mob, auto));
		if((mob.location().okMessage(mob,msg))
		&&(target.okMessage(mob,msg)))
		{
			mob.location().send(mob,msg);
			target=(Room)msg.target();
			planeArea = ((Room)msg.target()).getArea();

			final List<MOB> h=properTargetList(mob,givenTarget,false);
			if(h==null)
				return false;

			this.lastCasting=System.currentTimeMillis();
			this.planeArea = planeArea;
			final PlanarAbility A;
			if((planeArea!=oldPlaneArea)
			&&(planeArea.fetchEffect(ID())!=null))
			{
				oldPlaneArea.destroy();
				CMLib.map().delArea(oldPlaneArea);
				A=(PlanarAbility)planeArea.fetchEffect(ID());
			}
			else
			{
				A=(PlanarAbility)this.beneficialAffect(mob, planeArea, asLevel, 0);
				if(A!=null)
				{
					A.setHardBumpLevel(hardBumpLevel);
					A.setMiscText(planeName);
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
			planeArea.addBlurbFlag("PLANEOFEXISTENCE {"+planeName+"}");
		}
		// return whether it worked
		return success;
	}
}
