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
import java.io.BufferedReader
import java.io.InputStreamReader

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
        var frameCount = 0
        var startTime = System.nanoTime()
        val refreshInterval = 1000 // dalam milidetik

        val fpsHandler = Handler(Looper.getMainLooper())
        val fpsRunnable = object : Runnable {
            override fun run() {
                val currentTime = System.nanoTime()
                frameCount++
                if ((currentTime - startTime) >= refreshInterval * 1_000_000) {
                    fps = (frameCount * 1_000_000_000L / (currentTime - startTime)).toInt()
                    frameCount = 0
                    startTime = System.nanoTime()
                    binding.tvFPS.text = "FPS: $fps"
                }
                fpsHandler.postDelayed(this, refreshInterval.toLong())
            }
        }
        fpsHandler.post(fpsRunnable)
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
        Thread {
            try {
                val process = Runtime.getRuntime().exec("/system/bin/ping -c 1 www.google.com")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                var pingResult = ""
                while (reader.readLine().also { line = it } != null) {
                    if (line!!.contains("time=")) {
                        val start = line!!.indexOf("time=") + 5
                        val end = line!!.indexOf(" ms", start)
                        pingResult = line!!.substring(start, end) + " ms"
                        break
                    }
                }
                reader.close()
                runOnUiThread {
                    binding.tvPing.text = "Ping: $pingResult"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    binding.tvPing.text = "Ping: Error"
                }
            }
        }.start()
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
