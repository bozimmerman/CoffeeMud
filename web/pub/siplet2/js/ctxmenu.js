function delayhidemenu()
{
    setTimeout(function() {
        var menu = document.getElementById('ctxmenu');
        if(menu != null)
            menu.outerHTML='';
    },250);
}

function dropdownmenu(obj, e, href, hint, prompt, menuwidth){
	if (window.event) 
		event.cancelBubble=true
	else 
	if (e.stopPropagation) 
		e.stopPropagation()
	if(e.preventDefault)
		e.preventDefault();
	var menucontents = getCtxMenu(obj, href, hint, prompt);
	var menu = document.createElement("div")
	menu.id = "ctxmenu"
	menu.style = "top:" + (e.pageY-10)+"px;"
			   + "left:" + (e.pageX-40)+"px;"
			   + "font-family: monospace;"
			   + "font-size: 12px;"
			   + "position: fixed;"
			   + "background: darkgray;"
			   + "color: black;"
			   + "cursor: pointer;"
			   + "border: 1px black solid"
	menu.onmouseleave = delayhidemenu;
	menu.onclick = delayhidemenu;
	var pstyle = "<p style=\"padding: 0 1rem; margin: 0;\">";
	for(var i=0;i<menucontents.length;i++)
		menu.innerHTML += pstyle+menucontents[i]+'</p>'
	document.body.appendChild(menu)
}

function fixCtxEnt(s)
{
    var x=s.indexOf('\"');
    while(x>=0)
    {
        s=s.substr(0,x)+'&quot;'+s.substr(x+1);
        x=s.indexOf('\"');
    }
    return s;
}

function getCtxMenu(titleSet,menu,hints,prompt)
{
    var mmenu=new Array();
    if(menu.length==0) 
    	return mmenu;
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
        mmenu[count]='<a href="javascript:addToPrompt(\''+fixCtxEnt(menu.substr(0,x))+'\','+prompt+');">'+hint+'</a>';
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
    mmenu[count]='<a href="javascript:addToPrompt(\''+fixCtxEnt(menu)+'\','+prompt+');">'+hint+'</a>';
    return mmenu;
}

