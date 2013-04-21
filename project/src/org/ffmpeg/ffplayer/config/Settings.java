package org.ffmpeg.ffplayer.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import org.ffmpeg.ffplayer.FFPlayer;
import org.ffmpeg.ffplayer.render.SDLInput;
import org.ffmpeg.ffplayer.render.SDL_Keys;
import org.ffmpeg.ffplayer.config.Settings.AdditionalMouseConfig;
import org.ffmpeg.ffplayer.config.Settings.AudioConfig;
import org.ffmpeg.ffplayer.config.Settings.CalibrateTouchscreenMenu;
import org.ffmpeg.ffplayer.config.Settings.CustomizeScreenKbLayout;
import org.ffmpeg.ffplayer.config.Settings.DisplaySizeConfig;
import org.ffmpeg.ffplayer.config.Settings.DownloadConfig;
import org.ffmpeg.ffplayer.config.Settings.GyroscopeCalibration;
import org.ffmpeg.ffplayer.config.Settings.JoystickMouseConfig;
import org.ffmpeg.ffplayer.config.Settings.KeyEventsListener;
import org.ffmpeg.ffplayer.config.Settings.KeyRemapToolMouseClick;
import org.ffmpeg.ffplayer.config.Settings.KeyboardConfigMainMenu;
import org.ffmpeg.ffplayer.config.Settings.LeftClickConfig;
import org.ffmpeg.ffplayer.config.Settings.MainMenu;
import org.ffmpeg.ffplayer.config.Settings.Menu;
import org.ffmpeg.ffplayer.config.Settings.MouseConfigMainMenu;
import org.ffmpeg.ffplayer.config.Settings.OkButton;
import org.ffmpeg.ffplayer.config.Settings.OptionalDownloadConfig;
import org.ffmpeg.ffplayer.config.Settings.RemapHwKeysConfig;
import org.ffmpeg.ffplayer.config.Settings.RemapScreenKbConfig;
import org.ffmpeg.ffplayer.config.Settings.ResetToDefaultsConfig;
import org.ffmpeg.ffplayer.config.Settings.RightClickConfig;
import org.ffmpeg.ffplayer.config.Settings.ScreenGesturesConfig;
import org.ffmpeg.ffplayer.config.Settings.ScreenKeyboardDrawSizeConfig;
import org.ffmpeg.ffplayer.config.Settings.ScreenKeyboardSizeConfig;
import org.ffmpeg.ffplayer.config.Settings.ScreenKeyboardThemeConfig;
import org.ffmpeg.ffplayer.config.Settings.ScreenKeyboardTransparencyConfig;
import org.ffmpeg.ffplayer.config.Settings.SdcardAppPath;
import org.ffmpeg.ffplayer.config.Settings.TouchEventsListener;
import org.ffmpeg.ffplayer.config.Settings.TouchPressureMeasurementTool;
import org.ffmpeg.ffplayer.config.Settings.VideoSettingsConfig;
import org.ffmpeg.ffplayer.config.Settings.CalibrateTouchscreenMenu.ScreenEdgesCalibrationTool;
import org.ffmpeg.ffplayer.config.Settings.CustomizeScreenKbLayout.CustomizeScreenKbLayoutTool;
import org.ffmpeg.ffplayer.config.Settings.RemapHwKeysConfig.KeyRemapTool;
import org.ffmpeg.ffplayer.config.Settings.SdcardAppPath.Dummy;
import org.ffmpeg.ffplayer.config.Settings.SdcardAppPath.Froyo;
import org.ffmpeg.ffplayer.config.Settings.TouchPressureMeasurementTool.TouchMeasurementTool;
import org.ffmpeg.ffplayer.render.SDLInput.Mouse;
import org.ffmpeg.ffplayer.render.SDLInput.SDL_1_2_Keycodes;
import org.ffmpeg.ffplayer.util.AccelerometerReader;
import org.ffmpeg.ffplayer.util.DataDownloader;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.ffmpeg.ffplayer.R;

public class Settings {
	static String SettingsFileName = "libsdl-settings.cfg";

	static boolean settingsLoaded = false;
	public static boolean settingsChanged = false;
	static final int SETTINGS_FILE_VERSION = 5;

	static void Save(final FFPlayer p) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(
					p.openFileOutput(SettingsFileName,
							p.MODE_WORLD_READABLE));
			out.writeInt(SETTINGS_FILE_VERSION);
			out.writeBoolean(Globals.DownloadToSdcard);
			out.writeBoolean(Globals.PhoneHasArrowKeys);
			out.writeBoolean(Globals.PhoneHasTrackball);
			out.writeBoolean(Globals.UseAccelerometerAsArrowKeys);
			out.writeBoolean(Globals.UseTouchscreenKeyboard);
			out.writeInt(Globals.TouchscreenKeyboardSize);
			out.writeInt(Globals.AccelerometerSensitivity);
			out.writeInt(Globals.AccelerometerCenterPos);
			out.writeInt(Globals.TrackballDampening);
			out.writeInt(Globals.AudioBufferConfig);
			out.writeInt(Globals.TouchscreenKeyboardTheme);
			out.writeInt(Globals.RightClickMethod);
			out.writeInt(Globals.ShowScreenUnderFinger);
			out.writeInt(Globals.LeftClickMethod);
			out.writeBoolean(Globals.MoveMouseWithJoystick);
			out.writeBoolean(Globals.ClickMouseWithDpad);
			out.writeInt(Globals.ClickScreenPressure);
			out.writeInt(Globals.ClickScreenTouchspotSize);
			out.writeBoolean(Globals.KeepAspectRatio);
			out.writeInt(Globals.MoveMouseWithJoystickSpeed);
			out.writeInt(Globals.MoveMouseWithJoystickAccel);
			out.writeInt(SDL_Keys.JAVA_KEYCODE_LAST);
			for (int i = 0; i < SDL_Keys.JAVA_KEYCODE_LAST; i++) {
				out.writeInt(Globals.RemapHwKeycode[i]);
			}
			out.writeInt(Globals.RemapScreenKbKeycode.length);
			for (int i = 0; i < Globals.RemapScreenKbKeycode.length; i++) {
				out.writeInt(Globals.RemapScreenKbKeycode[i]);
			}
			out.writeInt(Globals.ScreenKbControlsShown.length);
			for (int i = 0; i < Globals.ScreenKbControlsShown.length; i++) {
				out.writeBoolean(Globals.ScreenKbControlsShown[i]);
			}
			out.writeInt(Globals.TouchscreenKeyboardTransparency);
			out.writeInt(Globals.RemapMultitouchGestureKeycode.length);
			for (int i = 0; i < Globals.RemapMultitouchGestureKeycode.length; i++) {
				out.writeInt(Globals.RemapMultitouchGestureKeycode[i]);
				out.writeBoolean(Globals.MultitouchGesturesUsed[i]);
			}
			out.writeInt(Globals.MultitouchGestureSensitivity);
			for (int i = 0; i < Globals.TouchscreenCalibration.length; i++)
				out.writeInt(Globals.TouchscreenCalibration[i]);
			out.writeInt(Globals.DataDir.length());
			for (int i = 0; i < Globals.DataDir.length(); i++)
				out.writeChar(Globals.DataDir.charAt(i));
			out.writeInt(Globals.CommandLine.length());
			for (int i = 0; i < Globals.CommandLine.length(); i++)
				out.writeChar(Globals.CommandLine.charAt(i));
			out.writeInt(Globals.ScreenKbControlsLayout.length);
			for (int i = 0; i < Globals.ScreenKbControlsLayout.length; i++)
				for (int ii = 0; ii < 4; ii++)
					out.writeInt(Globals.ScreenKbControlsLayout[i][ii]);
			out.writeInt(Globals.LeftClickKey);
			out.writeInt(Globals.RightClickKey);
			out.writeBoolean(Globals.VideoLinearFilter);
			out.writeInt(Globals.LeftClickTimeout);
			out.writeInt(Globals.RightClickTimeout);
			out.writeBoolean(Globals.RelativeMouseMovement);
			out.writeInt(Globals.RelativeMouseMovementSpeed);
			out.writeInt(Globals.RelativeMouseMovementAccel);
			out.writeBoolean(Globals.MultiThreadedVideo);

			out.writeInt(Globals.OptionalDataDownload.length);
			for (int i = 0; i < Globals.OptionalDataDownload.length; i++)
				out.writeBoolean(Globals.OptionalDataDownload[i]);
			out.writeBoolean(Globals.BrokenLibCMessageShown);
			out.writeInt(Globals.TouchscreenKeyboardDrawSize);
			out.writeInt(((FFPlayer) p).getApplicationVersion());
			out.writeFloat(Globals.gyro_x1);
			out.writeFloat(Globals.gyro_x2);
			out.writeFloat(Globals.gyro_xc);
			out.writeFloat(Globals.gyro_y1);
			out.writeFloat(Globals.gyro_y2);
			out.writeFloat(Globals.gyro_yc);
			out.writeFloat(Globals.gyro_z1);
			out.writeFloat(Globals.gyro_z2);
			out.writeFloat(Globals.gyro_zc);

