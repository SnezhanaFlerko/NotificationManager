package com.example.notificationmanager_flerko

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var notificationManager: NotificationManagerCompat
    private val channelId = "shopping_reminder_channel"
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editTextSeconds = findViewById<EditText>(R.id.editTextSeconds)
        val buttonSetReminder = findViewById<Button>(R.id.buttonSetReminder)

        notificationManager = NotificationManagerCompat.from(this)
        createNotificationChannel()

        buttonSetReminder.setOnClickListener {
            val input = editTextSeconds.text.toString()
            if (input.isNotEmpty()) {
                try {
                    val seconds = input.toInt()
                    if (seconds <= 0) {
                        Toast.makeText(this, "Введите число больше нуля", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    // Переводим секунды в миллисекунды
                    setReminder(seconds * 1000L)

                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Введите корректное число", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Введите время", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setReminder(delayMillis: Long) {
        handler.removeCallbacksAndMessages(null)

        val runnable = Runnable {
            showNotification()
        }

        handler.postDelayed(runnable, delayMillis)

        Toast.makeText(this, "Напоминание через $delayMillis мс", Toast.LENGTH_SHORT).show()
    }

    private fun showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Разрешение на уведомления не получено", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Покупки")
            .setContentText("Время сделать покупки!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(1, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Напоминания о покупках",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Канал для напоминаний о покупках"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}