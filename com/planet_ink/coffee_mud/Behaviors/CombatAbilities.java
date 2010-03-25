package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
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
public class CombatAbilities extends StdBehavior
{
	public String ID(){return "CombatAbilities";}

	public int combatMode=0;
	public Hashtable<MOB,int[]> aggro=null;
	public short chkDown=0;
	public Vector skillsNever=null;
	public Vector skillsAlways=null;
	protected boolean[] wandUseCheck={false,false};
	protected boolean proficient=false;
	protected int preCastSet=Integer.MAX_VALUE;
	protected int preCastDown=Integer.MAX_VALUE;
	protected String lastSpell=null;
	protected StringBuffer record=null;
	protected int physicalDamageTaken=0;

	public final static int COMBAT_RANDOM=0;
	public final static int COMBAT_DEFENSIVE=1;
	public final static int COMBAT_OFFENSIVE=2;
	public final static int COMBAT_MIXEDOFFENSIVE=3;
	public final static int COMBAT_MIXEDDEFENSIVE=4;
	public final static int COMBAT_ONLYALWAYS=5;
	public final static String[] names={
		"RANDOM",
		"DEFENSIVE",
		"OFFENSIVE",
		"MIXEDOFFENSIVE",
		"MIXEDDEFENSIVE",
		"ONLYALWAYS"
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
				int proficiency=CMLib.ableMapper().getMaxProficiency(mob,true,A.ID())/2;
				if(A.proficiency()<proficiency)	A.setProficiency(proficiency);
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
					newOne.setProficiency(CMLib.ableMapper().getMaxProficiency(newOne.ID()));
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
				if(attack!=0) text+=" ATTACK"+(attack>0?"+":"")+attack;
				if(armor!=0) text+=" ARMOR"+(armor>0?"+":"")+armor;
				if(damage!=0) text+=" DAMAGE"+(damage>0?"+":"")+damage;
				if(hp!=0) text+=" HP"+(hp>0?"+":"")+hp;
				if(mana!=0) text+=" MANA"+(mana>0?"+":"")+mana;
				if(move!=0) text+=" MOVE"+(move>0?"+":"")+move;
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

	public void adjustAggro(MOB hostM, MOB attackerM, int amt)
	{
		if(aggro==null) aggro=new Hashtable<MOB,int[]>();
		synchronized(aggro)
		{
			int[] I = aggro.get(attackerM);
			if(I==null)
			{
				I=new int[]{0};
				aggro.put(attackerM, I);
			}
			I[0]+=amt;
			MOB curVictim=hostM.getVictim();
			if((curVictim==attackerM)
			||(curVictim==null)
			||(!aggro.containsKey(curVictim)))
				return;
			int vicAmt=aggro.get(curVictim)[0];
			if((I[0]>(vicAmt*1.5))
			&&(I[0]>hostM.maxState().getHitPoints()/10)
			&&(!attackerM.amDead())
			&&(attackerM.isInCombat()))
			{
	            if((hostM.getGroupMembers(new HashSet()).contains(attackerM))
	            ||(!CMLib.flags().canBeSeenBy(attackerM, hostM)))
	            	I[0]=0;
	            else
	            {
					hostM.setVictim(attackerM);
					aggro.clear();
	            }
			}
		}
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(host instanceof MOB)
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
					if(((msg.tool()==null)||(msg.tool() instanceof Item))
					&&(msg.target()==mob)
					&&(msg.source()==mob.getVictim()))
						physicalDamageTaken+=msg.value();
					if(msg.target()==host)
						adjustAggro(mob,msg.source(),msg.value()*2);
					else
					{
						if((victim==msg.source())
						||(msg.source().getGroupMembers(new HashSet()).contains(victim)))
							adjustAggro(mob,msg.source(),msg.value());
					}
				}
				else
				if((msg.targetMinor()==CMMsg.TYP_HEALING)&&(msg.value()>0)
				&&(msg.source()!=mob)
				&&(msg.target()!=mob))
				{
					if((msg.target()==victim)
					||(msg.source().getGroupMembers(new HashSet()).contains(victim)))
						adjustAggro(mob,msg.source(),msg.value()*2);
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
						if(level>0) adjustAggro(mob,msg.source(),level);
					}
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
		proficient=false;
		Vector V=CMParms.parse(getParms());
		String s=null;
		Ability A=null;
		for(int v=0;v<V.size();v++)
		{
			s=(String)V.elementAt(v);
			if(s.equalsIgnoreCase("proficient"))
				proficient=true;
			else
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

	protected boolean isRightCombatAbilities(MOB mob)
	{
        // insures we only try this once!
		Behavior B;
        for(int b=0;b<mob.numBehaviors();b++)
        {
            B=mob.fetchBehavior(b);
            if((B==null)||(B==this))
            	return true;
            else
            if(B instanceof CombatAbilities)
                return false;
        }
        return true;
	}

	protected Ability useSkill(MOB mob, MOB victim, MOB leader) throws CMException
	{
		int tries=0;
		Ability tryThisOne=null;
		// now find a skill to use
		Ability A=null;
		

		MOB target = null;
        int victimQuality=Ability.QUALITY_INDIFFERENT;
        int selfQuality=Ability.QUALITY_INDIFFERENT;
        int leaderQuality=Ability.QUALITY_INDIFFERENT;
		while((tryThisOne==null)&&((++tries)<100)&&(mob.numAbilities()>0))
		{
			if((combatMode==COMBAT_ONLYALWAYS)&&(this.skillsAlways!=null)&&(this.skillsAlways.size()>0))
				A=mob.fetchAbility((String)skillsAlways.elementAt(CMLib.dice().roll(1,mob.numAbilities(),-1)));
			else
				A=mob.fetchAbility(CMLib.dice().roll(1,mob.numAbilities(),-1));
			
            if((A==null)
            ||(A.isAutoInvoked())
            ||(A.triggerStrings()==null)
            ||(A.triggerStrings().length==0)
            ||((skillsAlways!=null)&&(!skillsAlways.contains(A.ID())))
            ||((skillsNever!=null)&&(skillsNever.contains(A.ID()))))
                continue;
            
			victimQuality=(victim!=null)?A.castingQuality(mob,victim):Ability.QUALITY_INDIFFERENT;
            selfQuality=A.castingQuality(mob,mob);
            leaderQuality=((mob==leader)||(leader==null))?Ability.QUALITY_INDIFFERENT:A.castingQuality(mob,leader);

			if(victimQuality==Ability.QUALITY_MALICIOUS)
			{
				switch(combatMode)
				{
				case COMBAT_RANDOM:
				case COMBAT_ONLYALWAYS:
	                tryThisOne=A;
					break;
				case COMBAT_DEFENSIVE:
					if(CMLib.dice().rollPercentage()<=5)
		                tryThisOne=A;
					break;
				case COMBAT_OFFENSIVE:
	                tryThisOne=A;
					break;
				case COMBAT_MIXEDOFFENSIVE:
					if(CMLib.dice().rollPercentage()<=75)
		                tryThisOne=A;
					break;
				case COMBAT_MIXEDDEFENSIVE:
					if(CMLib.dice().rollPercentage()<=25)
		                tryThisOne=A;
					break;
				}
			}
			else
            if((selfQuality==Ability.QUALITY_BENEFICIAL_SELF)
            ||(leaderQuality==Ability.QUALITY_BENEFICIAL_OTHERS))
			{
				switch(combatMode)
				{
				case COMBAT_RANDOM:
				case COMBAT_ONLYALWAYS:
	                tryThisOne=A;
					break;
				case COMBAT_DEFENSIVE:
	                tryThisOne=A;
					break;
				case COMBAT_OFFENSIVE:
					if(CMLib.dice().rollPercentage()<=5)
		                tryThisOne=A;
					break;
				case COMBAT_MIXEDOFFENSIVE:
					if(CMLib.dice().rollPercentage()<=25)
		                tryThisOne=A;
					break;
				case COMBAT_MIXEDDEFENSIVE:
					if(CMLib.dice().rollPercentage()<=75)
		                tryThisOne=A;
					break;
				}
			}
			target=victim;
			if(selfQuality==Ability.QUALITY_BENEFICIAL_SELF)
			    target=mob;
			else
			if(leaderQuality==Ability.QUALITY_BENEFICIAL_OTHERS)
			    target=((leader==null)||(mob.location()!=leader.location()))?mob:leader;
			if((target != null) && (tryThisOne != null) && (target.fetchEffect(tryThisOne.ID())!=null))
				tryThisOne = null;
		}

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
                	   throw new CMException("Not enough mana");
				   mob.curState().adjMana(tryThisOne.usageCost(mob,false)[0],mob.maxState());
				}
				mob.curState().adjMana(5,mob.maxState());
			}
			if(CMath.bset(tryThisOne.usageType(),Ability.USAGE_MOVEMENT))
			{
				if((Math.random()>CMath.div(mob.curState().getMovement(),mob.maxState().getMovement()))
				||(mob.curState().getMovement()<tryThisOne.usageCost(mob,false)[1]))
             	   throw new CMException("Not enough movement");
				mob.curState().adjMovement(5,mob.maxState());
			}
			if(CMath.bset(tryThisOne.usageType(),Ability.USAGE_HITPOINTS))
			{
				if((Math.random()>CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()))
				   ||(mob.curState().getHitPoints()<tryThisOne.usageCost(mob,false)[2]))
	             	   throw new CMException("Not enough hp");
			}
			
