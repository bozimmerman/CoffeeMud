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
	public String ID() { return "Prop_HaveAdjuster"; }
	public String name(){ return "Adjustments to stats when owned";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	public boolean bubbleAffect(){return true;}
	private CharStats adjCharStats=null;
	private CharState adjCharState=null;
	private EnvStats adjEnvStats=null;
	boolean gotClass=false;
	boolean gotRace=false;
	boolean gotSex=false;
	public Environmental newInstance(){	Prop_HaveAdjuster BOB=new Prop_HaveAdjuster();	BOB.setMiscText(text()); return BOB;}

	public static int setAdjustments(String newText, EnvStats adjEnvStats, CharStats adjCharStats, CharState adjCharState)
	{
		boolean gotClass=false;
		boolean gotRace=false;
		boolean gotSex=false;

		adjEnvStats.setAbility(getVal(newText,"abi"));
		adjEnvStats.setArmor(getVal(newText,"arm"));
		adjEnvStats.setAttackAdjustment(getVal(newText,"att"));
		adjEnvStats.setDamage(getVal(newText,"dam"));
		adjEnvStats.setDisposition(getVal(newText,"dis"));
		adjEnvStats.setLevel(getVal(newText,"lev"));
		adjEnvStats.setRejuv(getVal(newText,"rej"));
		adjEnvStats.setSensesMask(getVal(newText,"sen"));
		adjEnvStats.setSpeed(getVal(newText,"spe"));
		adjEnvStats.setWeight(getVal(newText,"wei"));
		adjEnvStats.setHeight(getVal(newText,"hei"));

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
			adjCharStats.setCurrentClass(CMClass.getCharClass(val));
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
		this.adjEnvStats=new DefaultEnvStats();
		int gotit=setAdjustments(newText,adjEnvStats,adjCharStats,adjCharState);
		gotClass=((gotit&1)==1);
		gotRace=((gotit&2)==2);
		gotSex=((gotit&4)==4);
	}

	public static void envStuff(EnvStats affectableStats, EnvStats adjEnvStats)
	{
		affectableStats.setAbility(affectableStats.ability()+adjEnvStats.ability());
		affectableStats.setArmor(affectableStats.armor()+adjEnvStats.armor());
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+adjEnvStats.attackAdjustment());
		affectableStats.setDamage(affectableStats.damage()+adjEnvStats.damage());
		affectableStats.setDisposition(affectableStats.disposition()|adjEnvStats.disposition());
		affectableStats.setLevel(affectableStats.level()+adjEnvStats.level());
		affectableStats.setRejuv(affectableStats.rejuv()+adjEnvStats.rejuv());
		affectableStats.setSensesMask(affectableStats.sensesMask()|adjEnvStats.sensesMask());
		affectableStats.setSpeed(affectableStats.speed()+adjEnvStats.speed());
		affectableStats.setWeight(affectableStats.weight()+adjEnvStats.weight());
		affectableStats.setHeight(affectableStats.height()+adjEnvStats.height());
	}

	private void ensureStarted()
	{
		if(adjCharStats==null)
			setMiscText(text());
	}

	public void affectEnvStats(Environmental affectedMOB, EnvStats affectableStats)
	{
		ensureStarted();
		if((affectedMOB!=null)
		&&(affectedMOB instanceof MOB)
		&&(affected!=null)
		&&(affected instanceof Item))
			envStuff(affectableStats,adjEnvStats);
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
			affectedStats.setCurrentClass(adjCharStats.getCurrentClass());
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
		adjCharStats(affectedStats,gotClass,gotRace,gotSex,adjCharStats);
		super.affectCharStats(affectedMOB,affectedStats);
	}
	public void affectCharState(MOB affectedMOB, CharState affectedState)
	{
		ensureStarted();
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