package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

public class GenSuperPill extends GenPill
{
	public String ID(){	return "GenSuperPill";}
	public GenSuperPill()
	{
		super();

		setName("a pill");
		baseEnvStats.setWeight(1);
		setDisplayText("An strange pill lies here.");
		setDescription("Large and round, with strange markings.");
		secretIdentity="";
		baseGoldValue=200;
		recoverEnvStats();
		material=EnvResource.RESOURCE_CORN;
	}

	public Environmental newInstance()
	{
		return new GenSuperPill();
	}
	public boolean isGeneric(){return true;}

	public static int getVal(String text, String key)
	{
		text=text.toUpperCase();
		key=key.toUpperCase();
		int x=text.indexOf(key);
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='+')&&(text.charAt(x)!='-'))
					x++;
				if(x<text.length())
				{
					char pm=text.charAt(x);
					while((x<text.length())&&(!Character.isDigit(text.charAt(x))))
						x++;
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())&&(Character.isDigit(text.charAt(x))))
							x++;
						if(pm=='+')
							return Util.s_int(text.substring(0,x));
						else
							return -Util.s_int(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return 0;
	}

	public static String getStr(String text, String key)
	{
		String oldText=text;
		text=text.toUpperCase();
		key=key.toUpperCase();
		int x=text.indexOf(key);
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='='))
					x++;
				if(x<text.length())
				{
					while((x<text.length())&&(!Character.isLetter(text.charAt(x))))
						x++;
					if(x<text.length())
					{
						oldText=oldText.substring(x);
						text=text.substring(x);
						x=0;
						while((x<text.length())&&(Character.isLetter(text.charAt(x))))
							x++;
						return oldText.substring(0,x).trim();
					}

				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return "";
	}

	public String secretIdentity()
	{
		String id=StdScroll.makeSecretIdentity("super pill",super.secretIdentity(),"",getSpells(this));
		return id;
	}

	public void EATME(MOB mob)
	{
		boolean redress=false;
		if(getSpells(this).size()>0)
			eatIfAble(mob,this);
		mob.baseEnvStats().setAbility(mob.baseEnvStats().ability()+getVal(readableText,"abi"));
		mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()+getVal(readableText,"arm"));
		mob.baseEnvStats().setAttackAdjustment(mob.baseEnvStats().attackAdjustment()+getVal(readableText,"att"));
		mob.baseEnvStats().setDamage(mob.baseEnvStats().damage()+getVal(readableText,"dam"));
		mob.baseEnvStats().setDisposition(mob.baseEnvStats().disposition()|getVal(readableText,"dis"));
		mob.baseEnvStats().setLevel(mob.baseEnvStats().level()+getVal(readableText,"lev"));
		mob.baseEnvStats().setRejuv(mob.baseEnvStats().rejuv()+getVal(readableText,"rej"));
		mob.baseEnvStats().setSensesMask(mob.baseEnvStats().sensesMask()|getVal(readableText,"sen"));
		mob.baseEnvStats().setSpeed(mob.baseEnvStats().speed()+getVal(readableText,"spe"));
		mob.baseEnvStats().setWeight(mob.baseEnvStats().weight()+getVal(readableText,"wei"));
		if(getVal(readableText,"wei")!=0) redress=true;
		mob.baseEnvStats().setHeight(mob.baseEnvStats().height()+getVal(readableText,"hei"));
		if(getVal(readableText,"hei")!=0) redress=true;

		String val=getStr(readableText,"gen").toUpperCase();
		if((val.length()>0)&&((val.charAt(0)=='M')||(val.charAt(0)=='F')||(val.charAt(0)=='N')))
			mob.baseCharStats().setStat(CharStats.GENDER,(int)val.charAt(0));
		val=getStr(readableText,"cla").toUpperCase();
		if((val.length()>0)&&(CMClass.getCharClass(val)!=null))
			mob.baseCharStats().setCurrentClass(CMClass.getCharClass(val));
		if(getVal(readableText,"lev")!=0)
			mob.baseCharStats().setClassLevel(mob.baseCharStats().getCurrentClass(),mob.baseCharStats().getClassLevel(mob.baseCharStats().getCurrentClass())+getVal(readableText,"lev"));
		val=getStr(readableText,"rac").toUpperCase();
		if((val.length()>0)&&(CMClass.getRace(val)!=null))
		{
			redress=true;
			mob.baseCharStats().setMyRace(CMClass.getRace(val));
			mob.baseCharStats().getMyRace().startRacing(mob,false);
		}
		mob.baseCharStats().setStat(CharStats.MAX_STRENGTH_ADJ,mob.baseCharStats().getStat(CharStats.MAX_STRENGTH_ADJ)+getVal(readableText,"maxstr"));
		mob.baseCharStats().setStat(CharStats.MAX_WISDOM_ADJ,mob.baseCharStats().getStat(CharStats.MAX_WISDOM_ADJ)+getVal(readableText,"maxwis"));
		mob.baseCharStats().setStat(CharStats.MAX_CHARISMA_ADJ,mob.baseCharStats().getStat(CharStats.MAX_CHARISMA_ADJ)+getVal(readableText,"maxcha"));
		mob.baseCharStats().setStat(CharStats.MAX_CONSTITUTION_ADJ,mob.baseCharStats().getStat(CharStats.MAX_CONSTITUTION_ADJ)+getVal(readableText,"maxcon"));
		mob.baseCharStats().setStat(CharStats.MAX_DEXTERITY_ADJ,mob.baseCharStats().getStat(CharStats.MAX_DEXTERITY_ADJ)+getVal(readableText,"maxdex"));
		mob.baseCharStats().setStat(CharStats.MAX_INTELLIGENCE_ADJ,mob.baseCharStats().getStat(CharStats.MAX_INTELLIGENCE_ADJ)+getVal(readableText,"maxint"));

		mob.baseState().setHitPoints(mob.curState().getHitPoints()+getVal(readableText,"hit"));
		mob.curState().setHunger(mob.curState().getHunger()+getVal(readableText,"hun"));
		mob.curState().setMana(mob.curState().getMana()+getVal(readableText,"man"));
		mob.curState().setMovement(mob.curState().getMovement()+getVal(readableText,"mov"));
		mob.curState().setThirst(mob.curState().getThirst()+getVal(readableText,"thi"));

		mob.setPractices(mob.getPractices()+getVal(readableText,"prac"));
		mob.setTrains(mob.getTrains()+getVal(readableText,"trai"));
		mob.setQuestPoint(mob.getQuestPoint()+getVal(readableText,"ques"));
		mob.setMoney(mob.getMoney()+getVal(readableText,"coin"));
		int exp=getVal(readableText,"expe");
		if(exp>0) mob.charStats().getCurrentClass().gainExperience(mob,null,mob.getLeigeID(),exp,false);
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.recoverMaxState();
		if(redress)	mob.confirmWearability();
	}

	public void affect(Environmental myHost, Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_EAT:
				if((affect.sourceMessage()==null)&&(affect.othersMessage()==null))
				{
					EATME(mob);
					super.affect(myHost,affect);
				}
				else
					affect.addTrailerMsg(new FullMsg(affect.source(),affect.target(),affect.tool(),affect.NO_EFFECT,null,affect.targetCode(),affect.targetMessage(),affect.NO_EFFECT,null));
				break;
			default:
				super.affect(myHost,affect);
				break;
			}
		}
		else
			super.affect(myHost,affect);
	}
}
