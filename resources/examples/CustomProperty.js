//CustomProperty.js
/*
 a sample ABIITY class to demonstrate Javascript class creation.
 to load into your game manually, use:
 load ability /resources/examples/CustomProperty.js
*/

//extends com.planet_ink.coffee_mud.Abilities.ThinAbility
//do not change the above line -- it's actual code!

// just like in all other cm classes, an ID method must be defined
// and the value returned must be the class name.
// this should be unique amongst all abilities!
function ID() 
{
	return "CustomProperty";
}

// the name doesn't matter a whole lot really
function name()
{
	return "a custom property";
}

// an alias to the lower-name
function Name()
{
	return name();
}

// Define shortcuts to java singletons and interfaces
var CMLib=Packages.com.planet_ink.coffee_mud.core.CMLib;
var CMSecurity=Packages.com.planet_ink.coffee_mud.core.CMSecurity;
var MOB=Packages.com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
var MozillaContext=Packages.org.mozilla.javascript.Context;
var Ability=Packages.com.planet_ink.coffee_mud.Abilities.interfaces.Ability;
var TriggeredAffect=Packages.com.planet_ink.coffee_mud.Abilities.interfaces.TriggeredAffect;

// here is our tick method.  It is called every 4 seconds when
// this is an effect on mobs. 
function tick(host,tickID)
{
	return this.super$tick(host,tickID);
}

// an aid to in-game editors to know which properties to list
function canAffectCode()
{
	return Ability.CAN_MOBS | Ability.CAN_ITEMS | Ability.CAN_ROOMS | Ability.CAN_EXITS | Ability.CAN_AREAS;
}

// a string to show to the identify spell and so forth
function accountForYourself()
{
	return "Does Something Customizeable, because its JavaScript!";
}

// if this was a triggered effect, you'd want to put the proper flags here
function triggerMask()
{
	return 0;
}

// set any property arguments.  very useful!
function setMiscText(newText)
{
	this.super$setMiscText(newText);
}

// message previewer
// the example code in here can all be replaced .. it
// just to give you ideas.  If you are curious, the code
// blocks players from looting a corpse unless they 
// are PK.
function okMessage(host,msg)
{
	if((msg.target() != null)
	&&(msg.source().isPlayer())
	&&(msg.target().ID().equals("Corpse"))
	&&(msg.isTarget(MozillaContext.toString("GET")))
	&&(msg.target().isPlayerCorpse())
	&&(msg.target().getMobName().length()>0)
	&&(!msg.target().getMobName().equals(msg.source().Name()))
	)
	{
		if(CMSecurity.isAllowed(msg.source(),msg.source().location(),CMSecurity.SecFlag.CMDITEMS))
			return true;
		var ultimateFollowing=msg.source().amUltimatelyFollowing();
		if((msg.source().isMonster())
		&&((ultimateFollowing==null)||(ultimateFollowing.isMonster())))
			return true;
		if(!((msg.source()).isAttributeSet(MOB.Attrib.PLAYERKILL)))
		{
			msg.source().tell(CMLib.lang().L("You can not get that.  You are not a player killer."));
			return false;
		}
		else
		if(!msg.target().getMobPKFlag())
		{
			msg.source().tell(CMLib.lang().L("You can not get that.  @x1 was not a player killer.",msg.target().getMobName()));
			return false;
		}
	}
	return this.super$okMessage(host,msg);
}