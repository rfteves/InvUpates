/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.repos;

import com.gotkcups.model.Fedexstates;
import com.gotkcups.model.Keurigorders;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author rfteves
 */
public interface FedexstatesJpaRepository extends JpaRepository<Fedexstates, Long> {

  //@Query("SELECT f FROM Fedexstates f WHERE f.abbreviation= :abbreviation")
  public List<Fedexstates> findByAbbreviation(/*@Param("abbreviation")*/ String abbreviation);
}
