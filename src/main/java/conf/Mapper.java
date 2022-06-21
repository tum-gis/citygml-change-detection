package conf;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.processing.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "oldFile",
        "newFile",
        "rules"
})
@Generated("jsonschema2pojo")
public class Mapper {

    @JsonProperty("oldFile")
    private String oldFile;
    @JsonProperty("newFile")
    private String newFile;
    @JsonProperty("rules")
    private Rules rules;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("oldFile")
    public String getOldFile() {
        return oldFile;
    }

    @JsonProperty("oldFile")
    public void setOldFile(String oldFile) {
        this.oldFile = oldFile;
    }

    public Mapper withOldFile(String oldFile) {
        this.oldFile = oldFile;
        return this;
    }

    @JsonProperty("newFile")
    public String getNewFile() {
        return newFile;
    }

    @JsonProperty("newFile")
    public void setNewFile(String newFile) {
        this.newFile = newFile;
    }

    public Mapper withNewFile(String newFile) {
        this.newFile = newFile;
        return this;
    }

    @JsonProperty("rules")
    public Rules getRules() {
        return rules;
    }

    @JsonProperty("rules")
    public void setRules(Rules rules) {
        this.rules = rules;
    }

    public Mapper withRules(Rules rules) {
        this.rules = rules;
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

    public Mapper withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Mapper.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("oldFile");
        sb.append('=');
        sb.append(((this.oldFile == null) ? "<null>" : this.oldFile));
        sb.append(',');
        sb.append("newFile");
        sb.append('=');
        sb.append(((this.newFile == null) ? "<null>" : this.newFile));
        sb.append(',');
        sb.append("rules");
        sb.append('=');
        sb.append(((this.rules == null) ? "<null>" : this.rules));
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
        result = ((result * 31) + ((this.newFile == null) ? 0 : this.newFile.hashCode()));
        result = ((result * 31) + ((this.oldFile == null) ? 0 : this.oldFile.hashCode()));
        result = ((result * 31) + ((this.rules == null) ? 0 : this.rules.hashCode()));
        result = ((result * 31) + ((this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Mapper) == false) {
            return false;
        }
        Mapper rhs = ((Mapper) other);
        return (((((this.newFile == rhs.newFile) || ((this.newFile != null) && this.newFile.equals(rhs.newFile))) && ((this.oldFile == rhs.oldFile) || ((this.oldFile != null) && this.oldFile.equals(rhs.oldFile)))) && ((this.rules == rhs.rules) || ((this.rules != null) && this.rules.equals(rhs.rules)))) && ((this.additionalProperties == rhs.additionalProperties) || ((this.additionalProperties != null) && this.additionalProperties.equals(rhs.additionalProperties))));
    }

}
