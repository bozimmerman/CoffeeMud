package com.planet_ink.coffee_mud.MOBS;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class StdMOB implements MOB
{
	public String ID(){return "StdMOB";}
	protected String Username="";

	private String clanID=null;
	private int clanRole=0;

	protected CharStats baseCharStats=new DefaultCharStats();
	protected CharStats charStats=new DefaultCharStats();

	protected EnvStats envStats=new DefaultEnvStats();
	protected EnvStats baseEnvStats=new DefaultEnvStats();
	
	protected PlayerStats playerStats=null;

	protected boolean amDead=false;
	protected Room location=null;
	protected Room lastLocation=null;
	protected Rideable riding=null;

	protected Session mySession=null;
	protected boolean pleaseDestroy=false;
	protected byte[] description=null;
	protected String displayText="";
	protected byte[] miscText=null;

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
			mySession.setTermID(((Util.bset(attributesBitmap,MOB.ATT_ANSI))?1:0)+((Util.bset(attributesBitmap,MOB.ATT_SOUND))?2:0));
	}

	protected int minuteCounter=0;
	private int movesSinceTick=0;

	// the core state values
	public CharState curState=new DefaultCharState();
	public CharState maxState=new DefaultCharState();
	public CharState baseState=new DefaultCharState();
	private long lastTickedDateTime=System.currentTimeMillis();
	public long lastTickedDateTime(){return lastTickedDateTime;}

	// mental characteristics
	protected int Alignment=0;
	protected String WorshipCharID="";
	protected String LeigeID="";
	protected int WimpHitPoint=0;
	protected int QuestPoint=0;
	protected int DeityIndex=-1;
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
	public Deity getMyDeity()
	{
		if(getWorshipCharID().length()==0) return null;
		Deity bob=CMMap.getDeity(getWorshipCharID());
		if(bob==null)
			setWorshipCharID("");
		return bob;
	}

	// location!
	protected Room StartRoom=null;
	public Room getStartRoom(){return StartRoom;}
	public void setStartRoom(Room newVal){StartRoom=newVal;}


	protected MOB victim=null;
	protected MOB amFollowing=null;
	protected MOB soulMate=null;
	private double speeder=0.0;
	protected int atRange=-1;
	private long peaceTime=0;
	public long peaceTime(){return peaceTime;}

	public String Name()
	{
		return Username;
	}
	public void setName(String newName){Username=newName;}
	public String name()
	{
		if(envStats().newName()!=null) return envStats().newName();
		return Username;
	}
	public Environmental newInstance()
	{
		return new StdMOB();
	}
	public StdMOB(){}

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
		for(int i=0;i<inventorySize();i++)
		{
			Item I2=fetchInventory(i);
			if((I2!=null)
			&&(I2.container()!=null)
			&&(!isMine(I2.container())))
				for(int ii=0;ii<E.inventorySize();ii++)
					if((E.fetchInventory(ii)==I2.container())&&(ii<inventorySize()))
					{ I2.setContainer(fetchInventory(ii)); break;}
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
				behaviors.addElement((Behavior)B.copyOf());
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
		if(getMyDeity()!=null) getMyDeity().affectEnvStats(this,envStats);
		if(charStats!=null)
		{
			charStats().getCurrentClass().affectEnvStats(this,envStats);
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
		/* the follower light exception*/
		if(!Sense.isLightSource(this))
		for(int f=0;f<numFollowers();f++)
			if(Sense.isLightSource(fetchFollower(f)))
				envStats.setDisposition(envStats().disposition()|EnvStats.IS_LIGHTSOURCE);
	}
	public void setBaseEnvStats(EnvStats newBaseEnvStats)
	{
		baseEnvStats=newBaseEnvStats.cloneStats();
	}

	public int maxCarry()
	{
		double str=new Integer(charStats().getStat(CharStats.STRENGTH)).doubleValue();
		double bodyWeight=0.0;
		if(charStats().getMyRace()==baseCharStats().getMyRace())
			bodyWeight=new Integer(baseEnvStats().weight()).doubleValue();
		else
			bodyWeight=new Integer(charStats().getMyRace().getMaxWeight()).doubleValue();
		return (int)Math.round(bodyWeight + ((str+10.0)*str*bodyWeight/150.0) + (str*5.0));
	}
	public int maxFollowers()
	{
		return ((int)Math.round(Util.div(charStats().getStat(CharStats.CHARISMA),4.0))+1);
	}

	public CharStats baseCharStats(){return baseCharStats;}
	public CharStats charStats(){return charStats;}
	public void recoverCharStats()
	{
		baseCharStats.setClassLevel(baseCharStats.getCurrentClass(),baseEnvStats().level()-baseCharStats().combinedSubLevels());
		charStats=baseCharStats().cloneCharStats();

		if(riding()!=null) riding().affectCharStats(this,charStats);
		if(getMyDeity()!=null) getMyDeity().affectCharStats(this,charStats);
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
		charStats.getCurrentClass().affectCharStats(this,charStats);
		charStats.getMyRace().affectCharStats(this,charStats);
	}
	public void setBaseCharStats(CharStats newBaseCharStats)
	{
		baseCharStats=newBaseCharStats.cloneCharStats();
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if((Sense.isLightSource(this))&&(affected instanceof Room))
		{
			if(Sense.isInDark(affected))
				affectableStats.setDisposition(affectableStats.disposition()-EnvStats.IS_DARK);
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_LIGHTSOURCE);
		}
	}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}

	public CharState curState(){return curState;}
	public CharState maxState(){return maxState;}
	public CharState baseState(){return baseState;}
	public PlayerStats playerStats()
	{
		if((playerStats==null)&&(soulMate!=null))
			return soulMate.playerStats();
		return playerStats;
	}
	public void setPlayerStats(PlayerStats newStats){playerStats=newStats;}
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

	public boolean amDead()
	{
		return amDead||pleaseDestroy;
	}

	
	public void destroy()
	{
		removeFromGame();
		while(numBehaviors()>0)
			delBehavior(fetchBehavior(0));
		while(numAffects()>0)
			delAffect(fetchAffect(0));
		while(numAbilities()>0)
			delAbility(fetchAbility(0));
		while(inventorySize()>0)
		{
			Item I=fetchInventory(0);
			I.destroy();
			delInventory(I);
		}
	}
	
	public void removeFromGame()
	{
		pleaseDestroy=true;
		if(location!=null)
		{
			location().delInhabitant(this);
			if(mySession!=null)
				location().show(this,null,Affect.MSG_OK_ACTION,"<S-NAME> vanish(es) in a puff of smoke.");
		}
		setFollowing(null);
		Vector oldFollowers=new Vector();
		while(numFollowers()>0)
		{
			MOB follower=fetchFollower(0);
			if(follower!=null)
			{
				if(follower.isMonster())
					oldFollowers.addElement(follower);
				follower.setFollowing(null);
				delFollower(follower);
			}
		}
		
		if(!isMonster())
		{
			for(int f=0;f<oldFollowers.size();f++)
			{
				MOB follower=(MOB)oldFollowers.elementAt(f);
				if(follower.location()!=null)
				{
					MOB newFol=(MOB)follower.copyOf();
					newFol.baseEnvStats().setRejuv(0);
					newFol.text();
					follower.killMeDead(false);
					addFollower(newFol);
				}
			}
			session().setKillFlag(true);
		}
	}

	public String getClanID(){return ((clanID==null)?"":clanID);}
	public void setClanID(String clan){clanID=clan;}
	public int getClanRole(){return clanRole;}
	public void setClanRole(int role){clanRole=role;}

	public void bringToLife(Room newLocation, boolean resetStats)
	{
		amDead=false;
		if((miscText!=null)&&(resetStats))
		{
			if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MOBCOMPRESS))
				setMiscText(Util.decompressString(miscText));
			else
				setMiscText(new String(miscText));
		}
		if(getStartRoom()==null)
			setStartRoom(isMonster()?newLocation:CMMap.getStartRoom(this));
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
			location().addInhabitant(this);
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
		if((!isGeneric())&&(resetStats))
			resetToMaxState();

		if(Sense.isSleeping(this))
			tell("(You are asleep)");
		else
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
			if((victim instanceof StdMOB)
			&&(((StdMOB)victim).victim==this))
				victim.setVictim(null);
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
		if(mob.curState().getHitPoints()<=0) return false;
		if(amDead()) return false;
		if(curState().getHitPoints()<=0) return false;
		if(mob.isMonster()) return true;
		if(isMonster()){
			MOB fol=amFollowing();
			if((fol!=null)&&(!fol.isMonster()))
				return fol.mayIFight(mob);
			return true;
		}
		if(mob==this) return true;
		if(CommonStrings.getVar(CommonStrings.SYSTEM_PKILL).startsWith("ALWAYS"))
			return true;
		if(CommonStrings.getVar(CommonStrings.SYSTEM_PKILL).startsWith("NEVER"))
			return false;
		if(!Util.bset(getBitmap(),MOB.ATT_PLAYERKILL)) return false;
		if(!Util.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL)) return false;
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
		double att=new Integer(
				envStats().attackAdjustment()
				+((charStats().getStat(CharStats.STRENGTH)-9)*3)).doubleValue();
		if(curState().getHunger()<1) att=att*.9;
		if(curState().getThirst()<1) att=att*.9;
		if(curState().getFatigue()>CharState.FATIGUED_MILLIS) att=att*.8;
		return (int)Math.round(att);
	}

	public int adjustedArmor()
	{
		double arm=new Integer(((charStats().getStat(CharStats.DEXTERITY)-9)*3)
							   +50).doubleValue();
		if((envStats().disposition()&EnvStats.IS_SLEEPING)>0) arm=0.0;
		if(arm>0.0)
		{
			if(curState().getHunger()<1) arm=arm*.85;
			if(curState().getThirst()<1) arm=arm*.85;
			if(curState().getFatigue()>CharState.FATIGUED_MILLIS) arm=arm*.85;
			if((envStats().disposition()&EnvStats.IS_SITTING)>0) arm=arm*.75;
		}
		return (int)Math.round(envStats().armor()-arm);
	}
	
	public int adjustedDamage(Weapon weapon, MOB target)
	{
		double damageAmount=0.0;
		if(target!=null)
		{
			if((weapon!=null)&&((weapon.weaponClassification()==Weapon.CLASS_RANGED)||(weapon.weaponClassification()==Weapon.CLASS_THROWN)))
				damageAmount = new Integer(Dice.roll(1, weapon.envStats().damage(),1)).doubleValue();
			else
				damageAmount = new Integer(Dice.roll(1, envStats().damage(), (charStats().getStat(CharStats.STRENGTH) / 3)-2)).doubleValue();
			if(!Sense.canBeSeenBy(target,this)) damageAmount *=.5;
			if(Sense.isSleeping(target)) damageAmount *=1.5;
			else
			if(Sense.isSitting(target)) damageAmount *=1.2;
		}
		else
		if((weapon!=null)&&((weapon.weaponClassification()==Weapon.CLASS_RANGED)||(weapon.weaponClassification()==Weapon.CLASS_THROWN)))
			damageAmount = new Integer(weapon.envStats().damage()+1).doubleValue();
		else
			damageAmount = new Integer(envStats().damage()+(charStats().getStat(CharStats.STRENGTH) / 3)-2).doubleValue();
		if(curState().getHunger() < 1) damageAmount *= .8;
		if(curState().getFatigue()>CharState.FATIGUED_MILLIS) damageAmount *=.8;
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
			||(mob.amDead())
			||(amDead())
			||(mob.location()!=location())
			||(!location().isInhabitant(this))
			||(!location().isInhabitant(mob)))
			{
				if(victim!=null)
					victim.setVictim(null);
				victim=null;
			}
			else
			{
				mob.recoverCharStats();
				mob.recoverEnvStats();
				mob.recoverMaxState();
			}
		}
	}
	public DeadBody killMeDead(boolean createBody)
	{
		Room deathRoom=null;
		if(isMonster())
			deathRoom=location();
		else
			deathRoom=CMMap.getBodyRoom(this);
		if(location()!=null) location().delInhabitant(this);
		DeadBody Body=null;
		if(createBody)
			Body=charStats().getMyRace().getCorpse(this,deathRoom);
		amDead=true;
		makePeace();
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
			bringToLife(CMMap.getDeathRoom(this),true);
		if(Body!=null) Body.startTicker(deathRoom);
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
		return (Weapon)CMClass.getWeapon("Natural");
	}

	public String displayText(MOB viewer)
	{
		if((displayText.length()==0)
		   ||(!name().equals(name()))
		   ||(Sense.isSleeping(this))
		   ||(Sense.isSitting(this))
		   ||(riding()!=null)
		   ||((this instanceof Rideable)&&(((Rideable)this).numRiders()>0))
		   ||(isInCombat()))
		{
			StringBuffer sendBack=null;
			sendBack=new StringBuffer(name());
			sendBack.append(" ");
			sendBack.append(Sense.dispositionString(this,Sense.flag_is));
			sendBack.append(" here");
			if(riding()!=null)
			{
				sendBack.append(" "+riding().stateString(this)+" ");
				if(riding()==viewer)
					sendBack.append("YOU");
				else
					sendBack.append(riding().name());
			}
			else
			if((this instanceof Rideable)
			   &&(((Rideable)this).numRiders()>0)
			   &&(((Rideable)this).stateStringSubject(((Rideable)this).fetchRider(0)).length()>0))
			{
				Rideable me=(Rideable)this;
				String first=me.stateStringSubject(me.fetchRider(0));
				sendBack.append(" "+first+" ");
				for(int r=0;r<me.numRiders();r++)
				{
					Rider rider=me.fetchRider(r);
					if((rider!=null)&&(me.stateStringSubject(rider).equals(first)))
					{
						if(r>0)
						{
							sendBack.append(", ");
							if(r==me.numRiders()-1)
								sendBack.append("and ");
						}
						if(rider==viewer)
							sendBack.append("you");
						else
							sendBack.append(rider.name());
					}

				}
			}
			if((isInCombat())&&(Sense.canMove(this))&&(!Sense.isSleeping(this)))
			{
				sendBack.append(" fighting ");
				if(getVictim()==viewer)
					sendBack.append("YOU");
				else
					sendBack.append(getVictim().name());
			}
			sendBack.append(".");
			return sendBack.toString();
		}
		else
			return displayText;
	}

	public String displayText()
	{
		return displayText;
	}
	public void setDisplayText(String newDisplayText)
	{
		displayText=newDisplayText;
	}
	public String description()
	{
		if((description==null)||(description.length==0))
			return "";
		else
		if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MOBDCOMPRESS))
			return Util.decompressString(description);
		else
			return new String(description);
	}
	public void setDescription(String newDescription)
	{
		if(newDescription.length()==0)
			description=null;
		else
		if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MOBDCOMPRESS))
			description=Util.compressString(newDescription);
		else
			description=newDescription.getBytes();
	}
	public void setMiscText(String newText)
	{
		if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MOBCOMPRESS))
			miscText=Util.compressString(newText);
		else
			miscText=newText.getBytes();
	}
	public String text()
	{
		if(miscText.length==0)
			return "";
		else
		if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MOBCOMPRESS))
			return Util.decompressString(miscText);
		else
			return new String(miscText);
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
					Rider rider=source.riding().fetchRider(r);
					if(!(rider instanceof MOB)) continue;
					MOB otherMOB=(MOB)rider;
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


	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((getMyDeity()!=null)&&(!getMyDeity().okAffect(this,affect)))
			return false;

		if(charStats!=null)
		{
			if(!charStats().getCurrentClass().okAffect(this,affect))
				return false;
			if(!charStats().getMyRace().okAffect(this, affect))
				return false;
		}

		for(int i=0;i<numAffects();i++)
		{
			Ability aff=(Ability)fetchAffect(i);
			if((aff!=null)&&(!aff.okAffect(this,affect)))
				return false;
		}

		for(int i=0;i<inventorySize();i++)
		{
			Item I=(Item)fetchInventory(i);
			if((I!=null)&&(!I.okAffect(this,affect)))
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
		&&(!Util.bset(affect.sourceMajor(),Affect.MASK_GENERAL)))
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
					if((getLeigeID().length()>0)&&(target.Name().equals(getLeigeID())))
					{
						tell("You are serving '"+getLeigeID()+"'!");
						return false;
					}
					establishRange(this,(MOB)affect.target(),affect.tool());
				}
			}


			if(Util.bset(srcMajor,Affect.MASK_EYES))
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
			if(Util.bset(srcMajor,Affect.MASK_MOUTH))
			{
				if(!Sense.aliveAwakeMobile(this,false))
					return false;
				if(Util.bset(srcMajor,Affect.MASK_SOUND))
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
						if(Sense.isAnimalIntelligence(this))
						{
							tell("You aren't smart enough to speak.");
							return false;
						}
					}
				}
				else
				{
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
			if(Util.bset(srcMajor,Affect.MASK_HANDS))
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
				  (affect.sourceMinor()!=Affect.TYP_SITMOVE)&&
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

			if(Util.bset(srcMajor,Affect.MASK_MOVE))
			{
				boolean sitting=Sense.isSitting(this);
				if((sitting)
				&&((affect.sourceMinor()==Affect.TYP_LEAVE)
				||(affect.sourceMinor()==Affect.TYP_ENTER)))
					sitting=false;

				if(((Sense.isSleeping(this))||(sitting))
				&&(affect.sourceMinor()!=Affect.TYP_STAND)
				&&(affect.sourceMinor()!=Affect.TYP_SITMOVE)
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
			case Affect.TYP_ENTER:
				movesSinceTick++;
				break;
			case Affect.TYP_JUSTICE:
				if((affect.target()!=null)
				&&(isInCombat())
				&&(affect.target() instanceof Item))
				{
					tell("Not while you are fighting!");
					return false;
				}
				break;
			case Affect.TYP_OPEN:
			case Affect.TYP_CLOSE:
				if(isInCombat())
				{
					if((affect.target()!=null)
					&&((affect.target() instanceof Exit)||(affect.source().isMine(affect.target()))))
						break;
					tell("Not while you are fighting!");
					return false;
				}
				break;
			case Affect.TYP_BUY:
			case Affect.TYP_DELICATE_HANDS_ACT:
			case Affect.TYP_LEAVE:
			case Affect.TYP_FILL:
			case Affect.TYP_LIST:
			case Affect.TYP_LOCK:
			case Affect.TYP_SIT:
			case Affect.TYP_SLEEP:
			case Affect.TYP_UNLOCK:
			case Affect.TYP_VALUE:
			case Affect.TYP_SELL:
			case Affect.TYP_VIEW:
			case Affect.TYP_READSOMETHING:
				if(isInCombat())
				{
					tell("Not while you are fighting!");
					return false;
				}
				break;
			case Affect.TYP_REBUKE:
				if((affect.target()==null)||(!(affect.target() instanceof Deity)))
				{
					if(affect.target()!=null)
					{
						if(!Sense.canBeHeardBy(this,affect.target()))
						{
							tell(affect.target().name()+" can't hear you!");
							return false;
						}
						else
						if((!((affect.target() instanceof MOB)
						&&(((MOB)affect.target()).getLeigeID().equals(Name()))))
						&&(!affect.target().Name().equals(getLeigeID())))
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
				}
				else
				if(getWorshipCharID().length()==0)
				{
					tell("You aren't worshipping anyone!");
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
				if(affect.target() instanceof Deity)
					break;
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
		&&(!Util.bset(affect.sourceCode(),Affect.MASK_GENERAL))
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
				boolean ok=location().okAffect(this,affect);
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
			if(affect.targetMinor()==Affect.TYP_RETREAT)
			{
				if(curState().getMovement()<25)
				{
					tell("You are too tired.");
					return false;
				}
				if((location()!=null)
				   &&(rangeToTarget()>=location().maxRange()))
				{
					tell("You cannot retreat any further.");
					return false;
				}
				curState().adjMovement(-25,maxState());
				setAtRange(rangeToTarget()+1);
				if(victim!=null)
				{
					victim.setAtRange(rangeToTarget());
					victim.recoverEnvStats();
				}
				recoverEnvStats();
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
						ExternalPlay.remove(this,(Item)tool,false);
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
				&&(!Util.bset(affect.sourceMajor(),Affect.MASK_GENERAL))
				&&((affect.tool()==null)||(!(affect.tool() instanceof Ability))||(!((Ability)affect.tool()).isNowAnAutoEffect())))
				{
					mob.tell("You like yourself too much.");
					if(victim==this) victim=null;
					return false;
				}

				if(!mayIFight(mob))
				{
					mob.tell("You are not allowed to attack "+name()+".");
					mob.setVictim(null);
					if(victim==mob) setVictim(null);
					return false;
				}

				if((!isMonster())
				&&(!mob.isMonster())
				&&(mob.envStats().level()>envStats().level()+CommonStrings.getPKillLevelDiff()))
				{
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
			case Affect.TYP_THROW:
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
				FullMsg msg=new FullMsg(affect.source(),affect.tool(),null,Affect.MSG_DROP,null);
				if(!location().okAffect(affect.source(),msg))
					return false;
				if((affect.target()!=null)&&(affect.target() instanceof MOB))
				{
					msg=new FullMsg((MOB)affect.target(),affect.tool(),null,Affect.MSG_GET,null);
					if(!location().okAffect(affect.target(),msg))
					{
						mob.tell(affect.target().name()+" cannot seem to accept "+affect.tool().name()+".");
						return false;
					}
				}
				break;
			case Affect.TYP_FOLLOW:
				if(numFollowers()>=maxFollowers())
				{
					mob.tell(name()+" can't accept any more followers.");
					return false;
				}
				break;
			}
		}
		return true;
	}

	public void tell(MOB source, Environmental target, Environmental tool, String msg)
	{
		if(mySession!=null)
			mySession.stdPrintln(source,target,tool,msg);

	}

	public void tell(String msg)
	{
		tell(this,this,null,msg);
	}

	public void affect(Environmental myHost, Affect affect)
	{
		if(getMyDeity()!=null)
		   getMyDeity().affect(this,affect);

		if(charStats!=null)
		{
			charStats().getCurrentClass().affect(this,affect);
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
			case Affect.TYP_PANIC:
				ExternalPlay.flee(mob,"");
				break;
			case Affect.TYP_DEATH:
				if((affect.tool()!=null)&&(affect.tool() instanceof MOB))
					ExternalPlay.justDie((MOB)affect.tool(),this);
				else
					ExternalPlay.justDie(null,this);
				tell(this,affect.target(),affect.tool(),affect.sourceMessage());
				break;
			case Affect.TYP_REBUKE:
				if(((affect.target()==null)&&(getLeigeID().length()>0))
				||((affect.target()!=null)&&(affect.target().Name().equals(getLeigeID()))))
					setLeigeID("");
				tell(this,affect.target(),affect.tool(),affect.sourceMessage());
				break;
			case Affect.TYP_SERVE:
				if((affect.target()!=null)&&(!(affect.target() instanceof Deity)))
					setLeigeID(affect.target().Name());
				tell(this,affect.target(),affect.tool(),affect.sourceMessage());
				break;
			case Affect.TYP_EXAMINESOMETHING:
				if((Sense.canBeSeenBy(this,mob))&&(affect.amITarget(this)))
				{
					StringBuffer myDescription=new StringBuffer("");
					if(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
						myDescription.append(ID()+"\n\rRejuv:"+baseEnvStats().rejuv()+"\n\rAbile:"+baseEnvStats().ability()+"\n\rLevel:"+baseEnvStats().level()+"\n\rMisc : "+text()+"\n\r"+description()+"\n\rRoom :'"+((getStartRoom()==null)?"null":getStartRoom().roomID())+"\n\r");
					if(!isMonster())
					{
						String levelStr=charStats().displayClassLevel(this,false);
						myDescription.append(name()+" the "+charStats().raceName()+" is a "+levelStr+".\n\r");
					}
					if(envStats().height()>0)
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
				tell(this,affect.target(),affect.tool(),affect.sourceMessage());
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
				tell(this,affect.target(),affect.tool(),affect.sourceMessage());
				}
				break;
			case Affect.TYP_QUIT:
				if(mob.isInCombat())
				{
					mob.getVictim().resetToMaxState();
					ExternalPlay.flee(mob,"NOWHERE");
					mob.makePeace();
				}
				tell(affect.source(),affect.target(),affect.tool(),affect.sourceMessage());
				break;
			case Affect.TYP_STAND:
				{
				int oldDisposition=mob.baseEnvStats().disposition();
				oldDisposition=oldDisposition&(Integer.MAX_VALUE-EnvStats.IS_SLEEPING-EnvStats.IS_SNEAKING-EnvStats.IS_SITTING);
				mob.baseEnvStats().setDisposition(oldDisposition);
				mob.recoverEnvStats();
				mob.recoverCharStats();
				mob.recoverMaxState();
				tell(this,affect.target(),affect.tool(),affect.sourceMessage());
				}
				break;
			case Affect.TYP_RECALL:
				if((affect.target()!=null) && (affect.target() instanceof Room) && (location() != affect.target()))
				{
					tell(affect.source(),null,affect.tool(),affect.targetMessage());
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
					tell(affect.source(),affect.target(),affect.tool(),affect.sourceMessage());
				}
				break;
			case Affect.TYP_NOFOLLOW:
				setFollowing(null);
				tell(affect.source(),affect.target(),affect.tool(),affect.sourceMessage());
				break;
			default:
				// you pretty much always know what you are doing, if you can do it.
				tell(affect.source(),affect.target(),affect.tool(),affect.sourceMessage());
				break;
			}
		}
		else
		if((affect.targetCode()!=Affect.NO_EFFECT)&&(affect.amITarget(this)))
		{
			int targetMajor=affect.targetMajor();

			// malicious by itself is pure pain
			if(Util.bset(affect.targetCode(),Affect.MASK_HURT))
			{
				int dmg=affect.targetCode()-Affect.MASK_HURT;
				if(dmg>0)
				{
					if((!curState().adjHitPoints(-dmg,maxState()))&&(location()!=null))
						ExternalPlay.postDeath(affect.source(),this,affect);
					else
					if((curState().getHitPoints()<getWimpHitPoint())&&(isInCombat()))
						ExternalPlay.postPanic(this,affect);
				}
			}
			else
			if(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
			{
				if((!isInCombat())
				&&(!amDead)
				&&(location().isInhabitant((MOB)affect.source())))
				{
					establishRange(this,affect.source(),affect.tool());
					setVictim(affect.source());
				}
				if((isInCombat())&&(!amDead))
				{
					if(affect.targetMinor()==Affect.TYP_WEAPONATTACK)
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
					else
					if((affect.tool()!=null)
					&&(affect.tool() instanceof Weapon))
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
				location().send(this,msg);
				msg=new FullMsg((MOB)affect.target(),affect.tool(),null,Affect.MSG_GET,null);
				location().send(this,msg);
			}
			else
			if((affect.targetMinor()==Affect.TYP_EXAMINESOMETHING)
			&&(Sense.canBeSeenBy(this,mob)))
			{
				StringBuffer myDescription=new StringBuffer("");
				if(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
					myDescription.append(Name()+"\n\rRejuv:"+baseEnvStats().rejuv()+"\n\rAbile:"+baseEnvStats().ability()+"\n\rLevel:"+baseEnvStats().level()+"\n\rMisc :'"+text()+"\n\rRoom :'"+((getStartRoom()==null)?"null":getStartRoom().roomID())+"\n\r"+description()+"\n\r");
				if(!isMonster())
				{
					String levelStr=charStats().displayClassLevel(this,false);
					myDescription.append(name()+" the "+charStats().raceName()+" is a "+levelStr+".\n\r");
				}
				if(envStats().height()>0)
					myDescription.append(charStats().HeShe()+" is "+envStats().height()+" inches tall and weighs "+baseEnvStats().weight()+" pounds.\n\r");
				myDescription.append(healthText()+"\n\r\n\r");
				myDescription.append(description()+"\n\r\n\r");
				myDescription.append(charStats().HeShe()+" is wearing:\n\r"+ExternalPlay.getEquipment(affect.source(),this));
				mob.tell(myDescription.toString());
			}
			else
			if((affect.targetMinor()==Affect.TYP_REBUKE)
			&&(affect.source().Name().equals(getLeigeID())))
				setLeigeID("");
			else
			if(Util.bset(targetMajor,affect.MASK_CHANNEL))
			{
				if((playerStats()!=null)
				&&(!Util.isSet(playerStats().getChannelMask(),((affect.targetCode()-affect.MASK_CHANNEL)-Affect.TYP_CHANNEL))))
					tell(affect.source(),affect.target(),affect.tool(),affect.targetMessage());
			}

			if((Util.bset(targetMajor,Affect.MASK_SOUND))
			&&(canhearsrc)&&(!asleep))
			{
				if((affect.targetMinor()==Affect.TYP_SPEAK)
				 &&(affect.source()!=null)
				 &&(playerStats()!=null))
					playerStats().setReplyTo(affect.source());
				tell(affect.source(),affect.target(),affect.tool(),affect.targetMessage());
			}
			else
			if(((Util.bset(targetMajor,Affect.MASK_EYES))
			  ||(Util.bset(affect.targetCode(),Affect.MASK_HURT))
			  ||(Util.bset(targetMajor,Affect.MASK_GENERAL)))
			&&(!asleep)&&(canseesrc))
				tell(affect.source(),affect.target(),affect.tool(),affect.targetMessage());
			else
			if(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
				tell(affect.source(),affect.target(),affect.tool(),affect.targetMessage());
			else
			if(((Util.bset(targetMajor,Affect.MASK_HANDS))
				||(Util.bset(targetMajor,Affect.MASK_MOVE))
				||((Util.bset(targetMajor,Affect.MASK_MOUTH))
				   &&(!Util.bset(targetMajor,Affect.MASK_SOUND))))
			&&(!asleep)&&((canhearsrc)||(canseesrc)))
				tell(affect.source(),affect.target(),affect.tool(),affect.targetMessage());
		}
		else
		if((affect.othersCode()!=Affect.NO_EFFECT)
		&&(!affect.amISource(this))
		&&(!affect.amITarget(this)))
		{
			int othersMajor=affect.othersMajor();
			int othersMinor=affect.othersMinor();

			if(Util.bset(affect.othersCode(),Affect.MASK_MALICIOUS)&&(affect.target() instanceof MOB))
				fightingFollowers((MOB)affect.target(),affect.source());

			if((othersMinor==Affect.TYP_ENTER) // exceptions to movement
			||(othersMinor==Affect.TYP_FLEE)
			||(othersMinor==Affect.TYP_LEAVE))
			{
				if(((!asleep)||(affect.othersMinor()==Affect.TYP_ENTER))
				&&(Sense.canSenseMoving(affect.source(),this)))
					tell(affect.source(),affect.target(),affect.tool(),affect.othersMessage());
			}
			else
			if(Util.bset(othersMajor,affect.MASK_CHANNEL))
			{
				if((playerStats()!=null)
				&&(!Util.isSet(playerStats().getChannelMask(),((affect.othersCode()-affect.MASK_CHANNEL)-Affect.TYP_CHANNEL))))
					tell(affect.source(),affect.target(),affect.tool(),affect.othersMessage());
			}
			else
			if((Util.bset(othersMajor,Affect.MASK_SOUND))
			&&(!asleep)
			&&(canhearsrc))
				tell(affect.source(),affect.target(),affect.tool(),affect.othersMessage());
			else
			if(((Util.bset(othersMajor,Affect.MASK_EYES))
			||(Util.bset(othersMajor,Affect.MASK_HANDS))
			||(Util.bset(othersMajor,Affect.MASK_GENERAL)))
			&&((!asleep)&&(canseesrc)))
				tell(affect.source(),affect.target(),affect.tool(),affect.othersMessage());
			else
			if(((Util.bset(othersMajor,Affect.MASK_MOVE))
				||((Util.bset(othersMajor,Affect.MASK_MOUTH))&&(!Util.bset(othersMajor,Affect.MASK_SOUND))))
			&&(!asleep)
			&&((canseesrc)||(canhearsrc)))
				tell(affect.source(),affect.target(),affect.tool(),affect.othersMessage());

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
				I.affect(this,affect);
		}

		for(int i=0;i<numAffects();i++)
		{
			Ability A=(Ability)fetchAffect(i);
			if(A!=null)
				A.affect(this,affect);
		}
	}

	public void affectCharStats(MOB affectedMob, CharStats affectableStats){}

	public int movesSinceLastTick(){return movesSinceTick;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(pleaseDestroy)
			return false;

		if(tickID==Host.MOB_TICK)
		{
			movesSinceTick=0;
			if(amDead)
			{
				if(isMonster())
					if((envStats().rejuv()<Integer.MAX_VALUE)
					&&(baseEnvStats().rejuv()>0))
					{
						envStats().setRejuv(envStats().rejuv()-1);
						if(envStats().rejuv()<0)
						{
							bringToLife(getStartRoom(),true);
							location().showOthers(this,null,Affect.MSG_OK_ACTION,"<S-NAME> appears!");
						}
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
				curState().recoverTick(this,maxState);
				curState().expendEnergy(this,maxState,false);
				if(!Sense.canBreathe(this))
				{
					location().show(this,this,Affect.MSG_OK_VISUAL,("^Z<S-NAME> can't breathe!^.^?")+CommonStrings.msp("choke.wav",10));
					ExternalPlay.postDamage(this,this,null,(int)Math.round(Util.mul(Math.random(),baseEnvStats().level()+2)),Affect.NO_EFFECT,-1,null);
				}
				if(isInCombat())
				{
					peaceTime=0;
					if(Util.bset(getBitmap(),MOB.ATT_AUTODRAW))
					 	ExternalPlay.drawIfNecessary(this,false);

					Item weapon=this.fetchWieldedItem();
					double curSpeed=Math.floor(speeder);
					speeder+=envStats().speed();
					int numAttacks=(int)Math.round(Math.floor(speeder-curSpeed));
					if(Sense.aliveAwakeMobile(this,true))
					{
						for(int s=0;s<numAttacks;s++)
						{
							if((!amDead())
							&&(curState().getHitPoints()>0)
							&&(isInCombat())
							&&((s==0)||(!Sense.isSitting(this))))
							{
								if((weapon!=null)&&(weapon.amWearingAt(Item.INVENTORY)))
									weapon=this.fetchWieldedItem();
								if((!Util.bset(getBitmap(),MOB.ATT_AUTOMELEE)))
									ExternalPlay.postAttack(this,victim,weapon);
								else
								{
									boolean inminrange=(rangeToTarget()>=minRange(weapon));
									boolean inmaxrange=(rangeToTarget()<=maxRange(weapon));
									if((!inminrange)&&(curState().getMovement()>=25))
									{
										FullMsg msg=new FullMsg(this,victim,Affect.MSG_RETREAT,"<S-NAME> retreat(s) before <T-NAME>.");
										if(location().okAffect(this,msg))
											location().send(this,msg);
									}
									else
									if((weapon!=null)&&inminrange&&inmaxrange)
										ExternalPlay.postAttack(this,victim,weapon);
								}
							}
							else
								break;
						}

						if(Dice.rollPercentage()>(charStats().getStat(CharStats.CONSTITUTION)*4))
							curState().adjMovement(-1,maxState());
					}

					if(!isMonster())
					{
						MOB target=this.getVictim();
						if((target!=null)&&(!target.amDead())&&(Sense.canBeSeenBy(target,this)))
							session().print(target.healthText()+"\n\r\n\r");
					}
				}
				else
				{
					speeder=0.0;
					peaceTime+=Host.TICK_TIME;
					if(Util.bset(getBitmap(),MOB.ATT_AUTODRAW)
					&&(peaceTime>=SHEATH_TIME)
					&&(Sense.aliveAwakeMobile(this,true)))
						ExternalPlay.sheathIfPossible(this);
				}
				if(!isMonster())
				{
					if(Sense.isSleeping(this))
						curState().adjFatigue(-CharState.REST_PER_TICK,maxState());
					else
						curState().adjFatigue(Host.TICK_TIME,maxState());
				}
				
				if((riding()!=null)&&(CoffeeUtensils.roomLocation(riding())!=location()))
					setRiding(null);
				if((!isMonster())&&(((++minuteCounter)*Host.TICK_TIME)>60000))
				{
					minuteCounter=0;
					setAgeHours(AgeHours+1);
					if(AgeHours>60000)
					{
						if(((AgeHours%120)==0)&&(Dice.rollPercentage()==1))
						{
							Ability A=CMClass.getAbility("Disease_Cancer");
							if((A!=null)&&(fetchAffect(A.ID())==null))
								A.invoke(this,this,true);
						}
						else
						if(((AgeHours%1200)==0)&&(Dice.rollPercentage()<25))
						{
							Ability A=CMClass.getAbility("Disease_Arthritis");
							if((A!=null)&&(fetchAffect(A.ID())==null))
								A.invoke(this,this,true);
						}
					}
				}
			}

			int a=0;
			while(a<numAffects())
			{
				Ability A=fetchAffect(a);
				if(A!=null)
				{
					int s=affects.size();
					if(!A.tick(ticking,tickID))
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
				if(B!=null) B.tick(ticking,tickID);
			}

			charStats().getCurrentClass().tick(ticking,tickID);
			charStats().getMyRace().tick(ticking,tickID);
		}
		lastTickedDateTime=System.currentTimeMillis();
		return !pleaseDestroy;
	}

	public boolean isMonster(){	return (mySession==null);}
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public boolean isASysOp(Room of)
	{
		if(baseCharStats()==null) return false;
		if(baseCharStats().getClassLevel("Archon")>=0)
			return true;
		if(of==null) return false;
		if(of.getArea()==null) return false;
		if(of.getArea().amISubOp(Username))
			return true;
		return false;
	}

	public void confirmWearability()
	{
		Race R=charStats().getMyRace();
		for(int i=0;i<inventorySize();i++)
		{
			Item item=fetchInventory(i);
			if((item!=null)&&(!item.amWearingAt(Item.INVENTORY)))
			{
				long oldCode=item.rawWornCode();
				item.unWear();
				int msgCode=Affect.MSG_WEAR;
				if((oldCode&Item.WIELD)>0)
					msgCode=Affect.MSG_WIELD;
				else
				if((oldCode&Item.HELD)>0)
					msgCode=Affect.MSG_HOLD;
				FullMsg msg=new FullMsg(this,item,null,Affect.NO_EFFECT,null,msgCode,null,Affect.NO_EFFECT,null);
				if((R.okAffect(this,msg))&&(item.okAffect(item,msg)))
				   item.wearAt(oldCode);
			}
		}
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
		Item item=(Item)CoffeeUtensils.fetchAvailableItem(inventory,itemName,null,Item.WORN_REQ_ANY,true);
		if(item==null) item=(Item)CoffeeUtensils.fetchAvailableItem(inventory,itemName,null,Item.WORN_REQ_ANY,false);
		return item;
	}
	public Item fetchInventory(Item goodLocation, String itemName)
	{
		Item item=(Item)CoffeeUtensils.fetchAvailableItem(inventory,itemName,goodLocation,Item.WORN_REQ_ANY,true);
		if(item==null) item=(Item)CoffeeUtensils.fetchAvailableItem(inventory,itemName,goodLocation,Item.WORN_REQ_ANY,false);
		return item;
	}
	public Item fetchCarried(Item goodLocation, String itemName)
	{
		Item item=(Item)CoffeeUtensils.fetchAvailableItem(inventory,itemName,goodLocation,Item.WORN_REQ_UNWORNONLY,true);
		if(item==null) item=(Item)CoffeeUtensils.fetchAvailableItem(inventory,itemName,goodLocation,Item.WORN_REQ_UNWORNONLY,false);
		return item;
	}
	public Item fetchWornItem(String itemName)
	{
		Item item=(Item)CoffeeUtensils.fetchAvailableItem(inventory,itemName,null,Item.WORN_REQ_WORNONLY,true);
		if(item==null) item=(Item)CoffeeUtensils.fetchAvailableItem(inventory,itemName,null,Item.WORN_REQ_WORNONLY,false);
		return item;
	}
	public void addFollower(MOB follower)
	{
		if((follower!=null)&&(!followers.contains(follower)))
		{
			followers.addElement(follower);
		}
	}

	public void delFollower(MOB follower)
	{
		if((follower!=null)&&(followers.contains(follower)))
		{
			followers.removeElement(follower);
		}
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
	public boolean willFollowOrdersOf(MOB mob)
	{
		if(mob.isASysOp(mob.location())
		||(amFollowing()==mob)
		||(getLeigeID().equals(mob.Name()))
		||((getClanID().length()>0)
			&&(getClanID().equals(mob.getClanID()))
			&&((mob.getClanRole()==Clan.POS_LEADER)
				||(mob.getClanRole()==Clan.POS_BOSS)))
		||(ExternalPlay.doesOwnThisProperty(mob,getStartRoom())))
			return true;
		return false;
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

	public Hashtable getRideBuddies(Hashtable list)
	{
		if(list==null) return list;
		if(list.get(this)==null) list.put(this,this);
		if(riding()!=null)
			riding().getRideBuddies(list);
		return list;
	}

	public Hashtable getGroupMembers(Hashtable list)
	{
		if(list==null) return list;
		if(list.get(this)==null) list.put(this,this);
		if(amFollowing()!=null)
			amFollowing().getGroupMembers(list);
		for(int f=0;f<numFollowers();f++)
		{
			MOB follower=fetchFollower(f);
			if((follower!=null)&&(list.get(follower)==null))
				follower.getGroupMembers(list);
		}
		return list;
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
	public boolean hasAbilityEvoker(String word)
	{
		try
		{
			for(int a=0;a<abilities.size();a++)
			{
				Ability A=(Ability)abilities.elementAt(a);
				for(int s=0;s<A.triggerStrings().length;s++)
				{
					if(A.triggerStrings()[s].startsWith(word))
						return true;
				}
			}
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return false;
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
			if((A!=null)
			&&((A.ID().equalsIgnoreCase(ID))||(A.Name().equalsIgnoreCase(ID))))
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
	public Behavior fetchBehavior(String ID)
	{
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equalsIgnoreCase(ID)))
				return B;
		}
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

	public void giveItem(Item thisContainer)
	{
		// caller is responsible for recovering any env
		// stat changes!
		if(Sense.isHidden(thisContainer))
			thisContainer.baseEnvStats().setDisposition(thisContainer.baseEnvStats().disposition()&((int)EnvStats.ALLMASK-EnvStats.IS_HIDDEN));

		// ensure its out of its previous place
		Environmental owner=location();
		if(thisContainer.owner()!=null)
		{
			owner=thisContainer.owner();
			if(thisContainer.owner() instanceof Room)
				((Room)thisContainer.owner()).delItem(thisContainer);
			else
			if(thisContainer.owner() instanceof MOB)
				((MOB)thisContainer.owner()).delInventory(thisContainer);
		}
		location().delItem(thisContainer);
		
		thisContainer.unWear();
		
		if(!isMine(thisContainer))
			addInventory(thisContainer);
		thisContainer.recoverEnvStats();
		
		boolean nothingDone=true;
		do
		{
			nothingDone=true;
			if(owner instanceof Room)
			{
				Room R=(Room)owner;
				for(int i=0;i<R.numItems();i++)
				{
					Item thisItem=R.fetchItem(i);
					if((thisItem!=null)&&(thisItem.container()==thisContainer))
					{
						giveItem(thisItem);
						nothingDone=false;
						break;
					}
				}
			}
			else
			if(owner instanceof MOB)
			{
				MOB M=(MOB)owner;
				for(int i=0;i<M.inventorySize();i++)
				{
					Item thisItem=M.fetchInventory(i);
					if((thisItem!=null)&&(thisItem.container()==thisContainer))
					{
						giveItem(thisItem);
						nothingDone=false;
						break;
					}
				}
			}
		}while(!nothingDone);
	}
	private void fightingFollowers(MOB target, MOB source)
	{
		if((source==null)||(target==null)) return;
		if(source==target) return;
		if((target==this)||(source==this)) return;
		if((target.location()!=location())||(target.location()!=source.location()))
			return;
		if((Util.bset(getBitmap(),MOB.ATT_AUTOASSIST))) return;
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

	protected static String[] CODES={"CLASS","LEVEL","ABILITY","TEXT"};
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return ""+baseEnvStats().level();
		case 2: return ""+baseEnvStats().ability();
		case 3: return text();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: baseEnvStats().setLevel(Util.s_int(val)); break;
		case 2: baseEnvStats().setAbility(Util.s_int(val)); break;
		case 3: setMiscText(val); break;
		}
	}
	public String[] getStatCodes(){return CODES;}
	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdMOB)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
}
