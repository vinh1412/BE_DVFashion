/*
 * @ {#} ProductTranslationService.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.request.ProductRequest;
import vn.edu.iuh.fit.entities.Product;
import vn.edu.iuh.fit.enums.Language;

/*
 * @description: Service interface for handling product translations
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
public interface ProductTranslationService {
    /**
     * Creates translations for the given product based on the provided request data and input language.
     *
     * @param product   The product entity to create translations for.
     * @param request   The product request data containing translation information.
     * @param inputLang The language of the input data.
     */
    void createProductTranslations(Product product, ProductRequest request, Language inputLang);

    /**
     * Updates translations for the given product based on the provided request data and input language.
     *
     * @param product   The product entity to update translations for.
     * @param request   The product request data containing updated translation information.
     * @param inputLang The language of the input data.
     */
    void updateProductTranslations(Product product, ProductRequest request, Language inputLang);
}
