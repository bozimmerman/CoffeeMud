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
        mask=new Vector();
        buildMask(newText,mask);
    }
    
	public String accountForYourself()
	{ 
        return new Prop_HaveAdjuster().fixAccoutingsWithMask("Affects those who enter: "+text());
    }

    

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(((msg.targetMinor()==CMMsg.TYP_ENTER)&&((affected instanceof Room)||(affected instanceof Exit)))
		   ||((msg.targetMinor()==CMMsg.TYP_SIT)&&(affected==msg.target())&&(affected instanceof Rideable)))
        &&((mask.size()==0)||(MUDZapper.zapperCheckReal(mask,msg.source()))))
        {
            MOB mob=msg.source();
            String[] strs=separateMask(text());
            Vector theSpells=new Vector();
            int del=strs[0].indexOf(";");
            while(del>=0)
            {
                String thisOne=strs[0].substring(0,del);
                if((thisOne.length()>0)&&(!thisOne.equals(";")))
                {
                    Ability A=CMClass.getAbility(thisOne);
                    if((A!=null)&&(!CMAble.classOnly("Archon",A.ID())))
                    {
                        A=(Ability)A.copyOf();
                        theSpells.addElement(A);
                    }
                }
                strs[0]=strs[0].substring(del+1);
                del=strs[0].indexOf(";");
            }
            if((strs[0].length()>0)&&(!strs[0].equals(";")))
            {
                Ability A=CMClass.getAbility(strs[0]);
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
            
            mob.baseEnvStats().setAbility(mob.baseEnvStats().ability()+Util.getParmPlus(strs[0],"abi"));
            mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()+Util.getParmPlus(strs[0],"arm"));
            mob.baseEnvStats().setAttackAdjustment(mob.baseEnvStats().attackAdjustment()+Util.getParmPlus(strs[0],"att"));
            mob.baseEnvStats().setDamage(mob.baseEnvStats().damage()+Util.getParmPlus(strs[0],"dam"));
            mob.baseEnvStats().setDisposition(mob.baseEnvStats().disposition()|Util.getParmPlus(strs[0],"dis"));
            mob.baseEnvStats().setLevel(mob.baseEnvStats().level()+Util.getParmPlus(strs[0],"lev"));
            mob.baseEnvStats().setRejuv(mob.baseEnvStats().rejuv()+Util.getParmPlus(strs[0],"rej"));
            mob.baseEnvStats().setSensesMask(mob.baseEnvStats().sensesMask()|Util.getParmPlus(strs[0],"sen"));
            mob.baseEnvStats().setSpeed(mob.baseEnvStats().speed()+Util.getParmPlus(strs[0],"spe"));
            mob.baseEnvStats().setWeight(mob.baseEnvStats().weight()+Util.getParmPlus(strs[0],"wei"));
            mob.baseEnvStats().setHeight(mob.baseEnvStats().height()+Util.getParmPlus(strs[0],"hei"));

            mob.baseCharStats().setStat(CharStats.CHARISMA,mob.baseCharStats().getStat(CharStats.CHARISMA)+Util.getParmPlus(strs[0],"cha"));
            mob.baseCharStats().setStat(CharStats.CONSTITUTION,mob.baseCharStats().getStat(CharStats.CONSTITUTION)+Util.getParmPlus(strs[0],"con"));
            mob.baseCharStats().setStat(CharStats.DEXTERITY,mob.baseCharStats().getStat(CharStats.DEXTERITY)+Util.getParmPlus(strs[0],"dex"));
            String val=Util.getParmStr(strs[0],"gen","").toUpperCase();
            if((val.length()>0)&&((val.charAt(0)=='M')||(val.charAt(0)=='F')||(val.charAt(0)=='N')))
                mob.baseCharStats().setStat(CharStats.GENDER,val.charAt(0));
            mob.baseCharStats().setStat(CharStats.INTELLIGENCE,mob.baseCharStats().getStat(CharStats.INTELLIGENCE)+Util.getParmPlus(strs[0],"int"));
            val=Util.getParmStr(strs[0],"cla","").toUpperCase();
            if((val.length()>0)&&(CMClass.findCharClass(val)!=null)&&(!val.equalsIgnoreCase("Archon")))
                mob.baseCharStats().setCurrentClass(CMClass.findCharClass(val));
            val=Util.getParmStr(strs[0],"rac","").toUpperCase();
            if((val.length()>0)&&(CMClass.getRace(val)!=null))
            {
                int oldCat=mob.baseCharStats().ageCategory();
                mob.baseCharStats().setMyRace(CMClass.getRace(val));
                mob.baseCharStats().getMyRace().startRacing(mob,false);
                if(mob.baseCharStats().getStat(CharStats.AGE)>0)
                    mob.baseCharStats().setStat(CharStats.AGE,mob.baseCharStats().getMyRace().getAgingChart()[oldCat]);
            }
            mob.baseCharStats().setStat(CharStats.STRENGTH,mob.baseCharStats().getStat(CharStats.STRENGTH)+Util.getParmPlus(strs[0],"str"));
            mob.baseCharStats().setStat(CharStats.WISDOM,mob.baseCharStats().getStat(CharStats.WISDOM)+Util.getParmPlus(strs[0],"wis"));
            if(Util.getParmPlus(strs[0],"lev")!=0)
                mob.baseCharStats().setClassLevel(mob.baseCharStats().getCurrentClass(),mob.baseCharStats().getClassLevel(mob.baseCharStats().getCurrentClass())+Util.getParmPlus(strs[0],"lev"));

            mob.baseState().setHitPoints(mob.curState().getHitPoints()+Util.getParmPlus(strs[0],"hit"));
            mob.curState().setHunger(mob.curState().getHunger()+Util.getParmPlus(strs[0],"hun"));
            mob.curState().setMana(mob.curState().getMana()+Util.getParmPlus(strs[0],"man"));
            mob.curState().setMovement(mob.curState().getMovement()+Util.getParmPlus(strs[0],"mov"));
            mob.curState().setThirst(mob.curState().getThirst()+Util.getParmPlus(strs[0],"thi"));

            mob.setPractices(mob.getPractices()+Util.getParmPlus(strs[0],"prac"));
            mob.setTrains(mob.getTrains()+Util.getParmPlus(strs[0],"trai"));
            mob.setQuestPoint(mob.getQuestPoint()+Util.getParmPlus(strs[0],"ques"));
            int newMoney=Util.getParmPlus(strs[0],"coin");
            if(newMoney!=0) BeanCounter.setMoney(mob,BeanCounter.getMoney(mob)+newMoney);
            int exp=Util.getParmPlus(strs[0],"expe");
            if(exp>0) MUDFight.postExperience(mob,null,null,exp,false);
            mob.recoverCharStats();
            mob.recoverEnvStats();
            mob.recoverMaxState();
            mob.confirmWearability();
        }
		return super.okMessage(myHost,msg);
	}
}
