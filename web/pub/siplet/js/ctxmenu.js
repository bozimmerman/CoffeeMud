function delayhidemenu()
{
    setTimeout(nowhidemenu,250);
}

function nowhidemenu()
{
    var menu = document.getElementById('ctxmenu');
    if(menu != null)
        menu.outerHTML='';
}

function contextmenu(obj, e, href, hint, prompt) {
	var menu= dropdownmenu(obj, e, href, hint, prompt, e.pageX-40, e.pageY-10, 200);
	menu.style.border = "1px solid";
	menu.style.borderColor = "white";
	menu.style.left = (parseInt(menu.style.left || "0") + 10) + "px";
	return menu;
}

function dropdownmenu(obj, e, href, hint, prompt, x,y,width) {
	if (window.event) 
		window.event.cancelBubble=true;
	else 
	if (e.stopPropagation) 
		e.stopPropagation();
	if(e.preventDefault)
		e.preventDefault();
	var menucontents = getCtxMenu(obj, href, hint, prompt);
	var menu = document.createElement("div");
	menu.id = "ctxmenu"
	menu.style.cssText = "top:" + y+"px;"
					   + "left:" + x+"px;"
					   + "width:" + width+"px;"
					   + "font-family: monospace;"
					   + "font-size: 12px;"
					   + "position: fixed;"
					   + "background: black;"
					   + "color: yellow;"
					   + "cursor: pointer;"
					   + "z-order: 999;"
					   + "z-index: 999;"
					   + "border: 1px black solid";
	menu.onmouseleave = delayhidemenu;
	menu.onclick = delayhidemenu;
	var pstyle = "<p style=\"padding: 0 1rem; margin: 0;\">";
	for(var i=0;i<menucontents.length;i++)
		menu.innerHTML += pstyle+menucontents[i]+'</p>';
	document.body.appendChild(menu);
	return menu;
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
        var m = fixCtxEnt(menu.substr(0,x));
        if(m.length==0)
            mmenu[count]='<font color=lightgray>'+hint+'</font>';
        else
        if(m.startsWith("javascript:"))
            mmenu[count]='<a href="'+m+'">'+hint+'</a>';
        else
            mmenu[count]='<a href="javascript:addToPrompt(\''+m+'\','+prompt+');">'+hint+'</a>';
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
    var m = fixCtxEnt(menu);
    if(m.length==0)
        mmenu[count]='<font color=lightgray>'+hint+'</font>';
    else
    if(m.startsWith("javascript:"))
        mmenu[count]='<a href="'+m+'">'+hint+'</a>';
    else
        mmenu[count]='<a href="javascript:addToPrompt(\''+m+'\','+prompt+');">'+hint+'</a>';
    return mmenu;
}

function contextHelp(obj, e,title)
{
	nowhidemenu();
	var content = dropdownmenu(obj, e, '', '', '', 0, 20, 400)
	var f = 'help_' + title.toLowerCase() + '.htm';
	content.style.height = '400px';
	populateDivFromUrl(content, 'help/'+f,function(){
		content.lastElementChild.style.cssText = 
			"background-color:black;"
			+"position:absolute;"
			+"color:white;"
			+"font-size:14;"
			+"overflow-x:auto;"
			+"overflow-y:auto;"
			+"overflow:auto;"
			+"height: 400px;";
	});
	
}