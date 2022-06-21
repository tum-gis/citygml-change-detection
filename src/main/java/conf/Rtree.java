package conf;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.processing.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "exportDir",
        "nodeRef"
})
@Generated("jsonschema2pojo")
public class Rtree {

    @JsonProperty("exportDir")
    private String exportDir;
    @JsonProperty("nodeRef")
    private Integer nodeRef;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("exportDir")
    public String getExportDir() {
        return exportDir;
    }

    @JsonProperty("exportDir")
    public void setExportDir(String exportDir) {
        this.exportDir = exportDir;
    }

    public Rtree withExportDir(String exportDir) {
        this.exportDir = exportDir;
        return this;
    }

    @JsonProperty("nodeRef")
    public Integer getNodeRef() {
        return nodeRef;
    }

    @JsonProperty("nodeRef")
    public void setNodeRef(Integer nodeRef) {
        this.nodeRef = nodeRef;
    }

    public Rtree withNodeRef(Integer nodeRef) {
        this.nodeRef = nodeRef;
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

    public Rtree withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Rtree.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("exportDir");
        sb.append('=');
        sb.append(((this.exportDir == null) ? "<null>" : this.exportDir));
        sb.append(',');
        sb.append("nodeRef");
        sb.append('=');
        sb.append(((this.nodeRef == null) ? "<null>" : this.nodeRef));
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
        result = ((result * 31) + ((this.exportDir == null) ? 0 : this.exportDir.hashCode()));
        result = ((result * 31) + ((this.nodeRef == null) ? 0 : this.nodeRef.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Rtree) == false) {
            return false;
        }
        Rtree rhs = ((Rtree) other);
        return ((((this.additionalProperties == rhs.additionalProperties) || ((this.additionalProperties != null) && this.additionalProperties.equals(rhs.additionalProperties))) && ((this.exportDir == rhs.exportDir) || ((this.exportDir != null) && this.exportDir.equals(rhs.exportDir)))) && ((this.nodeRef == rhs.nodeRef) || ((this.nodeRef != null) && this.nodeRef.equals(rhs.nodeRef))));
    }

}
