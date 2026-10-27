package com.alrashid.iptv
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
class PlayerActivity:Activity(){
 private var player:ExoPlayer?=null
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
  val root=FrameLayout(this).apply{setBackgroundColor(Color.BLACK)}
  val view=PlayerView(this);root.addView(view,FrameLayout.LayoutParams(-1,-1))
  val title=TextView(this).apply{text=intent.getStringExtra("name")?:"Live TV";textSize=16f;setTextColor(Color.WHITE);setPadding(20,16,20,16);setBackgroundColor(0x77000000)}
  root.addView(title,FrameLayout.LayoutParams(-1,-2,Gravity.TOP));setContentView(root)
  val stream=intent.getStringExtra("url")?:run{finish();return}
  player=ExoPlayer.Builder(this).build().also{exo->view.player=exo;exo.addListener(object:Player.Listener{override fun onPlayerError(error:PlaybackException){title.text="تعذر التشغيل: "+error.errorCodeName}});exo.setMediaItem(MediaItem.fromUri(stream));exo.prepare();exo.playWhenReady=true}
 }
 override fun onStop(){super.onStop();player?.pause()}
 override fun onDestroy(){player?.release();player=null;super.onDestroy()}
}
