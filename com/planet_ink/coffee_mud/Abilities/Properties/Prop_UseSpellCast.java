package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_UseSpellCast extends Property
{
	private boolean processing=false;
	public String ID() { return "Prop_UseSpellCast"; }
	public String name(){ return "Casting spells when used";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	public Environmental newInstance(){	Prop_UseSpellCast BOB=new Prop_UseSpellCast();	BOB.setMiscText(text()); return BOB;}

	public void addMeIfNeccessary(MOB sourceMOB, MOB newMOB)
	{
		Vector V=Prop_SpellAdder.getMySpellsV(this);
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
		Vector V=Prop_SpellAdder.getMySpellsV(this);
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
			id="Casts "+id+" when used.";
		return id;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);

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
					addMeIfNeccessary(msg.source(),msg.source());
				break;
			case CMMsg.TYP_WEAR:
				if((myItem instanceof Armor)
				  &&(msg.amITarget(myItem)))
					addMeIfNeccessary(msg.source(),msg.source());
				break;
			case CMMsg.TYP_PUT:
				if((myItem instanceof Container)
				  &&(msg.amITarget(myItem)))
					addMeIfNeccessary(msg.source(),msg.source());
				break;
			case CMMsg.TYP_WIELD:
			case CMMsg.TYP_HOLD:
				if((!(myItem instanceof Drink))
				  &&(!(myItem instanceof Armor))
				  &&(!(myItem instanceof Container))
				  &&(msg.amITarget(myItem)))
					addMeIfNeccessary(msg.source(),msg.source());
				break;
			}
		processing=false;
	}
}