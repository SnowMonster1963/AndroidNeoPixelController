package com.thomassnow.xmastree.ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import com.thomassnow.xmastree.R;
import com.thomassnow.xmastree.data.ControllerNode.NodeActivityListener;
import com.thomassnow.xmastree.data.LightProgram;
import com.thomassnow.xmastree.data.LightPrograms;
import com.thomassnow.xmastree.data.MQTTInternals;
import com.thomassnow.xmastree.data.MQTTSettings;
import com.thomassnow.xmastree.data.RGB;
import com.thomassnow.xmastree.network.MQTTHandler;
import com.thomassnow.xmastree.network.MQTTHandler.MQTTPublish;
import com.thomassnow.xmastree.service.MQTTService;
import com.thomassnow.xmastree.ui.views.ColorView;
import com.thomassnow.xmastree.data.ControllerNode;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
	{
		private static final String programFile = "programs.dat";
		
		MQTTService mService;
		boolean mBound = false;
		private MQTTServiceConnection mConnection;
		private MQTTSettings settings;
		private MQTTService.MQTTStartedListener mqttlistener;
		private PlaceholderFragment phf;
		private ArrayAdapter<ControllerNode> nodes_inlb;
		private ArrayList<ControllerNode> nodes;
		private ControllerNode active_node;
		private RGB rgbarry[];
		private int cvidx;
		private ArrayAdapter<LightProgram> programs_inlb;
		private int progidx;
		private int temp;
		
		private enum UpdateMode
		{
			updateWidget,
			updateController,
			updateBoth
		}
		
		private static final int[] colorControlList =
					{
					        R.id.seekBlue,
					        R.id.seekBlueMinus,
					        R.id.seekBluePlus,
					        R.id.seekGreen,
					        R.id.seekGreenMinus,
					        R.id.seekGreenPlus,
					        R.id.seekRed,
					        R.id.seekRedMinus,
					        R.id.seekRedPlus,
					        R.id.textViewBlue,
					        R.id.textViewGreen,
					        R.id.textViewRed,
					};
		
		private static final int[] enableList =
					{
					        R.id.color0,
					        R.id.color1,
					        R.id.color10,
					        R.id.color11,
					        R.id.color12,
					        R.id.color13,
					        R.id.color14,
					        R.id.color15,
					        R.id.color2,
					        R.id.color3,
					        R.id.color4,
					        R.id.color5,
					        R.id.color6,
					        R.id.color7,
					        R.id.color8,
					        R.id.color9,
					        R.id.radioCMode0,
					        R.id.radioCMode1,
					        R.id.radioCMode2,
					        R.id.radioEffect0,
					        R.id.radioEffect1,
					        R.id.radioEffect2,
					        R.id.radioEffect3,
					        R.id.radioEffect4,
					        R.id.radioEffect5,
					        R.id.seekAttack,
					        R.id.seekAttackMinus,
					        R.id.seekAttackPlus,
					        R.id.seekBlue,
					        R.id.seekBlueMinus,
					        R.id.seekBluePlus,
					        R.id.seekDecay,
					        R.id.seekDecayMinus,
					        R.id.seekDecayPlus,
					        R.id.seekDensity,
					        R.id.seekDensityMinus,
					        R.id.seekDensityPlus,
					        R.id.seekGreen,
					        R.id.seekGreenMinus,
					        R.id.seekGreenPlus,
					        R.id.seekRed,
					        R.id.seekRedMinus,
					        R.id.seekRedPlus,
					        R.id.seekSustain,
					        R.id.seekSustainMinus,
					        R.id.seekSustainPlus,
					        R.id.textColorModeLabel,
					        R.id.textEffectLabel,
					        R.id.textViewAttack,
					        R.id.textViewBlue,
					        R.id.textViewDecay,
					        R.id.textViewDensity,
					        R.id.textViewGreen,
					        R.id.textViewRed,
					        R.id.textViewSustain,
					        R.id.textPrograms,
					        R.id.listPrograms,
					        R.id.buttonNew,
					        R.id.buttonUpdate,
					        R.id.buttonDelete,
					};

		class MQTTServiceConnection implements ServiceConnection
			{
				private Activity activity;

				public MQTTServiceConnection(Activity a)
					{
						activity = a;
					}

				@Override
				public void onServiceConnected(ComponentName name, IBinder service)
					{
						MQTTService.LocalBinder binder = (MQTTService.LocalBinder) service;
						mService = binder.getService();
						// mService.startMqtt(settings);
						mBound = true;
						doRestart();
					}

				@Override
				public void onServiceDisconnected(ComponentName name)
					{
						mBound = false;
						mService.stopMqtt();
					}

			}
		
		private void loadPrograms()
			{
				FileInputStream fis;
				LightPrograms programs = new LightPrograms();
				
				programs_inlb.clear();
				
				try
					{
						fis = openFileInput(programFile);
						ObjectInputStream ois = new ObjectInputStream(fis);
						programs = (LightPrograms) ois.readObject();
						for(int i=0;i<programs.size();i++)
							programs_inlb.add(programs.get(i));
						fis.close();
					}
				catch(Exception e)
					{
						programs = new LightPrograms();
					}
			}
		
		private void savePrograms()
			{
				FileOutputStream fos;
				LightPrograms programs = new LightPrograms();
				for(int i=0;i<programs_inlb.getCount();i++)
					programs.add(programs_inlb.getItem(i));
				
				try
					{
						fos = openFileOutput(programFile,Context.MODE_PRIVATE);
						ObjectOutputStream oos = new ObjectOutputStream(fos);
						oos.writeObject(programs);
						fos.close();
					}
				catch(Exception e)
					{
						Log.e("File Save",e.getMessage());
					}
			}
		
		private void editProgram(LightProgram prog)
			{
				String s = "Program " + Integer.toString(temp);
				temp++;
				prog.setName(s);
				prog.setDescription("Description of " + s);
			}

		private void addProgram()
			{
				if(active_node == null)
					return;
				
				LightProgram lp = new LightProgram();
				editProgram(lp);
				lp.copyNode(active_node);
				
				programs_inlb.add(lp);
				progidx = programs_inlb.getCount() - 1;
				phf.getPrograms_list().setSelection(progidx);
				savePrograms();
			}

		private void removeProgram()
			{
				if(progidx >= 0)
					{
						LightProgram lp = programs_inlb.getItem(progidx);
						programs_inlb.remove(lp);
						progidx = -1;
						savePrograms();
					}
			}
		
		private void updateProgram()
			{
				if(progidx >= 0 && active_node != null)
					{
						LightProgram lp = programs_inlb.getItem(progidx);
						editProgram(lp);
						lp.copyNode(active_node);
						savePrograms();
					}
			}

		@Override
		protected void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_main);
				if (savedInstanceState == null)
					{
						phf = new PlaceholderFragment();
						getFragmentManager().beginTransaction().add(R.id.container, phf).commit();
					}
				settings = new MQTTSettings(this);
				nodes_inlb = new ArrayAdapter<ControllerNode>(this, android.R.layout.simple_list_item_1);
				programs_inlb = new ArrayAdapter<LightProgram>(this,android.R.layout.simple_list_item_1);
				nodes = new ArrayList<ControllerNode>();
				cvidx = -1;
				temp = 1;

			}

		private void doBind()
			{
				// Bind to LocalService
				Intent intent = new Intent(this, MQTTService.class);
				mConnection = new MQTTServiceConnection(this);
				bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			}

		private void enableControls()
			{
				for(int i=0;i < enableList.length;i++)
					phf.getView().findViewById(enableList[i]).setEnabled(true);
			}

		private void disableControls()
			{
				for(int i=0;i < enableList.length;i++)
					phf.getView().findViewById(enableList[i]).setEnabled(false);
			}
		
		private void setEffect(int effect,UpdateMode umode)
			{
				RadioButton rbv;
				switch (effect)
					{
						case 0:
							rbv = (RadioButton) phf.getView().findViewById(R.id.radioEffect0);
							break;
						case 1:
							rbv = (RadioButton) phf.getView().findViewById(R.id.radioEffect1);
							break;
						case 2:
							rbv = (RadioButton) phf.getView().findViewById(R.id.radioEffect2);
							break;
						case 3:
							rbv = (RadioButton) phf.getView().findViewById(R.id.radioEffect3);
							break;
						case 4:
							rbv = (RadioButton) phf.getView().findViewById(R.id.radioEffect4);
							break;
						case 5:
							rbv = (RadioButton) phf.getView().findViewById(R.id.radioEffect5);
							break;
						default:
							rbv = (RadioButton) phf.getView().findViewById(R.id.radioEffect0);
							break;
					}
				
				switch(umode)
				{
					case updateWidget:
						rbv.setChecked(true);
						break;
					case updateController:
						if(active_node != null)
							active_node.setEffect(effect);
						break;
					case updateBoth:
						rbv.setChecked(true);
						if(active_node != null)
							active_node.setEffect(effect);
						break;
				}
				
				
			}
		
		private void setColorMode(int cm)
			{
				RadioButton rbv;
				switch (cm)
				{
					case 0:
						rbv = (RadioButton) phf.getView().findViewById(R.id.radioCMode0);
						break;
					case 1:
						rbv = (RadioButton) phf.getView().findViewById(R.id.radioCMode1);
						break;
					case 2:
						rbv = (RadioButton) phf.getView().findViewById(R.id.radioCMode2);
						break;
					default:
						rbv = (RadioButton) phf.getView().findViewById(R.id.radioCMode0);
						break;
				}
			rbv.setChecked(true);
			}
		
		private void setAttack(int Attack,UpdateMode umode)
			{
				switch(umode)
				{
					case updateWidget:
						phf.getSeekAttack().setProgress(Attack);
						break;
					case updateController:
						if(active_node != null)
							active_node.setAttack(Attack);
						break;
					case updateBoth:
						phf.getSeekAttack().setProgress(Attack);
						if(active_node != null)
							active_node.setAttack(Attack);
						break;
				}
			}
		
		private void setSustain(int Sustain,UpdateMode umode)
			{
				switch(umode)
				{
					case updateWidget:
						phf.getSeekSustain().setProgress(Sustain);
						break;
					case updateController:
						if(active_node != null)
							active_node.setSustain(Sustain);
						break;
					case updateBoth:
						phf.getSeekSustain().setProgress(Sustain);
						if(active_node != null)
							active_node.setSustain(Sustain);
						break;
				}
			}
		
		private void setDecay(int Decay,UpdateMode umode)
			{
				switch(umode)
				{
					case updateWidget:
						phf.getSeekDecay().setProgress(Decay);
						break;
					case updateController:
						if(active_node != null)
							active_node.setDecay(Decay);
						break;
					case updateBoth:
						phf.getSeekDecay().setProgress(Decay);
						if(active_node != null)
							active_node.setDecay(Decay);
						break;
				}
			}
		
		private void setDensity(int Density,UpdateMode umode)
			{
				switch(umode)
				{
					case updateWidget:
						phf.getSeekDensity().setProgress(Density);
						break;
					case updateController:
						if(active_node != null)
							active_node.setDensity(Density);
						break;
					case updateBoth:
						phf.getSeekDensity().setProgress(Density);
						if(active_node != null)
							active_node.setDensity(Density);
						break;
				}
			}
		
		private void setColors(RGB[] colors)
			{
				for (int i = 0; i < phf.getColorCount(); i++)
					{
						ColorView cv = phf.getColorView(i);
						if(i < colors.length)
							{
							cv.setColor(colors[i].getColor());
							cv.setSelected(cvidx == i);
							}
						else
							{
								cv.setColor(0);
								cv.setSelected(false);
								if(cvidx == i)
									cvidx = -1;
							}
					}
				if (cvidx >= 0)
					{
						ColorView cv = phf.getColorView(cvidx);
						RGB c = new RGB(cv.getColor());
						boolean b = true;
						
						if(active_node != null)
							{	// prevent slider updates from updating device
							b = active_node.isPayAttention();
							active_node.setPayAttention(false);
							}
						phf.getSeekRed().setProgress(c.getRed());
						phf.getSeekGreen().setProgress(c.getGreen());
						phf.getSeekBlue().setProgress(c.getBlue());
						if(active_node != null)
							{	// prevent slider updates from updating device
							active_node.setPayAttention(b);
							}
						enableColorControls(true);
					}
				else
					enableColorControls(false);
				if (active_node != null)
					active_node.setColors(colors);
			}

		private void updateControls(ControllerNode cn)
			{
				if (cn != null && active_node != null && cn.getName().compareTo(active_node.getName()) == 0)
					{
						cn.setPayAttention(false);
						enableControls();
						int effect = cn.getEffect();
						setEffect(effect,UpdateMode.updateWidget);

						effect = cn.getColorMode();
						setColorMode(effect);
						
						setAttack(cn.getAttack(),UpdateMode.updateWidget);
						setSustain(cn.getSustain(),UpdateMode.updateWidget);
						setDecay(cn.getDecay(),UpdateMode.updateWidget);
						setDensity(cn.getDensity(),UpdateMode.updateWidget);

						setColors(cn.getColors());
						cn.setPayAttention(true);
					}
				else
					{
						if(active_node == null)
							disableControls();
					}
			}
		
		private void enableColorControls(boolean enable)
			{
				for(int i = 0;i< colorControlList.length;i++)
					phf.getView().findViewById(colorControlList[i]).setEnabled(enable);
			}
		
		private void setProgram(LightProgram lp)
			{
				cvidx = -1;

				setAttack(lp.getAttack(),UpdateMode.updateBoth);
				setSustain(lp.getSustain(),UpdateMode.updateBoth);
				setDecay(lp.getDecay(),UpdateMode.updateBoth);
				setDensity(lp.getDensity(),UpdateMode.updateBoth);
				setEffect(lp.getEffect());
				setColors(lp.getColors());
			}

		@Override
		protected void onStart()
			{
				super.onStart();
				loadPrograms();
				phf = (PlaceholderFragment) getFragmentManager().findFragmentById(R.id.container);
				phf.getController_list().setAdapter(nodes_inlb);
				phf.getController_list().setOnItemClickListener(new AdapterView.OnItemClickListener()
					{

						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id)
							{
								view.setSelected(true);
								ControllerNode cn = (ControllerNode) parent.getItemAtPosition(position);
								cvidx = -1;
								active_node = cn;
								updateControls(cn);
							}
					});
				
				phf.getPrograms_list().setAdapter(programs_inlb);
				phf.getPrograms_list().setOnItemClickListener(new AdapterView.OnItemClickListener()
					{


						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id)
							{
								view.setSelected(true);
								LightProgram lp = (LightProgram) parent.getItemAtPosition(position);
								progidx = position;
								setProgram(lp);
							}
					});
				
				phf.getButtonNew().setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v)
						{
							addProgram();
						}});
				
				phf.getButtonUpdate().setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v)
						{
							updateProgram();
						}});
				
				phf.getButtonDelete().setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v)
						{
							removeProgram();
						}});
				
				/*
				phf.getController_list().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
					{

						@Override
						public void onNothingSelected(AdapterView<?> parent)
							{
							}

						@Override
						public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
							{
								ControllerNode cn = (ControllerNode) parent.getSelectedItem();
								int x = cn.getEffect();
								RadioButton rbv;
								switch (x)
									{
										case 0:
											rbv = (RadioButton) phf.getView().findViewById(R.id.radioEffect0);
											break;
										case 1:
											rbv = (RadioButton) phf.getView().findViewById(R.id.radioEffect1);
											break;
										case 2:
											rbv = (RadioButton) phf.getView().findViewById(R.id.radioEffect2);
											break;
										case 3:
											rbv = (RadioButton) phf.getView().findViewById(R.id.radioEffect3);
											break;
										case 4:
											rbv = (RadioButton) phf.getView().findViewById(R.id.radioEffect4);
											break;
										case 5:
											rbv = (RadioButton) phf.getView().findViewById(R.id.radioEffect5);
											break;
										default:
											rbv = (RadioButton) phf.getView().findViewById(R.id.radioEffect0);
											break;
									}
								rbv.setChecked(true);
							}
					});
				*/
				phf.getTextViewAttack().setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v)
						{
							if(phf.getSeekAttack().getProgress() > 0)
								{
									setAttack(0);
								}
							else
								{
									setAttack(255);
								}
						}});
				
				phf.getSeekAttack().setOnSeekBarChangeListener(new OnSeekBarChangeListener()
					{

						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
							{
								String msg = "Attack set to " + Integer.toString(progress);
								Log.d("Setting Attack", msg);
								if (active_node != null)
									active_node.setAttack(progress);
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

							}
					});

				phf.getSeekAttackMinus().setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
							{
								int idx = phf.getSeekAttack().getProgress();
								if (idx > 0)
									{
										phf.getSeekAttack().setProgress(idx - 1);
									}
							}
					});

				phf.getSeekAttackPlus().setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
							{
								int idx = phf.getSeekAttack().getProgress();
								if (idx < 255)
									{
										phf.getSeekAttack().setProgress(idx + 1);
									}
							}
					});

				phf.getTextViewSustain().setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v)
						{
							if(phf.getSeekSustain().getProgress() > 0)
								{
									phf.getSeekSustain().setProgress(0);
								}
							else
								{
									phf.getSeekSustain().setProgress(255);
								}
						}});
				
				phf.getSeekSustain().setOnSeekBarChangeListener(new OnSeekBarChangeListener()
					{

						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
							{
								String msg = "Sustain set to " + Integer.toString(progress);
								Log.d("Setting Sustain", msg);
								if (active_node != null)
									active_node.setSustain(progress);
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

							}
					});

				phf.getSeekSustainMinus().setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
							{
								int idx = phf.getSeekSustain().getProgress();
								if (idx > 0)
									{
										phf.getSeekSustain().setProgress(idx - 1);
									}
							}
					});

				phf.getSeekSustainPlus().setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
							{
								int idx = phf.getSeekSustain().getProgress();
								if (idx < 255)
									{
										phf.getSeekSustain().setProgress(idx + 1);
									}
							}
					});

				phf.getTextViewDecay().setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v)
						{
							if(phf.getSeekDecay().getProgress() > 0)
								{
									phf.getSeekDecay().setProgress(0);
								}
							else
								{
									phf.getSeekDecay().setProgress(255);
								}
						}});
				
				phf.getSeekDecay().setOnSeekBarChangeListener(new OnSeekBarChangeListener()
					{

						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
							{
								String msg = "Decay set to " + Integer.toString(progress);
								Log.d("Setting Decay", msg);
								if (active_node != null)
									active_node.setDecay(progress);
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

							}
					});

				phf.getSeekDecayMinus().setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
							{
								int idx = phf.getSeekDecay().getProgress();
								if (idx > 0)
									{
										phf.getSeekDecay().setProgress(idx - 1);
									}
							}
					});

				phf.getSeekDecayPlus().setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
							{
								int idx = phf.getSeekDecay().getProgress();
								if (idx < 255)
									{
										phf.getSeekDecay().setProgress(idx + 1);
									}
							}
					});

				phf.getTextViewDensity().setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v)
						{
							if(phf.getSeekDensity().getProgress() > 0)
								{
									phf.getSeekDensity().setProgress(0);
								}
							else
								{
									phf.getSeekDensity().setProgress(99);
								}
						}});
				
				phf.getSeekDensity().setOnSeekBarChangeListener(new OnSeekBarChangeListener()
					{

						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
							{
								if (active_node != null)
									active_node.setDensity(progress);
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

							}
					});

				phf.getSeekDensityMinus().setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
							{
								int idx = phf.getSeekDensity().getProgress();
								if (idx > 0)
									{
										phf.getSeekDensity().setProgress(idx - 1);
									}
							}
					});

				phf.getSeekDensityPlus().setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
							{
								int idx = phf.getSeekDensity().getProgress();
								if (idx < 99)
									{
										phf.getSeekDensity().setProgress(idx + 1);
									}
							}
					});

				phf.getTextViewRed().setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v)
						{
							if(phf.getSeekRed().getProgress() > 0)
								{
									phf.getSeekRed().setProgress(0);
								}
							else
								{
									phf.getSeekRed().setProgress(255);
								}
						}});
				
				phf.getSeekRed().setOnSeekBarChangeListener(new OnSeekBarChangeListener()
					{

						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
							{
								updateColors();
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

							}
					});

				phf.getSeekRedMinus().setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
							{
								int idx = phf.getSeekRed().getProgress();
								if (idx > 0)
									{
										phf.getSeekRed().setProgress(idx - 1);
									}
							}
					});

				phf.getSeekRedPlus().setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
							{
								int idx = phf.getSeekRed().getProgress();
								if (idx < 255)
									{
										phf.getSeekRed().setProgress(idx + 1);
									}
							}
					});

				phf.getTextViewGreen().setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v)
						{
							if(phf.getSeekGreen().getProgress() > 0)
								{
									phf.getSeekGreen().setProgress(0);
								}
							else
								{
									phf.getSeekGreen().setProgress(255);
								}
						}});
				
				phf.getSeekGreen().setOnSeekBarChangeListener(new OnSeekBarChangeListener()
					{

						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
							{
								updateColors();
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

							}
					});

				phf.getSeekGreenMinus().setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
							{
								int idx = phf.getSeekGreen().getProgress();
								if (idx > 0)
									{
										phf.getSeekGreen().setProgress(idx - 1);
									}
							}
					});

				phf.getSeekGreenPlus().setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
							{
								int idx = phf.getSeekGreen().getProgress();
								if (idx < 255)
									{
										phf.getSeekGreen().setProgress(idx + 1);
									}
							}
					});

				phf.getTextViewBlue().setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v)
						{
							if(phf.getSeekBlue().getProgress() > 0)
								{
									phf.getSeekBlue().setProgress(0);
								}
							else
								{
									phf.getSeekBlue().setProgress(255);
								}
						}});
				
				phf.getSeekBlue().setOnSeekBarChangeListener(new OnSeekBarChangeListener()
					{

						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
							{
								updateColors();
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

							}
					});

				phf.getSeekBlueMinus().setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
							{
								int idx = phf.getSeekBlue().getProgress();
								if (idx > 0)
									{
										phf.getSeekBlue().setProgress(idx - 1);
									}
							}
					});

				phf.getSeekBluePlus().setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
							{
								int idx = phf.getSeekBlue().getProgress();
								if (idx < 255)
									{
										phf.getSeekBlue().setProgress(idx + 1);
									}
							}
					});

				for (int i = 0; i < phf.getColorCount(); i++)
					{
						phf.getColorView(i).setOnClickListener(new View.OnClickListener()
							{

								@Override
								public void onClick(View v)
									{
										ColorView cv = (ColorView) v;
										handleColorViewClick(cv);
									}
							});
					}
				doBind();
			}

		private void updateColors()
			{
				int r, b, g;

				r = phf.getSeekRed().getProgress();
				g = phf.getSeekGreen().getProgress();
				b = phf.getSeekBlue().getProgress();
				RGB c = new RGB(r, g, b, 0xff);
				if (cvidx >= 0)
					{
						handleColorEdit(cvidx, c.getColor());
					}
			}

		private void removeColor(int idx)
			{
				handleColorEdit(idx, 0);
			}

		private void setColorSelection()
			{
				for (int i = 0; i < phf.getColorCount(); i++)
					{
						if (i == cvidx)
							{
								phf.getColorView(i).setSelected(true);
							}
						else
							{
								phf.getColorView(i).setSelected(false);
							}
					}
			}

		private void handleColorViewClick(ColorView cv)
			{
				RGB rgb = new RGB(cv.getColor());
				int idx = phf.getColorIndexByID(cv.getId());
				boolean b = true;
				for (int i = 0; i < phf.getColorCount(); i++)
					{
						if (b)
							{
								if (idx == i)
									i = phf.getColorCount();
								else
									{
										RGB z = new RGB(phf.getColorView(i).getColor());
										if (z.getAlpha() == 0)
											return;
									}
							}
					}

				if (cv.isSelected())
					{
						if (cvidx == 0)
							{
								RGB nc = new RGB(phf.getColorView(1).getColor());
								if (nc.getAlpha() == 0)
									return;
							}
						removeColor(cvidx);
						cvidx = -1;
					}
				else
					{
						cvidx = idx;
						if (rgb.getAlpha() == 0)
							rgb.setColor(0xffffffff); // make it white
						if (this.active_node != null)
							this.active_node.setPayAttention(false);
						phf.getSeekRed().setProgress(rgb.getRed());
						phf.getSeekGreen().setProgress(rgb.getGreen());
						if (this.active_node != null)
							this.active_node.setPayAttention(true);
						phf.getSeekBlue().setProgress(rgb.getBlue());
					}
				setColorSelection();
			}

