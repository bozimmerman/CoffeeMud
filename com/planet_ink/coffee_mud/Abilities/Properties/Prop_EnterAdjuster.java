package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_EnterAdjuster extends Property
{
	public String ID() { return "Prop_EnterAdjuster"; }
	public String name(){ return "Room entering adjuster";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ROOMS;}
	public Environmental newInstance()
	{	Prop_EnterAdjuster newOne=new Prop_EnterAdjuster(); newOne.setMiscText(text());return newOne; }

	public String accountForYourself()
	{ return "Goodies for entry.";	}

	public void eatIfAble(MOB mob)
	{
		String names=text();
		Vector theSpells=new Vector();
		int del=names.indexOf(";");
		while(del>=0)
		{
			String thisOne=names.substring(0,del);
			if((thisOne.length()>0)&&(!thisOne.equals(";")))
			{
				Ability A=(Ability)CMClass.getAbility(thisOne);
				if(A!=null)
				{
					A=(Ability)A.copyOf();
					theSpells.addElement(A);
				}
			}
			names=names.substring(del+1);
			del=names.indexOf(";");
		}
		if((names.length()>0)&&(!names.equals(";")))
		{
			Ability A=(Ability)CMClass.getAbility(names);
			if(A!=null)
			{
				A=(Ability)A.copyOf();
				theSpells.addElement(A);
			}
		}
		for(int i=0;i<theSpells.size();i++)
		{
			Ability thisOne=(Ability)((Ability)theSpells.elementAt(i)).copyOf();
			thisOne.invoke(mob,mob,true);
		}
	}

	public void EATME(MOB mob)
	{
		eatIfAble(mob);
		String readableText=text();
		mob.baseEnvStats().setAbility(mob.baseEnvStats().ability()+Util.getParmPlus(readableText,"abi"));
		mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()+Util.getParmPlus(readableText,"arm"));
		mob.baseEnvStats().setAttackAdjustment(mob.baseEnvStats().attackAdjustment()+Util.getParmPlus(readableText,"att"));
		mob.baseEnvStats().setDamage(mob.baseEnvStats().damage()+Util.getParmPlus(readableText,"dam"));
		mob.baseEnvStats().setDisposition(mob.baseEnvStats().disposition()|Util.getParmPlus(readableText,"dis"));
		mob.baseEnvStats().setLevel(mob.baseEnvStats().level()+Util.getParmPlus(readableText,"lev"));
		mob.baseEnvStats().setRejuv(mob.baseEnvStats().rejuv()+Util.getParmPlus(readableText,"rej"));
		mob.baseEnvStats().setSensesMask(mob.baseEnvStats().sensesMask()|Util.getParmPlus(readableText,"sen"));
		mob.baseEnvStats().setSpeed(mob.baseEnvStats().speed()+Util.getParmPlus(readableText,"spe"));
		mob.baseEnvStats().setWeight(mob.baseEnvStats().weight()+Util.getParmPlus(readableText,"wei"));
		mob.baseEnvStats().setHeight(mob.baseEnvStats().height()+Util.getParmPlus(readableText,"hei"));

		mob.baseCharStats().setStat(CharStats.CHARISMA,mob.baseCharStats().getStat(CharStats.CHARISMA)+Util.getParmPlus(readableText,"cha"));
		mob.baseCharStats().setStat(CharStats.CONSTITUTION,mob.baseCharStats().getStat(CharStats.CONSTITUTION)+Util.getParmPlus(readableText,"con"));
		mob.baseCharStats().setStat(CharStats.DEXTERITY,mob.baseCharStats().getStat(CharStats.DEXTERITY)+Util.getParmPlus(readableText,"dex"));
		String val=Util.getParmStr(readableText,"gen","").toUpperCase();
		if((val.length()>0)&&((val.charAt(0)=='M')||(val.charAt(0)=='F')||(val.charAt(0)=='N')))
			mob.baseCharStats().setStat(CharStats.GENDER,(int)val.charAt(0));
		mob.baseCharStats().setStat(CharStats.INTELLIGENCE,mob.baseCharStats().getStat(CharStats.INTELLIGENCE)+Util.getParmPlus(readableText,"int"));
		val=Util.getParmStr(readableText,"cla","").toUpperCase();
		if((val.length()>0)&&(CMClass.getCharClass(val)!=null))
			mob.baseCharStats().setCurrentClass(CMClass.getCharClass(val));
		val=Util.getParmStr(readableText,"rac","").toUpperCase();
		if((val.length()>0)&&(CMClass.getRace(val)!=null))
		{
			mob.baseCharStats().setMyRace(CMClass.getRace(val));
			mob.baseCharStats().getMyRace().startRacing(mob,false);
		}
		mob.baseCharStats().setStat(CharStats.STRENGTH,mob.baseCharStats().getStat(CharStats.STRENGTH)+Util.getParmPlus(readableText,"str"));
		mob.baseCharStats().setStat(CharStats.WISDOM,mob.baseCharStats().getStat(CharStats.WISDOM)+Util.getParmPlus(readableText,"wis"));
		if(Util.getParmPlus(readableText,"lev")!=0)
			mob.baseCharStats().setClassLevel(mob.baseCharStats().getCurrentClass(),mob.baseCharStats().getClassLevel(mob.baseCharStats().getCurrentClass())+Util.getParmPlus(readableText,"lev"));

		mob.baseState().setHitPoints(mob.curState().getHitPoints()+Util.getParmPlus(readableText,"hit"));
		mob.curState().setHunger(mob.curState().getHunger()+Util.getParmPlus(readableText,"hun"));
		mob.curState().setMana(mob.curState().getMana()+Util.getParmPlus(readableText,"man"));
		mob.curState().setMovement(mob.curState().getMovement()+Util.getParmPlus(readableText,"mov"));
		mob.curState().setThirst(mob.curState().getThirst()+Util.getParmPlus(readableText,"thi"));

		mob.setPractices(mob.getPractices()+Util.getParmPlus(readableText,"prac"));
		mob.setTrains(mob.getTrains()+Util.getParmPlus(readableText,"trai"));
		mob.setQuestPoint(mob.getQuestPoint()+Util.getParmPlus(readableText,"ques"));
		mob.setMoney(mob.getMoney()+Util.getParmPlus(readableText,"coin"));
		int exp=Util.getParmPlus(readableText,"expe");
		if(exp>0) ExternalPlay.postExperience(mob,null,mob.getLeigeID(),exp,false);
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.recoverMaxState();
		mob.confirmWearability();
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected!=null)&&((affected instanceof Room)||(affected instanceof Exit)))
		{
			switch(affect.targetMinor())
			{
			case Affect.TYP_ENTER:
				EATME(affect.source());
				break;
			}
		}
		return super.okAffect(myHost,affect);
	}
}
