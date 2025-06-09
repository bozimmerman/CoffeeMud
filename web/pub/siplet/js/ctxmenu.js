function ContextDelayHide()
{
	setTimeout(ContextHideAll,250);
}

function ContextDelaySubHide()
{
	setTimeout(ContextHideSub,250);
}

function ContextHideAll()
{
	ContextHideSub();
	var menu = document.getElementById('ctxmenu');
	if(menu != null)
		menu.outerHTML='';
}

function ContextHideSub()
{
	var menu = document.getElementById('ctxsubmenu');
	if(menu != null)
		menu.outerHTML='';
}

function IsContextHover(e, menuName)
{
	if(menuName === undefined)
		return IsContextHover(e, 'ctxmenu') || IsContextHover(e, 'ctxsubmenu'); 
	var div = document.getElementById(menuName);
	if (!div) 
		return false;
	var x = e.clientX || 0;
	var y = e.clientY || 0;
	var elementAtPoint = document.elementFromPoint(x, y);
	return elementAtPoint === div || div.contains(elementAtPoint);
}

function IsContextMenuHover(e)
{
	return IsContextHover(e, 'ctxmenu');
}

function IsContextSubMenuHover(e)
{
	return IsContextHover(e, 'ctxsubmenu');
}

function ContextMenu(obj, e, href, hint, prompt) {
	var menu= ContextMenuOpen(obj, e, href, hint, prompt, e.pageX-40, e.pageY-10, 200);
	menu.style.border = "1px solid";
	menu.style.borderColor = "white";
	menu.style.left = (parseInt(menu.style.left || "0") + 10) + "px";
	return menu;
}

function ContextMenuOpen(obj, e, href, hint, prompt, x,y,width) {
	if (window.event) 
		window.event.cancelBubble=true;
	else
	if (e && e.stopPropagation) 
		e.stopPropagation();
	if(e && e.preventDefault)
		e.preventDefault();
	var menucontents = ParseContextMenu(obj, href, hint, prompt);
	var menu = CreateContextDiv('ctxmenu',x,y,width);
	menu.onmouseleave = function(e) {
		if(!IsContextHover(e))
			ContextDelayHide();
	}
	menu.onclick = function() {
		ContextDelayHide();
	};
	var pstyle = "<p style=\"padding: 0 1rem; margin: 0;\">";
	for(var i=0;i<menucontents.length;i++)
		menu.innerHTML += pstyle+menucontents[i]+'</p>';
	return menu;
}

function CreateContextDiv(id, x, y, width)
{
	var menu = document.createElement("div");
	menu.id = id;
	menu.style.cssText = "top:" + y+"px;"
					   + "left:" + x+"px;"
					   + "font-family: monospace;"
					   + "font-size: 12px;"
					   + "position: fixed;"
					   + "background: black;"
					   + "color: yellow;"
					   + "cursor: pointer;"
					   + "z-order: 999;"
					   + "z-index: 999;"
					   + "border: 1px black solid";
	if(width === 'auto')
	{
		menu.style.display = 'inline-block';
		menu.style.whiteSpace = 'nowrap';
	}
	else
		menu.style.width = width+'px';
	document.body.appendChild(menu);
	return menu;
}

function ContextSubMenuOpen(obj, e, href, hint, prompt, x,y,width) {
	if (window.event) 
		window.event.cancelBubble=true;
	else
	if (e && e.stopPropagation) 
		e.stopPropagation();
	if(e && e.preventDefault)
		e.preventDefault();
	ContextHideSub();
	var menucontents = ParseContextMenu(obj, href, hint, prompt);
	var menu = CreateContextDiv('ctxsubmenu',x,y,width);
	menu.onmouseleave = function(e) {
		if(!IsContextHover(e))
			ContextDelayHide();
		else
		if(!IsContextSubMenuHover(e))
			ContextDelaySubHide();
	}
	menu.onclick = ContextDelayHide;
	var pstyle = "<p style=\"padding: 0 1rem; margin: 0;\">";
	for(var i=0;i<menucontents.length;i++)
		menu.innerHTML += pstyle+menucontents[i]+'</p>';
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

function ParseContextMenu(titleSet,menu,hints,prompt)
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
		if(titleSet)
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
			mmenu[count]='<a href="#" onclick="'+m.substr(11)+'">'+hint+'</a>';
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

function ContextHelp(obj, e,title)
{
	ContextHideAll();
	var content = ContextMenuOpen(obj, e, '', '', '', 0, 20, 400)
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