package com.deepesh.speechdemo;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,RecognitionListener, SensorEventListener,LocationListener{

    //@InjectView(R.id.textView)
    TextView txt;

    //@InjectView(R.id.buttonSpeek)
    Button btn;

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 2;


    SpeechRecognizer speeach;
    TextToSpeech tts;

    //SensorManager sMgr;
    //Sensor sensor;

    ProgressDialog progressDialog;

    WifiManager wifiManager;

    LocationManager lMgr;
    Double latitude, longitude;
    List<Address> list;
    StringBuffer bufferLocation;
    Geocoder geocoder;
    String loc;

    float batteryTemp;
    IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt=(TextView)findViewById(R.id.textView);
        btn=(Button)findViewById(R.id.buttonSpeek);
        //sMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        //sensor = sMgr.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        //sMgr.registerListener(this, sensor, sMgr.SENSOR_DELAY_NORMAL);

        if (Build.VERSION.SDK_INT >= 23) {
            // Pain in A$$ Marshmallow+ Permission APIs
            checkAndRequestPermissions();

        } else {
            // Pre-Marshmallow
            setUpView();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    private void setUpView(){


        //ButterKnife.inject(this);
        btn.setOnClickListener(this);
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Listning....");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.robotlisticon));

        speeach=SpeechRecognizer.createSpeechRecognizer(this);
        speeach.setRecognitionListener(this);

        tts=new TextToSpeech(this,new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i==TextToSpeech.SUCCESS){
                    //Toast.makeText(MainActivity.this,"tts is Init...",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //Checking permission & Grant Permission at run time
    private  boolean checkAndRequestPermissions() {
        int permissionSendMessage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO);
        int internet = ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET);
        int permissionStorage1 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE);
        int locationPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (internet != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.INTERNET);
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.RECORD_AUDIO);
        }
        if (permissionStorage1 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.CALL_PHONE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        setUpView();

        return true;
    }


    @Override
    public void onClick(View view) {
        int id =view.getId();
        if(id==R.id.buttonSpeek){
            speeach.startListening(RecognizerIntent.getVoiceDetailsIntent(this));
        }

    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    @Override
    public void onBeginningOfSpeech() {
        progressDialog.show();
    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {
        progressDialog.dismiss();
    }

    @Override
    public void onError(int i) {

    }

    @Override
    public void onResults(Bundle bundle) {
        ArrayList<String> resultList=bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if(resultList !=null && resultList.size()>0){
            String output=resultList.get(0);
            txt.setText(output);
            /*//In case of Empty Output
            if(output.toLowerCase().isEmpty()==true){
                tts.speak("Sorry ! Speak Again !",TextToSpeech.QUEUE_FLUSH,null);

            }*/

            //for Setting
            //WIFI
            if((output.toLowerCase().contains("wi-fi")&&output.toLowerCase().contains("setting"))||(output.toLowerCase().contains("wifi")&&output.toLowerCase().contains("setting"))){
                //tts.speak("yes..! no doubt!",TextToSpeech.QUEUE_FLUSH,null);
                Intent intent=new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);

            }
            if((output.toLowerCase().contains("turn")&&output.toLowerCase().contains("on")&&output.toLowerCase().contains("wi-fi"))||(output.toLowerCase().contains("start")&&output.toLowerCase().contains("wi-fi"))||(output.toLowerCase().contains("open")&&output.toLowerCase().contains("wi-fi"))||(output.toLowerCase().contains("open")&&output.toLowerCase().contains("1"))||(output.toLowerCase().contains("wi-fi")&&output.toLowerCase().contains("enable"))||(output.toLowerCase().contains("start")&&output.toLowerCase().contains("wifi"))||(output.toLowerCase().contains("open")&&output.toLowerCase().contains("wifi"))||(output.toLowerCase().contains("turn")&&output.toLowerCase().contains("on")&&output.toLowerCase().contains("wifi"))){
                tts.speak("now your wifi is enable",TextToSpeech.QUEUE_FLUSH,null);
                wifiManager=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiManager.setWifiEnabled(true);

            }
            if((output.toLowerCase().contains("turn")&&output.toLowerCase().contains("on")&&output.toLowerCase().contains("wifi"))||(output.toLowerCase().contains("turn")&&output.toLowerCase().contains("off")&&output.toLowerCase().contains("wi-fi"))||(output.toLowerCase().contains("stop")&&output.toLowerCase().contains("wi-fi"))||(output.toLowerCase().contains("close")&&output.toLowerCase().contains("wi-fi"))||(output.toLowerCase().contains("wi-fi")&&output.toLowerCase().contains("disable"))||(output.toLowerCase().contains("stop")&&output.toLowerCase().contains("wifi"))||(output.toLowerCase().contains("close")&&output.toLowerCase().contains("wifi"))){
                tts.speak("now your wifi is disable!",TextToSpeech.QUEUE_FLUSH,null);
                wifiManager=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiManager.setWifiEnabled(false);

            }

            //BLUETOOTH
            if((output.toLowerCase().contains("open")&&output.toLowerCase().contains("bluetooth")&&output.toLowerCase().contains("setting"))||output.toLowerCase().contains("bluetooth")&&output.toLowerCase().contains("setting")||(output.toLowerCase().contains("open")&&output.toLowerCase().contains("bluetooth")&&output.toLowerCase().contains("sitting"))||output.toLowerCase().contains("bluetooth")&&output.toLowerCase().contains("sitting")){
                //tts.speak("yes..! no doubt!",TextToSpeech.QUEUE_FLUSH,null);
                Intent intent=new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);


            }
            if((output.toLowerCase().contains("turn")&&output.toLowerCase().contains("on")&&output.toLowerCase().contains("bluetooth"))||(output.toLowerCase().contains("start")&&output.toLowerCase().contains("bluetooth"))||(output.toLowerCase().contains("open")&&output.toLowerCase().contains("bluetooth"))||(output.toLowerCase().contains("enable")&&output.toLowerCase().contains("bluetooth"))){
                tts.speak("now your bluetooth is enable",TextToSpeech.QUEUE_FLUSH,null);
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!mBluetoothAdapter.isEnabled()){
                    mBluetoothAdapter.enable();
                }

            }
            if((output.toLowerCase().contains("turn")&&output.toLowerCase().contains("off")&&output.toLowerCase().contains("bluetooth"))||(output.toLowerCase().contains("stop")&&output.toLowerCase().contains("bluetooth"))||(output.toLowerCase().contains("close")&&output.toLowerCase().contains("bluetooth"))||(output.toLowerCase().contains("disable")&&output.toLowerCase().contains("bluetooth"))){
                tts.speak("now your bluetooth is disable!",TextToSpeech.QUEUE_FLUSH,null);
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                }

            }

            //Location
            if((output.toLowerCase().contains("turn")&&output.toLowerCase().contains("on")&&output.toLowerCase().contains("gps"))||(output.toLowerCase().contains("start")&&output.toLowerCase().contains("location"))||(output.toLowerCase().contains("enable")&&output.toLowerCase().contains("gps"))||(output.toLowerCase().contains("start")&&output.toLowerCase().contains("gps"))||(output.toLowerCase().contains("open")&&output.toLowerCase().contains("location"))||(output.toLowerCase().contains("enable")&&output.toLowerCase().contains("location"))){
                LocationManager locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
                boolean locationStatus=locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);//make sure Access_file_location declare in manifest file
                if(locationStatus==true){
                    tts.speak("Location is already Enabled !",TextToSpeech.QUEUE_FLUSH,null);
                }else {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            }



            //Current Time
            if(output.toLowerCase().contains("current")&&output.toLowerCase().contains("time")) {
                //String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                Calendar c = Calendar.getInstance();

                int seconds = c.get(Calendar.SECOND);
                int minutes = c.get(Calendar.MINUTE);
                int hour = c.get(Calendar.HOUR);
                String time = hour + ":" + minutes + ":" + seconds;

                tts.speak(time, TextToSpeech.QUEUE_FLUSH, null);
                txt.setText(time);
            }

            //Current date
            if(output.toLowerCase().contains("current")&&output.toLowerCase().contains("date")) {
                //String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                Calendar c = Calendar.getInstance();

                int day = c.get(Calendar.DAY_OF_MONTH);
                int month = c.get(Calendar.MONTH);
                int year = c.get(Calendar.YEAR);
                String date = day + " / " + month + " / " + year;
                tts.speak(date, TextToSpeech.QUEUE_FLUSH, null);
                txt.setText(date);
            }

            //Current Time & Location
            if(output.toLowerCase().contains("current")&&output.toLowerCase().contains("time")&&output.toLowerCase().contains("date")) {
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                tts.speak(currentDateTimeString, TextToSpeech.QUEUE_FLUSH, null);
                txt.setText(currentDateTimeString);
            }


            //Current Location
            if(output.toLowerCase().contains("current")&&output.toLowerCase().contains("location")){

                lMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                }else {
                    lMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 5, this);
                }


            }
            //play my Favorite song
            if((output.toLowerCase().contains("play")&&output.toLowerCase().contains("favourite")&&output.toLowerCase().contains("song"))||(output.toLowerCase().contains("play")&&output.toLowerCase().contains("favorite")&&output.toLowerCase().contains("song"))||(output.toLowerCase().contains("favourite")&&output.toLowerCase().contains("song"))||(output.toLowerCase().contains("favorite")&&output.toLowerCase().contains("song"))){
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);//ACTION_VIEW- use to open any types of file
                File file = new File("sdcard/my feb.mp3");//intent.setDataAndType(Uri.parse("/mnt/sdcard/xxx/xxx/Pictures/xxx.jpg"), "image/*");
                intent.setDataAndType(Uri.fromFile(file), "audio/*");//
                startActivity(intent);
            }

            if((output.toLowerCase().contains("dil")&&output.toLowerCase().contains("kahe")&&output.toLowerCase().contains("kahaniyan"))){
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);//ACTION_VIEW- use to open any types of file
                File file = new File("sdcard/pehli dafa.mp3");//intent.setDataAndType(Uri.parse("/mnt/sdcard/xxx/xxx/Pictures/xxx.jpg"), "image/*");
                intent.setDataAndType(Uri.fromFile(file), "audio/*");//
                startActivity(intent);
            }
            if((output.toLowerCase().contains("kabhi")&&output.toLowerCase().contains("yaadon")&&output.toLowerCase().contains("mein"))||(output.toLowerCase().contains("kabhi")&&output.toLowerCase().contains("yaadon"))){
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);//ACTION_VIEW- use to open any types of file
                File file = new File("sdcard/kabhi yaadon mein.mp3");//intent.setDataAndType(Uri.parse("/mnt/sdcard/xxx/xxx/Pictures/xxx.jpg"), "image/*");
                intent.setDataAndType(Uri.fromFile(file), "audio/*");//
                startActivity(intent);
            }
            if((output.toLowerCase().contains("khuda")&&output.toLowerCase().contains("bhi")&&output.toLowerCase().contains("jab"))){
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);//ACTION_VIEW- use to open any types of file
                File file = new File("sdcard/khuda bhi jab.mp3");//intent.setDataAndType(Uri.parse("/mnt/sdcard/xxx/xxx/Pictures/xxx.jpg"), "image/*");
                intent.setDataAndType(Uri.fromFile(file), "audio/*");//
                startActivity(intent);
            }
            if(output.toLowerCase().contains("dama")&&output.toLowerCase().contains("dam")&&output.toLowerCase().contains("mast")&&output.toLowerCase().contains("kalandar")){
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);//ACTION_VIEW- use to open any types of file
                File file = new File("sdcard/dum a dum.mp3");//intent.setDataAndType(Uri.parse("/mnt/sdcard/xxx/xxx/Pictures/xxx.jpg"), "image/*");
                intent.setDataAndType(Uri.fromFile(file), "audio/*");//
                startActivity(intent);
            }
            if(output.toLowerCase().contains("jab")&&output.toLowerCase().contains("koi")&&output.toLowerCase().contains("baat")){
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);//ACTION_VIEW- use to open any types of file
                File file = new File("sdcard/jab koi baat bigad jaye.mp3");//intent.setDataAndType(Uri.parse("/mnt/sdcard/xxx/xxx/Pictures/xxx.jpg"), "image/*");
                intent.setDataAndType(Uri.fromFile(file), "audio/*");//
                startActivity(intent);
            }

            ///OPEN APPLICATION
            //Whatsapp
            if((output.toLowerCase().contains("open")&&output.toLowerCase().contains("whatsapp"))||(output.toLowerCase().contains("open")&&output.toLowerCase().contains("whats")&&output.toLowerCase().contains("up"))||(output.toLowerCase().contains("open")&&output.toLowerCase().contains("what's")&&output.toLowerCase().contains("up"))){
                Context ctx=MainActivity.this; // or you can replace **'this'** with your **ActivityName.this**
                try {
                    Intent i = ctx.getPackageManager().getLaunchIntentForPackage("com.whatsapp");
                    ctx.startActivity(i);
                }catch (Exception e){

                }
            }
            //Playstore
            if(output.toLowerCase().contains("open")&&output.toLowerCase().contains("play")&&output.toLowerCase().contains("store")){
                Context ctx=MainActivity.this; // or you can replace **'this'** with your **ActivityName.this**
                try {
                    Intent i = ctx.getPackageManager().getLaunchIntentForPackage("com.android.vending");
                    ctx.startActivity(i);
                }catch (Exception e){

                }
            }
            //ShareIt
            if(output.toLowerCase().contains("open")&&output.toLowerCase().contains("Shareit")){
                Context ctx=MainActivity.this; // or you can replace **'this'** with your **ActivityName.this**
                try {
                    Intent i = ctx.getPackageManager().getLaunchIntentForPackage("com.lenovo.anyshare.gps");
                    ctx.startActivity(i);
                }catch (Exception e){

                }
            }
            //Browser
            if(output.toLowerCase().contains("open")&&output.toLowerCase().contains("browser")){
                Context ctx=MainActivity.this; // or you can replace **'this'** with your **ActivityName.this**
                try {
                    Intent i = ctx.getPackageManager().getLaunchIntentForPackage("com.android.browser");
                    ctx.startActivity(i);
                }catch (Exception e){

                }
            }
            //Google Chrome
            if((output.toLowerCase().contains("open")&&output.toLowerCase().contains("google")&&output.toLowerCase().contains("chrome"))||(output.toLowerCase().contains("open")&&output.toLowerCase().contains("chrome"))){
                Context ctx=MainActivity.this; // or you can replace **'this'** with your **ActivityName.this**
                try {
                    Intent i = ctx.getPackageManager().getLaunchIntentForPackage("com.android.chrome");
                    ctx.startActivity(i);
                }catch (Exception e){

                }
            }
            //Contact
            if((output.toLowerCase().contains("open")&&output.toLowerCase().contains("contact")&&output.toLowerCase().contains("list"))||(output.toLowerCase().contains("open")&&output.toLowerCase().contains("contact"))){
                Context ctx=MainActivity.this; // or you can replace **'this'** with your **ActivityName.this**
                try {
                    Intent i = ctx.getPackageManager().getLaunchIntentForPackage("com.android.contacts");
                    ctx.startActivity(i);
                }catch (Exception e){

                }
            }
            //Gallery
            if((output.toLowerCase().contains("open")&&output.toLowerCase().contains("gallery"))||(output.toLowerCase().contains("open")&&output.toLowerCase().contains("photos"))||(output.toLowerCase().contains("open")&&output.toLowerCase().contains("videos"))||(output.toLowerCase().contains("open")&&output.toLowerCase().contains("photo")&&output.toLowerCase().contains("videos"))){
                Context ctx=MainActivity.this; // or you can replace **'this'** with your **ActivityName.this**
                try {
                    Intent i = ctx.getPackageManager().getLaunchIntentForPackage("com.android.gallery3d");
                    ctx.startActivity(i);
                }catch (Exception e){

                }
            }
            //Gmail
            if(output.toLowerCase().contains("open")&&output.toLowerCase().contains("gmail")){
                Context ctx=MainActivity.this; // or you can replace **'this'** with your **ActivityName.this**
                try {
                    Intent i = ctx.getPackageManager().getLaunchIntentForPackage("com.google.android.gm");
                    ctx.startActivity(i);
                }catch (Exception e){

                }
            }
            //Music player
            if(output.toLowerCase().contains("open")&&output.toLowerCase().contains("music")){
                Context ctx=MainActivity.this; // or you can replace **'this'** with your **ActivityName.this**
                try {
                    Intent i = ctx.getPackageManager().getLaunchIntentForPackage("com.android.music");
                    ctx.startActivity(i);
                }catch (Exception e){

                }
            }
            //Saavan
            if(output.toLowerCase().contains("open")&&output.toLowerCase().contains("saavn")){
                Context ctx=MainActivity.this; // or you can replace **'this'** with your **ActivityName.this**
                try {
                    Intent i = ctx.getPackageManager().getLaunchIntentForPackage("com.saavn.android");
                    ctx.startActivity(i);
                }catch (Exception e){

                }
            }
            //Youtube
            if((output.toLowerCase().contains("open")&&output.toLowerCase().contains("youtube"))){
                Context ctx=MainActivity.this; // or you can replace **'this'** with your **ActivityName.this**
                try {
                    Intent i = ctx.getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
                    ctx.startActivity(i);
                }catch (Exception e){

                }
            }
            //Message
            if((output.toLowerCase().contains("open")&&output.toLowerCase().contains("message"))){
                Context ctx=MainActivity.this; // or you can replace **'this'** with your **ActivityName.this**
                try {
                    Intent i = ctx.getPackageManager().getLaunchIntentForPackage("com.android.mms");
                    ctx.startActivity(i);
                }catch (Exception e){

                }
            }
            //Google drive
            if(output.toLowerCase().contains("open")&&output.toLowerCase().contains("google")&&output.toLowerCase().contains("drive")){
                Context ctx=MainActivity.this; // or you can replace **'this'** with your **ActivityName.this**
                try {
                    Intent i = ctx.getPackageManager().getLaunchIntentForPackage("com.google.android.apps.docs");
                    ctx.startActivity(i);
                }catch (Exception e){

                }
            }
            //Calculator
            if(output.toLowerCase().contains("open")&&output.toLowerCase().contains("calculator")){
                Context ctx=MainActivity.this; // or you can replace **'this'** with your **ActivityName.this**
                try {
                    Intent i = ctx.getPackageManager().getLaunchIntentForPackage("com.android.calculator2");
                    ctx.startActivity(i);
                }catch (Exception e){

                }
            }


            //for Calling
            //meeee
            if((output.toLowerCase().contains("call")&&output.toLowerCase().contains("monu"))||(output.toLowerCase().contains("call")&&output.toLowerCase().contains("deepesh"))||(output.toLowerCase().contains("call")&&output.toLowerCase().contains("boss"))){
                tts.speak("Wait..!",TextToSpeech.QUEUE_FLUSH,null);
                Intent i=new Intent(Intent.ACTION_CALL);
                i.setData(Uri.parse("tel:+919803475225"));

                if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
                    tts.speak("first you need to grant permission to use Auto Calling facility",TextToSpeech.QUEUE_FLUSH,null);
                }else {
                    startActivity(i);
                }
            }

            //Family
            if(output.toLowerCase().contains("call")&&output.toLowerCase().contains("mummy")){
                tts.speak("Wait..!",TextToSpeech.QUEUE_FLUSH,null);
                Intent i=new Intent(Intent.ACTION_CALL);
                i.setData(Uri.parse("tel:+919464431250"));

                if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
                    tts.speak("first you need to grant permission to use Auto Calling facility",TextToSpeech.QUEUE_FLUSH,null);
                }else {
                    startActivity(i);
                }
            }
            if((output.toLowerCase().contains("call")&&output.toLowerCase().contains("dadu"))||(output.toLowerCase().contains("call")&&output.toLowerCase().contains("babaji"))){
                tts.speak("Wait..!",TextToSpeech.QUEUE_FLUSH,null);
                Intent i=new Intent(Intent.ACTION_CALL);
                i.setData(Uri.parse("tel:+919217850104"));

                if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
                    tts.speak("first you need to grant permission to use Auto Calling facility",TextToSpeech.QUEUE_FLUSH,null);
                }else {
                    startActivity(i);
                }
            }
            if((output.toLowerCase().contains("call")&&output.toLowerCase().contains("papa"))||(output.toLowerCase().contains("call")&&output.toLowerCase().contains("papaji"))){
                tts.speak("Wait..!",TextToSpeech.QUEUE_FLUSH,null);
                Intent i=new Intent(Intent.ACTION_CALL);
                i.setData(Uri.parse("tel:+919417049247"));

                if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
                    tts.speak("first you need to grant permission to use Auto Calling facility",TextToSpeech.QUEUE_FLUSH,null);
                }else {
                    startActivity(i);
                }
            }

            if((output.toLowerCase().contains("call")&&output.toLowerCase().contains("bhaiya"))||(output.toLowerCase().contains("call")&&output.toLowerCase().contains("sonu"))||(output.toLowerCase().contains("call")&&output.toLowerCase().contains("deepak"))){
                tts.speak("Wait..!",TextToSpeech.QUEUE_FLUSH,null);
                Intent i=new Intent(Intent.ACTION_CALL);
                i.setData(Uri.parse("tel:+918433757102"));

                if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
                    tts.speak("first you need to grant permission to use Auto Calling facility",TextToSpeech.QUEUE_FLUSH,null);
                }else {
                    startActivity(i);
                }
            }


            if(output.toLowerCase().contains("call")&&output.toLowerCase().contains("ishant")){
                tts.speak("Wait..!",TextToSpeech.QUEUE_FLUSH,null);
                Intent i=new Intent(Intent.ACTION_CALL);
                i.setData(Uri.parse("tel:+91991557177"));

                if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
                    tts.speak("first you need to grant permission to use Auto Calling facility",TextToSpeech.QUEUE_FLUSH,null);
                }else {
                    startActivity(i);
                }
            }
            if(output.toLowerCase().contains("call")&&output.toLowerCase().contains("jivesh")){
                tts.speak("Wait..!",TextToSpeech.QUEUE_FLUSH,null);
                Intent i=new Intent(Intent.ACTION_CALL);
                i.setData(Uri.parse("tel:+919814422549"));

                if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
                    tts.speak("first you need to grant permission to use Auto Calling facility",TextToSpeech.QUEUE_FLUSH,null);
                }else {
                    startActivity(i);
                }
            }




            //how to Grant permission
            if(output.toLowerCase().contains("grant")&&output.toLowerCase().contains("permission")){
                tts.speak("Follow me !! Open setting in your phone ! ! go to App ! ! find kiki ! ! and look carefully ! ! make sure everythings is enabled ! !",TextToSpeech.QUEUE_FLUSH,null);

            }

            //introduction
            //Introduction
            if((output.toLowerCase().contains("hi")&&output.toLowerCase().contains("kiki"))||(output.toLowerCase().contains("hey")&&output.toLowerCase().contains("kiki"))){
                tts.speak("hi ! how can i help you ?",TextToSpeech.QUEUE_FLUSH,null);
            }
            if(output.toLowerCase().contains("how")&&output.toLowerCase().contains("you")){
                tts.speak("I am fine! thank you !",TextToSpeech.QUEUE_FLUSH,null);

            }
            if(output.toLowerCase().contains("your")&&output.toLowerCase().contains("name")){
                tts.speak("my name is kiki!",TextToSpeech.QUEUE_FLUSH,null);

            }
            if((output.toLowerCase().contains("about")&&output.toLowerCase().contains("yourself"))||(output.toLowerCase().contains("apne")&&output.toLowerCase().contains("baare"))||(output.toLowerCase().contains("apne")&&output.toLowerCase().contains("batao"))||(output.toLowerCase().contains("who")&&output.toLowerCase().contains("you"))){
                tts.speak("myself kiki !! I am an  Artificial intelligence ! Created By Mr. monu ! i can help you ! to find somthing ! which you want to know ! ",TextToSpeech.QUEUE_FLUSH,null);

            }
            if(output.toLowerCase().contains("who")&&output.toLowerCase().contains("boss")){
                tts.speak("my boss is Mr. monu! ",TextToSpeech.QUEUE_FLUSH,null);

            }

            if(output.toLowerCase().contains("help")&&output.toLowerCase().contains("me")){
                tts.speak("yes..! how can i help you",TextToSpeech.QUEUE_FLUSH,null);

            }
            if(output.toLowerCase().contains("love")&&output.toLowerCase().contains("you")) {
                tts.speak("I Love  you too !", TextToSpeech.QUEUE_FLUSH, null);
            }


            if((output.toLowerCase().contains("by")&&output.toLowerCase().contains("kiki"))||(output.toLowerCase().contains("bye")&&output.toLowerCase().contains("kiki"))||(output.toLowerCase().contains("ok")&&output.toLowerCase().contains("bye"))){
                tts.speak("bye! see you latter..",TextToSpeech.QUEUE_FLUSH,null);

            }

            //monu
            if((output.toLowerCase().contains("know")&&output.toLowerCase().contains("monu"))||(output.toLowerCase().contains("no")&&output.toLowerCase().contains("monu"))||(output.toLowerCase().contains("monu")&&output.toLowerCase().contains("janti"))||(output.toLowerCase().contains("monu")&&output.toLowerCase().contains("janti"))||(output.toLowerCase().contains("monu")&&output.toLowerCase().contains("pehchante"))||(output.toLowerCase().contains("monu")&&output.toLowerCase().contains("pehchanti"))){
                tts.speak("yes ! he is my boss!",TextToSpeech.QUEUE_FLUSH,null);

            }
            if((output.toLowerCase().contains("about")&&output.toLowerCase().contains("monu"))||(output.toLowerCase().contains("who")&&output.toLowerCase().contains("monu"))||(output.toLowerCase().contains("batao")&&output.toLowerCase().contains("monu"))){
                tts.speak(" Mr. monu is software developer! some people called him Deepesh ! monu is 22 year's old ! he is a Student of MCA ! in GNE college !! he lives in Dugri ! phase 2 ! Ludhiana ! Now a days monu is getting traning at Auribises in Kichnu nagar ! ludhiana ! Mr monu is very nice person",TextToSpeech.QUEUE_FLUSH,null);

            }
            if((output.toLowerCase().contains("about")&&output.toLowerCase().contains("boss"))||(output.toLowerCase().contains("batao")&&output.toLowerCase().contains("boss"))){
                tts.speak("mu boss is Mr. monu ! he is software developer! some people called him Deepesh ! monu is 22 year's old ! he is a Student of MCA in GNE college !! he lives in Dugri ! phase 2 ! Ludhiana ! Now a days monu is getting traning at Auribises in Kichnu nagar ! ludhiana ! my boss is very nice person",TextToSpeech.QUEUE_FLUSH,null);

            }


            ////Anshul
            if((output.toLowerCase().contains("know")&&output.toLowerCase().contains("anshul"))||(output.toLowerCase().contains("no")&&output.toLowerCase().contains("anshul"))||(output.toLowerCase().contains("anshul")&&output.toLowerCase().contains("janti"))||(output.toLowerCase().contains("anshul")&&output.toLowerCase().contains("janti"))||(output.toLowerCase().contains("anshul")&&output.toLowerCase().contains("pehchante"))||(output.toLowerCase().contains("anshul")&&output.toLowerCase().contains("pehchanti"))){
                tts.speak("yes..! i know Anshul!",TextToSpeech.QUEUE_FLUSH,null);

            }
            if((output.toLowerCase().contains("about")&&output.toLowerCase().contains("anshul"))||(output.toLowerCase().contains("batao")&&output.toLowerCase().contains("anshul"))){
                tts.speak(" Anshul is very intelligent boy ! he is a student of B.Com ! now he is get prepration for Cat ! Anshul is 20 years's old ! he lives in Dugri ! Anshul is very nice Persion !",TextToSpeech.QUEUE_FLUSH,null);

            }

            ////Ankush
            if((output.toLowerCase().contains("know")&&output.toLowerCase().contains("ankush"))||(output.toLowerCase().contains("no")&&output.toLowerCase().contains("ankush"))||(output.toLowerCase().contains("ankush")&&output.toLowerCase().contains("janti"))||(output.toLowerCase().contains("ankush")&&output.toLowerCase().contains("janti"))||(output.toLowerCase().contains("ankush")&&output.toLowerCase().contains("pehchante"))||(output.toLowerCase().contains("ankush")&&output.toLowerCase().contains("pehchanti"))){
                tts.speak("yes..! i know Ankush!",TextToSpeech.QUEUE_FLUSH,null);

            }
            if((output.toLowerCase().contains("about")&&output.toLowerCase().contains("ankush"))||(output.toLowerCase().contains("batao")&&output.toLowerCase().contains("ankush"))){
                tts.speak(" Ankush is very Smart boy ! he is a student of plus two !! Ankush is 15 years's old ! he lives in Dugri ! Ankush is very intelligent boy !",TextToSpeech.QUEUE_FLUSH,null);

            }

            ////Sunidhi
            if((output.toLowerCase().contains("know")&&output.toLowerCase().contains("sunidhi"))||(output.toLowerCase().contains("no")&&output.toLowerCase().contains("sunidhi"))||(output.toLowerCase().contains("sunidhi")&&output.toLowerCase().contains("janti"))||(output.toLowerCase().contains("sunidhi")&&output.toLowerCase().contains("janti"))||(output.toLowerCase().contains("sunidhi")&&output.toLowerCase().contains("pehchante"))||(output.toLowerCase().contains("sunidhi")&&output.toLowerCase().contains("pehchanti"))){
                tts.speak("yes..! i know Sunidhi!",TextToSpeech.QUEUE_FLUSH,null);

            }
            if((output.toLowerCase().contains("about")&&output.toLowerCase().contains("sunidhi"))||(output.toLowerCase().contains("batao")&&output.toLowerCase().contains("sunidhi"))){
                tts.speak(" Sunidhi is very intelligent girl ! She is student of B.tech !! She is 17 years's old ! he lives in Dugri ! Sunidhi is always try Somthing different ! ",TextToSpeech.QUEUE_FLUSH,null);

            }


            //Anshul , ankush sunidhi call
            if(output.toLowerCase().contains("call")&&output.toLowerCase().contains("anshul")){
                tts.speak("Wait..!",TextToSpeech.QUEUE_FLUSH,null);
                Intent i=new Intent(Intent.ACTION_CALL);
                i.setData(Uri.parse("tel:+917888556536"));

                if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
                    tts.speak("first you need to grant permission to use Auto Calling facility",TextToSpeech.QUEUE_FLUSH,null);
                }else {
                    startActivity(i);
                }
            }
            if(output.toLowerCase().contains("call")&&output.toLowerCase().contains("ankush")){
                tts.speak("Wait..!",TextToSpeech.QUEUE_FLUSH,null);
                Intent i=new Intent(Intent.ACTION_CALL);
                i.setData(Uri.parse("tel:+918288844170"));

                if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
                    tts.speak("first you need to grant permission to use Auto Calling facility",TextToSpeech.QUEUE_FLUSH,null);
                }else {
                    startActivity(i);
                }
            }
            if(output.toLowerCase().contains("call")&&output.toLowerCase().contains("sunidhi")){
                tts.speak("Wait..!",TextToSpeech.QUEUE_FLUSH,null);
                Intent i=new Intent(Intent.ACTION_CALL);
                i.setData(Uri.parse("tel:+918264207914"));

                if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
                    tts.speak("first you need to grant permission to use Auto Calling facility",TextToSpeech.QUEUE_FLUSH,null);
                }else {
                    startActivity(i);
                }
            }
            if(output.toLowerCase().contains("call")&&output.toLowerCase().contains("kishore")){
                tts.speak("Wait..!",TextToSpeech.QUEUE_FLUSH,null);
                Intent i=new Intent(Intent.ACTION_CALL);
                i.setData(Uri.parse("tel:+919256888037"));

                if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
                    tts.speak("first you need to grant permission to use Auto Calling facility",TextToSpeech.QUEUE_FLUSH,null);
                }else {
                    startActivity(i);
                }
            }


            //introduction to sumit
            //sumit
            if((output.toLowerCase().contains("know")&&output.toLowerCase().contains("sumit"))||(output.toLowerCase().contains("no")&&output.toLowerCase().contains("sumit"))||(output.toLowerCase().contains("sumit")&&output.toLowerCase().contains("janti"))||(output.toLowerCase().contains("sumit")&&output.toLowerCase().contains("janti"))||(output.toLowerCase().contains("sumit")&&output.toLowerCase().contains("pehchante"))||(output.toLowerCase().contains("sumit")&&output.toLowerCase().contains("pehchanti"))){
                tts.speak("yes..! very nicely!  he is very naughty boy!",TextToSpeech.QUEUE_FLUSH,null);

            }
            if((output.toLowerCase().contains("about")&&output.toLowerCase().contains("sumit"))||(output.toLowerCase().contains("batao")&&output.toLowerCase().contains("sumit"))){
                tts.speak("Sumit is very smart boy !he is 10 years old ! Sumit is in Fifth standerd ! his favorite game is Clash of clans ! Sumit is very naughty boy !",TextToSpeech.QUEUE_FLUSH,null);

            }

            if(output.toLowerCase().contains("sumit")&&output.toLowerCase().contains("n******")){
                tts.speak("yes..! no doubt!",TextToSpeech.QUEUE_FLUSH,null);

            }
            if(output.toLowerCase().contains("you")&&output.toLowerCase().contains("sure")){
                tts.speak("yes..! hundred percent sure!",TextToSpeech.QUEUE_FLUSH,null);

            }
            if(output.toLowerCase().contains("i")&&output.toLowerCase().contains("sure")){
                tts.speak("yes..! hundred percent sure!",TextToSpeech.QUEUE_FLUSH,null);

            }
            if(output.toLowerCase().contains("where")&&output.toLowerCase().contains("sumit")){
                tts.speak("Sumit is sitting quietly !",TextToSpeech.QUEUE_FLUSH,null);

            }

            //introduction to Mehak
            //mehak ji
            if((output.toLowerCase().contains("know")&&output.toLowerCase().contains("naina"))||(output.toLowerCase().contains("no")&&output.toLowerCase().contains("naina"))||(output.toLowerCase().contains("mehak")&&output.toLowerCase().contains("janti"))||(output.toLowerCase().contains("mehak")&&output.toLowerCase().contains("janti"))||(output.toLowerCase().contains("mehak")&&output.toLowerCase().contains("pehchante"))||(output.toLowerCase().contains("mehak")&&output.toLowerCase().contains("pehchanti"))){
                tts.speak("yes..! Mehak is my best Friend !  She is very Cute girl ! she look like ! ! angel !",TextToSpeech.QUEUE_FLUSH,null);

            }
            if((output.toLowerCase().contains("about")&&output.toLowerCase().contains("naina"))||(output.toLowerCase().contains("batao")&&output.toLowerCase().contains("mehak"))){
                tts.speak("naina is my best Friend !  She is very Cute girl ! she look like ! ! angel !",TextToSpeech.QUEUE_FLUSH,null);

            }

            if(output.toLowerCase().contains("where")&&output.toLowerCase().contains("mehak")){
                tts.speak("mehak is sitting ! and smiling slowly !",TextToSpeech.QUEUE_FLUSH,null);

            }
            if(output.toLowerCase().contains("best")&&output.toLowerCase().contains("friend")){
                tts.speak("my best friend is naina ! she is very cute !",TextToSpeech.QUEUE_FLUSH,null);

            }
            //Sumit & mehak Call
            if((output.toLowerCase().contains("call")&&output.toLowerCase().contains("sumit"))||(output.toLowerCase().contains("call")&&output.toLowerCase().contains("chachu"))||(output.toLowerCase().contains("call")&&output.toLowerCase().contains("chachi"))){
                tts.speak("Wait..!",TextToSpeech.QUEUE_FLUSH,null);
                Intent i=new Intent(Intent.ACTION_CALL);
                i.setData(Uri.parse("tel:+919256888037"));

                if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
                    tts.speak("first you need to grant permission to use Auto Calling facility",TextToSpeech.QUEUE_FLUSH,null);
                }else {
                    startActivity(i);
                }
            }


        }
    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        /*float[] value = sensorEvent.values;
        float proximity = value[0];

        if (proximity == 0) {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this,"Please Grant Permissions",Toast.LENGTH_LONG).show();
            }else {
                //speeach.startListening(RecognizerIntent.getVoiceDetailsIntent(this));
            }
        }else {

        }*/
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //sMgr.unregisterListener(this);
    }

    /// Current Location


    @Override
    public void onLocationChanged(Location location) {
        latitude=location.getLatitude();
        longitude=location.getLongitude();


        /*txt1.setText(latitude+"  "+longitude);*/
        try{
            geocoder=new Geocoder(this);
            list=geocoder.getFromLocation(latitude,longitude,5);
            if((list!=null)&&(list.size()>0)){
                Address addr=list.get(0);
                bufferLocation=new StringBuffer();
                for(int i=0;i<addr.getMaxAddressLineIndex();i++){
                    bufferLocation.append(addr.getAddressLine(i));
                }
                loc=bufferLocation.toString();
                tts.speak("Now you are! Standing at ! "+loc,TextToSpeech.QUEUE_FLUSH,null);
                txt.setText(loc);
            }
            lMgr.removeUpdates(this); //now i will not repeate again and again
        }catch (Exception e){

        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
