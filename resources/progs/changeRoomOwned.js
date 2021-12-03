var lib=Packages.com.planet_ink.coffee_mud.core.CMLib;
var cmparms=Packages.com.planet_ink.coffee_mud.core.CMParms;
var parms=getParms();
if(parms.length < 1)
	mob().tell("Specify new owner, or null/delete to un-own");
else
{
	function isTitle(obj) {
		return obj instanceof Packages.com.planet_ink.coffee_mud.core.interfaces.LandTitle;
	}
	
	var R=mob().location();
	var i;
	var found=false;
	for(i=0;i<R.numEffects();i++)
	{
		var obj=R.fetchEffect(i);
		if(isTitle(obj))
		{
			found = obj.getOwnerName();
			if(parms.toLowerCase() == "null")
				obj.setOwnerName("");
			else
			if(parms.toLowerCase() == "del")
				obj.setOwnerName("");
			else
			if(parms.toLowerCase() == "delete")
				obj.setOwnerName("");
			else
				obj.setOwnerName(parms);
			found="changed from "+found+" to "+obj.getOwnerName();
			mob().tell(found);
			obj.updateTitle();
			break;
		}
	}
	if(found === false)
		mob().tell("No title was found here.");
}
