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
@Table(name = "fedexstates")
@XmlRootElement
public class Fedexstates implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @Basic(optional = false)
  @Column(name = "abbreviation")
  private String abbreviation;

  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 60)
  @Column(name = "statename")
  private String statename;

  @Basic(optional = false)
  @NotNull
  @Column(name = "indexoption")
  private Integer indexoption;

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (abbreviation != null ? abbreviation.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof Fedexstates)) {
      return false;
    }
    Fedexstates other = (Fedexstates) object;
    if ((this.abbreviation == null && other.abbreviation != null) || (this.abbreviation != null && !this.abbreviation.equals(other.abbreviation))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "com.gotkcups.model.Fedexstates[ recordnumber=" + abbreviation + " ]";
  }

  /**
   * @return the abbreviation
   */
  public String getAbbreviation() {
    return abbreviation;
  }

  /**
   * @param abbreviation the abbreviation to set
   */
  public void setAbbreviation(String abbreviation) {
    this.abbreviation = abbreviation;
  }

  /**
   * @return the statename
   */
  public String getStatename() {
    return statename;
  }

  /**
   * @param statename the statename to set
   */
  public void setStatename(String statename) {
    this.statename = statename;
  }

  /**
   * @return the indexoption
   */
  public Integer getIndexoption() {
    return indexoption;
  }

  /**
   * @param indexoption the indexoption to set
   */
  public void setIndexoption(Integer indexoption) {
    this.indexoption = indexoption;
  }

}
