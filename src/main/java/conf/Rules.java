
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
    "citygml",
    "gml",
    "xal"
})
@Generated("jsonschema2pojo")
public class Rules {

    @JsonProperty("citygml")
    private String citygml;
    @JsonProperty("gml")
    private String gml;
    @JsonProperty("xal")
    private String xal;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("citygml")
    public String getCitygml() {
        return citygml;
    }

    @JsonProperty("citygml")
    public void setCitygml(String citygml) {
        this.citygml = citygml;
    }

    public Rules withCitygml(String citygml) {
        this.citygml = citygml;
        return this;
    }

    @JsonProperty("gml")
    public String getGml() {
        return gml;
    }

    @JsonProperty("gml")
    public void setGml(String gml) {
        this.gml = gml;
    }

    public Rules withGml(String gml) {
        this.gml = gml;
        return this;
    }

    @JsonProperty("xal")
    public String getXal() {
        return xal;
    }

    @JsonProperty("xal")
    public void setXal(String xal) {
        this.xal = xal;
    }

    public Rules withXal(String xal) {
        this.xal = xal;
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

    public Rules withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Rules.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("citygml");
        sb.append('=');
        sb.append(((this.citygml == null)?"<null>":this.citygml));
        sb.append(',');
        sb.append("gml");
        sb.append('=');
        sb.append(((this.gml == null)?"<null>":this.gml));
        sb.append(',');
        sb.append("xal");
        sb.append('=');
        sb.append(((this.xal == null)?"<null>":this.xal));
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
        result = ((result* 31)+((this.xal == null)? 0 :this.xal.hashCode()));
        result = ((result* 31)+((this.citygml == null)? 0 :this.citygml.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.gml == null)? 0 :this.gml.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Rules) == false) {
            return false;
        }
        Rules rhs = ((Rules) other);
        return (((((this.xal == rhs.xal)||((this.xal!= null)&&this.xal.equals(rhs.xal)))&&((this.citygml == rhs.citygml)||((this.citygml!= null)&&this.citygml.equals(rhs.citygml))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.gml == rhs.gml)||((this.gml!= null)&&this.gml.equals(rhs.gml))));
    }

}
