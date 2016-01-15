package it.jaschke.alexandria;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.jaschke.alexandria.api.BookListAdapter;
import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.data.AlexandriaContract;


public class ListOfBooks extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private BookListAdapter bookListAdapter;
    private int position = ListView.INVALID_POSITION;

    @Bind(R.id.listOfBooks) ListView mBookList;
    @Bind(R.id.searchText) EditText mSearchText;
    @Bind(R.id.searchButton) ImageButton mSearchButton;
    @Bind(R.id.no_results) LinearLayout mEmpty;
    @Bind(R.id.books_list) RelativeLayout mListOfBooks;

    private final int LOADER_ID = 10;

    public ListOfBooks() {
    }

    private void setBooksViewVisibility(boolean isListEmpty) {
        // Set visibility depending on if we have books in list or not
        // If there are no books, no reason to show search controls - it is confusing and looks like
        // search is broken
        if (isListEmpty) {
            mEmpty.setVisibility(View.VISIBLE);
            mListOfBooks.setVisibility(View.GONE);
        } else {
            mEmpty.setVisibility(View.GONE);
            mListOfBooks.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // need to update when getting back after deleting book from detail page
        // book should not be present in the list
        restartLoader();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Cursor cursor = getActivity().getContentResolver().query(
                AlexandriaContract.BookEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );


        bookListAdapter = new BookListAdapter(getActivity(), cursor, 0);
        View rootView = inflater.inflate(R.layout.fragment_list_of_books, container, false);
        ButterKnife.bind(this, rootView);

        mSearchButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        restartLoader();
                    }
                }
        );

        mBookList.setAdapter(bookListAdapter);
        mBookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = bookListAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    ((Callback) getActivity())
                            .onItemSelected(cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID)));
                }
            }
        });

        return rootView;
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        final String selection = AlexandriaContract.BookEntry.TITLE +" LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ";
        String searchString = mSearchText.getText().toString();

        if(searchString.length()>0){
            searchString = "%" + searchString + "%";
            return new CursorLoader(
                    getActivity(),
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    null,
                    selection,
                    new String[]{searchString, searchString},
                    null
            );
        }

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        bookListAdapter.swapCursor(data);
        // If no books in cursor:
        // hide list and search button/text field and show message and button to quickly add new books
        setBooksViewVisibility(data == null || data.getCount() == 0);
        if (position != ListView.INVALID_POSITION) {
            mBookList.smoothScrollToPosition(position);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookListAdapter.swapCursor(null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.books);
    }
}
