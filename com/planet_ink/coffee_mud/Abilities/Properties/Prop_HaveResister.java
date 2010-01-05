package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
    protected String maskString="";
    protected String parmString="";
    protected boolean ignoreCharStats=true;
    protected long lastProtection=0;
    protected int remainingProtection=0;

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		adjCharStats=(CharStats)CMClass.getCommon("DefaultCharStats");
        ignoreCharStats=true;
        parmString=newText;
        int maskindex=newText.toUpperCase().indexOf("MASK=");
        if(maskindex>0)
        {
            maskString=newText.substring(maskindex+5).trim();
            parmString=newText.substring(0,maskindex).trim();
        }
        for(int i : CharStats.CODES.SAVING_THROWS())
        {
        	if(parmString.toUpperCase().indexOf(CharStats.CODES.NAME(i))>=0)
	            adjCharStats.setStat(i,getProtection(CharStats.CODES.NAME(i)));
        	else
                adjCharStats.setStat(i,getProtection(CMStrings.limit(CharStats.CODES.NAME(i),4)));
            if(adjCharStats.getStat(i)!=0)
                ignoreCharStats=false;
        }
	}

    protected void ensureStarted()
	{
		if(adjCharStats==null)
			setMiscText(text());
	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		ensureStarted();
        if((!ignoreCharStats)
        &&(canResist(affectedMOB))
        &&((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,affectedMOB,false))))
            for(int i : CharStats.CODES.SAVING_THROWS())
                affectedStats.setStat(i,affectedStats.getStat(i)+adjCharStats.getStat(i));
		super.affectCharStats(affectedMOB,affectedStats);
	}


	public boolean checkProtection(String protType){ return getProtection(protType)!=0;}

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
				tot+=CMath.s_int(""+nonMask.charAt(x))*mul;
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

    protected int weaponProtection(String kind, int damage, int myLevel, int hisLevel)
    {
        int protection=remainingProtection;
        if((System.currentTimeMillis()-lastProtection)>=Tickable.TIME_TICK)
        {    protection=(getProtection(kind)+(myLevel-hisLevel)); lastProtection=System.currentTimeMillis();}
        if(protection<=0) return damage;
        remainingProtection=protection-100;
        if(protection>=100){ return 0;}
        return (int)Math.round(CMath.mul(damage,1.0-CMath.div(protection,100.0)));
    }
    
	public void resistAffect(CMMsg msg, MOB mob, Ability me, String maskString)
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
                if((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,mob,false)))
    				msg.setValue(weaponProtection("weapons",msg.value(),mob.envStats().level(),msg.source().envStats().level()));
            }
			else
			{
				Weapon W=(Weapon)msg.tool();
				if((W.weaponType()==Weapon.TYPE_BASHING)
                &&(checkProtection("blunt"))
                &&((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,mob,false))))
                    msg.setValue(weaponProtection("blunt",msg.value(),mob.envStats().level(),msg.source().envStats().level()));
				if((W.weaponType()==Weapon.TYPE_PIERCING)
                &&(checkProtection("pierce"))
                &&((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,mob,false))))
                    msg.setValue(weaponProtection("pierce",msg.value(),mob.envStats().level(),msg.source().envStats().level()));
			    if((W.weaponType()==Weapon.TYPE_SLASHING)
                &&(checkProtection("slash"))
                &&((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,mob,false))))
                    msg.setValue(weaponProtection("slash",msg.value(),mob.envStats().level(),msg.source().envStats().level()));
			}
			return;
		}
	}

    public String accountForYourself()
    { return "The owner gains resistances: "+describeResistance(text());}

	public boolean isOk(CMMsg msg, Ability me, MOB mob, String maskString)
	{
		if(!CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
			return true;

		if(CMath.bset(msg.targetCode(),CMMsg.MASK_MAGIC))
		{
			if(msg.tool() instanceof Ability)
			{
				Ability A=(Ability)msg.tool();
				if(CMath.bset(A.flags(),Ability.FLAG_TRANSPORTING))
				{
					if((checkProtection("teleport"))
                    &&((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,mob,false))))
					{
						msg.source().tell("You can't seem to fixate on '"+mob.name()+"'.");
						return false;
					}
				}
				else
				if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
				&&(CMath.bset(A.flags(),Ability.FLAG_HOLY))
				&&(!CMath.bset(A.flags(),Ability.FLAG_UNHOLY)))
				{
					if((checkProtection("holy"))
                    &&((maskString.length()==0)||(CMLib.masking().maskCheck(maskString,mob,false))))
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
            id+="  Restrictions: "+CMLib.masking().maskDesc(maskString)+".";
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
        if((canResist(msg.target()))
        &&(msg.target() instanceof MOB)
        &&(((MOB)msg.target()).location()!=null))
		{
			if((msg.value()<=0)&&(!isOk(msg,this,(MOB)msg.target(),maskString)))
				return false;
			resistAffect(msg,(MOB)msg.target(),this,maskString);
		}
		return true;
	}
}
