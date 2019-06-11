package com.example.musicplayer

import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.IllegalArgumentException
import java.util.*

class MainActivity : AppCompatActivity() {

    private var mainAdapter : MainAdapter? = null
    private val songList : ArrayList<Song> = arrayListOf()
    private val mediaPlayer : MediaPlayer = MediaPlayer()
    private var currentPlay = -1
    private var isPaused = false
    private val handler = Handler()
    private var isSeeking = false

    companion object {
        const val PERMISSION_REQUEST_CODE = 732
        const val updateDelay : Long = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            !=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this@MainActivity,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
        }else{
            loadData()
        }

        listenSeekBar()
        handler.postDelayed(update, updateDelay)

        playButton.setOnClickListener { play() }
        pauseButton.setOnClickListener { pause() }
        nextButton.setOnClickListener { next() }
        previousButton.setOnClickListener { previous() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode== PERMISSION_REQUEST_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                loadData()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun loadData(){
        var songCursor: Cursor?= contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,null,null,null)
        while(songCursor!= null && songCursor.moveToNext()){
            var songName = songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
            var songDuration = songCursor.getLong(songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
            var songPath = songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.DATA))
            songList.add(Song(songName,songDuration,songPath))
        }

        val recyclerView : RecyclerView = findViewById(R.id.mainRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        mainAdapter = MainAdapter(this, songList)
        recyclerView.adapter = mainAdapter
    }

    fun reset(){
        isPaused = false
        mediaPlayer.reset()
    }

    fun setPlayButtonVisible(playButtonVisible: Boolean){
        if(playButtonVisible){
            pauseButton.visibility = View.INVISIBLE
            playButton.visibility = View.VISIBLE
        }else{
            playButton.visibility = View.INVISIBLE
            pauseButton.visibility = View.VISIBLE
        }
    }

    fun pause(){
        setPlayButtonVisible(true)
        isPaused=true
        mediaPlayer.pause()
    }

    fun changeSong(i:Int){
        if(i<0 || i>=songList.size) throw IllegalArgumentException()
        currentPlay = i
        isPaused = false
    }

    fun play(){
        setPlayButtonVisible(false)
        if(isPaused) {
            isPaused = false
            mediaPlayer.start()
            return
        }
        else {
            if (!mediaPlayer.isPlaying) {
                if (currentPlay < 0 || currentPlay >= songList.size) {
                    val random = Random()
                    changeSong(random.nextInt(songList.size))
                }
            }
            mediaPlayer.setDataSource(songList[currentPlay].path)
            mediaPlayer.prepare()
            mediaPlayer.start()
        }
    }

    fun previous(){
        reset()
        currentPlay--
        if(currentPlay<0) changeSong(songList.size-1)
        play()
    }

    fun next(){
        reset()
        currentPlay++
        if(currentPlay>=songList.size) changeSong(0)
        play()
    }

    private val update = object : Runnable{
        override fun run() {
            val currentPosition = mediaPlayer.currentPosition
            val duration = mediaPlayer.duration

            if(currentPosition >= duration){
                setPlayButtonVisible(true)
                reset()
            }

            if(!isSeeking) {

                songSeekBar.progress = ((currentPosition.toDouble() / duration) * songSeekBar.max).toInt()
            }

            handler.postDelayed(this, updateDelay)
        }
    }

    private fun listenSeekBar() {
        songSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                var userSelectedPosition = 0

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    isSeeking = true
                }

                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        userSelectedPosition = progress
                    }
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    isSeeking = false
                    mediaPlayer.seekTo(userSelectedPosition*mediaPlayer.duration/songSeekBar.max)
                }
            }
        )
    }
}
