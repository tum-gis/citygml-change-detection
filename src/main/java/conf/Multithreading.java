
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
    "enabled",
    "producers",
    "consumers",
    "splitTopLevel",
    "timeout"
})
@Generated("jsonschema2pojo")
public class Multithreading {

    @JsonProperty("enabled")
    private Boolean enabled;
    @JsonProperty("producers")
    private Integer producers;
    @JsonProperty("consumers")
    private Integer consumers;
    @JsonProperty("splitTopLevel")
    private Boolean splitTopLevel;
    @JsonProperty("timeout")
    private Integer timeout;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("enabled")
    public Boolean getEnabled() {
        return enabled;
    }

    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Multithreading withEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @JsonProperty("producers")
    public Integer getProducers() {
        return producers;
    }

    @JsonProperty("producers")
    public void setProducers(Integer producers) {
        this.producers = producers;
    }

    public Multithreading withProducers(Integer producers) {
        this.producers = producers;
        return this;
    }

    @JsonProperty("consumers")
    public Integer getConsumers() {
        return consumers;
    }

    @JsonProperty("consumers")
    public void setConsumers(Integer consumers) {
        this.consumers = consumers;
    }

    public Multithreading withConsumers(Integer consumers) {
        this.consumers = consumers;
        return this;
    }

    @JsonProperty("splitTopLevel")
    public Boolean getSplitTopLevel() {
        return splitTopLevel;
    }

    @JsonProperty("splitTopLevel")
    public void setSplitTopLevel(Boolean splitTopLevel) {
        this.splitTopLevel = splitTopLevel;
    }

    public Multithreading withSplitTopLevel(Boolean splitTopLevel) {
        this.splitTopLevel = splitTopLevel;
        return this;
    }

    @JsonProperty("timeout")
    public Integer getTimeout() {
        return timeout;
    }

    @JsonProperty("timeout")
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Multithreading withTimeout(Integer timeout) {
        this.timeout = timeout;
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

    public Multithreading withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Multithreading.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("enabled");
        sb.append('=');
        sb.append(((this.enabled == null)?"<null>":this.enabled));
        sb.append(',');
        sb.append("producers");
        sb.append('=');
        sb.append(((this.producers == null)?"<null>":this.producers));
        sb.append(',');
        sb.append("consumers");
        sb.append('=');
        sb.append(((this.consumers == null)?"<null>":this.consumers));
        sb.append(',');
        sb.append("splitTopLevel");
        sb.append('=');
        sb.append(((this.splitTopLevel == null)?"<null>":this.splitTopLevel));
        sb.append(',');
        sb.append("timeout");
        sb.append('=');
        sb.append(((this.timeout == null)?"<null>":this.timeout));
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
        result = ((result* 31)+((this.consumers == null)? 0 :this.consumers.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.splitTopLevel == null)? 0 :this.splitTopLevel.hashCode()));
        result = ((result* 31)+((this.enabled == null)? 0 :this.enabled.hashCode()));
        result = ((result* 31)+((this.timeout == null)? 0 :this.timeout.hashCode()));
        result = ((result* 31)+((this.producers == null)? 0 :this.producers.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Multithreading) == false) {
            return false;
        }
        Multithreading rhs = ((Multithreading) other);
        return (((((((this.consumers == rhs.consumers)||((this.consumers!= null)&&this.consumers.equals(rhs.consumers)))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.splitTopLevel == rhs.splitTopLevel)||((this.splitTopLevel!= null)&&this.splitTopLevel.equals(rhs.splitTopLevel))))&&((this.enabled == rhs.enabled)||((this.enabled!= null)&&this.enabled.equals(rhs.enabled))))&&((this.timeout == rhs.timeout)||((this.timeout!= null)&&this.timeout.equals(rhs.timeout))))&&((this.producers == rhs.producers)||((this.producers!= null)&&this.producers.equals(rhs.producers))));
    }

}
