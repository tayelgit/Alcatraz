package AlcatrazLocal;

import java.io.Serializable;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.UUID;

import static java.rmi.server.RemoteServer.getClientHost;

public class GameLocal implements Serializable {


    private static final long serialVersionUID = 7602783824923127611L;
    private UUID gameID;
    private ArrayList<Gamer> gamerList ;
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
        this.gamerList = new ArrayList<>();
        this.playerCount = playercount;

    }

    public UUID getGameID(){
        return this.gameID;
    }

    public void addGamer(Gamer gamer){
         gamerList.add(gamer);
         gamerList.forEach((g)->System.out.println(g.getName()));
    }

    public boolean isGamerNameAvaliable(String gamerName){
        return  !this.gamerList.stream().anyMatch((gamer)-> gamer.getName().equals(gamerName));
    }

    public void removeGamer(String gamer) {
        gamerList.removeIf(g -> g.equals(gamer));
    }

    public ArrayList<Gamer> getGamers(){
        return this.gamerList;
    }

}
