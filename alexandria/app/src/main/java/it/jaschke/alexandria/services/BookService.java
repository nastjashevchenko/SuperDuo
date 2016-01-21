package it.jaschke.alexandria.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import it.jaschke.alexandria.MainActivity;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.model.Book;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class BookService extends IntentService {

    private final String LOG_TAG = BookService.class.getSimpleName();

    public static final String FETCH_BOOK = "it.jaschke.alexandria.services.action.FETCH_BOOK";
    public static final String DELETE_BOOK = "it.jaschke.alexandria.services.action.DELETE_BOOK";
    public static final String SAVE_BOOK = "it.jaschke.alexandria.services.action.SAVE_BOOK";

    public static final String EAN = "it.jaschke.alexandria.services.extra.EAN";

    public BookService() {
        super("Alexandria");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            final String ean = intent.getStringExtra(EAN);
            final Book book = intent.getParcelableExtra(MainActivity.BOOK_KEY);
            if (FETCH_BOOK.equals(action)) {
                fetchBook(ean);
            } else if (SAVE_BOOK.equals(action)) {
                saveBook(book);
            } else if (DELETE_BOOK.equals(action)) {
                deleteBook(ean);
            }
        }
    }

    // Method to check internet connection
    public static boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    // Method to send message to show in main thread (in activity)
    private void sendMessage(String messageText) {
        Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
        messageIntent.putExtra(MainActivity.MESSAGE_KEY, messageText);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
    }

    // Method to send fetched book back to UI thread to show in Fragment
    private void sendBook(Book book, boolean isSaved) {
        Intent bookIntent = new Intent(MainActivity.MESSAGE_EVENT);
        bookIntent.putExtra(MainActivity.BOOK_KEY, book);
        bookIntent.putExtra(MainActivity.BOOK_SAVED, isSaved);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(bookIntent);
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void deleteBook(String ean) {
        if(ean != null) {
            getContentResolver().delete(AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)), null, null);
        }
    }

    private void saveBook(Book book) {
        book.writeBackBook(getContentResolver());
        if (book.hasAuthors()) book.writeBackAuthors(getContentResolver());
        if (book.hasCategories()) book.writeBackCategories(getContentResolver());
    }

    /**
     * Handle action fetchBook in the provided background thread with the provided
     * parameters.
     */
    private void fetchBook(String ean) {
        // if no internet connection, show message to user and do nothing
        if (!isConnected(getApplicationContext())) {
            sendMessage(getResources().getString(R.string.no_connection));
            return;
        }

        Cursor bookEntry = getContentResolver().query(
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if(bookEntry != null && bookEntry.moveToFirst()){
            String title = bookEntry.getString(bookEntry.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
            String subtitle = bookEntry.getString(bookEntry.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
            String desc = bookEntry.getString(bookEntry.getColumnIndex(AlexandriaContract.BookEntry.DESC));
            String authors = bookEntry.getString(bookEntry.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
            String imgUrl = bookEntry.getString(bookEntry.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
            String categories = bookEntry.getString(bookEntry.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));

            Book book = new Book(ean, title, subtitle, desc, imgUrl);
            if (authors != null) book.setAuthors(authors);
            if (categories != null) book.setCategories(categories);
            sendBook(book, true);

            bookEntry.close();
            return;
        }

        if (bookEntry != null) bookEntry.close();

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJsonString = null;

        try {
            final String FORECAST_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
            final String QUERY_PARAM = "q";

            final String ISBN_PARAM = "isbn:" + ean;

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, ISBN_PARAM)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) return;

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) return;

            bookJsonString = buffer.toString();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }

        }

        final String ITEMS = "items";
        final String VOLUME_INFO = "volumeInfo";

        final String TITLE = "title";
        final String SUBTITLE = "subtitle";
        final String AUTHORS = "authors";
        final String DESC = "description";
        final String CATEGORIES = "categories";
        final String IMG_URL_PATH = "imageLinks";
        final String IMG_URL = "thumbnail";

        try {
            JSONObject bookJson = new JSONObject(bookJsonString);
            JSONArray bookArray;
            if(bookJson.has(ITEMS)){
                bookArray = bookJson.getJSONArray(ITEMS);
            } else {
                sendMessage(getResources().getString(R.string.not_found));
                return;
            }

            JSONObject bookInfo = ((JSONObject) bookArray.get(0)).getJSONObject(VOLUME_INFO);

            String title = bookInfo.getString(TITLE);
            String subtitle = bookInfo.has(SUBTITLE) ? bookInfo.getString(SUBTITLE) : "";
            String desc = bookInfo.has(DESC) ? bookInfo.getString(DESC) : "";
            String imgUrl = "";
            if(bookInfo.has(IMG_URL_PATH) && bookInfo.getJSONObject(IMG_URL_PATH).has(IMG_URL)) {
                imgUrl = bookInfo.getJSONObject(IMG_URL_PATH).getString(IMG_URL);
            }

            Book book = new Book(ean, title, subtitle, desc, imgUrl);
            if(bookInfo.has(AUTHORS)) {
                String[] authors = new String[bookInfo.getJSONArray(AUTHORS).length()];
                for (int i = 0; i < bookInfo.getJSONArray(AUTHORS).length(); i++) {
                    authors[i] = bookInfo.getJSONArray(AUTHORS).getString(i);
                }
                book.setAuthors(authors);
            }

            if(bookInfo.has(CATEGORIES)) {
                String[] categories = new String[bookInfo.getJSONArray(CATEGORIES).length()];
                for (int i = 0; i < bookInfo.getJSONArray(CATEGORIES).length(); i++) {
                    categories[i] = bookInfo.getJSONArray(CATEGORIES).getString(i);
                }
                book.setCategories(categories);
            }

            sendBook(book, false);
        } catch (JSONException | NullPointerException e) {
            // If user has connection, but for some reason we can not parse response to JSON
            // or response string is null
            sendMessage(getResources().getString(R.string.bad_server_response));
        }
    }
 }