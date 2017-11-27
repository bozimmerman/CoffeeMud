var mynum = top.term.currentWindow;
var myname='dwindow'+top.term.currentWindow;
var mydivname='ewindow'+top.term.currentWindow;
var gauges=new Array();
var theSiplet = null;
var lastReceived = new Date().getTime();

var limit=50000;
var pingDelay=2400;
var halflimit=limit/2;
var tenthlimit=limit/10;
var errorState=false;
var connCheck=null;

function connectChecker()
{
	if((theSiplet!=null)&&(!errorState))
	{
		var ellapsed=new Date().getTime() - lastReceived;
		displayData();
	}
	else
		lastReceived = new Date().getTime()
	connCheck = setTimeout(connectChecker,10001);
}

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

function handlePacket(txt,tk)
{
	var x=txt.indexOf(tk);
	if(x<0)
	{
		window.console.info("Siplet: Packet missing tk: "+txt);
		goOffline();
	}
	else
	{
		var data=txt.substr(0,x);
		var s="";
		s=s+data;
		addToWindow(s);

		txt=txt.substr(x+tk.length);
		x=txt.indexOf(tk);
		if(x>0)
		{
			data=txt.substr(0,x);
			s="";
			s=s+data;
			if(s.length>0)
				eval(s);
		}
		lastReceived = new Date().getTime();
		//setTimeout(displayData,pingDelay);
	}
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

var sounders=[];
var soundDates=[];
var soundPriorities=[];
function PlaySound(key,playerName,url,repeats,volume,priority)
{
	var theSoundPlayer = sounders[playerName];  
	if(theSoundPlayer)
	{
		var now=new Date();
		var ellapsed=Math.abs(now.getTime() - soundDates[playerName].getTime());
		if((ellapsed < 1500) && (priority <= soundPriorities[playerName]))
			return;
		if(theSoundPlayer.stop)
			theSoundPlayer.stop();
		document.childNodes[0].removeChild(theSoundPlayer);
	}
	theSoundPlayer=document.createElement('embed');
	if(!theSoundPlayer) return;
	sounders[playerName]=theSoundPlayer;
	soundDates[playerName]=new Date();
	soundPriorities[playerName]=priority;
	document.childNodes[0].appendChild(theSoundPlayer);
	theSoundPlayer.setAttribute('src', url+key);
	theSoundPlayer.setAttribute('type','audio/wav');
	theSoundPlayer.setAttribute('hidden','true');
	theSoundPlayer.setAttribute('volume', volume);
	if(theSoundPlayer.play)
		theSoundPlayer.play();
}

function StopSound(key,playerName)
{
	var theSoundPlayer=document.getElementById(playerName);
	theSoundPlayer.src='';
	theSoundPlayer.Play();
	theSoundPlayer.innerHTML='';
}

function addToWindow(s)
{
	if(s.length>0)
	{
		var theSpan=document.getElementById("DISPLAYSPAN");
		var theend=document.getElementById("NODISPLAY");
		if(theSpan.innerHTML.length>(limit+halflimit))
		{
			var x=theSpan.innerHTML;
			var xcess=x.length-limit;
			var br='<BR>';
			var y=x.indexOf(br,xcess);
			if(y<0) 
			{
				br='<BR >';
				y=x.indexOf(br,xcess);
			}
			if(y<0) 
			{
				br='<br>';
				y=x.indexOf(br,xcess);
			}
			if(y<0) 
			{
				br='<br >';
				y=x.indexOf(br,xcess);
			}
			if((y<0)||(y>xcess+tenthlimit))
				y=x.indexOf(br,xcess/2);
			if(y>=0)
				x=x.substring(y+br.length);
			else
			while(x.length>limit)
			{
				var y=x.indexOf(br);
				if(y<0) break;
				x=x.substring(y+br.length);
			}
			theSpan.innerHTML=x;
		}
		theSpan.innerHTML+=s;
		theend.scrollIntoView(false);
		if(mynum != top.term.currentWindow)
			top.bar.lightgreenColor(mynum);
	}
}

function goCaution()
{
	var theSpan=top.term.document.getElementById(myname+'namer');
	theSpan.style.backgroundColor='yellow'
	theSpan=top.term.document.getElementById(myname+'bar');
	theSpan.style.backgroundColor='yellow'
	top.bar.yellowColor(mynum);
}

function goGreen()
{
	var theSpan=top.term.document.getElementById(myname+'namer');
	theSpan.style.backgroundColor='green'
	theSpan=top.term.document.getElementById(myname+'bar');
	theSpan.style.backgroundColor='green'
	top.bar.greenColor(mynum);
}

function goRed()
{
	var theSpan=top.term.document.getElementById(myname+'bar');
	theSpan.style.backgroundColor='red'
	theSpan=top.term.document.getElementById(myname+'namer');
	theSpan.style.backgroundColor='red'
	top.bar.redColor(mynum);
}


function BoxFocus()
{
	top.entry.boxFocus();
}

function NoJava()
{
	var theSpan=document.getElementById("DISPLAYSPAN");
	theSpan.innerHTML='<P><BR><FONT COLOR=RED SIZE=4>It does not appear that your browser has the proper version of the Java Runtime Environment installed.  Please visit <a href=http://java.sun.com>java.sun.com</a> to download the JRE.  Then restart your browser and try this page again.  If problems persist, make sure your browser has the <I>Allow Applet Installation</I> and <I>Allow Scripting</I> features enabled.</FONT><P>';
}

function addToPrompt(x,att)
{
	top.entry.document.ENTER.TEXT.value=x;
	if(!att)
		top.entry.sendText(x);
	else
		top.entry.document.ENTER.TEXT.value+=" ";
}

function goDefault(x)
{
	top.term.front('dwindow'+x,x);
}

top.term.allwindows[top.term.currentWindow]=this;

