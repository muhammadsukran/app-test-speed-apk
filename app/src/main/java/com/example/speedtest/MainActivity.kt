package com.example.speedtest

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.speedtest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private var fps: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menjalankan activity dalam mode fullscreen
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        setupUI()

        // Menjalankan animasi di latar belakang
        animateBackground()
    }


    private fun setupUI() {
        binding.btnStartTest.setOnClickListener {
            simulateLag()
            measurePerformance()
        }
    }

    private fun simulateLag() {
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            // Simulasi proses yang lebih berat
            val result = factorial(10000)
            Toast.makeText(this, "Simulating lag... Result: $result", Toast.LENGTH_SHORT).show()
        }
        handler.postDelayed(runnable, 5000)
    }

    private fun factorial(n: Int): Long {
        return if (n == 0) 1 else n.toLong() * factorial(n - 1)
    }

    private fun measurePerformance() {
        measureFPS()
        measureBatteryLevel()
        measurePing()
    }

    private fun measureFPS() {
        // Menampilkan placeholder FPS
        binding.tvFPS.text = "FPS: $fps"
    }

    private fun measureBatteryLevel() {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            applicationContext.registerReceiver(null, ifilter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = (level / scale.toFloat() * 100).toInt()
        binding.tvBattery.text = "Battery Level: $batteryPct%"

        // Menampilkan pesan jika informasi baterai tidak tersedia
        if (level == -1 || scale == -1) {
            Toast.makeText(this, "Battery information not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun measurePing() {
        // Simulasi pengukuran ping
        val pingValue = "50 ms"
        binding.tvPing.text = "Ping: $pingValue"
    }

    private fun animateBackground() {
        val imageView = ImageView(this)

        val layoutParams = binding.root.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        imageView.layoutParams = layoutParams

        binding.root.addView(imageView)

        val animator = ObjectAnimator.ofFloat(imageView, "translationX", 0f, 1000f)
        animator.duration = 5000
        animator.repeatMode = ObjectAnimator.REVERSE
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::handler.isInitialized && ::runnable.isInitialized) {
            handler.removeCallbacks(runnable)
        }
    }
}
