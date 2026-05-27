package com.ooas.service;

import com.ooas.dto.InventoryRequest;
import com.ooas.dto.InventoryResponse;
import com.ooas.dto.SiteRequest;
import com.ooas.dto.SiteResponse;
import com.ooas.dto.SkuRequest;
import com.ooas.dto.SkuResponse;
import com.ooas.entity.Site;
import com.ooas.entity.Sku;
import java.util.List;
import org.springframework.lang.NonNull;

public interface CatalogService {
    List<SkuResponse> listSkus(String search);
    SkuResponse createSku(SkuRequest request);
    SkuResponse updateSku(@NonNull String id, SkuRequest request);
    List<SiteResponse> listSites(Boolean active, String search);
    SiteResponse createSite(SiteRequest request);
    SiteResponse updateSite(@NonNull String id, SiteRequest request);
    List<InventoryResponse> getSiteInventory(@NonNull String siteId);
    InventoryResponse upsertInventory(@NonNull String siteId, InventoryRequest request);
    Sku requireSku(@NonNull String id);
    Site requireSite(@NonNull String id);
}
