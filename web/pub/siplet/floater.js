//DHTML Window script- Copyright Dynamic Drive (http://www.dynamicdrive.com)
//For full source code, documentation, and terms of usage,
//Visit http://www.dynamicdrive.com/dynamicindex9/dhtmlwindow.htm
var fileisnamed='floater.js'
var dragapproved=false
var dragged=''
var minrestore=0
var initialwidth,initialheight
var ie5=document.all&&document.getElementById
var ns6=document.getElementById&&!document.all
var topBound=10
var leftBound=10
var rightBound=2000
var botBound=2000

function iecompattest(){
return (!window.opera && document.compatMode && document.compatMode!="BackCompat")? document.documentElement : document.body
}

function drag_drop(e){
if (ie5&&dragapproved&&event.button==1){
document.getElementById(dragged).style.left=tempx+event.clientX-offsetx+"px"
document.getElementById(dragged).style.top=tempy+event.clientY-offsety+"px"
}
else if (ns6&&dragapproved){
document.getElementById(dragged).style.left=tempx+e.clientX-offsetx+"px"
document.getElementById(dragged).style.top=tempy+e.clientY-offsety+"px"
}
}

function initializedrag(e,wname){
offsetx=ie5? event.clientX : e.clientX
offsety=ie5? event.clientY : e.clientY
document.getElementById(wname+"content").style.display="none" //extra
tempx=parseInt(document.getElementById(wname).style.left)
tempy=parseInt(document.getElementById(wname).style.top)

dragapproved=true
dragged=wname
document.getElementById(wname).onmousemove=drag_drop;
}

function loadwindow(url,width,height,wname){
if (!ie5&&!ns6){
window.open(url,"","width=width,height=height,scrollbars=1")
}
else{
document.getElementById(wname).style.display=''
document.getElementById(wname).style.width=initialwidth=width+"px"
document.getElementById(wname).style.height=initialheight=height+"px"
document.getElementById(wname).style.left=leftBound+"px"
document.getElementById(wname).style.top=ns6? window.pageYOffset*1+topBound+"px" : iecompattest().scrollTop*1+topBound+"px"
document.getElementById(wname+"frame").src=url
}
}

function maximize(wname){
if (minrestore==0){
minrestore=1 //maximize window
document.getElementById(wname+"max").setAttribute("src","restore.gif")
initialWidth=document.getElementById(wname).style.width
initialHeight=document.getElementById(wname).style.height
var windowWidth=ns6?window.innerWidth-leftBound:iecompattest().clientWidth-leftBound;
if(windowWidth>(rightBound-leftBound)) windowWidth=rightBound-leftBound;
var windowHeight=ns6?window.innerHeight-topBound-5:iecompattest().clientHeight-topBound-5;
if(windowHeight>(botBound-topBound-5)) windowHeight=botBound-topBound-5;
document.getElementById(wname).style.width=windowWidth+"px"
document.getElementById(wname).style.height=windowHeight+"px"
}
else{
minrestore=0 //restore window
document.getElementById(wname+"max").setAttribute("src","max.gif")
document.getElementById(wname).style.width=initialwidth
document.getElementById(wname).style.height=initialheight
}
document.getElementById(wname).style.left=ns6? window.pageXOffset+"px" : iecompattest().scrollLeft+"px"
document.getElementById(wname).style.top=ns6? window.pageYOffset+"px" : iecompattest().scrollTop+"px"
}

function closeit(wname){
document.getElementById(wname).style.display="none"
}

function stopdrag(wname){
dragapproved=false;
document.getElementById(wname).onmousemove=null;
document.getElementById(wname+"content").style.display="" //extra
}

function getFrameHTML(wname)
{
    var addBackToDivForDragDrop='onMousedown="initializedrag(event,\''+wname+'\')" onMouseup="stopdrag(\''+wname+'\')"';
    var s='<div id="'+wname+'" style="position:absolute;background-color:#EBEBEB;cursor:hand;left:0px;top:0px;display:none"  onSelectStart="return false">';
    s+='<div align="right" style="background-color:yellow">';
    s+='<img src="max.gif" id="'+wname+'max" onClick="maximize(\''+wname+'\')">';
    //s+='<img src="close.gif" onClick="closeit(\''+wname+'\')">';
    s+='</div><div id="'+wname+'content" style="height:100%">';
    s+='<iframe id="'+wname+'frame" src="" width=100% height=100%></iframe>';
    s+='</div>';
    s+='</div>';
    return s;
}
