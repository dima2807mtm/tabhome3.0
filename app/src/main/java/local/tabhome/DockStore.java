package local.tabhome;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Хранит список приложений в доке (переживает перезапуски). Каждая запись - это
 * пакет, подпись и необязательный "kind" (если задан - рисуем свой векторный
 * значок через IconDrawable; если нет - берём настоящую иконку приложения и
 * оформляем её в той же тёмной плашке).
 */
public class DockStore {

    public static final class Entry {
        public final String pkg, label, kind;
        public Entry(String pkg, String label, String kind) {
            this.pkg = pkg; this.label = label; this.kind = kind;
        }
    }

    private static final String PREFS = "tabhome";
    private static final String KEY_DOCK = "dock_v2";
    // разделители, которых не бывает в названиях пакетов/приложений
    private static final String ITEM_SEP = "\u0001";
    private static final String FIELD_SEP = "\u0002";

    public static List<Entry> load(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = p.getString(KEY_DOCK, null);
        List<Entry> out = new ArrayList<Entry>();
        if (raw == null) {
            // первый запуск - кладём стандартный набор
            out.add(new Entry("ru.ivi.client", "Иви", "play"));
            out.add(new Entry("ru.kinopoisk", "Кинопоиск", "clapper"));
            out.add(new Entry("com.android.chrome", "Chrome", "chrome"));
            save(ctx, out);
            return out;
        }
        if (raw.length() == 0) return out;
        for (String item : raw.split(ITEM_SEP)) {
            String[] f = item.split(FIELD_SEP, -1);
            if (f.length < 2) continue;
            String pkg = f[0], label = f[1];
            String kind = (f.length > 2 && f[2].length() > 0) ? f[2] : null;
            out.add(new Entry(pkg, label, kind));
        }
        return out;
    }

    public static void save(Context ctx, List<Entry> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            Entry e = list.get(i);
            if (i > 0) sb.append(ITEM_SEP);
            sb.append(e.pkg).append(FIELD_SEP).append(e.label).append(FIELD_SEP)
                    .append(e.kind == null ? "" : e.kind);
        }
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_DOCK, sb.toString()).apply();
    }

    public static void add(Context ctx, String pkg, String label) {
        List<Entry> list = load(ctx);
        for (Entry e : list) if (e.pkg.equals(pkg)) return; // уже есть
        list.add(new Entry(pkg, label, null));
        save(ctx, list);
    }

    public static void remove(Context ctx, String pkg) {
        List<Entry> list = load(ctx);
        List<Entry> out = new ArrayList<Entry>();
        for (Entry e : list) if (!e.pkg.equals(pkg)) out.add(e);
        save(ctx, out);
    }

    public static boolean contains(Context ctx, String pkg) {
        for (Entry e : load(ctx)) if (e.pkg.equals(pkg)) return true;
        return false;
    }
}
