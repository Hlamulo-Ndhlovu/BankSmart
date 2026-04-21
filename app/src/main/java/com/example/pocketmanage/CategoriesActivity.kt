package com.example.pocketmanage

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketmanage.auth.AuthGuard
import com.example.pocketmanage.data.AppDatabase
import com.example.pocketmanage.data.CategoryEntry
import com.example.pocketmanage.databinding.ActivityCategoriesBinding
import com.example.pocketmanage.util.CategoryNameSuggestions
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoriesBinding
    private lateinit var db: AppDatabase
    private lateinit var entriesAdapter: CategoryEntryAdapter

    private var entryDateMillis: Long = 0L
    private var startDateMillis: Long = 0L
    private var endDateMillis: Long = 0L

    private var pendingPhotoPath: String? = null

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && !pendingPhotoPath.isNullOrBlank()) {
                val path = pendingPhotoPath!!
                binding.previewPhoto.visibility = View.VISIBLE
                val bmp = BitmapFactory.decodeFile(path)
                if (bmp != null) binding.previewPhoto.setImageBitmap(bmp)
            }
        }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchCameraInternal()
            } else {
                Snackbar.make(binding.root, R.string.camera_permission_required, Snackbar.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = AppDatabase.get(this)
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        entryDateMillis = today
        startDateMillis = today
        endDateMillis = today
        refreshDateFields()

        binding.btnAddCategoryType.setOnClickListener { showAddCategoryTypeDialog() }
        refreshCategoryDropdown()

        binding.editEntryDate.setOnClickListener {
            showDatePicker(entryDateMillis) {
                entryDateMillis = it
                refreshDateFields()
            }
        }
        binding.editStartDate.setOnClickListener {
            showDatePicker(startDateMillis) {
                startDateMillis = it
                refreshDateFields()
            }
        }
        binding.editEndDate.setOnClickListener {
            showDatePicker(endDateMillis) {
                endDateMillis = it
                refreshDateFields()
            }
        }

        entriesAdapter = CategoryEntryAdapter { entry ->
            entriesAdapter.selectedId = entry.id
            entryDateMillis = entry.entryDateMillis
            startDateMillis = entry.startDateMillis
            endDateMillis = entry.endDateMillis
            refreshDateFields()
            binding.editDescription.setText(entry.description)
            binding.dropdownCategory.setText(entry.categoryName, false)
            pendingPhotoPath = entry.photoPath
            loadPhotoPreview(entry.photoPath)
        }
        binding.entriesRecycler.layoutManager = LinearLayoutManager(this)
        binding.entriesRecycler.adapter = entriesAdapter

        binding.btnTakePicture.setOnClickListener { requestCameraAndLaunch() }
        binding.btnAdd.setOnClickListener { addEntry() }
        binding.btnDelete.setOnClickListener { deleteSelected() }

        val bottomNav = binding.bottomNav
        bottomNav.selectedItemId = R.id.nav_categories
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
                R.id.nav_categories -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        loadEntries()
    }

    private fun refreshCategoryDropdown(selectName: String? = null) {
        lifecycleScope.launch {
            val names = CategoryNameSuggestions.forCategoryEntries(this@CategoriesActivity, db)
            val adapter = ArrayAdapter(
                this@CategoriesActivity,
                android.R.layout.simple_dropdown_item_1line,
                names,
            )
            binding.dropdownCategory.setAdapter(adapter)
            val resolved = when {
                !selectName.isNullOrBlank() && names.any { it.equals(selectName, ignoreCase = true) } ->
                    names.first { it.equals(selectName, ignoreCase = true) }
                else -> names.firstOrNull()
            }
            if (resolved != null) {
                binding.dropdownCategory.setText(resolved, false)
            }
        }
    }

    private fun showAddCategoryTypeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category_type, null, false)
        val nameEdit = dialogView.findViewById<TextInputEditText>(R.id.editCategoryTypeName)
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.category_add_type)
            .setView(dialogView)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = nameEdit.text?.toString()?.trim().orEmpty()
                if (name.isEmpty()) {
                    Snackbar.make(binding.root, R.string.category_type_empty, Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (!CategoryNameSuggestions.addCustomLabel(this, name)) {
                    Snackbar.make(binding.root, R.string.category_type_exists, Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                dialog.dismiss()
                refreshCategoryDropdown(selectName = name)
                Snackbar.make(binding.root, R.string.category_type_saved, Snackbar.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        AuthGuard.requireSignedIn(this)
    }

    private fun refreshDateFields() {
        binding.editEntryDate.setText(formatUtcMillis(entryDateMillis))
        binding.editStartDate.setText(formatUtcMillis(startDateMillis))
        binding.editEndDate.setText(formatUtcMillis(endDateMillis))
    }

    private fun formatUtcMillis(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(millis))
    }

    private fun showDatePicker(currentUtcMillis: Long, onPicked: (Long) -> Unit) {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setSelection(currentUtcMillis)
            .build()
        picker.addOnPositiveButtonClickListener { selection: Long -> onPicked(selection) }
        picker.show(supportFragmentManager, "category_date")
    }

    private fun requestCameraAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED -> launchCameraInternal()
            else -> requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCameraInternal() {
        val dir = File(filesDir, "pictures").apply { mkdirs() }
        val file = File(dir, "category_${System.currentTimeMillis()}.jpg")
        pendingPhotoPath = file.absolutePath
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file,
        )
        takePictureLauncher.launch(uri)
    }

    private fun loadPhotoPreview(path: String?) {
        if (path.isNullOrBlank()) {
            binding.previewPhoto.visibility = View.GONE
            binding.previewPhoto.setImageDrawable(null)
            return
        }
        val bmp = BitmapFactory.decodeFile(path)
        if (bmp != null) {
            binding.previewPhoto.visibility = View.VISIBLE
            binding.previewPhoto.setImageBitmap(bmp)
        } else {
            binding.previewPhoto.visibility = View.GONE
        }
    }

    private fun addEntry() {
        val category = binding.dropdownCategory.text?.toString()?.trim().orEmpty()
        if (category.isEmpty()) {
            Snackbar.make(binding.root, R.string.fill_required_fields, Snackbar.LENGTH_SHORT).show()
            return
        }
        val desc = binding.editDescription.text?.toString().orEmpty()
        val entry = CategoryEntry(
            entryDateMillis = entryDateMillis,
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            description = desc,
            categoryName = category,
            photoPath = pendingPhotoPath?.takeIf { it.isNotBlank() },
        )
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                db.categoryEntryDao().insert(entry)
            }
            CategoryNameSuggestions.rememberUsedLabel(this@CategoriesActivity, category)
            clearForm()
            loadEntries()
            Snackbar.make(binding.root, R.string.entry_saved, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun deleteSelected() {
        val id = entriesAdapter.selectedId ?: run {
            Snackbar.make(binding.root, R.string.select_entry_to_delete, Snackbar.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            val pathToRemove = withContext(Dispatchers.IO) {
                val row = db.categoryEntryDao().getById(id)
                db.categoryEntryDao().deleteById(id)
                row?.photoPath
            }
            pathToRemove?.let { File(it).delete() }
            clearForm()
            loadEntries()
            Snackbar.make(binding.root, R.string.entry_deleted, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun clearForm() {
        entriesAdapter.selectedId = null
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        entryDateMillis = today
        startDateMillis = today
        endDateMillis = today
        refreshDateFields()
        binding.editDescription.setText("")
        refreshCategoryDropdown()
        pendingPhotoPath = null
        binding.previewPhoto.visibility = View.GONE
        binding.previewPhoto.setImageDrawable(null)
    }

    private fun loadEntries() {
        lifecycleScope.launch {
            val list = withContext(Dispatchers.IO) {
                db.categoryEntryDao().getAll()
            }
            entriesAdapter.submitList(list)
        }
    }
}
