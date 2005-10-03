package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/*
* <p>Portions Copyright (c) 2003 Jeremy Vyska</p>
* <p>Portions Copyright (c) 2004 Bo Zimmerman</p>

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
public class Test extends StdCommand
{
    public Test(){}

    private String[] access={"Test"};
    public String[] getAccessWords(){return access;}
    
    public static final String[] spells={"Spell_Blur","Spell_ResistMagicMissiles"};
    public static String semiSpellList=null;
    public static String semiSpellList()
    {
        if(semiSpellList!=null) return semiSpellList;
        StringBuffer str=new StringBuffer("");
        for(int i=0;i<spells.length;i++)
            str.append(spells[i]+";");
        semiSpellList=str.toString();
        return semiSpellList;
    }

    public boolean isAllAdjusted(MOB mob)
    {
        //System.out.println("1-"+mob.envStats().ability());        
        if(mob.envStats().ability()<10)
            return false;
        //System.out.println("2-"+(char)mob.charStats().getStat(CharStats.GENDER));        
        if(mob.charStats().getStat(CharStats.GENDER)!='F')
            return false;
        //System.out.println("3-"+mob.charStats().getCurrentClass().ID());        
        if(!mob.charStats().getCurrentClass().ID().equals("Fighter"))
            return false;
        //System.out.println("4-"+mob.charStats().getStat(CharStats.CHARISMA));        
        if(mob.charStats().getStat(CharStats.CHARISMA)<18)
            return false;
        //System.out.println("5-"+mob.maxState().getMana());        
        if(mob.maxState().getMana()<1000)
            return false;
        //System.out.println("6-!");        
        return true;
    }
    public boolean isAnyAdjusted(MOB mob)
    {
        //System.out.println("7-"+mob.envStats().ability());        
        if(mob.envStats().ability()>=10)
            return true;
        //System.out.println("8-"+(char)mob.charStats().getStat(CharStats.GENDER));        
        if(mob.charStats().getStat(CharStats.GENDER)=='F')
            return true;
        //System.out.println("9-"+mob.charStats().getCurrentClass().ID());        
        if(mob.charStats().getCurrentClass().ID().equals("Fighter"))
            return true;
        //System.out.println("10-"+mob.charStats().getStat(CharStats.CHARISMA));        
        if(mob.charStats().getStat(CharStats.CHARISMA)>=18)
            return true;
        //System.out.println("11-"+mob.maxState().getMana());        
        if(mob.maxState().getMana()>=1000)
            return true;
        //System.out.println("12-!");        
        return false;
    }
    
    
    public void giveAbility(Environmental E, Ability A)
    {
        Ability A2=((Ability)A.copyOf());
        A2.setMiscText(A.text());
        E.addNonUninvokableEffect(A2);
    }
    
    public boolean testResistance(MOB mob)
    {
        //Resistance.setMiscText("pierce 100% holy 100% acid 10%");
        Item I=CMClass.getWeapon("Dagger");
        mob.curState().setHitPoints(mob.maxState().getHitPoints());
        int curHitPoints=mob.curState().getHitPoints();
        MUDFight.postDamage(mob,mob,I,5,CMMsg.MSG_WEAPONATTACK,Weapon.TYPE_PIERCING,"<S-NAME> <DAMAGE> <T-NAME>");
        if(mob.curState().getHitPoints()<curHitPoints-3)
            return false;
        curHitPoints=mob.curState().getHitPoints();
        Factions.setAlignmentOldRange(mob,0);
        Ability A=CMClass.getAbility("Prayer_DispelEvil");
        A.invoke(mob,mob,true,1);
        if(mob.curState().getHitPoints()<curHitPoints)
            return false;
        curHitPoints=mob.curState().getHitPoints();
        if(mob.charStats().getStat(CharStats.SAVE_ACID)<30)
            return false;
        return true;
    }

    
    public Item[] giveTo(Item I, Ability A, MOB mob1, MOB mob2, int code)
    {
        Item[] IS=new Item[2];
        Item I1=(Item)I.copyOf();
        giveAbility(I1,A);
        if(code<2)
        {
            mob1.addInventory(I1);
            if(code==1) I1.wearEvenIfImpossible(mob1);
        }
        else
        {
            mob1.location().addItem(I1);
            if((I1 instanceof Rideable)&&(code==2))
                mob1.setRiding((Rideable)I1);
        }
            
        IS[0]=I1;
        
        Item I2=(Item)I.copyOf();
        giveAbility(I2,A);
        if(mob2!=null)
        {
            if(code<2)
            {
                mob2.addInventory(I2);
                if(code==1) I2.wearEvenIfImpossible(mob2);
            }
            else
            {
                mob2.location().addItem(I2);
                if((I2 instanceof Rideable)&&(code==2))
                    mob2.setRiding((Rideable)I2);
            }
        }
        IS[1]=I2;
        mob1.location().recoverRoomStats();
        return IS;
    }
    
    public boolean spellCheck(MOB mob)
    {
        for(int i=0;i<spells.length;i++)
            if(mob.fetchAbility(spells[i])==null)
                return false;
        return true;
    }

    public boolean effectCheck(MOB mob)
    {
        for(int i=0;i<spells.length;i++)
            if(mob.fetchEffect(spells[i])==null)
                return false;
        return true;
    }

    public void reset(MOB[] mobs,MOB[] backups, Room R, Item[] IS,Room R2)
    {
        while(R2.numEffects()>0)
        {
            int num=R2.numEffects();
            R2.fetchEffect(0).unInvoke();
            if(num==R2.numEffects())
                R2.delEffect(R2.fetchEffect(0));
        }
        if(IS!=null)
        {
            if(IS[0]!=null)
                IS[0].destroy();
            if(IS[1]!=null)
                IS[1].destroy();
        }
        if(mobs[0]!=null)
            mobs[0].destroy();
        if(mobs[1]!=null)
            mobs[1].destroy();
        R.recoverRoomStats();
        mobs[0]=CMClass.getMOB("StdMOB");
        mobs[0].baseCharStats().setMyRace(CMClass.getRace("Dwarf"));
        mobs[0].setName("A Dwarf");
        mobs[0].recoverCharStats();
        backups[0]=(MOB)mobs[0].copyOf();
        mobs[1]=CMClass.getMOB("StdMOB");
        mobs[1].setName("A Human");
        mobs[1].baseCharStats().setMyRace(CMClass.getRace("Human"));
        mobs[1].recoverCharStats();
        backups[0]=(MOB)mobs[1].copyOf();
        
        mobs[0].bringToLife(R,true);
        mobs[1].bringToLife(R,true);
    }
    
    public boolean execute(MOB mob, Vector commands)
        throws java.io.IOException
    {
        if(commands.size()>1)
        {
            String what=((String)commands.elementAt(1)).toUpperCase().trim();
            Ability A2=null;
            Item I=null;
            FullMsg msg=null;
            Command C=null;
            Item IS[]=new Item[2];
            Room R=mob.location();
            Room upRoom=R.rawDoors()[Directions.UP];
            Exit upExit=R.rawExits()[Directions.UP];
            Room R2=CMClass.getLocale("StoneRoom");
            R2.setArea(R.getArea());
            R.rawExits()[Directions.UP]=CMClass.getExit("Open");
            R2.rawExits()[Directions.DOWN]=CMClass.getExit("Open");
            R.rawDoors()[Directions.UP]=R2;
            R2.rawDoors()[Directions.DOWN]=R;
            MOB[] mobs=new MOB[2];
            MOB[] backups=new MOB[2];
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_HaveEnabler")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability HaveEnabler=CMClass.getAbility("Prop_HaveEnabler");
                HaveEnabler.setMiscText(semiSpellList());
                mob.tell("Test#1-1: "+HaveEnabler.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),HaveEnabler,mobs[0],null,0);
                if(!spellCheck(mobs[0])){ mob.tell("Error1-1"); return false;}
                IS[0].unWear();
                R.bringItemHere(IS[0],0);
                R.recoverRoomStats();
                if(spellCheck(mobs[0])){ mob.tell("Error1-2"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                HaveEnabler.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
                mob.tell("Test#1-2: "+HaveEnabler.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),HaveEnabler,mobs[0],mobs[1],0);
                if(!spellCheck(mobs[0])){ mob.tell("Error1-3"); return false;}
                if(spellCheck(mobs[1])){ mob.tell("Error1-4"); return false;}
                IS[0].unWear();
                IS[1].unWear();
                R.bringItemHere(IS[0],0);
                R.bringItemHere(IS[1],0);
                R.recoverRoomStats();
                if(spellCheck(mobs[0])){ mob.tell("Error1-5"); return false;}
                if(spellCheck(mobs[1])){ mob.tell("Error1-6"); return false;}
            }
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_HaveSpellCast")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability HaveSpellCast=CMClass.getAbility("Prop_HaveSpellCast");
                HaveSpellCast.setMiscText(semiSpellList());
                mob.tell("Test#2-1: "+HaveSpellCast.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),HaveSpellCast,mobs[0],null,0);
                if(!effectCheck(mobs[0])){ mob.tell("Error2-1"); return false;}
                IS[0].unWear();
                R.bringItemHere(IS[0],0);
                R.recoverRoomStats();
                if(effectCheck(mobs[0])){ mob.tell("Error2-2"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                HaveSpellCast.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
                mob.tell("Test#2-2: "+HaveSpellCast.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),HaveSpellCast,mobs[0],mobs[1],0);
                if(!effectCheck(mobs[0])){ mob.tell("Error2-3"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error2-4"); return false;}
                IS[0].unWear();
                IS[1].unWear();
                R.bringItemHere(IS[0],0);
                R.bringItemHere(IS[1],0);
                R.recoverRoomStats();
                if(effectCheck(mobs[0])){ mob.tell("Error2-5"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error2-6"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                HaveSpellCast.setMiscText(semiSpellList()+"MASK=-Human");
                mob.tell("Test#2-3: "+HaveSpellCast.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),HaveSpellCast,mobs[0],mobs[1],0);
                if(!effectCheck(mobs[0])){ mob.tell("Error2-7"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error2-8"); return false;}
                IS[0].unWear();
                IS[1].unWear();
                R.bringItemHere(IS[0],0);
                R.bringItemHere(IS[1],0);
                R.recoverRoomStats();
                if(effectCheck(mobs[0])){ mob.tell("Error2-9"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error2-10"); return false;}
            }
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_WearEnabler")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability WearEnabler=CMClass.getAbility("Prop_WearEnabler");
                WearEnabler.setMiscText(semiSpellList());
                mob.tell("Test#3-1: "+WearEnabler.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),WearEnabler,mobs[0],null,1);
                if(!spellCheck(mobs[0])){ mob.tell("Error3-1"); return false;}
                IS[0].unWear();
                R.recoverRoomStats();
                if(spellCheck(mobs[0])){ mob.tell("Error3-2"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                WearEnabler.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
                mob.tell("Test#3-2: "+WearEnabler.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),WearEnabler,mobs[0],mobs[1],1);
                if(!spellCheck(mobs[0])){ mob.tell("Error3-3"); return false;}
                if(spellCheck(mobs[1])){ mob.tell("Error3-4"); return false;}
                IS[0].unWear();
                IS[1].unWear();
                R.recoverRoomStats();
                if(spellCheck(mobs[0])){ mob.tell("Error3-5"); return false;}
                if(spellCheck(mobs[1])){ mob.tell("Error3-6"); return false;}
            }
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_WearSpellCast")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability WearSpellCast=CMClass.getAbility("Prop_WearSpellCast");
                WearSpellCast.setMiscText(semiSpellList());
                mob.tell("Test#4-1: "+WearSpellCast.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),WearSpellCast,mobs[0],null,1);
                if(!effectCheck(mobs[0])){ mob.tell("Error4-1"); return false;}
                IS[0].unWear();
                R.recoverRoomStats();
                if(effectCheck(mobs[0])){ mob.tell("Error4-2"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                WearSpellCast.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
                mob.tell("Test#4-2: "+WearSpellCast.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),WearSpellCast,mobs[0],mobs[1],1);
                if(!effectCheck(mobs[0])){ mob.tell("Error4-3"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error4-4"); return false;}
                IS[0].unWear();
                IS[1].unWear();
                R.recoverRoomStats();
                if(effectCheck(mobs[0])){ mob.tell("Error4-5"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error4-6"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                WearSpellCast.setMiscText(semiSpellList()+"MASK=-Human");
                mob.tell("Test#4-3: "+WearSpellCast.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),WearSpellCast,mobs[0],mobs[1],1);
                if(!effectCheck(mobs[0])){ mob.tell("Error4-7"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error4-8"); return false;}
                IS[0].unWear();
                IS[1].unWear();
                R.recoverRoomStats();
                if(effectCheck(mobs[0])){ mob.tell("Error4-9"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error4-10"); return false;}
            }
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_RideEnabler")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability RideEnabler=CMClass.getAbility("Prop_RideEnabler");
                RideEnabler.setMiscText(semiSpellList());
                mob.tell("Test#5-1: "+RideEnabler.accountForYourself());
                IS=giveTo(CMClass.getItem("Boat"),RideEnabler,mobs[0],null,2);
                if(!spellCheck(mobs[0])){ mob.tell("Error5-1"); return false;}
                mobs[0].setRiding(null);
                R.recoverRoomStats();
                if(spellCheck(mobs[0])){ mob.tell("Error5-2"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                RideEnabler.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
                mob.tell("Test#5-2: "+RideEnabler.accountForYourself());
                IS=giveTo(CMClass.getItem("Boat"),RideEnabler,mobs[0],mobs[1],2);
                if(!spellCheck(mobs[0])){ mob.tell("Error5-3"); return false;}
                if(spellCheck(mobs[1])){ mob.tell("Error5-4"); return false;}
                mobs[0].setRiding(null);
                mobs[1].setRiding(null);
                R.recoverRoomStats();
                if(spellCheck(mobs[0])){ mob.tell("Error5-5"); return false;}
                if(spellCheck(mobs[1])){ mob.tell("Error5-6"); return false;}
            }
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_RideSpellCast")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability RideSpellCast=CMClass.getAbility("Prop_RideSpellCast");
                RideSpellCast.setMiscText(semiSpellList());
                mob.tell("Test#6-1: "+RideSpellCast.accountForYourself());
                IS=giveTo(CMClass.getItem("Boat"),RideSpellCast,mobs[0],null,2);
                if(!effectCheck(mobs[0])){ mob.tell("Error6-1"); return false;}
                mobs[0].setRiding(null);
                R.recoverRoomStats();
                if(effectCheck(mobs[0])){ mob.tell("Error6-2"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                RideSpellCast.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
                mob.tell("Test#6-2: "+RideSpellCast.accountForYourself());
                IS=giveTo(CMClass.getItem("Boat"),RideSpellCast,mobs[0],mobs[1],2);
                if(!effectCheck(mobs[0])){ mob.tell("Error6-3"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error6-4"); return false;}
                mobs[0].setRiding(null);
                mobs[1].setRiding(null);
                R.recoverRoomStats();
                if(effectCheck(mobs[0])){ mob.tell("Error6-5"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error6-6"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                RideSpellCast.setMiscText(semiSpellList()+"MASK=-Human");
                mob.tell("Test#6-3: "+RideSpellCast.accountForYourself());
                IS=giveTo(CMClass.getItem("Boat"),RideSpellCast,mobs[0],mobs[1],2);
                if(!effectCheck(mobs[0])){ mob.tell("Error6-7"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error6-8"); return false;}
                mobs[0].setRiding(null);
                mobs[1].setRiding(null);
                R.recoverRoomStats();
                if(effectCheck(mobs[0])){ mob.tell("Error6-9"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error6-10"); return false;}
            }
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_HereSpellCast")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability HereSpellCast=CMClass.getAbility("Prop_HereSpellCast");
                HereSpellCast.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
                mob.tell("Test#7-1: "+HereSpellCast.accountForYourself());
                A2=((Ability)HereSpellCast.copyOf());
                A2.setMiscText((HereSpellCast).text());
                R2.addNonUninvokableEffect(A2);
                R2.recoverRoomStats();
                MUDTracker.move(mobs[0],Directions.UP,false,false);
                if(!effectCheck(mobs[0])){ mob.tell("Error7-1"); return false;}
                MUDTracker.move(mobs[0],Directions.DOWN,false,false);
                if(effectCheck(mobs[0])){ mob.tell("Error7-2"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                HereSpellCast.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
                mob.tell("Test#7-2: "+HereSpellCast.accountForYourself());
                A2=((Ability)HereSpellCast.copyOf());
                A2.setMiscText((HereSpellCast).text());
                R2.addNonUninvokableEffect(A2);
                R2.recoverRoomStats();
                MUDTracker.move(mobs[0],Directions.UP,false,false);
                MUDTracker.move(mobs[1],Directions.UP,false,false);
                if(!effectCheck(mobs[0])){ mob.tell("Error7-3"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error7-4"); return false;}
                MUDTracker.move(mobs[0],Directions.DOWN,false,false);
                MUDTracker.move(mobs[1],Directions.DOWN,false,false);
                if(effectCheck(mobs[0])){ mob.tell("Error7-5"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error7-6"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                HereSpellCast.setMiscText(semiSpellList()+"MASK=-Human");
                mob.tell("Test#7-3: "+HereSpellCast.accountForYourself());
                A2=((Ability)HereSpellCast.copyOf());
                A2.setMiscText((HereSpellCast).text());
                R2.addNonUninvokableEffect(A2);
                R2.recoverRoomStats();
                MUDTracker.move(mobs[0],Directions.UP,false,false);
                MUDTracker.move(mobs[1],Directions.UP,false,false);
                if(!effectCheck(mobs[0])){ mob.tell("Error7-7"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error7-8"); return false;}
                MUDTracker.move(mobs[0],Directions.DOWN,false,false);
                MUDTracker.move(mobs[1],Directions.DOWN,false,false);
                if(effectCheck(mobs[0])){ mob.tell("Error7-9"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error7-10"); return false;}
            }
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_SpellAdder")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability SpellAdder=CMClass.getAbility("Prop_SpellAdder");
                SpellAdder.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
                mob.tell("Test#8-1: "+SpellAdder.accountForYourself());
                R2.addNonUninvokableEffect(SpellAdder);
                R2.recoverRoomStats();
                MUDTracker.move(mobs[0],Directions.UP,false,false);
                if(!effectCheck(mobs[0])){ mob.tell("Error8-1"); return false;}
                MUDTracker.move(mobs[0],Directions.DOWN,false,false);
                if(effectCheck(mobs[0])){ mob.tell("Error8-2"); return false;}
            }
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_UseSpellCast")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability UseSpellCast=CMClass.getAbility("Prop_UseSpellCast"); // put IN
                UseSpellCast.setMiscText(semiSpellList());
                mob.tell("Test#9-1: "+UseSpellCast.accountForYourself());
                IS=giveTo(CMClass.getItem("SmallSack"),UseSpellCast,mobs[0],null,0);
                I=CMClass.getItem("StdFood");
                mobs[0].addInventory(I);
                C=CMClass.getCommand("Put");
                C.execute(mobs[0],Util.makeVector("Put","Food","Sack"));
                if(!effectCheck(mobs[0])){ mob.tell("Error9-1"); return false;}
                R.recoverRoomStats();
                
                reset(mobs,backups,R,IS,R2);
                UseSpellCast.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
                mob.tell("Test#9-2: "+UseSpellCast.accountForYourself());
                IS=giveTo(CMClass.getItem("SmallSack"),UseSpellCast,mobs[0],mobs[1],0);
                I=CMClass.getItem("StdFood");
                mobs[0].addInventory(I);
                C=CMClass.getCommand("Put");
                C.execute(mobs[0],Util.makeVector("Put","Food","Sack"));
                if(!effectCheck(mobs[0])){ mob.tell("Error9-2"); return false;}
                I=CMClass.getItem("StdFood");
                mobs[1].addInventory(I);
                C=CMClass.getCommand("Put");
                C.execute(mobs[1],Util.makeVector("Put","Food","Sack"));
                if(effectCheck(mobs[1])){ mob.tell("Error9-3"); return false;}
                R.recoverRoomStats();
            }
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_UseSpellCast2")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability UseSpellCast2=CMClass.getAbility("Prop_UseSpellCast2"); // EAT
                UseSpellCast2.setMiscText(semiSpellList());
                mob.tell("Test#10-1: "+UseSpellCast2.accountForYourself());
                IS=giveTo(CMClass.getItem("StdFood"),UseSpellCast2,mobs[0],null,0);
                C=CMClass.getCommand("Eat");
                C.execute(mobs[0],Util.makeVector("Eat","ALL"));
                if(!effectCheck(mobs[0])){ mob.tell("Error10-1"); return false;}
                R.recoverRoomStats();
                
                reset(mobs,backups,R,IS,R2);
                UseSpellCast2.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
                mob.tell("Test#10-2: "+UseSpellCast2.accountForYourself());
                IS=giveTo(CMClass.getItem("StdFood"),UseSpellCast2,mobs[0],mobs[1],0);
                C=CMClass.getCommand("Eat");
                C.execute(mobs[0],Util.makeVector("Eat","ALL"));
                if(!effectCheck(mobs[0])){ mob.tell("Error10-2"); return false;}
                C=CMClass.getCommand("Eat");
                C.execute(mobs[1],Util.makeVector("Eat","ALL"));
                if(effectCheck(mobs[1])){ mob.tell("Error10-3"); return false;}
                R.recoverRoomStats();
            }
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_FightSpellCast")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability FightSpellCast=CMClass.getAbility("Prop_FightSpellCast");
                FightSpellCast.setMiscText(semiSpellList());
                mob.tell("Test#11-1: "+FightSpellCast.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),FightSpellCast,mobs[0],null,1);
                if(effectCheck(mobs[1])){ mob.tell("Error11-1"); return false;}
                if(effectCheck(mobs[0])){ mob.tell("Error11-2"); return false;}
                for(int i=0;i<100;i++)
                {
                    MUDFight.postAttack(mobs[0],mobs[1],mobs[0].fetchWieldedItem());
                    if(effectCheck(mobs[1]))
                        break;
                }
                if(!effectCheck(mobs[1])){ mob.tell("Error11-3"); return false;}
                R.recoverRoomStats();
                
                reset(mobs,backups,R,IS,R2);
                FightSpellCast.setMiscText(semiSpellList()+"MASK=-RACE +Human");
                mob.tell("Test#11-2: "+FightSpellCast.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),FightSpellCast,mobs[0],null,1);
                if(effectCheck(mobs[1])){ mob.tell("Error11-4"); return false;}
                if(effectCheck(mobs[0])){ mob.tell("Error11-5"); return false;}
                for(int i=0;i<100;i++)
                {
                    MUDFight.postAttack(mobs[0],mobs[1],mobs[0].fetchWieldedItem());
                    if(effectCheck(mobs[1]))
                        break;
                }
                if(effectCheck(mobs[1])){ mob.tell("Error11-6"); return false;}
                R.recoverRoomStats();
                
                reset(mobs,backups,R,IS,R2);
                FightSpellCast.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
                mob.tell("Test#11-3: "+FightSpellCast.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),FightSpellCast,mobs[0],null,1);
                if(effectCheck(mobs[1])){ mob.tell("Error11-7"); return false;}
                if(effectCheck(mobs[0])){ mob.tell("Error11-8"); return false;}
                for(int i=0;i<100;i++)
                {
                    MUDFight.postAttack(mobs[0],mobs[1],mobs[0].fetchWieldedItem());
                    if(effectCheck(mobs[1]))
                        break;
                }
                if(!effectCheck(mobs[1])){ mob.tell("Error11-9"); return false;}
                R.recoverRoomStats();
            }
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_HaveZapper")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability HaveZapper=CMClass.getAbility("Prop_HaveZapper");
                HaveZapper.setMiscText("-RACE +Dwarf");
                mob.tell("Test#12-1: "+HaveZapper.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),HaveZapper,mobs[0],mobs[1],2);
                CommonMsgs.get(mobs[0],null,IS[0],false);
                CommonMsgs.get(mobs[1],null,IS[1],false);
                if(!mobs[0].isMine(IS[0])){ mob.tell("Error12-1"); return false;}
                if(mobs[1].isMine(IS[1])){ mob.tell("Error12-2"); return false;}
            }
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_RideZapper")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability RideZapper=CMClass.getAbility("Prop_RideZapper");
                RideZapper.setMiscText("-RACE +Dwarf");
                mob.tell("Test#13-1: "+RideZapper.accountForYourself());
                IS=giveTo(CMClass.getItem("Boat"),RideZapper,mobs[0],mobs[1],3);
                msg=new FullMsg(mobs[0],IS[0],null,CMMsg.MSG_MOUNT,"<S-NAME> mount(s) <T-NAMESELF>.");
                if(R.okMessage(mobs[0],msg)) R.send(mobs[0],msg);
                msg=new FullMsg(mobs[1],IS[1],null,CMMsg.MSG_MOUNT,"<S-NAME> mount(s) <T-NAMESELF>.");
                if(R.okMessage(mobs[1],msg)) R.send(mobs[1],msg);
                if(mobs[0].riding()!=IS[0]){ mob.tell("Error13-1"); return false;}
                if(mobs[1].riding()==IS[1]){ mob.tell("Error13-2"); return false;}
            }
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_WearZapper")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability WearZapper=CMClass.getAbility("Prop_WearZapper");
                WearZapper.setMiscText("-RACE +Dwarf");
                mob.tell("Test#14-1: "+WearZapper.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),WearZapper,mobs[0],mobs[1],0);
                msg=new FullMsg(mobs[0],IS[0],null,CMMsg.MSG_WIELD,"<S-NAME> wield(s) <T-NAMESELF>.");
                if(R.okMessage(mobs[0],msg)) R.send(mobs[0],msg);
                msg=new FullMsg(mobs[1],IS[1],null,CMMsg.MSG_WIELD,"<S-NAME> wield(s) <T-NAMESELF>.");
                if(R.okMessage(mobs[1],msg)) R.send(mobs[1],msg);
                if(IS[0].amWearingAt(Item.INVENTORY)){ mob.tell("Error14-1"); return false;}
                if(!IS[1].amWearingAt(Item.INVENTORY)){ mob.tell("Error14-2"); return false;}
            }
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_Resistance")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability Resistance=CMClass.getAbility("Prop_Resistance");
                Resistance.setMiscText("pierce 100% holy 100% acid 30%");
                mob.tell("Test#15-1: "+Resistance.accountForYourself());
                if(testResistance(mobs[0])){ mob.tell("Error15-1"); return false;}
                giveAbility(mobs[0],Resistance);
                R.recoverRoomStats();
                if(!testResistance(mobs[0])){ mob.tell("Error15-2"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                Resistance.setMiscText("pierce 100% holy 100% acid 30% MASK=-RACE +DWARF");
                mob.tell("Test#15-2: "+Resistance.accountForYourself());
                if(testResistance(mobs[0])){ mob.tell("Error15-3"); return false;}
                if(testResistance(mobs[1])){ mob.tell("Error15-4"); return false;}
                giveAbility(mobs[0],Resistance);
                giveAbility(mobs[1],Resistance);
                R.recoverRoomStats();
                if(!testResistance(mobs[0])){ mob.tell("Error15-5"); return false;}
                if(testResistance(mobs[1])){ mob.tell("Error15-6"); return false;}
            }
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_HaveResister")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability HaveResister=CMClass.getAbility("Prop_HaveResister");
                HaveResister.setMiscText("pierce 100% holy 100% acid 30%");
                mob.tell("Test#16-1: "+HaveResister.accountForYourself());
                if(testResistance(mobs[0])){ mob.tell("Error16-1"); return false;}
                IS=giveTo(CMClass.getItem("SmallSack"),HaveResister,mobs[0],null,0);
                R.recoverRoomStats();
                if(!testResistance(mobs[0])){ mob.tell("Error16-2"); return false;}
                IS[0].unWear();
                R.bringItemHere(IS[0],0);
                R.recoverRoomStats();
                if(testResistance(mobs[0])){ mob.tell("Error16-3"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                HaveResister.setMiscText("pierce 100% holy 100% acid 30% MASK=-RACE +DWARF");
                mob.tell("Test#16-2: "+HaveResister.accountForYourself());
                if(testResistance(mobs[0])){ mob.tell("Error16-4"); return false;}
                if(testResistance(mobs[1])){ mob.tell("Error16-5"); return false;}
                IS=giveTo(CMClass.getItem("SmallSack"),HaveResister,mobs[0],mobs[1],0);
                R.recoverRoomStats();
                if(!testResistance(mobs[0])){ mob.tell("Error16-6"); return false;}
                if(testResistance(mobs[1])){ mob.tell("Error16-7"); return false;}
                IS[0].unWear();
                IS[1].unWear();
                R.bringItemHere(IS[0],0);
                R.bringItemHere(IS[1],0);
                R.recoverRoomStats();
                if(testResistance(mobs[0])){ mob.tell("Error16-8"); return false;}
                if(testResistance(mobs[1])){ mob.tell("Error16-9"); return false;}
            }
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_WearResister")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability WearResister=CMClass.getAbility("Prop_WearResister");
                WearResister.setMiscText("pierce 100% holy 100% acid 30%");
                mob.tell("Test#17-1: "+WearResister.accountForYourself());
                if(testResistance(mobs[0])){ mob.tell("Error17-1"); return false;}
                IS=giveTo(CMClass.getWeapon("Sword"),WearResister,mobs[0],null,1);
                R.recoverRoomStats();
                if(!testResistance(mobs[0])){ mob.tell("Error17-2"); return false;}
                IS[0].unWear();
                R.recoverRoomStats();
                if(testResistance(mobs[0])){ mob.tell("Error17-3"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                WearResister.setMiscText("pierce 100% holy 100% acid 30% MASK=-RACE +DWARF");
                mob.tell("Test#17-2: "+WearResister.accountForYourself());
                if(testResistance(mobs[0])){ mob.tell("Error17-4"); return false;}
                if(testResistance(mobs[1])){ mob.tell("Error17-5"); return false;}
                IS=giveTo(CMClass.getWeapon("Sword"),WearResister,mobs[0],mobs[1],1);
                R.recoverRoomStats();
                if(!testResistance(mobs[0])){ mob.tell("Error17-6"); return false;}
                if(testResistance(mobs[1])){ mob.tell("Error17-7"); return false;}
                IS[0].unWear();
                IS[1].unWear();
                R.recoverRoomStats();
                if(testResistance(mobs[0])){ mob.tell("Error17-8"); return false;}
                if(testResistance(mobs[1])){ mob.tell("Error17-9"); return false;}
            }
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_RideResister")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability RideResister=CMClass.getAbility("Prop_RideResister");
                RideResister.setMiscText("pierce 100% holy 100% acid 30%");
                mob.tell("Test#18-1: "+RideResister.accountForYourself());
                if(testResistance(mobs[0])){ mob.tell("Error18-1"); return false;}
                IS=giveTo(CMClass.getItem("Boat"),RideResister,mobs[0],null,2);
                R.recoverRoomStats();
                if(!testResistance(mobs[0])){ mob.tell("Error18-2"); return false;}
                mobs[0].setRiding(null);
                R.recoverRoomStats();
                if(testResistance(mobs[0])){ mob.tell("Error18-3"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                RideResister.setMiscText("pierce 100% holy 100% acid 30% MASK=-RACE +DWARF");
                mob.tell("Test#18-2: "+RideResister.accountForYourself());
                if(testResistance(mobs[0])){ mob.tell("Error18-4"); return false;}
                if(testResistance(mobs[1])){ mob.tell("Error18-5"); return false;}
                IS=giveTo(CMClass.getItem("Boat"),RideResister,mobs[0],mobs[1],2);
                R.recoverRoomStats();
                if(!testResistance(mobs[0])){ mob.tell("Error18-6"); return false;}
                if(testResistance(mobs[1])){ mob.tell("Error18-7"); return false;}
                mobs[0].setRiding(null);
                mobs[1].setRiding(null);
                R.recoverRoomStats();
                if(testResistance(mobs[0])){ mob.tell("Error18-8"); return false;}
                if(testResistance(mobs[1])){ mob.tell("Error18-9"); return false;}
            }
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_HaveAdjuster")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability HaveAdjuster=CMClass.getAbility("Prop_HaveAdjuster");
                HaveAdjuster.setMiscText("abi+10 gen=F class=Fighter cha+10 man+1000");
                mob.tell("Test#19-1: "+HaveAdjuster.accountForYourself());
                if(isAnyAdjusted(mobs[0])){ mob.tell("Error19-1"); return false;}
                IS=giveTo(CMClass.getItem("SmallSack"),HaveAdjuster,mobs[0],null,0);
                R.recoverRoomStats();
                if(!isAllAdjusted(mobs[0])){ mob.tell("Error19-2"); return false;}
                IS[0].unWear();
                R.bringItemHere(IS[0],0);
                R.recoverRoomStats();
                if(isAnyAdjusted(mobs[0])){ mob.tell("Error19-3"); return false;}
                
                HaveAdjuster.setMiscText("abi+10 gen=F class=Fighter cha+10 man+1000 MASK=-RACE +Dwarf");
                mob.tell("Test#19-2: "+HaveAdjuster.accountForYourself());
                if(isAnyAdjusted(mobs[0])){ mob.tell("Error19-4"); return false;}
                if(isAnyAdjusted(mobs[1])){ mob.tell("Error19-5"); return false;}
                IS=giveTo(CMClass.getItem("SmallSack"),HaveAdjuster,mobs[0],mobs[1],0);
                R.recoverRoomStats();
                if(!isAllAdjusted(mobs[0])){ mob.tell("Error19-6"); return false;}
                if(isAnyAdjusted(mobs[1])){ mob.tell("Error19-7"); return false;}
                IS[0].unWear();
                IS[1].unWear();
                R.bringItemHere(IS[0],0);
                R.bringItemHere(IS[1],0);
                R.recoverRoomStats();
                if(isAnyAdjusted(mobs[0])){ mob.tell("Error19-8"); return false;}
                if(isAnyAdjusted(mobs[0])){ mob.tell("Error19-9"); return false;}
            }
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_WearAdjuster")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability WearAdjuster=CMClass.getAbility("Prop_WearAdjuster");
                WearAdjuster.setMiscText("abi+10 gen=F class=Fighter cha+10 man+1000");
                mob.tell("Test#20-1: "+WearAdjuster.accountForYourself());
                if(isAnyAdjusted(mobs[0])){ mob.tell("Error20-1"); return false;}
                IS=giveTo(CMClass.getItem("SmallSack"),WearAdjuster,mobs[0],null,1);
                R.recoverRoomStats();
                if(!isAllAdjusted(mobs[0])){ mob.tell("Error20-2"); return false;}
                IS[0].unWear();
                R.recoverRoomStats();
                if(isAnyAdjusted(mobs[0])){ mob.tell("Error20-3"); return false;}
                
                WearAdjuster.setMiscText("abi+10 gen=F class=Fighter cha+10 man+1000 MASK=-RACE +Dwarf");
                mob.tell("Test#20-1: "+WearAdjuster.accountForYourself());
                if(isAnyAdjusted(mobs[0])){ mob.tell("Error20-4"); return false;}
                if(isAnyAdjusted(mobs[1])){ mob.tell("Error20-5"); return false;}
                IS=giveTo(CMClass.getItem("SmallSack"),WearAdjuster,mobs[0],mobs[1],1);
                R.recoverRoomStats();
                if(!isAllAdjusted(mobs[0])){ mob.tell("Error20-6"); return false;}
                if(isAnyAdjusted(mobs[1])){ mob.tell("Error20-7"); return false;}
                IS[0].unWear();
                IS[1].unWear();
                R.recoverRoomStats();
                if(isAnyAdjusted(mobs[0])){ mob.tell("Error20-8"); return false;}
                if(isAnyAdjusted(mobs[0])){ mob.tell("Error20-9"); return false;}
            }
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_RideAdjuster")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability RideAdjuster=CMClass.getAbility("Prop_RideAdjuster");
                RideAdjuster.setMiscText("abi+10 gen=F class=Fighter cha+10 man+1000");
                mob.tell("Test#21-1: "+RideAdjuster.accountForYourself());
                if(isAnyAdjusted(mobs[0])){ mob.tell("Error21-1"); return false;}
                IS=giveTo(CMClass.getItem("Boat"),RideAdjuster,mobs[0],null,2);
                R.recoverRoomStats();
                if(!isAllAdjusted(mobs[0])){ mob.tell("Error21-2"); return false;}
                mobs[0].setRiding(null);
                R.recoverRoomStats();
                if(isAnyAdjusted(mobs[0])){ mob.tell("Error21-3"); return false;}
                
                RideAdjuster.setMiscText("abi+10 gen=F class=Fighter cha+10 man+1000 MASK=-RACE +Dwarf");
                mob.tell("Test#21-1: "+RideAdjuster.accountForYourself());
                if(isAnyAdjusted(mobs[0])){ mob.tell("Error21-4"); return false;}
                if(isAnyAdjusted(mobs[1])){ mob.tell("Error21-5"); return false;}
                IS=giveTo(CMClass.getItem("Boat"),RideAdjuster,mobs[0],mobs[1],2);
                R.recoverRoomStats();
                if(!isAllAdjusted(mobs[0])){ mob.tell("Error21-6"); return false;}
                if(isAnyAdjusted(mobs[1])){ mob.tell("Error21-7"); return false;}
                mobs[0].setRiding(null);
                mobs[1].setRiding(null);
                R.recoverRoomStats();
                if(isAnyAdjusted(mobs[0])){ mob.tell("Error21-8"); return false;}
                if(isAnyAdjusted(mobs[0])){ mob.tell("Error21-9"); return false;}
            }
            if((what.equalsIgnoreCase("all_properties"))
            ||(what.equalsIgnoreCase("Prop_HereAdjuster")))
            {
                reset(mobs,backups,R,IS,R2);
                Ability HereAdjuster=CMClass.getAbility("Prop_HereAdjuster");
                HereAdjuster.setMiscText("abi+10 gen=F class=Fighter cha+10 man+1000");
                mob.tell("Test#22-1: "+HereAdjuster.accountForYourself());
                A2=((Ability)HereAdjuster.copyOf());
                A2.setMiscText((HereAdjuster).text());
                R2.addNonUninvokableEffect(A2);
                R2.recoverRoomStats();
                if(isAnyAdjusted(mobs[0])){ mob.tell("Error22-0"); return false;}
                MUDTracker.move(mobs[0],Directions.UP,false,false);
                R2.recoverRoomStats();
                if(!isAllAdjusted(mobs[0])){ mob.tell("Error22-1"); return false;}
                MUDTracker.move(mobs[0],Directions.DOWN,false,false);
                R2.recoverRoomStats();
                if(isAnyAdjusted(mobs[0])){ mob.tell("Error22-2"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                HereAdjuster.setMiscText("abi+10 gen=F class=Fighter cha+10 man+1000 MASK=-RACE +Dwarf");
                mob.tell("Test#22-2: "+HereAdjuster.accountForYourself());
                A2=((Ability)HereAdjuster.copyOf());
                A2.setMiscText((HereAdjuster).text());
                R2.addNonUninvokableEffect(A2);
                R2.recoverRoomStats();
                MUDTracker.move(mobs[0],Directions.UP,false,false);
                MUDTracker.move(mobs[1],Directions.UP,false,false);
                R2.recoverRoomStats();
                if(!isAllAdjusted(mobs[0])){ mob.tell("Error22-3"); return false;}
                if(isAnyAdjusted(mobs[1])){ mob.tell("Error22-4"); return false;}
                MUDTracker.move(mobs[0],Directions.DOWN,false,false);
                MUDTracker.move(mobs[1],Directions.DOWN,false,false);
                R2.recoverRoomStats();
                if(isAnyAdjusted(mobs[0])){ mob.tell("Error22-5"); return false;}
                if(isAnyAdjusted(mobs[1])){ mob.tell("Error22-6"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                HereAdjuster.setMiscText("abi+10 gen=F class=Fighter cha+10 man+1000 MASK=-Human");
                mob.tell("Test#22-3: "+HereAdjuster.accountForYourself());
                A2=((Ability)HereAdjuster.copyOf());
                A2.setMiscText((HereAdjuster).text());
                R2.addNonUninvokableEffect(A2);
                R2.recoverRoomStats();
                MUDTracker.move(mobs[0],Directions.UP,false,false);
                MUDTracker.move(mobs[1],Directions.UP,false,false);
                R2.recoverRoomStats();
                if(!isAllAdjusted(mobs[0])){ mob.tell("Error22-7"); return false;}
                if(isAnyAdjusted(mobs[1])){ mob.tell("Error22-8"); return false;}
                MUDTracker.move(mobs[0],Directions.DOWN,false,false);
                MUDTracker.move(mobs[1],Directions.DOWN,false,false);
                R2.recoverRoomStats();
                if(isAnyAdjusted(mobs[0])){ mob.tell("Error22-9"); return false;}
                if(isAnyAdjusted(mobs[1])){ mob.tell("Error22-10"); return false;}
            }
            
            reset(mobs,backups,R,IS,R2);
            CoffeeUtensils.clearTheRoom(R2);
            R2.destroyRoom();
            R.rawDoors()[Directions.UP]=upRoom;
            R.rawExits()[Directions.UP]=upExit;
            mobs[0].destroy();
            mobs[1].destroy();
            R.recoverRoomStats();
            mob.tell("Test(s) passed or completed.");
        }
        else
            mob.tell("Test what?");
        return false;
    }
    public int ticksToExecute(){return 0;}
    public boolean canBeOrdered(){return false;}
    public boolean securityCheck(MOB mob){return CMSecurity.isASysOp(mob);}
    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    
}
