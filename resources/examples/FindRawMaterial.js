//extends com.planet_ink.coffee_mud.Commands.StdCommand
var CMLib=Packages.com.planet_ink.coffee_mud.core.CMLib;
var CMParms=Packages.com.planet_ink.coffee_mud.core.CMParms;
var RawMaterial=Packages.com.planet_ink.coffee_mud.Items.interfaces.RawMaterial;

function ID(){ return "FindRawMaterial";}

var commands=CMParms.toStringArray(CMParms.parse("FINDRAWMATERIAL"));

function getAccessWords() { return commands;}

function execute(mob,commands,x) {
    var e;
    var R;
    var i;
    var codeStr=CMParms.combine(commands,1);
    var code = RawMaterial.CODES.instance().FIND_IgnoreCase(codeStr);
    if(code < 0)
    {
    	mob.tell("Unknown material "+codeStr);
    }
    else
    for(e=CMLib.map().rooms();e.hasMoreElements();)
    {
        R=e.nextElement();
        if(R!=null)
        {
        	var rsc=R.myResource();
        	if(rsc==code)
        		mob.tell("Found some at "+CMLib.map().getExtendedRoomID(R));
        }
    }
    return true;
}
