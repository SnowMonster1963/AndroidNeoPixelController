package com.thomassnow.xmastree.ui;

import com.thomassnow.xmastree.R;
import com.thomassnow.xmastree.R.id;
import com.thomassnow.xmastree.R.layout;
import com.thomassnow.xmastree.R.menu;
import com.thomassnow.xmastree.data.RGB;
import com.thomassnow.xmastree.ui.views.ColorView;
import com.thomassnow.xmastree.ui.views.VerticalSeekBar;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.os.Build;

public class ColorPickerActivity extends Activity
	{

		@Override
		protected void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_color_picker);
				Intent i = getIntent();
				
				if (savedInstanceState == null)
					{
						getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment(i)).commit();
					}
			}

		@Override
		public boolean onCreateOptionsMenu(Menu menu)
			{
				// Inflate the menu; this adds items to the action bar if it is
				// present.
				getMenuInflater().inflate(R.menu.color_picker, menu);
				return true;
			}

		@Override
		public boolean onOptionsItemSelected(MenuItem item)
			{
				// Handle action bar item clicks here. The action bar will
				// automatically handle clicks on the Home/Up button, so long
				// as you specify a parent activity in AndroidManifest.xml.
				int id = item.getItemId();
				if (id == R.id.action_settings)
					{
						return true;
					}
				return super.onOptionsItemSelected(item);
			}

		/**
		 * A placeholder fragment containing a simple view.
		 */
		public static class PlaceholderFragment extends Fragment
			{
				Intent intent;
				VerticalSeekBar r;
				VerticalSeekBar g;
				VerticalSeekBar b;
				ColorView cv;
				CheckBox active;
				Button ok;
				RGB rgb;

				public PlaceholderFragment(Intent ci)
					{
						intent = ci;
					}
				
				private void handleOK()
					{
						Activity a = getActivity();
						if(active.isChecked())
							intent.putExtra("color", rgb.getColor());
						else
							intent.putExtra("color", 0);
						
						a.setResult(RESULT_OK,intent);
						a.finish();
					}
				
				private void handleActive(boolean isChecked)
					{
						if(isChecked)
							{
								rgb.setAlpha(0xff);
								cv.setColor(rgb.getColor());
							}
						else
							{
							cv.setColor(0);
							r.setProgress(0);
							g.setProgress(0);
							b.setProgress(0);
							rgb.setAlpha(0);
							}
					}

				@Override
				public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
					{
						
						View rootView = inflater.inflate(R.layout.fragment_color_picker, container, false);
						r = (VerticalSeekBar)rootView.findViewById(R.id.seekRed);
						g = (VerticalSeekBar)rootView.findViewById(R.id.seekGreen);
						b = (VerticalSeekBar)rootView.findViewById(R.id.seekBlue);
						active = (CheckBox)rootView.findViewById(R.id.checkActive);
						cv = (ColorView)rootView.findViewById(R.id.colorPicked);
						ok = (Button)rootView.findViewById(R.id.buttonOK);
						Bundle bun = intent.getExtras();
						int color = bun.getInt("color");
						rgb = new RGB(color);
						if(rgb.getAlpha() == 0)
							{
								r.setProgress(0);
								g.setProgress(0);
								b.setProgress(0);
								active.setChecked(false);
							}
						else
							{
								r.setProgress(rgb.getRed());
								g.setProgress(rgb.getGreen());
								b.setProgress(rgb.getBlue());
								active.setChecked(true);
							}
						cv.setColor(color);
						r.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

							@Override
							public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
								{
									rgb.setRed(progress);
									cv.setColor(rgb.getColor());
								}

							@Override
							public void onStartTrackingTouch(SeekBar seekBar)
								{
									// TODO Auto-generated method stub
									
								}

							@Override
							public void onStopTrackingTouch(SeekBar seekBar)
								{
									// TODO Auto-generated method stub
									
								}});
						
						g.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

							@Override
							public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
								{
									rgb.setGreen(progress);
									cv.setColor(rgb.getColor());
								}

							@Override
							public void onStartTrackingTouch(SeekBar seekBar)
								{
									// TODO Auto-generated method stub
									
								}

							@Override
							public void onStopTrackingTouch(SeekBar seekBar)
								{
									// TODO Auto-generated method stub
									
								}});
						
						b.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

							@Override
							public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
								{
									rgb.setBlue(progress);
									cv.setColor(rgb.getColor());
								}

							@Override
							public void onStartTrackingTouch(SeekBar seekBar)
								{
									// TODO Auto-generated method stub
									
								}

							@Override
							public void onStopTrackingTouch(SeekBar seekBar)
								{
									// TODO Auto-generated method stub
									
								}});
						
						ok.setOnClickListener(new OnClickListener(){

							@Override
							public void onClick(View v)
								{
									handleOK();
								}});
						
						active.setOnCheckedChangeListener(new OnCheckedChangeListener(){

							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
								{
									handleActive(isChecked);
								}});
						
						return rootView;
					}
			}
	}