//		private void launchColorPicker(RGB rgb, int idx)
//			{
//				Intent intent = new Intent(this, ColorPickerActivity.class);
//				intent.putExtra("color", rgb.getColor());
//				intent.putExtra("index", idx);
//				this.startActivityForResult(intent, 2);
//			}

		private String trimNode(String topic)
			{
				int idx = topic.indexOf('/');
				if (idx > 0)
					topic = topic.substring(0, idx);
				return topic;
			}

		private void addNode(String topic)
			{
				String ControllerName = trimNode(topic);
				for (int i = 0; i < nodes.size(); i++)
					{
						if (nodes.get(i).getName().compareTo(ControllerName) == 0)
							return;
					}
				ControllerNode cn = new ControllerNode(ControllerName, mService);
				nodes.add(cn);
				nodes_inlb.add(cn);
				phf.getController_list().setSelection(nodes.size() - 1);
				cn.setNodeActivityListener(new NodeActivityListener(){

					@Override
					public void onNodeActivity(ControllerNode node)
						{
							node.setPayAttention(false);
							updateControls(node);
							node.setPayAttention(true);
						}});
			}

		private void removeNode(String topic)
			{
				topic = trimNode(topic);
				for (int i = 0; i < nodes.size(); i++)
					{
						ControllerNode n = nodes.get(i);
						if (n.getName().compareTo(topic) == 0)
							{
								n.setNodeActivityListener(null);
								active_node = null;
								nodes_inlb.remove(nodes.get(i));
								nodes.remove(i);
								updateControls(null);
								return;
							}
					}
			}

		private void doRestart()
			{
				mqttlistener = new MQTTService.MQTTStartedListener()
					{

						@Override
						public void onMQTTStarted()
							{
								mService.subscribe("+/Active", 1, new MQTTPublish()
									{

										@Override
										public void onPublished(String topic, byte[] payload)
											{
												Log.d("Data Dump", "topic");
												MQTTInternals.dumpData(topic, payload);
												String pl = new String(payload);
												if (topic.compareTo("Android/Active") != 0)
													{
														if (pl.compareTo("Yes") == 0)
															{
																addNode(topic);
															}
														else
															{
																removeNode(topic);

															}
													}

											}
									});
							}
					};
				mService.startMqtt(settings, mqttlistener);
			}

		@Override
		protected void onResume()
			{
				super.onResume();
				// The activity has become visible (it is now "resumed").
				if (mBound && mService.isConnected() == false)
					{
						doRestart();
					}
			}

		@Override
		protected void onPause()
			{
				// // Another activity is taking focus (this activity is about
				// to
				// // be "paused").
				// if (mBound && mService.isConnected())
				// {
				// mService.stopMqtt();
				// mBound = false;
				// // unbindService((ServiceConnection) mService);
				// }
				super.onPause();
			}

		@Override
		protected void onStop()
			{
				super.onStop();
			}

		@Override
		protected void onDestroy()
			{
				super.onDestroy();
				// The activity is about to be destroyed.
			}

		@Override
		public boolean onCreateOptionsMenu(Menu menu)
			{
				// Inflate the menu; this adds items to the action bar if it is
				// present.
				getMenuInflater().inflate(R.menu.main, menu);
				return true;
			}

		private void doTest()
			{
				if (mService.isConnected())
					{
						Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_LONG).show();
						mService.subscribe("+/Active", new MQTTHandler.MQTTPublish()
							{

								@Override
								public void onPublished(String topic, byte[] payload)
									{
										String s = "Topic: ";
										s += topic + "\r\nMessage: ";
										s += new String(payload);

										Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
									}
							});
					}
				else
					{
						Toast.makeText(getApplicationContext(), "Not connected...", Toast.LENGTH_LONG).show();
					}
			}

		@Override
		public boolean onOptionsItemSelected(MenuItem item)
			{
				// Handle action bar item clicks here. The action bar will
				// automatically handle clicks on the Home/Up button, so long
				// as you specify a parent activity in AndroidManifest.xml.
				int id = item.getItemId();
				switch (id)
					{
						case R.id.MQTTBroker:
							Intent intent = new Intent(this, MQTTSetupActivity.class);
							this.startActivityForResult(intent, 1);
							break;
						case R.id.action_settings:
							doTest();
						default:
							break;
					}

				return super.onOptionsItemSelected(item);
			}

		private void handleColorEdit(int idx, int color)
			{
				RGB c = new RGB(color);
				if (c.getAlpha() == 0)
					{ // delete color and move rest down
						for (int i = idx; i < phf.getColorCount() - 1; i++)
							{
								phf.getColorView(i).setColor(phf.getColorView(i + 1).getColor());
							}
						phf.getColorView(phf.getColorCount() - 1).setColor(0);
					}
				else
					{ // make all inactive before, black
						for (int i = 0; i < idx; i++)
							{
								ColorView cv = phf.getColorView(i);
								RGB nc = new RGB(cv.getColor());
								if (nc.getAlpha() == 0)
									{
										cv.setColor(0xff000000); // black
									}
							}
						phf.getColorView(idx).setColor(color);
					}
				rgbarry = new RGB[phf.getColorCount()];
				for (int i = 0; i < phf.getColorCount(); i++)
					rgbarry[i] = new RGB(phf.getColorView(i).getColor());

				if (active_node != null)
					active_node.setColors(rgbarry);
			}

		public void onActivityResult(int request, int result, Intent data)
			{
				switch (request)
					{
						case 1:
							switch (result)
								{
									case RESULT_CANCELED:
										Log.e("Activity Result", "Cancelled");
										break;
									case RESULT_OK:
										Log.e("Activity Result", "OK");
										mService.stopMqtt();
										settings = new MQTTSettings(this); // retrieve
																			// new
																			// creds
										mService.startMqtt(settings, mqttlistener);
										break;
									default:
										break;
								}
							break;
						case 2:
							if (result == RESULT_OK)
								{
									try
										{
											Bundle bun = data.getExtras();
											int idx = bun.getInt("index");
											int color = bun.getInt("color");
											handleColorEdit(idx, color);
										}
									catch (Exception e)
										{
											Log.e("Activity", e.getMessage());
										}
								}
							break;
						default:
							break;
					}
			}

		public void onRadioButtonClicked(View view)
			{
				// Is the button now checked?
				boolean checked = ((RadioButton) view).isChecked();

				// Check which radio button was clicked
				switch (view.getId())
					{
						case R.id.radioEffect0:
							if (checked)
								{
									Log.d("Radio Button", "radioEffect0");
									if (active_node != null)
										active_node.setEffect(0);
								}
							break;
						case R.id.radioEffect1:
							if (checked)
								{
									Log.d("Radio Button", "radioEffect1");
									if (active_node != null)
										active_node.setEffect(1);
								}
							break;
						case R.id.radioEffect2:
							if (checked)
								{
									Log.d("Radio Button", "radioEffect2");
									if (active_node != null)
										active_node.setEffect(2);
								}
							break;
						case R.id.radioEffect3:
							if (checked)
								{
									Log.d("Radio Button", "radioEffect3");
									if (active_node != null)
										active_node.setEffect(3);
								}
							break;
						case R.id.radioEffect4:
							if (checked)
								{
									Log.d("Radio Button", "radioEffect4");
									if (active_node != null)
										active_node.setEffect(4);
								}
							break;
						case R.id.radioEffect5:
							if (checked)
								{
									Log.d("Radio Button", "radioEffect5");
									if (active_node != null)
										active_node.setEffect(5);
								}
							break;
						case R.id.radioCMode0:
							if (checked)
								{
									Log.d("Radio Button", "radioCMode0");
									if (active_node != null)
										active_node.setColorMode(0);
								}
							break;
						case R.id.radioCMode1:
							if (checked)
								{
									Log.d("Radio Button", "radioCMode1");
									if (active_node != null)
										active_node.setColorMode(1);
								}
							break;
						case R.id.radioCMode2:
							if (checked)
								{
									Log.d("Radio Button", "radioCMode2");
									if (active_node != null)
										active_node.setColorMode(2);
								}
							break;
					}
			}

		/**
		 * A placeholder fragment containing a simple view.
		 */
		public static class PlaceholderFragment extends Fragment
			{
				private ListView controller_list;
				private TextView textViewAttack;
				private SeekBar seekAttack;
				private Button seekAttackMinus;
				private Button seekAttackPlus;
				private TextView textViewSustain;
				private SeekBar seekSustain;
				private Button seekSustainMinus;
				private Button seekSustainPlus;
				private TextView textViewDecay;
				private SeekBar seekDecay;
				private Button seekDecayMinus;
				private Button seekDecayPlus;
				private TextView textViewDensity;
				private SeekBar seekDensity;
				private Button seekDensityMinus;
				private Button seekDensityPlus;
				private TextView textViewRed;
				private SeekBar seekRed;
				private Button seekRedMinus;
				private Button seekRedPlus;
				private TextView textViewGreen;
				private SeekBar seekGreen;
				private Button seekGreenMinus;
				private Button seekGreenPlus;
				private TextView textViewBlue;
				private SeekBar seekBlue;
				private Button seekBlueMinus;
				private Button seekBluePlus;
				private ListView programs_list;
				private Button buttonNew;
				private Button buttonUpdate;
				private Button buttonDelete;
				private ColorView[] cvColors;
				private static final int[] colorIDs =
					{ R.id.color0, R.id.color1, R.id.color2, R.id.color3, R.id.color4, R.id.color5, R.id.color6, R.id.color7,
							R.id.color8, R.id.color9, R.id.color10, R.id.color11, R.id.color12, R.id.color13, R.id.color14,
							R.id.color15, };

				public PlaceholderFragment()
					{
						cvColors = new ColorView[colorIDs.length];
					}

				@Override
				public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
					{
						View rootView = inflater.inflate(R.layout.fragment_main, container, false);
						setController_list((ListView) rootView.findViewById(R.id.listControllers));
						setTextViewAttack((TextView) rootView.findViewById(R.id.textViewAttack));
						setSeekAttack((SeekBar) rootView.findViewById(R.id.seekAttack));
						setSeekAttackMinus((Button) rootView.findViewById(R.id.seekAttackMinus));
						setSeekAttackPlus((Button) rootView.findViewById(R.id.seekAttackPlus));
						setTextViewSustain((TextView) rootView.findViewById(R.id.textViewSustain));
						setSeekSustain((SeekBar) rootView.findViewById(R.id.seekSustain));
						setSeekSustainMinus((Button) rootView.findViewById(R.id.seekSustainMinus));
						setSeekSustainPlus((Button) rootView.findViewById(R.id.seekSustainPlus));
						setTextViewDecay((TextView) rootView.findViewById(R.id.textViewDecay));
						setSeekDecay((SeekBar) rootView.findViewById(R.id.seekDecay));
						setSeekDecayMinus((Button) rootView.findViewById(R.id.seekDecayMinus));
						setSeekDecayPlus((Button) rootView.findViewById(R.id.seekDecayPlus));
						setTextViewDensity((TextView) rootView.findViewById(R.id.textViewDensity));
						setSeekDensity((SeekBar) rootView.findViewById(R.id.seekDensity));
						setSeekDensityMinus((Button) rootView.findViewById(R.id.seekDensityMinus));
						setSeekDensityPlus((Button) rootView.findViewById(R.id.seekDensityPlus));
						setTextViewRed((TextView) rootView.findViewById(R.id.textViewRed));
						setSeekRed((SeekBar) rootView.findViewById(R.id.seekRed));
						setSeekRedMinus((Button) rootView.findViewById(R.id.seekRedMinus));
						setSeekRedPlus((Button) rootView.findViewById(R.id.seekRedPlus));
						setTextViewGreen((TextView) rootView.findViewById(R.id.textViewGreen));
						setSeekGreen((SeekBar) rootView.findViewById(R.id.seekGreen));
						setSeekGreenMinus((Button) rootView.findViewById(R.id.seekGreenMinus));
						setSeekGreenPlus((Button) rootView.findViewById(R.id.seekGreenPlus));
						setTextViewBlue((TextView) rootView.findViewById(R.id.textViewBlue));
						setSeekBlue((SeekBar) rootView.findViewById(R.id.seekBlue));
						setSeekBlueMinus((Button) rootView.findViewById(R.id.seekBlueMinus));
						setSeekBluePlus((Button) rootView.findViewById(R.id.seekBluePlus));
						setButtonNew((Button) rootView.findViewById(R.id.buttonNew));
						setButtonUpdate((Button) rootView.findViewById(R.id.buttonUpdate));
						setButtonDelete((Button) rootView.findViewById(R.id.buttonDelete));
						setPrograms_list((ListView) rootView.findViewById(R.id.listPrograms));
						for (int i = 0; i < colorIDs.length; i++)
							{
								setColorView(i, (ColorView) rootView.findViewById(colorIDs[i]));
							}
						return rootView;
					}

				public int getColorIndexByID(int id)
					{
						for (int i = 0; i < colorIDs.length; i++)
							{
								if (colorIDs[i] == id)
									return i;
							}
						return -1;
					}

				public int getColorCount()
					{
						return colorIDs.length;
					}

				public ListView getController_list()
					{
						return controller_list;
					}

				private void setController_list(ListView controller_list)
					{
						this.controller_list = controller_list;
					}

				public ColorView getColorView(int idx)
					{
						return cvColors[idx];
					}

				public void setColorView(int idx, ColorView cv)
					{
						cvColors[idx] = cv;
					}

				public TextView getTextViewAttack()
					{
							return textViewAttack;
					}

				public void setTextViewAttack(TextView textViewAttack)
					{
							this.textViewAttack = textViewAttack;
					}

				public SeekBar getSeekAttack()
					{
						return seekAttack;
					}

				public void setSeekAttack(SeekBar seekAttack)
					{
						this.seekAttack = seekAttack;
					}

				public Button getSeekAttackMinus()
					{
						return seekAttackMinus;
					}

				public void setSeekAttackMinus(Button seekAttackMinus)
					{
						this.seekAttackMinus = seekAttackMinus;
					}

				public Button getSeekAttackPlus()
					{
						return seekAttackPlus;
					}

				public void setSeekAttackPlus(Button seekAttackPlus)
					{
						this.seekAttackPlus = seekAttackPlus;
					}

				public TextView getTextViewSustain()
					{
							return textViewSustain;
					}

				public void setTextViewSustain(TextView textViewSustain)
					{
							this.textViewSustain = textViewSustain;
					}

				public SeekBar getSeekSustain()
					{
						return seekSustain;
					}

				public void setSeekSustain(SeekBar seekSustain)
					{
						this.seekSustain = seekSustain;
					}

				public Button getSeekSustainMinus()
					{
						return seekSustainMinus;
					}

				public void setSeekSustainMinus(Button seekSustainMinus)
					{
						this.seekSustainMinus = seekSustainMinus;
					}

				public Button getSeekSustainPlus()
					{
						return seekSustainPlus;
					}

				public void setSeekSustainPlus(Button seekSustainPlus)
					{
						this.seekSustainPlus = seekSustainPlus;
					}

				public TextView getTextViewDecay()
					{
							return textViewDecay;
					}

				public void setTextViewDecay(TextView textViewDecay)
					{
							this.textViewDecay = textViewDecay;
					}

				public SeekBar getSeekDecay()
					{
						return seekDecay;
					}

				public void setSeekDecay(SeekBar seekDecay)
					{
						this.seekDecay = seekDecay;
					}

				public Button getSeekDecayMinus()
					{
						return seekDecayMinus;
					}

				public void setSeekDecayMinus(Button seekDecayMinus)
					{
						this.seekDecayMinus = seekDecayMinus;
					}

				public Button getSeekDecayPlus()
					{
						return seekDecayPlus;
					}

				public void setSeekDecayPlus(Button seekDecayPlus)
					{
						this.seekDecayPlus = seekDecayPlus;
					}

				public TextView getTextViewDensity()
					{
							return textViewDensity;
					}

				public void setTextViewDensity(TextView textViewDensity)
					{
							this.textViewDensity = textViewDensity;
					}

				public SeekBar getSeekDensity()
					{
						return seekDensity;
					}

				public void setSeekDensity(SeekBar seekDensity)
					{
						this.seekDensity = seekDensity;
					}

				public Button getSeekDensityMinus()
					{
						return seekDensityMinus;
					}

				public void setSeekDensityMinus(Button seekDensityMinus)
					{
						this.seekDensityMinus = seekDensityMinus;
					}

				public Button getSeekDensityPlus()
					{
						return seekDensityPlus;
					}

				public void setSeekDensityPlus(Button seekDensityPlus)
					{
						this.seekDensityPlus = seekDensityPlus;
					}

				public TextView getTextViewRed()
					{
							return textViewRed;
					}

				public void setTextViewRed(TextView textViewRed)
					{
							this.textViewRed = textViewRed;
					}

				public SeekBar getSeekRed()
					{
						return seekRed;
					}

				public void setSeekRed(SeekBar seekRed)
					{
						this.seekRed = seekRed;
					}

				public Button getSeekRedMinus()
					{
						return seekRedMinus;
					}

				public void setSeekRedMinus(Button seekRedMinus)
					{
						this.seekRedMinus = seekRedMinus;
					}

				public Button getSeekRedPlus()
					{
						return seekRedPlus;
					}

				public void setSeekRedPlus(Button seekRedPlus)
					{
						this.seekRedPlus = seekRedPlus;
					}

				public TextView getTextViewGreen()
					{
							return textViewGreen;
					}

				public void setTextViewGreen(TextView textViewGreen)
					{
							this.textViewGreen = textViewGreen;
					}

				public SeekBar getSeekGreen()
					{
						return seekGreen;
					}

				public void setSeekGreen(SeekBar seekGreen)
					{
						this.seekGreen = seekGreen;
					}

				public Button getSeekGreenMinus()
					{
						return seekGreenMinus;
					}

				public void setSeekGreenMinus(Button seekGreenMinus)
					{
						this.seekGreenMinus = seekGreenMinus;
					}

				public Button getSeekGreenPlus()
					{
						return seekGreenPlus;
					}

				public void setSeekGreenPlus(Button seekGreenPlus)
					{
						this.seekGreenPlus = seekGreenPlus;
					}

				public TextView getTextViewBlue()
					{
							return textViewBlue;
					}

				public void setTextViewBlue(TextView textViewBlue)
					{
							this.textViewBlue = textViewBlue;
					}

				public SeekBar getSeekBlue()
					{
						return seekBlue;
					}

				public void setSeekBlue(SeekBar seekBlue)
					{
						this.seekBlue = seekBlue;
					}

				public Button getSeekBlueMinus()
					{
						return seekBlueMinus;
					}

				public void setSeekBlueMinus(Button seekBlueMinus)
					{
						this.seekBlueMinus = seekBlueMinus;
					}

				public Button getSeekBluePlus()
					{
						return seekBluePlus;
					}

				public void setSeekBluePlus(Button seekBluePlus)
					{
						this.seekBluePlus = seekBluePlus;
					}

				public ListView getPrograms_list()
					{
							return programs_list;
					}

				public void setPrograms_list(ListView programs_list)
					{
							this.programs_list = programs_list;
					}

				public Button getButtonNew()
					{
							return buttonNew;
					}

				public void setButtonNew(Button buttonNew)
					{
							this.buttonNew = buttonNew;
					}

				public Button getButtonUpdate()
					{
							return buttonUpdate;
					}

				public void setButtonUpdate(Button buttonUpdate)
					{
							this.buttonUpdate = buttonUpdate;
					}

				public Button getButtonDelete()
					{
							return buttonDelete;
					}

				public void setButtonDelete(Button buttonDelete)
					{
							this.buttonDelete = buttonDelete;
					}

			}

	}
