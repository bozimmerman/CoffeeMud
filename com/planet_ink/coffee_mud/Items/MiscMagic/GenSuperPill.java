package com.planet_ink.coffee_mud.Items.MiscMagic;
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
public class GenSuperPill extends GenPill
{
	public String ID(){	return "GenSuperPill";}
	public GenSuperPill()
	{
		super();

		setName("a pill");
		baseEnvStats.setWeight(1);
		setDisplayText("A strange pill lies here.");
		setDescription("Large and round, with strange markings.");
		secretIdentity="";
		baseGoldValue=200;
		recoverEnvStats();
		material=EnvResource.RESOURCE_CORN;
	}


	public boolean isGeneric(){return true;}

	public String secretIdentity()
	{
		String tx=StdScroll.makeSecretIdentity("super pill",super.secretIdentity(),"",getSpells());
		String id=readableText;
		int x=id.toUpperCase().indexOf("ARM");
		for(StringBuffer ID=new StringBuffer(id);((x>0)&&(x<id.length()));x++)
			if(id.charAt(x)=='-')
			{
				ID.setCharAt(x,'+');
				id=ID.toString();
				break;
			}
			else
			if(id.charAt(x)=='+')
			{
				ID.setCharAt(x,'-');
				id=ID.toString();
				break;
			}
			else
			if(Character.isDigit(id.charAt(x)))
				break;
		x=id.toUpperCase().indexOf("DIS");
		if(x>=0)
		{
			long val=Util.getParmPlus(id,"dis");
			int y=id.indexOf(""+val,x);
			if((val!=0)&&(y>x))
			{
				StringBuffer middle=new StringBuffer("");
				for(int num=0;num<EnvStats.dispositionsVerb.length;num++)
					if(Util.bset(val,Util.pow(2,num)))
						middle.append(EnvStats.dispositionsVerb[num]+" ");
				id=id.substring(0,x)+middle.toString().trim()+id.substring(y+((""+val).length()));
			}
		}
		x=id.toUpperCase().indexOf("SEN");
		if(x>=0)
		{
			long val=Util.getParmPlus(id,"sen");
			int y=id.indexOf(""+val,x);
			if((val!=0)&&(y>x))
			{
				StringBuffer middle=new StringBuffer("");
				for(int num=0;num<EnvStats.sensesVerb.length;num++)
					if(Util.bset(val,Util.pow(2,num)))
						middle.append(EnvStats.sensesVerb[num]+" ");
				id=id.substring(0,x)+middle.toString().trim()+id.substring(y+((""+val).length()));
			}
		}
		return tx+"\n("+id+")\n";
	}

