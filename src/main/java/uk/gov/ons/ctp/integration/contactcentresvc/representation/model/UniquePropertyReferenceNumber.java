//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package uk.gov.ons.ctp.integration.contactcentresvc.representation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.apache.commons.lang3.StringUtils;

public class UniquePropertyReferenceNumber {
  @JsonProperty("uprn")
  @JsonSerialize(using = ToStringSerializer.class)
  private long value;

  public UniquePropertyReferenceNumber() {}

  public UniquePropertyReferenceNumber(String str) {
    setValue(str);
  }

  public long getValue() {
    return this.value;
  }

  public void setValue(long value) {
    this.value = value;
  }

  public void setValue(String str) {
    if (!StringUtils.isBlank(str)) {
      try {
        Long uprn = Long.parseLong(str);
        if (uprn < 0L || uprn > 999999999999L) {
          throw new IllegalArgumentException("String '" + uprn + "' is not a valid UPRN");
        }

        this.value = uprn;
      } catch (NumberFormatException var3) {
        throw new IllegalArgumentException();
      }
    }
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof UniquePropertyReferenceNumber)) {
      return false;
    } else {
      UniquePropertyReferenceNumber other = (UniquePropertyReferenceNumber) o;
      if (!other.canEqual(this)) {
        return false;
      } else {
        return this.getValue() == other.getValue();
      }
    }
  }

  protected boolean canEqual(Object other) {
    return other instanceof UniquePropertyReferenceNumber;
  }

  public int hashCode() {
    int result = 1;
    long val = this.getValue();
    result = result * 59 + (int) (val >>> 32 ^ val);
    return result;
  }

  public String toString() {
    return "UniquePropertyReferenceNumber(value=" + this.getValue() + ")";
  }

  public UniquePropertyReferenceNumber(long value) {
    this.value = value;
  }
}
