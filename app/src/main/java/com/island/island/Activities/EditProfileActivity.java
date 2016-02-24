package com.island.island.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.island.island.Database.ProfileDatabase;
import com.island.island.Models.Profile;
import com.island.island.Database.IslandDB;
import com.island.island.Models.ProfileWithImageData;
import com.island.island.R;
import com.island.island.Utils.ImageUtils;
import com.island.island.Utils.Utils;
import com.island.island.VersionedContentBuilder;

public class EditProfileActivity extends AppCompatActivity {
    private static String TAG = EditProfileActivity.class.getSimpleName();

    private EditText aboutMe;
    private ImageView profileImage;
    private ImageView headerImage;

    private Uri prevProfileImageUri = null;
    private Uri prevHeaderImageUri = null;
    private Uri profileImageUri = null;
    private Uri headerImageUri = null;

    private static final int SELECT_PROFILE_IMAGE = 1;
    private static final int SELECT_HEADER_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Profile profile = ProfileDatabase.getInstance(
                getApplicationContext()).get(Utils.getUser(this));

        TextView userName = (TextView) findViewById(R.id.profile_user_name);
        profileImage = (ImageView) findViewById(R.id.profile_profile_image);
        headerImage = (ImageView) findViewById(R.id.profile_header_image);
        aboutMe = (EditText) findViewById(R.id.edit_profile_about_me);

        prevProfileImageUri = profile.getProfileImageUri();
        prevHeaderImageUri = profile.getHeaderImageUri();

        userName.setText(profile.getUsername());
        aboutMe.setText(profile.getAboutMe());

        ImageUtils.setProfileImageSampled(getApplicationContext(), profileImage,
                profile.getProfileImageUri());
        ImageUtils.setHeaderImageSampled(getApplicationContext(), headerImage,
                profile.getHeaderImageUri());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PROFILE_IMAGE) {
                profileImageUri = data.getData();
                ImageUtils.setProfileImageSampled(getApplicationContext(),
                        profileImage, profileImageUri);

            }
            else if(requestCode == SELECT_HEADER_IMAGE) {
                headerImageUri = data.getData();
                ImageUtils.setHeaderImageSampled(getApplicationContext(),
                        headerImage, headerImageUri);
            }
        }
    }

    public void saveProfile(View view) {
        String newAboutMeText = aboutMe.getText().toString();
        String myUsername = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString(getApplicationContext().getString(R.string.user_name), "");

        // TODO: Implement separate REST calls for profile/header images
        Uri newProfileUri = profileImageUri == null ? prevProfileImageUri : profileImageUri;
        Uri newHeaderUri = headerImageUri == null ? prevHeaderImageUri : headerImageUri;

        ProfileWithImageData newProfileWithImageData = VersionedContentBuilder.buildProfile(
                getApplicationContext(),
                myUsername,
                newAboutMeText,
                ImageUtils.getByteArrayFromUri(getApplicationContext(), newProfileUri),
                ImageUtils.getByteArrayFromUri(getApplicationContext(), newHeaderUri)
        );

        // TODO: This saves a new image every time. Will change with new REST calls.
        Uri savedProfileImageUri = ImageUtils.saveBitmapToInternalFromUri(getApplicationContext(),
                newProfileUri);
        Uri savedHeaderImageUri = ImageUtils.saveBitmapToInternalFromUri(getApplicationContext(),
                newHeaderUri);

        Profile newProfile = new Profile(
                myUsername,
                newAboutMeText,
                savedProfileImageUri,
                savedHeaderImageUri,
                newProfileWithImageData.getVersion()
        );

        ProfileDatabase.getInstance(getApplicationContext()).update(newProfile);
        IslandDB.postProfile(getApplicationContext(), newProfileWithImageData);

        Snackbar.make(view, getString(R.string.profile_saved), Snackbar.LENGTH_SHORT).show();
    }

    public void chooseProfileImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)),
                SELECT_PROFILE_IMAGE);
    }

    public void chooseHeaderImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)),
                SELECT_HEADER_IMAGE);
    }
}
