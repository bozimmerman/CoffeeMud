package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

// this ability is the very picture of the infectuous affect.
// It lobs itself onto other qualified objects, and withdraws
// again when it will.  Don't lothe the HaveAdjuster, LOVE IT.
public class Prop_HaveAdjuster extends Property
{
	private Item myItem=null;
	private MOB lastMOB=null;
	private CharStats adjCharStats=null;
	private CharState adjCharState=null;
	boolean gotClass=false;
	boolean gotRace=false;
	boolean gotSex=false;

	public Prop_HaveAdjuster()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Adjustments to stats when owned";
	}

	public Environmental newInstance()
	{
		Prop_HaveAdjuster BOB=new Prop_HaveAdjuster();
		BOB.setMiscText(text());
		return BOB;
	}

	public boolean isBorrowed(Environmental toMe)
	{
		if(toMe instanceof MOB)
			return true;
		return borrowed;
	}

	public static int getVal(String text, String key)
	{
		text=text.toUpperCase();
		key=key.toUpperCase();
		int x=text.indexOf(key);
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='+')&&(text.charAt(x)!='-'))
					x++;
				if(x<text.length())
				{
					char pm=text.charAt(x);
					while((x<text.length())&&(!Character.isDigit(text.charAt(x))))
						x++;
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())&&(Character.isDigit(text.charAt(x))))
							x++;
						if(pm=='+')
							return Util.s_int(text.substring(0,x));
						else
							return -Util.s_int(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return 0;
	}

	public static String getStr(String text, String key)
	{
		String oldText=text;
		text=text.toUpperCase();
		key=key.toUpperCase();
		int x=text.indexOf(key);
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='='))
					x++;
				if(x<text.length())
				{
					while((x<text.length())&&(!Character.isLetter(text.charAt(x))))
						x++;
					if(x<text.length())
					{
						oldText=oldText.substring(x);
						text=text.substring(x);
						x=0;
						while((x<text.length())&&(Character.isLetter(text.charAt(x))))
							x++;
						return oldText.substring(0,x).trim();
					}

				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return "";
	}

	public static int setAdjustments(String newText, EnvStats baseEnvStats, CharStats adjCharStats, CharState adjCharState)
	{
		boolean gotClass=false;
		boolean gotRace=false;
		boolean gotSex=false;

		baseEnvStats.setAbility(getVal(newText,"abi"));
		baseEnvStats.setArmor(getVal(newText,"arm"));
		baseEnvStats.setAttackAdjustment(getVal(newText,"att"));
		baseEnvStats.setDamage(getVal(newText,"dam"));
		baseEnvStats.setDisposition(getVal(newText,"dis"));
		baseEnvStats.setLevel(getVal(newText,"lev"));
		baseEnvStats.setRejuv(getVal(newText,"rej"));
		baseEnvStats.setSensesMask(getVal(newText,"sen"));
		baseEnvStats.setSpeed(getVal(newText,"spe"));
		baseEnvStats.setWeight(getVal(newText,"wei"));

		adjCharStats.setStat(CharStats.CHARISMA,getVal(newText,"cha"));
		adjCharStats.setStat(CharStats.CONSTITUTION,getVal(newText,"con"));
		adjCharStats.setStat(CharStats.DEXTERITY,getVal(newText,"dex"));
		String val=getStr(newText,"gen").toUpperCase();
		if((val.length()>0)&&((val.charAt(0)=='M')||(val.charAt(0)=='F')||(val.charAt(0)=='N')))
		{
			adjCharStats.setStat(CharStats.GENDER,(int)val.charAt(0));
			gotSex=true;
		}

		adjCharStats.setStat(CharStats.INTELLIGENCE,getVal(newText,"int"));
		val=getStr(newText,"cla").toUpperCase();
		if((val.length()>0)&&(CMClass.getCharClass(val)!=null))
		{
			gotClass=true;
			adjCharStats.setMyClass(CMClass.getCharClass(val));
		}
		val=getStr(newText,"rac").toUpperCase();
		if((val.length()>0)&&(CMClass.getRace(val)!=null))
		{
			gotRace=true;
			adjCharStats.setMyRace(CMClass.getRace(val));
		}
		adjCharStats.setStat(CharStats.STRENGTH,getVal(newText,"str"));
		adjCharStats.setStat(CharStats.WISDOM,getVal(newText,"wis"));

		adjCharState.setHitPoints(getVal(newText,"hit"));
		adjCharState.setHunger(getVal(newText,"hun"));
		adjCharState.setMana(getVal(newText,"man"));
		adjCharState.setMovement(getVal(newText,"mov"));
		adjCharState.setThirst(getVal(newText,"thi"));
		return ((gotClass?1:0)+(gotRace?2:0)+(gotSex?4:0));
	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		this.adjCharStats=new DefaultCharStats();
		this.adjCharState=new DefaultCharState();
		int gotit=setAdjustments(newText,baseEnvStats(),adjCharStats,adjCharState);
		gotClass=((gotit&1)==1);
		gotRace=((gotit&2)==2);
		gotSex=((gotit&4)==4);
	}

	public static void envStuff(EnvStats affectableStats, EnvStats baseEnvStats)
	{
		affectableStats.setAbility(affectableStats.ability()+baseEnvStats.ability());
		affectableStats.setArmor(affectableStats.armor()+baseEnvStats.armor());
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+baseEnvStats.attackAdjustment());
		affectableStats.setDamage(affectableStats.damage()+baseEnvStats.damage());
		affectableStats.setDisposition(affectableStats.disposition()|baseEnvStats.disposition());
		affectableStats.setLevel(affectableStats.level()+baseEnvStats.level());
		affectableStats.setRejuv(affectableStats.rejuv()+baseEnvStats.rejuv());
		affectableStats.setSensesMask(affectableStats.sensesMask()|baseEnvStats.sensesMask());
		affectableStats.setSpeed(affectableStats.speed()+baseEnvStats.speed());
		affectableStats.setWeight(affectableStats.weight()+baseEnvStats.weight());
	}

	public static void addMe(MOB lastMOB, CharState adjCharState, Ability me)
	{
		lastMOB.addNonUninvokableAffect(me);
		lastMOB.recoverMaxState();
		lastMOB.recoverEnvStats();
		lastMOB.recoverCharStats();
		lastMOB.curState().adjHitPoints(adjCharState.getHitPoints(),lastMOB.maxState());
		lastMOB.curState().adjHunger(adjCharState.getHunger(),lastMOB.maxState());
		lastMOB.curState().adjMana(adjCharState.getMana(),lastMOB.maxState());
		lastMOB.curState().adjMovement(adjCharState.getMovement(),lastMOB.maxState());
		lastMOB.curState().adjThirst(adjCharState.getThirst(),lastMOB.maxState());
	}

	public static void removeMyAffectFromLastMob(Ability me, MOB mylastMOB, CharState adjCharState)
	{
		int x=0;
		while(x<mylastMOB.numAffects())
		{
			Ability aff=mylastMOB.fetchAffect(x);
			if((aff!=null)&&(aff==me))
				mylastMOB.delAffect(aff);
			else
				x++;
		}
		mylastMOB.recoverEnvStats();
		mylastMOB.recoverMaxState();
		mylastMOB.recoverCharStats();

		mylastMOB.curState().adjHitPoints(-adjCharState.getHitPoints(),mylastMOB.maxState());
		mylastMOB.curState().adjHunger(-adjCharState.getHunger(),mylastMOB.maxState());
		mylastMOB.curState().adjMana(-adjCharState.getMana(),mylastMOB.maxState());
		mylastMOB.curState().adjMovement(-adjCharState.getMovement(),mylastMOB.maxState());
		mylastMOB.curState().adjThirst(-adjCharState.getThirst(),mylastMOB.maxState());
	}

	private void ensureStarted()
	{
		if(adjCharStats==null)
			setMiscText(text());
	}

	public void affectEnvStats(Environmental affectedMOB, EnvStats affectableStats)
	{
		ensureStarted();
		if(affectedMOB!=null)
		{
			if(affectedMOB instanceof Item)
			{
				myItem=(Item)affectedMOB;
				if(myItem.myOwner() instanceof MOB)
				{
					if((lastMOB!=null)&&(myItem.myOwner()!=lastMOB))
					{	Prop_HaveAdjuster.removeMyAffectFromLastMob(this,lastMOB,adjCharState); lastMOB=null;}
					if(myItem.myOwner() !=null)
					{
						lastMOB=(MOB)myItem.myOwner();
						if(!isAffectedBy(lastMOB,this))
							addMe(lastMOB,adjCharState,this);
					}
				}
			}
			else
			if(affectedMOB instanceof MOB)
			{
				if((myItem.myOwner() instanceof MOB)
				   &&(myItem.myOwner()==affectedMOB))
				{
					if((lastMOB!=null)&&(affectedMOB!=lastMOB))
					{	Prop_HaveAdjuster.removeMyAffectFromLastMob(this,lastMOB,adjCharState); lastMOB=null;}
					lastMOB=(MOB)affectedMOB;
					envStuff(affectableStats,baseEnvStats());
				}
				else
				if((affectedMOB!=null)&&(affectedMOB!=myItem.myOwner()))
				{
					Prop_HaveAdjuster.removeMyAffectFromLastMob(this,(MOB)affectedMOB,adjCharState);
				}
			}
		}
		super.affectEnvStats(affectedMOB,affectableStats);
	}

	public static void adjCharStats(CharStats affectedStats,
									boolean gotClass,
									boolean gotRace,
									boolean gotSex,
									CharStats adjCharStats)
	{
		affectedStats.setStat(CharStats.CHARISMA,affectedStats.getStat(CharStats.CHARISMA)+adjCharStats.getStat(CharStats.CHARISMA));
		affectedStats.setStat(CharStats.CONSTITUTION,affectedStats.getStat(CharStats.CONSTITUTION)+adjCharStats.getStat(CharStats.CONSTITUTION));
		affectedStats.setStat(CharStats.DEXTERITY,affectedStats.getStat(CharStats.DEXTERITY)+adjCharStats.getStat(CharStats.DEXTERITY));
		if(gotSex)
			affectedStats.setStat(CharStats.GENDER,(int)adjCharStats.getStat(CharStats.GENDER));
		affectedStats.setStat(CharStats.INTELLIGENCE,affectedStats.getStat(CharStats.INTELLIGENCE)+adjCharStats.getStat(CharStats.INTELLIGENCE));
		if(gotClass)
			affectedStats.setMyClass(adjCharStats.getMyClass());
		if(gotRace)
			affectedStats.setMyRace(adjCharStats.getMyRace());
		affectedStats.setStat(CharStats.STRENGTH,affectedStats.getStat(CharStats.STRENGTH)+adjCharStats.getStat(CharStats.STRENGTH));
		affectedStats.setStat(CharStats.WISDOM,affectedStats.getStat(CharStats.WISDOM)+adjCharStats.getStat(CharStats.WISDOM));
	}

	public static void adjCharState(CharState affectedState,
									CharState adjCharState)
	{
		affectedState.setHitPoints(affectedState.getHitPoints()+adjCharState.getHitPoints());
		affectedState.setHunger(affectedState.getHunger()+adjCharState.getHunger());
		affectedState.setMana(affectedState.getMana()+adjCharState.getMana());
		affectedState.setMovement(affectedState.getMovement()+adjCharState.getMovement());
		affectedState.setThirst(affectedState.getThirst()+adjCharState.getThirst());
	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		ensureStarted();
		if((affectedMOB!=null)
		   &&(lastMOB==affectedMOB))
			adjCharStats(affectedStats,gotClass,gotRace,gotSex,adjCharStats);
		super.affectCharStats(affectedMOB,affectedStats);
	}
	public void affectCharState(MOB affectedMOB, CharState affectedState)
	{
		ensureStarted();
		if((affectedMOB!=null)
		   &&(lastMOB==affectedMOB))
			adjCharState(affectedState,adjCharState);
		super.affectCharState(affectedMOB,affectedState);
	}

	public String accountForYourself()
	{
		String id="Affects the owner: "+text();
		int x=id.toUpperCase().indexOf("ARM");
		for(StringBuffer ID=new StringBuffer(id);((x>0)&&(x<id.length()));x++)
			if(id.charAt(x)=='-')
			{
				ID.setCharAt(x,'+');
				id=ID.toString();
				break;
			}
			else
			if(id.charAt(x)=='+')
			{
				ID.setCharAt(x,'-');
				id=ID.toString();
				break;
			}
			else
			if(Character.isDigit(id.charAt(x)))
				break;
			
		return id;
	}
}