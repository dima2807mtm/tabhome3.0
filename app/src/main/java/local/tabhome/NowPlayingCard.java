package local.tabhome;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Карточка "сейчас играет": показывает то, что проигрывается в любом
 * приложении (громкость/музыка/видео), с кнопками назад/пауза/вперёд.
 * Чтобы это работало, один раз нужно разрешить "Доступ к уведомлениям" -
 * без этого Android не даёт читать чужие медиа-сессии.
 */
public class NowPlayingCard extends FrameLayout {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private MediaSessionManager msm;
    private MediaController active;
    private final MediaSessionManager.OnActiveSessionsChangedListener sessionsListener =
            new MediaSessionManager.OnActiveSessionsChangedListener() {
                @Override public void onActiveSessionsChanged(List<MediaController> controllers) {
                    bind(controllers);
                }
            };
    private final MediaController.Callback controllerCallback = new MediaController.Callback() {
        @Override public void onPlaybackStateChanged(PlaybackState state) { refresh(); }
        @Override public void onMetadataChanged(MediaMetadata metadata) { refresh(); }
        @Override public void onSessionDestroyed() { refresh(); }
    };

    private FrameLayout cover;
    private TextView title, artist;
    private ImageView playPause;
    private View grantCard, playingCard;

    public NowPlayingCard(Context ctx) {
        super(ctx);
        build();
    }

