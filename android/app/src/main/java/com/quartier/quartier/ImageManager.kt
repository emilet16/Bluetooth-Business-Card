package com.quartier.quartier

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import androidx.core.graphics.createBitmap
import com.quartier.quartier.database.ConnectionsDatabase
import com.quartier.quartier.database.ConnectionsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

interface ImageRepository {
    fun cropImageTo400(uri: Uri) : Bitmap
    fun convertToWebPByteArray(bitmap: Bitmap) : ByteArray
}

class ImageManager @Inject constructor(@ApplicationContext val context: Context) : ImageRepository {
    override fun cropImageTo400(uri: Uri) : Bitmap {
        val source = ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
            decoder.isMutableRequired = true
        }
        val sourceWidth = source.width
        val sourceHeight = source.height

        val croppedBitmap = createBitmap(400, 400)
        val canvas = Canvas(croppedBitmap)

        val scale = maxOf(400f/sourceWidth, 400f/sourceHeight)
        val xTranslation = (400f - sourceWidth*scale)/2f
        val yTranslation = (400f - sourceHeight*scale)/2f

        val transformation = Matrix().apply {
            setScale(scale, scale)
            postTranslate(xTranslation, yTranslation)
        }

        canvas.drawBitmap(source, transformation, Paint(Paint.FILTER_BITMAP_FLAG))
        return croppedBitmap
    }

    override fun convertToWebPByteArray(bitmap: Bitmap) : ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, outputStream)
        return outputStream.toByteArray()
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class ImageModule {
    @Binds
    abstract fun bindImageRepository(imageManager: ImageManager): ImageRepository
}