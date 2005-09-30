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
public class Prop_Resistance extends Property
{
	public String ID() { return "Prop_Resistance"; }
	public String name(){ return "Stuff Neutralizing";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	private CharStats adjCharStats=null;
    private Vector mask=new Vector();

	private void ensureStarted()
	{
		if(adjCharStats==null)
			setMiscText(text());
	}
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		this.adjCharStats=new DefaultCharStats();
		Prop_HaveResister.setAdjustments(this,adjCharStats);
        mask.clear();
        Prop_HaveAdjuster.buildMask(newText,mask);
	}
    
    public String accountForYourself()
    { return "Have resistances: "+Prop_Resistance.describeResistance(text());}
    
    public static String describeResistance(String text)
    {
        String[] strs=Prop_HaveAdjuster.separateMask(text);
        String id=strs[0]+".";
        if(strs[1].length()>0)
            id+="\n\rRestrictions: "+MUDZapper.zapperDesc(strs[1])+".";
        return id;
    }
    
	public void affectCharStats(MOB affected, CharStats affectedStats)
	{

	    super.affectCharStats(affected,affectedStats);
	    ensureStarted();
        if((mask.size()==0)||(MUDZapper.zapperCheckReal(mask,affected)))
        {
    	    affectedStats.setStat(CharStats.SAVE_MAGIC,affectedStats.getStat(CharStats.SAVE_MAGIC)+adjCharStats.getStat(CharStats.SAVE_MAGIC));
    		affectedStats.setStat(CharStats.SAVE_TRAPS,affectedStats.getStat(CharStats.SAVE_TRAPS)+adjCharStats.getStat(CharStats.SAVE_TRAPS));
    		affectedStats.setStat(CharStats.SAVE_GAS,affectedStats.getStat(CharStats.SAVE_GAS)+adjCharStats.getStat(CharStats.SAVE_GAS));
    		affectedStats.setStat(CharStats.SAVE_FIRE,affectedStats.getStat(CharStats.SAVE_FIRE)+adjCharStats.getStat(CharStats.SAVE_FIRE));
    		affectedStats.setStat(CharStats.SAVE_ELECTRIC,affectedStats.getStat(CharStats.SAVE_ELECTRIC)+adjCharStats.getStat(CharStats.SAVE_ELECTRIC));
    		affectedStats.setStat(CharStats.SAVE_MIND,affectedStats.getStat(CharStats.SAVE_MIND)+adjCharStats.getStat(CharStats.SAVE_MIND));
    		affectedStats.setStat(CharStats.SAVE_JUSTICE,affectedStats.getStat(CharStats.SAVE_JUSTICE)+adjCharStats.getStat(CharStats.SAVE_JUSTICE));
    		affectedStats.setStat(CharStats.SAVE_COLD,affectedStats.getStat(CharStats.SAVE_COLD)+adjCharStats.getStat(CharStats.SAVE_COLD));
    		affectedStats.setStat(CharStats.SAVE_ACID,affectedStats.getStat(CharStats.SAVE_ACID)+adjCharStats.getStat(CharStats.SAVE_ACID));
    		affectedStats.setStat(CharStats.SAVE_WATER,affectedStats.getStat(CharStats.SAVE_WATER)+adjCharStats.getStat(CharStats.SAVE_WATER));
    		affectedStats.setStat(CharStats.SAVE_UNDEAD,affectedStats.getStat(CharStats.SAVE_DISEASE)+adjCharStats.getStat(CharStats.SAVE_DISEASE));
    		affectedStats.setStat(CharStats.SAVE_DISEASE,affectedStats.getStat(CharStats.SAVE_UNDEAD)+adjCharStats.getStat(CharStats.SAVE_UNDEAD));
    		affectedStats.setStat(CharStats.SAVE_POISON,affectedStats.getStat(CharStats.SAVE_POISON)+adjCharStats.getStat(CharStats.SAVE_POISON));
    		affectedStats.setStat(CharStats.SAVE_PARALYSIS,affectedStats.getStat(CharStats.SAVE_PARALYSIS)+adjCharStats.getStat(CharStats.SAVE_PARALYSIS));
        }
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if((msg.amITarget(mob))&&(mob.location()!=null))
		{
			if((msg.value()<=0)&&(!Prop_HaveResister.isOk(msg,this,mob,mask)))
				return false;
			Prop_HaveResister.resistAffect(msg,mob,this,mask);
		}
		return super.okMessage(myHost,msg);
	}
}
