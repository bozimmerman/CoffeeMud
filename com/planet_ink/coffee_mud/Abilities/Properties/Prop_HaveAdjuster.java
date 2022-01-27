package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2022 Bo Zimmerman

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
public class Prop_HaveAdjuster extends Property implements TriggeredAffect
{
	@Override
	public String ID()
	{
		return "Prop_HaveAdjuster";
	}

	@Override
	public String name()
	{
		return "Adjustments to stats when owned";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public boolean bubbleAffect()
	{
		return true;
	}

	protected static class ItemSetDef
	{
		protected String			name			= "";
		protected int				allSetReqNum	= 2;
		protected volatile boolean	setChecked		= false;
		protected volatile boolean	setActivated	= false;
	}

	protected Object[]		charStatsChanges	= null;
	protected Object[]		charStateChanges	= null;
	protected Object[]		phyStatsChanges		= null;
	protected CompiledZMask	mask				= null;
	protected boolean		multiplyPhyStats	= false;
	protected boolean		multiplyCharStates	= false;
	protected boolean		firstTime			= false;
	protected String[]		parameters			= new String[] { "", "" };
	protected ItemSetDef	allSet				= null;

	private static String[]	allParms			= null;

	@Override
	public long flags()
	{
		return Ability.FLAG_ADJUSTER;
	}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_GET;
	}

	public boolean addIfPlussed(final Map<String,String> ps, final String newText, final String parm, final int parmCode, final ArrayList<Object> addTo, final List<String> errors)
	{
		if(ps.containsKey(parm))
		{
			final String val=ps.get(parm);
			if(val.startsWith("+")
			&&(CMath.isNumber(val.substring(1))))
			{
				addTo.add(Integer.valueOf(parmCode));
				addTo.add(Integer.valueOf(CMath.s_int(val.substring(1))));
				return true;
			}
			else
			if(val.startsWith("-")
			&&(CMath.isNumber(val.substring(1))))
			{
				addTo.add(Integer.valueOf(parmCode));
				addTo.add(Integer.valueOf(CMath.s_int(val)));
				return true;
			}
			else
			if(val.startsWith("+")
			&&(CMath.isMathExpression(val.substring(1))))
			{
				addTo.add(Integer.valueOf(parmCode));
				addTo.add(Integer.valueOf(CMath.s_parseIntExpression(val.substring(1))));
				return true;
			}
			else
			if(val.startsWith("-")
			&&(CMath.isMathExpression(val.substring(1))))
			{
				addTo.add(Integer.valueOf(parmCode));
				addTo.add(Integer.valueOf(CMath.s_parseIntExpression(val)));
				return true;
			}
			else
			if(val.startsWith("+'")
			&& val.endsWith("'")
			&&(CMath.isMathExpression(val.substring(2,val.length()-1))))
			{
				addTo.add(Integer.valueOf(parmCode));
				addTo.add(Integer.valueOf(CMath.s_parseIntExpression(val.substring(2,val.length()-1))));
				return true;
			}
			else
			if(val.startsWith("-'")
			&& val.endsWith("'")
			&&(CMath.isMathExpression("-"+val.substring(2,val.length()-1))))
			{
				addTo.add(Integer.valueOf(parmCode));
				addTo.add(Integer.valueOf(CMath.s_parseIntExpression("-"+val.substring(2,val.length()-1))));
				return true;
			}
			else
			if(errors!=null)
				errors.add("Argument "+parm+": expected +-, got '"+val+"' in '"+newText+"'");
		}
		return false;
	}

	public Object[] makeObjectArray(final ArrayList<? extends Object> V)
	{
		if(V==null)
			return null;
		if(V.size()==0)
			return null;
		final Object[] O=new Object[V.size()];
		for(int i=0;i<V.size();i++)
			O[i]=V.get(i);
		return O;
	}

	private final Boolean getParmBool(final Map<String,String> ps, final String newText, final String parm, final boolean def, final List<String> errors)
	{
		if(ps.containsKey(parm))
		{
			final String val=ps.get(parm);
			if(CMath.isBool(val))
				return Boolean.valueOf(CMath.s_bool(val));
			else
			if(errors!=null)
				errors.add("Argument "+parm+": expected boolean, got '"+val+"' in '"+newText+"'");
		}
		return Boolean.valueOf(def);
	}

	private final void addAbleAdjustments(final Map<String,String> ps, final String newText, final String parm, final String prefix, final List<Object> charStatsV, final List<String> errors)
	{
		final String ableProfs=this.getParmStr(ps, parameters[0], parm, "");
		if(ableProfs.length()>0)
		{
			final int size=charStatsV.size();
			final List<String> parts=CMParms.parseCommas(ableProfs, true);
			for(final String s : parts)
			{
				if(s.endsWith(")"))
				{
					final int x=s.indexOf('(');
					if(x>0)
					{
						final String ableID = s.substring(0,x);
						String amts=s.substring(x+1, s.length()-1).trim();
						if(amts.startsWith("+"))
							amts=amts.substring(1);
						if(CMath.isInteger(amts))
							charStatsV.add(new Pair<String,Integer>(prefix+ableID.toUpperCase(),Integer.valueOf(CMath.s_int(amts))));
					}
				}
			}
			if(charStatsV.size()-parts.size()!=size)
				errors.add("Unable to properly parse all "+parm+": "+ableProfs);
		}
	}

	private final double getParmDoublePlus(final Map<String,String> ps, final String newText, final String parm, final double def, final List<String> errors)
	{
		if(ps.containsKey(parm))
		{
			String val=ps.get(parm);
			if(val.startsWith("+")
			&&(CMath.isNumber(val.substring(1))))
				val=val.substring(1);
			else
			if(val.startsWith("-")
			&&(CMath.isNumber(val.substring(1))))
			{}
			else
			{
				if(errors!=null)
					errors.add("Argument "+parm+": expected +-, got '"+val+"' in '"+newText+"'");
				return def;
			}
			return CMath.s_double(val);
		}
		return def;
	}

	private final String getParmStr(final Map<String,String> ps, final String newText, final String parm, final String def)
	{
		if(ps.containsKey(parm))
			return ps.get(parm);
		return def;
	}

