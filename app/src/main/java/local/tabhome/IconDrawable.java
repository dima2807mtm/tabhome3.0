package local.tabhome;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/**
 * Тёмные "стеклянные" значки, нарисованные кодом - без картинок и без системных
 * иконок. Плитка: тёмный почти-чёрный градиент, тонкая цветная обводка-свечение
 * и мягкий блик сверху. Сам символ - белый/цветной штрих поверх плитки.
 *
 * kind: play, chrome, clapper, globe, lock, backspace, pause, next, prev, note
 */
public class IconDrawable extends Drawable {

    private final String kind;
    private final int accentA, accentB;
    private final boolean circle;
    private final boolean plainGlyph; // true - без тёмной плитки, просто белый значок (для кнопок UI)

    private final Paint tileBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tileStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gloss = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF rect = new RectF();

    public IconDrawable(String kind, int accentA, int accentB, boolean circle, boolean plainGlyph) {
        this.kind = kind;
        this.accentA = accentA;
        this.accentB = accentB;
        this.circle = circle;
        this.plainGlyph = plainGlyph;
        tileStroke.setStyle(Paint.Style.STROKE);
        fill.setStyle(Paint.Style.FILL);
        fill.setColor(0xFFFFFFFF);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setColor(0xFFFFFFFF);
        stroke.setStrokeCap(Paint.Cap.ROUND);
        stroke.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    public void draw(Canvas c) {
        Rect b = getBounds();
        float w = b.width(), h = b.height();
        if (w <= 0 || h <= 0) return;
        float s = Math.min(w, h);
        float left = b.left + (w - s) / 2f;
        float top = b.top + (h - s) / 2f;

        if (!plainGlyph) {
            float r = circle ? s / 2f : s * 0.26f;
            rect.set(left, top, left + s, top + s);
            tileBg.setShader(new LinearGradient(left, top, left + s, top + s,
                    0xFF23262F, 0xFF121319, Shader.TileMode.CLAMP));
            c.drawRoundRect(rect, r, r, tileBg);

            tileStroke.setStrokeWidth(s * 0.018f);
            int strokeStart = (accentA & 0x00FFFFFF) | 0x80000000;
            int strokeEnd = (accentB & 0x00FFFFFF) | 0x10000000;
            tileStroke.setShader(new LinearGradient(left, top, left + s, top + s,
                    strokeStart, strokeEnd, Shader.TileMode.CLAMP));
            rect.set(left + s * 0.01f, top + s * 0.01f, left + s * 0.99f, top + s * 0.99f);
            c.drawRoundRect(rect, r, r, tileStroke);

            gloss.setShader(new LinearGradient(0, top, 0, top + s * 0.55f,
                    0x26FFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
            c.save();
            rect.set(left, top, left + s, top + s);
            Path clip = new Path();
            clip.addRoundRect(rect, r, r, Path.Direction.CW);
            c.clipPath(clip);
            rect.set(left, top, left + s, top + s * 0.55f);
            c.drawRect(rect, gloss);
            c.restore();
        }

        c.save();
        c.translate(left, top);
        drawGlyph(c, s);
        c.restore();
    }

    private void drawGlyph(Canvas c, float s) {
        Paint accent = new Paint(Paint.ANTI_ALIAS_FLAG);
        accent.setShader(new LinearGradient(0, 0, s, s, accentA, accentB, Shader.TileMode.CLAMP));
        accent.setStyle(Paint.Style.FILL);
        Paint accentStroke = new Paint(accent);
        accentStroke.setStyle(Paint.Style.STROKE);
        accentStroke.setStrokeCap(Paint.Cap.ROUND);
        accentStroke.setStrokeJoin(Paint.Join.ROUND);
        accentStroke.setStrokeWidth(s * 0.065f);

        Paint glyphPaint = plainGlyph ? stroke : accentStroke;
        Paint glyphFill = plainGlyph ? fill : accent;

        if ("play".equals(kind)) {
            glyphPaint.setStrokeWidth(s * 0.055f);
            c.drawCircle(s * 0.5f, s * 0.5f, s * 0.3f, glyphPaint);
            path.reset();
            path.moveTo(s * 0.43f, s * 0.37f);
            path.lineTo(s * 0.66f, s * 0.5f);
            path.lineTo(s * 0.43f, s * 0.63f);
            path.close();
            Paint tri = new Paint(glyphFill);
            tri.setStyle(Paint.Style.FILL_AND_STROKE);
            tri.setStrokeJoin(Paint.Join.ROUND);
            tri.setStrokeWidth(s * 0.03f);
            c.drawPath(path, tri);

        } else if ("clapper".equals(kind)) {
            float l = s * 0.24f, r = s * 0.76f, t = s * 0.42f, bt = s * 0.74f;
            rect.set(l, t, r, bt);
            c.drawRoundRect(rect, s * 0.04f, s * 0.04f, glyphFill);
            c.save();
            c.rotate(-12f, l, t - s * 0.03f);
            rect.set(l, t - s * 0.14f, r, t - s * 0.025f);
            c.drawRoundRect(rect, s * 0.03f, s * 0.03f, glyphFill);
            Paint cut = new Paint(Paint.ANTI_ALIAS_FLAG);
            cut.setColor(0xFF121319);
            cut.setStyle(Paint.Style.STROKE);
            cut.setStrokeWidth(s * 0.035f);
            for (int i = 1; i <= 3; i++) {
                float x = l + (r - l) * i / 4f;
                c.drawLine(x - s * 0.03f, t - s * 0.14f, x + s * 0.03f, t - s * 0.025f, cut);
            }
            c.restore();
            Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
            dot.setColor(0xFF121319);
            c.drawCircle(s * 0.5f, s * 0.58f, s * 0.045f, dot);

        } else if ("chrome".equals(kind)) {
            // собственный значок "браузер": глобус-кольцо + орбита, не повторяет логотип Chrome
            glyphPaint.setStrokeWidth(s * 0.055f);
            float cx = s * 0.5f, cy = s * 0.5f, rad = s * 0.27f;
            c.drawCircle(cx, cy, rad, glyphPaint);
            Paint dotP = new Paint(glyphFill);
            c.drawCircle(cx, cy, s * 0.06f, dotP);
            c.save();
            c.rotate(-30f, cx, cy);
            rect.set(cx - rad * 1.12f, cy - rad * 0.4f, cx + rad * 1.12f, cy + rad * 0.4f);
            c.drawArc(rect, 0f, 180f, false, glyphPaint);
            c.restore();
            c.save();
            c.rotate(90f, cx, cy);
            rect.set(cx - rad * 1.12f, cy - rad * 0.4f, cx + rad * 1.12f, cy + rad * 0.4f);
            c.drawArc(rect, 0f, 180f, false, glyphPaint);
            c.restore();

        } else if ("globe".equals(kind)) {
            glyphPaint.setStrokeWidth(s * 0.05f);
            float cx = s * 0.5f, cy = s * 0.5f, rad = s * 0.28f;
            c.drawCircle(cx, cy, rad, glyphPaint);
            rect.set(cx - rad * 0.45f, cy - rad, cx + rad * 0.45f, cy + rad);
            c.drawOval(rect, glyphPaint);
            c.drawLine(cx - rad, cy, cx + rad, cy, glyphPaint);

        } else if ("lock".equals(kind)) {
            glyphPaint.setStrokeWidth(s * 0.07f);
            rect.set(s * 0.33f, s * 0.2f, s * 0.67f, s * 0.6f);
            c.drawArc(rect, 180f, 180f, false, glyphPaint);
            c.drawLine(s * 0.33f, s * 0.4f, s * 0.33f, s * 0.5f, glyphPaint);
            c.drawLine(s * 0.67f, s * 0.4f, s * 0.67f, s * 0.5f, glyphPaint);
            rect.set(s * 0.26f, s * 0.46f, s * 0.74f, s * 0.78f);
            c.drawRoundRect(rect, s * 0.07f, s * 0.07f, glyphFill);
            Paint hole = new Paint(Paint.ANTI_ALIAS_FLAG);
            hole.setColor(0x66000000);
            c.drawCircle(s * 0.5f, s * 0.61f, s * 0.045f, hole);

        } else if ("backspace".equals(kind)) {
            glyphPaint.setStrokeWidth(s * 0.06f);
            path.reset();
            path.moveTo(s * 0.18f, s * 0.5f);
            path.lineTo(s * 0.34f, s * 0.3f);
            path.lineTo(s * 0.8f, s * 0.3f);
            path.lineTo(s * 0.8f, s * 0.7f);
            path.lineTo(s * 0.34f, s * 0.7f);
            path.close();
            c.drawPath(path, glyphPaint);
            c.drawLine(s * 0.5f, s * 0.42f, s * 0.66f, s * 0.58f, glyphPaint);
            c.drawLine(s * 0.66f, s * 0.42f, s * 0.5f, s * 0.58f, glyphPaint);

        } else if ("pause".equals(kind)) {
            rect.set(s * 0.32f, s * 0.28f, s * 0.44f, s * 0.72f);
            c.drawRoundRect(rect, s * 0.03f, s * 0.03f, glyphFill);
            rect.set(s * 0.56f, s * 0.28f, s * 0.68f, s * 0.72f);
            c.drawRoundRect(rect, s * 0.03f, s * 0.03f, glyphFill);

        } else if ("next".equals(kind)) {
            path.reset();
            path.moveTo(s * 0.3f, s * 0.26f);
            path.lineTo(s * 0.62f, s * 0.5f);
            path.lineTo(s * 0.3f, s * 0.74f);
            path.close();
            c.drawPath(path, glyphFill);
            rect.set(s * 0.64f, s * 0.26f, s * 0.72f, s * 0.74f);
            c.drawRoundRect(rect, s * 0.02f, s * 0.02f, glyphFill);

        } else if ("prev".equals(kind)) {
            path.reset();
            path.moveTo(s * 0.7f, s * 0.26f);
            path.lineTo(s * 0.38f, s * 0.5f);
            path.lineTo(s * 0.7f, s * 0.74f);
            path.close();
            c.drawPath(path, glyphFill);
            rect.set(s * 0.28f, s * 0.26f, s * 0.36f, s * 0.74f);
            c.drawRoundRect(rect, s * 0.02f, s * 0.02f, glyphFill);

        } else if ("plus".equals(kind)) {
            glyphPaint.setStrokeWidth(s * 0.1f);
            c.drawLine(s * 0.5f, s * 0.14f, s * 0.5f, s * 0.86f, glyphPaint);
            c.drawLine(s * 0.14f, s * 0.5f, s * 0.86f, s * 0.5f, glyphPaint);

        } else if ("note".equals(kind)) {
            glyphPaint.setStrokeWidth(s * 0.055f);
            c.drawCircle(s * 0.38f, s * 0.68f, s * 0.1f, glyphFill);
            c.drawCircle(s * 0.68f, s * 0.6f, s * 0.1f, glyphFill);
            c.drawLine(s * 0.48f, s * 0.68f, s * 0.48f, s * 0.26f, glyphPaint);
            c.drawLine(s * 0.78f, s * 0.6f, s * 0.78f, s * 0.22f, glyphPaint);
            c.drawLine(s * 0.48f, s * 0.26f, s * 0.78f, s * 0.22f, glyphPaint);
            c.drawLine(s * 0.48f, s * 0.38f, s * 0.78f, s * 0.34f, glyphPaint);

        } else {
            Paint tp = new Paint(Paint.ANTI_ALIAS_FLAG);
            tp.setColor(0xFFFFFFFF);
            tp.setTextAlign(Paint.Align.CENTER);
            tp.setTextSize(s * 0.5f);
            tp.setFakeBoldText(true);
            String letter = kind != null && kind.startsWith("letter:") ? kind.substring(7) : "?";
            c.drawText(letter, s * 0.5f, s * 0.5f + s * 0.18f, tp);
        }
    }

    @Override public void setAlpha(int alpha) { tileBg.setAlpha(alpha); }
    @Override public void setColorFilter(ColorFilter cf) { tileBg.setColorFilter(cf); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
