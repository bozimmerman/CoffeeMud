package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Prop_RideEnabler extends Property
{
	public String ID() { return "Prop_RideEnabler"; }
	public String name(){ return "Granting skills when ridden";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_MOBS;}
	private Vector lastRiders=new Vector();
	boolean processing=false;
	protected Vector spellV=null;
	public Vector getMySpellsV()
	{
		if(spellV!=null) return spellV;
		spellV=Prop_SpellAdder.getMySpellsV(this);
		return spellV;
	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		spellV=null;
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
			id="Grants "+id+" to those mounted.";
		return id;
	}

	public void addMeIfNeccessary(MOB newMOB)
	{
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
				A.setProfficiency(proff);
				newMOB.addAbility(A);
				A.setBorrowed(newMOB,true);
			}
		}
		if(!lastRiders.contains(newMOB))	lastRiders.addElement(newMOB);
	}

	public void removeMyAffectsFromRider(MOB E)
	{
		Vector V=getMySpellsV();
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.elementAt(v);
			E.delAbility(A);
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
