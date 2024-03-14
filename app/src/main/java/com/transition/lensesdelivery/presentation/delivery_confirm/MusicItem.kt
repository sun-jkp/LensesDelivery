package com.transition.lensesdelivery.presentation.delivery_confirm

import android.net.Uri
import androidx.media3.common.MediaItem

data class MusicItem(
    val contentUri: Uri,
    val mediaItem: MediaItem,
    val name: String = "",
)
