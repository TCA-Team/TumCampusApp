package de.tum.`in`.tumcampusapp.component.tumui.feedback

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import de.tum.`in`.tumcampusapp.component.tumui.feedback.model.Feedback

interface FeedbackContract {

    interface View {
        fun setFeedback(message: String)
        fun showEmptyMessageError()
        fun showSendConfirmationDialog()
        fun showWarning(message: String)
        fun showDialog(title: String, message: String)
        fun showProgressDialog()
        fun showSendErrorDialog()
        fun onFeedbackSent()
        fun openCamera(intent: Intent)
        fun openGallery(intent: Intent)
        fun showPermissionRequestDialog(permission: String, requestCode: Int)
        fun onImageAdded(path: String)
        fun onImageRemoved(position: Int)
    }

    interface Presenter {
        fun attachView(view: FeedbackContract.View)
        fun init(savedInstanceState: Bundle)
        fun initEmail()
        fun getFeedback(): Feedback
        fun getLrzId(): String
        fun removeImage(path: String)
        fun onSendFeedback()
        fun onConfirmSend()
        fun onImageOptionSelected(option: Int)
        fun onNewImageTaken()
        fun onNewImageSelected(uri: Uri?)
        fun takePicture()
        fun openGallery()
        fun detachView()
    }

}
