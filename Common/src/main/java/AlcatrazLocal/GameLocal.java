package AlcatrazLocal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class GameLocal implements Serializable {


    private static final long serialVersionUID = 7602783824923127611L;
    private UUID gameID;
    private ArrayList<String> gamerList ;

    public GameLocal(UUID gameID)  {
        this.gameID = gameID;
        this. gamerList = new ArrayList<>();
    }

    public UUID getGameID(){
        return this.gameID;
    }

    public void addGamer(String gamer) {
        if(gamerList.contains(gamer)) gamerList.add(gamer);
    }


    public void removeGamer(String gamer) {
        gamerList.removeIf(g -> g.equals(gamer));
    }

    public ArrayList<String> getGamers(){
        return this.gamerList;
    }

}
