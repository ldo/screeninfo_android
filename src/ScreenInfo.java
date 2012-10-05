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

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Main activity class.  Displays information to user.
 */
public class ScreenInfo extends Activity {
	
	//////////////////////////////////////////////////////////////////////////
	// Constants
	//////////////////////////////////////////////////////////////////////////
	
	private final static int ABOUT_DIALOG = 1;
	private final static int MENU_ABOUT = Menu.FIRST;
	private final static int MENU_SHARE = Menu.FIRST + 1;
	
	//////////////////////////////////////////////////////////////////////////
	// State
	//////////////////////////////////////////////////////////////////////////

	Dialog mAbout;
	Screen mScreen;
	
	//////////////////////////////////////////////////////////////////////////
	// Activity Lifecycle
	//////////////////////////////////////////////////////////////////////////
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mScreen = new Screen(this);
    }
    
    @Override
	protected void onResume() {
		super.onResume();
		
        showDeviceInfo();
        showScreenMetrics();
    	showScreenDiagonalSize();
        ShowInfoRest();
	}
    
	//////////////////////////////////////////////////////////////////////////
	// Info Display
	//////////////////////////////////////////////////////////////////////////

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
					  }
					else if (TypeName == "float")
					  {
						MemberType = Class.forName("java.lang.Float");
					  }
					else if (TypeName == "double")
					  {
						MemberType = Class.forName("java.lang.Double");
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
			catch (java.lang.reflect.InvocationTargetException err)
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
			catch (java.lang.reflect.InvocationTargetException err)
			  {
				throw new RuntimeException(err.toString());
			  } /*try*/
		  } /*GetValue*/

	  } /*InfoMethod*/;

	/**
     * Show basic information about the device.
     */
    public void showDeviceInfo() {
		for
		  (
			InfoMember Member :
				new InfoMember[]
					{
                        new InfoMethod(mScreen, "deviceModel", R.id.device_name),
                        new InfoMethod(mScreen, "androidVersion", R.id.os_version),
						new InfoMethod(mScreen, "GetSizeName", R.id.screen_size_name),
						new InfoMethod(mScreen, "GetDensityName", R.id.screen_dpi_name),
					}
		  )
		  {
			Member.ShowValue();
		  } /*for*/
    }
    
    /**
     * Show the screen metrics (pixel dimensions, density, dpi, etc) for the device.
     */
    public void showScreenMetrics() {
		for
		  (
			InfoMember Member :
				new InfoMember[]
					{
                        new InfoField(mScreen, "widthPx", R.id.width_pixels),
                        new InfoField(mScreen, "heightPx", R.id.height_pixels),
                        new InfoField(mScreen, "widthDp", R.id.width_dp),
                        new InfoField(mScreen, "heightDp", R.id.height_dp),
                        new InfoField(mScreen, "smallestDp", R.id.smallest_dp),
						new InfoField(mScreen, "densityDpi", R.id.screen_dpi),
						new InfoField(mScreen, "xdpi", R.id.actual_xdpi),
						new InfoField(mScreen, "ydpi", R.id.actual_ydpi),
						new InfoField(mScreen, "density", R.id.logical_density),
						new InfoField(mScreen, "scaledDensity", R.id.font_scale_density),
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
	private void showScreenDiagonalSize() {
		for
		  (
			InfoMember Member :
				new InfoMember[]
					{
                        new InfoField(mScreen, "xyDiagonalSizeInches", R.id.xy_diagonal_size_inches),
                        new InfoField(mScreen, "xyDiagonalSizeMillimeters", R.id.xy_diagonal_size_mm),
                        new InfoField(mScreen, "xyWidthSizeInches", R.id.xy_width_size_inches),
                        new InfoField(mScreen, "xyHeightSizeInches", R.id.xy_height_size_inches),
                        new InfoField(mScreen, "xyWidthSizeMillimeters", R.id.xy_width_size_mm),
                        new InfoField(mScreen, "xyHeightSizeMillimeters", R.id.xy_height_size_mm),
                        new InfoField(mScreen, "screenDiagonalSizeInches", R.id.screen_diagonal_size_inches),
                        new InfoField(mScreen, "screenDiagonalSizeMillimeters", R.id.screen_diagonal_size_mm),
                        new InfoField(mScreen, "screenWidthSizeInches", R.id.screen_width_size_inches),
                        new InfoField(mScreen, "screenHeightSizeInches", R.id.screen_height_size_inches),
                        new InfoField(mScreen, "screenWidthSizeMillimeters", R.id.screen_width_size_mm),
                        new InfoField(mScreen, "screenHeightSizeMillimeters", R.id.screen_height_size_mm),
					}
		  )
		  {
			Member.ShowValue();
		  } /*for*/
	}
	
    private void ShowInfoRest()
      {
		for
		  (
			InfoMember Member :
				new InfoMember[]
					{
                        new InfoMethod(mScreen, "screenLayoutText", R.id.long_wide),
                        new InfoMethod(mScreen, "defaultOrientationText", R.id.natural_orientation),
                        new InfoMethod(mScreen, "currentOrientationText", R.id.current_orientation),
                        new InfoMethod(mScreen, "touchScreenText", R.id.touchscreen),
                        new InfoMethod(mScreen, "pixelFormatText", R.id.pixel_format),
                        new InfoField(mScreen, "refreshRate", R.id.refresh_rate),
					}
		  )
		  {
			Member.ShowValue();
		  } /*for*/
      } /*ShowInfoRest*/
	
 	
	//////////////////////////////////////////////////////////////////////////
	// About Dialog
	//////////////////////////////////////////////////////////////////////////
	
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

		switch (id)
		  {
		case ABOUT_DIALOG:
	        mAbout = new Dialog(this);
	        mAbout.setContentView(R.layout.about_dialog);
	        mAbout.setTitle(R.string.about_title);
	        ((TextView) mAbout.findViewById(R.id.about_version)).setText(appVersion());
	        ((Button)mAbout.findViewById(R.id.about_dismiss)).setOnClickListener
			  (
				new View.OnClickListener()
				  {
					public void onClick(View v)
					  {
						mAbout.dismiss();
					  } /*onClick*/
				  } /*View.OnClickListener*/
			  );
		break;
		  } /*switch*/

		return mAbout;
	}
 	
 	//////////////////////////////////////////////////////////////////////////
	// Share
	//////////////////////////////////////////////////////////////////////////

	/**
	 * Share summary report via email or whatever
	 */
	private void share() {
		String summaryString = mScreen.summaryText( this );
		final Intent shareIntent = new Intent( android.content.Intent.ACTION_SEND );
		shareIntent.setType( "text/plain" );
		shareIntent.putExtra( android.content.Intent.EXTRA_SUBJECT, 
				appendVersionToSubject( R.string.share_summary_subject ) );
		shareIntent.putExtra( android.content.Intent.EXTRA_TEXT, summaryString );

		startActivity( Intent.createChooser( shareIntent, getString( R.string.share_title ) ) );
	}

	/**
	 * Append the version number of the app to the Subject: string.
	 * 
	 * @param subjectResId
	 * @return
	 */
	private String appendVersionToSubject( int subjectResId ) {
		StringBuilder subjectLine = new StringBuilder();
		subjectLine.append( getString( subjectResId ) );
		subjectLine.append( " (" );
		subjectLine.append( appVersion() );
		subjectLine.append( ")" );
		return subjectLine.toString();
	}

	//////////////////////////////////////////////////////////////////////////
	// Menu
	//////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add( 0, MENU_ABOUT, 0, R.string.about_menu )
			.setIcon( android.R.drawable.ic_menu_info_details );
		menu.add( 0, MENU_SHARE, 0, R.string.share_menu )
			.setIcon( android.R.drawable.ic_menu_share );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch( item.getItemId() ) {
		case MENU_ABOUT:
			showDialog(ABOUT_DIALOG);
			return true;
		case MENU_SHARE:
			share();
			return true;
		}
		
		return false;
	}
}
