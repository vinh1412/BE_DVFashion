/*
 * @ {#} RecommendationModelVersionService.java   1.0     26/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.request.CreateRecommendationModelVersionRequest;
import vn.edu.iuh.fit.dtos.response.RecommendationModelVersionResponse;

import java.util.List;

/*
 * @description: Service interface for managing recommendation model versions
 * @author: Tran Hien Vinh
 * @date:   26/10/2025
 * @version:    1.0
 */
public interface RecommendationModelVersionService {
    /*
     * Retrieves all recommendation model versions.
     *
     * @return a list of RecommendationModelVersionResponse
     */
    List<RecommendationModelVersionResponse> getAllVersions();

    /*
     * Activates a recommendation model version by its ID.
     *
     * @param id the ID of the model version to activate
     */
    void activateModel(Long id);

    /*
     * Retrieves the currently active recommendation model version.
     *
     * @return the active RecommendationModelVersionResponse
     */
    RecommendationModelVersionResponse getActiveModel();

    /*
     * Evaluates a new recommendation model version based on the provided request.
     *
     * @param request the CreateRecommendationModelVersionRequest containing model parameters
     *
     * @return the evaluated RecommendationModelVersionResponse
     */
    RecommendationModelVersionResponse evaluateModel(CreateRecommendationModelVersionRequest request);
}
