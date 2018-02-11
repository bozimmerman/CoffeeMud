package com.planet_ink.coffee_mud.Abilities.Properties;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
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

	protected Object[]		charStatsChanges	= null;
	protected Object[]		charStateChanges	= null;
	protected Object[]		phyStatsChanges		= null;
	protected CompiledZMask	mask				= null;
	protected boolean		multiplyPhyStats	= false;
	protected boolean		multiplyCharStates	= false;
	protected boolean		firstTime			= false;
	protected String[]		parameters			= new String[] { "", "" };

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

	public boolean addIfPlussed(String newText, String parm, int parmCode, Vector<Object> addTo)
	{
		final int val=CMParms.getParmPlus(newText,parm);
		if(val==0)
			return false;
		addTo.addElement(Integer.valueOf(parmCode));
		addTo.addElement(Integer.valueOf(val));
		return true;
	}

	public Object[] makeObjectArray(Vector<? extends Object> V)
	{
		if(V==null)
			return null;
		if(V.size()==0)
			return null;
		final Object[] O=new Object[V.size()];
		for(int i=0;i<V.size();i++)
			O[i]=V.elementAt(i);
		return O;
	}

	@Override
	public void setMiscText(String newText)
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
		
		multiplyPhyStats = CMParms.getParmBool(parameters[0],"MULTIPLYPH",false);
		multiplyCharStates = CMParms.getParmBool(parameters[0],"MULTIPLYCH",false);
		
		final Vector<Object> phyStatsV=new Vector<Object>();
		addIfPlussed(parameters[0],"abi",PhyStats.STAT_ABILITY,phyStatsV);
		addIfPlussed(parameters[0],"arm",PhyStats.STAT_ARMOR,phyStatsV);
		addIfPlussed(parameters[0],"att",PhyStats.STAT_ATTACK,phyStatsV);
		addIfPlussed(parameters[0],"dam",PhyStats.STAT_DAMAGE,phyStatsV);
		addIfPlussed(parameters[0],"dis",PhyStats.STAT_DISPOSITION,phyStatsV);
		addIfPlussed(parameters[0],"lev",PhyStats.STAT_LEVEL,phyStatsV);
		addIfPlussed(parameters[0],"rej",PhyStats.STAT_REJUV,phyStatsV);
		addIfPlussed(parameters[0],"sen",PhyStats.STAT_SENSES,phyStatsV);
		final double dval=CMParms.getParmDoublePlus(parameters[0],"spe");
		if(dval!=0)
		{
			phyStatsV.addElement(Integer.valueOf(PhyStats.NUM_STATS));
			phyStatsV.addElement(Double.valueOf(dval));
		}
		addIfPlussed(parameters[0],"wei",PhyStats.STAT_WEIGHT,phyStatsV);
		addIfPlussed(parameters[0],"hei",PhyStats.STAT_HEIGHT,phyStatsV);

		final Vector<Object> charStatsV=new Vector<Object>();
		String val=CMParms.getParmStr(parameters[0],"gen","").toUpperCase();
		if((val.length()>0)&&((val.charAt(0)=='M')||(val.charAt(0)=='F')||(val.charAt(0)=='N')))
		{
			charStatsV.addElement(new Character('G'));
			charStatsV.addElement(new Character(val.charAt(0)));
		}
		val=CMParms.getParmStr(parameters[0],"cla","").toUpperCase();
		if(val.length()>0)
		{
			final CharClass C=CMClass.findCharClass(val);
			if((C!=null)&&(C.availabilityCode()!=0))
			{
				charStatsV.addElement(new Character('C'));
				charStatsV.addElement(C);
			}
		}
		val=CMParms.getParmStr(parameters[0],"cls","").toUpperCase();
		if(val.length()>0)
		{
			charStatsV.addElement(new Character('S'));
			charStatsV.addElement(Integer.valueOf(CMath.s_int(val)));
		}
		val=CMParms.getParmStr(parameters[0],"rac","").toUpperCase();
		if((val.length()>0)&&(CMClass.getRace(val)!=null))
		{
			charStatsV.addElement(new Character('R'));
			charStatsV.addElement(CMClass.getRace(val));
		}
		for(final int i : CharStats.CODES.BASECODES())
		{
			final String name = CMStrings.limit(CharStats.CODES.NAME(i).toLowerCase(),3);
			addIfPlussed(parameters[0],name,i,charStatsV);
			addIfPlussed(parameters[0],"max"+name,CharStats.CODES.toMAXBASE(i),charStatsV);
		}
		final int[] CMMSGMAP=CharStats.CODES.CMMSGMAP();
		for(final int c : CharStats.CODES.SAVING_THROWS())
		{
			addIfPlussed(parameters[0],"save"+CMStrings.limit(CharStats.CODES.NAME(c).toLowerCase(),3),c,charStatsV);
		}
		for(int c = CharStats.STAT_FAITH; c<CharStats.CODES.TOTAL();c++)
			addIfPlussed(parameters[0],CharStats.CODES.NAME(c).toLowerCase(),c,charStatsV);

		final Vector<Object> charStateV=new Vector<Object>();
		addIfPlussed(parameters[0],"hit",CharState.STAT_HITPOINTS,charStateV);
		addIfPlussed(parameters[0],"hun",CharState.STAT_HUNGER,charStateV);
		addIfPlussed(parameters[0],"man",CharState.STAT_MANA,charStateV);
		addIfPlussed(parameters[0],"mov",CharState.STAT_MOVE,charStateV);
		addIfPlussed(parameters[0],"thi",CharState.STAT_THIRST,charStateV);
		
		int allSavesPlus=CMParms.getParmPlus(newText,"ALLSAVES");
		if(allSavesPlus!=0)
		{
			for(final int c : CharStats.CODES.SAVING_THROWS())
			{
				if(CMMSGMAP[c]!=-1)
				{
					charStatsV.addElement(Integer.valueOf(c));
					charStatsV.addElement(Integer.valueOf(allSavesPlus));
				}
			}
		}
		this.charStateChanges=makeObjectArray(charStateV);
		this.phyStatsChanges=makeObjectArray(phyStatsV);
		this.charStatsChanges=makeObjectArray(charStatsV);
	}

	public void phyStuff(Object[] changes, PhyStats phyStats)
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
					phyStats.setDisposition(phyStats.disposition() | ((Integer) changes[c + 1]).intValue());
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
					phyStats.setSensesMask(phyStats.sensesMask() | ((Integer) changes[c + 1]).intValue());
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
					phyStats.setDisposition(phyStats.disposition() | ((Integer) changes[c + 1]).intValue());
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
					phyStats.setSensesMask(phyStats.sensesMask() | ((Integer) changes[c + 1]).intValue());
					break;
				case PhyStats.STAT_WEIGHT:
					phyStats.setWeight(phyStats.weight() + ((Integer) changes[c + 1]).intValue());
					break;
				case PhyStats.STAT_HEIGHT:
					phyStats.setHeight(phyStats.height() + ((Integer) changes[c + 1]).intValue());
					break;
				case PhyStats.NUM_STATS:
					phyStats.setSpeed(phyStats.speed() + ((Double) changes[c + 1]).doubleValue());
					break;
				}
			}
		}
	}

	public boolean canApply(MOB mob)
	{
		if((affected!=null)
		&&(affected instanceof Item)
		&&(!((Item)affected).amDestroyed())
		&&((mask==null)||(CMLib.masking().maskCheck(mask,mob,true))))
			return true;
		return false;
	}

	public boolean canApply(Environmental E)
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
	public void affectPhyStats(Physical host, PhyStats affectableStats)
	{
		ensureStarted();
		if(canApply(host)) 
			phyStuff(phyStatsChanges,affectableStats);
		super.affectPhyStats(host,affectableStats);
	}

	public void adjCharStats(final MOB mob, Object[] changes, CharStats charStats)
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
		}
	}

	public void adjCharState(final MOB mob, Object[] changes, CharState charState)
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
	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		ensureStarted();
		if(canApply(affectedMOB))
			adjCharStats(affectedMOB, charStatsChanges,affectedStats);
		super.affectCharStats(affectedMOB,affectedStats);
	}

	@Override
	public void affectCharState(MOB affectedMOB, CharState affectedState)
	{
		ensureStarted();
		if(canApply(affectedMOB))
			adjCharState(affectedMOB,charStateChanges,affectedState);
		super.affectCharState(affectedMOB,affectedState);
	}

	public static final String fixAccoutingsWithMask(String parameters, final String mask)
	{
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
	public String getStat(String code)
	{
		if(code == null)
			return "";
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
		else
		if(code.equalsIgnoreCase("LEVEL"))
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
							level += (((Integer)changes[i+1]).intValue() * 15);
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
							level+= (amt / 20);
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
							level+= (((Double) changes[c + 1]).intValue() * 5);
							break;
						}
					}
				}
			}
			return ""+level;
		}
		return super.getStat(code);
	}

	@Override
	public void setStat(String code, String val)
	{
		if(code!=null)
		{
			if(code.equalsIgnoreCase("LEVEL"))
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
										if(newNum != 0)
											s=s.substring(0,plusminus+1)+newNum+s.substring(spaceafter);
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
		else
			super.setStat(code, val);
	}
}
