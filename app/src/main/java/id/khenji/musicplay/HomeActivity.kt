package id.khenji.musicplay

import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import eightbitlab.com.blurview.BlurAlgorithm
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import com.squareup.picasso.Callback as CB

class HomeActivity : AppCompatActivity() {

    private lateinit var songs: ArrayList<Song>
    private lateinit var shuffleSong: ArrayList<Song>
    private lateinit var currentSong: KSong
    private lateinit var fab: FloatingActionButton
    private lateinit var body: String
    private lateinit var song_name: TextView
    private lateinit var song_details: TextView
    private lateinit var seekbar_song: SeekBar
    private lateinit var play: ImageButton
    private lateinit var next: ImageButton
    private lateinit var prev: ImageButton
    private lateinit var repeat: ImageButton
    private lateinit var shufle: ImageButton
    private lateinit var thumb: ImageView
    private lateinit var mp: MediaPlayer
    private lateinit var blurView: BlurView
    private lateinit var startTime: TextView
    private lateinit var endTime: TextView
    private val myHandler: Handler = Handler()

    companion object {
        var isReady = false
        var isPause = false
        var isPlay = false
        var isShuffle = false
        var isRepeat = false
        var isStart = false
        var isTouch = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.statusBarColor = Color.TRANSPARENT
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

        this.fab = findViewById(R.id.fabs)
        this.next = findViewById(R.id.btn_next)
        this.prev = findViewById(R.id.btn_prev)
        this.repeat = findViewById(R.id.btn_repeat)
        this.play = findViewById(R.id.btn_play)
        this.shufle = findViewById(R.id.btn_shufle)
        this.thumb = findViewById(R.id.thumbnail)
        this.song_name = findViewById(R.id.song)
        this.song_details = findViewById(R.id.song_details)
        this.seekbar_song = findViewById(R.id.seekbar)
        this.songs = ArrayList()
        this.currentSong = KSong()
        this.shuffleSong = ArrayList()
        this.mp = MediaPlayer()
        this.blurView = findViewById(R.id.blurView)
        this.blurView.applyBlur();
        this.seekbar_song.thumb.mutate().alpha = 0
        this.endTime = findViewById(R.id.endtime)
        this.startTime = findViewById(R.id.starttime)

        mp.setAudioStreamType(AudioManager.STREAM_MUSIC)
        shufle.setColorFilter(R.color.black)
        repeat.setColorFilter(R.color.black)
        Glide.with(this)
            .load(R.drawable.nanya)
            .into(thumb)
        var ready = getSongs()


        fab!!.setOnClickListener { view ->
            if (isReady){
                setReady()
            }
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        play.setOnClickListener { view ->
            if (!isPlay){
                isPlay = true
                play.setImageResource(R.drawable.pause)
                if(isPause){
                    mp.start()
                } else {
                    try {
                        mp.reset()
                        mp.setDataSource(currentSong.stream)
                        mp.prepare()
                        mp.start()
                    } catch (e: Exception) {
                        mp.release()
                        Toast.makeText(this@HomeActivity, e.toString(), Toast.LENGTH_SHORT).show()
                    }
                    Toast.makeText(
                        this@HomeActivity,
                        "belum dimulai, dan memulai",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                isPlay = false
                isPause = true
                play.setImageResource(R.drawable.play)
                mp.pause()
            }
        }
        mp.setOnCompletionListener {
            myHandler.removeCallbacks(this.updateTimeline)
            isPlay = false
            isPause = false
            seekbar_song.progress = 0
            startTime.text = "0:00"
            endTime.text = "0.00"
            song_name.text = "Kamu nanyea?"
            song_details.text = "Kamu bertanya tanya?"
            thumb.setImageResource(R.drawable.rawrr)
            play.setImageResource(R.drawable.play)
            Toast.makeText(this,"finish song", Toast.LENGTH_SHORT).show()
        }
        mp.setOnPreparedListener {
            var timeStart = mp.currentPosition
            var timeEnd = mp.duration
            if(!isStart) seekbar_song.max = timeEnd.toInt(); isStart = true
            var firsttime = TimeUnit.MILLISECONDS.toSeconds(timeStart.toLong()) -
                        TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(
                                timeStart.toLong()
                            )
                        )
            var seconds = TimeUnit.MILLISECONDS.toSeconds(timeEnd.toLong()) -
                    TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(
                            timeEnd.toLong()
                        )
                    )
            var textTime: String = String.format(
                "%d:%s",
                TimeUnit.MILLISECONDS.toMinutes(timeStart.toLong()),
                if (firsttime < 10) "0$firsttime" else firsttime
            )
            var textTime2: String = String.format(
                "%d:%s",
                TimeUnit.MILLISECONDS.toMinutes(timeEnd.toLong()),
                if (seconds < 10) "0$seconds" else seconds
            )

            startTime.text = textTime
            endTime.text = textTime2
            seekbar_song.progress = timeStart as Int
            myHandler.postDelayed(updateTimeline, 100)
        }
//        mp.setOnErrorListener { mplayer, what, i2 ->
//            if(what == MediaPlayer.MEDIA_ERROR_SERVER_DIED){
//                mplayer.reset()
//            } else if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN){
//                mplayer.reset()
//                mplayer.setOnErrorListener(this@HomeActivity.mp)
//                mplayer.setOnCompletionListener(this@HomeActivity)
//                mplayer.setOnPreparedListener(this@HomeActivity)
//            }
//            return true
//        }
        seekbar_song.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(p0: SeekBar?) {
                isTouch = true
            }

            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if(isTouch) mp.seekTo(p1)
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                p0?.thumb?.mutate()?.alpha = 0
                isTouch = false
            }
        })

        shufle.setOnClickListener {
            if (!isShuffle){
                isShuffle = true
                shufle.clearColorFilter()
            } else {
                isShuffle = false
                shufle.setColorFilter(R.color.black)
            }
        }
        repeat.setOnClickListener {
            if (!isRepeat){
                isRepeat = true
                mp.isLooping = true
                repeat.clearColorFilter()
            } else {
                isRepeat = false
                mp.isLooping = false
                repeat.setColorFilter(R.color.black)
            }
        }

    }

    private fun setReady(){
        var rand: Int = Random.nextInt(0,songs.size)
        currentSong.setSong(songs[rand].song,songs[rand].song_details,songs[rand].thumb,songs[rand].stream)
        setCurrent(currentSong)
    }

    private fun setCurrent(list: KSong){
        // set thumbnail
        Picasso.get()
            .load(list.thumb)
            .networkPolicy(NetworkPolicy.OFFLINE)
            .placeholder(R.drawable.rawrr)
            .resize(200, 200)
            .centerCrop()
            .error(R.drawable.sunflower)
            .into(thumb, object: CB {
                override fun onSuccess() {}
                override fun onError(e: Exception) {
                    Toast.makeText(this@HomeActivity, "Failed load offline, now load online", Toast.LENGTH_SHORT).show()
                    Picasso.get()
                        .load(list.thumb)
                        .error(R.drawable.sunflower)
                        .into(thumb, object: CB {
                            override fun onSuccess() {}
                            override fun onError(e: Exception) {
                                Toast.makeText(this@HomeActivity, e.toString(), Toast.LENGTH_SHORT).show()
                            }
                        })
                }
            })
        this.song_name.text = list.song
        this.song_details.text = list.song_details
    }

    private fun getSongs() {
        val retro = Retrofit.Builder()
            .baseUrl(
                "https://raw.githubusercontent.com/npdkdev/simple-music-player/main/"
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val retroAPI = retro.create(API::class.java)
        val call: Call<ArrayList<Song>?>? = retroAPI.showSongs()
        call!!.enqueue(object: Callback<ArrayList<Song>?> {
            override fun onResponse(
                calls: Call<ArrayList<Song>?>,
                response: Response<ArrayList<Song>?>) {
                if (response.isSuccessful) {
                    songs = response.body()!!
                    isReady = true
                }
            }
            override fun onFailure(calls: Call<ArrayList<Song>?>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Fail to get the data..", Toast.LENGTH_SHORT)
                    .show()
                Log.d("ayush: ", t.toString())
            }
        })
    }

    private fun BlurView.applyBlur(){
        val radius = 20f
        val decorView = window.decorView
        // ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
        val rootView = decorView.findViewById<View>(android.R.id.content) as ViewGroup
        val windowBackground = decorView.background
        val blurEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            RenderEffect.createBlurEffect(x, y, Shader.TileMode.MIRROR)
        } else {
            RenderScriptBlur(this@HomeActivity)
        }
        this.setupWith(rootView) // or RenderEffectBlur
            .setBlurAlgorithm(blurEffect as BlurAlgorithm?)
            .setFrameClearDrawable(windowBackground) // Optional
            .setBlurRadius(radius)
    }
    private val updateTimeline: Runnable = object : Runnable {
        override fun run() {
            var timeStart = mp.currentPosition
            var timeEnd = mp.duration
            var firsttime = TimeUnit.MILLISECONDS.toSeconds(timeStart.toLong()) -
                    TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(
                            timeStart.toLong()
                        )
                    )
            var textTime: String = String.format(
                "%d:%s",
                TimeUnit.MILLISECONDS.toMinutes(timeStart.toLong()),
                if (firsttime < 10) "0$firsttime" else firsttime
            )
            startTime.text = textTime
            seekbar_song.progress = timeStart.toInt()
            if (seekbar_song.progress == seekbar_song.max){
                //play.callOnClick()
            }
            myHandler.postDelayed(this, 100)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mp != null) {
            mp.release()
        }
    }
}

