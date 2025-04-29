var gauges=new Array();

function createGauge(entity,caption,color,value,max)
{
	var gaugedata=new Array(5);
	gaugedata[0]=entity;
	gaugedata[1]=caption;
	gaugedata[2]=color;
	gaugedata[3]=value;
	gaugedata[4]=max;
	gauges[gauges.length]=gaugedata;
	modifyGauge(entity,value,max);
}

function removeGauge(entity)
{
	var oldgauges=gauges;
	gauges=new Array();
	var o=0;
	var ndex=0;
	for(o=0;o<oldgauges.length;o++)
	{
		var gaugedata=oldgauges[o];
		if(gaugedata[0]!=entity)
		{
			gauges[ndex]=gaugedata;
			ndex++;
		}
	}
	modifyGauge(entity,-1,-1);
}

function modifyGauge(entity,value,max)
{
	var div=top.term.document.getElementById(myname+'extracontent');
	if(gauges.length==0)
		div.innerHTML='';
	else
	{
		var gaugewid=100;
		var s='<TABLE WIDTH=100% CELLPADDING=0 CELLSPACING=0 BORDER=1><TR>';
		var i=0;
		var cellwidth=100/gauges.length;
		for(i=0;i<gauges.length;i++)
		{
			var gaugedata=gauges[i];
			if(gaugedata[0]==entity)
			{
				gaugedata[3]=value;
				gaugedata[4]=max;
			}
		}
		for(i=0;i<gauges.length;i++)
		{
			var gaugedata=gauges[i];
			s+='<TD WIDTH='+cellwidth+'%>';
			s+='<FONT STYLE="color: '+gaugedata[2]+'" SIZE=-2>'+gaugedata[1]+'</FONT><BR>';
			var gaugedata=gauges[i];
			var fullwidth=100-gaugedata[3];
			var lesswidth=gaugedata[3];
			s+='<TABLE WIDTH=100% CELLPADDING=0 CELLSPACING=0 BORDER=0 HEIGHT=5><TR HEIGHT=5>';
			s+='<TD STYLE="background-color: '+gaugedata[2]+'" WIDTH='+lesswidth+'%></TD>';
			s+='<TD STYLE="background-color: black" WIDTH='+fullwidth+'%></TD>';
			s+='</TR></TABLE>';
			s+='</TD>';
		}
		s+='</TR></TABLE>'
		div.innerHTML=s;
	}
}
