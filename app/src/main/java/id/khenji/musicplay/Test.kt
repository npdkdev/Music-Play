package id.khenji.musicplay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import id.khenji.musicplay.R
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.graphics.drawable.Drawable
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur

internal class Test : AppCompatActivity() {
    private lateinit var blurView: BlurView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        }

    }

}