package com.planet_ink.coffee_mud.MOBS;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class StdMOB implements MOB
{
	protected String Username="";
	protected String Password="";
	protected Calendar LastDateTime=Calendar.getInstance();
	protected int channelMask;

	protected int termID = 0;	//0:plain, 1:ansi
	
	protected CharStats baseCharStats=new DefaultCharStats();
	protected CharStats charStats=new DefaultCharStats();

	protected EnvStats envStats=new DefaultEnvStats();
	protected EnvStats baseEnvStats=new DefaultEnvStats();

	protected boolean amDead=false;
	protected Room location=null;
	protected Room lastLocation=null;
	protected Rideable riding=null;

	protected Session mySession=null;
	protected boolean pleaseDestroy=false;
	protected String description="";
	protected String displayText="";
	protected String miscText="";

	/* instantiated item types word, contained, owned*/
	protected Vector inventory=new Vector();

	/* instantiated creature types listed as followers*/
	protected Vector followers=new Vector();

	/* All Ability codes, including languages*/
	protected Vector abilities=new Vector();

	/* instantiated affects on this user*/
	protected Vector affects=new Vector();

	protected Vector behaviors=new Vector();

	// gained attributes
	protected int Experience=0;
	protected int ExpNextLevel=1000;
	protected int Practices=0;
	protected int Trains=0;
	protected long AgeHours=0;
	protected int Money=0;
	protected int attributesBitmap=0;
	public long getAgeHours(){return AgeHours;}
	public int getPractices(){return Practices;}
	public int getExperience(){return Experience;}
	public int getExpNextLevel(){return ExpNextLevel;}
	public int getExpNeededLevel()
	{
		if(ExpNextLevel<=getExperience())
			ExpNextLevel=getExperience()+1000;
		return ExpNextLevel-getExperience();
	}
	public int getTrains(){return Trains;}
	public int getMoney(){return Money;}
	public int getBitmap(){return attributesBitmap;}
	public void setAgeHours(long newVal){ AgeHours=newVal;}
	public void setExperience(int newVal){ Experience=newVal; }
	public void setExpNextLevel(int newVal){ ExpNextLevel=newVal;}
	public void setPractices(int newVal){ Practices=newVal;}
	public void setTrains(int newVal){ Trains=newVal;}
	public void setMoney(int newVal){ Money=newVal;}
	public void setBitmap(int newVal)
	{ 
		attributesBitmap=newVal;
		if(mySession!=null)
			mySession.setTermID(((attributesBitmap&MOB.ATT_ANSI)>0)?1:0);
	}

	protected int minuteCounter=0;

	// the core state values
	public CharState curState=new DefaultCharState();
	public CharState maxState=new DefaultCharState();
	public CharState baseState=new DefaultCharState();
	private Calendar lastTickedDateTime=Calendar.getInstance();
	public Calendar lastTickedDateTime(){return lastTickedDateTime;}

	// mental characteristics
	protected int Alignment=0;
	protected String WorshipCharID="";
	protected String LeigeID="";
	protected int WimpHitPoint=0;
	protected int QuestPoint=0;
	public String getLeigeID(){return LeigeID;}
	public String getWorshipCharID(){return WorshipCharID;}
	public int getAlignment(){return Alignment;}
	public int getWimpHitPoint(){return WimpHitPoint;}
	public int getQuestPoint(){return QuestPoint;}
	public void setLeigeID(String newVal){LeigeID=newVal;}
	public void setAlignment(int newVal)
	{
		if(newVal<0) newVal=0;
		if(newVal>1000) newVal=1000;
		Alignment=newVal;
	}
	public void setWorshipCharID(String newVal){ WorshipCharID=newVal;}
	public void setWimpHitPoint(int newVal){ WimpHitPoint=newVal;}
	public void setQuestPoint(int newVal){ QuestPoint=newVal;}

	// location!
	protected Room StartRoom=null;
	public Room getStartRoom(){return StartRoom;}
	public void setStartRoom(Room newVal){StartRoom=newVal;}


	protected MOB victim=null;
	protected MOB replyTo=null;
	protected MOB amFollowing=null;
	protected MOB soulMate=null;
	private double speeder=0.0;
	protected int atRange=-1;

	public String ID()
	{
		return Username;
	}
	public String name(){ return Username;}
	public void setName(String newName){Username=newName;}
	public String password()
	{
		return Password;
	}
	public Environmental newInstance()
	{
		return new StdMOB();
	}
	public StdMOB()
	{
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_INFRARED);
	}
	private void cloneFix(MOB E)
	{

		affects=new Vector();
		baseEnvStats=E.baseEnvStats().cloneStats();
		envStats=E.envStats().cloneStats();
		baseCharStats=E.baseCharStats().cloneCharStats();
		charStats=E.charStats().cloneCharStats();
		baseState=E.baseState().cloneCharState();
		curState=E.curState().cloneCharState();
		maxState=E.maxState().cloneCharState();

		pleaseDestroy=false;

		inventory=new Vector();
		followers=new Vector();
		abilities=new Vector();
		affects=new Vector();
		behaviors=new Vector();
		for(int i=0;i<E.inventorySize();i++)
		{
			Item I2=E.fetchInventory(i);
			if(I2!=null)
			{
				Item I=(Item)I2.copyOf();
				I.setOwner(this);
				inventory.addElement(I);
			}
		}
		for(int i=0;i<E.numAbilities();i++)
		{
			Ability A2=E.fetchAbility(i);
			if(A2!=null)
				abilities.addElement(A2.copyOf());
		}
		for(int i=0;i<E.numAffects();i++)
		{
			Ability A=(Ability)E.fetchAffect(i);
			if((A!=null)&&(!A.canBeUninvoked()))
				addAffect((Ability)A.copyOf());
		}
		for(int i=0;i<E.numBehaviors();i++)
		{
			Behavior B=E.fetchBehavior(i);
			if(B!=null)
				behaviors.addElement(B);
		}

	}
	public Environmental copyOf()
	{
		try
		{
			StdMOB E=(StdMOB)this.clone();
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}
	public boolean isGeneric(){return false;}
	public EnvStats envStats()
	{
		return envStats;
	}

	public EnvStats baseEnvStats()
	{
		return baseEnvStats;
	}
	public void recoverEnvStats()
	{
		envStats=baseEnvStats.cloneStats();
		if(location()!=null)
			location().affectEnvStats(this,envStats);
		envStats().setWeight(envStats().weight()+(int)Math.round(Util.div(getMoney(),100.0)));
		if(riding()!=null) riding().affectEnvStats(this,envStats);
		if(charStats!=null)
		{
			if(charStats().getMyClass()!=null)
				charStats().getMyClass().affectEnvStats(this,envStats);
			if(charStats().getMyRace()!=null)
				charStats().getMyRace().affectEnvStats(this,envStats);
		}
		for(int i=0;i<inventorySize();i++)
		{
			Item item=fetchInventory(i);
			if(item!=null)
			{
				item.recoverEnvStats();
				item.affectEnvStats(this,envStats);
			}
		}
		for(int a=0;a<numAffects();a++)
		{
			Ability affect=fetchAffect(a);
			if(affect!=null)
				affect.affectEnvStats(this,envStats);
		}
	}
	public void setBaseEnvStats(EnvStats newBaseEnvStats)
	{
		baseEnvStats=newBaseEnvStats.cloneStats();
	}

	public Calendar lastDateTime(){return LastDateTime;}
	public void setUserInfo(String newUsername,
							String newPassword,
							Calendar newCalendar)
	{
		Username=newUsername;
		Password=newPassword;
		LastDateTime=newCalendar.getInstance();
	}

	public int maxCarry()
	{
		return (baseEnvStats().weight()+50+(charStats().getStat(CharStats.STRENGTH)*30));
	}

	public CharStats baseCharStats(){return baseCharStats;}
	public CharStats charStats(){return charStats;}
	public void recoverCharStats()
	{
		charStats=baseCharStats().cloneCharStats();
		if(riding()!=null) riding().affectCharStats(this,charStats);
		for(int a=0;a<numAffects();a++)
		{
			Ability affect=fetchAffect(a);
			if(affect!=null)
				affect.affectCharStats(this,charStats);
		}
		for(int i=0;i<inventorySize();i++)
		{
			Item item=fetchInventory(i);
			if(item!=null)
				item.affectCharStats(this,charStats);
		}
		if(location()!=null)
			location().affectCharStats(this,charStats);
		if(charStats.getMyClass()!=null)
			charStats.getMyClass().affectCharStats(this,charStats);
		if(charStats.getMyRace()!=null)
			charStats.getMyRace().affectCharStats(this,charStats);
	}
	public void setBaseCharStats(CharStats newBaseCharStats)
	{
		baseCharStats=newBaseCharStats.cloneCharStats();
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if((Sense.isLight(this))&&(affected instanceof Room))
		{
			if(Sense.isInDark(affected))
				affectableStats.setDisposition(affectableStats.disposition()-EnvStats.IS_DARK);
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_LIGHT);
		}
	}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}

	public CharState curState(){return curState;}
	public CharState maxState(){return maxState;}
	public CharState baseState(){return baseState;}
	public void setBaseState(CharState newState)
	{
		baseState=newState.cloneCharState();
		maxState=newState.cloneCharState();
	}
	public void resetToMaxState()
	{
		recoverMaxState();
		curState=maxState.cloneCharState();
	}
	public void recoverMaxState()
	{
		maxState=baseState.cloneCharState();
		if(charStats.getMyRace()!=null)	charStats.getMyRace().affectCharState(this,maxState);
		if(riding()!=null) riding().affectCharState(this,maxState);
		for(int a=0;a<numAffects();a++)
		{
			Ability affect=fetchAffect(a);
			if(affect!=null)
				affect.affectCharState(this,maxState);
		}
		for(int i=0;i<inventorySize();i++)
		{
			Item item=fetchInventory(i);
			if(item!=null)
				item.affectCharState(this,maxState);
		}
		if(location()!=null)
			location().affectCharState(this,maxState);
	}

	public void setChannelMask(int newMask)
	{
		channelMask=newMask;
	}
	public int getChannelMask()
	{
		return channelMask;
	}

	public boolean amDead()
	{
		return amDead||pleaseDestroy;
	}

	public void destroy()
	{
		pleaseDestroy=true;
		if(location!=null)
		{
			location().delInhabitant(this);
			location().show(this,null,Affect.MSG_OK_ACTION,"<S-NAME> vanish(es) in a puff of smoke.");
		}
		setFollowing(null);
		if((!isMonster())&&(numFollowers()>0))
			ExternalPlay.DBUpdateFollowers(this);

		while(numFollowers()>0)
		{
			MOB follower=fetchFollower(0);
			if(follower!=null)
			{
				if((follower.amFollowing()==this)&&(follower.isMonster()))
					follower.destroy();
				delFollower(follower);
			}
		}
		if(!isMonster())
			session().setKillFlag(true);
		LastDateTime=Calendar.getInstance();
	}

	public MOB replyTo()
	{	return replyTo;	}
	public void setReplyTo(MOB mob)
	{	replyTo=mob;	}
	
	public void bringToLife(Room newLocation)
	{
		amDead=false;
		recoverEnvStats();
		recoverCharStats();
		recoverMaxState();
		resetToMaxState();
		setMiscText(miscText);
		if(getStartRoom()==null)
			setStartRoom(CMMap.startRoom());
		if(getStartRoom()==null)
			setStartRoom((Room)CMMap.getRoom("START"));
		if(getStartRoom()==null)
			setStartRoom((Room)CMMap.getRoom(0));
		setLocation(newLocation);
		if(location()==null)
		{
			setLocation(getStartRoom());
			if(location()==null)
			{
				Log.errOut("StdMOB",Username+" cannot get a location.");
				return;
			}
		}
		if(!location().isInhabitant(this))
		{
			location().addInhabitant(this);
			location().showOthers(this,null,Affect.MSG_OK_ACTION,"<S-NAME> appears!");
		}
		pleaseDestroy=false;

		// will ensure no duplicate ticks, this obj, this id
		ExternalPlay.startTickDown(this,Host.MOB_TICK,1);
		for(int a=0;a<numAbilities();a++)
		{
			Ability A=fetchAbility(a);
			if(A!=null)
				A.autoInvocation(this);
		}
		location().recoverRoomStats();
		ExternalPlay.look(this,null,true);
	}

	public boolean isInCombat()
	{
		if(victim==null) return false;
		if((victim.location()==null)
		||(location()==null)
		||(victim.location()!=location())
		||(victim.amDead()))
		{
			setVictim(null);
			return false;
		}
		return true;
	}
	public boolean mayIFight(MOB mob)
	{
		if(mob==null) return false;
		if(location()==null) return false;
		if(mob.location()==null) return false;
		if(mob.amDead()) return false;
		if(amDead()) return false;
		if(mob.isMonster()) return true;
		if(isMonster()) return true;
		if(mob==this) return true;
		if((getBitmap()&MOB.ATT_PLAYERKILL)==0) return false;
		if((mob.getBitmap()&MOB.ATT_PLAYERKILL)==0) return false;
		return true;
	}
	public boolean mayPhysicallyAttack(MOB mob)
	{
		if((!mayIFight(mob))
		||(location()!=mob.location())
		||(!location().isInhabitant(this))
		||(!mob.location().isInhabitant(mob)))
		   return false;
		return true;
	}
	public int adjustedAttackBonus()
	{
		return	envStats().attackAdjustment()
				+((charStats().getStat(CharStats.STRENGTH)-9)*3)
				-((curState().getHunger()<1)?10:0)
				-((curState().getThirst()<1)?10:0);
	}

	public int adjustedArmor()
	{
		return  envStats().armor()
				-((charStats().getStat(CharStats.DEXTERITY)-9)*3)
				+((curState().getHunger()<1)?10:0)
				+((curState().getThirst()<1)?10:0)
				+(((envStats().disposition()&EnvStats.IS_SITTING)>0)?15:0)
				+(((envStats().disposition()&EnvStats.IS_SLEEPING)>0)?30:0)
				-50;
	}
	public int adjustedDamage(Weapon weapon, MOB target)
	{
		double damageAmount=0.0;
		if((weapon!=null)&&((weapon.weaponClassification()==Weapon.CLASS_RANGED)||(weapon.weaponClassification()==Weapon.CLASS_THROWN)))
			damageAmount = new Integer(Dice.roll(1, weapon.envStats().damage(),1)).doubleValue();
		else
			damageAmount = new Integer(Dice.roll(1, envStats().damage(), (charStats().getStat(CharStats.STRENGTH) / 3)-2)).doubleValue();
        if(!Sense.canBeSeenBy(target,this)) damageAmount *=.5;
        if(Sense.isSleeping(target)) damageAmount *=1.5;
		else
        if(Sense.isSitting(target)) damageAmount *=1.2;
		if(curState().getHunger() < 1) damageAmount *= .8;
		if(curState().getThirst() < 1) damageAmount *= .9;
		if(damageAmount<1.0) damageAmount=1.0;
		return (int)Math.round(damageAmount);
	}

	public void setAtRange(int newRange){atRange=newRange;}
	public int rangeToTarget(){return atRange;}
	public int maxRange(){return maxRange(null);}
	public int minRange(){return maxRange(null);}
	public int maxRange(Environmental tool)
	{
		int max=0;
		if(tool!=null)
			max=tool.maxRange();
		if((location()!=null)&&(location().maxRange()<max))
			max=location().maxRange();
		return max;
	}
	public int minRange(Environmental tool)
	{
		if(tool!=null) return tool.minRange();
		return 0;
	}

	public void makePeace()
	{
		MOB myVictim=victim;
		setVictim(null);
		if(myVictim!=null)
		{
			MOB oldVictim=myVictim.getVictim();
			if(oldVictim==this)
				myVictim.makePeace();
		}
	}

	public MOB getVictim()
	{
		if(!isInCombat()) return null;
		return victim;
	}

	public void setVictim(MOB mob)
	{
		if(mob==null) setAtRange(-1);
		if(victim==mob) return;
		if(mob==this) return;
		victim=mob;
		recoverEnvStats();
		recoverCharStats();
		recoverMaxState();
		if(mob!=null)
		{
			if((mob.location()==null)
			||(location()==null)
			||(mob.location()!=location())
			||(!location().isInhabitant(this))
			||(!location().isInhabitant(mob)))
				victim=null;
			else
			{
				mob.recoverCharStats();
				mob.recoverEnvStats();
				mob.recoverMaxState();
			}
		}
	}
	public DeadBody killMeDead()
	{
		Room deathRoom=location();
		if(location()!=null) location().delInhabitant(this);
		DeadBody Body=charStats().getMyRace().getCorpse(this,deathRoom);
		amDead=true;
		victim=null;
		setRiding(null);
		for(int a=numAffects()-1;a>=0;a--)
		{
			Ability A=fetchAffect(a);
			if(A!=null) A.unInvoke();
		}
		setLocation(null);
		while(numFollowers()>0)
		{
			MOB follower=fetchFollower(0);
			if(follower!=null)
			{
				follower.setFollowing(null);
				delFollower(follower);
			}
		}
		setFollowing(null);
		if((!isMonster())&&(soulMate()==null))
			bringToLife(getStartRoom());
		Body.startTicker(deathRoom);
		deathRoom.recoverRoomStats();
		return Body;
	}

	public Room location()
	{
		if(location==null) return lastLocation;
		return location;
	}
	public void setLocation(Room newRoom)
	{
		lastLocation=location;
		location=newRoom;
	}
	public Rideable riding(){return riding;}
	public void setRiding(Rideable ride)
	{
		if((ride!=null)&&(riding()!=null)&&(riding()==ride)&&(riding().amRiding(this)))
			return;
		if((riding()!=null)&&(riding().amRiding(this)))
			riding().delRider(this);
		riding=ride;
		if((riding()!=null)&&(!riding().amRiding(this)))
			riding().addRider(this);
	}
	public Session session()
	{
		return mySession;
	}
	public void setSession(Session newSession)
	{
		mySession=newSession;
		setBitmap(getBitmap());
	}
	public Weapon myNaturalWeapon()
	{
		if((charStats()!=null)&&(charStats().getMyRace()!=null))
			return charStats().getMyRace().myNaturalWeapon();
		return (Weapon)CMClass.getWeapon("Natural").newInstance();
	}

	public String displayText()
	{
		if((displayText.length()==0)
		   ||(envStats().replacementName()!=null)
		   ||(Sense.isSleeping(this))
		   ||(Sense.isSitting(this))
		   ||(riding()!=null)
		   ||((this instanceof Rideable)&&(((Rideable)this).numRiders()>0))
		   ||(isInCombat()))
		{
			StringBuffer sendBack=null;
			if(envStats().replacementName()!=null) 
				sendBack=new StringBuffer(envStats().replacementName());
			else
				sendBack=new StringBuffer(name());
			sendBack.append(" ");
			sendBack.append(Sense.dispositionString(this,Sense.flag_is));
			sendBack.append(" here");
			if(riding()!=null)
			{
				sendBack.append(" "+riding().stateString()+" ");
				sendBack.append(riding().name());
			}
			else
			if((this instanceof Rideable)
			   &&(((Rideable)this).numRiders()>0)
			   &&(((Rideable)this).stateStringSubject().length()>0))
			{
				Rideable me=(Rideable)this;
				sendBack.append(" "+me.stateStringSubject()+" ");
				for(int r=0;r<me.numRiders();r++)
				{
					MOB rider=me.fetchRider(r);
					if(rider!=null)
						if(r>0)
						{
							sendBack.append(", ");
							if(r==me.numRiders()-1)
								sendBack.append("and ");
						}
						sendBack.append(rider.name());
					
				}
			}
			if((isInCombat())&&(Sense.canMove(this))&&(!Sense.isSleeping(this)))
			{
				sendBack.append(" fighting ");
				sendBack.append(getVictim().name());
			}
			sendBack.append(".");
			return sendBack.toString();
		}
		else
			return displayText;
	}

	public String rawDisplayText()
	{
		return displayText;
	}
	public void setDisplayText(String newDisplayText)
	{
		displayText=newDisplayText;
	}
	public String description()
	{
		return description;
	}
	public void setDescription(String newDescription)
	{
		description=newDescription;
	}
	public void setMiscText(String newText)
	{
		miscText=newText;
	}
	public String text()
	{
		return miscText;
	}

	public boolean isCorrectPass(String possiblePassword)
	{
		return Password.equals(possiblePassword);
	}
	public String healthText()
	{
		if((charStats()!=null)&&(charStats().getMyRace()!=null))
			return charStats().getMyRace().healthText(this);
		return CommonStrings.standardMobCondition(this);
	}

	public void establishRange(MOB source, MOB target, Environmental tool)
	{
		// establish and enforce range
		if((source.rangeToTarget()<0))
		{
			if(source.riding()!=null)
			{
				if((target==riding())||(source.riding().amRiding(target)))
					source.setAtRange(0);
				else
				if((source.riding() instanceof MOB)
				   &&(((MOB)source.riding()).isInCombat())
				   &&(((MOB)source.riding()).getVictim()==target)
				   &&(((MOB)source.riding()).rangeToTarget()>=0)
				   &&(((MOB)source.riding()).rangeToTarget()<rangeToTarget()))
				{
					source.setAtRange(((MOB)source.riding()).rangeToTarget());
					recoverEnvStats();
					return;
				}
				else
				for(int r=0;r<source.riding().numRiders();r++)
				{
					MOB otherMOB=source.riding().fetchRider(r);
					if((otherMOB!=null)
					   &&(otherMOB!=this)
					   &&(otherMOB.isInCombat())
					   &&(otherMOB.getVictim()==target)
					   &&(otherMOB.rangeToTarget()>=0)
					   &&(otherMOB.rangeToTarget()<rangeToTarget()))
					{
						source.setAtRange(otherMOB.rangeToTarget());
						source.recoverEnvStats();
						return;
					}
				}
			}
			
			if(target.getVictim()==source)
			{
				if(target.rangeToTarget()>=0)
					source.setAtRange(target.rangeToTarget());
				else
					source.setAtRange(maxRange(tool));
			}
			else
				source.setAtRange(maxRange(tool));
			recoverEnvStats();
		}
	}
	
	
	public boolean okAffect(Affect affect)
	{
		// sneaking exception
		if((Sense.isSneaking(affect.source()))
		 &&((affect.targetMinor()==Affect.TYP_LEAVE)||(affect.targetMinor()==Affect.TYP_ENTER))
		 &&(!Sense.canSeeSneakers(this))
		 &&(this!=affect.source()))
			return true;
		
		if(charStats!=null)
		{
			if(charStats().getMyClass()!=null)
				if(!charStats().getMyClass().okAffect(this,affect))
					return false;
			if(charStats().getMyRace()!=null)
				if(!charStats().getMyRace().okAffect(this, affect))
					return false;
		}

		for(int i=0;i<numAffects();i++)
		{
			Ability aff=(Ability)fetchAffect(i);
			if((aff!=null)&&(!aff.okAffect(affect)))
				return false;
		}

		for(int i=0;i<inventorySize();i++)
		{
			Item I=(Item)fetchInventory(i);
			if((I!=null)&&(!I.okAffect(affect)))
				return false;
		}

		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(!B.okAffect(this,affect)))
				return false;
		}
		
		MOB mob=affect.source();
		if((affect.sourceCode()!=Affect.NO_EFFECT)
		&&(affect.amISource(this))
		&&(!Util.bset(affect.sourceMajor(),Affect.ACT_GENERAL)))
		{
			int srcMajor=affect.sourceMajor();

			if(amDead())
			{
				tell("You are DEAD!");
				return false;
			}

			if(Util.bset(affect.sourceCode(),Affect.MASK_MALICIOUS))
			{
				if((affect.target()!=this)&&(affect.target()!=null)&&(affect.target() instanceof MOB))
				{
					MOB target=(MOB)affect.target();
					if((amFollowing()!=null)&&(target==amFollowing()))
					{
						tell("You like "+amFollowing().charStats().himher()+" too much.");
						return false;
					}
					if((getLeigeID().length()>0)&&(target.name().equals(getLeigeID())))
					{
						tell("You are serving '"+getLeigeID()+"'!");
						return false;
					}
					establishRange(this,(MOB)affect.target(),affect.tool());
				}
			}

			
			if(Util.bset(srcMajor,Affect.ACT_EYES))
			{
				if(Sense.isSleeping(this))
				{
					tell("Not while you are sleeping.");
					return false;
				}
				if(!(affect.target() instanceof Room))
					if(!Sense.canBeSeenBy(affect.target(),this))
					{
						tell("You can't see that!");
						return false;
					}
			}
			if(Util.bset(srcMajor,Affect.ACT_MOUTH))
			{
				if(Util.bset(srcMajor,Affect.ACT_SOUND))
				{
					if((affect.tool()==null)
					||(!(affect.tool() instanceof Ability))
					||(!((Ability)affect.tool()).isNowAnAutoEffect()))
					{
						if(Sense.isSleeping(this))
						{
							tell("Not while you are sleeping.");
							return false;
						}
						if(!Sense.canSpeak(this))
						{
							tell("You can't make sounds!");
							return false;
						}
						if(charStats().getStat(CharStats.INTELLIGENCE)<2)
						{
							tell("You aren't smart enough to speak.");
							return false;
						}
					}
				}
				else
				{
					if(!Sense.aliveAwakeMobile(this,false))
						return false;
					if((!Sense.canBeSeenBy(affect.target(),this))
					&&(!(isMine(affect.target())&&(affect.target() instanceof Item))))
					{
						mob.tell("You don't see '"+affect.target().name()+"' here.");
						return false;
					}
					if(!Sense.canTaste(this))
					{
						tell("You can't eat or drink!");
						return false;
					}
				}
			}
			if(Util.bset(srcMajor,Affect.ACT_HANDS))
			{
				if((!Sense.canBeSeenBy(affect.target(),this))
				&&(!(isMine(affect.target())&&(affect.target() instanceof Item)))
				&&(!(isInCombat()&&(affect.target()==victim))))
				{
					mob.tell("You don't see '"+affect.target().name()+"' here.");
					return false;
				}
				if(!Sense.aliveAwakeMobile(this,false))
					return false;

				if((Sense.isSitting(this))&&
				  (affect.targetCode()!=Affect.MSG_OK_VISUAL)&&
				  (((affect.target()!=null)
				    &&(((!(affect.target() instanceof Item))
				      ||(!this.isMine(affect.target())))))
				  ||((affect.tool()!=null)
				    &&(((!(affect.tool() instanceof Item))
				      ||(!this.isMine(affect.tool())))))))
				{
					tell("You need to stand up!");
					return false;
				}
			}

			if(Util.bset(srcMajor,Affect.ACT_MOVE))
			{
				boolean sitting=Sense.isSitting(this);
				if((sitting)
				&&((affect.sourceMinor()==Affect.TYP_LEAVE)
				||(affect.sourceMinor()==Affect.TYP_ENTER)))
					sitting=false;
				   
				if(((Sense.isSleeping(this))||(sitting))
				&&(affect.sourceMinor()!=Affect.TYP_STAND)
				&&(affect.sourceMinor()!=Affect.TYP_SLEEP))
				{
					tell("You need to stand up!");
					return false;
				}
				if(!Sense.canMove(this))
				{
					tell("You can't move!");
					return false;
				}
			}

			switch(affect.sourceMinor())
			{
			case Affect.TYP_BUY:
			case Affect.TYP_CLOSE:
			case Affect.TYP_DELICATE_HANDS_ACT:
			case Affect.TYP_DRINK:
			case Affect.TYP_EAT:
			case Affect.TYP_LEAVE:
			case Affect.TYP_FILL:
			case Affect.TYP_LIST:
			case Affect.TYP_LOCK:
			case Affect.TYP_OPEN:
			case Affect.TYP_SIT:
			case Affect.TYP_SLEEP:
			case Affect.TYP_UNLOCK:
			case Affect.TYP_VALUE:
			case Affect.TYP_SELL:
			case Affect.TYP_READSOMETHING:
				if(isInCombat())
				{
					// the door-for-fleeing exception!
					if(((affect.sourceMinor()==Affect.TYP_OPEN)||(affect.sourceMinor()==Affect.TYP_CLOSE))
					   &&(affect.target()!=null)
					   &&(affect.target() instanceof Exit))
						break;
					tell("Not while you are fighting!");
					return false;
				}
				break;
			case Affect.TYP_REBUKE:
				if(affect.target()!=null)
				{
					if(!Sense.canBeHeardBy(this,affect.target()))
					{
						tell(affect.target().name()+" can't hear you!");
						return false;
					}
					else
					if((!((affect.target() instanceof MOB)&&(((MOB)affect.target()).getLeigeID().equals(name()))))
					&&(!affect.target().name().equals(getLeigeID())))
					{
						tell(affect.target().name()+" does not serve you, and you do not serve "+affect.target().name()+".");
						return false;
					}
				}
				else
				if(getLeigeID().length()==0)
				{
					tell("You aren't serving anyone!");
					return false;
				}
				break;
			case Affect.TYP_SERVE:
				if(affect.target()==null) return false;
				if(affect.target()==this)
				{
					tell("You can't serve yourself!");
					return false;
				}
				if(!Sense.canBeHeardBy(this,affect.target()))
				{
					tell(affect.target().name()+" can't hear you!");
					return false;
				}
				if(getLeigeID().length()>0)
				{
					tell("You are already serving '"+getLeigeID()+"'.");
					return false;
				}
				break;
			case Affect.TYP_CAST_SPELL:
				if(charStats().getStat(CharStats.INTELLIGENCE)<5)
				{
					tell("You aren't smart enough to do magic.");
					return false;
				}
				break;
			default:
				break;
			}
		}
		
		if((affect.sourceCode()!=Affect.NO_EFFECT)
		&&(affect.amISource(this))
		&&(affect.target()!=null)
		&&(affect.target()!=this)
		&&(!Util.bset(affect.sourceCode(),Affect.ACT_GENERAL))
		&&(affect.target() instanceof MOB)
		&&(location()==((MOB)affect.target()).location()))
		{
			MOB target=(MOB)affect.target();
			// and now, the consequences of range
			if((location()!=null)
			   &&(affect.targetMinor()==Affect.TYP_WEAPONATTACK)
			   &&(rangeToTarget()>maxRange(affect.tool())))
			{
				String newstr="<S-NAME> advance(s) at ";
				affect.modify(this,target,null,Affect.MSG_ADVANCE,newstr+target.name(),Affect.MSG_ADVANCE,newstr+"you",Affect.MSG_ADVANCE,newstr+target.name());
				boolean ok=location().okAffect(affect);
				if(ok) setAtRange(rangeToTarget()-1);
				if(victim!=null)
				{
					victim.setAtRange(rangeToTarget());
					victim.recoverEnvStats();
				}
				recoverEnvStats();
				return ok;
			}
			else
			if(affect.tool()!=null)
			{
				int useRange=rangeToTarget();
				Environmental tool=affect.tool();
				if(getVictim()!=null)
				{
					if(getVictim()==target)
						useRange=rangeToTarget();
					else
					{
						if(target.getVictim()==this)
							useRange=target.rangeToTarget();
						else
							useRange=maxRange(tool);
					}
				}
				if((useRange>=0)&&(maxRange(tool)<useRange))
				{
					mob.tell("You are too far away from "+target.name()+" to use "+tool.name()+".");
					return false;
				}
				else
				if((useRange>=0)&&(minRange(tool)>useRange))
				{
					mob.tell("You are too close to "+target.name()+" to use "+tool.name()+".");
					if((affect.targetMinor()==Affect.TYP_WEAPONATTACK)
					&&(tool instanceof Weapon)
					&&(!((Weapon)tool).amWearingAt(Item.INVENTORY)))
						ExternalPlay.remove(this,(Item)tool);
					return false;
				}
			}
		}

		if((affect.targetCode()!=Affect.NO_EFFECT)&&(affect.amITarget(this)))
		{
			if((amDead())||(location()==null))
				return false;
			if(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
			{
				if((affect.amISource(this))
				&&(!Util.bset(affect.sourceMajor(),Affect.ACT_GENERAL)))
				{
					mob.tell("You like yourself too much.");
					if(victim==this) victim=null;
					return false;
				}

				if((!mayIFight(mob))
				||(mob.envStats().level()>envStats().level()+26)&&isMonster()&&mob.isMonster())
				{
					if(!mayIFight(mob))
						mob.tell("Player killing is highly discouraged.");
					else
						mob.tell("That is not EVEN a fair fight.");
					mob.setVictim(null);
					if(victim==mob) setVictim(null);
					return false;
				}
				if(this.amFollowing()==mob)
					setFollowing(null);
				if(isInCombat())
				{
					if((rangeToTarget()>0)
					&&(getVictim()!=affect.source())
					&&(affect.source().getVictim()==this)
					&&(affect.source().rangeToTarget()==0))
					{
					    setVictim(affect.source());
						setAtRange(0);
					}
				}
				
				if(affect.targetMinor()!=Affect.TYP_WEAPONATTACK)
				{
					int chanceToFail=Integer.MIN_VALUE;
					int saveCode=-1;
					for(int c=0;c<CharStats.affectTypeMap.length;c++)
						if(affect.targetMinor()==CharStats.affectTypeMap[c])
						{	saveCode=c; chanceToFail=charStats().getSave(c); break;}
					if((chanceToFail>Integer.MIN_VALUE)&&(!affect.wasModified()))
					{
						chanceToFail+=(envStats().level()-affect.source().envStats().level());
						if(chanceToFail<5)
							chanceToFail=5;
						else
						if(chanceToFail>95)
						   chanceToFail=95;
						
						if(Dice.rollPercentage()<chanceToFail)
						{
							CommonStrings.resistanceMsgs(affect,affect.source(),this);
							affect.tagModified(true);
						}
					}
				}
			}

			if((rangeToTarget()>0)&&(!isInCombat()))
				setAtRange(-1);
			
			switch(affect.targetMinor())
			{
			case Affect.TYP_CLOSE:
			case Affect.TYP_DRINK:
			case Affect.TYP_DROP:
			case Affect.TYP_EAT:
			case Affect.TYP_FILL:
			case Affect.TYP_GET:
			case Affect.TYP_HOLD:
			case Affect.TYP_LOCK:
			case Affect.TYP_OPEN:
			case Affect.TYP_PULL:
			case Affect.TYP_PUT:
			case Affect.TYP_UNLOCK:
			case Affect.TYP_WEAR:
			case Affect.TYP_WIELD:
			case Affect.TYP_MOUNT:
			case Affect.TYP_DISMOUNT:
				mob.tell("You can't do that to "+name()+".");
				return false;
			case Affect.TYP_GIVE:
				if(affect.tool()==null) return false;
				if(!(affect.tool() instanceof Item)) return false;
				if(!Sense.canBeSeenBy(affect.tool(),this))
				{
					mob.tell(name()+" can't see what you are giving.");
					return false;
				}
				break;
			case Affect.TYP_FOLLOW:
				if(isMonster())
				{
					mob.tell("You cannot follow '"+name()+"'.");
					return false;
				}
				if(numFollowers()>=((int)Math.round(Util.div(charStats().getStat(CharStats.CHARISMA),4.0))+1))
				{
					mob.tell(name()+" can't accept any more followers.");
					return false;
				}
				break;
			}
		}
		return true;
	}

	public void tell(MOB source, Environmental target, String msg)
	{
		if(mySession!=null)
			mySession.stdPrintln(source,target,msg);

	}

	public void tell(String msg)
	{
		tell(this,this,msg);
	}

	public void affect(Affect affect)
	{
		
		// sneaking exception
		if((Sense.isSneaking(affect.source()))
		 &&((affect.targetMinor()==Affect.TYP_LEAVE)||(affect.targetMinor()==Affect.TYP_ENTER))
		 &&(!Sense.canSeeSneakers(this))
		 &&(this!=affect.source()))
			return;
		
		if(charStats!=null)
		{
			if(charStats().getMyClass()!=null)
				charStats().getMyClass().affect(this,affect);
			if(charStats().getMyRace()!=null)
				charStats().getMyRace().affect(this,affect);
		}

		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if(B!=null)	B.affect(this,affect);
		}

		MOB mob=affect.source();

		boolean asleep=Sense.isSleeping(this);
		boolean canseesrc=Sense.canBeSeenBy(affect.source(),this);
		boolean canhearsrc=Sense.canBeHeardBy(affect.source(),this);

		if((affect.sourceCode()!=Affect.NO_EFFECT)&&(affect.amISource(this)))
		{
			if(Util.bset(affect.sourceCode(),Affect.MASK_MALICIOUS))
				if((affect.target() instanceof MOB)&&(getVictim()!=affect.target()))
				{
					establishRange(this,(MOB)affect.target(),affect.tool());
					setVictim((MOB)affect.target());
				}

			switch(affect.sourceMinor())
			{
			case Affect.TYP_REBUKE:
				if(((affect.target()==null)&&(getLeigeID().length()>0))
				||((affect.target()!=null)&&(affect.target().name().equals(getLeigeID()))))
					setLeigeID("");
				
				tell(this,affect.target(),affect.sourceMessage());
				break;
			case Affect.TYP_SERVE:
				if(affect.target()!=null)
					setLeigeID(affect.target().name());
				tell(this,affect.target(),affect.sourceMessage());
				break;
			case Affect.TYP_EXAMINESOMETHING:
				if((Sense.canBeSeenBy(this,mob))&&(affect.amITarget(this)))
				{
					StringBuffer myDescription=new StringBuffer("");
					if((mob.getBitmap()&MOB.ATT_SYSOPMSGS)>0)
						myDescription.append(ID()+"\n\rRejuv:"+baseEnvStats().rejuv()+"\n\rAbile:"+baseEnvStats().ability()+"\n\rLevel:"+baseEnvStats().level()+"\n\rMisc : "+text()+"\n\r"+description()+"\n\rRoom :'"+((getStartRoom()==null)?"null":getStartRoom().ID())+"\n\r");
					if(!isMonster())
						myDescription.append(name()+" the "+charStats().getMyRace().name()+" is a level "+envStats().level()+" "+charStats().getMyClass().name()+".\n\r");
					myDescription.append(charStats().HeShe()+" is "+envStats().height()+" inches tall and weighs "+baseEnvStats().weight()+" pounds.\n\r");
					myDescription.append(healthText()+"\n\r\n\r");
					myDescription.append(description()+"\n\r\n\r");
					myDescription.append(charStats().HeShe()+" is wearing:\n\r"+ExternalPlay.getEquipment(affect.source(),this));
					tell(myDescription.toString());
				}
				break;
			case Affect.TYP_READSOMETHING:
				if((Sense.canBeSeenBy(this,mob))&&(affect.amITarget(this)))
					tell("There is nothing written on "+name());
				break;
			case Affect.TYP_SIT:
				{
				int oldDisposition=mob.baseEnvStats().disposition();
				oldDisposition=oldDisposition&(Integer.MAX_VALUE-EnvStats.IS_SLEEPING-EnvStats.IS_SNEAKING-EnvStats.IS_SITTING);
				mob.baseEnvStats().setDisposition(oldDisposition|EnvStats.IS_SITTING);
				mob.recoverEnvStats();
				mob.recoverCharStats();
				mob.recoverMaxState();
				tell(this,affect.target(),affect.sourceMessage());
				}
				break;
			case Affect.TYP_SLEEP:
				{
				int oldDisposition=mob.baseEnvStats().disposition();
				oldDisposition=oldDisposition&(Integer.MAX_VALUE-EnvStats.IS_SLEEPING-EnvStats.IS_SNEAKING-EnvStats.IS_SITTING);
				mob.baseEnvStats().setDisposition(oldDisposition|EnvStats.IS_SLEEPING);
				mob.recoverEnvStats();
				mob.recoverCharStats();
				mob.recoverMaxState();
				tell(this,affect.target(),affect.sourceMessage());
				}
				break;
			case Affect.TYP_STAND:
				{
				int oldDisposition=mob.baseEnvStats().disposition();
				oldDisposition=oldDisposition&(Integer.MAX_VALUE-EnvStats.IS_SLEEPING-EnvStats.IS_SNEAKING-EnvStats.IS_SITTING);
				mob.baseEnvStats().setDisposition(oldDisposition);
				mob.recoverEnvStats();
				mob.recoverCharStats();
				mob.recoverMaxState();
				tell(this,affect.target(),affect.sourceMessage());
				}
				break;
			case Affect.TYP_RECALL:
				if((affect.target()!=null) && (affect.target() instanceof Room) && (location() != affect.target()))
				{
					tell(affect.source(),null,affect.targetMessage());
					location().delInhabitant(this);
					((Room)affect.target()).addInhabitant(this);
					((Room)affect.target()).showOthers(mob,null,Affect.MSG_ENTER,"<S-NAME> appears out of the Java Plain.");
					setLocation(((Room)affect.target()));
					recoverEnvStats();
					recoverCharStats();
					recoverMaxState();
					ExternalPlay.look(mob,new Vector(),true);
				}
				break;
			case Affect.TYP_FOLLOW:
				if((affect.target()!=null)&&(affect.target() instanceof MOB))
				{
					setFollowing((MOB)affect.target());
					tell(affect.source(),affect.target(),affect.sourceMessage());
				}
				break;
			case Affect.TYP_NOFOLLOW:
				setFollowing(null);
				tell(affect.source(),affect.target(),affect.sourceMessage());
				break;
			default:
				// you pretty much always know what you are doing, if you can do it.
				tell(affect.source(),affect.target(),affect.sourceMessage());
				break;
			}
		}
		else
		if((affect.targetCode()!=Affect.NO_EFFECT)&&(affect.amITarget(this)))
		{
			// malicious by itself is pure pain
			if(Util.bset(affect.targetCode(),Affect.MASK_HURT))
			{
				int dmg=affect.targetCode()-Affect.MASK_HURT;
				if(dmg>0)
				{
					if((!curState().adjHitPoints(-dmg,maxState()))&&(location()!=null))
						ExternalPlay.die(affect.source(),this);
					else
					if((curState().getHitPoints()<getWimpHitPoint())&&(isInCombat()))
						ExternalPlay.flee(this,"");
				}
			}
			else
			if(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
			{
				if((!isInCombat())&&(location().isInhabitant((MOB)affect.source())))
				{
					establishRange(this,affect.source(),affect.tool());
					setVictim(affect.source());
				}
				if(affect.targetMinor()==Affect.TYP_WEAPONATTACK)
				{
					if((isInCombat())&&(!amDead))
					{
						Weapon weapon=affect.source().myNaturalWeapon();
						if((affect.tool()!=null)&&(affect.tool() instanceof Weapon))
							weapon=(Weapon)affect.tool();
						if(weapon!=null)
						{
							boolean isHit=(CoffeeUtensils.normalizeAndRollLess(affect.source().adjustedAttackBonus()+adjustedArmor()));
							ExternalPlay.postWeaponDamage(affect.source(),this,weapon,isHit);
							affect.tagModified(true);
						}
					}
				}
				else
				{
					// what is this?  a malicious, weapon attack not attached to hurt or weaponstrike!
					if((affect.tool()!=null)&&(affect.tool() instanceof Weapon))
						ExternalPlay.postWeaponDamage(affect.source(),this,(Weapon)affect.tool(),true);
				}
				ExternalPlay.standIfNecessary(this);
			}
			else
			if((affect.targetMinor()==Affect.TYP_GIVE)
			 &&(affect.tool()!=null)
			 &&(affect.tool() instanceof Item))
			{
				FullMsg msg=new FullMsg(affect.source(),affect.tool(),null,Affect.MSG_DROP,null);
				if(!location().okAffect(msg))
					return;
				location().send(this,msg);
				msg=new FullMsg((MOB)affect.target(),affect.tool(),null,Affect.MSG_GET,null);
				if(!location().okAffect(msg))
					return;
				location().send(this,msg);
			}
			else
			if((affect.targetMinor()==Affect.TYP_EXAMINESOMETHING)
			&&(Sense.canBeSeenBy(this,mob)))
			{
				StringBuffer myDescription=new StringBuffer("");
				if((mob.getBitmap()&MOB.ATT_SYSOPMSGS)>0)
					myDescription.append(ID()+"\n\rRejuv:"+baseEnvStats().rejuv()+"\n\rAbile:"+baseEnvStats().ability()+"\n\rLevel:"+baseEnvStats().level()+"\n\rMisc :'"+text()+"\n\rRoom :'"+((getStartRoom()==null)?"null":getStartRoom().ID())+"\n\r"+description()+"\n\r");
				if(!isMonster())
					myDescription.append(name()+" the "+charStats().getMyRace().name()+" is a level "+envStats().level()+" "+charStats().getMyClass().name()+".\n\r");
				myDescription.append(charStats().HeShe()+" is "+envStats().height()+" inches tall and weighs "+baseEnvStats().weight()+" pounds.\n\r");
				myDescription.append(healthText()+"\n\r\n\r");
				myDescription.append(description()+"\n\r\n\r");
				myDescription.append(charStats().HeShe()+" is wearing:\n\r"+ExternalPlay.getEquipment(affect.source(),this));
				mob.tell(myDescription.toString());
			}
			else
			if((affect.targetMinor()==Affect.TYP_REBUKE)
			&&(affect.source().name().equals(getLeigeID())))
				setLeigeID("");
			
			int targetMajor=affect.targetMajor();
			if((Util.bset(targetMajor,Affect.AFF_SOUNDEDAT))
			&&(canhearsrc)&&(!asleep))
			{
				if((affect.targetMinor()==Affect.TYP_SPEAK)&&(affect.source()!=null))
					replyTo=affect.source();
				tell(affect.source(),affect.target(),affect.targetMessage());
			}
			else
			if(((Util.bset(targetMajor,Affect.AFF_HEARD))
			  ||(Util.bset(targetMajor,Affect.AFF_SEEN))
			  ||(Util.bset(targetMajor,Affect.AFF_GENERAL)))
			&&(!asleep)&&(canseesrc))
				tell(affect.source(),affect.target(),affect.targetMessage());
			else
			if(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
				tell(affect.source(),affect.target(),affect.targetMessage());
			else
			if(((Util.bset(targetMajor,Affect.AFF_TOUCHED))
				||(Util.bset(targetMajor,Affect.AFF_MOVEDON))
				||((Util.bset(targetMajor,Affect.AFF_CONSUMED))&&(!Util.bset(targetMajor,Affect.AFF_SOUNDEDAT))))
			&&(!asleep)&&((canhearsrc)||(canseesrc)))
				tell(affect.source(),affect.target(),affect.targetMessage());
		}
		else
		if((affect.othersCode()!=Affect.NO_EFFECT)
		&&(!affect.amISource(this))
		&&(!affect.amITarget(this)))
		{
			int othersMajor=affect.othersMajor();

			if(Util.bset(affect.othersCode(),Affect.MASK_MALICIOUS)&&(affect.target() instanceof MOB))
				fightingFollowers((MOB)affect.target(),affect.source());

			if(Util.bset(othersMajor,affect.MASK_CHANNEL))
			{
				if(!Util.isSet(getChannelMask(),(affect.othersCode()-affect.MASK_CHANNEL)))
					tell(affect.source(),affect.target(),affect.othersMessage());
			}
			else
			if((Util.bset(othersMajor,Affect.OTH_HEAR_SOUNDS))
			&&(!asleep)
			&&(canhearsrc))
				tell(affect.source(),affect.target(),affect.othersMessage());
			else
			if(((Util.bset(othersMajor,Affect.OTH_SEE_SEEING))
			||(Util.bset(othersMajor,Affect.OTH_SENSE_LISTENING))
			||(Util.bset(othersMajor,Affect.OTH_SENSE_TOUCHING))
			||(Util.bset(othersMajor,Affect.OTH_GENERAL)))
			&&((!asleep)&&(canseesrc)))
			{
				tell(affect.source(),affect.target(),affect.othersMessage());
			}
			else
			if(((Util.bset(othersMajor,Affect.OTH_SENSE_MOVEMENT))
				||((Util.bset(othersMajor,Affect.OTH_SENSE_CONSUMPTION))&&(!Util.bset(othersMajor,Affect.OTH_HEAR_SOUNDS))))
			&&((!asleep)||(affect.othersMinor()==Affect.TYP_ENTER))
			&&((canseesrc)||(canhearsrc)))
				tell(affect.source(),affect.target(),affect.othersMessage());
			if((affect.othersMinor()==Affect.TYP_DEATH)&&(victim!=null))
			{
				if(victim==affect.source()) 
					setVictim(null);
				else
				if((victim.getVictim()==null)||(victim.getVictim()==affect.source()))
				{
					if((amFollowing()!=null)&&(victim.amFollowing()!=null)&&(amFollowing()==victim.amFollowing()))
						setVictim(null);
					else
					{
						victim.setAtRange(-1);
						victim.setVictim(this);
					}
				}
			}
		}

		for(int i=0;i<inventorySize();i++)
		{
			Item I=(Item)fetchInventory(i);
			if(I!=null)
				I.affect(affect);
		}

		for(int i=0;i<numAffects();i++)
		{
			Ability A=(Ability)fetchAffect(i);
			if(A!=null)
				A.affect(affect);
		}
	}

	public void affectCharStats(MOB affectedMob, CharStats affectableStats){}
	
	public boolean tick(int tickID)
	{
		if(pleaseDestroy)
			return false;

		if(tickID==Host.MOB_TICK)
		{
			if(amDead)
			{
				if(isMonster())
					if(envStats().rejuv()<Integer.MAX_VALUE)
					{
						envStats().setRejuv(envStats().rejuv()-1);
						if(envStats().rejuv()<0)
							bringToLife(getStartRoom());
					}
					else
					{
						destroy();
						return false;
					}
			}
			else
			if(location()!=null)
			{
				curState().adjState(this,maxState);
				curState().expendEnergy(this,maxState,false);
				if(!Sense.canBreathe(this))
				{
					this.location().show(this,this,Affect.MSG_OK_VISUAL,"^Z<S-NAME> can't breathe!^?");
					ExternalPlay.postDamage(this,this,null,(int)Math.round(Util.mul(Math.random(),baseEnvStats().level()+2)),Affect.NO_EFFECT,-1,null);
				}
				if(isInCombat())
				{
					Item weapon=this.fetchWieldedItem();
					if(weapon==null) // try to wield anything!
					{
						for(int i=0;i<inventorySize();i++)
						{
							Item thisItem=fetchInventory(i);
							if((thisItem!=null)
							 &&(thisItem.canBeWornAt(Item.WIELD))
							 &&(thisItem.canWear(this))
							 &&(!thisItem.amWearingAt(Item.INVENTORY)))
							{
								thisItem.wearAt(Item.WIELD);
								weapon=thisItem;
								break;
							}
						}
					}
					if(((getBitmap()&MOB.ATT_AUTOMELEE)==0)
					   ||(rangeToTarget()<=minRange(weapon)))
					{
						double curSpeed=Math.floor(speeder);
						speeder+=envStats().speed();
						if(Sense.aliveAwakeMobile(this,true))
						{
							int numAttacks=(int)Math.round(Math.floor(speeder-curSpeed));
							for(int s=0;s<numAttacks;s++)
								if((!amDead())
								&&(curState().getHitPoints()>0)
								&&((s==0)||(!Sense.isSitting(this))))
								{
									if((weapon!=null)&&(weapon.amWearingAt(Item.INVENTORY)))
										weapon=this.fetchWieldedItem();
									ExternalPlay.postAttack(this,victim,weapon);
								}
							curState().expendEnergy(this,maxState,false);
						}
					}
					if(!isMonster())
					{
						MOB target=this.getVictim();
						if((target!=null)&&(!target.amDead())&&(Sense.canBeSeenBy(target,this)))
							session().print(target.healthText()+"\n\r\n\r");
					}
				}
				else
					speeder=0.0;
				if((riding()!=null)&&(CoffeeUtensils.roomLocation(riding())!=location()))
					setRiding(null);
				if((!isMonster())&&(((++minuteCounter)*Host.TICK_TIME)>60000))
				{
					minuteCounter=0;
					setAgeHours(AgeHours+1);
				}
			}

			int a=0;
			while(a<numAffects())
			{
				Ability A=fetchAffect(a);
				if(A!=null)
				{
					int s=affects.size();
					if(!A.tick(tickID))
						A.unInvoke();
					if(affects.size()==s)
						a++;
				}
				else
					a++;
			}

			for(int b=0;b<numBehaviors();b++)
			{
				Behavior B=fetchBehavior(b);
				if(B!=null) B.tick(this,tickID);
			}
		}
		lastTickedDateTime=Calendar.getInstance();
		return !pleaseDestroy;
	}

	public boolean isMonster()
	{
		if(mySession==null)
		   return true;
		return false;
	}

	public boolean isASysOp(Room of)
	{
		if(baseCharStats()==null) return false;
		if(baseCharStats().getMyClass()==null) return false;
		if(this.baseCharStats().getMyClass().ID().equals("Archon"))
			return true;
		if(of==null) return false;
		if(of.getArea()==null) return false;
		if(of.getArea().amISubOp(Username))
			return true;
		return false;
	}

	public void addInventory(Item item)
	{
		item.setOwner(this);
		inventory.addElement(item);
		item.recoverEnvStats();
	}
	public void delInventory(Item item)
	{
		inventory.removeElement(item);
		item.recoverEnvStats();
	}
	public int inventorySize()
	{
		return inventory.size();
	}
	public Item fetchInventory(int index)
	{
		try
		{
			return (Item)inventory.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Item fetchInventory(String itemName)
	{
		Item item=(Item)CoffeeUtensils.fetchAvailableItem(inventory,itemName,null,false,false,true);
		if(item==null) item=(Item)CoffeeUtensils.fetchAvailableItem(inventory,itemName,null,false,false,false);
		return item;
	}
	public Item fetchCarried(Item goodLocation, String itemName)
	{
		Item item=(Item)CoffeeUtensils.fetchAvailableItem(inventory,itemName,goodLocation,false,true,true);
		if(item==null) item=(Item)CoffeeUtensils.fetchAvailableItem(inventory,itemName,goodLocation,false,true,false);
		return item;
	}
	public Item fetchWornItem(String itemName)
	{
		Item item=(Item)CoffeeUtensils.fetchAvailableItem(inventory,itemName,null,true,false,true);
		if(item==null) item=(Item)CoffeeUtensils.fetchAvailableItem(inventory,itemName,null,true,false,false);
		return item;
	}
	public void addFollower(MOB follower)
	{
		if(follower!=null)
			followers.addElement(follower);
	}

	public void delFollower(MOB follower)
	{
		if(follower!=null)
			followers.removeElement(follower);
	}
	public int numFollowers()
	{
		return followers.size();
	}
	public MOB fetchFollower(int index)
	{
		try
		{
			return (MOB)followers.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public MOB fetchFollower(MOB thisOne)
	{
		if(followers.contains(thisOne))
			return thisOne;
		return null;
	}
	public MOB fetchFollower(String ID)
	{
		MOB mob=(MOB)CoffeeUtensils.fetchEnvironmental(followers,ID,true);
		if (mob==null) mob=(MOB)CoffeeUtensils.fetchEnvironmental(followers,ID,false);
		return mob;
	}
	public MOB amFollowing()
	{
		if(amFollowing!=null)
		{
			if(amFollowing.fetchFollower(this)==null)
				amFollowing=null;
		}
		return amFollowing;
	}
	public void setFollowing(MOB mob)
	{
		if(mob==null)
		{
			if(amFollowing!=null)
			{
				if(amFollowing.fetchFollower(this)!=null)
					amFollowing.delFollower(this);
			}
		}
		else
		if(mob.fetchFollower(this)==null)
			mob.addFollower(this);
		amFollowing=mob;
	}
	private void addFollowers(MOB mob, Hashtable toThis)
	{
		if(toThis.get(mob)==null)
		   	toThis.put(mob,mob);

		for(int f=0;f<mob.numFollowers();f++)
		{
			MOB follower=mob.fetchFollower(f);
			if((follower!=null)&&(toThis.get(follower)==null))
			{
				toThis.put(follower,follower);
				addFollowers(follower,toThis);
			}
		}
	}

	public void getGroupMembers(Hashtable list)
	{
		if(list==null) return;
		if(list.get(this)==null) list.put(this,this);
		if(amFollowing()!=null)
			amFollowing().getGroupMembers(list);
		for(int f=0;f<numFollowers();f++)
		{
			MOB follower=fetchFollower(f);
			if((follower!=null)&&(list.get(follower)==null))
				follower.getGroupMembers(list);
		}
	}

	public boolean isEligibleMonster()
	{
		if(!isMonster())
			return false;
		MOB followed=amFollowing();
		if(followed!=null)
			if(!followed.isMonster())
				return false;
		return true;

	}

	public MOB soulMate()
	{
		return soulMate;
	}

	public void setSoulMate(MOB mob)
	{
		soulMate=mob;
	}

	public void addAbility(Ability to)
	{
		if(to==null) return;
		for(int a=0;a<numAbilities();a++)
		{
			Ability A=fetchAbility(a);
			if((A!=null)&&(A.ID().equals(to.ID())))
				return;
		}
		int qualifyingLevel=to.qualifyingLevel(this);
		if((qualifyingLevel>=0)&&(qualifyingLevel!=to.envStats().level()))
		{
			to.baseEnvStats().setLevel(qualifyingLevel);
			to.recoverEnvStats();
		}
		abilities.addElement(to);
	}
	public void delAbility(Ability to)
	{
		abilities.removeElement(to);
	}
	public int numAbilities()
	{
		return abilities.size();
	}
	public Ability fetchAbility(int index)
	{
		try
		{
			return (Ability)abilities.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Ability fetchAbility(String ID)
	{
		for(int a=0;a<numAbilities();a++)
		{
			Ability A=fetchAbility(a);
			if((A!=null)&&((A.ID().equalsIgnoreCase(ID))||(A.name().equalsIgnoreCase(ID))))
				return A;
		}
		return (Ability)CoffeeUtensils.fetchEnvironmental(abilities,ID,false);
	}

	public void addNonUninvokableAffect(Ability to)
	{
		if(to==null) return;
		if(affects.contains(to)) return;
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void addAffect(Ability to)
	{
		if(to==null) return;
		if(affects.contains(to)) return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void delAffect(Ability to)
	{
		int size=affects.size();
		affects.removeElement(to);
		if(affects.size()<size)
			to.setAffectedOne(null);
	}
	public int numAffects()
	{
		return affects.size();
	}
	public Ability fetchAffect(int index)
	{
		try
		{
			return (Ability)affects.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Ability fetchAffect(String ID)
	{
		for(int a=0;a<numAffects();a++)
		{
			Ability A=fetchAffect(a);
			if((A!=null)&&(A.ID().equals(ID)))
				return A;
		}
		return null;
	}

	/** Manipulation of Behavior objects, which includes
	 * movement, speech, spellcasting, etc, etc.*/
	public void addBehavior(Behavior to)
	{
		if(to==null) return;
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equals(to.ID())))
				return;
		}
		to.startBehavior(this);
		behaviors.addElement(to);
	}
	public void delBehavior(Behavior to)
	{
		behaviors.removeElement(to);
	}
	public int numBehaviors()
	{
		return behaviors.size();
	}
	public Behavior fetchBehavior(int index)
	{
		try
		{
			return (Behavior)behaviors.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}

	public boolean amWearingSomethingHere(long wornCode)
	{
		for(int i=0;i<inventorySize();i++)
		{
			Item thisItem=fetchInventory(i);
			if((thisItem!=null)&&(thisItem.amWearingAt(wornCode)))
				return true;
		}
		return false;
	}
	public Item fetchWornItem(long wornCode)
	{
		for(int i=0;i<inventorySize();i++)
		{
			Item thisItem=fetchInventory(i);
			if((thisItem!=null)&&(thisItem.amWearingAt(wornCode)))
				return thisItem;
		}
		return null;
	}

	public Item fetchWieldedItem()
	{
		for(int i=0;i<inventorySize();i++)
		{
			Item thisItem=fetchInventory(i);
			if((thisItem!=null)&&(thisItem.amWearingAt(Item.WIELD)))
				return thisItem;
		}
		return null;
	}

	public boolean isMine(Environmental env)
	{
		if(env instanceof Item)
		{
			if(inventory.contains(env)) return true;
			return false;
		}
		else
		if(env instanceof MOB)
		{
			if(followers.contains(env)) return true;
			return false;
		}
		else
		if(env instanceof Ability)
		{
			if(abilities.contains(env)) return true;
			if(affects.contains(env)) return true;
			return false;
		}
		return false;
	}

	private void fightingFollowers(MOB target, MOB source)
	{
		if((source==null)||(target==null)) return;
		if((target==this)||(source==this)) return;
		if(((getBitmap()&MOB.ATT_AUTOASSIST)>0)) return;
		if(isInCombat()) return;

		if((amFollowing()==target)
		||(target.amFollowing()==this)
		||((target.amFollowing()!=null)&&(target.amFollowing()==this.amFollowing())))
			setVictim(source);//ExternalPlay.postAttack(this,source,fetchWieldedItem());
		else
		if((amFollowing()==source)
		||(source.amFollowing()==this)
		||((source.amFollowing()!=null)&&(source.amFollowing()==this.amFollowing())))
			setVictim(target);//ExternalPlay.postAttack(this,target,fetchWieldedItem());
	}
	public int getTermID()
	{
		return termID;
	}
	public void setTermID(int tid)
	{
		termID = tid != 0 ? 1 : 0;
		if(mySession!=null)
			mySession.setTermID(tid);
	}
}
