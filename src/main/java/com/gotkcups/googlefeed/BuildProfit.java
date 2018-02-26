/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.googlefeed;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.GridCoordinate;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.UpdateCellsRequest;
import static com.gotkcups.adhoc.ProfitCalculator.AVERAGE_TAX_RATE;
import com.gotkcups.configs.MainConfiguration;
import com.gotkcups.data.Constants;
import com.gotkcups.io.RestHelper;
import com.gotkcups.model.Metafield;
import static com.gotkcups.page.DocumentProcessor.BUNDLE_DISCOUNT;
import static com.gotkcups.page.DocumentProcessor.MARKUP_NON_TAXABLE;
import static com.gotkcups.page.DocumentProcessor.MARKUP_TAXABLE;
import com.gotkcups.repos.MetafieldJPARepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * @author rfteves
 */
@Component
@Profile("buildprofit")
public class BuildProfit implements CommandLineRunner {

  @Autowired
  private MainConfiguration config;
  @Autowired
  private GoogleProductsIds gpIds;
  @Autowired
  private GoogleShopping gs;
  @Autowired
  protected RestHelper restHelper;
  @Autowired
  private MetafieldJPARepository jpa;

  @Value("${sheets.sheetid.profit}")
  private String sheetId;
  @Value("${sheets.gridid.profit}")
  private Integer gridId;

