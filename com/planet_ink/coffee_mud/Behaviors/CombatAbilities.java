package com.planet_ink.coffee_mud.Behaviors;
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
   Copyright 2000-2008 Bo Zimmerman

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
public class CombatAbilities extends StdBehavior
{
	public String ID(){return "CombatAbilities";}

	public int combatMode=0;
	public DVector aggro=null;
	public short chkDown=0;
	public Vector skillsNever=null;
	public Vector skillsAlways=null;
	protected boolean[] wandUseCheck={false,false};
	protected StringBuffer record=null;

	public final static int COMBAT_RANDOM=0;
	public final static int COMBAT_DEFENSIVE=1;
	public final static int COMBAT_OFFENSIVE=2;
	public final static int COMBAT_MIXEDOFFENSIVE=3;
	public final static int COMBAT_MIXEDDEFENSIVE=4;
	public final static String[] names={
		"RANDOM",
		"DEFENSIVE",
		"OFFENSIVE",
		"MIXEDOFFENSIVE",
		"MIXEDDEFENSIVE"
	};

	protected void makeClass(MOB mob, String theParms, String defaultClassName)
	{
	    CharClass C=null;
	    if(theParms.trim().length()==0) 
	    {
	        C=CMClass.findCharClass(defaultClassName);
			if(mob.baseCharStats().getCurrentClass()!=C)
			{
				mob.baseCharStats().setCurrentClass(C);
				mob.recoverCharStats();
			}
			return;
	    }
	    Vector V=CMParms.parse(theParms.trim());
	    Vector classes=new Vector();
	    for(int v=0;v<V.size();v++)
	    {
	        C=CMClass.findCharClass((String)V.elementAt(v));
	        if((C!=null)&&(!C.ID().equalsIgnoreCase("Archon")))
	            classes.addElement(C);
	    }
	    if(classes.size()==0) 
	    {
	        C=CMClass.findCharClass(defaultClassName);
			if(mob.baseCharStats().getCurrentClass()!=C)
			{
				mob.baseCharStats().setCurrentClass(C);
				mob.recoverCharStats();
			}
			return;
	    }
	    for(int i=0;i<classes.size();i++)
	    {
	        C=(CharClass)classes.elementAt(i);
	        mob.baseCharStats().setCurrentClass(C);
	        mob.baseCharStats().setClassLevel(C,mob.baseEnvStats().level()/classes.size());
	    }
	    mob.recoverCharStats();
	}

	protected String getParmsMinusCombatMode()
	{
		Vector V=CMParms.parse(getParms());
		for(int v=V.size()-1;v>=0;v--)
		{
			String s=((String)V.elementAt(v)).toUpperCase();
			for(int i=0;i<names.length;i++)
				if(names[i].startsWith(s))
				{
					combatMode=i;
					V.removeElementAt(v);
				}
		}
		return CMParms.combine(V,0);
	}

