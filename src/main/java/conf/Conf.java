
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
    "message",
    "workingDir",
    "db",
    "rtree",
    "batch",
    "multithreading",
    "mapper",
    "matcher"
})
@Generated("jsonschema2pojo")
public class Conf {

    @JsonProperty("message")
    private String message;
    @JsonProperty("workingDir")
    private String workingDir;
    @JsonProperty("db")
    private Db db;
    @JsonProperty("rtree")
    private Rtree rtree;
    @JsonProperty("batch")
    private Batch batch;
    @JsonProperty("multithreading")
    private Multithreading multithreading;
    @JsonProperty("mapper")
    private Mapper mapper;
    @JsonProperty("matcher")
    private Matcher matcher;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    public Conf withMessage(String message) {
        this.message = message;
        return this;
    }

    @JsonProperty("workingDir")
    public String getWorkingDir() {
        return workingDir;
    }

    @JsonProperty("workingDir")
    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public Conf withWorkingDir(String workingDir) {
        this.workingDir = workingDir;
        return this;
    }

    @JsonProperty("db")
    public Db getDb() {
        return db;
    }

    @JsonProperty("db")
    public void setDb(Db db) {
        this.db = db;
    }

    public Conf withDb(Db db) {
        this.db = db;
        return this;
    }

    @JsonProperty("rtree")
    public Rtree getRtree() {
        return rtree;
    }

    @JsonProperty("rtree")
    public void setRtree(Rtree rtree) {
        this.rtree = rtree;
    }

    public Conf withRtree(Rtree rtree) {
        this.rtree = rtree;
        return this;
    }

    @JsonProperty("batch")
    public Batch getBatch() {
        return batch;
    }

    @JsonProperty("batch")
    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public Conf withBatch(Batch batch) {
        this.batch = batch;
        return this;
    }

    @JsonProperty("multithreading")
    public Multithreading getMultithreading() {
        return multithreading;
    }

    @JsonProperty("multithreading")
    public void setMultithreading(Multithreading multithreading) {
        this.multithreading = multithreading;
    }

    public Conf withMultithreading(Multithreading multithreading) {
        this.multithreading = multithreading;
        return this;
    }

    @JsonProperty("mapper")
    public Mapper getMapper() {
        return mapper;
    }

    @JsonProperty("mapper")
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public Conf withMapper(Mapper mapper) {
        this.mapper = mapper;
        return this;
    }

    @JsonProperty("matcher")
    public Matcher getMatcher() {
        return matcher;
    }

    @JsonProperty("matcher")
    public void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

    public Conf withMatcher(Matcher matcher) {
        this.matcher = matcher;
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

    public Conf withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Conf.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("message");
        sb.append('=');
        sb.append(((this.message == null)?"<null>":this.message));
        sb.append(',');
        sb.append("workingDir");
        sb.append('=');
        sb.append(((this.workingDir == null)?"<null>":this.workingDir));
        sb.append(',');
        sb.append("db");
        sb.append('=');
        sb.append(((this.db == null)?"<null>":this.db));
        sb.append(',');
        sb.append("rtree");
        sb.append('=');
        sb.append(((this.rtree == null)?"<null>":this.rtree));
        sb.append(',');
        sb.append("batch");
        sb.append('=');
        sb.append(((this.batch == null)?"<null>":this.batch));
        sb.append(',');
        sb.append("multithreading");
        sb.append('=');
        sb.append(((this.multithreading == null)?"<null>":this.multithreading));
        sb.append(',');
        sb.append("mapper");
        sb.append('=');
        sb.append(((this.mapper == null)?"<null>":this.mapper));
        sb.append(',');
        sb.append("matcher");
        sb.append('=');
        sb.append(((this.matcher == null)?"<null>":this.matcher));
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
        result = ((result* 31)+((this.workingDir == null)? 0 :this.workingDir.hashCode()));
        result = ((result* 31)+((this.batch == null)? 0 :this.batch.hashCode()));
        result = ((result* 31)+((this.multithreading == null)? 0 :this.multithreading.hashCode()));
        result = ((result* 31)+((this.mapper == null)? 0 :this.mapper.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.message == null)? 0 :this.message.hashCode()));
        result = ((result* 31)+((this.rtree == null)? 0 :this.rtree.hashCode()));
        result = ((result* 31)+((this.matcher == null)? 0 :this.matcher.hashCode()));
        result = ((result* 31)+((this.db == null)? 0 :this.db.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Conf) == false) {
            return false;
        }
        Conf rhs = ((Conf) other);
        return ((((((((((this.workingDir == rhs.workingDir)||((this.workingDir!= null)&&this.workingDir.equals(rhs.workingDir)))&&((this.batch == rhs.batch)||((this.batch!= null)&&this.batch.equals(rhs.batch))))&&((this.multithreading == rhs.multithreading)||((this.multithreading!= null)&&this.multithreading.equals(rhs.multithreading))))&&((this.mapper == rhs.mapper)||((this.mapper!= null)&&this.mapper.equals(rhs.mapper))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.message == rhs.message)||((this.message!= null)&&this.message.equals(rhs.message))))&&((this.rtree == rhs.rtree)||((this.rtree!= null)&&this.rtree.equals(rhs.rtree))))&&((this.matcher == rhs.matcher)||((this.matcher!= null)&&this.matcher.equals(rhs.matcher))))&&((this.db == rhs.db)||((this.db!= null)&&this.db.equals(rhs.db))));
    }

}
