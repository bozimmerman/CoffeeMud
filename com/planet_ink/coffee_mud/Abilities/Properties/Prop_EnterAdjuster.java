package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.AnimalHusbandry;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2023 Bo Zimmerman

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
public class Prop_EnterAdjuster extends Property implements TriggeredAffect
{
	@Override
	public String ID()
	{
		return "Prop_EnterAdjuster";
	}

	@Override
	public String name()
	{
		return "Room entering adjuster";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_EXITS | Ability.CAN_ROOMS;
	}

	protected CompiledZMask	mask		= CMLib.masking().createEmptyMask();
	protected String[]		parameters	= new String[] { "", "" };
	protected List<Ability>	theSpells	= null;

	protected enum EAMisc
	{
		PHYSSTATS,
		CHARSTATS,
		CHARSTATE,
		BASESTATE,
		GENDER,
		CCLASS,
		CRACE,
		PRACS,
		TRAINS,
		QP,
		MONEY,
		EXPE,
		CLEVEL
	}

	protected final List<Triad<EAMisc,Integer,Object>> miscStatChanges = new Vector<Triad<EAMisc,Integer,Object>>(3);

	@Override
	public long flags()
	{
		return Ability.FLAG_ADJUSTER;
	}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_ENTER;
	}

	protected void addPlusChange(final EAMisc type, final int stat, final String code)
	{
		final int x = CMParms.getParmPlus(parameters[0],code);
		if(x != 0)
			miscStatChanges.add(new Triad<EAMisc,Integer,Object>(type,Integer.valueOf(stat),Integer.valueOf(x)));
	}

	protected void addPlusStatChange(final EAMisc type, final String code)
	{
		final int x = CMParms.getParmPlus(parameters[0],code);
		if(x != 0)
			miscStatChanges.add(new Triad<EAMisc,Integer,Object>(type,Integer.valueOf(0),Integer.valueOf(x)));
	}

	protected void justAddPChange(final EAMisc type, final int subType, final Object obj)
	{
		miscStatChanges.add(new Triad<EAMisc,Integer,Object>(type,Integer.valueOf(subType),obj));
	}

	@Override
	public void setMiscText(final String newText)
	{
		super.setMiscText(newText);
		parameters=CMLib.masking().separateMaskStrs(text());
		if(parameters[1].trim().length()==0)
			mask=CMLib.masking().createEmptyMask();
		else
			mask=CMLib.masking().getPreCompiledMask(parameters[1]);
		int del=parameters[0].indexOf(';');
		while(del>=0)
		{
			final String thisOne=parameters[0].substring(0,del);
			if((thisOne.length()>0)&&(!thisOne.equals(";")))
			{
				Ability A=CMClass.getAbility(thisOne);
				if((A!=null)&&(!CMLib.ableMapper().classOnly("Archon",A.ID())))
				{
					A=(Ability)A.copyOf();
					if(theSpells == null)
						theSpells=new Vector<Ability>();
					theSpells.add(A);
				}
			}
			parameters[0]=parameters[0].substring(del+1);
			del=parameters[0].indexOf(';');
		}
		if((parameters[0].length()>0)&&(!parameters[0].equals(";")))
		{
			Ability A=CMClass.getAbility(parameters[0]);
			if(A!=null)
			{
				A=(Ability)A.copyOf();
				if(theSpells == null)
					theSpells=new Vector<Ability>();
				theSpells.add(A);
			}
		}
		miscStatChanges.clear();

		addPlusChange(EAMisc.PHYSSTATS,PhyStats.STAT_ABILITY,"abi");
		addPlusChange(EAMisc.PHYSSTATS,PhyStats.STAT_ARMOR,"arm");
		addPlusChange(EAMisc.PHYSSTATS,PhyStats.STAT_ATTACK,"att");
		addPlusChange(EAMisc.PHYSSTATS,PhyStats.STAT_DAMAGE,"dam");
		addPlusChange(EAMisc.PHYSSTATS,PhyStats.STAT_DISPOSITION,"dis");
		addPlusChange(EAMisc.PHYSSTATS,PhyStats.STAT_LEVEL,"lev");
		addPlusChange(EAMisc.PHYSSTATS,PhyStats.STAT_REJUV,"rej");
		addPlusChange(EAMisc.PHYSSTATS,PhyStats.STAT_SENSES,"sen");
		addPlusChange(EAMisc.PHYSSTATS,PhyStats.NUM_STATS,"spe");
		addPlusChange(EAMisc.PHYSSTATS,PhyStats.STAT_WEIGHT,"wei");
		addPlusChange(EAMisc.PHYSSTATS,PhyStats.STAT_HEIGHT,"hei");

		addPlusChange(EAMisc.CHARSTATS,CharStats.STAT_CHARISMA,"cha");
		addPlusChange(EAMisc.CHARSTATS,CharStats.STAT_CONSTITUTION,"con");
		addPlusChange(EAMisc.CHARSTATS,CharStats.STAT_DEXTERITY,"dex");
		addPlusChange(EAMisc.CHARSTATS,CharStats.STAT_INTELLIGENCE,"int");
		addPlusChange(EAMisc.CHARSTATS,CharStats.STAT_STRENGTH,"str");
		addPlusChange(EAMisc.CHARSTATS,CharStats.STAT_WISDOM,"wis");

		String val=CMParms.getParmStr(parameters[0],"gen","").toUpperCase();
		if((val.length()>0)&&((val.charAt(0)=='M')||(val.charAt(0)=='F')||(val.charAt(0)=='N')))
			justAddPChange(EAMisc.GENDER,0,Character.valueOf(val.charAt(0)));

		val=CMParms.getParmStr(parameters[0],"cla","").toUpperCase();
		if(val.length()>0)
		{
			final CharClass C=CMClass.findCharClass(val);
			if((C!=null)&&(C.availabilityCode()!=0))
				justAddPChange(EAMisc.CCLASS,0,C);
		}
		val=CMParms.getParmStr(parameters[0],"rac","").toUpperCase();
		if((val.length()>0)&&(CMClass.getRace(val)!=null))
			justAddPChange(EAMisc.CRACE,0,CMClass.getRace(val));
		addPlusStatChange(EAMisc.CLEVEL,"clev");

		addPlusChange(EAMisc.BASESTATE, CharState.STAT_HITPOINTS, "hit");
		addPlusChange(EAMisc.CHARSTATE, CharState.STAT_HUNGER, "hun");
		addPlusChange(EAMisc.CHARSTATE, CharState.STAT_MANA, "man");
		addPlusChange(EAMisc.CHARSTATE, CharState.STAT_MOVE, "mov");
		addPlusChange(EAMisc.CHARSTATE, CharState.STAT_THIRST, "thi");

		addPlusStatChange(EAMisc.PRACS,"prac");
		addPlusStatChange(EAMisc.TRAINS,"trai");
		addPlusStatChange(EAMisc.QP,"ques");
		addPlusStatChange(EAMisc.MONEY,"coin");
		addPlusStatChange(EAMisc.EXPE,"expe");
	}

	@Override
	public String accountForYourself()
	{
		String parameters = "Affects those who enter: "+this.parameters[0];
		final String mask = this.parameters[1];
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
		if(theSpells != null)
		{
			for(final Ability A  : theSpells)
				parameters += "  "+A.Name();
		}
		if(mask.length()>0)
			parameters+="  Restrictions: "+CMLib.masking().maskDesc(mask);
		return parameters;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		&&(((msg.targetMinor()==CMMsg.TYP_ENTER)&&((affected instanceof Room)||(affected instanceof Exit)))
		   ||((msg.targetMinor()==CMMsg.TYP_SIT)&&(affected==msg.target())&&(affected instanceof Rideable)))
		&&((mask==null)||(CMLib.masking().maskCheck(mask,msg.source(),true))))
		{
			final MOB mob=msg.source();
			if(theSpells != null)
			{
				for(int i=0;i<theSpells.size();i++)
				{
					final Ability thisOne=(Ability)theSpells.get(i).copyOf();
					thisOne.invoke(mob,mob,true,0);
				}
			}
			for(final Triad<EAMisc,Integer,Object> t : miscStatChanges)
			{
				switch(t.first)
				{
				case BASESTATE:
					mob.baseState().setStat(t.second.intValue(), mob.baseState().getStat(t.second.intValue()) + ((Integer)t.third).intValue());
					break;
				case CCLASS:
					mob.baseCharStats().setCurrentClass((CharClass)t.third);
					break;
				case CHARSTATE:
					mob.curState().setStat(t.second.intValue(), mob.curState().getStat(t.second.intValue()) + ((Integer)t.third).intValue());
					break;
				case CHARSTATS:
					mob.baseCharStats().setStat(t.second.intValue(), mob.baseCharStats().getStat(t.second.intValue()) + ((Integer)t.third).intValue());
					break;
				case CLEVEL:
					mob.baseCharStats().setClassLevel(mob.baseCharStats().getCurrentClass(),
							mob.baseCharStats().getClassLevel(mob.baseCharStats().getCurrentClass())+((Integer)t.third).intValue());
					break;
				case CRACE:
				{
					final int oldCat=mob.baseCharStats().ageCategory();
					mob.baseCharStats().setMyRace((Race)t.third);
					mob.baseCharStats().getMyRace().startRacing(mob,false);
					if(mob.baseCharStats().getStat(CharStats.STAT_AGE)>0)
						mob.baseCharStats().setStat(CharStats.STAT_AGE,mob.baseCharStats().getMyRace().getAgingChart()[oldCat]);
					break;
				}
				case EXPE:
					CMLib.leveler().postExperience(mob,"ABILITY:"+ID(),null,null,((Integer)t.third).intValue(), false);
					break;
				case GENDER:
					mob.baseCharStats().setStat(CharStats.STAT_GENDER,((Character)t.third).charValue());
					break;
				case MONEY:
					CMLib.beanCounter().setMoney(mob,CMLib.beanCounter().getMoney(mob)+((Integer)t.third).intValue());
					break;
				case PHYSSTATS:
					if(t.second.intValue() >= PhyStats.NUM_STATS)
						mob.basePhyStats().setSpeed(mob.basePhyStats().speed() + ((Integer)t.third).intValue());
					else
						mob.basePhyStats().setStat(t.second.intValue(), mob.basePhyStats().getStat(t.second.intValue()) + ((Integer)t.third).intValue());
					break;
				case PRACS:
					mob.setPractices(mob.getPractices()+((Integer)t.third).intValue());
					break;
				case QP:
					mob.setQuestPoint(mob.getQuestPoint()+((Integer)t.third).intValue());
					CMLib.players().bumpPrideStat(mob,PrideStat.QUESTPOINTS_EARNED, ((Integer)t.third).intValue());
					break;
				case TRAINS:
					mob.setTrains(mob.getTrains()+((Integer)t.third).intValue());
					break;
				default:
					break;

				}
			}
			mob.recoverCharStats();
			mob.recoverPhyStats();
			mob.recoverMaxState();
			CMLib.utensils().confirmWearability(mob);
		}
		return super.okMessage(myHost,msg);
	}
}
