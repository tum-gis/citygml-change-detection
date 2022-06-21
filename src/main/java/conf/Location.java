package conf;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.processing.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "neo4jConf",
        "neo4jDB",
        "logFile",
        "exportDir"
})
@Generated("jsonschema2pojo")
public class Location {

    @JsonProperty("neo4jConf")
    private String neo4jConf;
    @JsonProperty("neo4jDB")
    private String neo4jDB;
    @JsonProperty("logFile")
    private String logFile;
    @JsonProperty("exportDir")
    private String exportDir;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("neo4jConf")
    public String getNeo4jConf() {
        return neo4jConf;
    }

    @JsonProperty("neo4jConf")
    public void setNeo4jConf(String neo4jConf) {
        this.neo4jConf = neo4jConf;
    }

    public Location withNeo4jConf(String neo4jConf) {
        this.neo4jConf = neo4jConf;
        return this;
    }

    @JsonProperty("neo4jDB")
    public String getNeo4jDB() {
        return neo4jDB;
    }

    @JsonProperty("neo4jDB")
    public void setNeo4jDB(String neo4jDB) {
        this.neo4jDB = neo4jDB;
    }

    public Location withNeo4jDB(String neo4jDB) {
        this.neo4jDB = neo4jDB;
        return this;
    }

    @JsonProperty("logFile")
    public String getLogFile() {
        return logFile;
    }

    @JsonProperty("logFile")
    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public Location withLogFile(String logFile) {
        this.logFile = logFile;
        return this;
    }

    @JsonProperty("exportDir")
    public String getExportDir() {
        return exportDir;
    }

    @JsonProperty("exportDir")
    public void setExportDir(String exportDir) {
        this.exportDir = exportDir;
    }

    public Location withExportDir(String exportDir) {
        this.exportDir = exportDir;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Location withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Location.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("neo4jConf");
        sb.append('=');
        sb.append(((this.neo4jConf == null) ? "<null>" : this.neo4jConf));
        sb.append(',');
        sb.append("neo4jDB");
        sb.append('=');
        sb.append(((this.neo4jDB == null) ? "<null>" : this.neo4jDB));
        sb.append(',');
        sb.append("logFile");
        sb.append('=');
        sb.append(((this.logFile == null) ? "<null>" : this.logFile));
        sb.append(',');
        sb.append("exportDir");
        sb.append('=');
        sb.append(((this.exportDir == null) ? "<null>" : this.exportDir));
        sb.append(',');
        sb.append("additionalProperties");
        sb.append('=');
        sb.append(((this.additionalProperties == null) ? "<null>" : this.additionalProperties));
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',') {
            sb.setCharAt((sb.length() - 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result * 31) + ((this.neo4jDB == null) ? 0 : this.neo4jDB.hashCode()));
        result = ((result * 31) + ((this.neo4jConf == null) ? 0 : this.neo4jConf.hashCode()));
        result = ((result * 31) + ((this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode()));
        result = ((result * 31) + ((this.exportDir == null) ? 0 : this.exportDir.hashCode()));
        result = ((result * 31) + ((this.logFile == null) ? 0 : this.logFile.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Location) == false) {
            return false;
        }
        Location rhs = ((Location) other);
        return ((((((this.neo4jDB == rhs.neo4jDB) || ((this.neo4jDB != null) && this.neo4jDB.equals(rhs.neo4jDB))) && ((this.neo4jConf == rhs.neo4jConf) || ((this.neo4jConf != null) && this.neo4jConf.equals(rhs.neo4jConf)))) && ((this.additionalProperties == rhs.additionalProperties) || ((this.additionalProperties != null) && this.additionalProperties.equals(rhs.additionalProperties)))) && ((this.exportDir == rhs.exportDir) || ((this.exportDir != null) && this.exportDir.equals(rhs.exportDir)))) && ((this.logFile == rhs.logFile) || ((this.logFile != null) && this.logFile.equals(rhs.logFile))));
    }

}
