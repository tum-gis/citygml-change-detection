
package conf;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "length",
    "angle",
    "shared"
})
@Generated("jsonschema2pojo")
public class Fuzzy {

    @JsonProperty("length")
    private Double length;
    @JsonProperty("angle")
    private Double angle;
    @JsonProperty("shared")
    private Shared shared;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("length")
    public Double getLength() {
        return length;
    }

    @JsonProperty("length")
    public void setLength(Double length) {
        this.length = length;
    }

    public Fuzzy withLength(Double length) {
        this.length = length;
        return this;
    }

    @JsonProperty("angle")
    public Double getAngle() {
        return angle;
    }

    @JsonProperty("angle")
    public void setAngle(Double angle) {
        this.angle = angle;
    }

    public Fuzzy withAngle(Double angle) {
        this.angle = angle;
        return this;
    }

    @JsonProperty("shared")
    public Shared getShared() {
        return shared;
    }

    @JsonProperty("shared")
    public void setShared(Shared shared) {
        this.shared = shared;
    }

    public Fuzzy withShared(Shared shared) {
        this.shared = shared;
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

    public Fuzzy withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Fuzzy.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("length");
        sb.append('=');
        sb.append(((this.length == null)?"<null>":this.length));
        sb.append(',');
        sb.append("angle");
        sb.append('=');
        sb.append(((this.angle == null)?"<null>":this.angle));
        sb.append(',');
        sb.append("shared");
        sb.append('=');
        sb.append(((this.shared == null)?"<null>":this.shared));
        sb.append(',');
        sb.append("additionalProperties");
        sb.append('=');
        sb.append(((this.additionalProperties == null)?"<null>":this.additionalProperties));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.length == null)? 0 :this.length.hashCode()));
        result = ((result* 31)+((this.angle == null)? 0 :this.angle.hashCode()));
        result = ((result* 31)+((this.shared == null)? 0 :this.shared.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Fuzzy) == false) {
            return false;
        }
        Fuzzy rhs = ((Fuzzy) other);
        return (((((this.length == rhs.length)||((this.length!= null)&&this.length.equals(rhs.length)))&&((this.angle == rhs.angle)||((this.angle!= null)&&this.angle.equals(rhs.angle))))&&((this.shared == rhs.shared)||((this.shared!= null)&&this.shared.equals(rhs.shared))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))));
    }

}
