package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ScoresAdapter extends CursorAdapter {
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_LEAGUE = 5;
    public static final int COL_MATCH_DAY = 9;
    public static final int COL_ID = 8;
    public static final int COL_MATCH_TIME = 2;
    public double detailMatchId = 0;
    private String FOOTBALL_SCORES_HASHTAG = "#Football_Scores";

    public ScoresAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View mItem = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder mHolder = new ViewHolder(mItem);
        mItem.setTag(mHolder);
        return mItem;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final ViewHolder mHolder = (ViewHolder) view.getTag();

        String home = cursor.getString(COL_HOME);
        String away = cursor.getString(COL_AWAY);
        int homeGoals = cursor.getInt(COL_HOME_GOALS);
        int awayGoals = cursor.getInt(COL_AWAY_GOALS);
        String time = cursor.getString(COL_MATCH_TIME);

        mHolder.homeName.setText(home);
        mHolder.awayName.setText(away);
        mHolder.date.setText(time);
        mHolder.score.setText(Utils.getScores(context, homeGoals, awayGoals));
        mHolder.matchId = cursor.getDouble(COL_ID);
        mHolder.homeCrest.setImageResource(Utils.getTeamCrestByTeamName(home));
        mHolder.awayCrest.setImageResource(Utils.getTeamCrestByTeamName(away));

        String contentDescription = context.getString(R.string.teams_accessibility_desc,
                home, away, time);
        if (homeGoals >= 0 && awayGoals >= 0)
            contentDescription += context.getString(R.string.score_accessibility_desc,
                    home, homeGoals, away, awayGoals);

        LayoutInflater vi = (LayoutInflater) context.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.detail_fragment, null);

        view.setContentDescription(contentDescription);
        ViewGroup container = (ViewGroup) view.findViewById(R.id.details_fragment_container);

        if(mHolder.matchId == detailMatchId) {
            container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            TextView matchDay = (TextView) v.findViewById(R.id.matchday_textview);
            matchDay.setText(Utils.getMatchDay(context, cursor.getInt(COL_MATCH_DAY),
                    cursor.getInt(COL_LEAGUE)));
            TextView league = (TextView) v.findViewById(R.id.league_textview);
            league.setText(Utils.getLeague(context, cursor.getInt(COL_LEAGUE)));
            Button shareButton = (Button) v.findViewById(R.id.share_button);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //add Share Action
                    context.startActivity(createShareForecastIntent(mHolder.homeName.getText() + " "
                            + mHolder.score.getText() + " " + mHolder.awayName.getText() + " "));
                }
            });
        } else {
            container.removeAllViews();
        }

    }
    public Intent createShareForecastIntent(String ShareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText + FOOTBALL_SCORES_HASHTAG);
        return shareIntent;
    }

}
