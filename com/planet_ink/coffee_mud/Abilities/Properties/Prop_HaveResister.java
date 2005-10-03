package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

// this ability is the very picture of the infectuous msg.
// It lobs itself onto other qualified objects, and withdraws
// again when it will.  Don't lothe the HaveResister, LOVE IT.
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
public class Prop_HaveResister extends Property
{
	public String ID() { return "Prop_HaveResister"; }
	public String name(){ return "Resistance due to ownership";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	public boolean bubbleAffect(){return true;}
    protected CharStats adjCharStats=null;
    protected Vector mask=new Vector();
    protected String maskString="";
    protected String parmString="";
    protected boolean ignoreCharStats=true;

    public static Object[][] stats={
            {new Integer(CharStats.SAVE_MAGIC),"magic"},
            {new Integer(CharStats.SAVE_GAS),"gas"},
            {new Integer(CharStats.SAVE_FIRE),"fire"},
            {new Integer(CharStats.SAVE_ELECTRIC),"elec"},
            {new Integer(CharStats.SAVE_MIND),"mind"},
            {new Integer(CharStats.SAVE_JUSTICE),"justice"},
            {new Integer(CharStats.SAVE_COLD),"cold"},
            {new Integer(CharStats.SAVE_ACID),"acid"},
            {new Integer(CharStats.SAVE_WATER),"water"},
            {new Integer(CharStats.SAVE_UNDEAD),"evil"},
            {new Integer(CharStats.SAVE_DISEASE),"disease"},
            {new Integer(CharStats.SAVE_POISON),"poison"},
            {new Integer(CharStats.SAVE_PARALYSIS),"paralyze"},
            {new Integer(CharStats.SAVE_TRAPS),"traps"}
    };
    
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		adjCharStats=new DefaultCharStats();
        ignoreCharStats=true;
        for(int i=0;i<stats.length;i++)
        {
            adjCharStats.setStat(((Integer)stats[i][0]).intValue(),getProtection((String)stats[i][1]));
            if(adjCharStats.getStat(((Integer)stats[i][0]).intValue())!=0)
                ignoreCharStats=false;
        }
        mask=new Vector();
        parmString=newText;
        int maskindex=newText.toUpperCase().indexOf("MASK=");
        if(maskindex>0)
        {
            maskString=newText.substring(maskindex+5).trim();
            if(maskString.length()>0)
                Util.addToVector(MUDZapper.zapperCompile(maskString),mask);
            parmString=newText.substring(0,maskindex).trim();
        }
	}

	private void ensureStarted()
	{
		if(adjCharStats==null)
			setMiscText(text());
	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		ensureStarted();
        if((!ignoreCharStats)
        &&(canResist(affectedMOB))
        &&((mask.size()==0)||(MUDZapper.zapperCheckReal(mask,affectedMOB))))
            for(int i=0;i<stats.length;i++)
                affectedStats.setStat(((Integer)stats[i][0]).intValue(),affectedStats.getStat(((Integer)stats[i][0]).intValue())+adjCharStats.getStat(((Integer)stats[i][0]).intValue()));
		super.affectCharStats(affectedMOB,affectedStats);
	}


	public boolean checkProtection(String protType)
	{
		int prot=getProtection(protType);
		if(prot==0) return false;
		if(prot<5) prot=5;
		if(prot>95) prot=95;
		if(Dice.rollPercentage()<prot)
			return true;
		return false;
	}

	public int getProtection(String protType)
	{
        String nonMask=parmString.toUpperCase();
		int z=nonMask.indexOf(protType.toUpperCase());
		if(z<0) 
            return 0;
		int x=nonMask.indexOf("%",z+protType.length());
		if(x<0)
			return 50;
		int mul=1;
		int tot=0;
		while((--x)>=0)
		{
			if(Character.isDigit(nonMask.charAt(x)))
				tot+=Util.s_int(""+nonMask.charAt(x))*mul;
			else
			{
				if(nonMask.charAt(x)=='-')
					mul=mul*-1;
				x=-1;
			}
			mul=mul*10;
		}
		return tot;
	}
    
