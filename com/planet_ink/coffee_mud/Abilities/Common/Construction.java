package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class Construction extends CommonSkill
{
	public String ID() { return "Construction"; }
	public String name(){ return "Construction";}
	private static final String[] triggerStrings = {"CONSTRUCT"};
	public String[] triggerStrings(){return triggerStrings;}

	private final static int BUILD_WALL=0;
	private final static int BUILD_DOOR=1;
	private final static int BUILD_ROOF=2;
	private final static int BUILD_GATE=3;
	private final static int BUILD_FENCE=4;
	private final static int BUILD_DEMOLISH=5;
	private final static int BUILD_TITLE=6;
	private final static int BUILD_DESC=7;
	private final static int BUILD_STAIRS=8;

	private final static String[] names={"Wall","Door","Roof","Gate","Fence","Demolish","Title","Description","Stairs"};
	private final static int[] woodReq={100,125,350,50,50,0,0,0,350};

	private Room room=null;
	private int dir=-1;
	private int doingCode=-1;
	private boolean messedUp=false;
	private static boolean mapped=false;
	private String designTitle="";
	private String designDescription="";

	public Construction()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",10,ID(),false);}
	}
	public Environmental newInstance(){	return new Construction();}

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
					case BUILD_STAIRS:
						commonTell(mob,"You've ruined the attic!");
						break;
					case BUILD_WALL:
						commonTell(mob,"You've ruined the wall!");
						break;
					case BUILD_DOOR:
						commonTell(mob,"You've ruined the door!");
						break;
					case BUILD_FENCE:
						commonTell(mob,"You've ruined the fence!");
						break;
					case BUILD_GATE:
						commonTell(mob,"You've ruined the gate!");
						break;
					case BUILD_TITLE:
						commonTell(mob,"You've ruined the titling!");
						break;
					case BUILD_DESC:
						commonTell(mob,"You've ruined the describing!");
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
							Room R=CMClass.getLocale("WoodRoom");
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
						break;
					case BUILD_STAIRS:
						break;
					case BUILD_WALL:
					case BUILD_FENCE:
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
							room.setDescription(designDescription);
							ExternalPlay.DBUpdateRoom(room);
						}
						break;
					case BUILD_GATE:
						{
							Exit X=CMClass.getExit("GenExit");
							X.setName("a wooden gate");
							X.setDescription("");
							X.setDisplayText("");
							X.setExitParams("gate","close","open","a closed gate");
							X.setDoorsNLocks(true,false,true,false,false,false);
							X.text();
							room.rawExits()[dir]=X;
							if(room.rawDoors()[dir]!=null)
							{
								Exit X2=(Exit)X.copyOf();
								room.rawDoors()[dir].rawExits()[Directions.getOpDirectionCode(dir)]=X2;
								ExternalPlay.DBUpdateExits(room.rawDoors()[dir]);
							}
							ExternalPlay.DBUpdateExits(room);
						}
						break;
					case BUILD_DOOR:
						{
							Exit X=CMClass.getExit("GenExit");
							X.setName("a door");
							X.setDescription("");
							X.setDisplayText("");
							X.setExitParams("door","close","open","a closed door");
							X.setDoorsNLocks(true,false,true,false,false,false);
							X.text();
							room.rawExits()[dir]=X;
							if(room.rawDoors()[dir]!=null)
							{
								Exit X2=(Exit)X.copyOf();
								room.rawDoors()[dir].rawExits()[Directions.getOpDirectionCode(dir)]=X2;
								ExternalPlay.DBUpdateExits(room.rawDoors()[dir]);
							}
							ExternalPlay.DBUpdateExits(room);
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
			commonTell(mob,"Construct what, where? Try Construct list.");
			return false;
		}
		String str=(String)commands.elementAt(0);
		if(("LIST").startsWith(str.toUpperCase()))
		{
			StringBuffer buf=new StringBuffer(Util.padRight("Item",20)+" Wood required\n\r");
			for(int r=0;r<8;r++)
				buf.append(Util.padRight(names[r],20)+" "+woodReq[r]+"\n\r");
			commonTell(mob,buf.toString());
			return true;
		}

		designTitle="";
		designDescription="";
		String startStr=null;
		int completion=35;
		doingCode=-1;
		dir=-1;

		room=null;
		messedUp=false;

		String firstWord=(String)commands.firstElement();
		for(int r=0;r<8;r++)
		{
			if(names[r].toUpperCase().startsWith(firstWord.toUpperCase()))
				doingCode=r;
		}
		if(doingCode<0)
		{
			commonTell(mob,"'"+firstWord+"' is not a valid construction project.  Try LIST.");
			return false;
		}
		String dirName=(String)commands.lastElement();
		dir=Directions.getGoodDirectionCode(dirName);
		if((doingCode==BUILD_DEMOLISH)&&(dirName.equalsIgnoreCase("roof")))
		   dir=-1;
		else
		if(((dir<0)||(dir>3))
		   &&(doingCode!=BUILD_ROOF)&&(doingCode!=BUILD_DESC)&&(doingCode!=BUILD_TITLE))
		{
			commonTell(mob,"A valid direction in which to build must also be specified.");
			return false;
		}

		int woodRequired=woodReq[doingCode];
		if(((mob.location().domainType()&Room.INDOORS)==0)&&(doingCode<BUILD_ROOF))
		{
			commonTell(mob,"That can only be built after a roof, which includes the frame.");
			return false;
		}
		else
		if(((mob.location().domainType()&Room.INDOORS)>0)&&(doingCode>BUILD_ROOF)&&(doingCode<BUILD_DEMOLISH))
		{
			commonTell(mob,"That can only be built outdoors!");
			return false;
		}

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
			String title=Util.combine(commands,1);
			if(title.length()==0)
			{
				commonTell(mob,"A description must be specified.");
				return false;
			}
			designDescription=title;
		}

		Item firstWood=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_WOODEN);
		int foundWood=0;
		if(firstWood!=null)
			foundWood=findNumberOfResource(mob.location(),firstWood.material());
		if((foundWood==0)&&(woodRequired>0))
		{
			commonTell(mob,"There is no wood here to make anything from!  It might need to put it down first.");
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
				I.destroyThis();
		}

		switch(doingCode)
		{
		case BUILD_ROOF:
			verb="building a frame and roof";
			break;
		case BUILD_STAIRS:
			verb="building an attic";
			break;
		case BUILD_WALL:
			verb="building the "+Directions.getDirectionName(dir)+" wall";
			break;
		case BUILD_FENCE:
			verb="building the "+Directions.getDirectionName(dir)+" fence";
			break;
		case BUILD_GATE:
			verb="building the "+Directions.getDirectionName(dir)+" gate";
			break;
		case BUILD_DOOR:
			verb="building the "+Directions.getDirectionName(dir)+" door";
			break;
		case BUILD_TITLE:
			verb="giving this place a title";
			break;
		case BUILD_DESC:
			verb="giving this place a description";
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
		if(completion<25) completion=25;
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,startStr+".");
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,completion);
		}
		return true;
	}
}
