var CMLib=Packages.com.planet_ink.coffee_mud.core.CMLib;
var e;
var R;
var i;
var M;
for(e=CMLib.map().rooms();e.hasMoreElements();)
{
    R=e.nextElement();
    for(i=0;i<R.numInhabitants();i++)
    {
        M=R.fetchInhabitant(i);
        if((M!=null)&&(M.getStartRoom().getArea()!=R.getArea()))
            mob().tell(M.Name()+", in "+CMLib.map().getExtendedRoomID(R)+", should be in "+M.getStartRoom().roomID());
    }
}
