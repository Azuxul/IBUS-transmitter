package fr.azuxul.ibustransmitter;

public class CommandChannels {

    private byte roll = 0;
    private byte pitch = 1;
    private byte yaw = 3;
    private byte throttle = 2;

    private byte leftStickX;
    private byte leftStickY;
    private byte rightStickX;
    private byte rightStickY;

    private byte mode;

    public void setMode(int mode) {
        this.mode = (byte) mode;
        updateMode();
    }

    public void updateMode() {

        if (mode == 1 || mode == 3) {
            leftStickY = pitch;
            rightStickY = throttle;
        } else {
            leftStickY = throttle;
            rightStickY = pitch;
        }

        if (mode == 1 || mode == 2) {
            leftStickX = yaw;
            rightStickX = roll;
        } else {
            leftStickX = roll;
            rightStickX = yaw;
        }
    }

    public byte getLeftStickX() {
        return leftStickX;
    }

    public byte getLeftStickY() {
        return leftStickY;
    }

    public byte getRightStickX() {
        return rightStickX;
    }

    public byte getRightStickY() {
        return rightStickY;
    }
}
