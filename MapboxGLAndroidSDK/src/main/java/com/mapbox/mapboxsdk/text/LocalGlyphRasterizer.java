package com.mapbox.mapboxsdk.text;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Typeface;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

/**
 * LocalGlyphRasterizer is the Android-specific platform implementation used
 * by the portable local_glyph_rasterizer.hpp
 */
@Keep
public class LocalGlyphRasterizer {

  public class GlyphMetrics {
    public int width;
    public int height;
    public int top;
    public int left;
    public int ascender;
    public int descender;
    public int advance;
    public Bitmap glyphBitmap;

    GlyphMetrics() {
      this.glyphBitmap = Bitmap.createBitmap(35, 35, Bitmap.Config.ARGB_8888);
      this.width = 0;
      this.height = 0;
      this.top = 0;
      this.left = 0;
      this.ascender = 0;
      this.descender = 0;
      this.advance = 0;
    }
  }
  private final GlyphMetrics glyphMetrics;

  private final Bitmap bitmap;
  @NonNull
  private final Paint paint;
  @NonNull
  private final Canvas canvas;

  LocalGlyphRasterizer() {
    /*
      35x35px dimensions are hardwired to match local_glyph_rasterizer.cpp
      These dimensions are large enough to draw a 24 point character in the middle
      of the bitmap (y: 20) with some buffer around the edge
    */
    bitmap = Bitmap.createBitmap(35, 35, Bitmap.Config.ARGB_8888);
    glyphMetrics = this.new GlyphMetrics();
    paint = new Paint();
    paint.setAntiAlias(true);
    paint.setTextSize(24);

    canvas = new Canvas();
    canvas.setBitmap(glyphMetrics.glyphBitmap);
  }

  /***
   * Uses Android-native drawing code to rasterize a single glyph
   * to a square {@link Bitmap} which can be returned to portable
   * code for transformation into a Signed Distance Field glyph.
   *
   * @param fontFamily Font family string to pass to Typeface.create
   * @param bold If true, use Typeface.BOLD option
   * @param glyphID 16-bit Unicode BMP codepoint to draw
   *
   * @return Return a {@link Bitmap} to be displayed in the requested tile.
   */
  @WorkerThread
  protected Bitmap drawGlyphBitmap(String fontFamily, boolean bold, char glyphID) {
    paint.setTypeface(Typeface.create(fontFamily, bold ? Typeface.BOLD : Typeface.NORMAL));
    canvas.drawColor(Color.WHITE);
    canvas.drawText(String.valueOf(glyphID), 5, 25, paint);
    return glyphMetrics.glyphBitmap;
  }

  @WorkerThread
  protected GlyphMetrics getGlyphMetrics(String fontFamily, boolean bold, char glyphID) {
    paint.setTypeface(Typeface.create(fontFamily, bold ? Typeface.BOLD : Typeface.NORMAL));
    canvas.drawColor(Color.WHITE);
    canvas.drawText(String.valueOf(glyphID), 5, 25, paint);

    Paint.FontMetricsInt metrics = paint.getFontMetricsInt();
    glyphMetrics.ascender = Math.abs(metrics.ascent);
    glyphMetrics.descender = Math.abs(metrics.descent);

    Rect bounds = new Rect();
    paint.getTextBounds(String.valueOf(glyphID), 0, 1, bounds);
    glyphMetrics.left = bounds.left;
    glyphMetrics.width = bounds.right - bounds.left;
    glyphMetrics.height = bounds.bottom - bounds.top;
    glyphMetrics.top = Math.abs(bounds.top) - glyphMetrics.ascender;
    float width = paint.measureText(String.valueOf(glyphID), 0, 1);
    glyphMetrics.advance = Math.round(width);
//    glyphMetrics.glyphBitmap = bitmap;
//    Bitmap emptyBitmap = Bitmap.createBitmap(35, 35, Bitmap.Config.ARGB_8888);

    return glyphMetrics;
  }
}
