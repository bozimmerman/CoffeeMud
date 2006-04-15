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
	protected int[] racialAbilityProfficiencies(){return null;}
	protected boolean[] racialAbilityQuals(){return null;}
	protected boolean mappedCulturalAbilities=false;
	protected String[] culturalAbilityNames(){return null;}
	protected int[] culturalAbilityProfficiencies(){return null;}
	protected boolean uncharmable(){return false;}
	protected boolean destroyBodyAfterUse(){return false;}

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
				    charStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ,charStats.getStat(CharStats.STAT_MAX_CONSTITUTION_ADJ)-2);
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
                &&(msg.source().fetchWornItems(Item.WORN_LEGS|Item.WORN_WAIST,(short)0).size()==0))
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
    			&&(myChar.fetchWornItems(Item.WORN_LEGS|Item.WORN_WAIST,(short)0).size()==0)
    			&&(msg.source().fetchWornItems(Item.WORN_LEGS|Item.WORN_WAIST,(short)0).size()==0)
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
	public int adjustExperienceGain(MOB mob, MOB victim, int amount) { return amount;}
	
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

			if((culturalAbilityNames()!=null)&&(culturalAbilityProfficiencies()!=null)
			   &&(culturalAbilityNames().length==culturalAbilityProfficiencies().length))
			{
				for(int a=0;a<culturalAbilityNames().length;a++)
				{
					Ability A=CMClass.getAbility(culturalAbilityNames()[a]);
					if(A!=null)
					{
						A.setProfficiency(culturalAbilityProfficiencies()[a]);
						mob.addAbility(A);
						A.autoInvocation(mob);
						if((mob.isMonster())&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE))
							A.invoke(mob,mob,false,0);
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

	public String healthText(MOB mob)
	{
		return CMLib.combat().standardMobCondition(mob);
	}

	public Weapon funHumanoidWeapon()
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
					naturalWeapon.setWeaponType(Weapon.TYPE_NATURAL);
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

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	protected Item makeResource(String name, int type)
	{
		Item I=null;
		if(((type&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_FLESH)
		||((type&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION))
			I=CMClass.getItem("GenFoodResource");
		else
		if((type&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
			I=CMClass.getItem("GenLiquidResource");
		else
			I=CMClass.getItem("GenResource");
		I.setName(name);
		I.setDisplayText(name+" has been left here.");
		I.setDescription("It looks like "+name());
		I.setMaterial(type);
		I.setBaseValue(RawMaterial.RESOURCE_DATA[type&RawMaterial.RESOURCE_MASK][1]);
		I.baseEnvStats().setWeight(1);
		I.recoverEnvStats();
		return I;
	}

	public DeadBody getCorpseContainer(MOB mob, Room room)
	{
		if(room==null) room=mob.location();

		DeadBody Body=(DeadBody)CMClass.getItem("Corpse");
		Body.setCharStats((CharStats)mob.baseCharStats().copyOf());
		Body.baseEnvStats().setLevel(mob.baseEnvStats().level());
		Body.baseEnvStats().setWeight(mob.baseEnvStats().weight());
		Body.setPlayerCorpse(!mob.isMonster());
        Body.setTimeOfDeath(System.currentTimeMillis());
		Body.setMobPKFlag(CMath.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL));
		Body.setName("the body of "+mob.Name());
		Body.setMobName(mob.Name());
		Body.setMobDescription(mob.description());
		Body.setDisplayText("the body of "+mob.Name()+" lies here.");
		if(room!=null)
			room.addItemRefuse(Body,mob.isMonster()?Item.REFUSE_MONSTER_BODY:Item.REFUSE_PLAYER_BODY);
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
        Hashtable containerMap=new Hashtable();
        Hashtable itemMap=new Hashtable();
		for(int i=0;i<mob.inventorySize();)
		{
			Item thisItem=mob.fetchInventory(i);
			if((thisItem!=null)&&(thisItem.savable()))
			{
				if(mob.isMonster())
				{
					Item newItem=(Item)thisItem.copyOf();
                    itemMap.put(thisItem,newItem);
                    if(thisItem.container()!=null)
                        containerMap.put(thisItem,thisItem.container());
					newItem.setContainer(null);
					newItem.setExpirationDate(System.currentTimeMillis()+Math.round(Item.REFUSE_MONSTER_EQ*TimeManager.MILI_HOUR));
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
						room.addItemRefuse(I,Item.REFUSE_MONSTER_EQ);
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
			level=new Integer(mob.envStats().level());
		else
			level=new Integer(Integer.MAX_VALUE);

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
					A.setProfficiency(100);
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

	public Vector racialAbilities(MOB mob)
	{
		if((racialAbilityMap==null)
		&&(racialAbilityNames()!=null)
		&&(racialAbilityLevels()!=null)
		&&(racialAbilityProfficiencies()!=null)
		&&(racialAbilityQuals()!=null))
		{
			racialAbilityMap=new Hashtable();
			for(int i=0;i<racialAbilityNames().length;i++)
			{
				CMLib.ableMapper().addCharAbilityMapping(ID(),
											 racialAbilityLevels()[i],
											 racialAbilityNames()[i],
											 racialAbilityProfficiencies()[i],
											 "",
											 !racialAbilityQuals()[i],
											 false);
			}
		}
		if(racialAbilityMap==null) return empty;
		Integer level=null;
		if(mob!=null)
			level=new Integer(mob.envStats().level());
		else
			level=new Integer(Integer.MAX_VALUE);
		if(racialAbilityMap.containsKey(level))
			return (Vector)racialAbilityMap.get(level);
		Vector V=CMLib.ableMapper().getUpToLevelListings(ID(),level.intValue(),true,(mob!=null));
		Vector finalV=new Vector();
		for(int v=0;v<V.size();v++)
		{
			Ability A=CMClass.getAbility((String)V.elementAt(v));
			if(A!=null)
			{
				A.setProfficiency(CMLib.ableMapper().getDefaultProfficiency(ID(),false,A.ID()));
				A.setSavable(false);
				A.setMiscText(CMLib.ableMapper().getDefaultParm(ID(),false,A.ID()));
				finalV.addElement(A);
			}
		}
		racialAbilityMap.put(level,finalV);
		return finalV;
	}

	public String racialParms(){ return "";}
	public void setRacialParms(String parms){}
	protected static String[] CODES={"CLASS","PARMS"};
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