	private final void getComplexPhyTerm(final Map<String,String> ps, final String parm, final int parmCode, final String[] parmCodeStr, final ArrayList<Object> addTo, final List<String> errors)
	{
		if(ps.containsKey(parm))
		{
			String senStr=ps.get(parm).toUpperCase();
			if((senStr.startsWith("+")||senStr.startsWith("-"))
			&&(CMath.isNumber(senStr.substring(1))))
				addIfPlussed(ps,parameters[0],parm,parmCode,addTo,errors);
			else
			{
				int multiply=1;
				if(senStr.startsWith("+"))
					senStr=senStr.substring(1);
				else
				if(senStr.startsWith("-"))
				{
					multiply=-1;
					senStr=senStr.substring(1);
				}
				boolean found=false;
				for(int chc=0;chc<parmCodeStr.length;chc++)
				{
					if(parmCodeStr[chc].indexOf(senStr)>=0)
					{
						found=true;
						addTo.add(Integer.valueOf(parmCode));
						addTo.add(Integer.valueOf(multiply*((int)Math.pow(2,chc))));
						break;
					}
				}
				if(!found)
					errors.add("Illegal "+parm+" term: "+senStr);
			}
		}
	}

	@Override
	public void setMiscText(final String newText)
	{
		super.setMiscText(newText);
		this.charStateChanges=null;
		this.phyStatsChanges=null;
		this.charStatsChanges=null;
		this.multiplyPhyStats = false;
		this.multiplyCharStates = false;
		parameters=CMLib.masking().separateMaskStrs(text());
		if(parameters[1].trim().length()==0)
			mask=CMLib.masking().createEmptyMask();
		else
			mask=CMLib.masking().getPreCompiledMask(parameters[1]);

		if(allParms == null)
		{
			final List<String> tempV=new ArrayList<String>();
			tempV.addAll(Arrays.asList(new String[] {
				"MULTIPLYPH", "MULTIPLYCH", "abi", "arm", "att", "dam", "dis", "lev",
				"rej", "sen", "spe", "wei", "hei", "gen", "cla", "cls", "rac", "hit",
				"hun", "man", "mov", "thi", "ALLSAVES", "ABLEPROFS", "ABLELVLS","chr","hp",
				"ALLSET"
			}));
			for(final int i : CharStats.CODES.BASECODES())
			{
				final String name = CMStrings.limit(CharStats.CODES.NAME(i).toLowerCase(),3);
				tempV.add(name);
				tempV.add("max"+name);
			}
			for(final int c : CharStats.CODES.SAVING_THROWS())
				tempV.add("save"+CMStrings.limit(CharStats.CODES.NAME(c).toLowerCase(),3));
			for(int c = CharStats.STAT_SAVE_DOUBT; c<CharStats.CODES.TOTAL();c++)
				tempV.add(CharStats.CODES.NAME(c).toLowerCase());
			allParms = tempV.toArray(new String[tempV.size()]);
		}
		final List<String> errors = new LinkedList<String>();
		final Map<String,String> ps = CMParms.parseLooseParms(parameters[0], allParms, errors);
		if(parameters[0].startsWith("+")
		|| parameters[0].startsWith("-"))
			errors.add("Likely bad arguments: "+parameters[0]);

		final String allSet=getParmStr(ps, parameters[0], "ALLSET", null);
		this.allSet=null;
		if((allSet!=null) && (allSet.length()>0))
		{
			this.allSet=new ItemSetDef();
			final int x=allSet.lastIndexOf('-');
			if((x>0)
			&&(CMath.isInteger(allSet.substring(x+1).trim())))
			{
				this.allSet.allSetReqNum=CMath.s_int(allSet.substring(x+1).trim());
				this.allSet.name=allSet.substring(0,x).trim();
			}
			else
				this.allSet.name=allSet;
		}
		multiplyPhyStats = getParmBool(ps, parameters[0],"MULTIPLYPH",false,errors).booleanValue();
		multiplyCharStates = getParmBool(ps, parameters[0],"MULTIPLYCH",false,errors).booleanValue();

		final ArrayList<Object> phyStatsV=new ArrayList<Object>();
		addIfPlussed(ps,parameters[0],"abi",PhyStats.STAT_ABILITY,phyStatsV,errors);
		addIfPlussed(ps,parameters[0],"arm",PhyStats.STAT_ARMOR,phyStatsV,errors);
		addIfPlussed(ps,parameters[0],"att",PhyStats.STAT_ATTACK,phyStatsV,errors);
		addIfPlussed(ps,parameters[0],"dam",PhyStats.STAT_DAMAGE,phyStatsV,errors);
		getComplexPhyTerm(ps, "dis", PhyStats.STAT_DISPOSITION, PhyStats.IS_CODES, phyStatsV, errors);
		addIfPlussed(ps,parameters[0],"lev",PhyStats.STAT_LEVEL,phyStatsV,errors);
		addIfPlussed(ps,parameters[0],"rej",PhyStats.STAT_REJUV,phyStatsV,errors);
		getComplexPhyTerm(ps, "sen", PhyStats.STAT_SENSES, PhyStats.CAN_SEE_CODES, phyStatsV, errors);
		final double dval=getParmDoublePlus(ps,parameters[0],"spe",0,errors);
		if(dval!=0)
		{
			phyStatsV.add(Integer.valueOf(PhyStats.NUM_STATS));
			phyStatsV.add(Double.valueOf(dval));
		}
		addIfPlussed(ps,parameters[0],"wei",PhyStats.STAT_WEIGHT,phyStatsV,errors);
		addIfPlussed(ps,parameters[0],"hei",PhyStats.STAT_HEIGHT,phyStatsV,errors);

		final ArrayList<Object> charStatsV=new ArrayList<Object>();
		String val=getParmStr(ps,parameters[0],"gen","").toUpperCase();
		if((val.length()>0)
		&&((val.charAt(0)=='M')||(val.charAt(0)=='F')||(val.charAt(0)=='N')))
		{
			charStatsV.add(new Character('G'));
			charStatsV.add(new Character(val.charAt(0)));
		}
		val=getParmStr(ps,parameters[0],"cla","").toUpperCase();
		if(val.length()>0)
		{
			final CharClass C=CMClass.findCharClass(val);
			if((C!=null)&&(C.availabilityCode()!=0))
			{
				charStatsV.add(new Character('C'));
				charStatsV.add(C);
			}
		}
		val=getParmStr(ps,parameters[0],"cls","").toUpperCase();
		if(val.length()>0)
		{
			charStatsV.add(new Character('S'));
			charStatsV.add(Integer.valueOf(CMath.s_int(val)));
		}
		val=getParmStr(ps,parameters[0],"rac","").toUpperCase();
		if((val.length()>0)&&(CMClass.getRace(val)!=null))
		{
			charStatsV.add(new Character('R'));
			charStatsV.add(CMClass.getRace(val));
		}
		for(final int i : CharStats.CODES.BASECODES())
		{
			final String name = CMStrings.limit(CharStats.CODES.NAME(i).toLowerCase(),3);
			addIfPlussed(ps,parameters[0],name,i,charStatsV,errors);
			addIfPlussed(ps,parameters[0],"max"+name,CharStats.CODES.toMAXBASE(i),charStatsV,errors);
		}
		final int[] CMMSGMAP=CharStats.CODES.CMMSGMAP();
		for(final int c : CharStats.CODES.SAVING_THROWS())
			addIfPlussed(ps,parameters[0],"save"+CMStrings.limit(CharStats.CODES.NAME(c).toLowerCase(),3),c,charStatsV,errors);
		for(int c = CharStats.STAT_SAVE_DOUBT; c<CharStats.CODES.TOTAL();c++)
			addIfPlussed(ps,parameters[0],CharStats.CODES.NAME(c).toLowerCase(),c,charStatsV,errors);

		final ArrayList<Object> charStateV=new ArrayList<Object>();
		addIfPlussed(ps,parameters[0],"hit",CharState.STAT_HITPOINTS,charStateV,errors);
		addIfPlussed(ps,parameters[0],"hp",CharState.STAT_HITPOINTS,charStateV,errors);
		addIfPlussed(ps,parameters[0],"hun",CharState.STAT_HUNGER,charStateV,errors);
		addIfPlussed(ps,parameters[0],"man",CharState.STAT_MANA,charStateV,errors);
		addIfPlussed(ps,parameters[0],"mov",CharState.STAT_MOVE,charStateV,errors);
		addIfPlussed(ps,parameters[0],"thi",CharState.STAT_THIRST,charStateV,errors);

		final double allSavesPlus=getParmDoublePlus(ps,newText,"ALLSAVES",0,errors);
		if(allSavesPlus!=0)
		{
			final int allSavesPlusInt = (int)Math.round(allSavesPlus);
			for(final int c : CharStats.CODES.SAVING_THROWS())
			{
				if(CMMSGMAP[c]!=-1)
				{
					charStatsV.add(Integer.valueOf(c));
					charStatsV.add(Integer.valueOf(allSavesPlusInt));
				}
			}
		}
		this.addAbleAdjustments(ps, parameters[0], "ABLEPROFS", "PROF+", charStatsV, errors);
		this.addAbleAdjustments(ps, parameters[0], "ABLELVLS", "LEVEL+", charStatsV, errors);
		addIfPlussed(ps,parameters[0],"chr",CharStats.STAT_CHARISMA,charStatsV,errors);
		this.charStateChanges=makeObjectArray(charStateV);
		this.phyStatsChanges=makeObjectArray(phyStatsV);
		this.charStatsChanges=makeObjectArray(charStatsV);
		if(errors.size()>0)
		{
			final Prop_HaveAdjuster meSave=this;
			CMLib.threads().scheduleRunnable(new Runnable()
			{
				final Prop_HaveAdjuster me=meSave;
				final List<String> errs = new LinkedList<String>(errors);
				@Override
				public void run()
				{
					if(me.affected != null)
						Log.errOut(ID(),"Following errors found on adjuster on "+me.affected.Name()+"@"+CMLib.map().getApproximateExtendedRoomID(CMLib.map().roomLocation(me.affected))+": ");
					for(final String err: errs)
						Log.errOut(ID(),err);
				}

			}, 500);
		}
	}

