package local.tabhome;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Полноэкранный список всех установленных приложений, которые можно открыть
 * (как в системном выборе). Нажатие на строку добавляет приложение в док и
 * закрывает список.
 */
public class AppPickerOverlay extends FrameLayout {

    public interface Callback {
        void onPicked(String pkg, String label);
        void onCancel();
    }

    private final Callback callback;
    private final LinearLayout list;
    private final FrameLayout card;
    private boolean opened = false;

    public AppPickerOverlay(Context ctx, Callback cb) {
        super(ctx);
        this.callback = cb;
        setVisibility(GONE);
        setClickable(true);
        setBackground(solid(0xCC05070D));
        setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { callback.onCancel(); }
        });

        card = new FrameLayout(ctx);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xF2141722, 0xF50B0D14});
        bg.setCornerRadius(dp(26));
        bg.setStroke(dp(1), 0x1FFFFFFF);
        card.setBackground(bg);
        card.setClickable(true); // не закрывать при клике внутри карточки
        LayoutParams clp = new LayoutParams(dp(560), dp(640), Gravity.CENTER);
        addView(card, clp);

        LinearLayout column = new LinearLayout(ctx);
        column.setOrientation(LinearLayout.VERTICAL);
        card.addView(column, new FrameLayout.LayoutParams(-1, -1));

        TextView title = new TextView(ctx);
        title.setText("Добавить приложение");
        title.setTextColor(Color.WHITE);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        title.setPadding(dp(28), dp(26), dp(28), dp(14));
        column.addView(title);

        ScrollView scroll = new ScrollView(ctx);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-1, 0, 1f);
        column.addView(scroll, slp);

        list = new LinearLayout(ctx);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(14), 0, dp(14), dp(20));
        scroll.addView(list, new ScrollView.LayoutParams(-1, -2));
    }

    public boolean isOpen() { return opened; }

    public void open(List<String> excludePkgs) {
        populate(excludePkgs);
        opened = true;
        setVisibility(VISIBLE);
        setAlpha(0f);
        card.setAlpha(0f);
        card.setTranslationY(dp(40));
        animate().alpha(1f).setDuration(200).start();
        card.animate().alpha(1f).translationY(0f).setDuration(320)
                .setInterpolator(new DecelerateInterpolator(1.8f)).start();
    }

    public void close() {
        opened = false;
        animate().alpha(0f).setDuration(180).withEndAction(new Runnable() {
            @Override public void run() { setVisibility(GONE); }
        }).start();
    }

    private void populate(List<String> excludePkgs) {
        list.removeAllViews();
        final PackageManager pm = getContext().getPackageManager();
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = pm.queryIntentActivities(launcherIntent, 0);

        final List<ResolveInfo> sorted = new ArrayList<ResolveInfo>(apps);
        final Collator collator = Collator.getInstance(new Locale("ru"));
        Collections.sort(sorted, new Comparator<ResolveInfo>() {
            @Override public int compare(ResolveInfo a, ResolveInfo b) {
                return collator.compare(String.valueOf(a.loadLabel(pm)), String.valueOf(b.loadLabel(pm)));
            }
        });

        String self = getContext().getPackageName();
        for (final ResolveInfo ri : sorted) {
            final String pkg = ri.activityInfo.packageName;
            if (pkg.equals(self)) continue;
            if (excludePkgs.contains(pkg)) continue;
            final String label = String.valueOf(ri.loadLabel(pm));
            list.addView(row(pkg, label, ri));
        }
        if (list.getChildCount() == 0) {
            TextView empty = new TextView(getContext());
            empty.setText("Все приложения уже в панели");
            empty.setTextColor(0x99FFFFFF);
            empty.setPadding(dp(14), dp(20), dp(14), dp(20));
            list.addView(empty);
        }
    }

    private View row(final String pkg, final String label, ResolveInfo ri) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(12), dp(14), dp(12));
        row.setClickable(true);
        row.setFocusable(true);
        GradientDrawable rowBg = new GradientDrawable();
        rowBg.setColor(Color.TRANSPARENT);
        rowBg.setCornerRadius(dp(16));
        row.setBackground(rowBg);

        ImageView icon = new ImageView(getContext());
        icon.setImageDrawable(ri.loadIcon(getContext().getPackageManager()));
        row.addView(icon, new LinearLayout.LayoutParams(dp(44), dp(44)));

        TextView t = new TextView(getContext());
        t.setText(label);
        t.setTextColor(Color.WHITE);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0, -2, 1f);
        tlp.leftMargin = dp(16);
        row.addView(t, tlp);

        row.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, android.view.MotionEvent e) {
                if (e.getActionMasked() == android.view.MotionEvent.ACTION_DOWN) rowBg.setColor(0x1FFFFFFF);
                if (e.getActionMasked() == android.view.MotionEvent.ACTION_UP
                        || e.getActionMasked() == android.view.MotionEvent.ACTION_CANCEL) rowBg.setColor(Color.TRANSPARENT);
                return false;
            }
        });
        row.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { callback.onPicked(pkg, label); }
        });
        return row;
    }

    private GradientDrawable solid(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        return d;
    }

    private int dp(float v) {
        return Math.round(v * getContext().getResources().getDisplayMetrics().density);
    }
}
