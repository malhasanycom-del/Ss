package com.alrashid.iptv
import org.junit.Assert.assertEquals
import org.junit.Test
class M3uParserTest { @Test fun parsesPlaylist(){val input="""#EXTM3U
#EXTINF:-1 group-title="Sports",Channel One
https://stream.example/live.m3u8
""";val result=M3uParser.parse(input);assertEquals(1,result.size);assertEquals("Channel One",result[0].name);assertEquals("Sports",result[0].group)} }
