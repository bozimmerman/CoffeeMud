package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
	public int[] bodyMask(){return parts;}

	private static final Vector empty=new Vector();
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
	protected String[] culturalAbilityNames(){return null;}
	protected int[] culturalAbilityProfficiencies(){return null;}
	protected boolean uncharmable(){return false;}
	protected boolean destroyBodyAfterUse(){return false;}
	
	public int availability(){return Race.AVAILABLE_MAGICONLY;}

	public boolean fertile(){return true;}

	public Race copyOf()
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

	/** some general statistics about such an item
	 * see class "EnvStats" for more information. */
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{

	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{

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
		&&(Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_CHARMING)))
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
		if((msg.amITarget(myChar))
		&&(msg.tool()!=null)
		&&(msg.tool().ID().equals("Social"))
		&&(myChar.charStats().getStat(CharStats.GENDER)==('F'))
		&&(msg.source().charStats().getStat(CharStats.GENDER)==('M'))
		&&(msg.tool().Name().equals("MATE <T-NAME>")
			||msg.tool().Name().equals("SEX <T-NAME>"))
		&&(myChar.location()==msg.source().location()))
	   {
			msg.source().curState().adjFatigue(CharState.FATIGUED_MILLIS,msg.source().maxState());
			myChar.curState().adjFatigue(CharState.FATIGUED_MILLIS,myChar.maxState());
			if((Dice.rollPercentage()<10)
			&&(fertile())
			&&(myChar.numWearingHere(Item.ON_LEGS)==0)
			&&(msg.source().numWearingHere(Item.ON_LEGS)==0)
			&&(myChar.numWearingHere(Item.ON_WAIST)==0)
			&&(msg.source().numWearingHere(Item.ON_WAIST)==0)
			&&((ID().equals("Human"))
			   ||(msg.source().charStats().getMyRace().ID().equals("Human"))
			   ||(msg.source().charStats().getMyRace().ID().equals(ID())))
			&&(msg.source().charStats().getMyRace().fertile()))
			{
				Ability A=CMClass.getAbility("Pregnancy");
				if((A!=null)
				&&(myChar.fetchAbility(A.ID())==null)
				&&(myChar.fetchEffect(A.ID())==null))
					A.invoke(msg.source(),myChar,true,0);
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
	public void level(MOB mob)
	{
	}
	public long getTickStatus(){return Tickable.STATUS_NOT;}
	public boolean tick(Tickable myChar, int tickID){return true;}
	public void startRacing(MOB mob, boolean verifyOnly)
	{
		if(!verifyOnly)
		{
			if(mob.baseEnvStats().level()<=1)
			{
				mob.setPractices(mob.getPractices()+practicesAtFirstLevel());
				mob.setTrains(mob.getTrains()+trainsAtFirstLevel());
			}
			setHeightWeight(mob.baseEnvStats(),(char)mob.baseCharStats().getStat(CharStats.GENDER));

			if((culturalAbilityNames()!=null)&&(culturalAbilityProfficiencies()!=null)
			   &&(culturalAbilityNames().length==culturalAbilityProfficiencies().length))
			for(int a=0;a<culturalAbilityNames().length;a++)
			{
				Ability A=CMClass.getAbility(culturalAbilityNames()[a]);
				if(A!=null)
				{
					A.setProfficiency(culturalAbilityProfficiencies()[a]);
					mob.addAbility(A);
					A.autoInvocation(mob);
					if((mob.isMonster())&&((A.classificationCode()&Ability.ALL_CODES)==Ability.LANGUAGE))
						A.invoke(mob,mob,false,0);
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

	public Vector outfit(){return outfitChoices;}
	
	public String healthText(MOB mob)
	{
		return CommonStrings.standardMobCondition(mob);
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
			return (Weapon)naturalWeaponChoices.elementAt(Dice.roll(1,naturalWeaponChoices.size(),-1));
		else
			return CMClass.getWeapon("Natural");
	}
	public Vector myResources(){return new Vector();}
	public void setHeightWeight(EnvStats stats, char gender)
	{
		int weightModifier=0;
		if(weightVariance()>0)
			weightModifier=Dice.roll(1,weightVariance(),0);
		stats.setWeight(lightestWeight()+weightModifier);
		int heightModifier=0;
		if(heightVariance()>0)
		{
			if(weightModifier>0)
			{
				double variance=Util.div(weightModifier,weightVariance());
				heightModifier=(int)Math.round(Util.mul(heightVariance(),variance));
			}
			else
				heightModifier=Dice.roll(1,heightVariance(),0);
		}
		if (gender == 'M')
			stats.setHeight(shortestMale()+heightModifier);
 		else
			stats.setHeight(shortestFemale()+heightModifier);
	}

	public int getMaxWeight()
	{
		return lightestWeight()+weightVariance();
	}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	protected Item makeResource(String name, int type)
	{
		Item I=null;
		if(((type&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_FLESH)
		||((type&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_VEGETATION))
			I=CMClass.getItem("GenFoodResource");
		else
		if((type&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LIQUID)
			I=CMClass.getItem("GenLiquidResource");
		else
			I=CMClass.getItem("GenResource");
		I.setName(name);
		I.setDisplayText(name+" has been left here.");
		I.setDescription("It looks like "+name());
		I.setMaterial(type);
		I.setBaseValue(EnvResource.RESOURCE_DATA[type&EnvResource.RESOURCE_MASK][1]);
		I.baseEnvStats().setWeight(1);
		I.recoverEnvStats();
		return I;
	}

	public void reRoll(MOB mob, CharStats C)
	{
		int avg=0;
		int max=CommonStrings.getIntVar(CommonStrings.SYSTEMI_MAXSTAT);
		int baseMax=CommonStrings.getIntVar(CommonStrings.SYSTEMI_BASEMAXSTAT);
		if(max<(3*6)) max=3*6;
		if(max>(baseMax*6)) max=baseMax*6;
		int tries=0;
		double baseMaxDouble=new Integer(baseMax).doubleValue()-2.0;
		while((avg!=max)&&((++tries)<100000000))
		{
			C.setStat(CharStats.STRENGTH,3+(int)Math.floor(Math.random()*baseMaxDouble));
			C.setStat(CharStats.INTELLIGENCE,3+(int)Math.floor(Math.random()*baseMaxDouble));
			C.setStat(CharStats.DEXTERITY,3+(int)Math.floor(Math.random()*baseMaxDouble));
			C.setStat(CharStats.WISDOM,3+(int)Math.floor(Math.random()*baseMaxDouble));
			C.setStat(CharStats.CONSTITUTION,3+(int)Math.floor(Math.random()*baseMaxDouble));
			C.setStat(CharStats.CHARISMA,3+(int)Math.floor(Math.random()*baseMaxDouble));
			avg=(C.getStat(CharStats.STRENGTH)
				 +C.getStat(CharStats.INTELLIGENCE)
				 +C.getStat(CharStats.DEXTERITY)
				 +C.getStat(CharStats.WISDOM)
				 +C.getStat(CharStats.CONSTITUTION)
				 +C.getStat(CharStats.CHARISMA));
		}
	}

	public DeadBody getCorpseContainer(MOB mob, Room room)
	{
		if(room==null) room=mob.location();

		DeadBody Body=(DeadBody)CMClass.getItem("Corpse");
		Body.setCharStats(mob.baseCharStats().cloneCharStats());
		Body.baseEnvStats().setLevel(mob.baseEnvStats().level());
		Body.baseEnvStats().setWeight(mob.baseEnvStats().weight());
		Body.setPlayerCorpse(!mob.isMonster());
		if(!mob.isMonster())
			Body.baseEnvStats().setRejuv(Body.baseEnvStats().rejuv()*10);
		Body.setMobPKFlag(Util.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL));
		Body.setName("the body of "+mob.Name());
		Body.setMobName(mob.Name());
		Body.setMobDescription(mob.description());
		Body.setDisplayText("the body of "+mob.Name()+" lies here.");
		if(room!=null)
			room.addItem(Body);
		Body.setDestroyAfterLooting(destroyBodyAfterUse());
		Body.recoverEnvStats();
		for(int i=0;i<mob.numAllEffects();i++)
		{
			Ability A=mob.fetchEffect(i);
			if((A!=null)&&(A instanceof DiseaseAffect))
			{
				if((Util.bset(((DiseaseAffect)A).abilityCode(),DiseaseAffect.SPREAD_CONSUMPTION))
				||(Util.bset(((DiseaseAffect)A).abilityCode(),DiseaseAffect.SPREAD_CONTACT)))
					Body.addNonUninvokableEffect((Ability)A.copyOf());
			}
		}

		Vector items=new Vector();
		for(int i=0;i<mob.inventorySize();)
		{
			Item thisItem=mob.fetchInventory(i);
			if((thisItem!=null)&&(thisItem.savable()))
			{
				if(mob.isMonster())
				{
					Item newItem=(Item)thisItem.copyOf();
					newItem.setContainer(null);
					newItem.setDispossessionTime(System.currentTimeMillis()+(Item.REFUSE_MONSTER_EQ*IQCalendar.MILI_HOUR));
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
		if(mob.getMoney()>0)
		{
			Item C=CMClass.getItem("StdCoins");
			C.baseEnvStats().setAbility(mob.getMoney());
			C.recoverEnvStats();
			C.setContainer(Body);
			if(room!=null)
				room.addItemRefuse(C,Item.REFUSE_MONSTER_EQ);
			mob.setMoney(0);
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
					A.setBorrowed(mob,true);
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
				CMAble.addCharAbilityMapping(ID(),
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
		Vector V=CMAble.getUpToLevelListings(ID(),level.intValue(),true,(mob!=null));
		Vector finalV=new Vector();
		for(int v=0;v<V.size();v++)
		{
			Ability A=CMClass.getAbility((String)V.elementAt(v));
			if(A!=null)
			{
				A.setProfficiency(CMAble.getDefaultProfficiency(ID(),false,A.ID()));
				A.setBorrowed(mob,true);
				A.setMiscText(CMAble.getDefaultParm(ID(),false,A.ID()));
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
