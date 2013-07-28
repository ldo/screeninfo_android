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

import android.app.Dialog;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * Main activity class.  Displays information to user.
 */
public class ScreenInfo extends InfoActivity {
	
	//////////////////////////////////////////////////////////////////////////
	// Constants
	//////////////////////////////////////////////////////////////////////////
	
	private final static int ABOUT_DIALOG = 1;
	
	//////////////////////////////////////////////////////////////////////////
	// State
	//////////////////////////////////////////////////////////////////////////

	Dialog mAbout;
	Screen mScreen;
	
	//////////////////////////////////////////////////////////////////////////
	// Activity Lifecycle
	//////////////////////////////////////////////////////////////////////////

    java.util.Map<MenuItem, Runnable> OptionsMenu;

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
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
                        new InfoMethod(mScreen, "GetRealWidthPx", R.id.total_width_pixels),
                        new InfoMethod(mScreen, "GetRealHeightPx", R.id.total_height_pixels),
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
		android.content.pm.PackageInfo info;
		try {
			info = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = this.getString(R.string.version) + " " + info.versionName;
		} catch (android.content.pm.PackageManager.NameNotFoundException ignore) {;}

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
	        ((android.widget.TextView)mAbout.findViewById(R.id.about_version))
				.setText(appVersion());
	        ((android.widget.Button)mAbout.findViewById(R.id.about_dismiss))
				.setOnClickListener
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
		String summaryString = mScreen.summaryText();
		final Intent shareIntent = new Intent( Intent.ACTION_SEND );
		shareIntent.setType( "text/plain" );
		shareIntent.putExtra( Intent.EXTRA_SUBJECT, 
				appendVersionToSubject( R.string.share_summary_subject ) );
		shareIntent.putExtra( Intent.EXTRA_TEXT, summaryString );

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

    private interface MenuItemAdder
      {

        public MenuItem AddAction
          (
            Menu TheMenu,
            int ItemString,
            int ItemIcon,
            int ActionBarUsage
          );

      } /*MenuItemAdder*/;

    private static class MenuItemAdder_pre_Honeycomb implements MenuItemAdder
      {

        public MenuItem AddAction
          (
            Menu TheMenu,
            int ItemString,
            int ItemIcon,
            int ActionBarUsage /* ignored */
          )
          {
            final MenuItem Result = TheMenu.add(ItemString);
            if (ItemIcon != 0)
              {
                Result.setIcon(ItemIcon);
              } /*if*/
            return
                Result;
          } /*AddAction*/

      } /*MenuItemAdder_pre_Honeycomb*/;

    private static class MenuItemAdder_post_Gingerbread implements MenuItemAdder
      {

        public MenuItem AddAction
          (
            android.view.Menu TheMenu,
            int ItemString,
            int ItemIcon,
            int ActionBarUsage
          )
          {
            final MenuItem Result = TheMenu.add(ItemString);
            if (ItemIcon != 0)
              {
                Result.setIcon(ItemIcon);
              } /*if*/
            Result.setShowAsAction(ActionBarUsage);
            return
                Result;
          } /*AddAction*/

      } /*MenuItemAdder_post_Gingerbread*/;

    private static MenuItemAdder MenuItemAdd = new MenuItemAdder_post_Gingerbread();
    private static boolean OldAPI = false;

    @Override
    public boolean onCreateOptionsMenu
      (
        Menu TheMenu
      )
      {
        OptionsMenu = new java.util.HashMap<MenuItem, Runnable>();
        for (;;)
          {
            try
              {
                OptionsMenu.put
                  (
                    MenuItemAdd.AddAction
                      (
                        /*TheMenu =*/ TheMenu,
                        /*ItemString =*/ R.string.about_menu,
                        /*ItemIcon =*/ android.R.drawable.ic_menu_info_details,
                        /*ActionBarUsage =*/ MenuItem.SHOW_AS_ACTION_IF_ROOM
                      ),
                    new Runnable()
                      {
                        public void run()
                          {
                            showDialog(ABOUT_DIALOG);
                          } /*run*/
                      } /*Runnable*/
                  );
                break;
              }
            catch (NoSuchMethodError Fail)
              {
                if (OldAPI)
                  {
                    throw new RuntimeException(Fail.toString());
                  } /*if*/
                TheMenu.clear();
                MenuItemAdd = new MenuItemAdder_pre_Honeycomb();
                OldAPI = true;
              } /*try*/
          } /*for*/
        OptionsMenu.put
          (
            MenuItemAdd.AddAction
              (
                /*TheMenu =*/ TheMenu,
                /*ItemString =*/ R.string.share_menu,
                /*ItemIcon =*/ android.R.drawable.ic_menu_share,
                /*ActionBarUsage =*/ MenuItem.SHOW_AS_ACTION_IF_ROOM
              ),
            new Runnable()
              {
                public void run()
                  {
					share();
                  } /*run*/
              } /*Runnable*/
          );
        return
            true;
	  } /*onCreateOptionsMenu*/

    @Override
    public boolean onOptionsItemSelected
      (
        MenuItem TheItem
      )
      {
        boolean Handled = false;
        final Runnable Action = OptionsMenu.get(TheItem);
        if (Action != null)
          {
            Action.run();
            Handled = true;
          } /*if*/
        return
            Handled;
      } /*onOptionsItemSelected*/

} /*ScreenInfo*/;
