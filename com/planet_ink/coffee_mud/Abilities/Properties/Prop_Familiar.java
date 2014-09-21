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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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
public class Prop_Familiar extends Property
{
	@Override public String ID() { return "Prop_Familiar"; }
	@Override public String name(){ return "Find Familiar Property";}
	protected String displayText=L("Familiarity with an animal");
	@Override public String displayText() {return displayText;}
	@Override protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected final static int DOG=0;
	protected final static int TURTLE=1;
	protected final static int CAT=2;
	protected final static int BAT=3;
	protected final static int RAT=4;
	protected final static int SNAKE=5;
	protected final static int OWL=6;
	protected final static int RABBIT=7;
	protected final static int RAVEN=8;
	protected final static String[] names={"dog","turtle","cat","bat","rat","snake",
										 "owl","rabbit","raven"};

	protected MOB familiarTo=null;
	protected MOB familiarWith=null;
	protected boolean imthedaddy=false;
	protected int familiarType=0;
	protected int[] lastBreathablesSet=null;
	protected int[] newBreathablesSet=null;

	@Override
	public String accountForYourself()
	{
		return "is a familiar MOB";
	}

	@Override public long flags(){return Ability.FLAG_ADJUSTER;}

	public boolean removeMeFromFamiliarTo()
	{
		if(familiarTo!=null)
		{
			final Ability A=familiarTo.fetchEffect(ID());
			if(A!=null)
			{
				familiarTo.delEffect(A);
				/*if(!familiarTo.amDead())
				{
					CMLib.leveler().postExperience(familiarTo,null,null,-50,false);
					familiarTo.tell(L("You`ve just lost 50 experience points for losing your familiar"));
				}*/
				familiarTo.recoverCharStats();
				familiarTo.recoverPhyStats();
			}
		}
		if(familiarWith!=null)
		{
			final Ability A=familiarWith.fetchEffect(ID());
			if(A!=null)
			{
				familiarWith.delEffect(A);
				familiarWith.recoverCharStats();
				familiarWith.recoverPhyStats();
			}
			if(familiarWith.amDead())
				familiarWith.setLocation(null);
			familiarWith.destroy();
			familiarWith.setLocation(null);
		}
		return false;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if(!(affected instanceof MOB))
				return removeMeFromFamiliarTo();
			if(((familiarTo != null) && (familiarTo.amDestroyed()))
			||((familiarWith != null) && (familiarWith.amDestroyed())))
			{
				familiarTo=null;
				familiarWith=null;
				imthedaddy=false;
			}
			final MOB familiar=(MOB)affected;
			if(familiar.amDead())
				return removeMeFromFamiliarTo();
			if((!imthedaddy)
			&&(familiarTo==null)
			&&(familiarWith==null)
			&&(CMLib.flags().isInTheGame((MOB)affected,true))
			&&(((MOB)affected).amFollowing()!=null)
			&&(CMLib.flags().isInTheGame(((MOB)affected).amFollowing(),true)))
			{
				final MOB following=((MOB)affected).amFollowing();
				familiarWith=(MOB)affected;
				familiarTo=following;
				final Prop_Familiar F=(Prop_Familiar)copyOf();
				F.setSavable(false);
				F.imthedaddy=true;
				F.familiarWith=(MOB)affected;
				F.familiarTo = following; // yes, points to self
				following.delEffect(following.fetchEffect(F.ID()));
				following.addEffect(F);
				following.recoverCharStats();
				following.recoverPhyStats();
			}
			if((familiarWith!=null)
			&&(familiarTo!=null)
			&&((familiarWith.amFollowing()==null)
					||(familiarWith.amFollowing()!=familiarTo))
			&&(CMLib.flags().isInTheGame(familiarWith,true)))
				removeMeFromFamiliarTo();
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{

		if((familiarWith!=null)
		&&(familiarTo!=null)
		&&(familiarWith.location()==familiarTo.location()))
		switch(familiarType)
		{
		case DOG:
				affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_HIDDEN);
				break;
		case TURTLE:
				break;
		case CAT:
				break;
		case BAT:
				if(((affectableStats.sensesMask()&PhyStats.CAN_NOT_SEE)>0)&&(affected instanceof MOB))
					affectableStats.setSensesMask(affectableStats.sensesMask()-PhyStats.CAN_NOT_SEE);
				break;
		case RAT:
				break;
		case SNAKE:
				break;
		case OWL:
				affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_INFRARED);
				break;
		case RABBIT:
				break;
		case RAVEN:
				break;
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(((msg.targetMajor()&CMMsg.MASK_MALICIOUS)>0)
		&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
		&&(familiarWith!=null)
		&&(familiarTo!=null)
		&&((msg.amITarget(familiarWith))||(msg.amITarget(familiarTo)))
		&&(familiarWith.location()==familiarTo.location())
		&&(familiarType==RABBIT))
		{
			final MOB target=(MOB)msg.target();
			if((!target.isInCombat())
			&&(msg.source().location()==target.location())
			&&(msg.source().getVictim()!=target))
			{
				msg.source().tell(L("You are too much in awe of @x1",target.name(msg.source())));
				if(familiarWith.getVictim()==msg.source())
					familiarWith.makePeace();
				if(familiarTo.getVictim()==msg.source())
					familiarTo.makePeace();
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((msg.sourceMinor()==CMMsg.TYP_DEATH)
		&&((msg.source()==familiarWith)||(msg.source()==familiarTo)))
			removeMeFromFamiliarTo();
		super.executeMsg(host,msg);
	}

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		familiarType=CMath.s_int(newText);
		if(newText.trim().length()>2)
		for(int i=0;i<names.length;i++)
			if(newText.trim().equalsIgnoreCase(names[i]))
			{ familiarType=i; break;}
		displayText=L("(Familiarity with the @x1)",names[familiarType]);
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		if((familiarWith!=null)
		&&(familiarTo!=null)
		&&(familiarWith.location()==familiarTo.location()))
		switch(familiarType)
		{
		case DOG:
				affectableStats.setStat(CharStats.STAT_STRENGTH,affectableStats.getStat(CharStats.STAT_STRENGTH)+1);
				break;
		case TURTLE:
			{
				final int[] breatheables=affectableStats.getBreathables();
				if(breatheables.length==0)
					return;
				if((lastBreathablesSet!=breatheables)||(newBreathablesSet==null))
				{
					newBreathablesSet=Arrays.copyOf(affectableStats.getBreathables(),affectableStats.getBreathables().length+2);
					newBreathablesSet[newBreathablesSet.length-1]=RawMaterial.RESOURCE_SALTWATER;
					newBreathablesSet[newBreathablesSet.length-2]=RawMaterial.RESOURCE_FRESHWATER;
					Arrays.sort(newBreathablesSet);
					lastBreathablesSet=breatheables;
				}
				affectableStats.setBreathables(newBreathablesSet);
				affectableStats.setStat(CharStats.STAT_STRENGTH,affectableStats.getStat(CharStats.STAT_STRENGTH)+1);
				break;
			}
		case CAT:
				affectableStats.setStat(CharStats.STAT_DEXTERITY,affectableStats.getStat(CharStats.STAT_DEXTERITY)+1);
				if(affectableStats.getStat(CharStats.STAT_SAVE_PARALYSIS)<500)
					affectableStats.setStat(CharStats.STAT_SAVE_PARALYSIS,affectableStats.getStat(CharStats.STAT_SAVE_PARALYSIS)+100);
				break;
		case BAT:
				affectableStats.setStat(CharStats.STAT_DEXTERITY,affectableStats.getStat(CharStats.STAT_DEXTERITY)+1);
				break;
		case RAT:
				if(affectableStats.getStat(CharStats.STAT_SAVE_DISEASE)<500)
					affectableStats.setStat(CharStats.STAT_SAVE_DISEASE,affectableStats.getStat(CharStats.STAT_SAVE_DISEASE)+100);
				affectableStats.setStat(CharStats.STAT_CONSTITUTION,affectableStats.getStat(CharStats.STAT_CONSTITUTION)+1);
				break;
		case SNAKE:
				if(affectableStats.getStat(CharStats.STAT_SAVE_POISON)<500)
					affectableStats.setStat(CharStats.STAT_SAVE_POISON,affectableStats.getStat(CharStats.STAT_SAVE_POISON)+100);
				affectableStats.setStat(CharStats.STAT_CONSTITUTION,affectableStats.getStat(CharStats.STAT_CONSTITUTION)+1);
				break;
		case OWL:
				affectableStats.setStat(CharStats.STAT_WISDOM,affectableStats.getStat(CharStats.STAT_WISDOM)+1);
				break;
		case RABBIT:
				affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)+1);
				break;
		case RAVEN:
				if(affectableStats.getStat(CharStats.STAT_SAVE_UNDEAD)<500)
					affectableStats.setStat(CharStats.STAT_SAVE_UNDEAD,affectableStats.getStat(CharStats.STAT_SAVE_UNDEAD)+100);
				affectableStats.setStat(CharStats.STAT_INTELLIGENCE,affectableStats.getStat(CharStats.STAT_INTELLIGENCE)+1);
				break;
		}
	}
}
