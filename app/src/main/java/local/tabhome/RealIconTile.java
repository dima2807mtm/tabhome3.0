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
 * Для приложений, добавленных через "+": настоящая иконка приложения в той же
 * тёмной стеклянной плашке, что и у нарисованных значков - чтобы док выглядел
 * одним целым, даже когда часть иконок "чужие".
 */
public class RealIconTile extends Drawable {

    private final Drawable real;
    private final Paint tileBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gloss = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public RealIconTile(Drawable real) {
        this.real = real;
    }

    @Override
    public void draw(Canvas c) {
        Rect b = getBounds();
        float w = b.width(), h = b.height();
        if (w <= 0 || h <= 0) return;
        float s = Math.min(w, h);
        float left = b.left + (w - s) / 2f;
        float top = b.top + (h - s) / 2f;
        float r = s * 0.26f;

        rect.set(left, top, left + s, top + s);
        tileBg.setShader(new LinearGradient(left, top, left + s, top + s,
                0xFF23262F, 0xFF121319, Shader.TileMode.CLAMP));
        c.drawRoundRect(rect, r, r, tileBg);

        float pad = s * 0.17f;
        if (real != null) {
            real.setBounds((int) (left + pad), (int) (top + pad), (int) (left + s - pad), (int) (top + s - pad));
            real.draw(c);
        }

        gloss.setShader(new LinearGradient(0, top, 0, top + s * 0.55f, 0x1FFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
        c.save();
        Path clip = new Path();
        rect.set(left, top, left + s, top + s);
        clip.addRoundRect(rect, r, r, Path.Direction.CW);
        c.clipPath(clip);
        rect.set(left, top, left + s, top + s * 0.55f);
        c.drawRect(rect, gloss);
        c.restore();
    }

    @Override public void setAlpha(int alpha) { tileBg.setAlpha(alpha); if (real != null) real.setAlpha(alpha); }
    @Override public void setColorFilter(ColorFilter cf) { tileBg.setColorFilter(cf); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
