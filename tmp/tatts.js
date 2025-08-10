var CMLib=Packages.com.planet_ink.coffee_mud.core.CMLib;
var pname = getParm(0);
var aname = getParm(1);
var P = CMLib.players().getLoadPlayer(pname);
if(P == null)
    mob().tell("No player "+pname);
else
{
    var pd = P.playerStats();
    if(pd == null)
        mob().tell("Not player "+pname);
    else
    {
        if(P.findTattoo(aname)==null)
            mob().tell(pname+" does NOT have tattoo "+aname);
        else
            mob().tell(pname+" does HAVE tattoo "+aname);
        var A = pd.getAccount();
        if(A!=null)
        {
            if(A.findTattoo(aname)==null)
                mob().tell(A.getAccountName()+" account does NOT have tattoo "+aname);
            else
                mob().tell(A.getAccountName()+" account does HAVE tattoo "+aname);
        }
    }
}
