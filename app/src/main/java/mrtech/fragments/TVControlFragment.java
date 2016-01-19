package mrtech.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import mrtech.activities.R;
import mrtech.smarthome.router.Models.CommunicationManager;
import mrtech.smarthome.router.Router;
import mrtech.smarthome.rpc.Messages;
import mrtech.smarthome.rpc.Models;
import mrtech.smarthome.util.RequestUtil;
import rx.functions.Action2;

public class TVControlFragment extends Fragment {

    private Models.Device mDevice;
    private Router mRouter;
    private Models.InfraredDevice mInfraredDevice;
    private CommunicationManager mCommunicationManager;

    public static TVControlFragment newInstance(Router router, Models.Device device) {
        final TVControlFragment fragment = new TVControlFragment();
        fragment.setDevice(device);
        fragment.setRouter(router);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tv_control, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initButtons();
    }

    private void initButtons() {
        binding(Models.TelevisionCommand.TELEVISION_0,R.id.num0);
        binding(Models.TelevisionCommand.TELEVISION_1,R.id.num1);
        binding(Models.TelevisionCommand.TELEVISION_2,R.id.num2);
        binding(Models.TelevisionCommand.TELEVISION_3,R.id.num3);
        binding(Models.TelevisionCommand.TELEVISION_4,R.id.num4);
        binding(Models.TelevisionCommand.TELEVISION_5,R.id.num5);
        binding(Models.TelevisionCommand.TELEVISION_6,R.id.num6);
        binding(Models.TelevisionCommand.TELEVISION_7,R.id.num7);
        binding(Models.TelevisionCommand.TELEVISION_8,R.id.num8);
        binding(Models.TelevisionCommand.TELEVISION_9,R.id.num9);
        binding(Models.TelevisionCommand.TELEVISION_AV_TV,R.id.source);
        binding(Models.TelevisionCommand.TELEVISION_CHANNEL_PLUS,R.id.channelPlus);
        binding(Models.TelevisionCommand.TELEVISION_CHANNEL_REDUCTION,R.id.channelMinus);
        binding(Models.TelevisionCommand.TELEVISION_COMBINATION,R.id.combination);
        binding(Models.TelevisionCommand.TELEVISION_UP,R.id.up);
        binding(Models.TelevisionCommand.TELEVISION_DOWN,R.id.down);
        binding(Models.TelevisionCommand.TELEVISION_LEFT,R.id.left);
        binding(Models.TelevisionCommand.TELEVISION_RIGHT,R.id.right);
        binding(Models.TelevisionCommand.TELEVISION_CONFIRM,R.id.ok);
        binding(Models.TelevisionCommand.TELEVISION_MENU,R.id.menu);
        binding(Models.TelevisionCommand.TELEVISION_MUTE,R.id.silence);
        binding(Models.TelevisionCommand.TELEVISION_POWER,R.id.power);
        binding(Models.TelevisionCommand.TELEVISION_RETURN,R.id.turnBack);
        binding(Models.TelevisionCommand.TELEVISION_VOLUME,R.id.volumePlus);
        binding(Models.TelevisionCommand.TELEVISION_VOLUME_REDUCTION,R.id.volumeMinus);
    }

    public void binding(final Models.TelevisionCommand command,@IdRes int res){
        getView().findViewById(res).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand(command);
            }
        });
    }


    public void setDevice(Models.Device device) {
        mDevice = device;
    }

    public void setRouter(Router router) {
        mRouter = router;
    }

    private void initContext() {
        if (mRouter == null || mDevice == null) {
            if (mRouter == null) {
                Toast.makeText(getContext(), R.string.router_not_found, Toast.LENGTH_SHORT).show();
            }
            if (mDevice == null) {
                Toast.makeText(getContext(), R.string.infrared_not_found, Toast.LENGTH_SHORT).show();
            }
            return;
        }
        mInfraredDevice = mDevice.getExtension(Models.InfraredDevice.detail);
        mCommunicationManager = mRouter.getRouterSession().getCommunicationManager();
    }

    public void sendCommand(final Models.TelevisionCommand command){
        final Messages.Request request = RequestUtil
                .sendIrCommand(mInfraredDevice.getId(), Models.InfraredCommand.newBuilder().setTelevision(command).build());
        mCommunicationManager.postRequestAsync(request, new Action2<Messages.Response, Throwable>() {
            @Override
            public void call(Messages.Response response, final Throwable throwable) {
                new Handler(getContext().getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (throwable != null) {
                            Toast.makeText(getContext(), command + "操作失败" + throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), command + "操作成功", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }



}
