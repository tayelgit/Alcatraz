package communctation.Interface.ServerReplication;

import AlcatrazLocal.GameLocal;

import java.util.Map;
import java.util.UUID;

public interface GameStateObserver {
    void replicateGameState(Map<UUID, GameLocal> gameLocalList);
}
