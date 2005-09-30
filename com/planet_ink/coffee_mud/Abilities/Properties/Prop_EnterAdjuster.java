package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Prop_EnterAdjuster extends Property
{
	public String ID() { return "Prop_EnterAdjuster"; }
	public String name(){ return "Room entering adjuster";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ROOMS;}
    private Vector mask=new Vector();

    public void setMiscText(String newText)
    {
        super.setMiscText(newText);
        mask.clear();
        Prop_HaveAdjuster.buildMask(newText,mask);
    }
    
	public String accountForYourself()
	{ 
        return Prop_HaveAdjuster.fixAccoutingsWithMask("Affects those who enter: "+text());
    }

    
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
				Ability A=CMClass.getAbility(thisOne);
				if((A!=null)&&(!CMAble.classOnly("Archon",A.ID())))
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
			Ability A=CMClass.getAbility(names);
			if(A!=null)
			{
				A=(Ability)A.copyOf();
				theSpells.addElement(A);
			}
		}
		for(int i=0;i<theSpells.size();i++)
		{
			Ability thisOne=(Ability)((Ability)theSpells.elementAt(i)).copyOf();
			thisOne.invoke(mob,mob,true,0);
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
			mob.baseCharStats().setStat(CharStats.GENDER,val.charAt(0));
		mob.baseCharStats().setStat(CharStats.INTELLIGENCE,mob.baseCharStats().getStat(CharStats.INTELLIGENCE)+Util.getParmPlus(readableText,"int"));
		val=Util.getParmStr(readableText,"cla","").toUpperCase();
		if((val.length()>0)&&(CMClass.findCharClass(val)!=null)&&(!val.equalsIgnoreCase("Archon")))
			mob.baseCharStats().setCurrentClass(CMClass.findCharClass(val));
		val=Util.getParmStr(readableText,"rac","").toUpperCase();
		if((val.length()>0)&&(CMClass.getRace(val)!=null))
		{
		    int oldCat=mob.baseCharStats().ageCategory();
			mob.baseCharStats().setMyRace(CMClass.getRace(val));
			mob.baseCharStats().getMyRace().startRacing(mob,false);
			if(mob.baseCharStats().getStat(CharStats.AGE)>0)
				mob.baseCharStats().setStat(CharStats.AGE,mob.baseCharStats().getMyRace().getAgingChart()[oldCat]);
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
		int newMoney=Util.getParmPlus(readableText,"coin");
		if(newMoney!=0) BeanCounter.setMoney(mob,BeanCounter.getMoney(mob)+newMoney);
		int exp=Util.getParmPlus(readableText,"expe");
		if(exp>0) MUDFight.postExperience(mob,null,null,exp,false);
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.recoverMaxState();
		mob.confirmWearability();
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(((msg.targetMinor()==CMMsg.TYP_ENTER)&&((affected instanceof Room)||(affected instanceof Exit)))
		   ||((msg.targetMinor()==CMMsg.TYP_SIT)&&(affected==msg.target())&&(affected instanceof Rideable)))
        &&((mask.size()==0)||(MUDZapper.zapperCheckReal(mask,msg.source()))))
			EATME(msg.source());
		return super.okMessage(myHost,msg);
	}
}
