package at.nopro.phasmo.gameplay;

import at.nopro.phasmo.gameplay.editor.Editor;
import at.nopro.phasmo.gameplay.lobby.LobbyInstance;

import java.io.IOException;

public final class Gameplay {
    public static void init() throws IOException {
        Editor.init();

        LobbyInstance.init();
    }
}
