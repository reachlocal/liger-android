PAGE.tabsPage = function(){
    TABSPAGE.initialize();
    return true;
}

PAGE.closeDialogArguments = function(args){
	$('#args').append(JSON.stringify(args));
}

PAGE.childUpdates = function(args){
	$('#args').append(JSON.stringify(args));
}

PAGE.onPageAppear = function(){
	$('#pageAppear').append('*');
}

var TABSPAGE = {
	
	initialize: function(){
		var me = this;

        me.addBindings();

	},

	addBindings: function(){
		$("#firstPage, #secondPage, #thirdPage").unbind();
        
        $("#firstPage").click(function(){
			PAGE.openPage('First Page', 'firstPage', {'test1': 'test2'}, {"right":{"button":"save"}});
			return false;
        });

        $("#secondPage").click(function(){
			PAGE.openPage('Second Page', 'secondPage', {'test1': 'test2'}, {"right":{"button":"save"}});
			return false;
        });
        
        $("#thirdPage").click(function(){
            PAGE.openPage('Third Page', 'thirdPage', {'test1': 'test2'}, {"right":{"button":"save"}});
            return false;
        });
		
	}
}
