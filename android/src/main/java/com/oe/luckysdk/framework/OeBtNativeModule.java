package com.oe.luckysdk.framework;

import javax.annotation.Nullable;

import java.util.List;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanSettings;
import android.content.ComponentName;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import com.csr.csrmesh2.ConfigModelApi;
import com.csr.csrmesh2.DeviceInfo;
import com.csr.csrmesh2.MeshConstants;
import com.csr.csrmesh2.MeshService;
import com.csr.csrmesh2.LightModelApi;
import com.csr.csrmesh2.PowerModelApi;
import com.csr.csrmesh2.PowerState;
import com.csr.csrmesh2.TimeModelApi;

import com.oe.luckysdk.UiUtil;
import com.oe.luckysdk.framework.Manager;
import com.oe.luckysdk.framework.NetworkConfig;
import com.oe.luckysdk.framework.UniId;
// import com.oe.luckysdk.framework.Prot;
import com.oe.luckysdk.utils.Hex;
import com.oe.luckysdk.utils.TaskPool;
import com.oe.luckysdk.utils.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.oe.luckysdk.framework.OeBtPackage.TAG;

public class OeBtNativeModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {

    // Debugging
    private static final boolean D = true;

    private static final int ACCESS_COARSE_LOCATION_RESULT_CODE = 4;
    private static final int BLUETOOTH_RESULT_CODE = 5;
    private static final int STORAGE_RESULT_CODE = 6;

    private static int MIN_DEVICE_ID = 0x8000;

    // Event names
    public static final String BT_ENABLED = "bluetoothEnabled";
    public static final String BT_DISABLED = "bluetoothDisabled";
    public static final String SERVICE_CONNECTED = "serviceConnected";
    public static final String SERVICE_DISCONNECTED = "serviceDisconnected";
    public static final String NOTIFICATION_ONLINE_STATUS = "notificationOnlineStatus";
    public static final String NOTIFICATION_GET_DEVICE_STATE = "notificationGetDeviceState";
    public static final String DEVICE_STATUS_CONNECTING = "deviceStatusConnecting";
    public static final String DEVICE_STATUS_CONNECTED = "deviceStatusConnected";
    public static final String DEVICE_STATUS_LOGINING = "deviceStatusLogining";
    public static final String DEVICE_STATUS_LOGIN = "deviceStatusLogin";
    public static final String DEVICE_STATUS_LOGOUT = "deviceStatusLogout";
    public static final String DEVICE_STATUS_ERROR_N = "deviceStatusErrorAndroidN";
    public static final String DEVICE_STATUS_UPDATE_MESH_COMPLETED = "deviceStatusUpdateMeshCompleted";
    public static final String DEVICE_STATUS_UPDATING_MESH = "deviceStatusUpdatingMesh";
    public static final String DEVICE_STATUS_UPDATE_MESH_FAILURE = "deviceStatusUpdateMeshFailure";
    public static final String DEVICE_STATUS_UPDATE_ALL_MESH_COMPLETED = "deviceStatusUpdateAllMeshCompleted";
    public static final String DEVICE_STATUS_GET_LTK_COMPLETED = "deviceStatusGetLtkCompleted";
    public static final String DEVICE_STATUS_GET_LTK_FAILURE = "deviceStatusGetLtkFailure";
    public static final String DEVICE_STATUS_MESH_OFFLINE = "deviceStatusMeshOffline";
    public static final String DEVICE_STATUS_MESH_SCAN_COMPLETED = "deviceStatusMeshScanCompleted";
    public static final String DEVICE_STATUS_MESH_SCAN_TIMEOUT = "deviceStatusMeshScanTimeout";
    public static final String DEVICE_STATUS_OTA_COMPLETED = "deviceStatusOtaCompleted";
    public static final String DEVICE_STATUS_OTA_FAILURE = "deviceStatusOtaFailure";
    public static final String DEVICE_STATUS_OTA_PROGRESS = "deviceStatusOtaProgress";
    public static final String DEVICE_STATUS_GET_FIRMWARE_COMPLETED = "deviceStatusGetFirmwareCompleted";
    public static final String DEVICE_STATUS_GET_FIRMWARE_FAILURE = "deviceStatusGetFirmwareFailure";
    public static final String DEVICE_STATUS_DELETE_COMPLETED = "deviceStatusDeleteCompleted";
    public static final String DEVICE_STATUS_DELETE_FAILURE = "deviceStatusDeleteFailure";
    public static final String LE_SCAN = "leScan";
    public static final String LE_SCAN_COMPLETED = "leScanCompleted";
    public static final String LE_SCAN_TIMEOUT = "leScanTimeout";
    public static final String MESH_OFFLINE = "meshOffline";

