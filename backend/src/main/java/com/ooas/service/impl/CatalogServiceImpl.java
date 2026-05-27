package com.ooas.service.impl;

import com.ooas.service.CatalogService;

import com.ooas.dto.InventoryRequest;
import com.ooas.dto.InventoryResponse;
import com.ooas.dto.SiteRequest;
import com.ooas.dto.SiteResponse;
import com.ooas.dto.SkuRequest;
import com.ooas.dto.SkuResponse;
import com.ooas.exception.ApiException;
import com.ooas.entity.Site;
import com.ooas.entity.SiteInventory;
import com.ooas.entity.Sku;
import com.ooas.repository.SiteInventoryRepository;
import com.ooas.repository.SiteRepository;
import com.ooas.repository.SkuRepository;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CatalogServiceImpl implements CatalogService {

    private final SkuRepository skuRepository;
    private final SiteRepository siteRepository;
    private final SiteInventoryRepository inventoryRepository;

    public CatalogServiceImpl(SkuRepository skuRepository, SiteRepository siteRepository, SiteInventoryRepository inventoryRepository) {
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
    public SkuResponse updateSku(@NonNull String id, SkuRequest request) {
        Sku sku = requireSku(id);
        skuRepository.findByCodeIgnoreCase(java.util.Objects.requireNonNull(request.code()))
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
    public SiteResponse updateSite(@NonNull String id, SiteRequest request) {
        Site site = requireSite(id);
        siteRepository.findByCodeIgnoreCase(java.util.Objects.requireNonNull(request.code()))
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
    public List<InventoryResponse> getSiteInventory(@NonNull String siteId) {
        requireSite(siteId);
        return inventoryRepository.findBySiteIdOrderBySkuCodeAsc(siteId).stream()
                .map(InventoryResponse::from)
                .toList();
    }

    @Transactional
    public InventoryResponse upsertInventory(@NonNull String siteId, InventoryRequest request) {
        Site site = requireSite(siteId);
        Sku sku = requireSku(java.util.Objects.requireNonNull(request.skuId()));
        SiteInventory inventory = inventoryRepository.findBySiteIdAndSkuId(siteId, java.util.Objects.requireNonNull(request.skuId()))
                .orElseGet(() -> new SiteInventory(site, sku, 0));
        inventory.setQuantity(request.quantity());
        inventoryRepository.save(inventory);
        return InventoryResponse.from(inventory);
    }

    public Sku requireSku(@NonNull String id) {
        return skuRepository.findById(id).orElseThrow(() -> ApiException.notFound("Khong tim thay SKU"));
    }

    public Site requireSite(@NonNull String id) {
        return siteRepository.findById(id).orElseThrow(() -> ApiException.notFound("Khong tim thay site"));
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
