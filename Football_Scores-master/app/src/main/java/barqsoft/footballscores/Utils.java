package barqsoft.footballscores;

import android.content.Context;

public class Utils {
    public static final int SERIE_A = 357;
    public static final int PREMIER_LEAGUE = 354;
    public static final int CHAMPIONS_LEAGUE = 362;
    public static final int PRIMERA_DIVISION = 358;
    public static final int BUNDESLIGA = 351;

    public static String getLeague(Context c, int leagueNum) {
        switch (leagueNum) {
            case SERIE_A : return c.getString(R.string.seriaa);
            case PREMIER_LEAGUE: return c.getString(R.string.premierleague);
            case CHAMPIONS_LEAGUE : return c.getString(R.string.champions_league);
            case PRIMERA_DIVISION : return c.getString(R.string.primeradivison);
            case BUNDESLIGA : return c.getString(R.string.bundesliga);
            default: return c.getString(R.string.unknown_league);
        }
    }

    public static String getMatchDay(Context c, int matchDay, int leagueNum) {
        if(leagueNum == CHAMPIONS_LEAGUE) {
            if (matchDay <= 6) {
                return c.getString(R.string.group_stage_text) + ", "
                        + c.getString(R.string.matchday_text) + ": 6";
            } else if(matchDay == 7 || matchDay == 8) {
                return c.getString(R.string.first_knockout_round);
            } else if(matchDay == 9 || matchDay == 10) {
                return c.getString(R.string.quarter_final);
            } else if(matchDay == 11 || matchDay == 12) {
                return c.getString(R.string.semi_final);
            } else {
                return c.getString(R.string.final_text);
            }
        } else {
            return c.getString(R.string.matchday_text) + " : " + String.valueOf(matchDay);
        }
    }

    public static String getScores(Context c, int homeGoals, int awayGoals) {
        if(homeGoals < 0 || awayGoals < 0) {
            return c.getString(R.string.score_separator);
        } else {
            return String.valueOf(homeGoals) + c.getString(R.string.score_separator) + String.valueOf(awayGoals);
        }
    }

    public static int getTeamCrestByTeamName (String teamName) {
        if (teamName == null) return R.drawable.no_icon;
        switch (teamName) {
            //This is the set of icons that are currently in the app. Feel free to find and add more
            //as you go.
            case "Arsenal London FC" : return R.drawable.arsenal;
            case "Manchester United FC" : return R.drawable.manchester_united;
            case "Swansea City" : return R.drawable.swansea_city_afc;
            case "Leicester City" : return R.drawable.leicester_city_fc_hd_logo;
            case "Everton FC" : return R.drawable.everton_fc_logo1;
            case "West Ham United FC" : return R.drawable.west_ham;
            case "Tottenham Hotspur FC" : return R.drawable.tottenham_hotspur;
            case "West Bromwich Albion" : return R.drawable.west_bromwich_albion_hd_logo;
            case "Sunderland AFC" : return R.drawable.sunderland;
            case "Stoke City FC" : return R.drawable.stoke_city;
            default: return R.drawable.no_icon;
        }
    }
}
