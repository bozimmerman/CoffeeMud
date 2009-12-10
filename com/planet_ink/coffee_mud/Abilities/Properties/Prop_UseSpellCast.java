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
@SuppressWarnings("unchecked")
public class Prop_UseSpellCast extends Prop_SpellAdder
{
	public String ID() { return "Prop_UseSpellCast"; }
	public String name(){ return "Casting spells when used";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
    
    public boolean addMeIfNeccessary(Environmental source, Environmental target, int asLevel)
    {
        Vector V=getMySpellsV();
        if((target==null)
        ||(V.size()==0)
        ||((compiledMask!=null))&&(compiledMask.size()>0)&&(!CMLib.masking().maskCheck(compiledMask,target,true)))
            return false;
        
        MOB qualMOB=getInvokerMOB(source,target);
        
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.elementAt(v);
			Ability EA=target.fetchEffect(A.ID());
			if((EA==null)&&(didHappen(100)))
			{
				String t=A.text();
				A=(Ability)A.copyOf();
				Vector V2=new Vector();
				if(t.length()>0)
				{
					int x=t.indexOf("/");
					if(x<0)
					{
						V2=CMParms.parse(t);
						A.setMiscText("");
					}
					else
					{
						V2=CMParms.parse(t.substring(0,x));
						A.setMiscText(t.substring(x+1));
					}
				}
				A.invoke(qualMOB,V2,target,true,asLevel>0?asLevel:((affected!=null)?affected.envStats().level():0));
			}
		}
        return true;
	}

    public String accountForYourself()
    { return spellAccountingsWithMask("Casts "," when used.");}

    public void affectEnvStats(Environmental host, EnvStats affectableStats)
    {}
    
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(processing) return;
		processing=true;

		if(affected==null) return;
		Item myItem=(Item)affected;
		if(myItem.owner()==null) return;
		if(!(myItem.owner() instanceof MOB)) return;
		if(msg.amISource((MOB)myItem.owner()))
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_FILL:
				if((myItem instanceof Drink)
				&&(msg.tool()!=myItem)
				&&(msg.amITarget(myItem)))
					addMeIfNeccessary(msg.source(),msg.source(),0);
				break;
			case CMMsg.TYP_WEAR:
				if((myItem instanceof Armor)
				  &&(msg.amITarget(myItem)))
					addMeIfNeccessary(msg.source(),msg.source(),0);
				break;
			case CMMsg.TYP_PUT:
				if((myItem instanceof Container)
				  &&(msg.amITarget(myItem)))
					addMeIfNeccessary(msg.source(),msg.source(),0);
				break;
			case CMMsg.TYP_WIELD:
			case CMMsg.TYP_HOLD:
				if((!(myItem instanceof Drink))
				  &&(!(myItem instanceof Armor))
				  &&(!(myItem instanceof Container))
				  &&(msg.amITarget(myItem)))
					addMeIfNeccessary(msg.source(),msg.source(),0);
				break;
			}
		processing=false;
	}
}
