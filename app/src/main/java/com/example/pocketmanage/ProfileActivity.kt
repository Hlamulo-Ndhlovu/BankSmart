package com.example.pocketmanage

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.pocketmanage.auth.AuthGuard
import com.example.pocketmanage.auth.LocalAuth
import com.example.pocketmanage.auth.LocalAuthRepository
import com.example.pocketmanage.data.AppDatabase
import com.example.pocketmanage.data.FinanceRepository
import com.example.pocketmanage.util.MoneyFormat
import com.example.pocketmanage.util.ProfileAvatarStorage
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private val pickVisualMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            val userId = LocalAuth.currentUserId() ?: return@registerForActivityResult
            if (uri == null) return@registerForActivityResult
            lifecycleScope.launch {
                val ok = ProfileAvatarStorage.saveFromUri(this@ProfileActivity, userId, uri)
                withContext(Dispatchers.Main) {
                    if (ok) {
                        bindAvatar(userId)
                        snackbar(R.string.profile_photo_updated)
                    } else {
                        snackbar(R.string.profile_photo_error)
                    }
                }
            }
        }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val userId = LocalAuth.currentUserId() ?: return@registerForActivityResult
            if (success) {
                bindAvatar(userId)
                snackbar(R.string.profile_photo_updated)
            }
        }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchCameraCapture()
            } else {
                snackbar(R.string.camera_permission_required)
            }
        }

    private val placeholderPadding: Int by lazy {
        resources.getDimensionPixelSize(R.dimen.profile_avatar_inner_padding)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        val mainView = findViewById<androidx.coordinatorlayout.widget.CoordinatorLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ShapeableImageView>(R.id.profileAvatar).setOnClickListener { showPhotoOptionsDialog() }

        findViewById<MaterialButton>(R.id.buttonEditProfile).setOnClickListener { showEditProfileDialog() }
        findViewById<View>(R.id.notificationsRow).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
        findViewById<View>(R.id.securityRow).setOnClickListener { showChangePasswordDialog() }

        findViewById<View>(R.id.logoutRow).setOnClickListener {
            LocalAuth.signOut()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_profile
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_analysis -> {
                    startActivity(Intent(this, AnalysisActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_accounts -> {
                    startActivity(Intent(this, AccountsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_categories -> {
                    startActivity(Intent(this, CategoriesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun snackbar(msgRes: Int) {
        Snackbar.make(findViewById(R.id.main), msgRes, Snackbar.LENGTH_SHORT).show()
    }

    private fun showPhotoOptionsDialog() {
        val userId = LocalAuth.currentUserId() ?: return
        val hasPhoto = ProfileAvatarStorage.avatarFile(this, userId).exists()
        val options = mutableListOf(
            getString(R.string.profile_photo_gallery),
            getString(R.string.profile_photo_camera),
        )
        if (hasPhoto) {
            options.add(getString(R.string.profile_photo_remove))
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.profile_photo_chooser_title)
            .setItems(options.toTypedArray()) { _, which ->
                when (which) {
                    0 -> pickVisualMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                    1 -> requestCameraAndCapture()
                    2 -> if (hasPhoto) {
                        ProfileAvatarStorage.delete(this, userId)
                        bindAvatar(userId)
                        snackbar(R.string.profile_photo_removed)
                    }
                }
            }
            .show()
    }

    private fun requestCameraAndCapture() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED -> launchCameraCapture()
            else -> requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCameraCapture() {
        val userId = LocalAuth.currentUserId() ?: return
        val file = ProfileAvatarStorage.avatarFile(this, userId)
        file.parentFile?.mkdirs()
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file,
        )
        takePicture.launch(uri)
    }

    private fun bindAvatar(userId: Long) {
        val iv = findViewById<ShapeableImageView>(R.id.profileAvatar)
        val file = ProfileAvatarStorage.avatarFile(this, userId)
        if (!file.exists()) {
            resetAvatarPlaceholder(iv)
            return
        }
        val bmp = ProfileAvatarStorage.decodeForDisplay(file.absolutePath)
        if (bmp != null) {
            iv.setImageBitmap(bmp)
            iv.scaleType = ImageView.ScaleType.CENTER_CROP
            iv.setPadding(0, 0, 0, 0)
            iv.setBackgroundResource(android.R.color.transparent)
        } else {
            resetAvatarPlaceholder(iv)
        }
    }

    private fun resetAvatarPlaceholder(iv: ShapeableImageView) {
        iv.setImageResource(R.drawable.ic_profile_person)
        iv.scaleType = ImageView.ScaleType.CENTER_INSIDE
        iv.setPadding(placeholderPadding, placeholderPadding, placeholderPadding, placeholderPadding)
        iv.setBackgroundResource(R.drawable.profile_avatar_placeholder_circle)
    }

    private fun showEditProfileDialog() {
        val userId = LocalAuth.currentUserId() ?: return
        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) {
                AppDatabase.get(this@ProfileActivity).userDao().getById(userId)
            }
            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null, false)
            val nameEdit = dialogView.findViewById<TextInputEditText>(R.id.editDisplayName)
            nameEdit.setText(user?.displayName.orEmpty())
            MaterialAlertDialogBuilder(this@ProfileActivity)
                .setTitle(R.string.edit_profile)
                .setView(dialogView)
                .setPositiveButton(R.string.save) { _, _ ->
                    val name = nameEdit.text?.toString()?.trim().orEmpty()
                    if (name.isEmpty()) return@setPositiveButton
                    lifecycleScope.launch {
                        val result = LocalAuthRepository.updateDisplayName(this@ProfileActivity, userId, name)
                        if (result.isSuccess) {
                            findViewById<android.widget.TextView>(R.id.profileName).text = name
                            Snackbar.make(
                                findViewById(R.id.main),
                                R.string.profile_updated,
                                Snackbar.LENGTH_SHORT,
                            ).show()
                        }
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    private fun showChangePasswordDialog() {
        val userId = LocalAuth.currentUserId() ?: return
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null, false)
        val currentEt = dialogView.findViewById<TextInputEditText>(R.id.editCurrentPassword)
        val newEt = dialogView.findViewById<TextInputEditText>(R.id.editNewPassword)
        val confirmEt = dialogView.findViewById<TextInputEditText>(R.id.editConfirmPassword)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.security)
            .setView(dialogView)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val current = currentEt.text?.toString().orEmpty()
                val newPass = newEt.text?.toString().orEmpty()
                val confirm = confirmEt.text?.toString().orEmpty()
                if (newPass != confirm) {
                    Snackbar.make(findViewById(R.id.main), R.string.password_mismatch, Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                lifecycleScope.launch {
                    val result = LocalAuthRepository.changePassword(
                        this@ProfileActivity,
                        userId,
                        current,
                        newPass,
                    )
                    if (result.isSuccess) {
                        dialog.dismiss()
                        Snackbar.make(
                            findViewById(R.id.main),
                            R.string.password_changed,
                            Snackbar.LENGTH_SHORT,
                        ).show()
                    } else {
                        val msg = when (result.exceptionOrNull()?.message) {
                            "wrong_password" -> getString(R.string.error_wrong_password)
                            "password_short" -> getString(R.string.error_password_short)
                            else -> getString(R.string.auth_failed)
                        }
                        Snackbar.make(findViewById(R.id.main), msg, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        AuthGuard.requireSignedIn(this)
        val userId = LocalAuth.currentUserId()
        lifecycleScope.launch {
            val user = userId?.let { id ->
                withContext(Dispatchers.IO) { AppDatabase.get(this@ProfileActivity).userDao().getById(id) }
            }
            findViewById<android.widget.TextView>(R.id.profileName).text =
                user?.displayName?.takeIf { it.isNotBlank() } ?: getString(R.string.profile)
            findViewById<android.widget.TextView>(R.id.profileEmail).text =
                user?.email ?: "—"

            FinanceRepository.ensureSeeded()
            val (inc, exp) = FinanceRepository.monthIncomeExpenseCents()
            findViewById<android.widget.TextView>(R.id.profileMonthlyIncome).text =
                MoneyFormat.formatCents(inc)
            findViewById<android.widget.TextView>(R.id.profileMonthlySpent).text =
                MoneyFormat.formatCents(exp)
            findViewById<android.widget.TextView>(R.id.profileBudgetsCount).text =
                FinanceRepository.budgetCategoriesCount().toString()

            userId?.let { bindAvatar(it) }
        }
    }
}
