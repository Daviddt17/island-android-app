package io.islnd.android.islnd.app.util;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.models.CommentViewModel;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.models.Comment;

import io.islnd.android.islnd.app.preferences.ThemePreferenceFragment;
import io.islnd.android.islnd.messaging.ServerTime;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;

import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Util {
    private static final String TAG = Util.class.getSimpleName();

    public static String smartTimestampFromUnixTime(Context context, long unixTimeMillis) {
        // currentTimeMillis is already in UTC!
        long currentTime = ServerTime.getCurrentTimeMillis(context) / 1000;
        long timeDiff = currentTime - unixTimeMillis / 1000;

        String timestamp = "";

        // Under 1 minute
        if(timeDiff < 60) {
            timestamp = timeDiff + (timeDiff == 1 ? " sec" : " secs");
        }
        // Under one hour
        else if(timeDiff >= 60 && timeDiff < 3600) {
            long minutes = timeDiff / 60;
            timestamp = minutes + (minutes == 1 ? " min" : " mins");
        }
        // Under 24 hours
        else if(timeDiff >= 3600 && timeDiff < 86400) {
            long hours = timeDiff / 3600;
            timestamp = hours + (hours == 1 ? " hr" : " hrs");
        }
        // Display date of post
        else {
            TimeZone timeZone = TimeZone.getDefault();

            SimpleDateFormat dateFormat = new SimpleDateFormat();
            dateFormat.setTimeZone(timeZone);

            dateFormat.applyPattern("MMM d, yyyy");
            timestamp = dateFormat.format(new Date(unixTimeMillis));
        }

        return timestamp;
    }

    public static String numberOfCommentsString(int numberOfComments) {
        String comments = numberOfComments == 1 ? " Comment" : " Comments";
        return numberOfComments + comments;
    }

    public static boolean isUser(Context context, int userId) {
        return getUserId(context) == userId;
    }

    public static void applyAppTheme(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String value = sharedPref.getString(ThemePreferenceFragment.PREFERENCE_THEME_KEY, "1");

        switch (value) {
            case "1": // Light
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "2": // Dark
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "3": // DayNight Auto
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
                break;
        }
    }

    public static int getUserId(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getInt(context.getString(R.string.user_id), -1);
    }

    public static String getAlias(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(context.getString(R.string.alias), "");
    }

    public static String getPseudonymSeed(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(context.getString(R.string.pseudonym_seed), "");
    }

    public static Key getGroupKey(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return CryptoUtil.decodeSymmetricKey(
                sharedPref.getString(context.getString(R.string.group_key), ""));
    }

    public static Key getPublicKey(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return CryptoUtil.decodePublicKey(
                sharedPref.getString(context.getString(R.string.public_key), ""));
    }

    public static Key getPrivateKey(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return CryptoUtil.decodePrivateKey(
                sharedPref.getString(context.getString(R.string.private_key), ""));
    }

    public static int getEventId(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getInt(context.getString(R.string.event_id), 0);
    }

    public static void setEventId(Context context, int eventId) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(context.getString(R.string.event_id), eventId);
        editor.commit();
    }

    public static String getApiKey(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(context.getString(R.string.api_key), "");
    }

    public static void setApiKey(Context context, String apiKey) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.api_key), apiKey);
        editor.commit();
    }

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static int getDpFromResource(Context context, int res) {
        return (int) Util.dpFromPx(context, context.getResources().getDimension(res));
    }

    public static List<CommentViewModel> buildCommentViewModels(Context context, List<Comment> comments) {
        List<CommentViewModel> commentViewModels = new ArrayList<>();
        for (Comment comment : comments) {
            commentViewModels.add(buildCommentViewModel(null, comment));
        }

        return commentViewModels;
    }

    public static CommentViewModel buildCommentViewModel(Context context, Comment comment) {
        throw new UnsupportedOperationException("working on it");
    }

    public static void buildQrCode(ImageView qrImageView, String content) {
        final int DIMEN = 250;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, DIMEN, DIMEN);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        if(bitMatrix == null) {
            return;
        }

        Bitmap bmp = Bitmap.createBitmap(DIMEN, DIMEN, Bitmap.Config.RGB_565);
        for (int x = 0; x < DIMEN; x++) {
            for (int y = 0; y < DIMEN; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
            }
        }

        qrImageView.setImageBitmap(bmp);
    }

    public static Profile buildDefaultProfile(Context context, String displayName) {
        // TODO: default image Uris will probably be assets...
        return new Profile(
                displayName,
                context.getString(R.string.profile_default_about_me),
                ImageUtil.getDefaultProfileImageUri(context),
                ImageUtil.getDefaultHeaderImageUri(context),
                Integer.MIN_VALUE);
    }

    public static Account getSyncAccount(Context context) {
        return new Account(
                context.getString(R.string.sync_account),
                context.getString(R.string.sync_account_type));
    }
}
