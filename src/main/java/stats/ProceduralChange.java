package stats;

public class ProceduralChange extends Change {

    public ProceduralChange() {
        super();
        this.initMapEntry("id");
        this.initMapEntry("creationDate");
        this.initMapEntry("srsName");
        this.initMapEntry("srsDimension");
    }

    @Override
    public String getLabel() {
        return "Procedural Changes";
    }


}
