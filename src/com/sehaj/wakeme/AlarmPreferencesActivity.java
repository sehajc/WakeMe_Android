package com.sehaj.wakeme;

//Activity to set up alarm preferences

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.sehaj.wakeme.AlarmPreference.Key;

public class AlarmPreferencesActivity extends ListActivity {

	TextView okButton;
	TextView cancelButton;
	private Alarm alarm;
	private MediaPlayer mediaPlayer;
	private CountDownTimer alarmToneTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.alarm_preferences);
		
		okButton = (TextView) findViewById(R.id.textView_OK);
		okButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					okButton.setBackgroundColor(getResources().getColor(
							R.color.holo_blue_light));
					break;
				case MotionEvent.ACTION_UP:
					v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
					Model.init(getApplicationContext());
					if (getAlarm().getId() < 1) {
						Model.create(getAlarm());
					} else {
						Model.update(getAlarm());
					}
					callAlarmService();
					Toast.makeText(AlarmPreferencesActivity.this,
							getAlarm().getTimeUntilNextAlarmMessage(),
							Toast.LENGTH_LONG).show();
					finish();
				case MotionEvent.ACTION_MOVE:
				case MotionEvent.ACTION_CANCEL:
					okButton.setBackgroundColor(getResources().getColor(
							android.R.color.transparent));
					break;
				}
				return true;
			}
		});
		
		cancelButton = (TextView) findViewById(R.id.textView_cancel);
		cancelButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					cancelButton.setBackgroundColor(getResources().getColor(
							R.color.holo_blue_light));
					break;
				case MotionEvent.ACTION_UP:
					v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
					finish();
				case MotionEvent.ACTION_MOVE:
				case MotionEvent.ACTION_CANCEL:
					cancelButton.setBackgroundColor(getResources().getColor(
							android.R.color.transparent));
					break;
				}
				return true;
			}
		});
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null && bundle.containsKey("alarm")) {
			setAlarm((Alarm) bundle.getSerializable("alarm"));
		}

	}

	private void callAlarmService() {
		Intent alarmServiceIntent = new Intent(this,
				AlarmServiceBroadcastReciever.class);
		sendBroadcast(alarmServiceIntent, null);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		final AlarmPreferenceListAdapter alarmPreferenceListAdapter = (AlarmPreferenceListAdapter) getListAdapter();
		final AlarmPreference alarmPreference = (AlarmPreference) alarmPreferenceListAdapter
				.getItem(position);

		AlertDialog.Builder alert;
		v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		switch (alarmPreference.getType()) {
		case BOOLEAN:
			CheckedTextView checkedTextView = (CheckedTextView) v;
			boolean checked = !checkedTextView.isChecked();
			((CheckedTextView) v).setChecked(checked);
			switch (alarmPreference.getKey()) {
			case ALARM_ACTIVE:
				alarm.setAlarmActive(checked);
				break;
			case ALARM_VIBRATE:
				alarm.setVibrate(checked);
				if (checked) {
					Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
					vibrator.vibrate(1000);
				}
				break;
			}
			alarmPreference.setValue(checked);
			break;
		case STRING:

			alert = new AlertDialog.Builder(this);

			alert.setTitle(alarmPreference.getTitle());
			// alert.setMessage(message);

			// Set an EditText view to get user input
			final EditText input = new EditText(this);
			
			input.setText(alarmPreference.getValue().toString());

			alert.setView(input);
			alert.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {

							alarmPreference.setValue(input.getText().toString());

							if (alarmPreference.getKey() == Key.ALARM_NAME) {
								alarm.setAlarmName(alarmPreference.getValue().toString());
							}
							if (alarmPreference.getKey() == Key.ALARM_SMS_NO) {
								alarm.setSmsNo(alarmPreference.getValue().toString());
							}
							if (alarmPreference.getKey() == Key.ALARM_SMS_MESSAGE) {
								alarm.setSmsMessage(alarmPreference.getValue().toString());
							}

							alarmPreferenceListAdapter
									.setAlarm(getAlarm());
							alarmPreferenceListAdapter
									.notifyDataSetChanged();
						}
					});
			alert.show();
			break;
		case LIST:
			alert = new AlertDialog.Builder(this);

			alert.setTitle(alarmPreference.getTitle());
			// alert.setMessage(message);

			CharSequence[] items = new CharSequence[alarmPreference
					.getOptions().length];
			for (int i = 0; i < items.length; i++)
				items[i] = alarmPreference.getOptions()[i];

			alert.setItems(items, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (alarmPreference.getKey()) {
					case ALARM_DIFFICULTY:
						Alarm.Difficulty d = Alarm.Difficulty.values()[which];
						alarm.setDifficulty(d);
						break;
					case ALARM_TONE:
						alarm.setAlarmTonePath(alarmPreferenceListAdapter
										.getAlarmTonePaths()[which]);
						if (alarm.getAlarmTonePath() != null) {
							if (mediaPlayer == null){								
								mediaPlayer = new MediaPlayer();
							}else{
							if(mediaPlayer.isPlaying())
								mediaPlayer.stop();
							mediaPlayer.reset();
							}
							try {
//								mediaPlayer.setVolume(1.0f, 1.0f);
								mediaPlayer.setVolume(0.2f, 0.2f);
								mediaPlayer.setDataSource(
										AlarmPreferencesActivity.this,
										Uri.parse(alarm.getAlarmTonePath()));
								mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
								mediaPlayer.setLooping(false);
								mediaPlayer.prepare();
								mediaPlayer.start();

								// Force the mediaPlayer to stop after 3
								// seconds...
								if(alarmToneTimer != null)
									alarmToneTimer.cancel();
								alarmToneTimer = new CountDownTimer(3000, 3000) {
									@Override
									public void onTick(long millisUntilFinished) {

									}

									@Override
									public void onFinish() {
										try {
											if (mediaPlayer.isPlaying())
												mediaPlayer.stop();											
										} catch (Exception e) {

										}
									}
								};
								alarmToneTimer.start();
							} catch (Exception e) {
								try {
									if (mediaPlayer.isPlaying())
									mediaPlayer.stop();
								} catch (Exception e2) {

								}
							}
						}
						break;
					}
					alarmPreferenceListAdapter.setAlarm(getAlarm());
					alarmPreferenceListAdapter.notifyDataSetChanged();
				}

			});

			alert.show();
			break;
		case MULTIPLE_LIST:
			alert = new AlertDialog.Builder(this);

			alert.setTitle(alarmPreference.getTitle());
			// alert.setMessage(message);

			CharSequence[] multiListItems = new CharSequence[alarmPreference
					.getOptions().length];
			for (int i = 0; i < multiListItems.length; i++)
				multiListItems[i] = alarmPreference.getOptions()[i];

			boolean[] checkedItems = new boolean[multiListItems.length];
			for (Alarm.Day day : getAlarm().getDays()) {
				checkedItems[day.ordinal()] = true;
			}
			alert.setMultiChoiceItems(multiListItems, checkedItems,
					new OnMultiChoiceClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which,
								boolean isChecked) {

							Alarm.Day thisDay = Alarm.Day.values()[which];

							if (isChecked) {
								alarm.addDay(thisDay);
							} else {
								alarm.removeDay(thisDay);
							}

						}
					});
			alert.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					alarmPreferenceListAdapter.setAlarm(getAlarm());
					alarmPreferenceListAdapter.notifyDataSetChanged();

				}
			});
			alert.show();
			break;
		case TIME:
			TimePickerDialog timePickerDialog = new TimePickerDialog(this,
					new OnTimeSetListener() {

						@Override
						public void onTimeSet(TimePicker timePicker, int hours,
								int minutes) {
							Calendar newAlarmTime = Calendar.getInstance();
							newAlarmTime.set(Calendar.HOUR_OF_DAY, hours);
							newAlarmTime.set(Calendar.MINUTE, minutes);
							newAlarmTime.set(Calendar.SECOND, 0);
							alarm.setAlarmTime(newAlarmTime);
							alarmPreferenceListAdapter
									.setAlarm(getAlarm());
							alarmPreferenceListAdapter
									.notifyDataSetChanged();
						}
					}, alarm.getAlarmTime().get(Calendar.HOUR_OF_DAY),
					alarm.getAlarmTime().get(Calendar.MINUTE), true);
			timePickerDialog.setTitle(alarmPreference.getTitle());
			timePickerDialog.show();
		default:
			break;
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		Object[] bundle = { getAlarm(), getListAdapter() };
		return bundle;
	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			if (mediaPlayer != null)
				mediaPlayer.release();
		} catch (Exception e) {
		}
	}

	@Override
	protected void onResume() {

		// Restore data in event of case of orientation change
		@SuppressWarnings("deprecation")
		final Object data = getLastNonConfigurationInstance();
		if (data == null) {
			if (getAlarm() == null)
				setAlarm(new Alarm());

			setListAdapter(new AlarmPreferenceListAdapter(this,
					getAlarm()));
		} else {
			Object[] bundle = (Object[]) data;
			setAlarm((Alarm) bundle[0]);
			setListAdapter((AlarmPreferenceListAdapter) bundle[1]);
		}
		super.onResume();
	}

	public Alarm getAlarm() {
		return alarm;
	}

	public void setAlarm(Alarm alarm) {
		this.alarm = alarm;
	}


}
