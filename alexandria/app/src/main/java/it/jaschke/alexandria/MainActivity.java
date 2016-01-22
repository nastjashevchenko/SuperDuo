package it.jaschke.alexandria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.model.Book;
import it.jaschke.alexandria.services.BookService;


public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, Callback {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment navigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence title;
    public static boolean IS_TABLET = false;
    private BroadcastReceiver messageReceiver;
    public static Book mBook;
    public Fragment mCurrentFragment;

    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String DELETE_EVENT = "DELETE_EVENT";
    public static final String SAVE_EVENT = "SAVE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";
    public static final String BOOK_KEY = "BOOK_EXTRA";
    public static final String BOOK_SAVED = "BOOK_SAVED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IS_TABLET = isTablet();
        if(IS_TABLET){
            setContentView(R.layout.activity_main_tablet);
        }else {
            setContentView(R.layout.activity_main);
        }

        messageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MESSAGE_EVENT);
        filter.addAction(DELETE_EVENT);
        filter.addAction(SAVE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, filter);

        navigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        title = getTitle();

        // Set up the drawer.
        navigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment nextFragment;

        switch (position){
            default:
            case 0:
                nextFragment = new ListOfBooks();
                break;
            case 1:
                nextFragment = new AddBook();
                break;
            case 2:
                nextFragment = new About();
                break;

        }

        fragmentManager.beginTransaction()
                .replace(R.id.container, nextFragment)
                .addToBackStack((String) title)
                .commit();
    }

    public void setTitle(int titleId) {
        title = getString(titleId);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!navigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        super.onDestroy();
    }

    @Override
    public void onItemSelected(String ean) {
        Bundle args = new Bundle();
        args.putString(BookDetail.EAN_KEY, ean);

        BookDetail fragment = new BookDetail();
        fragment.setArguments(args);

        int id = R.id.container;
        if(findViewById(R.id.right_container) != null){
            id = R.id.right_container;
            findViewById(R.id.right_container).setVisibility(View.VISIBLE);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(id, fragment)
                .addToBackStack("Book Detail")
                .commit();

    }

    public void openAddBookFragment(View view) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new AddBook())
                .addToBackStack((String) title)
                .commit();
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mCurrentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
            if (intent.getAction().equals(MESSAGE_EVENT)) {
                if (intent.getStringExtra(MESSAGE_KEY) != null) {
                    Toast.makeText(getApplicationContext(),
                            intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_LONG).show();
                }
                if (intent.getParcelableExtra(BOOK_KEY) != null) {
                    mBook = intent.getParcelableExtra(BOOK_KEY);
                    boolean isSaved = intent.getBooleanExtra(BOOK_SAVED, false);
                    if (mCurrentFragment instanceof AddBook) {
                        AddBook fragment = (AddBook) mCurrentFragment;
                        fragment.showBookInfo(mBook, isSaved);
                    }
                }
            } else if (intent.getAction().equals(SAVE_EVENT)) {
                if (MainActivity.this.mCurrentFragment instanceof ListOfBooks) {
                    ListOfBooks fragment = (ListOfBooks) MainActivity.this.mCurrentFragment;
                    fragment.restartLoader();
                }
            } else if (intent.getAction().equals(DELETE_EVENT)) {
                // When book was deleted, we immediately delete it from DB, but store in variable
                // Snackbar with UNDO button will be shown on receive delete book event
                // If user hits UNDO, book from variable will be added back to DB and list of books
                mBook = intent.getParcelableExtra(BOOK_KEY);
                Snackbar deleted = Snackbar.make(findViewById(R.id.coordinator_layout),
                        R.string.snack_deleted_book, Snackbar.LENGTH_LONG);
                deleted.setAction(R.string.snack_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar restored = Snackbar.make(findViewById(R.id.coordinator_layout),
                                R.string.snack_restored_book, Snackbar.LENGTH_SHORT);
                        restored.show();
                        Intent bookIntent = new Intent(getApplicationContext(), BookService.class);
                        bookIntent.putExtra(MainActivity.BOOK_KEY, mBook);
                        bookIntent.setAction(BookService.SAVE_BOOK);
                        getApplicationContext().startService(bookIntent);
                    }
                });
                deleted.show();
            }
        }
    }

    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() < 2){
            finish();
        }
        super.onBackPressed();
    }
}