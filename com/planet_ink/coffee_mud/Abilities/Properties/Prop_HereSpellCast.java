package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_HereSpellCast extends Property
{
	public String ID() { return "Prop_HereSpellCast"; }
	public String name(){ return "Casting spells when here";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
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
				A.invoke(newMOB,V2,newMOB,true);
				EA=newMOB.fetchEffect(A.ID());
			}
			if(EA!=null)
				EA.makeLongLasting();
		}
	}

	public void affectEnvStats(Environmental affectedMOB, EnvStats affectableStats)
	{
		if(processing) return;
		processing=true;
		if((affectedMOB instanceof MOB)&&(affected instanceof Room))
		{
			MOB mob=(MOB)affectedMOB;
			Room room=(Room)affected;
			if(lastNum!=room.numInhabitants())
			{
				Hashtable h=getMySpellsH();
				for(int v=lastMOBs.size()-1;v>=0;v--)
				{
					MOB lastMOB=(MOB)lastMOBs.elementAt(v);
					if(lastMOB.location()!=affected)
					{
						int x=0;
						while(x<lastMOB.numEffects())
						{
							Ability thisAffect=lastMOB.fetchEffect(x);
							if(thisAffect!=null)
							{
								String ID=(String)h.get(thisAffect.ID());
								if((ID!=null)&&(thisAffect.invoker()==lastMOB))
								{
									thisAffect.unInvoke();
									x=-1;
								}
							}
							x++;
						}
						lastMOBs.removeElementAt(v);
					}
				}
				lastNum=room.numInhabitants();
			}
			if(!lastMOBs.contains(mob))
			{
				addMeIfNeccessary(mob);
				lastMOBs.addElement(mob);
			}
		}
		super.affectEnvStats(affectedMOB,affectableStats);
		processing=false;
	}
}