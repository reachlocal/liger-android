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
         PAGE.openPage('First Page', 'firstPage', {'test1': 'test2'}, {"right":{"button":"save"}});
     },

     addBindings: function(){
         $("#firstPage, #secondPage, #thirdPage").unbind();

         $("#firstPage").click(function(){
             PAGE.openPage('Navigator', 'navigator',{
                                                                    "pages": [
                                                                      {
                                                                        "title": "First Page",
                                                                        "page": "firstPage",
                                                                        "accessibilityLabel": "firstPage",
                                                                        "args": {
                                                                          "hello": "world"
                                                                        }
                                                                      },
                                                                      {
                                                                        "title": "Second Page",
                                                                        "page": "secondPage",
                                                                        "accessibilityLabel": "secondPage",
                                                                        "args": {
                                                                          "hello": "world"
                                                                        }
                                                                      },
                                                                      {
                                                                        "title": "Third Page",
                                                                        "page": "thirdPage",
                                                                        "accessibilityLabel": "thirdPage",
                                                                        "args": {
                                                                          "hello": "world"
                                                                        }
                                                                      }
                                                                    ]
                                                                  }, {"right":{"button":"save"}});
             return false;
         });

         $("#secondPage").click(function(){
             PAGE.openPage('System', 'navigator', {
                                                                  "title": "System Pages",
                                                                  "page": "system",
                                                                  "accessibilityLabel": "system"
                                                                }, {});
             return false;
         });

         $("#thirdPage").click(function(){
             PAGE.openPage('Native Page', 'navigator', {
                                                                       "page": "nativePages",
                                                                       "title": "Native Page",
                                                                       "accessibilityLabel": "nativeDialog"
                                                                     }, {"right":{"button":"save"}});
             return false;
         });

     }
 }
