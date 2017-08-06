/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.data;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author rfteves
 */
@XmlRootElement(name="span")
public class KeurigSpan {
    private List<KeurigAnchor>anchor = new ArrayList<>();

    /**
     * @return the anchor
     */
    @XmlElement(name="a")
    public List<KeurigAnchor> getAnchor() {
        return anchor;
    }

    /**
     * @param anchor the a to set
     */
    public void setAnchor(List<KeurigAnchor> anchor) {
        this.anchor = anchor;
    }
}
