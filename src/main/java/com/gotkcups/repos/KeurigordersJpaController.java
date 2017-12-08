/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.repos;

import com.gotkcups.model.Keurigorders;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author rfteves
 */
public interface KeurigordersJpaController extends JpaRepository<Keurigorders, Long> {

  @Query("SELECT k FROM Keurigorders k WHERE k.marketordernumber = :marketordernumber")
  public List<Keurigorders> findByMarketordernumber(@Param("marketordernumber") String marketordernumber);
}
