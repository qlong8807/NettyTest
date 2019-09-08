package com.xml.server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name="Envelope")
@XmlAccessorType(XmlAccessType.FIELD)
public class HeartBeatEnvelope {

    @XmlElement(name = "Header")
    private HeartBeatHeader header;
    @XmlElement(name = "Body")
    private HeartBeatBody body;
}
