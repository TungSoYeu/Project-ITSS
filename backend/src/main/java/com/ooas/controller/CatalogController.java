package com.ooas.controller;

import com.ooas.dto.InventoryRequest;
import com.ooas.dto.InventoryResponse;
import com.ooas.dto.SiteRequest;
import com.ooas.dto.SiteResponse;
import com.ooas.dto.SkuRequest;
import com.ooas.dto.SkuResponse;
import com.ooas.service.CatalogService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/skus")
    public List<SkuResponse> listSkus(@RequestParam(required = false) String search) {
        return catalogService.listSkus(search);
    }

    @PostMapping("/skus")
    @PreAuthorize("hasAnyAuthority('ADMIN','OVERSEAS_ORDER')")
    public SkuResponse createSku(@Valid @RequestBody SkuRequest request) {
        return catalogService.createSku(request);
    }

    @PutMapping("/skus/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','OVERSEAS_ORDER')")
    public SkuResponse updateSku(@PathVariable @org.springframework.lang.NonNull String id, @Valid @RequestBody SkuRequest request) {
        return catalogService.updateSku(id, request);
    }

    @GetMapping("/sites")
    public List<SiteResponse> listSites(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search
    ) {
        return catalogService.listSites(active, search);
    }

    @PostMapping("/sites")
    @PreAuthorize("hasAnyAuthority('ADMIN','OVERSEAS_ORDER')")
    public SiteResponse createSite(@Valid @RequestBody SiteRequest request) {
        return catalogService.createSite(request);
    }

    @PutMapping("/sites/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','OVERSEAS_ORDER')")
    public SiteResponse updateSite(@PathVariable @org.springframework.lang.NonNull String id, @Valid @RequestBody SiteRequest request) {
        return catalogService.updateSite(id, request);
    }

    @GetMapping("/sites/{id}/inventory")
    public List<InventoryResponse> getSiteInventory(@PathVariable @org.springframework.lang.NonNull String id) {
        return catalogService.getSiteInventory(id);
    }

    @PutMapping("/sites/{id}/inventory")
    @PreAuthorize("hasAnyAuthority('ADMIN','OVERSEAS_ORDER','WAREHOUSE')")
    public InventoryResponse upsertInventory(@PathVariable @org.springframework.lang.NonNull String id, @Valid @RequestBody InventoryRequest request) {
        return catalogService.upsertInventory(id, request);
    }
}
