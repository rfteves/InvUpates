/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.googlefeed;

import com.gotkcups.data.Constants;
import com.gotkcups.io.GateWay;
import com.gotkcups.io.Utilities;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.bson.Document;
import org.jsoup.Jsoup;

/**
 *
 * @author Ricardo
 */
public class GKProduct extends Document {

  private String itemId;
  private Document product, variant;
  private List<Document> metafields, variants;
  private long productId, variantId;

  public GKProduct(String itemId) {
    this.itemId = itemId;
    initializeItemId();
  }

  public GKProduct(Document product, List<Document> variants, Long variantId) {
    this.variantId = variantId;
    this.productId = product.getLong(Constants.Id);
    this.itemId = String.format("shopify_US_%d_%d", productId, variantId);
    this.product = product;
    this.variants = variants;
    String resp = GateWay.getProductMetafields(Constants.Production, productId);
    Document metas = Document.parse(resp);
    metafields = (List) metas.get(Constants.Metafields);
    variant = variants.stream().filter(var -> var.getLong(Constants.Id) == variantId).findFirst().get();
  }

  private void initializeItemId() {
    String[] ids = itemId.split("_");
    productId = Long.parseLong(ids[2]);
    variantId = Long.parseLong(ids[3]);
    String resp = GateWay.getProduct(Constants.Production, productId);
    Document result = Document.parse(resp);
    product = (Document) result.get(Constants.Product);
    resp = GateWay.getProductMetafields(Constants.Production, productId);
    Document metas = Document.parse(resp);
    metafields = (List) metas.get(Constants.Metafields);
    variants = (List) product.get(Constants.Variants);
    variant = variants.stream().filter(var -> var.getLong(Constants.Id) == variantId).findFirst().get();
  }

