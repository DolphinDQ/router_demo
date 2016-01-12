package mrtech.smarthome.ipc.Models;


public interface IPCAudioFrame extends IPCEventData {
    byte[] getPcm();

    int getPcmSize();
}
