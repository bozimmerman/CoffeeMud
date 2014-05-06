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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Prop_HaveAdjuster extends Property implements TriggeredAffect
{
	@Override public String ID() { return "Prop_HaveAdjuster"; }
	@Override public String name(){ return "Adjustments to stats when owned";}
	@Override protected int canAffectCode(){return Ability.CAN_ITEMS;}
	@Override public boolean bubbleAffect(){return true;}
	protected Object[] charStatsChanges=null;
	protected Object[] charStateChanges=null;
	protected Object[] phyStatsChanges=null;
	protected MaskingLibrary.CompiledZapperMask mask=null;
	protected String[] parameters=new String[]{"",""};

	@Override public long flags(){return Ability.FLAG_ADJUSTER;}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_GET;
	}

	public boolean addIfPlussed(String newText, String parm, int parmCode, Vector addTo)
	{
		final int val=CMParms.getParmPlus(newText,parm);
		if(val==0) return false;
		addTo.addElement(Integer.valueOf(parmCode));
		addTo.addElement(Integer.valueOf(val));
		return true;
	}

	public Object[] makeObjectArray(Vector V)
	{
		if(V==null) return null;
		if(V.size()==0) return null;
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
		parameters=CMLib.masking().separateMaskStrs(text());
		if(parameters[1].trim().length()==0)
			mask=MaskingLibrary.CompiledZapperMask.EMPTY();
		else
			mask=CMLib.masking().getPreCompiledMask(parameters[1]);
		final Vector phyStatsV=new Vector();
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

		final Vector charStatsV=new Vector();
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
			if(CMMSGMAP[c]!=-1)
				addIfPlussed(parameters[0],"save"+CMStrings.limit(CharStats.CODES.NAME(c).toLowerCase(),3),c,charStatsV);

		final Vector charStateV=new Vector();
		addIfPlussed(parameters[0],"hit",CharState.STAT_HITPOINTS,charStateV);
		addIfPlussed(parameters[0],"hun",CharState.STAT_HUNGER,charStateV);
		addIfPlussed(parameters[0],"man",CharState.STAT_MANA,charStateV);
		addIfPlussed(parameters[0],"mov",CharState.STAT_MOVE,charStateV);
		addIfPlussed(parameters[0],"thi",CharState.STAT_THIRST,charStateV);

		this.charStateChanges=makeObjectArray(charStateV);
		this.phyStatsChanges=makeObjectArray(phyStatsV);
		this.charStatsChanges=makeObjectArray(charStatsV);
	}

	public void phyStuff(Object[] changes, PhyStats phyStats)
	{
		if(changes==null) return;
		for(int c=0;c<changes.length;c+=2)
		switch(((Integer)changes[c]).intValue())
		{
		case PhyStats.STAT_ABILITY: phyStats.setAbility(phyStats.ability()+((Integer)changes[c+1]).intValue()); break;
		case PhyStats.STAT_ARMOR: phyStats.setArmor(phyStats.armor()+((Integer)changes[c+1]).intValue()); break;
		case PhyStats.STAT_ATTACK: phyStats.setAttackAdjustment(phyStats.attackAdjustment()+((Integer)changes[c+1]).intValue()); break;
		case PhyStats.STAT_DAMAGE: phyStats.setDamage(phyStats.damage()+((Integer)changes[c+1]).intValue()); break;
		case PhyStats.STAT_DISPOSITION: phyStats.setDisposition(phyStats.disposition()+((Integer)changes[c+1]).intValue()); break;
		case PhyStats.STAT_LEVEL:  {
			phyStats.setLevel(phyStats.level()+((Integer)changes[c+1]).intValue());
			if(phyStats.level()<0) phyStats.setLevel(0);
			break;
		}
		case PhyStats.STAT_REJUV: phyStats.setRejuv(phyStats.rejuv()+((Integer)changes[c+1]).intValue()); break;
		case PhyStats.STAT_SENSES: phyStats.setSensesMask(phyStats.sensesMask()+((Integer)changes[c+1]).intValue()); break;
		case PhyStats.STAT_WEIGHT: phyStats.setWeight(phyStats.weight()+((Integer)changes[c+1]).intValue()); break;
		case PhyStats.STAT_HEIGHT: phyStats.setHeight(phyStats.height()+((Integer)changes[c+1]).intValue()); break;
		case PhyStats.NUM_STATS: phyStats.setSpeed(phyStats.speed()+((Double)changes[c+1]).doubleValue()); break;
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
		if(canApply(host)) phyStuff(phyStatsChanges,affectableStats);
		super.affectPhyStats(host,affectableStats);
	}

	public void adjCharStats(Object[] changes, CharStats charStats)
	{
		if(changes==null) return;
		for(int i=0;i<changes.length;i+=2)
		{
			if(changes[i] instanceof Integer)
				charStats.setStat(((Integer)changes[i]).intValue(),charStats.getStat(((Integer)changes[i]).intValue())+((Integer)changes[i+1]).intValue());
			else
			if(changes[i] instanceof Character)
			switch(((Character)changes[i]).charValue())
			{
			case 'G': charStats.setStat(CharStats.STAT_GENDER,((Character)changes[i+1]).charValue()); break;
			case 'C': charStats.setCurrentClass((CharClass)changes[i+1]); break;
			case 'R': charStats.setMyRace((Race)changes[i+1]); break;
			}
		}
	}

	public void adjCharState(Object[] changes, CharState charState)
	{
		if(changes==null) return;
		for(int c=0;c<changes.length;c+=2)
		switch(((Integer)changes[c]).intValue())
		{
		case CharState.STAT_HITPOINTS: charState.setHitPoints(charState.getHitPoints()+((Integer)changes[c+1]).intValue()); break;
		case CharState.STAT_HUNGER: charState.setHunger(charState.getHunger()+((Integer)changes[c+1]).intValue()); break;
		case CharState.STAT_THIRST: charState.setThirst(charState.getThirst()+((Integer)changes[c+1]).intValue()); break;
		case CharState.STAT_MANA: charState.setMana(charState.getMana()+((Integer)changes[c+1]).intValue()); break;
		case CharState.STAT_MOVE: charState.setMovement(charState.getMovement()+((Integer)changes[c+1]).intValue()); break;
		}
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		ensureStarted();
		if(canApply(affectedMOB)) adjCharStats(charStatsChanges,affectedStats);
		super.affectCharStats(affectedMOB,affectedStats);
	}
	@Override
	public void affectCharState(MOB affectedMOB, CharState affectedState)
	{
		ensureStarted();
		if(canApply(affectedMOB)) adjCharState(charStateChanges,affectedState);
		super.affectCharState(affectedMOB,affectedState);
	}

	public static final String fixAccoutingsWithMask(String parameters, final String mask)
	{
		int x=parameters.toUpperCase().indexOf("ARM");
		for(final StringBuffer ID=new StringBuffer(parameters);((x>0)&&(x<parameters.length()));x++)
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
		x=parameters.toUpperCase().indexOf("DIS");
		if(x>=0)
		{
			final long val=CMParms.getParmPlus(parameters,"dis");
			final int y=parameters.indexOf(""+val,x);
			if((val!=0)&&(y>x))
			{
				final StringBuffer middle=new StringBuffer("");
				for(int num=0;num<PhyStats.IS_VERBS.length;num++)
					if(CMath.bset(val,CMath.pow(2,num)))
						middle.append(PhyStats.IS_VERBS[num]+" ");
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
					if(CMath.bset(val,CMath.pow(2,num)))
						middle.append(PhyStats.CAN_SEE_VERBS[num]+" ");
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
}
