package de.tum.in.tumcampusapp.auxiliary;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * Subclass of drawable that can draw a subway line icon
 */
class MVVSymbolView extends Drawable {
    private final Paint mBgPaint;
    private final RectF mRect;
    private int mTriangle;
    private boolean mRounded;
    private int mTextColor;

    /**
     * Standard constructor
     * @param line Line symbol name e.g. U6, S1, T14
     */
    public MVVSymbolView(String line) {
        mRect = new RectF();
        mRounded = false;
        mTextColor = 0xffffffff;
        mTriangle = 0;
        int backgroundColor = 0;

        try {
            switch (line.charAt(0)) {
                case 'S':
                    int num = Integer.parseInt(line.substring(1));
                    mRounded = true;
                    if (num <= 8)
                        backgroundColor = sLineColor[num - 1];
                    else if (num == 20)
                        backgroundColor = 0xffca536a;
                    else if (num == 27)
                        backgroundColor = 0xffd99098;
                    mTextColor = num == 8 ? 0xfff1cb00 : 0xffffffff;
                    break;
                case 'U':
                    num = Integer.parseInt(line.substring(1));
                    if (num == 7) {
                        mTriangle = 1;
                        backgroundColor = 0xffc10134;
                    } else if (num == 8) {
                        mTriangle = 2;
                        backgroundColor = 0xffeb6a27;
                    } else {
                        backgroundColor = uLineColor[num - 1];
                    }
                    mTextColor = 0xfffcfefc;
                    break;
                case 'N':
                    backgroundColor = 0xff000000;
                    mTextColor = 0xffebd22e;
                    break;
                case 'X':
                    backgroundColor = 0xff477f70;
                    break;
                default:
                    num = Integer.parseInt(line);
                    if (num < 50) {
                        backgroundColor = 0xffdc261c;
                        mTextColor = 0xfffcfefc;
                    } else if (num < 90) {
                        backgroundColor = 0xfffc6604;
                    } else {
                        backgroundColor = 0xff004a5d;
                    }
            }
        } catch (NumberFormatException e) {
            //Leave default
        }
        mBgPaint = new Paint();
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setColor(backgroundColor);
    }

    /**
     * Gets the text color that should be used to draw text on top of this drawable
     * @return Text color
     */
    public int getTextColor() {
        return mTextColor;
    }

    private static final int sLineColor[] = {
            0xff6db9df, 0xff8db335, 0xff7d187b, 0xffc1003c, 0, 0xff00915e, 0xff7f3229, 0xff1f1e21
    };
    private static final int uLineColor[] = {
            0xff44723c, 0xffcc263c, 0xffe4721c, 0xff04a27c, 0xffa4721c, 0xff045ea4
    };

    @Override
    public void draw(Canvas canvas) {
        final int width = canvas.getWidth();
        final int height = canvas.getHeight();
        mRect.top = 0;
        mRect.bottom = height;
        mRect.left = 0;
        mRect.right = width;
        if(mRounded) {
            canvas.drawRoundRect(mRect, height/2.0f, height/2.0f, mBgPaint);
        } else if(mTriangle==1) {
            drawTriangle(canvas, 0xff51812e);
        } else if(mTriangle==2) {
            drawTriangle(canvas, 0xffc10134);
        } else {
            canvas.drawRect(mRect, mBgPaint);
        }
    }

    private void drawTriangle(Canvas canvas, int color) {
        canvas.drawRect(mRect, mBgPaint);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        Point point1 = new Point(0,0);
        Point point2 = new Point((int)mRect.right,0);
        Point point3 = new Point(0,(int)mRect.bottom);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(point1.x,point1.y);
        path.lineTo(point2.x,point2.y);
        path.lineTo(point3.x,point3.y);
        path.lineTo(point1.x,point1.y);
        path.close();

        canvas.drawPath(path, paint);
    }

    @Override
    public void setAlpha(int i) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}