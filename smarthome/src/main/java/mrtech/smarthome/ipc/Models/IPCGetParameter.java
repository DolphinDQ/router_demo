package mrtech.smarthome.ipc.Models;


public interface IPCGetParameter extends IPCEventData {
    int getParamType();

    Object getParamData();
}
