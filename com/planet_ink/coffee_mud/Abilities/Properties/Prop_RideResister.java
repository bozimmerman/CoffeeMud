package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_RideResister extends Property
{
	public String ID() { return "Prop_RideResister"; }
	public String name(){ return "Resistance due to riding";}
	public boolean bubbleAffect(){return true;}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_MOBS;}
	private CharStats adjCharStats=null;

	public Environmental newInstance(){	Prop_RideResister BOB=new Prop_RideResister();	BOB.setMiscText(text()); return BOB;}

	public String accountForYourself()
	{
		String id="Those mounted gain resistances: "+text();
		return id;
	}

	private void ensureStarted()
	{
		if(adjCharStats==null)
			setMiscText(text());
	}
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		this.adjCharStats=new DefaultCharStats();
		Prop_HaveResister.setAdjustments(this,adjCharStats);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		ensureStarted();
		if((affected !=null)
		&&(affectedMOB instanceof Rider)
		&&(((Rider)affectedMOB).riding()==affected))
			Prop_HaveResister.adjCharStats(affectedStats,adjCharStats);
		super.affectCharStats(affectedMOB,affectedStats);
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;
		if((affected !=null)
		&&(affected instanceof Rideable)
		&&(affect.target()!=null)
		&&(affect.target() instanceof MOB)
		&&(((Rideable)affected).amRiding((MOB)affect.target()))
		&&(!affect.wasModified())
		&&(((MOB)affect.target()).location()!=null))
		{
			MOB mob=(MOB)affect.target();
			if(!Prop_HaveResister.isOk(affect,this,mob))
				return false;
			Prop_HaveResister.resistAffect(affect,mob,this);
		}
		return true;
	}

}