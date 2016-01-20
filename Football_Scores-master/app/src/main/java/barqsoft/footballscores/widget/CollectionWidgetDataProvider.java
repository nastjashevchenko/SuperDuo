package barqsoft.footballscores.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utils;

public class CollectionWidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {
    Context mContext;
    Cursor cursor;

    public CollectionWidgetDataProvider(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return (cursor != null) ? cursor.getCount() : 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews mView = new RemoteViews(mContext.getPackageName(),
                R.layout.collection_widget_item);

        cursor.moveToPosition(position);
        String home = cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.HOME_COL));
        String away = cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.AWAY_COL));
        int homeGoals = cursor.getInt(cursor.getColumnIndex(DatabaseContract.scores_table.HOME_GOALS_COL));
        int awayGoals = cursor.getInt(cursor.getColumnIndex(DatabaseContract.scores_table.AWAY_GOALS_COL));

        mView.setTextViewText(R.id.home_name, home);
        mView.setTextViewText(R.id.away_name, away);
        mView.setTextViewText(R.id.score_textview, Utils.getScores(mContext, homeGoals, awayGoals));

        return mView;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        initData();
    }

    private void initData() {
        String[] columnsForWidget = new String[] {
                DatabaseContract.scores_table.HOME_COL,
                DatabaseContract.scores_table.AWAY_COL,
                DatabaseContract.scores_table.HOME_GOALS_COL,
                DatabaseContract.scores_table.AWAY_GOALS_COL
        };
        Uri dateUri = DatabaseContract.scores_table.buildScoreWithDate();
        cursor = mContext.getContentResolver().query(dateUri,
                columnsForWidget,
                DatabaseContract.scores_table.DATE_COL,
                new String[]{new SimpleDateFormat("yyyy-MM-dd").format(new Date())}, null);
    }

    @Override
    public void onDestroy() {
        cursor.close();
    }
}
