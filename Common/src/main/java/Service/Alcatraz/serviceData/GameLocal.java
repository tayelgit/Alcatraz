package Service.Alcatraz.serviceData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameLocal implements Serializable {


    private static final long serialVersionUID = 7602783824923127611L;
    private UUID gameID;
    private Map<String, Gamer> gamerList ;

    private String gameName;

    public String getGameName() {
        return gameName;
    }

    public int getPlayerCount() {
        return playerCount;
    }
    public int getTakenPlaces(){
        return this.gamerList.size();
    }
    private int playerCount;

    public GameLocal(UUID gameID, String gameName, int playercount )  {
        this.gameID = gameID;
        this.gameName = gameName;
        this.gamerList = new HashMap<>();
        this.playerCount = playercount;

    }

    public UUID getGameID(){
        return this.gameID;
    }

    public void addGamer(Gamer gamer){
         gamerList.put(gamer.getName(),gamer);
    }

    public boolean isGamerNameAvaliable(String gamerName){
        return this.gamerList.get(gamerName) == null;
    }

    public void removeGamer(String gamer) {
        gamerList.remove(gamer);
    }

    public Map<String,Gamer> getGamers(){
        return  this.gamerList;
    }
    public void toggleReady(String gamerName){
        Gamer gamer = this.gamerList.get(gamerName);
        gamer.setReady(!gamer.isReady());
    }
    public boolean areAllReady(){
        return !this.gamerList.values().stream().anyMatch((gamer)-> !gamer.isReady());
    }
}
