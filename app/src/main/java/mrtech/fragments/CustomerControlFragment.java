package mrtech.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import mrtech.activities.R;
import mrtech.smarthome.router.Models.CommunicationManager;
import mrtech.smarthome.router.Router;
import mrtech.smarthome.rpc.Messages;
import mrtech.smarthome.rpc.Models;
import mrtech.smarthome.util.RequestUtil;
import rx.functions.Action2;


public class CustomerControlFragment extends Fragment {

    private Router mRouter;
    private Models.Device mDevice;
    private Models.InfraredDevice mInfraredDevice;
    private CommunicationManager mCommunicationManager;


    public static CustomerControlFragment newInstance(Router router, Models.Device device) {
        final CustomerControlFragment fragment = new CustomerControlFragment();
        fragment.setDevice(device);
        fragment.setRouter(router);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContext();
    }



    private void initButtons() {
        final   GridView buttons = (GridView) getView().findViewById(R.id.buttons);
        final View progressBar =getView().findViewById(R.id.press_progress);
        progressBar.setVisibility(View.GONE);
        buttons.setAdapter(new ArrayAdapter<Models.InfraredOpCode>(getContext(), R.layout.layou_custom_infrared_button, mInfraredDevice.getOpcodesList()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.layou_custom_infrared_button, parent, false);
                final Models.InfraredOpCode opCode = getItem(position);
                final Button button = (Button) convertView.findViewById(R.id.custom_infrared_button);
                button.setText(opCode.getName());
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected void onPreExecute() {
                                progressBar.setVisibility(View.VISIBLE);
                            }

                            @Override
                            protected Void doInBackground(Void... params) {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                progressBar.setVisibility(View.GONE);
                            }
                        }.execute();

                        final Messages.Request request = RequestUtil
                                .sendIrCommand(opCode.getDeviceId(), Models.InfraredCommand.newBuilder()
                                        .setExtension(Models.ExtensionCommand.newBuilder().setOpcodeId(opCode.getId())).build(),false);
                        mCommunicationManager.postRequestAsync(request, new Action2<Messages.Response, Throwable>() {
                            @Override
                            public void call(Messages.Response response, final Throwable throwable) {
                                new Handler(getContext().getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (throwable != null) {
                                            Toast.makeText(getContext(), opCode.getName() + "操作失败" + throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getContext(), opCode.getName() + "操作成功", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
                return convertView;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_customer_control, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initButtons();
    }

    private void setDevice(Models.Device device) {
        mDevice = device;
    }

    private void setRouter(Router router) {
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
}
