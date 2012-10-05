package com.jotabout.screeninfo;

/**
 * ScreenInfo
 * 
 * Display the screen configuration parameters for an Android device.
 * 
 * Copyright (c) 2011 Michael J. Portuesi (http://www.jotabout.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import java.lang.reflect.Method;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Screen is a model object that summarizes information about the
 * device's display.
 * 
 * It unifies information from a few disparate Android APIs (Display,
 * DisplayMetrics, Configuration), and derives some additional device
 * statistics.  It also provides helpers to format data as strings
 * for display.
 * 
 */
public class Screen {

	private final Context ctx;
	private final Display mDisplay;
	private final DisplayMetrics mMetrics;
	private final Configuration mConfig;
 	
	public final int mSizeClass;
	
	public final int widthPx;
	public final int heightPx;
	public final int widthDp;
	public final int heightDp;
	public final int smallestDp;
	 /* Smallest screen dimension in dp, (smallestWidthDp used in layout classification)
		(see: http://android-developers.blogspot.com/2011/07/new-tools-for-managing-screen-sizes.html) */
	public final int densityDpi; /* nominal screen density */
	public final float xdpi, ydpi; /* actual screen density */
	public double density;
	public float scaledDensity;
	 /* Scaling factor for fonts used on the display (DisplayMetrics.scaledDensity) */

	public final double xyPhysicalWidth, xyPhysicalHeight;
	public final double screenPhysicalWidth, screenPhysicalHeight;
	public final double xyDiagonalSizeInches, xyDiagonalSizeMillimeters;
	public final double screenDiagonalSizeInches, screenDiagonalSizeMillimeters;
	public final double xyWidthSizeInches, xyHeightSizeInches;
	public final double xyWidthSizeMillimeters, xyHeightSizeMillimeters;
	public final double screenWidthSizeInches, screenHeightSizeInches;
	public final double screenWidthSizeMillimeters, screenHeightSizeMillimeters;

    public final int screenLayout;
    public final int touchScreen;
    
    public final int defaultOrientation;
	 /* Default, or "natural" screen orientation of the device. */
	public final int currentOrientation;
        
    public final int pixelFormat;
    public final float refreshRate;

	private static class CodeName
	  /* mapping a constant value to a descriptive string resource ID. */
	  {
		public final int Value, ResID;

		public CodeName
		  (
			int Value,
			int ResID
		  )
		  {
			this.Value = Value;
			this.ResID = ResID;
		  } /*CodeName*/

	  } /*CodeName*/;

	private String GetCodeName
	  (
		int Value,
		CodeName[] Table
	  )
	  /* returns the string resource ID from the Table entry with the specified Value. */
	  {
		int Result = 0;
		for (int i = 0;;)
		  {
			if (i == Table.length)
				break;
			if (Table[i].Value == Value)
			  {
				Result = Table[i].ResID;
				break;
			  } /*if*/
			++i;
		  } /*for*/
		return
            Result != 0 ?
    			ctx.getString(Result)
            :
                String.format(ctx.getString(R.string.nosuch), Value);
	  } /*GetCodeName*/

