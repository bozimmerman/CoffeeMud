package com.planet_ink.coffee_mud.Abilities.Tech;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CommonSkill;
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
   Copyright 2013-2014 Bo Zimmerman

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

@SuppressWarnings("rawtypes")
public class AstroEngineering extends TechSkill
{
	public String ID() { return "AstroEngineering"; }
	public String name(){ return "Astro Engineering";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected  int canTargetCode(){return CAN_ITEMS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"ASTROENGINEER","ASTROENGINEERING","ENGINEER","AE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT;}
	
	protected volatile int baseTickSpan = Integer.MAX_VALUE;
	protected volatile boolean aborted = false;
	protected volatile boolean failure = false;
	protected volatile Item targetItem = null;
	protected volatile Item targetPanel = null;
	protected volatile Room targetRoom  = null;
	protected volatile Operation op = Operation.REPAIR;
	protected volatile String altverb="";
	
	protected static enum Operation 
	{ 
		INSTALL("installing"),
		REPAIR("repairing"),
		ENHANCE("enhancing");
		public String verb="";
		private Operation(String verb)
		{
			this.verb=verb;
		}
	}

	/**
	 * Here's the scoop: Send the manufacturer and item type, and look at a expertise list
	 * for the level of expertise in each.  The sum is 0-50 normally, 25 for
	 * each. Every time it comes up, there is a chance of expertise rating going up in 
	 * that manufacturer and item type, and if less than 25, pulling down the oldest used
	 * manufacturer or item type as knowledge fades.  Makes it tough to get to full knowledge
	 * in everything.  Make sure undefined expertise names end with "%" so they are displayed 
	 * right in the expertise command. 
	 * @param mob
	 * @param item
	 * @return
	 */
	public int getBonus(MOB mob, Electronics item, int multiplyBy)
	{
		if((mob==null)||(item==null))
			return 0;
		double score = 0.0;
		Manufacturer m=item.getFinalManufacturer();
		if(m==null)
			score+=0.5;
		else
		{
			Pair<String,Integer> manuExpertise=mob.fetchExpertise(m.name()+"%");
			if(manuExpertise!=null)
				score += (0.5 * CMath.div(manuExpertise.second.intValue(), 100.0));
		}
		Technical.TechType ttype = item.getTechType();
		if(ttype==null)
			score+=0.5;
		else
		{
			Pair<String,Integer> techTypeExpertise=mob.fetchExpertise(ttype.getDisplayName()+"%");
			if(techTypeExpertise!=null)
				score += (0.5 * CMath.div(techTypeExpertise.second.intValue(), 100.0));
		}
		return (int)Math.round(CMath.mul(multiplyBy, score));
	}
	
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected instanceof MOB)
			&&(((MOB)affected).location()!=null))
			{
				MOB mob=(MOB)affected;
				Room room=mob.location();
				if((aborted)||(room==null))
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> stop(s) "+op.verb+".");
				else
				if(failure)
				{
					mob.location().show(mob,targetItem,CMMsg.MSG_OK_VISUAL,"<S-NAME> mess(es) up "+op.verb+" <T-NAME>.");
				}
				else
				if(op==Operation.INSTALL)
				{
					CMMsg msg=CMClass.getMsg(mob,targetPanel,targetItem,CMMsg.MSG_INSTALL,"<S-NAME> install(s) <T-NAME> into <O-NAME>.");
					msg.setValue(50+getBonus(mob,(Electronics)targetItem,50));
					if(room.okMessage(msg.source(), msg))
						room.send(msg.source(), msg);
				}
				else
				if(op==Operation.REPAIR)
				{
					CMMsg msg=CMClass.getMsg(mob,targetItem,this,CMMsg.MSG_REPAIR,"<S-NAME> repair(s) <T-NAME>.");
					msg.setValue(CMLib.dice().roll(1, proficiency()/2, getBonus(mob,(Electronics)targetItem,50)));
					if(room.okMessage(msg.source(), msg))
						room.send(msg.source(), msg);
				}
				else
				{
					CMMsg msg=CMClass.getMsg(mob,targetItem,this,CMMsg.MSG_ENHANCE,"<S-NAME> enhance(s) <T-NAME>.");
					msg.setValue((proficiency()/2)+getBonus(mob,(Electronics)targetItem,50));
					if(room.okMessage(msg.source(), msg))
						room.send(msg.source(), msg);
				}
			}
			aborted = false;
			targetItem = null;
			targetPanel = null;
			targetRoom = null;
		}
		super.unInvoke();
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
			if((mob.isInCombat())
			||(mob.location()!=targetRoom)
			||(!CMLib.flags().aliveAwakeMobileUnbound(mob,true)))
			{
				aborted=true; 
				unInvoke(); 
				return false;
			}
			if(tickDown==4)
				mob.location().show(mob,targetItem,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> <S-IS-ARE> almost done "+op.verb+" <T-NAME>.");
			else
			if(((baseTickSpan-tickDown)%4)==0)
			{
				int total=baseTickSpan;
				int tickUp=(baseTickSpan-tickDown);
				int pct=(int)Math.round(CMath.div(tickUp,total)*100.0);
				String verb=op.verb;
				if(op==Operation.ENHANCE)
					verb=altverb;
				mob.location().show(mob,targetItem,this,CMMsg.MSG_NOISYMOVEMENT,
						"<S-NAME> continue(s) "+verb+" <T-NAME> ("+pct+"% completed).",
						null,"<S-NAME> continue(s) "+verb+" <T-NAME>.");
			}
			if((mob.soulMate()==null)&&(mob.playerStats()!=null)&&(mob.location()!=null))
				mob.playerStats().adjHygiene(PlayerStats.HYGIENE_COMMONDIRTY);
		}
		
		if(!super.tick(ticking,tickID))
			return false;
		return true;
	}
	
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((myHost instanceof MOB)&&(myHost == this.affected)&&(((MOB)myHost).location()!=null))
		{
			if((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
			||((msg.sourceMinor()==CMMsg.TYP_QUIT)&&(msg.amISource((MOB)myHost))))
			{
				aborted=true;
				unInvoke();
			}
		}
		return true;
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell("What would you like to install, repair, or enhance?");
			return false;
		}
		aborted = false;
		failure = false;
		targetItem = null;
		targetPanel = null;
		targetRoom = mob.location();
		op=Operation.REPAIR;
		if(commands.firstElement() instanceof String)
		{
			if(((String)commands.firstElement()).equalsIgnoreCase("INSTALL"))
			{
				op=Operation.INSTALL;
				commands.remove(0);
				if(givenTarget instanceof Item)
				{
					targetPanel=(Item)givenTarget;
				}
				else
				if(commands.size()<2)
				{
					mob.tell("You need to specify an item to install and a panel to install it into.");
					return false;
				}
				else
				{
					String panelName=(String)commands.lastElement();
					targetPanel=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,panelName);
					if(targetPanel==null)
					{
						targetPanel=targetRoom.findItem(null,panelName);
					}
					if((targetPanel==null)||(!CMLib.flags().canBeSeenBy(targetPanel,mob)))
					{
						mob.tell("You don't see '"+panelName+"' here.");
						return false;
					}
					if(!(targetPanel instanceof Electronics.ElecPanel))
					{
						mob.tell("That's not an electronics panel.");
						return false;
					}
					commands.remove(commands.size()-1);
				}
			}
			else
			if(((String)commands.firstElement()).equalsIgnoreCase("REPAIR"))
			{
				op=Operation.REPAIR;
				commands.remove(0);
			}
			else
			if(((String)commands.firstElement()).equalsIgnoreCase("ENHANCE"))
			{
				op=Operation.ENHANCE;
				final String[] verbs=CMProps.getListFileStringList(CMProps.ListFile.TECH_BABBLE_VERBS);
				final String[] adjs1=CMProps.getListFileStringList(CMProps.ListFile.TECH_BABBLE_ADJ1);
				final String[] adjs2=CMProps.getListFileStringList(CMProps.ListFile.TECH_BABBLE_ADJ2);
				final String[] adjs12=new String[adjs1.length+adjs2.length];
				System.arraycopy(adjs1, 0, adjs12, 9, adjs1.length);
				System.arraycopy(adjs2, 0, adjs12, adjs1.length, adjs2.length);
				final String[] nouns=CMProps.getListFileStringList(CMProps.ListFile.TECH_BABBLE_NOUN);
				altverb=verbs[CMLib.dice().roll(1, verbs.length, -1)].trim()+" "
						+adjs12[CMLib.dice().roll(1, adjs12.length, -1)].trim()+" "
						+adjs2[CMLib.dice().roll(1, adjs2.length, -1)].trim()+" "
						+nouns[CMLib.dice().roll(1, nouns.length, -1)].trim()
						;
				commands.remove(0);
			}
		}
		String itemName=CMParms.combine(commands,0);
		if(targetItem == null)
			targetItem=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,itemName);
		if(targetItem==null)
			targetItem=targetRoom.findItem(null,itemName);
		if((targetItem==null)||(!CMLib.flags().canBeSeenBy(targetItem,mob)))
		{
			mob.tell("You don't see '"+itemName+"' here.");
			return false;
		}
		if(!(targetItem instanceof Electronics))
		{
			mob.tell("That's not an electronics item.");
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell(mob,null,null,"<S-NAME> <S-IS-ARE> in combat!");
			return false;
		}
		if(!CMLib.flags().canBeSeenBy(targetRoom,mob))
		{
			mob.tell(mob,null,null,"<S-NAME> can't see to do that!");
			return false;
		}
		if(CMLib.flags().isSitting(mob)||CMLib.flags().isSleeping(mob))
		{
			mob.tell(mob,null,null,"You need to stand up!");
			return false;
		}
		if(op==Operation.REPAIR)
		{
			if(!targetItem.subjectToWearAndTear())
			{
				mob.tell(mob,targetItem,null,"<T-NAME> can't be repaired!");
				return false;
			}
		}
		for(final Enumeration<Ability> a=mob.personalEffects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)||(A.ID().equalsIgnoreCase("AstroEngineering"))))
			{
				if(A instanceof AstroEngineering)
					((AstroEngineering)A).aborted=true;
				A.unInvoke();
			}
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		failure=!proficiencyCheck(mob,0,auto);
		
		baseTickSpan = targetItem.basePhyStats().weight()/4;
		if(failure)
			baseTickSpan=baseTickSpan/2;
		if(baseTickSpan<8)
			baseTickSpan=8;
		startTickDown(mob, mob, baseTickSpan);
		
		return !failure;
	}
}
