package com.thomassnow.xmastree.ui.views;

import com.thomassnow.xmastree.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class ColorView extends View
	{

		private Paint paint;
		private Rect rCanvas;
		private int color;
		private int height;
		private int width;
		private static final int sizeBox = 40;
		private boolean selected;
		private Rect rColor;

		public ColorView(Context context)
			{
				super(context);
				color = 0;
				paint = new Paint(0);
				rCanvas = new Rect();
				rCanvas.left = 0;
				rCanvas.top = 0;
				rCanvas.right = sizeBox;
				rCanvas.bottom = sizeBox;
				height = width = sizeBox;
				selected = false;
				rColor = new Rect(rCanvas);
				rColor.left +=2;
				rColor.top += 2;
				rColor.right -= 2;
				rColor.bottom -= 2;
			}

		public ColorView(Context context, AttributeSet attrs)
			{
				super(context, attrs);
				color = 0;
				TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ColorView, 0, 0);

				paint = new Paint(0);
				try
					{
						color = a.getInteger(R.styleable.ColorView_color, 0);
					}
				finally
					{
						a.recycle();
					}
				rCanvas = new Rect();
				rCanvas.left = 0;
				rCanvas.top = 0;
				rCanvas.right = sizeBox;
				rCanvas.bottom = sizeBox;
				height = width = sizeBox;
				rColor = new Rect(rCanvas);
				rColor.left +=2;
				rColor.top += 2;
				rColor.right -= 2;
				rColor.bottom -= 2;
				selected = false;
			}

		public ColorView(Context context, AttributeSet attrs, int defStyleAttr)
			{
				super(context, attrs, defStyleAttr);
				color = 0;
				paint = new Paint(0);
				rCanvas = new Rect();
				rCanvas.left = 0;
				rCanvas.top = 0;
				rCanvas.right = sizeBox;
				rCanvas.bottom = sizeBox;
				height = width = sizeBox;
				rColor = new Rect(rCanvas);
				rColor.left +=2;
				rColor.top += 2;
				rColor.right -= 2;
				rColor.bottom -= 2;
				selected = false;
			}

		protected void onDraw(Canvas canvas)
			{
				super.onDraw(canvas);
				boolean bEnabled = isEnabled();
				
				if(bEnabled)
					paint.setColor(0xff000000);
				else
					paint.setColor(0xffc0c0c0);
				
				canvas.drawRect(rCanvas, paint);
				if((color & 0xff000000) != 0)
					{
						paint.setColor(color);
						canvas.drawRect(rColor, paint);
					}
				else
					{
						paint.setColor(0xffc0c0c0);
						canvas.drawRect(rColor, paint);
						if(bEnabled)
							{
								paint.setColor(0xffff0000);
							}
						else
							{
								paint.setColor(0xffc0c0c0);
							}
						paint.setStrokeWidth(2);
						canvas.drawLine((float) rColor.right, (float)rColor.top, (float)rColor.left , (float)rColor.bottom , paint);
					}
				if(selected)
					{
						if(bEnabled)
							{
								int x = color ^ 0x00ffffff;
								x |= 0xff000000;
								paint.setColor(x);
							}
						else
							{
								paint.setColor(0xffc0c0c0);
							}
						canvas.drawCircle(rColor.right/2, rColor.bottom/2, 4, paint);
					}
			}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
			{
//				// Try for a width based on our minimum
//				//int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
//				int minw = getSuggestedMinimumWidth();
//				int w = resolveSizeAndState(minw, widthMeasureSpec, 1);
//				if(w > 0)
//					width = w;
//
//				// Whatever the width ends up being, ask for a height that would
//				// let the pie
//				// get as big as it can
//				int minh = (int) getSuggestedMinimumHeight();// + getPaddingBottom() + getPaddingTop();
//				int h = resolveSizeAndState(minh, heightMeasureSpec, 1);
//				if(h > 0)
//					height = h;

				//super.onMeasure(heightMeasureSpec, widthMeasureSpec);
				height = getSuggestedMinimumHeight();
				width = getSuggestedMinimumWidth();
				if(width > height)
					width = height;
				else
					height = width;
				
				width = resolveSizeAndState(width, widthMeasureSpec, 1);
				height = resolveSizeAndState(height, heightMeasureSpec, 1);
				
				setMeasuredDimension(width, height);
				rCanvas.right = width-1;
				rCanvas.bottom = height-1;
				rColor.left = 2;
				rColor.top = 2;
				rColor.right = rCanvas.right - 2;
				rColor.bottom = rCanvas.bottom - 2;
			}
		
		public void setColor(int color)
			{
				this.color = color;
				invalidate();
				requestLayout();
			}
		
		public int getColor()
			{
				return color;
			}
		
		public boolean isSelected()
			{
				return selected;
			}
		
		public void setSelected(boolean b)
			{
				selected = b;
				invalidate();
				requestLayout();
			}
	}
