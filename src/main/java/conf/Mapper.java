
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
    "oldFile",
    "newFile",
    "fullClassName",
    "isSet",
    "rules"
})
@Generated("jsonschema2pojo")
public class Mapper {

    @JsonProperty("oldFile")
    private String oldFile;
    @JsonProperty("newFile")
    private String newFile;
    @JsonProperty("fullClassName")
    private Boolean fullClassName;
    @JsonProperty("isSet")
    private Boolean isSet;
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

    @JsonProperty("fullClassName")
    public Boolean getFullClassName() {
        return fullClassName;
    }

    @JsonProperty("fullClassName")
    public void setFullClassName(Boolean fullClassName) {
        this.fullClassName = fullClassName;
    }

    public Mapper withFullClassName(Boolean fullClassName) {
        this.fullClassName = fullClassName;
        return this;
    }

    @JsonProperty("isSet")
    public Boolean getIsSet() {
        return isSet;
    }

    @JsonProperty("isSet")
    public void setIsSet(Boolean isSet) {
        this.isSet = isSet;
    }

    public Mapper withIsSet(Boolean isSet) {
        this.isSet = isSet;
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
        sb.append(((this.oldFile == null)?"<null>":this.oldFile));
        sb.append(',');
        sb.append("newFile");
        sb.append('=');
        sb.append(((this.newFile == null)?"<null>":this.newFile));
        sb.append(',');
        sb.append("fullClassName");
        sb.append('=');
        sb.append(((this.fullClassName == null)?"<null>":this.fullClassName));
        sb.append(',');
        sb.append("isSet");
        sb.append('=');
        sb.append(((this.isSet == null)?"<null>":this.isSet));
        sb.append(',');
        sb.append("rules");
        sb.append('=');
        sb.append(((this.rules == null)?"<null>":this.rules));
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
        result = ((result* 31)+((this.oldFile == null)? 0 :this.oldFile.hashCode()));
        result = ((result* 31)+((this.fullClassName == null)? 0 :this.fullClassName.hashCode()));
        result = ((result* 31)+((this.isSet == null)? 0 :this.isSet.hashCode()));
        result = ((result* 31)+((this.newFile == null)? 0 :this.newFile.hashCode()));
        result = ((result* 31)+((this.rules == null)? 0 :this.rules.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
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
        return (((((((this.oldFile == rhs.oldFile)||((this.oldFile!= null)&&this.oldFile.equals(rhs.oldFile)))&&((this.fullClassName == rhs.fullClassName)||((this.fullClassName!= null)&&this.fullClassName.equals(rhs.fullClassName))))&&((this.isSet == rhs.isSet)||((this.isSet!= null)&&this.isSet.equals(rhs.isSet))))&&((this.newFile == rhs.newFile)||((this.newFile!= null)&&this.newFile.equals(rhs.newFile))))&&((this.rules == rhs.rules)||((this.rules!= null)&&this.rules.equals(rhs.rules))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))));
    }

}
