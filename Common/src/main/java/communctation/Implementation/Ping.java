package communctation.Implementation;


import Service.Alcatraz.AlcatrazRemote.Implementation.GameImpl;
import Service.Alcatraz.AlcatrazRemote.Interface.GameRemote;

import java.rmi.RemoteException;
import java.util.TimerTask;

public class Ping extends TimerTask {

   private GameRemote game;
   private GameImpl gameImpl;
   public Ping(GameRemote game, GameImpl gameImpl){
       this.game = game;
       this.gameImpl = gameImpl;
   }
    public void run() {
        try {
            game.ping();
        } catch (RemoteException e) {
            this.gameImpl.initAbort();
        }


    }
}