	public Screen( Context ctx ) {
		this.ctx = ctx;
		final WindowManager wm = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE));
		mDisplay = wm.getDefaultDisplay();
 		mMetrics = new DisplayMetrics();
		mDisplay.getMetrics(mMetrics);
        mConfig = ctx.getResources().getConfiguration();

        // Screen Size classification
		mSizeClass = mConfig.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		
		// Screen dimensions
		  {
			final Point pt = new Point();
			try {
				// Try to get size without the Status bar, if we can (API level 13)
				final Method getSizeMethod = mDisplay.getClass().getMethod("getSize", Point.class);
				getSizeMethod.invoke( mDisplay, pt );
			} catch (Exception ignore) {
				// Use older APIs
				pt.x = mDisplay.getWidth();
				pt.y = mDisplay.getHeight();
			}
			widthPx = pt.x;
			heightPx = pt.y;
		  }
    	
		// Calculate screen sizes in device-independent pixels (dp)
		widthDp = (int) (((double) widthPx / mMetrics.density) + 0.5);
		heightDp = (int) (((double) heightPx / mMetrics.density) + 0.5);
		smallestDp = widthDp > heightDp ? heightDp : widthDp;

		// DPI
		densityDpi = mMetrics.densityDpi;
		
		// Screen density scaling factors
		density = mMetrics.density;
		scaledDensity = mMetrics.scaledDensity;
		
		// Normalize the xdpi/ydpi for the next set of calculations
		// Guard against divide-by-zero, possible with lazy device manufacturers
		// who set these fields incorrectly. Set the density to our best guess.
		xdpi = mMetrics.xdpi < 1.0f ? mMetrics.densityDpi : mMetrics.xdpi;
		ydpi = mMetrics.ydpi < 1.0f ? mMetrics.densityDpi : mMetrics.ydpi;
		
		// Calculate physical screen width/height
		xyPhysicalWidth = (float)mMetrics.widthPixels / xdpi;
		xyPhysicalHeight = (float)mMetrics.heightPixels / ydpi;
		screenPhysicalWidth = (float)mMetrics.widthPixels / mMetrics.densityDpi;
		screenPhysicalHeight = (float)mMetrics.heightPixels / mMetrics.densityDpi;

		// Calculate width and height screen size, in Metric units
		xyWidthSizeInches = Math.floor( xyPhysicalWidth * 10.0 + 0.5 ) / 10.0;
		xyHeightSizeInches = Math.floor( xyPhysicalHeight * 10.0 + 0.5 ) / 10.0;
		xyWidthSizeMillimeters = Math.floor( xyPhysicalWidth * 25.4 + 0.5 );
		xyHeightSizeMillimeters = Math.floor( xyPhysicalHeight * 25.4 + 0.5 );
		screenWidthSizeInches = Math.floor( screenPhysicalWidth * 10.0 + 0.5 ) / 10.0;
		screenHeightSizeInches = Math.floor( screenPhysicalHeight * 10.0 + 0.5 ) / 10.0;
		screenWidthSizeMillimeters = Math.floor( screenPhysicalWidth * 25.4 + 0.5 );
		screenHeightSizeMillimeters = Math.floor( screenPhysicalHeight * 25.4 + 0.5 );
		
		// Calculate diagonal screen size, in both U.S. and Metric units
		final double xyRawDiagonalSizeInches = Math.sqrt(Math.pow(xyPhysicalWidth, 2) + Math.pow(xyPhysicalHeight, 2));
		xyDiagonalSizeInches = Math.floor( xyRawDiagonalSizeInches * 10.0 + 0.5 ) / 10.0;
		xyDiagonalSizeMillimeters = Math.floor( xyRawDiagonalSizeInches * 25.4 + 0.5 );
		final double screenRawDiagonalSizeInches = Math.sqrt(Math.pow(screenPhysicalWidth, 2) + Math.pow(screenPhysicalHeight, 2));
		screenDiagonalSizeInches = Math.floor( screenRawDiagonalSizeInches * 10.0 + 0.5 ) / 10.0;
		screenDiagonalSizeMillimeters = Math.floor( screenRawDiagonalSizeInches * 25.4 + 0.5 );
		// Calculate diagonal screen size, in both U.S. and Metric units
		final double rawDiagonalSizeInches = Math.sqrt(Math.pow(xyPhysicalWidth, 2) + Math.pow(xyPhysicalHeight, 2));
		
		// Long/wide
        screenLayout = mConfig.screenLayout & Configuration.SCREENLAYOUT_LONG_MASK;
        
        // Orientation
        defaultOrientation = mConfig.orientation;
		// Do the best job we can to find out which way the screen is currently rotated.
		int rotation = -1;
		// First, try the Display#getRotation() call, which was introduced in Froyo.
		// Reference: http://android-developers.blogspot.com/2010/09/one-screen-turn-deserves-another.html
		try {
			final Method getRotationMethod = mDisplay.getClass().getMethod("getRotation");
			rotation = (Integer) getRotationMethod.invoke(mDisplay);
		}
		catch (SecurityException ignore) {}
		catch (NoSuchMethodException ignore) {} 
		catch (IllegalArgumentException ignore) {}
		catch (IllegalAccessException ignore) {}
		catch (java.lang.reflect.InvocationTargetException ignore) {}
		if (rotation >= 0)
		  {
			currentOrientation = rotation;
		  }
		else
		  {
			// Fall back on the deprecated Display#getOrientation method from earlier releases of Android.
			currentOrientation = mDisplay.getOrientation();
		  } /*if*/
        
        // Touchscreen type
        touchScreen = mConfig.touchscreen;
        
        // Pixel format
		pixelFormat = mDisplay.getPixelFormat();
		
		// Refresh rate
        refreshRate = mDisplay.getRefreshRate();
	}
 	
 	/**
	 * Model name of device.
	 * @return
	 */
	public String deviceModel() {
		return Build.MODEL;
	}
	
	/**
	 * Version of Android (e.g. "2.3.5").
	 * 
	 * @return
	 */
	public String androidVersion() {
		return Build.VERSION.RELEASE;
	}
	
	private static final CodeName[] SizeCodes = new CodeName[]
		{
			new CodeName(Configuration.SCREENLAYOUT_SIZE_SMALL, R.string.small),
			new CodeName(Configuration.SCREENLAYOUT_SIZE_NORMAL, R.string.normal),
			new CodeName(Configuration.SCREENLAYOUT_SIZE_LARGE, R.string.large),
			new CodeName(Configuration.SCREENLAYOUT_SIZE_XLARGE, R.string.xlarge),
			new CodeName(Configuration.SCREENLAYOUT_SIZE_UNDEFINED, R.string.undefined),
		};

	public String GetSizeName()
	  {
		return
			GetCodeName(mConfig.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK, SizeCodes);
	  } /*GetSizeName*/

	private static final CodeName[] DensityCodes = new CodeName[]
		{
			new CodeName(DisplayMetrics.DENSITY_LOW, R.string.ldpi),
			new CodeName(DisplayMetrics.DENSITY_MEDIUM, R.string.mdpi),
			new CodeName(DisplayMetrics.DENSITY_TV, R.string.tvdpi),
			new CodeName(DisplayMetrics.DENSITY_HIGH, R.string.hdpi),
			new CodeName(DisplayMetrics.DENSITY_XHIGH, R.string.xhdpi),
			new CodeName(/*DisplayMetrics.DENSITY_XXHIGH*/ 480, R.string.xxhdpi),
		};

	public String GetDensityName()
	  {
		return
			GetCodeName(mMetrics.densityDpi, DensityCodes);
	  } /*GetDensityName*/

	private static final CodeName[] LongWideCodes = new CodeName[]
		{
			new CodeName(Configuration.SCREENLAYOUT_LONG_YES, R.string.yes),
			new CodeName(Configuration.SCREENLAYOUT_LONG_NO, R.string.no),
			new CodeName(Configuration.SCREENLAYOUT_LONG_UNDEFINED, R.string.undefined),
		};
	
	/**
	 * Screen layout, as text
	 */
	public String screenLayoutText() {
		return
			GetCodeName(mConfig.screenLayout & Configuration.SCREENLAYOUT_LONG_MASK, LongWideCodes);
	}

	private static final CodeName[] OrientationCodes = new CodeName[]
		{
			new CodeName(Configuration.ORIENTATION_LANDSCAPE, R.string.orientation_landscape),
			new CodeName(Configuration.ORIENTATION_PORTRAIT, R.string.orientation_portrait),
			new CodeName(Configuration.ORIENTATION_SQUARE, R.string.orientation_square),
			new CodeName(Configuration.ORIENTATION_UNDEFINED, R.string.undefined),
		};
	
	/**
	 * Default orientation as text
	 */
	public String defaultOrientationText() {
		return
			GetCodeName(defaultOrientation, OrientationCodes);
	}

	private static final CodeName[] RotationAngles = new CodeName[]
		{
			new CodeName(Surface.ROTATION_0, R.string.degrees_0),
			new CodeName(Surface.ROTATION_90, R.string.degrees_90),
			new CodeName(Surface.ROTATION_180, R.string.degrees_180),
			new CodeName(Surface.ROTATION_270, R.string.degrees_270),
		};
	
	/**
	 * Current orientation as text
	 */
	public String currentOrientationText() {
		return
			GetCodeName(currentOrientation, RotationAngles);
	}

	private static final CodeName[] TouchScreenCodes = new CodeName[]
		{
			new CodeName(Configuration.TOUCHSCREEN_FINGER, R.string.touchscreen_finger),
			new CodeName(Configuration.TOUCHSCREEN_STYLUS, R.string.touchscreen_stylus),
			new CodeName(Configuration.TOUCHSCREEN_NOTOUCH, R.string.touchscreen_none),
			new CodeName(Configuration.TOUCHSCREEN_UNDEFINED, R.string.undefined),
		};
	
	/**
	 * Touchscreen properties as text
	 */
	public String touchScreenText() {
		return
			GetCodeName(touchScreen, TouchScreenCodes);
	}

    private static final CodeName[] PixelFormatCodes = new CodeName[]
        {
            new CodeName(PixelFormat.A_8, R.string.a_8),
            new CodeName(PixelFormat.JPEG, R.string.jpeg),
            new CodeName(PixelFormat.L_8, R.string.l_8),
            new CodeName(PixelFormat.LA_88, R.string.la_88),
            new CodeName(PixelFormat.OPAQUE, R.string.opaque),
            new CodeName(PixelFormat.RGB_332, R.string.rgb_332),
            new CodeName(PixelFormat.RGB_565, R.string.rgb_565),
            new CodeName(PixelFormat.RGB_888, R.string.rgb_888),
            new CodeName(PixelFormat.RGBA_4444, R.string.rgba_4444),
            new CodeName(PixelFormat.RGBA_5551, R.string.rgba_5551),
            new CodeName(PixelFormat.RGBA_8888, R.string.rgba_8888),
            new CodeName(PixelFormat.RGBX_8888, R.string.rgbx_8888),
            new CodeName(5, R.string.bgra_8888), /* see platform/system/core/include/system/graphics.h */
            new CodeName(PixelFormat.TRANSLUCENT, R.string.translucent),
            new CodeName(PixelFormat.TRANSPARENT, R.string.transparent),
            new CodeName(PixelFormat.UNKNOWN, R.string.unknown),
            new CodeName(ImageFormat.NV21, R.string.nv21),
            new CodeName(ImageFormat.YUY2, R.string.yuy2),
            new CodeName(ImageFormat.NV16, R.string.nv16),
        };
	
	/**
	 * Pixel format as text
	 */
	public String pixelFormatText() {
		return
			GetCodeName(pixelFormat, PixelFormatCodes);
	}

	/**
	 * Return a string containing a text-based summary, suitable
	 * to share, email, save to SD card, etc.
	 * 
	 * @param ctx
	 * @return
	 */
	public String summaryText( Context ctx ) {
		StringBuilder sb = new StringBuilder();
		
		addLine( sb, ctx, R.string.device_label, 						deviceModel() );
		addLine( sb, ctx, R.string.os_version_label, 					androidVersion() );
		addLine( sb, ctx, R.string.screen_class_label, 					GetSizeName() );
		addLine( sb, ctx, R.string.density_class_label, 				GetDensityName() );
		addLine( sb, ctx, R.string.width_pixels_label, 					widthPx );
		addLine( sb, ctx, R.string.height_pixels_label, 				heightPx );
		addLine( sb, ctx, R.string.width_dp_label, 						widthDp );
		addLine( sb, ctx, R.string.height_dp_label, 					heightDp );
		addLine( sb, ctx, R.string.smallest_dp_label, 					smallestDp );
		addLine( sb, ctx, R.string.long_wide_label, 					screenLayoutText() );
		addLine( sb, ctx, R.string.natural_orientation_label, 			defaultOrientationText() );
		addLine( sb, ctx, R.string.current_orientation_label, 			currentOrientationText() );
		addLine( sb, ctx, R.string.touchscreen_label, 					touchScreenText() );
		addLine( sb, ctx, R.string.screen_dpi_label, 					densityDpi );
		addLine( sb, ctx, R.string.actual_xdpi_label, 					xdpi );
		addLine( sb, ctx, R.string.actual_ydpi_label, 					ydpi );
		addLine( sb, ctx, R.string.logical_density_label, 				density );
		addLine( sb, ctx, R.string.font_scale_density_label, 			scaledDensity );
		addLine(sb, ctx, R.string.xy_diagonal_size_inches_label, xyDiagonalSizeInches);
		addLine(sb, ctx, R.string.xy_diagonal_size_mm_label, xyDiagonalSizeMillimeters);
		addLine(sb, ctx, R.string.xy_width_size_inches_label, xyWidthSizeInches);
		addLine(sb, ctx, R.string.xy_height_size_inches_label, xyHeightSizeInches);
		addLine(sb, ctx, R.string.xy_width_size_mm_label, xyWidthSizeMillimeters);
		addLine(sb, ctx, R.string.xy_height_size_mm_label, xyHeightSizeMillimeters);
		addLine(sb, ctx, R.string.screen_diagonal_size_inches_label, screenDiagonalSizeInches);
		addLine(sb, ctx, R.string.screen_diagonal_size_mm_label, screenDiagonalSizeMillimeters);
		addLine(sb, ctx, R.string.screen_width_size_inches_label, screenWidthSizeInches);
		addLine(sb, ctx, R.string.screen_height_size_inches_label, screenHeightSizeInches);
		addLine(sb, ctx, R.string.screen_width_size_mm_label, screenWidthSizeMillimeters);
		addLine(sb, ctx, R.string.screen_height_size_mm_label, screenHeightSizeMillimeters);
		addLine( sb, ctx, R.string.pixel_format_label, 					pixelFormatText() );
		addLine( sb, ctx, R.string.refresh_rate_label, 					refreshRate );
		
		return sb.toString();
	}
	
	private void addLine( StringBuilder sb, Context ctx, int resId, String value ) {
		sb.append( ctx.getString( resId ) ).append( " " ).append( value ).append( "\n" );
	}
	
	private void addLine( StringBuilder sb, Context ctx, int resId, int value ) {
		addLine( sb, ctx, resId, Integer.toString(value) );
	}
	
	private void addLine( StringBuilder sb, Context ctx, int resId, float value ) {
		addLine( sb, ctx, resId, Float.toString(value) );
	}
	
	private void addLine( StringBuilder sb, Context ctx, int resId, double value ) {
		addLine( sb, ctx, resId, Double.toString(value) );
	}

}