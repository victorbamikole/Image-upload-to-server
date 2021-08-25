package com.example.uploadimagetoserver.models

data class UploadResponse(
    val status: Int,
    val message: String,
    val payload: PayLoad
)

data class PayLoad(
    val fileId: String,
    val fileType: String,
    val fileName: String,
    val downloadUri: String,
    val uploadStatus: Boolean
)
