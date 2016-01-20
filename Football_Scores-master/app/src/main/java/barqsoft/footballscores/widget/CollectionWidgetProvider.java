package barqsoft.footballscores.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;

public class CollectionWidgetProvider extends AppWidgetProvider {
    public static final String EXTRA_POSITION = "com.dharmangsoni.widgets.EXTRA_POSITION";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            RemoteViews mView = new RemoteViews(context.getPackageName(),
                    R.layout.collection_widget_layout);

            Intent intent = new Intent(context, CollectionWidgetService.class);
            mView.setRemoteAdapter(widgetId, R.id.widget_collection_list, intent);
            mView.setEmptyView(R.id.widget_collection_list, android.R.id.empty);

            Intent clickIntentTemplate = new Intent(context, MainActivity.class);
            PendingIntent clickPendingIntentTemplate = PendingIntent.getActivity(context, 0,
                    clickIntentTemplate, PendingIntent.FLAG_UPDATE_CURRENT);
            mView.setPendingIntentTemplate(R.id.widget_collection_list, clickPendingIntentTemplate);

            appWidgetManager.updateAppWidget(widgetId, mView);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
