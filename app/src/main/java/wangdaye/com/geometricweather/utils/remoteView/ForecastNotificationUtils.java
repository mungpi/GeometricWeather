package wangdaye.com.geometricweather.utils.remoteView;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RemoteViews;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/** <br> forecast notification utils. */

public class ForecastNotificationUtils {

    private static final int NOTIFICATION_ID = 318;

    public static void buildForecastAndSendIt(Context context, Weather weather, boolean today) {
        if (weather == null) {
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean fahrenheit = sharedPreferences.getBoolean(
                context.getString(R.string.key_fahrenheit),
                false);
        String iconStyle = sharedPreferences.getString(
                context.getString(R.string.key_notification_icon_style),
                "material");
        boolean tempIcon = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_temp_icon),
                false);
        boolean backgroundColor = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_background),
                false);
        String textColor = sharedPreferences.getString(
                context.getString(R.string.key_notification_text_color),
                "grey");

        int mainColor;
        int subColor;
        switch (textColor) {
            case "dark":
                mainColor = ContextCompat.getColor(context, R.color.colorTextDark);
                subColor = ContextCompat.getColor(context, R.color.colorTextDark2nd);
                break;

            case "grey":
                mainColor = ContextCompat.getColor(context, R.color.colorTextGrey);
                subColor = ContextCompat.getColor(context, R.color.colorTextGrey2nd);
                break;

            case "light":
            default:
                mainColor = ContextCompat.getColor(context, R.color.colorTextLight);
                subColor = ContextCompat.getColor(context, R.color.colorTextLight2nd);
                break;
        }

        // get manager & builder.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationCompat.CATEGORY_REMINDER);

        // set notification level.
        if (sharedPreferences.getBoolean(context.getString(R.string.key_notification_hide_icon), false)) {
            builder.setPriority(NotificationCompat.PRIORITY_MIN);
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        }

        // set notification visibility.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (sharedPreferences.getBoolean(context.getString(R.string.key_notification_hide_in_lockScreen), false)) {
                builder.setVisibility(NotificationCompat.VISIBILITY_SECRET);
            } else {
                builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            }
        }

        // set small icon.
        int iconTemp = today ? weather.dailyList.get(0).temps[0] : weather.dailyList.get(1).temps[0];
        builder.setSmallIcon(
                tempIcon ?
                        ValueUtils.getTempIconId(
                                fahrenheit ?
                                        ValueUtils.calcFahrenheit(iconTemp)
                                        :
                                        iconTemp)
                        :
                        WeatherHelper.getNotificationWeatherIcon(
                                today ?
                                        weather.dailyList.get(0).weatherKinds[0]
                                        :
                                        weather.dailyList.get(1).weatherKinds[0],
                                true));

        // set view
        RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.notification_forecast);

        // set icon.
        int[] imageIds = new int[2];
        if (today) {
            imageIds[0] = WeatherHelper.getWidgetNotificationIcon(
                    weather.dailyList.get(0).weatherKinds[0],
                    true,
                    iconStyle,
                    textColor);
            imageIds[1] = WeatherHelper.getWidgetNotificationIcon(
                    weather.dailyList.get(0).weatherKinds[1],
                    false,
                    iconStyle,
                    textColor);
        } else {
            imageIds[0] = WeatherHelper.getWidgetNotificationIcon(
                    weather.dailyList.get(0).weatherKinds[0],
                    true,
                    iconStyle,
                    textColor);
            imageIds[1] = WeatherHelper.getWidgetNotificationIcon(
                    weather.dailyList.get(1).weatherKinds[0],
                    true,
                    iconStyle,
                    textColor);
        }
        view.setImageViewResource(
                R.id.notification_forecast_icon_1,
                imageIds[0]);
        view.setImageViewResource(
                R.id.notification_forecast_icon_2,
                imageIds[1]);

        // set title.
        String[] titles = new String[2];
        if (today) {
            titles[0] = context.getString(R.string.day) + " " + ValueUtils.buildCurrentTemp(weather.dailyList.get(0).temps[0], false, fahrenheit);
            titles[1] = context.getString(R.string.night) + " " + ValueUtils.buildCurrentTemp(weather.dailyList.get(0).temps[1], false, fahrenheit);
        } else {
            titles[0] = context.getString(R.string.today) + " " + ValueUtils.buildCurrentTemp(weather.dailyList.get(0).temps[0], false, fahrenheit);
            titles[1] = context.getString(R.string.tomorrow) + " " + ValueUtils.buildCurrentTemp(weather.dailyList.get(1).temps[0], false, fahrenheit);
        }
        view.setTextViewText(
                R.id.notification_forecast_title_1, titles[0]);
        view.setTextViewText(
                R.id.notification_forecast_title_2, titles[1]);

        // set content.
        String[] contents = new String[2];
        if (today) {
            contents[0] = weather.dailyList.get(0).weathers[0];
            contents[1] = weather.dailyList.get(0).weathers[1];
        } else {
            contents[0] = weather.dailyList.get(0).weathers[0];
            contents[1] = weather.dailyList.get(1).weathers[0];
        }
        view.setTextViewText(
                R.id.notification_forecast_content_1,
                contents[0]);
        view.setTextViewText(
                R.id.notification_forecast_content_2,
                contents[1]);

        // set background.
        if (backgroundColor) {
            view.setViewVisibility(R.id.notification_forecast_background, View.VISIBLE);
        } else {
            view.setViewVisibility(R.id.notification_forecast_background, View.GONE);
        }

        // set text color.
        view.setTextColor(R.id.notification_forecast_title_1, mainColor);
        view.setTextColor(R.id.notification_forecast_title_2, mainColor);
        view.setTextColor(R.id.notification_forecast_content_1, subColor);
        view.setTextColor(R.id.notification_forecast_content_2, subColor);
        builder.setContent(view);

        // set intent.
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, IntentHelper.buildMainActivityIntent(context, null), 0);
        builder.setContentIntent(pendingIntent);

        // set sound & vibrate.
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setAutoCancel(true);

        Notification notification = builder.build();

        // commit.
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification);
    }

    public static boolean isEnable(Context context, boolean today) {
        if (today) {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(
                            context.getString(R.string.key_forecast_today),
                            false);
        } else {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(
                            context.getString(R.string.key_forecast_tomorrow),
                            false);
        }
    }
}
