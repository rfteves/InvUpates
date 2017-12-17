/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.googlefeed;

import com.gotkcups.data.Constants;
import com.gotkcups.model.Metafield;
import org.bson.Document;
import org.springframework.stereotype.Component;

/**
 *
 * @author rfteves
 */

public abstract class Task {
  public abstract void process(String... args) throws Exception;

  public static Metafield createMetafield(Document meta) {
    Metafield metafield = new Metafield();
    metafield.setId(meta.getLong(Constants.Id));
    metafield.setCreated_at(meta.getString(Constants.Created_At));
    metafield.setDescription(meta.getString(Constants.Description));
    metafield.setKey(meta.getString(Constants.Key));
    metafield.setNamespace(meta.getString(Constants.Namespace));
    metafield.setOwnerid(meta.getLong(Constants.Owner_Id));
    metafield.setOwner_resource(meta.getString(Constants.Owner_Resource));
    metafield.setType(meta.getString(Constants.Value_Type));
    metafield.setUpdated_at(meta.getString(Constants.Updated_At));
    metafield.setValue(meta.getString(Constants.Value));
    return metafield;
  }
  
  public static Document createDocument(Metafield meta) {
    Document metafield = new Document();
    metafield.append(Constants.Id, meta.getId());
    metafield.append(Constants.Created_At, meta.getCreated_at());
    metafield.append(Constants.Description, meta.getDescription());
    metafield.append(Constants.Key, meta.getKey());
    metafield.append(Constants.Namespace, meta.getNamespace());
    metafield.append(Constants.Owner_Id, meta.getOwnerid());
    metafield.append(Constants.Owner_Resource, meta.getOwner_resource());
    metafield.append(Constants.Value_Type, meta.getType());
    metafield.append(Constants.Updated_At, meta.getUpdated_at());
    metafield.append(Constants.Value, meta.getValue());
    return metafield;
  }
}
