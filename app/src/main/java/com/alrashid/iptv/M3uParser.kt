package com.alrashid.iptv
object M3uParser {
 fun parse(text:String):List<Channel>{
  val result=mutableListOf<Channel>(); var name:String?=null; var group="عام"; var logo:String?=null
  text.lineSequence().forEach { raw ->
   val line=raw.trim().removePrefix("\uFEFF")
   when {
    line.startsWith("#EXTINF",true)->{
     val attrs=Regex("([\\\\w-]+)=\"([^\"]*)\"").findAll(line).associate{it.groupValues[1].lowercase() to it.groupValues[2]}
     group=attrs["group-title"]?.takeIf{it.isNotBlank()}?:"عام"; logo=attrs["tvg-logo"]?.takeIf{it.isNotBlank()}
     name=line.substringAfterLast(',',"").trim().ifBlank{attrs["tvg-name"].orEmpty()}
    }
    line.startsWith("http://",true)||line.startsWith("https://",true)->{result+=Channel(name?.takeIf{it.isNotBlank()}?:"قناة "+(result.size+1),line,group,logo);name=null;group="عام";logo=null}
   }
  };result
 }
}
