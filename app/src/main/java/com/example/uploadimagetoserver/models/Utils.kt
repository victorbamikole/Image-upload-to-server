package com.example.uploadimagetoserver.models

import android.view.View
import com.google.android.material.snackbar.Snackbar
import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns


//This function displays the snackbar
fun View.snackbar(message: String){
    Snackbar.make(
        this,
        message,
        Snackbar.LENGTH_LONG
    ).also { snackbar ->
        snackbar.setAction("OK"){
            snackbar.dismiss()
        }
    }.show()
}

//This function gets the name of the selected image
fun ContentResolver.getFileName(uri: Uri): String{
    var name = ""
    val cursor = query(uri, null, null, null, null)
    cursor?.use {
        it.moveToFirst()
        name = cursor.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
    }
    return name

}