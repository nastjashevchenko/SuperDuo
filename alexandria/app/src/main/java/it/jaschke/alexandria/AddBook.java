package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.jaschke.alexandria.model.Book;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;


public class AddBook extends Fragment {
    static final String EAN_EXTRA_NAME = "EAN";
    private final static int BARCODE_SCANNER_INTENT = 101;

    private final String EAN_CONTENT="eanContent";

    @Bind(R.id.bookTitle) TextView mBookTitle;
    @Bind(R.id.bookSubTitle) TextView mBookSubTitle;
    @Bind(R.id.authors) TextView mAuthors;
    @Bind(R.id.categories) TextView mCategories;
    @Bind(R.id.bookCover) ImageView mBookCover;
    @Bind(R.id.save_button) Button mSaveButton;
    @Bind(R.id.delete_button) Button mDeleteButton;
    @Bind(R.id.ean) EditText mEan;
    @Bind(R.id.scan_button) Button mScanButton;
    @Bind(R.id.book_container) RelativeLayout mBookContainer;
    boolean mIsSaved;


    public AddBook(){
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mEan != null) {
            outState.putString(EAN_CONTENT, mEan.getText().toString());
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ButterKnife.bind(this, rootView);

        mEan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                // If we accept > 13 chars and allow dashes, user can copy/paste ISBNs
                // It is ok to allow put > 13 digits, it should not affect search
                String ean = s.toString().replace("-", "");

                // Removed catching isbn10
                // Catching isbn10 numbers logic was not correct. From many examples _real_ isbn10
                // for the book is not the same as isbn13 without "978" prefix
                // It would be ok to allow user fill in shorter version (with no prefix), but nobody
                // would do so, because how one can figure it out? Hint asks for 13 digit code.

                if (ean.length() < 13) {
                    mBookContainer.setVisibility(View.GONE);
                    return;
                }
                //Once we have an ISBN, start a book intent
                if(ean.length() == 13){
                    Intent bookIntent = new Intent(getActivity(), BookService.class);
                    bookIntent.putExtra(BookService.EAN, ean);
                    bookIntent.setAction(BookService.FETCH_BOOK);
                    // Fetch book action queries DB for book with given EAN
                    // If no such book in DB yet, it would fetch info via API
                    // Service already run loader, no need to rerun it again, better pass results from service
                    getActivity().startService(bookIntent);
                }
            }
        });

        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getActivity();
                Intent barcodeScanner = new Intent(context, BarcodeScanningActivity.class);
                startActivityForResult(barcodeScanner, BARCODE_SCANNER_INTENT);
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // On save we add this book to DB, if book was not saved previously
                if (!mIsSaved) {
                    Intent bookIntent = new Intent(getActivity(), BookService.class);
                    bookIntent.putExtra(MainActivity.BOOK_KEY, MainActivity.mBook);
                    bookIntent.setAction(BookService.SAVE_BOOK);
                    getActivity().startService(bookIntent);
                }

                mEan.setText("");
                Toast bookSaved = Toast.makeText(getContext(), R.string.book_saved_message,
                        Toast.LENGTH_SHORT);
                bookSaved.show();
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If user decided not to save, but start new search, we just empty fields
                mEan.setText("");
            }
        });

        if(savedInstanceState != null){
            mEan.setText(savedInstanceState.getString(EAN_CONTENT));
            mEan.setHint("");
        }

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BARCODE_SCANNER_INTENT) {
            if (resultCode == Activity.RESULT_OK) {
                mEan.setText(data.getStringExtra(EAN_EXTRA_NAME));
            }
        }
    }

    public void showBookInfo(Book book, boolean isSaved) {
        mBookTitle.setText(book.getTitle());
        mBookSubTitle.setText(book.getSubtitle());
        // Book can have no authors, then can not use placeholder
        String authors = (book.getAuthorsStr() != null && book.getAuthorsStr().length() > 0)
                ? String.format(getResources().getString(R.string.authors_placeholder),
                book.getAuthorsStr()) : "";
        mAuthors.setText(authors);
        // Delete previous image in case new book doesn't have cover
        mBookCover.setVisibility(View.GONE);
        String imgUrl = book.getImgUrl();
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            new DownloadImage(mBookCover).execute(imgUrl);
            mBookCover.setVisibility(View.VISIBLE);
        }
        mCategories.setText(book.getCategoriesStr());
        mBookContainer.setVisibility(View.VISIBLE);
        mIsSaved = isSaved;
    }
}
