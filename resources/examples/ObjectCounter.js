var CMLib=Packages.com.planet_ink.coffee_mud.core.CMLib;
var CMParms=Packages.com.planet_ink.coffee_mud.core.CMParms;

var e,e2;
var R,M;
var i;
var numrooms=0;
var numidrooms=0;
var nummobs=0;
var numitems=0;
for(e=CMLib.map().rooms();e.hasMoreElements();)
{
    R=e.nextElement();
    if(R!=null)
    {
    	nummobs+=R.numInhabitants();
    	numitems+=R.numItems();
    	numrooms++;
    	if(R.roomID().length()>0) 
    		numidrooms++;
    	for(e2=0;e2<R.numInhabitants();e2++)
    	{
    		M=R.fetchInhabitant(e2);
    		if(M!=null)
    		{
    			numitems+=M.numItems();
    			if(M.ID().equals("GenShopkeeper"))
    			{
    			    numitems+=M.getShop().totalStockSize();
    			}
    		}
    	}
    }
}
mob().tell("Number of rooms: "+numrooms+" (Base: "+numidrooms+"), mobs="+nummobs+", items="+numitems);
