package com.example.uploadimagetoserver

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.uploadimagetoserver.models.UploadRequestBody
import com.example.uploadimagetoserver.models.UploadResponse
import com.example.uploadimagetoserver.models.getFileName
import com.example.uploadimagetoserver.models.snackbar
import com.example.uploadimagetoserver.network.ApiCall
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URI

class MainActivity : AppCompatActivity(), UploadRequestBody.UploadCallBack {


    //This variable stores the selected image by the user
    private var selectedImage: Uri? = null


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermission()
        setContentView(R.layout.activity_main)


        /**Here we called the openImageFunction by clicking the button, it first check if the permission has been granted or not *
         * if permission has not been granted before, the app request for permission
         * if permission is granted, the openImage function is called
         * User can then choose image to upload
         */
        image_view.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )
            } else {
                openImageChooser()
            }
        }

        //This button appears if user deny permission a for user to be able to ask for permission again
        button_show.setOnClickListener {
            checkPermission()
        }

        //Add click listener to our upload button
        button_upload.setOnClickListener {
            uploadImage()
        }

    }

    //This function uploads the image to server
    private fun uploadImage() {
        if (selectedImage == null) {
            layout_root.snackbar("Choose an image first")
            return
        }

        //We will copy the selected image by the user to the app specific storage
        val parcelFileDescriptor =
            contentResolver.openFileDescriptor(selectedImage!!, "r", null) ?: return

        var file = File(cacheDir, contentResolver.getFileName(selectedImage!!))
        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val outPutStream = FileOutputStream(file)

        inputStream.copyTo(outPutStream)

        //Now we will use this file to Upload it
        progress_bar.progress = 0
        val body = UploadRequestBody(file, "image", this)

        //Here we call the Api and call the function upload image
        ApiCall().uploadImage(
            MultipartBody.Part.createFormData("file", file.name, body),
            RequestBody.create(MediaType.parse("multipart/form-data"), "Image uploaded from device")
        ).enqueue(object : Callback<UploadResponse> {
            override fun onResponse(
                call: Call<UploadResponse>, response: Response<UploadResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@MainActivity,
                        "Message:${response.body().toString()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    progress_bar.progress = 100
                    resultMsg.text = response.body()?.message.toString()
                    resultMsg.visibility = View.VISIBLE
                    layout_root.snackbar(response.body()?.message.toString())
                } else {
                    Toast.makeText(this@MainActivity, "Message:$response", Toast.LENGTH_SHORT)
                        .show()

                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                layout_root.snackbar(t.message!!)
            }

        })
    }

    //Here we create a function that chooses our image from the gallery by clicking the button
    private fun openImageChooser() {
        //Here we use intent to to pick our image with ACTION_PICK
        Intent(Intent.ACTION_PICK).also {
            it.type = "image/*"
            val mineypes = arrayOf("image/jpeg", "image/png")
            it.putExtra(Intent.EXTRA_MIME_TYPES, mineypes)
            startActivityForResult(it, REQUEST_CODE_IMAGE_PICKER)
        }
    }

    //This function gets the selected image by the user
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //This checks if user select an image or not
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_IMAGE_PICKER -> {
                    selectedImage = data?.data
                    image_view.setImageURI(selectedImage)

                }
            }

        }
    }

    override fun onProgressUpdate(percentage: Int) {
        progress_bar.progress = percentage

    }

    //Here, we create a constant value in our companion object
    companion object {
        private const val REQUEST_CODE_IMAGE_PICKER = 100

    }

    //Here we check if permission has been granted or not, if it has not been granted, the user is prompt agin to allow
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        }

    }

    /**This function check for the user response for the request permission and  handles the permission if granted
    /If not granted it display error message and the request for permission button*/
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionHandling()
            } else {
                msg.visibility = View.VISIBLE
                button_show.visibility = View.VISIBLE
            }
        }
    }

    //This function calls the image chooser function after permission has been granted
    fun permissionHandling() {
        msg.visibility = View.GONE
        button_show.visibility = View.GONE
        openImageChooser()
    }

}