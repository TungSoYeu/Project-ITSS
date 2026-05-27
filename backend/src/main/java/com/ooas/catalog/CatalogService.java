package com.ooas.catalog;

import com.ooas.catalog.dto.InventoryRequest;
import com.ooas.catalog.dto.InventoryResponse;
import com.ooas.catalog.dto.SiteRequest;
import com.ooas.catalog.dto.SiteResponse;
import com.ooas.catalog.dto.SkuRequest;
import com.ooas.catalog.dto.SkuResponse;
import com.ooas.common.ApiException;
import com.ooas.domain.Site;
import com.ooas.domain.SiteInventory;
import com.ooas.domain.Sku;
import com.ooas.repository.SiteInventoryRepository;
import com.ooas.repository.SiteRepository;
import com.ooas.repository.SkuRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CatalogService {

    private final SkuRepository skuRepository;
    private final SiteRepository siteRepository;
    private final SiteInventoryRepository inventoryRepository;

    public CatalogService(SkuRepository skuRepository, SiteRepository siteRepository, SiteInventoryRepository inventoryRepository) {
        this.skuRepository = skuRepository;
        this.siteRepository = siteRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public List<SkuResponse> listSkus(String search) {
        return skuRepository.search(normalize(search)).stream().map(SkuResponse::from).toList();
    }

    @Transactional
    public SkuResponse createSku(SkuRequest request) {
        if (skuRepository.existsByCodeIgnoreCase(request.code())) {
            throw ApiException.conflict("Ma SKU da ton tai");
        }
        Sku sku = new Sku(request.code().trim().toUpperCase(), request.name().trim(), request.unit().trim(), request.description());
        return SkuResponse.from(skuRepository.save(sku));
    }

    @Transactional
    public SkuResponse updateSku(String id, SkuRequest request) {
        Sku sku = requireSku(id);
        skuRepository.findByCodeIgnoreCase(request.code())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw ApiException.conflict("Ma SKU da ton tai");
                });
        sku.setCode(request.code().trim().toUpperCase());
        sku.setName(request.name().trim());
        sku.setUnit(request.unit().trim());
        sku.setDescription(request.description());
        return SkuResponse.from(sku);
    }

    @Transactional(readOnly = true)
    public List<SiteResponse> listSites(Boolean active, String search) {
        return siteRepository.search(active, normalize(search)).stream()
                .map(site -> SiteResponse.from(site, inventoryRepository.countBySiteId(site.getId())))
                .toList();
    }

    @Transactional
    public SiteResponse createSite(SiteRequest request) {
        if (siteRepository.existsByCodeIgnoreCase(request.code())) {
            throw ApiException.conflict("Ma site da ton tai");
        }
        Site site = new Site(
                request.code().trim().toUpperCase(),
                request.name().trim(),
                request.country().trim(),
                request.seaLeadTime(),
                request.airLeadTime(),
                request.active() == null || request.active()
        );
        siteRepository.save(site);
        return SiteResponse.from(site, 0);
    }

    @Transactional
    public SiteResponse updateSite(String id, SiteRequest request) {
        Site site = requireSite(id);
        siteRepository.findByCodeIgnoreCase(request.code())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw ApiException.conflict("Ma site da ton tai");
                });
        site.setCode(request.code().trim().toUpperCase());
        site.setName(request.name().trim());
        site.setCountry(request.country().trim());
        site.setSeaLeadTime(request.seaLeadTime());
        site.setAirLeadTime(request.airLeadTime());
        if (request.active() != null) {
            site.setActive(request.active());
        }
        return SiteResponse.from(site, inventoryRepository.countBySiteId(site.getId()));
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getSiteInventory(String siteId) {
        requireSite(siteId);
        return inventoryRepository.findBySiteIdOrderBySkuCodeAsc(siteId).stream()
                .map(InventoryResponse::from)
                .toList();
    }

    @Transactional
    public InventoryResponse upsertInventory(String siteId, InventoryRequest request) {
        Site site = requireSite(siteId);
        Sku sku = requireSku(request.skuId());
        SiteInventory inventory = inventoryRepository.findBySiteIdAndSkuId(siteId, request.skuId())
                .orElseGet(() -> new SiteInventory(site, sku, 0));
        inventory.setQuantity(request.quantity());
        inventoryRepository.save(inventory);
        return InventoryResponse.from(inventory);
    }

    public Sku requireSku(String id) {
        return skuRepository.findById(id).orElseThrow(() -> ApiException.notFound("Khong tim thay SKU"));
    }

    public Site requireSite(String id) {
        return siteRepository.findById(id).orElseThrow(() -> ApiException.notFound("Khong tim thay site"));
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
