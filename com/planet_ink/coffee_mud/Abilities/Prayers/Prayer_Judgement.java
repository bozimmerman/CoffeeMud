package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2014-2018 Bo Zimmerman

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

public class Prayer_Judgement extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Judgement";
	}

	private final static String localizedName = CMLib.lang().L("Judgement");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Judgement)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_CURSING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}
	
	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL;
	}
	
	private final String socialName="PRAY <T-NAME>";
	private final int numToDo=10;
	private final Set<MOB> doneMobs = new HashSet<MOB>();
	private final Set<Room> botheredRooms = new HashSet<Room>();
	
	private static String[] FRIENDLY_SOCIALS = 
		{"ANGELIC","APOLOGIZE","AWE","BEAM","BEARHUG","BKISS","BLUSH","BOW","BSCRATCH","CHEER","CLAP",
		"COMFORT","CRY","CUDDLE","DANCE","EMBRACE","GIRN","GREET","GRIN","GROVEL","HANDSHAKE","HIGHFIVE",
		"HSHAKE","HUG","KISS","LOVE","MASSAGE","NOD","NUZZLE","PAT","PATPAT","PET","PRAY","PURR","SALUTE",
		"SERENADE","SMILE","SMOOCH","SNUGGLE","SPOON","SSMILE","STROKE","SUPPORT","SWEET","THANK","TICKLE","TOAST"};
	
	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		// undo the affects of this spell
		final MOB mob=(MOB)affected;
		final MOB invoker=this.invoker();
		super.unInvoke();
		if((canBeUninvoked()&&(!mob.amDead())))
		{
			if(doneMobs.size()>=numToDo)
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> <S-HAS-HAVE> survived the judgement."));
			else
			if(super.tickDown < 2)
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> <S-IS-ARE> consumed by <S-HIS-HER> judgement!"));
				CMLib.combat().postDeath(invoker(), mob, null);
			}
			if(mob.isMonster() && (!mob.amDead()))
			{
				CMLib.commands().postStand(mob,true);
				CMLib.tracking().wanderAway(mob,false,true);
				if((invoker!=null)&&(invoker!=mob)&&(invoker.location()==mob.location()))
					CMLib.combat().postAttack(mob, invoker, mob.fetchWieldedItem());
			}
			botheredRooms.clear();
			doneMobs.clear();
		}
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		final Physical affected=this.affected;
		if((affected instanceof MOB)&&(((MOB)affected).isMonster()))
		{
			final MOB mob=(MOB)affected;
			final Room R=mob.location();
			if(mob.isInCombat())
				CMLib.combat().postPanic(mob, null);
			else
			if(!CMLib.flags().canMove(mob))
				unInvoke();
			else
			if(R!=null)
			{
				MOB doMOB=null;
				for(Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
				{
					final MOB M=m.nextElement();
					if((!doneMobs.contains(M)) && (M!=mob) && CMLib.flags().canBeSeenBy(M,mob))
					{
						doMOB=M;
						break;
					}
				}
				if(doMOB==null)
				{
					if(!botheredRooms.contains(R))
						botheredRooms.add(R);
					CMLib.commands().postStand(mob,true);
					if(!CMLib.tracking().beMobile(mob,true,true,false,true,null,botheredRooms))
						CMLib.tracking().beMobile(mob,true,true,false,false,null,null);
				}
				else
				{
					Social S=CMLib.socials().fetchSocial(socialName, true);
					if(S==null)
						unInvoke();
					else
						S.invoke(mob, new XVector<String>(S.baseName(),R.getContextName(doMOB)), doMOB, false);
				}
					
			}
		}
		return true;
	}
	
	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((msg.source()==affected) 
		&& (msg.tool() instanceof Social)
		&& (msg.target() instanceof MOB)
		&& (msg.tool().Name().equals(socialName))
		&& (msg.source() != msg.target())
		&& (!doneMobs.contains(msg.target())))
		{
			doneMobs.add((MOB)msg.target());
			if(doneMobs.size()>=numToDo)
				unInvoke();
			else
				msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_OK_ACTION,L("^HThat`s @x1/@x2.",""+doneMobs.size(),""+numToDo),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success && (!CMLib.flags().isGood(target)))
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto)|CMMsg.MASK_MALICIOUS,auto?L("<T-NAME> gain(s) an holy geas of judgement!"):L("^S<S-NAME> judge(s) <T-NAMESELF>, laying a geas upon <T-HIM-HER>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					final Ability A=maliciousAffect(mob,target,asLevel,0,-1);
					if(A!=null)
					{
						int num=10 + super.getX1Level(mob) + super.getXLEVELLevel(mob);
						String socialName=null;
						String socialBaseName=null;
						int tries=100;
						while( ((--tries)>0) && (socialName==null))
						{
							String name=FRIENDLY_SOCIALS[CMLib.dice().roll(1, FRIENDLY_SOCIALS.length, -1)];
							Social S=CMLib.socials().fetchSocial(name+" <T-NAME>", true);
							if(S!=null)
							{
								socialName=S.Name();
								socialBaseName=S.baseName().toLowerCase();
							}
						}
						if(socialName == null)
							A.unInvoke();
						else
						{
							A.setMiscText("NUM="+num+" SOCIAL=\""+socialName+"\"");
							target.tell(L("You have been placed under a powerful geas which you must fulfill before this spell expires, or you will die.  You must @x1 at least @x2 unique creatures to survive. BEGIN!",socialBaseName,""+num));
						}
					}
					else
						success=false;
					target.recoverPhyStats();
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to curse <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