	public void EATME(MOB mob)
	{
		boolean redress=false;
		if(getSpells().size()>0)
			eatIfAble(mob);
		if((Util.getParmPlus(readableText,"beacon")>0)
		&&(mob.location()!=null))
			mob.setStartRoom(mob.location());
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
		if(Util.getParmPlus(readableText,"wei")!=0) redress=true;
		mob.baseEnvStats().setHeight(mob.baseEnvStats().height()+Util.getParmPlus(readableText,"hei"));
		if(Util.getParmPlus(readableText,"hei")!=0) redress=true;

		String val=Util.getParmStr(readableText,"gen","").toUpperCase();
		if((val.length()>0)&&((val.charAt(0)=='M')||(val.charAt(0)=='F')||(val.charAt(0)=='N')))
			mob.baseCharStats().setStat(CharStats.GENDER,val.charAt(0));
		val=Util.getParmStr(readableText,"cla","").toUpperCase();
		if((val.length()>0)&&(CMClass.getCharClass(val)!=null)&&(!val.equalsIgnoreCase("Archon")))
		{
			mob.baseCharStats().setCurrentClass(CMClass.getCharClass(val));
			if((!mob.isMonster())&&(mob.soulMate()==null))
				CoffeeTables.bump(mob,CoffeeTables.STAT_CLASSCHANGE);
		}
		if(Util.getParmPlus(readableText,"lev")!=0)
			mob.baseCharStats().setClassLevel(mob.baseCharStats().getCurrentClass(),mob.baseCharStats().getClassLevel(mob.baseCharStats().getCurrentClass())+Util.getParmPlus(readableText,"lev"));
		val=Util.getParmStr(readableText,"rac","").toUpperCase();
		if((val.length()>0)&&(CMClass.getRace(val)!=null))
		{
			redress=true;
			mob.baseCharStats().setMyRace(CMClass.getRace(val));
			mob.baseCharStats().getMyRace().startRacing(mob,false);
		}
		mob.baseCharStats().setStat(CharStats.STRENGTH,mob.baseCharStats().getStat(CharStats.STRENGTH)+Util.getParmPlus(readableText,"str"));
		mob.baseCharStats().setStat(CharStats.WISDOM,mob.baseCharStats().getStat(CharStats.WISDOM)+Util.getParmPlus(readableText,"wis"));
		mob.baseCharStats().setStat(CharStats.CHARISMA,mob.baseCharStats().getStat(CharStats.CHARISMA)+Util.getParmPlus(readableText,"cha"));
		mob.baseCharStats().setStat(CharStats.CONSTITUTION,mob.baseCharStats().getStat(CharStats.CONSTITUTION)+Util.getParmPlus(readableText,"con"));
		mob.baseCharStats().setStat(CharStats.DEXTERITY,mob.baseCharStats().getStat(CharStats.DEXTERITY)+Util.getParmPlus(readableText,"dex"));
		mob.baseCharStats().setStat(CharStats.INTELLIGENCE,mob.baseCharStats().getStat(CharStats.INTELLIGENCE)+Util.getParmPlus(readableText,"int"));
		mob.baseCharStats().setStat(CharStats.MAX_STRENGTH_ADJ,mob.baseCharStats().getStat(CharStats.MAX_STRENGTH_ADJ)+Util.getParmPlus(readableText,"maxstr"));
		mob.baseCharStats().setStat(CharStats.MAX_WISDOM_ADJ,mob.baseCharStats().getStat(CharStats.MAX_WISDOM_ADJ)+Util.getParmPlus(readableText,"maxwis"));
		mob.baseCharStats().setStat(CharStats.MAX_CHARISMA_ADJ,mob.baseCharStats().getStat(CharStats.MAX_CHARISMA_ADJ)+Util.getParmPlus(readableText,"maxcha"));
		mob.baseCharStats().setStat(CharStats.MAX_CONSTITUTION_ADJ,mob.baseCharStats().getStat(CharStats.MAX_CONSTITUTION_ADJ)+Util.getParmPlus(readableText,"maxcon"));
		mob.baseCharStats().setStat(CharStats.MAX_DEXTERITY_ADJ,mob.baseCharStats().getStat(CharStats.MAX_DEXTERITY_ADJ)+Util.getParmPlus(readableText,"maxdex"));
		mob.baseCharStats().setStat(CharStats.MAX_INTELLIGENCE_ADJ,mob.baseCharStats().getStat(CharStats.MAX_INTELLIGENCE_ADJ)+Util.getParmPlus(readableText,"maxint"));

		mob.baseState().setHitPoints(mob.baseState().getHitPoints()+Util.getParmPlus(readableText,"hit"));
		mob.curState().setHunger(mob.curState().getHunger()+Util.getParmPlus(readableText,"hun"));
		mob.baseState().setMana(mob.baseState().getMana()+Util.getParmPlus(readableText,"man"));
		mob.baseState().setMovement(mob.baseState().getMovement()+Util.getParmPlus(readableText,"mov"));
		mob.curState().setThirst(mob.curState().getThirst()+Util.getParmPlus(readableText,"thi"));

		mob.setPractices(mob.getPractices()+Util.getParmPlus(readableText,"prac"));
		mob.setTrains(mob.getTrains()+Util.getParmPlus(readableText,"trai"));
		mob.setQuestPoint(mob.getQuestPoint()+Util.getParmPlus(readableText,"ques"));
		int newMoney=Util.getParmPlus(readableText,"coin");
		if(newMoney!=0) BeanCounter.setMoney(mob,BeanCounter.getMoney(mob)+newMoney);
		int exp=Util.getParmPlus(readableText,"expe");
		if(exp!=0) MUDFight.postExperience(mob,null,null,exp,false);
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.recoverMaxState();
		if(redress)	mob.confirmWearability();
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_EAT:
				if((msg.sourceMessage()==null)&&(msg.othersMessage()==null))
				{
					EATME(mob);
					super.executeMsg(myHost,msg);
				}
				else
					msg.addTrailerMsg(new FullMsg(msg.source(),msg.target(),msg.tool(),CMMsg.NO_EFFECT,null,msg.targetCode(),msg.targetMessage(),CMMsg.NO_EFFECT,null));
				break;
			default:
				super.executeMsg(myHost,msg);
				break;
			}
		}
		else
			super.executeMsg(myHost,msg);
	}
}
