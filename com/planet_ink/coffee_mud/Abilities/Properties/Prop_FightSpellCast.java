package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_FightSpellCast extends Property
{
	private boolean processing=false;
	public String ID() { return "Prop_FightSpellCast"; }
	public String name(){ return "Casting spells when properly used during combat";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	public Environmental newInstance(){	Prop_FightSpellCast BOB=new Prop_FightSpellCast();	BOB.setMiscText(text()); return BOB;}
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


	public void addMeIfNeccessary(MOB sourceMOB, MOB newMOB)
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
				A.invoke(sourceMOB,V2,newMOB,true);
			}
		}
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
			id="Casts "+id+" during combat.";
		return id;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(processing) return;
		processing=true;

		if(affected==null) return;

		Item myItem=(Item)affected;

		if((myItem!=null)
		&&(!myItem.amWearingAt(Item.INVENTORY))
		&&(myItem.owner()!=null)
		&&(myItem.owner() instanceof MOB)
		&&(msg.target()!=null)
		&&(msg.target() instanceof MOB))
		{
			MOB mob=(MOB)myItem.owner();
			if((mob.isInCombat())
			&&(mob.location()!=null)
			&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&((msg.value())>0)
			&&(!mob.amDead()))
			{
				if((myItem instanceof Weapon)
				&&(msg.tool()==myItem)
				&&(myItem.amWearingAt(Item.WIELD))
				&&(msg.amISource(mob)))
					addMeIfNeccessary(msg.source(),(MOB)msg.target());
				else
				if((msg.amITarget(mob))
				&&(!myItem.amWearingAt(Item.WIELD))
				&&(!(myItem instanceof Weapon)))
					addMeIfNeccessary((MOB)msg.target(),(MOB)msg.target());
			}
		}
		processing=false;
	}
}