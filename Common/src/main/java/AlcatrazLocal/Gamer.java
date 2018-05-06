package AlcatrazLocal;

import java.io.Serializable;

public class Gamer implements Serializable {
    private static final long serialVersionUID = -928187873803555722L;
    private String name;
    private String endpoint;


    public Gamer(String name, String endpoint){
        this.name = name;
        this.endpoint = endpoint;
    }

    public String getEndpoint(){
        return this.endpoint;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