	public void resistAffect(CMMsg msg, MOB mob, Ability me, Vector mask)
	{
		if(mob.location()==null) return;
		if(mob.amDead()) return;
		if(!msg.amITarget(mob)) return;

		if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((msg.value())>0)
		&&(msg.tool()!=null)
	    &&(msg.tool() instanceof Weapon))
		{
			if(checkProtection("weapons"))
            {
                if((mask.size()==0)||(MUDZapper.zapperCheckReal(mask,mob)))
    				msg.setValue((int)Math.round(Util.mul(msg.value(),1.0-Util.div(getProtection("weapons"),100.0))));
            }
			else
			{
				Weapon W=(Weapon)msg.tool();
				if((W.weaponType()==Weapon.TYPE_BASHING)
                &&(checkProtection("blunt"))
                &&((mask.size()==0)||(MUDZapper.zapperCheckReal(mask,mob))))
					msg.setValue((int)Math.round(Util.mul(msg.value(),1.0-Util.div(getProtection("blunt"),100.0))));
				if((W.weaponType()==Weapon.TYPE_PIERCING)
                &&(checkProtection("pierce"))
                &&((mask.size()==0)||(MUDZapper.zapperCheckReal(mask,mob))))
					msg.setValue((int)Math.round(Util.mul(msg.value(),1.0-Util.div(getProtection("pierce"),100.0))));
			    if((W.weaponType()==Weapon.TYPE_SLASHING)
                &&(checkProtection("slash"))
                &&((mask.size()==0)||(MUDZapper.zapperCheckReal(mask,mob))))
			    	msg.setValue((int)Math.round(Util.mul(msg.value(),1.0-Util.div(getProtection("slash"),100.0))));
			}
			return;
		}
	}

    public String accountForYourself()
    { return "The owner gains resistances: "+describeResistance(text());}

	public boolean isOk(CMMsg msg, Ability me, MOB mob, Vector mask)
	{
		if(!Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
			return true;

		if(Util.bset(msg.targetCode(),CMMsg.MASK_MAGIC))
		{
			if(msg.tool() instanceof Ability)
			{
				Ability A=(Ability)msg.tool();
				if(Util.bset(A.flags(),Ability.FLAG_TRANSPORTING))
				{
					if((checkProtection("teleport"))
                    &&((mask.size()==0)||(MUDZapper.zapperCheckReal(mask,mob))))
					{
						msg.source().tell("You can't seem to fixate on '"+mob.name()+"'.");
						return false;
					}
				}
				else
				if(((A.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)
				&&(Util.bset(A.flags(),Ability.FLAG_HOLY))
				&&(!Util.bset(A.flags(),Ability.FLAG_UNHOLY)))
				{
					if((checkProtection("holy"))
                    &&((mask.size()==0)||(MUDZapper.zapperCheckReal(mask,mob))))
					{
						mob.location().show(msg.source(),mob,CMMsg.MSG_OK_VISUAL,"Holy energies from <S-NAME> are repelled from <T-NAME>.");
						return false;
					}
				}
			}
		}
		return true;
	}
    
    public String describeResistance(String text)
    {
        String id=parmString+".";
        if(maskString.length()>0)
            id+="  Restrictions: "+MUDZapper.zapperDesc(maskString)+".";
        return id;
    }
    
    public boolean canResist(Environmental E)
    {
        if((affected instanceof Item)
        &&(E instanceof MOB)
        &&(!((Item)affected).amDestroyed())
        &&(E==((Item)affected).owner()))
            return true;
        return false;
    }

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
        if(canResist(msg.target()))
		{
			MOB mob=(MOB)msg.target();
			if((msg.amITarget(mob))&&(mob.location()!=null))
			{
				if((msg.value()<=0)&&(!isOk(msg,this,mob,mask)))
					return false;
				resistAffect(msg,mob,this,mask);
			}
		}
		return true;
	}
}
