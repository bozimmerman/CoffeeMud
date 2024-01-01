package com.planet_ink.coffee_mud.Abilities.Poisons;
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
   Copyright 2003-2024 Bo Zimmerman

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
public class Poison_Alcohol extends Poison
{
	@Override
	public String ID()
	{
		return "Poison_Alcohol";
	}

	private final static String localizedName = CMLib.lang().L("Alcohol");

	@Override
	public String name()
	{
		return localizedName;
	}

	public Poison_Alcohol()
	{
		super();
		drunkness=5;
	}

	private static final String[] triggerStrings = I(new String[] { "POISONALCOHOL" });

	@Override
	public String displayText()
	{
		return (drunkness <= 0) ? "(Holding your own)" : (drunkness <= 3) ? "(Tipsy)" : ((drunkness < 10) ? "(Drunk)" : "(Smashed)");
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public long flags()
	{
		return super.flags() | Ability.FLAG_INTOXICATING;
	}

	@Override
	protected int POISON_TICKS()
	{
		return 65;
	}

	@Override
	protected int POISON_DELAY()
	{
		return 1;
	}

	@Override
	protected String POISON_DONE()
	{
		return "You feel sober again.";
	}

	@Override
	protected String POISON_START()
	{
		return "^G<S-NAME> burp(s)!^?";
	}

	@Override
	protected String POISON_AFFECT()
	{
		return "";
	}

	@Override
	protected String POISON_CAST()
	{
		return "^F^<FIGHT^><S-NAME> inebriate(s) <T-NAMESELF>!^</FIGHT^>^?";
	}

	@Override
	protected String POISON_FAIL()
	{
		return "<S-NAME> attempt(s) to inebriate <T-NAMESELF>, but fail(s).";
	}

	@Override
	protected int POISON_DAMAGE()
	{
		return 0;
	}

	protected boolean disableHappiness = false;

	protected int alcoholContribution()
	{
		return 1;
	}

	protected int level()
	{
		return 1;
	}

	protected int drunkness = 5;

	@Override
	public int abilityCode()
	{
		return drunkness;
	}

	@Override
	public void setAbilityCode(final int newCode)
	{
		drunkness=newCode;
	}

	protected Ability mood = null;

	protected Ability getMood()
	{
		if(mood == null)
		{
			final Physical affected=this.affected;
			if(affected == null)
				return null;
			mood = CMClass.getAbility("Mood");
			if((mood == null)
			||(affected.phyStats().isAmbiance(PhyStats.Ambiance.SUPPRESS_MOOD))
			||(affected.phyStats().isAmbiance(PhyStats.Ambiance.SUPPRESS_DRUNKENNESS)))
				return null;
			final String moods[] = {"HAPPY","MEAN","SILLY","ANGRY","SAD",""};
			final String moodStr = moods[Math.abs(affected.Name().hashCode())%moods.length];
			if(moodStr.length()==0)
				return null;
			mood.setMiscText(moodStr);
			mood.setAffectedOne(affected);
		}
		mood.setAffectedOne(affected);
		return mood;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		if((affected instanceof MOB)&&(drunkness>0))
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(drunkness+((MOB)affected).phyStats().level()));
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.STAT_DEXTERITY,(affectableStats.getStat(CharStats.STAT_DEXTERITY)-drunkness));
		if(affectableStats.getStat(CharStats.STAT_DEXTERITY)<=0)
			affectableStats.setStat(CharStats.STAT_DEXTERITY,1);
	}

	@Override
	protected int POISON_ADDICTION_CHANCE()
	{
		return (alcoholContribution()*alcoholContribution()*alcoholContribution()/10);
	}

	@Override
	public void unInvoke()
	{
		if((affected instanceof MOB)&&(canBeUninvoked()))
		{
			final MOB mob=(MOB)affected;
			if((CMLib.dice().rollPercentage()==1)&&(!((MOB)affected).isMonster())&&(drunkness>0))
			{
				final Ability A=CMClass.getAbility("Disease_Migraines");
				if((A!=null)&&(mob.fetchEffect(A.ID())==null))
					A.invoke(mob,mob,true,0);
			}
			CMLib.commands().postStand(mob,true, false);
		}
		super.unInvoke();
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if(!(affected instanceof MOB))
			return true;

		if(disableHappiness)
		{
			disableHappiness=false;
			return true;
		}

		final MOB mob=(MOB)affected;
		if(mob==null)
			return true;

		final Ability mood = getMood();
		if((mood != null)
		&&(CMLib.dice().rollPercentage()<(4*drunkness)))
			mood.tick(mob, Tickable.TICKID_MOB);

		final Room room=mob.location();
		if((CMLib.dice().rollPercentage()<(4*drunkness))
		&&(CMLib.flags().isAliveAwakeMobile(mob,true))
		&&(!mob.phyStats().isAmbiance(PhyStats.Ambiance.SUPPRESS_DRUNKENNESS))
		&&(room!=null))
		{
			if(CMLib.flags().isEvil(mob))
			switch(CMLib.dice().roll(1,9,-1))
			{
			case 0:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> stagger(s) around making ugly faces."));
				break;
			case 1:
				room.show(mob,null,this,CMMsg.MSG_NOISE,L("<S-NAME> belch(es) grotesquely."));
				break;
			case 2:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> spin(s) <S-HIS-HER> head around."));
				break;
			case 3:
				room.show(mob,null,this,CMMsg.MSG_NOISE,L("<S-NAME> can't stop snarling."));
				break;
			case 4:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> just fell over!"));
				break;
			case 5:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> look(s) around with glazed over eyes."));
				break;
			case 6:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> can't seem to focus."));
				break;
			case 7:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> <S-IS-ARE> definitely sh** faced!"));
				break;
			case 8:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> stare(s) blankly at the ground."));
				break;
			}
			else
			if(!CMLib.flags().isGood(mob))
			switch(CMLib.dice().roll(1,9,-1))
			{
			case 0:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> stagger(s) around aimlessly."));
				break;
			case 1:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> burp(s) noncommitally."));
				break;
			case 2:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> look(s) around with glazed over eyes."));
				break;
			case 3:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> can't seem to focus."));
				break;
			case 4:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> almost fell over."));
				break;
			case 5:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> hiccup(s) and almost smile(s)."));
				break;
			case 6:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> belch(es)!"));
				break;
			case 7:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> <S-IS-ARE> definitely drunk!"));
				break;
			case 8:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> stare(s) blankly ahead."));
				break;
			}
			else
			switch(CMLib.dice().roll(1,9,-1))
			{
			case 0:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> stagger(s) around trying to hug everyone."));
				break;
			case 1:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> hiccup(s) and smile(s)."));
				break;
			case 2:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> bob(s) <S-HIS-HER> head back and forth."));
				break;
			case 3:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> can't stop smiling."));
				break;
			case 4:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> lean(s) slightly to one side."));
				break;
			case 5:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> look(s) around with glazed over eyes."));
				break;
			case 6:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> can't seem to focus."));
				break;
			case 7:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> <S-IS-ARE> definitely a bit tipsy!"));
				break;
			case 8:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> stare(s) blankly at <S-HIS-HER> eyelids."));
				break;
			}

		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(affected instanceof MOB)
		{
			if(msg.source()!=affected)
				return true;
			if(msg.source().location()==null)
				return true;

			final Ability mood = getMood();
			if((mood != null)
			&&(!mood.okMessage(msg.source(), msg)))
				return false;

			if((msg.amISource((MOB)affected))
			&&(msg.sourceMessage()!=null)
			&&(msg.tool()==null)
			&&(drunkness>=5)
			&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
			   ||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL)))
			&&(!msg.source().phyStats().isAmbiance(PhyStats.Ambiance.SUPPRESS_DRUNKENNESS)))
			{
				final Ability A=CMClass.getAbility("Drunken");
				if(A!=null)
				{
					A.setProficiency(100);
					A.invoke(msg.source(),null,true,0);
					A.setAffectedOne(msg.source());
					if(!A.okMessage(myHost,msg))
						return false;
				}
			}
			else
			if((!msg.targetMajor(CMMsg.MASK_ALWAYS))
			&&(CMLib.dice().rollPercentage()<(drunkness*20))
			&&(msg.targetMajor()>0)
			&&(!msg.source().phyStats().isAmbiance(PhyStats.Ambiance.SUPPRESS_DRUNKENNESS)))
			{

				final Room room=msg.source().location();
				if((msg.target() !=null)&&(msg.target() instanceof MOB)&&(room!=null))
				{
					Environmental target=msg.target();
					if(room.numInhabitants()>2)
					{
						target=room.fetchInhabitant(CMLib.dice().roll(1,room.numInhabitants(),0)-1);
						if(!CMLib.flags().canBeSeenBy(target, msg.source()))
							target=msg.target();
					}
					msg.modify(msg.source(),target,msg.tool(),msg.sourceCode(),msg.sourceMessage(),msg.targetCode(),msg.targetMessage(),msg.othersCode(),msg.othersMessage());
				}
			}
		}
		else
		{

		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		int largest=alcoholContribution();
		if((givenTarget instanceof MOB)&&(auto))
		{
			final Vector<Ability> found=new Vector<Ability>();
			final Vector<Ability> remove=new Vector<Ability>();
			largest=0;
			for(final Enumeration<Ability> a=givenTarget.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(A instanceof Poison_Alcohol)
				{
					largest+=((Poison_Alcohol)A).drunkness;
					if(((Poison_Alcohol)A).level()>=level())
						found.addElement(A);
					else
						remove.addElement(A);
				}
			}
			largest+=alcoholContribution();
			if(found.size()>0)
			{
				final CMMsg msg=CMClass.getMsg(mob,givenTarget,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_POISON|CMMsg.MASK_ALWAYS,POISON_CAST());
				final Room R=(((MOB)givenTarget).location()!=null)?((MOB)givenTarget).location():mob.location();
				if(R.okMessage(mob,msg))
				{
					R.send(mob,msg);
					if(msg.value()<=0)
					{
						R.show((MOB)givenTarget,null,CMMsg.MSG_OK_VISUAL,POISON_START());
						for(int a=0;a<found.size();a++)
						{
							((Poison_Alcohol)found.elementAt(a)).drunkness=largest;
							((Poison_Alcohol)found.elementAt(a)).tickDown=POISON_TICKS();
						}
						R.recoverRoomStats();
						return true;
					}
				}
				return false;
			}

			for(int i=0;i<remove.size();i++)
				givenTarget.delEffect(remove.elementAt(i));
		}
		final boolean success=super.invoke(mob,commands,givenTarget,auto,asLevel);
		if(success&&(givenTarget instanceof MOB)&&(auto))
		{
			final Ability A=givenTarget.fetchEffect(ID());
			if(A!=null)
			{
				((Poison_Alcohol)A).drunkness=largest;
				((Poison_Alcohol)A).tickDown=POISON_TICKS();
			}
			final Room R=(((MOB)givenTarget).location()!=null)?((MOB)givenTarget).location():mob.location();
			R.recoverRoomStats();
		}
		return success;
	}
}
