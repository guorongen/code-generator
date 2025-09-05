package com.gre.cli.pattern;

public class Client {
    public static void main(String[] args) {
        Device tv = new Device("TV");
        Device stereo = new Device("Stereo");

        TurnOnCommand turnOn = new TurnOnCommand(tv);
        TurnOffCommand turnOff = new TurnOffCommand(stereo);

        RemoteControl remoteControl = new RemoteControl();
        remoteControl.setCommand(turnOn);
        remoteControl.pressButton();

        remoteControl.setCommand(turnOff);
        remoteControl.pressButton();
    }
}
