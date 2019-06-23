package ca.allanwang.gitdroid.codeview.language

import java.util.regex.Pattern

// Keyword lists for various languages.
internal const val FLOW_CONTROL_KEYWORDS = "break,continue,do,else,for,if,return,while"
internal const val C_KEYWORDS =
    "$FLOW_CONTROL_KEYWORDS,auto,case,char,const,default,double,enum,extern,float,goto," +
            "inline,int,long,register,short,signed,sizeof,static,struct,switch,typedef,union,unsigned,void,volatile"
internal const val COMMON_KEYWORDS = "$C_KEYWORDS,catch,class,delete,false,import," +
        "new,operator,private,protected,public,this,throw,true,try,typeof"
internal const val CPP_KEYWORDS = "$COMMON_KEYWORDS,alignof,align_union,asm,axiom,bool," +
        "concept,concept_map,const_cast,constexpr,decltype,delegate," +
        "dynamic_cast,explicit,export,friend,generic,late_check," +
        "mutable,namespace,nullptr,property,reinterpret_cast,static_assert," +
        "static_cast,template,typeid,typename,using,virtual,where"
internal const val JAVA_KEYWORDS =
    "$COMMON_KEYWORDS,abstract,assert,boolean,byte,extends,final,finally,implements,import," +
            "instanceof,interface,null,native,package,strictfp,super,synchronized," +
            "throws,transient"
internal const val RUST_KEYWORDS = "$FLOW_CONTROL_KEYWORDS,as,assert,const,copy,drop," +
        "enum,extern,fail,false,fn,impl,let,log,loop,match,mod,move,mut,priv," +
        "pub,pure,ref,self,static,struct,true,trait,type,unsafe,use"
internal const val CSHARP_KEYWORDS = "$JAVA_KEYWORDS,as,base,by,checked,decimal,delegate,descending,dynamic,event," +
        "fixed,foreach,from,group,implicit,in,internal,into,is,let," +
        "lock,object,out,override,orderby,params,partial,readonly,ref,sbyte," +
        "sealed,stackalloc,string,select,uint,ulong,unchecked,unsafe,ushort," +
        "var,virtual,where"
internal const val COFFEE_KEYWORDS = "all,and,by,catch,class,else,extends,false,finally," +
        "for,if,in,is,isnt,loop,new,no,not,null,of,off,on,or,return,super,then," +
        "throw,true,try,unless,until,when,while,yes"
internal const val JSCRIPT_KEYWORDS =
    "$COMMON_KEYWORDS,debugger,eval,export,function,get,null,set,undefined,var,with," +
            "Infinity,NaN"
internal const val PERL_KEYWORDS = "caller,delete,die,do,dump,elsif,eval,exit,foreach,for," +
        "goto,if,import,last,local,my,next,no,our,print,package,redo,require," +
        "sub,undef,unless,until,use,wantarray,while,BEGIN,END"
internal const val PYTHON_KEYWORDS = "$FLOW_CONTROL_KEYWORDS,and,as,assert,class,def,del," +
        "elif,except,exec,finally,from,global,import,in,is,lambda," +
        "nonlocal,not,or,pass,print,raise,try,with,yield," +
        "False,True,None"
internal const val RUBY_KEYWORDS = "$FLOW_CONTROL_KEYWORDS,alias,and,begin,case,class," +
        "def,defined,elsif,end,ensure,false,in,module,next,nil,not,or,redo," +
        "rescue,retry,self,super,then,true,undef,unless,until,when,yield," +
        "BEGIN,END"
internal const val SH_KEYWORDS = "$FLOW_CONTROL_KEYWORDS,case,done,elif,esac,eval,fi," +
        "function,in,local,set,then,until"
internal const val ALL_KEYWORDS =
    "$CPP_KEYWORDS,CSHARP_KEYWORDS,JSCRIPT_KEYWORDS,PERL_KEYWORDS,$PYTHON_KEYWORDS,$RUBY_KEYWORDS,$SH_KEYWORDS"
internal val C_TYPES =
    Pattern.compile("^(DIR|FILE|vector|(de|priority_)?queue|list|stack|(const_)?iterator|(multi)?(set|map)|bitset|u?(int|float)\\d*)\\b")