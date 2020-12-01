package cn.edu.music

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.jar.Manifest
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    val TAG="MainActivity"
    val mediaPlayer=MediaPlayer()
    val musicList= mutableListOf<String>()
    val musicNameList= mutableListOf<String>()
    var current=0
    var ispause=0
    val Channel_ID="my channel"
    val Notification_ID=1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mediaPlayer.setOnPreparedListener{
            it.start()
        }
        mediaPlayer.setOnCompletionListener {
            current++
            if(current>=musicList.size){
                current=0
            }
            play()
        }
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),0)
        } else{
            getMusicList()
        }
        seekBar.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if(p2){
                    mediaPlayer.seekTo(p1)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })
        thread{
            while(true){
                Thread.sleep(1000)
                runOnUiThread{
                    seekBar.max=mediaPlayer.duration
                    seekBar.progress=mediaPlayer.currentPosition
                }
            }
        }

    }
    fun onplay(v:View){
        play()
    }
    fun onpause(v:View){
        if(ispause==1){
            mediaPlayer.start()
            ispause=0
        }else{
            mediaPlayer.pause()
            ispause=1
        }
    }
    fun onstop(v:View){
        mediaPlayer.stop()
    }
    fun onnext(v:View){
        current++
        if(current>=musicList.size){
            current=0
        }
        play()

    }
    fun onprev(v:View){
        current--
        if(current<0){
            current=musicList.size-1
        }
        play()

    }
    fun play(){

        if(musicList.size==0) return
        val path = musicList[current]
        mediaPlayer.reset()
        try{
            mediaPlayer.setDataSource(path)
            mediaPlayer.prepareAsync()
            mn.text=musicNameList[current]
            count.text="${current+1}/${musicList.size}"
            val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE)as NotificationManager
            val builder:Notification.Builder
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel=NotificationChannel(Channel_ID,"test",NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(notificationChannel)
                builder = Notification.Builder(this,Channel_ID)
            }else{
                builder=Notification.Builder(this)
            }
            val intent = Intent(this,MainActivity::class.java)
            val pendingIntent=PendingIntent.getActivities(this,1, arrayOf(intent),PendingIntent.FLAG_UPDATE_CURRENT)
            val notification=builder.setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("当前歌曲为")
                .setContentText(musicNameList[current])
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            notificationManager.notify(Notification_ID,notification)
        }catch (e:IOException){
            e.printStackTrace()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        getMusicList()
    }
    private fun getMusicList(){
        val cursor=contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,null,null,null)
        if(cursor!=null){
            while (cursor.moveToNext()){
            val musicPath=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
            musicList.add(musicPath)
            val musicName=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
            musicNameList.add(musicName)
                Log.d(TAG,"getMusicList:$musicPath name:$musicName")
            }
            cursor.close()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}