  @Override
  public void run(String... strings) throws Exception {
    Map<String, Integer> ordersMap = this.buildOrders();
    List<Document> variants = this.buildVariants();
    this.updateProfitSheet(variants, ordersMap);
    new Thread(new Runnable() {
      public void run() {
        try {
          Thread.sleep(5000);
        } catch (InterruptedException ex) {
          Logger.getLogger(BuildProfit.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
      }
    }).start();
  }

  private void updateProfitSheet(List<Document> variants, Map<String, Integer> orders) throws IOException {
    Sheets service = gs.getSheetsService();
    List<Request> requests = new ArrayList<>();
    StringBuilder builder = new StringBuilder();
    int rowIndex = 1;
    for (Document variant : variants) {
      List<CellData> cellValues = new ArrayList<>();
      // ID
      builder.setLength(0);
      builder.append("shopify_us_");
      builder.append(variant.getLong(Constants.Product_Id));
      builder.append("_");
      builder.append(variant.getLong(Constants.Id));
      String gpId = builder.toString();
      cellValues.add(new CellData()
        .setUserEnteredValue(new ExtendedValue()
          .setStringValue(gpId)));
      // Description
      builder.setLength(0);
      builder.append(variant.getString(Constants.Title).replaceAll(",", ""));
      cellValues.add(new CellData()
        .setUserEnteredValue(new ExtendedValue()
          .setStringValue(builder.toString())));
      // Type
      cellValues.add(new CellData()
        .setUserEnteredValue(new ExtendedValue()
          .setStringValue(variant.getString(Constants.Product_Type))));
      // Price
      cellValues.add(new CellData()
        .setUserEnteredValue(new ExtendedValue()
          .setNumberValue(Double.parseDouble(variant.getString("price")))));
      // Profit
      boolean taxable = !variant.getBoolean("taxable");
      double price = Double.parseDouble(variant.getString(Constants.Price));
      double cost = 0;
      if (taxable) {
        cost = price * (MARKUP_TAXABLE + AVERAGE_TAX_RATE);
      } else {
        cost = price * MARKUP_NON_TAXABLE;
      }
      List<Metafield> metafields = jpa.findByOwnerid(variant.getLong(Constants.Product_Id));
      Metafield metarecord = null;
      for (Metafield meta : metafields) {
        if (meta.getKey().equalsIgnoreCase(Constants.Vendor)) {
          metarecord = meta;
          break;
        }
      }
      if (variant.getInteger(Constants.Inventory_Quantity) == 0) {
        cellValues.add(new CellData()
          .setUserEnteredValue(new ExtendedValue()
            .setNumberValue(0.00d)));
      } else if (metarecord == null) {
        double profit = Double.parseDouble(variant.getString("price")) * .10;
        profit = BigDecimal.valueOf(profit).setScale(2, RoundingMode.HALF_UP).doubleValue();
        cellValues.add(new CellData()
          .setUserEnteredValue(new ExtendedValue()
            .setNumberValue(profit)));
      } else {
        Document vendor = (Document) Document.parse(metarecord.getValue()).get(Constants.Vendor);
        double defaultshipping = 0;
        double discount = 0;
        double extraCost = 0;
        int minqty = 1;
        if (vendor != null) {
          if (vendor.containsKey(Constants.Default_Min_Quantity)) {
            minqty = vendor.getInteger(Constants.Default_Min_Quantity);
          }
          if (vendor.containsKey(Constants.DefaultShipping)) {
            defaultshipping = vendor.getDouble(Constants.DefaultShipping);
            defaultshipping = 0;
          }
          if (minqty >= 5 && price > 50) {
            discount = minqty * BUNDLE_DISCOUNT;
          }
          if (vendor.containsKey(Constants.ExtraCost)) {
            extraCost = minqty * vendor.getDouble(Constants.ExtraCost);
          }
        }
        cost -= defaultshipping;
        cost -= extraCost;
        cost += discount;
        double profit = Math.round((price - cost) * 100) * 0.01;
        profit = BigDecimal.valueOf(profit).setScale(2, RoundingMode.HALF_UP).doubleValue();
        cellValues.add(new CellData()
          .setUserEnteredValue(new ExtendedValue()
            .setNumberValue(profit)));
      }
      // Shopify Conversions
      double qty = 0;
      if (orders.containsKey(gpId)) {
        qty = orders.get(gpId);
      }
      cellValues.add(new CellData()
        .setUserEnteredValue(new ExtendedValue()
          .setNumberValue(qty)));
      /*
      // Budget, cost, conversions, bid
      cellValues.add(new CellData()
        .setUserEnteredValue(new ExtendedValue()
          .setStringValue("")));
      // Cost
      cellValues.add(new CellData()
        .setUserEnteredValue(new ExtendedValue()
          .setStringValue("")));
      // Bid
      cellValues.add(new CellData()
        .setUserEnteredValue(new ExtendedValue()
          .setStringValue("")));*/

      requests.add(new Request()
        .setUpdateCells(new UpdateCellsRequest()
          .setStart(new GridCoordinate()
            .setSheetId(gridId)
            .setRowIndex(rowIndex++)
            .setColumnIndex(0))
          .setRows(Arrays.asList(
            new RowData().setValues(cellValues)))
          .setFields("userEnteredValue,userEnteredFormat.backgroundColor")));
      if (requests.size() >= 100) {
        BatchUpdateSpreadsheetRequest request = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        service.spreadsheets().batchUpdate(sheetId, request)
          .execute();
        requests.clear();
      }
    }
//    if (requests.size() >0) {
//      BatchUpdateSpreadsheetRequest request = new BatchUpdateSpreadsheetRequest().setRequests(requests);
//      service.spreadsheets().batchUpdate(sheetId, request)
//        .execute();
//    }
    List<CellData> cellValues = new ArrayList<>();
    cellValues.add(new CellData()
      .setUserEnteredValue(new ExtendedValue()
        .setStringValue("" + new Date())));
    requests.add(new Request()
      .setUpdateCells(new UpdateCellsRequest()
        .setStart(new GridCoordinate()
          .setSheetId(gridId)
          .setRowIndex(rowIndex++)
          .setColumnIndex(0))
        .setRows(Arrays.asList(
          new RowData().setValues(cellValues)))
        .setFields("userEnteredValue,userEnteredFormat.backgroundColor")));
    BatchUpdateSpreadsheetRequest request = new BatchUpdateSpreadsheetRequest().setRequests(requests);
    service.spreadsheets().batchUpdate(sheetId, request)
      .execute();

  }

  private Map<String, Integer> buildOrders() throws IOException {
    Map<String, String> params = config.stringMap();
    String yesterday = config.getDateConverter().format(config.getCurrentDate(0, true));
    String monthago = config.getDateConverter().format(config.getCurrentDate(-31, false));
    params.put("created_at_min", monthago);
    params.put("created_at_max", yesterday);
    params.put("fields", "id,line_items");
    Document resp = restHelper.getAllOrders(params, 150, -1);
    List<Document> orders = (List) resp.get("orders");
    Map<String, Integer> ordersMap = config.stringIntMap();
    orders.stream().forEach(order -> {
      List<Document> lineItems = (List) order.get("line_items");
      lineItems.stream().forEach(lineItem -> {
        String id = "shopify_us_".concat(lineItem.getLong(Constants.Product_Id).toString()).concat("_").concat(lineItem.getLong(Constants.Variant_Id).toString());
        int qty = 0;
        if (ordersMap.containsKey(id)) {
          qty = ordersMap.get(id);
        }
        ordersMap.put(id, qty += lineItem.getInteger("quantity"));
      });
    });
    return ordersMap;
  }

  private List<Document> buildVariants() throws IOException {
    Set<Long> validIds = gpIds.getValidIds();
    Map<String, String> params = config.stringMap();
    params.put("fields", "id,title,product_type,variants");
    Map<String, Document> sortedVariants = new TreeMap<>();
    List<Document> variantsList = config.documentList();
    Document resp = restHelper.getAllProducts(params, 150, -1);
    List<Document> products = (List) resp.get("products");
    for (Document product : products) {
      if (!validIds.contains(product.getLong(Constants.Id))) {
        continue;
      }
      //if (product.getLong(Constants.Id) != 113695391767l)continue;
      List<Document> variants = (List) product.get("variants");
      for (Document variant : variants) {
        Document d = new Document();
        variant.put(Constants.Title, product.getString(Constants.Title));
        variant.put(Constants.Product_Type, product.getString(Constants.Product_Type));
        d.putAll(variant);
        sortedVariants.put(product.getString(Constants.Product_Type).concat(product.getString(Constants.Title)), d);
      }
    }
    sortedVariants.keySet().stream().forEach(key -> {
      variantsList.add(sortedVariants.get(key));
    });
    return variantsList;
  }

}
