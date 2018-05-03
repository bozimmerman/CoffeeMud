package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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

public class Prop_HaveEnabler extends Prop_SpellAdder
{
	@Override
	public String ID()
	{
		return "Prop_HaveEnabler";
	}

	@Override
	public String name()
	{
		return "Granting skills when owned";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	protected Item				myItem			= null;
	protected List<String>		lastMOBeffects	= new SVector<String>();
	protected boolean			processing2		= false;
	protected volatile boolean	clearedYet		= false;

	@Override
	public long flags()
	{
		return Ability.FLAG_ENABLER;
	}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_GET;
	}

	@Override
	public String accountForYourself()
	{
		return spellAccountingsWithMask("Grants ", " to the owner.");
	}

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		lastMOBeffects=new Vector<String>();
	}

	public boolean addMeIfNeccessary(Environmental source, Environmental target, short maxTicks)
	{
		if((!(target instanceof MOB))
		||((compiledMask!=null)&&(!CMLib.masking().maskCheck(compiledMask,target,true))))
			return false;
		final MOB newMOB=(MOB)target;
		final List<Ability> allAbles=getMySpellsV();
		int proff=100;
		int x=text().indexOf('%');
		if(x>0)
		{
			int mul=1;
			int tot=0;
			while((--x)>=0)
			{
				if(Character.isDigit(text().charAt(x)))
					tot+=CMath.s_int(""+text().charAt(x))*mul;
				else
					x=-1;
				mul=mul*10;
			}
			proff=tot;
		}
		for(int v=0;v<allAbles.size();v++)
		{
			final Ability A=allAbles.get(v);
			if(newMOB.fetchAbility(A.ID())==null)
			{
				final String t=A.text();
				if(t.length()>0)
				{
					x=t.indexOf('/');
					if(x<0)
						A.setMiscText("");
					else
						A.setMiscText(t.substring(x+1));
				}
				final Ability A2=newMOB.fetchEffect(A.ID());
				A.setProficiency(proff);
				newMOB.addAbility(A);
				A.setSavable(false);
				A.autoInvocation(newMOB, false);
				if(!clearedYet)
				{
					lastMOBeffects.clear();
					clearedYet=true;
				}
				if((A2==null)
				&&(!lastMOBeffects.contains(A.ID())))
					lastMOBeffects.add(A.ID());
			}
		}
		lastMOB=newMOB;
		return true;
	}

	@Override
	public void removeMyAffectsFrom(Physical P)
	{
		if(!(P instanceof MOB))
			return;
		final List<Ability> V=getMySpellsV();
		final Set<String> removedAbles = new HashSet<String>();
		for(int v=0;v<V.size();v++)
		{
			final Ability A=V.get(v);
			if((!A.isSavable())&&(((MOB)P).isMine(A)))
			{
				removedAbles.add(A.ID());
				((MOB)P).delAbility(A);
			}
		}
		if(P==lastMOB)
		{
			if(removedAbles.size()>0)
			{
				for(final Iterator<String> e=lastMOBeffects.iterator();e.hasNext();)
				{
					final String AID=e.next();
					final Ability A2=lastMOB.fetchEffect(AID);
					if((A2!=null)&&(removedAbles.contains(A2.ID())))
					{
						A2.unInvoke();
						lastMOB.delEffect(A2);
					}
				}
			}
			lastMOBeffects.clear();
		}
	}

	public void removeMyAffectsFromLastMob()
	{
		if(!(lastMOB instanceof MOB))
			return;
		removeMyAffectsFrom(lastMOB);
		lastMOB=null;
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
	}

	@Override
	public void affectPhyStats(Physical host, PhyStats affectableStats)
	{
		if(processing)
			return;
		processing=true;
		if(host instanceof Item)
		{
			myItem=(Item)host;

			if((lastMOB instanceof MOB)
			&&((myItem.owner()!=lastMOB)||(myItem.amDestroyed()))
			&&(((MOB)lastMOB).location()!=null))
				removeMyAffectsFromLastMob();

			if((lastMOB==null)
			&&(myItem.owner()!=null)
			&&(myItem.owner() instanceof MOB)
			&&(((MOB)myItem.owner()).location()!=null))
				addMeIfNeccessary(myItem.owner(),myItem.owner(),maxTicks);
		}
		processing=false;
	}
}
