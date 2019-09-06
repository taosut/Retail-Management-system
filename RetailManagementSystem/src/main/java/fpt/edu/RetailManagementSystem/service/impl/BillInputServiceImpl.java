package fpt.edu.RetailManagementSystem.service.impl;

import fpt.edu.RetailManagementSystem.persistent.entity.BillInput;
import fpt.edu.RetailManagementSystem.persistent.entity.BillInputDetail;
import fpt.edu.RetailManagementSystem.persistent.repository.AccountRepository;
import fpt.edu.RetailManagementSystem.persistent.repository.BillInputDetailRepository;
import fpt.edu.RetailManagementSystem.persistent.repository.BillInputRepository;
import fpt.edu.RetailManagementSystem.persistent.repository.ProductRepository;
import fpt.edu.RetailManagementSystem.service.BillInputService;
import fpt.edu.RetailManagementSystem.service.dto.BillInputDTO;
import fpt.edu.RetailManagementSystem.service.dto.BillInputDetailDTO;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BillInputServiceImpl implements BillInputService {
    private final BillInputRepository billInputRepository;
    private final BillInputDetailRepository billInputDetailRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;

    public BillInputServiceImpl(BillInputRepository billInputRepository, BillInputDetailRepository billInputDetailRepository, ProductRepository productRepository, AccountRepository accountRepository) {
        this.billInputRepository = billInputRepository;
        this.billInputDetailRepository = billInputDetailRepository;
        this.productRepository = productRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public Boolean create(List<BillInputDetailDTO> billInputDetailDTOS, Integer accountID){
        float total = 0;
        for (BillInputDetailDTO b : billInputDetailDTOS) {
            float sum = productRepository.findByID(b.getProductID()).getPrice()*b.getQuantity();
            total += sum;
        }
        BillInput bill = new BillInput();
        bill.setTimeCreated(new Date());
        bill.setTotal(total);
        bill.setStatus(false);
        bill.setAccountID(accountID);
        billInputRepository.save(bill);
        for (BillInputDetailDTO b : billInputDetailDTOS) {
            int newQuantiTY = productRepository.findByID(b.getProductID()).getQuantity() + b.getQuantity();
            productRepository.updateQuantity(newQuantiTY, b.getProductID());
            BillInputDetail billDetail = new BillInputDetail();
            billDetail.setStatus(true);
            billDetail.setQuantity(b.getQuantity());
            billDetail.setProductID(b.getProductID());
            billDetail.setBillID(bill.getId());
            billInputDetailRepository.save(billDetail);
        }
        return true;
    }

    @Override
    public List<BillInputDTO> getAllBill(){
        List<BillInput> bills = billInputRepository.findAllByIsDelete();
        List<BillInputDTO> billDTOS = new ArrayList<>();
        ModelMapper modelMapper = new ModelMapper();
        for (BillInput b : bills ) {
            BillInputDTO billDTO = modelMapper.map(b, BillInputDTO.class);
            billDTOS.add(billDTO);
        }
        return billDTOS;
    }
    @Override
    public List<BillInputDetailDTO> getAllProductOfBill(Integer billID){
        List<BillInputDetail> billDetails = billInputDetailRepository.findAllByIsDelete(billID);
        List<BillInputDetailDTO> billDetailDTOS = new ArrayList<>();
        ModelMapper modelMapper = new ModelMapper();
        for (BillInputDetail b : billDetails ) {
            BillInputDetailDTO billDetailDTO = modelMapper.map(b, BillInputDetailDTO.class);
            billDetailDTO.setCode(productRepository.findByID(b.getProductID()).getCode());
            billDetailDTO.setPrice(productRepository.findByID(b.getProductID()).getPrice());
            billDetailDTO.setName(productRepository.findByID(b.getProductID()).getName());
            billDetailDTOS.add(billDetailDTO);
        }
        return billDetailDTOS;
    }

    @Override
    public Boolean updateStatus(Integer id){
        Optional.ofNullable(billInputRepository.findById(id)).orElseThrow(() ->new EntityNotFoundException());
        if(billInputRepository.findBillByID(id).getStatus())
            billInputRepository.deleteByID(id, false);
        else
            billInputRepository.deleteByID(id, true);
        return true;
    }
}