package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class Masonry extends CommonSkill
{
	public String ID() { return "Masonry"; }
	public String name(){ return "Masonry";}
	private static final String[] triggerStrings = {"MASONRY"};
	public String[] triggerStrings(){return triggerStrings;}

	private final static int BUILD_WALL=0;
	private final static int BUILD_ROOF=1;
	private final static int BUILD_ARCH=2;
	private final static int BUILD_DEMOLISH=3;
	private final static int BUILD_TITLE=4;
	private final static int BUILD_DESC=5;
	private final static int BUILD_MONUMENT=6;
	private final static int BUILD_WINDOW=7;
	private final static int BUILD_CRAWLWAY=8;
	
	private final static String[] names={"Wall","Roof","Archway","Demolish","Title","Description","Druidic Monument","Window","Crawlway"};
	private final static int[] woodReq={250,500,200,0,0,0,1000,100,500};

	private Room room=null;
	private int dir=-1;
	private int doingCode=-1;
	private boolean messedUp=false;
	private int workingOn=-1;
	private static boolean mapped=false;
	private String designTitle="";
	private String designDescription="";

	public Masonry()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",5,ID(),false);}
	}
	public Environmental newInstance(){	return new Masonry();}

	public Exit generify(Exit E)
	{
		Exit E2=CMClass.getExit("GenExit");
		E2.setName(E.name());
		E2.setDisplayText(E.displayText());
		E2.setDescription(E.description());
		E2.setDoorsNLocks(E.hasADoor(),E.isOpen(),E.defaultsClosed(),E.hasALock(),E.isLocked(),E.defaultsLocked());
		E2.setBaseEnvStats(E.baseEnvStats().cloneStats());
		E2.setExitParams(E.doorName(),E.closeWord(),E.openWord(),E.closedText());
		E2.setKeyName(E.keyName());
		E2.setOpenDelayTicks(E.openDelayTicks());
		E2.setReadable(E.isReadable());
		E2.setReadableText(E.readableText());
		E2.setTemporaryDoorLink(E.temporaryDoorLink());
		E2.recoverEnvStats();
		return E2;
	}
	
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if(!aborted)
				{
					if((messedUp)&&(room!=null))
					switch(doingCode)
					{
					case BUILD_ROOF:
						commonTell(mob,"You've ruined the frame and roof!");
						break;
					case BUILD_WALL:
						commonTell(mob,"You've ruined the wall!");
						break;
					case BUILD_ARCH:
						commonTell(mob,"You've ruined the archway!");
						break;
					case BUILD_TITLE:
						commonTell(mob,"You've ruined the titling!");
						break;
					case BUILD_DESC:
						commonTell(mob,"You've ruined the describing!");
						break;
					case BUILD_MONUMENT:
						commonTell(mob,"You've ruined the druidic monument!");
						break;
					case BUILD_WINDOW:
						commonTell(mob,"You've ruined the window!");
						break;
					case BUILD_CRAWLWAY:
						commonTell(mob,"You've ruined the crawlway!");
						break;
					case BUILD_DEMOLISH:
					default:
						commonTell(mob,"You've failed to demolish!");
						break;
					}
					else
					switch(doingCode)
					{
					case BUILD_ROOF:
						{
							Room R=CMClass.getLocale("StoneRoom");
							R.setRoomID(room.roomID());
							R.setDisplayText(room.displayText());
							R.setDescription(room.description());
							R.setArea(room.getArea());
							for(int a=room.numAffects()-1;a>=0;a--)
							{
								Ability A=room.fetchAffect(a);
								if(A!=null){
									room.delAffect(A);
									R.addAffect(A);
								}
							}
							for(int i=room.numItems()-1;i>=0;i--)
							{
								Item I=room.fetchItem(i);
								if(I!=null){
									room.delItem(I);
									R.addItem(I);
								}
							}
							for(int m=room.numInhabitants()-1;m>=0;m--)
							{
								MOB M=room.fetchInhabitant(m);
								if(M!=null){
									room.delInhabitant(M);
									R.addInhabitant(M);
									M.setLocation(R);
								}
							}
							ExternalPlay.deleteTick(room,-1);
							CMMap.delRoom(room);
							CMMap.addRoom(R);
							for(int d=0;d<R.rawDoors().length;d++)
							{
								if((R.rawDoors()[d]==null)
								||(R.rawDoors()[d].roomID().length()>0))
									R.rawDoors()[d]=room.rawDoors()[d];
							}
							for(int d=0;d<R.rawExits().length;d++)
							{
								if((R.rawDoors()[d]==null)
								||(R.rawDoors()[d].roomID().length()>0))
									R.rawExits()[d]=room.rawExits()[d];
							}
							R.startItemRejuv();
							for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
							{
								Room R2=(Room)r.nextElement();
								for(int d=0;d<R2.rawDoors().length;d++)
									if(R2.rawDoors()[d]==room)
									{
										R2.rawDoors()[d]=R;
										if(R2 instanceof GridLocale)
											((GridLocale)R2).buildGrid();
									}
							}
							R.getArea().clearMap();
							R.getArea().fillInAreaRoom(R);
							ExternalPlay.DBUpdateRoom(R);
							ExternalPlay.DBUpdateExits(R);
						}
						break;
					case BUILD_ARCH:
						{
							Exit x=CMClass.getExit("GenExit");
							Exit x2=CMClass.getExit("GenExit");
							x.setName("an archway");
							x.setDescription("A majestic archway towers above you.");
							x2.setName("an archway");
							x2.setDescription("A majestic archway towers above you.");
							room.rawExits()[dir]=x;
							if(room.rawDoors()[dir]!=null)
							{
								room.rawDoors()[dir].rawExits()[Directions.getOpDirectionCode(dir)]=x2;
								ExternalPlay.DBUpdateExits(room.rawDoors()[dir]);
							}
							ExternalPlay.DBUpdateExits(room);
						}
						break;
					case BUILD_WALL:
						{
							room.rawExits()[dir]=null;
							if(room.rawDoors()[dir]!=null)
							{
								room.rawDoors()[dir].rawExits()[Directions.getOpDirectionCode(dir)]=null;
								ExternalPlay.DBUpdateExits(room.rawDoors()[dir]);
							}
							ExternalPlay.DBUpdateExits(room);
						}
						break;
					case BUILD_TITLE:
						{
							room.setDisplayText(designTitle);
							ExternalPlay.DBUpdateRoom(room);
						}
						break;
					case BUILD_DESC:
						{
							if(workingOn>=0)
							{
								Exit E=room.getExitInDir(workingOn);
								if((!E.isGeneric())&&(room.rawExits()[workingOn]==E))
								{
									E=generify(E);
									room.rawExits()[workingOn]=E;
								}
								E.setDescription(designDescription);
								ExternalPlay.DBUpdateExits(room);
							}
							else
							{
								room.setDescription(designDescription);
								ExternalPlay.DBUpdateRoom(room);
							}
						}
						break;
					case BUILD_MONUMENT:
						{
							Item I=CMClass.getItem("DruidicMonument");
							room.addItem(I);
							I.setDispossessionTime(0);
						}
						break;
					case BUILD_CRAWLWAY:
						{
							if(workingOn>=0)
							{
								Exit E=room.getExitInDir(workingOn);
								if((!E.isGeneric())&&(room.rawExits()[workingOn]==E))
								{
									E=generify(E);
									room.rawExits()[workingOn]=E;
								}
								E.baseEnvStats().setDisposition(E.baseEnvStats().disposition()|EnvStats.IS_SITTING);
								ExternalPlay.DBUpdateExits(room);
							}
						}
						break;
					case BUILD_WINDOW:
						{
							if(workingOn>=0)
							{
								Exit E=room.getExitInDir(workingOn);
								if((!E.isGeneric())&&(room.rawExits()[workingOn]==E))
								{
									E=generify(E);
									room.rawExits()[workingOn]=E;
								}
								Room R2=room.getRoomInDir(workingOn);
								if(R2!=null)
								{
									Ability A=CMClass.getAbility("Prop_RoomView");
									if(A!=null)
									{
										A.setMiscText(CMMap.getExtendedRoomID(R2));
										E.addNonUninvokableAffect(A);
									}
								}
								ExternalPlay.DBUpdateExits(room);
							}
						}
						break;
					case BUILD_DEMOLISH:
					default:
						{
							if(dir<0)
							{
								Room R=CMClass.getLocale("Plains");
								R.setRoomID(room.roomID());
								R.setDisplayText(room.displayText());
								R.setDescription(room.description());
								R.setArea(room.getArea());
								for(int a=room.numAffects()-1;a>=0;a--)
								{
									Ability A=room.fetchAffect(a);
									if(A!=null){
										room.delAffect(A);
										R.addAffect(A);
									}
								}
								for(int i=room.numItems()-1;i>=0;i--)
								{
									Item I=room.fetchItem(i);
									if(I!=null){
										room.delItem(I);
										R.addItem(I);
									}
								}
								for(int m=room.numInhabitants()-1;m>=0;m--)
								{
									MOB M=room.fetchInhabitant(m);
									if(M!=null){
										room.delInhabitant(M);
										R.addInhabitant(M);
										M.setLocation(R);
									}
								}
								ExternalPlay.deleteTick(room,-1);
								CMMap.delRoom(room);
								CMMap.addRoom(R);
								for(int d=0;d<R.rawDoors().length;d++)
									R.rawDoors()[d]=room.rawDoors()[d];
								for(int d=0;d<R.rawExits().length;d++)
									R.rawExits()[d]=room.rawExits()[d];
								R.startItemRejuv();
								for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
								{
									Room R2=(Room)r.nextElement();
									for(int d=0;d<R2.rawDoors().length;d++)
										if(R2.rawDoors()[d]==room)
										{
											R2.rawDoors()[d]=R;
											if(R2 instanceof GridLocale)
												((GridLocale)R2).buildGrid();
										}
								}
								R.getArea().clearMap();
								R.getArea().fillInAreaRoom(R);
								ExternalPlay.DBUpdateRoom(R);
								ExternalPlay.DBUpdateExits(R);
							}
							else
							{
								room.rawExits()[dir]=CMClass.getExit("Open");
								if(room.rawDoors()[dir]!=null)
								{
									room.rawDoors()[dir].rawExits()[Directions.getOpDirectionCode(dir)]=CMClass.getExit("Open");
									ExternalPlay.DBUpdateExits(room.rawDoors()[dir]);
								}
								ExternalPlay.DBUpdateExits(room);
							}
						}
						break;
					}
				}
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()==0)
		{
			commonTell(mob,"What kind of masonry, where? Try Masonry list.");
			return false;
		}
		String str=(String)commands.elementAt(0);
		if(("LIST").startsWith(str.toUpperCase()))
		{
			StringBuffer buf=new StringBuffer(Util.padRight("Item",20)+" Stone required\n\r");
			for(int r=0;r<names.length;r++)
			{
				if((r!=BUILD_MONUMENT)||(mob.charStats().getCurrentClass().baseClass().equals("Druid")))
					buf.append(Util.padRight(names[r],20)+" "+woodReq[r]+"\n\r");
			}
			commonTell(mob,buf.toString());
			return true;
		}

		designTitle="";
		designDescription="";
		String startStr=null;
		int completion=15;
		doingCode=-1;
		dir=-1;

		room=null;
		messedUp=false;

		String firstWord=(String)commands.firstElement();
		for(int r=0;r<names.length;r++)
		{
			if((r!=BUILD_MONUMENT)||(mob.charStats().getCurrentClass().baseClass().equals("Druid")))
				if(names[r].toUpperCase().startsWith(firstWord.toUpperCase()))
					doingCode=r;
		}
		if(doingCode<0)
		{
			commonTell(mob,"'"+firstWord+"' is not a valid masonry project.  Try LIST.");
			return false;
		}
		String dirName=(String)commands.lastElement();
		dir=Directions.getGoodDirectionCode(dirName);
		if((doingCode==BUILD_DEMOLISH)&&(dirName.equalsIgnoreCase("roof")))
		   dir=-1;
		else
		if(((dir<0)||(dir>3))
		   &&(doingCode!=BUILD_ROOF)
		   &&(doingCode!=BUILD_DESC)
		   &&(doingCode!=BUILD_MONUMENT)
		   &&(doingCode!=BUILD_TITLE))
		{
			commonTell(mob,"A valid direction in which to build must also be specified.");
			return false;
		}

		int woodRequired=woodReq[doingCode];
		if(doingCode==BUILD_TITLE)
		{
			String title=Util.combine(commands,1);
			if(title.length()==0)
			{
				commonTell(mob,"A title must be specified.");
				return false;
			}
			for(Enumeration r=mob.location().getArea().getMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.displayText().equalsIgnoreCase(title))
				{
					commonTell(mob,"That title has already been taken.  Choose another.");
					return false;
				}
			}
			designTitle=title;
		}
		else
		if(doingCode==BUILD_DESC)
		{
			if(commands.size()<3)
			{
				commonTell(mob,"You must specify an exit direction or room, followed by a description for it.");
				return false;
			}
			workingOn=-1;
			if(Directions.getGoodDirectionCode((String)commands.elementAt(1))>=0)
			{
				int dir=Directions.getGoodDirectionCode((String)commands.elementAt(1));
				
				if(mob.location().getExitInDir(dir)==null)
				{
					commonTell(mob,"There is no exit "+Directions.getInDirectionName(dir)+" to describe.");
					return false;
				}
				workingOn=dir;
				commands.removeElementAt(1);
			}
			else
			if(!((String)commands.elementAt(1)).equalsIgnoreCase("room"))
			{
				commonTell(mob,"'"+((String)commands.elementAt(1))+"' is neither the word room, nor an exit direction.");
				return false;
			}
			else
				commands.removeElementAt(1);
			designDescription=Util.combine(commands,2);
		}

		Item firstWood=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_ROCK);
		int foundWood=0;
		if(firstWood!=null)
			foundWood=findNumberOfResource(mob.location(),firstWood.material());
		if((foundWood==0)&&(woodRequired>0))
		{
			commonTell(mob,"There is no stone here to make anything from!  It might need to put it down first.");
			return false;
		}
		if(foundWood<woodRequired)
		{
			commonTell(mob,"You need "+woodRequired+" pounds of "+EnvResource.RESOURCE_DESCS[(firstWood.material()&EnvResource.RESOURCE_MASK)].toLowerCase()+" to construct a "+names[doingCode].toLowerCase()+".  There is not enough here.  Are you sure you set it all on the ground first?");
			return false;
		}

		boolean canBuild=(ExternalPlay.doesOwnThisProperty(mob,mob.location())
		   ||((mob.amFollowing()!=null)&&(ExternalPlay.doesOwnThisProperty(mob.amFollowing(),mob.location()))));
		if(!canBuild)
		{
			if((doingCode!=BUILD_ROOF)
			   &&(doingCode!=BUILD_TITLE)
			   &&(doingCode!=BUILD_MONUMENT)
			   &&(doingCode!=BUILD_DESC)
			   &&(dir>=0))
			{
				Room R=mob.location().getRoomInDir(dir);
				if((R!=null)
				&&((ExternalPlay.doesOwnThisProperty(mob,R))
					||((mob.amFollowing()!=null)&&(ExternalPlay.doesOwnThisProperty(mob.amFollowing(),R)))))
					canBuild=true;
			}
		}
		if(!canBuild)
		{
			commonTell(mob,"You'll need the permission of the owner to do that.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		room=mob.location();
		int woodDestroyed=woodRequired;
		if(woodRequired>0)
		for(int i=mob.location().numItems()-1;i>=0;i--)
		{
			Item I=mob.location().fetchItem(i);
			if((I instanceof EnvResource)
			&&(I.container()==null)
			&&(I.material()==firstWood.material())
			&&((--woodDestroyed)>=0))
				I.destroy();
		}

		switch(doingCode)
		{
		case BUILD_ROOF:
			verb="building a frame and roof";
			break;
		case BUILD_WALL:
			verb="building the "+Directions.getDirectionName(dir)+" wall";
			break;
		case BUILD_ARCH:
			verb="building the "+Directions.getDirectionName(dir)+" archway";
			break;
		case BUILD_TITLE:
			verb="giving this place a title";
			break;
		case BUILD_DESC:
			verb="giving this place a description";
			break;
		case BUILD_MONUMENT:
			verb="building a druidic monument";
			break;
		case BUILD_WINDOW:
			verb="building a window "+Directions.getDirectionName(dir);
			break;
		case BUILD_CRAWLWAY:
			verb="building a crawlway "+Directions.getDirectionName(dir);
			break;
		case BUILD_DEMOLISH:
		default:
			if(dir<0)
				verb="demolishing the roof";
			else
				verb="demolishing the "+Directions.getDirectionName(dir)+" wall";
			break;
		}
		messedUp=!profficiencyCheck(0,auto);
		startStr="<S-NAME> start(s) "+verb;
		if(completion<15) completion=15;
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,startStr+".");
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,completion);
		}
		return true;
	}
}
