/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.data;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 *
 * @author rfteves
 */
@XmlRootElement(name="select")
public class KeurigSelect {

    
    private List<KeurigOption>option = new ArrayList<>();
    private String id, style;

    /**
     * @return the options
     */
    @XmlElement(name="option")
    public List<KeurigOption> getOption() {
        return option;
    }

    /**
     * @param options the options to set
     */
    public void setOptions(List<KeurigOption> option) {
        this.option = option;
    }

    /**
     * @param option the option to set
     */
    public void setOption(List<KeurigOption> option) {
        this.option = option;
    }

    /**
     * @return the id
     */
    @XmlAttribute
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the style
     */
    @XmlAttribute
    public String getStyle() {
        return style;
    }

    /**
     * @param style the style to set
     */
    public void setStyle(String style) {
        this.style = style;
    }
}
