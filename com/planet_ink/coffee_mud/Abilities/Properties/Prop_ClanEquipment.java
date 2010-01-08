package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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

/**
 * <p>
 * Title: False Realities Flavored CoffeeMUD
 * </p>
 * <p>
 * Description: The False Realities Version of CoffeeMUD
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003 Jeremy Vyska
 * </p>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * <p>
 * you may not use this file except in compliance with the License.
 * <p>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * <p>
 * distributed under the License is distributed on an "AS IS" BASIS,
 * <p>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>
 * See the License for the specific language governing permissions and
 * <p>
 * limitations under the License.
 * <p>
 * Company: http://www.falserealities.com
 * </p>
 *
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */
@SuppressWarnings("unchecked")
public class Prop_ClanEquipment extends Property
{
    public String ID()
    {
        return "Prop_ClanEquipment";
    }

    public String name()
    {
        return "Clan Equipment";
    }

    public boolean bubbleAffect()
    {
        return true;
    }

    protected int canAffectCode()
    {
        return Ability.CAN_ITEMS;
    }
    protected boolean activated=false;
    protected String type="";
    protected int TypeOfEffect=0;
    protected int WeaponType=0;
    protected int PowerLevel=0;
    protected long LastChecked=0;
    protected boolean notAgain=false;
    protected String clanName="";
    protected String clanType="";
    String lastMessage=null;
    protected CharStats EQadjCharStats=null;
    protected String secretWord=CMProps.getSListVar(CMProps.SYSTEML_MAGIC_WORDS)[CMLib.dice().roll(1,CMProps.getSListVar(CMProps.SYSTEML_MAGIC_WORDS).length,0)-1];

    public String accountForYourself()
    {
        // My slightly complicated way of showing the clan effect when ID'd
        StringBuffer id=new StringBuffer(clanType+" "+clanName+" Bonus: ");
        if((affected instanceof Weapon)&&(!(affected instanceof Wand))&&(TypeOfEffect<1000))
        {
            id.append("Does "+(1*PowerLevel)+"-"+(6*PowerLevel)+" additional "+type.toLowerCase()+" damage.");
        }else
            if((affected instanceof Armor)&&(TypeOfEffect<1000)&&(!(affected instanceof Shield)))
            {
                id.append("Raises "+type.toLowerCase()+" resistance by "+(PowerLevel*5)+".");
            }
        if((affected instanceof Armor)&&(TypeOfEffect<1000)&&(affected instanceof Shield))
        {
            id.append("Causes "+(1*PowerLevel)+"-"+(3*PowerLevel)+" "+type.toLowerCase()+" damage to melee attackers.");
        }
        if((affected instanceof Wand)&&(TypeOfEffect<1000))
        {
            id.append("Does "+(1*PowerLevel)+"-"+(6*PowerLevel)+" "+type.toLowerCase()+" damage when the user says `"+secretWord+"` to the target.");
        }
        return id.toString();
    }

