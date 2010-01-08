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
public class Prop_WearSpellCast extends Prop_HaveSpellCast
{
	public String ID() { return "Prop_WearSpellCast"; }
	public String name(){ return "Casting spells when worn";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
    public String accountForYourself()
    { return spellAccountingsWithMask("Casts "," on the wearer.");}
    public boolean checked=false;
    public boolean disabled=false;
    public boolean layered=false;

    public void check(MOB mob, Armor A)
    {
    	if(!layered){ checked=true; disabled=false;}
    	boolean oldDisabled=disabled;
    	if(A.amWearingAt(Wearable.IN_INVENTORY))
    	{
    		checked=false;
    		return;
    	}
    	if(checked) return;
    	Item I=null;
    	disabled=false;
    	for(int i=0;i<mob.inventorySize();i++)
    	{
    		I=mob.fetchInventory(i);
    		if((I instanceof Armor)
    		&&(!I.amWearingAt(Wearable.IN_INVENTORY))
    		&&((I.rawWornCode()&A.rawWornCode())>0)
    		&&(I!=A))
    		{
    			disabled=A.getClothingLayer()<=((Armor)I).getClothingLayer();
    			if(disabled)
    			{
    				break;
    			}
    		}
    	}
    	if((!oldDisabled)&&(disabled))
    		this.removeMyAffectsFromLastMOB();
		checked=true;
    }

    public void setMiscText(String newText)
    {
    	super.setMiscText(newText);
        layered=CMParms.parseSemicolons(newText.toUpperCase(),true).indexOf("LAYERED")>=0;
    }
    
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((affected instanceof Armor)&&(msg.source()==((Armor)affected).owner()))
		{
			if((msg.targetMinor()==CMMsg.TYP_REMOVE)
			||(msg.sourceMinor()==CMMsg.TYP_WEAR)
			||(msg.sourceMinor()==CMMsg.TYP_WIELD)
			||(msg.sourceMinor()==CMMsg.TYP_HOLD)
			||(msg.sourceMinor()==CMMsg.TYP_DROP))
				checked=false;
			else
			{
				check(msg.source(),(Armor)affected);
				super.executeMsg(host,msg);
			}
		}
		else
			super.executeMsg(host,msg);
	}
	public boolean addMeIfNeccessary(Environmental source, Environmental target, boolean makeLongLasting, int asLevel)
	{
		if(disabled&&checked) return false;
		return super.addMeIfNeccessary(source,target,makeLongLasting,asLevel);
	}

	public void affectEnvStats(Environmental host, EnvStats affectableStats)
	{
		if(processing) return;
		processing=true;
		if((host!=null)&&(host instanceof Item))
		{
			myItem=(Item)host;

			boolean worn=(!myItem.amWearingAt(Wearable.IN_INVENTORY))
			&&((!myItem.amWearingAt(Wearable.WORN_FLOATING_NEARBY))||(myItem.fitsOn(Wearable.WORN_FLOATING_NEARBY)));
			
			if((lastMOB instanceof MOB)
			&&(((MOB)lastMOB).location()!=null)
			&&((myItem.owner()!=lastMOB)||(!worn)))
				removeMyAffectsFromLastMOB();

			if((lastMOB==null)
			&&(worn)
			&&(myItem.owner()!=null)
			&&(myItem.owner() instanceof MOB)
			&&(((MOB)myItem.owner()).location()!=null))
			{
				if(myItem instanceof Armor)
					check((MOB)myItem.owner(),((Armor)myItem));
				addMeIfNeccessary(myItem.owner(),myItem.owner(),true,0);
			}
		}
		processing=false;
	}
}
