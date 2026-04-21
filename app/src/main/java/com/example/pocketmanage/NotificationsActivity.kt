package com.example.pocketmanage

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pocketmanage.auth.AuthGuard
import com.example.pocketmanage.databinding.ActivityNotificationsBinding
import com.example.pocketmanage.notifications.ReminderPreferences
import com.example.pocketmanage.notifications.ReminderScheduler
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Locale

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private var suppressSwitchCallback = false

    private val postNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                suppressSwitchCallback = true
                binding.reminderSwitch.isChecked = true
                suppressSwitchCallback = false
                ReminderPreferences.setEnabled(this, true)
                ReminderScheduler.schedule(this)
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.notifications_permission_denied,
                    Snackbar.LENGTH_LONG,
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        syncSwitchFromPrefs()
        updateTimeLabel()
        updateReminderLabelAlpha()

        binding.reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (suppressSwitchCallback) return@setOnCheckedChangeListener
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        suppressSwitchCallback = true
                        binding.reminderSwitch.isChecked = false
                        suppressSwitchCallback = false
                        postNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        return@setOnCheckedChangeListener
                    }
                }
                ReminderPreferences.setEnabled(this, true)
                ReminderScheduler.schedule(this)
            } else {
                ReminderPreferences.setEnabled(this, false)
                ReminderScheduler.cancel(this)
            }
            updateReminderLabelAlpha()
        }

        binding.pickTimeButton.setOnClickListener { showTimePicker() }

        binding.openSystemSettings.setOnClickListener { openAppNotificationSettings() }
    }

    override fun onStart() {
        super.onStart()
        AuthGuard.requireSignedIn(this)
    }

    private fun syncSwitchFromPrefs() {
        suppressSwitchCallback = true
        binding.reminderSwitch.isChecked = ReminderPreferences.isEnabled(this)
        suppressSwitchCallback = false
    }

    private fun updateReminderLabelAlpha() {
        val on = ReminderPreferences.isEnabled(this)
        binding.reminderTimeLabel.alpha = if (on) 1f else 0.5f
    }

    private fun updateTimeLabel() {
        val h = ReminderPreferences.getHour(this)
        val m = ReminderPreferences.getMinute(this)
        binding.reminderTimeLabel.text = getString(
            R.string.reminder_time_summary,
            String.format(Locale.getDefault(), "%02d:%02d", h, m),
        )
    }

    private fun showTimePicker() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(ReminderPreferences.getHour(this))
            .setMinute(ReminderPreferences.getMinute(this))
            .setTitleText(R.string.reminder_time_title)
            .build()
        picker.addOnPositiveButtonClickListener {
            ReminderPreferences.setTime(this, picker.hour, picker.minute)
            updateTimeLabel()
            if (ReminderPreferences.isEnabled(this)) {
                ReminderScheduler.schedule(this)
            }
            Snackbar.make(binding.root, R.string.reminder_time_updated, Snackbar.LENGTH_SHORT).show()
        }
        picker.show(supportFragmentManager, "reminder_time")
    }

    private fun openAppNotificationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                },
            )
        } else {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                },
            )
        }
    }
}