	protected void newCharacter(MOB mob)
	{
		Vector oldAbilities=new Vector();
		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if(A!=null)
			{
				if(A.proficiency()<50)	A.setProficiency(50);
				oldAbilities.addElement(A);
			}
		}
		mob.charStats().getCurrentClass().startCharacter(mob,true,false);
		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability newOne=mob.fetchAbility(a);
			if((newOne!=null)&&(!oldAbilities.contains(newOne)))
			{
				if(!CMLib.ableMapper().qualifiesByLevel(mob,newOne))
				{
					mob.delAbility(newOne);
					mob.delEffect(mob.fetchEffect(newOne.ID()));
					a=a-1;
				}
				else
					newOne.setProficiency(100);
			}
		}
	}
	
	public void setCombatStats(MOB mob, int attack, int armor, int damage, int hp, int mana, int move)
	{
		Ability A=mob.fetchEffect("Prop_CombatAdjuster");
		if(A==null)
		{
			A=CMClass.getAbility("Prop_CombatAdjuster");
			if(A!=null)
			{
				String text="";
				if(attack!=0) text+="ATTACK"+(attack>0?"+":"")+attack;
				if(armor!=0) text+="ARMOR"+(armor>0?"+":"")+armor;
				if(damage!=0) text+="DAMAGE"+(damage>0?"+":"")+damage;
				if(hp!=0) text+="HP"+(hp>0?"+":"")+hp;
				if(mana!=0) text+="MANA"+(mana>0?"+":"")+mana;
				if(move!=0) text+="MOVE"+(move>0?"+":"")+move;
				if(text.length()>0)
				{
					mob.addNonUninvokableEffect(A);
					A.setMiscText(text);
					A.setSavable(false);
					mob.recoverEnvStats();
					mob.recoverMaxState();
					mob.resetToMaxState();
				}
			}
		}
	}

	public void adjustAggro(MOB mob, int amt)
	{
		if(aggro==null) aggro=new DVector(2);
		synchronized(aggro)
		{
			Integer I=null;
			int x=aggro.indexOf(mob);
			if(x>=0) 
				I=(Integer)aggro.elementAt(x,2);
			else
			{
				x=aggro.size();
				I=new Integer(0);
				aggro.addElement(mob,I);
			}
			aggro.setElementAt(x,2,new Integer(I.intValue()+amt));
		}
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		MOB mob=(MOB)host;
		if(mob.isInCombat()) 
		{
			MOB victim=mob.getVictim();
			if(victim==null){}else
			if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(msg.value()>0)
			&&(msg.source()!=mob))
			{
				if(msg.target()==host)
					adjustAggro(msg.source(),msg.value()*2);
				else
				{
					if((victim==msg.source())
					||(msg.source().getGroupMembers(new HashSet()).contains(victim)))
						adjustAggro(msg.source(),msg.value());
				}
			}
			else
			if((msg.targetMinor()==CMMsg.TYP_HEALING)&&(msg.value()>0)
			&&(msg.source()!=mob)
			&&(msg.target()!=mob))
			{
				if((msg.target()==victim)
				||(msg.source().getGroupMembers(new HashSet()).contains(victim)))
					adjustAggro(msg.source(),msg.value()*2);
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
			&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
			&&(msg.source()!=host)
			&&(msg.tool() instanceof Ability)
			&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_SONG)
			&&(msg.source().isInCombat()))
			{
				if((msg.source()==victim)
				||(msg.source().getGroupMembers(new HashSet()).contains(victim)))
				{
					int level=CMLib.ableMapper().qualifyingLevel(msg.source(),(Ability)msg.tool());
					if(level<=0) level=CMLib.ableMapper().lowestQualifyingLevel(msg.tool().ID());
					if(level>0) adjustAggro(msg.source(),level);
				}
			}
		}
		super.executeMsg(host,msg);
	}
	
	
	
	public void startBehavior(Environmental forMe)
	{
		super.startBehavior(forMe);
		skillsNever=null;
		wandUseCheck[0]=false;
		Vector V=CMParms.parse(getParms());
		String s=null;
		Ability A=null;
		for(int v=0;v<V.size();v++)
		{
			s=(String)V.elementAt(v);
			if((s.startsWith("-"))
			&&((A=CMClass.getAbility(s.substring(1)))!=null))
			{
				if(skillsNever==null) skillsNever=new Vector();
				skillsNever.addElement(A.ID());
			}
			else
			if((s.startsWith("+"))
			&&((A=CMClass.getAbility(s.substring(1)))!=null))
			{
				if(skillsAlways==null) skillsAlways=new Vector();
				skillsAlways.addElement(A.ID());
			}
		}
		if(skillsNever!=null) skillsNever.trimToSize();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(ticking==null) return true;
		if(tickID!=Tickable.TICKID_MOB)
		{
			Log.errOut("CombatAbilities",ticking.name()+" wants to fight?!");
			return true;
		}
		MOB mob=(MOB)ticking;

		if(!canActAtAll(mob)) 
		    return true;
		if(!mob.isInCombat())
		{ 
		    if(aggro!=null)
		        aggro=null; 
		    return true;
		}
		MOB victim=mob.getVictim();
		if(victim==null) return true;
		
        // insures we only try this once!
		Behavior B;
        for(int b=0;b<mob.numBehaviors();b++)
        {
            B=mob.fetchBehavior(b);
            if((B==null)||(B==this))
                break;
            else
            if(B instanceof CombatAbilities)
                return true;
        }

        Room R=mob.location();
        
        if(!wandUseCheck[0]) {
            wandUseCheck[0]=true;
            Ability wandUse=mob.fetchAbility("Skill_WandUse");
            wandUseCheck[1]=false;
            if(wandUse!=null)
            { 
                wandUseCheck[1]=true;
                wandUse.setProficiency(100); 
                wandUse.setInvoker(mob);
            }
        }
        Item myWand=null;
        Item backupWand=null;
        Item wieldMe=null;
        Item wieldedItem=null;
        Item I=null;
        int rtt=mob.rangeToTarget();
        for(int i=0;i<mob.inventorySize();i++)
        {
            I=mob.fetchInventory(i);
            if(I instanceof Wand)
            {
                if(!I.amWearingAt(Item.IN_INVENTORY))
                    myWand=I;
                else
                    backupWand=I;
            }
            if(I instanceof Weapon)
            {
                if((((Weapon)I).minRange()>rtt)
                ||(((Weapon)I).maxRange()<rtt))
                {
                    if(I.amWearingAt(Item.WORN_WIELD))
                        wieldedItem=I;
                    else
                    if((wieldMe==null)||(I.amWearingAt(Item.WORN_HELD)))
                        wieldMe=I;
                }
            }
        }
        
        // first look for an appropriate weapon to weild
        if((wieldedItem==null)&&((--chkDown)<=0))
        {
            if((wieldMe==null)&&(R!=null))
            {
                Vector choices=new Vector(1);
                for(int r=0;r<R.numItems();r++)
                {
                    I=R.fetchItem(r);
                    if((!(I instanceof Weapon))
                    ||(((Weapon)I).minRange()>rtt)
                    ||(((Weapon)I).maxRange()<rtt)
                    ||(I.container()!=null)
                    ||(!CMLib.flags().isGettable(I))
                    ||(I.envStats().level()>mob.envStats().level()))
                        continue;
                    choices.addElement(I);
                }
                I=(choices.size()==0)?null:(Item)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
                if(I!=null)
                {
                    CMLib.commands().forceStandardCommand(mob,"GET",CMParms.makeVector("GET",I.Name()));
                    if(mob.isMine(I))
                        wieldMe=I;
                }
            }
            if(wieldMe!=null)
                CMLib.commands().forceStandardCommand(mob,"WIELD",CMParms.makeVector("WIELD",wieldMe.Name()));
            chkDown=5;
        }
        
        // next deal with aggro changes
		if(aggro!=null)
		{
			synchronized(aggro)
			{
				int windex=-1;
				int winAmt=0;
				int vicAmt=0;
				int minAmt=mob.maxState().getHitPoints()/10;
				for(int a=0;a<aggro.size();a++)
				{
					if(aggro.elementAt(a,1)==victim)
						vicAmt=((Integer)aggro.elementAt(a,2)).intValue();
					else
					if((((Integer)aggro.elementAt(a,2)).intValue()>winAmt)
					&&(CMLib.flags().canBeSeenBy((MOB)aggro.elementAt(a,1),mob)))
					{
						winAmt=((Integer)aggro.elementAt(a,2)).intValue();
						windex=a;
					}
				}
				if((winAmt>minAmt)
				&&(winAmt>(vicAmt+(vicAmt/2)))
				&&(!((MOB)aggro.elementAt(windex,1)).amDead())
				&&(((MOB)aggro.elementAt(windex,1)).isInCombat())
                &&(!mob.getGroupMembers(new HashSet()).contains((MOB)aggro.elementAt(windex,1))))
				{
					mob.setVictim((MOB)aggro.elementAt(windex,1));
					victim=mob.getVictim();
					aggro.clear();
				}
			}
		}
		if(victim==null) return true;
		
		MOB leader=mob.amFollowing();
		// now find a skill to use
		int tries=0;
		Ability tryThisOne=null;

		while((tryThisOne==null)&&(tries<100)&&((mob.numAbilities())>0))
		{
			tryThisOne=mob.fetchAbility(CMLib.dice().roll(1,mob.numAbilities(),-1));

			if((tryThisOne==null)
			||(tryThisOne.isAutoInvoked())
			||(tryThisOne.triggerStrings()==null)
			||(tryThisOne.triggerStrings().length==0))
				tryThisOne=null;
			else
		    if(((skillsAlways==null)||(!skillsAlways.contains(tryThisOne.ID())))
			&&(((tryThisOne.castingQuality(mob,victim)!=Ability.QUALITY_MALICIOUS)
				&&(tryThisOne.castingQuality(mob,mob)!=Ability.QUALITY_BENEFICIAL_SELF)
				&&(tryThisOne.castingQuality(mob,leader)!=Ability.QUALITY_BENEFICIAL_OTHERS))
			||((skillsNever!=null)&&(skillsNever.contains(tryThisOne.ID())))
			||(victim.fetchEffect(tryThisOne.ID())!=null)))
				tryThisOne=null;
			else
			if(tryThisOne.castingQuality(mob,victim)==Ability.QUALITY_MALICIOUS)
			{
				switch(combatMode)
				{
				case COMBAT_RANDOM:
					break;
				case COMBAT_DEFENSIVE:
					if(CMLib.dice().rollPercentage()>5)
						tryThisOne=null;
					break;
				case COMBAT_OFFENSIVE:
					break;
				case COMBAT_MIXEDOFFENSIVE:
					if(CMLib.dice().rollPercentage()>75)
						tryThisOne=null;
					break;
				case COMBAT_MIXEDDEFENSIVE:
					if(CMLib.dice().rollPercentage()>25)
						tryThisOne=null;
					break;
				}
			}
			else
			{
				switch(combatMode)
				{
				case COMBAT_RANDOM:
					break;
				case COMBAT_DEFENSIVE:
					break;
				case COMBAT_OFFENSIVE:
					if(CMLib.dice().rollPercentage()>5)
						tryThisOne=null;
					break;
				case COMBAT_MIXEDOFFENSIVE:
					if(CMLib.dice().rollPercentage()>25)
						tryThisOne=null;
					break;
				case COMBAT_MIXEDDEFENSIVE:
					if(CMLib.dice().rollPercentage()>75)
						tryThisOne=null;
					break;
				}
			}
			tries++;
		}


		boolean skillUsed=true;
		if(tryThisOne!=null)
		{
			if(CMath.bset(tryThisOne.usageType(),Ability.USAGE_MANA))
			{
				if((Math.random()>CMath.div(mob.curState().getMana(), mob.maxState().getMana()))
                ||(mob.curState().getMana() < tryThisOne.usageCost(mob,false)[0]))
				{
                   if((CMLib.dice().rollPercentage()>30)
				   ||(CMProps.getIntVar(CMProps.SYSTEMI_MANACONSUMETIME)<=0)
				   ||((mob.amFollowing()!=null)&&(!mob.amFollowing().isMonster())))
                        return true;
				   mob.curState().adjMana(tryThisOne.usageCost(mob,false)[0],mob.maxState());
				}
				mob.curState().adjMana(5,mob.maxState());
			}
			if(CMath.bset(tryThisOne.usageType(),Ability.USAGE_MOVEMENT))
			{
				if((Math.random()>CMath.div(mob.curState().getMovement(),mob.maxState().getMovement()))
				||(mob.curState().getMovement()<tryThisOne.usageCost(mob,false)[1]))
					return true;
				mob.curState().adjMovement(5,mob.maxState());
			}
			if(CMath.bset(tryThisOne.usageType(),Ability.USAGE_HITPOINTS))
			{
				if((Math.random()>CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()))
				   ||(mob.curState().getHitPoints()<tryThisOne.usageCost(mob,false)[2]))
					return true;
			}

			if(tryThisOne.castingQuality(mob,mob)==Ability.QUALITY_BENEFICIAL_SELF)
				victim=mob;
			else
			if(tryThisOne.castingQuality(mob,leader)==Ability.QUALITY_BENEFICIAL_OTHERS)
			{ victim=((leader==null)||(mob.location()!=leader.location()))?mob:leader;}
	        

			tryThisOne.setProficiency(CMLib.dice().roll(1,70,mob.baseEnvStats().level()));
			Vector V=new Vector();
			V.addElement(victim.name());
			if(tryThisOne.invoke(mob,V,victim,false,0))
			    skillUsed=true;
			else
            if(record!=null) record.append("!");
			if(record!=null) record.append("!"+tryThisOne.ID()+";");
		}
		
		// if a skill use failed, take a stab at wanding
		if((!skillUsed)
        &&(wandUseCheck[1])
		&&(victim.location()!=null)
		&&(!victim.amDead())
		&&((myWand!=null)||(backupWand!=null)))
		{
		    Ability A=null;
			if((myWand==null)&&(backupWand!=null)&&(backupWand.canWear(mob,Item.WORN_HELD)))
			{
				Vector V=new Vector();
				V.addElement("hold");
				V.addElement(backupWand.name());
				mob.doCommand(V,Command.METAFLAG_FORCED);
			}
			else
			if(myWand!=null)
			{
				A=((Wand)myWand).getSpell();
				if((A!=null)
				&&((A.castingQuality(mob,mob.getVictim())==Ability.QUALITY_MALICIOUS)
				||(A.castingQuality(mob,mob)==Ability.QUALITY_BENEFICIAL_SELF)
				||(A.castingQuality(mob,leader)==Ability.QUALITY_BENEFICIAL_OTHERS)))
				{
					if(A.castingQuality(mob,mob)==Ability.QUALITY_BENEFICIAL_SELF)
						victim=mob;
					else
					if(A.castingQuality(mob,leader)==Ability.QUALITY_BENEFICIAL_OTHERS)
					{ victim=((leader==null)||(mob.location()!=leader.location()))?mob:leader;}
					else
					if(mob.getVictim()!=null)
						victim=mob.getVictim();
					else
						victim=null;
					if(victim!=null)
					{
						Vector V=new Vector();
						V.addElement("sayto");
						V.addElement(victim.name());
						V.addElement(((Wand)myWand).magicWord());
						mob.doCommand(V,Command.METAFLAG_FORCED);
					}
				}
			}
		}
		return true;
	}
	
	
    protected static String[] CODES=null;
    public String[] getStatCodes(){
        if(this.CODES==null)
        {
            String[] superCodes=super.getStatCodes();
            CODES=new String[superCodes.length+1];
            for(int c=0;c<superCodes.length;c++)
                CODES[c]=superCodes[c];
            CODES[CODES.length-1]="RECORD";
        }
        return CODES;
    }
    protected int getCodeNum(String code){
        for(int i=0;i<CODES.length;i++)
            if(code.equalsIgnoreCase(CODES[i])) return i;
        return -1;
    }
    public String getStat(String code){
        int x=getCodeNum(code);
        if(x<super.getStatCodes().length)
            return super.getStat(code);
        x=x-super.getStatCodes().length;
        switch(getCodeNum(code))
        {
        case 0: return (record==null)?"":record.toString();
        }
        return "";
    }
    public void setStat(String code, String val)
    {
        int x=getCodeNum(code);
        if(x<super.getStatCodes().length)
            super.setStat(code,val);
        x=x-super.getStatCodes().length;
        switch(getCodeNum(code))
        {
        case 0:
            if(val.length()==0)
                record=null;
            else
                record=new StringBuffer(val.trim());
            break;
        }
    }
}
