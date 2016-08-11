package com.thomassnow.xmastree.ui;

import android.app.DialogFragment;

public class ProgramDialogFragment extends DialogFragment
	{
		public interface Response
		{
			public void onProgramDialogOk();
			public void onProgramDialogCancel();
		}

		public ProgramDialogFragment()
			{
				// TODO Auto-generated constructor stub
			}

	}
