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
public class Prop_RideSpellCast extends Property
{
	public String ID() { return "Prop_RideSpellCast"; }
	public String name(){ return "Casting spells when ridden";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_MOBS;}
	private Vector lastRiders=new Vector();
	private boolean processing=false;

	protected Hashtable spellH=null;
	protected Vector spellV=null;
    private Vector mask=new Vector();

	public Vector getMySpellsV()
	{
		if(spellV!=null) return spellV;
		spellV=Prop_SpellAdder.getMySpellsV(this);
		return spellV;
	}
	public Hashtable getMySpellsH()
	{
		if(spellH!=null) return spellH;
		spellH=Prop_SpellAdder.getMySpellsH(this);
		return spellH;
	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		spellV=null;
		spellH=null;
        mask.clear();
        Prop_HaveAdjuster.buildMask(newText,mask);
	}


	public String accountForYourself()
	{
		String id="";
		Vector V=getMySpellsV();
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.elementAt(v);
			if(V.size()==1)
				id+=A.name();
			else
			if(v==(V.size()-1))
				id+="and "+A.name();
			else
				id+=A.name()+", ";
		}
		if(V.size()>0)
			id="Casts "+id+" on the mounted.";
		return id;
	}

	public void addMeIfNeccessary(MOB E)
	{
		Vector V=getMySpellsV();
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.elementAt(v);
			Ability EA=E.fetchEffect(A.ID());
			if(EA==null)
			{
				String t=A.text();
				A=(Ability)A.copyOf();
				Vector V2=new Vector();
				if(t.length()>0)
				{
					int x=t.indexOf("/");
					if(x<0)
					{
						V2=Util.parse(t);
						A.setMiscText("");
					}
					else
					{
						V2=Util.parse(t.substring(0,x));
						A.setMiscText(t.substring(x+1));
					}
				}
                if((mask.size()==0)||(MUDZapper.zapperCheckReal(mask,E)))
    				A.invoke(E,V2,E,true,(affected!=null)?affected.envStats().level():0);
				EA=E.fetchEffect(A.ID());
			}
			if(EA!=null)
				EA.makeLongLasting();
		}
		if(!lastRiders.contains(E))	lastRiders.addElement(E);
	}

	public void removeMyAffectsFromRider(MOB E)
	{
		Hashtable h=getMySpellsH();
		int x=0;
		while(x<E.numEffects())
		{
			Ability thisAffect=E.fetchEffect(x);
			if(thisAffect!=null)
			{
				String ID=(String)h.get(thisAffect.ID());
				if((ID!=null)&&(thisAffect.invoker()==E))
				{
					thisAffect.unInvoke();
					x=-1;
				}
			}
			x++;
		}
		while(lastRiders.contains(E))
			lastRiders.removeElement(E);
	}

	public void affectEnvStats(Environmental affectedMOB, EnvStats affectableStats)
	{
		if(processing) return;
		processing=true;
		if(affected instanceof Rideable)
		{
			Rideable RI=(Rideable)affected;
			for(int r=0;r<RI.numRiders();r++)
			{
				Rider R=RI.fetchRider(r);
				if(R instanceof MOB)
				{
					MOB M=(MOB)R;
					if((!lastRiders.contains(M))&&(RI.amRiding(M)))
						addMeIfNeccessary(M);
				}
			}
			for(int i=lastRiders.size()-1;i>=0;i--)
			{
				MOB M=(MOB)lastRiders.elementAt(i);
				if(!RI.amRiding(M))
					removeMyAffectsFromRider(M);
			}
		}
		super.affectEnvStats(affectedMOB,affectableStats);
		processing=false;
	}
}
