/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.model;

import java.io.Serializable;
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
@Table(name = "orderstagged")
@XmlRootElement
public class Orderstagged implements Serializable {

  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Basic(optional = false)
  @Column(name = "recordnumber")
  private Integer recordnumber;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 30)
  @Column(name = "marketordernumber")
  private String marketordernumber;
  @Column(name = "datetagged")
  @Temporal(TemporalType.TIMESTAMP)
  private Date datetagged;

  public Orderstagged() {
  }

  public Orderstagged(Integer recordnumber) {
    this.recordnumber = recordnumber;
  }

  public Orderstagged(Integer recordnumber, String marketordernumber) {
    this.recordnumber = recordnumber;
    this.marketordernumber = marketordernumber;
  }

  public Integer getRecordnumber() {
    return recordnumber;
  }

  public void setRecordnumber(Integer recordnumber) {
    this.recordnumber = recordnumber;
  }

  public String getMarketordernumber() {
    return marketordernumber;
  }

  public void setMarketordernumber(String marketordernumber) {
    this.marketordernumber = marketordernumber;
  }

  public Date getDatetagged() {
    return datetagged;
  }

  public void setDatetagged(Date datetagged) {
    this.datetagged = datetagged;
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
    if (!(object instanceof Orderstagged)) {
      return false;
    }
    Orderstagged other = (Orderstagged) object;
    if ((this.recordnumber == null && other.recordnumber != null) || (this.recordnumber != null && !this.recordnumber.equals(other.recordnumber))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "com.gotkcups.model.Orderstagged[ recordnumber=" + recordnumber + " ]";
  }
  
}
