package conf;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.processing.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "location",
        "bolt",
        "csvDelimiter"
})
@Generated("jsonschema2pojo")
public class Db {

    @JsonProperty("location")
    private Location location;
    @JsonProperty("bolt")
    private Bolt bolt;
    @JsonProperty("csvDelimiter")
    private String csvDelimiter;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("location")
    public Location getLocation() {
        return location;
    }

    @JsonProperty("location")
    public void setLocation(Location location) {
        this.location = location;
    }

    public Db withLocation(Location location) {
        this.location = location;
        return this;
    }

    @JsonProperty("bolt")
    public Bolt getBolt() {
        return bolt;
    }

    @JsonProperty("bolt")
    public void setBolt(Bolt bolt) {
        this.bolt = bolt;
    }

    public Db withBolt(Bolt bolt) {
        this.bolt = bolt;
        return this;
    }

    @JsonProperty("csvDelimiter")
    public String getCsvDelimiter() {
        return csvDelimiter;
    }

    @JsonProperty("csvDelimiter")
    public void setCsvDelimiter(String csvDelimiter) {
        this.csvDelimiter = csvDelimiter;
    }

    public Db withCsvDelimiter(String csvDelimiter) {
        this.csvDelimiter = csvDelimiter;
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

    public Db withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Db.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("location");
        sb.append('=');
        sb.append(((this.location == null) ? "<null>" : this.location));
        sb.append(',');
        sb.append("bolt");
        sb.append('=');
        sb.append(((this.bolt == null) ? "<null>" : this.bolt));
        sb.append(',');
        sb.append("csvDelimiter");
        sb.append('=');
        sb.append(((this.csvDelimiter == null) ? "<null>" : this.csvDelimiter));
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
        result = ((result * 31) + ((this.location == null) ? 0 : this.location.hashCode()));
        result = ((result * 31) + ((this.bolt == null) ? 0 : this.bolt.hashCode()));
        result = ((result * 31) + ((this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode()));
        result = ((result * 31) + ((this.csvDelimiter == null) ? 0 : this.csvDelimiter.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Db) == false) {
            return false;
        }
        Db rhs = ((Db) other);
        return (((((this.location == rhs.location) || ((this.location != null) && this.location.equals(rhs.location))) && ((this.bolt == rhs.bolt) || ((this.bolt != null) && this.bolt.equals(rhs.bolt)))) && ((this.additionalProperties == rhs.additionalProperties) || ((this.additionalProperties != null) && this.additionalProperties.equals(rhs.additionalProperties)))) && ((this.csvDelimiter == rhs.csvDelimiter) || ((this.csvDelimiter != null) && this.csvDelimiter.equals(rhs.csvDelimiter))));
    }

}
