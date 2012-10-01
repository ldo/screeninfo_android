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
			new CodeName(/*DisplayMetrics.DENSITY_XXHIGH*/ 480, R.string.xxhdpi),
		};

	String GetCodeName
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

	abstract class InfoMember
	  /* obtaining and displaying a value from a member of an info structure. */
	  {
		final Object InObject;
		/*final*/ java.lang.reflect.Method ToString;
		final int TextID;

		public InfoMember
		  (
			Object InObject, /* the info structure */
			int TextID /* ID of TextView to be set to value */
		  )
		  {
			this.InObject = InObject;
			this.TextID = TextID;
		  } /*InfoMember*/

		protected void Init
		  (
			Class<?> MemberType
		  )
		  /* remainder of common initialization that can't be done in constructor. */
		  {
			if (MemberType.isPrimitive())
			  {
				try
				  {
					final String TypeName = MemberType.getName().intern();
					if (TypeName == "int")
					  {
						MemberType = Class.forName("java.lang.Integer");
					  } /*if*/
					else if (TypeName == "float")
					  {
						MemberType = Class.forName("java.lang.Float");
					  } /*if*/
				  /* add other replacements of primitive types here as necessary */
				  }
				catch (ClassNotFoundException err)
				  {
					throw new RuntimeException(err.toString());
				  } /*try*/
			  } /*if*/
			try
			  {
				this.ToString = MemberType.getDeclaredMethod("toString");
			  }
			catch (NoSuchMethodException err)
			  {
				throw new RuntimeException(err.toString());
			  } /*try*/
		  } /*Init*/

		abstract public Object GetValue();
		  /* must return the value to be displayed. */

		public void ShowValue()
		  /* sets the field to show the member return value. */
		  {
			try
			  {
				((TextView)findViewById(TextID)).setText((String)ToString.invoke(GetValue()));
			  }
			catch (IllegalAccessException err)
			  {
				throw new RuntimeException(err.toString());
			  }
			catch (InvocationTargetException err)
			  {
				throw new RuntimeException(err.toString());
			  } /*try*/
		  } /*ShowValue*/

	  } /*InfoMember*/;

	class InfoField extends InfoMember
	  /* obtaining and displaying a field value from an info structure. */
	  {
		final java.lang.reflect.Field ObjField;

		public InfoField
		  (
			Object InObject, /* the info structure */
			String FieldName, /* value of this field will be shown */
			int TextID /* ID of TextView to be set to value */
		  )
		  {
			super(InObject, TextID);
			try
			  {
				this.ObjField = InObject.getClass().getDeclaredField(FieldName);
			  }
			catch (NoSuchFieldException err)
			  {
				throw new RuntimeException(err.toString());
			  } /*try*/
			Init(this.ObjField.getType());
		  } /*InfoField*/

		public Object GetValue()
		  {
			try
			  {
				return
					ObjField.get(InObject);
			  }
			catch (IllegalAccessException err)
			  {
				throw new RuntimeException(err.toString());
			  } /*try*/
		  } /*GetValue*/

	  } /*InfoField*/;

	class InfoMethod extends InfoMember
	  /* obtaining and displaying a method return value from an info structure. */
	  {
		final java.lang.reflect.Method ObjMethod;

		public InfoMethod
		  (
			Object InObject, /* the info structure */
			String MethodName, /* must take no arguments */
			int TextID /* ID of TextView to be set to value */
		  )
		  {
			super(InObject, TextID);
			try
			  {
				this.ObjMethod = InObject.getClass().getDeclaredMethod(MethodName);
			  }
			catch (NoSuchMethodException err)
			  {
				throw new RuntimeException(err.toString());
			  } /*try*/
			Init(this.ObjMethod.getReturnType());
		  } /*InfoMethod*/

		public Object GetValue()
		  {
			try
			  {
				return
					ObjMethod.invoke(InObject);
			  }
			catch (IllegalAccessException err)
			  {
				throw new RuntimeException(err.toString());
			  }
			catch (InvocationTargetException err)
			  {
				throw new RuntimeException(err.toString());
			  } /*try*/
		  } /*GetValue*/

	  } /*InfoMethod*/;

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
		for
		  (
			InfoMember Member :
				new InfoMember[]
					{
						new InfoMethod(display, "getWidth", R.id.width_pixels),
						new InfoMethod(display, "getHeight", R.id.height_pixels),
						new InfoField(metrics, "densityDpi", R.id.screen_dpi),
						new InfoField(metrics, "xdpi", R.id.actual_xdpi),
						new InfoField(metrics, "ydpi", R.id.actual_ydpi),
						new InfoField(metrics, "density", R.id.logical_density),
						new InfoField(metrics, "scaledDensity", R.id.font_scale_density),
					}
		  )
		  {
			Member.ShowValue();
		  } /*for*/
    }

    /**
     * Calculate and display the physical diagonal size of the screen.
     * The size is calculated in inches, rounded to one place after decimal (e.g. '3.7', '10.1')
     * The size is also calculated in millimeters
     * 
     * @param metrics
     */
	private void showScreenDiagonalSize(DisplayMetrics metrics) {
		
		// Guard against divide-by-zero, possible with lazy device manufacturers who set these fields incorrectly
		// Set the density to our best guess.
		final float xdpi = metrics.xdpi < 1.0f ? metrics.densityDpi : metrics.xdpi;
		final float ydpi = metrics.ydpi < 1.0f ? metrics.densityDpi : metrics.ydpi;
		
		// Calculate physical screen width/height
		final float xyPhysicalWidth = (float)metrics.widthPixels / xdpi;
		final float xyPhysicalHeight = (float)metrics.heightPixels / ydpi;
		final float screenPhysicalWidth = (float)metrics.widthPixels / metrics.densityDpi;
		final float screenPhysicalHeight = (float)metrics.heightPixels / metrics.densityDpi;
		
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

	static final CodeName[] LongWideCodes = new CodeName[]
		{
			new CodeName(Configuration.SCREENLAYOUT_LONG_YES, R.string.yes),
			new CodeName(Configuration.SCREENLAYOUT_LONG_NO, R.string.no),
			new CodeName(Configuration.SCREENLAYOUT_LONG_UNDEFINED, R.string.undefined),
		};
	
	/**
	 * Display whether or not the device has a display that is longer or wider than normal.
	 */
	private void showScreenLongWide(Configuration config) {
        ((TextView)findViewById(R.id.long_wide)).setText
		  (
			GetCodeName(config.screenLayout & Configuration.SCREENLAYOUT_LONG_MASK, LongWideCodes)
		  );
	}

	/**
	 * Display the "natural" screen orientation of the device.
	 */
	private void showDefaultOrientation(Configuration config) {
		// Screen default orientation
        setOrientationText((TextView)findViewById(R.id.natural_orientation), config.orientation);
	}

	static final CodeName[] RotationAngles = new CodeName[]
		{
			new CodeName(Surface.ROTATION_0, R.string.degrees_0),
			new CodeName(Surface.ROTATION_90, R.string.degrees_90),
			new CodeName(Surface.ROTATION_180, R.string.degrees_180),
			new CodeName(Surface.ROTATION_270, R.string.degrees_270),
		};

	/**
	 * Display the current screen orientation of the device, with respect to natural orientation.
	 */
	private void showCurrentOrientation(Display display) {
        TextView orientationText = ((TextView)findViewById(R.id.current_orientation));

		int rotation = -1;
		// First, try the Display#getRotation() call, which was introduced in Froyo.
		// Reference: http://android-developers.blogspot.com/2010/09/one-screen-turn-deserves-another.html
		try {
			Method getRotationMethod = display.getClass().getMethod("getRotation");
			rotation = (Integer)getRotationMethod.invoke(display);
		}
		catch (SecurityException ignore) {}
		catch (NoSuchMethodException ignore) {} 
		catch (IllegalArgumentException ignore) {}
		catch (IllegalAccessException ignore) {}
		catch (InvocationTargetException ignore) {}
		if (rotation >= 0)
		  {
			orientationText.setText
			  (
				GetCodeName(rotation, RotationAngles)
			  );
		  }
		else
		  {
			// Fall back on the deprecated Display#getOrientation method from earlier releases of Android.
			setOrientationText(orientationText, display.getOrientation());
		  } /*if*/
	}

	static final CodeName[] OrientationCodes = new CodeName[]
		{
			new CodeName(Configuration.ORIENTATION_LANDSCAPE, R.string.orientation_landscape),
			new CodeName(Configuration.ORIENTATION_PORTRAIT, R.string.orientation_portrait),
			new CodeName(Configuration.ORIENTATION_SQUARE, R.string.orientation_square),
			new CodeName(Configuration.ORIENTATION_UNDEFINED, R.string.undefined),
		};

	/**
	 * Helper sets an orientation string in the given text widget.
	 * 
	 * @param orientationText
	 * @param orientation
	 */
	private void setOrientationText(TextView orientationText, int orientation) {
		orientationText.setText(GetCodeName(orientation, OrientationCodes));
	}

	static final CodeName[] TouchScreenCodes = new CodeName[]
		{
			new CodeName(Configuration.TOUCHSCREEN_FINGER, R.string.touchscreen_finger),
			new CodeName(Configuration.TOUCHSCREEN_STYLUS, R.string.touchscreen_stylus),
			new CodeName(Configuration.TOUCHSCREEN_NOTOUCH, R.string.touchscreen_none),
			new CodeName(Configuration.TOUCHSCREEN_UNDEFINED, R.string.undefined),
		};
	
	private void showTouchScreen(Configuration config) {
		((TextView)findViewById(R.id.touchscreen)).setText
		  (
			GetCodeName(config.touchscreen, TouchScreenCodes)
		  );
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
