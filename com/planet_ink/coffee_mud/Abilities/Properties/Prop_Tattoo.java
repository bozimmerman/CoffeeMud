package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_Tattoo extends Property
{
	public String ID() { return "Prop_Tattoo"; }
	public String name(){ return "A Tattoo";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	Prop_Tattoo BOB=new Prop_Tattoo();	BOB.setMiscText(text());return BOB;}
	
	public static Vector getTattoos(MOB mob)
	{
		Vector tattos=new Vector();
		Ability A=mob.fetchAbility("Prop_Tattoo");
		if(A!=null)
			tattos=Util.parseSemicolons(A.text().toUpperCase());
		else
		{
			A=mob.fetchAffect("Prop_Tattoo");
			if(A!=null)
				tattos=Util.parseSemicolons(A.text().toUpperCase());
		}
		return tattos;
	}
			

	public void affect(Environmental myHost, Affect affect)
	{
		/*if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;

			if((affect.amITarget(mob))
			   &&(affect.targetMinor()==Affect.TYP_EXAMINESOMETHING)
			   &&(text().length()>0))
			{
				Vector V=getTattoos(affect.source());
				String tattoos="";
				if(V.size()==1)
				   tattoos=(String)V.elementAt(0);
				else
				for(int v=0;v<V.size();v++)
					if(v==0)
						tattoos+=(String)V.elementAt(v);
					else
					if(v==(V.size()-1))
					   tattoos+=", and "+(String)V.elementAt(v);
					else
					   tattoos+=", "+(String)V.elementAt(v);
				if(tattoos.length()>0)
					affect.addTrailerMsg(new FullMsg(affect.source(),mob,null,Affect.MSG_OK_VISUAL,"<T-NAME> has the following tattoos: "+tattoos.toLowerCase(),Affect.NO_EFFECT,null,Affect.NO_EFFECT,null));
			}
		}*/
		super.affect(myHost,affect);
	}
}