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
public class Prop_HaveEnabler extends Prop_SpellAdder
{
	public String ID() { return "Prop_HaveEnabler"; }
	public String name(){ return "Granting skills when owned";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
    protected Item myItem=null;
    protected Vector lastMOBeffected=new Vector();
    protected boolean processing2=false;
    
    public String accountForYourself()
    { return spellAccountingsWithMask("Grants "," to the owner.");}

    public void setMiscText(String newText)
    {
        super.setMiscText(newText);
        lastMOBeffected=new Vector();
    }
	public boolean addMeIfNeccessary(Environmental source, Environmental target)
	{
        if((!(target instanceof MOB))
        ||((mask.size()>0)&&(!MUDZapper.zapperCheckReal(mask,target))))
            return false;
        MOB newMOB=(MOB)target;
		Vector V=getMySpellsV();
		int proff=100;
		int x=text().indexOf("%");
		if(x>0)
		{
			int mul=1;
			int tot=0;
			while((--x)>=0)
			{
				if(Character.isDigit(text().charAt(x)))
					tot+=Util.s_int(""+text().charAt(x))*mul;
				else
					x=-1;
				mul=mul*10;
			}
			proff=tot;
		}
        boolean clearedYet=false;
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.elementAt(v);
			if(newMOB.fetchAbility(A.ID())==null)
			{
				String t=A.text();
				if(t.length()>0)
				{
					x=t.indexOf("/");
					if(x<0)
						A.setMiscText("");
					else
						A.setMiscText(t.substring(x+1));
				}
                Ability A2=newMOB.fetchEffect(A.ID());
				A.setProfficiency(proff);
				newMOB.addAbility(A);
				A.setBorrowed(newMOB,true);
                A.autoInvocation(newMOB);
                if(!clearedYet)
                {
                    lastMOBeffected.clear();    
                    clearedYet=true;
                }
                if((A2==null)&&(!lastMOBeffected.contains(A.ID()))) 
                    lastMOBeffected.addElement(A.ID());
			}
		}
		lastMOB=newMOB;
        return true;
	}

    public void removeMyAffectsFrom(Environmental E)
    {
        if(!(E instanceof MOB))
            return;
        Vector V=getMySpellsV();
        for(int v=0;v<V.size();v++)
        {
            Ability A=(Ability)V.elementAt(v);
            ((MOB)E).delAbility(A);
        }
        if(E==lastMOB)
        {
            for(Iterator e=lastMOBeffected.iterator();e.hasNext();)
            {
                String AID=(String)e.next();
                Ability A2=lastMOB.fetchEffect(AID);
                if(A2!=null)
                {
                    A2.unInvoke();
                    A2.delEffect(A2);
                }
            }
            lastMOBeffected.clear();
        }
    }
    
	public void removeMyAffectsFromLastMob()
	{
        if(!(lastMOB instanceof MOB))
            return;
        removeMyAffectsFrom(lastMOB);
		lastMOB=null;
	}

    public void recoverEnvStats()
    {
        if(processing2) return;
        processing2=true;
        if((affected instanceof Item)
        &&(lastMOB instanceof MOB)
        &&((((Item)affected).owner()!=lastMOB)||(((Item)affected).amDestroyed()))
        &&(((MOB)lastMOB).location()!=null))
            removeMyAffectsFromLastMob();
        processing2=false;
    }
    
    public void executeMsg(Environmental host, CMMsg msg)
    {}
    
	public void affectEnvStats(Environmental host, EnvStats affectableStats)
	{
		if(processing) return;
		processing=true;
		if(host instanceof Item)
		{
			myItem=(Item)host;

			if((lastMOB instanceof MOB)
			&&((myItem.owner()!=lastMOB)||(myItem.amDestroyed()))
			&&(((MOB)lastMOB).location()!=null))
				removeMyAffectsFromLastMob();
			
			if((lastMOB==null)
            &&(myItem.owner()!=null)
			&&(myItem.owner() instanceof MOB)
            &&(((MOB)myItem.owner()).location()!=null))
				addMeIfNeccessary(myItem.owner(),myItem.owner());
		}
		processing=false;
	}
}
