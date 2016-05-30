package mrtech.smarthome.router;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import mrtech.smarthome.SmartHomeApp;
import mrtech.smarthome.router.Models.*;
import mrtech.smarthome.ipc.IPCManager;
import mrtech.smarthome.ipc.IPCamera;
import mrtech.smarthome.rpc.Messages;
import mrtech.smarthome.util.RequestUtil;
import rx.functions.Action1;
import rx.functions.Action2;

/**
 * Created by sphynx on 2015/12/25.
 */
class RouterCameraDataManager implements CameraDataManager {

    private static void trace(String msg) {
        if (SmartHomeApp.DEBUG)
            Log.d(RouterCameraDataManager.class.getName(), msg);
    }

    private final IPCManager mIPCManager;
    private final CommunicationManager mCommunicationManager;

    public RouterCameraDataManager(CommunicationManager communicationManager) {
        mIPCManager = IPCManager.createNewManager();
        mCommunicationManager = communicationManager;
    }

    @Override
    public IPCManager getIPCManager() {
        return mIPCManager;
    }

    @Override
    public void reloadIPCAsync(boolean cache, final Action1<Throwable> exception) {
        try {
            mCommunicationManager.postRequestAsync(RequestUtil.getDevices(mrtech.smarthome.rpc.Models.DeviceQuery.newBuilder()
                    .setType(mrtech.smarthome.rpc.Models.DeviceType.DEVICE_TYPE_CAMERA)
                    .setPage(0)
                    .setPageSize(100)
                    .build()), new Action2<Messages.Response, Throwable>() {
                @Override
                public void call(Messages.Response response, Throwable throwable) {
                    if (response != null) {
                        final List<mrtech.smarthome.rpc.Models.Device> result = response.getExtension(Messages.QueryDeviceResponse.response).getResultsList();
                        if (result != null && result.size() > 0) {
                            try {
                                mIPCManager.removeAll();
                                List<IPCamera> cameras= new ArrayList<>();
                                for (mrtech.smarthome.rpc.Models.Device device : result) {
                                    final mrtech.smarthome.rpc.Models.CameraDevice cameraDevice = device.getExtension(mrtech.smarthome.rpc.Models.CameraDevice.detail);
                                    if (cameraDevice == null || device.getType() != mrtech.smarthome.rpc.Models.DeviceType.DEVICE_TYPE_CAMERA) {
                                        throw new IllegalArgumentException("device must be type of camera");
                                    }
                                    cameras.add(new IPCamera(device, cameraDevice.getDeviceid(), cameraDevice.getUser(), cameraDevice.getPassword()));
                                }
                                mIPCManager.addCamera(cameras.toArray(new IPCamera[cameras.size()]));
                            } catch (Exception e) {
                                throwable = e;
                            }
                        } else {
                            throwable = new NoSuchElementException("未添加摄像头");
                        }
                    }
                    if (exception != null) exception.call(throwable);
                }
            }, cache);
        } catch (Exception ex) {
            if (exception != null) exception.call(ex);
        }
    }

    @Override
    public void saveCamera(final mrtech.smarthome.rpc.Models.Device device, final Action1<Throwable> result) {
        mCommunicationManager.postRequestAsync(RequestUtil.saveCamera(device),
                new Action2<Messages.Response, Throwable>() {
                    @Override
                    public void call(Messages.Response response, Throwable throwable) {
                        if (throwable == null && response != null) {
                            if (response.getErrorCode() != Messages.Response.ErrorCode.SUCCESS) {
                                throwable = new Exception(RouterManager.getErrorMessage(response.getErrorCode()));
                            }
                        }
                        if (result != null) {
                            result.call(throwable);
                        }
                    }
                });
    }

    @Override
    public void deleteCamera(final IPCamera camera, final Action1<Throwable> result) {
        if (camera == null || camera.getTag() == null) {
            if (result != null)
                result.call(new IllegalArgumentException("camera or camera tag null."));
        } else {
            mrtech.smarthome.rpc.Models.Device device = (mrtech.smarthome.rpc.Models.Device) camera.getTag();
            mCommunicationManager.postRequestAsync(RequestUtil.deleteDevice(device.getId()), new Action2<Messages.Response, Throwable>() {
                @Override
                public void call(final Messages.Response response, Throwable throwable) {
                    if (throwable == null && response != null) {
                        if (response.getErrorCode() != Messages.Response.ErrorCode.SUCCESS) {
                            throwable = new Exception(RouterManager.getErrorMessage(response.getErrorCode()));
                        } else {
                            mIPCManager.removeCamera(camera);
                        }
                    }
                    if (result != null) {
                        result.call(throwable);
                    }
                }
            });
        }
    }

//    public void searchCamera(final Action2<List<Models.Device>, Throwable> callback) {
//        try {
//            mCommunicationManager.postRequestAsync(RequestUtil.searchCamera(), new Action2<Messages.Response, Throwable>() {
//                @Override
//                public void call(Messages.Response response, Throwable throwable) {
//                    if (response == null || response.getErrorCode() != Messages.Response.ErrorCode.SUCCESS || throwable != null) {
//                        Throwable result;
//                        if (response == null) {
//                            result = throwable;
//                        } else {
//                            result = new Exception(response.getErrorCode().toString(), throwable);
//                        }
//                        callback.call(null, result);
//                    } else {
//                        final List<Models.Device> devicesList = response.getExtension(Messages.SearchCameraResponse.response).getDevicesList();
//                        callback.call(devicesList, null);
//                    }
//                }
//            });
//        } catch (Exception ex) {
//            callback.call(null, ex);
//        }
//    }

}
