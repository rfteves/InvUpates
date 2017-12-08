/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author rfteves
 */
@Entity
@Table(name = "keurigorders")
@XmlRootElement
public class Keurigorders implements Serializable {

  private static final long serialVersionUID = 1L;
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Basic(optional = false)
  @Column(name = "recordnumber")
  private Integer recordnumber;
  
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 60)
  @Column(name = "name")
  private String name;
  
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 50)
  @Column(name = "address")
  
  private String address;
  @Size(max = 50)
  @Column(name = "address2")
 
  private String address2;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 30)
  @Column(name = "city")
  
  private String city;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 20)
  @Column(name = "state")
  private String state;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 15)
  @Column(name = "zip")
  private String zip;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 30)
  @Column(name = "keurigordernumber")
  private String keurigordernumber;
  @Column(name = "transdate")
  @Temporal(TemporalType.DATE)
  private Date transdate;
  // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
  @Column(name = "amountpaid")
  private BigDecimal amountpaid;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 30)
  @Column(name = "marketordernumber")
  private String marketordernumber;
  @Size(max = 30)
  @Column(name = "account")
  private String account;
  @Size(max = 50)
  @Column(name = "company")
  private String company;
  // @Pattern(regexp="^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$", message="Invalid phone/fax format, should be as xxx-xxx-xxxx")//if the field contains phone or fax number consider using this annotation to enforce field validation
  @Size(max = 50)
  @Column(name = "phone")
  private String phone;
  @Column(name = "keu_subtotal")
  private BigDecimal keuSubtotal;
  @Column(name = "keu_shipping")
  private BigDecimal keuShipping;
  @Column(name = "keu_salestax")
  private BigDecimal keuSalestax;
  @Column(name = "keu_ordertotal")
  private BigDecimal keuOrdertotal;
  @Column(name = "keu_discount")
  private BigDecimal keuDiscount;
  @Size(max = 24)
  @Column(name = "card")
  private String card;

  public Keurigorders() {
  }

  public Keurigorders(Integer recordnumber) {
    this.recordnumber = recordnumber;
  }

  public Keurigorders(Integer recordnumber, String name, String address, String city, String state, String zip, String keurigordernumber, String marketordernumber) {
    this.recordnumber = recordnumber;
    this.name = name;
    this.address = address;
    this.city = city;
    this.state = state;
    this.zip = zip;
    this.keurigordernumber = keurigordernumber;
    this.marketordernumber = marketordernumber;
  }

  public Integer getRecordnumber() {
    return recordnumber;
  }

  public void setRecordnumber(Integer recordnumber) {
    this.recordnumber = recordnumber;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getAddress2() {
    return address2;
  }

  public void setAddress2(String address2) {
    this.address2 = address2;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getZip() {
    return zip;
  }

  public void setZip(String zip) {
    this.zip = zip;
  }

  public String getKeurigordernumber() {
    return keurigordernumber;
  }

  public void setKeurigordernumber(String keurigordernumber) {
    this.keurigordernumber = keurigordernumber;
  }

  public Date getTransdate() {
    return transdate;
  }

  public void setTransdate(Date transdate) {
    this.transdate = transdate;
  }

  public BigDecimal getAmountpaid() {
    return amountpaid;
  }

  public void setAmountpaid(BigDecimal amountpaid) {
    this.amountpaid = amountpaid;
  }

  public String getMarketordernumber() {
    return marketordernumber;
  }

  public void setMarketordernumber(String marketordernumber) {
    this.marketordernumber = marketordernumber;
  }

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public BigDecimal getKeuSubtotal() {
    return keuSubtotal;
  }

  public void setKeuSubtotal(BigDecimal keuSubtotal) {
    this.keuSubtotal = keuSubtotal;
  }

  public BigDecimal getKeuShipping() {
    return keuShipping;
  }

  public void setKeuShipping(BigDecimal keuShipping) {
    this.keuShipping = keuShipping;
  }

  public BigDecimal getKeuSalestax() {
    return keuSalestax;
  }

  public void setKeuSalestax(BigDecimal keuSalestax) {
    this.keuSalestax = keuSalestax;
  }

  public BigDecimal getKeuOrdertotal() {
    return keuOrdertotal;
  }

  public void setKeuOrdertotal(BigDecimal keuOrdertotal) {
    this.keuOrdertotal = keuOrdertotal;
  }

  public BigDecimal getKeuDiscount() {
    return keuDiscount;
  }

  public void setKeuDiscount(BigDecimal keuDiscount) {
    this.keuDiscount = keuDiscount;
  }

  public String getCard() {
    return card;
  }

  public void setCard(String card) {
    this.card = card;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (recordnumber != null ? recordnumber.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof Keurigorders)) {
      return false;
    }
    Keurigorders other = (Keurigorders) object;
    if ((this.recordnumber == null && other.recordnumber != null) || (this.recordnumber != null && !this.recordnumber.equals(other.recordnumber))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "com.gotkcups.model.Keurigorders[ recordnumber=" + recordnumber + " ]";
  }
  
}
