//extends com.planet_ink.coffee_mud.Commands.StdCommand
var CMLib=Packages.com.planet_ink.coffee_mud.core.CMLib;
var CMParms=Packages.com.planet_ink.coffee_mud.core.CMParms;

function ID(){ return "QuickWho";}

var commands=CMParms.toStringArray(CMParms.makeVector("QUICKWHO"));

function getAccessWords() { return commands;}

function execute(mob,commands) {
    var e;
    var M;
    for(e=CMLib.map().players();e.hasMoreElements();)
    {
        M=e.nextElement();
        if(M!=null)
            mob.tell(M.Name());
    }
    return true;
}
