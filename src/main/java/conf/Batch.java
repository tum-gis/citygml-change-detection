package conf;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.processing.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "topLevel",
        "feature",
        "trans",
        "logTopLevel"
})
@Generated("jsonschema2pojo")
public class Batch {

    @JsonProperty("topLevel")
    private Integer topLevel;
    @JsonProperty("feature")
    private Integer feature;
    @JsonProperty("trans")
    private Integer trans;
    @JsonProperty("logTopLevel")
    private Integer logTopLevel;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("topLevel")
    public Integer getTopLevel() {
        return topLevel;
    }

    @JsonProperty("topLevel")
    public void setTopLevel(Integer topLevel) {
        this.topLevel = topLevel;
    }

    public Batch withTopLevel(Integer topLevel) {
        this.topLevel = topLevel;
        return this;
    }

    @JsonProperty("feature")
    public Integer getFeature() {
        return feature;
    }

    @JsonProperty("feature")
    public void setFeature(Integer feature) {
        this.feature = feature;
    }

    public Batch withFeature(Integer feature) {
        this.feature = feature;
        return this;
    }

    @JsonProperty("trans")
    public Integer getTrans() {
        return trans;
    }

    @JsonProperty("trans")
    public void setTrans(Integer trans) {
        this.trans = trans;
    }

    public Batch withTrans(Integer trans) {
        this.trans = trans;
        return this;
    }

    @JsonProperty("logTopLevel")
    public Integer getLogTopLevel() {
        return logTopLevel;
    }

    @JsonProperty("logTopLevel")
    public void setLogTopLevel(Integer logTopLevel) {
        this.logTopLevel = logTopLevel;
    }

    public Batch withLogTopLevel(Integer logTopLevel) {
        this.logTopLevel = logTopLevel;
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

    public Batch withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Batch.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("topLevel");
        sb.append('=');
        sb.append(((this.topLevel == null) ? "<null>" : this.topLevel));
        sb.append(',');
        sb.append("feature");
        sb.append('=');
        sb.append(((this.feature == null) ? "<null>" : this.feature));
        sb.append(',');
        sb.append("trans");
        sb.append('=');
        sb.append(((this.trans == null) ? "<null>" : this.trans));
        sb.append(',');
        sb.append("logTopLevel");
        sb.append('=');
        sb.append(((this.logTopLevel == null) ? "<null>" : this.logTopLevel));
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
        result = ((result * 31) + ((this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode()));
        result = ((result * 31) + ((this.feature == null) ? 0 : this.feature.hashCode()));
        result = ((result * 31) + ((this.logTopLevel == null) ? 0 : this.logTopLevel.hashCode()));
        result = ((result * 31) + ((this.topLevel == null) ? 0 : this.topLevel.hashCode()));
        result = ((result * 31) + ((this.trans == null) ? 0 : this.trans.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Batch) == false) {
            return false;
        }
        Batch rhs = ((Batch) other);
        return ((((((this.additionalProperties == rhs.additionalProperties) || ((this.additionalProperties != null) && this.additionalProperties.equals(rhs.additionalProperties))) && ((this.feature == rhs.feature) || ((this.feature != null) && this.feature.equals(rhs.feature)))) && ((this.logTopLevel == rhs.logTopLevel) || ((this.logTopLevel != null) && this.logTopLevel.equals(rhs.logTopLevel)))) && ((this.topLevel == rhs.topLevel) || ((this.topLevel != null) && this.topLevel.equals(rhs.topLevel)))) && ((this.trans == rhs.trans) || ((this.trans != null) && this.trans.equals(rhs.trans))));
    }

}
