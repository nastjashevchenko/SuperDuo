package barqsoft.footballscores.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import barqsoft.footballscores.R;

public class CollectionWidgetProvider extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            RemoteViews mView = new RemoteViews(context.getPackageName(),
                    R.layout.collection_widget_layout);

            Intent intent = new Intent(context, CollectionWidgetService.class);
            //intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            //intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            mView.setRemoteAdapter(widgetId, R.id.widget_collection_list, intent);
            mView.setEmptyView(R.id.widget_collection_list, android.R.id.empty);

            appWidgetManager.updateAppWidget(widgetId, mView);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