	public void phyStuff(final Object[] changes, final PhyStats phyStats)
	{
		if(changes==null)
			return;
		if(multiplyPhyStats)
		{
			for(int c=0;c<changes.length;c+=2)
			{
				switch(((Integer)changes[c]).intValue())
				{
				case PhyStats.STAT_ABILITY:
					phyStats.setAbility(phyStats.ability() + ((Integer) changes[c + 1]).intValue());
					break;
				case PhyStats.STAT_ARMOR:
				{
					final int baseAmt=100 - phyStats.armor();
					phyStats.setArmor(100 - (int)Math.round(CMath.mul(baseAmt, CMath.div(((Integer) changes[c + 1]).intValue(),100))));
					break;
				}
				case PhyStats.STAT_ATTACK:
					phyStats.setAttackAdjustment((int)Math.round(CMath.mul(phyStats.attackAdjustment(), CMath.div(((Integer) changes[c + 1]).intValue(),100))));
					break;
				case PhyStats.STAT_DAMAGE:
					phyStats.setDamage((int)Math.round(CMath.mul(phyStats.damage(), CMath.div(((Integer) changes[c + 1]).intValue(),100))));
					break;
				case PhyStats.STAT_DISPOSITION:
				{
					final int val=((Integer) changes[c + 1]).intValue();
					if(val > 0)
						phyStats.setDisposition(phyStats.disposition() | val);
					else
					if(val < 0)
						phyStats.setDisposition(phyStats.disposition() & ~(-val));
					break;
				}
				case PhyStats.STAT_LEVEL:
				{
					phyStats.setLevel(phyStats.level() + ((Integer) changes[c + 1]).intValue());
					if (phyStats.level() < 0)
						phyStats.setLevel(0);
					break;
				}
				case PhyStats.STAT_REJUV:
					phyStats.setRejuv(phyStats.rejuv() + ((Integer) changes[c + 1]).intValue());
					break;
				case PhyStats.STAT_SENSES:
					{
						final int val=((Integer) changes[c + 1]).intValue();
						if(val > 0)
							phyStats.setSensesMask(phyStats.sensesMask() | val);
						else
						if(val < 0)
							phyStats.setSensesMask(phyStats.sensesMask() & ~(-val));
					}
					break;
				case PhyStats.STAT_WEIGHT:
					phyStats.setWeight((int)Math.round(CMath.mul(phyStats.weight(), CMath.div(((Integer) changes[c + 1]).intValue(),100))));
					break;
				case PhyStats.STAT_HEIGHT:
					phyStats.setHeight((int)Math.round(CMath.mul(phyStats.height(), CMath.div(((Integer) changes[c + 1]).intValue(),100))));
					break;
				case PhyStats.NUM_STATS:
					phyStats.setSpeed(phyStats.speed() * ((Double) changes[c + 1]).doubleValue());
					break;
				}
			}
		}
		else
		{
			for(int c=0;c<changes.length;c+=2)
			{
				switch(((Integer)changes[c]).intValue())
				{
				case PhyStats.STAT_ABILITY:
					phyStats.setAbility(phyStats.ability() + ((Integer) changes[c + 1]).intValue());
					break;
				case PhyStats.STAT_ARMOR:
					phyStats.setArmor(phyStats.armor() + ((Integer) changes[c + 1]).intValue());
					break;
				case PhyStats.STAT_ATTACK:
					phyStats.setAttackAdjustment(phyStats.attackAdjustment() + ((Integer) changes[c + 1]).intValue());
					break;
				case PhyStats.STAT_DAMAGE:
					phyStats.setDamage(phyStats.damage() + ((Integer) changes[c + 1]).intValue());
					break;
				case PhyStats.STAT_DISPOSITION:
					{
						final int val=((Integer) changes[c + 1]).intValue();
						if(val > 0)
							phyStats.setDisposition(phyStats.disposition() | val);
						else
						if(val < 0)
							phyStats.setDisposition(phyStats.disposition() & ~(-val));
					}
					break;
				case PhyStats.STAT_LEVEL:
				{
					phyStats.setLevel(phyStats.level() + ((Integer) changes[c + 1]).intValue());
					if (phyStats.level() < 0)
						phyStats.setLevel(0);
					break;
				}
				case PhyStats.STAT_REJUV:
					phyStats.setRejuv(phyStats.rejuv() + ((Integer) changes[c + 1]).intValue());
					break;
				case PhyStats.STAT_SENSES:
					{
						final int val=((Integer) changes[c + 1]).intValue();
						if(val > 0)
							phyStats.setSensesMask(phyStats.sensesMask() | val);
						else
						if(val < 0)
							phyStats.setSensesMask(phyStats.sensesMask() & ~(-val));
					}
					break;
				case PhyStats.STAT_WEIGHT:
					phyStats.setWeight(phyStats.weight() + ((Integer) changes[c + 1]).intValue());
					break;
				case PhyStats.STAT_HEIGHT:
					phyStats.setHeight(phyStats.height() + ((Integer) changes[c + 1]).intValue());
					break;
				case PhyStats.NUM_STATS:
					phyStats.setSpeed(phyStats.speed() + (((Double) changes[c + 1]).doubleValue()));
					break;
				}
			}
		}
	}

