package com.shashi.task

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import java.io.*
import java.io.File.separator


class MainActivity : AppCompatActivity() {

    private lateinit var image: ImageView
    private lateinit var buttonChoose: Button
    private lateinit var buttonDownload: Button
    private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeUI()

    }

    fun initializeUI() {
        buttonChoose = findViewById(R.id.btnImage)
        buttonDownload = findViewById(R.id.btnDownload)
        image = findViewById(R.id.ivImage)

        buttonChoose.setOnClickListener {
            chooseImage()
        }

        buttonDownload.setOnClickListener {
            saveUriToPhone()
        }
    }

    fun showDownload() {
        buttonDownload.visibility = View.VISIBLE
    }

    fun saveUriToPhone() {
        var bitmap = getContactBitmapFromURI(this, uri)
        bitmap?.saveImage(applicationContext)
        Toast.makeText(this, "Image has been downloaded", Toast.LENGTH_SHORT).show()
    }

    // Save Image
    fun Bitmap.saveImage(context: Context): Uri? {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/test_pictures")
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "img_${SystemClock.uptimeMillis()}")

            val uri: Uri? =
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                saveImageToStream(this, context.contentResolver.openOutputStream(uri))
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, values, null, null)
                return uri
            }
        } else {
            val directory =
                File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        .toString() + separator + "test_pictures"
                )
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = "img_${SystemClock.uptimeMillis()}" + ".jpeg"
            val file = File(directory, fileName)
            saveImageToStream(this, FileOutputStream(file))
            if (file.absolutePath != null) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                // .DATA is deprecated in API 29
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                return Uri.fromFile(file)
            }
        }
        return null
    }

    fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getContactBitmapFromURI(context: Context, uri: Uri?): Bitmap? {
        try {
            val input: InputStream =
                uri?.let { context.contentResolver.openInputStream(it) } ?: return null
            return BitmapFactory.decodeStream(input)
        } catch (e: FileNotFoundException) {
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
        }
        return null
    }

    // Choose Image from Another Activity
    fun chooseImage() {
        ImagePicker.with(this)
            .crop()
            .compress(512)
            .galleryOnly()
            .maxResultSize(
                1080,
                1080
            )
            .start()
    }

    // Get URI result of cropped Image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            uri = data?.data!!

            image.setImageURI(uri)

            showDownload()
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

}