    private void build() {
        setClipChildren(false);

        // ---- запрос доступа к уведомлениям ----
        grantCard = glassCard();
        LinearLayout gc = (LinearLayout) grantCard;
        TextView gt = label(14, "sans-serif-medium", 0xE6FFFFFF);
        gt.setText("ВКЛЮЧИТЬ ПЛЕЕР");
        gc.addView(gt);
        TextView gs = label(13, "sans-serif", 0x99FFFFFF);
        gs.setText("Нажмите, чтобы показывать здесь музыку из любых приложений");
        gs.setMaxWidth(dp(220));
        LinearLayout.LayoutParams gslp = new LinearLayout.LayoutParams(-2, -2);
        gslp.topMargin = dp(4);
        gc.addView(gs, gslp);
        grantCard.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    getContext().startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch (RuntimeException ignored) { }
            }
        });
        addView(grantCard, new LayoutParams(-2, -2));

        // ---- карточка "сейчас играет" ----
        playingCard = buildPlayingCard();
        addView(playingCard, new LayoutParams(-2, -2));

        grantCard.setVisibility(GONE);
        playingCard.setVisibility(GONE);
    }

    private View buildPlayingCard() {
        LinearLayout row = (LinearLayout) glassCard();
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setClickable(false);

        cover = new FrameLayout(getContext());
        cover.setBackground(new IconDrawable("note", 0xFF8AB4FF, 0xFFB78CFF, false, false));
        row.addView(cover, new LinearLayout.LayoutParams(dp(52), dp(52)));

        LinearLayout texts = new LinearLayout(getContext());
        texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(dp(150), -2);
        tlp.setMargins(dp(14), 0, dp(10), 0);
        title = label(14, "sans-serif-medium", Color.WHITE);
        title.setMaxLines(1);
        title.setEllipsize(TextUtils.TruncateAt.END);
        texts.addView(title);
        artist = label(12, "sans-serif", 0x99FFFFFF);
        artist.setMaxLines(1);
        artist.setEllipsize(TextUtils.TruncateAt.END);
        texts.addView(artist);
        row.addView(texts, tlp);

        row.addView(control("prev", dp(30), new OnClickListener() {
            @Override public void onClick(View v) { if (active != null) active.getTransportControls().skipToPrevious(); }
        }));
        playPause = control("play", dp(36), new OnClickListener() {
            @Override public void onClick(View v) { togglePlay(); }
        });
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(dp(36), dp(36));
        plp.setMargins(dp(6), 0, dp(6), 0);
        row.removeView(playPause);
        row.addView(playPause, plp);
        row.addView(control("next", dp(30), new OnClickListener() {
            @Override public void onClick(View v) { if (active != null) active.getTransportControls().skipToNext(); }
        }));
        return row;
    }

    private ImageView control(String kind, int size, OnClickListener l) {
        ImageView iv = new ImageView(getContext());
        iv.setImageDrawable(new IconDrawable(kind, 0, 0, false, true));
        iv.setPadding(dp(4), dp(4), dp(4), dp(4));
        iv.setClickable(true);
        iv.setOnClickListener(l);
        return iv;
    }

    private void togglePlay() {
        if (active == null) return;
        PlaybackState st = active.getPlaybackState();
        boolean playing = st != null && st.getState() == PlaybackState.STATE_PLAYING;
        if (playing) active.getTransportControls().pause(); else active.getTransportControls().play();
    }

    private View glassCard() {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setClickable(true);
        card.setPadding(dp(18), dp(14), dp(18), dp(14));
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xCC1A1C24, 0xCC0E0F14});
        bg.setCornerRadius(dp(22));
        bg.setStroke(dp(1), 0x1FFFFFFF);
        card.setBackground(bg);
        return card;
    }

    private TextView label(float sp, String family, int color) {
        TextView t = new TextView(getContext());
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTypeface(Typeface.create(family, Typeface.NORMAL));
        t.setTextColor(color);
        return t;
    }

    // ------------------------------------------------------------------ жизненный цикл

    public void start() {
        if (!hasNotificationAccess()) {
            grantCard.setVisibility(VISIBLE);
            playingCard.setVisibility(GONE);
            return;
        }
        grantCard.setVisibility(GONE);
        try {
            msm = (MediaSessionManager) getContext().getSystemService(Context.MEDIA_SESSION_SERVICE);
            ComponentName comp = new ComponentName(getContext(), NowPlayingListener.class);
            msm.addOnActiveSessionsChangedListener(sessionsListener, comp, handler);
            bind(msm.getActiveSessions(comp));
        } catch (SecurityException e) {
            grantCard.setVisibility(VISIBLE);
            playingCard.setVisibility(GONE);
        }
    }

    public void stop() {
        if (active != null) { active.unregisterCallback(controllerCallback); active = null; }
        if (msm != null) { try { msm.removeOnActiveSessionsChangedListener(sessionsListener); } catch (RuntimeException ignored) { } }
    }

    private void bind(List<MediaController> controllers) {
        if (active != null) active.unregisterCallback(controllerCallback);
        active = null;
        if (controllers != null) {
            List<MediaController> list = new ArrayList<MediaController>(controllers);
            for (MediaController mc : list) {
                PlaybackState st = mc.getPlaybackState();
                if (st != null && st.getState() == PlaybackState.STATE_PLAYING) { active = mc; break; }
            }
            if (active == null && !list.isEmpty()) active = list.get(0);
        }
        if (active != null) active.registerCallback(controllerCallback, handler);
        refresh();
    }

    private void refresh() {
        if (active == null) {
            playingCard.setVisibility(GONE);
            return;
        }
        MediaMetadata md = active.getMetadata();
        String t = md != null ? md.getString(MediaMetadata.METADATA_KEY_TITLE) : null;
        String a = md != null ? md.getString(MediaMetadata.METADATA_KEY_ARTIST) : null;
        if (TextUtils.isEmpty(t)) { playingCard.setVisibility(GONE); return; }
        title.setText(t);
        artist.setText(TextUtils.isEmpty(a) ? appLabel(active.getPackageName()) : a);

        Bitmap art = md.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
        if (art == null) art = md.getBitmap(MediaMetadata.METADATA_KEY_ART);
        if (art != null) {
            cover.setBackground(new RealIconTile(new BitmapDrawable(getResources(), art)));
        } else {
            cover.setBackground(new IconDrawable("note", 0xFF8AB4FF, 0xFFB78CFF, false, false));
        }

        PlaybackState st = active.getPlaybackState();
        boolean playing = st != null && st.getState() == PlaybackState.STATE_PLAYING;
        playPause.setImageDrawable(new IconDrawable(playing ? "pause" : "play", 0, 0, false, true));

        playingCard.setVisibility(VISIBLE);
    }

    private String appLabel(String pkg) {
        try {
            return String.valueOf(getContext().getPackageManager()
                    .getApplicationLabel(getContext().getPackageManager().getApplicationInfo(pkg, 0)));
        } catch (Exception e) { return ""; }
    }

    private boolean hasNotificationAccess() {
        String flat = Settings.Secure.getString(getContext().getContentResolver(), "enabled_notification_listeners");
        if (TextUtils.isEmpty(flat)) return false;
        String me = getContext().getPackageName() + "/" + NowPlayingListener.class.getName();
        for (String s : flat.split(":")) if (s.equalsIgnoreCase(me)) return true;
        return false;
    }

    private int dp(float v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
