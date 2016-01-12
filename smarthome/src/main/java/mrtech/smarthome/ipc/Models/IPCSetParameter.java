package mrtech.smarthome.ipc.Models;

public interface IPCSetParameter extends IPCEventData {
    int getParamType();

    Object getResult();
}
