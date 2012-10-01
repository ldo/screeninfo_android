package com.jotabout.screeninfo;

/**
 * ScreenInfo
 * 
 * A simple app to display the screen configuration parameters for an
 * Android device.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class ScreenInfo extends Activity {
	
	private final static int ABOUT_DIALOG = 1;
	private final static int MENU_ABOUT = Menu.FIRST;
	Dialog mAbout;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    @Override
	protected void onResume() {
		super.onResume();

		final WindowManager wm = ((WindowManager) getSystemService(Context.WINDOW_SERVICE));
		final Display display = wm.getDefaultDisplay();
 		final DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
        final Configuration config = getResources().getConfiguration();
		
        showDeviceInfo(config, metrics);
        showScreenMetrics(display, metrics);
    	showScreenDiagonalSize(metrics);
    	showScreenLongWide(config);
        showDefaultOrientation(config);
        showCurrentOrientation(display);
        showTouchScreen(config);
	}

	static class CodeName
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

	static final CodeName[] SizeCodes = new CodeName[]
		{
			new CodeName(Configuration.SCREENLAYOUT_SIZE_SMALL, R.string.small),
			new CodeName(Configuration.SCREENLAYOUT_SIZE_NORMAL, R.string.normal),
			new CodeName(Configuration.SCREENLAYOUT_SIZE_LARGE, R.string.large),
			new CodeName(Configuration.SCREENLAYOUT_SIZE_XLARGE, R.string.xlarge),
			new CodeName(Configuration.SCREENLAYOUT_SIZE_UNDEFINED, R.string.undefined),
		};

	static final CodeName[] DensityCodes = new CodeName[]
		{
			new CodeName(DisplayMetrics.DENSITY_LOW, R.string.ldpi),
			new CodeName(DisplayMetrics.DENSITY_MEDIUM, R.string.mdpi),
			new CodeName(/*DisplayMetrics.DENSITY_TV*/ 213, R.string.tvdpi),
			new CodeName(DisplayMetrics.DENSITY_HIGH, R.string.hdpi),
			new CodeName(DisplayMetrics.DENSITY_XHIGH, R.string.xhdpi),
		};

	String GetCodeName
	  (
		int Value,
		CodeName[] Table
	  )
	  /* returns the string resource ID matching the specified table value. */
	  {
		int Result = 0;
		for (int i = 0;;)
		  {
			if (i == Table.length)
			  {
				Result = R.string.nosuch;
				break;
			  } /*if*/
			if (Table[i].Value == Value)
			  {
				Result = Table[i].ResID;
				break;
			  } /*if*/
			++i;
		  } /*for*/
		return
			getString(Result);
	  } /*GetCodeName*/

	/**
     * Show basic information about the device.
     */
    public void showDeviceInfo(Configuration config, DisplayMetrics metrics) {
        ((TextView) findViewById(R.id.device_name)).setText( Build.MODEL );
        ((TextView) findViewById(R.id.os_version)).setText( Build.VERSION.RELEASE );
		((TextView)findViewById(R.id.screen_size_name)).setText
		  (
			GetCodeName(config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK, SizeCodes)
		  );
		((TextView)findViewById(R.id.screen_dpi_name)).setText
		  (
			GetCodeName(metrics.densityDpi, DensityCodes)
		  );
    }
    
    /**
     * Show the screen metrics (pixel dimensions, density, dpi, etc) for the device.
     */
    public void showScreenMetrics(Display display, DisplayMetrics metrics) {
    	
        ((TextView) findViewById(R.id.width_pixels)).setText( Integer.toString(display.getWidth()) );
        ((TextView) findViewById(R.id.height_pixels)).setText( Integer.toString(display.getHeight()) );
        ((TextView) findViewById(R.id.screen_dpi)).setText( Integer.toString(metrics.densityDpi) );
        ((TextView) findViewById(R.id.actual_xdpi)).setText( Float.toString(metrics.xdpi) );
        ((TextView) findViewById(R.id.actual_ydpi)).setText( Float.toString(metrics.ydpi) );
        ((TextView) findViewById(R.id.logical_density)).setText( Double.toString(metrics.density) );
        ((TextView) findViewById(R.id.font_scale_density)).setText( Float.toString(metrics.scaledDensity) );
    }

    /**
     * Calculate and display the physical diagonal size of the screen.
     * The size is calculated in inches, rounded to one place after decimal (e.g. '3.7', '10.1')
     * The size is also calculated in millimeters
     * 
     * @param metrics
     */
	private void showScreenDiagonalSize(DisplayMetrics metrics) {
		
		double xdpi = metrics.xdpi;
		if ( xdpi < 1.0 ) {
			// Guard against divide-by-zero, possible with lazy device manufacturers who set these fields incorrectly
			// Set the density to our best guess.
			xdpi = metrics.densityDpi;
		}
		double ydpi = metrics.ydpi;
		if ( ydpi < 1.0 ) {
			ydpi =  metrics.densityDpi;
		}
		
		// Calculate physical screen width/height
		double xyPhysicalWidth = ((double) metrics.widthPixels) / xdpi;
		double xyPhysicalHeight = ((double) metrics.heightPixels) / ydpi;
		double screenPhysicalWidth = ((double) metrics.widthPixels) / metrics.densityDpi;
		double screenPhysicalHeight = ((double) metrics.heightPixels) / metrics.densityDpi;
		
		// Calculate width and height screen size, in Metric units
		double xyWidthSizeInches = Math.floor( xyPhysicalWidth * 10.0 + 0.5 ) / 10.0;
		double xyHeightSizeInches = Math.floor( xyPhysicalHeight * 10.0 + 0.5 ) / 10.0;
		double xyWidthSizeMillimeters = Math.floor( xyPhysicalWidth * 25.4 + 0.5 );
		double xyHeightSizeMillimeters = Math.floor( xyPhysicalHeight * 25.4 + 0.5 );
		double screenWidthSizeInches = Math.floor( screenPhysicalWidth * 10.0 + 0.5 ) / 10.0;
		double screenHeightSizeInches = Math.floor( screenPhysicalHeight * 10.0 + 0.5 ) / 10.0;
		double screenWidthSizeMillimeters = Math.floor( screenPhysicalWidth * 25.4 + 0.5 );
		double screenHeightSizeMillimeters = Math.floor( screenPhysicalHeight * 25.4 + 0.5 );
		
		// Calculate diagonal screen size, in both U.S. and Metric units
		double xyRawDiagonalSizeInches = Math.sqrt(Math.pow(xyPhysicalWidth, 2) + Math.pow(xyPhysicalHeight, 2));
		double xyDiagonalSizeInches = Math.floor( xyRawDiagonalSizeInches * 10.0 + 0.5 ) / 10.0;
		double xyDiagonalSizeMillimeters = Math.floor( xyRawDiagonalSizeInches * 25.4 + 0.5 );
		double screenRawDiagonalSizeInches = Math.sqrt(Math.pow(screenPhysicalWidth, 2) + Math.pow(screenPhysicalHeight, 2));
		double screenDiagonalSizeInches = Math.floor( screenRawDiagonalSizeInches * 10.0 + 0.5 ) / 10.0;
		double screenDiagonalSizeMillimeters = Math.floor( screenRawDiagonalSizeInches * 25.4 + 0.5 );
		
		
        ((TextView) findViewById(R.id.xy_diagonal_size_inches)).setText( Double.toString(xyDiagonalSizeInches) );
        ((TextView) findViewById(R.id.xy_diagonal_size_mm)).setText( Double.toString(xyDiagonalSizeMillimeters) );
        ((TextView) findViewById(R.id.xy_width_size_inches)).setText(Double.toString(xyWidthSizeInches));
        ((TextView) findViewById(R.id.xy_height_size_inches)).setText(Double.toString(xyHeightSizeInches));
        ((TextView) findViewById(R.id.xy_width_size_mm)).setText(Double.toString(xyWidthSizeMillimeters));
        ((TextView) findViewById(R.id.xy_height_size_mm)).setText(Double.toString(xyHeightSizeMillimeters));
        ((TextView) findViewById(R.id.screen_diagonal_size_inches)).setText( Double.toString(screenDiagonalSizeInches) );
        ((TextView) findViewById(R.id.screen_diagonal_size_mm)).setText( Double.toString(screenDiagonalSizeMillimeters) );
        ((TextView) findViewById(R.id.screen_width_size_inches)).setText(Double.toString(screenWidthSizeInches));
        ((TextView) findViewById(R.id.screen_height_size_inches)).setText(Double.toString(screenHeightSizeInches));
        ((TextView) findViewById(R.id.screen_width_size_mm)).setText(Double.toString(screenWidthSizeMillimeters));
        ((TextView) findViewById(R.id.screen_height_size_mm)).setText(Double.toString(screenHeightSizeMillimeters));
	}
	
	/**
	 * Display whether or not the device has a display that is longer or wider than normal.
	 */
	private void showScreenLongWide(Configuration config) {
        TextView longWideText = ((TextView) findViewById(R.id.long_wide));
        
        int screenLongLayout = config.screenLayout & Configuration.SCREENLAYOUT_LONG_MASK;
        switch (screenLongLayout) {
        case Configuration.SCREENLAYOUT_LONG_YES:
        	longWideText.setText(R.string.yes);
        	break;
        case Configuration.SCREENLAYOUT_LONG_NO:
        	longWideText.setText(R.string.no);
        	break;
        case Configuration.SCREENLAYOUT_LONG_UNDEFINED:
        	longWideText.setText(R.string.undefined);
        	break;
        }
        
	}

	/**
	 * Display the "natural" screen orientation of the device.
	 */
	private void showDefaultOrientation(Configuration config) {
		// Screen default orientation
        TextView orientationText = ((TextView) findViewById(R.id.natural_orientation));
        setOrientationText(orientationText, config.orientation);
	}

	/**
	 * Display the current screen orientation of the device, with respect to natural orientation.
	 */
	private void showCurrentOrientation(Display display) {
        TextView orientationText = ((TextView) findViewById(R.id.current_orientation));
		
		// First, try the Display#getRotation() call, which was introduced in Froyo.
		// Reference: http://android-developers.blogspot.com/2010/09/one-screen-turn-deserves-another.html
		try {
			Method getRotationMethod = display.getClass().getMethod("getRotation");
			int rotation = (Integer) getRotationMethod.invoke(display);
			switch (rotation) {
			case Surface.ROTATION_0:
				orientationText.setText("0");
				break;
			case Surface.ROTATION_90:
				orientationText.setText("90");
				break;
			case Surface.ROTATION_180:
				orientationText.setText("180");
				break;
			case Surface.ROTATION_270:
				orientationText.setText("270");
				break;
			}
			
			return;
		}
		catch (SecurityException ignore) {;}
		catch (NoSuchMethodException ignore) {;} 
		catch (IllegalArgumentException ignore) {;}
		catch (IllegalAccessException ignore) {;}
		catch (InvocationTargetException ignore) {;}
		
		// Fall back on the deprecated Display#getOrientation method from earlier releases of Android.
		int orientation = display.getOrientation();
		setOrientationText( orientationText, orientation );
	}
	
	/**
	 * Helper sets an orientation string in the given text widget.
	 * 
	 * @param orientationText
	 * @param orientation
	 */
	private void setOrientationText(TextView orientationText, int orientation) {
		switch ( orientation ) {
        case Configuration.ORIENTATION_LANDSCAPE:
        	orientationText.setText(R.string.orientation_landscape);
        	break;
        case Configuration.ORIENTATION_PORTRAIT:
        	orientationText.setText(R.string.orientation_portrait);
        	break;
        case Configuration.ORIENTATION_SQUARE:
        	orientationText.setText(R.string.orientation_square);
        	break;
        case Configuration.ORIENTATION_UNDEFINED:
        	orientationText.setText(R.string.undefined);
        	break;
        }
	}
	
	private void showTouchScreen(Configuration config) {
        TextView touchScreenText = ((TextView) findViewById(R.id.touchscreen));
        
        switch (config.touchscreen ) {
        case Configuration.TOUCHSCREEN_FINGER:
        	touchScreenText.setText(R.string.touchscreen_finger);
        	break;
        case Configuration.TOUCHSCREEN_STYLUS:
        	touchScreenText.setText(R.string.touchscreen_stylus);        	
        	break;
        case Configuration.TOUCHSCREEN_NOTOUCH:
        	touchScreenText.setText(R.string.touchscreen_none);
        	break;
        case Configuration.TOUCHSCREEN_UNDEFINED:
        	touchScreenText.setText(R.string.undefined);
        	break;
        }
	}
	
	/**
	 * Helper returns a string containing version number from the package manifest.
	 */
	private String appVersion() {
		String version = "";
		PackageInfo info;
		try {
			info = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = this.getString(R.string.version) + " " + info.versionName;
		} catch (NameNotFoundException ignore) {;}

		return version;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		mAbout = null;

		switch( id ) {
		case ABOUT_DIALOG:
	        mAbout = new Dialog(this);
	        mAbout.setContentView(R.layout.about_dialog);
	        mAbout.setTitle(R.string.about_title);
	        ((TextView) mAbout.findViewById(R.id.about_version)).setText(appVersion());
	        ((Button) mAbout.findViewById(R.id.about_dismiss)).setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					mAbout.dismiss();
				}
	        });
		}

		return mAbout;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add( 0, MENU_ABOUT, 0, R.string.about_menu );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch( item.getItemId() ) {
		case MENU_ABOUT:
			showDialog(ABOUT_DIALOG);
			return true;
		}
		
		return false;
	}
}
