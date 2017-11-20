package com.gotkcups.repos;

import com.gotkcups.model.Metafield;
import java.util.List;
import org.bson.Document;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MetafieldJPARepository extends JpaRepository<Metafield, Long> {

  public List<Metafield> findByOwnerid(Long ownerid);
}
