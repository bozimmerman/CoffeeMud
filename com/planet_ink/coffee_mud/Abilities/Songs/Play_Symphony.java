package com.planet_ink.coffee_mud.Abilities.Songs;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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

public class Play_Symphony extends Play
{
	@Override
	public String ID()
	{
		return "Play_Symphony";
	}

	private final static String localizedName = CMLib.lang().L("Symphony");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		if(toDoCode<0)
			return Ability.QUALITY_BENEFICIAL_OTHERS;
		else
		if(toDoCode==CODE_DOWNSAVE)
			return Ability.QUALITY_MALICIOUS;
		else
			return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	public final static int CODE_UPSAVE=1;
	public final static int CODE_UPDAMAGEPER3=2;
	public final static int CODE_UPSTAT=3;
	public final static int CODE_DOWNDAMAGEPER5=4;
	public final static int CODE_CASTMALICIOUSSPELLPER10=5;
	public final static int CODE_CASTFRIENDLYSPELLPER2=6;
	public final static int CODE_UPENVPER2=7;
	public final static int CODE_UPENVPER5=8;
	public final static int CODE_REMOVESPELLTYPE=9;
	public final static int CODE_SPEEDCOMMONSKILLS=10;
	public final static int CODE_UPMOVEMENT=11;
	public final static int CODE_CASTFRIENDLYSPELLPER5=12;
	public final static int CODE_CASTFRIENDLYSPELLPER10=13;
	public final static int CODE_CASTFRIENDLYSPELLPER20=14;
	public final static int CODE_DOWNSAVE=15;
	public final static int CODE_UPDAMAGEPER5=16;
	public int toDoCode=-1;
	public String toDoString="";
	public int toDoVal=0;

