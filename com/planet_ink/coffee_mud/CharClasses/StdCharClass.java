package com.planet_ink.coffee_mud.CharClasses;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;



/*
   Copyright 2000-2006 Bo Zimmerman

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
public class StdCharClass implements CharClass
{
	public String ID(){return "StdCharClass";}
	public String name(){return "mob";}
    
    public String name(int classLevel){return name();}
	public String baseClass(){return ID();}
	public int getBonusPracLevel(){return 0;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.STAT_STRENGTH;}
	public int getPracsFirstLevel(){return 5;}
	public int getTrainsFirstLevel(){return 3;}
	public int getLevelsPerBonusDamage(){ return 1;}
	public int getMovementMultiplier(){return 10;}
	public int getHPDivisor(){return 3;}
	public int getHPDice(){return 1;}
	public int getHPDie(){return 6;}
	public int getManaDivisor(){return 3;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 6;}
	protected int maxStatAdj[]={0,0,0,0,0,0};
	protected Vector outfitChoices=null;
	public int allowedArmorLevel(){return CharClass.ARMOR_ANY;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_ANY;}
	protected HashSet disallowedWeaponClasses(MOB mob){return null;}
	protected HashSet requiredWeaponMaterials(){return null;}
	protected int requiredArmorSourceMinor(){return -1;}
	protected String armorFailMessage(){return "<S-NAME> fumble(s) <S-HIS-HER> <SKILL> due to <S-HIS-HER> armor!";}
	public boolean raceless(){return false;}
	public boolean leveless(){return false;}
	public boolean expless(){return false;}
    private static final Vector empty=new Vector();
    public Vector getSecurityGroups(int classLevel){return empty;}
    public CMObject newInstance(){return this;}
    protected String[] names=null;
    public String[] nameSet()
    {
        if(names!=null) return names;
        names=new String[1];
        names[0]=name();
        return names;
    }
    public void initializeClass()
    {
        if(!ID().equals("StdCharClass")) return;
        CMLib.ableMapper().addCharAbilityMapping("All",5,"Armorsmithing",false,CMParms.parseSemicolons("Blacksmithing(75)",true),"+STR 12");
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Blacksmithing",false,"+STR 10");
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Butchering",false);
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Carpentry",false,"+CON 10");
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Chopping",false);
        CMLib.ableMapper().addCharAbilityMapping("All",1,"ClanCrafting",false);
        CMLib.ableMapper().addCharAbilityMapping("All",5,"Cobbling",false,CMParms.parseSemicolons("LeatherWorking",true),"+CON 12");
        CMLib.ableMapper().addCharAbilityMapping("All",10,"Construction",false,CMParms.parseSemicolons("Carpentry",true),"+CON 12");
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Cooking",false);
        CMLib.ableMapper().addCharAbilityMapping("All",5,"Baking",false,CMParms.parseSemicolons("Cooking",true));
        CMLib.ableMapper().addCharAbilityMapping("All",5,"FoodPrep",false,CMParms.parseSemicolons("Cooking",true));
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Digging",false);
        CMLib.ableMapper().addCharAbilityMapping("All",10,"Distilling",false,CMParms.parseSemicolons("Cooking",true));
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Drilling",false);
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Shearing",false);
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Dyeing",false,"+CHA 8");
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Embroidering",false,CMParms.parseSemicolons("Skill_Write",true),"+CHA 10");
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Engraving",false,CMParms.parseSemicolons("Skill_Write",true),"+CHA 10");
        CMLib.ableMapper().addCharAbilityMapping("All",10,"Farming",false);
        CMLib.ableMapper().addCharAbilityMapping("All",10,"Costuming",false,CMParms.parseSemicolons("Tailoring",true),"+INT 12");
        CMLib.ableMapper().addCharAbilityMapping("All",1,"FireBuilding",false);
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Fishing",false,"+WIS 8");
        CMLib.ableMapper().addCharAbilityMapping("All",10,"Fletching",false,CMParms.parseSemicolons("Specialization_Ranged;Carpentry",true),"+DEX 12");
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Foraging",false);
        CMLib.ableMapper().addCharAbilityMapping("All",5,"GlassBlowing",false,"+CON 12");
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Herbology",false);
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Hunting",false,"+WIS 8");
        CMLib.ableMapper().addCharAbilityMapping("All",15,"JewelMaking",false,CMParms.parseSemicolons("Blacksmithing,Pottery",true),"+WIS 16");
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Lacquerring",false,"+CHA 8");
        CMLib.ableMapper().addCharAbilityMapping("All",1,"LeatherWorking",false,"+CON 10");
        CMLib.ableMapper().addCharAbilityMapping("All",15,"LockSmith",false,CMParms.parseSemicolons("Blacksmithing",true),"+DEX 14");
        CMLib.ableMapper().addCharAbilityMapping("All",10,"Masonry",false,CMParms.parseSemicolons("Sculpting",true),"+CON 12");
        CMLib.ableMapper().addCharAbilityMapping("All",30,"MasterTailoring",false,CMParms.parseSemicolons("Tailoring(100)",true),"+DEX 16");
        CMLib.ableMapper().addCharAbilityMapping("All",30,"MasterCostuming",false,CMParms.parseSemicolons("Costuming(100)",true),"+INT 16");
        CMLib.ableMapper().addCharAbilityMapping("All",30,"MasterLeatherWorking",false,CMParms.parseSemicolons("LeatherWorking(100)",true),"+CON 16");
        CMLib.ableMapper().addCharAbilityMapping("All",30,"MasterWeaponsmithing",false,CMParms.parseSemicolons("Weaponsmithing(100);Specialization_*",true),"+STR 16");
        CMLib.ableMapper().addCharAbilityMapping("All",30,"MasterArmorsmithing",false,CMParms.parseSemicolons("Armorsmithing(100)",true),"+STR 16");
        CMLib.ableMapper().addCharAbilityMapping("All",20,"Merchant",false,"+CHA 10");
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Mining",false);
        CMLib.ableMapper().addCharAbilityMapping("All",5,"Painting",false,"+CHA 12");
        CMLib.ableMapper().addCharAbilityMapping("All",5,"PaperMaking",false);
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Pottery",false);
        CMLib.ableMapper().addCharAbilityMapping("All",5,"ScrimShaw",false,"Sculpting");
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Sculpting",false,"+CON 10");
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Searching",false);
        CMLib.ableMapper().addCharAbilityMapping("All",10,"Shipwright",false,CMParms.parseSemicolons("Carpentry",true),"+WIS 12");
        CMLib.ableMapper().addCharAbilityMapping("All",10,"Smelting",false,CMParms.parseSemicolons("Blacksmithing",true),"+CON 12");
        CMLib.ableMapper().addCharAbilityMapping("All",1,"SmokeRings",false,"+CHA 5");
        CMLib.ableMapper().addCharAbilityMapping("All",10,"Speculate",false,"+WIS 10");
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Tailoring",false,"+DEX 10");
        CMLib.ableMapper().addCharAbilityMapping("All",20,"Taxidermy",false,"+INT 12");
        CMLib.ableMapper().addCharAbilityMapping("All",10,"Wainwrighting",false,CMParms.parseSemicolons("Carpentry",true),"+INT 12");
        CMLib.ableMapper().addCharAbilityMapping("All",5,"Weaponsmithing",false,CMParms.parseSemicolons("Blacksmithing(75);Specialization_*",true),"+STR 12");
        CMLib.ableMapper().addCharAbilityMapping("All",1,"Weaving",false,"+WIS 10");
        CMLib.ableMapper().addCharAbilityMapping("Mage",1,"Alchemy",false,"+INT 12 +WIS 12");
        CMLib.ableMapper().addCharAbilityMapping("Bard",10,"Alchemy",false,"+INT 12 +WIS 12");
        CMLib.ableMapper().addCharAbilityMapping("Cleric",1,"Alchemy",false,"+INT 12 +WIS 12");
    }

	public boolean isGeneric(){return false;}
	public int availabilityCode(){return 0;}

	public void cloneFix(CharClass C)
	{
	}

	public CMObject copyOf()
	{
		try
		{
			StdCharClass E=(StdCharClass)this.clone();
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this;
		}
	}

	public int classDurationModifier(MOB myChar, Ability skill, int duration)
	{ return duration;}

	public long getTickStatus(){return Tickable.STATUS_NOT;}
	public boolean tick(Tickable myChar, int tickID){
		return true;
	}

	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if((!mob.isMonster())&&(mob.baseEnvStats().level()>0))
		{
			if(mob.charStats().getCurrentClass().ID().equals(ID()))
			{
				if(!quiet)
					mob.tell("But you are already a "+name()+"!");
				return false;
			}
			if((CMProps.getVar(CMProps.SYSTEM_MULTICLASS).startsWith("NO"))
			&&(!mob.charStats().getCurrentClass().baseClass().equals("StdCharClass")))
			{
				if(!quiet)
					mob.tell("You should be happy to be a "+name()+"!");
				return false;
			}
			else
			if((!CMProps.getVar(CMProps.SYSTEM_MULTICLASS).startsWith("MULTI"))
			&&(!mob.charStats().getCurrentClass().baseClass().equals(baseClass()))
			&&(!mob.charStats().getCurrentClass().baseClass().equals("StdCharClass"))
			&&(!mob.charStats().getCurrentClass().baseClass().equals("Commoner")))
			{
				if(!quiet)
					mob.tell("You must be a "+baseClass()+" type to become a "+name()+".");
				return false;
			}
		}
		return true;
	}
	public String weaponLimitations()
	{ return WEAPONS_LONGDESC[allowedWeaponLevel()];}
	public String armorLimitations()
	{ return ARMOR_LONGDESC[allowedArmorLevel()];}
	public String otherLimitations(){return "";}
	public String otherBonuses(){return "";}
	public String statQualifications(){return "";}
	
	protected HashSet buildDisallowedWeaponClasses(){return buildDisallowedWeaponClasses(allowedWeaponLevel());}
	protected HashSet buildDisallowedWeaponClasses(int lvl)
	{
		if(lvl==CharClass.WEAPONS_ANY)
			return null;
		int[] set=CharClass.WEAPONS_SETS[lvl];
		HashSet H=new HashSet();
		if(set[0]>Weapon.classifictionDescription.length)
			return null;
		for(int i=0;i<Weapon.classifictionDescription.length;i++)
		{
			boolean found=false;
			for(int s=0;s<set.length;s++)
				if(set[s]==i) found=true;
			if(!found) H.add(new Integer(i));
		}
		return H;
	}
	protected HashSet buildRequiredWeaponMaterials()
	{
		if(allowedWeaponLevel()==CharClass.WEAPONS_ANY)
			return null;
		int[] set=CharClass.WEAPONS_SETS[allowedWeaponLevel()];
		if(set[0]>Weapon.classifictionDescription.length)
		{
			HashSet H=new HashSet();
			for(int s=0;s<set.length;s++)
				H.add(new Integer(set[s]));
			return H;
		}
		return null;
	}


	protected boolean isQualifyingAuthority(MOB mob, Ability A)
	{
		CharClass C=null;
		int ql=0;
		for(int i=(mob.charStats().numClasses()-1);i>=0;i--) // last one is current
		{
			C=mob.charStats().getMyClass(i);
			ql=CMLib.ableMapper().getQualifyingLevel(C.ID(),true,A.ID());
			if((C!=null)
			&&(ql>0)
			&&(ql<=mob.charStats().getClassLevel(C)))
				return (C==this);
		}
		return false;
	}
	
	
	protected boolean armorCheck(MOB mob, int sourceCode, Environmental E)
	{   
		if(!(E instanceof Ability)) return true;
		if((allowedArmorLevel()!=CharClass.ARMOR_ANY)
		&&((requiredArmorSourceMinor()<0)||(sourceCode&CMMsg.MINOR_MASK)==requiredArmorSourceMinor())
		&&(isQualifyingAuthority(mob,(Ability)E))
		&&(mob.isMine(E))
		&&(!E.ID().equals("Skill_Recall"))
		&&(((Ability)E).classificationCode()!=Ability.ACODE_COMMON_SKILL)
		&&(((Ability)E).classificationCode()!=Ability.ACODE_LANGUAGE)
		&&(!CMLib.utensils().armorCheck(mob,allowedArmorLevel()))
		&&(CMLib.dice().rollPercentage()>(mob.charStats().getStat(getAttackAttribute())*2)))
			return false;
		return true;
	}
	protected boolean weaponCheck(MOB mob, int sourceCode, Environmental E)
	{
		if((((sourceCode&CMMsg.MINOR_MASK)==CMMsg.TYP_WEAPONATTACK)||((sourceCode&CMMsg.MINOR_MASK)==CMMsg.TYP_THROW))
		&&(E instanceof Weapon)
		&&(mob.charStats().getCurrentClass()==this)
		&&(((requiredWeaponMaterials()!=null)&&(!requiredWeaponMaterials().contains(new Integer(((Weapon)E).material()&RawMaterial.MATERIAL_MASK))))
			||((disallowedWeaponClasses(mob)!=null)&&(disallowedWeaponClasses(mob).contains(new Integer(((Weapon)E).weaponClassification())))))
		&&(CMLib.dice().rollPercentage()>(mob.charStats().getStat(getAttackAttribute())*2))
		&&(mob.fetchWieldedItem()!=null))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> fumble(s) horribly with "+E.name()+".");
			return false;
		}
		return true;
	}

	protected void giveMobAbility(MOB mob, Ability A, int proficiency, String defaultParm, boolean isBorrowedClass)
	{ giveMobAbility(mob,A,proficiency,defaultParm,isBorrowedClass,true);}
	protected void giveMobAbility(MOB mob, Ability A, int proficiency, String defaultParm, boolean isBorrowedClass, boolean autoInvoke)
	{
		if(mob.fetchAbility(A.ID())==null)
		{
			A=(Ability)A.copyOf();
			A.setSavable(!isBorrowedClass);
			A.setProficiency(proficiency);
			A.setMiscText(defaultParm);
			mob.addAbility(A);
			if(autoInvoke)
				A.autoInvocation(mob);
		}
	}

	public int[] maxStatAdjustments()
	{
		return maxStatAdj;
	}

	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
        if(CMSecurity.isAllowedEverywhere(mob,"ALLSKILLS"))
        {
            // the most efficient way of doing this -- just hash em!
            Hashtable alreadyAble=new Hashtable();
            Hashtable alreadyAff=new Hashtable();
            for(int a=0;a<mob.numAllEffects();a++)
            {
                Ability A=mob.fetchEffect(a);
                if(A!=null) alreadyAff.put(A.ID(),A);
            }
            for(int a=0;a<mob.numLearnedAbilities();a++)
            {
                Ability A=mob.fetchAbility(a);
                if(A!=null)
                {
                    A.setProficiency(100);
                    A.setSavable(false);
                    Ability A2=(Ability)alreadyAff.get(A.ID());
                    if(A2!=null)
                        A2.setProficiency(100);
                    else
                        A.autoInvocation(mob);
                    alreadyAble.put(A.ID(),A);
                }
            }
            for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
            {
                Ability A=(Ability)a.nextElement();
                int lvl=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
                if((lvl>=0)
			    &&(CMLib.ableMapper().qualifiesByAnyCharClass(A.ID()))
                &&(!alreadyAble.containsKey(A.ID())))
                    giveMobAbility(mob,A,100,"",true,false);
            }
            for(Enumeration e=CMLib.expertises().definitions();e.hasMoreElements();)
            	mob.addExpertise(((ExpertiseLibrary.ExpertiseDefinition)e.nextElement()).ID);
            alreadyAble.clear();
            alreadyAff.clear();
        }
        else
        {
        	Vector onesToAdd=new Vector();
			for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
			{
				Ability A=(Ability)a.nextElement();
				if((CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID())>0)
				&&(CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID())<=mob.baseCharStats().getClassLevel(this))
				&&(CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
					onesToAdd.addElement(A);
			}
			for(int v=0;v<onesToAdd.size();v++)
			{
				Ability A=(Ability)onesToAdd.elementAt(v);
				giveMobAbility(mob,A,CMLib.ableMapper().getDefaultProficiency(ID(),true,A.ID()),CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
        }
	}
	public void endCharacter(MOB mob)
	{
	}
	public void startCharacter(MOB mob, boolean isBorrowedClass, boolean verifyOnly)
	{
		if(!verifyOnly)
		{
			mob.setPractices(mob.getPractices()+getPracsFirstLevel());
			mob.setTrains(mob.getTrains()+getTrainsFirstLevel());
			grantAbilities(mob,isBorrowedClass);
		}
	}

	public Vector outfit(MOB myChar){return outfitChoices;}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{

	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
		if(affectableStats.getCurrentClass()==this)
		for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
			affectableStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ+i,affectableStats.getStat(CharStats.STAT_MAX_STRENGTH_ADJ+i)+maxStatAdj[i]);
	}

	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((msg.source()==myHost)
		&&(!msg.source().isMonster()))
		{
			if(!armorCheck(msg.source(),msg.sourceCode(),msg.tool()))
			{
				if(msg.tool()==null)
				    msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_VISUAL,CMStrings.replaceAll(armorFailMessage(),"<SKILL>","maneuver"));
				else
				    msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_VISUAL,CMStrings.replaceAll(armorFailMessage(),"<SKILL>",msg.tool().name()+" attempt"));
				return false;
			}
			if(!weaponCheck(msg.source(),msg.sourceCode(),msg.tool()))
				return false;
		}
		return true;
	}
	

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
	    if((msg.source()==myHost)
	    &&(msg.targetMinor()==CMMsg.TYP_WIELD)
	    &&(msg.target() instanceof Weapon)
		&&(msg.source().charStats().getCurrentClass()==this)
	    &&(!msg.source().isMonster())
		&&(((requiredWeaponMaterials()!=null)&&(!requiredWeaponMaterials().contains(new Integer(((Weapon)msg.target()).material()&RawMaterial.MATERIAL_MASK))))
			||((disallowedWeaponClasses(msg.source())!=null)&&(disallowedWeaponClasses(msg.source()).contains(new Integer(((Weapon)msg.target()).weaponClassification()))))))
	        msg.addTrailerMsg(CMClass.getMsg(msg.source(),msg.target(),null,CMMsg.TYP_OK_VISUAL,"<T-NAME> feel(s) a bit strange in your hands.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
	}
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public void unLevel(MOB mob){}

	public MOB fillOutMOB(MOB mob, int level)
	{
		if(mob==null) mob=CMClass.getMOB("StdMOB");
		if(!mob.isMonster()) return mob;

		long rejuv=Tickable.TICKS_PER_RLMIN+Tickable.TICKS_PER_RLMIN+(level*Tickable.TICKS_PER_RLMIN/2);
		if(rejuv>(Tickable.TICKS_PER_RLMIN*20)) rejuv=(Tickable.TICKS_PER_RLMIN*20);
		mob.baseEnvStats().setLevel(level);
		mob.baseEnvStats().setRejuv((int)rejuv);
		mob.baseEnvStats().setSpeed(getLevelSpeed(mob));
		mob.baseEnvStats().setArmor(getLevelArmor(mob));
		mob.baseEnvStats().setDamage(getLevelDamage(mob));
		mob.baseEnvStats().setAttackAdjustment(getLevelAttack(mob));
		mob.setMoney(CMLib.dice().roll(1,level,0)+CMLib.dice().roll(1,10,0));
		return mob;
	}

	public void level(MOB mob, Vector gainedAbilityIDs){}

	public int adjustExperienceGain(MOB mob, MOB victim, int amount) { return amount;}
	
	public int getLevelMana(MOB mob)
	{
		return 100+((mob.baseEnvStats().level()-1)*((int)Math.round(CMath.div(mob.baseCharStats().getStat(CharStats.STAT_INTELLIGENCE),getManaDivisor())))+(getManaDie()*(getManaDice()+1)/2));
	}

	public int getLevelAttack(MOB mob)
	{
		int attGain=(int)Math.round(CMath.div(mob.charStats().getStat(getAttackAttribute()),6.0))+getBonusAttackLevel();
		return ((mob.baseEnvStats().level()-1)*attGain);
	}

	public int getLevelArmor(MOB mob)
	{
		return 100-(mob.baseEnvStats().level()*(4+getBonusAttackLevel()));
	}

	public int getLevelDamage(MOB mob)
	{
		return 2+(mob.baseEnvStats().level());
	}

	public double getLevelSpeed(MOB mob)
	{
		return 1.0+Math.floor(CMath.div(mob.baseEnvStats().level(),25.0));
	}

	public int getLevelMove(MOB mob)
	{
		int move=100;
		double lvlMul=1.0;//-CMath.div(mob.envStats().level(),100.0);
		if(lvlMul<0.1) lvlMul=.1;
		if(mob.baseEnvStats().level()>1)
			move+=((int)Math.round(CMath.mul(mob.baseEnvStats().level()-1,CMath.mul(CMath.mul(lvlMul,CMath.div(mob.baseCharStats().getStat(CharStats.STAT_STRENGTH),18.0)),getMovementMultiplier()))));
		return move;
	}

	public boolean isValidClassBeneficiary(MOB killer, MOB killed, MOB mob, HashSet followers)
	{
		if((mob!=null)
        &&(mob!=killed)
		&&(!mob.amDead())
		&&((mob.getVictim()==killed)
		 ||(followers.contains(mob))
		 ||(mob==killer)))
			return true;
		return false;
	}
    
	public String classParms(){ return "";}
	public void setClassParms(String parms){}
	protected static String[] CODES={"CLASS","PARMS"};
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return ""+classParms();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setClassParms(val); break;
		}
	}
	public String[] getStatCodes(){return CODES;}
	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public boolean sameAs(CharClass E)
	{
		if(!(E instanceof StdCharClass)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
}
