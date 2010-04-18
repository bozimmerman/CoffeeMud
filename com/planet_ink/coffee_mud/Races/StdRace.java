package com.planet_ink.coffee_mud.Races;
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
public class StdRace implements Race
{
	public String ID(){	return "StdRace"; }
	public String name(){ return "StdRace"; }
	protected int practicesAtFirstLevel(){return 0;}
	protected int trainsAtFirstLevel(){return 0;}
	public int shortestMale(){return 24;}
	public int shortestFemale(){return 24;}
	public int heightVariance(){return 5;}
	public int lightestWeight(){return 60;}
	public int weightVariance(){return 10;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Unknown";}
	public boolean isGeneric(){return false;}
	public boolean classless(){return false;}
	public boolean leveless(){return false;}
	public boolean expless(){return false;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
	public int[] bodyMask(){return parts;}

    public CMObject newInstance(){return this;}
    public void initializeClass(){}
	private int[] agingChart={0,1,3,15,35,53,70,74,78};
	public int[] getAgingChart(){return agingChart;}

    protected static final Vector empty=new Vector();
	protected Weapon naturalWeapon=null;
	protected Vector naturalWeaponChoices=null;
	protected Vector outfitChoices=null;
	protected Hashtable racialEffectMap=null;
	protected String[] racialEffectNames(){return null;}
	protected int[] racialEffectLevels(){return null;}
	protected String[] racialEffectParms(){return null;}
	protected Hashtable racialAbilityMap=null;
	protected String[] racialAbilityNames(){return null;}
	protected int[] racialAbilityLevels(){return null;}
	protected int[] racialAbilityProficiencies(){return null;}
	protected boolean[] racialAbilityQuals(){return null;}
	protected boolean mappedCulturalAbilities=false;
	protected String[] culturalAbilityNames(){return null;}
	protected int[] culturalAbilityProficiencies(){return null;}
	protected boolean uncharmable(){return false;}
	protected boolean destroyBodyAfterUse(){return false;}
	protected String baseStatChgDesc = null;
	protected String sensesChgDesc = null;
	protected String dispChgDesc = null;
	protected String abilitiesDesc = null;
	protected String languagesDesc = null;

	public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	public boolean fertile(){return true;}

	public CMObject copyOf()
	{
		try
		{
			StdRace E=(StdRace)this.clone();
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this;
		}
	}

	public Race healthBuddy(){return this;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{

	}
	public void affectCharStats(MOB affectedMob, CharStats charStats)
	{
	}
	public void agingAffects(MOB mob, CharStats baseStats, CharStats charStats)
	{
		if((baseStats.getStat(CharStats.STAT_AGE)>0)&&(!CMSecurity.isAllowed(mob,mob.location(),"IMMORT")))
		{
			switch(baseStats.ageCategory())
			{
				case -1: break;
				case Race.AGE_INFANT:
				case Race.AGE_TODDLER:
				    charStats.setStat(CharStats.STAT_SAVE_MIND,charStats.getStat(CharStats.STAT_SAVE_MIND)-10);
				    charStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,charStats.getStat(CharStats.STAT_MAX_STRENGTH_ADJ)-2);
				    charStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,charStats.getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ)-4);
				    charStats.setStat(CharStats.STAT_MAX_WISDOM_ADJ,charStats.getStat(CharStats.STAT_MAX_WISDOM_ADJ)-4);
				    break;
				case Race.AGE_CHILD:
				    charStats.setStat(CharStats.STAT_SAVE_MIND,charStats.getStat(CharStats.STAT_SAVE_MIND)-5);
				    charStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,charStats.getStat(CharStats.STAT_MAX_STRENGTH_ADJ)-1);
				    charStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,charStats.getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ)-2);
				    charStats.setStat(CharStats.STAT_MAX_WISDOM_ADJ,charStats.getStat(CharStats.STAT_MAX_WISDOM_ADJ)-2);
				    break;
				case Race.AGE_YOUNGADULT:
				case Race.AGE_MATURE:
				    break;
				case Race.AGE_MIDDLEAGED:
				    charStats.setStat(CharStats.STAT_SAVE_MIND,charStats.getStat(CharStats.STAT_SAVE_MIND)+5);
				    charStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,charStats.getStat(CharStats.STAT_MAX_STRENGTH_ADJ)-1);
				    charStats.setStat(CharStats.STAT_MAX_CONSTITUTION_ADJ,charStats.getStat(CharStats.STAT_MAX_CONSTITUTION_ADJ)-1);
				    charStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ,charStats.getStat(CharStats.STAT_MAX_DEXTERITY_ADJ)-1);
				    charStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,charStats.getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ)+1);
				    charStats.setStat(CharStats.STAT_MAX_WISDOM_ADJ,charStats.getStat(CharStats.STAT_MAX_WISDOM_ADJ)+1);
				    charStats.setStat(CharStats.STAT_MAX_CHARISMA_ADJ,charStats.getStat(CharStats.STAT_MAX_CHARISMA_ADJ)+1);
				    break;
				case Race.AGE_OLD:
				    charStats.setStat(CharStats.STAT_SAVE_MIND,charStats.getStat(CharStats.STAT_SAVE_MIND)+10);
				    charStats.setStat(CharStats.STAT_SAVE_UNDEAD,charStats.getStat(CharStats.STAT_SAVE_UNDEAD)-5);
				    charStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,charStats.getStat(CharStats.STAT_MAX_STRENGTH_ADJ)-2);
				    charStats.setStat(CharStats.STAT_MAX_CONSTITUTION_ADJ,charStats.getStat(CharStats.STAT_MAX_CONSTITUTION_ADJ)-2);
				    charStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ,charStats.getStat(CharStats.STAT_MAX_DEXTERITY_ADJ)-2);
				    charStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,charStats.getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ)+2);
				    charStats.setStat(CharStats.STAT_MAX_WISDOM_ADJ,charStats.getStat(CharStats.STAT_MAX_WISDOM_ADJ)+2);
				    charStats.setStat(CharStats.STAT_MAX_CHARISMA_ADJ,charStats.getStat(CharStats.STAT_MAX_CHARISMA_ADJ)+2);
				    break;
				case Race.AGE_VENERABLE:
				    charStats.setStat(CharStats.STAT_SAVE_MIND,charStats.getStat(CharStats.STAT_SAVE_MIND)+15);
				    charStats.setStat(CharStats.STAT_SAVE_UNDEAD,charStats.getStat(CharStats.STAT_SAVE_UNDEAD)-25);
				    charStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,charStats.getStat(CharStats.STAT_MAX_STRENGTH_ADJ)-3);
				    charStats.setStat(CharStats.STAT_MAX_CONSTITUTION_ADJ,charStats.getStat(CharStats.STAT_MAX_CONSTITUTION_ADJ)-3);
				    charStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ,charStats.getStat(CharStats.STAT_MAX_DEXTERITY_ADJ)-3);
				    charStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,charStats.getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ)+3);
				    charStats.setStat(CharStats.STAT_MAX_WISDOM_ADJ,charStats.getStat(CharStats.STAT_MAX_WISDOM_ADJ)+3);
				    charStats.setStat(CharStats.STAT_MAX_CHARISMA_ADJ,charStats.getStat(CharStats.STAT_MAX_CHARISMA_ADJ)+3);
				    break;
				case Race.AGE_ANCIENT:
				{
					int[] chart=getAgingChart();
					int diff=chart[Race.AGE_ANCIENT]-chart[Race.AGE_VENERABLE];
					int age=baseStats.getStat(CharStats.STAT_AGE)-chart[Race.AGE_ANCIENT];
					int num=(diff>0)?(int)Math.abs(Math.floor(CMath.div(age,diff)))-1:1;
					if(num==0) num=1;
					if(num>16) num=16;
				    charStats.setStat(CharStats.STAT_SAVE_MIND,charStats.getStat(CharStats.STAT_SAVE_MIND)+20+(5*num));
				    charStats.setStat(CharStats.STAT_SAVE_UNDEAD,charStats.getStat(CharStats.STAT_SAVE_UNDEAD)-50+15+(5*num));
				    charStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,charStats.getStat(CharStats.STAT_MAX_STRENGTH_ADJ)-(3+(1*num)));
				    charStats.setStat(CharStats.STAT_MAX_CONSTITUTION_ADJ,charStats.getStat(CharStats.STAT_MAX_CONSTITUTION_ADJ)-(3+(num)));
				    charStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ,charStats.getStat(CharStats.STAT_MAX_DEXTERITY_ADJ)-(3+(num)));
				    charStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,charStats.getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ)+(3+(num)));
				    charStats.setStat(CharStats.STAT_MAX_WISDOM_ADJ,charStats.getStat(CharStats.STAT_MAX_WISDOM_ADJ)+(3+(num)));
				    charStats.setStat(CharStats.STAT_MAX_CHARISMA_ADJ,charStats.getStat(CharStats.STAT_MAX_CHARISMA_ADJ)+(3+(num)));
				    break;
				}
			}
		}
	}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{

	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(uncharmable()
		&&(msg.target()==myHost)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&(myHost instanceof MOB)
		&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_CHARMING)))
		{
			msg.source().location().show(msg.source(),myHost,CMMsg.MSG_OK_VISUAL,"<T-NAME> seem(s) unaffected by the charm magic from <S-NAMESELF>.");
			return false;
		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		// the sex rules
		if(!(myHost instanceof MOB)) return;

		MOB myChar=(MOB)myHost;
		if((msg.tool() instanceof Social)
		&&(msg.amITarget(myChar)||(msg.source()==myChar))
		&&(myChar.location()==msg.source().location())
		&&(msg.tool().Name().startsWith("MATE ")
            ||msg.tool().Name().startsWith("SEX ")))
        {
            if(msg.tool().Name().endsWith("SELF"))
            {
                if((msg.source()==myChar)
                &&(fertile())
                &&(msg.source().fetchWornItems(Wearable.WORN_LEGS|Wearable.WORN_WAIST,(short)-2048,(short)0).size()==0))
                {
                    msg.source().curState().adjFatigue(CharState.FATIGUED_MILLIS,msg.source().maxState());
                    myChar.curState().adjFatigue(CharState.FATIGUED_MILLIS,myChar.maxState());
                    Ability A=CMClass.getAbility("Spell_Blindness");
                    if(A!=null) A.invoke(myChar,myChar,true,myChar.envStats().level());
                }
            }
            else
            if((msg.target()==myChar)
            &&(msg.tool().Name().endsWith("<T-NAME>")))
            {
                msg.source().curState().adjFatigue(CharState.FATIGUED_MILLIS,msg.source().maxState());
                myChar.curState().adjFatigue(CharState.FATIGUED_MILLIS,myChar.maxState());
    			if((CMLib.dice().rollPercentage()<10)
                &&(myChar.charStats().getStat(CharStats.STAT_GENDER)==('F'))
                &&(msg.source().charStats().getStat(CharStats.STAT_GENDER)==('M'))
    			&&(fertile())
    			&&(myChar.fetchWornItems(Wearable.WORN_LEGS|Wearable.WORN_WAIST,(short)-2048,(short)0).size()==0)
    			&&(msg.source().fetchWornItems(Wearable.WORN_LEGS|Wearable.WORN_WAIST,(short)-2048,(short)0).size()==0)
    			&&((ID().equals("Human"))
    			   ||(msg.source().charStats().getMyRace().ID().equals("Human"))
    			   ||(msg.source().charStats().getMyRace().ID().equals(ID())))
    			&&(msg.source().charStats().getMyRace().fertile())
    			&&((msg.source().charStats().getStat(CharStats.STAT_AGE)==0)
    			        ||((msg.source().charStats().ageCategory()>Race.AGE_CHILD)
    			                &&(msg.source().charStats().ageCategory()<Race.AGE_OLD)))
    			&&((myChar.charStats().getStat(CharStats.STAT_AGE)==0)
    			        ||((myChar.charStats().ageCategory()>Race.AGE_CHILD)
    			                &&(myChar.charStats().ageCategory()<Race.AGE_OLD))))
    			{
    				Ability A=CMClass.getAbility("Pregnancy");
    				if((A!=null)
    				&&(myChar.fetchAbility(A.ID())==null)
    				&&(myChar.fetchEffect(A.ID())==null))
    					A.invoke(msg.source(),myChar,true,0);
    			}
            }
		}
	}
	public String arriveStr()
	{
		return "arrives";
	}
	public String leaveStr()
	{
		return "leaves";
	}
	public void level(MOB mob, Vector gainedAbilityIDs){}
	public int adjustExperienceGain(MOB host, MOB mob, MOB victim, int amount) { return amount;}

	public long getTickStatus(){return Tickable.STATUS_NOT;}
	public boolean tick(Tickable myChar, int tickID){return true;}
	public void startRacing(MOB mob, boolean verifyOnly)
	{
		if((!mappedCulturalAbilities)
		&&(culturalAbilityNames()!=null))
		{
			for(int a=0;a<culturalAbilityNames().length;a++)
			    CMLib.ableMapper().addCharAbilityMapping(ID(),0,culturalAbilityNames()[a],false);
			mappedCulturalAbilities=true;
		}
		if(!verifyOnly)
		{
			if(mob.baseEnvStats().level()<=1)
			{
				mob.setPractices(mob.getPractices()+practicesAtFirstLevel());
				mob.setTrains(mob.getTrains()+trainsAtFirstLevel());
			}
			setHeightWeight(mob.baseEnvStats(),(char)mob.baseCharStats().getStat(CharStats.STAT_GENDER));

			if((culturalAbilityNames()!=null)&&(culturalAbilityProficiencies()!=null)
			   &&(culturalAbilityNames().length==culturalAbilityProficiencies().length))
			{
				for(int a=0;a<culturalAbilityNames().length;a++)
				{
					Ability A=CMClass.getAbility(culturalAbilityNames()[a]);
					if(A!=null)
					{
						A.setProficiency(culturalAbilityProficiencies()[a]);
						mob.addAbility(A);
						A.autoInvocation(mob);
						if((mob.isMonster())
                        &&(!CMLib.flags().isChild(mob))
                        &&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE))
						{

							if(A.proficiency()>0) A.setProficiency(100);
							A.invoke(mob,mob,false,0);
							if(CMLib.flags().isChild(mob))
							{
								A=mob.fetchAbility("Common");
								if(A==null){ A=CMClass.getAbility("Common"); if(A!=null)mob.addAbility(A);}
								if(A!=null) A.setProficiency(100);
							}
						}
					}
				}
			}
		}
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
			naturalWeapon=CMClass.getWeapon("Natural");
		return naturalWeapon;
	}

	public Vector outfit(MOB myChar){return outfitChoices;}

	public String healthText(MOB viewer, MOB mob)
	{
		return CMLib.combat().standardMobCondition(viewer,mob);
	}

	protected Weapon funHumanoidWeapon()
	{
		if(naturalWeaponChoices==null)
		{
			naturalWeaponChoices=new Vector();
			for(int i=1;i<11;i++)
			{
				naturalWeapon=CMClass.getWeapon("StdWeapon");
				if(naturalWeapon==null) continue;
				switch(i)
				{
					case 1:
					case 2:
					case 3:
					naturalWeapon.setName("a quick punch");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 4:
					naturalWeapon.setName("fingernails and teeth");
					naturalWeapon.setWeaponType(Weapon.TYPE_PIERCING);
					break;
					case 5:
					naturalWeapon.setName("an elbow");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 6:
					naturalWeapon.setName("a backhand");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 7:
					naturalWeapon.setName("a strong jab");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 8:
					naturalWeapon.setName("a stinging punch");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 9:
					naturalWeapon.setName("a knee");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 10:
					naturalWeapon.setName("a head butt");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
				}
				naturalWeaponChoices.addElement(naturalWeapon);
			}
		}
		if(naturalWeaponChoices.size()>0)
			return (Weapon)naturalWeaponChoices.elementAt(CMLib.dice().roll(1,naturalWeaponChoices.size(),-1));
		return CMClass.getWeapon("Natural");
	}

	public Vector myResources(){return new Vector();}
	public void setHeightWeight(EnvStats stats, char gender)
	{
		int weightModifier=0;
		if(weightVariance()>0)
			weightModifier=CMLib.dice().roll(1,weightVariance(),0);
		stats.setWeight(lightestWeight()+weightModifier);
		int heightModifier=0;
		if(heightVariance()>0)
		{
			if(weightModifier>0)
			{
				double variance=CMath.div(weightModifier,weightVariance());
				heightModifier=(int)Math.round(CMath.mul(heightVariance(),variance));
			}
			else
				heightModifier=CMLib.dice().roll(1,heightVariance(),0);
		}
		if (gender == 'M')
			stats.setHeight(shortestMale()+heightModifier);
 		else
			stats.setHeight(shortestFemale()+heightModifier);
	}

	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	protected Item makeResource(String name, int type)
	{
		return (Item)CMLib.materials().makeResource(type,ID(),true,name);
	}

	public DeadBody getCorpseContainer(MOB mob, Room room)
	{
		if(room==null) room=mob.location();

		DeadBody Body=(DeadBody)CMClass.getItem("Corpse");
        if((mob.amFollowing()!=null)
        &&(mob.isMonster())
        &&((!mob.amFollowing().isMonster())||(!mob.amUltimatelyFollowing().isMonster())))
            Body.setSavedMOB((MOB)mob.copyOf());
		Body.setCharStats((CharStats)mob.baseCharStats().copyOf());
		Body.baseEnvStats().setLevel(mob.baseEnvStats().level());
		Body.baseEnvStats().setWeight(mob.baseEnvStats().weight());
		Body.setPlayerCorpse(!mob.isMonster());
        Body.setTimeOfDeath(System.currentTimeMillis());
		Body.setMobPKFlag(CMath.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL));
		Body.setName("the body of "+mob.Name().replace('\'','`'));
		Body.setMobName(mob.Name().replace('\'','`'));
		Body.setMobDescription(mob.description().replace('\'','`'));
		Body.setDisplayText("the body of "+mob.Name().replace('\'','`')+" lies here.");
		Ability AGE=mob.fetchEffect("Age");
		if(AGE!=null) Body.addNonUninvokableEffect(AGE);
		if(room!=null)
			room.addItemRefuse(Body,mob.isMonster()?CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_MONSTER_BODY):CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_BODY));
		Body.setDestroyAfterLooting(destroyBodyAfterUse());
		Body.recoverEnvStats();
		for(int i=0;i<mob.numAllEffects();i++)
		{
			Ability A=mob.fetchEffect(i);
			if((A!=null)&&(A instanceof DiseaseAffect))
			{
				if((CMath.bset(((DiseaseAffect)A).abilityCode(),DiseaseAffect.SPREAD_CONSUMPTION))
				||(CMath.bset(((DiseaseAffect)A).abilityCode(),DiseaseAffect.SPREAD_CONTACT)))
					Body.addNonUninvokableEffect((Ability)A.copyOf());
			}
		}

		Vector items=new Vector();
		CMLib.beanCounter().getTotalAbsoluteNativeValue(mob); // converts mob.get-Money();
		if(mob.getMoneyVariation()>0.0)
            CMLib.beanCounter().addMoney(mob, Math.random()*mob.getMoneyVariation());
        else
        if(mob.getMoneyVariation()<0.0)
    		CMLib.beanCounter().subtractMoney(mob, -(Math.random()*mob.getMoneyVariation()));
        Hashtable containerMap=new Hashtable();
        Hashtable itemMap=new Hashtable();
        DVector lootPolicies=CMLib.utensils().parseLootPolicyFor(mob);
		for(int i=0;i<mob.inventorySize();)
		{
			Item thisItem=mob.fetchInventory(i);
			if((thisItem!=null)&&(thisItem.savable()))
			{
				if(mob.isMonster())
				{
                    Item newItem=CMLib.utensils().isRuinedLoot(lootPolicies,thisItem);
                    if(newItem==null){i++; continue;}
                    if(newItem==thisItem) newItem=(Item)thisItem.copyOf();
                    if(newItem instanceof Container)
                        itemMap.put(thisItem,newItem);
                    if(thisItem.container()!=null)
                        containerMap.put(thisItem,thisItem.container());
					newItem.setContainer(null);
					newItem.setExpirationDate( System.currentTimeMillis() +
					                           CMProps.getIntVar( CMProps.SYSTEMI_EXPIRE_MONSTER_EQ )* TimeManager.MILI_HOUR );
					newItem.recoverEnvStats();
					thisItem=newItem;
					i++;
				}
				else
					mob.delInventory(thisItem);
				thisItem.unWear();
				if(thisItem.container()==null)
					thisItem.setContainer(Body);
				if(room!=null)
					room.addItem(thisItem);
				items.addElement(thisItem);
			}
			else
			if(thisItem!=null)
				mob.delInventory(thisItem);
			else
				i++;
		}

		Item dropItem=CMLib.catalog().getDropItem(mob,false);
		if(dropItem!=null)
		{
		    dropItem.unWear();
            if(dropItem.container()==null)
                dropItem.setContainer(Body);
            if(room!=null)
                room.addItem(dropItem);
            items.addElement(dropItem);
		}

        for(Enumeration e=itemMap.keys();e.hasMoreElements();)
        {
            Item oldItem=(Item)e.nextElement();
            Item newItem=(Item)itemMap.get(oldItem);
            Item oldContainer=(Item)containerMap.get(oldItem);
            if((oldContainer!=null)&&(newItem!=null))
                newItem.setContainer((Item)itemMap.get(oldContainer));
        }
		if(destroyBodyAfterUse())
		{
			for(int r=0;r<myResources().size();r++)
			{
				Item I=(Item)myResources().elementAt(r);
				if(I!=null)
				{
					I=(Item)I.copyOf();
					I.setContainer(Body);
					if(room!=null)
						room.addItemRefuse(I,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_MONSTER_EQ));
				}
			}
		}
		return Body;
	}

	public Vector racialEffects(MOB mob)
	{
		if(racialEffectNames()==null)
			return empty;

		if((racialEffectMap==null)
		&&(racialEffectNames()!=null)
		&&(racialEffectLevels()!=null)
		&&(racialEffectParms()!=null))
			racialEffectMap=new Hashtable();

		if(racialEffectMap==null) return empty;

		Integer level=null;
		if(mob!=null)
			level=Integer.valueOf(mob.envStats().level());
		else
			level=Integer.valueOf(Integer.MAX_VALUE);

		if(racialEffectMap.containsKey(level))
			return (Vector)racialEffectMap.get(level);
		Vector finalV=new Vector();
		for(int v=0;v<racialEffectLevels().length;v++)
		{
			if((racialEffectLevels()[v]<=level.intValue())
			&&(racialEffectNames().length>v)
			&&(racialEffectParms().length>v))
			{
				Ability A=CMClass.getAbility(racialEffectNames()[v]);
				if(A!=null)
				{
					A.setProficiency(CMLib.ableMapper().getMaxProficiency(mob,true,A.ID()));
					A.setSavable(false);
					A.setMiscText(racialEffectParms()[v]);
					A.makeNonUninvokable();
					finalV.addElement(A);
				}
			}
		}
		racialEffectMap.put(level,finalV);
		return finalV;
	}

	public Race makeGenRace()
	{
		Race GR=(Race)CMClass.getRace("GenRace").copyOf();
		GR.setRacialParms("<RACE><ID>"+ID()+"</ID><NAME>"+name()+"</NAME></RACE>");
		GR.setStat("CAT",racialCategory());
		GR.setStat("BWEIGHT",""+lightestWeight());
		GR.setStat("VWEIGHT",""+weightVariance());
		GR.setStat("MHEIGHT",""+shortestMale());
		GR.setStat("FHEIGHT",""+shortestFemale());
		GR.setStat("WEAR",""+forbiddenWornBits());
		GR.setStat("AVAIL",""+availabilityCode());
		GR.setStat("VHEIGHT",""+heightVariance());
		GR.setStat("PLAYER",""+CMProps.getIntVar(CMProps.SYSTEMI_MUDTHEME));
		GR.setStat("LEAVE",leaveStr());
		GR.setStat("ARRIVE",arriveStr());
		GR.setStat("HEALTHRACE",CMClass.classID(this));
		GR.setStat("EVENTRACE",CMClass.classID(this));
		GR.setStat("BODYKILL",""+destroyBodyAfterUse());
		GR.setStat("HELP",""+CMLib.help().getHelpText(name(),null,false));
		GR.setStat("AGING",CMParms.toStringList(getAgingChart()));
		for(int i=0;i<Race.BODYPARTSTR.length;i++)
				GR.bodyMask()[i]=bodyMask()[i];

		Weapon W=myNaturalWeapon();
		Weapon NW=CMClass.getWeapon("Natural");
		if((W!=null)&&(W!=NW))
		{
			if(!W.isGeneric())
			{
				Weapon W2=CMClass.getWeapon("GenWeapon");
				W2.setName(W.name());
				W2.setWeaponClassification(W.weaponClassification());
				W2.setWeaponType(W.weaponType());
				W2.baseEnvStats().setDamage(W.envStats().damage());
				W2.baseEnvStats().setAttackAdjustment(W.envStats().attackAdjustment());
				W2.recoverEnvStats();
				W2.text();
				W=W2;
			}
			GR.setStat("WEAPONCLASS",W.ID());
			GR.setStat("WEAPONXML",W.text());
		}
		GR.setStat("WEAPONRACE",getClass().getName());

        EnvStats RS=(EnvStats)CMClass.getCommon("DefaultEnvStats");
        RS.setAllValues(0);
        MOB fakeMOB=CMClass.getMOB("StdMOB");
        affectEnvStats(fakeMOB,RS);
        RS.setRejuv(0);
		GR.setStat("ESTATS",CMLib.coffeeMaker().getEnvStatsStr(RS));

        CharStats S1=(CharStats)CMClass.getCommon("DefaultCharStats");
        S1.setAllValues(0);
        CharStats S2=(CharStats)CMClass.getCommon("DefaultCharStats");
        S2.setAllValues(10);
        CharStats S3=(CharStats)CMClass.getCommon("DefaultCharStats");
        S3.setAllValues(11);
        CharStats SETSTAT=(CharStats)CMClass.getCommon("DefaultCharStats");
        SETSTAT.setAllValues(0);
        CharStats ADJSTAT=(CharStats)CMClass.getCommon("DefaultCharStats");
        ADJSTAT.setAllValues(0);
		affectCharStats(fakeMOB,S1);
		affectCharStats(fakeMOB,S2);
		affectCharStats(fakeMOB,S3);
		for(int i: CharStats.CODES.ALL())
			if(i!=CharStats.STAT_AGE)
			{
				if(CharStats.CODES.isBASE(i))
				{
					int max = CharStats.CODES.toMAXBASE(i);
					if((S2.getStat(i)==S3.getStat(i))
					&&(S1.getStat(max)!=0))
					{
						SETSTAT.setStat(i,S2.getStat(i));
						S1.setStat(max,0);
						S2.setStat(max,0);
						S3.setStat(max,0);
					}
					else
						ADJSTAT.setStat(i,S1.getStat(i));
				}
				else
					ADJSTAT.setStat(i,S1.getStat(i));
			}
		GR.setStat("ASTATS",CMLib.coffeeMaker().getCharStatsStr(ADJSTAT));
		GR.setStat("CSTATS",CMLib.coffeeMaker().getCharStatsStr(SETSTAT));

        CharState CS=(CharState)CMClass.getCommon("DefaultCharState"); CS.setAllValues(0);
		affectCharState(fakeMOB,CS);
		GR.setStat("ASTATE",CMLib.coffeeMaker().getCharStateStr(CS));

        //CharState STARTCS=(CharState)CMClass.getCommon("DefaultCharState"); STARTCS.setAllValues(0);
		//startRacing(fakeMOB,falsed);
		//GR.setStat("STARTASTATE",CMLib.coffeeMaker().getCharStateStr(STARTCS));

		GR.setStat("DISFLAGS",""+((classless()?Race.GENFLAG_NOCLASS:0)
								|(leveless()?Race.GENFLAG_NOLEVELS:0)
								|(uncharmable()?Race.GENFLAG_NOCHARM:0)
								|(fertile()?0:Race.GENFLAG_NOFERTILE)
								|(expless()?Race.GENFLAG_NOEXP:0)));

        Vector rscs=myResources();
        if(rscs==null)rscs=new Vector();
        String txt=null;
        Item I=null;
        GR.setStat("NUMRSC",""+rscs.size());
		for(int i=0;i<rscs.size();i++)
        {
            I=(Item)rscs.elementAt(i);
            I.recoverEnvStats();
            txt=I.text();
            GR.setStat("GETRSCID"+i,I.ID());
			GR.setStat("GETRSCPARM"+i,txt);
        }

		Vector outfit=outfit(null);
        if(outfit==null) outfit=new Vector();
		GR.setStat("NUMOFT",""+outfit.size());
		for(int i=0;i<outfit.size();i++)
			GR.setStat("GETOFTID"+i,((Item)outfit.elementAt(i)).ID());
		for(int i=0;i<outfit.size();i++)
			GR.setStat("GETOFTPARM"+i,((Item)outfit.elementAt(i)).text());

        GR.setStat("NUMRABLE","");
        if(racialAbilityNames()!=null)
        {
    		GR.setStat("NUMRABLE",""+racialAbilityNames().length);
    		for(int i=0;i<racialAbilityNames().length;i++)
    		{
    			GR.setStat("GETRABLE"+i,racialAbilityNames()[i]);
    			GR.setStat("GETRABLELVL"+i,""+racialAbilityLevels()[i]);
    			GR.setStat("GETRABLEQUAL"+i,""+racialAbilityQuals()[i]);
    			GR.setStat("GETRABLEPROF"+i,""+racialAbilityProficiencies()[i]);
    		}
        }

        GR.setStat("NUMCABLE","");
        if(culturalAbilityNames()!=null)
        {
            GR.setStat("NUMCABLE",""+culturalAbilityNames().length);
    		for(int i=0;i<culturalAbilityNames().length;i++)
    		{
    			GR.setStat("GETCABLE"+i,culturalAbilityNames()[i]);
    			GR.setStat("GETCABLEPROF"+i,""+culturalAbilityProficiencies()[i]);
    		}
        }

		GR.setStat("NUMREFF","");
        if(racialEffectNames()!=null)
        {
            GR.setStat("NUMREFF",""+racialEffectNames().length);
    		for(int i=0;i<racialEffectNames().length;i++)
    		{
    			GR.setStat("GETREFF"+i,racialEffectNames()[i]);
    			GR.setStat("GETREFFLVL"+i,""+racialEffectLevels()[i]);
    			GR.setStat("GETREFFPARM"+i,racialEffectParms()[i]);
    		}
        }
        fakeMOB.destroy();
		return GR;
	}

	public Race mixRace(Race race, String newRaceID, String newRaceName)
	{
		Race GR=(Race)CMClass.getRace("GenRace").copyOf();
		Race race1=this;
		Race race2=race;
		GR.setRacialParms("<RACE><ID>"+newRaceID+"</ID><NAME>"+newRaceName+"</NAME></RACE>");
		if(!race1.isGeneric()) race1=race1.makeGenRace();
		if(!race2.isGeneric()) race2=race2.makeGenRace();

		Race nonHuman=(race1.ID().equals("Human"))?race2:race1;
		Race otherRace=(nonHuman==race1)?race2:race1;
		GR.setStat("CAT",nonHuman.racialCategory());
		GR.setStat("BWEIGHT",""+((race1.lightestWeight()+race2.lightestWeight())/2));
		GR.setStat("VWEIGHT",""+((race1.weightVariance()+race2.weightVariance())/2));
		GR.setStat("MHEIGHT",""+((race1.shortestMale()+race2.shortestMale())/2));
		GR.setStat("FHEIGHT",""+((race1.shortestFemale()+race2.shortestFemale())/2));
		GR.setStat("VHEIGHT",""+((race1.heightVariance()+race2.heightVariance())/2));
		GR.setStat("PLAYER",""+CMProps.getIntVar(CMProps.SYSTEMI_MUDTHEME));
		GR.setStat("LEAVE",nonHuman.leaveStr());
		GR.setStat("ARRIVE",nonHuman.arriveStr());
		GR.setStat("HEALTHRACE",otherRace.getStat("HEALTHRACE"));
		GR.setStat("EVENTRACE",otherRace.getStat("EVENTRACE"));
		GR.setStat("WEAPONRACE",otherRace.getStat("WEAPONRACE"));
		int[] aging=(int[])race1.getAgingChart().clone();
		for(int i=0;i<aging.length;i++)
		    aging[i]+=race2.getAgingChart()[i];
		for(int i=0;i<aging.length;i++)
		    aging[i]=aging[i]/2;

		long race1worn=CMath.s_long(otherRace.getStat("WEAR"));
		long race2worn=CMath.s_long(nonHuman.getStat("WEAR"));
		long finalWear=0;
		boolean toggle=false;
		for(long wornCode : Wearable.CODES.ALL())
            if(wornCode != Wearable.IN_INVENTORY)
            {
				if((!CMath.bset(race1worn,wornCode))&&(!CMath.bset(race2worn,wornCode)))
				{}
				else
				if(CMath.bset(race1worn,wornCode)&&CMath.bset(race2worn,wornCode))
					finalWear=finalWear|wornCode;
				else
				if(CMath.bset(race1worn,wornCode))
					finalWear=finalWear|wornCode;
				else
				if(toggle)
				{
					finalWear=finalWear|wornCode;
					toggle=!toggle;
				}
            }

		GR.setStat("WEAR",""+finalWear);
		Weapon W=otherRace.myNaturalWeapon();
		if(W==null) W=nonHuman.myNaturalWeapon();
		if(W!=null)
		{
			GR.setStat("WEAPONCLASS",W.ID());
			GR.setStat("WEAPONXML",W.text());
		}

		GR.setStat("BODYKILL",""+otherRace.getStat("BODYKILL"));
		GR.setStat("AGING",CMParms.toStringList(aging));
		for(int i=0;i<Race.BODYPARTSTR.length;i++)
			if((race1.bodyMask()[i]>0)&&(race2.bodyMask()[i]>0))
				GR.bodyMask()[i]=((race1.bodyMask()[i]+race2.bodyMask()[i])/2);
			else
			if((race1.bodyMask()[i]<=0)&&(race2.bodyMask()[i]>=0))
				GR.bodyMask()[i]=race2.bodyMask()[i];
			else
				GR.bodyMask()[i]=race1.bodyMask()[i];

		EnvStats RS1=(EnvStats)CMClass.getCommon("DefaultEnvStats");
		RS1.setAllValues(0);
		CMLib.coffeeMaker().setEnvStats(RS1,race1.getStat("ESTATS"));

		EnvStats RS2=(EnvStats)CMClass.getCommon("DefaultEnvStats");
		RS2.setAllValues(0);
		CMLib.coffeeMaker().setEnvStats(RS2,race2.getStat("ESTATS"));

		EnvStats RS=(EnvStats)CMClass.getCommon("DefaultEnvStats");
		RS.setAbility((RS1.ability()+RS2.ability())/2);
		RS.setArmor((RS2.armor()+RS2.armor())/2);
		RS.setAttackAdjustment((RS1.attackAdjustment()+RS2.attackAdjustment())/2);
		RS.setDamage((RS1.damage()+RS2.damage())/2);
		RS.setHeight((RS1.height()+RS2.height())/2);
		RS.setSpeed((RS1.speed()+RS2.speed())/2.0);
		RS.setWeight((RS1.weight()+RS2.weight())/2);
		RS.setRejuv(0);
		GR.setStat("ESTATS",CMLib.coffeeMaker().getEnvStatsStr(RS));

        CharStats SETSTAT1=(CharStats)CMClass.getCommon("DefaultCharStats");
        SETSTAT1.setAllValues(0);
        CMLib.coffeeMaker().setCharStats(SETSTAT1,race1.getStat("CSTATS"));

        CharStats SETSTAT2=(CharStats)CMClass.getCommon("DefaultCharStats");
        SETSTAT2.setAllValues(0);
        CMLib.coffeeMaker().setCharStats(SETSTAT2,race2.getStat("CSTATS"));

        CharStats SETSTAT=(CharStats)CMClass.getCommon("DefaultCharStats");
        SETSTAT.setAllValues(0);

        CharStats ADJSTAT1=(CharStats)CMClass.getCommon("DefaultCharStats");
        ADJSTAT1.setAllValues(0);
        CMLib.coffeeMaker().setCharStats(ADJSTAT1,race1.getStat("ASTATS"));

        CharStats ADJSTAT2=(CharStats)CMClass.getCommon("DefaultCharStats");
        ADJSTAT2.setAllValues(0);
        CMLib.coffeeMaker().setCharStats(ADJSTAT2,race2.getStat("ASTATS"));

        CharStats ADJSTAT=(CharStats)CMClass.getCommon("DefaultCharStats");
        ADJSTAT.setAllValues(0);

		for(int i: CharStats.CODES.ALL())
		{
			if(CharStats.CODES.isBASE(i))
			{
				SETSTAT.setStat(i,(SETSTAT1.getStat(i)+SETSTAT2.getStat(i))/2);
				int newStat=((ADJSTAT1.getStat(i)+ADJSTAT2.getStat(i))/2);
				if(newStat>5)
					ADJSTAT.setStat(i,5);
				else
					ADJSTAT.setStat(i,newStat);
			}
			else
			if((i!=CharStats.STAT_GENDER)&&(i!=CharStats.STAT_AGE))
				ADJSTAT.setStat(i,(ADJSTAT1.getStat(i)+ADJSTAT2.getStat(i))/2);
		}
		GR.setStat("ASTATS",CMLib.coffeeMaker().getCharStatsStr(ADJSTAT));
		GR.setStat("CSTATS",CMLib.coffeeMaker().getCharStatsStr(SETSTAT));

        CharState CS1=(CharState)CMClass.getCommon("DefaultCharState");
        CS1.setAllValues(0);
        CMLib.coffeeMaker().setCharState(CS1,race1.getStat("ASTATE"));
        CharState CS2=(CharState)CMClass.getCommon("DefaultCharState");
        CS2.setAllValues(0);
        CMLib.coffeeMaker().setCharState(CS2,race2.getStat("ASTATE"));
        CharState CS=(CharState)CMClass.getCommon("DefaultCharState");
        CS.setAllValues(0);

		CS.setFatigue((CS1.getFatigue()+CS2.getFatigue())/2);
		CS.setHitPoints((CS1.getHitPoints()+CS2.getHitPoints())/2);
		CS.setHunger((CS1.getHunger()+CS2.getHunger())/2);
		CS.setMana((CS1.getMana()+CS2.getMana())/2);
		CS.setMovement((CS1.getMovement()+CS2.getMovement())/2);
		CS.setThirst((CS1.getThirst()+CS2.getThirst())/2);
		GR.setStat("ASTATE",CMLib.coffeeMaker().getCharStateStr(CS));

        CharState STARTCS1=(CharState)CMClass.getCommon("DefaultCharState");
        STARTCS1.setAllValues(0);
        CMLib.coffeeMaker().setCharState(STARTCS1,race1.getStat("STARTASTATE"));

        CharState STARTCS2=(CharState)CMClass.getCommon("DefaultCharState");
        STARTCS2.setAllValues(0);
        CMLib.coffeeMaker().setCharState(STARTCS1,race2.getStat("STARTASTATE"));

        CharState STARTCS=(CharState)CMClass.getCommon("DefaultCharState");
        STARTCS.setAllValues(0);

        STARTCS.setFatigue((STARTCS1.getFatigue()+STARTCS2.getFatigue())/2);
        STARTCS.setHitPoints((STARTCS1.getHitPoints()+STARTCS2.getHitPoints())/2);
        STARTCS.setHunger((STARTCS1.getHunger()+STARTCS2.getHunger())/2);
        STARTCS.setMana((STARTCS1.getMana()+STARTCS2.getMana())/2);
        STARTCS.setMovement((STARTCS1.getMovement()+STARTCS2.getMovement())/2);
        STARTCS.setThirst((STARTCS1.getThirst()+STARTCS2.getThirst())/2);
		GR.setStat("STARTASTATE",CMLib.coffeeMaker().getCharStateStr(STARTCS));

		GR.setStat("DISFLAGS",""+(CMath.s_int(race1.getStat("DISFLAGS"))|CMath.s_int(race2.getStat("DISFLAGS"))));

		Vector rscs=nonHuman.myResources();
		GR.setStat("NUMRSC",""+rscs.size());
		for(int i=0;i<rscs.size();i++)
			GR.setStat("GETRSCID"+i,((Item)rscs.elementAt(i)).ID());
		for(int i=0;i<rscs.size();i++)
			GR.setStat("GETRSCPARM"+i,((Item)rscs.elementAt(i)).text());

		GR.setStat("NUMOFT","");
		Race outfitRace=(nonHuman.outfit(null)!=null)?nonHuman:otherRace;
		Vector outfit=outfitRace.outfit(null);
		if((outfit!=null)&&(outfit.size()>0))
		{
			GR.setStat("NUMOFT",""+outfit.size());
			for(int i=0;i<outfit.size();i++)
				GR.setStat("GETOFTID"+i,((Item)outfit.elementAt(i)).ID());
			for(int i=0;i<outfit.size();i++)
				GR.setStat("GETOFTPARM"+i,((Item)outfit.elementAt(i)).text());
		}

		race1.racialAbilities(null);
		race2.racialAbilities(null);
		DVector dvata1=CMLib.ableMapper().getUpToLevelListings(race1.ID(),Integer.MAX_VALUE,true,false);
		DVector dvata2=CMLib.ableMapper().getUpToLevelListings(race2.ID(),Integer.MAX_VALUE,true,false);
		// kill half of them.
		for(int i=1;i<dvata1.size();i++)
			dvata1.removeElementAt(i);
		for(int i=1;i<dvata2.size();i++)
			dvata2.removeElementAt(i);

		if((dvata1.size()+dvata2.size())>0)
			GR.setStat("NUMRABLE",""+(dvata1.size()+dvata2.size()));
		else
			GR.setStat("NUMRABLE","");
		for(int i=0;i<dvata1.size();i++)
		{
			GR.setStat("GETRABLE"+i,(String)dvata1.elementAt(i,1));
			GR.setStat("GETRABLELVL"+i,""+CMLib.ableMapper().getQualifyingLevel(race1.ID(),false,(String)dvata1.elementAt(i,1)));
			GR.setStat("GETRABLEQUAL"+i,""+(!CMLib.ableMapper().getDefaultGain(race1.ID(),false,(String)dvata1.elementAt(i,1))));
			GR.setStat("GETRABLEPROF"+i,""+CMLib.ableMapper().getDefaultProficiency(race1.ID(),false,(String)dvata1.elementAt(i,1)));
		}
		for(int i=0;i<dvata2.size();i++)
		{
			GR.setStat("GETRABLE"+(i+dvata1.size()),(String)dvata2.elementAt(i,1));
			GR.setStat("GETRABLELVL"+(i+dvata1.size()),""+CMLib.ableMapper().getQualifyingLevel(race2.ID(),false,(String)dvata2.elementAt(i,1)));
			GR.setStat("GETRABLEQUAL"+(i+dvata1.size()),""+(!CMLib.ableMapper().getDefaultGain(race2.ID(),false,(String)dvata2.elementAt(i,1))));
			GR.setStat("GETRABLEPROF"+(i+dvata1.size()),""+CMLib.ableMapper().getDefaultProficiency(race2.ID(),false,(String)dvata2.elementAt(i,1)));
		}

		Vector data1=race1.racialEffects(null);
		Vector data2=race2.racialEffects(null);
		// kill half of them.
		for(int i=1;i<data1.size();i++)
			data1.removeElementAt(i);
		for(int i=1;i<data2.size();i++)
			data2.removeElementAt(i);

		if((data1.size()+data2.size())>0)
			GR.setStat("NUMREFF",""+(data1.size()+data2.size()));
		else
			GR.setStat("NUMREFF","");
		for(int i=0;i<data1.size();i++)
		{
			GR.setStat("GETREFF"+i,(String)data1.elementAt(i));
			GR.setStat("GETREFFLVL"+i,""+CMLib.ableMapper().getQualifyingLevel(race1.ID(),false,(String)data1.elementAt(i)));
			GR.setStat("GETREFFPARM"+i,""+CMLib.ableMapper().getDefaultProficiency(race1.ID(),false,(String)data1.elementAt(i)));
		}
		for(int i=0;i<data2.size();i++)
		{
			GR.setStat("GETREFF"+(i+data1.size()),(String)data2.elementAt(i));
			GR.setStat("GETREFFLVL"+(i+data1.size()),""+CMLib.ableMapper().getQualifyingLevel(race2.ID(),false,(String)data2.elementAt(i)));
			GR.setStat("GETREFFPARM"+(i+data1.size()),""+CMLib.ableMapper().getDefaultProficiency(race2.ID(),false,(String)data2.elementAt(i)));
		}
		return GR;
	}



    public DVector culturalAbilities()
    {
        DVector ables=new DVector(2);
        if((culturalAbilityNames()!=null)
        &&(culturalAbilityProficiencies()!=null))
            for(int i=0;i<culturalAbilityNames().length;i++)
                ables.addElement(culturalAbilityNames()[i],Integer.valueOf(culturalAbilityProficiencies()[i]));
        return ables;
    }
	public Vector racialAbilities(MOB mob)
	{
		if((racialAbilityMap==null)
		&&(racialAbilityNames()!=null)
		&&(racialAbilityLevels()!=null)
		&&(racialAbilityProficiencies()!=null)
		&&(racialAbilityQuals()!=null))
		{
			racialAbilityMap=new Hashtable();
			for(int i=0;i<racialAbilityNames().length;i++)
			{
				CMLib.ableMapper().addCharAbilityMapping(ID(),
											 racialAbilityLevels()[i],
											 racialAbilityNames()[i],
											 racialAbilityProficiencies()[i],
											 "",
											 !racialAbilityQuals()[i],
											 false);
			}
		}
		if(racialAbilityMap==null) return empty;
		Integer level=null;
		if(mob!=null)
			level=Integer.valueOf(mob.envStats().level());
		else
			level=Integer.valueOf(Integer.MAX_VALUE);
		if(racialAbilityMap.containsKey(level))
			return (Vector)racialAbilityMap.get(level);
		DVector V=CMLib.ableMapper().getUpToLevelListings(ID(),level.intValue(),true,(mob!=null));
		Vector finalV=new Vector();
		for(int v=0;v<V.size();v++)
		{
			Ability A=CMClass.getAbility((String)V.elementAt(v,1));
			if(A!=null)
			{
				A.setProficiency(CMLib.ableMapper().getDefaultProficiency(ID(),false,A.ID()));
				A.setSavable(false);
				A.setMiscText(CMLib.ableMapper().getDefaultParm(ID(),false,A.ID()));
				finalV.addElement(A);
			}
		}
		racialAbilityMap.put(level,finalV);
		return finalV;
	}

	public String getStatAdjDesc()
	{
		makeStatChgDesc();
		return baseStatChgDesc;
	}
	public String getSensesChgDesc()
	{
		makeStatChgDesc();
		return sensesChgDesc;
	}
	public String getDispositionChgDesc()
	{
		makeStatChgDesc();
		return dispChgDesc;
	}
	public String getTrainAdjDesc()
	{
		if(trainsAtFirstLevel()>0)
			return "trains+"+trainsAtFirstLevel();
		if(trainsAtFirstLevel()<0)
			return "trains"+trainsAtFirstLevel();
		return "";
	}
	public String getPracAdjDesc()
	{
		if(practicesAtFirstLevel()>0)
			return "practices+"+practicesAtFirstLevel();
		if(practicesAtFirstLevel()<0)
			return "practices"+practicesAtFirstLevel();
		return "";
	}
	public String getAbilitiesDesc()
	{
		makeStatChgDesc();
		return abilitiesDesc;
	}
	public String getLanguagesDesc()
	{
		makeStatChgDesc();
		return languagesDesc;
	}
	public String racialParms(){ return "";}
	public void setRacialParms(String parms){}
	
	protected void clrStatChgDesc()
	{ 
		baseStatChgDesc=null;
		dispChgDesc=null;
		sensesChgDesc=null;
		abilitiesDesc = null;
		languagesDesc = null;
	}
	protected void makeStatChgDesc()
	{
		if((baseStatChgDesc == null)
		||(dispChgDesc==null)
		||(sensesChgDesc==null))
		{
			StringBuilder str=new StringBuilder("");
			MOB mob=CMClass.getMOB("StdMOB");
			mob.setSession(null);
			mob.baseCharStats().setMyRace(this);
			startRacing(mob,false);
			mob.recoverCharStats();
			mob.recoverCharStats();
			mob.recoverEnvStats();
			mob.recoverMaxState();
			MOB mob2=CMClass.getMOB("StdMOB");
			mob2.setSession(null);
			mob2.baseCharStats().setMyRace(new StdRace());
			mob2.recoverCharStats();
			mob2.recoverEnvStats();
			mob2.recoverMaxState();
            for(int c: CharStats.CODES.ALL())
            {
                int oldStat=mob2.charStats().getStat(c);
                int newStat=mob.charStats().getStat(c);
                if(oldStat>newStat)
                    str.append(CharStats.CODES.DESC(c).toLowerCase()+"-"+(oldStat-newStat)+", ");
                else
                if(newStat>oldStat)
                    str.append(CharStats.CODES.DESC(c).toLowerCase()+"+"+(newStat-oldStat)+", ");
            }
            dispChgDesc=CMLib.flags().describeDisposition(mob);
            sensesChgDesc=CMLib.flags().describeSenses(mob);
            mob.destroy();
            mob2.destroy();
            baseStatChgDesc=str.toString();
            if(baseStatChgDesc.endsWith(", "))
            	baseStatChgDesc=baseStatChgDesc.substring(0,baseStatChgDesc.length()-2);
            StringBuilder astr=new StringBuilder("");
            StringBuilder lstr=new StringBuilder("");
            Vector ables=racialAbilities(null);
            if(ables==null) ables=new Vector();
            else ables=(Vector)ables.clone();
            DVector cables=culturalAbilities();
            Ability A=null;
            if(cables!=null)
            {
                for(int c=0;c<cables.size();c++)
                {
                    A=CMClass.getAbility((String)cables.elementAt(c,1));
                    if(A!=null)
                    {
                        A.setProficiency(((Integer)cables.elementAt(c,2)).intValue());
                        ables.addElement(A);
                    }
                }
            }
    		for(Enumeration e=ables.elements();e.hasMoreElements();)
    		{
    			A=(Ability)e.nextElement();
    			str = ((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE)?lstr:astr;
    			if(A.proficiency()<=0)
    				str.append(A.name()+", ");
    			else
    				str.append(A.name()+"("+A.proficiency()+"%), ");
    		}
    		abilitiesDesc=astr.toString();
    		if(abilitiesDesc.endsWith(", "))
    			abilitiesDesc=abilitiesDesc.substring(0,abilitiesDesc.length()-2);
    		languagesDesc=lstr.toString();
    		if(languagesDesc.endsWith(", "))
    			languagesDesc=languagesDesc.substring(0,languagesDesc.length()-2);
		}
	}
	
	
	protected static String[] CODES={"CLASS","PARMS"};
    public int getSaveStatIndex(){return getStatCodes().length;}
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return ""+racialParms();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setRacialParms(val); break;
		}
	}
	public String[] getStatCodes(){return CODES;}
    public boolean isStat(String code){ return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;}
	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public boolean sameAs(Race E)
	{
		if(!(E instanceof StdRace)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
}
