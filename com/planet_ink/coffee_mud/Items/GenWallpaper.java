package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class GenWallpaper implements Item
{
	public String ID(){	return "GenWallpaper";}
	protected String 	name="some wallpaper";
	protected String 	description="Looks like it needs a new description";
	protected String	readableText="";
	protected EnvStats envStats=new DefaultEnvStats();
	protected boolean destroyed=false;
	protected boolean	isReadable=false;
	protected Environmental owner=null;

	public boolean isGeneric(){return true;}
	public Rideable riding(){return null;}
	public void setRiding(Rideable one){};

	public String name(){ return name;}
	public void setName(String newName){name=newName;}
	public EnvStats envStats()
	{return envStats;}
	public EnvStats baseEnvStats()
	{ return envStats; }
	public void recoverEnvStats(){}
	public void setBaseEnvStats(EnvStats newBaseEnvStats){}
	public boolean isAContainer(){return false;}
	public Environmental newInstance()
	{return new GenWallpaper();}
	public boolean subjectToWearAndTear(){return false;}
	public Environmental copyOf()
	{
		try
		{
			GenWallpaper E=(GenWallpaper)this.clone();
			E.destroyed=false;
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	public Environmental owner(){return owner;}
	public void setOwner(Environmental E)
	{ owner=E;}
	public long dispossessionTime(){return 0;}
	public void setDispossessionTime(long time){}

	public boolean amDestroyed()
	{return destroyed;}

	public boolean amWearingAt(long wornCode){if(wornCode==Item.INVENTORY)return true; return false;}
	public boolean canBeWornAt(long wornCode){return false;}
	public void wearIfPossible(MOB mob){}
	public void wearAt(long wornCode){}
	public long rawProperLocationBitmap(){return 0;}
	public boolean rawLogicalAnd(){return false;}
	public void setRawProperLocationBitmap(long newValue){}
	public void setRawLogicalAnd(boolean newAnd){}
	public boolean compareProperLocations(Item toThis){return true;}
	public long whereCantWear(MOB mob){ return 0;}
	public boolean canWear(MOB mob){ return false;}
	public long rawWornCode(){return 0;}
	public void setRawWornCode(long newValue){}
	public void remove(){}
	public int capacity(){return 0;}
	public void setCapacity(int newValue){}
	public int material(){return EnvResource.RESOURCE_PAPER;}
	public void setMaterial(int newValue){}
	public int baseGoldValue(){return 0;}
	public int value(){return 0;}
	public void setBaseValue(int newValue){}
	public String readableText(){return readableText;}
	public void setReadableText(String text){readableText=text;}
	public boolean isReadable(){return isReadable;}
	public void setReadable(boolean isTrue){isReadable=isTrue;}
	public boolean isGettable(){return false;}
	public void setGettable(boolean isTrue){}
	public boolean isDroppable(){return true;}
	public void setDroppable(boolean isTrue){}
	public boolean isRemovable(){return true;}
	public void setRemovable(boolean isTrue){}
	public boolean isTrapped(){return false;}
	public void setTrapped(boolean isTrue){}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats){}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats){}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState){}
	public void setMiscText(String newText)
	{
		Vector V=XMLManager.parseAllXML(newText);
		if(V!=null)
		{
			setName(XMLManager.getValFromPieces(V,"NAME"));
			setDescription(XMLManager.getValFromPieces(V,"DESC"));
			Generic.setEnvFlags(this,Util.s_int(XMLManager.getValFromPieces(V,"FLAG")));
			setReadableText(XMLManager.getValFromPieces(V,"READ"));
		}
	}
	public String text()
	{	StringBuffer text=new StringBuffer("");
		text.append(XMLManager.convertXMLtoTag("NAME",name()));
		text.append(XMLManager.convertXMLtoTag("DESC",description()));
		text.append(XMLManager.convertXMLtoTag("FLAG",Generic.envFlags(this)));
		text.append(XMLManager.convertXMLtoTag("READ",readableText()));
		return text.toString();
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(destroyed) return false;
		return true;
	}
	public Item container(){return null;}
	public Item ultimateContainer(){return this;}
	public String rawSecretIdentity(){return "";}
	public String secretIdentity(){return "";}
	public void setSecretIdentity(String newIdentity){}
	public String displayText(){return "";}
	public void setDisplayText(String newDisplayText){}
	public String description()
	{ return description; }
	public void setDescription(String newDescription)
	{ description=newDescription; }
	public void setContainer(Item newContainer){}
	public int usesRemaining(){return Integer.MAX_VALUE;}
	public void setUsesRemaining(int newUses){}
	public boolean savable(){return true;}
	public boolean okAffect(Affect affect)
	{
		MOB mob=affect.source();
		if(!affect.amITarget(this))
			return true;
		else
		if(affect.targetCode()==Affect.NO_EFFECT)
			return true;
		else
		if(Util.bset(affect.targetCode(),Affect.MASK_MAGIC))
		{
			mob.tell("Please don't do that.");
			return false;
		}
		else
		switch(affect.targetMinor())
		{
		case Affect.TYP_EXAMINESOMETHING:
		case Affect.TYP_READSOMETHING:
		case Affect.TYP_SPEAK:
		case Affect.TYP_OK_ACTION:
		case Affect.TYP_OK_VISUAL:
		case Affect.TYP_NOISE:
			return true;
		case Affect.TYP_GET:
			if((affect.tool()==null)||(affect.tool() instanceof MOB))
			{
				mob.tell("You can't get "+name()+".");
				return false;
			}
			break;
		case Affect.TYP_DROP:
		case Affect.TYP_THROW:
			return true;
		default:
			break;
		}
		mob.tell("You can't do that to "+name()+".");
		return false;
	}

	public void affect(Affect affect)
	{
		MOB mob=affect.source();
		if(!affect.amITarget(this))
			return;
		else
		switch(affect.targetMinor())
		{
		case Affect.TYP_EXAMINESOMETHING:
			if(Sense.canBeSeenBy(this,mob))
			{
				if(description().length()==0)
					mob.tell("You don't see anything special about "+this.name());
				else
					mob.tell(description());
			}
			else
				mob.tell("You can't see that!");
			return;
		case Affect.TYP_READSOMETHING:
			if(Sense.canBeSeenBy(this,mob))
			{
				if((isReadable)&&(readableText()!=null)&&(readableText().length()>0))
					mob.tell("It says '"+readableText()+"'.");
				else
					mob.tell("There is nothing written on "+name()+".");
			}
			else
				mob.tell("You can't see that!");
			return;
		default:
			break;
		}
	}

	public void destroyThis()
	{
		if(owner==null) return;
		destroyed=true;
		removeThis();
	}

	public void removeThis()
	{
		if(owner==null) return;
		if (owner instanceof Room)
		{
			Room thisRoom=(Room)owner;
			thisRoom.delItem(this);
		}
		else
		if (owner instanceof MOB)
		{
			MOB mob=(MOB)owner;
			mob.delInventory(this);
		}
	}

	public void addNonUninvokableAffect(Ability to){}
	public void addAffect(Ability to){}
	public void delAffect(Ability to){}
	public int numAffects(){return 0;}
	public Ability fetchAffect(int index){return null;}
	public Ability fetchAffect(String ID){return null;}
	public void addBehavior(Behavior to){}
	public void delBehavior(Behavior to){}
	public int numBehaviors(){return 0;}
	public Behavior fetchBehavior(int index){return null;}
	public int maxRange(){return 0;}
	public int minRange(){return 0;}
	
	private static final String[] CODES={"CLASS","NAME","DESCRIPTION","ISREADABLE","READABLETEXT"};
	public String[] getStatCodes(){return CODES;}
	private int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return name();
		case 2: return description();
		case 3: return ""+isReadable();
		case 4: return readableText();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setName(val); break;
		case 2: setDescription(val); break;
		case 3: setReadable(Util.s_bool(val)); break;
		case 4: setReadableText(val); break;
		}
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenWallpaper)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
}
