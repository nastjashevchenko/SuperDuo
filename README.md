# P3: SuperDuo

This is project 3 for Udacity Android Nanodegree program. This project contains two applications: Alexandria (a book list and barcode scanner app) and Football scores (an app that tracks current and future football matches).

Code from initial commit to this repo was provided by Udacity, all following changes are mine.

## Alexandria
#### Required components ("meets specifications"):
* [x] Alexandria has barcode scanning functionality;
* [x] Alexandria does not crash while searching for a book without an internet connection;
#### Optional components ("exceeds specifications"):
* [x] Alexandria’s barcode scanning functionality does not require the installation of a separate app on first use;
* [x] Extra error cases are found, accounted for, and called out in code comments;
* [x] Strings are all included in the strings.xml file and untranslatable strings have a translatable tag marked to false.
#### Extra error cases and other improvements:
* Deleted Back button on Book detail, which was duplication of functionality (with system Back button) and was inconsistent with other Fragments;
* Bug fix: List of books was not updated after book is deleted from Book detail page;
* Bug fix (critical): If change orientation (portrait <-> landscape) while on Book detail page, app crashed;
* If list of books is empty, we should not show search view, because when user tries to search he/she sees that nothing happens and it looks like search is broken. It is not obvious
that it searches only among already added books. Also, as far as in that case this page is deadlock, now app suggests to go to Add/Scan page (by showing button which opens that Fragment);
* Layout changes:
** Book detail: Use LinearLayout instead of nested Relative/LinearLayouts which is more optimal and in case book doesn't have information for some fields (empty desc, empty subtitle etc.) layout looks better, doesn't have a lot of void space between views;
** Add/Scan book: Created container for book info to hide/show one view, not every view;
** Add/Scan book: New layout looks more compact and easy to read;
** About app: Added paddings and changed text (according to existed TODO);
* In all Fragments save views to class fields to re-use it instead of call findViewById() each time. Use Butterknife to be more concise;
* Updated to latest SDK;
* Code style fixes (added spaces, deleted unused resources), lint fixes.
