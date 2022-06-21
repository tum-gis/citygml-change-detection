package conf;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.processing.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "location",
        "purgePrevDb",
        "csvDelimiter"
})
@Generated("jsonschema2pojo")
public class Db {

    @JsonProperty("location")
    private Location location;
    @JsonProperty("purgePrevDb")
    private Boolean purgePrevDb;
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

    @JsonProperty("purgePrevDb")
    public Boolean getPurgePrevDb() {
        return purgePrevDb;
    }

    @JsonProperty("purgePrevDb")
    public void setPurgePrevDb(Boolean purgePrevDb) {
        this.purgePrevDb = purgePrevDb;
    }

    public Db withPurgePrevDb(Boolean purgePrevDb) {
        this.purgePrevDb = purgePrevDb;
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
        sb.append("purgePrevDb");
        sb.append('=');
        sb.append(((this.purgePrevDb == null) ? "<null>" : this.purgePrevDb));
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
        result = ((result * 31) + ((this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode()));
        result = ((result * 31) + ((this.purgePrevDb == null) ? 0 : this.purgePrevDb.hashCode()));
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
        return (((((this.location == rhs.location) || ((this.location != null) && this.location.equals(rhs.location))) && ((this.additionalProperties == rhs.additionalProperties) || ((this.additionalProperties != null) && this.additionalProperties.equals(rhs.additionalProperties)))) && ((this.purgePrevDb == rhs.purgePrevDb) || ((this.purgePrevDb != null) && this.purgePrevDb.equals(rhs.purgePrevDb)))) && ((this.csvDelimiter == rhs.csvDelimiter) || ((this.csvDelimiter != null) && this.csvDelimiter.equals(rhs.csvDelimiter))));
    }

}
