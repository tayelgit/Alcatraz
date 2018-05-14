package Service.Alcatraz.serviceData;

import java.io.Serializable;

public class Gamer implements Serializable {
    private static final long serialVersionUID = -928187873803555722L;
    private String name;
    private String endpoint;
    private boolean  isReady;
    private boolean confirmed;

    public Gamer(String name, String endpoint){
        this.name = name;
        this.endpoint = endpoint;
        this.isReady = false;
    }
    public void setReady(boolean isReady){this.isReady = isReady;}
    public boolean isReady(){return this.isReady;}
    public String getEndpoint(){
        return this.endpoint;
    }
    public String getName() {
        return name;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}
