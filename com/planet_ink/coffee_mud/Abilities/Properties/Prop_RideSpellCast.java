package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_RideSpellCast extends Property
{
	public String ID() { return "Prop_RideSpellCast"; }
	public String name(){ return "Casting spells when ridden";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_MOBS;}
	private Vector lastRiders=new Vector();
	private boolean processing=false;

	public Environmental newInstance(){	Prop_RideSpellCast BOB=new Prop_RideSpellCast(); BOB.setMiscText(text()); return BOB;}
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
				A.invoke(E,V2,E,true);
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
			Item myItem=(Item)affectedMOB;
			for(int r=0;r<RI.numRiders();r++)
			{
				Rider R=RI.fetchRider(r);
				if(R instanceof MOB)
				{
					MOB M=(MOB)R;
					if((!lastRiders.contains(M))&&(RI.amRiding(M)))
						addMeIfNeccessary(M);
				}
				for(int i=lastRiders.size()-1;i>=0;i--)
				{
					MOB M=(MOB)lastRiders.elementAt(i);
					if(!RI.amRiding(M))
						removeMyAffectsFromRider(M);
				}
			}
		}
		super.affectEnvStats(affectedMOB,affectableStats);
		processing=false;
	}
}