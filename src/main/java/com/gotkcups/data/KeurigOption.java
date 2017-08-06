/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 *
 * @author rfteves
 */
@XmlType(name = "ComplexType")
public class KeurigOption {

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
    
    private String dataPrice, dataCount, dataStock, dataPurchasable, dataCode, disabled;
    private String option;

    /**
     * @return the option
     */
    public String getOption() {
        return option;
    }

    /**
     * @param option the option to set
     */
    public void setOption(String option) {
        this.option = option;
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
     * @return the dataCount
     */
    @XmlAttribute(name="data-count")
    public String getDataCount() {
        return dataCount;
    }

    /**
     * @param dataCount the dataCount to set
     */
    public void setDataCount(String dataCount) {
        this.dataCount = dataCount;
    }

    /**
     * @return the dataStock
     */
    @XmlAttribute(name="data-stock")
    public String getDataStock() {
        return dataStock;
    }

    /**
     * @param dataStock the dataStock to set
     */
    public void setDataStock(String dataStock) {
        this.dataStock = dataStock;
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
     * @return the dataCode
     */
    @XmlAttribute(name="data-code")
    public String getDataCode() {
        return dataCode;
    }

    /**
     * @param dataCode the dataCode to set
     */
    public void setDataCode(String dataCode) {
        this.dataCode = dataCode;
    }
}
