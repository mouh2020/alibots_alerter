package com.example.alibots_alerter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.openxmlformats.schemas.spreadsheetml.x2006.main.WorkbookDocument

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Storage permission is granted", Toast.LENGTH_LONG).show()
                openFilePicker()
            } else {
                Toast.makeText(this, "Storage permission not granted", Toast.LENGTH_LONG).show()
            }
        }

    private val openFilePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                val intent = Intent(this, PreviewActivity::class.java)
                intent.putExtra("excel_path", uri.toString())
                startActivity(intent)
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        val uploadButton = findViewById<Button>(R.id.upload_btn)

        uploadButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                openFilePicker()
                return@setOnClickListener
            }
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Toast.makeText(this, "Storage permission is already granted", Toast.LENGTH_LONG)
                        .show()
                    openFilePicker()
                }
                else -> {
                    Toast.makeText(this, "Ask for storage permission", Toast.LENGTH_LONG).show()
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") // Filter for .xlsx files
        openFilePickerLauncher.launch(intent)
    }
}
