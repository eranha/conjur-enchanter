package com.cyberark.models.audit;

import com.cyberark.models.DataModel;
import com.cyberark.models.ResourceIdentifier;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class AuditEvent implements DataModel {
  ResourceIdentifier user;
  String facility;
  int severity;
  String timestamp;
  String hostname;
  String appname;
  String procid;
  String msgid;

  private final AuditEventSubjectData sdata;
  private final String message;

  @JsonCreator
  public AuditEvent(@JsonProperty("facility") String facility,
                    @JsonProperty("severity") int severity,
                    @JsonProperty("timestamp")
                    @JsonDeserialize(using = PrettyTimeDeserializer.class)
                          String timestamp,
                    @JsonProperty("hostname") String hostname,
                    @JsonProperty("appname") String appname,
                    @JsonProperty("procid") String procid,
                    @JsonProperty("sdata") AuditEventSubjectData sdata,
                    @JsonProperty("msgid") String msgid,
                    @JsonProperty("message") String message) {
    this.facility = facility;
    this.severity = severity;
    this.timestamp = timestamp;
    this.hostname = hostname;
    this.appname = appname;
    this.procid = procid;
    this.msgid = msgid;
    this.sdata = sdata;
    this.message = message;

//    if (timestamp.indexOf('+') > -1) {
//      try {
//        int index = timestamp.lastIndexOf('+');
//        String date  = timestamp.substring(0, index-1);
//        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
//        System.out.printf("");
//      } catch (ParseException e) {
//        e.printStackTrace();
//      }
//    }
  }

  @Override
  public String toString() {
    return "AuditEvent{" +
        "facility='" + facility + '\'' +
        ", severity=" + severity +
        ", timestamp='" + timestamp + '\'' +
        ", hostname='" + hostname + '\'' +
        ", appname='" + appname + '\'' +
        ", procid='" + procid + '\'' +
        ", msgid='" + msgid + '\'' +
        ", sdata=" + sdata +
        ", message='" + message + '\'' +
        '}';
  }

  public String getMessage() {
    return message;
  }

  ResourceIdentifier getUser() {
    if (user == null) {
      user = sdata != null &&
          sdata.auth != null &&
          !(sdata.auth.user.equals("not-found"))
          ? ResourceIdentifier.fromString(sdata.auth.user)
          : null;
    }

    return user;
  }
}
