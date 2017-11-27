var fileisnamed='floater.js'
var dragapproved=false
var dragged=''
var minrestore=0
var saveWidth=0
var saveHeight=0
var saveTop=0
var saveLeft=0
var ie5=document.all&&document.getElementById
var ns6=document.getElementById&&!document.all
var topBound=5
var leftBound=5
var rightFudge=5
var bottomFudge=50
var rightBound=2000
var botBound=2000

function minLeft(){ return leftBound+(ns6?window.pageXOffset:iecompattest().scrollLeft);}
function minTop(){ return topBound+(ns6?window.pageYOffset:iecompattest().scrollTop);}

function curWinWidth(){ return ns6?window.innerWidth:iecompattest().clientWidth;}
function curWinHeight(){ return ns6?window.innerHeight:iecompattest().clientHeight;}

function iecompattest(){return (!window.opera && document.compatMode && document.compatMode!="BackCompat")? document.documentElement : document.body;}

function drag_drop(e)
{
    if (ie5&&dragapproved&&event.button==1)
    {
        document.getElementById(dragged).style.left=tempx+event.clientX-offsetx+"px"
        document.getElementById(dragged).style.top=tempy+event.clientY-offsety+"px"
    }
    else 
    if (ns6&&dragapproved)
    {
        document.getElementById(dragged).style.left=tempx+e.clientX-offsetx+"px"
        document.getElementById(dragged).style.top=tempy+e.clientY-offsety+"px"
    }
}

function initializedrag(e,wname,wnum)
{
	front(wname,wnum);
    offsetx=ie5? event.clientX : e.clientX
    offsety=ie5? event.clientY : e.clientY
    document.getElementById(wname+"content").style.display="none"
    tempx=parseInt(document.getElementById(wname).style.left)
    tempy=parseInt(document.getElementById(wname).style.top)
    
    dragapproved=true
    dragged=wname
    document.getElementById(wname).onmousemove=drag_drop;
    //document.getElementById(wname).onmouseout=stopdrag;
}

function loadwindow(url,width,height,wname,wnum)
{
    if (!ie5&&!ns6)
        window.open(url,"","width=width,height=height,scrollbars=1")
    else
    {
        document.getElementById(wname).style.display=''
        document.getElementById(wname).style.width=saveWidth=width+"px"
        document.getElementById(wname).style.height=saveHeight=height+"px"
        document.getElementById(wname).style.left=saveLeft=minLeft()+"px"
        document.getElementById(wname).style.top=saveTop=minTop()+"px"
        document.getElementById(wname+"frame").src=url
        top.bar.redColor(wnum);
        top.term.currentWindow=-1;
        front(wname,wnum);
    }
}

function maximize(wname,wnum)
{
    if (minrestore==0)
    {
        minrestore=1 //maximize window
        document.getElementById(wname+"max").setAttribute("src","/siplet/restore.gif")
        
        saveWidth=document.getElementById(wname).style.width
        saveHeight=document.getElementById(wname).style.height
        saveTop=document.getElementById(wname).style.top
        saveLeft=document.getElementById(wname).style.left
                
        document.getElementById(wname).style.left=minLeft()+"px"
        document.getElementById(wname).style.top=minTop()+"px"
        document.getElementById(wname).style.width=(curWinWidth()-minLeft()-rightFudge)+"px"
        document.getElementById(wname).style.height=(curWinHeight()-minTop()-bottomFudge)+"px"
        
    }
    else
    {
        minrestore=0 //restore window
        document.getElementById(wname+"max").setAttribute("src","/siplet/max.gif")
        document.getElementById(wname).style.width=saveWidth
        document.getElementById(wname).style.height=saveHeight
        document.getElementById(wname).style.left=saveLeft
        document.getElementById(wname).style.top=saveTop
    }
    document.getElementById(wname+"content").style.display="none";
}

function reposition(wname,wnum)
{
    maximize(wname);
    maximize(wname);
}

function closewindow(wname,wnum)
{
	var obj = alldivs[wnum];
	var obj2 = document.getElementById(wname);
	obj2.style.display="none";
	var obj3 = top.term.allapplets[wnum];
	window.console.info("Siplet floater closewindow");
	obj3.disconnectFromURL();
	alldivs[wnum]=null;
	obj2.innerHTML = '';
	obj.innerHTML = '';
	for(var i=0;i<10;i++)
		if(top.term.alldivs[i] != null)
			top.term.currentWindow = i;
	top.bar.blackStatus(wnum);
	front('dwindow'+top.term.currentWindow,top.term.currentWindow);
}
function front(wname,wnum)
{
	if(top.term.currentWindow != wnum)
	{
	    document.getElementById("EWINDOW"+wnum).style.zIndex=10;
	    document.getElementById(wname).style.zIndex=10;
	    for(var i=0;i<10;i++)
	    	if(i!=wnum)
	    	{
	    		var obj = alldivs[i];
	    		if(obj != null)
	    			obj.style.zIndex=0;
	    		obj = document.getElementById("dwindow"+i);
	    		if(obj != null)
	    			obj.style.zIndex=0;
	    	}
	    top.term.currentWindow = wnum;
	    if(top && top.entry && top.entry.boxFocus)
		    top.entry.boxFocus();
	    top.bar.greenIfLight(wnum);
	    return false;
	}
	return true;
}
function hideit(wname,wnum)
{
    document.getElementById(wname).style.display="none"
}
function unhideit(wname,wnum)
{
    document.getElementById(wname).style.display=""
}

function stopdrag(wname, wnum)
{
    dragapproved=false;
    document.getElementById(wname).onmousemove=null;
    document.getElementById(wname+"content").style.display=""
}

function getFrameHTML(wname,wnum)
{
    var s='<div id="'+wname+'" style="position:absolute;background-color:#EBEBEB;cursor:hand;left:0px;top:0px;display:none" onMousedown="initializedrag(event,\''+wname+'\','+wnum+')" onMouseup="stopdrag(\''+wname+'\','+wnum+')" onSelectStart="return false">';
    s+='<div id="'+wname+'bar" style="background-color:red">';
    s+='<table width=100% border=0 cellspacing=0 cellpadding=0 onclick="top.term.front(\''+wname+'\','+wnum+')"><tr>';
    s+='<td width=80% align=left>'
    s+='<div id="'+wname+'content" style="height:100%">';
    s+='<div id="'+wname+'namer" style="background-color:red"></div>';
    s+='</td><td width=20% align=right>'
    s+='<img src="/siplet/max.gif" id="'+wname+'max" onClick="maximize(\''+wname+'\','+wnum+')">';
    s+='<img src="/siplet/close.gif" id="'+wname+'close" onClick="closewindow(\''+wname+'\','+wnum+')">';
    s+='</td></tr></table>'
    s+='</div>';
    s+='<div id="'+wname+'extracontent"></div>';
    s+='<iframe id="'+wname+'frame" src="" width=100% height=100%></iframe>';
    s+='</div>';
    s+='</div>';
    return s;
}
