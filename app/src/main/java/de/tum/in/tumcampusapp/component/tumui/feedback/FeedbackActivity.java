package de.tum.in.tumcampusapp.component.tumui.feedback;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.tumui.feedback.model.Feedback;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.widget.Toast.LENGTH_SHORT;
import static androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL;
import static de.tum.in.tumcampusapp.component.tumui.feedback.FeedbackPresenter.PERMISSION_CAMERA;
import static de.tum.in.tumcampusapp.component.tumui.feedback.FeedbackPresenter.PERMISSION_FILES;
import static de.tum.in.tumcampusapp.component.tumui.feedback.FeedbackPresenter.PERMISSION_LOCATION;
import static de.tum.in.tumcampusapp.component.tumui.feedback.FeedbackPresenter.REQUEST_GALLERY;
import static de.tum.in.tumcampusapp.component.tumui.feedback.FeedbackPresenter.REQUEST_TAKE_PHOTO;

public class FeedbackActivity extends BaseActivity
        implements FeedbackContract.View, FeedbackThumbnailsAdapter.RemoveListener {

    private CheckBox includeEmailCheckbox;
    private CheckBox includeLocationCheckbox;
    private TextInputLayout customEmailViewLayout;
    private TextInputEditText feedbackTextView;
    private TextInputEditText customEmailTextView;

    private FeedbackThumbnailsAdapter thumbnailsAdapter;
    private AlertDialog progressDialog; // TODO Use ProgressDialog

    private FeedbackContract.Presenter presenter;

    public FeedbackActivity() {
        super(R.layout.activity_feedback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String lrzId = Utils.getSetting(this, Const.LRZ_ID, "");
        presenter = new FeedbackPresenter(this, lrzId);
        presenter.attachView(this);

        if (savedInstanceState != null) {
            presenter.init(savedInstanceState);
        }

        feedbackTextView = findViewById(R.id.feedback_message);
        customEmailViewLayout = findViewById(R.id.feedback_custom_email_layout);
        customEmailTextView = findViewById(R.id.feedback_custom_email);
        includeEmailCheckbox = findViewById(R.id.feedback_include_email);
        includeLocationCheckbox = findViewById(R.id.feedback_include_location);

        initFeedbackType();
        initIncludeLocation();
        initPictureGalley();

        if (savedInstanceState == null) {
            presenter.initEmail();
        }
        initIncludeEmail();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Const.FEEDBACK, presenter.getFeedback());
    }

    private void initPictureGalley() {
        RecyclerView pictureList = findViewById(R.id.feedback_image_list);
        pictureList.setLayoutManager(new LinearLayoutManager(this, HORIZONTAL, false));

        List<String> imagePaths = presenter.getFeedback().getPicturePaths();
        thumbnailsAdapter = new FeedbackThumbnailsAdapter(imagePaths, this);
        pictureList.setAdapter(thumbnailsAdapter);

        findViewById(R.id.feedback_add_image).setOnClickListener(view -> showImageOptionsDialog());
    }

    @Override
    public void onThumbnailRemoved(String path) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.picture_dialog, null);

        ImageView imageView = view.findViewById(R.id.feedback_big_image);
        imageView.setImageURI(Uri.fromFile(new File(path)));

        builder.setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.feedback_remove_image, (dialog, i) -> removeThumbnail(path));

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }
        dialog.show();
    }

    private void removeThumbnail(String path) {
        presenter.removeImage(path);
    }

    private void showImageOptionsDialog() {
        String[] options = {
                getString(R.string.feedback_take_picture),
                getString(R.string.gallery)
        };

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.feedback_add_picture)
                .setItems(options, (dialog, index) -> presenter.onImageOptionSelected(index))
                .setNegativeButton(R.string.cancel, null)
                .create();

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }
        alertDialog.show();
    }

    @Override
    public void setFeedback(@NotNull String message) {
        feedbackTextView.setText(message);
    }

    @Override
    public void openCamera(@NotNull Intent intent) {
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    @Override
    public void openGallery(@NotNull Intent intent) {
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void showPermissionRequestDialog(@NotNull String permission, int requestCode) {
        requestPermissions(new String[] { permission }, requestCode);
    }

    @SuppressLint("NewApi")
    private void initIncludeLocation() {
        includeLocationCheckbox.setChecked(presenter.getFeedback().getIncludeLocation());
    }

    private void initIncludeEmail() {
        Feedback feedback = presenter.getFeedback();
        String email = feedback.getEmail();
        includeEmailCheckbox.setChecked(feedback.getIncludeEmail());

        if (presenter.getLrzId().isEmpty()) {
            includeEmailCheckbox.setText(R.string.feedback_include_email);
            customEmailTextView.setText(email);
        } else {
            includeEmailCheckbox.setText(getString(R.string.feedback_include_email_tum_id, email));
        }

        toggleEmailInput();

        includeEmailCheckbox.setOnClickListener(view -> {
            final boolean includesEmail = presenter.getFeedback().getIncludeEmail();
            presenter.getFeedback().setIncludeEmail(!includesEmail);
            toggleEmailInput();
        });
    }

    private void toggleEmailInput() {
        if (includeEmailCheckbox.isChecked() && presenter.getLrzId().isEmpty()) {
            customEmailViewLayout.setVisibility(View.VISIBLE);
        } else {
            customEmailViewLayout.setVisibility(View.GONE);
        }
    }

    private void initFeedbackType() {
        RadioGroup radioGroup = findViewById(R.id.radioButtonsGroup);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String topic = checkedId == R.id.tumInGeneralRadioButton
                    ? Const.FEEDBACK_TOPIC_GENERAL : Const.FEEDBACK_TOPIC_APP;
            presenter.getFeedback().setTopic(topic);
        });

        String topic = presenter.getFeedback().getTopic();
        radioGroup.clearCheck();
        if (topic.equals(Const.FEEDBACK_TOPIC_GENERAL)) {
            radioGroup.check(R.id.tumInGeneralRadioButton);
        } else {
            radioGroup.check(R.id.tumCampusAppRadioButton);
        }
    }

    public void onSendClicked(View view) {
        presenter.onSendFeedback();
    }

    @Override
    public void showEmptyMessageError() {
        feedbackTextView.setError(getString(R.string.feedback_empty));
    }

    @Override
    public void showWarning(@NotNull String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showDialog(@NotNull String title, @NotNull String message) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }

        dialog.show();
    }

    @Override
    public void showProgressDialog() {
        progressDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.feedback_sending)
                .setView(new ProgressBar(this))
                .setCancelable(false)
                .setNeutralButton(R.string.cancel, null)
                .create();

        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }

        progressDialog.show();
    }

    @Override
    public void showSendErrorDialog() {
        progressDialog.dismiss();

        AlertDialog errorDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.feedback_sending_error)
                .setIcon(R.drawable.ic_error_outline)
                .setPositiveButton(R.string.try_again, (dialog, i) -> presenter.getFeedback())
                .setNegativeButton(R.string.ok, null)
                .create();

        if (errorDialog.getWindow() != null) {
            errorDialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }

        errorDialog.show();
    }

    @Override
    public void onFeedbackSent() {
        progressDialog.dismiss();
        Toast.makeText(this, R.string.feedback_send_success, LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void showSendConfirmationDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.send_feedback_question)
                .setPositiveButton(R.string.send, (dialog, i) -> presenter.onConfirmSend())
                .setNegativeButton(R.string.no, null)
                .create();

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow()
                    .setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }

        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                presenter.onNewImageTaken();
                break;
            case REQUEST_GALLERY:
                Uri filePath = data.getData();
                presenter.onNewImageSelected(filePath);
                break;
        }
    }

    @Override
    public void onImageAdded(@NotNull String path) {
        final int thumbnailSize = (int) getResources().getDimension(R.dimen.thumbnail_size);
        thumbnailsAdapter.addImage(path, thumbnailSize);
    }

    @Override
    public void onImageRemoved(int position) {
        thumbnailsAdapter.removeImage(position);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (grantResults.length == 0) {
            return;
        }

        final boolean isGranted = grantResults[0] == PERMISSION_GRANTED;

        switch (requestCode) {
            case PERMISSION_LOCATION: {
                includeLocationCheckbox.setChecked(isGranted);
                return;
            }
            case PERMISSION_CAMERA: {
                if (isGranted) {
                    presenter.takePicture();
                }
                return;
            }
            case PERMISSION_FILES: {
                if (isGranted) {
                    presenter.openGallery();
                }
                return;
            }
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }

}
