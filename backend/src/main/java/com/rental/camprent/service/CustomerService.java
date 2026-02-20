package com.rental.camprent.service;

import com.rental.camprent.domain.customer.Customer;
import com.rental.camprent.domain.customer.CustomerRepository;
import com.rental.camprent.domain.customer.CustomerType;
import com.rental.camprent.dto.request.CustomerCreateRequest;
import com.rental.camprent.dto.request.CustomerUpdateRequest;
import com.rental.camprent.dto.response.CustomerResponse;
import com.rental.camprent.exception.ItemNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {
    private final CustomerRepository customerRepository;

    public List<CustomerResponse> findAll() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
                .map(CustomerResponse::from)
                .toList();
    }

    public List<CustomerResponse> findByType(CustomerType type) {
        List<Customer> customers = customerRepository.findByType(type);
        return customers.stream()
                .map(CustomerResponse::from)
                .toList();
    }

    public CustomerResponse findById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("존재하지 않는 고객입니다."));
        return CustomerResponse.from(customer);
    }

    @Transactional
    public CustomerResponse create(CustomerCreateRequest request) {
        // TODO: 기업 고객 기능 확장 시 주석 해제
        // 현재는 개인 고객(INDIVIDUAL)만 사용 (당근 벤치마킹 - 동네 기반 개인 간 거래)
        // 향후 지역 축제/캠핑장 사업자/학교 단체 등 기업 고객 지원 시 활성화
//        if(request.getType() == CustomerType.BUSINESS &&
//           (request.getBusinessNumber() == null ||
//           request.getBusinessNumber().isBlank())) {
//            throw new IllegalArgumentException("기업 고객은 사업자등록번호가 필수입니다.");
//        }

        Customer customer = Customer.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .type(request.getType())
                .address(request.getAddress())
                .businessNumber(request.getBusinessNumber())
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        return CustomerResponse.from(savedCustomer);
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("존재하지 않는 고객입니다."));

        customer.updateInfo(request.getName(), request.getPhone(),
                            request.getEmail(), request.getAddress());

        return CustomerResponse.from(customer);
    }

    @Transactional
    public void delete(Long id) {
        if(!customerRepository.existsById(id)) {
            throw new ItemNotFoundException("존재하지 않는 고객입니다.");
        }
        customerRepository.deleteById(id);
    }
}
