package wend.web.id.happyplaces.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.launch
import wend.web.id.happyplaces.HappyPlacesApp
import wend.web.id.happyplaces.R
import wend.web.id.happyplaces.databases.HappyPlaceDao
import wend.web.id.happyplaces.databases.HappyPlaceEntity
import wend.web.id.happyplaces.databinding.ActivityAddPlacesBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddPlacesActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityAddPlacesBinding
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var tempImageUri: Uri? = null
    private var details: HappyPlaceEntity? = null

    companion object {
        private const val IMAGE_DIRECTORY = "HappyPlaceImages"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlacesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // toolbar
        setSupportActionBar(binding.tbAddPlace)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Add Places"
        }
        binding.tbAddPlace.setNavigationOnClickListener {
            onBackPressed()
        }

        if (intent.hasExtra(MainActivity.HAPPY_PLACE_ENTITY)) {
            details = intent.getParcelableExtra(MainActivity.HAPPY_PLACE_ENTITY)
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateView()
        }

        if (details == null) {
            updateDateView()
        } else {
            supportActionBar?.title = "Edit Places"
            binding.etDate.setText(details?.date)
            binding.etTitle.setText(details?.title)
            binding.etDescription.setText(details?.description)
            binding.ivPlace.setImageURI(Uri.parse(details?.image))
            tempImageUri = Uri.parse(details?.image)
            binding.etLocation.setText(details?.location)
            binding.btnSave.text = getString(R.string.update)
        }
        binding.etDate.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
        binding.tvAddImage.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val happyPlaceDao = (application as HappyPlacesApp).db.happyPlaceDao()

        when (v!!.id) {
            R.id.etDate -> {
                DatePickerDialog(
                    this@AddPlacesActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            R.id.tvAddImage -> {
                val imageDialog = AlertDialog.Builder(this)
                imageDialog.setTitle("Select action")
                val imageDialogItems = arrayOf(
                    "Select photo from Gallery",
                    "Capture photo from Camera"
                )
                imageDialog.setItems(imageDialogItems) { _, which ->
                    when (which) {
                        0 -> chooseImageFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }.show()
            }
            R.id.btnSave -> addPlace(happyPlaceDao)
        }
    }

    private fun takePhotoFromCamera() {
        Dexter.withContext(this@AddPlacesActivity)
            .withPermission(
                Manifest.permission.CAMERA
            )
            .withListener(object : PermissionListener {
                // called after user check granted or denied
                override fun onPermissionGranted(res: PermissionGrantedResponse?) {
                    if (res != null) {
                        val intent =
                            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        openCameraLauncher.launch(intent)
                    }
                }

                override fun onPermissionDenied(res: PermissionDeniedResponse?) {
                    showDialogSetPermissionFromSettings()
                }

                override fun onPermissionRationaleShouldBeShown(
                    request: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }

            })
            .withErrorListener { error ->
                Log.e(
                    "Dexter Error",
                    "There was an error $error"
                )
            }
            .onSameThread().check()
    }

    private fun chooseImageFromGallery() {
        Dexter.withContext(this@AddPlacesActivity)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                // called after user check granted or denied
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // if all permission is granted then access media store
                    if (report.areAllPermissionsGranted()) {
                        val intent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        openGalleryLauncher.launch(intent)

                        // called after a permission is permanently denied
                    } else if (report.isAnyPermissionPermanentlyDenied) {
                        showDialogSetPermissionFromSettings()
                    } else {
                        Toast.makeText(
                            this@AddPlacesActivity,
                            "Permission is required to access your gallery", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // continue with other permissions request
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            })
            .withErrorListener { error ->
                Log.e(
                    "Dexter Error",
                    "There was an error $error"
                )
            }
            .onSameThread().check()
    }

    private fun showDialogSetPermissionFromSettings() {
        AlertDialog.Builder(this)
            .setMessage("You've turn the permission off. You can set it on Setting")
            .setPositiveButton("Go to Settings") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun updateDateView() {
        val dateFormat = "dd MMM yyyy"
        val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
        binding.etDate.setText(sdf.format(cal.time).toString())
    }

    // open gallery and set ivPlace to selected image
    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val image: Bitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, result.data?.data)
                binding.ivPlace.setImageURI(result.data?.data)
                tempImageUri = saveImageToStorage(image)
            }
        }

    // open camera and set ivPlace to selected image
    private val openCameraLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val image: Bitmap = result.data!!.extras!!.get("data") as Bitmap
                binding.ivPlace.setImageBitmap(image)
                tempImageUri = saveImageToStorage(image)
            }
        }

    private fun saveImageToStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    private fun addPlace(happyPlaceDao: HappyPlaceDao) {
        // run on coroutine
        lifecycleScope.launch {
            if (details == null) { // if new
                Log.e("AddPlacesActivity", "new")
                val insert = happyPlaceDao.insert(
                    HappyPlaceEntity(
                        id = 0,
                        title = binding.etTitle.text.toString(),
                        image = tempImageUri.toString(),
                        description = binding.etDescription.text.toString(),
                        location = binding.etLocation.text.toString(),
                        date = binding.etDate.text.toString(),
                        latitude = 0.00,
                        longitude = 0.00
                    )
                )
                if (insert > 0) {
                    Toast.makeText(
                        this@AddPlacesActivity,
                        "Place added successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(
                        this@AddPlacesActivity,
                        "Place not added",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else { // if edit
                Log.e("Edit", "Edit")
                val update = happyPlaceDao.update(
                    HappyPlaceEntity(
                        id = details?.id!!,
                        title = binding.etTitle.text.toString(),
                        image = tempImageUri.toString(),
                        description = binding.etDescription.text.toString(),
                        location = binding.etLocation.text.toString(),
                        date = binding.etDate.text.toString(),
                        latitude = 0.00,
                        longitude = 0.00
                    )
                )
                if (update > 0) {
                    Toast.makeText(
                        this@AddPlacesActivity,
                        "Place updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(
                        this@AddPlacesActivity,
                        "Place not updated",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}