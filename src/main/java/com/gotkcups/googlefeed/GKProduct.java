/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.googlefeed;

import com.google.api.services.content.model.Price;
import com.google.api.services.content.model.Product;
import com.google.api.services.content.model.ProductShippingWeight;
import com.google.api.services.content.model.ProductTax;
import com.gotkcups.data.Constants;
import com.gotkcups.io.RestHelper;
import com.gotkcups.io.Utilities;
import com.gotkcups.model.Metafield;
import com.gotkcups.repos.MetafieldJPARepository;
import com.gotkcups.repos.MetafieldRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 *
 * @author Ricardo
 */
@Service
public class GKProduct {

  @Autowired
  private RestHelper restHelper;
  @Autowired
  private MetafieldRepository repo;
  @Autowired
  private UpdateStateTaxes taxes;
  @Autowired
  private MetafieldJPARepository jpa;

  private String itemId;
  private Document product, variant, vendor;
  private List<Document> metafields, variants;
  private long productId, variantId;
  private Product productEntry;

  public GKProduct() {
  }

  public GKProduct setProductVariant(Document product, List<Document> variants, Long variantId) {
    productEntry = new Product();
    this.variantId = variantId;
    this.productId = product.getLong(Constants.Id);
    this.itemId = String.format("shopify_US_%d_%d", productId, variantId);
    this.product = product;
    this.variants = variants;
    List<Metafield> metas = jpa.findByOwnerid(productId);
    metafields = new ArrayList<>();
    metas.stream().forEach(meta -> {
      Document d = Task.createDocument(meta);
      metafields.add(d);
    });
    variant = variants.stream().filter(var -> var.getLong(Constants.Id) == variantId).findFirst().get();
    return this;
  }

