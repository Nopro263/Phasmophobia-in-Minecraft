package at.nopro.phasmo;

import java.io.File;
import java.io.IOException;

import static at.nopro.phasmo.Configuration.config;

public class VirtualClient {
    private final int screenId;
    private final File hmcPath;
    private Process xvfbProcess;
    private Process minecraftProcess;

    public VirtualClient(File hmcPath) throws IOException {
        String displayVariable = System.getenv("DISPLAY");
        if (displayVariable == null) {
            throw new RuntimeException("Did not find DISPLAY env-variable");
        }
        screenId = Integer.parseInt(displayVariable.substring(1)); // strip :
        System.out.println("using DISPLAY " + screenId);
        this.hmcPath = hmcPath;

        createVirtualScreen();
    }

    private void createVirtualScreen() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("/usr/bin/bash", "-c", "Xvfb :" + screenId + " -screen 0 1024x768x24 +extension GLX +render -noreset");
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        this.xvfbProcess = processBuilder.start();
    }

    public void start() throws IOException {
        startMinecraftClient(hmcPath);
    }

    private void startMinecraftClient(File cwd) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("/usr/bin/bash", "-c", "./headlessmc-launcher --command launch fabric:1.21.10").directory(cwd);
        processBuilder.environment().put("DISPLAY", ":" + screenId);
        processBuilder.environment().put("CONNECT_PORT", config.mcServer.port + "");
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        this.minecraftProcess = processBuilder.start();
    }
}
