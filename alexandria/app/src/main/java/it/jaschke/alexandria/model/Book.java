package it.jaschke.alexandria.model;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import it.jaschke.alexandria.data.AlexandriaContract;

/**
 * Class to hold Book object
 */
public class Book implements Parcelable {
    private String ean;
    private String title;
    private String subtitle;
    private String desc;
    private String imgUrl;
    private String[] authors;
    private String[] categories;

    public Book(String ean, String title, String subtitle, String desc, String imgUrl) {
        this.ean = ean;
        this.title = title;
        this.subtitle = subtitle;
        this.desc = desc;
        this.imgUrl = imgUrl;
    }

    public void setAuthors(String[] authors) {
        this.authors = authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors.split(",");
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }

    public void setCategories(String categories) {
        this.categories = categories.split(",");
    }

    public boolean hasAuthors() {
        return this.authors != null && this.authors.length > 0;
    }

    public boolean hasCategories() {
        return this.categories != null && this.categories.length > 0;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getDesc() {
        return desc;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    private String createStringFromArray(String[] stringArray) {
        StringBuilder builder = new StringBuilder();
        if (stringArray == null) return "";
        for (String string : stringArray) {
            if (builder.length() > 0) builder.append(",");
            builder.append(string);
        }
        return builder.toString();
    }

    public String getAuthorsStr() {
        return createStringFromArray(authors);
    }

    public String getCategoriesStr() {
        return createStringFromArray(categories);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ean);
        dest.writeString(title);
        dest.writeString(subtitle);
        dest.writeString(desc);
        dest.writeString(imgUrl);
        dest.writeStringArray(authors);
        dest.writeStringArray(categories);
    }

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    private Book(Parcel in) {
        this.ean = in.readString();
        this.title = in.readString();
        this.subtitle = in.readString();
        this.desc = in.readString();
        this.imgUrl = in.readString();
        this.authors = in.createStringArray();
        this.categories = in.createStringArray();
    }

    public void writeBackBook(ContentResolver resolver) {
        ContentValues values = new ContentValues();
        values.put(AlexandriaContract.BookEntry._ID, ean);
        values.put(AlexandriaContract.BookEntry.TITLE, title);
        values.put(AlexandriaContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(AlexandriaContract.BookEntry.SUBTITLE, subtitle);
        values.put(AlexandriaContract.BookEntry.DESC, desc);
        resolver.insert(AlexandriaContract.BookEntry.CONTENT_URI, values);
    }

    public void writeBackAuthors(ContentResolver resolver) {
        ContentValues values;
        for (String author : authors) {
            values = new ContentValues();
            values.put(AlexandriaContract.AuthorEntry._ID, ean);
            values.put(AlexandriaContract.AuthorEntry.AUTHOR, author);
            resolver.insert(AlexandriaContract.AuthorEntry.CONTENT_URI, values);
        }
    }

    public void writeBackCategories(ContentResolver resolver) {
        ContentValues values;
        for (String category : categories) {
            values = new ContentValues();
            values.put(AlexandriaContract.CategoryEntry._ID, ean);
            values.put(AlexandriaContract.CategoryEntry.CATEGORY, category);
            resolver.insert(AlexandriaContract.CategoryEntry.CONTENT_URI, values);
        }
    }
}