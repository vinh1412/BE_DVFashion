/*
 * @ {#} InventoryMapper.java   1.0     13/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.response.InventoryResponse;
import vn.edu.iuh.fit.entities.*;
import vn.edu.iuh.fit.enums.Language;

import java.util.List;

/*
 * @description: Mapper class for converting Inventory entities to InventoryResponse DTOs
 * @author: Tran Hien Vinh
 * @date:   13/09/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
public class InventoryMapper {

    public InventoryResponse mapToInventoryResponse(Inventory inventory, Language language) {
        // Get size, variant, and product details
        Size size = inventory.getSize();
        ProductVariant variant = size.getProductVariant();
        Product product = variant.getProduct();

        // Get product name based on language
        String productName = getProductName(product, language);

        return new InventoryResponse(
                inventory.getId(),
                size.getId(),
                size.getSizeName(),
                productName,
                variant.getColor(),
                inventory.getQuantityInStock(),
                inventory.getReservedQuantity(),
                inventory.getAvailableQuantity(),
                inventory.getMinStockLevel(),
                inventory.getLastUpdated(),
                inventory.getAvailableQuantity() <= inventory.getMinStockLevel()
        );
    }

    public List<InventoryResponse> mapToInventoryResponseList(List<Inventory> inventories, Language language) {
        return inventories.stream()
                .map(inventory -> mapToInventoryResponse(inventory, language))
                .toList();
    }

    private String getProductName(Product product, Language language) {
        // Try to get name in requested language first
        String productName = product.getTranslations()
                .stream()
                .filter(translation -> translation.getLanguage() == language)
                .findFirst()
                .map(ProductTranslation::getName)
                .orElse(null);

        // If not found, fallback to Vietnamese
        if (productName == null) {
            productName = product.getTranslations()
                    .stream()
                    .filter(translation -> translation.getLanguage() == Language.VI)
                    .findFirst()
                    .map(ProductTranslation::getName)
                    .orElse("Unknown Product");
        }

        return productName;
    }
}