	protected boolean setItemCheck(final Item I)
	{
		return true;
	}

	protected boolean setCheck(final Item I)
	{
		final ItemSetDef allSet;
		synchronized(this)
		{
			allSet=this.allSet;
		}
		if(allSet==null)
			return true;
		final ItemPossessor P=I.owner();
		if(!(P instanceof MOB))
		{
			allSet.setChecked=false;
			allSet.setActivated=false;
			return false;
		}
		if(allSet.setChecked)
			return allSet.setActivated;
		final MOB mob=(MOB)P;
		int ct=0;
		final List<ItemSetDef> allDefs=new LinkedList<ItemSetDef>();
		for(int i=0;i<mob.numItems();i++)
		{
			final Item I2=mob.getItem(i);
			if((I2!=null) && (I2.numEffects()>0))
			{
				final Prop_HaveAdjuster hA=(Prop_HaveAdjuster)I2.fetchEffect(ID());
				if((hA!=null)
				&&(hA.allSet!=null)
				&&(hA.allSet.name.equalsIgnoreCase(allSet.name))) // just having it is enough
				{
					if(setItemCheck(I2))
						ct++;
					allDefs.add(hA.allSet);
				}
			}
		}
		final boolean setActivated=ct >= allSet.allSetReqNum;
		for(final ItemSetDef def : allDefs)
		{
			def.setChecked=true;
			def.setActivated=setActivated;
		}
		return setActivated;
	}

	protected void clearSet(final MOB mob, final ItemSetDef allSet)
	{
		if(allSet!=null)
		{
			synchronized(this)
			{
				allSet.setActivated=false;
				allSet.setChecked=false;
			}
			if(mob != null)
			{
				for(int i=0;i<mob.numItems();i++)
				{
					final Item I=mob.getItem(i);
					if((I!=affected)
					&&(I!=null)
					&&(I.numEffects()>0))
					{
						final Prop_HaveAdjuster hA=(Prop_HaveAdjuster)I.fetchEffect(ID());
						if((hA!=null)
						&&(hA.allSet!=null)
						&&(hA.allSet.name.equalsIgnoreCase(allSet.name)))
						{
							synchronized(hA)
							{
								hA.allSet.setActivated=false;
								hA.allSet.setChecked=false;
							}
						}
					}
				}
			}
		}
	}

	public boolean canApply(final MOB mob)
	{
		if((affected instanceof Item)
		&&(!((Item)affected).amDestroyed())
		&&((mask==null)||(CMLib.masking().maskCheck(mask,mob,true)))
		&&(setCheck((Item)affected)))
			return true;
		return false;
	}

	public boolean canApply(final Environmental E)
	{
		if(E instanceof MOB)
			return canApply((MOB)E);
		return false;
	}

	protected void ensureStarted()
	{
		if(mask==null)
			setMiscText(text());
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((msg.target() == affected)
		&&(affected instanceof Item)
		&&(msg.source()==((Item)affected).owner())
		&&(this.allSet!=null)
		&&(ID().equals("Prop_HaveAdjuster")))
		{
			if((msg.targetMinor()==CMMsg.TYP_DROP)
			||(msg.sourceMinor()==CMMsg.TYP_GET)
			||(msg.sourceMinor()==CMMsg.TYP_GIVE))
				clearSet(msg.source(),this.allSet);
		}
	}

