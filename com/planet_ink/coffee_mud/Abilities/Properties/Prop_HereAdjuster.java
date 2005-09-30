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
public class Prop_HereAdjuster extends Property
{
	public String ID() { return "Prop_HereAdjuster"; }
	public String name(){ return "Adjustments to stats when here";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	public boolean bubbleAffect(){return true;}
	private CharStats adjCharStats=null;
	private CharState adjCharState=null;
	private EnvStats  adjEnvStats=null;
	boolean gotClass=false;
	boolean gotRace=false;
	boolean gotSex=false;
    private Vector mask=new Vector();


	public String accountForYourself()
	{
		return Prop_HaveAdjuster.fixAccoutingsWithMask("Affects on those here: "+text());
	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		this.adjCharStats=new DefaultCharStats();
		this.adjEnvStats=new DefaultEnvStats();
		this.adjCharState=new DefaultCharState();
        this.mask=new Vector();
		int gotit=Prop_HaveAdjuster.setAdjustments(newText,adjEnvStats,adjCharStats,adjCharState,mask);
		gotClass=((gotit&1)==1);
		gotRace=((gotit&2)==2);
		gotSex=((gotit&4)==4);
	}

	private void ensureStarted()
	{
		if(adjCharStats==null)
			setMiscText(text());
	}
	public void affectEnvStats(Environmental affectedMOB, EnvStats affectableStats)
	{
		ensureStarted();
		if((affectedMOB instanceof MOB)
		&&(((MOB)affectedMOB).location()==affected)
        &&((mask.size()==0)||(MUDZapper.zapperCheckReal(mask,affectedMOB))))
			Prop_HaveAdjuster.envStuff(affectableStats,adjEnvStats);
		super.affectEnvStats(affectedMOB,affectableStats);
	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		ensureStarted();
		if((affectedMOB.location()==affected)
        &&((mask.size()==0)||(MUDZapper.zapperCheckReal(mask,affectedMOB))))
			Prop_HaveAdjuster.adjCharStats(affectedStats,gotClass,gotRace,gotSex,adjCharStats);
		super.affectCharStats(affectedMOB,affectedStats);
	}
	public void affectCharState(MOB affectedMOB, CharState affectedState)
	{
		ensureStarted();
		if((affectedMOB.location()==affected)
        &&((mask.size()==0)||(MUDZapper.zapperCheckReal(mask,affectedMOB))))
			Prop_HaveAdjuster.adjCharState(affectedState,adjCharState);
		super.affectCharState(affectedMOB,affectedState);
	}
}
