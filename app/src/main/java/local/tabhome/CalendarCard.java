package local.tabhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Декоративный мини-календарик: число крупно, день недели и месяц под ним.
 * Без доступа к календарю телефона - просто красиво показывает сегодняшнюю дату.
 */
public class CalendarCard extends LinearLayout {

    private TextView day, weekMonth;

    public CalendarCard(Context ctx) {
        super(ctx);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setPadding(dp(18), dp(14), dp(18), dp(14));
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xCC1A1C24, 0xCC0E0F14});
        bg.setCornerRadius(dp(22));
        bg.setStroke(dp(1), 0x1FFFFFFF);
        setBackground(bg);

        day = new TextView(ctx);
        day.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        day.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        day.setTextColor(Color.WHITE);
        day.setGravity(Gravity.CENTER);
        addView(day);

        weekMonth = new TextView(ctx);
        weekMonth.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        weekMonth.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        weekMonth.setTextColor(0x99FFFFFF);
        weekMonth.setLetterSpacing(0.12f);
        weekMonth.setGravity(Gravity.CENTER);
        LayoutParams wlp = new LayoutParams(-2, -2);
        wlp.topMargin = dp(2);
        addView(weekMonth, wlp);

        refresh();
    }

    public void refresh() {
        Calendar now = Calendar.getInstance();
        day.setText(String.valueOf(now.get(Calendar.DAY_OF_MONTH)));
        String wm = new SimpleDateFormat("EEE, LLLL", new Locale("ru")).format(now.getTime());
        weekMonth.setText(wm.toUpperCase(new Locale("ru")));
    }

    private int dp(float v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
