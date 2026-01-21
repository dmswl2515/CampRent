package com.rental.camprent.service;

import com.rental.camprent.domain.campingitem.CampingItem;
import com.rental.camprent.domain.campingitem.CampingItemRepository;
import com.rental.camprent.dto.request.CampingItemCreateRequest;
import com.rental.camprent.dto.request.CampingItemUpdateRequest;
import com.rental.camprent.dto.request.StatusUpdateRequest;
import com.rental.camprent.dto.request.StockUpdateRequest;
import com.rental.camprent.dto.response.CampingItemResponse;
import com.rental.camprent.exception.ItemNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor // Lombok - final 필드 생성자 주입
public class CampingItemService {

    private final CampingItemRepository campingItemRepository;

    public List<CampingItemResponse> findAll() {
        return campingItemRepository.findAll().stream()
                .map(CampingItemResponse::from)
                .toList();
    }

    public CampingItemResponse findById(Long id) {
        CampingItem entity = campingItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("장비를 찾을 수 없습니다. id: " + id));

        return CampingItemResponse.from(entity);
    }

    @Transactional
    public CampingItemResponse create(CampingItemCreateRequest request) {
        // DTO -> Entity
        CampingItem entity = CampingItem.builder()
                .name(request.getName())
                .category(request.getCategory())
                .model(request.getModel())
                .description(request.getDescription())
                .stockQuantity(request.getStockQuantity())
                .baseDailyRate(request.getBaseDailyRate())
                .status(request.getStatus())
                .build();

        CampingItem saved = campingItemRepository.save(entity);

        // Entity -> DTO
        return CampingItemResponse.from(saved);
    }

    @Transactional
    public CampingItemResponse update(Long id, CampingItemUpdateRequest request) {
        CampingItem entity = campingItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("장비를 찾을 수 없습니다. id: " + id));

        // 정보 수정
        entity.updateInfo(request.getName(), request.getModel(), request.getDescription(), request.getBaseDailyRate());

        return CampingItemResponse.from(entity);
    }

    @Transactional
    public CampingItemResponse increaseStock(Long id, StockUpdateRequest request) {
        CampingItem entity = campingItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("장비를 찾을 수 없습니다. id: " + id));

        entity.increaseStock(request.getQuantity());

        return CampingItemResponse.from(entity);
    }

    @Transactional
    public CampingItemResponse decreaseStock(Long id, StockUpdateRequest request) {
        CampingItem entity = campingItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("장비를 찾을 수 없습니다. id: " + id));

        entity.decreaseStock(request.getQuantity());

        return CampingItemResponse.from(entity);
    }

    @Transactional
    public CampingItemResponse updateStatus(Long id, StatusUpdateRequest request) {
        CampingItem entity = campingItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("장비를 찾을 수 없습니다. id: " + id));

        entity.updateStatus(request.getStatus());

        return CampingItemResponse.from(entity);
    }
}

