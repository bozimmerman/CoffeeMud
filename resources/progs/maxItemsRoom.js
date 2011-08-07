var CMLib=Packages.com.planet_ink.coffee_mud.core.CMLib;
var e;
var R;
var i;
var M;
var AI=new Array();
var AR=new Array();
var numI=0;
var smallest=0;
var time=Packages.java.lang.System.currentTimeMillis();
mob().tell("Timestamp is: "+time);
for(e=CMLib.map().rooms();e.hasMoreElements();)
{
	R=e.nextElement();
	numI=R.numItems();
	if(R.roomID().length()>0)
	{
		smallest=-1;
	        for(i=0;i<20;i++)
		if(AI[i]==null)
		{
			AI[i]=numI;
			AR[i]=R.roomID();
		}
		else
		if((AI[i]<numI)&&((smallest<0)||(AI[i]<AI[smallest])))
			smallest=i;
			
	   	if(smallest>=0)
	   	{
	   		AI[smallest]=numI;
	   		AR[smallest]=R.roomID();
	   	}
	}
}
	for(i=0;i<20;i++)
		 mob().tell(AI[i]+") "+AR[i]);
