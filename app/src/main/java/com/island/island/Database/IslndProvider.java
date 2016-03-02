package com.island.island.Database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class IslndProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private IslndDbHelper mOpenHelper;

    static final int POST = 100;
    static final int POST_WITH_USER = 101;

    private static final String sUserIdSelection =
            IslndContract.PostEntry.TABLE_NAME +
                    "." + IslndContract.PostEntry.COLUMN_POST_ID + " = ? ";

    private Cursor getPostsByUserId(Uri uri, String[] projection, String sortOrder) {
        int userId = IslndContract.PostEntry.getUserIdFromUri(uri);

        String[] selectionArgs = new String[] {Integer.toString(userId)};
        String selection = sUserIdSelection;

        return mOpenHelper.getReadableDatabase().query(
                IslndContract.PostEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = IslndContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, IslndContract.PATH_POST, POST);
        matcher.addURI(authority, IslndContract.PATH_POST + "/#", POST_WITH_USER);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new IslndDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case POST: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        IslndContract.PostEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case POST_WITH_USER: {
                retCursor = getPostsByUserId(uri, projection, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case POST:
                return IslndContract.PostEntry.CONTENT_TYPE;
            case POST_WITH_USER:
                return IslndContract.PostEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case POST: {
                long _id = db.insert(IslndContract.PostEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = IslndContract.PostEntry.buildPostUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null); // notify with base uri
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case POST:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(IslndContract.PostEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        // this makes delete all rows return the number of rows deleted
        if (selection == null) {
            selection = "1";
        }

        switch (match) {
            case POST:
                rowsDeleted = db.delete(
                        IslndContract.PostEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("update operation is not supported");
    }
}