	public int getToDoCode()
	{
		if(instrument==null)
		{
			toDoCode=-1;
			return toDoCode;
		}
		if(toDoCode>0)
			return toDoCode;
		final int ilvl=instrument.phyStats().level();
		switch(instrument.getInstrumentType())
		{
		case CLARINETS:
		{
			if(ilvl<=2)
			{
				toDoCode=CODE_CASTMALICIOUSSPELLPER10;
				toDoString="Spell_Lightning";
			}
			else
			if(ilvl<=11)
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.STAT_SAVE_ELECTRIC;
			}
			else
			if(ilvl<=15)
			{
				toDoCode=CODE_UPDAMAGEPER3;
				toDoString=""+CMMsg.TYP_ELECTRIC;
			}
			else
			if(ilvl<=18)
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.STAT_SAVE_ELECTRIC;
			}
			else
			if(ilvl<=21)
			{
				toDoCode=CODE_DOWNDAMAGEPER5;
				toDoString=""+CMMsg.TYP_ELECTRIC;
			}
			else
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.STAT_SAVE_ELECTRIC;
			}
			break;
		}
		case CYMBALS:
		{
			if(ilvl<=2)
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.STAT_DEXTERITY;
			}
			else
			if(ilvl<=3)
			{
				toDoCode=CODE_SPEEDCOMMONSKILLS;
			}
			else
			if(ilvl<=4)
			{
				toDoCode=CODE_CASTFRIENDLYSPELLPER10;
				toDoString="Spell_Haste";
			}
			else
			if(ilvl<=9)
			{
				toDoCode=CODE_REMOVESPELLTYPE;
				toDoString=""+Ability.FLAG_CHARMING;
			}
			else
			if(ilvl<=13)
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.STAT_DEXTERITY;
			}
			else
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.STAT_SAVE_POISON;
			}
			break;
		}
		case DRUMS:
		{
			if(ilvl<=3)
			{
				toDoCode=CODE_CASTMALICIOUSSPELLPER10;
				toDoString="Skill_Disarm";
			}
			else
			if(ilvl<=5)
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.STAT_SAVE_JUSTICE;
			}
			else
			if(ilvl<=6)
			{
				toDoCode=CODE_CASTMALICIOUSSPELLPER10;
				toDoString="Spell_Deafness";
			}
			else
			if(ilvl<=7)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.STAT_SAVE_JUSTICE;
			}
			else
			if(ilvl<=8)
			{
				toDoCode=CODE_CASTMALICIOUSSPELLPER10;
				toDoString="Fighter_Whomp";
			}
			else
			if(ilvl<=16)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.STAT_SAVE_JUSTICE;
			}
			else
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.STAT_SAVE_JUSTICE;
			}
			break;
		}
		case FLUTES:
		{
			if(ilvl<=1)
			{
				toDoCode=CODE_CASTMALICIOUSSPELLPER10;
				toDoString="Spell_Fear";
			}
			else
			if(ilvl<=3)
			{
				toDoCode=CODE_CASTMALICIOUSSPELLPER10;
				toDoString="Spell_Confusion";
			}
			else
			if(ilvl<=5)
			{
				toDoCode=CODE_CASTMALICIOUSSPELLPER10;
				toDoString="Spell_Hunger";
			}
			else
			if(ilvl<=6)
			{
				toDoCode=CODE_CASTMALICIOUSSPELLPER10;
				toDoString="Spell_Shrink";
			}
			else
			if(ilvl<=8)
			{
				toDoCode=CODE_CASTMALICIOUSSPELLPER10;
				toDoString="Spell_Charm";
			}
			else
			{
				toDoCode=CODE_CASTMALICIOUSSPELLPER10;
				toDoString="Spell_Thirst";
			}
			break;
		}
		case GUITARS:
		{
			if(ilvl<=4)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.STAT_SAVE_FIRE;
			}
			else
			if(ilvl<=5)
			{
				toDoCode=CODE_UPDAMAGEPER5;
				toDoString=""+CMMsg.TYP_FIRE;
			}
			else
			if(ilvl<=6)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.STAT_SAVE_FIRE;
			}
			else
			if(ilvl<=9)
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.STAT_SAVE_FIRE;
			}
			else
			if(ilvl<=12)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.STAT_SAVE_FIRE;
			}
			else
			if(ilvl<=13)
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.STAT_SAVE_FIRE;
			}
			else
			{
				toDoCode=CODE_UPDAMAGEPER3;
				toDoString=""+CMMsg.TYP_FIRE;
			}
			break;
		}
		case HARMONICAS:
		{
			toDoCode=CODE_UPENVPER5;
			toDoString="D";
			break;
		}
		case HARPS:
		{
			if(ilvl<=3)
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.STAT_CHARISMA;
			}
			else
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.STAT_CHARISMA;
			}
			break;
		}
		case HORNS:
		{
			if(ilvl<=4)
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.STAT_SAVE_ACID;
			}
			else
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.STAT_SAVE_ACID;
			}
			break;
		}
		case OBOES:
		{
			if(ilvl<=4)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.STAT_SAVE_ACID;
			}
			else
			if(ilvl<=7)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.STAT_SAVE_ACID;
			}
			else
			if(ilvl<=9)
			{
				toDoCode=CODE_UPDAMAGEPER3;
				toDoString=""+CMMsg.TYP_ACID;
			}
			else
			if(ilvl<=12)
			{
				toDoCode=CODE_CASTMALICIOUSSPELLPER10;
				toDoString="Spell_Nightmare";
			}
			else
			if(ilvl<=17)
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.STAT_SAVE_ACID;
			}
			else
			if(ilvl<=19)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.STAT_SAVE_ACID;
			}
			else
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.STAT_SAVE_ACID;
			}
			break;
		}
		case ORGANS:
		{
			if(ilvl<=22)
			{
				toDoCode=CODE_CASTFRIENDLYSPELLPER2;
				toDoString="Prayer_CureLight";
			}
			else
			if(ilvl<=24)
			{
				toDoCode=CODE_CASTFRIENDLYSPELLPER10;
				toDoString="Prayer_CureCritical";
			}
			else
			if(ilvl<=25)
			{
				toDoCode=CODE_CASTFRIENDLYSPELLPER5;
				toDoString="Prayer_CureSerious";
			}
			else
			{
				toDoCode=CODE_CASTFRIENDLYSPELLPER20;
				toDoString="Prayer_Heal";
			}
			break;
		}
		case PIANOS:
		{
			if(ilvl<=18)
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.STAT_SAVE_MIND;
			}
			else
			if(ilvl<=19)
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.STAT_SAVE_MIND;
			}
			else
			if(ilvl<=24)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.STAT_SAVE_MIND;
			}
			else
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.STAT_SAVE_MIND;
			}
			break;
		}
		case TROMBONES:
		{
			if(ilvl<=8)
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.STAT_CONSTITUTION;
			}
			else
			if(ilvl<=12)
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.STAT_CONSTITUTION;
			}
			else
			if(ilvl<=21)
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.STAT_CONSTITUTION;
			}
			else
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.STAT_CONSTITUTION;
			}
			break;
		}
		case TRUMPETS:
		{
			if(ilvl<=9)
			{
				toDoCode=CODE_UPENVPER2;
				toDoString="A";
			}
			else
			if(ilvl<=13)
			{
				toDoCode=CODE_UPENVPER2;
				toDoString="A";
			}
			else
			{
				toDoCode=CODE_UPENVPER2;
				toDoString="A";
			}
			break;
		}
		case TUBAS:
		{
			if(ilvl<=7)
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.STAT_STRENGTH;
			}
			else
			if(ilvl<=10)
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.STAT_STRENGTH;
			}
			else
			if(ilvl<=15)
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.STAT_STRENGTH;
			}
			else
			if(ilvl<=17)
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.STAT_STRENGTH;
			}
			else
			if(ilvl<=19)
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.STAT_STRENGTH;
			}
			else
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.STAT_STRENGTH;
			}
			break;
		}
		case VIOLINS:
		{
			if(ilvl<=7)
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.STAT_SAVE_COLD;
			}
			else
			if(ilvl<=14)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.STAT_SAVE_COLD;
			}
			else
			if(ilvl<=15)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.STAT_SAVE_COLD;
			}
			else
			if(ilvl<=16)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.STAT_SAVE_COLD;
			}
			else
			if(ilvl<=17)
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.STAT_SAVE_COLD;
			}
			else
			{
				toDoCode=CODE_UPDAMAGEPER3;
				toDoString=""+CMMsg.TYP_COLD;
			}
			break;
		}
		case WOODS:
		{
			if(ilvl<=1)
			{
				toDoCode=CODE_UPENVPER2;
				toDoString="D";
			}
			else
			if(ilvl<=2)
			{
				toDoCode=CODE_UPENVPER2;
				toDoString="D";
			}
			else
			{
				toDoCode=CODE_UPENVPER2;
				toDoString="D";
			}
			break;
		}
		case XYLOPHONES:
		{
			if(ilvl<=11)
			{
				toDoCode=CODE_CASTFRIENDLYSPELLPER10;
				toDoString="Prayer_RestoreVoice";
			}
			else
			if(ilvl<=12)
			{
				toDoCode=CODE_CASTFRIENDLYSPELLPER10;
				toDoString="Prayer_RemoveParalysis";
			}
			else
			if(ilvl<=15)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.STAT_SAVE_DISEASE;
			}
			else
			if(ilvl<=18)
			{
				toDoCode=CODE_CASTMALICIOUSSPELLPER10;
				toDoString="Spell_Exhaustion";
			}
			else
			{
				toDoCode=CODE_UPMOVEMENT;
			}
			break;
		}
		default:
			break;
		}
		toDoVal=CMath.s_int(toDoString);
		return toDoCode;
	}

	@Override
	public void executeMsg(Environmental E, CMMsg msg)
	{
		if(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		switch(toDoCode)
		{
		case CODE_DOWNDAMAGEPER5:
			if((msg.sourceMinor()==toDoVal)
			&&(msg.target()==affected))
			{
				final int dmg=(adjustedLevel(invoker(),0)/5);
				msg.setValue(msg.value()-dmg);
			}
			break;
		case CODE_UPDAMAGEPER3:
		case CODE_UPDAMAGEPER5:
			if((affected==invoker())
			&&(msg.sourceMinor()==toDoVal)
			&&(msg.target() instanceof Physical)
			&&((((Physical)msg.target()).fetchEffect(ID())==null)
				||(((Physical)msg.target()).fetchEffect(ID()).invoker()!=invoker())))
			{
				int dmg=0;
				if(toDoCode==CODE_UPDAMAGEPER3)
					dmg=dmg+(adjustedLevel(invoker(),0)/3);
				else
					dmg=dmg+(adjustedLevel(invoker(),0)/5);
				msg.setValue(msg.value()+dmg);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void affectCharStats(MOB mob, CharStats stats)
	{
		super.affectCharStats(mob,stats);
		switch(toDoCode)
		{
		case CODE_UPSAVE:
			stats.setStat(toDoVal,stats.getStat(toDoVal)+adjustedLevel(invoker(),0));
			break;
		case CODE_UPSTAT:
			int lvl=adjustedLevel(invoker(),0)/10;
			if(lvl<1)
				lvl=1;
			stats.setStat(toDoVal,stats.getStat(toDoVal)+(lvl));
			break;
		case CODE_DOWNSAVE:
			if(mob!=invoker())
				stats.setStat(toDoVal,stats.getStat(toDoVal)-adjustedLevel(invoker(),0));
			break;
		}
	}

	@Override
	public void affectPhyStats(Physical mob, PhyStats stats)
	{
		super.affectPhyStats(mob,stats);
		if(((toDoCode==CODE_UPENVPER2)||(toDoCode==CODE_UPENVPER5))
		&&(toDoString.length()>0)
		&&(invoker()!=null))
		{
			int lvl=adjustedLevel(invoker(),0);
			if(toDoCode==CODE_UPENVPER2)
				lvl=lvl/2;
			else
				lvl=lvl/5;
			switch(toDoString.charAt(0))
			{
			case 'A':
				stats.setAttackAdjustment(stats.armor()+lvl);
				break;
			case 'D':
				stats.setArmor(stats.armor()-lvl);
				break;
			}
		}
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if((mob.isInCombat())&&(mob.isMonster()))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!(affected instanceof MOB))
			return true;
		int per=-1;
		int addedLevel=0;
		switch(toDoCode)
		{
		case CODE_CASTMALICIOUSSPELLPER10:
			if(!((MOB)affected).isInCombat())
				return true;
			per=10;
			addedLevel=10;
			break;
		case CODE_CASTFRIENDLYSPELLPER2:
			per=2;
			break;
		case CODE_CASTFRIENDLYSPELLPER5:
			per=5;
			break;
		case CODE_CASTFRIENDLYSPELLPER10:
			per=10;
			addedLevel=10;
			break;
		case CODE_CASTFRIENDLYSPELLPER20:
			per=20;
			addedLevel=20;
			break;
		case CODE_UPMOVEMENT:
			((MOB)affected).curState().adjMovement(1,((MOB)affected).maxState());
			break;
		case CODE_REMOVESPELLTYPE:
			{
				final MOB M=(MOB)affected;
				final List<Ability> V=CMLib.flags().flaggedAffects(M,toDoVal);
				for(int v=0;v<V.size();v++)
				{
					final Ability A =V.get(v);
					A.unInvoke();
					if(M.fetchEffect(A.ID())==null)
						break;
				}
				return true;
			}
		case CODE_SPEEDCOMMONSKILLS:
			{
				final MOB M=(MOB)affected;
				for(final Enumeration<Ability> a=M.effects();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((A!=null)
					&&(A.invoker()==M)
					&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL))
						A.tick(M,Tickable.TICKID_MOB);
				}
				return true;
			}
		default:
			return true;
		}
		if((per>0)
		&&(CMLib.dice().rollPercentage()<((addedLevel+adjustedLevel(invoker(),0))/per)))
		switch(toDoCode)
		{
		case CODE_CASTMALICIOUSSPELLPER10:
			{
				final MOB M=(MOB)affected;
				final MOB V=M.getVictim();
				final Ability A=CMClass.getAbility(toDoString);
				if(A==null)
					Log.errOut("Symphony","No spell- "+toDoString);
				else A.invoke(M,V,true,0);
			}
			break;
		default:
			{
				final MOB M=(MOB)affected;
				final Ability A=CMClass.getAbility(toDoString);
				if(A==null)
					Log.errOut("Symphony","No spell- "+toDoString);
				else A.invoke(M,M,true,0);
			}
			break;
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		instrument=null;
		toDoCode=-1;
		if(!auto)
		{
			instrument=getInstrument(mob,requiredInstrumentType(),true);
			if(instrument!=null)
				getToDoCode();
		}
		return super.invoke(mob,commands,givenTarget,auto,asLevel);
	}
}

