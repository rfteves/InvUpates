/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author rfteves
 */
@XmlType(name = "ComplexType")
public class KeurigAnchor {
    private String anchor;
    private String dataColorPickerName,
            dataCode,
            dataPrice,
            dataPurchasable,
            disabled; 

    /**
     * @return the anchor
     */
    public String getAnchor() {
        return anchor;
    }

    /**
     * @param anchor the anchor to set
     */
    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    /**
     * @return the dataColorPickerName
     */
    @XmlAttribute(name="data-color-picker-name")
    public String getDataColorPickerName() {
        return dataColorPickerName;
    }

    /**
     * @param dataColorPickerName the dataColorPickerName to set
     */
    public void setDataColorPickerName(String dataColorPickerName) {
        this.dataColorPickerName = dataColorPickerName;
    }

    /**
     * @return the dataSwatchColor
     */
    @XmlAttribute(name="data-swatch-color")
    public String getDataCode() {
        return dataCode;
    }

    /**
     * @param dataSwatchColor the dataSwatchColor to set
     */
    public void setDataCode(String dataCode) {
        this.dataCode = dataCode;
    }

    /**
     * @return the dataPrice
     */
    @XmlAttribute(name="data-price")
    public String getDataPrice() {
        return dataPrice;
    }

    /**
     * @param dataPrice the dataPrice to set
     */
    public void setDataPrice(String dataPrice) {
        this.dataPrice = dataPrice;
    }

    /**
     * @return the dataPurchasable
     */
    @XmlAttribute(name="data-purchasable")
    public String getDataPurchasable() {
        return dataPurchasable;
    }

    /**
     * @param dataPurchasable the dataPurchasable to set
     */
    public void setDataPurchasable(String dataPurchasable) {
        this.dataPurchasable = dataPurchasable;
    }

    /**
     * @return the disabled
     */
    public String getDisabled() {
        return disabled;
    }

    /**
     * @param disabled the disabled to set
     */
    public void setDisabled(String disabled) {
        this.disabled = disabled;
    }
    
}