  public Document buildProduct() {
    this.append(Constants.OfferId, String.format("shopify_US_%s_%s",
      product.getLong(Constants.Id).toString(),
      variant.getLong(Constants.Id)));
    if (this.toJson().contains("73895936023")) {
      int imagelinks = 0;
    }
    this.append(Constants.Id, String.format("online:en:US:%s", this.getString(Constants.OfferId)));
    this.append(Constants.AdwordsRedirect,
      String.format("https://www.gotkcups.com/products/%s?utm_medium=cpc&utm_source=googlepla&variant=%s",
        product.getString(Constants.Handle),
        variant.getLong(Constants.Id)));
    this.append(Constants.Link,
      String.format("https://www.gotkcups.com/products/%s?utm_medium=cpc&utm_source=googleshopping&variant=%s",
        product.getString(Constants.Handle),
        variant.getLong(Constants.Id)));
    this.append(Constants.Availability, variant.getInteger(Constants.Inventory_Quantity) == 0 ? "out of stock" : "in stock");
    this.append(Constants.Brand, product.getString(Constants.Vendor));
    this.append(Constants.Channel, Constants.Online);
    this.append(Constants.ContentLanguage, Constants.En);
    this.append(Constants.GTIN, variant.getString(Constants.Barcode));
    this.append(Constants.IdentifierExists, true);
    this.append(Constants.Kind, Constants.Content_Product);
    Document price = new Document("currency", "USD");
    price.append(Constants.Value, variant.getString(Constants.Price));
    this.append(Constants.Price, price);
    this.append(Constants.ProductType, product.getString(Constants.Product_Type));
    Document weight = new Document("unit", "g");
    weight.append(Constants.Value, Double.valueOf(variant.getInteger(Constants.Grams)));
    this.append(Constants.ShippingWeight, weight);
    this.append(Constants.TargetCountry, Constants.US);

    metafields.stream().forEach(meta -> {
      if (meta.getString(Constants.Namespace).equals(Constants.Google)) {
        if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Adwords_Grouping)) {
          this.append(Constants.AdwordsGrouping, meta.getString(Constants.Value));
        } else if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Adwords_Labels)) {
          List<String> labels = new ArrayList<>();
          labels.add(meta.getString(Constants.Value));
          this.append(Constants.AdwordsLabels, labels);
        } else if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Condition)) {
          this.append(Constants.Condition, meta.getString(Constants.Value));
        } else if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Google_Product_Type)) {
          this.append(Constants.GoogleProductCategory,
            Utilities.properNamePloy(meta.getString(Constants.Value)));
        } else if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Age_Group)) {
          this.append(Constants.AgeGroup, meta.getString(Constants.Value));
        } else if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Gender)) {
          this.append(Constants.Gender, meta.getString(Constants.Value));
        } else if (meta.getString(Constants.Key).startsWith("custom_label_")) {
          String key = meta.getString(Constants.Key);
          this.append("customLabel".concat(key.substring(key.length() - 1)), meta.getString(Constants.Value));
        }
      } else if (meta.getString(Constants.Namespace).equals(Constants.Global)) {
        if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Description_Tag)) {
          this.append(Constants.Description, meta.getString(Constants.Value));
        } else if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Title_Tag)) {
          this.append(Constants.Title, meta.getString(Constants.Value));
        }
      }
    });
    // Taxes
    this.initTaxes();
    // Images
    this.initImages();
    // Options
    this.initOptions();
    // Final check
    if (!this.containsKey(Constants.Description)) {
      String html = Jsoup.parse(product.getString(Constants.Body_Html)).text();
      this.append(Constants.Description, html);
    }
    if (!this.containsKey(Constants.Title)) {
      this.append(Constants.Title, product.getString(Constants.Title));
    }
    if (!this.containsKey(Constants.Condition)) {
      this.append(Constants.Condition, Constants.New);
    }
    this.append("targetCountry", "US");
    return this;
  }

  private void initOptions() {
    List<Document> options = (List) product.get(Constants.options);
    for (String name : new String[]{"color", "size"}) {
      options.stream().filter(option -> option.getString(Constants.Name)
        .toLowerCase().contains(name)).forEach(option
        -> {
        String key = option.getString(Constants.Name).toLowerCase().contains(Constants.Color) ? Constants.Color : Constants.Sizes;
        Object value = null;
        if (key.equalsIgnoreCase(Constants.Color)) {
          value = variant.getString(String.format("option1"));
          this.append(key, value);
        } else {
          List<String> available = new ArrayList<>();
          this.append("sizes", available);
          value = available;
          variants.stream()
            .filter(
              var
              -> var.getInteger("inventory_quantity") > 0
              && var.getString("option1").equals(this.getString("color")))
            .map(var -> var.getString("option2"))
            .forEach(available::add);
        }
      });
    }
  }

  private void initImages() {
    // Assign primary image
    List<Document> images = (List) product.get(Constants.Images);
    images.stream()
      .filter(image -> image.getInteger(Constants.Position).equals(variant.getInteger(Constants.Position)))
      .forEach(image -> this.append(Constants.ImageLink, image.getString(Constants.Src)));
    // Assign secondary images which is none existent right now
    if (variants.size() > 1) {
      return;
    }
    List<String> additionalImageLinks = new ArrayList<>();
    images.stream().forEach(image -> {
      List<Long> variant_ids = (List) image.get(Constants.Variant_Ids);
      if (variant_ids.isEmpty() || variant_ids.contains(variant.getLong(Constants.Id))) {
        if (!image.getString(Constants.Src).equals(this.getString(Constants.ImageLink))
          && !additionalImageLinks.contains(image.getString(Constants.Src))) {
          additionalImageLinks.add(image.getString(Constants.Src));
        }
      }
    });
    if (!additionalImageLinks.isEmpty()) {
      this.append(Constants.AdditionalImageLinks, additionalImageLinks);
    }
  }

  static List<Document> taxes = new ArrayList<>();

  private void initTaxes() {
    if (taxes.isEmpty()) {
      Document tax = new Document();
      tax.append("country", "US");
      tax.append("rate", 0.0);
      tax.append("taxShip", false);
      taxes.add(tax);
      String countries = GateWay.getCountries(Constants.Production);
      Document d = Document.parse(countries);
      List<Document> c = (List) d.get(Constants.Countries);
      List<Document> p = (List) c.get(0).get(Constants.Provinces);
      p.stream().forEach(new Consumer<Document>() {
        @Override
        public void accept(Document ar) {
          double rate = ar.getDouble(Constants.Tax) * 100;
          if (rate != 0) {
            rate = BigDecimal.valueOf(rate).setScale(2, RoundingMode.HALF_UP).doubleValue();
          }
          Document tx = new Document();
          tx.append(Constants.Country, Constants.US);
          tx.append(Constants.Rate, rate);
          tx.append(Constants.Region, ar.getString(Constants.Code));
          tx.append(Constants.TaxShip, rate != 0);
          taxes.add(tx);
        }
      });
    }
    if (variant.getBoolean(Constants.Taxable)) {
      this.append("taxes", taxes);
    }
  }
}