    public void setMiscText(String text)
    {
        super.setMiscText(text);
        Vector V=CMParms.parse(text);
        if(V.size()<4)
        {
            return;
        }
        type=(String)V.elementAt(0);
        PowerLevel=Integer.valueOf((String)V.elementAt(1)).intValue();
        clanName=(String)V.elementAt(2);
        clanType=(String)V.elementAt(3);
        secretWord=getWandWord(text); // try to randomize the spell word a
                                        // little
        // Armor
        this.EQadjCharStats=(CharStats)CMClass.getCommon("DefaultCharStats");
        initAdjustments(EQadjCharStats);
        if(type.equalsIgnoreCase("PARALYSIS"))
        {
            TypeOfEffect=CMMsg.TYP_PARALYZE;
            WeaponType=Weapon.TYPE_STRIKING;
            this.EQadjCharStats.setStat(CharStats.STAT_SAVE_PARALYSIS,PowerLevel*5);
        }else
            if(type.equalsIgnoreCase("FIRE"))
            {
                TypeOfEffect=CMMsg.TYP_FIRE;
                WeaponType=Weapon.TYPE_BURNING;
                this.EQadjCharStats.setStat(CharStats.STAT_SAVE_FIRE,PowerLevel*5);
            }else
                if(type.equalsIgnoreCase("COLD"))
                {
                    TypeOfEffect=CMMsg.TYP_COLD;
                    WeaponType=Weapon.TYPE_FROSTING;
                    this.EQadjCharStats.setStat(CharStats.STAT_SAVE_COLD,PowerLevel*5);
                }else
                    if(type.equalsIgnoreCase("WATER"))
                    {
                        TypeOfEffect=CMMsg.TYP_WATER;
                        WeaponType=Weapon.TYPE_SLASHING;
                        this.EQadjCharStats.setStat(CharStats.STAT_SAVE_WATER,PowerLevel*5);
                    }else
                        if(type.equalsIgnoreCase("GAS"))
                        {
                            TypeOfEffect=CMMsg.TYP_GAS;
                            WeaponType=Weapon.TYPE_GASSING;
                            this.EQadjCharStats.setStat(CharStats.STAT_SAVE_GAS,PowerLevel*5);
                        }else
                            if(type.equalsIgnoreCase("MIND"))
                            {
                                TypeOfEffect=CMMsg.TYP_MIND;
                                WeaponType=Weapon.TYPE_STRIKING;
                                this.EQadjCharStats.setStat(CharStats.STAT_SAVE_MIND,PowerLevel*5);
                            }else
                                if(type.equalsIgnoreCase("ACID"))
                                {
                                    TypeOfEffect=CMMsg.TYP_ACID;
                                    WeaponType=Weapon.TYPE_MELTING;
                                    this.EQadjCharStats.setStat(CharStats.STAT_SAVE_ACID,PowerLevel*5);
                                }else
                                    if(type.equalsIgnoreCase("ELECTRIC"))
                                    {
                                        TypeOfEffect=CMMsg.TYP_ELECTRIC;
                                        WeaponType=Weapon.TYPE_BURNING;
                                        this.EQadjCharStats.setStat(CharStats.STAT_SAVE_ELECTRIC,PowerLevel*5);
                                    }else
                                        if(type.equalsIgnoreCase("POISON"))
                                        {
                                            TypeOfEffect=CMMsg.TYP_POISON;
                                            WeaponType=Weapon.TYPE_STRIKING;
                                            this.EQadjCharStats.setStat(CharStats.STAT_SAVE_POISON,PowerLevel*5);
                                        }else
                                            if(type.equalsIgnoreCase("DISEASE"))
                                            {
                                                TypeOfEffect=CMMsg.TYP_DISEASE;
                                                WeaponType=Weapon.TYPE_STRIKING;
                                                this.EQadjCharStats.setStat(CharStats.STAT_SAVE_DISEASE,PowerLevel*5);
                                            }else
                                                if(type.equalsIgnoreCase("HEALTH"))
                                                {
                                                    TypeOfEffect=1001;
                                                    WeaponType=Weapon.TYPE_BURSTING;
                                                }else
                                                    if(type.equalsIgnoreCase("MAGIC"))
                                                    {
                                                        TypeOfEffect=1002;
                                                        WeaponType=Weapon.TYPE_BURSTING;
                                                    }else
                                                        if(type.equalsIgnoreCase("SPEED"))
                                                        {
                                                            TypeOfEffect=1003;
                                                        }
    }

    public boolean useAsWand(MOB mob, int level)
    {
        int manaRequired=50;
        // For simplicity, there's no charges BUT use costs a flat 10% mana
        manaRequired=(int)CMath.div(mob.maxState().getMana(),10);
        manaRequired-=(5*level);
        if(manaRequired<5) manaRequired=5;

        if(manaRequired>mob.curState().getMana())
        {
            mob.tell("You don't have enough mana.");
            return false;
        }
        mob.curState().adjMana(-manaRequired,mob.maxState());
        return true;
    }

    public static String getWandWord(String from)
    {
        int hash=from.hashCode();
        if(hash<0)
        {
            hash=hash*-1;
        }
        return CMProps.getSListVar(CMProps.SYSTEML_MAGIC_WORDS)[hash%CMProps.getSListVar(CMProps.SYSTEML_MAGIC_WORDS).length];
    }

