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

    public Item[] giveTo(Item I, Ability A, MOB mob1, MOB mob2, int code)
    {
        Item[] IS=new Item[2];
        Item I1=(Item)I.copyOf();
        I1.addNonUninvokableEffect((Ability)A.copyOf());
        if(code<2)
        {
            mob1.addInventory(I1);
            if(code==1) I1.wearEvenIfImpossible(mob1);
        }
        else
        {
            mob1.location().addItem(I1);
            mob1.setRiding((Rideable)I1);
        }
            
        IS[0]=I1;
        
        Item I2=(Item)I.copyOf();
        I2.addNonUninvokableEffect((Ability)A.copyOf());
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
                mob2.setRiding((Rideable)I2);
            }
        }
        IS[1]=I2;
        mob1.location().recoverRoomStats();
        return IS;
    }
    
    public boolean spellCheck(MOB mob)
    {
        if((mob.fetchAbility("Spell_Sleep")!=null)
        &&(mob.fetchAbility("Spell_Blur")!=null))
            return true;
        return false;
    }

    public boolean effectCheck(MOB mob)
    {
        if((mob.fetchEffect("Spell_Sleep")!=null)
        &&(mob.fetchEffect("Spell_Blur")!=null))
            return true;
        return false;
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
            if(what.equalsIgnoreCase("props"))
            {
                Item IS[]=new Item[2];
                Room R=mob.location();
                Room R2=CMClass.getLocale("StoneRoom");
                R2.setArea(R.getArea());
                R.rawExits()[Directions.UP]=CMClass.getExit("Open");
                R2.rawExits()[Directions.DOWN]=CMClass.getExit("Open");
                R.rawDoors()[Directions.UP]=R2;
                R2.rawDoors()[Directions.DOWN]=R2;
                
                MOB[] mobs=new MOB[2];
                MOB[] backups=new MOB[2];
                reset(mobs,backups,R,IS,R2);
                
                Ability HaveEnabler=CMClass.getAbility("Prop_HaveEnabler");
                HaveEnabler.setMiscText("Spell_Sleep;Spell_Blur");
                Log.sysOut("Test","Test 1-1: "+HaveEnabler.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),HaveEnabler,mobs[0],null,0);
                if(!spellCheck(mobs[0])){ mob.tell("Error1-1"); return false;}
                IS[0].unWear();
                R.bringItemHere(IS[0],0);
                R.recoverRoomStats();
                if(spellCheck(mobs[0])){ mob.tell("Error1-2"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                HaveEnabler.setMiscText("Spell_Sleep;Spell_Blur;MASK=-RACE +Dwarf");
                Log.sysOut("Test","Test 1-2: "+HaveEnabler.accountForYourself());
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
                
                reset(mobs,backups,R,IS,R2);
                Ability HaveSpellCast=CMClass.getAbility("Prop_HaveSpellCast");
                HaveSpellCast.setMiscText("Spell_Sleep;Spell_Blur");
                Log.sysOut("Test","Test 2-1: "+HaveSpellCast.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),HaveSpellCast,mobs[0],null,0);
                if(!effectCheck(mobs[0])){ mob.tell("Error2-1"); return false;}
                IS[0].unWear();
                R.bringItemHere(IS[0],0);
                R.recoverRoomStats();
                if(effectCheck(mobs[0])){ mob.tell("Error2-2"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                HaveSpellCast.setMiscText("Spell_Sleep;Spell_Blur;MASK=-RACE +Dwarf");
                Log.sysOut("Test","Test 2-2: "+HaveSpellCast.accountForYourself());
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
                HaveSpellCast.setMiscText("Spell_Sleep;Spell_Blur;MASK=-Human");
                Log.sysOut("Test","Test 2-3: "+HaveSpellCast.accountForYourself());
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
                
                reset(mobs,backups,R,IS,R2);
                Ability WearEnabler=CMClass.getAbility("Prop_WearEnabler");
                WearEnabler.setMiscText("Spell_Sleep;Spell_Blur");
                Log.sysOut("Test","Test 3-1: "+WearEnabler.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),WearEnabler,mobs[0],null,1);
                if(!spellCheck(mobs[0])){ mob.tell("Error3-1"); return false;}
                IS[0].unWear();
                R.recoverRoomStats();
                if(spellCheck(mobs[0])){ mob.tell("Error3-2"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                WearEnabler.setMiscText("Spell_Sleep;Spell_Blur;MASK=-RACE +Dwarf");
                Log.sysOut("Test","Test 3-2: "+WearEnabler.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),WearEnabler,mobs[0],mobs[1],1);
                if(!spellCheck(mobs[0])){ mob.tell("Error3-3"); return false;}
                if(spellCheck(mobs[1])){ mob.tell("Error3-4"); return false;}
                IS[0].unWear();
                IS[1].unWear();
                R.recoverRoomStats();
                if(spellCheck(mobs[0])){ mob.tell("Error3-5"); return false;}
                if(spellCheck(mobs[1])){ mob.tell("Error3-6"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                Ability WearSpellCast=CMClass.getAbility("Prop_WearSpellCast");
                WearSpellCast.setMiscText("Spell_Sleep;Spell_Blur");
                Log.sysOut("Test","Test 4-1: "+WearSpellCast.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),WearSpellCast,mobs[0],null,1);
                if(!effectCheck(mobs[0])){ mob.tell("Error4-1"); return false;}
                IS[0].unWear();
                R.recoverRoomStats();
                if(effectCheck(mobs[0])){ mob.tell("Error4-2"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                WearSpellCast.setMiscText("Spell_Sleep;Spell_Blur;MASK=-RACE +Dwarf");
                Log.sysOut("Test","Test 4-2: "+WearSpellCast.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),WearSpellCast,mobs[0],mobs[1],1);
                if(!effectCheck(mobs[0])){ mob.tell("Error4-3"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error4-4"); return false;}
                IS[0].unWear();
                IS[1].unWear();
                R.recoverRoomStats();
                if(effectCheck(mobs[0])){ mob.tell("Error4-5"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error4-6"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                WearSpellCast.setMiscText("Spell_Sleep;Spell_Blur;MASK=-Human");
                Log.sysOut("Test","Test 4-3: "+WearSpellCast.accountForYourself());
                IS=giveTo(CMClass.getWeapon("Sword"),WearSpellCast,mobs[0],mobs[1],1);
                if(!effectCheck(mobs[0])){ mob.tell("Error4-7"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error4-8"); return false;}
                IS[0].unWear();
                IS[1].unWear();
                R.recoverRoomStats();
                if(effectCheck(mobs[0])){ mob.tell("Error4-9"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error4-10"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                Ability RideEnabler=CMClass.getAbility("Prop_RideEnabler");
                Log.sysOut("Test","Test 5-1: "+RideEnabler.accountForYourself());
                RideEnabler.setMiscText("Spell_Sleep;Spell_Blur");
                IS=giveTo(CMClass.getItem("Boat"),RideEnabler,mobs[0],null,2);
                if(!spellCheck(mobs[0])){ mob.tell("Error5-1"); return false;}
                mobs[0].setRiding(null);
                R.recoverRoomStats();
                if(spellCheck(mobs[0])){ mob.tell("Error5-2"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                RideEnabler.setMiscText("Spell_Sleep;Spell_Blur;MASK=-RACE +Dwarf");
                Log.sysOut("Test","Test 5-2: "+RideEnabler.accountForYourself());
                IS=giveTo(CMClass.getItem("Boat"),RideEnabler,mobs[0],mobs[1],2);
                if(!spellCheck(mobs[0])){ mob.tell("Error5-3"); return false;}
                if(spellCheck(mobs[1])){ mob.tell("Error5-4"); return false;}
                mobs[0].setRiding(null);
                mobs[1].setRiding(null);
                R.recoverRoomStats();
                if(spellCheck(mobs[0])){ mob.tell("Error5-5"); return false;}
                if(spellCheck(mobs[1])){ mob.tell("Error5-6"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                Ability RideSpellCast=CMClass.getAbility("Prop_RideSpellCast");
                RideSpellCast.setMiscText("Spell_Sleep;Spell_Blur");
                Log.sysOut("Test","Test 6-1: "+RideSpellCast.accountForYourself());
                IS=giveTo(CMClass.getItem("Boat"),RideSpellCast,mobs[0],null,2);
                if(!effectCheck(mobs[0])){ mob.tell("Error6-1"); return false;}
                mobs[0].setRiding(null);
                R.recoverRoomStats();
                if(effectCheck(mobs[0])){ mob.tell("Error6-2"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                RideSpellCast.setMiscText("Spell_Sleep;Spell_Blur;MASK=-RACE +Dwarf");
                Log.sysOut("Test","Test 6-2: "+RideSpellCast.accountForYourself());
                IS=giveTo(CMClass.getItem("Boat"),RideSpellCast,mobs[0],mobs[1],2);
                if(!effectCheck(mobs[0])){ mob.tell("Error6-3"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error6-4"); return false;}
                mobs[0].setRiding(null);
                mobs[1].setRiding(null);
                R.recoverRoomStats();
                if(effectCheck(mobs[0])){ mob.tell("Error6-5"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error6-6"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                RideSpellCast.setMiscText("Spell_Sleep;Spell_Blur;MASK=-Human");
                Log.sysOut("Test","Test 6-3: "+RideSpellCast.accountForYourself());
                IS=giveTo(CMClass.getItem("Boat"),RideSpellCast,mobs[0],mobs[1],2);
                if(!effectCheck(mobs[0])){ mob.tell("Error6-7"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error6-8"); return false;}
                mobs[0].setRiding(null);
                mobs[1].setRiding(null);
                R.recoverRoomStats();
                if(effectCheck(mobs[0])){ mob.tell("Error6-9"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error6-10"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                Ability HereSpellCast=CMClass.getAbility("Prop_HereSpellCast");
                HereSpellCast.setMiscText("Spell_Sleep;Spell_Blur;MASK=-RACE +Dwarf");
                Log.sysOut("Test","Test 7-1: "+HereSpellCast.accountForYourself());
                R2.addNonUninvokableEffect(HereSpellCast);
                R2.recoverRoomStats();
                MUDTracker.move(mobs[0],Directions.UP,false,false);
                if(!effectCheck(mobs[0])){ mob.tell("Error7-1"); return false;}
                MUDTracker.move(mobs[0],Directions.DOWN,false,false);
                if(effectCheck(mobs[0])){ mob.tell("Error7-2"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                HereSpellCast.setMiscText("Spell_Sleep;Spell_Blur;MASK=-RACE +Dwarf");
                Log.sysOut("Test","Test 7-2: "+HereSpellCast.accountForYourself());
                R2.addNonUninvokableEffect(HereSpellCast);
                R2.recoverRoomStats();
                MUDTracker.move(mobs[0],Directions.UP,false,false);
                MUDTracker.move(mobs[1],Directions.UP,false,false);
                if(!effectCheck(mobs[0])){ mob.tell("Error7-1"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error7-2"); return false;}
                MUDTracker.move(mobs[0],Directions.DOWN,false,false);
                MUDTracker.move(mobs[1],Directions.DOWN,false,false);
                if(effectCheck(mobs[0])){ mob.tell("Error7-3"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error7-4"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                HereSpellCast.setMiscText("Spell_Sleep;Spell_Blur;MASK=-Human");
                Log.sysOut("Test","Test 7-3: "+HereSpellCast.accountForYourself());
                R2.addNonUninvokableEffect(HereSpellCast);
                R2.recoverRoomStats();
                MUDTracker.move(mobs[0],Directions.UP,false,false);
                MUDTracker.move(mobs[1],Directions.UP,false,false);
                if(!effectCheck(mobs[0])){ mob.tell("Error7-1"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error7-2"); return false;}
                MUDTracker.move(mobs[0],Directions.DOWN,false,false);
                MUDTracker.move(mobs[1],Directions.DOWN,false,false);
                if(effectCheck(mobs[0])){ mob.tell("Error7-3"); return false;}
                if(effectCheck(mobs[1])){ mob.tell("Error7-4"); return false;}
                
                reset(mobs,backups,R,IS,R2);
                Ability SpellAdder=CMClass.getAbility("Prop_SpellAdder");
                SpellAdder.setMiscText("Spell_Sleep;Spell_Blur;MASK=-RACE +Dwarf");
                Log.sysOut("Test","Test 8-1: "+SpellAdder.accountForYourself());
                R2.addNonUninvokableEffect(SpellAdder);
                R2.recoverRoomStats();
                MUDTracker.move(mobs[0],Directions.UP,false,false);
                if(!effectCheck(mobs[0])){ mob.tell("Error8-1"); return false;}
                MUDTracker.move(mobs[0],Directions.DOWN,false,false);
                if(effectCheck(mobs[0])){ mob.tell("Error8-2"); return false;}
                
                Ability UseSpellCast=CMClass.getAbility("Prop_UseSpellCast");
                Ability UseSpellCast2=CMClass.getAbility("Prop_UseSpellCast2");
                Ability FightSpellCast=CMClass.getAbility("Prop_FightSpellCast");
                
                Ability HaveZapper=CMClass.getAbility("Prop_HaveZapper");
                Ability RideZapper=CMClass.getAbility("Prop_RideZapper");
                Ability WearZapper=CMClass.getAbility("Prop_WearZapper");
                Ability Resistance=CMClass.getAbility("Prop_Resistance");
                Ability HaveResister=CMClass.getAbility("Prop_HaveResister");
                Ability RideResister=CMClass.getAbility("Prop_RideResister");
                Ability WearResister=CMClass.getAbility("Prop_WearResister");
                Ability WearAdjuster=CMClass.getAbility("Prop_WearAdjuster");
                Ability RideAdjuster=CMClass.getAbility("Prop_RideAdjuster");
                Ability HaveAdjuster=CMClass.getAbility("Prop_HaveAdjuster");
                Ability HereAdjuster=CMClass.getAbility("Prop_HereAdjuster");
                Ability EnterAdjuster=CMClass.getAbility("Prop_EnterAdjuster");
                Ability ClanEquipment=CMClass.getAbility("Prop_ClanEquipment");
                mob.tell("Properties test passed!");
            }
            else
                mob.tell("Test command passed its internal check!");
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