			if(proficient)
                tryThisOne.setProficiency(100);
			else
			{
				int qualLevel=CMLib.ableMapper().qualifyingLevel(mob,tryThisOne);
				if(qualLevel<=0)
	                tryThisOne.setProficiency(75);
				else
				{
					int levelDiff=mob.baseEnvStats().level()-qualLevel;
					if((levelDiff>50)||(levelDiff<0)) levelDiff=50;
					tryThisOne.setProficiency(50+levelDiff);
				}
			}
			if(target==null) return null;
			boolean skillUsed=tryThisOne.invoke(mob,CMParms.makeVector(target.name()),null,false,0);
			if((combatMode==COMBAT_ONLYALWAYS)&&(!skillUsed))
			{
				int retries=0;
				while((++retries<10)&&(!skillUsed))
					skillUsed=tryThisOne.invoke(mob,CMParms.makeVector(target.name()),null,false,0);
			}
			if(skillUsed)
			{
			    skillUsed=true;
				if(lastSpell!=null)
				    lastSpell=tryThisOne.ID();
			}
			else
			{
	            if(lastSpell!=null)
    	            lastSpell="!"+tryThisOne.ID();
                if(record!=null) 
                    record.append("!");
			}
			if(record!=null) 
				record.append(tryThisOne.ID()).append("; ");
			return skillUsed?tryThisOne:null;
		}
		return null;
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
		    {
		    	synchronized(aggro)
		    	{
			        aggro=null;
		    	}
		    }
		    if((preCastSet < Integer.MAX_VALUE) && (preCastSet >0) && ((--preCastDown)<=0))
		    {
		    	preCastDown=preCastSet;
				if(!isRightCombatAbilities(mob))
					return true;
				try {
					useSkill(mob,null,null);
				} catch(CMException cme){}
		    }
		    return true;
		}
		MOB victim=mob.getVictim();
		if(victim==null) return true;
		
        // insures we only try this once!
		if(!isRightCombatAbilities(mob))
			return true;

        Room R=mob.location();
        if((lastSpell!=null)&&(lastSpell.length()>0))
            lastSpell="";
        
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
                if(!I.amWearingAt(Wearable.IN_INVENTORY))
                    myWand=I;
                else
                    backupWand=I;
            }
            if(I instanceof Weapon)
            {
                if((((Weapon)I).minRange()>rtt)
                ||(((Weapon)I).maxRange()<rtt))
                {
                    if(I.amWearingAt(Wearable.WORN_WIELD))
                        wieldedItem=I;
                    else
                    if((wieldMe==null)||(I.amWearingAt(Wearable.WORN_HELD)))
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
				int winAmt=0;
				MOB winMOB = null;
				int vicAmt=0;
				int minAmt=mob.maxState().getHitPoints()/10;
				if(aggro.containsKey(victim))
					vicAmt = aggro.get(victim)[0];
				int[] amt = null;
				for(MOB M : aggro.keySet())
				{
					amt = aggro.get(M);
					if((amt[0]>winAmt)
					&&(CMLib.flags().canBeSeenBy(M,mob)))
					{
						winAmt=amt[0];
						winMOB=M;
					}
				}
				if((winAmt>minAmt)
				&&(winAmt>(vicAmt+(vicAmt/2)))
				&&(winMOB!=null)
				&&(!winMOB.amDead())
				&&(winMOB.isInCombat())
	            &&(!mob.getGroupMembers(new HashSet()).contains(winMOB)))
				{
					mob.setVictim(winMOB);
					victim=mob.getVictim();
					aggro.clear();
				}
			}
		}
		if(victim==null) return true;
		
		MOB leader=mob.amFollowing();
		
		boolean skillUsed=false;
		try {
			skillUsed=useSkill(mob, victim, leader)!=null;
		} catch(CMException cme) { return true;}
		
		Ability A=null;
		// if a skill use failed, take a stab at wanding
		if((!skillUsed)
        &&(wandUseCheck[1])
		&&(victim.location()!=null)
		&&(!victim.amDead())
		&&((myWand!=null)||(backupWand!=null)))
		{
			if((myWand==null)&&(backupWand!=null)&&(backupWand.canWear(mob,Wearable.WORN_HELD)))
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
                MOB target=null;
				if(A!=null)
				{
                    if(A.castingQuality(mob,mob)==Ability.QUALITY_BENEFICIAL_SELF)
                        target=mob;
                    else
                    if(A.castingQuality(mob,victim)==Ability.QUALITY_MALICIOUS)
                        target=victim;
                    else
                    if(((mob!=leader)&&(leader!=null))&&(A.castingQuality(mob,leader)==Ability.QUALITY_BENEFICIAL_OTHERS))
                        target=((leader==null)||(mob.location()!=leader.location()))?mob:leader;
				}
				if(target!=null)
				{
					Vector V=new Vector();
					V.addElement("sayto");
					V.addElement(target.name());
					V.addElement(((Wand)myWand).magicWord());
					mob.doCommand(V,Command.METAFLAG_FORCED);
				}
			}
		}
		return true;
	}
	
	
    protected static String[] CODES=null;
    public String[] getStatCodes(){
        if(CombatAbilities.CODES==null)
        {
            String[] superCodes=super.getStatCodes();
            CODES=new String[superCodes.length+8];
            for(int c=0;c<superCodes.length;c++)
                CODES[c]=superCodes[c];
            CODES[CODES.length-8]="RECORD";
            CODES[CODES.length-7]="PROF";
            CODES[CODES.length-6]="LASTSPELL";
            CODES[CODES.length-5]="PRECAST";
            CODES[CODES.length-4]="PHYSDAMTAKEN";
            CODES[CODES.length-3]="SKILLSALWAYS";
            CODES[CODES.length-2]="SKILLSNEVER";
            CODES[CODES.length-1]="COMBATMODE";
        }
        return CODES;
    }
    protected int getCodeNum(String code){
        String[] CODES=getStatCodes();
        for(int i=0;i<CODES.length;i++)
            if(code.equalsIgnoreCase(CODES[i])) return i;
        return -1;
    }
    public String getStat(String code){
        int x=getCodeNum(code);
        if(x<super.getStatCodes().length)
            return super.getStat(code);
        x=x-super.getStatCodes().length;
        switch(x)
        {
        case 0: return (record==null)?"":record.toString();
        case 1: return Boolean.toString(proficient);
        case 2: return lastSpell!=null?lastSpell:"";
        case 3: return Integer.toString(preCastSet);
        case 4: return Integer.toString(physicalDamageTaken);
        case 5: return (skillsAlways==null)?"":CMParms.toSemicolonList(skillsAlways);
        case 6: return (skillsAlways==null)?"":CMParms.toSemicolonList(skillsNever);
        case 7: return Integer.toString(combatMode);
        }
        return "";
    }
    public void setStat(String code, String val)
    {
        int x=getCodeNum(code);
        if(x<super.getStatCodes().length)
            super.setStat(code,val);
        x=x-super.getStatCodes().length;
        switch(x)
        {
        case 0:
            if(val.length()==0)
                record=null;
            else
                record=new StringBuffer(val.trim());
            break;
        case 1:
            proficient=CMath.s_bool(val);
            break;
        case 2:
            lastSpell=val;
            break;
        case 3:
        	preCastSet=CMath.s_int(val);
        	preCastDown=CMath.s_int(val);
        	break;
        case 4:
        	physicalDamageTaken=CMath.s_int(val);
        	break;
        case 5:
        	skillsAlways=CMParms.parseSemicolons(val,true);
        	if(skillsAlways.size()==0) skillsAlways=null;
        	break;
        case 6:
        	skillsNever=CMParms.parseSemicolons(val,true);
        	if(skillsNever.size()==0) skillsNever=null;
        	break;
        case 7:
        	if(CMath.isInteger(val))
	        	combatMode=CMath.s_int(val);
        	else
        		combatMode=CMParms.indexOf(names,val.toUpperCase().trim());
        	break;
        }
    }
}