	@Override
	public void affectPhyStats(final Physical host, final PhyStats affectableStats)
	{
		ensureStarted();
		if(canApply(host))
			phyStuff(phyStatsChanges,affectableStats);
		super.affectPhyStats(host,affectableStats);
	}

	public void adjCharStats(final MOB mob, final Object[] changes, final CharStats charStats)
	{
		if(changes==null)
			return;
		for(int i=0;i<changes.length;i+=2)
		{
			if(changes[i] instanceof Integer)
				charStats.setStat(((Integer)changes[i]).intValue(),charStats.getStat(((Integer)changes[i]).intValue())+((Integer)changes[i+1]).intValue());
			else
			if(changes[i] instanceof Character)
			{
				switch(((Character)changes[i]).charValue())
				{
				case 'G':
					charStats.setStat(CharStats.STAT_GENDER, ((Character) changes[i + 1]).charValue());
					break;
				case 'C':
					charStats.setCurrentClass((CharClass) changes[i + 1]);
					break;
				case 'S':
					if(mob.baseCharStats().getCurrentClass()!=charStats.getCurrentClass())
					{
						mob.baseCharStats().setCurrentClass(charStats.getCurrentClass());
						mob.baseCharStats().setCurrentClassLevel(mob.phyStats().level()-((Integer)changes[i + 1]).intValue());
					}
					charStats.setCurrentClassLevel(mob.phyStats().level()-((Integer)changes[i + 1]).intValue());
					break;
				case 'R':
					charStats.setMyRace((Race) changes[i + 1]);
					charStats.setWearableRestrictionsBitmap(charStats.getWearableRestrictionsBitmap()|charStats.getMyRace().forbiddenWornBits());
					break;
				}
			}
			else
			if(changes[i] instanceof Pair)
			{
				@SuppressWarnings("unchecked")
				final Pair<String,Integer> p = (Pair<String,Integer>)changes[i];
				charStats.adjustAbilityAdjustment(p.first, p.second.intValue());
			}
		}
	}

	public void adjCharState(final MOB mob, final Object[] changes, final CharState charState)
	{
		if(changes==null)
			return;
		if(multiplyCharStates)
		{
			for(int c=0;c<changes.length;c+=2)
			{
				switch(((Integer)changes[c]).intValue())
				{
				case CharState.STAT_HITPOINTS:
					charState.setHitPoints((int)Math.round(CMath.mul(charState.getHitPoints(), CMath.div(((Integer) changes[c + 1]).intValue(),100))));
					break;
				case CharState.STAT_HUNGER:
					charState.setHunger(charState.getHunger() + ((Integer) changes[c + 1]).intValue());
					break;
				case CharState.STAT_THIRST:
					charState.setThirst(charState.getThirst() + ((Integer) changes[c + 1]).intValue());
					break;
				case CharState.STAT_MANA:
					charState.setMana((int)Math.round(CMath.mul(charState.getMana(), CMath.div(((Integer) changes[c + 1]).intValue(),100))));
					break;
				case CharState.STAT_MOVE:
					charState.setMovement((int)Math.round(CMath.mul(charState.getMovement(), CMath.div(((Integer) changes[c + 1]).intValue(),100))));
					break;
				}
			}
		}
		else
		{
			for(int c=0;c<changes.length;c+=2)
			{
				switch(((Integer)changes[c]).intValue())
				{
				case CharState.STAT_HITPOINTS:
					charState.setHitPoints(charState.getHitPoints() + ((Integer) changes[c + 1]).intValue());
					break;
				case CharState.STAT_HUNGER:
					charState.setHunger(charState.getHunger() + ((Integer) changes[c + 1]).intValue());
					break;
				case CharState.STAT_THIRST:
					charState.setThirst(charState.getThirst() + ((Integer) changes[c + 1]).intValue());
					break;
				case CharState.STAT_MANA:
					charState.setMana(charState.getMana() + ((Integer) changes[c + 1]).intValue());
					break;
				case CharState.STAT_MOVE:
					charState.setMovement(charState.getMovement() + ((Integer) changes[c + 1]).intValue());
					break;
				}
			}
		}
		if(firstTime)
		{
			firstTime=false;
			charState.copyInto(mob.curState());
		}
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectedStats)
	{
		ensureStarted();
		if(canApply(affectedMOB))
			adjCharStats(affectedMOB, charStatsChanges,affectedStats);
		super.affectCharStats(affectedMOB,affectedStats);
	}

	@Override
	public void affectCharState(final MOB affectedMOB, final CharState affectedState)
	{
		ensureStarted();
		if(canApply(affectedMOB))
			adjCharState(affectedMOB,charStateChanges,affectedState);
		super.affectCharState(affectedMOB,affectedState);
	}

	public final String fixAccoutingsWithMask(String parameters, final String mask)
	{
		if(allSet != null)
			parameters = CMParms.delParmStr(parameters, "ALLSET");
		int x=parameters.toUpperCase().indexOf("ARM");
		for(final StringBuffer ID=new StringBuffer(parameters);((x>0)&&(x<parameters.length()));x++)
		{
			if(parameters.charAt(x)=='-')
			{
				ID.setCharAt(x,'+');
				parameters=ID.toString();
				break;
			}
			else
			if(parameters.charAt(x)=='+')
			{
				ID.setCharAt(x,'-');
				parameters=ID.toString();
				break;
			}
			else
			if(Character.isDigit(parameters.charAt(x)))
				break;
		}
		x=parameters.toUpperCase().indexOf("DIS");
		if(x>=0)
		{
			final long val=CMParms.getParmPlus(parameters,"dis");
			final int y=parameters.indexOf(""+val,x);
			if((val!=0)&&(y>x))
			{
				final StringBuffer middle=new StringBuffer("");
				for(int num=0;num<PhyStats.IS_VERBS.length;num++)
				{
					if(CMath.bset(val,CMath.pow(2,num)))
						middle.append(PhyStats.IS_VERBS[num]+" ");
				}
				parameters=parameters.substring(0,x)+middle.toString().trim()+parameters.substring(y+((""+val).length()));
			}
		}
		x=parameters.toUpperCase().indexOf("SEN");
		if(x>=0)
		{
			final long val=CMParms.getParmPlus(parameters,"sen");
			final int y=parameters.indexOf(""+val,x);
			if((val!=0)&&(y>x))
			{
				final StringBuffer middle=new StringBuffer("");
				for(int num=0;num<PhyStats.CAN_SEE_VERBS.length;num++)
				{
					if(CMath.bset(val,CMath.pow(2,num)))
						middle.append(PhyStats.CAN_SEE_VERBS[num]+" ");
				}
				parameters=parameters.substring(0,x)+middle.toString().trim()+parameters.substring(y+((""+val).length()));
			}
		}
		if(allSet!=null)
			parameters+="  (Requires "+allSet.allSetReqNum+" pieces from the *"+allSet.name.replace('_', ' ')+"* set)";
		if(mask.length()>0)
			parameters+="  Restrictions: "+CMLib.masking().maskDesc(mask);
		return parameters;
	}

