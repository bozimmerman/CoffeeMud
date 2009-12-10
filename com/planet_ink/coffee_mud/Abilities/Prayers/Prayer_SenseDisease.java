package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Prayer_SenseDisease extends Prayer
{
	public String ID() { return "Prayer_SenseDisease"; }
	public String name(){ return "Sense Disease";}
	public String displayText(){ return "(Sense Disease)";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;}
	public int enchantQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	public long flags(){return Ability.FLAG_HOLY;}
	protected Room lastRoom=null;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			lastRoom=null;
			mob.tell("Your disease sensations fade.");
		}
	}

	public Ability getDisease(Environmental mob)
	{
		if(mob instanceof MOB)
		{
			for(int m=0;m<((MOB)mob).numAllEffects();m++)
			{
				Ability A=((MOB)mob).fetchEffect(m);
				if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_DISEASE)
					return A;
			}
		}
		else
		for(int m=0;m<mob.numEffects();m++)
		{
			Ability A=mob.fetchEffect(m);
			if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_DISEASE)
				return A;
		}
		return null;
	}
	private static final Vector empty=new Vector();
	public Vector diseased(MOB mob, Room R)
	{
		if(R==null) return empty;
		Vector V=null;
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if((M!=null)&&(M!=mob)&&(getDisease(M)!=null))
			{
				if(V==null) V=new Vector();
				V.addElement(M);
			}
		}
		for(int i=0;i<R.numItems();i++)
		{
			Item I=R.fetchItem(i);
			if((I!=null)
			&&(I.container()==null)
			&&(getDisease(I)!=null))
			{
				if(V==null) V=new Vector();
				V.addElement(I);
			}
		}
		if(V!=null)
			return V;
		return empty;
	}

	public void messageTo(MOB mob)
	{
		String last="";
		String dirs="";
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			Room R=mob.location().getRoomInDir(d);
			Exit E=mob.location().getExitInDir(d);
			if((R!=null)&&(E!=null)&&(diseased(mob,R).size()>0))
			{
				if(last.length()>0)
					dirs+=", "+last;
				last=Directions.getFromDirectionName(d);
			}
		}
		Vector V=diseased(mob,mob.location());
		if(V.size()>0)
		{
			boolean didSomething=false;
			for(int v=0;v<V.size();v++)
			{
				Environmental E=(Environmental)V.elementAt(v);
				if(CMLib.flags().canBeSeenBy(E,mob))
				{
					didSomething=true;
					if(last.length()>0)
						dirs+=", "+last;
					last=E.name();
				}
			}
			if(!didSomething)
			{
				if(last.length()>0)
					dirs+=", "+last;
				last="here";
			}
		}

		if((dirs.length()==0)&&(last.length()==0))
			mob.tell("You do not sense any disease.");
		else
		if(dirs.length()==0)
			mob.tell("You sense disease coming from "+last+".");
		else
			mob.tell("You sense disease coming from "+dirs.substring(2)+", and "+last+".");
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)
		   &&(affected!=null)
		   &&(affected instanceof MOB)
		   &&(((MOB)affected).location()!=null)
		   &&((lastRoom==null)||(((MOB)affected).location()!=lastRoom)))
		{
			lastRoom=((MOB)affected).location();
			messageTo((MOB)affected);
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Environmental target=mob;
		if((auto)&&(givenTarget!=null)) target=givenTarget;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> attain(s) disease senses!":"^S<S-NAME> listen(s) for a message from "+hisHerDiety(mob)+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> listen(s) to "+hisHerDiety(mob)+" for a message, but there is no answer.");


		// return whether it worked
		return success;
	}
}
