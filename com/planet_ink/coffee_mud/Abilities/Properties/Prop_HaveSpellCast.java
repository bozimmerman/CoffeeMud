package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_HaveSpellCast extends Property
{
	public String ID() { return "Prop_HaveSpellCast"; }
	public String name(){ return "Casting spells when owned";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	private Item myItem=null;
	private MOB lastMOB=null;
	boolean processing=false;
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
			id="Casts "+id+" on the owner.";
		return id;
	}

	public void addMeIfNeccessary(MOB newMOB)
	{
		Vector V=getMySpellsV();
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.elementAt(v);
			Ability EA=newMOB.fetchEffect(A.ID());
			if((EA==null)&&(Prop_SpellAdder.didHappen(100,this)))
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
		lastMOB=newMOB;
	}

	public void removeMyAffectsFromLastMob()
	{
		Hashtable h=getMySpellsH();
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
		lastMOB=null;
	}

	public void affectEnvStats(Environmental affectedMOB, EnvStats affectableStats)
	{
		if(processing) return;
		processing=true;
		if((affectedMOB!=null)&&(affectedMOB instanceof Item))
		{
			myItem=(Item)affectedMOB;

			if((lastMOB!=null)
			&&((myItem.owner()!=lastMOB)||(myItem.amDestroyed()))
			&&(lastMOB.location()!=null))
				removeMyAffectsFromLastMob();
			
			if((lastMOB==null)&&(myItem.owner()!=null)
			&&(myItem.owner() instanceof MOB)&&(((MOB)myItem.owner()).location()!=null))
				addMeIfNeccessary((MOB)myItem.owner());
		}
		super.affectEnvStats(affectedMOB,affectableStats);
		processing=false;
	}
}