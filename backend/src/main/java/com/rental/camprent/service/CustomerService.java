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

    // 코드 설명좀 해줘. 개념설명하면서 코드 보여주기로 했었잖아. 잊지마, 잊을거 같으면 저번에 만든 지침서에 적어놔
    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream()
                .map(CustomerResponse::from)
                .toList();
    }

    public List<CustomerResponse> findByType(CustomerType type) {
        return customerRepository.findByType(type).stream()
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
        // 기업 고객은 사업자등록번호 필수
        if(request.getType() == CustomerType.BUSINESS &&
           (request.getBusinessNumber() == null ||
           request.getBusinessNumber().isBlank())) {
            throw new IllegalArgumentException("기업 고객은 사업자등록번호가 필수 입니다.");
            // IllegalArgumentException는 인수를 잘못전달해서 발생하는 오류 맞지? type이 일반 고객이어야하는데, 지금 기업고객이거나 비어있는 값이 들어가서 잘못된 인수전달? 내가 이해한게 맞아?
        }

        // DTO -> entity
        Customer customer = Customer.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .type(request.getType())
                .address(request.getAddress())
                .businessNumber(request.getBusinessNumber())
                .build();

        // 그 JPA 영속성 컨텍스트 기능중에 트렌젝션 안에서 변화가 일어나면 이를 감지해서 트렌젝션 끝날 때 쯤에 save()호출하지 않아도 update 해주는거 여기는 그런거 없는거야?
        // 그리고 지금 save를 정적펙토리메서드 안에서 호출했잖아? 그리까 밑에 코드를 두줄로 분리할 있는데 지금 return에서 하나로 합친거잖아? 실무에서 보통 이런식으로 해? 아니면 개취야?
        return CustomerResponse.from(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("존재하지 않는 고객입니다."));

        customer.updateInfo(request.getName(), request.getPhone(),
                            request.getPhone(), request.getAddress());

        return CustomerResponse.from(customer);
    }

    @Transactional
    public void delete(Long id) {
        if(!customerRepository.existsById(id)) {
            throw new ItemNotFoundException("존재하지 않는 고객입니다.");
        }
        customerRepository.deleteById(id); //delete랑 save는 repository에 메서드 정의 안해놔도 그냥 JPA가 알아서 해줘?
    }
}
