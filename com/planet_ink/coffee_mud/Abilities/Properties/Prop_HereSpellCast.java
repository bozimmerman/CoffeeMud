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
public class Prop_HereSpellCast extends Property
{
	public String ID() { return "Prop_HereSpellCast"; }
	public String name(){ return "Casting spells when here";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	public boolean bubbleAffect(){return true;}
	private boolean processing=false;
	protected int lastNum=-1;

	protected Vector lastMOBs=new Vector();
	protected Hashtable spellH=null;
	protected Vector spellV=null;
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
			id="Casts "+id+" on those here.";
		return id;
	}

	public void addMeIfNeccessary(MOB newMOB)
	{
		Vector V=getMySpellsV();
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.elementAt(v);
			Ability EA=newMOB.fetchEffect(A.ID());
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
				A.invoke(newMOB,V2,newMOB,true,0);
				EA=newMOB.fetchEffect(A.ID());
			}
			if(EA!=null)
				EA.makeLongLasting();
		}
	}

	public void process(MOB mob, Room room, int code) // code=0 add/sub, 1=addon, 2=subon
	{
		if((code==2)||((code==0)&&(lastNum!=room.numInhabitants())))
		{
			Hashtable h=getMySpellsH();
			for(int v=lastMOBs.size()-1;v>=0;v--)
			{
				MOB lastMOB=(MOB)lastMOBs.elementAt(v);
				if((lastMOB.location()!=room)
				||((mob==lastMOB)&&(code==2)))
				{
					int x=0;
					while(x<lastMOB.numEffects())
					{
						Ability thisAffect=lastMOB.fetchEffect(x);
						if((thisAffect!=null)
						&&(h.containsKey(thisAffect.ID())
						&&(thisAffect.invoker()==lastMOB)))
						{
							thisAffect.unInvoke();
							x=-1;
						}
						x++;
					}
					lastMOBs.removeElementAt(v);
				}
			}
			lastNum=room.numInhabitants();
		}
		if((!lastMOBs.contains(mob))
		&&((code==1)||((code==0)&&(room.isInhabitant(mob)))))
		{
			addMeIfNeccessary(mob);
			lastMOBs.addElement(mob);
		}
	}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(processing) return;
		if((((msg.targetMinor()==CMMsg.TYP_ENTER)&&(msg.target()==affected))
			||((msg.targetMinor()==CMMsg.TYP_RECALL)&&(msg.target()==affected)))
		&&(affected instanceof Room))
			process(msg.source(),(Room)affected,1);
		else
		if((((msg.targetMinor()==CMMsg.TYP_LEAVE)&&(msg.target()==affected))
			||((msg.targetMinor()==CMMsg.TYP_RECALL)&&(msg.target()!=affected)))
		&&(affected instanceof Room))
			process(msg.source(),(Room)affected,2);
	}
	
	public void affectEnvStats(Environmental affectedMOB, EnvStats affectableStats)
	{
		if(processing) return;
		processing=true;
		if((affectedMOB instanceof MOB)&&(affected instanceof Room))
			process((MOB)affectedMOB, (Room)affected,0);
		super.affectEnvStats(affectedMOB,affectableStats);
		processing=false;
	}
}
