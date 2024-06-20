package com.soiadmahedi.storagecard;

import android.Manifest;
import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.appbar.AppBarLayout;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.*;
import org.json.*;

public class MainActivity extends AppCompatActivity {
	
	private Toolbar _toolbar;
	private AppBarLayout _app_bar;
	private CoordinatorLayout _coordinator;
	private boolean start = false;
	
	private ArrayList<HashMap<String, Object>> listmap = new ArrayList<>();
	
	private LinearLayout linear1;
	private ListView listview;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.main);
		initialize(_savedInstanceState);
		
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
		|| ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
		} else {
			initializeLogic();
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1000) {
			initializeLogic();
		}
	}
	
	private void initialize(Bundle _savedInstanceState) {
		_app_bar = findViewById(R.id._app_bar);
		_coordinator = findViewById(R.id._coordinator);
		_toolbar = findViewById(R.id._toolbar);
		setSupportActionBar(_toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _v) {
				onBackPressed();
			}
		});
		linear1 = findViewById(R.id.linear1);
		listview = findViewById(R.id.listview);
		
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> _param1, View _param2, int _param3, long _param4) {
				final int _position = _param3;
				
			}
		});
	}
	
	private void initializeLogic() {
		
		if (android.os.Build.VERSION.SDK_INT >= 23) {
				if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) finishAffinity();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		_Refresh();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addDataScheme("file");
		registerReceiver(br, intentFilter);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(br);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	public void _Refresh() {
		try {
			DeviceManager dm = new DeviceManager(this);
			if (!dm.isSupport()) {
				Toast.makeText(getApplicationContext(), "Not Supported", Toast.LENGTH_SHORT).show();
			}
			else {
				listmap.clear();
				List<DeviceManager.Storage> list = dm.getStorage();
				for (DeviceManager.Storage s : list) {
					HashMap<String, Object> map = new HashMap<>();
					map.put("data", s);
					listmap.add(map);
				}
				listview.setAdapter(new ListviewAdapter(listmap));
				((BaseAdapter)listview.getAdapter()).notifyDataSetChanged();
			}
		}
		catch (Exception e) {
			showMessage(e.getMessage());
		}
	}
	
	
	public void _() {
	}
	public String _GetReadableSize(long size) {
		    if (size <= 0) return "0 B";
		    final String[] units = new String[] { "B", "kB", "MB", "GB", "TB", "EB" };
		    int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		    return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
	public static class DeviceManager {
			private Context context;
			public DeviceManager(Context context) {
					this.context = context;
			}
			public boolean isSupport() {
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) return true;
					return false;
			}
			public List<Storage> getStorage() throws Exception {
					if (!isSupport()) throw new Exception("Not Supported");
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Build.VERSION.SDK_INT <= Build.VERSION_CODES.M)
					return getStorageForApi9To23(this.context);
					else
					return getStorageForApi24AndAbove(this.context);
			}
			public List<Storage> getStorageForApi9To23(Context context) throws Exception {
					List<Storage> list = new ArrayList();
					EnvironmentSDCard.Device[] devices = EnvironmentSDCard.getDevices(context);
					for (EnvironmentSDCard.Device d : devices) {
							if (!d.isAvailable()) continue;
							java.io.File file = d.getDir();
							if (!file.exists()) continue;
							list.add(new Storage(
							file,
							d.isPrimary(),
							d.isRemovable(),
							d.isEmulated(),
							d.getUserLabel(),
							d.getUuid()
							));
					}
					return list;
			}
			public List<Storage> getStorageForApi24AndAbove(Context context) throws Exception {
					List<Storage> list = new ArrayList();
					android.os.storage.StorageManager sm = (android.os.storage.StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
					String[] vs = (String[])sm.getClass().getMethod("getVolumePaths", new Class[0]).invoke(sm, new Object[0]);
					List<android.os.storage.StorageVolume> svl = sm.getStorageVolumes();
					for (int i = 0; i < vs.length; i++) {
							android.os.storage.StorageVolume sv = svl.get(i);
							String volume = vs[i].toString();
							if (!android.os.Environment.MEDIA_MOUNTED.equals(sv.getState())) continue;
							java.io.File file = new java.io.File(volume);
							if (!file.exists()) continue;
							list.add(new Storage(
							file,
							sv.isPrimary(),
							sv.isRemovable(),
							sv.isEmulated(),
							sv.getDescription(context),
							sv.getUuid()
							));
					}
					return list;
			}
			
			public class Storage {
					java.io.File directory; String description, uuid;
					boolean isPrimary, isRemovable, isEmulated;
					public Storage(java.io.File directory, boolean isPrimary, boolean isRemovable, boolean isEmulated, String description, String uuid) {
							this.directory = directory;
							this.isPrimary = isPrimary;
							this.isRemovable = isRemovable;
							this.isEmulated = isEmulated;
							this.description = description;
							this.uuid = uuid;
					}
					public boolean isPrimary() { return this.isPrimary; }
					public boolean isRemovable() { return this.isRemovable; }
					public boolean isEmulated() { return this.isEmulated; }
					
					public java.io.File getDirectory() { return this.directory; }
					public String getDescription() { return this.description; }
					public String getUuid() { return this.uuid; }
					
					public long getFreeSpace() { return this.directory.getFreeSpace(); }
					public long getTotalSpace() { return this.directory.getTotalSpace(); }
					public long getUsedSpace() { return getTotalSpace() - getFreeSpace(); }
			}
			
			private static class EnvironmentSDCard {
					public final static String MEDIA_UNKNOWN = "unknown";
					
					public final static String TYPE_PRIMARY = "primary";
					public final static String TYPE_INTERNAL = "internal";
					public final static String TYPE_SD = "MicroSD";
					public final static String TYPE_USB = "USB";
					public final static String TYPE_UNKNOWN = "unknown";
					
					public final static String WRITE_NONE = "none";
					public final static String WRITE_READONLY = "readonly";
					public final static String WRITE_APPONLY = "apponly";
					public final static String WRITE_FULL = "readwrite";
					
					private static Device[] devices, externalstorage, storage;
					
					public static Device[] getDevices(android.content.Context context) {
							if (devices == null) initDevices(context);
							return devices;
					}
					
					public static Device[] getExternalStorage(android.content.Context context) {
							if (devices == null) initDevices(context);
							return externalstorage;
					}
					
					public static Device[] getStorage(android.content.Context context) {
							if (devices == null) initDevices(context);
							return storage;
					}
					
					public static void initDevices(android.content.Context context) {
							android.os.storage.StorageManager sm = (android.os.storage.StorageManager) context.getSystemService(android.content.Context.STORAGE_SERVICE);
							Class c = sm.getClass();
							Object[] vols;
							try {
									java.lang.reflect.Method m = c.getMethod("getVolumeList", null);
									vols = (Object[]) m.invoke(sm, null);
									Device[] temp = new Device[vols.length];
									for (int i = 0; i < vols.length; i++) temp[i] = new Device(vols[i]);
									Device primary = null;
									for (Device d : temp) if (d.mPrimary) primary = d;
									if (primary == null) for (Device d : temp)
									if (!d.mRemovable) {
											d.mPrimary = true;
											primary = d;
											break;
									}
									if (primary == null) {
											primary = temp[0];
											primary.mPrimary = true;
									}
									java.util.ArrayList<Device> tempDev = new java.util.ArrayList<Device>(10);
									java.util.ArrayList<Device> tempStor = new java.util.ArrayList<Device>(10);
									java.util.ArrayList<Device> tempExt = new java.util.ArrayList<Device>(10);
									for (Device d : temp) {
											tempDev.add(d);
											if (d.isAvailable()) {
													tempExt.add(d);
													tempStor.add(d);
											}
									}
									Device internal = new Device(context);
									tempStor.add(0, internal);
									if (!primary.mEmulated) tempDev.add(0, internal);
									devices = tempDev.toArray(new Device[tempDev.size()]);
									storage = tempStor.toArray(new Device[tempStor.size()]);
									externalstorage = tempExt.toArray(new Device[tempExt.size()]);
							} catch (Exception e) {
									throw new RuntimeException(e);
							}
							
					}
					
					public static class Device extends java.io.File {
							String mUserLabel, mUuid, mState, mWriteState, mType;
							boolean mPrimary, mRemovable, mEmulated;
							
							Device(android.content.Context context) {
									super(android.os.Environment.getDataDirectory().getAbsolutePath());
									mState = android.os.Environment.MEDIA_MOUNTED;
									mType = TYPE_INTERNAL;
									mWriteState = WRITE_APPONLY;
							}
							
							@SuppressWarnings("NullArgumentToVariableArgMethod")
							Device(Object storage) throws NoSuchMethodException, java.lang.reflect.InvocationTargetException, IllegalAccessException {
									super((String) storage.getClass().getMethod("getPath", null).invoke(storage, null));
									for (java.lang.reflect.Method m : storage.getClass().getMethods()) {
											if (m.getName().equals("getUserLabel") && m.getParameterTypes().length == 0 && m.getReturnType() == String.class)
											mUserLabel = (String) m.invoke(storage, null);
											if (m.getName().equals("getUuid") && m.getParameterTypes().length == 0 && m.getReturnType() == String.class)
											mUuid = (String) m.invoke(storage, null);
											if (m.getName().equals("getState") && m.getParameterTypes().length == 0 && m.getReturnType() == String.class)
											mState = (String) m.invoke(storage, null);
											if (m.getName().equals("isRemovable") && m.getParameterTypes().length == 0 && m.getReturnType() == boolean.class)
											mRemovable = (Boolean) m.invoke(storage, null);
											if (m.getName().equals("isPrimary") && m.getParameterTypes().length == 0 && m.getReturnType() == boolean.class)
											mPrimary = (Boolean) m.invoke(storage, null);
											if (m.getName().equals("isEmulated") && m.getParameterTypes().length == 0 && m.getReturnType() == boolean.class)
											mEmulated = (Boolean) m.invoke(storage, null);
									}
									if (mState == null) mState = getState();
									if (mPrimary) mType = TYPE_PRIMARY;
									else {
											String n = getAbsolutePath().toLowerCase();
											if (n.indexOf("sd") > 0) mType = TYPE_SD;
											else if (n.indexOf("usb") > 0) mType = TYPE_USB;
											else mType = TYPE_UNKNOWN + " " + getAbsolutePath();
									}
							}
							
							public boolean isAvailable() {
									String s = getState();
									return (android.os.Environment.MEDIA_MOUNTED.equals(s) || android.os.Environment.MEDIA_MOUNTED_READ_ONLY.equals(s));
							}
							public boolean isPrimary() {
									return mPrimary;
							}
							public boolean isRemovable() {
									return mRemovable;
							}
							public boolean isEmulated() {
									return mEmulated;
							}
							public String getUserLabel() {
									return mUserLabel;
							}
							public java.io.File getDir() {
									return new java.io.File(this, "");
							}		
							public String getUuid() {
									return mUuid;
							}
							public String getState() {
									if (mRemovable || mState == null) {
											if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
											mState = android.os.Environment.getExternalStorageState(this);
											else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
											mState = android.os.Environment.getStorageState(this);
											else if (canRead() && getTotalSpace() > 0)
											mState = android.os.Environment.MEDIA_MOUNTED;
											else if (mState == null || android.os.Environment.MEDIA_MOUNTED.equals(mState))
											mState = MEDIA_UNKNOWN;
									}
									return mState;
							}
							public String getType() {
									return mType;
							}
					}
					
			}
			
	}
	private BroadcastReceiver br = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
			        _Refresh();
			        //showMessage(intent.getAction());
			}
	};
	{
	}
	
	public class ListviewAdapter extends BaseAdapter {
		
		ArrayList<HashMap<String, Object>> _data;
		
		public ListviewAdapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@Override
		public int getCount() {
			return _data.size();
		}
		
		@Override
		public HashMap<String, Object> getItem(int _index) {
			return _data.get(_index);
		}
		
		@Override
		public long getItemId(int _index) {
			return _index;
		}
		
		@Override
		public View getView(final int _position, View _v, ViewGroup _container) {
			LayoutInflater _inflater = getLayoutInflater();
			View _view = _v;
			if (_view == null) {
				_view = _inflater.inflate(R.layout.list, null);
			}
			
			final LinearLayout linear2 = _view.findViewById(R.id.linear2);
			final ImageView imageview_icon = _view.findViewById(R.id.imageview_icon);
			final LinearLayout linear = _view.findViewById(R.id.linear);
			final TextView text = _view.findViewById(R.id.text);
			final TextView used_space = _view.findViewById(R.id.used_space);
			final TextView free_space = _view.findViewById(R.id.free_space);
			final TextView total_space = _view.findViewById(R.id.total_space);
			final LinearLayout linear1 = _view.findViewById(R.id.linear1);
			final ProgressBar progressbar = _view.findViewById(R.id.progressbar);
			final TextView percentage = _view.findViewById(R.id.percentage);
			
			try{
				final DeviceManager.Storage storage = (DeviceManager.Storage)_data.get(_position).get("data");
				if (storage.isPrimary() || storage.isRemovable() || storage.isEmulated()) {
					if (storage.isPrimary()) {
						text.setText("Phone Storage");
						imageview_icon.setImageResource(R.drawable.ic_phone_pic);
					}
					else {
						if (storage.isRemovable()) {
							String description = storage.getDescription();
							description = description != null ? description : "External Storage";
							text.setText(String.format("%s (%s)", description, storage.getUuid()));
							imageview_icon.setImageResource(R.drawable.ic_sd_card);
						}
						else {
							text.setText("Emulated Directory");
							imageview_icon.setImageResource(R.drawable.ic_sd_card);
						}
					}
					used_space.setText("Used Space : " + _GetReadableSize(storage.getUsedSpace()));
					free_space.setText("Free Space : " + _GetReadableSize(storage.getFreeSpace()));
					total_space.setText("Total Space : " + _GetReadableSize(storage.getTotalSpace()));
					final int p = (int)((storage.getUsedSpace() * 100) / storage.getTotalSpace());
					percentage.setText(p + "%");
					progressbar.setProgress((int)p);
				}
				else {
					text.setText("Unavailable Storage");
				}
				linear.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View _view) {
						Toast.makeText(getApplicationContext(), storage.getDirectory().getPath(), Toast.LENGTH_SHORT).show();
					}
				});
			}catch(Exception exception){
				text.setText(Log.getStackTraceString(exception));
			}
			
			return _view;
		}
	}
	
	@Deprecated
	public void showMessage(String _s) {
		Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
	}
	
	@Deprecated
	public int getLocationX(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[0];
	}
	
	@Deprecated
	public int getLocationY(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[1];
	}
	
	@Deprecated
	public int getRandom(int _min, int _max) {
		Random random = new Random();
		return random.nextInt(_max - _min + 1) + _min;
	}
	
	@Deprecated
	public ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
		ArrayList<Double> _result = new ArrayList<Double>();
		SparseBooleanArray _arr = _list.getCheckedItemPositions();
		for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
			if (_arr.valueAt(_iIdx))
			_result.add((double)_arr.keyAt(_iIdx));
		}
		return _result;
	}
	
	@Deprecated
	public float getDip(int _input) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
	}
	
	@Deprecated
	public int getDisplayWidthPixels() {
		return getResources().getDisplayMetrics().widthPixels;
	}
	
	@Deprecated
	public int getDisplayHeightPixels() {
		return getResources().getDisplayMetrics().heightPixels;
	}
}