var CMLib=Packages.com.planet_ink.coffee_mud.core.CMLib;
var e;
var R;
var i;
var M;
var anyoneFound=false;
for(e=CMLib.map().rooms();e.hasMoreElements();)
{
    R=e.nextElement();
    for(i=0;i<R.numInhabitants();i++)
    {
        M=R.fetchInhabitant(i);
        if((M!=null)&&(M.location()==R)&&(!CMLib.threads().isTicking(M,-1)))
        {
            mob().tell(M.Name()+", in "+CMLib.map().getExtendedRoomID(R)+", is not ticking.");
            anyoneFound=true;
        }
    }
}
if(!anyoneFound)
	mob().tell("Everyone seems to be ticking!");
