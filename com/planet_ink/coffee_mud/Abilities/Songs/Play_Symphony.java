package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Play_Symphony extends Play
{
	public String ID() { return "Play_Symphony"; }
	public String name(){ return "Symphony";}
	public int quality(){
		if(toDoCode<0)
			return BENEFICIAL_OTHERS;
		else
		if(toDoCode==CODE_DOWNSAVE)
			return MALICIOUS;
		else
			return BENEFICIAL_OTHERS;
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
		if(instrument==null){ toDoCode=-1; return toDoCode;}
		if(toDoCode>0) return toDoCode;
		int ilvl=instrument.envStats().level();
		switch(instrument.instrumentType())
		{
		case 0: //"CLARINETS",
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
				toDoString=""+CharStats.SAVE_ELECTRIC;
			}
			else
			if(ilvl<=15)
			{
				toDoCode=CODE_UPDAMAGEPER3;
				toDoString=""+Weapon.TYPE_STRIKING;
			}
			else
			if(ilvl<=18)
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.SAVE_ELECTRIC;
			}
			else
			if(ilvl<=21)
			{
				toDoCode=CODE_DOWNDAMAGEPER5;
				toDoString=""+Weapon.TYPE_STRIKING;
			}
			else
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.SAVE_ELECTRIC;
			}
			break;
		}
		case 1: //"CYMBALS",
		{
			if(ilvl<=2)
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.DEXTERITY;
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
				toDoString=""+CharStats.DEXTERITY;
			}
			else
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.SAVE_POISON;
			}
			break;
		}
		case 2: //"DRUMS",
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
				toDoString=""+CharStats.SAVE_JUSTICE;
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
				toDoString=""+CharStats.SAVE_JUSTICE;
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
				toDoString=""+CharStats.SAVE_JUSTICE;
			}
			else
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.SAVE_JUSTICE;
			}
			break;
		}
		case 3: //"FLUTES",
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
		case 4: //"GUITARS",
		{
			if(ilvl<=4)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.SAVE_FIRE;
			}
			else
			if(ilvl<=5)
			{
				toDoCode=CODE_UPDAMAGEPER5;
				toDoString=""+Weapon.TYPE_BURNING;
			}
			else
			if(ilvl<=6)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.SAVE_FIRE;
			}
			else
			if(ilvl<=9)
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.SAVE_FIRE;
			}
			else
			if(ilvl<=12)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.SAVE_FIRE;
			}
			else
			if(ilvl<=13)
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.SAVE_FIRE;
			}
			else
			{
				toDoCode=CODE_UPDAMAGEPER3;
				toDoString=""+CharStats.SAVE_FIRE;
			}
			break;
		}
		case 5: //"HARMONICAS",
		{
			toDoCode=CODE_UPENVPER5;
			toDoString="D";
			break;
		}
		case 6: //"HARPS",
		{
			if(ilvl<=3)
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.CHARISMA;
			}
			else
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.CHARISMA;
			}
			break;
		}
		case 7: //"HORNS",
		{
			if(ilvl<=4)
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.SAVE_ACID;
			}
			else
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.SAVE_ACID;
			}
			break;
		}
		case 8: //"OBOES",
		{
			if(ilvl<=4)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.SAVE_ACID;
			}
			else
			if(ilvl<=7)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.SAVE_ACID;
			}
			else
			if(ilvl<=9)
			{
				toDoCode=CODE_UPDAMAGEPER3;
				toDoString=""+Weapon.TYPE_MELTING;
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
				toDoString=""+CharStats.SAVE_ACID;
			}
			else
			if(ilvl<=19)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.SAVE_ACID;
			}
			else
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.SAVE_ACID;
			}
			break;
		}
		case 9: //"ORGANS",
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
		case 10: //"PIANOS",
		{
			if(ilvl<=18)
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.SAVE_MIND;
			}
			else
			if(ilvl<=19)
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.SAVE_MIND;
			}
			else
			if(ilvl<=24)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.SAVE_MIND;
			}
			else
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.SAVE_MIND;
			}
			break;
		}
		case 11: //"TROMBONES",
		{
			if(ilvl<=8)
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.CONSTITUTION;
			}
			else
			if(ilvl<=12)
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.CONSTITUTION;
			}
			else
			if(ilvl<=21)
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.CONSTITUTION;
			}
			else
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.CONSTITUTION;
			}
			break;
		}
		case 12: //"TRUMPETS",
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
		case 13: //"TUBAS",
		{
			if(ilvl<=7)
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.STRENGTH;
			}
			else
			if(ilvl<=10)
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.STRENGTH;
			}
			else
			if(ilvl<=15)
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.STRENGTH;
			}
			else
			if(ilvl<=17)
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.STRENGTH;
			}
			else
			if(ilvl<=19)
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.STRENGTH;
			}
			else
			{
				toDoCode=CODE_UPSTAT;
				toDoString=""+CharStats.STRENGTH;
			}
			break;
		}
		case 14: //"VIOLINS",
		{
			if(ilvl<=7)
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.SAVE_COLD;
			}
			else
			if(ilvl<=14)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.SAVE_COLD;
			}
			else
			if(ilvl<=15)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.SAVE_COLD;
			}
			else
			if(ilvl<=16)
			{
				toDoCode=CODE_UPSAVE;
				toDoString=""+CharStats.SAVE_COLD;
			}
			else
			if(ilvl<=17)
			{
				toDoCode=CODE_DOWNSAVE;
				toDoString=""+CharStats.SAVE_COLD;
			}
			else
			{
				toDoCode=CODE_UPDAMAGEPER3;
				toDoString=""+Weapon.TYPE_FROSTING;
			}
			break;
		}
		case 15: //"WOODS",
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
		case 16: //"XYLOPHONES"};
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
				toDoString=""+CharStats.SAVE_DISEASE;
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
		toDoVal=Util.s_int(toDoString);
		return toDoCode;
	}

	public void executeMsg(Environmental E, CMMsg msg)
	{
		if(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		switch(toDoCode)
		{
		case CODE_DOWNDAMAGEPER5:
		if((msg.sourceMinor()==toDoVal)
		&&(msg.target()==affected))
		{
			int dmg=(invokerLevel()/5);
			msg.setValue(msg.value()-dmg);
			break;
		}
		case CODE_UPDAMAGEPER3:
		case CODE_UPDAMAGEPER5:
		if((affected==invoker())
		&&(msg.sourceMinor()==toDoVal)
		&&(msg.target()!=null)
		&&(msg.target().fetchEffect(ID())==null)
		&&(msg.target().fetchEffect(ID()).invoker()!=invoker()))
		{
			int dmg=0;
			if(toDoCode==CODE_UPDAMAGEPER3)
				dmg=dmg+(invokerLevel()/3);
			else
				dmg=dmg+(invokerLevel()/5);
			msg.setValue(msg.value()+dmg);
			break;
		}
		default:
			break;
		}
	}

	public void affectCharStats(MOB mob, CharStats stats)
	{
		super.affectCharStats(mob,stats);
		switch(toDoCode)
		{
		case CODE_UPSAVE:
			stats.setStat(toDoVal,stats.getStat(toDoVal)+invokerLevel());
			break;
		case CODE_UPSTAT:
			int lvl=invokerLevel()/10;
			if(lvl<1) lvl=1;
			stats.setStat(toDoVal,stats.getStat(toDoVal)+(lvl));
			break;
		case CODE_DOWNSAVE:
			if(mob!=invoker())
				stats.setStat(toDoVal,stats.getStat(toDoVal)-invokerLevel());
			break;
		}
	}
	public void affectEnvStats(Environmental mob, EnvStats stats)
	{
		super.affectEnvStats(mob,stats);
		if(((toDoCode==CODE_UPENVPER2)||(toDoCode==CODE_UPENVPER5))
		&&(toDoString.length()>0)
		&&(invoker()!=null))
		{
			int lvl=invokerLevel();
			if(toDoCode==CODE_UPENVPER2)
				lvl=lvl/2;
			else
				lvl=lvl/5;
			switch((int)toDoString.charAt(0))
			{
			case (int)'A':
				stats.setAttackAdjustment(stats.armor()+lvl);
				break;
			case (int)'D':
				stats.setArmor(stats.armor()-lvl);
				break;
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
		   return true;
		int per=-1;
		switch(toDoCode)
		{
		case CODE_CASTMALICIOUSSPELLPER10:
			if(!((MOB)affected).isInCombat())
				return true;
			per=10;
			break;
		case CODE_CASTFRIENDLYSPELLPER2:
			per=2;
			break;
		case CODE_CASTFRIENDLYSPELLPER5:
			per=5;
			break;
		case CODE_CASTFRIENDLYSPELLPER10:
			per=10;
			break;
		case CODE_CASTFRIENDLYSPELLPER20:
			per=20;
			break;
		case CODE_UPMOVEMENT:
			((MOB)affected).curState().adjMovement(1,((MOB)affected).maxState());
			break;
		case CODE_REMOVESPELLTYPE:
			{
				MOB M=(MOB)affected;
				Vector V=Sense.flaggedAffects(M,toDoVal);
				for(int v=0;v<V.size();v++){((Ability)V.elementAt(v)).unInvoke(); break;}
				return true;
			}
		case CODE_SPEEDCOMMONSKILLS:
			{
				MOB M=(MOB)affected;
				for(int a=0;a<M.numAllEffects();a++)
				{
					Ability A=M.fetchEffect(a);
					if((A!=null)
					&&(A.invoker()==M)
					&&((A.classificationCode()&Ability.ALL_CODES)==Ability.COMMON_SKILL))
						A.tick(M,MudHost.TICK_MOB);
				}
				return true;
			}
		default:
			return true;
		}
		if((per>0)
		&&(Dice.rollPercentage()<(invokerLevel()/per)))
		switch(toDoCode)
		{
		case CODE_CASTMALICIOUSSPELLPER10:
			{
				MOB M=(MOB)affected;
				MOB V=M.getVictim();
				Ability A=CMClass.getAbility(toDoString);
				if(A==null) Log.errOut("Symphony","No spell- "+toDoString);
				else A.invoke(M,V,true);
			}
			break;
		default:
			{
				MOB M=(MOB)affected;
				Ability A=CMClass.getAbility(toDoString);
				if(A==null) Log.errOut("Symphony","No spell- "+toDoString);
				else A.invoke(M,M,true);
			}
			break;
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		instrument=null;
		toDoCode=-1;
		if(!auto)
		{
			instrument=getInstrument(mob,requiredInstrumentType(),true);
			if(instrument!=null) getToDoCode();
		}
		return super.invoke(mob,commands,givenTarget,auto);
	}
}