			out.close();
			settingsLoaded = true;

		} catch (FileNotFoundException e) {
		} catch (SecurityException e) {
		} catch (IOException e) {
		}
		;
	}

	public static void load(final FFPlayer p) {
		if (settingsLoaded) // Prevent starting twice
		{
			return;
		}
		System.out.println("libSDL: Settings.Load(): enter");
		nativeInitKeymap();
		for (int i = 0; i < SDL_Keys.JAVA_KEYCODE_LAST; i++) {
			int sdlKey = nativeGetKeymapKey(i);
			int idx = 0;
			for (int ii = 0; ii < SDL_Keys.values.length; ii++)
				if (SDL_Keys.values[ii] == sdlKey)
					idx = ii;
			Globals.RemapHwKeycode[i] = idx;
		}
		for (int i = 0; i < Globals.RemapScreenKbKeycode.length; i++) {
			int sdlKey = nativeGetKeymapKeyScreenKb(i);
			int idx = 0;
			for (int ii = 0; ii < SDL_Keys.values.length; ii++)
				if (SDL_Keys.values[ii] == sdlKey)
					idx = ii;
			Globals.RemapScreenKbKeycode[i] = idx;
		}
		Globals.ScreenKbControlsShown[0] = Globals.AppNeedsArrowKeys;
		Globals.ScreenKbControlsShown[1] = Globals.AppNeedsTextInput;
		for (int i = 2; i < Globals.ScreenKbControlsShown.length; i++)
			Globals.ScreenKbControlsShown[i] = (i - 2 < Globals.AppTouchscreenKeyboardKeysAmount);
		for (int i = 0; i < Globals.RemapMultitouchGestureKeycode.length; i++) {
			int sdlKey = nativeGetKeymapKeyMultitouchGesture(i);
			int idx = 0;
			for (int ii = 0; ii < SDL_Keys.values.length; ii++)
				if (SDL_Keys.values[ii] == sdlKey)
					idx = ii;
			Globals.RemapMultitouchGestureKeycode[i] = idx;
		}
		for (int i = 0; i < Globals.MultitouchGesturesUsed.length; i++)
			Globals.MultitouchGesturesUsed[i] = true;

		System.out.println("android.os.Build.MODEL: "
				+ android.os.Build.MODEL);
		if ((android.os.Build.MODEL.equals("GT-N7000") || android.os.Build.MODEL
				.equals("SGH-I717"))
				&& android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
			// Samsung Galaxy Note generates a keypress when you hover a
			// stylus over the screen, and that messes up OpenTTD dialogs
			// ICS update sends events in a proper way
			Globals.RemapHwKeycode[112] = SDLInput.SDL_1_2_Keycodes.SDLK_UNKNOWN;
		}

		try {
			ObjectInputStream settingsFile = new ObjectInputStream(
					new FileInputStream(p.getFilesDir().getAbsolutePath()
							+ "/" + SettingsFileName));
			if (settingsFile.readInt() != SETTINGS_FILE_VERSION)
				throw new IOException();
			Globals.DownloadToSdcard = settingsFile.readBoolean();
			Globals.PhoneHasArrowKeys = settingsFile.readBoolean();
			Globals.PhoneHasTrackball = settingsFile.readBoolean();
			Globals.UseAccelerometerAsArrowKeys = settingsFile
					.readBoolean();
			Globals.UseTouchscreenKeyboard = settingsFile.readBoolean();
			Globals.TouchscreenKeyboardSize = settingsFile.readInt();
			Globals.AccelerometerSensitivity = settingsFile.readInt();
			Globals.AccelerometerCenterPos = settingsFile.readInt();
			Globals.TrackballDampening = settingsFile.readInt();
			Globals.AudioBufferConfig = settingsFile.readInt();
			Globals.TouchscreenKeyboardTheme = settingsFile.readInt();
			Globals.RightClickMethod = settingsFile.readInt();
			Globals.ShowScreenUnderFinger = settingsFile.readInt();
			Globals.LeftClickMethod = settingsFile.readInt();
			Globals.MoveMouseWithJoystick = settingsFile.readBoolean();
			Globals.ClickMouseWithDpad = settingsFile.readBoolean();
			Globals.ClickScreenPressure = settingsFile.readInt();
			Globals.ClickScreenTouchspotSize = settingsFile.readInt();
			Globals.KeepAspectRatio = settingsFile.readBoolean();
			Globals.MoveMouseWithJoystickSpeed = settingsFile.readInt();
			Globals.MoveMouseWithJoystickAccel = settingsFile.readInt();
			int readKeys = settingsFile.readInt();
			for (int i = 0; i < readKeys; i++) {
				Globals.RemapHwKeycode[i] = settingsFile.readInt();
			}
			if (settingsFile.readInt() != Globals.RemapScreenKbKeycode.length)
				throw new IOException();
			for (int i = 0; i < Globals.RemapScreenKbKeycode.length; i++) {
				Globals.RemapScreenKbKeycode[i] = settingsFile.readInt();
			}
			if (settingsFile.readInt() != Globals.ScreenKbControlsShown.length)
				throw new IOException();
			for (int i = 0; i < Globals.ScreenKbControlsShown.length; i++) {
				Globals.ScreenKbControlsShown[i] = settingsFile
						.readBoolean();
			}
			Globals.TouchscreenKeyboardTransparency = settingsFile
					.readInt();
			if (settingsFile.readInt() != Globals.RemapMultitouchGestureKeycode.length)
				throw new IOException();
			for (int i = 0; i < Globals.RemapMultitouchGestureKeycode.length; i++) {
				Globals.RemapMultitouchGestureKeycode[i] = settingsFile
						.readInt();
				Globals.MultitouchGesturesUsed[i] = settingsFile
						.readBoolean();
			}
			Globals.MultitouchGestureSensitivity = settingsFile.readInt();
			for (int i = 0; i < Globals.TouchscreenCalibration.length; i++)
				Globals.TouchscreenCalibration[i] = settingsFile.readInt();
			StringBuilder b = new StringBuilder();
			int len = settingsFile.readInt();
			for (int i = 0; i < len; i++)
				b.append(settingsFile.readChar());
			Globals.DataDir = b.toString();

			b = new StringBuilder();
			len = settingsFile.readInt();
			for (int i = 0; i < len; i++)
				b.append(settingsFile.readChar());
			Globals.CommandLine = b.toString();

			if (settingsFile.readInt() != Globals.ScreenKbControlsLayout.length)
				throw new IOException();
			for (int i = 0; i < Globals.ScreenKbControlsLayout.length; i++)
				for (int ii = 0; ii < 4; ii++)
					Globals.ScreenKbControlsLayout[i][ii] = settingsFile
							.readInt();
			Globals.LeftClickKey = settingsFile.readInt();
			Globals.RightClickKey = settingsFile.readInt();
			Globals.VideoLinearFilter = settingsFile.readBoolean();
			Globals.LeftClickTimeout = settingsFile.readInt();
			Globals.RightClickTimeout = settingsFile.readInt();
			Globals.RelativeMouseMovement = settingsFile.readBoolean();
			Globals.RelativeMouseMovementSpeed = settingsFile.readInt();
			Globals.RelativeMouseMovementAccel = settingsFile.readInt();
			Globals.MultiThreadedVideo = settingsFile.readBoolean();

			Globals.OptionalDataDownload = new boolean[settingsFile
					.readInt()];
			for (int i = 0; i < Globals.OptionalDataDownload.length; i++)
				Globals.OptionalDataDownload[i] = settingsFile
						.readBoolean();
			Globals.BrokenLibCMessageShown = settingsFile.readBoolean();
			Globals.TouchscreenKeyboardDrawSize = settingsFile.readInt();
			int cfgVersion = settingsFile.readInt();
			Globals.gyro_x1 = settingsFile.readFloat();
			Globals.gyro_x2 = settingsFile.readFloat();
			Globals.gyro_xc = settingsFile.readFloat();
			Globals.gyro_y1 = settingsFile.readFloat();
			Globals.gyro_y2 = settingsFile.readFloat();
			Globals.gyro_yc = settingsFile.readFloat();
			Globals.gyro_z1 = settingsFile.readFloat();
			Globals.gyro_z2 = settingsFile.readFloat();
			Globals.gyro_zc = settingsFile.readFloat();

			settingsLoaded = true;

			System.out
					.println("libSDL: Settings.Load(): loaded settings successfully");
			settingsFile.close();

			System.out.println("libSDL: old cfg version " + cfgVersion
					+ ", our version " + p.getApplicationVersion());
			if (cfgVersion != ((FFPlayer) p).getApplicationVersion()) {
				DeleteFilesOnUpgrade();
				if (Globals.ResetSdlConfigForThisVersion) {
					System.out.println("libSDL: old cfg version "
							+ cfgVersion + ", our version "
							+ p.getApplicationVersion()
							+ " and we need to clean up config file");
					// Delete settings file, and restart the application
					DeleteSdlConfigOnUpgradeAndRestart(p);
				}
				Save(p);
			}

			return;

		} catch (FileNotFoundException e) {
		} catch (SecurityException e) {
		} catch (IOException e) {
			DeleteFilesOnUpgrade();
			if (Globals.ResetSdlConfigForThisVersion) {
				System.out
						.println("libSDL: old cfg version unknown or too old, our version "
								+ p.getApplicationVersion()
								+ " and we need to clean up config file");
				DeleteSdlConfigOnUpgradeAndRestart(p);
			}
		}
		;

		if (Globals.DataDir.length() == 0) {
			if (!Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				System.out
						.println("libSDL: SD card or external storage is not mounted (state "
								+ Environment.getExternalStorageState()
								+ "), switching to the internal storage.");
				Globals.DownloadToSdcard = false;
			}
			Globals.DataDir = Globals.DownloadToSdcard ? SdcardAppPath
					.getPath(p) : p.getFilesDir().getAbsolutePath();
			if (Globals.DownloadToSdcard) {
				// Check if data already installed into deprecated location
				// at /sdcard/app-data/<package-name>
				String[] fileList = new File(
						SdcardAppPath.deprecatedPath(p)).list();
				if (fileList != null)
					for (String s : fileList)
						if (s.toUpperCase().startsWith(
								DataDownloader.DOWNLOAD_FLAG_FILENAME
										.toUpperCase()))
							Globals.DataDir = SdcardAppPath
									.deprecatedPath(p);
			}
		}

		System.out
				.println("libSDL: Settings.Load(): loading settings failed, running config dialog");
		((FFPlayer) p).setUpStatusLabel();
		if (checkRamSize(p))
			showConfig(p, true);
	}

	// ===============================================================================================

	public static abstract class Menu {
		// Should be overridden by children
		abstract void run(final FFPlayer p);

		abstract String title(final FFPlayer p);

		boolean enabled() {
			return true;
		}

		// Should not be overridden
		boolean enabledOrHidden() {
			for (Settings.Menu m : Globals.HiddenMenuOptions) {
				if (m.getClass().getName()
						.equals(this.getClass().getName()))
					return false;
			}
			return enabled();
		}

		void showMenuOptionsList(final FFPlayer p, final Settings.Menu[] list) {
			menuStack.add(this);
			ArrayList<CharSequence> items = new ArrayList<CharSequence>();
			for (Settings.Menu m : list) {
				if (m.enabledOrHidden())
					items.add(m.title(p));
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(title(p));
			builder.setItems(items.toArray(new CharSequence[0]),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							dialog.dismiss();
							int selected = 0;

							for (Settings.Menu m : list) {
								if (m.enabledOrHidden()) {
									if (selected == item) {
										m.run(p);
										return;
									}
									selected++;
								}
							}
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBackOuterMenu(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static ArrayList<Settings.Menu> menuStack = new ArrayList<Settings.Menu>();

	public static void showConfig(final FFPlayer p, final boolean firstStart) {
		settingsChanged = true;
		if (Globals.OptionalDataDownload == null) {
			String downloads[] = Globals.DataDownloadUrl;
			Globals.OptionalDataDownload = new boolean[downloads.length];
			boolean oldFormat = true;
			for (int i = 0; i < downloads.length; i++) {
				if (downloads[i].indexOf("!") == 0) {
					Globals.OptionalDataDownload[i] = true;
					oldFormat = false;
				}
			}
			if (oldFormat)
				Globals.OptionalDataDownload[0] = true;
		}

		if (!firstStart)
			new MainMenu().run(p);
		else {
			if (Globals.StartupMenuButtonTimeout > 0) // If we did not
														// disable startup
														// menu altogether
			{
				for (Settings.Menu m : Globals.FirstStartMenuOptions) {
					boolean hidden = false;
					for (Settings.Menu m1 : Globals.HiddenMenuOptions) {
						if (m1.getClass().getName()
								.equals(m.getClass().getName()))
							hidden = true;
					}
					if (!hidden)
						menuStack.add(m);
				}
			}
			goBack(p);
		}
	}

	static void goBack(final FFPlayer p) {
		if (menuStack.isEmpty()) {
			Save(p);
			p.startDownloader();
		} else {
			Settings.Menu c = menuStack.remove(menuStack.size() - 1);
			c.run(p);
		}
	}

	static void goBackOuterMenu(final FFPlayer p) {
		if (!menuStack.isEmpty())
			menuStack.remove(menuStack.size() - 1);
		goBack(p);
	}

	static class OkButton extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(R.string.ok);
		}

		void run(final FFPlayer p) {
			goBackOuterMenu(p);
		}
	}

	public static class DummyMenu extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(R.string.ok);
		}

		void run(final FFPlayer p) {
			goBack(p);
		}
	}

	static class MainMenu extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(R.string.device_config);
		}

		void run(final FFPlayer p) {
			Settings.Menu options[] = { new DownloadConfig(),
					new OptionalDownloadConfig(false),
					new KeyboardConfigMainMenu(),
					new MouseConfigMainMenu(), new GyroscopeCalibration(),
					new AudioConfig(), new RemapHwKeysConfig(),
					new ScreenGesturesConfig(), new VideoSettingsConfig(),
					new ResetToDefaultsConfig(), new OkButton(), };
			showMenuOptionsList(p, options);
		}
	}

	static class MouseConfigMainMenu extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(R.string.mouse_emulation);
		}

		boolean enabled() {
			return Globals.AppUsesMouse;
		}

		void run(final FFPlayer p) {
			Settings.Menu options[] = { new DisplaySizeConfig(false),
					new LeftClickConfig(), new RightClickConfig(),
					new AdditionalMouseConfig(), new JoystickMouseConfig(),
					new TouchPressureMeasurementTool(),
					new CalibrateTouchscreenMenu(), new OkButton(), };
			showMenuOptionsList(p, options);
		}
	}

	static class KeyboardConfigMainMenu extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(R.string.controls_screenkb);
		}

		boolean enabled() {
			return Globals.UseTouchscreenKeyboard;
		}

		void run(final FFPlayer p) {
			Settings.Menu options[] = { new ScreenKeyboardThemeConfig(),
					new ScreenKeyboardSizeConfig(),
					new ScreenKeyboardDrawSizeConfig(),
					new ScreenKeyboardTransparencyConfig(),
					new RemapScreenKbConfig(),
					new CustomizeScreenKbLayout(), new OkButton(), };
			showMenuOptionsList(p, options);
		}
	}

	static class DownloadConfig extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(R.string.storage_question);
		}

		void run(final FFPlayer p) {
			long freeSdcard = 0;
			long freePhone = 0;
			try {
				StatFs sdcard = new StatFs(Environment
						.getExternalStorageDirectory().getPath());
				StatFs phone = new StatFs(Environment.getDataDirectory()
						.getPath());
				freeSdcard = (long) sdcard.getAvailableBlocks()
						* sdcard.getBlockSize() / 1024 / 1024;
				freePhone = (long) phone.getAvailableBlocks()
						* phone.getBlockSize() / 1024 / 1024;
			} catch (Exception e) {
			}

			final CharSequence[] items = {
					p.getResources().getString(R.string.storage_phone,
							freePhone),
					p.getResources().getString(R.string.storage_sd,
							freeSdcard),
					p.getResources().getString(R.string.storage_custom) };
			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(
					R.string.storage_question));
			builder.setSingleChoiceItems(items,
					Globals.DownloadToSdcard ? 1 : 0,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							dialog.dismiss();

							if (item == 2)
								showCustomDownloadDirConfig(p);
							else {
								Globals.DownloadToSdcard = (item != 0);
								Globals.DataDir = Globals.DownloadToSdcard ? SdcardAppPath
										.getPath(p) : p.getFilesDir()
										.getAbsolutePath();
								goBack(p);
							}
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}

		static void showCustomDownloadDirConfig(final FFPlayer p) {
			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(
					R.string.storage_custom));

			final EditText edit = new EditText(p);
			edit.setFocusableInTouchMode(true);
			edit.setFocusable(true);
			edit.setText(Globals.DataDir);
			builder.setView(edit);

			builder.setPositiveButton(
					p.getResources().getString(R.string.ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							Globals.DataDir = edit.getText().toString();
							dialog.dismiss();
							showCommandLineConfig(p);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}

		static void showCommandLineConfig(final FFPlayer p) {
			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(
					R.string.storage_commandline));

			final EditText edit = new EditText(p);
			edit.setFocusableInTouchMode(true);
			edit.setFocusable(true);
			edit.setText(Globals.CommandLine);
			builder.setView(edit);

			builder.setPositiveButton(
					p.getResources().getString(R.string.ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							Globals.CommandLine = edit.getText().toString();
							dialog.dismiss();
							goBack(p);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	public static class OptionalDownloadConfig extends Settings.Menu {
		boolean firstStart = false;

		public OptionalDownloadConfig() {
			firstStart = false;
		}

		public OptionalDownloadConfig(boolean firstStart) {
			this.firstStart = firstStart;
		}

		public String title(final FFPlayer p) {
			return p.getResources().getString(R.string.downloads);
		}

		public void run(final FFPlayer p) {
			String[] downloadFiles = Globals.DataDownloadUrl;
			final boolean[] mandatory = new boolean[downloadFiles.length];

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(R.string.downloads));

			CharSequence[] items = new CharSequence[downloadFiles.length];
			for (int i = 0; i < downloadFiles.length; i++) {
				items[i] = new String(downloadFiles[i].split("[|]")[0]);
				if (items[i].toString().indexOf("!") == 0)
					items[i] = items[i].toString().substring(1);
				if (items[i].toString().indexOf("!") == 0) {
					items[i] = items[i].toString().substring(1);
					mandatory[i] = true;
				}
			}

			if (Globals.OptionalDataDownload == null
					|| Globals.OptionalDataDownload.length != items.length) {
				Globals.OptionalDataDownload = new boolean[downloadFiles.length];
				boolean oldFormat = true;
				for (int i = 0; i < downloadFiles.length; i++) {
					if (downloadFiles[i].indexOf("!") == 0) {
						Globals.OptionalDataDownload[i] = true;
						oldFormat = false;
					}
				}
				if (oldFormat) {
					Globals.OptionalDataDownload[0] = true;
					mandatory[0] = true;
				}
			}

			builder.setMultiChoiceItems(items,
					Globals.OptionalDataDownload,
					new DialogInterface.OnMultiChoiceClickListener() {
						public void onClick(DialogInterface dialog,
								int item, boolean isChecked) {
							Globals.OptionalDataDownload[item] = isChecked;
							if (mandatory[item] && !isChecked) {
								Globals.OptionalDataDownload[item] = true;
								((AlertDialog) dialog).getListView()
										.setItemChecked(item, true);
							}
						}
					});
			builder.setPositiveButton(
					p.getResources().getString(R.string.ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							dialog.dismiss();
							goBack(p);
						}
					});
			if (firstStart) {
				builder.setNegativeButton(
						p.getResources().getString(
								R.string.show_more_options),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int item) {
								dialog.dismiss();
								menuStack.clear();
								new MainMenu().run(p);
							}
						});
			}
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class ScreenKeyboardSizeConfig extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(
					R.string.controls_screenkb_size);
		}

		void run(final FFPlayer p) {
			final CharSequence[] items = {
					p.getResources().getString(
							R.string.controls_screenkb_large),
					p.getResources().getString(
							R.string.controls_screenkb_medium),
					p.getResources().getString(
							R.string.controls_screenkb_small),
					p.getResources().getString(
							R.string.controls_screenkb_tiny) };

			for (int i = 0; i < Globals.ScreenKbControlsLayout.length; i++)
				for (int ii = 0; ii < 4; ii++)
					Globals.ScreenKbControlsLayout[i][ii] = 0;

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(
					R.string.controls_screenkb_size));
			builder.setSingleChoiceItems(items,
					Globals.TouchscreenKeyboardSize,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							Globals.TouchscreenKeyboardSize = item;

							dialog.dismiss();
							goBack(p);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class ScreenKeyboardDrawSizeConfig extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(
					R.string.controls_screenkb_drawsize);
		}

		void run(final FFPlayer p) {
			final CharSequence[] items = {
					p.getResources().getString(
							R.string.controls_screenkb_large),
					p.getResources().getString(
							R.string.controls_screenkb_medium),
					p.getResources().getString(
							R.string.controls_screenkb_small),
					p.getResources().getString(
							R.string.controls_screenkb_tiny) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(
					R.string.controls_screenkb_drawsize));
			builder.setSingleChoiceItems(items,
					Globals.TouchscreenKeyboardDrawSize,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							Globals.TouchscreenKeyboardDrawSize = item;

							dialog.dismiss();
							goBack(p);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class ScreenKeyboardThemeConfig extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(
					R.string.controls_screenkb_theme);
		}

		void run(final FFPlayer p) {
			final CharSequence[] items = {
					p.getResources().getString(
							R.string.controls_screenkb_by,
							"Ultimate Droid", "Sean Stieber"),
					p.getResources().getString(
							R.string.controls_screenkb_by, "Simple Theme",
							"Beholder"),
					p.getResources().getString(
							R.string.controls_screenkb_by, "Sun", "Sirea") };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(
					R.string.controls_screenkb_theme));
			builder.setSingleChoiceItems(items,
					Globals.TouchscreenKeyboardTheme,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							Globals.TouchscreenKeyboardTheme = item;

							dialog.dismiss();
							goBack(p);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class ScreenKeyboardTransparencyConfig extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(
					R.string.controls_screenkb_transparency);
		}

		void run(final FFPlayer p) {
			final CharSequence[] items = {
					p.getResources().getString(
							R.string.controls_screenkb_trans_0),
					p.getResources().getString(
							R.string.controls_screenkb_trans_1),
					p.getResources().getString(
							R.string.controls_screenkb_trans_2),
					p.getResources().getString(
							R.string.controls_screenkb_trans_3),
					p.getResources().getString(
							R.string.controls_screenkb_trans_4) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(
					R.string.controls_screenkb_transparency));
			builder.setSingleChoiceItems(items,
					Globals.TouchscreenKeyboardTransparency,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							Globals.TouchscreenKeyboardTransparency = item;

							dialog.dismiss();
							goBack(p);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class AudioConfig extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(R.string.audiobuf_question);
		}

		void run(final FFPlayer p) {
			final CharSequence[] items = {
					p.getResources().getString(R.string.audiobuf_verysmall),
					p.getResources().getString(R.string.audiobuf_small),
					p.getResources().getString(R.string.audiobuf_medium),
					p.getResources().getString(R.string.audiobuf_large) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.audiobuf_question);
			builder.setSingleChoiceItems(items, Globals.AudioBufferConfig,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							Globals.AudioBufferConfig = item;
							dialog.dismiss();
							goBack(p);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	public static class DisplaySizeConfig extends Settings.Menu {
		boolean firstStart = false;

		public DisplaySizeConfig() {
			this.firstStart = false;
		}

		public DisplaySizeConfig(boolean firstStart) {
			this.firstStart = firstStart;
		}

		String title(final FFPlayer p) {
			return p.getResources().getString(R.string.display_size_mouse);
		}

		void run(final FFPlayer p) {
			CharSequence[] items = {
					p.getResources().getString(
							R.string.display_size_tiny_touchpad),
					p.getResources().getString(R.string.display_size_tiny),
					p.getResources().getString(R.string.display_size_small),
					p.getResources().getString(
							R.string.display_size_small_touchpad),
					p.getResources().getString(R.string.display_size_large), };
			int _size_tiny_touchpad = 0;
			int _size_tiny = 1;
			int _size_small = 2;
			int _size_small_touchpad = 3;
			int _size_large = 4;
			int _more_options = 5;

			if (!Globals.SwVideoMode) {
				CharSequence[] items2 = {
						p.getResources().getString(
								R.string.display_size_small_touchpad),
						p.getResources().getString(
								R.string.display_size_large), };
				items = items2;
				_size_small_touchpad = 0;
				_size_large = 1;
				_size_tiny_touchpad = _size_tiny = _size_small = 1000;

			}
			if (firstStart) {
				CharSequence[] items2 = {
						p.getResources().getString(
								R.string.display_size_tiny_touchpad),
						p.getResources().getString(
								R.string.display_size_tiny),
						p.getResources().getString(
								R.string.display_size_small),
						p.getResources().getString(
								R.string.display_size_small_touchpad),
						p.getResources().getString(
								R.string.display_size_large),
						p.getResources().getString(
								R.string.show_more_options), };
				items = items2;
				if (!Globals.SwVideoMode) {
					CharSequence[] items3 = {
							p.getResources().getString(
									R.string.display_size_small_touchpad),
							p.getResources().getString(
									R.string.display_size_large),
							p.getResources().getString(
									R.string.show_more_options), };
					items = items3;
					_more_options = 3;
				}
			}
			// Java is so damn worse than C++11
			final int size_tiny_touchpad = _size_tiny_touchpad;
			final int size_tiny = _size_tiny;
			final int size_small = _size_small;
			final int size_small_touchpad = _size_small_touchpad;
			final int size_large = _size_large;
			final int more_options = _more_options;

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.display_size);
			class ClickListener implements DialogInterface.OnClickListener {
				public void onClick(DialogInterface dialog, int item) {
					dialog.dismiss();
					if (item == size_large) {
						Globals.LeftClickMethod = Mouse.LEFT_CLICK_NORMAL;
						Globals.RelativeMouseMovement = false;
						Globals.ShowScreenUnderFinger = Mouse.ZOOM_NONE;
					}
					if (item == size_small) {
						Globals.LeftClickMethod = Mouse.LEFT_CLICK_NEAR_CURSOR;
						Globals.RelativeMouseMovement = false;
						Globals.ShowScreenUnderFinger = Mouse.ZOOM_MAGNIFIER;
					}
					if (item == size_small_touchpad) {
						Globals.LeftClickMethod = Mouse.LEFT_CLICK_WITH_TAP_OR_TIMEOUT;
						Globals.RelativeMouseMovement = true;
						Globals.ShowScreenUnderFinger = Mouse.ZOOM_NONE;
					}
					if (item == size_tiny) {
						Globals.LeftClickMethod = Mouse.LEFT_CLICK_NEAR_CURSOR;
						Globals.RelativeMouseMovement = false;
						Globals.ShowScreenUnderFinger = Mouse.ZOOM_SCREEN_TRANSFORM;
					}
					if (item == size_tiny_touchpad) {
						Globals.LeftClickMethod = Mouse.LEFT_CLICK_WITH_TAP_OR_TIMEOUT;
						Globals.RelativeMouseMovement = true;
						Globals.ShowScreenUnderFinger = Mouse.ZOOM_FULLSCREEN_MAGNIFIER;
					}
					if (item == more_options) {
						menuStack.clear();
						new MainMenu().run(p);
						return;
					}
					goBack(p);
				}
			}
			builder.setItems(items, new ClickListener());
			/*
			 * else builder.setSingleChoiceItems(items,
			 * Globals.ShowScreenUnderFinger == Mouse.ZOOM_NONE ? (
			 * Globals.RelativeMouseMovement ? Globals.SwVideoMode ? 2 : 1 :
			 * 0 ) : ( Globals.ShowScreenUnderFinger == Mouse.ZOOM_MAGNIFIER
			 * && Globals.SwVideoMode ) ? 1 : Globals.ShowScreenUnderFinger
			 * + 1, new ClickListener());
			 */
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class LeftClickConfig extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(R.string.leftclick_question);
		}

		void run(final FFPlayer p) {
			final CharSequence[] items = {
					p.getResources().getString(R.string.leftclick_normal),
					p.getResources().getString(
							R.string.leftclick_near_cursor),
					p.getResources().getString(
							R.string.leftclick_multitouch),
					p.getResources().getString(R.string.leftclick_pressure),
					p.getResources().getString(R.string.rightclick_key),
					p.getResources().getString(R.string.leftclick_timeout),
					p.getResources().getString(R.string.leftclick_tap),
					p.getResources().getString(
							R.string.leftclick_tap_or_timeout) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.leftclick_question);
			builder.setSingleChoiceItems(items, Globals.LeftClickMethod,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							dialog.dismiss();
							Globals.LeftClickMethod = item;
							if (item == Mouse.LEFT_CLICK_WITH_KEY)
								p.keyListener = new KeyRemapToolMouseClick(
										p, true);
							else if (item == Mouse.LEFT_CLICK_WITH_TIMEOUT
									|| item == Mouse.LEFT_CLICK_WITH_TAP_OR_TIMEOUT)
								showLeftClickTimeoutConfig(p);
							else
								goBack(p);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}

		static void showLeftClickTimeoutConfig(final FFPlayer p) {
			final CharSequence[] items = {
					p.getResources().getString(
							R.string.leftclick_timeout_time_0),
					p.getResources().getString(
							R.string.leftclick_timeout_time_1),
					p.getResources().getString(
							R.string.leftclick_timeout_time_2),
					p.getResources().getString(
							R.string.leftclick_timeout_time_3),
					p.getResources().getString(
							R.string.leftclick_timeout_time_4) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.leftclick_timeout_time);
			builder.setSingleChoiceItems(items, Globals.LeftClickTimeout,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							Globals.LeftClickTimeout = item;
							dialog.dismiss();
							goBack(p);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class RightClickConfig extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(R.string.rightclick_question);
		}

		boolean enabled() {
			return Globals.AppNeedsTwoButtonMouse;
		}

		void run(final FFPlayer p) {
			final CharSequence[] items = {
					p.getResources().getString(R.string.rightclick_none),
					p.getResources().getString(
							R.string.rightclick_multitouch),
					p.getResources()
							.getString(R.string.rightclick_pressure),
					p.getResources().getString(R.string.rightclick_key),
					p.getResources().getString(R.string.leftclick_timeout) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.rightclick_question);
			builder.setSingleChoiceItems(items, Globals.RightClickMethod,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							Globals.RightClickMethod = item;
							dialog.dismiss();
							if (item == Mouse.RIGHT_CLICK_WITH_KEY)
								p.keyListener = new KeyRemapToolMouseClick(
										p, false);
							else if (item == Mouse.RIGHT_CLICK_WITH_TIMEOUT)
								showRightClickTimeoutConfig(p);
							else
								goBack(p);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}

		static void showRightClickTimeoutConfig(final FFPlayer p) {
			final CharSequence[] items = {
					p.getResources().getString(
							R.string.leftclick_timeout_time_0),
					p.getResources().getString(
							R.string.leftclick_timeout_time_1),
					p.getResources().getString(
							R.string.leftclick_timeout_time_2),
					p.getResources().getString(
							R.string.leftclick_timeout_time_3),
					p.getResources().getString(
							R.string.leftclick_timeout_time_4) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.leftclick_timeout_time);
			builder.setSingleChoiceItems(items, Globals.RightClickTimeout,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							Globals.RightClickTimeout = item;
							dialog.dismiss();
							goBack(p);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	public static class KeyRemapToolMouseClick implements KeyEventsListener {
		FFPlayer p;
		boolean leftClick;

		public KeyRemapToolMouseClick(FFPlayer _p, boolean leftClick) {
			p = _p;
			p.setText(p.getResources().getString(
					R.string.remap_hwkeys_press));
			this.leftClick = leftClick;
		}

		public void onKeyEvent(final int keyCode) {
			p.keyListener = null;
			int keyIndex = keyCode;
			if (keyIndex < 0)
				keyIndex = 0;
			if (keyIndex > SDL_Keys.JAVA_KEYCODE_LAST)
				keyIndex = 0;

			if (leftClick)
				Globals.LeftClickKey = keyIndex;
			else
				Globals.RightClickKey = keyIndex;

			goBack(p);
		}
	}

	static class AdditionalMouseConfig extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(
					R.string.pointandclick_question);
		}

		void run(final FFPlayer p) {
			CharSequence[] items = {
					p.getResources().getString(
							R.string.pointandclick_joystickmouse),
					p.getResources().getString(
							R.string.click_with_dpadcenter),
					p.getResources().getString(
							R.string.pointandclick_relative) };

			boolean defaults[] = { Globals.MoveMouseWithJoystick,
					Globals.ClickMouseWithDpad,
					Globals.RelativeMouseMovement };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(
					R.string.pointandclick_question));
			builder.setMultiChoiceItems(items, defaults,
					new DialogInterface.OnMultiChoiceClickListener() {
						public void onClick(DialogInterface dialog,
								int item, boolean isChecked) {
							if (item == 0)
								Globals.MoveMouseWithJoystick = isChecked;
							if (item == 1)
								Globals.ClickMouseWithDpad = isChecked;
							if (item == 2)
								Globals.RelativeMouseMovement = isChecked;
						}
					});
			builder.setPositiveButton(
					p.getResources().getString(R.string.ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							dialog.dismiss();
							if (Globals.RelativeMouseMovement)
								showRelativeMouseMovementConfig(p);
							else
								goBack(p);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}

		static void showRelativeMouseMovementConfig(final FFPlayer p) {
			final CharSequence[] items = {
					p.getResources().getString(R.string.accel_veryslow),
					p.getResources().getString(R.string.accel_slow),
					p.getResources().getString(R.string.accel_medium),
					p.getResources().getString(R.string.accel_fast),
					p.getResources().getString(R.string.accel_veryfast) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.pointandclick_relative_speed);
			builder.setSingleChoiceItems(items,
					Globals.RelativeMouseMovementSpeed,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							Globals.RelativeMouseMovementSpeed = item;

							dialog.dismiss();
							showRelativeMouseMovementConfig1(p);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}

		static void showRelativeMouseMovementConfig1(final FFPlayer p) {
			final CharSequence[] items = {
					p.getResources().getString(R.string.none),
					p.getResources().getString(R.string.accel_slow),
					p.getResources().getString(R.string.accel_medium),
					p.getResources().getString(R.string.accel_fast) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.pointandclick_relative_accel);
			builder.setSingleChoiceItems(items,
					Globals.RelativeMouseMovementAccel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							Globals.RelativeMouseMovementAccel = item;

							dialog.dismiss();
							goBack(p);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class JoystickMouseConfig extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(
					R.string.pointandclick_joystickmousespeed);
		}

		boolean enabled() {
			return Globals.MoveMouseWithJoystick;
		};

		void run(final FFPlayer p) {
			final CharSequence[] items = {
					p.getResources().getString(R.string.accel_slow),
					p.getResources().getString(R.string.accel_medium),
					p.getResources().getString(R.string.accel_fast) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.pointandclick_joystickmousespeed);
			builder.setSingleChoiceItems(items,
					Globals.MoveMouseWithJoystickSpeed,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							Globals.MoveMouseWithJoystickSpeed = item;

							dialog.dismiss();
							showJoystickMouseAccelConfig(p);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}

		static void showJoystickMouseAccelConfig(final FFPlayer p) {
			final CharSequence[] items = {
					p.getResources().getString(R.string.none),
					p.getResources().getString(R.string.accel_slow),
					p.getResources().getString(R.string.accel_medium),
					p.getResources().getString(R.string.accel_fast) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.pointandclick_joystickmouseaccel);
			builder.setSingleChoiceItems(items,
					Globals.MoveMouseWithJoystickAccel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							Globals.MoveMouseWithJoystickAccel = item;

							dialog.dismiss();
							goBack(p);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	public interface TouchEventsListener {
		public void onTouchEvent(final MotionEvent ev);
	}

	public interface KeyEventsListener {
		public void onKeyEvent(final int keyCode);
	}

	static class TouchPressureMeasurementTool extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(R.string.measurepressure);
		}

		boolean enabled() {
			return Globals.RightClickMethod == Mouse.RIGHT_CLICK_WITH_PRESSURE
					|| Globals.LeftClickMethod == Mouse.LEFT_CLICK_WITH_PRESSURE;
		};

		void run(final FFPlayer p) {
			p.setText(p.getResources().getString(
					R.string.measurepressure_touchplease));
			p.touchListener = new TouchMeasurementTool(p);
		}

		public static class TouchMeasurementTool implements
				TouchEventsListener {
			FFPlayer p;
			ArrayList<Integer> force = new ArrayList<Integer>();
			ArrayList<Integer> radius = new ArrayList<Integer>();
			static final int maxEventAmount = 100;

			public TouchMeasurementTool(FFPlayer _p) {
				p = _p;
			}

			public void onTouchEvent(final MotionEvent ev) {
				force.add(new Integer((int) (ev.getPressure() * 1000.0)));
				radius.add(new Integer((int) (ev.getSize() * 1000.0)));
				p.setText(p.getResources().getString(
						R.string.measurepressure_response,
						force.get(force.size() - 1),
						radius.get(radius.size() - 1)));
				try {
					Thread.sleep(10L);
				} catch (InterruptedException e) {
				}

				if (force.size() >= maxEventAmount) {
					p.touchListener = null;
					Globals.ClickScreenPressure = getAverageForce();
					Globals.ClickScreenTouchspotSize = getAverageRadius();
					System.out.println("SDL: measured average force "
							+ Globals.ClickScreenPressure + " radius "
							+ Globals.ClickScreenTouchspotSize);
					goBack(p);
				}
			}

			int getAverageForce() {
				int avg = 0;
				for (Integer f : force) {
					avg += f;
				}
				return avg / force.size();
			}

			int getAverageRadius() {
				int avg = 0;
				for (Integer r : radius) {
					avg += r;
				}
				return avg / radius.size();
			}
		}
	}

	static class RemapHwKeysConfig extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(R.string.remap_hwkeys);
		}

		// boolean enabled() { return true; };
		void run(final FFPlayer p) {
			p.setText(p.getResources().getString(
					R.string.remap_hwkeys_press));
			p.keyListener = new KeyRemapTool(p);
		}

		public static class KeyRemapTool implements KeyEventsListener {
			FFPlayer p;

			public KeyRemapTool(FFPlayer _p) {
				p = _p;
			}

			public void onKeyEvent(final int keyCode) {
				p.keyListener = null;
				int keyIndex = keyCode;
				if (keyIndex < 0)
					keyIndex = 0;
				if (keyIndex > SDL_Keys.JAVA_KEYCODE_LAST)
					keyIndex = 0;

				final int KeyIndexFinal = keyIndex;
				AlertDialog.Builder builder = new AlertDialog.Builder(p);
				builder.setTitle(R.string.remap_hwkeys_select);
				builder.setSingleChoiceItems(
						SDL_Keys.namesSorted,
						SDL_Keys.namesSortedBackIdx[Globals.RemapHwKeycode[keyIndex]],
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int item) {
								Globals.RemapHwKeycode[KeyIndexFinal] = SDL_Keys.namesSortedIdx[item];

								dialog.dismiss();
								goBack(p);
							}
						});
				builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						goBack(p);
					}
				});
				AlertDialog alert = builder.create();
				alert.setOwnerActivity(p);
				alert.show();
			}
		}
	}

	static class RemapScreenKbConfig extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(R.string.remap_screenkb);
		}

		// boolean enabled() { return true; };
		void run(final FFPlayer p) {
			CharSequence[] items = {
					p.getResources().getString(
							R.string.remap_screenkb_joystick),
					p.getResources().getString(
							R.string.remap_screenkb_button_text),
					p.getResources().getString(
							R.string.remap_screenkb_button)
							+ " 1",
					p.getResources().getString(
							R.string.remap_screenkb_button)
							+ " 2",
					p.getResources().getString(
							R.string.remap_screenkb_button)
							+ " 3",
					p.getResources().getString(
							R.string.remap_screenkb_button)
							+ " 4",
					p.getResources().getString(
							R.string.remap_screenkb_button)
							+ " 5",
					p.getResources().getString(
							R.string.remap_screenkb_button)
							+ " 6", };

			boolean defaults[] = { Globals.ScreenKbControlsShown[0],
					Globals.ScreenKbControlsShown[1],
					Globals.ScreenKbControlsShown[2],
					Globals.ScreenKbControlsShown[3],
					Globals.ScreenKbControlsShown[4],
					Globals.ScreenKbControlsShown[5],
					Globals.ScreenKbControlsShown[6],
					Globals.ScreenKbControlsShown[7], };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(
					R.string.remap_screenkb));
			builder.setMultiChoiceItems(items, defaults,
					new DialogInterface.OnMultiChoiceClickListener() {
						public void onClick(DialogInterface dialog,
								int item, boolean isChecked) {
							if (!Globals.UseTouchscreenKeyboard)
								item += 8;
							Globals.ScreenKbControlsShown[item] = isChecked;
						}
					});
			builder.setPositiveButton(
					p.getResources().getString(R.string.ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							dialog.dismiss();
							showRemapScreenKbConfig2(p, 0);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}

		static void showRemapScreenKbConfig2(final FFPlayer p,
				final int currentButton) {
			CharSequence[] items = {
					p.getResources().getString(
							R.string.remap_screenkb_button)
							+ " 1",
					p.getResources().getString(
							R.string.remap_screenkb_button)
							+ " 2",
					p.getResources().getString(
							R.string.remap_screenkb_button)
							+ " 3",
					p.getResources().getString(
							R.string.remap_screenkb_button)
							+ " 4",
					p.getResources().getString(
							R.string.remap_screenkb_button)
							+ " 5",
					p.getResources().getString(
							R.string.remap_screenkb_button)
							+ " 6", };

			if (currentButton >= Globals.RemapScreenKbKeycode.length) {
				goBack(p);
				return;
			}
			if (!Globals.ScreenKbControlsShown[currentButton + 2]) {
				showRemapScreenKbConfig2(p, currentButton + 1);
				return;
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(items[currentButton]);
			builder.setSingleChoiceItems(
					SDL_Keys.namesSorted,
					SDL_Keys.namesSortedBackIdx[Globals.RemapScreenKbKeycode[currentButton]],
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							Globals.RemapScreenKbKeycode[currentButton] = SDL_Keys.namesSortedIdx[item];

							dialog.dismiss();
							showRemapScreenKbConfig2(p, currentButton + 1);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class ScreenGesturesConfig extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(
					R.string.remap_screenkb_button_gestures);
		}

		// boolean enabled() { return true; };
		void run(final FFPlayer p) {
			CharSequence[] items = {
					p.getResources().getString(
							R.string.remap_screenkb_button_zoomin),
					p.getResources().getString(
							R.string.remap_screenkb_button_zoomout),
					p.getResources().getString(
							R.string.remap_screenkb_button_rotateleft),
					p.getResources().getString(
							R.string.remap_screenkb_button_rotateright), };

			boolean defaults[] = { Globals.MultitouchGesturesUsed[0],
					Globals.MultitouchGesturesUsed[1],
					Globals.MultitouchGesturesUsed[2],
					Globals.MultitouchGesturesUsed[3], };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(
					R.string.remap_screenkb_button_gestures));
			builder.setMultiChoiceItems(items, defaults,
					new DialogInterface.OnMultiChoiceClickListener() {
						public void onClick(DialogInterface dialog,
								int item, boolean isChecked) {
							Globals.MultitouchGesturesUsed[item] = isChecked;
						}
					});
			builder.setPositiveButton(
					p.getResources().getString(R.string.ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							dialog.dismiss();
							showScreenGesturesConfig2(p);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}

		static void showScreenGesturesConfig2(final FFPlayer p) {
			final CharSequence[] items = {
					p.getResources().getString(R.string.accel_slow),
					p.getResources().getString(R.string.accel_medium),
					p.getResources().getString(R.string.accel_fast),
					p.getResources().getString(R.string.accel_veryfast) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.remap_screenkb_button_gestures_sensitivity);
			builder.setSingleChoiceItems(items,
					Globals.MultitouchGestureSensitivity,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							Globals.MultitouchGestureSensitivity = item;

							dialog.dismiss();
							showScreenGesturesConfig3(p, 0);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}

		static void showScreenGesturesConfig3(final FFPlayer p,
				final int currentButton) {
			CharSequence[] items = {
					p.getResources().getString(
							R.string.remap_screenkb_button_zoomin),
					p.getResources().getString(
							R.string.remap_screenkb_button_zoomout),
					p.getResources().getString(
							R.string.remap_screenkb_button_rotateleft),
					p.getResources().getString(
							R.string.remap_screenkb_button_rotateright), };

			if (currentButton >= Globals.RemapMultitouchGestureKeycode.length) {
				goBack(p);
				return;
			}
			if (!Globals.MultitouchGesturesUsed[currentButton]) {
				showScreenGesturesConfig3(p, currentButton + 1);
				return;
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(items[currentButton]);
			builder.setSingleChoiceItems(
					SDL_Keys.namesSorted,
					SDL_Keys.namesSortedBackIdx[Globals.RemapMultitouchGestureKeycode[currentButton]],
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							Globals.RemapMultitouchGestureKeycode[currentButton] = SDL_Keys.namesSortedIdx[item];

							dialog.dismiss();
							showScreenGesturesConfig3(p, currentButton + 1);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class CalibrateTouchscreenMenu extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(
					R.string.calibrate_touchscreen);
		}

		// boolean enabled() { return true; };
		void run(final FFPlayer p) {
			p.setText(p.getResources().getString(
					R.string.calibrate_touchscreen_touch));
			Globals.TouchscreenCalibration[0] = 0;
			Globals.TouchscreenCalibration[1] = 0;
			Globals.TouchscreenCalibration[2] = 0;
			Globals.TouchscreenCalibration[3] = 0;
			CalibrateTouchscreenMenu.ScreenEdgesCalibrationTool tool = new ScreenEdgesCalibrationTool(
					p);
			p.touchListener = tool;
			p.keyListener = tool;
		}

		static class ScreenEdgesCalibrationTool implements
				TouchEventsListener, KeyEventsListener {
			FFPlayer p;
			ImageView img;
			Bitmap bmp;

			public ScreenEdgesCalibrationTool(FFPlayer _p) {
				p = _p;
				img = new ImageView(p);
				img.setLayoutParams(new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.FILL_PARENT,
						ViewGroup.LayoutParams.FILL_PARENT));
				img.setScaleType(ImageView.ScaleType.MATRIX);
				bmp = BitmapFactory.decodeResource(p.getResources(),
						R.drawable.calibrate);
				img.setImageBitmap(bmp);
				Matrix m = new Matrix();
				RectF src = new RectF(0, 0, bmp.getWidth(), bmp.getHeight());
				RectF dst = new RectF(Globals.TouchscreenCalibration[0],
						Globals.TouchscreenCalibration[1],
						Globals.TouchscreenCalibration[2],
						Globals.TouchscreenCalibration[3]);
				m.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
				img.setImageMatrix(m);
				p.getVideoLayout().addView(img);
			}

			public void onTouchEvent(final MotionEvent ev) {
				if (Globals.TouchscreenCalibration[0] == Globals.TouchscreenCalibration[1]
						&& Globals.TouchscreenCalibration[1] == Globals.TouchscreenCalibration[2]
						&& Globals.TouchscreenCalibration[2] == Globals.TouchscreenCalibration[3]) {
					Globals.TouchscreenCalibration[0] = (int) ev.getX();
					Globals.TouchscreenCalibration[1] = (int) ev.getY();
					Globals.TouchscreenCalibration[2] = (int) ev.getX();
					Globals.TouchscreenCalibration[3] = (int) ev.getY();
				}
				if (ev.getX() < Globals.TouchscreenCalibration[0])
					Globals.TouchscreenCalibration[0] = (int) ev.getX();
				if (ev.getY() < Globals.TouchscreenCalibration[1])
					Globals.TouchscreenCalibration[1] = (int) ev.getY();
				if (ev.getX() > Globals.TouchscreenCalibration[2])
					Globals.TouchscreenCalibration[2] = (int) ev.getX();
				if (ev.getY() > Globals.TouchscreenCalibration[3])
					Globals.TouchscreenCalibration[3] = (int) ev.getY();
				Matrix m = new Matrix();
				RectF src = new RectF(0, 0, bmp.getWidth(), bmp.getHeight());
				RectF dst = new RectF(Globals.TouchscreenCalibration[0],
						Globals.TouchscreenCalibration[1],
						Globals.TouchscreenCalibration[2],
						Globals.TouchscreenCalibration[3]);
				m.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
				img.setImageMatrix(m);
			}

			public void onKeyEvent(final int keyCode) {
				p.touchListener = null;
				p.keyListener = null;
				p.getVideoLayout().removeView(img);
				goBack(p);
			}
		}
	}

	static class CustomizeScreenKbLayout extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(
					R.string.screenkb_custom_layout);
		}

		// boolean enabled() { return true; };
		void run(final FFPlayer p) {
			p.setText(p.getResources().getString(
					R.string.screenkb_custom_layout_help));
			CustomizeScreenKbLayout.CustomizeScreenKbLayoutTool tool = new CustomizeScreenKbLayoutTool(
					p);
			p.touchListener = tool;
			p.keyListener = tool;
		}

		static class CustomizeScreenKbLayoutTool implements
				TouchEventsListener, KeyEventsListener {
			FFPlayer p;
			FrameLayout layout = null;
			ImageView imgs[] = new ImageView[Globals.ScreenKbControlsLayout.length];
			Bitmap bmps[] = new Bitmap[Globals.ScreenKbControlsLayout.length];
			ImageView boundary = null;
			Bitmap boundaryBmp = null;
			int currentButton = 0;
			int buttons[] = { R.drawable.dpad, R.drawable.keyboard,
					R.drawable.b1, R.drawable.b2, R.drawable.b3,
					R.drawable.b4, R.drawable.b5, R.drawable.b6 };

			public CustomizeScreenKbLayoutTool(FFPlayer _p) {
				p = _p;
				layout = new FrameLayout(p);
				p.getVideoLayout().addView(layout);
				boundary = new ImageView(p);
				boundary.setLayoutParams(new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.FILL_PARENT,
						ViewGroup.LayoutParams.FILL_PARENT));
				boundary.setScaleType(ImageView.ScaleType.MATRIX);
				boundaryBmp = BitmapFactory.decodeResource(
						p.getResources(), R.drawable.rectangle);
				boundary.setImageBitmap(boundaryBmp);
				layout.addView(boundary);
				currentButton = 0;
				if (Globals.TouchscreenKeyboardTheme == 2) {
					int buttons2[] = { R.drawable.sun_dpad,
							R.drawable.sun_keyboard, R.drawable.sun_b1,
							R.drawable.sun_b2, R.drawable.sun_b3,
							R.drawable.sun_b4, R.drawable.sun_b5,
							R.drawable.sun_b6 };
					buttons = buttons2;
				}

				for (int i = 0; i < Globals.ScreenKbControlsLayout.length; i++) {
					imgs[i] = new ImageView(p);
					imgs[i].setLayoutParams(new ViewGroup.LayoutParams(
							ViewGroup.LayoutParams.FILL_PARENT,
							ViewGroup.LayoutParams.FILL_PARENT));
					imgs[i].setScaleType(ImageView.ScaleType.MATRIX);
					bmps[i] = BitmapFactory.decodeResource(
							p.getResources(), buttons[i]);
					imgs[i].setImageBitmap(bmps[i]);
					imgs[i].setAlpha(128);
					layout.addView(imgs[i]);
					Matrix m = new Matrix();
					RectF src = new RectF(0, 0, bmps[i].getWidth(),
							bmps[i].getHeight());
					RectF dst = new RectF(
							Globals.ScreenKbControlsLayout[i][0],
							Globals.ScreenKbControlsLayout[i][1],
							Globals.ScreenKbControlsLayout[i][2],
							Globals.ScreenKbControlsLayout[i][3]);
					m.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
					imgs[i].setImageMatrix(m);
				}
				boundary.bringToFront();

				setupButton(true);
			}

			void setupButton(boolean undo) {
				do {
					currentButton += (undo ? -1 : 1);
					if (currentButton >= Globals.ScreenKbControlsLayout.length) {
						p.getVideoLayout().removeView(layout);
						layout = null;
						p.touchListener = null;
						p.keyListener = null;
						goBack(p);
						return;
					}
					if (currentButton < 0) {
						currentButton = 0;
						undo = false;
					}
				} while (!Globals.ScreenKbControlsShown[currentButton]);

				if (Globals.ScreenKbControlsLayout[currentButton][0] == Globals.ScreenKbControlsLayout[currentButton][2]
						|| Globals.ScreenKbControlsLayout[currentButton][1] == Globals.ScreenKbControlsLayout[currentButton][3]) {
					int displayX = 800;
					int displayY = 480;
					try {
						DisplayMetrics dm = new DisplayMetrics();
						p.getWindowManager().getDefaultDisplay()
								.getMetrics(dm);
						displayX = dm.widthPixels;
						displayY = dm.heightPixels;
					} catch (Exception eeeee) {
					}
					Globals.ScreenKbControlsLayout[currentButton][0] = displayX
							/ 2 - displayX / 6;
					Globals.ScreenKbControlsLayout[currentButton][2] = displayX
							/ 2 + displayX / 6;
					Globals.ScreenKbControlsLayout[currentButton][1] = displayY
							/ 2 - displayY / 4;
					Globals.ScreenKbControlsLayout[currentButton][3] = displayY
							/ 2 + displayY / 4;
				}
				Matrix m = new Matrix();
				RectF src = new RectF(0, 0, bmps[currentButton].getWidth(),
						bmps[currentButton].getHeight());
				RectF dst = new RectF(
						Globals.ScreenKbControlsLayout[currentButton][0],
						Globals.ScreenKbControlsLayout[currentButton][1],
						Globals.ScreenKbControlsLayout[currentButton][2],
						Globals.ScreenKbControlsLayout[currentButton][3]);
				m.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
				imgs[currentButton].setImageMatrix(m);
				m = new Matrix();
				src = new RectF(0, 0, boundaryBmp.getWidth(),
						boundaryBmp.getHeight());
				m.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
				boundary.setImageMatrix(m);
				String buttonText = (currentButton == 0 ? "DPAD"
						: (currentButton == 1 ? "Text input" : ""));
				if (currentButton >= 2
						&& currentButton - 2 < Globals.AppTouchscreenKeyboardKeysNames.length)
					buttonText = Globals.AppTouchscreenKeyboardKeysNames[currentButton - 2];
				p.setText(p.getResources().getString(
						R.string.screenkb_custom_layout_help)
						+ "\n" + buttonText.replace("_", " "));
			}

			public void onTouchEvent(final MotionEvent ev) {
				if (currentButton >= Globals.ScreenKbControlsLayout.length) {
					setupButton(false);
					return;
				}
				if (ev.getAction() == MotionEvent.ACTION_DOWN) {
					Globals.ScreenKbControlsLayout[currentButton][0] = (int) ev
							.getX();
					Globals.ScreenKbControlsLayout[currentButton][1] = (int) ev
							.getY();
					Globals.ScreenKbControlsLayout[currentButton][2] = (int) ev
							.getX();
					Globals.ScreenKbControlsLayout[currentButton][3] = (int) ev
							.getY();
				}
				if (ev.getAction() == MotionEvent.ACTION_MOVE) {
					if (Globals.ScreenKbControlsLayout[currentButton][0] > (int) ev
							.getX())
						Globals.ScreenKbControlsLayout[currentButton][0] = (int) ev
								.getX();
					if (Globals.ScreenKbControlsLayout[currentButton][1] > (int) ev
							.getY())
						Globals.ScreenKbControlsLayout[currentButton][1] = (int) ev
								.getY();
					if (Globals.ScreenKbControlsLayout[currentButton][2] < (int) ev
							.getX())
						Globals.ScreenKbControlsLayout[currentButton][2] = (int) ev
								.getX();
					if (Globals.ScreenKbControlsLayout[currentButton][3] < (int) ev
							.getY())
						Globals.ScreenKbControlsLayout[currentButton][3] = (int) ev
								.getY();
				}

				Matrix m = new Matrix();
				RectF src = new RectF(0, 0, bmps[currentButton].getWidth(),
						bmps[currentButton].getHeight());
				RectF dst = new RectF(
						Globals.ScreenKbControlsLayout[currentButton][0],
						Globals.ScreenKbControlsLayout[currentButton][1],
						Globals.ScreenKbControlsLayout[currentButton][2],
						Globals.ScreenKbControlsLayout[currentButton][3]);
				m.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
				imgs[currentButton].setImageMatrix(m);
				m = new Matrix();
				src = new RectF(0, 0, boundaryBmp.getWidth(),
						boundaryBmp.getHeight());
				m.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
				boundary.setImageMatrix(m);

				if (ev.getAction() == MotionEvent.ACTION_UP)
					setupButton(false);
			}

			public void onKeyEvent(final int keyCode) {
				if (layout != null && imgs[currentButton] != null)
					layout.removeView(imgs[currentButton]);
				imgs[currentButton] = null;
				setupButton(true);
			}
		}
	}

	static class VideoSettingsConfig extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(R.string.video);
		}

		// boolean enabled() { return true; };
		void run(final FFPlayer p) {
			CharSequence[] items = {
					p.getResources().getString(
							R.string.pointandclick_keepaspectratio),
					p.getResources().getString(R.string.video_smooth) };
			boolean defaults[] = { Globals.KeepAspectRatio,
					Globals.VideoLinearFilter };

			if (Globals.SwVideoMode && !Globals.CompatibilityHacksVideo) {
				CharSequence[] items2 = {
						p.getResources().getString(
								R.string.pointandclick_keepaspectratio),
						p.getResources().getString(R.string.video_smooth),
						p.getResources().getString(
								R.string.video_separatethread), };
				boolean defaults2[] = { Globals.KeepAspectRatio,
						Globals.VideoLinearFilter,
						Globals.MultiThreadedVideo };
				items = items2;
				defaults = defaults2;
			}

			if (Globals.Using_SDL_1_3) {
				CharSequence[] items2 = { p.getResources().getString(
						R.string.pointandclick_keepaspectratio), };
				boolean defaults2[] = { Globals.KeepAspectRatio, };
				items = items2;
				defaults = defaults2;
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(R.string.video));
			builder.setMultiChoiceItems(items, defaults,
					new DialogInterface.OnMultiChoiceClickListener() {
						public void onClick(DialogInterface dialog,
								int item, boolean isChecked) {
							if (item == 0)
								Globals.KeepAspectRatio = isChecked;
							if (item == 1)
								Globals.VideoLinearFilter = isChecked;
							if (item == 2)
								Globals.MultiThreadedVideo = isChecked;
						}
					});
			builder.setPositiveButton(
					p.getResources().getString(R.string.ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							dialog.dismiss();
							goBack(p);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class ShowReadme extends Settings.Menu {
		String title(final FFPlayer p) {
			return "Readme";
		}

		boolean enabled() {
			return true;
		}

		void run(final FFPlayer p) {
			String readmes[] = Globals.ReadmeText.split("\\^");
			String lang = new String(Locale.getDefault().getLanguage())
					+ ":";
			String readme = readmes[0];
			for (String r : readmes) {
				if (r.startsWith(lang))
					readme = r.substring(lang.length());
			}
			TextView text = new TextView(p);
			text.setMaxLines(1000);
			text.setText(readme);
			text.setLayoutParams(new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.FILL_PARENT));
			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			ScrollView scroll = new ScrollView(p);
			scroll.addView(text);
			Button ok = new Button(p);
			final AlertDialog alertDismiss[] = new AlertDialog[1];
			ok.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					alertDismiss[0].cancel();
				}
			});
			ok.setText(R.string.ok);
			LinearLayout layout = new LinearLayout(p);
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.addView(scroll);
			layout.addView(ok);
			builder.setView(layout);
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alertDismiss[0] = alert;
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	public static class GyroscopeCalibration extends Settings.Menu implements
			SensorEventListener {
		String title(final FFPlayer p) {
			return p.getResources().getString(R.string.calibrate_gyroscope);
		}

		boolean enabled() {
			return Globals.AppUsesGyroscope;
		}

		void run(final FFPlayer p) {
			if (!Globals.AppUsesGyroscope
					|| !AccelerometerReader.gyro.available(p)) {
				Toast toast = Toast
						.makeText(
								p,
								p.getResources()
										.getString(
												R.string.calibrate_gyroscope_not_supported),
								Toast.LENGTH_LONG);
				toast.show();
				goBack(p);
				return;
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(
					R.string.calibrate_gyroscope));
			builder.setMessage(p.getResources().getString(
					R.string.calibrate_gyroscope_text));
			builder.setPositiveButton(
					p.getResources().getString(R.string.ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							dialog.dismiss();
							startCalibration(p);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}

		ImageView img;
		Bitmap bmp;
		int numEvents;
		FFPlayer p;

		void startCalibration(final FFPlayer _p) {
			p = _p;
			img = new ImageView(p);
			img.setLayoutParams(new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.FILL_PARENT));
			img.setScaleType(ImageView.ScaleType.MATRIX);
			bmp = BitmapFactory.decodeResource(p.getResources(),
					R.drawable.calibrate);
			img.setImageBitmap(bmp);
			Matrix m = new Matrix();
			RectF src = new RectF(0, 0, bmp.getWidth(), bmp.getHeight());
			RectF dst = new RectF(p.getVideoLayout().getWidth() / 2 - 50, p
					.getVideoLayout().getHeight() / 2 - 50, p
					.getVideoLayout().getWidth() / 2 + 50, p
					.getVideoLayout().getHeight() / 2 + 50);
			m.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
			img.setImageMatrix(m);
			p.getVideoLayout().addView(img);
			numEvents = 0;
			AccelerometerReader.gyro.x1 = 100;
			AccelerometerReader.gyro.x2 = -100;
			AccelerometerReader.gyro.xc = 0;
			AccelerometerReader.gyro.y1 = 100;
			AccelerometerReader.gyro.y2 = -100;
			AccelerometerReader.gyro.yc = 0;
			AccelerometerReader.gyro.z1 = 100;
			AccelerometerReader.gyro.z2 = -100;
			AccelerometerReader.gyro.zc = 0;
			AccelerometerReader.gyro.registerListener(p, this);
			(new Thread(new Runnable() {
				public void run() {
					for (int count = 1; count < 10; count++) {
						p.setText("" + count + "0% ...");
						try {
							Thread.sleep(500);
						} catch (Exception e) {
						}
					}
					finishCalibration(p);
				}
			})).start();
		}

		public void onSensorChanged(SensorEvent event) {
			gyroscopeEvent(event.values[0], event.values[1],
					event.values[2]);
		}

		public void onAccuracyChanged(Sensor s, int a) {
		}

		void gyroscopeEvent(float x, float y, float z) {
			numEvents++;
			AccelerometerReader.gyro.xc += x;
			AccelerometerReader.gyro.yc += y;
			AccelerometerReader.gyro.zc += z;
			AccelerometerReader.gyro.x1 = Math.min(
					AccelerometerReader.gyro.x1, x * 1.1f); // Small safety
															// bound
															// coefficient
			AccelerometerReader.gyro.x2 = Math.max(
					AccelerometerReader.gyro.x2, x * 1.1f);
			AccelerometerReader.gyro.y1 = Math.min(
					AccelerometerReader.gyro.y1, y * 1.1f);
			AccelerometerReader.gyro.y2 = Math.max(
					AccelerometerReader.gyro.y2, y * 1.1f);
			AccelerometerReader.gyro.z1 = Math.min(
					AccelerometerReader.gyro.z1, z * 1.1f);
			AccelerometerReader.gyro.z2 = Math.max(
					AccelerometerReader.gyro.z2, z * 1.1f);
			final Matrix m = new Matrix();
			RectF src = new RectF(0, 0, bmp.getWidth(), bmp.getHeight());
			RectF dst = new RectF(x * 5000 + p.getVideoLayout().getWidth()
					/ 2 - 50, y * 5000 + p.getVideoLayout().getHeight() / 2
					- 50,
					x * 5000 + p.getVideoLayout().getWidth() / 2 + 50, y
							* 5000 + p.getVideoLayout().getHeight() / 2
							+ 50);
			m.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
			p.runOnUiThread(new Runnable() {
				public void run() {
					img.setImageMatrix(m);
				}
			});
		}

		void finishCalibration(final FFPlayer p) {
			AccelerometerReader.gyro.unregisterListener(p, this);
			try {
				Thread.sleep(200); // Just in case we have pending events
			} catch (Exception e) {
			}
			if (numEvents > 5) {
				AccelerometerReader.gyro.xc /= (float) numEvents;
				AccelerometerReader.gyro.yc /= (float) numEvents;
				AccelerometerReader.gyro.zc /= (float) numEvents;
			}
			p.runOnUiThread(new Runnable() {
				public void run() {
					p.getVideoLayout().removeView(img);
					goBack(p);
				}
			});
		}
	}

	static class ResetToDefaultsConfig extends Settings.Menu {
		String title(final FFPlayer p) {
			return p.getResources().getString(R.string.reset_config);
		}

		boolean enabled() {
			return true;
		}

		void run(final FFPlayer p) {
			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(
					R.string.reset_config_ask));
			builder.setMessage(p.getResources().getString(
					R.string.reset_config_ask));

			builder.setPositiveButton(
					p.getResources().getString(R.string.ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							DeleteSdlConfigOnUpgradeAndRestart(p); // Never
																	// returns
							dialog.dismiss();
							goBack(p);
						}
					});
			builder.setNegativeButton(
					p.getResources().getString(R.string.cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							dialog.dismiss();
							goBack(p);
						}
					});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	// ===============================================================================================

	public static boolean deleteRecursively(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteRecursively(new File(dir,
						children[i]));
				if (!success)
					return false;
			}
		}
		return dir.delete();
	}

	public static void DeleteFilesOnUpgrade() {
		String[] files = Globals.DeleteFilesOnUpgrade.split(" ");
		for (String path : files) {
			if (path.equals(""))
				continue;
			File f = new File(Globals.DataDir + "/" + path);
			if (!f.exists())
				continue;
			deleteRecursively(f);
		}
	}

	public static void DeleteSdlConfigOnUpgradeAndRestart(final FFPlayer p) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(
					p.openFileOutput(SettingsFileName,
							p.MODE_WORLD_READABLE));
			out.writeInt(-1);
			out.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		new File(p.getFilesDir() + "/" + SettingsFileName).delete();
		PendingIntent intent = PendingIntent.getActivity(p, 0,
				new Intent(p.getIntent()), p.getIntent().getFlags());
		AlarmManager mgr = (AlarmManager) p
				.getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, intent);
		System.exit(0);
	}

	// ===============================================================================================

	public static void Apply(Context mContext) {
		nativeSetVideoDepth(Globals.VideoDepthBpp, Globals.NeedGles2 ? 1
				: 0);
		if (Globals.VideoLinearFilter)
			nativeSetVideoLinearFilter();
		if (Globals.CompatibilityHacksVideo) {
			Globals.MultiThreadedVideo = true;
			Globals.SwVideoMode = true;
			nativeSetCompatibilityHacks();
		}
		if (Globals.SwVideoMode)
			nativeSetVideoForceSoftwareMode();
		if (Globals.SwVideoMode && Globals.MultiThreadedVideo)
			nativeSetVideoMultithreaded();
		if (Globals.PhoneHasTrackball)
			nativeSetTrackballUsed();
		if (Globals.AppUsesMouse)
			nativeSetMouseUsed(Globals.RightClickMethod,
					Globals.ShowScreenUnderFinger, Globals.LeftClickMethod,
					Globals.MoveMouseWithJoystick ? 1 : 0,
					Globals.ClickMouseWithDpad ? 1 : 0,
					Globals.ClickScreenPressure,
					Globals.ClickScreenTouchspotSize,
					Globals.MoveMouseWithJoystickSpeed,
					Globals.MoveMouseWithJoystickAccel,
					Globals.LeftClickKey, Globals.RightClickKey,
					Globals.LeftClickTimeout, Globals.RightClickTimeout,
					Globals.RelativeMouseMovement ? 1 : 0,
					Globals.RelativeMouseMovementSpeed,
					Globals.RelativeMouseMovementAccel,
					Globals.ShowMouseCursor ? 1 : 0);
		if (Globals.AppUsesJoystick
				&& (Globals.UseAccelerometerAsArrowKeys || Globals.UseTouchscreenKeyboard))
			nativeSetJoystickUsed();
		if (Globals.AppUsesAccelerometer)
			nativeSetAccelerometerUsed();
		if (Globals.AppUsesMultitouch)
			nativeSetMultitouchUsed();
		nativeSetAccelerometerSettings(Globals.AccelerometerSensitivity,
				Globals.AccelerometerCenterPos);
		nativeSetTrackballDampening(Globals.TrackballDampening);
		if (Globals.UseTouchscreenKeyboard) {
			boolean screenKbReallyUsed = false;
			for (int i = 0; i < Globals.ScreenKbControlsShown.length; i++)
				if (Globals.ScreenKbControlsShown[i])
					screenKbReallyUsed = true;
			if (screenKbReallyUsed) {
				nativeSetTouchscreenKeyboardUsed();
				nativeSetupScreenKeyboard(Globals.TouchscreenKeyboardSize,
						Globals.TouchscreenKeyboardDrawSize,
						Globals.TouchscreenKeyboardTheme,
						Globals.AppTouchscreenKeyboardKeysAmountAutoFire,
						Globals.TouchscreenKeyboardTransparency);
				SetupTouchscreenKeyboardGraphics(mContext);
				for (int i = 0; i < Globals.ScreenKbControlsShown.length; i++)
					nativeSetScreenKbKeyUsed(i,
							Globals.ScreenKbControlsShown[i] ? 1 : 0);
				for (int i = 0; i < Globals.RemapScreenKbKeycode.length; i++)
					nativeSetKeymapKeyScreenKb(
							i,
							SDL_Keys.values[Globals.RemapScreenKbKeycode[i]]);
				for (int i = 0; i < Globals.ScreenKbControlsLayout.length; i++)
					if (Globals.ScreenKbControlsLayout[i][0] < Globals.ScreenKbControlsLayout[i][2])
						nativeSetScreenKbKeyLayout(i,
								Globals.ScreenKbControlsLayout[i][0],
								Globals.ScreenKbControlsLayout[i][1],
								Globals.ScreenKbControlsLayout[i][2],
								Globals.ScreenKbControlsLayout[i][3]);
			} else
				Globals.UseTouchscreenKeyboard = false;
		}

		for (int i = 0; i < SDL_Keys.JAVA_KEYCODE_LAST; i++)
			nativeSetKeymapKey(i,
					SDL_Keys.values[Globals.RemapHwKeycode[i]]);
		for (int i = 0; i < Globals.RemapMultitouchGestureKeycode.length; i++)
			nativeSetKeymapKeyMultitouchGesture(
					i,
					Globals.MultitouchGesturesUsed[i] ? SDL_Keys.values[Globals.RemapMultitouchGestureKeycode[i]]
							: 0);
		nativeSetMultitouchGestureSensitivity(Globals.MultitouchGestureSensitivity);
		if (Globals.TouchscreenCalibration[2] > Globals.TouchscreenCalibration[0])
			nativeSetTouchscreenCalibration(
					Globals.TouchscreenCalibration[0],
					Globals.TouchscreenCalibration[1],
					Globals.TouchscreenCalibration[2],
					Globals.TouchscreenCalibration[3]);

		String lang = new String(Locale.getDefault().getLanguage());
		if (Locale.getDefault().getCountry().length() > 0)
			lang = lang + "_" + Locale.getDefault().getCountry();
		System.out.println("libSDL: setting envvar LANGUAGE to '" + lang
				+ "'");
		nativeSetEnv("LANG", lang);
		nativeSetEnv("LANGUAGE", lang);
		// TODO: get current user name and set envvar USER, the API is not
		// availalbe on Android 1.6 so I don't bother with this
		nativeSetEnv("APPDIR", mContext.getFilesDir().getAbsolutePath());
		nativeSetEnv("SECURE_STORAGE_DIR", mContext.getFilesDir()
				.getAbsolutePath());
		nativeSetEnv("DATADIR", Globals.DataDir);
		nativeSetEnv("UNSECURE_STORAGE_DIR", Globals.DataDir);
		nativeSetEnv("HOME", Globals.DataDir);
		nativeSetEnv("ANDROID_VERSION",
				String.valueOf(android.os.Build.VERSION.SDK_INT));
		try {
			DisplayMetrics dm = new DisplayMetrics();
			((Activity) mContext).getWindowManager().getDefaultDisplay()
					.getMetrics(dm);
			float xx = dm.widthPixels / dm.xdpi;
			float yy = dm.heightPixels / dm.ydpi;
			float x = Math.max(xx, yy);
			float y = Math.min(xx, yy);
			float displayInches = (float) Math.sqrt(x * x + y * y);
			nativeSetEnv("DISPLAY_SIZE", String.valueOf(displayInches));
			nativeSetEnv("DISPLAY_SIZE_MM",
					String.valueOf((int) (displayInches * 25.4f)));
			nativeSetEnv("DISPLAY_WIDTH", String.valueOf(x));
			nativeSetEnv("DISPLAY_HEIGHT", String.valueOf(y));
			nativeSetEnv("DISPLAY_WIDTH_MM",
					String.valueOf((int) (x * 25.4f)));
			nativeSetEnv("DISPLAY_HEIGHT_MM",
					String.valueOf((int) (y * 25.4f)));
			nativeSetEnv("DISPLAY_RESOLUTION_WIDTH", String.valueOf(Math
					.max(dm.widthPixels, dm.heightPixels)));
			nativeSetEnv("DISPLAY_RESOLUTION_HEIGHT", String.valueOf(Math
					.min(dm.widthPixels, dm.heightPixels)));
		} catch (Exception eeeee) {
		}
	}

	static byte[] loadRaw(Activity p, int res) {
		byte[] buf = new byte[65536 * 2];
		byte[] a = new byte[65536 * 4 * 10]; // We need 2363516 bytes for
												// the Sun theme
		int written = 0;
		try {
			InputStream is = new GZIPInputStream(p.getResources()
					.openRawResource(res));
			int readed = 0;
			while ((readed = is.read(buf)) >= 0) {
				if (written + readed > a.length) {
					byte[] b = new byte[written + readed];
					System.arraycopy(a, 0, b, 0, written);
					a = b;
				}
				System.arraycopy(buf, 0, a, written, readed);
				written += readed;
			}
		} catch (Exception e) {
		}
		;
		byte[] b = new byte[written];
		System.arraycopy(a, 0, b, 0, written);
		return b;
	}

	public static void SetupTouchscreenKeyboardGraphics(Context context) {
		if (Globals.UseTouchscreenKeyboard) {
			if (Globals.TouchscreenKeyboardTheme < 0)
				Globals.TouchscreenKeyboardTheme = 0;
			if (Globals.TouchscreenKeyboardTheme > 2)
				Globals.TouchscreenKeyboardTheme = 2;

			if (Globals.TouchscreenKeyboardTheme == 0) {
				nativeSetupScreenKeyboardButtons(loadRaw(
						(Activity) context, R.raw.ultimatedroid));
			}
			if (Globals.TouchscreenKeyboardTheme == 1) {
				nativeSetupScreenKeyboardButtons(loadRaw(
						(Activity) context, R.raw.simpletheme));
			}
			if (Globals.TouchscreenKeyboardTheme == 2) {
				nativeSetupScreenKeyboardButtons(loadRaw(
						(Activity) context, R.raw.sun));
			}
		}
	}

	abstract static class SdcardAppPath {
		private static Settings.SdcardAppPath get() {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO)
				return Froyo.Holder.sInstance;
			else
				return Dummy.Holder.sInstance;
		}

		public abstract String path(final FFPlayer p);

		public static String deprecatedPath(final FFPlayer p) {
			return Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/app-data/" + p.getPackageName();
		}

		public static String getPath(final FFPlayer p) {
			try {
				return get().path(p);
			} catch (Exception e) {
			}
			return Dummy.Holder.sInstance.path(p);
		}

		public static class Froyo extends Settings.SdcardAppPath {
			private static class Holder {
				private static final SdcardAppPath.Froyo sInstance = new Froyo();
			}

			public String path(final FFPlayer p) {
				return p.getExternalFilesDir(null).getAbsolutePath();
			}
		}

		public static class Dummy extends Settings.SdcardAppPath {
			private static class Holder {
				private static final SdcardAppPath.Dummy sInstance = new Dummy();
			}

			public String path(final FFPlayer p) {
				return Environment.getExternalStorageDirectory()
						.getAbsolutePath()
						+ "/Android/data/"
						+ p.getPackageName() + "/files";
			}
		}
	}

	static boolean checkRamSize(final FFPlayer p) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					"/proc/meminfo"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.indexOf("MemTotal:") == 0) {
					String[] fields = line.split("[ \t]+");
					Long size = Long.parseLong(fields[1]);
					System.out.println("Device RAM size: " + size / 1024
							+ " Mb, required minimum RAM: "
							+ Globals.AppMinimumRAM + " Mb");
					if (size / 1024 < Globals.AppMinimumRAM) {
						settingsChanged = true;
						AlertDialog.Builder builder = new AlertDialog.Builder(
								p);
						builder.setTitle(R.string.not_enough_ram);
						builder.setMessage(p.getResources().getString(
								R.string.not_enough_ram_size,
								Globals.AppMinimumRAM, (int) (size / 1024)));
						builder.setPositiveButton(p.getResources()
								.getString(R.string.ok),
								new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialog, int item) {
										p.startActivity(new Intent(
												Intent.ACTION_VIEW,
												Uri.parse("market://details?id="
														+ p.getPackageName())));
										System.exit(0);
									}
								});
						builder.setNegativeButton(p.getResources()
								.getString(R.string.ignore),
								new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialog, int item) {
										showConfig(p, true);
										return;
									}
								});
						builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
							public void onCancel(DialogInterface dialog) {
								p.startActivity(new Intent(
										Intent.ACTION_VIEW,
										Uri.parse("market://details?id="
												+ p.getPackageName())));
								System.exit(0);
							}
						});
						final AlertDialog alert = builder.create();
						alert.setOwnerActivity(p);
						alert.show();
						return false;
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error: cannot parse /proc/meminfo: "
					+ e.toString());
		}
		return true;
	}

	private static native void nativeSetTrackballUsed();

	private static native void nativeSetTrackballDampening(int value);

	private static native void nativeSetAccelerometerSettings(
			int sensitivity, int centerPos);

	private static native void nativeSetMouseUsed(int RightClickMethod,
			int ShowScreenUnderFinger, int LeftClickMethod,
			int MoveMouseWithJoystick, int ClickMouseWithDpad,
			int MaxForce, int MaxRadius, int MoveMouseWithJoystickSpeed,
			int MoveMouseWithJoystickAccel, int leftClickKeycode,
			int rightClickKeycode, int leftClickTimeout,
			int rightClickTimeout, int relativeMovement,
			int relativeMovementSpeed, int relativeMovementAccel,
			int showMouseCursor);

	private static native void nativeSetJoystickUsed();

	private static native void nativeSetAccelerometerUsed();

	private static native void nativeSetMultitouchUsed();

	private static native void nativeSetTouchscreenKeyboardUsed();

	private static native void nativeSetVideoLinearFilter();

	private static native void nativeSetVideoDepth(int bpp, int gles2);

	private static native void nativeSetCompatibilityHacks();

	private static native void nativeSetVideoMultithreaded();

	private static native void nativeSetVideoForceSoftwareMode();

	private static native void nativeSetupScreenKeyboard(int size,
			int drawsize, int theme, int nbuttonsAutoFire, int transparency);

	private static native void nativeSetupScreenKeyboardButtons(byte[] img);

	private static native void nativeInitKeymap();

	private static native int nativeGetKeymapKey(int key);

	private static native void nativeSetKeymapKey(int javakey, int key);

	private static native int nativeGetKeymapKeyScreenKb(int keynum);

	private static native void nativeSetKeymapKeyScreenKb(int keynum,
			int key);

	private static native void nativeSetScreenKbKeyUsed(int keynum, int used);

	private static native void nativeSetScreenKbKeyLayout(int keynum,
			int x1, int y1, int x2, int y2);

	private static native int nativeGetKeymapKeyMultitouchGesture(int keynum);

	private static native void nativeSetKeymapKeyMultitouchGesture(
			int keynum, int key);

	private static native void nativeSetMultitouchGestureSensitivity(
			int sensitivity);

	private static native void nativeSetTouchscreenCalibration(int x1,
			int y1, int x2, int y2);

	public static native void nativeSetEnv(final String name,
			final String value);

	public static native int nativeChmod(final String name, int mode);
}