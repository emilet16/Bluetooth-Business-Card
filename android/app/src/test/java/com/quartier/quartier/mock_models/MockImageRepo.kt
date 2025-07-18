package com.quartier.quartier.mock_models

import android.graphics.Bitmap
import android.net.Uri
import com.quartier.quartier.ImageRepository

class MockImageRepo : ImageRepository {
    var path: String = ""

    override fun cropImageTo400(uri: Uri): Bitmap {
        path = uri.toString()
        return Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
    }

    override fun convertToWebPByteArray(bitmap: Bitmap): ByteArray {
        return path.toByteArray(Charsets.UTF_8)
    }
}