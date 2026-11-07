package com.omarchytv.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * The focus highlight, drawn like Hyprland's active-window border in Omarchy: a crisp 2dp border in a
 * 45-degree blue-to-purple gradient, with a faint inner tint. One instance floats over the grid and
 * springs between tiles (the original launcher's "glass" behaviour, restyled).
 */
public class FocusFrameView extends View {

    private final Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint halo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float density;
    /** Room around the tile for the outer glow (MainActivity sizes the frame with the same inset). */
    static final float HALO_DP = 16f;
    /** Ring thickness: thick enough to read from the sofa. */
    static final float RING_DP = 5f;
    /** Outer glow: alpha per 2.5dp band, fading outward. */
    private static final float[] GLOW = {0.60f, 0.42f, 0.28f, 0.17f, 0.09f, 0.04f};
    private int glowColor;
    private float radius;

    public FocusFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        radius = 0f;   // Hyprland rounding = 0
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(Math.max(RING_DP, LauncherConfig.get(context).borderDp) * density);
        halo.setStyle(Paint.Style.STROKE);
        tint.setColor(Theme.m(0x147AA2F7));
    }

    private boolean moving;

    /** Move mode: orange-to-red border like a Hyprland window being dragged. */
    void setMoving(boolean m) {
        moving = m;
        onSizeChanged(getWidth(), getHeight(), getWidth(), getHeight());
        invalidate();
    }

    void setRadius(float px) {
        radius = px;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LauncherConfig cfg = LauncherConfig.get(getContext());
        border.setShader(new LinearGradient(0, 0, Math.max(1, w), Math.max(1, h),
                // Hyprland col.active_border (themed: hyprland_active_border or accent; gradients at 45deg).
                moving ? Theme.m(0xFFFF9E64) : Theme.cur().focusBright(), moving ? Theme.m(0xFFF7768E) : Theme.lighten(cfg.theme.activeBorder2, 0.45f),
                Shader.TileMode.CLAMP));
        tint.setColor((cfg.accent & 0x00FFFFFF) | 0x14000000);
        glowColor = moving ? Theme.m(0xFFFF9E64) : Theme.cur().focusBright();
        halo.setColor(glowColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float inset = HALO_DP * density;   // room for the glow
        rect.set(inset, inset, getWidth() - inset, getHeight() - inset);
        // Glow: concentric bands outside the ring, fading outward (drawn first, so the ring sits on top).
        float band = 2.5f * density, out = 0f;
        for (float a : GLOW) {
            halo.setStrokeWidth(band);
            halo.setAlpha(Math.round(a * 255));
            float o = out + band / 2f;
            canvas.drawRoundRect(rect.left - o, rect.top - o, rect.right + o, rect.bottom + o, radius + o, radius + o, halo);
            out += band;
        }
        float half = border.getStrokeWidth() / 2f;
        canvas.drawRoundRect(rect.left + half, rect.top + half, rect.right - half, rect.bottom - half, radius, radius, border);
    }

}
