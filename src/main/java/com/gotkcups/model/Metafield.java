/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;




/**
 *
 * @author rfteves
 */
@Entity
@Table(name = "metafield")
public class Metafield {
  @Id
  private long id;
  private String namespace;
  @Column(name="key_")
  private String key;
  private String value;
  private String valueType;
  private String description;
  @Column(name="owner_id")
  private long ownerid;
  private String created_at;
  private String updated_at;
  private String owner_resource;
  
  

  /**
   * @return the id
   */
  public long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(long id) {
    this.id = id;
  }

  /**
   * @return the namespace
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * @param namespace the namespace to set
   */
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  /**
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * @param key the key to set
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * @return the valueType
   */
  public String getType() {
    return valueType;
  }

  /**
   * @param type the valueType to set
   */
  public void setType(String type) {
    this.valueType = type;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the owner_id
   */
  public long getOwnerid() {
    return ownerid;
  }

  /**
   * @param owner_id the owner_id to set
   */
  public void setOwnerid(long ownerid) {
    this.ownerid = ownerid;
  }

  /**
   * @return the created_at
   */
  public String getCreated_at() {
    return created_at;
  }

  /**
   * @param created_at the created_at to set
   */
  public void setCreated_at(String created_at) {
    this.created_at = created_at;
  }

  /**
   * @return the updated_at
   */
  public String getUpdated_at() {
    return updated_at;
  }

  /**
   * @param updated_at the updated_at to set
   */
  public void setUpdated_at(String updated_at) {
    this.updated_at = updated_at;
  }

  /**
   * @return the owner_resource
   */
  public String getOwner_resource() {
    return owner_resource;
  }

  /**
   * @param owner_resource the owner_resource to set
   */
  public void setOwner_resource(String owner_resource) {
    this.owner_resource = owner_resource;
  }
}
