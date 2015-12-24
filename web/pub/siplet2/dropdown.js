/***********************************************
* AnyLink Drop Down Menu- © Dynamic Drive (www.dynamicdrive.com)
* This notice MUST stay intact for legal use
* Visit http://www.dynamicdrive.com/ for full source code

//Contents for menu 1
var menu1=new Array()
menu1[0]='<a href="http://www.javascriptkit.com">JavaScript Kit</a>'
menu1[1]='<a href="http://www.freewarejava.com">Freewarejava.com</a>'
menu1[2]='<a href="http://codingforums.com">Coding Forums</a>'
menu1[3]='<a href="http://www.cssdrive.com">CSS Drive</a>'

//Contents for menu 2, and so on
var menu2=new Array()
menu2[0]='<a href="http://cnn.com">CNN</a>'
menu2[1]='<a href="http://msnbc.com">MSNBC</a>'
menu2[2]='<a href="http://news.bbc.co.uk">BBC News</a>'
***********************************************/
        
var menuwidth='165px' //default menu width
var menubgcolor='lightyellow'  //menu bgcolor
var disappeardelay=250  //menu disappear speed onMouseout (in miliseconds)
var hidemenu_onclick="yes" //hide menu when user clicks within menu?

/////No further editting needed

var ie4=document.all
var ns6=document.getElementById&&!document.all

if (ie4||ns6)
document.write('<div id="dropmenudiv" style="visibility:hidden;width:'+menuwidth+';background-color:'+menubgcolor+'" onMouseover="clearhidemenu()" onMouseout="dynamichide(event)"></div>')

function getposOffset(what, offsettype){
var totaloffset=(offsettype=="left")? what.offsetLeft : what.offsetTop;
var parentEl=what.offsetParent;
while (parentEl!=null){
totaloffset=(offsettype=="left")? totaloffset+parentEl.offsetLeft : totaloffset+parentEl.offsetTop;
parentEl=parentEl.offsetParent;
}
return totaloffset;
}


function showhide(obj, e, visible, hidden, menuwidth){
if (ie4||ns6)
dropmenuobj.style.left=dropmenuobj.style.top=-500
if (menuwidth!=""){
dropmenuobj.widthobj=dropmenuobj.style
dropmenuobj.widthobj.width=menuwidth
}
if (e.type=="click" && obj.visibility==hidden || e.type=="mouseover")
obj.visibility=visible
else if (e.type=="click")
obj.visibility=hidden
}

function iecompattest(){
return (document.compatMode && document.compatMode!="BackCompat")? document.documentElement : document.body
}

function clearbrowseredge(obj, whichedge){
var edgeoffset=0
if (whichedge=="rightedge"){
var windowedge=ie4 && !window.opera? iecompattest().scrollLeft+iecompattest().clientWidth-15 : window.pageXOffset+window.innerWidth-15
dropmenuobj.contentmeasure=dropmenuobj.offsetWidth
if (windowedge-dropmenuobj.x < dropmenuobj.contentmeasure)
edgeoffset=dropmenuobj.contentmeasure-obj.offsetWidth
}
else{
var topedge=ie4 && !window.opera? iecompattest().scrollTop : window.pageYOffset
var windowedge=ie4 && !window.opera? iecompattest().scrollTop+iecompattest().clientHeight-15 : window.pageYOffset+window.innerHeight-18
dropmenuobj.contentmeasure=dropmenuobj.offsetHeight
if (windowedge-dropmenuobj.y < dropmenuobj.contentmeasure){ //move up?
edgeoffset=dropmenuobj.contentmeasure+obj.offsetHeight
if ((dropmenuobj.y-topedge)<dropmenuobj.contentmeasure) //up no good either?
edgeoffset=dropmenuobj.y+obj.offsetHeight-topedge
}
}
return edgeoffset
}

function populatemenu(what){
if (ie4||ns6)
dropmenuobj.innerHTML=what.join("")
}


function dropdownmenu(obj, e, menucontents, menuwidth){
if (window.event) event.cancelBubble=true
else if (e.stopPropagation) e.stopPropagation()
clearhidemenu()
dropmenuobj=document.getElementById? document.getElementById("dropmenudiv") : dropmenudiv
populatemenu(menucontents)

if (ie4||ns6){
showhide(dropmenuobj.style, e, "visible", "hidden", menuwidth)
dropmenuobj.x=getposOffset(obj, "left")
dropmenuobj.y=getposOffset(obj, "top")
dropmenuobj.style.left=dropmenuobj.x-clearbrowseredge(obj, "rightedge")+"px"
dropmenuobj.style.top=dropmenuobj.y-clearbrowseredge(obj, "bottomedge")+obj.offsetHeight+"px"
}

return clickreturnvalue()
}

function clickreturnvalue(){
if (ie4||ns6) return false
else return true
}

function contains_ns6(a, b) {
while (b.parentNode)
if ((b = b.parentNode) == a)
return true;
return false;
}

function dynamichide(e){
if (ie4&&!dropmenuobj.contains(e.toElement))
delayhidemenu()
else if (ns6&&e.currentTarget!= e.relatedTarget&& !contains_ns6(e.currentTarget, e.relatedTarget))
delayhidemenu()
}

function hidemenu(e){
if (typeof dropmenuobj!="undefined"){
if (ie4||ns6)
dropmenuobj.style.visibility="hidden"
}
top.entry.boxFocus();
}

function delayhidemenu(){
if (ie4||ns6)
delayhide=setTimeout("hidemenu()",disappeardelay)
}

function clearhidemenu(){
if (typeof delayhide!="undefined")
clearTimeout(delayhide)
}

if (hidemenu_onclick=="yes")
document.onclick=hidemenu

function replaceAll(s)
{
    var x=s.indexOf('\"');
    while(x>=0)
    {
        s=s.substr(0,x)+'&quot;'+s.substr(x+1);
        x=s.indexOf('\"');
    }
    return s;
}
function getSendMenu(titleSet,menu,hints,prompt)
{
    var mmenu=new Array();
    if(menu.length==0) return mmenu;
    var x=menu.indexOf("|");
    var y=hints.indexOf("|");
    var count=0;
    var count2=0;
    while(x>=0)
    {
        count++;
        x=menu.indexOf("|",x+1);
    }
    while(y>=0)
    {
        count2++;
        y=hints.indexOf("|",y+1);
    }
    if(count2>count)
    {
        y=hints.indexOf("|");
        titleSet.title=hints.substr(0,y);
        hints=hints.substr(y+1);
    }
    count=0;
    x=menu.indexOf("|");
    y=hints.indexOf("|");
    while(x>=0)
    {
        var hint=menu.substr(0,x);
        if(y>=0)
        {
            hint=hints.substr(0,y);
            hints=hints.substr(y+1);
            y=hints.indexOf("|");
        }
        else
        if(hints.length>0)
            hint=hints;
        mmenu[count]='<a href="javascript:addToPrompt(\''+replaceAll(menu.substr(0,x))+'\','+prompt+');">'+hint+'</a>';
        count++;
        menu=menu.substr(x+1);
        x=menu.indexOf("|");
    }
    var hint=menu;
    if(y>=0)
        hint=hints.substr(0,y);
    else
    if(hints.length>0)
        hint=hints;
    mmenu[count]='<a href="javascript:addToPrompt(\''+replaceAll(menu)+'\','+prompt+');">'+hint+'</a>';
    return mmenu;
}
