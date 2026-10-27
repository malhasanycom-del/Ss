package com.alrashid.iptv
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.text.InputType
import android.widget.*
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors
class MainActivity:Activity(){
 private val bg=Color.rgb(9,13,22);private val panel=Color.rgb(21,29,44);private val gold=Color.rgb(216,180,106)
 private val channels=mutableListOf<Channel>();private val favorites=mutableSetOf<String>();private val worker=Executors.newSingleThreadExecutor();private val main=Handler(Looper.getMainLooper())
 private lateinit var list:LinearLayout;private lateinit var status:TextView;private lateinit var search:EditText
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);window.statusBarColor=bg;window.navigationBarColor=bg;buildUi()}
 private fun buildUi(){
  val root=LinearLayout(this).apply{orientation=1;setBackgroundColor(bg);setPadding(16,12,16,12)}
  val header=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL}
  header.addView(TextView(this).apply{text="آل راشد";textSize=30f;setTextColor(gold);setTypeface(null,1)},LinearLayout.LayoutParams(0,-2,1f));header.addView(btn("＋ مصدر"){sourceMenu()});root.addView(header)
  root.addView(TextView(this).apply{text="IPTV  •  PREMIUM PLAYER";textSize=11f;setTextColor(Color.LTGRAY);setPadding(2,0,2,12)})
  val actions=LinearLayout(this);actions.addView(btn("رابط M3U"){m3uDialog()});actions.addView(btn("ملف"){pickFile()});actions.addView(btn("Xtream"){xtreamDialog()});actions.addView(btn("Stalker"){stalkerDialog()});root.addView(actions)
  search=EditText(this).apply{hint="ابحث عن قناة أو مجموعة";setHintTextColor(Color.GRAY);setTextColor(Color.WHITE);setSingleLine(true);setBackgroundColor(panel);addTextChangedListener(object:android.text.TextWatcher{override fun beforeTextChanged(s:CharSequence?,start:Int,count:Int,after:Int){};override fun onTextChanged(s:CharSequence?,start:Int,before:Int,count:Int){render()};override fun afterTextChanged(s:android.text.Editable?) {}})}
  root.addView(search,LinearLayout.LayoutParams(-1,-2).apply{topMargin=8;bottomMargin=6});status=TextView(this).apply{text="أضف قائمة تشغيل للبدء";setTextColor(gold);setPadding(2,4,2,8)};root.addView(status)
  val scroll=ScrollView(this);list=LinearLayout(this).apply{orientation=1};scroll.addView(list);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f));setContentView(root);render()
 }
 private fun btn(label:String,action:()->Unit)=Button(this).apply{text=label;textSize=11f;isAllCaps=false;setTextColor(gold);setBackgroundColor(panel);setOnClickListener{action()};layoutParams=LinearLayout.LayoutParams(-2,-2).apply{setMargins(2,2,2,2)}}
 private fun input(h:String,secret:Boolean=false)=EditText(this).apply{hint=h;setTextColor(Color.WHITE);setHintTextColor(Color.GRAY);setSingleLine(true);if(secret)inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD;setPadding(10,8,10,8)}
 private fun form(vararg fields:EditText)=LinearLayout(this).apply{orientation=1;setPadding(20,8,20,0);fields.forEach{addView(it)}}
 private fun sourceMenu(){AlertDialog.Builder(this).setTitle("إضافة مصدر").setItems(arrayOf("رابط M3U/M3U8","Xtream Codes","Stalker Portal","ملف من الجهاز")){_,i->when(i){0->m3uDialog();1->xtreamDialog();2->stalkerDialog();3->pickFile()}}.show()}
 private fun m3uDialog(){val field=input("رابط https://…/playlist.m3u");val d=AlertDialog.Builder(this).setTitle("تحميل قائمة M3U").setView(form(field)).setNegativeButton("إلغاء",null).setPositiveButton("تحميل",null).create();d.setOnShowListener{d.getButton(-1).setOnClickListener{val u=field.text.toString().trim();if(!u.startsWith("http://")&&!u.startsWith("https://")){field.error="أدخل رابطاً صحيحاً";return@setOnClickListener};d.dismiss();status.text="جارٍ تحميل القائمة…";worker.execute{val r=runCatching{M3uParser.parse(fetch(u))};main.post{r.onSuccess{accept(it,"M3U")}.onFailure{fail(it.message)}}}}};d.show()}
 private fun pickFile(){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="*/*"},701)}
 @Deprecated("Compatibility callback") override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){super.onActivityResult(requestCode,resultCode,data);if(requestCode==701&&resultCode==RESULT_OK)data?.data?.let{uri->status.text="جارٍ قراءة الملف…";worker.execute{val r=runCatching{contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use{it.readText()}?:error("تعذر فتح الملف")}.map{M3uParser.parse(it)};main.post{r.onSuccess{accept(it,"الملف")}.onFailure{fail(it.message)}}}}}
 private fun xtreamDialog(){val server=input("عنوان الخادم https://host:port");val user=input("اسم المستخدم");val pass=input("كلمة المرور",true);val d=AlertDialog.Builder(this).setTitle("Xtream Codes").setView(form(server,user,pass)).setNegativeButton("إلغاء",null).setPositiveButton("اتصال",null).create();d.setOnShowListener{d.getButton(-1).setOnClickListener{val base=server.text.toString().trim().trimEnd('/');val u=user.text.toString().trim();val p=pass.text.toString();if((!base.startsWith("http://")&&!base.startsWith("https://"))||u.isBlank()||p.isBlank()){server.error="تحقق من بيانات الدخول";return@setOnClickListener};d.dismiss();status.text="جارٍ الاتصال بـ Xtream…";worker.execute{val r=runCatching{val arr=JSONArray(fetch(base+"/player_api.php?username="+enc(u)+"&password="+enc(p)+"&action=get_live_streams"));buildList{for(i in 0 until arr.length()){val o=arr.getJSONObject(i);val id=o.optString("stream_id");if(id.isNotBlank())add(Channel(o.optString("name","قناة "+id),base+"/live/"+enc(u)+"/"+enc(p)+"/"+id+".ts",o.optString("category_name","قنوات"),o.optString("stream_icon").takeIf{it.isNotBlank()}))}}};main.post{r.onSuccess{accept(it,"Xtream")}.onFailure{fail(it.message)}}}}};d.show()}
 private fun stalkerDialog(){val portal=input("رابط Stalker Portal");val mac=input("MAC حسب مزودك");val d=AlertDialog.Builder(this).setTitle("Stalker Portal").setMessage("يختلف دعم القنوات حسب API الخاص بالمزود.").setView(form(portal,mac)).setNegativeButton("إلغاء",null).setPositiveButton("فحص",null).create();d.setOnShowListener{d.getButton(-1).setOnClickListener{val base=portal.text.toString().trim().trimEnd('/');if(!base.startsWith("http://")&&!base.startsWith("https://")){portal.error="أدخل رابط البوابة";return@setOnClickListener};val m=mac.text.toString().trim();d.dismiss();worker.execute{val r=runCatching{val c=URL(base+"/server/load.php?type=stb&action=handshake&token=").openConnection() as HttpURLConnection;c.connectTimeout=9000;c.readTimeout=9000;c.setRequestProperty("Cookie","mac="+m+"; stb_lang=en; timezone=UTC");val body=c.inputStream.bufferedReader().use{it.readText()};c.disconnect();if(!body.contains("token"))error("لم تُرجع البوابة رمز مصادقة");"نجح فحص الاتصال؛ جلب القنوات يتطلب تكامل API المزود."};main.post{r.onSuccess{status.text=it}.onFailure{fail(it.message)}}}}};d.show()}
 private fun render(){if(!::list.isInitialized)return;list.removeAllViews();val q=if(::search.isInitialized)search.text.toString().trim().lowercase() else "";val items=channels.filter{q.isEmpty()||it.name.lowercase().contains(q)||it.group.lowercase().contains(q)};if(items.isEmpty()){list.addView(TextView(this).apply{text=if(channels.isEmpty())"لا توجد قنوات بعد — أضف رابطاً أو ملفاً" else "لا توجد نتائج";textSize=16f;setTextColor(Color.LTGRAY);gravity=Gravity.CENTER;setPadding(20,50,20,50)});return};items.forEach{ch->val row=LinearLayout(this).apply{orientation=0;gravity=Gravity.CENTER_VERTICAL;setPadding(12,8,12,8);setBackgroundColor(panel);isFocusable=true};val info=LinearLayout(this).apply{orientation=1};info.addView(TextView(this).apply{text=ch.name;textSize=16f;setTextColor(Color.WHITE)});info.addView(TextView(this).apply{text=ch.group;textSize=12f;setTextColor(gold)});row.addView(info,LinearLayout.LayoutParams(0,-2,1f));row.addView(btn(if(favorites.contains(ch.url))"★" else "☆"){if(!favorites.add(ch.url))favorites.remove(ch.url);render()});row.setOnClickListener{startActivity(Intent(this,PlayerActivity::class.java).putExtra("url",ch.url).putExtra("name",ch.name))};list.addView(row,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=5})}}
 private fun accept(items:List<Channel>,source:String){if(items.isEmpty()){fail("لم يتم العثور على قنوات صالحة");return};channels.clear();channels.addAll(items);status.text="تم استيراد "+items.size+" قناة من "+source;render()}
 private fun fail(message:String?){status.text="حدث خطأ: "+(message?:"غير معروف");Toast.makeText(this,status.text,Toast.LENGTH_LONG).show()}
 private fun fetch(address:String):String{val c=URL(address).openConnection() as HttpURLConnection;c.connectTimeout=12000;c.readTimeout=20000;c.setRequestProperty("User-Agent","AlRashidIPTV/1.0");return try{if(c.responseCode !in 200..299)error("HTTP "+c.responseCode);c.inputStream.bufferedReader(Charsets.UTF_8).use{it.readText()}}finally{c.disconnect()}}
 private fun enc(s:String)=URLEncoder.encode(s,"UTF-8")
 override fun onDestroy(){worker.shutdownNow();super.onDestroy()}
}