    /*
     * ********************** Staff/Wand Clan Eq **********************
     */
    public void waveIfAble(MOB mob, Environmental afftarget, String message, Wand me)
    {
        if((mob.isMine(me))&&(afftarget!=null)&&(afftarget instanceof MOB)&&(!me.amWearingAt(Wearable.IN_INVENTORY)))
        {
            MOB target=null;
            if((mob.location()!=null))
            {
                target=(MOB)afftarget;
            }
            int x=message.toUpperCase().indexOf(secretWord.toUpperCase());
            if(x>=0)
            {
                message=message.substring(x+secretWord.length());
                int y=message.indexOf("'");
                if(y>=0)
                {
                    message=message.substring(0,y);
                }
                message=message.trim();
                Ability wandUse=mob.fetchAbility("Skill_WandUse");
                if((wandUse==null)||(!wandUse.proficiencyCheck(mob,0,false)))
                {
                    mob.tell(me.name()+" glows faintly for a moment, then fades.");
                }else
                {
					wandUse.setInvoker(mob);
                    if(useAsWand(mob,wandUse.abilityCode()))
                    {
                        mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,me.name()+" glows brightly.");
                        int flameDamage=CMLib.dice().roll(1,6*PowerLevel,1*PowerLevel);
                        CMLib.combat().postDamage(mob,target,null,flameDamage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|TypeOfEffect,WeaponType,
                                "^F^<FIGHT^>The magic of "+clanName+" <DAMAGE> <T-NAME>!^</FIGHT^>^?");
                        wandUse.helpProficiency(mob);
                        return;
                    }
                }
            }
        }
    }

    public static void initAdjustments(CharStats adjCharStats)
    {
        // ensure we get no NULL errors
    	for(int i : CharStats.CODES.SAVING_THROWS())
	        adjCharStats.setStat(i,0);
    }

    public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
    {
        if(EQadjCharStats==null)
        {
            setMiscText(text());
            /*
             * ************************* Armor-based Resistances
             * *************************
             */
        }
        if((affected!=null)&&(affected instanceof Armor)&&(!(affected instanceof Shield))&&(activated)
                &&(!((Armor)affected).amWearingAt(Wearable.IN_INVENTORY)))
        {
            for(int i : CharStats.CODES.SAVING_THROWS())
                affectedStats.setStat(i,affectedStats.getStat(i)+EQadjCharStats.getStat(i));
        }
        super.affectCharStats(affectedMOB,affectedStats);
    }

    public void executeMsg(Environmental myHost, CMMsg msg)
    {
        super.executeMsg(myHost,msg);
        if(((System.currentTimeMillis()-LastChecked)>TimeManager.MILI_HOUR)&&(affected!=null))
        {
            if((clanName!=null)&&(clanName.length()>0)&&(CMLib.clans().getClan(clanName)==null)) affected.delEffect(this);
            LastChecked=System.currentTimeMillis();
        }
        MOB mob=null;
        MOB source=null;
        if((affected!=null)&&(affected instanceof Item))
        {
            if((((Item)affected).owner()!=null)&&((Item)affected).owner() instanceof MOB)
            {
                mob=(MOB)((Item)affected).owner();
            }
        }
        // if held by the wrong clan, it is inactive.
        if((mob!=null)&&(mob.getClanID().equalsIgnoreCase(clanName)))
            activated=true;
        else
            activated=false;
        if(!activated) return;
        if(msg.source()!=null)
        {
            source=msg.source();
            /*
             * ********************** Weapon addtl. Damage
             * **********************
             */
        }
        if((msg.source().location()!=null)&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)&&(msg.value()>0)&&(msg.tool()==affected)&&(!notAgain)
                &&(msg.target() instanceof MOB)&&(msg.tool() instanceof Weapon)&&(!(msg.tool() instanceof Wand))&&(TypeOfEffect<1000)
                &&(!((MOB)msg.target()).amDead()))
        {
            double flameDamage=(double)CMLib.dice().roll(1,6*PowerLevel,1*PowerLevel);
            String str="^F^<FIGHT^>The magic of "+clanName+" <DAMAGE> <T-NAME>!^</FIGHT^>^?";
            CMLib.combat().postDamage(msg.source(),(MOB)msg.target(),null,(int)Math.round(flameDamage),
                    CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|TypeOfEffect,WeaponType,str);
        }
        /*
         * ********************** Shield Thorns Damage **********************
         */
        if((mob!=null)&&(msg.amITarget(mob))&&(affected instanceof Shield)&&(TypeOfEffect<1000))
        {
            if((CMLib.dice().rollPercentage()>32+msg.source().charStats().getStat(CharStats.STAT_DEXTERITY))&&(msg.source().rangeToTarget()==0)
                    &&((lastMessage==null)||(lastMessage.indexOf("The magic around")<0))
                    &&((CMath.bset(msg.targetMajor(),CMMsg.MASK_HANDS))||(CMath.bset(msg.targetMajor(),CMMsg.MASK_MOVE))))
            {
                CMMsg msg2=CMClass.getMsg(mob,source,this,CMMsg.MSG_CAST_ATTACK_VERBAL_SPELL,null);
                if((source!=null)&&(source.location().okMessage(source,msg2)))
                {
                    source.location().send(source,msg2);
                    if(msg2.value()<=0)
                    {
                        int damage=CMLib.dice().roll(1,3*PowerLevel,1*PowerLevel);
                        CMLib.combat().postDamage(mob,source,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|TypeOfEffect,WeaponType,
                                "^F^<FIGHT^>The magic of "+clanName+" around <S-NAME> <DAMAGE> <T-NAME>!^</FIGHT^>^?");
                    }
                }
            }
        }
        /*
         * ************************* Staff/Wand Message Watch
         * *************************
         */
        if(affected instanceof Wand)
            switch(msg.targetMinor())
            {
            case CMMsg.TYP_WAND_USE:
                if(msg.amITarget(this)) waveIfAble(mob,msg.tool(),msg.targetMessage(),(Wand)affected);
                break;
            case CMMsg.TYP_SPEAK:
                if(msg.sourceMinor()==CMMsg.TYP_SPEAK)
                    msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,msg.target(),CMMsg.NO_EFFECT,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_WAND_USE,msg
                            .targetMessage(),CMMsg.NO_EFFECT,null));
                break;
            default:
                break;
            }
    }
}