    // Members
    private static OeBtNativeModule mThis;
    private MeshService mService;
    private BluetoothAdapter mBluetoothAdapter;
    private ReactApplicationContext mReactContext;
    protected Context mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    protected boolean isInited = false;
    protected boolean isServiceStarted = false;
    protected int reqId = -1;
    protected WritableArray mDhmKey = Arguments.createArray();

    // Patch
    private String mPatchConfigNodeOldName;

    // Promises
    private Promise mConfigNodePromise;

    public OeBtNativeModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mThis = this;
        mReactContext = reactContext;
        mContext = mReactContext.getApplicationContext();
    }

    @Override
    public String getName() {
        return "OeBt";
    }

    public static OeBtNativeModule getInstance() {
        return mThis;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
        if (D) Log.d(TAG, "On activity result request: " + requestCode + ", result: " + resultCode);
        // if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
        //     if (resultCode == Activity.RESULT_OK) {
        //         if (D) Log.d(TAG, "User enabled Bluetooth");
        //         if (mEnabledPromise != null) {
        //             mEnabledPromise.resolve(true);
        //         }
        //     } else {
        //         if (D) Log.d(TAG, "User did *NOT* enable Bluetooth");
        //         if (mEnabledPromise != null) {
        //             mEnabledPromise.reject(new Exception("User did not enable Bluetooth"));
        //         }
        //     }
        //     mEnabledPromise = null;
        // }

        // if (requestCode == REQUEST_PAIR_DEVICE) {
        //     if (resultCode == Activity.RESULT_OK) {
        //         if (D) Log.d(TAG, "Pairing ok");
        //     } else {
        //         if (D) Log.d(TAG, "Pairing failed");
        //     }
        // }
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (D) Log.d(TAG, "On new intent");
    }


    @Override
    public void onHostResume() {
        if (D) Log.d(TAG, "Host resume");
        if (isInited) {
            this.doResume();
        }
    }

    @Override
    public void onHostPause() {
        if (D) Log.d(TAG, "Host pause");
        if (isInited) {
            this.doPause();
        }
    }

    @Override
    public void onHostDestroy() {
        if (D) Log.d(TAG, "Host destroy");
        // APP 切到后台时也会调用此处，导致切回前台 Resume 时无法再正常使用本组件，因此使不在此处调用 doDestroy
        // if (isInited) {
        //     this.doDestroy();
        // }
    }

    @Override
    public void onCatalystInstanceDestroy() {
        if (D) Log.d(TAG, "Catalyst instance destroyed");
        super.onCatalystInstanceDestroy();
        if (isInited) {
            this.doDestroy();
        }
    }


    @ReactMethod
    public void doInit() {
if (D) Log.d(TAG, "isInited: " + isInited);
        if (!isInited) {
            isInited = true;
        }

        boolean permBle = false, permLoc = false;
        permBle = ContextCompat.checkSelfPermission(getCurrentActivity(), Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                      && ContextCompat.checkSelfPermission(getCurrentActivity(), Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
        permLoc = ContextCompat.checkSelfPermission(getCurrentActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                      || ContextCompat.checkSelfPermission(getCurrentActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!permBle || !permLoc){
            ActivityCompat.requestPermissions(getCurrentActivity(),new String[]{
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            }, 10000);
        }

        // checkLocation();

        // if (mBluetoothAdapter == null) {
        //     mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // }

        // if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
        //     sendEvent(BT_ENABLED);
        // } else {
        //     sendEvent(BT_DISABLED);
        // }

        mReactContext.addActivityEventListener(this);
        mReactContext.addLifecycleEventListener(this);
        // registerBluetoothStateReceiver();

        Manager.autoCreateNetwork = false;
        Manager.context = getCurrentActivity();
        UiUtil.setContext(getCurrentActivity());
        Manager.setSdkKey("ad8b9266551eb826");
        Manager.inst().setAutoUploadAndSync(false);     // 我们自己会在 JS 层与我们自己的后端服务器进行数据同步，因此不需要 OE 的同步功能
        // Manager.inst().clearFirstUITip();
        sendEvent(SERVICE_CONNECTED);
    }

    @ReactMethod
    public void doDestroy() {
        if (isInited) {
            mHandler.removeCallbacksAndMessages(null);
            isInited = false;
        }

        // TODO: unregisterReceiver(bluetoothStateReceiver);
    }

    @ReactMethod
    protected void doResume() {
        //检查是否支持蓝牙设备
        // if (!LeBluetooth.getInstance().isSupport(mContext)) {
        //     Toast.makeText(mContext, "ble not support", Toast.LENGTH_SHORT).show();
        //     return;
        // }

        // if (!LeBluetooth.getInstance().isEnabled()) {
        //     AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //     builder.setMessage("开启蓝牙，体验智能灯!");
        //     builder.setNeutralButton("cancel", new DialogInterface.OnClickListener() {
        //         @Override
        //         public void onClick(DialogInterface dialog, int which) {
        //             finish();
        //         }
        //     });
        //     builder.setNegativeButton("enable", new DialogInterface.OnClickListener() {
        //         @Override
        //         public void onClick(DialogInterface dialog, int which) {
        //             LeBluetooth.getInstance().enable(getApplicationContext());
        //         }
        //     });
        //     builder.show();
        // }

//         checkPermissions();
//         checkAvailability();

        Log.d(TAG, "onResume");

        Manager.inst().lsnrs.addLsnr(lsnr);
        // Manager.inst().checkConnect();
        // TaskPool.DefSeqTaskPool().PushCycTask(autoConn, 16 * 1000, 3 * 1000);
    }


    @ReactMethod
    protected void doPause() {
        // TaskPool.DefSeqTaskPool().CancelCycTask(autoConn);
        Manager.inst().lsnrs.remLsnr(lsnr);
    }

    // Runnable autoConn = () -> {
    //     Manager.inst().checkConnect();
    // };

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getCurrentActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getCurrentActivity(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        ACCESS_COARSE_LOCATION_RESULT_CODE);
            }
            else if (ContextCompat.checkSelfPermission(getCurrentActivity(),
                    Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getCurrentActivity(),
                        new String[]{Manifest.permission.BLUETOOTH},
                        BLUETOOTH_RESULT_CODE);
            }
            else if (ContextCompat.checkSelfPermission(getCurrentActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getCurrentActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_RESULT_CODE);
            }
            else {
                Log.d(TAG, "checkPermissions ok");
            }
        }
    }

    //---- BT and BLE
    @TargetApi(18)
    private void checkAvailability() {
        if(Build.VERSION.SDK_INT < 18) {
            Log.d(TAG, "Bluetooth LE not supported by this device");
        } else if(!mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            Log.d(TAG, "Bluetooth LE not supported by this device");
        } else {
            if(((BluetoothManager)mContext.getSystemService(mContext.BLUETOOTH_SERVICE)).getAdapter().isEnabled())
                Log.d(TAG, "Bluetooth is enabled");
            else
                Log.d(TAG, "Bluetooth is not enabled!");
        }
    }

    public void checkLocation() {
        if (Build.VERSION.SDK_INT >=23) {
            LocationManager lm = null;
            boolean gps_enabled = false;
            boolean network_enabled = false;

            lm = (LocationManager) getCurrentActivity().getSystemService(mContext.LOCATION_SERVICE);
            // exceptions will be thrown if provider is not permitted.
            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                Log.d(TAG, "gps_enabled: " + gps_enabled);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                Log.d(TAG, "network_enabled:" + network_enabled);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (gps_enabled == false || network_enabled == false) {
                // Show our settings alert and let the use turn on the GPS/Location
                // showBTStatusDialog(false);
            }


        }
    }

    @ReactMethod
    public void notModeAutoConnectMesh(Promise promise) {
        promise.resolve(true);
    }

    @ReactMethod
    public void setNetworkPassPhrase(String config) {
        Manager.inst().restoreNetworkConfig(config, true);
Log.d(TAG, "NetworkListSize: " + Manager.inst().getLocalNetworkList().size());

        // if (Manager.inst().getLocalNetworkList().size() == 0)
        // {
        //     Manager.inst().createNetwork("foobar");
        //     Manager.NetworkInfo info = Manager.inst().getCurrNetInfo();
        //     String str = Manager.inst().getNetworkConfig(info.account, info.password, info.network);
        //     Log.d(TAG, "network: " + str);
        // }

// Log.d(TAG, "network: " + Manager.inst().getCurrNetInfo());
// Manager.inst().getNetworkConfig(account, password, networkName)
        // if (Manager.inst().getLocalNetworkList().size() > 0 && Manager.inst().getGroups().size() == 0)
        //     Manager.inst().addGroup("My first group");
    }

    @ReactMethod
    private void autoConnect() {
        if (Manager.inst().isConnected()) {
            sendEvent(DEVICE_STATUS_LOGIN);
        } else {
            sendEvent(DEVICE_STATUS_LOGOUT);
            Manager.inst().checkConnect();
        }
    }

    @ReactMethod
    private void autoRefreshNotify(int repeatCount, int Interval) {
        // LeRefreshNotifyParameters refreshNotifyParams = Parameters.createRefreshNotifyParameters();
        // refreshNotifyParams.setRefreshRepeatCount(repeatCount);
        // refreshNotifyParams.setRefreshInterval(Interval);

        // TelinkLightService.Instance().autoRefreshNotify(refreshNotifyParams);
    }

    @ReactMethod
    private void idleMode(boolean disconnect) {
    }

    @ReactMethod
    private void startScan(int timeoutSeconds) {
        if (Manager.inst().isConnected()) {
            Log.d(TAG, "startScan timeoutSeconds: " + timeoutSeconds);
            Manager.inst().scanNewDevice((long) (timeoutSeconds * 1000));
        }
    }

    @ReactMethod
    private void changePower(String devJson, int value) {
        try {
            NetworkConfig.Device dev = NetworkConfig.Device.a(new JSONObject(devJson));
            Manager.inst().onOff(value == 1 ? true : false, dev);
        } catch (JSONException localJSONException) {
            localJSONException.printStackTrace();
        }
    }

    @ReactMethod
    private void changeBrightness(String devJson, int value) {
        try {
            NetworkConfig.Device dev = NetworkConfig.Device.a(new JSONObject(devJson));
            Util.UIColor color = new Util.UIColor(0, 0, 0);
            Util.CoolWarm cw = new Util.CoolWarm(0, value / 255.0D);
            Manager.inst().hsbtb(color, cw, -1, dev);
        } catch (JSONException localJSONException) {
            localJSONException.printStackTrace();
        }
    }

    @ReactMethod
    private void changeColorTemp(String devJson, int value) {
        try {
            NetworkConfig.Device dev = NetworkConfig.Device.a(new JSONObject(devJson));
            Util.UIColor color = new Util.UIColor(0, 0, 0);
            Util.CoolWarm cw = new Util.CoolWarm(value / 255.0D, 0.5);
            Manager.inst().hsbtb(color, cw, 0.5, dev);
        } catch (JSONException localJSONException) {
            localJSONException.printStackTrace();
        }
    }

    @ReactMethod
    private void changeColor(String devJson, int value) {
        // try {
        //     NetworkConfig.Device dev = NetworkConfig.Device.a(new JSONObject(devJson));
        //     Util.UIColor color = new Util.UIColor(0, 0, 0);
        //     Util.CoolWarm cw = new Util.CoolWarm(value / 255.0D, 0.5);
        //     Manager.inst().hsbtb(color, cw, 0.5, dev);
        // } catch (JSONException localJSONException) {
        //     localJSONException.printStackTrace();
        // }
    }

    @ReactMethod
    private void configNode(String devJson, boolean isToClaim, Promise promise) {
        mConfigNodePromise = promise;
        try {
            NetworkConfig.Device dev = NetworkConfig.Device.a(new JSONObject(devJson));
            if (isToClaim) {
                Manager.inst().associateDevice(dev);
            } else {
                Manager.inst().resetAndDelDevice(dev, false);
            }
        } catch (JSONException localJSONException) {
            localJSONException.printStackTrace();
        }
    }

    private void onUpdateMeshCompleted(NetworkConfig.Device dev) {
        if (D) Log.d(TAG, "onUpdateMeshCompleted");
        if (mConfigNodePromise != null) {
            WritableMap params = Arguments.createMap();
            params.putString("dhmKey", Hex.encodeHexStr(dev.c));
            params.putInt("type", dev.b);
            mConfigNodePromise.resolve(params);
        }
        mConfigNodePromise = null;
    }

    private void onUpdateMeshFailure() {
        if (D) Log.d(TAG, "onUpdateMeshFailure");
        if (mConfigNodePromise != null) {
            mConfigNodePromise.reject(new Exception("onUpdateMeshFailure"));
        }
        mConfigNodePromise = null;
    }

    private void onGetDeviceStateNotify(NetworkConfig.Device dev) {
        WritableMap params = Arguments.createMap();
        params.putString("macAddress", dev.id.toString());
        params.putBoolean("onOff", dev.onOff);
        sendEvent(NOTIFICATION_GET_DEVICE_STATE, params);
    }

    private void onLeScan(NetworkConfig.Device dev) {
        WritableMap params = Arguments.createMap();
        params.putString("macAddress", dev.id.toString());
        params.putString("name", dev.name);
        params.putString("dhmKey", Hex.encodeHexStr(dev.c));
        params.putInt("type", dev.type());
        // params.putString("deviceName", deviceInfo.deviceName);
        // params.putString("meshName", deviceInfo.meshName);
        // params.putInt("meshAddress", deviceInfo.meshAddress);
        // params.putInt("meshUUID", uuid.toString());
        // params.putInt("productUUID", deviceInfo.productUUID);
        // params.putInt("status", deviceInfo.status);
        sendEvent(LE_SCAN, params);
    }

    Manager.ManagerLsnr lsnr = new Manager.ManagerLsnr() {
        @Override
        public void onDevFound(NetworkConfig.Device dev, double rssi) {
            Log.d(TAG, "Found device: " + dev);

            onLeScan(dev);
        }

        @Override
        public void onDevAddFailed(NetworkConfig.Device dev) {
            Log.d(TAG, "Add device failed: " + dev);
            onUpdateMeshFailure();
        }

        @Override
        public void onDevDelFailed(NetworkConfig.Device dev) {
            Log.d(TAG, "Delete device failed: " + dev);
            onUpdateMeshFailure();
        }

        @Override
        public void onDevAdd(final NetworkConfig.Device dev) {
            Log.d(TAG, "Added device: " + dev);

            onUpdateMeshCompleted(dev);

            // // // do some example control:
            // final Manager inst = Manager.inst();
            // // // send onOff.
            // // inst.onOff(false, n);
            // // // send rgb control: RGB = 0xFF0000
            // // inst.hsbtb(new Util.UIColor(0xFF, 0, 0), null, 0.0, n);
            // // // send cw control: CW = 0xFF00
            // // inst.hsbtb(null, new Util.CoolWarm(0xFF, 0), 0.0, n);

            // // reset/delete device if required.
            // if (false) inst.resetAndDelDevice(n, false);

            // // group item changes
            // if (false) {
            //     NetworkConfig.Group anyGroup = inst.getGroups().get(0); // set to your group.
            //     // add to group
            //     inst.addDeviceToGroup(n, anyGroup);
            //     // remove from group
            //     inst.delDeviceFromGroup(n, anyGroup);
            // }
        }

        @Override
        public void onDevDel(NetworkConfig.Device dev) {
            Log.d(TAG, "Deleted device: " + dev);
            onUpdateMeshCompleted(dev);
        }

        @Override
        public void onDevChanged(NetworkConfig.Device dev) {
            Log.d(TAG, "Device changed: " + dev);
            onGetDeviceStateNotify(dev);
        }

        // @Override
        // public void onPureData(int srcId, int destId, byte[] data) {
        //     Log.d(TAG, "Received pure data: { from: " + srcId + ", dest: " + destId + ", data: " + Hex.encodeHexStr(data) + "}.");
        // }

        // @Override
        // public void onReadCmd(int srcId, int destId, Prot.CmdRsp cmdRsp) {
        //     Log.d(TAG, "Received read cmd status: { from: " + srcId + ", dest: " + destId + ", cmdRsp: " + cmdRsp + "}.");
        // }

        @Override
        public void onGroupAdd(NetworkConfig.Group n) {
            Log.d(TAG, "Group added: " + n);
        }

        @Override
        public void onGroupDel(NetworkConfig.Group old) {
            Log.d(TAG, "Group deleted: " + old);
        }

        @Override
        public void onGroupChanged(NetworkConfig.Group changed) {
            Log.d(TAG, "Group changed: " + changed);
        }

        @Override
        public void onGroupItemChanged(NetworkConfig.Device dev, NetworkConfig.Group group, boolean isAdd, boolean succ) {
            if (succ) Log.d(TAG, "Group item changed: { dev: " + dev + ", group: " + group + ", isAdd: " + isAdd + ", succ: " + succ + " }");
            else Log.e(TAG, "Group item changed: { dev: " + dev + ", group: " + group + ", isAdd: " + isAdd + ", succ: " + succ + " }");
        }

        @Override
        public void onConnect(boolean isBleConn, boolean isWiFiConn) {
            if (Manager.inst().isConnected()) {
                sendEvent(DEVICE_STATUS_LOGIN);
            } else {
                sendEvent(DEVICE_STATUS_LOGOUT);
            }
        }

        @Override
        public void onNetworkCreate(Manager.NetworkInfo info) {
            Log.d(TAG, "Created network: " + info);
            // Manager.inst().switchToLocalNetwork(info.account, info.network);
        }

        @Override
        public void onNetworkDel(Manager.NetworkInfo info) {
            Log.d(TAG, "Deleted network: " + info);
        }

        @Override
        public void onNetworkListChanged() {
            Log.d(TAG, "Network list changed.");
        }

        @Override
        public void onRecentChanged() { }
    };

    /*********************/
    /** Private methods **/
    /*********************/

    /**
     * Check if is api level 19 or above
     * @return is above api level 19
     */
    private boolean isKitKatOrAbove () {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    /**
     * Send event to javascript
     * @param eventName Name of the event
     * @param params Additional params
     */
    public void sendEvent(String eventName, @Nullable WritableMap params) {
        if (mReactContext.hasActiveCatalystInstance()) {
            if (D) Log.d(TAG, "Sending event: " + eventName);
            mReactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
        }
    }

    public void sendEvent(String eventName, @Nullable WritableArray params) {
        if (mReactContext.hasActiveCatalystInstance()) {
            if (D) Log.d(TAG, "Sending event: " + eventName);
            mReactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
        }
    }

    public void sendEvent(String eventName) {
        if (mReactContext.hasActiveCatalystInstance()) {
            if (D) Log.d(TAG, "Sending event: " + eventName);
            mReactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, null);
        }
    }

    /**
     * Register receiver for bluetooth state change
     */
    private void registerBluetoothStateReceiver() {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            if (D) Log.d(TAG, "Bluetooth was disabled");
                            sendEvent(BT_DISABLED);
                            break;
                        case BluetoothAdapter.STATE_ON:
                            if (D) Log.d(TAG, "Bluetooth was enabled");
                            // autoConnect();
                            sendEvent(BT_ENABLED);
                            break;
                    }
                }
            }
        };

        mReactContext.registerReceiver(bluetoothStateReceiver, intentFilter);
    }
}