	@Override
	public String accountForYourself()
	{
		return fixAccoutingsWithMask("Affects the owner: "+parameters[0],parameters[1]);
	}

	public String getStrStatValue(final Object[] changes, final Integer statCode)
	{
		if(this.phyStatsChanges!=null)
		{
			for(int i=0;i<this.phyStatsChanges.length;i+=2)
			{
				if(this.phyStatsChanges[i].equals(statCode))
					return this.phyStatsChanges[i+1].toString();
			}
		}
		return "0";
	}

	public int getIntStatValue(final Object[] changes, final Integer statCode)
	{
		if(this.phyStatsChanges!=null)
		{
			for(int i=0;i<this.phyStatsChanges.length;i+=2)
			{
				if(this.phyStatsChanges[i].equals(statCode))
					return ((Integer)this.phyStatsChanges[i+1]).intValue();
			}
		}
		return 0;
	}

	@Override
	public String getStat(final String code)
	{
		if(code == null)
			return "";
		if(code.equalsIgnoreCase("STAT-LEVEL"))
		{
			int level = 0;
			Object[] changes = charStateChanges;
			if(changes != null)
			{
				if(multiplyCharStates)
				{
					for(int c=0;c<changes.length;c+=2)
					{
						int amt= ((Integer) changes[c + 1]).intValue();
						if(amt >= 100)
							amt -= 100;
						else
						if(amt < 100)
							amt = -(100-amt);

						switch(((Integer)changes[c]).intValue())
						{
						case CharState.STAT_HITPOINTS:
							level += (amt / 20);
							break;
						case CharState.STAT_HUNGER:
							break;
						case CharState.STAT_THIRST:
							break;
						case CharState.STAT_MANA:
							level += (amt / 20);
							break;
						case CharState.STAT_MOVE:
							level += (amt / 20);
							break;
						}
					}
				}
				else
				{
					for(int c=0;c<changes.length;c+=2)
					{
						switch(((Integer)changes[c]).intValue())
						{
						case CharState.STAT_HITPOINTS:
							level += ( ((Integer) changes[c + 1]).intValue() / 5);
							break;
						case CharState.STAT_HUNGER:
							break;
						case CharState.STAT_THIRST:
							break;
						case CharState.STAT_MANA:
							level += ( ((Integer) changes[c + 1]).intValue() / 5);
							break;
						case CharState.STAT_MOVE:
							level += ( ((Integer) changes[c + 1]).intValue() / 5);
							break;
						}
					}
				}
			}
			changes = charStatsChanges;
			if(changes != null)
			{
				for(int i=0;i<changes.length;i+=2)
				{
					if(changes[i] instanceof Integer)
					{
						if(CharStats.CODES.isBASE(((Integer)changes[i]).intValue()))
							level += (((Integer)changes[i+1]).intValue() * 10);
						else
						if(CMParms.indexOf(CharStats.CODES.MAXCODES(),((Integer)changes[i]).intValue())>=0)
							level += (((Integer)changes[i+1]).intValue() * 90);
						else
						if(CMParms.indexOf(CharStats.CODES.SAVING_THROWS(),((Integer)changes[i]).intValue())>=0)
							level += (((Integer)changes[i+1]).intValue() / 20);
					}
					else
					if(changes[i] instanceof Character)
					{
						switch(((Character)changes[i]).charValue())
						{
						case 'G':
							break;
						case 'C':
							break;
						case 'S':
							break;
						case 'R':
							break;
						}
					}
				}
			}
			changes = phyStatsChanges;
			if(changes != null)
			{
				if(multiplyPhyStats)
				{
					for(int c=0;c<changes.length;c+=2)
					{
						int amt;
						if(changes[c+1] instanceof Integer)
							amt= ((Integer) changes[c + 1]).intValue();
						else
						if(changes[c+1] instanceof Double)
							amt = (int)Math.round(((Double)changes[c+1]).doubleValue() * 100);
						else
							continue;

						if(amt >= 100)
							amt -= 100;
						else
						if(amt < 100)
							amt = -(100-amt);
						switch(((Integer)changes[c]).intValue())
						{
						case PhyStats.STAT_ABILITY:
							level+= (amt / 20);
							break;
						case PhyStats.STAT_ARMOR:
							level+= (amt / -5);
							break;
						case PhyStats.STAT_ATTACK:
							level+= (amt / 5);
							break;
						case PhyStats.STAT_DAMAGE:
							level+= (amt / 20);
							break;
						case PhyStats.STAT_DISPOSITION:
							level+=10;
							break;
						case PhyStats.STAT_LEVEL:
							level -= (amt / 20);
							break;
						case PhyStats.STAT_REJUV:
							break;
						case PhyStats.STAT_SENSES:
							level+=10;
							break;
						case PhyStats.STAT_WEIGHT:
							break;
						case PhyStats.STAT_HEIGHT:
							break;
						case PhyStats.NUM_STATS:
							level+= 10*(amt / .2);
							break;
						}
					}
				}
				else
				{
					for(int c=0;c<changes.length;c+=2)
					{
						switch(((Integer)changes[c]).intValue())
						{
						case PhyStats.STAT_ABILITY:
							level+= (((Integer) changes[c + 1]).intValue() * 5);
							break;
						case PhyStats.STAT_ARMOR:
							level+= (((Integer) changes[c + 1]).intValue() * -1);
							break;
						case PhyStats.STAT_ATTACK:
							level+= (((Integer) changes[c + 1]).intValue() * 1);
							break;
						case PhyStats.STAT_DAMAGE:
							level+= (((Integer) changes[c + 1]).intValue() * 3);
							break;
						case PhyStats.STAT_DISPOSITION:
							level+=10;
							break;
						case PhyStats.STAT_LEVEL:
							level -= ((Integer) changes[c + 1]).intValue() /3;
							break;
						case PhyStats.STAT_REJUV:
							break;
						case PhyStats.STAT_SENSES:
							level+=10;
							break;
						case PhyStats.STAT_WEIGHT:
							break;
						case PhyStats.STAT_HEIGHT:
							break;
						case PhyStats.NUM_STATS:
							level+= (((Double) changes[c + 1]).intValue() * 100);
							break;
						}
					}
				}
			}
			return ""+level;
		}
		else
		if(code.toUpperCase().startsWith("STAT-"))
		{
			final String subCode=code.substring(5).toUpperCase();
			if(this.multiplyPhyStats)
				return "0";
			else
			{
				if(subCode.startsWith("ATTACK"))
					return getStrStatValue(phyStatsChanges, Integer.valueOf(PhyStats.STAT_ATTACK));
				if(subCode.startsWith("DAMAGE"))
					return getStrStatValue(phyStatsChanges, Integer.valueOf(PhyStats.STAT_DAMAGE));
				if(subCode.startsWith("ARMOR"))
					return getStrStatValue(phyStatsChanges, Integer.valueOf(PhyStats.STAT_ARMOR));
			}
			return "0";
		}
		return super.getStat(code);
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if(code!=null)
		{
			if(code.equalsIgnoreCase("STAT-LEVEL"))
			{

			}
			else
			if(code.equalsIgnoreCase("TONEDOWN"))
			{
				setStat("TONEDOWN-ARMOR",val);
				setStat("TONEDOWN-WEAPON",val);
				setStat("TONEDOWN-MISC",val);
			}
			else
			if(code.equalsIgnoreCase("TONEDOWN-ARMOR"))
			{
				if(CMParms.getParmPlus(text(),"ARM")>0)
				{
					int a=text().toUpperCase().indexOf("ARM");
					if(a>=0)
						a=text().indexOf('-',a+1);
					if(a>0)
					{
						int a2=text().toUpperCase().indexOf(' ',a+1);
						if(a2<0)
							a2=text().length();
						final int num=CMath.s_int(text().substring(a+1,a2));
						int newNum = (int)Math.round(CMath.mul(num,0.9));
						if((newNum == num) && (newNum > 1))
							newNum--;
						if(newNum != 0)
						{
							setMiscText(text().substring(0,a+1)+newNum+text().substring(a2));
						}
					}
				}
			}
			else
			if(code.equalsIgnoreCase("TONEDOWN-WEAPON"))
			{
				final double pct=CMath.s_pct(val);
				if(CMParms.getParmPlus(text(),"DAM")>0)
				{
					int a=text().toUpperCase().indexOf("DAM");
					if(a>=0)
						a=text().indexOf('+',a+1);
					if(a>0)
					{
						int a2=text().toUpperCase().indexOf(' ',a+1);
						if(a2<0)
							a2=text().length();
						final int num=CMath.s_int(text().substring(a+1,a2));
						int newNum = (int)Math.round(CMath.mul(num,pct));
						if((newNum == num) && (newNum > 1))
							newNum--;
						if(newNum != 0)
						{
							setMiscText(text().substring(0,a+1)+newNum+text().substring(a2));
						}
					}
				}
				if(CMParms.getParmPlus(text(),"ATT")>0)
				{
					int a=text().toUpperCase().indexOf("ATT");
					if(a>=0)
						a=text().indexOf('+',a+1);
					if(a>0)
					{
						int a2=text().toUpperCase().indexOf(' ',a+1);
						if(a2<0)
							a2=text().length();
						final int num=CMath.s_int(text().substring(a+1,a2));
						int newNum = (int)Math.round(CMath.mul(num,pct));
						if((newNum == num) && (newNum > 1))
							newNum--;
						if(newNum != 0)
						{
							setMiscText(text().substring(0,a+1)+newNum+text().substring(a2));
						}
					}
				}
			}
			else
			if(code.equalsIgnoreCase("TONEDOWN-MISC"))
			{
				final double pct=CMath.s_pct(val);
				final Physical I=affected;
				String s=text();
				int plusminus=s.indexOf('+');
				int minus=s.indexOf('-');
				if((minus>=0)&&((plusminus<0)||(minus<plusminus)))
					plusminus=minus;
				while(plusminus>=0)
				{
					int spaceafter=s.indexOf(' ',plusminus+1);
					if(spaceafter<0)
						spaceafter=s.length();
					if(spaceafter>plusminus)
					{
						final String number=s.substring(plusminus+1,spaceafter).trim();
						if(CMath.isNumber(number))
						{
							final int num=CMath.s_int(number);
							int spacebefore=s.lastIndexOf(' ',plusminus);
							if(spacebefore<0)
								spacebefore=0;
							if(spacebefore<plusminus)
							{
								boolean proceed=true;
								final String wd=s.substring(spacebefore,plusminus).trim().toUpperCase();
								if(wd.startsWith("DIS"))
									proceed=false;
								else
								if(wd.startsWith("SEN"))
									proceed=false;
								else
								if(wd.startsWith("ARM")&&(I instanceof Armor))
									proceed=false;
								else
								if(wd.startsWith("ATT")&&(I instanceof Weapon))
									proceed=false;
								else
								if(wd.startsWith("DAM")&&(I instanceof Weapon))
									proceed=false;
								else
								if(wd.startsWith("ARM")&&(s.charAt(plusminus)=='+'))
									proceed=false;
								else
								if((!wd.startsWith("ARM"))&&(s.charAt(plusminus)=='-'))
									proceed=false;
								if(proceed)
								{
									if((num!=1)&&(num!=-1))
									{
										int newNum = (int)Math.round(CMath.mul(num,pct));
										if((newNum == num) && (newNum > 1))
											newNum--;
										final String newNumStr=((newNum>=0)?"+":"")+Integer.toString(newNum);
										if(newNum != 0)
											s=s.substring(0,plusminus)+newNumStr+s.substring(spaceafter);
									}
								}
							}
						}
					}
					minus=s.indexOf('-',plusminus+1);
					plusminus=s.indexOf('+',plusminus+1);
					if((minus>=0)&&((plusminus<0)||(minus<plusminus)))
						plusminus=minus;
				}
				setMiscText(s);
			}
			else
			if(code.equalsIgnoreCase("TONEUP"))
			{
				setStat("TONEUP-ARMOR",val);
				setStat("TONEUP-WEAPON",val);
				setStat("TONEUP-MISC",val);
			}
			else
			if(code.equalsIgnoreCase("TONEUP-ARMOR"))
			{
				if(CMParms.getParmPlus(text(),"ARM")!=0)
				{
					int a=text().toUpperCase().indexOf("ARM");
					if(a>=0)
						a=text().indexOf('-',a+1);
					if(a>0)
					{
						int a2=text().toUpperCase().indexOf(' ',a+1);
						if(a2<0)
							a2=text().length();
						final int num=CMath.s_int(text().substring(a+1,a2));
						int newNum = (int)Math.round(CMath.mul(num,1.1));
						if((newNum == num) && (newNum > 1))
							newNum--;
						if(newNum != 0)
						{
							setMiscText(text().substring(0,a+1)+newNum+text().substring(a2));
						}
					}
				}
				else
					setMiscText("ARMOR-5 "+text());
			}
			else
			if(code.equalsIgnoreCase("TONEUP-WEAPON"))
			{
				final double pct=CMath.s_pct(val);
				final boolean doesDamn =CMParms.getParmPlus(text(),"DAM")>0;
				final boolean doesAtt =CMParms.getParmPlus(text(),"DAM")>0;
				if((!doesDamn) && (!doesAtt))
				{
					if(CMLib.dice().rollPercentage()>50)
						setMiscText("ATTACK+5 "+text());
					else
						setMiscText("DAMAGE+1 "+text());
					return;
				}
				if(doesDamn)
				{
					int a=text().toUpperCase().indexOf("DAM");
					if(a>=0)
						a=text().indexOf('+',a+1);
					if(a>0)
					{
						int a2=text().toUpperCase().indexOf(' ',a+1);
						if(a2<0)
							a2=text().length();
						final int num=CMath.s_int(text().substring(a+1,a2));
						int newNum = (int)Math.round(CMath.mul(num,pct));
						if((newNum == num) && (newNum > 1))
							newNum--;
						if(newNum != 0)
						{
							setMiscText(text().substring(0,a+1)+newNum+text().substring(a2));
						}
					}
				}
				if(doesAtt)
				{
					int a=text().toUpperCase().indexOf("ATT");
					if(a>=0)
						a=text().indexOf('+',a+1);
					if(a>0)
					{
						int a2=text().toUpperCase().indexOf(' ',a+1);
						if(a2<0)
							a2=text().length();
						final int num=CMath.s_int(text().substring(a+1,a2));
						int newNum = (int)Math.round(CMath.mul(num,pct));
						if((newNum == num) && (newNum > 1))
							newNum--;
						if(newNum != 0)
						{
							setMiscText(text().substring(0,a+1)+newNum+text().substring(a2));
						}
					}
				}
			}
			else
			if(code.equalsIgnoreCase("TONEUP-MISC"))
			{
				final double pct=CMath.s_pct(val);
				final Physical I=affected;
				String s=text();
				int plusminus=s.indexOf('+');
				int minus=s.indexOf('-');
				if((minus>=0)&&((plusminus<0)||(minus<plusminus)))
					plusminus=minus;
				while(plusminus>=0)
				{
					int spaceafter=s.indexOf(' ',plusminus+1);
					if(spaceafter<0)
						spaceafter=s.length();
					if(spaceafter>plusminus)
					{
						final String number=s.substring(plusminus+1,spaceafter).trim();
						if(CMath.isNumber(number))
						{
							final int num=CMath.s_int(number);
							int spacebefore=s.lastIndexOf(' ',plusminus);
							if(spacebefore<0)
								spacebefore=0;
							if(spacebefore<plusminus)
							{
								final String wd=s.substring(spacebefore,plusminus).trim().toUpperCase();
								if(wd.startsWith("DIS")) {}
								else if(wd.startsWith("SEN"))  {}
								else if(wd.startsWith("ARM")&&(I instanceof Armor))  {}
								else if(wd.startsWith("ATT")&&(I instanceof Weapon))  {}
								else if(wd.startsWith("DAM")&&(I instanceof Weapon))  {}
								else if(wd.startsWith("ARM")&&(s.charAt(plusminus)=='+'))  {}
								else
								if((!wd.startsWith("ARM"))&&(s.charAt(plusminus)=='-'))
								{
									if((num!=1)&&(num!=-1)&&(pct>1))
									{
										int newNum = num + (int)Math.abs(Math.round(CMath.mul(num,pct-1.0)));
										if((newNum == num) && (newNum > 1))
											newNum--;
										final String newNumStr=((newNum>=0)?"+":"")+Integer.toString(newNum);
										if(newNum != 0)
											s=s.substring(0,plusminus)+newNumStr+s.substring(spaceafter);
									}
								}
								else
								{
									if((num!=1)&&(num!=-1))
									{
										int newNum = num + (int)Math.round(CMath.mul(num,pct));
										if((newNum == num) && (newNum > 1))
											newNum--;
										final String newNumStr=((newNum>=0)?"+":"")+Integer.toString(newNum);
										if(newNum != 0)
											s=s.substring(0,plusminus)+newNumStr+s.substring(spaceafter);
									}
								}
							}
						}
					}
					minus=s.indexOf('-',plusminus+1);
					plusminus=s.indexOf('+',plusminus+1);
					if((minus>=0)&&((plusminus<0)||(minus<plusminus)))
						plusminus=minus;
				}
				setMiscText(s);
			}
		}
		super.setStat(code, val);
	}
}