  public Product buildProduct() {
    productEntry.setOfferId(String.format("shopify_US_%s_%s",
      product.getLong(Constants.Id).toString(),
      variant.getLong(Constants.Id)));
    productEntry.setId(String.format("online:en:US:%s", productEntry.getOfferId()));
    productEntry.setAdwordsRedirect(String.format("https://www.gotkcups.com/products/%s?utm_medium=cpc&utm_source=googlepla&variant=%s",
      product.getString(Constants.Handle),
      variant.getLong(Constants.Id)));
    productEntry.setLink(String.format("https://www.gotkcups.com/products/%s?utm_medium=cpc&utm_source=googleshopping&variant=%s",
      product.getString(Constants.Handle),
      variant.getLong(Constants.Id)));
    productEntry.setAvailability(variant.getInteger(Constants.Inventory_Quantity) == 0 ? "out of stock" : "in stock");
    if (variant.getString(Constants.Barcode) == null || variant.getString(Constants.Barcode).trim().length() == 0) {
      
    } else {
      productEntry.setBrand(product.getString(Constants.Vendor));
    }
    productEntry.setChannel(Constants.Online);
    productEntry.setContentLanguage(Constants.En);
    if (variant.getString(Constants.Barcode) == null || variant.getString(Constants.Barcode).trim().length() == 0) {
      productEntry.setIdentifierExists(false);
    } else {
      productEntry.setGtin(variant.getString(Constants.Barcode));
    }
    productEntry.setKind(Constants.Content_Product);
    Price price = new Price();
    price.setCurrency("USD");
    price.setValue(variant.getString(Constants.Price));
    productEntry.setPrice(price);

    ProductShippingWeight weight = new com.google.api.services.content.model.ProductShippingWeight();
    weight.setUnit("g");
    weight.setValue(Double.valueOf(variant.getInteger(Constants.Grams)));
    productEntry.setShippingWeight(weight);
    productEntry.setTargetCountry(Constants.US);

    metafields.stream().forEach(meta -> {
      //metafieldRepo.save(m);
      if (meta.getString(Constants.Namespace).equals(Constants.Google)) {
        if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Adwords_Grouping)) {
          productEntry.setAdwordsGrouping(meta.getString(Constants.Value));
        } else if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Adwords_Labels)) {
          List<String> labels = new ArrayList<>();
          labels.add(meta.getString(Constants.Value));
          productEntry.setAdwordsLabels(labels);
        } else if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Condition)) {
          productEntry.setCondition(meta.getString(Constants.Value));
        } else if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Google_Product_Type)) {
          productEntry.setGoogleProductCategory(Utilities.properNamePloy(meta.getString(Constants.Value)));
        } else if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Age_Group)) {
          productEntry.setAgeGroup(meta.getString(Constants.Value));
        } else if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Gender)) {
          productEntry.setGender(meta.getString(Constants.Value));
        } else if (meta.getString(Constants.Key).startsWith("custom_label_")) {
          if (meta.getString(Constants.Key).endsWith("0")) {
            productEntry.setCustomLabel0(meta.getString(Constants.Value));
          } else if (meta.getString(Constants.Key).endsWith("1")) {
            productEntry.setCustomLabel1(meta.getString(Constants.Value));
          } else if (meta.getString(Constants.Key).endsWith("2")) {
            productEntry.setCustomLabel2(meta.getString(Constants.Value));
          } else if (meta.getString(Constants.Key).endsWith("3")) {
            productEntry.setCustomLabel3(meta.getString(Constants.Value));
          } else if (meta.getString(Constants.Key).endsWith("4")) {
            productEntry.setCustomLabel4(meta.getString(Constants.Value));
          }
        }
      } else if (meta.getString(Constants.Namespace).equals(Constants.Global)) {
        if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Description_Tag)) {
          productEntry.setDescription(meta.getString(Constants.Value));
        } else if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Title_Tag)) {
          productEntry.setTitle(meta.getString(Constants.Value));
        }
      } else if (meta.getString(Constants.Namespace).equals(Constants.Inventory)) {
        Document vendor = (Document) Document.parse(meta.getString(Constants.Value)).get("vendor");
        if (vendor.containsKey(Constants.Default_Min_Quantity) && vendor.getInteger(Constants.Default_Min_Quantity) > 1) {
          productEntry.setMultipack((long) vendor.getInteger(Constants.Default_Min_Quantity));
        }
      }
    });
    // Taxes
    this.initTaxes();
    // Images
    this.initImages();
    // Options
    // Final check
    if (productEntry.getDescription() == null) {
      String html = Jsoup.parse(product.getString(Constants.Body_Html)).text();
      productEntry.setDescription(html);
    }
    if (productEntry.getTitle() == null) {
      productEntry.setTitle(product.getString(Constants.Title));
    }
    if (productEntry.getCondition() == null) {
      productEntry.setCondition(Constants.New);
    }
    productEntry.setTargetCountry("US");
    this.initOptions();
    if(product.containsKey("promotionIds")) {
      productEntry.setPromotionIds((List)product.get("promotionIds"));
    }
    return productEntry;
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
          productEntry.setColor(value.toString());
          productEntry.setItemGroupId(String.format("shopify_US_%s",
            product.getLong(Constants.Id).toString()));
          productEntry.setTitle(productEntry.getTitle().concat(" ").concat(value.toString()));
        } else {
          variants.stream()
            .filter(
              var -> var.getLong(Constants.Id).equals(this.variantId))
            .map(var -> var.getString("option2"))
            .forEach(sz -> {
              List<String> sizes = new ArrayList<>();
              sizes.add(sz);
              productEntry.setSizes(sizes);
              productEntry.setTitle(productEntry.getTitle().concat(" ").concat(sz));
            });
        }
      });
    }
  }

  private void initImages() {
    /*if (variant.getLong(Constants.Id) == 47372547274L
      || variant.getLong(Constants.Id) == 47372530570L
      || variant.getLong(Constants.Id) == 47372544522L) {
      int y = 0;
    }*/
    // Assign primary image
    List<Document> images = (List) product.get(Constants.Images);
    if (variants.size() > 1) {
      images.stream()
        .filter(image -> image.getLong(Constants.Id).longValue() == variant.getLong(Constants.Image_Id))
        .forEach(image -> productEntry.setImageLink(image.getString(Constants.Src)));
      // Assign secondary images which is none existent right now
    } else {
      List<String> additionalImageLinks = new ArrayList<>();
      images.stream().map(img-> img.getString(Constants.Src)).forEach(additionalImageLinks::add);
      if (!additionalImageLinks.isEmpty()) {
        productEntry.setImageLink(additionalImageLinks.remove(0));
        productEntry.setAdditionalImageLinks(additionalImageLinks);
      }
      int y= 0;
    }
  }

  private void initTaxes() {
    if (variant.getBoolean(Constants.Taxable)) {
      productEntry.setTaxes(taxes.getTaxes());
    }
  }
}
