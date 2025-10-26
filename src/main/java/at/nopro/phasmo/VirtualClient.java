package at.nopro.phasmo;

import java.io.File;
import java.io.IOException;

public class VirtualClient {
    private int screenId;
    private Process xvfbProcess;
    private Process minecraftProcess;
    private File hmcPath;

    public VirtualClient(File hmcPath) throws IOException {
        String displayVariable = System.getenv("DISPLAY");
        if(displayVariable == null) {
            throw new RuntimeException("Did not find DISPLAY env-variable");
        }
        screenId = Integer.parseInt(displayVariable.substring(1)); // strip :
        this.hmcPath = hmcPath;

        createVirtualScreen();
    }

    public void start() throws IOException {
        startMinecraftClient(hmcPath);
    }

    private void createVirtualScreen() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("/usr/bin/bash", "-c", "Xvfb :" + screenId + " -screen 0 1024x768x24 +extension GLX +render -noreset");
        this.xvfbProcess = processBuilder.start();
    }

    private void startMinecraftClient(File cwd) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("/usr/bin/bash", "-c", "env DISPLAY=:" + screenId + " vglrun ./headlessmc-launcher --command launch fabric:1.21.10").directory(cwd);
        processBuilder.inheritIO();
        this.minecraftProcess = processBuilder.start();
    }
}
